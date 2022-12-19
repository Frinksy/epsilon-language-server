package io.github.frinksy.epsilonls;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.epsilon.common.parse.problem.ParseProblem;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.lsp4j.DeclarationParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
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
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

public class EpsilonLanguageTextDocumentService implements TextDocumentService {

    private EpsilonLanguageServer languageServer;

    private Map<TextDocumentItem, List<ParseProblem>> diagnosticsMap;


    public EpsilonLanguageTextDocumentService(EpsilonLanguageServer languageServer) {
        this.languageServer = languageServer;

        this.diagnosticsMap = new HashMap<TextDocumentItem, List<ParseProblem>>();

    }


    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        // Let's try to parse the file when it is opened.
        languageServer.getClient().logMessage(new MessageParams(MessageType.Info, "About to parse a file."));
        EolModule eolModule = new EolModule();
        URI textUri = null;
        try {
            textUri = new URI(params.getTextDocument().getUri());
        } catch (URISyntaxException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            if (eolModule.parse(textUri)) {
                languageServer.getClient().logMessage(new MessageParams(MessageType.Info, "Successfully parsed file: " + textUri));
                languageServer.getClient().publishDiagnostics(new PublishDiagnosticsParams(params.getTextDocument().getUri(), new ArrayList<Diagnostic>()));
            } else {
                eolModule.execute();
                List<ParseProblem> parseProblems = eolModule.getParseProblems();
                this.diagnosticsMap.put(params.getTextDocument(), parseProblems);
                List<Diagnostic> diagnostics = new ArrayList<>();
                for (ParseProblem problem: parseProblems) {
                    languageServer.getClient().logMessage(new MessageParams(MessageType.Error, problem.toString()));
                    diagnostics.add(
                        new Diagnostic(
                            new Range(
                                new Position(problem.getLine(), problem.getColumn()),
                                new Position(problem.getLine(), problem.getColumn())),
                                problem.getReason()
                            )
                    );
                }


                this.languageServer.getClient().publishDiagnostics(new PublishDiagnosticsParams(params.getTextDocument().getUri(), diagnostics));

                // throw new Exception();
            }

        } catch (EolRuntimeException eolRuntimeException) {

        }
        catch (Exception e) {
            languageServer.getClient().logMessage(new MessageParams(MessageType.Error, "Could not parse file: " + textUri));
        }

        
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        
        // Update server's text document version
        List<TextDocumentContentChangeEvent> changes = params.getContentChanges();
        String docUri = params.getTextDocument().getUri();
        
        for (TextDocumentContentChangeEvent change: changes) {
            List<Diagnostic> diagnostics = generateDiagnostics(change.getText(),null);
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
        MarkupContent contents = new MarkupContent(MarkupKind.PLAINTEXT, "This is a test hover text");
        hover.setContents(contents);
    


        return CompletableFuture.supplyAsync(() -> hover);
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> declaration(DeclarationParams params) {
     
        languageServer.getClient().logMessage(new MessageParams(MessageType.Info, "Got a goto declaration request."));

        Location new_location = new Location();
        new_location.setRange(
            new Range(new Position(params.getPosition().getLine() + 1, params.getPosition().getCharacter()),
            new Position(params.getPosition().getLine() + 1, params.getPosition().getCharacter()))
        );
        new_location.setUri(params.getTextDocument().getUri());
    
        List<Location> res = new ArrayList<Location>();
        res.add(new_location);


        return CompletableFuture.supplyAsync(() -> Either.forLeft(res));

    }

    @Override
    public CompletableFuture<DocumentDiagnosticReport> diagnostic(DocumentDiagnosticParams params) {
        this.languageServer.getClient().logMessage(new MessageParams(MessageType.Info, "Going to generate diagnostics."));
        URI documentUri = null;

        List<Diagnostic> diagnostics = new ArrayList<>();
        
        try {
            documentUri = new URI(params.getTextDocument().getUri());
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        if (documentUri!=null) {
            List<ParseProblem> parseProblems = diagnosticsMap.get(documentUri);


            for (ParseProblem problem: parseProblems) {
                this.languageServer.getClient().logMessage(new MessageParams(MessageType.Info, "Parse problem added to the diagnostics"));
                Diagnostic diagnostic = new Diagnostic(
                    new Range(
                        new Position(problem.getLine(), problem.getColumn()),
                        new Position(problem.getLine(), problem.getColumn())
                    ),
                    problem.getReason()
                );

                DiagnosticSeverity severity = DiagnosticSeverity.forValue(problem.getSeverity());
                diagnostic.setSeverity(severity);
                diagnostics.add(diagnostic);
            }

        }

        DocumentDiagnosticReport report = new DocumentDiagnosticReport(new RelatedFullDocumentDiagnosticReport(diagnostics));

        return CompletableFuture.supplyAsync(() -> report);
        
        
    }

    private List<Diagnostic> generateDiagnostics(String text, URI textUri) {
        languageServer.getClient().logMessage(new MessageParams(MessageType.Info, "About to parse a file."));
        EolModule eolModule = new EolModule();
        List<Diagnostic> diagnostics = new ArrayList<>();
        try {
            if (eolModule.parse(text, null)) {
                languageServer.getClient().logMessage(new MessageParams(MessageType.Info, "Successfully parsed file: " + textUri));
                return new ArrayList<Diagnostic>();
            } else {
                eolModule.execute();
                List<ParseProblem> parseProblems = eolModule.getParseProblems();

                
                for (ParseProblem problem: parseProblems) {
                    languageServer.getClient().logMessage(new MessageParams(MessageType.Error, problem.toString()));
                    diagnostics.add(
                        new Diagnostic(
                            new Range(
                                new Position(problem.getLine(), problem.getColumn()),
                                new Position(problem.getLine(), problem.getColumn())),
                                problem.getReason()
                            )
                    );
                }


                

            }

        } catch (EolRuntimeException eolRuntimeException) {

        }
        catch (Exception e) {
            languageServer.getClient().logMessage(new MessageParams(MessageType.Error, "Could not parse file: " + textUri));
        }


        return diagnostics;
    }


}
