package com.github.jsonldjava.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.client.cache.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarCacheResource implements Resource {

    private static final long serialVersionUID = -7101296464577357444L;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final URLConnection connection;

    public JarCacheResource(URL classpath) throws IOException {
        this.connection = classpath.openConnection();
    }

    @Override
    public long length() {
        // TODO should be getContentLengthLong() but this is not available in
        // Java 6.
        return connection.getContentLength();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return connection.getInputStream();
    }

    @Override
    public void dispose() {
        try {
            connection.getInputStream().close();
        } catch (final IOException e) {
            log.error("Can't close JarCacheResource input stream", e);
        }
    }
}