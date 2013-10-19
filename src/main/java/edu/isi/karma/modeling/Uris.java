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

public interface Uris {

	public static final String THING_URI = Namespaces.OWL + "Thing"; 
	public static final String RDFS_SUBCLASS_URI = Namespaces.RDFS + "subClassOf"; 
	public static final String RDF_TYPE_URI = Namespaces.RDF + "type"; 
	public static final String RDFS_CLASS_URI = Namespaces.RDFS + "Class";
	public static final String RDFS_LABEL_URI = Namespaces.RDFS + "label";

	// Karma Internal URIs
	public static final String CLASS_INSTANCE_LINK_URI = Namespaces.KARMA_DEV + "classLink"; 
	public static final String COLUMN_SUBCLASS_LINK_URI = Namespaces.KARMA_DEV + "columnSubClassOfLink"; 
	public static final String DATAPROPERTY_OF_COLUMN_LINK_URI = Namespaces.KARMA_DEV + "dataPropertyOfColumnLink";
	public static final String OBJECTPROPERTY_SPECIALIZATION_LINK_URI = Namespaces.KARMA_DEV + "objectPropertySpecialization";
	
	// R2RML Vocabulary URIs
	public static final String RR_TRIPLESMAP_CLASS_URI = Namespaces.RR + "TriplesMap";
	public static final String RR_SUBJECTMAP_URI = Namespaces.RR + "subjectMap";
	public static final String RR_TABLENAME_URI = Namespaces.RR + "tableName";
	public static final String RR_TEMPLATE_URI = Namespaces.RR + "template";
	public static final String RR_PRED_OBJ_MAP_URI = Namespaces.RR + "predicateObjectMap";
	public static final String RR_PREDICATE_URI = Namespaces.RR + "predicate";
	public static final String RR_OBJECTMAP_URI = Namespaces.RR + "objectMap";
	public static final String RR_COLUMN_URI = Namespaces.RR + "column";
	public static final String RR_LOGICAL_TABLE_URI = Namespaces.RR + "logicalTable";
	public static final String RR_REF_OBJECT_MAP_URI = Namespaces.RR + "RefObjectMap";
	public static final String RR_PARENT_TRIPLE_MAP_URI = Namespaces.RR + "parentTriplesMap";
	public static final String RR_TERM_TYPE_URI = Namespaces.RR + "termType";
	public static final String RR_BLANK_NODE_URI = Namespaces.RR + "BlankNode";
	public static final String RR_CLASS_URI = Namespaces.RR + "class";
	
	public static final String KM_BLANK_NODE_COVERS_COLUMN_URI = Namespaces.KARMA_DEV + "coversColumn";
	public static final String KM_BLANK_NODE_PREFIX_URI = Namespaces.KARMA_DEV + "namePrefix";
	public static final String KM_NODE_ID_URI = Namespaces.KARMA_DEV + "alignmentNodeId";
	public static final String KM_R2RML_MAPPING_URI = Namespaces.KARMA_DEV + "R2RMLMapping";
	public static final String KM_STEINER_TREE_ROOT_NODE = Namespaces.KARMA_DEV + "steinerTreeRootNode";
	public static final String KM_SOURCE_NAME_URI = Namespaces.KARMA_DEV + "sourceName";
	public static final String KM_HAS_TRIPLES_MAP_URI = Namespaces.KARMA_DEV + "hasTriplesMap";
	public static final String KM_HAS_TRANSFORMATION_URI = Namespaces.KARMA_DEV + "hasColumnTransformation";
	public static final String KM_HAS_WORKSHEET_HISTORY_URI = Namespaces.KARMA_DEV + "hasWorksheetHistory";
	public static final String KM_MODEL_PUBLICATION_TIME_URI = Namespaces.KARMA_DEV + "modelPublicationTime";
	
	public static final String PROV_ENTITY_URI = Namespaces.PROV + "Entity";
	public static final String PROV_WAS_DERIVED_FROM_URI = Namespaces.PROV + "wasDerivedFrom";
	
	// Worksheet properties related URIs
	public static final String KM_SERVICE_OPTIONS_URI = Namespaces.KARMA_DEV + "hasServiceOptions";
	public static final String KM_SERVICE_URL_URI = Namespaces.KARMA_DEV + "serviceUrl";
	public static final String KM_SERVICE_REQ_METHOD_URI = Namespaces.KARMA_DEV + "serviceRequestMethod";
	public static final String KM_SERVICE_POST_METHOD_TYPE_URI = Namespaces.KARMA_DEV + "servicePostMethodType";
	
	// Linking related URIs
	public static final String KM_LINKING_MATCHES_URI = Namespaces.KARMA_DEV + "possibleLinkingMatches";
}
