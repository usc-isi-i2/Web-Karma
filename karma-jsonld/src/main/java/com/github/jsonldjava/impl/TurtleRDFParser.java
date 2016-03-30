package com.github.jsonldjava.impl;

import static com.github.jsonldjava.core.JsonLdConsts.RDF_FIRST;
import static com.github.jsonldjava.core.JsonLdConsts.RDF_LANGSTRING;
import static com.github.jsonldjava.core.JsonLdConsts.RDF_NIL;
import static com.github.jsonldjava.core.JsonLdConsts.RDF_REST;
import static com.github.jsonldjava.core.JsonLdConsts.RDF_TYPE;
import static com.github.jsonldjava.core.JsonLdConsts.XSD_BOOLEAN;
import static com.github.jsonldjava.core.JsonLdConsts.XSD_DECIMAL;
import static com.github.jsonldjava.core.JsonLdConsts.XSD_DOUBLE;
import static com.github.jsonldjava.core.JsonLdConsts.XSD_INTEGER;
import static com.github.jsonldjava.core.RDFDatasetUtils.unescape;
import static com.github.jsonldjava.core.Regex.BLANK_NODE_LABEL;
import static com.github.jsonldjava.core.Regex.DECIMAL;
import static com.github.jsonldjava.core.Regex.DOUBLE;
import static com.github.jsonldjava.core.Regex.INTEGER;
import static com.github.jsonldjava.core.Regex.IRIREF;
import static com.github.jsonldjava.core.Regex.LANGTAG;
import static com.github.jsonldjava.core.Regex.PNAME_LN;
import static com.github.jsonldjava.core.Regex.PNAME_NS;
import static com.github.jsonldjava.core.Regex.STRING_LITERAL_LONG_QUOTE;
import static com.github.jsonldjava.core.Regex.STRING_LITERAL_LONG_SINGLE_QUOTE;
import static com.github.jsonldjava.core.Regex.STRING_LITERAL_QUOTE;
import static com.github.jsonldjava.core.Regex.STRING_LITERAL_SINGLE_QUOTE;
import static com.github.jsonldjava.core.Regex.UCHAR;
import static com.github.jsonldjava.core.Regex.WS;
import static com.github.jsonldjava.core.Regex.WS_0_N;
import static com.github.jsonldjava.core.Regex.WS_1_N;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.core.RDFParser;
import com.github.jsonldjava.core.UniqueNamer;

/**
 * A (probably terribly slow) Parser for turtle. Turtle is the internal
 * RDFDataset used by JSOND-Java
 *
 * TODO: this probably needs to be changed to use a proper parser/lexer
 *
 * @author Tristan
 *
 */
public class TurtleRDFParser implements RDFParser {

    static class Regex {
        final public static Pattern PREFIX_ID = Pattern.compile("@prefix" + WS_1_N + PNAME_NS
                + WS_1_N + IRIREF + WS_0_N + "\\." + WS_0_N);
        final public static Pattern BASE = Pattern.compile("@base" + WS_1_N + IRIREF + WS_0_N
                + "\\." + WS_0_N);
        final public static Pattern SPARQL_PREFIX = Pattern.compile("[Pp][Rr][Ee][Ff][Ii][Xx]" + WS
                + PNAME_NS + WS + IRIREF + WS_0_N);
        final public static Pattern SPARQL_BASE = Pattern.compile("[Bb][Aa][Ss][Ee]" + WS + IRIREF
                + WS_0_N);

        final public static Pattern PREFIXED_NAME = Pattern.compile("(?:" + PNAME_LN + "|"
                + PNAME_NS + ")");
        final public static Pattern IRI = Pattern.compile("(?:" + IRIREF + "|" + PREFIXED_NAME
                + ")");
        final public static Pattern ANON = Pattern.compile("(?:\\[" + WS + "*\\])");
        final public static Pattern BLANK_NODE = Pattern.compile(BLANK_NODE_LABEL + "|" + ANON);
        final public static Pattern STRING = Pattern.compile("(" + STRING_LITERAL_LONG_SINGLE_QUOTE
                + "|" + STRING_LITERAL_LONG_QUOTE + "|" + STRING_LITERAL_QUOTE + "|"
                + STRING_LITERAL_SINGLE_QUOTE + ")");
        final public static Pattern BOOLEAN_LITERAL = Pattern.compile("(true|false)");
        final public static Pattern RDF_LITERAL = Pattern.compile(STRING + "(?:" + LANGTAG
                + "|\\^\\^" + IRI + ")?");
        final public static Pattern NUMERIC_LITERAL = Pattern.compile("(" + DOUBLE + ")|("
                + DECIMAL + ")|(" + INTEGER + ")");
        final public static Pattern LITERAL = Pattern.compile(RDF_LITERAL + "|" + NUMERIC_LITERAL
                + "|" + BOOLEAN_LITERAL);

