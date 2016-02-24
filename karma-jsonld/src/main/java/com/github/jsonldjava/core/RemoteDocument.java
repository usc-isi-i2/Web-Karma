package com.github.jsonldjava.core;

public class RemoteDocument {
    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public Object getDocument() {
        return document;
    }

    public void setDocument(Object document) {
        this.document = document;
    }

    public String getContextUrl() {
        return contextUrl;
    }

    public void setContextUrl(String contextUrl) {
        this.contextUrl = contextUrl;
    }

    String documentUrl;
    Object document;
    String contextUrl;

    public RemoteDocument(String url, Object document) {
        this(url, document, null);
    }

    public RemoteDocument(String url, Object document, String context) {
        this.documentUrl = url;
        this.document = document;
        this.contextUrl = context;
    }
}
