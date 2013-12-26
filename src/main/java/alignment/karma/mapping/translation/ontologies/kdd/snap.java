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
public class snap
{
	private static final String PREFIX = "snap";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.ifomis.org/bfo/1.1/snap#");
	}

	public static String getNamespace()
	{
		return "http://www.ifomis.org/bfo/1.1/snap#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource Continuant = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#Continuant");
	public static final Resource DependentContinuant = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#DependentContinuant");
	public static final Resource Disposition = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#Disposition");
	public static final Resource FiatObjectPart = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#FiatObjectPart");
	public static final Resource Function = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#Function");
	public static final Resource GenericallyDependentContinuant = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#GenericallyDependentContinuant");
	public static final Resource IndependentContinuant = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#IndependentContinuant");
	public static final Resource MaterialEntity = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#MaterialEntity");
	public static final Resource Object = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#Object");
	public static final Resource ObjectAggregate = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#ObjectAggregate");
	public static final Resource ObjectBoundary = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#ObjectBoundary");
	public static final Resource OneDimensionalRegion = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#OneDimensionalRegion");
	public static final Resource Quality = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#Quality");
	public static final Resource RealizableEntity = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#RealizableEntity");
	public static final Resource Role = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#Role");
	public static final Resource Site = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#Site");
	public static final Resource SpatialRegion = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#SpatialRegion");
	public static final Resource SpecificallyDependentContinuant = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#SpecificallyDependentContinuant");
	public static final Resource System = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#System");
	public static final Resource ThreeDimensionalRegion = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#ThreeDimensionalRegion");
	public static final Resource TwoDimensionalRegion = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#TwoDimensionalRegion");
	public static final Resource ZeroDimensionalRegion = ResourceFactory.createResource("http://www.ifomis.org/bfo/1.1/snap#ZeroDimensionalRegion");
}

