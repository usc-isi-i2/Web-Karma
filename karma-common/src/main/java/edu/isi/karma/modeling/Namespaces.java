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

public interface Namespaces {

	public static final String XSD = com.hp.hpl.jena.vocabulary.XSD.getURI(); //"http://www.w3.org/2001/XMLSchema#"; 
	public static final String RDF = com.hp.hpl.jena.vocabulary.RDF.getURI(); //"http://www.w3.org/1999/02/22-rdf-syntax-ns#"; 
	public static final String RDFS = com.hp.hpl.jena.vocabulary.RDFS.getURI(); // "http://www.w3.org/2000/01/rdf-schema#"; 
	public static final String OWL = com.hp.hpl.jena.vocabulary.OWL.getURI(); //"http://www.w3.org/2002/07/owl#"; 
	public static final String SAWSDL = "http://www.w3.org/ns/sawsdl#"; 
	public static final String MSM = "http://cms-wg.sti2.org/ns/minimal-service-model#"; 
	public static final String WSMO_LITE = "http://www.wsmo.org/ns/wsmo-lite#"; 
	public static final String HRESTS = "http://purl.org/hRESTS/current#";
	public static final String RULEML = "http://www.w3.org/2003/11/ruleml#";
	public static final String SWRL = "http://www.w3.org/2003/11/swrl#";
	public static final String KARMA = "http://isi.edu/integration/karma/ontologies/model/current#";
	public static final String GEOSPATIAL = KARMA+"geospatial/";
	public static final String EXAMPLE = "http://example.com#";
	public static final String KARMA_DEV = "http://isi.edu/integration/karma/dev#";
	public static final String RR = "http://www.w3.org/ns/r2rml#";
	public static final String PROV = "http://www.w3.org/ns/prov#";
	public static final String RML = "http://mmlab.be/rml#";
	public static final String QL = "http://mmlab.be/ql#";
	public static final String SCHEMA = "http://schema.org/";
}
