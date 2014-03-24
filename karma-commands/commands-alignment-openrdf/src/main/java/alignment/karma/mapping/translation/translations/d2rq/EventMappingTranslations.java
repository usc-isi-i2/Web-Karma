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

package alignment.karma.mapping.translation.translations.d2rq;

import alignment.karma.mapping.translation.ontologies.BadOntology;
import alignment.karma.mapping.translation.ontologies.kdd.*;
import alignment.karma.mapping.translation.query.KarmaMappingSparqlQuery;
import alignment.karma.mapping.translation.query.KarmaSparqlQueryVariable;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import java.util.List;

/**
 * Class EventMappingTranslations
 *
 * @since 12/27/2013
 */
public class EventMappingTranslations extends D2rqTranslation
{
	private static Resource eventResource;

	public static void translatEventMappings(Model karmaModel, D2rqMapping d2rqMapping)
	{
		if (isThisAttributeMapped(karmaModel, BadOntology.Event))
		{
			createEventResource(d2rqMapping);
			eventTypeTranslation(karmaModel, d2rqMapping);
			eventLocationTranslation(karmaModel, d2rqMapping);
			eventDateTranslation(karmaModel, d2rqMapping);
			eventOrganizationParticipantTranslation(karmaModel, d2rqMapping);
			eventPersonParticipantTranslation(karmaModel, d2rqMapping);
			eventDescriptionTranslation(karmaModel, d2rqMapping);
		}
	}

	private static void eventDescriptionTranslation(Model karmaModel, D2rqMapping d2rqMapping)
	{
		KarmaMappingSparqlQuery kQuery = new KarmaMappingSparqlQuery();
		KarmaSparqlQueryVariable event = new KarmaSparqlQueryVariable(BadOntology.Event);
		KarmaSparqlQueryVariable description = new KarmaSparqlQueryVariable(BadOntology.EventDescription);
		kQuery.addStatement(event, BadOntology.event_has_description, description);
		kQuery.setSelectVariables(description);
		QueryExecution qe = QueryExecutionFactory.create(kQuery.getQuery(), karmaModel);
		ResultSet rs = qe.execSelect();
		int descriptionCount = 1;
		if (rs.hasNext())
		{
			Resource contentEntity = d2rqMapping.createClassMap("EventDescription" + descriptionCount, info.DescriptiveInformationContentEntity);
			d2rqMapping.createPropertyBridge(eventResource, info.described_by, contentEntity);
			Resource bearingEntity = d2rqMapping.createClassMap("EventDescriptionBearer" + descriptionCount, info.DescriptiveInformationBearingEntity);
			d2rqMapping.createPropertyBridge(contentEntity, ero.inheres_in, bearingEntity);
			List<String> columnNames = getConnectedColumns(rs.next().get(description.variableName), BadOntology.has_value, karmaModel);
			d2rqMapping.connectToColumn(bearingEntity, info.has_text_value, columnNames);
			descriptionCount++;
		}
	}

	private static void eventPersonParticipantTranslation(Model karmaModel, D2rqMapping d2rqMapping)
	{
		KarmaMappingSparqlQuery kQuery = new KarmaMappingSparqlQuery();
		KarmaSparqlQueryVariable event = new KarmaSparqlQueryVariable(BadOntology.Event);
		KarmaSparqlQueryVariable participant = new KarmaSparqlQueryVariable(BadOntology.ParticipantPerson);
		kQuery.addStatement(event, BadOntology.event_participant, participant);
		kQuery.setSelectVariables(participant);
		QueryExecution qe = QueryExecutionFactory.create(kQuery.getQuery(), karmaModel);
		ResultSet rs = qe.execSelect();
		int participantCount = 1;
		if (rs.hasNext())
		{
			Resource participantEntity = d2rqMapping.createClassMap("PersonParticipant" + participantCount, agent.Person);
			d2rqMapping.createPropertyBridge(eventResource, ro.has_participant, participantEntity);
			Resource participantName = d2rqMapping.createClassMap("PersonParticipantName" + participantCount, amo.PersonName);
			d2rqMapping.createPropertyBridge(participantEntity, info.designated_by, participantName);
			Resource participantNameBearer = d2rqMapping.createClassMap("PersonParticipantNameBearer" + participantCount, amo.PersonNameBearer);
			d2rqMapping.createPropertyBridge(participantName, ero.inheres_in, participantNameBearer);
			List<String> columnNames = getConnectedColumns(rs.next().get(participant.variableName), BadOntology.has_value, karmaModel);
			d2rqMapping.connectToColumn(participantNameBearer, info.has_text_value, columnNames);
			participantCount++;
		}
	}

	private static void eventOrganizationParticipantTranslation(Model karmaModel, D2rqMapping d2rqMapping)
	{
		KarmaMappingSparqlQuery kQuery = new KarmaMappingSparqlQuery();
		KarmaSparqlQueryVariable event = new KarmaSparqlQueryVariable(BadOntology.Event);
		KarmaSparqlQueryVariable participant = new KarmaSparqlQueryVariable(BadOntology.ParticipantOrganization);
		kQuery.addStatement(event, BadOntology.event_participant, participant);
		kQuery.setSelectVariables(participant);
		QueryExecution qe = QueryExecutionFactory.create(kQuery.getQuery(), karmaModel);
		ResultSet rs = qe.execSelect();
		int participantCount = 1;
		if (rs.hasNext())
		{
			Resource participantEntity = d2rqMapping.createClassMap("OrganizationParticipant" + participantCount, agent.Organization);
			d2rqMapping.createPropertyBridge(eventResource, ro.has_participant, participantEntity);
			Resource participantName = d2rqMapping.createClassMap("OrganizationParticipantName" + participantCount, amo.OrganizationName);
			d2rqMapping.createPropertyBridge(participantEntity, info.designated_by, participantName);
			Resource participantNameBearer = d2rqMapping.createClassMap("OrganizationParticipantNameBearer" + participantCount, amo.OrganizationNameBearer);
			d2rqMapping.createPropertyBridge(participantName, ero.inheres_in, participantNameBearer);
			List<String> columnNames = getConnectedColumns(rs.next().get(participant.variableName), BadOntology.has_value, karmaModel);
			d2rqMapping.connectToColumn(participantNameBearer, info.has_text_value, columnNames);
			participantCount++;
		}
	}

