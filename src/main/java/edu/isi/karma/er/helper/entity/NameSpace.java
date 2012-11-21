package edu.isi.karma.er.helper.entity;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class NameSpace {

	public static final String PREFIX_MATCH = "http://www.isi.edu/ontology/Match/";
	public static final String PREFIX_PROV = "http://www.w3.org/ns/prov#";
	public static final String PREFIX_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String PREFIX_SKOS = "http://www.w3.org/2004/02/skos/core#";
	public static final String PREFIX_RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	public static final String PREFIX_SAAM = "http://americanart.si.edu/saam/";
	public static final String PREFIX_DCTERM = "http://purl.org/dc/terms/";
	public static final String PREFIX_DBPEDIA_OWL = "http://dbpedia.org/ontology/";
	public static final String PREFIX_NYTIMES = "http://data.nytimes.com/elements/";
	public static final String PREFIX_OWL = "http://www.w3.org/2002/07/owl#";
	
	public static final Property DCTERM_HAS_VERSION = ResourceFactory.createProperty(PREFIX_DCTERM + "hasVersion");
	
	public static final Property MATCH_HAS_MATCH_SOURCE = ResourceFactory.createProperty(PREFIX_MATCH + "hasMatchSource");
	public static final Property MATCH_HAS_MATCH_TARGET = ResourceFactory.createProperty(PREFIX_MATCH + "hasMatchTarget");
	public static final Property MATCH_HAS_MATCH_TYPE = ResourceFactory.createProperty(PREFIX_MATCH + "hasMatchType");
	public static final Property MATCH_HAS_SCORE = ResourceFactory.createProperty(PREFIX_MATCH + "hasScore");
	public static final Property MATCH_SEE_ALSO_IN_SI = ResourceFactory.createProperty(PREFIX_MATCH + "seeAlsoInSmithsonian");
	public static final Property MATCH_SEE_ALSO_IN_WIKI = ResourceFactory.createProperty(PREFIX_MATCH + "seeAlsoInWikipedia");
	
	public static final Property NYTIMES_SEARCH_API_QUERY = ResourceFactory.createProperty(PREFIX_NYTIMES + "search_api_query");
	public static final Property NYTIMES_TOPIC_PAGE = ResourceFactory.createProperty(PREFIX_NYTIMES + "topicPage");
	
	public static final Property OWL_SAME_AS = ResourceFactory.createProperty(PREFIX_OWL + "sameAs");
	
	public static final Property PROV_GENERATED = ResourceFactory.createProperty(PREFIX_PROV + "generated");
	public static final Property PROV_GENERATED_AT_TIME = ResourceFactory.createProperty(PREFIX_PROV + "generatedAtTime");
	public static final Property PROV_HAD_MEMBER = ResourceFactory.createProperty(PREFIX_PROV + "hadMember");
	public static final Property PROV_WAS_ASSOCIATED_WITH = ResourceFactory.createProperty(PREFIX_PROV + "wasAssociatedWith");
	public static final Property PROV_WAS_ATTRIBUTED_TO = ResourceFactory.createProperty(PREFIX_PROV + "wasAttributedTo");
	public static final Property PROV_WAS_GENERATED_BY = ResourceFactory.createProperty(PREFIX_PROV + "wasGeneratedBy");
	public static final Property PROV_WAS_INFLUENCED_BY = ResourceFactory.createProperty(PREFIX_PROV + "wasInfluencedBy");
	public static final Property PROV_WAS_QUOTED_FROM = ResourceFactory.createProperty(PREFIX_PROV + "wasQuotedFrom");
	public static final Property PROV_WAS_REVISION_OF = ResourceFactory.createProperty(PREFIX_PROV + "wasRevisionOf");
	public static final Property PROV_USED = ResourceFactory.createProperty(PREFIX_PROV + "used");
	public static final Property PROV_VALUE = ResourceFactory.createProperty(PREFIX_PROV + "value");
	
	public static final Property RDF_PREDICATE = ResourceFactory.createProperty(PREFIX_RDF + "predicate");
	public static final Property RDF_TYPE = ResourceFactory.createProperty(PREFIX_RDF + "type");
	public static final Property RDFS_COMMENT = ResourceFactory.createProperty(PREFIX_RDFS + "comment");
	public static final Property RDFS_LABEL = ResourceFactory.createProperty(PREFIX_RDFS + "label");
	
	public static final Property SKOS_NOTE = ResourceFactory.createProperty(PREFIX_SKOS + "note");
	public static final Property SKOS_PREF_LABEL = ResourceFactory.createProperty(PREFIX_SKOS + "prefLabel");
	
	
	
	
	public static final Resource PROV_AGENT = ResourceFactory.createResource(NameSpace.PREFIX_PROV + "Agent");
	public static final Resource PROV_GENERATION = ResourceFactory.createProperty(PREFIX_PROV + "Generation");
	public static final Resource PROV_ENTITY = ResourceFactory.createResource(NameSpace.PREFIX_PROV + "Entity");
	public static final Resource PROV_SOFTWARE_AGENT = ResourceFactory.createResource(NameSpace.PREFIX_PROV + "SoftwareAgent");
	
	public static final Resource MATCH_EXACT_MATCH = ResourceFactory.createResource(NameSpace.PREFIX_MATCH + "ExactMatch");
	public static final Resource MATCH_NOT_MATCH = ResourceFactory.createResource(NameSpace.PREFIX_MATCH + "NotMatch");
	public static final Resource MATCH_UNSURE = ResourceFactory.createResource(NameSpace.PREFIX_MATCH + "Unsure");
	
	public static final Resource SKOS_CONCEPT = ResourceFactory.createResource(PREFIX_SKOS + "concept");
	
	
	
	
}
