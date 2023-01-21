package io.github.frinksy.epsilonls;

import java.io.IOException;
// import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.emfatic.core.EmfaticResourceFactory;
import org.eclipse.epsilon.common.parse.problem.ParseProblem;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.emc.emf.EmfMetaModel;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.emc.emf.EmfUtil;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.flexmi.FlexmiResourceFactory;
import org.eclipse.lsp4j.Diagnostic;
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
                                            new Position(problem.getLine()-1, problem.getColumn()),
                                            new Position(problem.getLine()-1, problem.getColumn())),
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

        return diagnostics;

    }

    /**
     * Run a module with a metamodel, and get the resulting diagnostics.
     * 
     * @param textUri
     * @param eolModule
     * @throws IOException
     */
    public void     executeModule(String textUri, EolModule eolModule) throws IOException {
        languageServer.getClient()
                .logMessage(new MessageParams(MessageType.Info, "About to execute file: " + textUri));
        try {
            // Get the model
            EmfModel mymodel = new EmfModel();
            mymodel.setMetamodelFile("./metamodel.emf");

            ResourceSet resourceSet = new ResourceSetImpl();
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("emf",
                    new EmfaticResourceFactory ());
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
    }

    public void parseMetamodel(String metamodelUri) {


        // ResourceSet resourceSet = new ResourceSetImpl(); 
		// Resource resource1 = resourceSet.getResource(URI.createURI(metamodelUri), true);
		// EPackage inputPackage = (EPackage)resource1.getContents().get(0);


        // EmfMetaModel myMetaModel = new EmfMetaModel(metamodelUri);

        EmfModel mymodel = new EmfModel();

        EmfMetaModel myMetaModel = new EmfMetaModel();
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("emf",
                new EmfaticResourceFactory ());
        Resource resource = resourceSet.createResource(
                URI.createFileURI("./metamodel.emf"));
        try {
            resource.load(null);

            myMetaModel.setResource(resource);

            languageServer.getClient().logMessage(new MessageParams(MessageType.Info, myMetaModel.allContents().toString()));
            
        } catch (IOException e1) {
            languageServer.getClient().logMessage(new MessageParams(MessageType.Error, "Error in emfatic factory for: " + metamodelUri));
        }
        final StringProperties properties = new StringProperties();

        properties.put(EmfModel.PROPERTY_FILE_BASED_METAMODEL_URI, URI.createURI(metamodelUri));


        try {
            mymodel.load(properties);
        } catch (EolModelLoadingException e) {
            // TODO Auto-generated catch block
            languageServer.getClient().logMessage(new MessageParams(MessageType.Error, "Error loading metamodel: " + e.getMessage()));
        }

        // try {
        //     myMetaModel.load();
        // } catch (EolModelLoadingException e) {
        //     languageServer.getClient()
        //             .logMessage(new MessageParams(MessageType.Error, "Error loading metamodel: " + e.getMessage()));
        // } finally {
        //     myMetaModel.close();
        // }

    }

}