	private static void eventDateTranslation(Model karmaModel, D2rqMapping d2rqMapping)
	{
		KarmaMappingSparqlQuery kQuery = new KarmaMappingSparqlQuery();
		KarmaSparqlQueryVariable event = new KarmaSparqlQueryVariable(BadOntology.Event);
		KarmaSparqlQueryVariable date = new KarmaSparqlQueryVariable(BadOntology.EventDate);
		kQuery.addStatement(event, BadOntology.event_has_date, date);
		kQuery.setSelectVariables(date);
		QueryExecution qe = QueryExecutionFactory.create(kQuery.getQuery(), karmaModel);
		ResultSet rs = qe.execSelect();
		int dateCount = 1;
		if (rs.hasNext())
		{
			Resource temporalInterval = d2rqMapping.createClassMap("Date" + dateCount, span.TemporalInterval);
			d2rqMapping.createPropertyBridge(eventResource, ero.occurs_on, temporalInterval);
			Resource temporalIntervalIdentifier = d2rqMapping.createClassMap("DateIdentifier" + dateCount, amo.TemporalIntervalIdentifier);
			d2rqMapping.createPropertyBridge(temporalInterval, info.designated_by, temporalIntervalIdentifier);
			Resource temporalIntervalIdentiferBearer = d2rqMapping.createClassMap("DateIdentifierBearer" + dateCount, amo.TemporalIntervalIdentifierBearer);
			d2rqMapping.createPropertyBridge(temporalIntervalIdentifier, ero.inheres_in, temporalIntervalIdentiferBearer);
			//get columns
			List<String> columnNames = getConnectedColumns(rs.next().get(date.variableName), BadOntology.has_value, karmaModel);
			d2rqMapping.connectToColumn(temporalIntervalIdentiferBearer, info.has_text_value, columnNames);
			dateCount++;
		}
	}

	private static void eventLocationTranslation(Model karmaModel, D2rqMapping d2rqMapping)
	{
		KarmaMappingSparqlQuery kQuery = new KarmaMappingSparqlQuery();
		KarmaSparqlQueryVariable event = new KarmaSparqlQueryVariable(BadOntology.Event);
		KarmaSparqlQueryVariable location = new KarmaSparqlQueryVariable(BadOntology.EventLocation);
		kQuery.addStatement(event, BadOntology.event_has_date, location);
		kQuery.setSelectVariables(location);
		QueryExecution qe = QueryExecutionFactory.create(kQuery.getQuery(), karmaModel);
		ResultSet rs = qe.execSelect();
		int locationCount = 1;
		if (rs.hasNext())
		{
			Resource geospatialLocation = d2rqMapping.createClassMap("Location" + locationCount, geo.GeospatialLocation);
			d2rqMapping.createPropertyBridge(eventResource, ero.occurs_at, geospatialLocation);
			Resource geospatialLocationIdentifier = d2rqMapping.createClassMap("LocationIdentifier" + locationCount, amo.GeospatialLocationIdentifier);
			d2rqMapping.createPropertyBridge(geospatialLocation, info.designated_by, geospatialLocationIdentifier);
			Resource geospatialLocationIdentiferBearer = d2rqMapping.createClassMap("LocationNameBearer" + locationCount, amo.GeospatialLocationNameBearer);
			d2rqMapping.createPropertyBridge(geospatialLocationIdentifier, ero.inheres_in, geospatialLocationIdentiferBearer);
			//get columns
			List<String> locationColumns = getConnectedColumns(rs.next().get(location.variableName), BadOntology.has_value, karmaModel);
			d2rqMapping.connectToColumn(geospatialLocationIdentiferBearer, info.has_text_value, locationColumns);
			locationCount++;
		}
	}

	private static void eventTypeTranslation(Model karmaModel, D2rqMapping d2rqMapping)
	{
		KarmaMappingSparqlQuery kQuery = new KarmaMappingSparqlQuery();
		KarmaSparqlQueryVariable event = new KarmaSparqlQueryVariable(BadOntology.Event);
		KarmaSparqlQueryVariable eventType = new KarmaSparqlQueryVariable(BadOntology.EventType);
		kQuery.addStatement(event, BadOntology.event_type_mention, eventType);
		kQuery.setSelectVariables(eventType);
		QueryExecution qe = QueryExecutionFactory.create(kQuery.getQuery(), karmaModel);
		ResultSet rs = qe.execSelect();
		while (rs.hasNext())
		{
			List<String> columnNames = getConnectedColumns(rs.next().get(eventType.variableName), BadOntology.has_value, karmaModel);
			d2rqMapping.translationTableTypeTranslation(eventResource, D2rqMapping.translationTables.EventType, columnNames);
		}
	}

	private static void createEventResource(D2rqMapping d2rqMapping)
	{
		eventResource = d2rqMapping.createClassMap("Event", event.Act);
		d2rqMapping.createPropertyBridge(eventResource, meta.is_mentioned_by, d2rqMapping.getDataRow());
	}
}
