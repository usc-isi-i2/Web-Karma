/**************************************************************************************************************************************
 * Copyright (c) 2013 CUBRC, Inc.                                                                                                     *
 * Unpublished Work - all rights reserved under the copyright laws of the United States.                                              *
 * CUBRC, Inc. does not grant permission to any party outside the Government                                                          *
 * to use, disclose, copy, or make derivative works of this software.                                                                 *
 **************************************************************************************************************************************/

package alignment.karma.mapping.translation.ontologies.kdd;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;


@SuppressWarnings("ALL")
public class meta
{
	private static final String PREFIX = "meta";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#");
	}

	public static String getNamespace()
	{
		return "http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource AnnotateDataElement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#AnnotateDataElement");
	public static final Resource AnnotatePersonNameBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#AnnotatePersonNameBearer");
	public static final Resource AnnotateSentences = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#AnnotateSentences");
	public static final Resource Chunking = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#Chunking");
	public static final Resource Coreference = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#Coreference");
	public static final Resource Justification = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#Justification");
	public static final Resource LexicalDescription = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#LexicalDescription");
	public static final Resource NLP = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#NLP");
	public static final Resource NamedEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#NamedEntity");
	public static final Resource ObsoleteClass = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#ObsoleteClass");
	public static final Resource PartOfSpeech = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#PartOfSpeech");
	public static final Resource Pedigree = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#Pedigree");
	public static final Resource ProcessingChainMetadata = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#ProcessingChainMetadata");
	public static final Resource ProperSequence = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#ProperSequence");
	public static final Resource Provenance = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#Provenance");
	public static final Resource Relationship = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#Relationship");
	public static final Resource SentenceBreaking = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#SentenceBreaking");
	public static final Resource SoftwareAgent = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#SoftwareAgent");
	public static final Resource TextZoning = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#TextZoning");
	public static final Resource Tokenizing = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#Tokenizing");
	public static final Resource VerbEvent = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#VerbEvent");

	// DatatypeProperty
	public static final Property assertionPedigree = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#assertionPedigree");
	public static final Property confidence = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#confidence");
	public static final Property contains_token = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#contains_token");
	public static final Property has_instance_represented_by_token = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#has_instance_represented_by_token");
	public static final Property has_token_representation_count = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#has_token_representation_count");
	public static final Property justification = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#justification");
	public static final Property pedigree = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#pedigree");

	// ObjectProperty
	public static final Property agent = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#agent");
	public static final Property agentParams = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#agentParams");
	public static final Property assertionReference = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#assertionReference");
	public static final Property contains_instance_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#contains_instance_of");
	public static final Property is_mention_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#is_mention_of");
	public static final Property is_mentioned_by = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#is_mentioned_by");
	public static final Property merger_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#merger_of");
	public static final Property merges_into = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/AIRSMetadataOntology#merges_into");
}

