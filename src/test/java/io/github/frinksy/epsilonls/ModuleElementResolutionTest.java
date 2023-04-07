package io.github.frinksy.epsilonls;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.common.parse.Region;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.TypeExpression;
import org.eclipse.lsp4j.Position;
import org.junit.jupiter.api.Test;

class ModuleElementResolutionTest extends TestTemplate {

    void testModuleResolution(
            EolDocument doc, Class<?> expectedClass, Region expectedRegion, String expectedName, int colStart,
            int colEnd,
            int line, String message) throws Exception {

        EolModule eolModule = new EolModule();
        eolModule.setUri(new URI(doc.getFilename()));
        eolModule.parse(doc.getContents());

        for (int col = colStart; col <= colEnd; col++) {

            Position hoveredPosition = new Position(line, col);

            ModuleElement actualResolvedModuleElement = EolDocument.getModuleElementAtPosition(eolModule,
                    hoveredPosition);

            assertEquals(expectedRegion, actualResolvedModuleElement.getRegion());

            assertEquals(expectedClass, actualResolvedModuleElement.getClass());

            if (expectedClass.equals(NameExpression.class)) {
                NameExpression nameExpression = ((NameExpression) actualResolvedModuleElement);
                assertEquals(expectedName, nameExpression.getName());
            }

        }

    }

    @Test
    void testSelfParameterInsideOperationCall() throws Exception {

        Region expectedRegion = new Region(14, 15, 14, 19);

        testModuleResolution(document, NameExpression.class, expectedRegion, "self", 15, 19, 13, null);

    }

    @Test
    void testForLoopParameterName() throws Exception {

        Region expectedRegion = new Region(8, 5, 8, 6);

        testModuleResolution(document, NameExpression.class, expectedRegion, "p", 5, 5, 7, null);

    }

    @Test
    void testPropertyCallExpression() throws Exception {

        Region expectedRegion = new Region(15, 17, 15, 26);

        testModuleResolution(document, NameExpression.class, expectedRegion, "firstName", 17, 26, 14, null);

    }

    @Test
    void testPropertyCallExpression2() throws Exception {

        Region expectedRegion = new Region(15, 40, 15, 48);

        testModuleResolution(document, NameExpression.class, expectedRegion, "lastName",
                40, 48, 14, null);

    }

    @Test
    void testOperationCall() throws Exception {

        Region expectedRegion = new Region(10, 6, 10, 19);

        testModuleResolution(document, NameExpression.class, expectedRegion, "getFullName",
                6, 19, 9, null);

    }

    @Test
    void testVarNameInVarDefinition() throws Exception {

        Region expectedRegion = new Region(22, 12, 22, 17);

        testModuleResolution(document2, NameExpression.class, expectedRegion, "other",
                12, 17, 21, null);

    }

    @Test
    void testVarNameInVarAssignment() throws Exception {

        Region expectedRegion = new Region(24, 8, 24, 13);

        testModuleResolution(document2, NameExpression.class, expectedRegion, "other",
                8, 13, 23, null);

    }

    @Test
    void testOperationDefinitionParameterName() throws Exception {

        Region expectedRegion = new Region(18, 23, 18, 28);

        testModuleResolution(document2, NameExpression.class, expectedRegion, "other",
                23, 28, 17, null);

    }

    @Test
    void testOperationDefinitionTargetType() throws Exception {

        Region expectedRegion = new Region(12, 10, 12, 16);

        testModuleResolution(document2, TypeExpression.class, expectedRegion, "Person",
                10, 16, 11, null);

    }

    @Test
    void testOperationParameterTypeAnnotation() throws Exception {

        Region expectedRegion = new Region(18, 30, 18, 36);

        testModuleResolution(document2, TypeExpression.class, expectedRegion, "Person",
                30, 36, 17, null);

    }

    @Test
    void testVariableDeclarationTypeAnnotation() throws Exception {

        Region expectedRegion = new Region(22, 19, 22, 25);

        testModuleResolution(document2, TypeExpression.class, expectedRegion, "String",
                19, 25, 21, null);

    }

}
