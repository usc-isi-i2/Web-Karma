/**************************************************************************************************************************************
 * Copyright (c) 2013 CUBRC, Inc.                                                                                                     *
 * Unpublished Work - all rights reserved under the copyright laws of the United States.                                              *
 * CUBRC, Inc. does not grant permission to any party outside the Government                                                          *
 * to use, disclose, copy, or make derivative works of this software.                                                                 *
 **************************************************************************************************************************************/

package alignment.karma.mapping.translation.ontologies;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;


@SuppressWarnings("ALL")
public class BadOntology
{
	private static final String PREFIX = "BadOntology";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.cubrc.org/BO#");
	}

	public static String getNamespace()
	{
		return "http://www.cubrc.org/BO#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource AssociatePerson = ResourceFactory.createResource("http://www.cubrc.org/BO#AssociatePerson");
	public static final Resource Birth = ResourceFactory.createResource("http://www.cubrc.org/BO#Birth");
	public static final Resource BirthDate = ResourceFactory.createResource("http://www.cubrc.org/BO#BirthDate");
	public static final Resource BirthLocation = ResourceFactory.createResource("http://www.cubrc.org/BO#BirthLocation");
	public static final Resource Death = ResourceFactory.createResource("http://www.cubrc.org/BO#Death");
	public static final Resource DeathDate = ResourceFactory.createResource("http://www.cubrc.org/BO#DeathDate");
	public static final Resource DeathLocation = ResourceFactory.createResource("http://www.cubrc.org/BO#DeathLocation");
	public static final Resource Event = ResourceFactory.createResource("http://www.cubrc.org/BO#Event");
	public static final Resource EventDate = ResourceFactory.createResource("http://www.cubrc.org/BO#EventDate");
	public static final Resource EventDescription = ResourceFactory.createResource("http://www.cubrc.org/BO#EventDescription");
	public static final Resource EventLocation = ResourceFactory.createResource("http://www.cubrc.org/BO#EventLocation");
	public static final Resource EventParticipant = ResourceFactory.createResource("http://www.cubrc.org/BO#EventParticipant");
	public static final Resource EventType = ResourceFactory.createResource("http://www.cubrc.org/BO#EventType");
	public static final Resource EyeColor = ResourceFactory.createResource("http://www.cubrc.org/BO#EyeColor");
	public static final Resource FamilyPerson = ResourceFactory.createResource("http://www.cubrc.org/BO#FamilyPerson");
	public static final Resource Father = ResourceFactory.createResource("http://www.cubrc.org/BO#Father");
	public static final Resource HairColor = ResourceFactory.createResource("http://www.cubrc.org/BO#HairColor");
	public static final Resource LanguageSkill = ResourceFactory.createResource("http://www.cubrc.org/BO#LanguageSkill");
	public static final Resource LifeEvents = ResourceFactory.createResource("http://www.cubrc.org/BO#LifeEvents");
	public static final Resource Marrage = ResourceFactory.createResource("http://www.cubrc.org/BO#Marrage");
	public static final Resource MarrageDate = ResourceFactory.createResource("http://www.cubrc.org/BO#MarrageDate");
	public static final Resource MarrageLocation = ResourceFactory.createResource("http://www.cubrc.org/BO#MarrageLocation");
	public static final Resource Mother = ResourceFactory.createResource("http://www.cubrc.org/BO#Mother");
	public static final Resource Nationality = ResourceFactory.createResource("http://www.cubrc.org/BO#Nationality");
	public static final Resource ParticipantOrganization = ResourceFactory.createResource("http://www.cubrc.org/BO#ParticipantOrganization");
	public static final Resource ParticipantPerson = ResourceFactory.createResource("http://www.cubrc.org/BO#ParticipantPerson");
	public static final Resource Person = ResourceFactory.createResource("http://www.cubrc.org/BO#Person");
	public static final Resource PersonAssociation = ResourceFactory.createResource("http://www.cubrc.org/BO#PersonAssociation");
	public static final Resource PersonAttribute = ResourceFactory.createResource("http://www.cubrc.org/BO#PersonAttribute");
	public static final Resource PersonDescription = ResourceFactory.createResource("http://www.cubrc.org/BO#PersonDescription");
	public static final Resource PersonDocument = ResourceFactory.createResource("http://www.cubrc.org/BO#PersonDocument");
	public static final Resource PersonHeight = ResourceFactory.createResource("http://www.cubrc.org/BO#PersonHeight");
	public static final Resource PersonIdentification = ResourceFactory.createResource("http://www.cubrc.org/BO#PersonIdentification");
	public static final Resource PersonName = ResourceFactory.createResource("http://www.cubrc.org/BO#PersonName");
	public static final Resource PersonSkill = ResourceFactory.createResource("http://www.cubrc.org/BO#PersonSkill");
	public static final Resource PersonWeight = ResourceFactory.createResource("http://www.cubrc.org/BO#PersonWeight");
	public static final Resource Sex = ResourceFactory.createResource("http://www.cubrc.org/BO#Sex");

	// DatatypeProperty
	public static final Property has_value = ResourceFactory.createProperty("http://www.cubrc.org/BO#has_value");

	// ObjectProperty
	public static final Property event_has_date = ResourceFactory.createProperty("http://www.cubrc.org/BO#event_has_date");
	public static final Property event_has_description = ResourceFactory.createProperty("http://www.cubrc.org/BO#event_has_description");
	public static final Property event_has_location = ResourceFactory.createProperty("http://www.cubrc.org/BO#event_has_location");
	public static final Property event_participant = ResourceFactory.createProperty("http://www.cubrc.org/BO#event_participant");
	public static final Property event_properties = ResourceFactory.createProperty("http://www.cubrc.org/BO#event_properties");
	public static final Property event_type_mention = ResourceFactory.createProperty("http://www.cubrc.org/BO#event_type_mention");
	public static final Property has_birth_date = ResourceFactory.createProperty("http://www.cubrc.org/BO#has_birth_date");
	public static final Property has_birth_location = ResourceFactory.createProperty("http://www.cubrc.org/BO#has_birth_location");
	public static final Property has_death_date = ResourceFactory.createProperty("http://www.cubrc.org/BO#has_death_date");
	public static final Property has_death_location = ResourceFactory.createProperty("http://www.cubrc.org/BO#has_death_location");
	public static final Property has_marrage_date = ResourceFactory.createProperty("http://www.cubrc.org/BO#has_marrage_date");
	public static final Property has_marrage_location = ResourceFactory.createProperty("http://www.cubrc.org/BO#has_marrage_location");
	public static final Property person_dates = ResourceFactory.createProperty("http://www.cubrc.org/BO#person_dates");
	public static final Property person_locations = ResourceFactory.createProperty("http://www.cubrc.org/BO#person_locations");
	public static final Property person_properties = ResourceFactory.createProperty("http://www.cubrc.org/BO#person_properties");
}

