package io.github.frinksy.epsilonls;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;

public abstract class EpsilonDocument {
    
    private String filename = null;
    private String contents = null;

    protected EpsilonLanguageServer languageServer;


    protected EpsilonDocument(EpsilonLanguageServer languageServer, String filename) {
        this.languageServer = languageServer;
        this.filename = filename;
    }

    protected void log(MessageType type, String message) {
        languageServer.getClient().logMessage(new MessageParams(type, message));
    }


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }


    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
