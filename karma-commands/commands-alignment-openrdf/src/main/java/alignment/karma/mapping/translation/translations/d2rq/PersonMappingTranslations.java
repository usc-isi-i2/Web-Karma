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
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import java.util.List;

/**
 * Class PersonMappingTranslations
 *
 * @since 12/03/2013
 */
public class PersonMappingTranslations extends D2rqTranslation
{
	private static Resource personResource;
	public static void translatePersonMappings(Model model, D2rqMapping d2rqMapping)
	{
		if (isThisAttributeMapped(model, BadOntology.Person))
		{
			createPersonResource(d2rqMapping);
			personNameTranslation(model,d2rqMapping);
			personBirthTranslation(model,d2rqMapping);
			personHeightTranslation(model,d2rqMapping);
			personWeightTranslation(model,d2rqMapping);
			personEyeColorTranslation(model, d2rqMapping);
			personHairColorTranslation(model, d2rqMapping);
			personFreeFormTextDescription(model, d2rqMapping);
			personIdentificationDocument(model, d2rqMapping);
			personCitizenshipNationality(model, d2rqMapping);
			//TODO contact info Phone email web
			personPhoneNumber(model, d2rqMapping);
			//TODO addresses home..work
			personHomeAddress(model, d2rqMapping);
		}
	}

	private static void personCitizenshipNationality(Model karmaModel, D2rqMapping d2rqMapping)
	{
		KarmaMappingSparqlQuery kQuery = new KarmaMappingSparqlQuery();
		KarmaSparqlQueryVariable person = new KarmaSparqlQueryVariable(BadOntology.Person);
		KarmaSparqlQueryVariable nationality = new KarmaSparqlQueryVariable(BadOntology.Nationality);
		kQuery.addStatement(person, BadOntology.person_properties, nationality);
		kQuery.setSelectVariables(nationality);
		QueryExecution qe = QueryExecutionFactory.create(kQuery.getQuery(), karmaModel);
		ResultSet rs = qe.execSelect();
		if (rs.hasNext())
		{
			Resource citizen = d2rqMapping.createClassMap("Nationality", agent.CitizenRole);
			d2rqMapping.createPropertyBridge(personResource, ero.has_role, citizen);
			List<String> columnNames = getConnectedColumns(rs.next().get(nationality.variableName), BadOntology.has_value, karmaModel);
			d2rqMapping.translationTableTypeTranslation(citizen, D2rqMapping.translationTables.Citizenship, columnNames);
		}
	}

	private static void personHomeAddress(Model karmaModel, D2rqMapping d2rqMapping)
	{
		KarmaMappingSparqlQuery kQuery = new KarmaMappingSparqlQuery();
		KarmaSparqlQueryVariable person = new KarmaSparqlQueryVariable(BadOntology.Person);
		KarmaSparqlQueryVariable homeAddress = new KarmaSparqlQueryVariable(BadOntology.PersonHome);
		kQuery.addStatement(person, BadOntology.person_properties, homeAddress);
		kQuery.setSelectVariables(homeAddress);
		QueryExecution qe = QueryExecutionFactory.create(kQuery.getQuery(), karmaModel);
		ResultSet rs = qe.execSelect();
		int addressCount = 1;
		while (rs.hasNext())
		{
			Resource actOfInhabatancy = d2rqMapping.createClassMap("HomeLiving" + addressCount, event.ActOfInhabitancy);
			d2rqMapping.createPropertyBridge(personResource, ro.agent_in, actOfInhabatancy);
			Resource facilityByRole = d2rqMapping.createClassMap("Home" + addressCount, artifact.FacilityByRole);
			d2rqMapping.createPropertyBridge(facilityByRole, ro.participates_in, actOfInhabatancy);
			Resource faciltyLocation = d2rqMapping.createClassMap("HomeStreetLocation" + addressCount, amo.FacilityLocation);
			d2rqMapping.createPropertyBridge(facilityByRole, ro.located_in, faciltyLocation);
			Resource facilityLocationIdentifier = d2rqMapping.createClassMap("HomeStreetLocationIdentifier" + addressCount, amo.FacilityLocationIdentifier);
			d2rqMapping.createPropertyBridge(faciltyLocation, info.designated_by, facilityLocationIdentifier);
			Resource facilityStreetAddressBearer = d2rqMapping.createClassMap("HomeStreetLocationIdentifierBearer" + addressCount, amo.FacilityStreetAddressBearer);
			d2rqMapping.createPropertyBridge(facilityLocationIdentifier, ero.inheres_in, facilityStreetAddressBearer);
			List<String> columnNames = getConnectedColumns(rs.next().get(homeAddress.variableName), BadOntology.has_value, karmaModel);
			d2rqMapping.connectToColumn(facilityStreetAddressBearer, info.has_text_value, columnNames);
			addressCount++;
		}
	}

