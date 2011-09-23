package edu.isi.karma.view;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetDataUpdate;
import edu.isi.karma.controller.update.WorksheetHeadersUpdate;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.WorksheetTest;
import edu.isi.karma.webserver.SampleDataFactory;

public class VWorkspaceTest extends TestCase {

	private RepFactory f;
	private VWorkspace vwsp;
	
	
	public VWorkspaceTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.f = new RepFactory();
		this.vwsp = new VWorkspace(f.createWorkspace());
	}

	public void testAddAllWorksheets() throws JSONException {
		SampleDataFactory.createFlatWorksheet(vwsp.getWorkspace(), 20, 3);
		SampleDataFactory.createFlatWorksheet(vwsp.getWorkspace(), 1, 2);
		WorksheetTest.createWS1(vwsp.getWorkspace());
		vwsp.addAllWorksheets();
		assertEquals(3, vwsp.getWorkspace().getWorksheets().size());
		assertEquals(3, vwsp.getVWorksheetList().getNumWorksheets());
		
		UpdateContainer c = new UpdateContainer();
		c.add(new WorksheetListUpdate(vwsp.getVWorksheetList()));
		for (VWorksheet vw : vwsp.getVWorksheetList().getVWorksheets()){
			c.add(new WorksheetHeadersUpdate(vw));
			c.add(new WorksheetDataUpdate(vw));
		}
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		c.generateJson("", pw, vwsp);
		
		assertTrue(true);
		System.err.println(sw.toString());
		
		// Test for parsing errors.
		new JSONObject(sw.toString());
	}

}
