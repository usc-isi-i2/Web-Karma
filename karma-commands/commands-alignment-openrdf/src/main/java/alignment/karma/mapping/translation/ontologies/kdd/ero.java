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
public class ero
{
	private static final String PREFIX = "ero";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#");
	}

	public static String getNamespace()
	{
		return "http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// ObjectProperty
	public static final Property accessory_in = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#accessory_in");
	public static final Property accomplice_in = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#accomplice_in");
	public static final Property affects = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#affects");
	public static final Property aggregate_has_disposition = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#aggregate_has_disposition");
	public static final Property aggregate_has_function = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#aggregate_has_function");
	public static final Property aggregate_has_quality = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#aggregate_has_quality");
	public static final Property aggregate_has_role = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#aggregate_has_role");
	public static final Property aggregrate_bearer_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#aggregrate_bearer_of");
	public static final Property bearer_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#bearer_of");
	public static final Property caused_by = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#caused_by");
	public static final Property disposition_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#disposition_of");
	public static final Property disposition_of_aggregate = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#disposition_of_aggregate");
	public static final Property function_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#function_of");
	public static final Property function_of_aggregate = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#function_of_aggregate");
	public static final Property has_accessory = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#has_accessory");
	public static final Property has_accomplice = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#has_accomplice");
	public static final Property has_disposition = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#has_disposition");
	public static final Property has_function = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#has_function");
	public static final Property has_input = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#has_input");
	public static final Property has_output = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#has_output");
	public static final Property has_quality = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#has_quality");
	public static final Property has_role = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#has_role");
	public static final Property inheres_in = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#inheres_in");
	public static final Property inheres_in_aggregate = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#inheres_in_aggregate");
	public static final Property is_affected_by = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#is_affected_by");
	public static final Property is_cause_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#is_cause_of");
	public static final Property is_input_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#is_input_of");
	public static final Property is_output_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#is_output_of");
	public static final Property is_predecessor_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#is_predecessor_of");
	public static final Property is_site_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#is_site_of");
	public static final Property is_successor_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#is_successor_of");
	public static final Property is_temporal_region_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#is_temporal_region_of");
	public static final Property occurs_at = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#occurs_at");
	public static final Property occurs_on = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#occurs_on");
	public static final Property quality_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#quality_of");
	public static final Property quality_of_aggregate = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#quality_of_aggregate");
	public static final Property realized_by = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#realized_by");
	public static final Property realizes = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#realizes");
	public static final Property role_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#role_of");
	public static final Property role_of_aggregate = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#role_of_aggregate");
}