        final public static Pattern DIRECTIVE = Pattern.compile("^(?:" + PREFIX_ID + "|" + BASE
                + "|" + SPARQL_PREFIX + "|" + SPARQL_BASE + ")");
        final public static Pattern SUBJECT = Pattern.compile("^" + IRI + "|" + BLANK_NODE);
        final public static Pattern PREDICATE = Pattern.compile("^" + IRI + "|a" + WS_1_N);
        final public static Pattern OBJECT = Pattern.compile("^" + IRI + "|" + BLANK_NODE + "|"
                + LITERAL);

        // others
        // final public static Pattern WS_AT_LINE_START = Pattern.compile("^" +
        // WS_1_N);
        final public static Pattern EOLN = Pattern.compile("(?:\r\n)|(?:\n)|(?:\r)");
        final public static Pattern NEXT_EOLN = Pattern.compile("^.*(?:" + EOLN + ")" + WS_0_N);
        // final public static Pattern EMPTY_LINE = Pattern.compile("^" + WS +
        // "*$");

        final public static Pattern COMMENT_OR_WS = Pattern.compile("^(?:(?:[#].*(?:" + EOLN + ")"
                + WS_0_N + ")|(?:" + WS_1_N + "))");

        private Regex() {
        }
    }

    private class State {
        String baseIri = "";
        Map<String, String> namespaces = new LinkedHashMap<>();
        String curSubject = null;
        String curPredicate = null;

        String line = null;

        int lineNumber = 0;
        int linePosition = 0;

        // int bnodes = 0;
        UniqueNamer namer = new UniqueNamer("_:b");// {{ getName(); }}; // call
        // getName() after
        // construction to make
        // first active bnode _:b1

        private final Stack<Map<String, String>> stack = new Stack<>();
        public boolean expectingBnodeClose = false;

        public State(String input) throws JsonLdError {
            line = input;
            lineNumber = 1;
            advanceLinePosition(0);
        }

        public void push() {
            stack.push(new LinkedHashMap<String, String>() {
                {
                    put(curSubject, curPredicate);
                }
            });
            expectingBnodeClose = true;
            curSubject = null;
            curPredicate = null;
        }

        public void pop() {
            if (!stack.isEmpty()) {
                for (final Entry<String, String> x : stack.pop().entrySet()) {
                    curSubject = x.getKey();
                    curPredicate = x.getValue();
                }
            }
            if (stack.isEmpty()) {
                expectingBnodeClose = false;
            }
        }

        private void advanceLineNumber() throws JsonLdError {
            final Matcher match = Regex.NEXT_EOLN.matcher(line);
            if (match.find()) {
                final String[] split = match.group(0).split("" + Regex.EOLN);
                lineNumber += (split.length - 1);
                linePosition += split[split.length - 1].length();
                line = line.substring(match.group(0).length());
            }
        }

        public void advanceLinePosition(int len) throws JsonLdError {
            if (len > 0) {
                linePosition += len;
                line = line.substring(len);
            }

            while (!"".equals(line)) {
                // clear any whitespace
                final Matcher match = Regex.COMMENT_OR_WS.matcher(line);
                if (match.find() && match.group(0).length() > 0) {
                    final Matcher eoln = Regex.EOLN.matcher(match.group(0));
                    int end = 0;
                    while (eoln.find()) {
                        lineNumber += 1;
                        end = eoln.end();
                    }
                    linePosition = match.group(0).length() - end;
                    line = line.substring(match.group(0).length());
                } else {
                    break;
                }
            }
            if ("".equals(line) && !endIsOK()) {
                throw new JsonLdError(JsonLdError.Error.PARSE_ERROR,
                        "Error while parsing Turtle; unexpected end of input. {line: " + lineNumber
                        + ", position:" + linePosition + "}");
            }
        }

