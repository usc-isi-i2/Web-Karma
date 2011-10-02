package edu.isi.karma.view.tabledata;

import java.util.List;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONStringer;

import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.util.Util;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.tableheadings.VTableHeadings;
import edu.isi.karma.webserver.SampleDataFactory;

public class VDTreeNodeTest extends TestCase {

	private RepFactory f;
	private VWorkspace vwsp;

	public VDTreeNodeTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.f = new RepFactory();
		this.vwsp = new VWorkspace(f.createWorkspace());
	}

	public void testPopulateVDRows() throws JSONException {
		Worksheet ws = SampleDataFactory//
				// .createSampleJsonWithNestedTable1(vwsp.getWorkspace())//
				.createSampleJsonWithNestedTable2(true/* true: 2 rows */, vwsp.getWorkspace())//
		// .createSampleJson(vwsp.getWorkspace(), 1)//
		// .createJsonWithFunnyCharacters(vwsp.getWorkspace())//
		//.createFlatWorksheet(vwsp.getWorkspace(), 2, 2)
		;

		System.err.println(ws.getDataTable().prettyPrint(f));

		vwsp.addAllWorksheets();
		List<HNodePath> headers = ws.getHeaders().getAllPaths();
		VTableHeadings vHeadings = new VTableHeadings(headers, ws.getHeaders()
				.getId());

		// System.err.println(Util.prettyPrintJson(vHeadings.prettyPrintJson(
		// new JSONStringer()).toString()));

		VDTableData vData = new VDTableData(vHeadings, vwsp.getVWorksheet(ws
				.getId()), vwsp);
		System.err.println(Util.prettyPrintJson(vData.prettyPrintJson(
				new JSONStringer(), /* verbose */false).toString()));
	}
}
