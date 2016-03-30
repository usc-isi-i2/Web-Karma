package com.github.jsonldjava.core;

import static com.github.jsonldjava.core.JsonLdConsts.RDF_FIRST;
import static com.github.jsonldjava.core.JsonLdConsts.RDF_LANGSTRING;
import static com.github.jsonldjava.core.JsonLdConsts.RDF_NIL;
import static com.github.jsonldjava.core.JsonLdConsts.RDF_REST;
import static com.github.jsonldjava.core.JsonLdConsts.RDF_TYPE;
import static com.github.jsonldjava.core.JsonLdConsts.XSD_BOOLEAN;
import static com.github.jsonldjava.core.JsonLdConsts.XSD_DOUBLE;
import static com.github.jsonldjava.core.JsonLdConsts.XSD_INTEGER;
import static com.github.jsonldjava.core.JsonLdConsts.XSD_STRING;
import static com.github.jsonldjava.core.JsonLdUtils.isKeyword;
import static com.github.jsonldjava.core.JsonLdUtils.isList;
import static com.github.jsonldjava.core.JsonLdUtils.isObject;
import static com.github.jsonldjava.core.JsonLdUtils.isString;
import static com.github.jsonldjava.core.JsonLdUtils.isValue;
import static com.github.jsonldjava.utils.Obj.newMap;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Starting to migrate away from using plain java Maps as the internal RDF
 * dataset store. Currently each item just wraps a Map based on the old format
 * so everything doesn't break. Will phase this out once everything is using the
 * new format.
 *
 * @author Tristan
 *
 */
public class RDFDataset extends LinkedHashMap<String, Object> {
    private static final long serialVersionUID = 2796344994239879165L;

    private static final Pattern PATTERN_INTEGER = Pattern.compile("^[\\-+]?[0-9]+$");
    private static final Pattern PATTERN_DOUBLE = Pattern
            .compile("^(\\+|-)?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)([Ee](\\+|-)?[0-9]+)?$");

    public static class Quad extends LinkedHashMap<String, Object> implements Comparable<Quad> {
        private static final long serialVersionUID = -7021918051975883082L;

        public Quad(final String subject, final String predicate, final String object,
                final String graph) {
            this(subject, predicate, object.startsWith("_:") ? new BlankNode(object) : new IRI(
                    object), graph);
        };

        public Quad(final String subject, final String predicate, final String value,
                final String datatype, final String language, final String graph) {
            this(subject, predicate, new Literal(value, datatype, language), graph);
        };

        private Quad(final String subject, final String predicate, final Node object,
                final String graph) {
            this(subject.startsWith("_:") ? new BlankNode(subject) : new IRI(subject), new IRI(
                    predicate), object, graph);
        };

        public Quad(final Node subject, final Node predicate, final Node object, final String graph) {
            super();
            put("subject", subject);
            put("predicate", predicate);
            put("object", object);
            if (graph != null && !"@default".equals(graph)) {
                // TODO: i'm not yet sure if this should be added or if the
                // graph should only be represented by the keys in the dataset
                put("name", graph.startsWith("_:") ? new BlankNode(graph) : new IRI(graph));
            }
        }

        public Node getSubject() {
            return (Node) get("subject");
        }

        public Node getPredicate() {
            return (Node) get("predicate");
        }

        public Node getObject() {
            return (Node) get("object");
        }

        public Node getGraph() {
            return (Node) get("name");
        }

        @Override
        public int compareTo(Quad o) {
            if (o == null) {
                return 1;
            }
            int rval = getGraph().compareTo(o.getGraph());
            if (rval != 0) {
                return rval;
            }
            rval = getSubject().compareTo(o.getSubject());
            if (rval != 0) {
                return rval;
            }
            rval = getPredicate().compareTo(o.getPredicate());
            if (rval != 0) {
                return rval;
            }
            return getObject().compareTo(o.getObject());
        }
    }

