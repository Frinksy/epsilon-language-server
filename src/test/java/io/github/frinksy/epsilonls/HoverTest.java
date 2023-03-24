package io.github.frinksy.epsilonls;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

            assertEquals(expectedHoverText, actualMarkupContent.getValue(), message);

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

}
