package io.github.frinksy.epsilonls;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.epsilon.emc.emf.EmfUtil;
import org.junit.jupiter.api.BeforeEach;

public abstract class TestTemplate {

    EolDocument document, document2, document3;

    @BeforeEach
    void registerMetamodels() throws Exception {
        EPackage.Registry.INSTANCE.clear();
        EPackage.Registry.INSTANCE.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
        EPackage.Registry.INSTANCE.put(XMLTypePackage.eNS_PREFIX, XMLTypePackage.eINSTANCE);
        EmfUtil.register(Paths.get("src", "test", "resources", "pencilcase.ecore").toFile(),
                EPackage.Registry.INSTANCE);
        EmfUtil.register(Paths.get("src", "test", "resources", "mymetamodel.ecore").toFile(),
                EPackage.Registry.INSTANCE);
    }

    @BeforeEach
    void setFileContents() throws IOException {
        Path resourcesPath = Paths.get("src", "test", "resources");
        Path testFilePath = resourcesPath.resolve("pens.eol");
        Path testFilePath2 = resourcesPath.resolve("random_program.eol");
        Path testFilePath3 = resourcesPath.resolve("properties_corner_cases.eol");

        document = new EolDocument(new MockedLanguageServer(), testFilePath.toUri().toString());
        document.setContents(Files.readString(testFilePath));

        document2 = new EolDocument(new MockedLanguageServer(), testFilePath2.toUri().toString());
        document2.setContents(Files.readString(testFilePath2));

        document3 = new EolDocument(new MockedLanguageServer(), testFilePath3.toUri().toString());
        document3.setContents(Files.readString(testFilePath3));
    }

}
