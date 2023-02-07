package io.github.frinksy.epsilonls;

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

public class EpsilonLanguageServer implements LanguageServer, LanguageClientAware{


    private TextDocumentService textService;
    private WorkspaceService workspaceService;
    private LanguageClient languageClient;

    public EpsilonLanguageServer() {
        textService = new EpsilonLanguageTextDocumentService(this);
        workspaceService = new EpsilonLanguageWorkspaceService();
    }


    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        
        final InitializeResult res = new InitializeResult(new ServerCapabilities());

        res.getCapabilities().setHoverProvider(Boolean.TRUE);
        res.getCapabilities().setDeclarationProvider(Boolean.TRUE);
        res.getCapabilities().setTextDocumentSync(TextDocumentSyncKind.Full);

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

}
