package io.github.frinksy.epsilonls;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.emfatic.core.EmfaticResourceFactory;
import org.eclipse.epsilon.common.module.ModuleMarker;
import org.eclipse.epsilon.common.parse.Region;
import org.eclipse.epsilon.common.parse.problem.ParseProblem;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.emc.emf.EmfMetaModel;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;
import org.eclipse.epsilon.flexmi.FlexmiResourceFactory;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public class EpsilonDiagnosticsService {

    private EpsilonLanguageServer languageServer;

    public EpsilonDiagnosticsService(EpsilonLanguageServer languageServer) {
        this.languageServer = languageServer;
    }

    public List<Diagnostic> generateDiagnostics(String text, java.net.URI textUri) {
        languageServer.getClient().logMessage(new MessageParams(MessageType.Info, "About to parse a file."));
        EolModule eolModule = new EolModule();
        List<Diagnostic> diagnostics = new ArrayList<>();
        try {
            if (eolModule.parse(text, null)) {
                languageServer.getClient()
                        .logMessage(new MessageParams(MessageType.Info, "Successfully parsed file: " + textUri));

            } else {

                List<ParseProblem> parseProblems = eolModule.getParseProblems();

                for (ParseProblem problem : parseProblems) {
                    languageServer.getClient().logMessage(new MessageParams(MessageType.Error, problem.toString()));
                    diagnostics.add(
                            new Diagnostic(
                                    new Range(
                                            new Position(problem.getLine() - 1, problem.getColumn()),
                                            new Position(problem.getLine() - 1, problem.getColumn())),
                                    problem.getReason()));
                }

            }
        } catch (EolModelLoadingException eolParseException) {
            languageServer.getClient().logMessage(
                    new MessageParams(MessageType.Error, "Error parsing model: " + eolParseException.getMessage()));

        } catch (Exception e) {
            languageServer.getClient()
                    .logMessage(new MessageParams(MessageType.Error, "Could not parse file: " + textUri));
        }

        diagnostics.addAll(runStaticAnalysis(eolModule));

        return diagnostics;

    }

    public List<Diagnostic> runStaticAnalysis(EolModule eolModule) {
        EolStaticAnalyser analyser = new EolStaticAnalyser();
        List<Diagnostic> diagnostics = new ArrayList<>();
        List<ModuleMarker> markers = analyser.validate(eolModule);
        for (ModuleMarker marker : markers) {
            languageServer.getClient().logMessage(new MessageParams(MessageType.Info, marker.getMessage()));

            Region region = marker.getRegion();
            Position start = new Position(region.getStart().getLine() - 1, region.getStart().getColumn());
            Position end = new Position(region.getEnd().getLine() - 1, region.getEnd().getColumn());

            Diagnostic diag = new Diagnostic(new Range(start, end), marker.getMessage());

            switch (marker.getSeverity()) {
                case Information:
                    diag.setSeverity(DiagnosticSeverity.Information);
                    break;

                case Warning:
                    diag.setSeverity(DiagnosticSeverity.Warning);
                    break;

                case Error:
                    diag.setSeverity(DiagnosticSeverity.Error);
                    break;

                default:
                    break;
            }

            diagnostics.add(diag);

        }


        return diagnostics;
    }

    /**
     * Run a module with a metamodel, and get the resulting diagnostics.
     * 
     * @param textUri
     * @param eolModule
     * @throws IOException
     */
    public void executeModule(String textUri, EolModule eolModule) throws IOException {
        languageServer.getClient()
                .logMessage(new MessageParams(MessageType.Info, "About to execute file: " + textUri));

        EmfModel mymodel = new EmfModel();
        try {
            // Get the model
            mymodel.setMetamodelFile("./metamodel.emf");

            ResourceSet resourceSet = new ResourceSetImpl();
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("emf",
                    new EmfaticResourceFactory());
            Resource resource = resourceSet.createResource(
                    URI.createFileURI("./metamodel.emf"));
            resource.load(null);
            ResourceSet resourceSet2 = new ResourceSetImpl();
            resourceSet2.getResourceFactoryRegistry().getExtensionToFactoryMap().put("flexmi",
                    new FlexmiResourceFactory());
            Resource resource2 = resourceSet2.createResource(
                    URI.createFileURI(".model.flexmi"));
            resource2.load(null);

            mymodel.setResource(resource);
            mymodel.setResource(resource2);
            mymodel.setName("M");
            mymodel.setReadOnLoad(true);
            mymodel.setStoredOnDisposal(false);
            mymodel.load();
            mymodel.validate();
            eolModule.getContext().getModelRepository().addModel(mymodel);

        } catch (EolRuntimeException eolRuntimeException) {
            languageServer.getClient().logMessage(
                    new MessageParams(MessageType.Error, "Error running module: " + eolRuntimeException.getMessage()));
        }
    
        mymodel.close();

    }

    public void parseMetamodel(String metamodelUri) {


        EmfModel mymodel = new EmfModel();

        EmfMetaModel myMetaModel = new EmfMetaModel();
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("emf",
                new EmfaticResourceFactory());
        Resource resource = resourceSet.createResource(
                URI.createFileURI("./metamodel.emf"));
        try {
            resource.load(null);

            myMetaModel.setResource(resource);

            languageServer.getClient()
                    .logMessage(new MessageParams(MessageType.Info, myMetaModel.allContents().toString()));

        } catch (IOException e1) {
            languageServer.getClient()
                    .logMessage(new MessageParams(MessageType.Error, "Error in emfatic factory for: " + metamodelUri));
        }
        final StringProperties properties = new StringProperties();

        properties.put(EmfModel.PROPERTY_FILE_BASED_METAMODEL_URI, URI.createURI(metamodelUri));

        try {
            mymodel.load(properties);
        } catch (EolModelLoadingException e) {
            // TODO Auto-generated catch block
            languageServer.getClient()
                    .logMessage(new MessageParams(MessageType.Error, "Error loading metamodel: " + e.getMessage()));
        }

        mymodel.close();
        myMetaModel.close();
    }

}