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
public class swrlb
{
	private static final String PREFIX = "swrlb";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.w3.org/2003/11/swrlb#");
	}

	public static String getNamespace()
	{
		return "http://www.w3.org/2003/11/swrlb#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// DatatypeProperty
	public static final Property args = ResourceFactory.createProperty("http://www.w3.org/2003/11/swrlb#args");
	public static final Property maxArgs = ResourceFactory.createProperty("http://www.w3.org/2003/11/swrlb#maxArgs");
	public static final Property minArgs = ResourceFactory.createProperty("http://www.w3.org/2003/11/swrlb#minArgs");
}

