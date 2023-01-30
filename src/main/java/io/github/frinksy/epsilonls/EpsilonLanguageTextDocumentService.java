package io.github.frinksy.epsilonls;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.lsp4j.DeclarationParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentDiagnosticParams;
import org.eclipse.lsp4j.DocumentDiagnosticReport;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RelatedFullDocumentDiagnosticReport;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

public class EpsilonLanguageTextDocumentService implements TextDocumentService {

    private EpsilonLanguageServer languageServer;
    private EpsilonDiagnosticsService diagnosticsService;

    public EpsilonLanguageTextDocumentService(EpsilonLanguageServer languageServer) {
        this.languageServer = languageServer;

        this.diagnosticsService = new EpsilonDiagnosticsService(languageServer);

    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {

        List<Diagnostic> diagnostics = diagnosticsService.generateDiagnostics(params.getTextDocument().getText(),
                URI.create(params.getTextDocument().getUri()));

        this.languageServer.getClient().publishDiagnostics(
                new PublishDiagnosticsParams(params.getTextDocument().getUri(), diagnostics));

    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        languageServer.getClient()
                .logMessage(new MessageParams(MessageType.Info, "File changed: " + params.getTextDocument().getUri()));
        // Update server's text document version
        List<TextDocumentContentChangeEvent> changes = params.getContentChanges();
        String docUri = params.getTextDocument().getUri();

        for (TextDocumentContentChangeEvent change : changes) {
            List<Diagnostic> diagnostics = diagnosticsService.generateDiagnostics(change.getText(), null);
            languageServer.getClient().publishDiagnostics(new PublishDiagnosticsParams(docUri, diagnostics));
        }

        EolModule eolModule = new EolModule();
        try {
            eolModule.parse(((TextDocumentContentChangeEvent) params.getContentChanges().toArray()[0]).getText(), null);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // diagnosticsService.parseMetamodel("file:///path/to/metamodel.emf");

        try {
            diagnosticsService.executeModule(docUri, eolModule);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        // TODO Auto-generated method stub

    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        languageServer.getClient().logMessage(new MessageParams(MessageType.Info, "Got a new hover request."));

        Hover hover = new Hover();
        MarkupContent contents = new MarkupContent(MarkupKind.PLAINTEXT, "This is a test hover text");
        hover.setContents(contents);

        return CompletableFuture.supplyAsync(() -> hover);
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> declaration(
            DeclarationParams params) {

        languageServer.getClient().logMessage(new MessageParams(MessageType.Info, "Got a goto declaration request."));

        Location new_location = new Location();
        new_location.setRange(
                new Range(new Position(params.getPosition().getLine() + 1, params.getPosition().getCharacter()),
                        new Position(params.getPosition().getLine() + 1, params.getPosition().getCharacter())));
        new_location.setUri(params.getTextDocument().getUri());

        List<Location> res = new ArrayList<Location>();
        res.add(new_location);

        return CompletableFuture.supplyAsync(() -> Either.forLeft(res));

    }

    @Override
    public CompletableFuture<DocumentDiagnosticReport> diagnostic(DocumentDiagnosticParams params) {
        this.languageServer.getClient()
                .logMessage(new MessageParams(MessageType.Info, "Going to generate diagnostics."));

        List<Diagnostic> diagnostics = new ArrayList<>();

        DocumentDiagnosticReport report = new DocumentDiagnosticReport(
                new RelatedFullDocumentDiagnosticReport(diagnostics));

        return CompletableFuture.supplyAsync(() -> report);

    }

}
