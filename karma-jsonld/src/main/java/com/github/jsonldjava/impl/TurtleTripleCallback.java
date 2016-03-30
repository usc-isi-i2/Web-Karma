package com.github.jsonldjava.impl;

import static com.github.jsonldjava.core.JsonLdConsts.RDF_FIRST;
import static com.github.jsonldjava.core.JsonLdConsts.RDF_NIL;
import static com.github.jsonldjava.core.JsonLdConsts.RDF_REST;
import static com.github.jsonldjava.core.JsonLdConsts.XSD_BOOLEAN;
import static com.github.jsonldjava.core.JsonLdConsts.XSD_DOUBLE;
import static com.github.jsonldjava.core.JsonLdConsts.XSD_FLOAT;
import static com.github.jsonldjava.core.JsonLdConsts.XSD_INTEGER;
import static com.github.jsonldjava.core.JsonLdConsts.XSD_STRING;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.jsonldjava.core.JsonLdTripleCallback;
import com.github.jsonldjava.core.RDFDataset;

public class TurtleTripleCallback implements JsonLdTripleCallback {

    private static final int MAX_LINE_LENGTH = 160;
    private static final int TAB_SPACES = 4;
    private static final String COLS_KEY = "..cols.."; // this shouldn't be a
    // valid iri/bnode i
    // hope!
    final Map<String, String> availableNamespaces = new LinkedHashMap<String, String>() {
        {
            // TODO: fill with default namespaces
        }
    };
    Set<String> usedNamespaces;

    public TurtleTripleCallback() {
    }

    @Override
    public Object call(RDFDataset dataset) {
        for (final Entry<String, String> e : dataset.getNamespaces().entrySet()) {
            availableNamespaces.put(e.getValue(), e.getKey());
        }
        usedNamespaces = new LinkedHashSet<>();

        final int tabs = 0;

        final Map<String, List<Object>> refs = new LinkedHashMap<>();
        final Map<String, Map<String, List<Object>>> ttl = new LinkedHashMap<>();

        for (String graphName : dataset.keySet()) {
            final List<RDFDataset.Quad> triples = dataset.getQuads(graphName);
            if ("@default".equals(graphName)) {
                graphName = null;
            }

            // http://www.w3.org/TR/turtle/#unlabeled-bnodes
            // TODO: implement nesting for unlabled nodes

            // map of what the output should look like
            // subj (or [ if bnode) > pred > obj
            // > obj (set ref if IRI)
            // > pred > obj (set ref if bnode)
            // subj > etc etc etc

            // subjid -> [ ref, ref, ref ]

            String prevSubject = "";
            String prevPredicate = "";

            Map<String, List<Object>> thisSubject = null;
            List<Object> thisPredicate = null;

            for (final RDFDataset.Quad triple : triples) {
                final String subject = triple.getSubject().getValue();
                final String predicate = triple.getPredicate().getValue();

                if (prevSubject.equals(subject)) {
                    if (prevPredicate.equals(predicate)) {
                        // nothing to do
                    } else {
                        // new predicate
                        if (thisSubject.containsKey(predicate)) {
                            thisPredicate = thisSubject.get(predicate);
                        } else {
                            thisPredicate = new ArrayList<>();
                            thisSubject.put(predicate, thisPredicate);
                        }
                        prevPredicate = predicate;
                    }
                } else {
                    // new subject
                    if (ttl.containsKey(subject)) {
                        thisSubject = ttl.get(subject);
                    } else {
                        thisSubject = new LinkedHashMap<>();
                        ttl.put(subject, thisSubject);
                    }
                    if (thisSubject.containsKey(predicate)) {
                        thisPredicate = thisSubject.get(predicate);
                    } else {
                        thisPredicate = new ArrayList<>();
                        thisSubject.put(predicate, thisPredicate);
                    }

                    prevSubject = subject;
                    prevPredicate = predicate;
                }

                if (triple.getObject().isLiteral()) {
                    thisPredicate.add(triple.getObject());
                } else {
                    final String o = triple.getObject().getValue();
                    if (o.startsWith("_:")) {
                        // add ref to o
                        if (!refs.containsKey(o)) {
                            refs.put(o, new ArrayList<>());
                        }
                        refs.get(o).add(thisPredicate);
                    }
                    thisPredicate.add(o);
                }
            }
        }

        final Map<String, List<Object>> collections = new LinkedHashMap<>();

        final List<String> subjects = new ArrayList<>(ttl.keySet());
        // find collections
        for (final String subj : subjects) {
            Map<String, List<Object>> preds = ttl.get(subj);
            if (preds != null && preds.containsKey(RDF_FIRST)) {
                final List<Object> col = new ArrayList<>();
                collections.put(subj, col);
                while (true) {
                    final List<Object> first = preds.remove(RDF_FIRST);
                    final Object o = first.get(0);
                    col.add(o);
                    // refs
                    if (refs.containsKey(o)) {
                        refs.get(o).remove(first);
                        refs.get(o).add(col);
                    }
                    final String next = (String) preds.remove(RDF_REST).get(0);
                    if (RDF_NIL.equals(next)) {
                        // end of this list
                        break;
                    }
                    // if collections already contains a value for "next", add
                    // it to this col and break out
                    if (collections.containsKey(next)) {
                        col.addAll(collections.remove(next));
                        break;
                    }
                    preds = ttl.remove(next);
                    refs.remove(next);
                }
            }
        }

        // process refs (nesting referenced bnodes if only one reference to them
        // in the whole graph)
        for (final Entry<String, List<Object>> stringListEntry : refs.entrySet()) {
            // skip items if there is more than one reference to them in the
            // graph
            if (stringListEntry.getValue().size() > 1) {
                continue;
            }

            // otherwise embed them into the referenced location
            Object object = ttl.remove(stringListEntry.getKey());
            if (collections.containsKey(stringListEntry.getKey())) {
                object = new LinkedHashMap<String, List<Object>>();
                final List<Object> tmp = new ArrayList<>();
                tmp.add(collections.remove(stringListEntry.getKey()));
                ((HashMap<String, Object>) object).put(COLS_KEY, tmp);
            }
            final List<Object> predicate = (List<Object>) stringListEntry.getValue().get(0);
            // replace the one bnode ref with the object
            predicate.set(predicate.lastIndexOf(stringListEntry.getKey()), object);
        }

        // replace the rest of the collections
        for (final Entry<String, List<Object>> stringListEntry : collections.entrySet()) {
            final Map<String, List<Object>> subj = ttl.get(stringListEntry.getKey());
            if (!subj.containsKey(COLS_KEY)) {
                subj.put(COLS_KEY, new ArrayList<>());
            }
            subj.get(COLS_KEY).add(stringListEntry.getValue());
        }

        // build turtle output
        final String output = generateTurtle(ttl, 0, 0, false);

        String prefixes = "";
        for (final String prefix : usedNamespaces) {
            final String name = availableNamespaces.get(prefix);
            prefixes += "@prefix " + name + ": <" + prefix + "> .\n";
        }

        return ("".equals(prefixes) ? "" : prefixes + "\n") + output;
    }

