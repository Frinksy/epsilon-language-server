package io.github.frinksy.epsilonls;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Mocking the EpsilonLanguageServer class.
 * Logging is just done to stdout instead of a language client.
 */
public class MockedLanguageServer extends EpsilonLanguageServer {

    private LanguageClient languageClient = new MockedLanguageClient();

    private class MockedLanguageClient implements LanguageClient {
        public void logMessage(MessageParams params) {
            System.out.println(params.getType().toString() + " : " + params.getMessage());
        }

        @Override
        public void telemetryEvent(Object object) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'telemetryEvent'");
        }

        @Override
        public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'publishDiagnostics'");
        }

        @Override
        public void showMessage(MessageParams messageParams) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'showMessage'");
        }

        @Override
        public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'showMessageRequest'");
        }
    }

    @Override
    public LanguageClient getClient() {
        // TODO Auto-generated method stub
        return languageClient;
    }

}
