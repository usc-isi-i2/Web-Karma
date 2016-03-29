package com.github.jsonldjava.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jsonldjava.core.DocumentLoader;
import com.github.jsonldjava.core.JsonLdApi;
import com.github.jsonldjava.core.JsonLdProcessor;

/**
 * Functions used to make loading, parsing, and serializing JSON easy using
 * Jackson.
 *
 * @author tristan
 *
 */
public class JsonUtils {
    /**
     * An HTTP Accept header that prefers JSONLD.
     */
    protected static final String ACCEPT_HEADER = "application/ld+json, application/json;q=0.9, application/javascript;q=0.5, text/javascript;q=0.5, text/plain;q=0.2, */*;q=0.1";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final JsonFactory JSON_FACTORY = new JsonFactory(JSON_MAPPER);
    private static DocumentLoader DOCUMENT_LOADER = new DocumentLoader();

    static {
        // Disable default Jackson behaviour to close
        // InputStreams/Readers/OutputStreams/Writers
        JSON_FACTORY.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        // Disable string retention features that may work for most JSON where
        // the field names are in limited supply, but does not work for JSON-LD
        // where a wide range of URIs are used for subjects and predicates
        JSON_FACTORY.disable(JsonFactory.Feature.INTERN_FIELD_NAMES);
        JSON_FACTORY.disable(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES);
    }

    private JsonUtils() {
    }

    /**
     * Parses a JSON-LD document from the given {@link InputStream} to an object
     * that can be used as input for the {@link JsonLdApi} and
     * {@link JsonLdProcessor} methods.<br>
     * Uses UTF-8 as the character encoding when decoding the InputStream.
     *
     * @param input
     *            The JSON-LD document in an InputStream.
     * @return A JSON Object.
     * @throws JsonParseException
     *             If there was a JSON related error during parsing.
     * @throws IOException
     *             If there was an IO error during parsing.
     */
    public static Object fromInputStream(InputStream input) throws IOException {
        // no readers from inputstreams w.o. encoding!!
        return fromInputStream(input, "UTF-8");
    }

    /**
     * Parses a JSON-LD document from the given {@link InputStream} to an object
     * that can be used as input for the {@link JsonLdApi} and
     * {@link JsonLdProcessor} methods.
     *
     * @param input
     *            The JSON-LD document in an InputStream.
     * @param enc
     *            The character encoding to use when interpreting the characters
     *            in the InputStream.
     * @return A JSON Object.
     * @throws JsonParseException
     *             If there was a JSON related error during parsing.
     * @throws IOException
     *             If there was an IO error during parsing.
     */
    public static Object fromInputStream(InputStream input, String enc) throws IOException {
        return fromReader(new BufferedReader(new InputStreamReader(input, enc)));
    }

    /**
     * Parses a JSON-LD document from the given {@link Reader} to an object that
     * can be used as input for the {@link JsonLdApi} and
     * {@link JsonLdProcessor} methods.
     *
     * @param reader
     *            The JSON-LD document in a Reader.
     * @return A JSON Object.
     * @throws JsonParseException
     *             If there was a JSON related error during parsing.
     * @throws IOException
     *             If there was an IO error during parsing.
     */
    public static Object fromReader(Reader reader) throws IOException {
        final JsonParser jp = JSON_FACTORY.createParser(reader);
        Object rval ;
        final JsonToken initialToken = jp.nextToken();

        if (initialToken == JsonToken.START_ARRAY) {
            rval = jp.readValueAs(List.class);
        } else if (initialToken == JsonToken.START_OBJECT) {
            rval = jp.readValueAs(Map.class);
        } else if (initialToken == JsonToken.VALUE_STRING) {
            rval = jp.readValueAs(String.class);
        } else if (initialToken == JsonToken.VALUE_FALSE || initialToken == JsonToken.VALUE_TRUE) {
            rval = jp.readValueAs(Boolean.class);
        } else if (initialToken == JsonToken.VALUE_NUMBER_FLOAT
                || initialToken == JsonToken.VALUE_NUMBER_INT) {
            rval = jp.readValueAs(Number.class);
        } else if (initialToken == JsonToken.VALUE_NULL) {
            rval = null;
        } else {
            throw new JsonParseException("document doesn't start with a valid json element : "
                    + initialToken, jp.getCurrentLocation());
        }
        
        JsonToken t ;
        try { t = jp.nextToken(); }
        catch (JsonParseException ex) {
            throw new JsonParseException("Document contains more content after json-ld element - (possible mismatched {}?)",
                                         jp.getCurrentLocation());
        }
        if ( t != null )
            throw new JsonParseException("Document contains possible json content after the json-ld element - (possible mismatched {}?)",
                                             jp.getCurrentLocation());
        return rval;
    }