	private static void personPhoneNumber(Model karmaModel, D2rqMapping d2rqMapping)
	{
		KarmaMappingSparqlQuery kQuery = new KarmaMappingSparqlQuery();
		KarmaSparqlQueryVariable person = new KarmaSparqlQueryVariable(BadOntology.Person);
		KarmaSparqlQueryVariable phoneNumber = new KarmaSparqlQueryVariable(BadOntology.PersonPhoneNumber);
		kQuery.addStatement(person, BadOntology.person_properties, phoneNumber);
		kQuery.setSelectVariables(phoneNumber);
		QueryExecution qe = QueryExecutionFactory.create(kQuery.getQuery(), karmaModel);
		ResultSet rs = qe.execSelect();
		int phoneCount = 1;
		while (rs.hasNext())
		{
			Resource phone = d2rqMapping.createClassMap("PersonPhone" + phoneCount, artifact.Telephone);
			d2rqMapping.createPropertyBridge(personResource, amo.uses, phone);
			Resource telecommunicationAddress = d2rqMapping.createClassMap("PersonPhoneNumber" + phoneCount, amo.TelecommunicationNetworkAddress);
			d2rqMapping.createPropertyBridge(phone, info.designated_by, telecommunicationAddress);
			Resource telecommAddressBearer = d2rqMapping.createClassMap("PersonPhoneNumberBearer" + phoneCount, amo.TelecommunicationNetworkAddressBearer);
			d2rqMapping.createPropertyBridge(telecommunicationAddress, ero.inheres_in, telecommAddressBearer);
			List<String> columnNames = getConnectedColumns(rs.next().get(phoneNumber.variableName), BadOntology.has_value, karmaModel);
			d2rqMapping.connectToColumn(telecommAddressBearer, info.has_text_value, columnNames);
			phoneCount++;
		}
	}

	//TODO add optional document governemt & dates.
	private static void personIdentificationDocument(Model karmaModel, D2rqMapping d2rqMapping)
	{
		KarmaMappingSparqlQuery kQuery = new KarmaMappingSparqlQuery();
		KarmaSparqlQueryVariable person = new KarmaSparqlQueryVariable(BadOntology.Person);
		KarmaSparqlQueryVariable document = new KarmaSparqlQueryVariable(BadOntology.PersonDocument);
		kQuery.addStatement(person, BadOntology.person_properties, document);
		kQuery.setSelectVariables(document);
		QueryExecution qe = QueryExecutionFactory.create(kQuery.getQuery(), karmaModel);
		ResultSet rs = qe.execSelect();
		int documentCount = 1;
		while (rs.hasNext())
		{
			Resource contentEntity = d2rqMapping.createClassMap("PersonDocumentNumber" + documentCount, info.DesignativeInformationContentEntity);
			d2rqMapping.createPropertyBridge(personResource, info.designated_by, contentEntity);
			Resource bearingEntity = d2rqMapping.createClassMap("PersonDocumentNumberBearer" + documentCount, info.DesignativeInformationBearingEntity);
			d2rqMapping.createPropertyBridge(contentEntity, ero.inheres_in, bearingEntity);
			Resource governmentDocument = d2rqMapping.createClassMap("PersonGovernmentDocument" + documentCount, artifact.GovernmentDocument);
			d2rqMapping.createPropertyBridge(governmentDocument, ro.has_part, bearingEntity);
			List<String> columnNames = getConnectedColumns(rs.next().get(document.variableName), BadOntology.has_value, karmaModel);
			d2rqMapping.connectToColumn(bearingEntity, info.has_text_value, columnNames);
			documentCount++;
		}
	}

	private static void createPersonResource(D2rqMapping d2rqMapping)
	{
		personResource = d2rqMapping.createClassMap("Person", agent.Person);
		d2rqMapping.createPropertyBridge(personResource, meta.is_mentioned_by,d2rqMapping.getDataRow());
	}

