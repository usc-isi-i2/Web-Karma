/**
 * 
 */
package edu.isi.karma.rep;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;
import edu.isi.karma.webserver.SampleDataFactory;

/**
 * @author szekely
 * 
 */
public class WorksheetTest extends TestCase {

	private Workspace workspace;
	private RepFactory f;

	@SuppressWarnings("unused")
	public static Worksheet createWS1(Workspace wsp) {
		RepFactory fac = wsp.getFactory();

		Worksheet w = fac.createWorksheet("My Table", wsp);

		String hn1 = w.addHNode("Column 1", fac).getId();
		String hn5 = w.addHNode("Column 5", fac).getId();
		String hn2 = w.addHNode("Column 2", fac).getId();

		HTable hn2t1 = w.getHeaders().getHNode(hn2)
				.addNestedTable("Nested Table", w, fac);
		String hn3 = hn2t1.addHNode("Column 3", w, fac).getId();
		String hn4 = hn2t1.addHNode("Column 4", w, fac).getId();

		Row r1 = w.addRow(fac);
		r1.setValue(hn1, "El Segundo").setValue(hn5, "Photography");

		Row nr1 = r1.addNestedRow(hn2, fac);
		nr1.setValue(hn3, "Pedro").setValue(hn4, "Szekely");

		Row nr2 = r1.addNestedRow(hn2, fac);
		nr2.setValue(hn3, "Aaron").setValue(hn4, "Szekely");

		Row r2 = w.addRow(fac);

		String hn6 = hn2t1.addHNode("Column 6", w, fac).getId();
		String hn7 = w.getHeaders().addHNode("Column 7", w, fac).getId();
		return w;
	}

	public static Worksheet createWS2(Workspace wsp) {
		RepFactory fac = wsp.getFactory();

		Worksheet w = fac.createWorksheet("Complex Smaple Table", wsp);
		String ss = w.addHNode("Social Security", fac).getId();
		String personContainer = w.addHNode("Person", fac).getId();
		String addressContainer = w.addHNode("Address", fac).getId();
		String relativesContainer = w.addHNode("Relatives", fac).getId();

		HTable personTable = w.getHeaders().getHNode(personContainer)
				.addNestedTable("Person Table", w, fac);
		String firstName = personTable.addHNode("First Name", w, fac).getId();
		String lastName = personTable.addHNode("Last Name", w, fac).getId();

		HTable addressTable = w.getHeaders().getHNode(addressContainer)
				.addNestedTable("Address Table", w, fac);
		String what = addressTable.addHNode("What", w, fac).getId();
		String address = addressTable.addHNode("Address", w, fac).getId();

		HTable relativesTable = w.getHeaders().getHNode(relativesContainer)
				.addNestedTable("Relatives Person Table", w, fac);
		String relFirstName = relativesTable.addHNode("Relatives First Name",
				w, fac).getId();
		String relLastName = relativesTable.addHNode("Relatives Last Name", w,
				fac).getId();

		// Row 1
		Row r1 = w.addRow(fac);
		r1.setValue(ss, "123-45-6789");
		r1.addNestedRow(personContainer, fac).setValue(firstName, "Pedro")
				.setValue(lastName, "Szekely");
		r1.addNestedRow(addressContainer, fac).setValue(what, "home")
				.setValue(address, "1401 E Maple Ave, El Segundo, CA 90245");
		r1.addNestedRow(addressContainer, fac)
				.setValue(what, "work")
				.setValue(address,
						"4676 Admiralty Way #1000, Marina del Rey, CA 90292");
		r1.addNestedRow(relativesContainer, fac)
				.setValue(relFirstName, "Claudia")
				.setValue(relLastName, "Szekely");
		r1.addNestedRow(relativesContainer, fac)
				.setValue(relFirstName, "Susana")
				.setValue(relLastName, "Szekely");
		r1.addNestedRow(relativesContainer, fac)
				.setValue(relFirstName, "Cristina")
				.setValue(relLastName, "Sierra");

		Row r2 = w.addRow(fac);
		r2.setValue(ss, "007-00-7007");
		r2.addNestedRow(personContainer, fac).setValue(firstName, "Shubham")
				.setValue(lastName, "Gupta");
		r2.addNestedRow(addressContainer, fac)
				.setValue(what, "home")
				.setValue(address,
						"7077 Alvern St # 118, Los Angeles, CA - 90045");
		r2.addNestedRow(addressContainer, fac)
				.setValue(what, "work")
				.setValue(address,
						"4676 Admiralty Way #1000, Marina del Rey, CA 90292");
		r2.addNestedRow(relativesContainer, fac)
				.setValue(relFirstName, "Anushree")
				.setValue(relLastName, "Mehra");
		r2.addNestedRow(relativesContainer, fac)
				.setValue(relFirstName, "Sameer")
				.setValue(relLastName, "Mohan");
		r2.addNestedRow(relativesContainer, fac)
				.setValue(relFirstName, "Suhani")
				.setValue(relLastName, "Gupta");
		r2.addNestedRow(relativesContainer, fac)
				.setValue(relFirstName, "Sarika")
				.setValue(relLastName, "Saxena");
		return w;
	}

	/**
	 * @param name
	 */
	public WorksheetTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.f = new RepFactory();
		this.workspace = f.createWorkspace();
	}

	// public void testAddRow() {
	// fail("Not yet implemented"); // TODO
	// }

	public void testAddHNode() {
		Worksheet w = f.createWorksheet("A Table", workspace);
		w.addHNode("Col 2", f);
		w.addHNode("Col 1", f);
		w.addHNode("Col 3", f);

		// Have the right number of nodes.
		assertEquals(3, w.getHeaders().getHNodes().size());

		// Nodes are in the right order.
		assertEquals("Col 2", w.getHeaders().getSortedHNodes().get(0)
				.getColumnName());
		assertEquals("Col 1", w.getHeaders().getSortedHNodes().get(1)
				.getColumnName());
		assertEquals("Col 3", w.getHeaders().getSortedHNodes().get(2)
				.getColumnName());
	}

	public void testCreateAllWorksheets() {
		Worksheet w1 = createWS1(workspace);
		Worksheet w2 = createWS2(workspace);
		Worksheet w3 = SampleDataFactory.createFlatWorksheet(workspace, 20, 3);

		// Just testing that creation of the worksheets does not throw
		// exceptions.
		assertTrue(true);

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		w1.prettyPrint("", pw, f);
		w2.prettyPrint("", pw, f);
		w3.prettyPrint("", pw, f);
		System.err.println(sw.toString());
	}

}
