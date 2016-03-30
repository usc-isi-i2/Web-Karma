package com.github.jsonldjava.core;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.jsonldjava.core.JsonLdConsts.*;
import static com.github.jsonldjava.core.JsonLdUtils.*;
import static com.github.jsonldjava.core.Regex.HEX;
import static com.github.jsonldjava.utils.Obj.newMap;

public class RDFDatasetUtils {

    private RDFDatasetUtils() {
    }

    /**
     * Creates an array of RDF triples for the given graph.
     *
     * @param graph
     *            the graph to create RDF triples for.
     * @param namer
     *            a UniqueNamer for assigning blank node names.
     *
     * @return the array of RDF triples for the given graph.
     * @deprecated Use {@link RDFDataset#graphToRDF(String, Map)} instead
     */
    @Deprecated
    static List<Object> graphToRDF(Map<String, Object> graph, UniqueNamer namer) {
        final List<Object> rval = new ArrayList<>();
        for (final Map.Entry<String, Object> stringObjectEntry : graph.entrySet()) {
            final Map<String, Object> node = (Map<String, Object>) stringObjectEntry.getValue();
            final List<String> properties = new ArrayList<>(node.keySet());

            Collections.sort(properties);
            for (String property : properties) {
                final Object items = node.get(property);
                if ("@type".equals(property)) {
                    property = RDF_TYPE;
                } else if (isKeyword(property)) {
                    continue;
                }

                for (final Object item : (List<Object>) items) {
                    // RDF subjects
                    final Map<String, Object> subject = newMap();
                    if (stringObjectEntry.getKey().indexOf("_:") == 0) {
                        subject.put("type", "blank node");
                        subject.put("value", namer.getName(stringObjectEntry.getKey()));
                    } else {
                        subject.put("type", "IRI");
                        subject.put("value", stringObjectEntry.getKey());
                    }

                    // RDF predicates
                    final Map<String, Object> predicate = newMap();
                    predicate.put("type", "IRI");
                    predicate.put("value", property);

                    // convert @list to triples
                    if (isList(item)) {
                        listToRDF((List<Object>) ((Map<String, Object>) item).get("@list"), namer,
                                subject, predicate, rval);
                    }
                    // convert value or node object to triple
                    else {
                        final Object object = objectToRDF(item, namer);
                        final Map<String, Object> tmp = newMap();
                        tmp.put("subject", subject);
                        tmp.put("predicate", predicate);
                        tmp.put("object", object);
                        rval.add(tmp);
                    }
                }
            }
        }

        return rval;
    }

    /**
     * Converts a @list value into linked list of blank node RDF triples (an RDF
     * collection).
     *
     * @param list
     *            the @list value.
     * @param namer
     *            a UniqueNamer for assigning blank node names.
     * @param subject
     *            the subject for the head of the list.
     * @param predicate
     *            the predicate for the head of the list.
     * @param triples
     *            the array of triples to append to.
     */
    private static void listToRDF(List<Object> list, UniqueNamer namer,
            Map<String, Object> subject, Map<String, Object> predicate, List<Object> triples) {
        final Map<String, Object> first = newMap();
        first.put("type", "IRI");
        first.put("value", RDF_FIRST);
        final Map<String, Object> rest = newMap();
        rest.put("type", "IRI");
        rest.put("value", RDF_REST);
        final Map<String, Object> nil = newMap();
        nil.put("type", "IRI");
        nil.put("value", RDF_NIL);

        for (final Object item : list) {
            final Map<String, Object> blankNode = newMap();
            blankNode.put("type", "blank node");
            blankNode.put("value", namer.getName());

            {
                final Map<String, Object> tmp = newMap();
                tmp.put("subject", subject);
                tmp.put("predicate", predicate);
                tmp.put("object", blankNode);
                triples.add(tmp);
            }

            subject = blankNode;
            predicate = first;
            final Object object = objectToRDF(item, namer);

            {
                final Map<String, Object> tmp = newMap();
                tmp.put("subject", subject);
                tmp.put("predicate", predicate);
                tmp.put("object", object);
                triples.add(tmp);
            }

            predicate = rest;
        }
        final Map<String, Object> tmp = newMap();
        tmp.put("subject", subject);
        tmp.put("predicate", predicate);
        tmp.put("object", nil);
        triples.add(tmp);
    }

