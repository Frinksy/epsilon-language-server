package io.github.frinksy.epsilonls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkspaceConfigurationTest {

    @BeforeEach
    public void resetState() {
        // EPackage Registry keeps state and loaded models between tests.
        // We don't want that.
        EPackage.Registry.INSTANCE.clear();
        EPackage.Registry.INSTANCE.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
        EPackage.Registry.INSTANCE.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
    }

    @Test
    void testRegisterEmptyWorkspaceMetamodels() throws URISyntaxException {

        EpsilonLanguageServer languageServer = new EpsilonLanguageServer() {
        };

        java.net.URI workspaceUri = Paths.get("./src/test/resources/empty_workspace").toUri();

        WorkspaceConfiguration.registerWorkspaceMetamodels(workspaceUri, languageServer);

        assertEquals(2,
                EPackage.Registry.INSTANCE.size(),
                "No nondefault models should be registered in an empty workspace.");

    }

    @Test
    void testRegisterWorkspaceMetamodels() throws URISyntaxException {

        EpsilonLanguageServer languageServer = new EpsilonLanguageServer() {
        };

        java.net.URI workspaceUri = Paths.get(".", "src", "test", "resources").toUri();

        WorkspaceConfiguration.registerWorkspaceMetamodels(workspaceUri, languageServer);

        // "ecore" and "type" models are always registered when loading Ecore models
        List<String> expectedMetamodelURIs = List.of(
                "pencilcase",
                "psl",
                "thisisametamodel",
                "ecore",
                "type");

        assertEquals(expectedMetamodelURIs.size(), EPackage.Registry.INSTANCE.size());

        List<String> actualMetamodelNames = Arrays.stream(EPackage.Registry.INSTANCE.values().toArray())
                .map(model -> (((EPackage) model).getName())).collect(Collectors.toList());

        for (String expectedName : expectedMetamodelURIs) {
            assertTrue(
                    actualMetamodelNames.contains(expectedName),
                    "The list of registered metamodels should contain " + expectedName);
        }

    }

}
