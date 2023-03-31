package io.github.frinksy.epsilonls;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

class LanguageServerTest {

    @Test
    void validInputNumberOfProblemsTest() {

        for (Integer i = -1; i < 999; i++) {
            Object object = new Gson().fromJson(
                    "{maxNumberOfProblems: " + i.toString() + "}", JsonElement.class);

            assertEquals(i, EpsilonLanguageServer.getConfiguredMaxNumberOfProblems(object),
                    "Returned maxNumberOfProblems should be "
                            + i.toString() + " for a well-formatted input.");
        }

    }

    @Test
    void missingNumberOfProblemsTest() {
        Object object = new Gson().fromJson("{some_other_field: 'zx80'}", JsonElement.class);

        assertEquals(-1, EpsilonLanguageServer.getConfiguredMaxNumberOfProblems(object),
                "Returned maxNumberOfProblems should be -1 for a missing input field.");
    }

    @Test
    void invalidInputTest() {
        Integer invalidTypeInput = 2;

        assertEquals(-1, EpsilonLanguageServer.getConfiguredMaxNumberOfProblems(invalidTypeInput),
                "Returned maxNumberOfProblems should be -1 for an invalid input object.");
    }

    @Test
    void nullInputTest() {

        assertEquals(-1, EpsilonLanguageServer.getConfiguredMaxNumberOfProblems(null),
                "Returned maxNumberOfProblems should be -1 for a null input.");

    }

}