    /**
     * Converts a JSON-LD value object to an RDF literal or a JSON-LD string or
     * node object to an RDF resource.
     *
     * @param item
     *            the JSON-LD value or node object.
     * @param namer
     *            the UniqueNamer to use to assign blank node names.
     *
     * @return the RDF literal or RDF resource.
     */
    private static Object objectToRDF(Object item, UniqueNamer namer) {
        final Map<String, Object> object = newMap();

        // convert value object to RDF
        if (isValue(item)) {
            object.put("type", "literal");
            final Object value = ((Map<String, Object>) item).get("@value");
            final Object datatype = ((Map<String, Object>) item).get("@type");

            // convert to XSD datatypes as appropriate
            if (value instanceof Boolean || value instanceof Number) {
                // convert to XSD datatype
                if (value instanceof Boolean) {
                    object.put("value", value.toString());
                    object.put("datatype", datatype == null ? XSD_BOOLEAN : datatype);
                } else if (value instanceof Double || value instanceof Float) {
                    // canonical double representation
                    final DecimalFormat df = new DecimalFormat("0.0###############E0");
                    df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
                    object.put("value", df.format(value));
                    object.put("datatype", datatype == null ? XSD_DOUBLE : datatype);
                } else {
                    final DecimalFormat df = new DecimalFormat("0");
                    object.put("value", df.format(value));
                    object.put("datatype", datatype == null ? XSD_INTEGER : datatype);
                }
            } else if (((Map<String, Object>) item).containsKey("@language")) {
                object.put("value", value);
                object.put("datatype", datatype == null ? RDF_LANGSTRING : datatype);
                object.put("language", ((Map<String, Object>) item).get("@language"));
            } else {
                object.put("value", value);
                object.put("datatype", datatype == null ? XSD_STRING : datatype);
            }
        }
        // convert string/node object to RDF
        else {
            final String id = isObject(item) ? (String) ((Map<String, Object>) item).get("@id")
                    : (String) item;
            if (id.indexOf("_:") == 0) {
                object.put("type", "blank node");
                object.put("value", namer.getName(id));
            } else {
                object.put("type", "IRI");
                object.put("value", id);
            }
        }

        return object;
    }

    public static String toNQuads(RDFDataset dataset) {
        final List<String> quads = new ArrayList<>();
        for (String graphName : dataset.graphNames()) {
            final List<RDFDataset.Quad> triples = dataset.getQuads(graphName);
            if ("@default".equals(graphName)) {
                graphName = null;
            }
            for (final RDFDataset.Quad triple : triples) {
                quads.add(toNQuad(triple, graphName));
            }
        }
        Collections.sort(quads);
        StringBuilder builder = new StringBuilder();
        for (final String quad : quads) {
            builder.append(quad);
        }
        return builder.toString();
    }

    static String toNQuad(RDFDataset.Quad triple, String graphName, String bnode) {
        final RDFDataset.Node s = triple.getSubject();
        final RDFDataset.Node p = triple.getPredicate();
        final RDFDataset.Node o = triple.getObject();

        String quad = "";

        // subject is an IRI or bnode
        if (s.isIRI()) {
            quad += "<" + escape(s.getValue()) + ">";
        }
        // normalization mode
        else if (bnode != null) {
            quad += bnode.equals(s.getValue()) ? "_:a" : "_:z";
        }
        // normal mode
        else {
            quad += s.getValue();
        }

        if (p.isIRI()) {
            quad += " <" + escape(p.getValue()) + "> ";
        }
        // otherwise it must be a bnode (TODO: can we only allow this if the
        // flag is set in options?)
        else {
            quad += " " + escape(p.getValue()) + " ";
        }

        // object is IRI, bnode or literal
        if (o.isIRI()) {
            quad += "<" + escape(o.getValue()) + ">";
        } else if (o.isBlankNode()) {
            // normalization mode
            if (bnode != null) {
                quad += bnode.equals(o.getValue()) ? "_:a" : "_:z";
            }
            // normal mode
            else {
                quad += o.getValue();
            }
        } else {
            final String escaped = escape(o.getValue());
            quad += "\"" + escaped + "\"";
            if (RDF_LANGSTRING.equals(o.getDatatype())) {
                quad += "@" + o.getLanguage();
            } else if (!XSD_STRING.equals(o.getDatatype())) {
                quad += "^^<" + escape(o.getDatatype()) + ">";
            }
        }

        // graph
        if (graphName != null) {
            if (graphName.indexOf("_:") != 0) {
                quad += " <" + escape(graphName) + ">";
            } else if (bnode != null) {
                quad += " _:g";
            } else {
                quad += " " + graphName;
            }
        }

        quad += " .\n";
        return quad;
    }

