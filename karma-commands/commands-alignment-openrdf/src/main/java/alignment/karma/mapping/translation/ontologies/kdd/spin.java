/**************************************************************************************************************************************
 * Copyright (c) 2013 CUBRC, Inc.                                                                                                     *
 * Unpublished Work - all rights reserved under the copyright laws of the United States.                                              *
 * CUBRC, Inc. does not grant permission to any party outside the Government                                                          *
 * to use, disclose, copy, or make derivative works of this software.                                                                 *
 **************************************************************************************************************************************/

package alignment.karma.mapping.translation.ontologies.kdd;

import com.hp.hpl.jena.shared.PrefixMapping;


@SuppressWarnings("ALL")
public class spin
{
	private static final String PREFIX = "spin";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://spinrdf.org/spin#");
	}

	public static String getNamespace()
	{
		return "http://spinrdf.org/spin#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}
}
