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

    EolDocument document, document2;

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
        Path resourcesPath = Paths.get(".", "src", "test", "resources");
        Path testFilePath = resourcesPath.resolve("pens.eol");
        Path testFilePath2 = resourcesPath.resolve("random_program.eol");

        document = new EolDocument(new MockedLanguageServer(), testFilePath.toUri().toString());
        document.setContents(Files.readString(testFilePath));

        document2 = new EolDocument(new MockedLanguageServer(), testFilePath2.toUri().toString());
        document2.setContents(Files.readString(testFilePath2));
    }

    void testDeclarationLocation(EolDocument doc, Location expectedLocation, int colStart, int colEnd, int line,
            String message) {

        for (int col = colStart; col <= colEnd; col++) {
            Position gotoDeclarationInvocationPosition = new Position(line, col);

            Location actualLocation = doc.getDeclarationLocation(doc.getFilename(),
                    gotoDeclarationInvocationPosition);

            assertEquals(expectedLocation, actualLocation, message);

        }

    }

    @Test
    @DisplayName("Test getting operation definition from call.")
    void penDefinitionTest() {
        // Find the declaration of getFullName
        // from use line 10 Col 7 - 17 (inclusive)
        // Declaration is at line 13 col 18-28 (inclusive)
        Location expectedLocation = new Location(
                document.getFilename(),
                new Range(
                        new Position(12, 17),
                        new Position(12, 27)));

        testDeclarationLocation(document, expectedLocation, 6, 16, 9, null);
    }

    @Test
    @DisplayName("Test getting operation definition from definition itself.")
    void penDefinitionfromDefinitionTest() {
        // Find the declaration of getFullName
        // from its definition at line 11 col 18-28 inclusive

        Location expectedLocation = new Location(
                document.getFilename(),
                new Range(
                        new Position(12, 17),
                        new Position(12, 27)));

        testDeclarationLocation(document, expectedLocation, 17, 26, 12, null);
    }

    @Test
    void parameterVariableDefintionTest() {
        // Find the declaration of other in the greet operation
        // from its use on lines 40 and 43

        Location expectedLocation = new Location(
                document2.getFilename(),
                new Range(
                        new Position(17, 23),
                        new Position(17, 27)));

        // "other" in an access to one of its attributes
        testDeclarationLocation(document2, expectedLocation, 36, 40, 39, null);
        
        // "other" in an call to getFullName on it.
        testDeclarationLocation(document2, expectedLocation, 46, 50, 42, null);
    }

}
