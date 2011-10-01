package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.io.StringWriter;

import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.util.Util;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.SampleDataFactory;
import junit.framework.TestCase;

public class WorksheetHierarchicalDataUpdateTest extends TestCase {

	private RepFactory f;
	private VWorkspace vwsp;

	public WorksheetHierarchicalDataUpdateTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.f = new RepFactory();
		this.vwsp = new VWorkspace(f.createWorkspace());
	}

	public void testGenerateJson() {
		@SuppressWarnings("unused")
		Worksheet ws = SampleDataFactory//
				// .createSampleJsonWithNestedTable1(vwsp.getWorkspace())//
				//.createSampleJsonWithNestedTable2(true/* true: 2 rows */, vwsp.getWorkspace())//
		// .createSampleJson(vwsp.getWorkspace(), 1)//
		// .createJsonWithFunnyCharacters(vwsp.getWorkspace())//
		.createFlatWorksheet(vwsp.getWorkspace(), 2, 2)
		;

		vwsp.addAllWorksheets();
		UpdateContainer c = new UpdateContainer();
		for (VWorksheet vw : vwsp.getVWorksheetList().getVWorksheets()) {
			c.add(new WorksheetHierarchicalDataUpdate(vw));
		}
		
		StringWriter sw1 = new StringWriter();
		PrintWriter pw1 = new PrintWriter(sw1);
		c.generateJson("", pw1, vwsp);
		System.err.println(Util.prettyPrintJson(sw1.toString()));

		assertTrue(true);
	}
}
