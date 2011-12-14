// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.rdf.testing;

import java.util.HashMap;
import edu.isi.mediator.rdf.TableRDFGenerator;

/**
 * @author Maria Muslea(USC/ISI)
 *
 */

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

		   TableRDFGenerator rdfGen = new TableRDFGenerator(domainFile, null);
		   rdfGen.generateTriples("NATIONALITY_ID", vals3, "t123");
		   rdfGen.generateTriples("NATIONALITY_ID", vals3, "t123");
		   rdfGen.closeWriter();
		   
		   }catch(Exception e){e.printStackTrace();}
	   }

	  
}
