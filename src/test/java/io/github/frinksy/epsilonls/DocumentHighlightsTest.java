package io.github.frinksy.epsilonls;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Test;

class DocumentHighlightsTest extends TestTemplate {

    /*
     * Sort according to the start position of the range in the location
     */
    int sortFunction(DocumentHighlight left, DocumentHighlight right) {

        Range leftRange = left.getRange();
        Range rightRange = right.getRange();

        if (leftRange.getStart().getLine() != rightRange.getStart().getLine()) {
            return leftRange.getStart().getLine() - rightRange.getStart().getLine();
        }

        return leftRange.getStart().getCharacter() - rightRange.getStart().getCharacter();

    }

    void testDocumentHighlights(Object[][] raw_coordinates, Object[][] expected_raw_coordinates, String message) {

        if (expected_raw_coordinates == null) {
            expected_raw_coordinates = raw_coordinates;
        }

        List<DocumentHighlight> expectedLocations = Arrays.stream(expected_raw_coordinates)
                .map((raw_coords) -> getDocumentHighlightFromRawCoords(raw_coords))
                .collect(Collectors.toList());
        expectedLocations.sort((left, right) -> sortFunction(left, right));
        for (Object[] coords : raw_coordinates) {

            int line = (int) coords[0];
            int colStart = (int) coords[1];
            int colEnd = (int) coords[2];
            EolDocument doc = (EolDocument) coords[3];

            for (int col = colStart; col < colEnd; col++) {

                Position hoveredPosition = new Position(line, col);

                List<DocumentHighlight> actualLocations = doc.getDocumentHighlights(hoveredPosition);
                actualLocations.sort((left, right) -> sortFunction(left, right));

                assertEquals(expectedLocations, actualLocations, message);

            }

        }

    }

    DocumentHighlight getDocumentHighlightFromRawCoords(Object[] coords) {

        return new DocumentHighlight(
                new Range(
                        new Position((int) coords[0], (int) coords[1]),
                        new Position((int) coords[0], (int) coords[2])));
    }

    @Test
    void testOperationArgumentDocumentHighlights() {

        Object[][] raw_coordinates = {
                { 17, 23, 28, document2 },
                { 39, 36, 41, document2 },
                { 42, 46, 51, document2 }
        };

        testDocumentHighlights(raw_coordinates, null, null);

    }

    @Test
    void testShadowedWhileOperationArgumentDocumentHighlights() {

        Object[][] raw_coordinates = {
                { 21, 12, 17, document2 },
                { 23, 8, 13, document2 }
        };

        testDocumentHighlights(raw_coordinates, null, null);

    }

    @Test
    void testShadowedIfOperationArgumentDocumentHighlights() {

        Object[][] raw_coordinates = {
                { 30, 12, 17, document2 },
                { 32, 8, 13, document2 },
                { 35, 12, 17, document2 }

        };

        testDocumentHighlights(raw_coordinates, null, null);

    }

    @Test
    void testForLoopParameterDocumentHighlights() {

        Object[][] raw_coordinates = {
                { 6, 5, 6, document2 },
                { 8, 4, 5, document2 }
        };

        testDocumentHighlights(raw_coordinates, null, null);

    }

    @Test
    void testVariableInOperationDocumentHighlights() {

        Object[][] raw_coordinates = {
                { 39, 8, 16, document2 },
                { 40, 30, 38, document2 }
        };

        testDocumentHighlights(raw_coordinates, null, null);

    }

    @Test
    void testOperationDocumentHighlights() {

        Object[][] raw_coordinates = {
                { 8, 6, 19, document2 },
                { 11, 17, 28, document2 },
                { 42, 17, 30, document2 },
                { 42, 52, 65, document2 }

        };

        testDocumentHighlights(raw_coordinates, null, null);

    }

    @Test
    void testOperationAcrossFilesDocumentHighlight() {

        Object[][] raw_coordinates = {
                { 13, 9, 15, document },
        };

        Object[][] expected_raw_coordinates = {
                { 13, 9, 20, document },
        };

        testDocumentHighlights(raw_coordinates, expected_raw_coordinates, null);

    }

    @Test
    void testOperationAcrossFilesDocumentHighlight2() {
        Object[][] raw_coordinates = {
                { 17, 17, 22, document2 }
        };

        testDocumentHighlights(raw_coordinates, raw_coordinates, null);
    }
}
