package io.github.frinksy.epsilonls;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )  throws InterruptedException, ExecutionException, IOException
    {

        EpsilonLanguageServer server = new EpsilonLanguageServer();
        Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);
        LanguageClient client = launcher.getRemoteProxy();
        server.connect(client);

        Future<?> startListening = launcher.startListening();

        startListening.get();


    }
}