        private boolean endIsOK() {
            return curSubject == null && stack.isEmpty();
        }

        public String expandIRI(String ns, String name) throws JsonLdError {
            if (namespaces.containsKey(ns)) {
                return namespaces.get(ns) + name;
            } else {
                throw new JsonLdError(JsonLdError.Error.PARSE_ERROR, "No prefix found for: " + ns
                        + " {line: " + lineNumber + ", position:" + linePosition + "}");
            }
        }
    }

    @Override
    public RDFDataset parse(Object input) throws JsonLdError {
        if (!(input instanceof String)) {
            throw new JsonLdError(JsonLdError.Error.INVALID_INPUT,
                    "Invalid input; Triple RDF Parser requires a string input");
        }
        final RDFDataset result = new RDFDataset();
        final State state = new State((String) input);

        while (!"".equals(state.line)) {
            // check if line is a directive
            Matcher match = Regex.DIRECTIVE.matcher(state.line);
            if (match.find()) {
                if (match.group(1) != null || match.group(4) != null) {
                    final String ns = match.group(1) != null ? match.group(1) : match.group(4);
                    String iri = match.group(1) != null ? match.group(2) : match.group(5);
                    if (!iri.contains(":")) {
                        iri = state.baseIri + iri;
                    }
                    iri = unescape(iri);
                    validateIRI(state, iri);
                    state.namespaces.put(ns, iri);
                    result.setNamespace(ns, iri);
                } else {
                    String base = match.group(3) != null ? match.group(3) : match.group(6);
                    base = unescape(base);
                    validateIRI(state, base);
                    if (!base.contains(":")) {
                        state.baseIri = state.baseIri + base;
                    } else {
                        state.baseIri = base;
                    }
                }
                state.advanceLinePosition(match.group(0).length());
                continue;
            }

            if (state.curSubject == null) {
                // we need to match a subject
                match = Regex.SUBJECT.matcher(state.line);
                if (match.find()) {
                    String iri;
                    if (match.group(1) != null) {
                        // matched IRI
                        iri = unescape(match.group(1));
                        if (!iri.contains(":")) {
                            iri = state.baseIri + iri;
                        }
                    } else if (match.group(2) != null) {
                        // matched NS:NAME
                        final String ns = match.group(2);
                        final String name = unescapeReserved(match.group(3));
                        iri = state.expandIRI(ns, name);
                    } else if (match.group(4) != null) {
                        // match ns: only
                        iri = state.expandIRI(match.group(4), "");
                    } else if (match.group(5) != null) {
                        // matched BNODE
                        iri = state.namer.getName(match.group(0).trim());
                    } else {
                        // matched anon node
                        iri = state.namer.getName();
                    }
                    // make sure IRI still matches an IRI after escaping
                    validateIRI(state, iri);
                    state.curSubject = iri;
                    state.advanceLinePosition(match.group(0).length());
                }
                // handle blank nodes
                else if (state.line.startsWith("[")) {
                    final String bnode = state.namer.getName();
                    state.advanceLinePosition(1);
                    state.push();
                    state.curSubject = bnode;
                }
                // handle collections
                else if (state.line.startsWith("(")) {
                    final String bnode = state.namer.getName();
                    // so we know we want a predicate if the collection close
                    // isn't followed by a subject end
                    state.curSubject = bnode;
                    state.advanceLinePosition(1);
                    state.push();
                    state.curSubject = bnode;
                    state.curPredicate = RDF_FIRST;
                }
                // make sure we have a subject already
                else {
                    throw new JsonLdError(JsonLdError.Error.PARSE_ERROR,
                            "Error while parsing Turtle; missing expected subject. {line: "
                                    + state.lineNumber + "position: " + state.linePosition + "}");
                }
            }

            if (state.curPredicate == null) {
                // match predicate
                match = Regex.PREDICATE.matcher(state.line);
                if (match.find()) {
                    String iri;
                    if (match.group(1) != null) {
                        // matched IRI
                        iri = unescape(match.group(1));
                        if (!iri.contains(":")) {
                            iri = state.baseIri + iri;
                        }
                    } else if (match.group(2) != null) {
                        // matched NS:NAME
                        final String ns = match.group(2);
                        final String name = unescapeReserved(match.group(3));
                        iri = state.expandIRI(ns, name);
                    } else if (match.group(4) != null) {
                        // matched ns:
                        iri = state.expandIRI(match.group(4), "");
                    } else {
                        // matched "a"
                        iri = RDF_TYPE;
                    }
                    validateIRI(state, iri);
                    state.curPredicate = iri;
                    state.advanceLinePosition(match.group(0).length());
                } else {
                    throw new JsonLdError(JsonLdError.Error.PARSE_ERROR,
                            "Error while parsing Turtle; missing expected predicate. {line: "
                                    + state.lineNumber + "position: " + state.linePosition + "}");
                }
            }

            // expecting bnode or object

            // match BNODE values
            if (state.line.startsWith("[")) {
                final String bnode = state.namer.getName();
                result.addTriple(state.curSubject, state.curPredicate, bnode);
                state.advanceLinePosition(1);
                // check for anonymous objects
                if (state.line.startsWith("]")) {
                    state.advanceLinePosition(1);
                    // next we expect a statement or object separator
                }
                // otherwise we're inside the blank node
                else {
                    state.push();
                    state.curSubject = bnode;
                    // next we expect a predicate
                    continue;
                }
            }
            // match collections
            else if (state.line.startsWith("(")) {
                state.advanceLinePosition(1);
                // check for empty collection
                if (state.line.startsWith(")")) {
                    state.advanceLinePosition(1);
                    result.addTriple(state.curSubject, state.curPredicate, RDF_NIL);
                    // next we expect a statement or object separator
                }
                // otherwise we're inside the collection
                else {
                    final String bnode = state.namer.getName();
                    result.addTriple(state.curSubject, state.curPredicate, bnode);
                    state.push();
                    state.curSubject = bnode;
                    state.curPredicate = RDF_FIRST;
                    continue;
                }
            } else {
                // match object
                match = Regex.OBJECT.matcher(state.line);
                if (match.find()) {
                    String iri = null;
                    if (match.group(1) != null) {
                        // matched IRI
                        iri = unescape(match.group(1));
                        if (!iri.contains(":")) {
                            iri = state.baseIri + iri;
                        }
                    } else if (match.group(2) != null) {
                        // matched NS:NAME
                        final String ns = match.group(2);
                        final String name = unescapeReserved(match.group(3));
                        iri = state.expandIRI(ns, name);
                    } else if (match.group(4) != null) {
                        // matched ns:
                        iri = state.expandIRI(match.group(4), "");
                    } else if (match.group(5) != null) {
                        // matched BNODE
                        iri = state.namer.getName(match.group(0).trim());
                    }
                    if (iri != null) {
                        validateIRI(state, iri);
                        // we have a object
                        result.addTriple(state.curSubject, state.curPredicate, iri);
                    } else {
                        // we have a literal
                        String value = match.group(6);
                        String lang = null;
                        String datatype = null;
                        if (value != null) {
                            // we have a string literal
                            value = unquoteString(value);
                            value = unescape(value);
                            lang = match.group(7);
                            if (lang == null) {
                                if (match.group(8) != null) {
                                    datatype = unescape(match.group(8));
                                    if (!datatype.contains(":")) {
                                        datatype = state.baseIri + datatype;
                                    }
                                    validateIRI(state, datatype);
                                } else if (match.group(9) != null) {
                                    datatype = state.expandIRI(match.group(9),
                                            unescapeReserved(match.group(10)));
                                } else if (match.group(11) != null) {
                                    datatype = state.expandIRI(match.group(11), "");
                                }
                            } else {
                                datatype = RDF_LANGSTRING;
                            }
                        } else if (match.group(12) != null) {
                            // integer literal
                            value = match.group(12);
                            datatype = XSD_DOUBLE;
                        } else if (match.group(13) != null) {
                            // decimal literal
                            value = match.group(13);
                            datatype = XSD_DECIMAL;
                        } else if (match.group(14) != null) {
                            // double literal
                            value = match.group(14);
                            datatype = XSD_INTEGER;
                        } else if (match.group(15) != null) {
                            // boolean literal
                            value = match.group(15);
                            datatype = XSD_BOOLEAN;
                        }
                        result.addTriple(state.curSubject, state.curPredicate, value, datatype,
                                lang);
                    }
                    state.advanceLinePosition(match.group(0).length());
                } else {
                    throw new JsonLdError(JsonLdError.Error.PARSE_ERROR,
                            "Error while parsing Turtle; missing expected object or blank node. {line: "
                                    + state.lineNumber + "position: " + state.linePosition + "}");
                }
            }

            // close collection
            boolean collectionClosed = false;
            while (state.line.startsWith(")")) {
                if (!RDF_FIRST.equals(state.curPredicate)) {
                    throw new JsonLdError(JsonLdError.Error.PARSE_ERROR,
                            "Error while parsing Turtle; unexpected ). {line: " + state.lineNumber
                            + "position: " + state.linePosition + "}");
                }
                result.addTriple(state.curSubject, RDF_REST, RDF_NIL);
                state.pop();
                state.advanceLinePosition(1);
                collectionClosed = true;
            }

            boolean expectDotOrPred = false;

            // match end of bnode
            if (state.line.startsWith("]")) {
                final String bnode = state.curSubject;
                state.pop();
                state.advanceLinePosition(1);
                if (state.curSubject == null) {
                    // this is a bnode as a subject and we
                    // expect either a . or a predicate
                    state.curSubject = bnode;
                    expectDotOrPred = true;
                }
            }

            // match list separator
            if (!expectDotOrPred && state.line.startsWith(",")) {
                state.advanceLinePosition(1);
                // now we expect another object/bnode
                continue;
            }

            // match predicate end
            if (!expectDotOrPred) {
                while (state.line.startsWith(";")) {
                    state.curPredicate = null;
                    state.advanceLinePosition(1);
                    // now we expect another predicate, or a dot
                    expectDotOrPred = true;
                }
            }

            if (state.line.startsWith(".")) {
                if (state.expectingBnodeClose) {
                    throw new JsonLdError(JsonLdError.Error.PARSE_ERROR,
                            "Error while parsing Turtle; missing expected )\"]\". {line: "
                                    + state.lineNumber + "position: " + state.linePosition + "}");
                }
                state.curSubject = null;
                state.curPredicate = null;
                state.advanceLinePosition(1);
                // this can now be the end of the document.
                continue;
            } else if (expectDotOrPred) {
                // we're expecting another predicate since we didn't find a dot
                continue;
            }

            // if we're in a collection
            if (RDF_FIRST.equals(state.curPredicate)) {
                final String bnode = state.namer.getName();
                result.addTriple(state.curSubject, RDF_REST, bnode);
                state.curSubject = bnode;
                continue;
            }

            if (collectionClosed) {
                // we expect another object
                // TODO: it's not clear yet if this is valid
                continue;
            }

            // if we get here, we're missing a close statement
            throw new JsonLdError(JsonLdError.Error.PARSE_ERROR,
                    "Error while parsing Turtle; missing expected \"]\" \",\" \";\" or \".\". {line: "
                            + state.lineNumber + "position: " + state.linePosition + "}");
        }

        return result;
    }