    public static abstract class Node extends LinkedHashMap<String, Object> implements
    Comparable<Node> {
        private static final long serialVersionUID = 1460990331795672793L;

        public abstract boolean isLiteral();

        public abstract boolean isIRI();

        public abstract boolean isBlankNode();

        public String getValue() {
            return (String) get("value");
        }

        public String getDatatype() {
            return (String) get("datatype");
        }

        public String getLanguage() {
            return (String) get("language");
        }

        @Override
        public int compareTo(Node o) {
            if (this.isIRI()) {
                if (!o.isIRI()) {
                    // IRIs > everything
                    return 1;
                }
            } else if (this.isBlankNode()) {
                if (o.isIRI()) {
                    // IRI > blank node
                    return -1;
                } else if (o.isLiteral()) {
                    // blank node > literal
                    return 1;
                }
            }
            return this.getValue().compareTo(o.getValue());
        }

        /**
         * Converts an RDF triple object to a JSON-LD object.
         *
         * @param o
         *            the RDF triple object to convert.
         * @param useNativeTypes
         *            true to output native types, false not to.
         *
         * @return the JSON-LD object.
         * @throws JsonLdError
         */
        Map<String, Object> toObject(Boolean useNativeTypes) throws JsonLdError {
            // If value is an an IRI or a blank node identifier, return a new
            // JSON object consisting
            // of a single member @id whose value is set to value.
            if (isIRI() || isBlankNode()) {
                return newMap("@id", getValue());
            }

            // convert literal object to JSON-LD
            final Map<String, Object> rval = newMap("@value", getValue());

            // add language
            if (getLanguage() != null) {
                rval.put("@language", getLanguage());
            }
            // add datatype
            else {
                final String type = getDatatype();
                final String value = getValue();
                if (useNativeTypes) {
                    // use native datatypes for certain xsd types
                    if (XSD_STRING.equals(type)) {
                        // don't add xsd:string
                    } else if (XSD_BOOLEAN.equals(type)) {
                        if ("true".equals(value)) {
                            rval.put("@value", Boolean.TRUE);
                        } else if ("false".equals(value)) {
                            rval.put("@value", Boolean.FALSE);
                        } else {
                            // Else do not replace the value, and add the
                            // boolean type in
                            rval.put("@type", type);
                        }
                    } else if (
                            // http://www.w3.org/TR/xmlschema11-2/#integer
                            (XSD_INTEGER.equals(type) && PATTERN_INTEGER.matcher(value).matches())
                            // http://www.w3.org/TR/xmlschema11-2/#nt-doubleRep
                            || (XSD_DOUBLE.equals(type) && PATTERN_DOUBLE.matcher(value).matches())) {
                        try {
                            final Double d = Double.parseDouble(value);
                            if (!Double.isNaN(d) && !Double.isInfinite(d)) {
                                if (XSD_INTEGER.equals(type)) {
                                    final Integer i = d.intValue();
                                    if (i.toString().equals(value)) {
                                        rval.put("@value", i);
                                    }
                                } else if (XSD_DOUBLE.equals(type)) {
                                    rval.put("@value", d);
                                } else {
                                    throw new RuntimeException(
                                            "This should never happen as we checked the type was either integer or double");
                                }
                            }
                        } catch (final NumberFormatException e) {
                            // TODO: This should never happen since we match the
                            // value with regex!
                            throw new RuntimeException(e);
                        }
                    }
                    // do not add xsd:string type
                    else {
                        rval.put("@type", type);
                    }
                } else if (!XSD_STRING.equals(type)) {
                    rval.put("@type", type);
                }
            }
            return rval;
        }
    }

    public static class Literal extends Node {
        private static final long serialVersionUID = 8124736271571220251L;

        public Literal(String value, String datatype, String language) {
            super();
            put("type", "literal");
            put("value", value);
            put("datatype", datatype != null ? datatype : XSD_STRING);
            if (language != null) {
                put("language", language);
            }
        }

        @Override
        public boolean isLiteral() {
            return true;
        }

        @Override
        public boolean isIRI() {
            return false;
        }

