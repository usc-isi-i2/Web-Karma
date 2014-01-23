/**************************************************************************************************************************************
 * Copyright (c) 2013 CUBRC, Inc.                                                                                                     *
 * Unpublished Work - all rights reserved under the copyright laws of the United States.                                              *
 * CUBRC, Inc. does not grant permission to any party outside the Government                                                          *
 * to use, disclose, copy, or make derivative works of this software.                                                                 *
 **************************************************************************************************************************************/

package alignment.karma.mapping.translation.ontologies;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;


@SuppressWarnings("ALL")
public class d2rq
{
	private static final String PREFIX = "d2rq";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#");
	}

	public static String getNamespace()
	{
		return "http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource AdditionalProperty = ResourceFactory.createResource("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#AdditionalProperty");
	public static final Resource ClassMap = ResourceFactory.createResource("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#ClassMap");
	public static final Resource Configuration = ResourceFactory.createResource("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#Configuration");
	public static final Resource D2RQModel = ResourceFactory.createResource("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#D2RQModel");
	public static final Resource Database = ResourceFactory.createResource("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#Database");
	public static final Resource DatatypePropertyBridge = ResourceFactory.createResource("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#DatatypePropertyBridge");
	public static final Resource DownloadMap = ResourceFactory.createResource("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#DownloadMap");
	public static final Resource ObjectPropertyBridge = ResourceFactory.createResource("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#ObjectPropertyBridge");
	public static final Resource PropertyBridge = ResourceFactory.createResource("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#PropertyBridge");
	public static final Resource ResourceMap = ResourceFactory.createResource("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#ResourceMap");
	public static final Resource Translation = ResourceFactory.createResource("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#Translation");
	public static final Resource TranslationTable = ResourceFactory.createResource("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#TranslationTable");

	// Property
	public static final Property additionalClassDefinitionProperty = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#additionalClassDefinitionProperty");
	public static final Property additionalProperty = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#additionalProperty");
	public static final Property additionalPropertyDefinitionProperty = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#additionalPropertyDefinitionProperty");
	public static final Property alias = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#alias");
	public static final Property allowDistinct = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#allowDistinct");
	public static final Property bNodeIdColumns = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#bNodeIdColumns");
	public static final Property belongsToClassMap = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#belongsToClassMap");
	public static final Property binaryColumn = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#binaryColumn");
	public static final Property bitColumn = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#bitColumn");
	public static final Property booleanColumn = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#booleanColumn");
	public static final Property clazz = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#class");
	public static final Property classDefinitionComment = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#classDefinitionComment");
	public static final Property classDefinitionLabel = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#classDefinitionLabel");
	public static final Property classMap = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#classMap");
	public static final Property column = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#column");
	public static final Property condition = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#condition");
	public static final Property constantValue = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#constantValue");
	public static final Property containsDuplicates = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#containsDuplicates");
	public static final Property contentDownloadColumn = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#contentDownloadColumn");
	public static final Property dataStorage = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#dataStorage");
	public static final Property databaseValue = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#databaseValue");
	public static final Property datatype = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#datatype");
	public static final Property dateColumn = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#dateColumn");
	public static final Property dynamicProperty = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#dynamicProperty");
	public static final Property fetchSize = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#fetchSize");
	public static final Property href = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#href");
	public static final Property intervalColumn = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#intervalColumn");
	public static final Property javaClass = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#javaClass");
	public static final Property jdbcDSN = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#jdbcDSN");
	public static final Property jdbcDriver = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#jdbcDriver");
	public static final Property join = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#join");
	public static final Property lang = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#lang");
	public static final Property limit = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#limit");
	public static final Property limitInverse = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#limitInverse");
	public static final Property mappingFile = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#mappingFile");
	public static final Property mediaType = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#mediaType");
	public static final Property numericColumn = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#numericColumn");
	public static final Property odbcDSN = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#odbcDSN");
	public static final Property orderAsc = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#orderAsc");
	public static final Property orderDesc = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#orderDesc");
	public static final Property password = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#password");
	public static final Property pattern = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#pattern");
	public static final Property property = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#property");
	public static final Property propertyBridge = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#propertyBridge");
	public static final Property propertyDefinitionComment = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#propertyDefinitionComment");
	public static final Property propertyDefinitionLabel = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#propertyDefinitionLabel");
	public static final Property propertyName = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#propertyName");
	public static final Property propertyValue = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#propertyValue");
	public static final Property rdfValue = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#rdfValue");
	public static final Property refersToClassMap = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#refersToClassMap");
	public static final Property resourceBaseURI = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#resourceBaseURI");
	public static final Property resultSizeLimit = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#resultSizeLimit");
	public static final Property serveVocabulary = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#serveVocabulary");
	public static final Property sqlExpression = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#sqlExpression");
	public static final Property startupSQLScript = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#startupSQLScript");
	public static final Property textColumn = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#textColumn");
	public static final Property timeColumn = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#timeColumn");
	public static final Property timestampColumn = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#timestampColumn");
	public static final Property translateWith = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#translateWith");
	public static final Property translation = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#translation");
	public static final Property uriColumn = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#uriColumn");
	public static final Property uriPattern = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#uriPattern");
	public static final Property uriSqlExpression = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#uriSqlExpression");
	public static final Property useAllOptimizations = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#useAllOptimizations");
	public static final Property username = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#username");
	public static final Property valueContains = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#valueContains");
	public static final Property valueMaxLength = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#valueMaxLength");
	public static final Property valueRegex = ResourceFactory.createProperty("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#valueRegex");
}

