package io.github.frinksy.epsilonls;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;

public interface NavigatableDocument {
    
    public Location getDeclarationLocation(String uri, Position position);


}
