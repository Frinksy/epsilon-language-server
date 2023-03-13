package io.github.frinksy.epsilonls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.lsp4j.Position;
import org.junit.jupiter.api.Test;

public class PositionConversionTest {
   
    @Test
    void testPositionConversion() throws Exception {
        File file = Paths.get(".", "src", "test", "resources", "standalone.eol").toFile();
        
        EolModule module = new EolModule();
        module.parse(file);


        org.eclipse.lsp4j.Position incomingPosition = new Position(0, 4);

        org.eclipse.epsilon.common.parse.Position epsilonPosition = null;
        
        
        ModuleElement resolvedModule = EolDocument.getModuleElementAtPosition(module, incomingPosition);


        assertTrue(resolvedModule instanceof NameExpression);

        NameExpression nameExpression = (NameExpression) resolvedModule;
        assertEquals("p", nameExpression.getName());

        
    }

}
