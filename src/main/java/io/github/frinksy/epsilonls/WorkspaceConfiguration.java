package io.github.frinksy.epsilonls;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.epsilon.emc.emf.EmfUtil;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;

public class WorkspaceConfiguration {

    public static List<IModel> registerWorkspaceMetamodels(java.net.URI folderUri,
            EpsilonLanguageServer languageServer) {

        List<IModel> models = new ArrayList<>();

        Path rootPath = Path.of(folderUri);

        Stream<Path> stream = null;
        try {

            stream = Files.walk(rootPath);

            Iterator<Path> iterator = stream.iterator();

            while (iterator.hasNext()) {

                Path currentPath = iterator.next();

                if (Files.isDirectory(currentPath)) {
                    continue;
                }

                // Check if it is a .ecore file
                String fileName = currentPath.getFileName().toString();
                int dotIndex = fileName.lastIndexOf(".");

                if (fileName.substring(dotIndex).equals(".ecore")) {
                    try {
                        EmfUtil.register(URI.createURI(currentPath.toString()), EPackage.Registry.INSTANCE);

                    } catch (Exception e) {
                        languageServer.getClient().logMessage(new MessageParams(MessageType.Warning,
                                "Error loading model: " + currentPath.toString()));
                    }
                }

            }

        } catch (IOException e) {
            languageServer.getClient().logMessage(
                    new MessageParams(MessageType.Error,
                            "An IO error occured whilst loading workspace Ecore models! \n" + e.getMessage()));
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return models;

    }

    private WorkspaceConfiguration() {
    }

}
