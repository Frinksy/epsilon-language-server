package io.github.frinksy.epsilonls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.epsilon.emc.emf.EmfUtil;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HoverTest {

    EolDocument document, document2;

    @BeforeEach
    void registerMetamodels() throws Exception {
        EPackage.Registry.INSTANCE.clear();
        EPackage.Registry.INSTANCE.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
        EPackage.Registry.INSTANCE.put(XMLTypePackage.eNS_PREFIX, XMLTypePackage.eINSTANCE);
        EmfUtil.register(Paths.get(".", "src", "test", "resources", "pencilcase.ecore").toFile(),
                EPackage.Registry.INSTANCE);
    }

    @BeforeEach
    void setFileContents() throws IOException {
        Path resourcesPath = Paths.get(".", "src", "test", "resources");
        Path testFilePath = resourcesPath.resolve("pens.eol");
        Path testFilePath2 = resourcesPath.resolve("random_program.eol");

        document = new EolDocument(new MockedLanguageServer(), testFilePath.toUri().toString());
        document.setContents(Files.readString(testFilePath));

        document2 = new EolDocument(new MockedLanguageServer(), testFilePath2.toUri().toString());
        document2.setContents(Files.readString(testFilePath2));

    }

    void testHoverText(EolDocument doc, String expectedHoverText, int colStart, int colEnd, int line, String message) {

        for (int col = colStart; col <= colEnd; col++) {
            Position hoveredPosition = new Position(line, col);

            MarkupContent actualMarkupContent = doc.getHoverContents(
                    new HoverParams(
                            new TextDocumentIdentifier(doc.getFilename()), hoveredPosition));
            if (expectedHoverText != null) {
                assertNotNull(actualMarkupContent);
                assertEquals(expectedHoverText, actualMarkupContent.getValue(), message);
            } else {
                assertNull(actualMarkupContent);
            }

        }

    }

    @Test
    void operationCallHoverTest() {

        String expectedHoverText = "getFullName() -> String";
        // call of getFullName on p at
        // line 10 (-1)
        // cols 6 - 16
        testHoverText(document, expectedHoverText, 6, 16, 9, null);
    }

    @Test
    void operationDefinitionHoverTest() {

        String expectedHoverText = "getFullName() -> String";

        // operation definition at
        // line 13 (-1)
        // col 17 - 27
        testHoverText(document, expectedHoverText, 17, 27, 12, null);
    }

    @Test
    void forLoopParameterHoverTest() {

        String expectedHoverText = "p : Person";

        // In the for loop declaration
        testHoverText(document2, expectedHoverText, 5, 5, 6, null);

        // Use within the for loop
        testHoverText(document2, expectedHoverText, 4, 4, 8, null);

    }

    @Test
    void operationParameterHoverTest() {

        String expectedHoverText = "other : Person";

        // Parameter definition
        testHoverText(document2, expectedHoverText, 23, 27, 17, null);

        // Use in the definition of the "combined" variable
        testHoverText(document2, expectedHoverText, 36, 40, 39, null);

        // Use in the return statement
        testHoverText(document2, expectedHoverText, 46, 50, 42, null);

    }

    @Test
    void variableInOperationHoverTest() {

        String expectedHoverText = "combined : Any";

        // Variable declaration
        testHoverText(document2, expectedHoverText, 8, 15, 39, null);

        // Use in the declaration of the "some_other_variable" variable
        testHoverText(document2, expectedHoverText, 30, 37, 40, null);

    }

    @Test
    void variableInGlobalScopeHoverTest() {

        String expectedHoverText = "other : String";

        // Variable declaration
        testHoverText(document2, expectedHoverText, 4, 8, 51, null);

        // Use on the next line.
        testHoverText(document2, expectedHoverText, 0, 4, 52, null);

    }

    @Test
    void variableInGlobalScopeHoverTest2() {

        String expectedHoverText = "thispersoninparticular : Person";

        testHoverText(document2, expectedHoverText, 4, 25, 49, null);

    }

}
