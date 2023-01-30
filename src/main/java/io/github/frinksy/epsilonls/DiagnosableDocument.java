package io.github.frinksy.epsilonls;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;

public interface DiagnosableDocument {
    
    public List<Diagnostic> generateDiagnostics();

}
