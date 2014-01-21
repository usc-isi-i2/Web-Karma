/**************************************************************************************************************************************
 *                                                                                                                                    *
 * Copyright (c) 2013 CUBRC, Inc.                                                                                                     *
 *                                                                                                                                    *
 * Unpublished Work - all rights reserved under the copyright laws of the United States.                                              *
 * CUBRC, Inc. does not grant permission to any party outside the Government                                                          *
 * to use, disclose, copy, or make derivative works of this software.                                                                 *
 *                                                                                                                                    *
 **************************************************************************************************************************************/

package alignment.karma.mapping.translation.ontologies.kdd;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Class graph
 *
 * @since 08/29/2013
 */
public class graph
{
	private static final String PREFIX = "graph";
	private static final String NAMESPACE = "http://www.cubrc.org/ontologies/KDD/Mid/GraphOntology#";

	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, NAMESPACE);
	}

	public static String getNamespace()
	{
		return NAMESPACE;
	}

	public static final Resource Graph = ResourceFactory.createResource(NAMESPACE + "Graph");
	public static final Resource Node = ResourceFactory.createResource(NAMESPACE + "Node");
	public static final Resource Edge = ResourceFactory.createResource(NAMESPACE + "Edge");
	//public static final Resource



}
