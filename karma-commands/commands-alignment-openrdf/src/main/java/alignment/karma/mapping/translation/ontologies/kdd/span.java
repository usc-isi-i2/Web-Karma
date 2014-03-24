/*
 * Copyright (c) 2014 CUBRC, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *               http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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

