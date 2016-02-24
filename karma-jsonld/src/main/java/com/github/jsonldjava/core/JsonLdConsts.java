package com.github.jsonldjava.core;

/**
 * URI Constants used in the JSON-LD parser.
 */
public final class JsonLdConsts {

    public static final String RDF_SYNTAX_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String RDF_SCHEMA_NS = "http://www.w3.org/2000/01/rdf-schema#";
    public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema#";

    public static final String XSD_ANYTYPE = XSD_NS + "anyType";
    public static final String XSD_BOOLEAN = XSD_NS + "boolean";
    public static final String XSD_DOUBLE = XSD_NS + "double";
    public static final String XSD_INTEGER = XSD_NS + "integer";
    public static final String XSD_FLOAT = XSD_NS + "float";
    public static final String XSD_DECIMAL = XSD_NS + "decimal";
    public static final String XSD_ANYURI = XSD_NS + "anyURI";
    public static final String XSD_STRING = XSD_NS + "string";

    public static final String RDF_TYPE = RDF_SYNTAX_NS + "type";
    public static final String RDF_FIRST = RDF_SYNTAX_NS + "first";
    public static final String RDF_REST = RDF_SYNTAX_NS + "rest";
    public static final String RDF_NIL = RDF_SYNTAX_NS + "nil";
    public static final String RDF_PLAIN_LITERAL = RDF_SYNTAX_NS + "PlainLiteral";
    public static final String RDF_XML_LITERAL = RDF_SYNTAX_NS + "XMLLiteral";
    public static final String RDF_OBJECT = RDF_SYNTAX_NS + "object";
    public static final String RDF_LANGSTRING = RDF_SYNTAX_NS + "langString";
    public static final String RDF_LIST = RDF_SYNTAX_NS + "List";
}
