package io.github.frinksy.epsilonls;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;

public abstract class EpsilonDocument {
    
    protected String filename;
    protected String contents;
    protected EpsilonLanguageServer languageServer;


    protected EpsilonDocument(EpsilonLanguageServer languageServer) {
        this.languageServer = languageServer;
    }

    protected void log(MessageType type, String message) {
        languageServer.getClient().logMessage(new MessageParams(type, message));
    }

}
