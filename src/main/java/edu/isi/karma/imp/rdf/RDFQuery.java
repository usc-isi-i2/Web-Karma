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

package edu.isi.karma.imp.rdf;

public class RDFQuery {
	String Prefix = " PREFIX dc:<http://purl.org/dc/elements/1.1/> "
			+ " PREFIX saam:<http://americanart.si.edu/saam/> "
			+ " PREFIX owl:<http://www.w3.org/2002/07/owl#> "
			+ " PREFIX BuildingOntology:<http://www.semanticweb.org/ontologies/2012/9/BuildingOntology.owl#> "
			+ " PREFIX s:<http://localhost:8080/source/wikimapia_building/> ";

	/* general Query for extract URI */
	public String createGeneralQuery() {
		String generalQuery = Prefix + " select distinct ?a " + " where {"
				+ " ?a owl:sameAs ?b. " + " ?a a BuildingOntology:Building. "
				+ "}";
		return generalQuery;
	}

	/* Longitude */
	public String createQueryForLongitudeS1(String subject, String prefix,
			String property) {
		String query = Prefix + " select ?c where{ <" + subject
				+ "> BuildingOntology:hasPoint ?a. "
				+ " ?a BuildingOntology:hasGeocoordinates ?b. "
				+ " ?b BuildingOntology:xInDecimalLongitude  ?c. }";
		return query;
	}

	public String createQueryForLongitudeS2(String subject, String prefix,
			String property) {
		String query = Prefix + " select  ?c where { " + "<" + subject
				+ ">   owl:sameAs  ?x.  "
				+ " ?x  BuildingOntology:hasPoint ?m. "
				+ " ?m BuildingOntology:hasGeocoordinates ?n. "
				+ " ?n BuildingOntology:xInDecimalLongitude  ?c. }";
		return query;
	}

	/* Latitude */
	public String createQueryForLatitudeS1(String subject, String prefix,
			String property) {
		String query = Prefix + " select ?c where{ <" + subject
				+ "> BuildingOntology:hasPoint ?a. "
				+ " ?a BuildingOntology:hasGeocoordinates ?b. "
				+ " ?b BuildingOntology:yInDecimalLatitude  ?c. }";

		return query;
	}

	public String createQueryForLatitudeS2(String subject, String prefix,
			String property) {
		String query = Prefix + " select  ?c where { " + "<" + subject
				+ ">   owl:sameAs  ?x.  "
				+ " ?x  BuildingOntology:hasPoint ?m. "
				+ " ?m BuildingOntology:hasGeocoordinates ?n. "
				+ " ?n BuildingOntology:yInDecimalLatitude  ?c. }";
		return query;
	}

	/* srid */
	public String createQueryForSridValueS1(String subject, String prefix,
			String property) {

		String query = Prefix + " select ?c where{  " + "<" + subject
				+ ">  BuildingOntology:hasSource ?a. "
				+ " ?a BuildingOntology:hasSRID ?b. "
				+ " ?b BuildingOntology:sridValue  ?c. }";
		return query;
	}

	public String createQueryForSridValueS2(String subject, String prefix,
			String property) {

		String query = Prefix + " select ?c  where{  " + "<" + subject
				+ ">  owl:sameAs ?x. "
				+ " ?x  BuildingOntology:hasSource  ?m. "
				+ " ?m BuildingOntology:hasSRID ?n. "
				+ " ?n BuildingOntology:sridValue  ?c. }";
		return query;
	}

	/* polygon */
	public String createQueryForWellKnownTextS1(String subject, String prefix,
			String property) {

		String query = Prefix + " select ?c where{  " + "<" + subject
				+ ">  BuildingOntology:hasPolygon ?a. "
				+ " ?a BuildingOntology:hasGeocoordinates ?b. "
				+ " ?b BuildingOntology:wellKnownText  ?c.  }";
		return query;
	}

	public String createQueryForWellKnownTextS2(String subject, String prefix,
			String property) {

		String query = Prefix + " select ?c where{  " + "<" + subject
				+ ">  owl:sameAs ?x. "
				+ " ?x  BuildingOntology:hasPolygon  ?m. "
				+ " ?m BuildingOntology:hasGeocoordinates ?n. "
				+ " ?n BuildingOntology:wellKnownText  ?c. }";
		return query;
	}

	/* stateName */
	public String createQueryForStateNameS1(String subject, String prefix,
			String property) {
		String query = Prefix + " select ?c ?d where{ " + "<" + subject
				+ ">  BuildingOntology:hasAddress ?a. "
				+ " ?a  BuildingOntology:hasState ?b. "
				+ " ?b  BuildingOntology:stateName  ?c.} ";

		return query;
	}

	public String createQueryForStateNameS2(String subject, String prefix,
			String property) {
		String query = Prefix + " select ?c  where{ " + "<" + subject
				+ ">  owl:sameAs ?x. " + "?x  BuildingOntology:hasAddress ?a. "
				+ " ?a  BuildingOntology:hasState ?b. "
				+ " ?b  BuildingOntology:stateName  ?c.} ";

		return query;
	}

	/* countyName */
	public String createQueryForCountyNameS1(String subject, String prefix,
			String property) {
		String query = Prefix + " select ?c where{ " + "<" + subject
				+ ">  BuildingOntology:hasAddress ?a. "
				+ " ?a  BuildingOntology:hasCounty ?b. "
				+ " ?b  BuildingOntology:countyName  ?c.} ";

		return query;
	}

	public String createQueryForCountyNameS2(String subject, String prefix,
			String property) {
		String query = Prefix + " select ?c ?d where{ " + "<" + subject
				+ ">  owl:sameAs ?x. " + "?x BuildingOntology:hasAddress ?a. "
				+ " ?a  BuildingOntology:hasCounty ?b. "
				+ " ?b  BuildingOntology:countyName  ?c.} ";

		return query;
	}

	/* BuildingName */
	public String createQueryForBuildingNameS1(String subject, String prefix,
			String property) {
		String query = Prefix + "select distinct ?c  where{ <" + subject
				+ ">   BuildingOntology:buildingName   ?c. }";
		return query;
	}

	public String createQueryForBuildingNameS2(String subject, String prefix,
			String property) {
		String query = Prefix + "select distinct ?c  where{<" + subject
				+ ">  owl:sameAs ?x.  "
				+ "?x   BuildingOntology:buildingName   ?c. }";
		return query;
	}

	public String createQueryForPoint(String subject, String prefix,
			String property) {
		System.out.println("subject:" + subject);
		String query = Prefix + " select distinct ?c where { <" + subject
				+ ">   owl:sameAs  ?x.  " + "<" + subject
				+ ">  BuildingOntology:hasPoint  ?a. "
				+ " ?a  BuildingOntology:hasGeocoordinates  ?b. "
				+ " ?b  BuildingOntology:xInDecimalLongitude  ?c. " +

				" ?x " + prefix + ":" + "hasPoint ?m. " + " ?m " + prefix + ":"
				+ "hasGeocoordinates ?n. " + " ?n " + prefix + ":" + property
				+ " ?d. }";
		System.out.println(query);
		return query;
	}

}
