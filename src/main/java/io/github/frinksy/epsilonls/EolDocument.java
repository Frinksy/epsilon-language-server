package io.github.frinksy.epsilonls;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.epsilon.common.module.ModuleMarker;
import org.eclipse.epsilon.common.parse.Region;
import org.eclipse.epsilon.common.parse.problem.ParseProblem;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.concurrent.EolModuleParallel;
import org.eclipse.epsilon.eol.dom.Statement;
import org.eclipse.epsilon.eol.dom.StatementBlock;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public class EolDocument extends EpsilonDocument implements DiagnosableDocument, NavigatableDocument {

    private EolModule eolModule;

    private EolStaticAnalyser analyser;
    private MessageDigest md;

    protected EolDocument(EpsilonLanguageServer languageServer) {
        super(languageServer);

        eolModule = new EolModule();
        analyser = new EolStaticAnalyser();
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // This should never happen!
            this.log(MessageType.Error, e.getMessage());
        }
    }

    private void parseProgram() {

        // Check that the file has changed since the last time it was modified.
        byte[] old_md5 = md.digest();
        md.update(contents.getBytes());

        if (Arrays.equals(old_md5, md.digest())) {
            // No new contents, we can return.
            return;
        }

        try {
            // We make a new EolModule because parsing things twice seem to break things.
            eolModule = new EolModule();
            
            if (eolModule.parse(contents)) {
                this.log(MessageType.Info, "Successfully parsed file: " + this.filename);
            }
        } catch (Exception e) {
            // This probably won't happen, as we're parsing contents of a string
            // rather than opening a file.
            this.log(MessageType.Error, e.getMessage());
        }
    }

    public List<Diagnostic> generateDiagnostics() {
        List<Diagnostic> diagnostics = new ArrayList<>();

        diagnostics.addAll(getParserDiagnostics());

        diagnostics.addAll(getStaticAnalysisDiagnostics());


        // Find if the file mentions a model that we can load

        // If there is a model, add it


        return diagnostics;

    }

    private List<Diagnostic> getParserDiagnostics() {
        // First make sure the file has been parsed.
        this.parseProgram();

        List<Diagnostic> diagnostics = new ArrayList<>();

        // Get the problems from the parser.

        for (ParseProblem problem : eolModule.getParseProblems()) {
            diagnostics.add(
                    new Diagnostic(
                            new Range(
                                    new Position(problem.getLine() - 1, problem.getColumn()),
                                    new Position(problem.getLine() - 1, problem.getColumn())),
                            problem.getReason(),
                            matchSeverity(problem.getSeverity()),
                            "EolParser"));
        }



        return diagnostics;
    }

    private List<Diagnostic> getStaticAnalysisDiagnostics() {

        List<Diagnostic> diagnostics = new ArrayList<>();
        List<ModuleMarker> markers = analyser.validate(this.eolModule);
        for (ModuleMarker marker : markers) {

            Region region = marker.getRegion();
            Position start = new Position(region.getStart().getLine() - 1, region.getStart().getColumn());
            Position end = new Position(region.getEnd().getLine() - 1, region.getEnd().getColumn());

            Diagnostic diag = new Diagnostic(new Range(start, end), marker.getMessage(),
                    getSeverity(marker.getSeverity()), "EolStaticAnalyser");
            diagnostics.add(diag);
        }

        return diagnostics;
    }


    public Location getDeclarationLocation(Position position) {
        
        StatementBlock mainBlock = this.eolModule.getMain();


        // Find the thing that matches the position.

        for (Statement statement: mainBlock.getStatements()) {

            Region reg = statement.getRegion();

            if (regionContainsPosition(reg, position)) {
                this.log(MessageType.Info, "Found a statement");
            }
            
        }


        return null;
    }

    private boolean regionContainsPosition(Region region, Position position) {


        return region.getStart().isBefore(new org.eclipse.epsilon.common.parse.Position(position.getLine(), position.getCharacter()))
        && region.getEnd().isAfter(new org.eclipse.epsilon.common.parse.Position(position.getLine(), position.getCharacter()));


        
    }

    private DiagnosticSeverity getSeverity(ModuleMarker.Severity severity) {
        switch (severity) {
            case Information:
                return DiagnosticSeverity.Information;
            case Warning:
                return DiagnosticSeverity.Warning;
            case Error:
                return DiagnosticSeverity.Error;
            default:
                return DiagnosticSeverity.Error;
        }
    }

    private DiagnosticSeverity matchSeverity(int severity) {
        switch (severity) {
            case ParseProblem.WARNING:
                return DiagnosticSeverity.Warning;
            case ParseProblem.ERROR:
                return DiagnosticSeverity.Error;
            default:
                return DiagnosticSeverity.Error;
        }
    }

}
