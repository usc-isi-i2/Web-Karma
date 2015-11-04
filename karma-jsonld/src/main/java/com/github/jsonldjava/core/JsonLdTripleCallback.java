package com.github.jsonldjava.core;

/**
 *
 * @author Tristan
 *
 *         TODO: in the JSONLD RDF API the callback we're representing here is
 *         QuadCallback which takes a list of quads (subject, predicat, object,
 *         graph). for the moment i'm just going to use the dataset provided by
 *         toRDF but this should probably change in the future
 */
public interface JsonLdTripleCallback {

    /**
     * Construct output based on internal RDF dataset format
     *
     * @param dataset
     *            The format of the dataset is a Map with the following
     *            structure: { GRAPH_1: [ TRIPLE_1, TRIPLE_2, ..., TRIPLE_N ],
     *            GRAPH_2: [ TRIPLE_1, TRIPLE_2, ..., TRIPLE_N ], ... GRAPH_N: [
     *            TRIPLE_1, TRIPLE_2, ..., TRIPLE_N ] }
     *
     *            GRAPH: Is the graph name/IRI. if no graph is present for a
     *            triple, it will be listed under the "@default" graph TRIPLE:
     *            Is a map with the following structure: { "subject" : SUBJECT
     *            "predicate" : PREDICATE "object" : OBJECT }
     *
     *            Each of the values in the triple map are also maps with the
     *            following key-value pairs: "value" : The value of the node.
     *            "subject" can be an IRI or blank node id. "predicate" should
     *            only ever be an IRI "object" can be and IRI or blank node id,
     *            or a literal value (represented as a string) "type" : "IRI" if
     *            the value is an IRI or "blank node" if the value is a blank
     *            node. "object" can also be "literal" in the case of literals.
     *            The value of "object" can also contain the following optional
     *            key-value pairs: "language" : the language value of a string
     *            literal "datatype" : the datatype of the literal. (if not set
     *            will default to XSD:string, if set to null, null will be
     *            used).
     *
     * @return the resulting RDF object in the desired format
     */
    public Object call(RDFDataset dataset);
}