    static String toNQuad(RDFDataset.Quad triple, String graphName) {
        return toNQuad(triple, graphName, null);
    }

    final private static Pattern UCHAR_MATCHED = Pattern.compile("\\u005C(?:([tbnrf\\\"'])|(?:u("
            + HEX + "{4}))|(?:U(" + HEX + "{8})))");

    public static String unescape(String str) {
        String rval = str;
        if (str != null) {
            final Matcher m = UCHAR_MATCHED.matcher(str);
            while (m.find()) {
                String uni = m.group(0);
                if (m.group(1) == null) {
                    final String hex = m.group(2) != null ? m.group(2) : m.group(3);
                    final int v = Integer.parseInt(hex, 16);// hex =
                    // hex.replaceAll("^(?:00)+",
                    // "");
                    if (v > 0xFFFF) {
                        // deal with UTF-32
                        // Integer v = Integer.parseInt(hex, 16);
                        final int vt = v - 0x10000;
                        final int vh = vt >> 10;
                        final int v1 = vt & 0x3FF;
                        final int w1 = 0xD800 + vh;
                        final int w2 = 0xDC00 + v1;

                        final StringBuffer b = new StringBuffer();
                        b.appendCodePoint(w1);
                        b.appendCodePoint(w2);
                        uni = b.toString();
                    } else {
                        uni = Character.toString((char) v);
                    }
                } else {
                    final char c = m.group(1).charAt(0);
                    switch (c) {
                    case 'b':
                        uni = "\b";
                        break;
                    case 'n':
                        uni = "\n";
                        break;
                    case 't':
                        uni = "\t";
                        break;
                    case 'f':
                        uni = "\f";
                        break;
                    case 'r':
                        uni = "\r";
                        break;
                    case '\'':
                        uni = "'";
                        break;
                    case '\"':
                        uni = "\"";
                        break;
                    case '\\':
                        uni = "\\";
                        break;
                    default:
                        // do nothing
                        continue;
                    }
                }
                final String pat = Pattern.quote(m.group(0));
                final String x = Integer.toHexString(uni.charAt(0));
                rval = rval.replaceAll(pat, uni);
            }
        }
        return rval;
    }

    public static String escape(String str) {
        String rval = "";
        for (int i = 0; i < str.length(); i++) {
            final char hi = str.charAt(i);
            if (hi <= 0x8 || hi == 0xB || hi == 0xC || (hi >= 0xE && hi <= 0x1F)
                    || (hi >= 0x7F && hi <= 0xA0) || // 0xA0 is end of
                    // non-printable latin-1
                    // supplement
                    // characters
                    ((hi >= 0x24F // 0x24F is the end of latin extensions
                    && !Character.isHighSurrogate(hi))
                    // TODO: there's probably a lot of other characters that
                    // shouldn't be escaped that
                    // fall outside these ranges, this is one example from the
                    // json-ld tests
                            )) {
                rval += String.format("\\u%04x", (int) hi);
            } else if (Character.isHighSurrogate(hi)) {
                final char lo = str.charAt(++i);
                final int c = (hi << 10) + lo + (0x10000 - (0xD800 << 10) - 0xDC00);
                rval += String.format("\\U%08x", c);
            } else {
                switch (hi) {
                case '\b':
                    rval += "\\b";
                    break;
                case '\n':
                    rval += "\\n";
                    break;
                case '\t':
                    rval += "\\t";
                    break;
                case '\f':
                    rval += "\\f";
                    break;
                case '\r':
                    rval += "\\r";
                    break;
                    // case '\'':
                    // rval += "\\'";
                    // break;
                case '\"':
                    rval += "\\\"";
                    // rval += "\\u0022";
                    break;
                case '\\':
                    rval += "\\\\";
                    break;
                default:
                    // just put the char as is
                    rval += hi;
                    break;
                }
            }
        }
        return rval;
    }