    /**
     * Parses a JSON-LD document from a string to an object that can be used as
     * input for the {@link JsonLdApi} and {@link JsonLdProcessor} methods.
     *
     * @param jsonString
     *            The JSON-LD document as a string.
     * @return A JSON Object.
     * @throws JsonParseException
     *             If there was a JSON related error during parsing.
     * @throws IOException
     *             If there was an IO error during parsing.
     */
    public static Object fromString(String jsonString) throws JsonParseException, IOException {
        return fromReader(new StringReader(jsonString));
    }

    /**
     * Parses a JSON-LD document, from the contents of the JSON resource
     * resolved from the JsonLdUrl, to an object that can be used as input for
     * the {@link JsonLdApi} and {@link JsonLdProcessor} methods.
     *
     * @param url
     *            The JsonLdUrl to resolve
     * @return A JSON Object.
     * @throws JsonParseException
     *             If there was a JSON related error during parsing.
     * @throws IOException
     *             If there was an IO error during parsing.
     */
    public static Object fromURL(java.net.URL url) throws JsonParseException, IOException {
        return DOCUMENT_LOADER.fromURL(url);
    }

    /**
     * Writes the given JSON-LD Object out to a String, using indentation and
     * new lines to improve readability.
     *
     * @param jsonObject
     *            The JSON-LD Object to serialize.
     * @return A JSON document serialised to a String.
     * @throws JsonGenerationException
     *             If there is a JSON error during serialization.
     * @throws IOException
     *             If there is an IO error during serialization.
     */
    public static String toPrettyString(Object jsonObject) throws JsonGenerationException,
    IOException {
        final StringWriter sw = new StringWriter();
        writePrettyPrint(sw, jsonObject);
        return sw.toString();
    }

    /**
     * Writes the given JSON-LD Object out to a String.
     *
     * @param jsonObject
     *            The JSON-LD Object to serialize.
     * @return A JSON document serialised to a String.
     * @throws JsonGenerationException
     *             If there is a JSON error during serialization.
     * @throws IOException
     *             If there is an IO error during serialization.
     */
    public static String toString(Object jsonObject) throws JsonGenerationException, IOException {
        final StringWriter sw = new StringWriter();
        write(sw, jsonObject);
        return sw.toString();
    }

    /**
     * Writes the given JSON-LD Object out to the given Writer.
     *
     * @param writer
     *            The writer that is to receive the serialized JSON-LD object.
     * @param jsonObject
     *            The JSON-LD Object to serialize.
     * @throws JsonGenerationException
     *             If there is a JSON error during serialization.
     * @throws IOException
     *             If there is an IO error during serialization.
     */
    public static void write(Writer writer, Object jsonObject) throws JsonGenerationException,
    IOException {
        final JsonGenerator jw = JSON_FACTORY.createGenerator(writer);
        jw.writeObject(jsonObject);
    }

    /**
     * Writes the given JSON-LD Object out to the given Writer, using
     * indentation and new lines to improve readability.
     *
     * @param writer
     *            The writer that is to receive the serialized JSON-LD object.
     * @param jsonObject
     *            The JSON-LD Object to serialize.
     * @throws JsonGenerationException
     *             If there is a JSON error during serialization.
     * @throws IOException
     *             If there is an IO error during serialization.
     */
    public static void writePrettyPrint(Writer writer, Object jsonObject)
            throws JsonGenerationException, IOException {
        final JsonGenerator jw = JSON_FACTORY.createGenerator(writer);
        jw.useDefaultPrettyPrinter();
        jw.writeObject(jsonObject);
    }
}