	private static void personFreeFormTextDescription(Model karmaModel, D2rqMapping d2rqMapping)
	{
		KarmaMappingSparqlQuery kQuery = new KarmaMappingSparqlQuery();
		KarmaSparqlQueryVariable person = new KarmaSparqlQueryVariable(BadOntology.Person);
		KarmaSparqlQueryVariable description = new KarmaSparqlQueryVariable(BadOntology.PersonDescription);
		kQuery.addStatement(person, BadOntology.person_properties, description);
		kQuery.setSelectVariables(description);
		QueryExecution qe = QueryExecutionFactory.create(kQuery.getQuery(), karmaModel);
		ResultSet rs = qe.execSelect();
		int descriptionCount = 1;
		while (rs.hasNext())
		{
			Resource contentEntity = d2rqMapping.createClassMap("PersonDescription" + descriptionCount, info.DescriptiveInformationContentEntity);
			d2rqMapping.createPropertyBridge(personResource, info.described_by, contentEntity);
			Resource bearingEntity = d2rqMapping.createClassMap("PersonDescriptionBearer" + descriptionCount, info.DescriptiveInformationBearingEntity);
			d2rqMapping.createPropertyBridge(contentEntity, ero.inheres_in, bearingEntity);
			List<String> columnNames = getConnectedColumns(rs.next().get(description.variableName), BadOntology.has_value, karmaModel);
			d2rqMapping.connectToColumn(bearingEntity, info.has_text_value, columnNames);
			descriptionCount++;
		}
	}

	public static void personNameTranslation(Model karmaModel, D2rqMapping d2rqMapping) {
		KarmaMappingSparqlQuery kQuery = new KarmaMappingSparqlQuery();
		KarmaSparqlQueryVariable person = new KarmaSparqlQueryVariable(BadOntology.Person);
		KarmaSparqlQueryVariable name = new KarmaSparqlQueryVariable(BadOntology.PersonName);
		kQuery.addStatement(person, BadOntology.person_properties, name);
		kQuery.setSelectVariables(name);
		QueryExecution qe = QueryExecutionFactory.create(kQuery.getQuery(),karmaModel);
		ResultSet rs = qe.execSelect();
		int personNameCount = 1;
		while (rs.hasNext())
		{
			Resource personName = d2rqMapping.createClassMap("PersonName" + personNameCount, amo.PersonName);
			d2rqMapping.createPropertyBridge(personResource,info.designated_by,personName);
			Resource personNameBearer = d2rqMapping.createClassMap("PersonNameBearer" + personNameCount, amo.PersonNameBearer);
			d2rqMapping.createPropertyBridge(personName, ero.inheres_in,personNameBearer);
			List<String> columnNames = getConnectedColumns(rs.next().get(name.variableName), BadOntology.has_value, karmaModel);
			d2rqMapping.connectToColumn(personNameBearer,info.has_text_value,columnNames);
			personNameCount++;
		}
	}

	public static void personHeightTranslation(Model karmaModel, D2rqMapping d2rqMapping) {
		KarmaMappingSparqlQuery kQuery = new KarmaMappingSparqlQuery();
		KarmaSparqlQueryVariable person = new KarmaSparqlQueryVariable(BadOntology.Person);
		KarmaSparqlQueryVariable height = new KarmaSparqlQueryVariable(BadOntology.PersonHeight);
		kQuery.addStatement(person, BadOntology.person_properties, height);
		kQuery.setSelectVariables(height);
		QueryExecution qe = QueryExecutionFactory.create(kQuery.getQuery(),karmaModel);
		ResultSet rs = qe.execSelect();
		if(rs.hasNext()) {
			Resource heightQuality = d2rqMapping.createClassMap("HeightQuality", quality.Height);
			d2rqMapping.createPropertyBridge(personResource,ero.has_quality,heightQuality);
			Resource heightQualityMeasurement = d2rqMapping.createClassMap("HeightQualityMeasurement",info.RatioMeasurementInformationContentEntity);
			d2rqMapping.createPropertyBridge(heightQuality, info.is_measured_by, heightQualityMeasurement);
			Resource heightQualityMeasurementBearer = d2rqMapping.createClassMap("HeightQualityMeasurement",info.RatioMeasurementInformationBearingEntity);
			d2rqMapping.createPropertyBridge(heightQualityMeasurement, ero.inheres_in, heightQualityMeasurementBearer);
			List<String> columnNames = getConnectedColumns(rs.next().get(height.variableName), BadOntology.has_value, karmaModel);
			d2rqMapping.connectToColumn(heightQualityMeasurementBearer,info.has_text_value,columnNames);
		}
	}

