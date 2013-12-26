package alignment.karma.mapping.translation;

import alignment.karma.mapping.translation.ontologies.BadOntology;
import alignment.karma.mapping.translation.ontologies.kdd.*;
import alignment.karma.mapping.translation.ontologies.r2rml;
import alignment.karma.mapping.translation.query.KarmaMappingSparqlQuery;
import alignment.karma.mapping.translation.query.KarmaSparqlQueryVariable;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;

//import org.cubrc.airs.structured.alignment.karma.mapping.translation.ontologies.personMappingOntology;

/**
 * Class PersonMappingTranslations
 *
 * @since 12/03/2013
 */
public class PersonMappingTranslations extends KarmaTranslations
{
	private static Resource personResource;
	public static void translatePersonMappings(Model model, D2rqMapping d2rqMapping)
	{
		StmtIterator stmtIterator = model.listStatements(null, r2rml.clazz, BadOntology.Person);
		if(stmtIterator.hasNext()) {
			createPersonResource(d2rqMapping);
			personNameTranslation(model,d2rqMapping);
			personBirthTranslation(model,d2rqMapping);
			personHeightTranslation(model,d2rqMapping);
			personWeightTranslation(model,d2rqMapping);
			personEyeColorTranslation(model, d2rqMapping);
			personHairColorTranslation(model, d2rqMapping);
		}
	}

	private static void createPersonResource(D2rqMapping d2rqMapping)
	{
		personResource = d2rqMapping.createClassMap("Person", agent.Person);
		d2rqMapping.createPropertyBridge(personResource, meta.is_mentioned_by,d2rqMapping.getDataRow());
	}

	public static void personNameTranslation(Model karmaModel, D2rqMapping d2rqMapping) {
		KarmaMappingSparqlQuery kQuery = new KarmaMappingSparqlQuery();
		KarmaSparqlQueryVariable person = new KarmaSparqlQueryVariable(BadOntology.Person);
		KarmaSparqlQueryVariable name = new KarmaSparqlQueryVariable(BadOntology.PersonName);
		kQuery.addStatement(person, BadOntology.person_properties, name);
		kQuery.setSelectVariables(name);
		QueryExecution qe = QueryExecutionFactory.create(kQuery.getQuery(),karmaModel);
		ResultSet rs = qe.execSelect();
		if(rs.hasNext()) {
			Resource personName = d2rqMapping.createClassMap("PersonName", amo.PersonName);
			d2rqMapping.createPropertyBridge(personResource,info.designated_by,personName);
			Resource personNameBearer = d2rqMapping.createClassMap("PersonNameBearer",amo.PersonNameBearer);
			d2rqMapping.createPropertyBridge(personName, ero.inheres_in,personNameBearer);
			List<String> columnNames = getConnectedColumns(rs.next().get(name.variableName), BadOntology.has_value, karmaModel);
			d2rqMapping.connectToColumn(personNameBearer,info.has_text_value,columnNames);
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
			for(String columnName: columnNames) {
				d2rqMapping.createPropertyBridge(hairColor, meta.is_mentioned_by, d2rqMapping.getDataElement(columnName));
			}
			d2rqMapping.addValueTranslationTable(hairColor,RDF.type,D2rqMapping.translationTables.Color,columnNames);
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
			for(String columnName: columnNames) {
				d2rqMapping.createPropertyBridge(eyesColor, meta.is_mentioned_by, d2rqMapping.getDataElement(columnName));
			}
			d2rqMapping.addValueTranslationTable(eyesColor,RDF.type,D2rqMapping.translationTables.Color,columnNames);
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
						Resource geospatialLocationIdentiferBearer = d2rqMapping.createClassMap("BirthLocationIdentifierBearer",amo.GeospatialLocationIdentifierBearer);
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

	private static List<String> getConnectedColumns(RDFNode rdfNode, Property predicate, Model karmaModel)
	{
		List<String> columnNames = new ArrayList<>();
		Resource triplesMap = karmaModel.listStatements(null,r2rml.subjectMap,rdfNode).nextStatement().getSubject().asResource();
		if(triplesMap.getProperty(RDF.type).getObject().equals(r2rml.TriplesMap)) {
			for(Statement s: triplesMap.listProperties(r2rml.predicateObjectMap).toList()) {
				Resource columnConnector = s.getObject().asResource();
				if(columnConnector.getProperty(r2rml.predicate).getObject().asResource().equals(predicate)) {
					for(Statement column: columnConnector.listProperties(r2rml.objectMap).toList()) {
						for(Statement columnVal : karmaModel.listStatements(column.getObject().asResource(),r2rml.column,(RDFNode)null).toList()){
							columnNames.add(columnVal.getObject().asLiteral().getString());	
						}
					}
				}
			}
		}
		return columnNames;
	}

	private static boolean isThisAttributeMapped(Model karmaModel, Resource type)
	{
		return karmaModel.listStatements(null,r2rml.clazz,type).hasNext();
	}
}
