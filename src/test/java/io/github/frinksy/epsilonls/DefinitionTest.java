package io.github.frinksy.epsilonls;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

class DefinitionTest extends TestTemplate {

    void testDeclarationLocation(EolDocument doc, Location expectedLocation, int colStart, int colEnd, int line,
            String message) {

        for (int col = colStart; col <= colEnd; col++) {
            Position gotoDeclarationInvocationPosition = new Position(line, col);

            Location actualLocation = doc.getDeclarationLocation(doc.getFilename(),
                    gotoDeclarationInvocationPosition);

            String error_message = message + "\nFinding declaration from line " + line + " and col " + col
                    + " did not yield expected result.";

            assertEquals(expectedLocation, actualLocation, error_message);

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
                        new Position(12, 28)));

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
                        new Position(12, 28)));

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
                        new Position(17, 28)));

        // "other" in an access to one of its attributes
        testDeclarationLocation(document2, expectedLocation, 36, 40, 39, null);

        // "other" in an call to getFullName on it.
        testDeclarationLocation(document2, expectedLocation, 46, 50, 42, null);
    }

    @Test
    void shadowedWhileVariableDefinitionTest() {

        // Find the shadowing declaration of "other" in the while loop in
        // the greet operation

        Location expectedLocation = new Location(
                document2.getFilename(),
                new Range(
                        new Position(21, 12),
                        new Position(21, 17)));

        // "other" from the definition in that scope
        testDeclarationLocation(document2, expectedLocation, 12, 16, 21, null);

        // "other" from the reassignment
        testDeclarationLocation(document2, expectedLocation, 8, 12, 23, null);

    }

    @Test
    void shadowedIfVariableDefinitionTest() {

        // Find the shadowing declaration of "other" in the if conditional
        // branch in the greet operation

        Location expectedLocation = new Location(
                document2.getFilename(),
                new Range(
                        new Position(30, 12),
                        new Position(30, 17)));

        // "other" from the definition in that scope
        testDeclarationLocation(document2, expectedLocation, 12, 16, 30, null);

        // "other" from the reassignment in the same scope
        testDeclarationLocation(document2, expectedLocation, 8, 12, 32, null);

        // "other" from the conditional branch in the scope
        testDeclarationLocation(document2, expectedLocation, 12, 16, 35, null);
    }

}