    private String generateObject(Object object, String sep, boolean hasNext, int indentation,
            int lineLength) {
        String rval = "";
        String obj;
        if (object instanceof String) {
            obj = getURI((String) object);
        } else if (object instanceof RDFDataset.Literal) {
            obj = ((RDFDataset.Literal) object).getValue();
            final String lang = ((RDFDataset.Literal) object).getLanguage();
            final String dt = ((RDFDataset.Literal) object).getDatatype();
            if (lang != null) {
                obj = "\"" + obj + "\"";
                obj += "@" + lang;
            } else if (dt != null) {
                // TODO: this probably isn't an exclusive list of all the
                // datatype literals that can be represented as native types
                if (!(XSD_DOUBLE.equals(dt) || XSD_INTEGER.equals(dt) || XSD_FLOAT.equals(dt) || XSD_BOOLEAN
                        .equals(dt))) {
                    obj = "\"" + obj + "\"";
                    if (!XSD_STRING.equals(dt)) {
                        obj += "^^" + getURI(dt);
                    }
                }
            } else {
                obj = "\"" + obj + "\"";
            }
        } else {
            // must be an object
            final Map<String, Map<String, List<Object>>> tmp = new LinkedHashMap<>();
            tmp.put("_:x", (Map<String, List<Object>>) object);
            obj = generateTurtle(tmp, indentation + 1, lineLength, true);
        }

        final int idxofcr = obj.indexOf("\n");
        // check if output will fix in the max line length (factor in comma if
        // not the last item, current line length and length to the next CR)
        if ((hasNext ? 1 : 0) + lineLength + (idxofcr != -1 ? idxofcr : obj.length()) > MAX_LINE_LENGTH) {
            rval += "\n" + tabs(indentation + 1);
            lineLength = (indentation + 1) * TAB_SPACES;
        }
        rval += obj;
        if (idxofcr != -1) {
            lineLength += (obj.length() - obj.lastIndexOf("\n"));
        } else {
            lineLength += obj.length();
        }
        if (hasNext) {
            rval += sep;
            lineLength += sep.length();
            if (lineLength < MAX_LINE_LENGTH) {
                rval += " ";
                lineLength++;
            } else {
                rval += "\n";
            }
        }
        return rval;
    }

