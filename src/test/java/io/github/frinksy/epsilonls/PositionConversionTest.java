package io.github.frinksy.epsilonls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Paths;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.common.parse.Region;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Test;

public class PositionConversionTest {

    /*
     * Epsilon: a region is start inclusive, end exclusive, lines are 1-based,
     * columns are 0-based
     * LSP4J: a region (Range) is start inclusive, end exclusive, lines are 0-based,
     * columns are 0-based
     */

    @Test
    void testPositionConversion() throws Exception {
        File file = Paths.get(".", "src", "test", "resources", "standalone.eol").toFile();

        EolModule module = new EolModule();
        module.parse(file);

        org.eclipse.lsp4j.Position incomingPosition = new Position(0, 4);

        ModuleElement resolvedModule = EolDocument.getModuleElementAtPosition(module, incomingPosition);

        assertTrue(resolvedModule instanceof NameExpression);

        NameExpression nameExpression = (NameExpression) resolvedModule;
        assertEquals("p", nameExpression.getName());

    }

    @Test
    void testRangeFromRegion() {

        Range expectedRange = new Range();
        expectedRange.setStart(new Position(0, 0));
        expectedRange.setEnd(new Position(0, 15));

        Region startRegion = new Region();
        startRegion.setStart(new org.eclipse.epsilon.common.parse.Position(1, 0));
        startRegion.setEnd(new org.eclipse.epsilon.common.parse.Position(1, 15));

        assertEquals(expectedRange, Util.getRangeFromRegion(startRegion));

    }

    @Test
    void testLspPositionToEpsilonPosition() {

        org.eclipse.lsp4j.Position startPosition = new Position(10, 20);

        org.eclipse.epsilon.common.parse.Position expectedPosition = new org.eclipse.epsilon.common.parse.Position(11,
                20);

        assertEquals(expectedPosition, Util.convertPosition(startPosition));
    }

    @Test
    void testZeroLspPositionToEpsilonPosition() {

        org.eclipse.lsp4j.Position startPosition = new Position(0, 0);

        org.eclipse.epsilon.common.parse.Position expectedPosition = new org.eclipse.epsilon.common.parse.Position(1,
                0);

        assertEquals(expectedPosition, Util.convertPosition(startPosition));
    }

    @Test
    void testpsilonPositionToLspPosition() {

        org.eclipse.epsilon.common.parse.Position startPosition = new org.eclipse.epsilon.common.parse.Position(11, 20);

        org.eclipse.lsp4j.Position expectedPosition = new Position(10, 20);

        assertEquals(expectedPosition, Util.convertPosition(startPosition));
    }

    @Test
    void testZeroEpsilonPositionToLspPosition() {

        org.eclipse.epsilon.common.parse.Position startPosition = new org.eclipse.epsilon.common.parse.Position(1, 0);

        org.eclipse.lsp4j.Position expectedPosition = new Position(0, 0);

        assertEquals(expectedPosition, Util.convertPosition(startPosition));
    }

}