    final public static Pattern IRIREF_MINUS_CONTAINER = Pattern
            .compile("(?:(?:[^\\x00-\\x20<>\"{}|\\^`\\\\]|" + UCHAR + ")*)|" + Regex.PREFIXED_NAME);

    private void validateIRI(State state, String iri) throws JsonLdError {
        if (!IRIREF_MINUS_CONTAINER.matcher(iri).matches()) {
            throw new JsonLdError(JsonLdError.Error.PARSE_ERROR,
                    "Error while parsing Turtle; invalid IRI after escaping. {line: "
                            + state.lineNumber + "position: " + state.linePosition + "}");
        }
    }

    final private static Pattern PN_LOCAL_ESC_MATCHED = Pattern
            .compile("[\\\\]([_~\\.\\-!$&'\\(\\)*+,;=/?#@%])");

    static String unescapeReserved(String str) {
        if (str != null) {
            final Matcher m = PN_LOCAL_ESC_MATCHED.matcher(str);
            if (m.find()) {
                return m.replaceAll("$1");
            }
        }
        return str;
    }

    private String unquoteString(String value) {
        if (value.startsWith("\"\"\"") || value.startsWith("'''")) {
            return value.substring(3, value.length() - 3);
        } else if (value.startsWith("\"") || value.startsWith("'")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

}