    private String generateTurtle(Map<String, Map<String, List<Object>>> ttl, int indentation,
            int lineLength, boolean isObject) {
        String rval = "";
        final Iterator<String> subjIter = ttl.keySet().iterator();
        while (subjIter.hasNext()) {
            final String subject = subjIter.next();
            final Map<String, List<Object>> subjval = ttl.get(subject);
            // boolean isBlankNode = subject.startsWith("_:");
            boolean hasOpenBnodeBracket = false;
            if (subject.startsWith("_:")) {
                // only open blank node bracket the node doesn't contain any
                // collections
                if (!subjval.containsKey(COLS_KEY)) {
                    rval += "[ ";
                    lineLength += 2;
                    hasOpenBnodeBracket = true;
                }

                // TODO: according to http://www.rdfabout.com/demo/validator/
                // 1) collections as objects cannot contain any predicates other
                // than rdf:first and rdf:rest
                // 2) collections cannot be surrounded with [ ]

                // check for collection
                if (subjval.containsKey(COLS_KEY)) {
                    final List<Object> collections = subjval.remove(COLS_KEY);
                    for (final Object collection : collections) {
                        rval += "( ";
                        lineLength += 2;
                        final Iterator<Object> objIter = ((List<Object>) collection).iterator();
                        while (objIter.hasNext()) {
                            final Object object = objIter.next();
                            rval += generateObject(object, "", objIter.hasNext(), indentation,
                                    lineLength);
                            lineLength = rval.length() - rval.lastIndexOf("\n");
                        }
                        rval += " ) ";
                        lineLength += 3;
                    }
                }
                // check for blank node
            } else {
                rval += getURI(subject) + " ";
                lineLength += subject.length() + 1;
            }
            final Iterator<String> predIter = ttl.get(subject).keySet().iterator();
            while (predIter.hasNext()) {
                final String predicate = predIter.next();
                rval += getURI(predicate) + " ";
                lineLength += predicate.length() + 1;
                final Iterator<Object> objIter = ttl.get(subject).get(predicate).iterator();
                while (objIter.hasNext()) {
                    final Object object = objIter.next();
                    rval += generateObject(object, ",", objIter.hasNext(), indentation, lineLength);
                    lineLength = rval.length() - rval.lastIndexOf("\n");
                }
                if (predIter.hasNext()) {
                    rval += " ;\n" + tabs(indentation + 1);
                    lineLength = (indentation + 1) * TAB_SPACES;
                }
            }
            if (hasOpenBnodeBracket) {
                rval += " ]";
            }
            if (!isObject) {
                rval += " .\n";
                if (subjIter.hasNext()) { // add blank space if we have another
                    // object below this
                    rval += "\n";
                }
            }
        }
        return rval;
    }

    // TODO: Assert (TAB_SPACES == 4) otherwise this needs to be edited, and
    // should fail to compile
    private String tabs(int tabs) {
        String rval = "";
        for (int i = 0; i < tabs; i++) {
            rval += "    "; // using spaces for tabs
        }
        return rval;
    }

    /**
     * checks the URI for a prefix, and if one is found, set used prefixes to
     * true
     *
     * @param predicate
     * @return
     */
    private String getURI(String uri) {
        // check for bnode
        if (uri.startsWith("_:")) {
            // return the bnode id
            return uri;
        }
        for (final Entry<String, String> stringStringEntry : availableNamespaces.entrySet()) {
            if (uri.startsWith(stringStringEntry.getKey())) {
                usedNamespaces.add(stringStringEntry.getKey());
                // return the prefixed URI
                return stringStringEntry.getValue() + ":" + uri.substring(stringStringEntry.getKey().length());
            }
        }
        // return the full URI
        return "<" + uri + ">";
    }

}
