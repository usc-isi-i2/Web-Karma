/**************************************************************************************************************************************
 * Copyright (c) 2013 CUBRC, Inc.                                                                                                     *
 * Unpublished Work - all rights reserved under the copyright laws of the United States.                                              *
 * CUBRC, Inc. does not grant permission to any party outside the Government                                                          *
 * to use, disclose, copy, or make derivative works of this software.                                                                 *
 **************************************************************************************************************************************/

package alignment.karma.mapping.translation.ontologies.kdd;

import com.hp.hpl.jena.shared.PrefixMapping;


@SuppressWarnings("ALL")
public class bridge
{
	private static final String PREFIX = "bridge";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://purl.org/obo/owl/ro_bfo1-1_bridge#");
	}

	public static String getNamespace()
	{
		return "http://purl.org/obo/owl/ro_bfo1-1_bridge#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}
}

