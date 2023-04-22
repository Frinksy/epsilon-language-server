package io.github.frinksy.epsilonls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DeclarationParams;
import org.eclipse.lsp4j.DefinitionParams;
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
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RelatedFullDocumentDiagnosticReport;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

public class EpsilonLanguageTextDocumentService implements TextDocumentService {

    private EpsilonLanguageServer languageServer;
    private Map<String, EpsilonDocument> documents = new HashMap<>();

    public EpsilonLanguageTextDocumentService(EpsilonLanguageServer languageServer) {
        this.languageServer = languageServer;

    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {

        // Check if the file is already in the map

        EpsilonDocument doc = null;

        if (this.documents.containsKey(params.getTextDocument().getUri())) {
            doc = this.documents.get(params.getTextDocument().getUri());
        } else {
            doc = new EolDocument(languageServer, params.getTextDocument().getUri());
            doc.setContents(params.getTextDocument().getText());
            doc.setFilename(params.getTextDocument().getUri());
            this.documents.put(doc.getFilename(), doc);
        }

        List<Diagnostic> diagnostics = ((EolDocument) doc).generateDiagnostics();

        if (languageServer.getMaxNumberOfProblems() >= 0) {
            diagnostics = diagnostics.subList(0, languageServer.getMaxNumberOfProblems());
        }

        this.languageServer.getClient().publishDiagnostics(
                new PublishDiagnosticsParams(params.getTextDocument().getUri(), diagnostics));

    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        languageServer.getClient()
                .logMessage(new MessageParams(MessageType.Info, "File changed: " + params.getTextDocument().getUri()));

        // Check if the file is already in the map, and insert it if needed
        String docUri = params.getTextDocument().getUri();
        EpsilonDocument doc = null;

        if (this.documents.containsKey(docUri)) {
            doc = this.documents.get(docUri);
        } else {
            doc = new EolDocument(languageServer, docUri);
            doc.setFilename(docUri);
            this.documents.put(docUri, doc);
        }
        doc.setContents(
                ((TextDocumentContentChangeEvent) params.getContentChanges().toArray()[0]).getText());

        if (doc instanceof DiagnosableDocument) {
            List<Diagnostic> diagnostics = ((DiagnosableDocument) doc).generateDiagnostics();

            if (languageServer.getMaxNumberOfProblems() >= 0) {
                diagnostics = diagnostics.subList(0, languageServer.getMaxNumberOfProblems());
            }

            languageServer.getClient().publishDiagnostics(new PublishDiagnosticsParams(docUri, diagnostics));
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
        MarkupContent contents = null;

        EolDocument doc = (EolDocument) this.documents.get(params.getTextDocument().getUri());

        if (doc != null) {
            contents = doc.getHoverContents(params);
        }

        if (contents == null) {
            return CompletableFuture.supplyAsync(() -> null);
        }
        hover.setContents(contents);

        return CompletableFuture.supplyAsync(() -> hover);
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> declaration(
            DeclarationParams params) {

        languageServer.getClient().logMessage(new MessageParams(MessageType.Info, "Got a goto declaration request."));
        List<Location> res = new ArrayList<>();

        EpsilonDocument doc = this.documents.get(params.getTextDocument().getUri());

        if (doc instanceof NavigatableDocument) {
            NavigatableDocument document = (NavigatableDocument) doc;
            Location declarationLocation = document.getDeclarationLocation(params.getTextDocument().getUri(),
                    params.getPosition());
            res.add(declarationLocation);
        }

        return CompletableFuture.supplyAsync(() -> Either.forLeft(res));

    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
            DefinitionParams params) {
        List<Location> res = new ArrayList<>();

        EpsilonDocument doc = this.documents.get(params.getTextDocument().getUri());

        if (doc instanceof NavigatableDocument) {
            NavigatableDocument document = (NavigatableDocument) doc;
            // Definition and declaration are just aliased here.
            Location definitionLocation = document.getDeclarationLocation(params.getTextDocument().getUri(),
                    params.getPosition());
            res.add(definitionLocation);
        }

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

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
        EpsilonDocument doc = this.documents.get(params.getTextDocument().getUri());

        List<Location> locations = new ArrayList<>();

        if (doc instanceof NavigatableDocument) {
            NavigatableDocument document = (NavigatableDocument) doc;
            locations.addAll(document.getReferences(params.getTextDocument().getUri(), params.getPosition()));
        }

        return CompletableFuture.supplyAsync(() -> locations);

    }
}
