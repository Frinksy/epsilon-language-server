package io.github.frinksy.epsilonls;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.epsilon.common.parse.problem.ParseProblem;
import org.eclipse.epsilon.emc.emf.EmfMetaModel;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
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

    public List<Diagnostic> generateDiagnostics(String text, URI textUri) {
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
                                            new Position(problem.getLine(), problem.getColumn()),
                                            new Position(problem.getLine(), problem.getColumn())),
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
     */
    public void ExecuteModule(String textUri, EolModule eolModule) {
        languageServer.getClient()
                .logMessage(new MessageParams(MessageType.Info, "About to execute file: " + textUri));
        try {
            // Get the model
            EmfModel mymodel = new EmfModel();
            mymodel.setMetamodelFile("/path/to/metamodel.emf");

            EmfMetaModel mymeta = new EmfMetaModel(
                    "file:///path/to/metamodel.emf");
            mymeta.loadModel();
            mymeta.close();

            // mymodel.setModelFile("/path/to/model.flexmi");

            // ResourceSet resourceSet = new ResourceSetImpl();
            // resourceSet.getResourceFactoryRegistry().
            // getExtensionToFactoryMap().put("flexmi",
            // new FlexmiResourceFactory());
            // Resource resource =
            // resourceSet.createResource(URI.createFileURI("/../acme.flexmi"));
            // resource.load(null);

            mymodel.setName("M");
            mymodel.setReadOnLoad(true);
            mymodel.setStoredOnDisposal(false);
            mymodel.load();
            eolModule.getContext().getModelRepository().addModel(mymodel);

            eolModule.execute();
        } catch (EolRuntimeException eolRuntimeException) {
            languageServer.getClient().logMessage(
                    new MessageParams(MessageType.Error, "Error running module: " + eolRuntimeException.getMessage()));
        }
    }

}