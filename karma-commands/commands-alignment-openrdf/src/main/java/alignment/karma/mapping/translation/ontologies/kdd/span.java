/**************************************************************************************************************************************
 * Copyright (c) 2013 CUBRC, Inc.                                                                                                     *
 * Unpublished Work - all rights reserved under the copyright laws of the United States.                                              *
 * CUBRC, Inc. does not grant permission to any party outside the Government                                                          *
 * to use, disclose, copy, or make derivative works of this software.                                                                 *
 **************************************************************************************************************************************/

package alignment.karma.mapping.translation.ontologies.kdd;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;


@SuppressWarnings("ALL")
public class span
{
	private static final String PREFIX = "span";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.ifomis.org/bfo/1.1/span#");
	}

	public static String getNamespace()
	{
		return "http://www.ifomis.org/bfo/1.1/span#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource ConnectedSpatiotemporalRegion = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/span#ConnectedSpatiotemporalRegion");
	public static final Resource ConnectedTemporalRegion = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/span#ConnectedTemporalRegion");
	public static final Resource FiatProcessPart = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/span#FiatProcessPart");
	public static final Resource Occurrent = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/span#Occurrent");
	public static final Resource Process = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/span#Process");
	public static final Resource ProcessAggregate = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/span#ProcessAggregate");
	public static final Resource ProcessBoundary = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/span#ProcessBoundary");
	public static final Resource ProcessualContext = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/span#ProcessualContext");
	public static final Resource ProcessualEntity = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/span#ProcessualEntity");
	public static final Resource ScatteredSpatiotemporalRegion = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/span#ScatteredSpatiotemporalRegion");
	public static final Resource ScatteredTemporalRegion = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/span#ScatteredTemporalRegion");
	public static final Resource SpatiotemporalInstant = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/span#SpatiotemporalInstant");
	public static final Resource SpatiotemporalInterval = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/span#SpatiotemporalInterval");
	public static final Resource SpatiotemporalRegion = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/span#SpatiotemporalRegion");
	public static final Resource TemporalInstant = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/span#TemporalInstant");
	public static final Resource TemporalInterval = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/span#TemporalInterval");
	public static final Resource TemporalRegion = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/span#TemporalRegion");
}

