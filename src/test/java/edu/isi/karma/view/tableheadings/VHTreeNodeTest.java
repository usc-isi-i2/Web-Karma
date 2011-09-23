/**
 * 
 */
package edu.isi.karma.view.tableheadings;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import junit.framework.TestCase;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.util.Util;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.SampleDataFactory;

/**
 * @author szekely
 * 
 */
public class VHTreeNodeTest extends TestCase {

	private RepFactory f;
	private VWorkspace vwsp;

	/**
	 * @param name
	 */
	public VHTreeNodeTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.f = new RepFactory();
		this.vwsp = new VWorkspace(f.createWorkspace());
	}

	public void testAddColumns() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		Worksheet ws = SampleDataFactory//
				 .createSampleJsonWithEmptyNestedTable1(vwsp.getWorkspace())//
				 //.createSampleJson(vwsp.getWorkspace(), 1)//
				//.createJsonWithFunnyCharacters(vwsp.getWorkspace())//
		;
		List<HNodePath> headers = ws.getHeaders().getAllPaths();

		ws.getHeaders().prettyPrint("__", pw, f);
		System.err.println(sw.toString());

		VHTreeNode root = new VHTreeNode(ws.getHeaders().getId());
		root.addColumns(headers);
		root.computeDerivedInformation();

		System.err.println(root.prettyPrint());

		vwsp.addAllWorksheets();
		UpdateContainer c = new UpdateContainer();
		for (VWorksheet vw : vwsp.getVWorksheetList().getVWorksheets()) {
			c.add(new WorksheetHierarchicalHeadersUpdate(vw));
		}

		StringWriter sw1 = new StringWriter();
		PrintWriter pw1 = new PrintWriter(sw1);
		c.generateJson("", pw1, vwsp);
		System.err.println(Util.prettyPrintJson(sw1.toString()));

		assertTrue(true);
	}

}
