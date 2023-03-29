package io.github.frinksy.epsilonls;

import java.io.File;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.common.module.ModuleMarker;
import org.eclipse.epsilon.common.parse.Region;
import org.eclipse.epsilon.common.parse.problem.ParseProblem;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.StatementBlock;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import io.github.frinksy.epsilonls.eol.EolDeclaration;
import io.github.frinksy.epsilonls.eol.EolHover;
import io.github.frinksy.epsilonls.eol.EolReferences;

public class EolDocument extends EpsilonDocument implements DiagnosableDocument, NavigatableDocument {

    private EolModule eolModule;

    private EolStaticAnalyser analyser;
    private MessageDigest md;

    protected EolDocument(EpsilonLanguageServer languageServer, String filename) {
        super(languageServer, filename);

        eolModule = new EolModule();
        analyser = new EolStaticAnalyser();
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // This should never happen!
            this.log(MessageType.Error, e.getMessage());
        }
    }

    @Override
    public void setContents(String contents) {
        super.setContents(contents);

        // Always parse the program on every update
        parseProgram();
        if (eolModule.getParseProblems().isEmpty()) {
            getStaticAnalysisDiagnostics();
        }

    }

    private void parseProgram() {

        // Check that the file has changed since the last time it was modified.
        byte[] oldMD5 = md.digest();
        md.update(this.getContents().getBytes());

        if (Arrays.equals(oldMD5, md.digest())) {
            // No new contents, we can return.
            return;
        }

        try {
            // We make a new EolModule because parsing things twice seem to break things.
            eolModule = new EolModule();

            if (eolModule.parse(this.getContents(), new File(URI.create(this.getFilename())))) {
                this.log(MessageType.Info, "Successfully parsed file: " + this.getFilename());
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

        if (!diagnostics.isEmpty()) {
            // Skip the static analysis if there are syntax errors.
            return diagnostics;
        }

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

            Diagnostic diag = new Diagnostic(Util.getRangeFromRegion(region), marker.getMessage(),
                    getSeverity(marker.getSeverity()), "EolStaticAnalyser");
            diagnostics.add(diag);
        }

        return diagnostics;
    }

    @Override
    public Location getDeclarationLocation(String uri, Position position) {
        return EolDeclaration.getDeclaration(position, eolModule, analyser);
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

    public MarkupContent getHoverContents(HoverParams params) {

        this.parseProgram();

        // We want to find the type that this is.

        Position pos = params.getPosition();
        ModuleElement resolvedModule = getModuleElementAtPosition(eolModule, pos);

        if (resolvedModule == null) {
            return null;
        }

        if (resolvedModule instanceof StatementBlock)
            return null;

        String contents = getHoverContentsForModule(resolvedModule);

        if (contents == null) {
            return null;
        }

        return new MarkupContent(MarkupKind.PLAINTEXT, contents);

    }

    private String getHoverContentsForModule(ModuleElement moduleElement) {

        if (moduleElement instanceof NameExpression) {

            // If the parent is an Operation

            ModuleElement parent = moduleElement.getParent();
            if (parent instanceof OperationCallExpression) {

                OperationCallExpression operationCall = (OperationCallExpression) parent;
                String operationName = operationCall.getName();
                String targetName = ((NameExpression) moduleElement).getName();

                if (operationName.equals(targetName)) {
                    // Find the operation in the static analyser
                    Operation operation = analyser.getExactMatchedOperation(operationCall);

                    if (operation != null) {
                        return EolHover.getHoverContents(operation);
                    } else {
                        return null;
                    }

                }
            }

            if (parent instanceof Operation) {
                Operation operation = (Operation) parent;

                return EolHover.getHoverContents(operation);
            }

            return EolHover.getNameExpressionHover((NameExpression) moduleElement);

        }

        return null;
    }

    public static ModuleElement getModuleElementAtPosition(ModuleElement module, Position pos) {

        if (!Util.regionContainsPosition(module.getRegion(), Util.convertPosition(pos))) {
            return null;
        }

        for (ModuleElement child : module.getChildren()) {
            ModuleElement resolved = getModuleElementAtPosition(child, pos);
            if (resolved != null) {
                return getModuleElementAtPosition(resolved, pos);
            }
        }

        return module;

    }

    @Override
    public List<Location> getReferences(String uri, Position position) {

        ModuleElement resolvedModule = getModuleElementAtPosition(eolModule, position);

        return EolReferences.getReferences(resolvedModule);
    }

}
