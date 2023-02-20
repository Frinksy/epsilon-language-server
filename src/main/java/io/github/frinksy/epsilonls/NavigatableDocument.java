package io.github.frinksy.epsilonls;

import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;

public interface NavigatableDocument {

    public Location getDeclarationLocation(String uri, Position position);

    public List<Location> getReferences(String uri, Position position);

}
