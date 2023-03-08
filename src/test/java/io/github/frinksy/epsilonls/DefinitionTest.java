package io.github.frinksy.epsilonls;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.epsilon.emc.emf.EmfUtil;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

class DefinitionTest {

    EolDocument document;

    @BeforeEach
    void registerMetamodels() throws Exception {
        EPackage.Registry.INSTANCE.clear();
        EPackage.Registry.INSTANCE.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
        EPackage.Registry.INSTANCE.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
        EmfUtil.register(Paths.get(".", "src", "test", "resources", "pencilcase.ecore").toFile(),
                EPackage.Registry.INSTANCE);

    }

    @BeforeEach
    void setFileContents() throws IOException {

        document = new EolDocument(new MockedLanguageServer());

        Path testFilePath = Paths.get(".", "src", "test", "resources", "pens.eol");

        document.setContents(Files.readString(testFilePath));
        document.setFilename(testFilePath.toUri().toString());

    }

    @Test
    @DisplayName("Test getting operation definition from call.")
    void penDefinitionTest() {
        // Find the declaration of getFullName
        // from use line 8 Col 7 - 17 (inclusive)
        // Declaration is at line 11 col 18-28 (inclusive)
        Location expectedLocation = new Location(
                document.getFilename(),
                new Range(
                        new Position(10, 17),
                        new Position(10, 28)));

        for (int col = 7; col < 18; col++) {

            Position operationCallPosition = new Position(7, col);

            Location actualLocation = document.getDeclarationLocation(document.getFilename(), operationCallPosition);

            assertEquals(expectedLocation, actualLocation);

        }
    }

    @Test
    @DisplayName("Test getting operation definition from definition itself.")
    void penDefinitionfromDefinitionTest() {
        // Find the declaration of getFullName
        // from its definition at line 11 col 18-28 inclusive

        Location expectedLocation = new Location(
                document.getFilename(),
                new Range(
                        new Position(10, 17),
                        new Position(10, 28)));

        for (int col = 18; col < 28; col++) {
            Position chosenOperationDeclarationPosition = new Position(10, col);
            Location actualLocation = document.getDeclarationLocation(document.getFilename(),
                    chosenOperationDeclarationPosition);

            assertEquals(expectedLocation, actualLocation);

        }
    }

}