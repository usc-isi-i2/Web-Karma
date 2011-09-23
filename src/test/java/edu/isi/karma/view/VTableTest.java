package edu.isi.karma.view;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.json.JSONException;

import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.util.Util;

public class VTableTest extends TestCase {

	private RepFactory f;
	private VWorkspace vwsp;

	public VTableTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.f = new RepFactory();
		this.vwsp = new VWorkspace(f.createWorkspace());
	}

	@SuppressWarnings("unused")
	public void testAddRows() throws JSONException {
		String jsonString = "{\"c\":[{\"d\":[{\"f\":\"_0_\"},{\"f\":\"_0_\"}],\"h\":\"_h0_\"},{\"d\":[{\"f\":\"_1_\"},{\"f\":\"_1_\"}],\"h\":\"_h1_\"}],\"a\":\"_a_\"}";
		Object o = Util.createJson(jsonString);
		JsonImport ji = new JsonImport(o, "Table-01", vwsp.getWorkspace());
		Worksheet w = ji.generateWorksheet();
		vwsp.addAllWorksheets();

		VWorksheet vw = vwsp.getVWorksheet(w.getId());
		VTable vt = vw.getDataTable();

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		w.prettyPrint("", pw, f);
		pw.println("-----------");
		vt.prettyPrint("", pw);

		vw.generateWorksheetDataJson("-->> ", pw, vwsp.getViewFactory());
		System.err.println(sw.toString());

		List<VRow> rows = vt.getRows();
		assertEquals(1, rows.size());

		VRow row = rows.get(0);
		List<VRowEntry> entries = row._getEntries();
		assertEquals(3, entries.size());

		VRowEntry e0 = entries.get(0);
		VRowEntry e1 = entries.get(1);
		VRowEntry e2 = entries.get(2);

		ArrayList<VCell> e1Cells = e1._getCells();

		System.err.println("CellValue 0:" + e1Cells.get(0).getValue());

		// assertEquals(4, e1Cells.size());

	}

}
