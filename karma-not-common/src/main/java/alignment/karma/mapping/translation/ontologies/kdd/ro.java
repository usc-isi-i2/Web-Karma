/**************************************************************************************************************************************
 * Copyright (c) 2013 CUBRC, Inc.                                                                                                     *
 * Unpublished Work - all rights reserved under the copyright laws of the United States.                                              *
 * CUBRC, Inc. does not grant permission to any party outside the Government                                                          *
 * to use, disclose, copy, or make derivative works of this software.                                                                 *
 **************************************************************************************************************************************/

package alignment.karma.mapping.translation.ontologies.kdd;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;


@SuppressWarnings("ALL")
public class ro
{
	private static final String PREFIX = "ro";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.obofoundry.org/ro/ro.owl#");
	}

	public static String getNamespace()
	{
		return "http://www.obofoundry.org/ro/ro.owl#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// ObjectProperty
	public static final Property adjacent_to = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#adjacent_to");
	public static final Property agent_in = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#agent_in");
	public static final Property contained_in = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#contained_in");
	public static final Property contains = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#contains");
	public static final Property has_agent = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#has_agent");
	public static final Property has_participant = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#has_participant");
	public static final Property participates_in = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#participates_in");
	public static final Property relationship = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#relationship");
	public static final Property transformed_into = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#transformed_into");

	// TransitiveProperty
	public static final Property derived_into = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#derived_into");
	public static final Property derives_from = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#derives_from");
	public static final Property has_improper_part = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#has_improper_part");
	public static final Property has_integral_part = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#has_integral_part");
	public static final Property has_part = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#has_part");
	public static final Property has_proper_part = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#has_proper_part");
	public static final Property improper_part_of = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#improper_part_of");
	public static final Property integral_part_of = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#integral_part_of");
	public static final Property located_in = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#located_in");
	public static final Property location_of = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#location_of");
	public static final Property part_of = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#part_of");
	public static final Property preceded_by = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#preceded_by");
	public static final Property precedes = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#precedes");
	public static final Property proper_part_of = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#proper_part_of");
	public static final Property transformation_of = ResourceFactory.createProperty("http://www.obofoundry.org/ro/ro.owl#transformation_of");
}

