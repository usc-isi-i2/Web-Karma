package com.github.jsonldjava.utils;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.http.Header;
import org.apache.http.HttpVersion;
import org.apache.http.client.cache.HeaderConstants;
import org.apache.http.client.cache.HttpCacheEntry;
import org.apache.http.client.cache.HttpCacheStorage;
import org.apache.http.client.cache.HttpCacheUpdateCallback;
import org.apache.http.client.cache.HttpCacheUpdateException;
import org.apache.http.client.cache.Resource;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JarCacheStorage implements HttpCacheStorage {

    private static final String JARCACHE_JSON = "jarcache.json";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final CacheConfig cacheConfig = new CacheConfig();
    private ClassLoader classLoader;

    public ClassLoader getClassLoader() {
        if (classLoader != null) {
            return classLoader;
        }
        return Thread.currentThread().getContextClassLoader();
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public JarCacheStorage() {
        this(null);
    }

    public JarCacheStorage(ClassLoader classLoader) {
        setClassLoader(classLoader);
        cacheConfig.setMaxObjectSize(0);
        cacheConfig.setMaxCacheEntries(0);
        cacheConfig.setMaxUpdateRetries(0);
        cacheConfig.getMaxCacheEntries();
    }

    @Override
    public void putEntry(String key, HttpCacheEntry entry) throws IOException {
        // ignored

    }

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public HttpCacheEntry getEntry(String key) throws IOException {
        log.trace("Requesting " + key);
        URI requestedUri;
        try {
            requestedUri = new URI(key);
        } catch (final URISyntaxException e) {
            return null;
        }
        if ((requestedUri.getScheme().equals("http") && requestedUri.getPort() == 80)
                || (requestedUri.getScheme().equals("https") && requestedUri.getPort() == 443)) {
            // Strip away default http ports
            try {
                requestedUri = new URI(requestedUri.getScheme(), requestedUri.getHost(),
                        requestedUri.getPath(), requestedUri.getFragment());
            } catch (final URISyntaxException e) {
            }
        }

        final Enumeration<URL> jarcaches = getResources();
        while (jarcaches.hasMoreElements()) {
            final URL url = jarcaches.nextElement();

            final JsonNode tree = getJarCache(url);
            // TODO: Cache tree per URL
            for (final JsonNode node : tree) {
                final URI uri = URI.create(node.get("Content-Location").asText());
                if (uri.equals(requestedUri)) {
                    return cacheEntry(requestedUri, url, node);

                }
            }
        }
        return null;
    }

    private Enumeration<URL> getResources() throws IOException {
        final ClassLoader cl = getClassLoader();
        if (cl != null) {
            return cl.getResources(JARCACHE_JSON);
        } else {
            return ClassLoader.getSystemResources(JARCACHE_JSON);
        }
    }

    /**
     * Map from uri of jarcache.json (e.g. jar://blab.jar!jarcache.json) to a
     * SoftReference to its content as JsonNode.
     *
     * @see #getJarCache(URL)
     */
    protected ConcurrentMap<URI, SoftReference<JsonNode>> jarCaches = new ConcurrentHashMap<>();

    protected JsonNode getJarCache(URL url) throws IOException, JsonProcessingException {

        URI uri;
        try {
            uri = url.toURI();
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Invalid jarCache URI " + url, e);
        }

        // Check if we have one from before - we'll use SoftReference so that
        // the maps reference is not counted for garbage collection purposes
        final SoftReference<JsonNode> jarCacheRef = jarCaches.get(uri);
        if (jarCacheRef != null) {
            final JsonNode jarCache = jarCacheRef.get();
            if (jarCache != null) {
                return jarCache;
            } else {
                jarCaches.remove(uri);
            }
        }

        // Only parse again if the optimistic get failed
        final JsonNode tree = mapper.readTree(url);
        // Use putIfAbsent to ensure concurrent reads do not return different
        // JsonNode objects, for memory management purposes
        final SoftReference<JsonNode> putIfAbsent = jarCaches.putIfAbsent(uri,
                new SoftReference<>(tree));
        if (putIfAbsent != null) {
            final JsonNode returnValue = putIfAbsent.get();
            if (returnValue != null) {
                return returnValue;
            } else {
                // Force update the reference if the existing reference had
                // been garbage collected
                jarCaches.put(uri, new SoftReference<>(tree));
            }
        }
        return tree;
    }

    protected HttpCacheEntry cacheEntry(URI requestedUri, URL baseURL, JsonNode cacheNode)
            throws MalformedURLException, IOException {
        final URL classpath = new URL(baseURL, cacheNode.get("X-Classpath").asText());
        log.debug("Cache hit for " + requestedUri);
        log.trace("{}", cacheNode);

        final List<Header> responseHeaders = new ArrayList<>();
        if (!cacheNode.has(HTTP.DATE_HEADER)) {
            responseHeaders
            .add(new BasicHeader(HTTP.DATE_HEADER, DateUtils.formatDate(new Date())));
        }
        if (!cacheNode.has(HeaderConstants.CACHE_CONTROL)) {
            responseHeaders.add(new BasicHeader(HeaderConstants.CACHE_CONTROL,
                    HeaderConstants.CACHE_CONTROL_MAX_AGE + "=" + Integer.MAX_VALUE));
        }
        final Resource resource = new JarCacheResource(classpath);
        final Iterator<String> fieldNames = cacheNode.fieldNames();
        while (fieldNames.hasNext()) {
            final String headerName = fieldNames.next();
            final JsonNode header = cacheNode.get(headerName);
            // TODO: Support multiple headers with []
            responseHeaders.add(new BasicHeader(headerName, header.asText()));
        }

        return new HttpCacheEntry(new Date(), new Date(), new BasicStatusLine(HttpVersion.HTTP_1_1,
                200, "OK"), responseHeaders.toArray(new Header[0]), resource);
    }

    @Override
    public void removeEntry(String key) throws IOException {
        // Ignored
    }

    @Override
    public void updateEntry(String key, HttpCacheUpdateCallback callback) throws IOException,
    HttpCacheUpdateException {
        // ignored
    }

    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }

}