        @Override
        public boolean isBlankNode() {
            return false;
        }

        @Override
        public int compareTo(Node o) {
            if (o == null) {
                // valid nodes are > null nodes
                return 1;
            }
            if (o.isIRI()) {
                // literals < iri
                return -1;
            }
            if (o.isBlankNode()) {
                // blank node < iri
                return -1;
            }
            if (this.getLanguage() == null && ((Literal) o).getLanguage() != null) {
                return -1;
            } else if (this.getLanguage() != null && ((Literal) o).getLanguage() == null) {
                return 1;
            }

            if (this.getDatatype() != null) {
                return this.getDatatype().compareTo(((Literal) o).getDatatype());
            } else if (((Literal) o).getDatatype() != null) {
                return -1;
            }
            return 0;
        }
    }

    public static class IRI extends Node {
        private static final long serialVersionUID = 1540232072155490782L;

        public IRI(String iri) {
            super();
            put("type", "IRI");
            put("value", iri);
        }

        @Override
        public boolean isLiteral() {
            return false;
        }

        @Override
        public boolean isIRI() {
            return true;
        }

        @Override
        public boolean isBlankNode() {
            return false;
        }
    }

    public static class BlankNode extends Node {
        private static final long serialVersionUID = -2842402820440697318L;

        public BlankNode(String attribute) {
            super();
            put("type", "blank node");
            put("value", attribute);
        }

        @Override
        public boolean isLiteral() {
            return false;
        }

        @Override
        public boolean isIRI() {
            return false;
        }

        @Override
        public boolean isBlankNode() {
            return true;
        }
    }

    private static final Node first = new IRI(RDF_FIRST);
    private static final Node rest = new IRI(RDF_REST);
    private static final Node nil = new IRI(RDF_NIL);

    private final Map<String, String> context;

    // private UniqueNamer namer;
    private JsonLdApi api;

    public RDFDataset() {
        super();
        put("@default", new ArrayList<Quad>());
        context = new LinkedHashMap<>();
        // put("@context", context);
    }

    /*
     * public RDFDataset(String blankNodePrefix) { this(new
     * UniqueNamer(blankNodePrefix)); }
     *
     * public RDFDataset(UniqueNamer namer) { this(); this.namer = namer; }
     */
    public RDFDataset(JsonLdApi jsonLdApi) {
        this();
        this.api = jsonLdApi;
    }

    public void setNamespace(String ns, String prefix) {
        context.put(ns, prefix);
    }

    public String getNamespace(String ns) {
        return context.get(ns);
    }

    /**
     * clears all the namespaces in this dataset
     */
    public void clearNamespaces() {
        context.clear();
    }

    public Map<String, String> getNamespaces() {
        return context;
    }

    /**
     * Returns a valid context containing any namespaces set
     *
     * @return The context map
     */
    public Map<String, Object> getContext() {
        final Map<String, Object> rval = newMap();
        rval.putAll(context);
        // replace "" with "@vocab"
        if (rval.containsKey("")) {
            rval.put("@vocab", rval.remove(""));
        }
        return rval;
    }

    /**
     * parses a context object and sets any namespaces found within it
     *
     * @param contextLike
     *            The context to parse
     * @throws JsonLdError
     *             If the context can't be parsed
     */
    public void parseContext(Object contextLike) throws JsonLdError {
        Context context;
        if (api != null) {
            context = new Context(api.opts);
        } else {
            context = new Context();
        }
        // Context will do our recursive parsing and initial IRI resolution
        context = context.parse(contextLike);
        // And then leak to us the potential 'prefixes'
        final Map<String, String> prefixes = context.getPrefixes(true);

        for (final Map.Entry<String, String> stringStringEntry : prefixes.entrySet()) {
            final String val = stringStringEntry.getValue();
            if ("@vocab".equals(stringStringEntry.getKey())) {
                if (val == null || isString(val)) {
                    setNamespace("", val);
                } else {
                }
            } else if (!isKeyword(stringStringEntry.getKey())) {
                setNamespace(stringStringEntry.getKey(), val);
                // TODO: should we make sure val is a valid URI prefix (i.e. it
                // ends with /# or ?)
                // or is it ok that full URIs for terms are used?
            }
        }
    }

