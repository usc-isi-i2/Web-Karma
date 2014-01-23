/**************************************************************************************************************************************
 * Copyright (c) 2013 CUBRC, Inc.                                                                                                     *
 * Unpublished Work - all rights reserved under the copyright laws of the United States.                                              *
 * CUBRC, Inc. does not grant permission to any party outside the Government                                                          *
 * to use, disclose, copy, or make derivative works of this software.                                                                 *
 **************************************************************************************************************************************/

package alignment.karma.mapping.translation.ontologies.kdd;

import com.hp.hpl.jena.shared.PrefixMapping;


@SuppressWarnings("ALL")
public class purl_rordf
{
	private static final String PREFIX = "purl_rordf";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://purl.org/obo/owl/ro#");
	}

	public static String getNamespace()
	{
		return "http://purl.org/obo/owl/ro#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}
}

