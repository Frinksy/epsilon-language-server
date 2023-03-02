package io.github.frinksy.epsilonls;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.epsilon.emc.emf.EmfUtil;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;

public class WorkspaceConfiguration {

    /**
     * Register all Ecore metamodels in the workspace.
     * @param folderUri the workspace folder to search
     * @param languageServer
     * @return 
     */
    public static void registerWorkspaceMetamodels(java.net.URI folderUri,
            EpsilonLanguageServer languageServer) {

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

                if (dotIndex > 0 && fileName.substring(dotIndex).equals(".ecore")) {
                    registerModel(currentPath, languageServer);
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

    }

    /**
     * Register a model, return success.
    */
    private static boolean registerModel(Path path, EpsilonLanguageServer languageServer) {
        try {
            EmfUtil.register(URI.createFileURI(path.toString()), EPackage.Registry.INSTANCE);
        } catch (Exception e) {
            languageServer.getClient().logMessage(new MessageParams(MessageType.Warning, "Error loading model: " + path.toString()));
            return false;
        }

        return true;
    }


    private WorkspaceConfiguration() {
    }

}