    /**
     * Adds a triple to the @default graph of this dataset
     *
     * @param subject
     *            the subject for the triple
     * @param predicate
     *            the predicate for the triple
     * @param value
     *            the value of the literal object for the triple
     * @param datatype
     *            the datatype of the literal object for the triple (null values
     *            will default to xsd:string)
     * @param language
     *            the language of the literal object for the triple (or null)
     */
    public void addTriple(final String subject, final String predicate, final String value,
            final String datatype, final String language) {
        addQuad(subject, predicate, value, datatype, language, "@default");
    }

    /**
     * Adds a triple to the specified graph of this dataset
     *
     * @param s
     *            the subject for the triple
     * @param p
     *            the predicate for the triple
     * @param value
     *            the value of the literal object for the triple
     * @param datatype
     *            the datatype of the literal object for the triple (null values
     *            will default to xsd:string)
     * @param graph
     *            the graph to add this triple to
     * @param language
     *            the language of the literal object for the triple (or null)
     */
    public void addQuad(final String s, final String p, final String value, final String datatype,
            final String language, String graph) {
        if (graph == null) {
            graph = "@default";
        }
        if (!containsKey(graph)) {
            put(graph, new ArrayList<Quad>());
        }
        ((ArrayList<Quad>) get(graph)).add(new Quad(s, p, value, datatype, language, graph));
    }

    /**
     * Adds a triple to the default graph of this dataset
     *
     * @param subject
     *            the subject for the triple
     * @param predicate
     *            the predicate for the triple
     * @param object
     *            the object for the triple
     */
    public void addTriple(final String subject, final String predicate, final String object) {
        addQuad(subject, predicate, object, "@default");
    }

    /**
     * Adds a triple to the specified graph of this dataset
     *
     * @param subject
     *            the subject for the triple
     * @param predicate
     *            the predicate for the triple
     * @param object
     *            the object for the triple
     * @param graph
     *            the graph to add this triple to
     */
    public void addQuad(final String subject, final String predicate, final String object,
            String graph) {
        if (graph == null) {
            graph = "@default";
        }
        if (!containsKey(graph)) {
            put(graph, new ArrayList<Quad>());
        }
        ((ArrayList<Quad>) get(graph)).add(new Quad(subject, predicate, object, graph));
    }

