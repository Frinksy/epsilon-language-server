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

    EolDocument document;
    Path testFilePath = Paths.get(".", "src", "test", "resources", "pens.eol");

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

        document = new EolDocument(new MockedLanguageServer(), testFilePath.toUri().toString());
        document.setContents(Files.readString(testFilePath));
    }

    @Test
    void operationCallHoverTest() {

        String expectedHoverText = "getFullName() -> String";

        // call of getFullName on p at
        // line 10 (-1)
        // cols 6 - 16
        int line = 9;
        for (int col = 6; col < 17; col++) {
            Position checkedPosition = new Position(line, col);

            MarkupContent actualContent = document.getHoverContents(new HoverParams(
                    new TextDocumentIdentifier(testFilePath.toUri().toString()), checkedPosition));

            assertEquals(expectedHoverText, actualContent.getValue());

        }

    }

    @Test
    void operationDefinitionHoverTest() {

        String expectedHoverText = "getFullName() -> String";

        // operation definition at
        // line 13 (-1)
        // col 17 - 27
        int line = 12;
        for (int col = 17; col < 28; col++) {
            Position checkedPosition = new Position(line, col);

            MarkupContent actualContent = document.getHoverContents(
                    new HoverParams(new TextDocumentIdentifier(testFilePath.toUri().toString()), checkedPosition));

            assertEquals(expectedHoverText, actualContent.getValue());

        }

    }

}
