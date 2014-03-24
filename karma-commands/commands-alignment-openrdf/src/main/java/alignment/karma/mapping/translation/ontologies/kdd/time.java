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

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;


@SuppressWarnings("ALL")
public class time
{
	private static final String PREFIX = "time";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#");
	}

	public static String getNamespace()
	{
		return "http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource Day = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#Day");
	public static final Resource Decade = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#Decade");
	public static final Resource Hour = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#Hour");
	public static final Resource Minute = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#Minute");
	public static final Resource Month = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#Month");
	public static final Resource MultiDayTemporalInterval = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#MultiDayTemporalInterval");
	public static final Resource MultiHourTemporalInterval = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#MultiHourTemporalInterval");
	public static final Resource MultiMinuteTemporalInterval = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#MultiMinuteTemporalInterval");
	public static final Resource MultiMonthTemporalInterval = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#MultiMonthTemporalInterval");
	public static final Resource MultiSecondTemporalInterval = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#MultiSecondTemporalInterval");
	public static final Resource MultiWeekTemporalInterval = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#MultiWeekTemporalInterval");
	public static final Resource MultiYearTemporalInterval = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#MultiYearTemporalInterval");
	public static final Resource Second = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#Second");
	public static final Resource TimeOfDay = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#TimeOfDay");
	public static final Resource Week = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#Week");
	public static final Resource Year = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#Year");

	// FunctionalProperty
	public static final Property has_ending_instant = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#has_ending_instant");
	public static final Property has_starting_instant = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#has_starting_instant");

	// InverseFunctionalProperty
	public static final Property is_ending_instant_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#is_ending_instant_of");
	public static final Property is_starting_instant_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#is_starting_instant_of");

	// ObjectProperty
	public static final Property has_inside_instant = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#has_inside_instant");
	public static final Property instant_is_after = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#instant_is_after");
	public static final Property instant_is_before = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#instant_is_before");
	public static final Property interval_disjoint = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#interval_disjoint");
	public static final Property interval_equals = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#interval_equals");
	public static final Property interval_finished_by = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#interval_finished_by");
	public static final Property interval_finishes = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#interval_finishes");
	public static final Property interval_is_after = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#interval_is_after");
	public static final Property interval_is_before = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#interval_is_before");
	public static final Property interval_meets = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#interval_meets");
	public static final Property interval_met_by = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#interval_met_by");
	public static final Property interval_overlapped_by = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#interval_overlapped_by");
	public static final Property interval_overlaps = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#interval_overlaps");
	public static final Property interval_started_by = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#interval_started_by");
	public static final Property interval_starts = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#interval_starts");
	public static final Property is_inside_instant_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#is_inside_instant_of");

	// TransitiveProperty
	public static final Property interval_contains = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#interval_contains");
	public static final Property interval_during = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TimeOntology#interval_during");
}