    private static class Regex {
        // define partial regexes
        // final public static Pattern IRI =
        // Pattern.compile("(?:<([^:]+:[^>]*)>)");
        final public static Pattern IRI = Pattern.compile("(?:<([^>]*)>)");
        final public static Pattern BNODE = Pattern.compile("(_:(?:[A-Za-z][A-Za-z0-9]*))");
        final public static Pattern PLAIN = Pattern.compile("\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"");
        final public static Pattern DATATYPE = Pattern.compile("(?:\\^\\^" + IRI + ")");
        final public static Pattern LANGUAGE = Pattern.compile("(?:@([a-z]+(?:-[a-zA-Z0-9]+)*))");
        final public static Pattern LITERAL = Pattern.compile("(?:" + PLAIN + "(?:" + DATATYPE
                + "|" + LANGUAGE + ")?)");
        final public static Pattern WS = Pattern.compile("[ \\t]+");
        final public static Pattern WSO = Pattern.compile("[ \\t]*");
        final public static Pattern EOLN = Pattern.compile("(?:\r\n)|(?:\n)|(?:\r)");
        final public static Pattern EMPTY = Pattern.compile("^" + WSO + "$");

        // define quad part regexes
        final public static Pattern SUBJECT = Pattern.compile("(?:" + IRI + "|" + BNODE + ")" + WS);
        final public static Pattern PROPERTY = Pattern.compile(IRI.pattern() + WS.pattern());
        final public static Pattern OBJECT = Pattern.compile("(?:" + IRI + "|" + BNODE + "|"
                + LITERAL + ")" + WSO);
        final public static Pattern GRAPH = Pattern.compile("(?:\\.|(?:(?:" + IRI + "|" + BNODE
                + ")" + WSO + "\\.))");

        // full quad regex
        final public static Pattern QUAD = Pattern.compile("^" + WSO + SUBJECT + PROPERTY + OBJECT
                + GRAPH + WSO + "$");
    }

    /**
     * Parses RDF in the form of N-Quads.
     *
     * @param input
     *            the N-Quads input to parse.
     *
     * @return an RDF dataset.
     * @throws JsonLdError
     *             If there was an error parsing the N-Quads document.
     */
    public static RDFDataset parseNQuads(String input) throws JsonLdError {
        // build RDF dataset
        final RDFDataset dataset = new RDFDataset();

        // split N-Quad input into lines
        final String[] lines = Regex.EOLN.split(input);
        int lineNumber = 0;
        for (final String line : lines) {
            lineNumber++;

            // skip empty lines
            if (Regex.EMPTY.matcher(line).matches()) {
                continue;
            }

            // parse quad
            final Matcher match = Regex.QUAD.matcher(line);
            if (!match.matches()) {
                throw new JsonLdError(JsonLdError.Error.SYNTAX_ERROR,
                        "Error while parsing N-Quads; invalid quad. line:" + lineNumber);
            }

            // get subject
            RDFDataset.Node subject;
            if (match.group(1) != null) {
                subject = new RDFDataset.IRI(unescape(match.group(1)));
            } else {
                subject = new RDFDataset.BlankNode(unescape(match.group(2)));
            }

            // get predicate
            final RDFDataset.Node predicate = new RDFDataset.IRI(unescape(match.group(3)));

            // get object
            RDFDataset.Node object;
            if (match.group(4) != null) {
                object = new RDFDataset.IRI(unescape(match.group(4)));
            } else if (match.group(5) != null) {
                object = new RDFDataset.BlankNode(unescape(match.group(5)));
            } else {
                final String language = unescape(match.group(8));
                final String datatype = match.group(7) != null ? unescape(match.group(7)) : match
                        .group(8) != null ? RDF_LANGSTRING : XSD_STRING;
                final String unescaped = unescape(match.group(6));
                object = new RDFDataset.Literal(unescaped, datatype, language);
            }

            // get graph name ('@default' is used for the default graph)
            String name = "@default";
            if (match.group(9) != null) {
                name = unescape(match.group(9));
            } else if (match.group(10) != null) {
                name = unescape(match.group(10));
            }

            final RDFDataset.Quad triple = new RDFDataset.Quad(subject, predicate, object, name);

            // initialise graph in dataset
            if (!dataset.containsKey(name)) {
                final List<RDFDataset.Quad> tmp = new ArrayList<>();
                tmp.add(triple);
                dataset.put(name, tmp);
            }
            // add triple if unique to its graph
            else {
                final List<RDFDataset.Quad> triples = (List<RDFDataset.Quad>) dataset.get(name);
                if (!triples.contains(triple)) {
                    triples.add(triple);
                }
            }
        }

        return dataset;
    }
}
