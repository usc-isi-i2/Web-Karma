/**
 * 
 */
package edu.isi.karma.view;

import java.util.List;

import junit.framework.TestCase;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.SampleDataFactory;

/**
 * @author szekely
 * 
 */
public class ViewFactoryTest extends TestCase {

	private RepFactory f;
	private Workspace workspace;
	private ViewFactory vf;
	private ViewPreferences pref;

	/**
	 * @param name
	 */
	public ViewFactoryTest(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.f = new RepFactory();
		this.workspace = f.createWorkspace();
		this.vf = new ViewFactory();
		this.pref = new ViewPreferences();
	}

	/**
	 * Test method for
	 * {@link edu.isi.karma.view.ViewFactory#createVWorksheet(edu.isi.karma.rep.Worksheet, java.util.List, java.util.List, edu.isi.karma.view.ViewPreferences)}
	 * .
	 */
	public void testCreateVWorksheet() {
		Worksheet w = SampleDataFactory.createFlatWorksheet(workspace, 20, 3);
		List<HNodePath> cols = w.getHeaders().getAllPaths();
		List<Row> rows = w.getDataTable().getRows(0, 10);

		VWorksheet vw = vf.createVWorksheet(w, cols, rows, pref);

		assertEquals(10, vw.getDataTable().getRows().size());
		// TODO: test that it has the right data.
	}

}
