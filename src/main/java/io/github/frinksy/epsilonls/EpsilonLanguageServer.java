package io.github.frinksy.epsilonls;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DiagnosticRegistrationOptions;
import org.eclipse.lsp4j.DocumentFilter;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import com.google.gson.JsonElement;

public class EpsilonLanguageServer implements LanguageServer, LanguageClientAware {

    private TextDocumentService textService;
    private WorkspaceService workspaceService;
    private LanguageClient languageClient;
    private int maxNumberOfProblems = -1;

    public EpsilonLanguageServer() {
        textService = new EpsilonLanguageTextDocumentService(this);
        workspaceService = new EpsilonLanguageWorkspaceService();
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {

        Object initialisationOptions = params.getInitializationOptions();
        maxNumberOfProblems = getConfiguredMaxNumberOfProblems(initialisationOptions);

        final InitializeResult res = new InitializeResult(new ServerCapabilities());

        res.getCapabilities().setHoverProvider(Boolean.TRUE);
        res.getCapabilities().setDeclarationProvider(Boolean.TRUE);
        res.getCapabilities().setTextDocumentSync(TextDocumentSyncKind.Full);
        res.getCapabilities().setReferencesProvider(Boolean.TRUE);

        DiagnosticRegistrationOptions diagnosticRegistrationOptions = new DiagnosticRegistrationOptions();
        List<DocumentFilter> documentSelector = new ArrayList<>();
        documentSelector.add(new DocumentFilter("eol", "file", "*.eol"));
        diagnosticRegistrationOptions.setDocumentSelector(documentSelector);
        diagnosticRegistrationOptions.setWorkspaceDiagnostics(false);
        diagnosticRegistrationOptions.setInterFileDependencies(false);
        res.getCapabilities().setDiagnosticProvider(diagnosticRegistrationOptions);

        List<WorkspaceFolder> folders = params.getWorkspaceFolders();

        if (folders != null) {
            for (WorkspaceFolder folder : folders) {

                this.languageClient.logMessage(new MessageParams(MessageType.Info, folder.toString()));
                WorkspaceConfiguration.registerWorkspaceMetamodels(URI.create(folder.getUri()), this);

            }
        }
        return CompletableFuture.supplyAsync(() -> res);

    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.supplyAsync(() -> Boolean.TRUE);
    }

    @Override
    public void exit() {
        // TODO Auto-generated method stub
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return this.textService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return this.workspaceService;
    }

    @Override
    public void connect(LanguageClient client) {
        this.languageClient = client;
    }

    public LanguageClient getClient() {
        return this.languageClient;
    }

    /**
     * Get the maxNumberOfProblems
     * 
     * @return the maxNumberOfProblems, >=-1 (-1 being default/unlimited)
     */
    public int getMaxNumberOfProblems() {
        return maxNumberOfProblems;
    }

    /**
     * Get the maxNumberOfProblems from a deserialized JSON object
     * 
     * @param initialisationOptions
     * @return
     */
    public static int getConfiguredMaxNumberOfProblems(Object initialisationOptions) {

        if (!(initialisationOptions instanceof JsonElement)) {
            return -1;
        }

        JsonElement options = ((JsonElement) initialisationOptions);

        if (!(options.isJsonObject())) {
            return -1;
        }

        JsonElement numberElement = options.getAsJsonObject().get("maxNumberOfProblems");

        if (numberElement == null) {
            return -1;
        }

        try {
            return numberElement.getAsInt();
        } catch (ClassCastException | IllegalStateException e) {
            return -1;
        }

    }
}
