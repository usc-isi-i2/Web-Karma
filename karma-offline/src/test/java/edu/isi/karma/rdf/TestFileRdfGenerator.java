package edu.isi.karma.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import org.junit.Test;

import edu.isi.karma.rdf.GenericRDFGenerator.InputType;


public class TestFileRdfGenerator extends TestRdfGenerator{
	
	@Test
	public void testScheduleRDFPyTranform() {
		try {

			String filename = "schedule.csv";
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			generateRdfFile(new File(getTestResource(filename).toURI()), null, "schedule-model", new File(getTestResource("schedule-model.txt").toURI()), pw);
			
			
			String rdf = sw.toString();
			assertNotEquals(rdf.length(), 0);
			String[] lines = rdf.split(System.getProperty("line.separator"));
			assertEquals(275, lines.length);
			
			int idx = rdf.indexOf("hasEventDate> \"2014-01-13\" .");
			assertNotEquals(idx, -1);

		} catch (Exception e) {
			fail("Exception: " + e.getMessage());
		}
	}
	
	@Test
	public void testCWeb2RDFPyTransform() {
		//
		try {
			String filename = "cbev2.WebConAltNames.csv";
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			generateRdfFile(new File(getTestResource(filename).toURI()), InputType.CSV, "cbev2.WebConAltNames-model", new File(getTestResource("cbev2.WebConAltNames-model.ttl").toURI()), pw);
			
			String rdf = sw.toString();
			assertNotEquals(rdf.length(), 0);
			String[] lines = rdf.split(System.getProperty("line.separator"));
			assertEquals(599, lines.length);
			
			String triple = "<http://collection.americanart.si.edu/id/person-constituent/2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.cidoc-crm.org/cidoc-crm/E21_Person>";
			int idx = rdf.indexOf(triple);
			assertNotEquals(idx, -1);
			
		} catch (Exception e) {
			fail("Exception: " + e.getMessage());
		}
	}

	private URL getTestResource(String name)
	{
		return getClass().getClassLoader().getResource(name);
	}
	
}
