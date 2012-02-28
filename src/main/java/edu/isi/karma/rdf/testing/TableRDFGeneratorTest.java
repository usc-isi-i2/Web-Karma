/**
 * Copyright 2012 University of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This code was developed by the Information Integration Group as part 
 *  of the Karma project at the Information Sciences Institute of the 
 *  University of Southern California.  For more information, publications, 
 *  and related projects, please see: http://www.isi.edu/integration
 *
 */

package edu.isi.karma.rdf.testing;

import java.io.PrintWriter;
import java.util.HashMap;

import edu.isi.karma.rdf.TableRDFGenerator;

/**
 * @author Maria Muslea(USC/ISI)
 *
 */
@SuppressWarnings("all")
public class TableRDFGeneratorTest {

	   public static void main(String[] args){
  
		   try{

		   String domainFile = "NAMESPACES:\n s:'http://www.dovetail.org/WITS/' \n dv:'http://www.dovetail.org/ontology/'\n" +
		   "LAV_RULES:\n WITS_Facility(NATIONALITY_ID, INCIDENT_ID, FACILITY_ID) -> `dv:Country`(uri(NATIONALITY_ID)) ^ `dv:hasNationality`(uri(1), uri(NATIONALITY_ID)) ^" +
		   "`dv:Person`(uri(1)) ^ `dv:belongsTo`(uri(2), uri(1)) ^" +
		   "`dv:Organization`(uri(2)) ^ `dv:carriedBy`(uri(INCIDENT_ID), uri(2)) ^" +
		   "`dv:Attack`(uri(INCIDENT_ID)) ^ `dv:causes`(uri(INCIDENT_ID), uri(FACILITY_ID)) ^" +
		   "`dv:Damage`(uri(FACILITY_ID))";
		   
		   HashMap<String,String> vals3 = new HashMap<String,String>();
		   vals3.put("NATIONALITY_ID","20");
		   vals3.put("INCIDENT_ID","67");
		   vals3.put("FACILITY_ID","89");

		   //TableRDFGenerator rdfGen = new TableRDFGenerator(domainFile, null);
		   //rdfGen.generateTriples("NATIONALITY_ID", vals3, "t123");
		   //rdfGen.generateTriples("NATIONALITY_ID", vals3, "t123");
		   //rdfGen.closeWriter();
		   
		   String dF1 = "NAMESPACES: s:'http://www.isi.edu/' LAV_RULES:" +
		   "SourceDescription(GENE_NAME, NAME, DISEASE_NAME, GENE_ID, DISEASE_ID, DRUG_ID, DRUG_NAME, ACCESSION_ID) -> " +
		   "`http://halowiki/ob/category#Disease`(uri(DISEASE_ID))" +
			"^ `http://halowiki/ob/property#isCausedBy`(uri(DISEASE_ID),uri(GENE_ID)) ^ `http://halowiki/ob/category#Gene`(uri(GENE_ID))" +
		   "^ `http://halowiki/ob/property#name`(uri(GENE_ID),GENE_NAME)" +
		   "^ `http://halowiki/ob/property#pharmGKBId`(uri(GENE_ID),GENE_ID)" +
		   "^ `inverse__of___http://halowiki/ob/property#involves`(uri(GENE_ID),uri(ACCESSION_ID)) ^ `http://halowiki/ob/category#Pathway`(uri(ACCESSION_ID))" +
		   " ^ `http://halowiki/ob/property#name`(uri(ACCESSION_ID),NAME)" +
		   "	^ `http://halowiki/ob/property#pharmGKBId`(uri(ACCESSION_ID),ACCESSION_ID)" +
		   "^ `http://halowiki/ob/property#isTreatedBy`(uri(DISEASE_ID),uri(DRUG_ID)) ^ `http://halowiki/ob/category#Drug`(uri(DRUG_ID))" +
		   "^ `http://halowiki/ob/property#name`(uri(DRUG_ID),DRUG_NAME)" +
		   "^ `http://halowiki/ob/property#pharmGKBId`(uri(DRUG_ID),DRUG_ID)" +
		   "^ `http://halowiki/ob/property#name`(uri(DISEASE_ID),DISEASE_NAME)" +
		   "^ `http://halowiki/ob/property#pharmGKBId`(uri(DISEASE_ID),DISEASE_ID)";

		   String dF2 = "NAMESPACES: s:'http://www.isi.edu/' LAV_RULES:" +
		   "SourceDescription(GENE_NAME, NAME, DISEASE_NAME, GENE_ID, DISEASE_ID, DRUG_ID, DRUG_NAME, ACCESSION_ID) ->" + 
		   "`http://halowiki/ob/category#Disease`(uri(0))" +
		   " ^ `http://halowiki/ob/property#isCausedBy`(uri(0),uri(GENE_ID)) ^ `http://halowiki/ob/category#Gene`(uri(GENE_ID))" +
		   " ^ `http://halowiki/ob/property#name`(uri(GENE_ID),GENE_NAME)" +
		   " ^ `http://halowiki/ob/property#pharmGKBId`(uri(GENE_ID),GENE_ID)" +
		   " ^ `inverse__of___http://halowiki/ob/property#involves`(uri(GENE_ID),uri(1)) ^ `http://halowiki/ob/category#Pathway`(uri(1))" +
		   " ^ `http://halowiki/ob/property#name`(uri(1),NAME)" +
		   " ^ `http://halowiki/ob/property#pharmGKBId`(uri(1),ACCESSION_ID)" +
		   " ^ `http://halowiki/ob/property#isTreatedBy`(uri(0),uri(DRUG_ID)) ^ `http://halowiki/ob/category#Drug`(uri(DRUG_ID))" +
		   " ^ `http://halowiki/ob/property#name`(uri(DRUG_ID),DRUG_NAME)" +
		   " ^ `http://halowiki/ob/property#pharmGKBId`(uri(DRUG_ID),DRUG_ID)" +
		   " ^ `http://halowiki/ob/property#name`(uri(0),DISEASE_NAME)" +
		   " ^ `http://halowiki/ob/property#pharmGKBId`(uri(0),DISEASE_ID)";
		   
		   String dF3 = "NAMESPACES: s:'http://www.isi.edu/' LAV_RULES:" +
		   "SourceDescription(`GENE_NAME`, `GENE_ID`) ->" + 
		   "`http://halowiki/ob/category#Disease`(uri(0))" +
		   " ^ `http://halowiki/ob/property#isCausedBy`(uri(0),uri(`GENE_ID`)) ^ `http://halowiki/ob/category#Gene`(uri(`GENE_ID`))" +
		   " ^ `http://halowiki/ob/property#name`(uri(`GENE_ID`),`GENE_NAME`)" +
		   " ^ `http://halowiki/ob/property#pharmGKBId`(uri(`GENE_ID`),`GENE_ID`)";

		   HashMap<String,String> vals1 = new HashMap<String,String>();
		   vals1.put("GENE_NAME","20");
		   vals1.put("NAME","67");
		   vals1.put("DISEASE_NAME","89");
		   vals1.put("GENE_ID","1");
		   vals1.put("DISEASE_ID","2");
		   vals1.put("DRUG_ID","3");
		   vals1.put("DRUG_NAME","4");
		   vals1.put("ACCESSION_ID","5");

		   //TableRDFGenerator rdfGen = new TableRDFGenerator(dF1, null);
		   TableRDFGenerator rdfGen = new TableRDFGenerator(dF3, new PrintWriter(System.out));
		   rdfGen.generateTriples("GENE_NAME", vals1, "t123");
		   rdfGen.generateTriples("GENE_ID", vals1, "t123");
		   //rdfGen.generateTriples("DISEASE_NAME", vals1, "t123");
		   //rdfGen.generateTriples("ACCESSION_ID", vals1, "t123");
		   rdfGen.closeWriter();

		   }catch(Exception e){e.printStackTrace();}
	   }

	  
}