    /**
     * Creates an array of RDF triples for the given graph.
     *
     * @param graphName
     *            The graph URI
     * @param graph
     *            the graph to create RDF triples for.
     */
    void graphToRDF(String graphName, Map<String, Object> graph) {
        // 4.2)
        final List<Quad> triples = new ArrayList<>();
        // 4.3)
        final List<String> subjects = new ArrayList<>(graph.keySet());
        // Collections.sort(subjects);
        for (final String id : subjects) {
            if (JsonLdUtils.isRelativeIri(id)) {
                continue;
            }
            final Map<String, Object> node = (Map<String, Object>) graph.get(id);
            final List<String> properties = new ArrayList<>(node.keySet());
            Collections.sort(properties);
            for (String property : properties) {
                final List<Object> values;
                // 4.3.2.1)
                if ("@type".equals(property)) {
                    values = (List<Object>) node.get("@type");
                    property = RDF_TYPE;
                }
                // 4.3.2.2)
                else if (isKeyword(property)) {
                    continue;
                }
                // 4.3.2.3)
                else if (property.startsWith("_:") && !api.opts.getProduceGeneralizedRdf()) {
                    continue;
                }
                // 4.3.2.4)
                else if (JsonLdUtils.isRelativeIri(property)) {
                    continue;
                } else {
                    values = (List<Object>) node.get(property);
                }

                Node subject;
                if (id.indexOf("_:") == 0) {
                    // NOTE: don't rename, just set it as a blank node
                    subject = new BlankNode(id);
                } else {
                    subject = new IRI(id);
                }

                // RDF predicates
                Node predicate;
                if (property.startsWith("_:")) {
                    predicate = new BlankNode(property);
                } else {
                    predicate = new IRI(property);
                }

                for (final Object item : values) {
                    // convert @list to triples
                    if (isList(item)) {
                        final List<Object> list = (List<Object>) ((Map<String, Object>) item)
                                .get("@list");
                        Node last = null;
                        Node firstBNode = nil;
                        if (!list.isEmpty()) {
                            last = objectToRDF(list.get(list.size() - 1));
                            firstBNode = new BlankNode(api.generateBlankNodeIdentifier());
                        }
                        triples.add(new Quad(subject, predicate, firstBNode, graphName));
                        for (int i = 0; i < list.size() - 1; i++) {
                            final Node object = objectToRDF(list.get(i));
                            triples.add(new Quad(firstBNode, first, object, graphName));
                            final Node restBNode = new BlankNode(api.generateBlankNodeIdentifier());
                            triples.add(new Quad(firstBNode, rest, restBNode, graphName));
                            firstBNode = restBNode;
                        }
                        if (last != null) {
                            triples.add(new Quad(firstBNode, first, last, graphName));
                            triples.add(new Quad(firstBNode, rest, nil, graphName));
                        }
                    }
                    // convert value or node object to triple
                    else {
                        final Node object = objectToRDF(item);
                        if (object != null) {
                            triples.add(new Quad(subject, predicate, object, graphName));
                        }
                    }
                }
            }
        }
        put(graphName, triples);
    }

    /**
     * Converts a JSON-LD value object to an RDF literal or a JSON-LD string or
     * node object to an RDF resource.
     *
     * @param item
     *            the JSON-LD value or node object.
     * @return the RDF literal or RDF resource.
     */
    private Node objectToRDF(Object item) {
        // convert value object to RDF
        if (isValue(item)) {
            final Object value = ((Map<String, Object>) item).get("@value");
            final Object datatype = ((Map<String, Object>) item).get("@type");

            // convert to XSD datatypes as appropriate
            if (value instanceof Boolean || value instanceof Number) {
                // convert to XSD datatype
                if (value instanceof Boolean) {
                    return new Literal(value.toString(), datatype == null ? XSD_BOOLEAN
                            : (String) datatype, null);
                } else if (value instanceof Double || value instanceof Float
                        || XSD_DOUBLE.equals(datatype)) {
                    // canonical double representation
                    final DecimalFormat df = new DecimalFormat("0.0###############E0");
                    df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
                    return new Literal(df.format(value), datatype == null ? XSD_DOUBLE
                            : (String) datatype, null);
                } else {
                    final DecimalFormat df = new DecimalFormat("0");
                    return new Literal(df.format(value), datatype == null ? XSD_INTEGER
                            : (String) datatype, null);
                }
            } else if (((Map<String, Object>) item).containsKey("@language")) {
                return new Literal((String) value, datatype == null ? RDF_LANGSTRING
                        : (String) datatype, (String) ((Map<String, Object>) item).get("@language"));
            } else {
                return new Literal((String) value, datatype == null ? XSD_STRING
                        : (String) datatype, null);
            }
        }
        // convert string/node object to RDF
        else {
            final String id;
            if (isObject(item)) {
                id = (String) ((Map<String, Object>) item).get("@id");
                if (JsonLdUtils.isRelativeIri(id)) {
                    return null;
                }
            } else {
                id = (String) item;
            }
            if (id.indexOf("_:") == 0) {
                // NOTE: once again no need to rename existing blank nodes
                return new BlankNode(id);
            } else {
                return new IRI(id);
            }
        }
    }

    public Set<String> graphNames() {
        // TODO Auto-generated method stub
        return keySet();
    }

    public List<Quad> getQuads(String graphName) {
        return (List<Quad>) get(graphName);
    }
}
