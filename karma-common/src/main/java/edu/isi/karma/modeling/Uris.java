/*******************************************************************************
 * Copyright 2012 University of Southern California
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.karma.modeling;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public abstract class Uris {

	public static final String BLANK_NODE_PREFIX = "_:";
	public static final String THING_URI = Namespaces.OWL + "Thing"; 
	public static final String RDFS_SUBCLASS_URI = Namespaces.RDFS + "subClassOf"; 
	public static final String RDF_TYPE_URI = Namespaces.RDF + "type"; 

	public static final String RDFS_CLASS_URI = Namespaces.RDFS + "Class";
	public static final String RDFS_LABEL_URI = Namespaces.RDFS + "label";
	public static final String RDFS_COMMENT_URI = Namespaces.RDFS + "comment";
	public static final String RDF_VALUE_URI = Namespaces.RDF + "value";

	public static final String OWL_SAMEAS_URI = Namespaces.OWL + "sameAs";
	// Karma Internal URIs
	public static final String DEFAULT_LINK_URI = Namespaces.KARMA_DEV + "defaultLink"; 
	public static final String DEFAULT_NODE_URI = Namespaces.KARMA_DEV + "defaultNode"; 
//	public static final String OBJECT_PROPERTY_DIRECT_LINK_URI = Namespaces.KARMA_DEV + "objectPropertyDirectLink"; 
//	public static final String OBJECT_PROPERTY_INDIRECT_LINK_URI = Namespaces.KARMA_DEV + "objectPropertyIndirectLink"; 
//	public static final String OBJECT_PROPERTY_WITH_ONLY_DOMAIN_LINK_URI = Namespaces.KARMA_DEV + "objectPropertyWithOnlyDomainLink"; 
//	public static final String OBJECT_PROPERTY_WITH_ONLY_RANGE_LINK_URI = Namespaces.KARMA_DEV + "objectPropertyWithOnlyRangeLink"; 
//	public static final String OBJECT_PROPERTY_WITHOUT_DOMAIN_AND_RANGE_LINK_URI = Namespaces.KARMA_DEV + "objectPropertyWithoutDomainAndRangeLink"; 
	
	public static final String CLASS_INSTANCE_LINK_URI = Namespaces.KARMA_DEV + "classLink"; 
	public static final String COLUMN_SUBCLASS_LINK_URI = Namespaces.KARMA_DEV + "columnSubClassOfLink"; 
	public static final String MODEL_HAS_DATA_URI = Namespaces.KARMA_DEV + "hasData"; 
	public static final String DATAPROPERTY_OF_COLUMN_LINK_URI = Namespaces.KARMA_DEV + "dataPropertyOfColumnLink";
	public static final String OBJECTPROPERTY_SPECIALIZATION_LINK_URI = Namespaces.KARMA_DEV + "objectPropertySpecialization";
	
	//SCHEMA.ORG domain and range predicates
	public static final String SCHEMA_DOMAIN_INCLUDES = Namespaces.SCHEMA + "domainIncludes";
	public static final String SCHEMA_RANGE_INCLUDES = Namespaces.SCHEMA + "rangeIncludes";
	
	// R2RML Vocabulary URIs
	public static final String RR_TRIPLESMAP_CLASS_URI = Namespaces.RR + "TriplesMap";
	public static final String RR_SUBJECTMAP_CLASS_URI = Namespaces.RR+ "SubjectMap";
	public static final String RR_PREDICATEOBJECTMAP_CLASS_URI = Namespaces.RR+ "PredicateObjectMap";
	public static final String RR_OBJECTMAP_CLASS_URI = Namespaces.RR+ "ObjectMap";
	public static final String RR_LOGICAL_TABLE_CLASS_URI = Namespaces.RR + "LogicalTable";
	public static final String RR_SUBJECTMAP_URI = Namespaces.RR + "subjectMap";
	public static final String RR_TABLENAME_URI = Namespaces.RR + "tableName";
	public static final String RR_TEMPLATE_URI = Namespaces.RR + "template";
	public static final String RR_CONSTANT = Namespaces.RR + "constant";
	public static final String RR_PRED_OBJ_MAP_URI = Namespaces.RR + "predicateObjectMap";
	public static final String RR_PREDICATE_URI = Namespaces.RR + "predicate";
	public static final String RR_OBJECTMAP_URI = Namespaces.RR + "objectMap";
	public static final String RR_COLUMN_URI = Namespaces.RR + "column";
	public static final String RR_DATATYPE_URI = Namespaces.RR + "datatype";
	public static final String RR_LANGUAGE_URI = Namespaces.RR + "language";
	public static final String RR_LOGICAL_TABLE_URI = Namespaces.RR + "logicalTable";
	public static final String RR_REF_OBJECT_MAP_CLASS_URI = Namespaces.RR + "RefObjectMap";
	public static final String RR_PARENT_TRIPLE_MAP_URI = Namespaces.RR + "parentTriplesMap";
	public static final String RR_TERM_TYPE_URI = Namespaces.RR + "termType";
	public static final String RR_BLANK_NODE_URI = Namespaces.RR + "BlankNode";
	public static final String RR_CLASS_URI = Namespaces.RR + "class";
	public static final String RR_LITERAL_URI = Namespaces.RR + "Literal";
	
	public static final String KM_BLANK_NODE_PREFIX_URI = Namespaces.KARMA_DEV + "namePrefix";
	public static final String KM_NODE_ID_URI = Namespaces.KARMA_DEV + "alignmentNodeId";
	public static final String KM_R2RML_MAPPING_URI = Namespaces.KARMA_DEV + "R2RMLMapping";
	public static final String KM_STEINER_TREE_ROOT_NODE = Namespaces.KARMA_DEV + "steinerTreeRootNode";
	public static final String KM_SOURCE_NAME_URI = Namespaces.KARMA_DEV + "sourceName";
	public static final String KM_HAS_TRIPLES_MAP_URI = Namespaces.KARMA_DEV + "hasTriplesMap";
	public static final String KM_HAS_SUBJECT_MAP_URI = Namespaces.KARMA_DEV + "hasSubjectMap";
	public static final String KM_HAS_OBJECT_MAP_URI = Namespaces.KARMA_DEV + "hasObjectMap";
	public static final String KM_HAS_PREDICATE_OBJECT_MAP_URI = Namespaces.KARMA_DEV + "hasPredicateObjectMap";
	public static final String KM_HAS_LOGICAL_TABLE_URI = Namespaces.KARMA_DEV + "hasLogicalTable";
	public static final String KM_IS_PART_OF_MAPPING_URI = Namespaces.KARMA_DEV + "isPartOfMapping";
	public static final String KM_HAS_WORKSHEET_HISTORY_URI = Namespaces.KARMA_DEV + "hasWorksheetHistory";
	public static final String KM_MODEL_PUBLICATION_TIME_URI = Namespaces.KARMA_DEV + "modelPublicationTime";
	public static final String KM_MODEL_VERSION_URI = Namespaces.KARMA_DEV + "modelVersion";
	public static final String KM_SOURCE_TYPE_URI = Namespaces.KARMA_DEV + "sourceType";
	public static final String KM_HAS_BLOOMFILTER = Namespaces.KARMA_DEV + "hasBloomFilter";
	public static final String KM_HAS_INPUTCOLUMNS = Namespaces.KARMA_DEV + "hasInputColumns";
	public static final String KM_HAS_OUTPUTCOLUMNS = Namespaces.KARMA_DEV + "hasOutputColumns";
	public static final String KM_HAS_MODELLABEL = Namespaces.KARMA_DEV + "hasModelLabel";
	public static final String KM_HAS_BASEURI = Namespaces.KARMA_DEV + "hasBaseURI";
	public static final String KM_HAS_OLDHISTORY = Namespaces.KARMA_DEV + "hasOldHistory";

	
	public static final String PROV_ENTITY_URI = Namespaces.PROV + "Entity";
	public static final String PROV_WAS_DERIVED_FROM_URI = Namespaces.PROV + "wasDerivedFrom";
	
	// Worksheet properties related URIs
	public static final String KM_SERVICE_OPTIONS_URI = Namespaces.KARMA_DEV + "hasServiceOptions";
	public static final String KM_SERVICE_URL_URI = Namespaces.KARMA_DEV + "serviceUrl";
	public static final String KM_SERVICE_REQ_METHOD_URI = Namespaces.KARMA_DEV + "serviceRequestMethod";
	public static final String KM_SERVICE_POST_METHOD_TYPE_URI = Namespaces.KARMA_DEV + "servicePostMethodType";
	
	// Linking related URIs
	public static final String KM_LINKING_MATCHES_URI = Namespaces.KARMA_DEV + "possibleLinkingMatches";
	
	public static final List<String> Uris = new ArrayList<>();
	static {
		List<Field> staticFields = new ArrayList<>();
	    Field[] allFields = Uris.class.getDeclaredFields();
	    for (Field field : allFields) {
	        if (Modifier.isStatic(field.getModifiers())) {
	        	staticFields.add(field);
	        }
	    }
	    for (Field field : staticFields) {
	    	Class<?> t = field.getType();
	    	if (t == String.class && !field.getName().equals("BLANK_NODE_PREFIX"))
				try {
					Uris.add((String)field.get(null));
				} catch (IllegalArgumentException e) {

				} catch (IllegalAccessException e) {

				}
	    }
	}

}
