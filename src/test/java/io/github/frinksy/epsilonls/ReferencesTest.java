package io.github.frinksy.epsilonls;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Test;

class ReferencesTest extends TestTemplate {

    /**
     * Sort according to the start position of the range in the location
     */
    int sortFunction(Location left, Location right) {

        Range leftRange = left.getRange();
        Range rightRange = right.getRange();

        if (leftRange.getStart().getLine() != rightRange.getStart().getLine()) {
            return leftRange.getStart().getLine() - rightRange.getStart().getLine();
        }

        return leftRange.getStart().getCharacter() - rightRange.getStart().getCharacter();

    }

    void testReferences(EolDocument doc, int[][] expected_raw_coordinates, String message) {

        List<Location> expectedLocations = Arrays.stream(expected_raw_coordinates)
                .map((raw_coords) -> getLocationFromRawCoords(raw_coords, doc.getFilename()))
                .collect(Collectors.toList());
        expectedLocations.sort((left, right) -> sortFunction(left, right));
        for (int[] coords : expected_raw_coordinates) {

            int line = coords[0];
            int colStart = coords[1];
            int colEnd = coords[2];

            for (int col = colStart; col < colEnd; col++) {

                Position hoveredPosition = new Position(line, col);

                List<Location> actualLocations = doc.getReferences(doc.getFilename(), hoveredPosition);
                actualLocations.sort((left, right) -> sortFunction(left, right));

                assertEquals(expectedLocations, actualLocations, message);

            }

        }

    }

    Location getLocationFromRawCoords(int[] coords, String uri) {

        return new Location(uri,
                new Range(
                        new Position(coords[0], coords[1]),
                        new Position(coords[0], coords[2])));
    }

    @Test
    void testOperationArgumentReferences() {

        int[][] raw_coordinates = {
                { 17, 23, 28 },
                { 39, 36, 41 },
                { 42, 46, 51 }
        };

        testReferences(document2, raw_coordinates, null);

    }

    @Test
    void testShadowedWhileOperationArgumentReferences() {

        int[][] raw_coordinates = {
                { 21, 12, 17 },
                { 23, 8, 13 }
        };

        testReferences(document2, raw_coordinates, null);

    }

    @Test
    void testShadowedIfOperationArgumentReferences() {

        int[][] raw_coordinates = {
                { 30, 12, 17 },
                { 32, 8, 13 },
                { 35, 12, 17 }

        };

        testReferences(document2, raw_coordinates, null);

    }

    @Test
    void testForLoopParameterReferences() {

        int[][] raw_coordinates = {
                { 6, 5, 6 },
                { 8, 4, 5 }
        };

        testReferences(document2, raw_coordinates, null);

    }

    @Test
    void testVariableInOperationReferences() {

        int[][] raw_coordinates = {
                { 39, 8, 16 },
                { 40, 30, 38 }
        };

        testReferences(document2, raw_coordinates, null);

    }

}