	public static void personWeightTranslation(Model karmaModel, D2rqMapping d2rqMapping) {
		KarmaMappingSparqlQuery kQuery = new KarmaMappingSparqlQuery();
		KarmaSparqlQueryVariable person = new KarmaSparqlQueryVariable(BadOntology.Person);
		KarmaSparqlQueryVariable weigth = new KarmaSparqlQueryVariable(BadOntology.PersonWeight);
		kQuery.addStatement(person, BadOntology.person_properties, weigth);
		kQuery.setSelectVariables(weigth);
		QueryExecution qe = QueryExecutionFactory.create(kQuery.getQuery(),karmaModel);
		ResultSet rs = qe.execSelect();
		if(rs.hasNext()) {
			Resource weightQuality = d2rqMapping.createClassMap("WeightQuality", quality.Weight);
			d2rqMapping.createPropertyBridge(personResource,ero.has_quality,weightQuality);
			Resource weightQualityMeasurement = d2rqMapping.createClassMap("WeightQualityMeasurement",info.RatioMeasurementInformationContentEntity);
			d2rqMapping.createPropertyBridge(weightQuality, info.is_measured_by, weightQualityMeasurement);
			Resource weightQualityMeasurementBearer = d2rqMapping.createClassMap("WeightQualityMeasurement",info.RatioMeasurementInformationBearingEntity);
			d2rqMapping.createPropertyBridge(weightQualityMeasurement, ero.inheres_in, weightQualityMeasurementBearer);
			List<String> columnNames = getConnectedColumns(rs.next().get(weigth.variableName), BadOntology.has_value, karmaModel);
			d2rqMapping.connectToColumn(weightQualityMeasurementBearer,info.has_text_value,columnNames);
		}
	}

	public static void personHairColorTranslation(Model karmaModel, D2rqMapping d2rqMapping) {
		KarmaMappingSparqlQuery kQuery = new KarmaMappingSparqlQuery();
		KarmaSparqlQueryVariable person = new KarmaSparqlQueryVariable(BadOntology.Person);
		KarmaSparqlQueryVariable hairColorKarma = new KarmaSparqlQueryVariable(BadOntology.HairColor);
		kQuery.addStatement(person, BadOntology.person_properties, hairColorKarma);
		kQuery.setSelectVariables(hairColorKarma);
		QueryExecution qe = QueryExecutionFactory.create(kQuery.getQuery(),karmaModel);
		ResultSet rs = qe.execSelect();
		if(rs.hasNext()) {
			Resource hair = d2rqMapping.createClassMap("HairOfHead",agent.HairOfHead);
			d2rqMapping.createPropertyBridge(personResource,ro.has_part,hair);
			Resource hairColor = d2rqMapping.createClassMap("HairColor",quality.Color);
			d2rqMapping.createPropertyBridge(hair,ero.has_quality,hairColor);
			List<String> columnNames = getConnectedColumns(rs.next().get(hairColorKarma.variableName), BadOntology.has_value, karmaModel);
			d2rqMapping.translationTableTypeTranslation(hairColor, D2rqMapping.translationTables.Color, columnNames);
		}
	}

	public static void personEyeColorTranslation(Model karmaModel, D2rqMapping d2rqMapping) {
		KarmaMappingSparqlQuery kQuery = new KarmaMappingSparqlQuery();
		KarmaSparqlQueryVariable person = new KarmaSparqlQueryVariable(BadOntology.Person);
		KarmaSparqlQueryVariable eyeColorKarma = new KarmaSparqlQueryVariable(BadOntology.HairColor);
		kQuery.addStatement(person, BadOntology.person_properties, eyeColorKarma);
		kQuery.setSelectVariables(eyeColorKarma);
		QueryExecution qe = QueryExecutionFactory.create(kQuery.getQuery(),karmaModel);
		ResultSet rs = qe.execSelect();
		if(rs.hasNext()) {
			Resource eyes = d2rqMapping.createClassMap("SetOfEyes",agent.SetOfEyes);
			d2rqMapping.createPropertyBridge(personResource,ro.has_part,eyes);
			Resource eyesColor = d2rqMapping.createClassMap("SetOfEyesColor",quality.Color);
			d2rqMapping.createPropertyBridge(eyes,ero.has_quality,eyesColor);
			List<String> columnNames = getConnectedColumns(rs.next().get(eyeColorKarma.variableName), BadOntology.has_value, karmaModel);
			d2rqMapping.translationTableTypeTranslation(eyesColor, D2rqMapping.translationTables.Color, columnNames);
		}
	}

