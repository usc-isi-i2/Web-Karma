package edu.isi.karma.webserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetDataUpdate;
import edu.isi.karma.controller.update.WorksheetHeadersUpdate;
import edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate;
import edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class GetExampleJSON extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		WorkspaceManager mgr = new WorkspaceManager();
		Workspace workspace = mgr.getFactory().createWorkspace();
		VWorkspace vwsp = new VWorkspace(workspace);

		WorkspaceRegistry.getInstance().register(new ExecutionController(vwsp));
	
		SampleDataFactory.createSample1small(workspace);
		SampleDataFactory.createSample1(workspace);
		SampleDataFactory.createSampleJsonWithNestedTable2(false/* true: 2 rows */,
				vwsp.getWorkspace());
//		//SampleDataFactory.createFlatWorksheet(workspace, 10000, 6);
//	//	SampleDataFactory.createFlatWorksheet(workspace, 2, 2);
//		//SampleDataFactory.createFromJsonTextFile(workspace, "samplejson-1.txt");
//		SampleDataFactory.createJsonWithFunnyCharacters(workspace);
		SampleDataFactory.createSampleJson(workspace, 3);
		SampleDataFactory.createSampleJsonWithEmptyNestedTable1(workspace);
		SampleDataFactory.createSampleJsonWithEmptyNestedTable2(workspace);
		SampleDataFactory.createSampleJsonWithEmptyNestedTable3(workspace);
		SampleDataFactory.createSampleJsonWithEmptyNestedTable4(workspace);
		SampleDataFactory.createUnitTest1(workspace);
		SampleDataFactory.createUnitTest2(workspace);
		SampleDataFactory.createUnitTest3(workspace);
		SampleDataFactory.createUnitTest4(workspace);
//		SampleDataFactory.createUnitTest5(workspace);
//		//	SampleDataFactory.createFromJsonTextFile(workspace, "unit-test-json.json");
//	//	SampleDataFactory.createFromJsonTextFile(workspace, "testGenerateUnitTest1.json");
//		SampleDataFactory.createFromJsonTextFile(workspace, "testGenerateUnitTest2.json");
//		SampleDataFactory.createFromJsonTextFile(workspace, "testGenerateUnitTest4.json");
//		SampleDataFactory.createFromJsonTextFile(workspace, "testSampleJsonWithEmptyNestedTable1.json");

		// Put all created worksheet models in the view.
		vwsp.addAllWorksheets();

		UpdateContainer c = new UpdateContainer();
		c.add(new WorksheetListUpdate(vwsp.getVWorksheetList()));
		for (VWorksheet vw : vwsp.getVWorksheetList().getVWorksheets()) {
			c.add(new WorksheetHeadersUpdate(vw));
			c.add(new WorksheetHierarchicalHeadersUpdate(vw));
			c.add(new WorksheetHierarchicalDataUpdate(vw));
			c.add(new WorksheetDataUpdate(vw));
		}

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		c.generateJson("", pw, vwsp);

		System.err.println(sw.toString());

		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(sw.toString());
		// response.getWriter().println("session=" +
		// request.getSession(true).getId());
	}
}