	public static void personBirthTranslation(Model karmaModel, D2rqMapping d2rqMapping) {
		if (isThisAttributeMapped(karmaModel, BadOntology.Birth))
		{
			KarmaMappingSparqlQuery kQuery = new KarmaMappingSparqlQuery();
			KarmaSparqlQueryVariable person = new KarmaSparqlQueryVariable(BadOntology.Person);
			KarmaSparqlQueryVariable birth = new KarmaSparqlQueryVariable(BadOntology.Birth);
			KarmaSparqlQueryVariable birthLocation = null;
			KarmaSparqlQueryVariable birthDate = null;
			kQuery.addStatement(person, BadOntology.person_properties, birth);
			if (isThisAttributeMapped(karmaModel, BadOntology.BirthLocation))
			{
				birthLocation = new KarmaSparqlQueryVariable(BadOntology.BirthLocation);
				kQuery.addStatement(birth, BadOntology.has_birth_location, birthLocation);
			}
			if (isThisAttributeMapped(karmaModel, BadOntology.BirthDate))
			{
				birthDate = new KarmaSparqlQueryVariable(BadOntology.BirthDate);
				kQuery.addStatement(birth, BadOntology.has_birth_date, birthDate);
			}
			kQuery.setSelectVariables(birthDate,birthLocation);
			QueryExecution qe = QueryExecutionFactory.create(kQuery.getQuery(),karmaModel);
			ResultSet rs = qe.execSelect();
			if(rs.hasNext()) {
				Resource birthEvent = d2rqMapping.createClassMap("BirthEvent", event.Birth);
				d2rqMapping.createPropertyBridge(personResource,ro.participates_in,birthEvent);
				while (rs.hasNext()) {
					QuerySolution qs = rs.next();
					if(birthLocation!=null) {
						//connect to person
						Resource geospatialLocation = d2rqMapping.createClassMap("BirthLocation",geo.GeospatialLocation);
						d2rqMapping.createPropertyBridge(birthEvent,ero.occurs_at,geospatialLocation);
						Resource geospatialLocationIdentifier = d2rqMapping.createClassMap("BirthLocationIdentifier",amo.GeospatialLocationIdentifier);
						d2rqMapping.createPropertyBridge(geospatialLocation,info.designated_by,geospatialLocationIdentifier);
						Resource geospatialLocationIdentiferBearer = d2rqMapping.createClassMap("BirthLocationNameBearer", amo.GeospatialLocationNameBearer);
						d2rqMapping.createPropertyBridge(geospatialLocationIdentifier,ero.inheres_in,geospatialLocationIdentiferBearer);
						//get columns
						List<String> birthColumns = getConnectedColumns(qs.get(birthLocation.variableName), BadOntology.has_value, karmaModel);
						d2rqMapping.connectToColumn(geospatialLocationIdentiferBearer,info.has_text_value,birthColumns);
					}
					if(birthDate!=null) {
						//connect to person
						Resource temporalInterval = d2rqMapping.createClassMap("BirthDate",span.TemporalInterval);
						d2rqMapping.createPropertyBridge(birthEvent,ero.occurs_on,temporalInterval);
						Resource temporalIntervalIdentifier = d2rqMapping.createClassMap("BirthDateIdentifier",amo.TemporalIntervalIdentifier);
						d2rqMapping.createPropertyBridge(temporalInterval,info.designated_by,temporalIntervalIdentifier);
						Resource temporalIntervalIdentiferBearer = d2rqMapping.createClassMap("BirthDateIdentifierBearer",amo.TemporalIntervalIdentifierBearer);
						d2rqMapping.createPropertyBridge(temporalIntervalIdentifier,ero.inheres_in,temporalIntervalIdentiferBearer);
						//get columns
						List<String> columnNames = getConnectedColumns(qs.get(birthDate.variableName), BadOntology.has_value, karmaModel);
						d2rqMapping.connectToColumn(temporalIntervalIdentiferBearer,info.has_text_value,columnNames);
					}
				}
			}
		}
	}
}
