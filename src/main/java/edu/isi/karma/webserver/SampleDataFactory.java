package edu.isi.karma.webserver;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.Util;

public class SampleDataFactory {

	private static Logger logger = LoggerFactory
			.getLogger(SampleDataFactory.class);

	public static Worksheet createSample1(Workspace workspace) {
		RepFactory f = workspace.getFactory();

		Worksheet w = f.createWorksheet("Complex Smaple Table", workspace);
		String ss = w.addHNode("Social Security", f).getId();
		String personContainer = w.addHNode("Person", f).getId();
		String addressContainer = w.addHNode("Address", f).getId();
		String relativesContainer = w.addHNode("Relatives", f).getId();

		HTable personTable = w.getHeaders().getHNode(personContainer)
				.addNestedTable("Person Table", w, f);
		String firstName = personTable.addHNode("First Name", w, f).getId();
		String lastName = personTable.addHNode("Last Name", w, f).getId();

		HTable addressTable = w.getHeaders().getHNode(addressContainer)
				.addNestedTable("Address Table", w, f);
		String what = addressTable.addHNode("What", w, f).getId();
		String address = addressTable.addHNode("Address", w, f).getId();

		HTable relativesTable = w.getHeaders().getHNode(relativesContainer)
				.addNestedTable("Relatives Person Table", w, f);
		String relFirstName = relativesTable.addHNode("Relatives First Name",
				w, f).getId();
		String relLastName = relativesTable.addHNode("Relatives Last Name", w,
				f).getId();

		// Row 1
		Row r1 = w.addRow(f);
		r1.setValue(ss, "123-45-6789");
		r1.addNestedRow(personContainer, f).setValue(firstName, "Pedro")
				.setValue(lastName, "Szekely");
		r1.addNestedRow(personContainer, f).setValue(firstName, "Peter")
				.setValue(lastName, "Szekely");
		r1.addNestedRow(personContainer, f).setValue(firstName, "P Alejandro")
				.setValue(lastName, "Szekely");
		r1.addNestedRow(personContainer, f).setValue(firstName, "Pedro A")
				.setValue(lastName, "Szekely");
		r1.addNestedRow(personContainer, f).setValue(firstName, "Pablo")
				.setValue(lastName, "Szekely");
		r1.addNestedRow(personContainer, f).setValue(firstName, "Piotr")
				.setValue(lastName, "Szekely");
		r1.addNestedRow(personContainer, f).setValue(firstName, "Peter")
				.setValue(lastName, "Szekeli");
		r1.addNestedRow(personContainer, f).setValue(firstName, "Peter")
				.setValue(lastName, "CK Lee");
		r1.addNestedRow(personContainer, f).setValue(firstName, "Peter")
				.setValue(lastName, "Zekely");
		r1.addNestedRow(personContainer, f).setValue(firstName, "Peter")
				.setValue(lastName, "Zsekely");
		r1.addNestedRow(personContainer, f).setValue(firstName, "Peter")
				.setValue(lastName, "Szeke");
		r1.addNestedRow(personContainer, f).setValue(firstName, "Peter")
				.setValue(lastName, "Sequeli");
		r1.addNestedRow(addressContainer, f).setValue(what, "home")
				.setValue(address, "1401 E Maple Ave, El Segundo, CA 90245");
		r1.addNestedRow(addressContainer, f)
				.setValue(what, "work")
				.setValue(address,
						"4676 Admiralty Way #1000, Marina del Rey, CA 90292");
		r1.addNestedRow(relativesContainer, f)
				.setValue(relFirstName, "Claudia")
				.setValue(relLastName, "Szekely");
		r1.addNestedRow(relativesContainer, f).setValue(relFirstName, "Susana")
				.setValue(relLastName, "Szekely");
		r1.addNestedRow(relativesContainer, f)
				.setValue(relFirstName, "Cristina")
				.setValue(relLastName, "Sierra");

		Row r2 = w.addRow(f);
		r2.setValue(ss, "007-00-7007");
		r2.addNestedRow(personContainer, f).setValue(firstName, "Shubham")
				.setValue(lastName, "Gupta");
		r2.addNestedRow(addressContainer, f)
				.setValue(what, "home")
				.setValue(address,
						"7077 Alvern St # 118, Los Angeles, CA - 90045");
		r2.addNestedRow(addressContainer, f)
				.setValue(what, "work")
				.setValue(address,
						"4676 Admiralty Way #1000, Marina del Rey, CA 90292");
		r2.addNestedRow(relativesContainer, f)
				.setValue(relFirstName, "Anushree")
				.setValue(relLastName, "Mehra");
		r2.addNestedRow(relativesContainer, f).setValue(relFirstName, "Sameer")
				.setValue(relLastName, "Mohan");
		r2.addNestedRow(relativesContainer, f).setValue(relFirstName, "Suhani")
				.setValue(relLastName, "Gupta");
		r2.addNestedRow(relativesContainer, f).setValue(relFirstName, "Sarika")
				.setValue(relLastName, "Saxena");
		return w;
	}

	public static Worksheet createSample1small(Workspace workspace) {
		RepFactory f = workspace.getFactory();

		Worksheet w = f.createWorksheet("Complex Smaple Table", workspace);
		String ss = w.addHNode("Social Security", f).getId();
		String personContainer = w.addHNode("Person", f).getId();
		String addressContainer = w.addHNode("Address", f).getId();
		String relativesContainer = w.addHNode("Relatives", f).getId();

		HTable personTable = w.getHeaders().getHNode(personContainer)
				.addNestedTable("Person Table", w, f);
		String firstName = personTable.addHNode("First Name", w, f).getId();
		String lastName = personTable.addHNode("Last Name", w, f).getId();

		HTable addressTable = w.getHeaders().getHNode(addressContainer)
				.addNestedTable("Address Table", w, f);
		String what = addressTable.addHNode("What", w, f).getId();
		String address = addressTable.addHNode("Address", w, f).getId();

		HTable relativesTable = w.getHeaders().getHNode(relativesContainer)
				.addNestedTable("Relatives Person Table", w, f);
		String relFirstName = relativesTable.addHNode("Relatives First Name",
				w, f).getId();
		String relLastName = relativesTable.addHNode("Relatives Last Name", w,
				f).getId();

		// Row 1
		Row r1 = w.addRow(f);
		r1.setValue(ss, "123-45-6789");
		r1.addNestedRow(personContainer, f).setValue(firstName, "Pedro")
				.setValue(lastName, "Szekely");
		r1.addNestedRow(addressContainer, f).setValue(what, "home")
				.setValue(address, "1401 E Maple Ave");
		r1.addNestedRow(addressContainer, f).setValue(what, "work")
				.setValue(address, "4676 Admiralty");
		r1.addNestedRow(relativesContainer, f)
				.setValue(relFirstName, "Claudia")
				.setValue(relLastName, "Szekely");
		r1.addNestedRow(relativesContainer, f).setValue(relFirstName, "Susana")
				.setValue(relLastName, "Szekely");
		r1.addNestedRow(relativesContainer, f)
				.setValue(relFirstName, "Cristina")
				.setValue(relLastName, "Sierra");

		return w;
	}

	public static Worksheet createFlatWorksheet(Workspace wsp, int numRows,
			int numColumns) {

		Worksheet w = wsp.getFactory().createWorksheet(
				"Table (" + numRows + ", " + numColumns + ")", wsp);

		for (int c = 1; c <= numColumns; c++) {
			w.getHeaders().addHNode("Column " + c, w, wsp.getFactory());
		}

		int vCount = 1;
		for (int r = 1; r <= numRows; r++) {
			Row row = w.addRow(wsp.getFactory());
			for (HNode hn : w.getHeaders().getSortedHNodes()) {
				row.setValue(hn.getId(), "Value " + vCount);
				vCount += 1;
			}
		}

		return w;
	}

	public static Worksheet createFromJsonTextFile(Workspace workspace,
			String fileName) {
		FileReader r;
		try {
			r = new FileReader(fileName);
			Object o = Util.createJson(r);
			// System.err.println("JSON:" + o.toString());
			// System.err.println("JSON:" + JSONObject.quote(o.toString()));
			JsonImport ji = new JsonImport(o, fileName, workspace);
			Worksheet w = ji.generateWorksheet();
			return w;
		} catch (FileNotFoundException e) {
			logger.error("Cannot read file " + fileName + ".");
			e.printStackTrace();
		} catch (JSONException e) {
			logger.error("Could not parse JSON in file " + fileName + ".");
			e.printStackTrace();
		}
		return null;
	}

	public static String getSampleJsonString(int numRows) {
		JSONStringer x = new JSONStringer();
		Random rand = new Random(0);
		try {
			JSONWriter top = x.array();
			for (int i = 1; i <= numRows; i++) {
				JSONWriter o = top.object();
				o.key("a").value("a" + i);

				o.key("b").value("b" + i);

				JSONWriter c = o.key("c").array();
				for (int ci = 1; ci <= 10; ci++) {
					JSONWriter co = c.object();
					co.key("c.1").value("c.1_" + ci);
					if (rand.nextBoolean()) {
						co.key("c.2").value("c.2_" + ci);
					}

					if (rand.nextBoolean()) {
						JSONWriter c3a = co.key("c.3").array();

						for (int c3i = 1; c3i < rand.nextInt(10) + 1; c3i++) {
							JSONWriter c3o = c3a.object();
							if (rand.nextBoolean()) {
								co.key("c.3.1").value(
										"c.3.1_" + c3i + "+" + ci + "+" + i);
							}
							if (rand.nextBoolean()) {
								co.key("c.3.2").value("c.3.2_" + c3i);
							}
							if (rand.nextBoolean()) {
								co.key("c.3.3").value("c.3.3_" + c3i);
							}
							if (rand.nextBoolean()) {
								JSONWriter c34o = co.key("c.3.4").object();
								c34o.key("c.3.4.1").value("c.3.4.1_X");
								c34o.key("c.3.4.2").value("c.3.4.1_Y");
								c34o.endObject();
							}
							c3o.endObject();
						}

						c3a.endArray();
					}

					co.endObject();
				}
				c.endArray();

				// List of primitive values.
				JSONWriter d = o.key("d").array();
				for (int di = 1; di < rand.nextInt(10) + 1; di++) {
					d.value("d" + di);
				}
				d.endArray();

				JSONWriter e = o.key("e").array();
				for (int ei = 1; ei < rand.nextInt(10) + 1; ei++) {
					if (rand.nextBoolean()) {
						e.value("e" + ei + "+" + i);
					} else {
						e.object().key("e.1").value("e.1_" + ei).key("e.2")
								.value("e.2_" + ei).endObject();
					}
				}
				e.endArray();

				o.endObject();
			}
			top.endArray();
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return x.toString();
	}


	public static String getJsonForUnitTest1() {
		JSONStringer x = new JSONStringer();
		Random rand = new Random(0);
		try {
			JSONWriter top = x.array();
			for (int i = 1; i <= 2; i++) {
				JSONWriter o = top.object();
				o.key("a").value("a" + i);

				o.key("b").value("b" + i);

				JSONWriter c = o.key("c").array();
				for (int ci = 1; ci <= 10; ci++) {
					JSONWriter co = c.object();
					co.key("c.1").value("c.1_" + ci);
					if (rand.nextBoolean()) {
						co.key("c.2").value("c.2_" + ci);
					}

					if (rand.nextBoolean()) {
						//
					}

					co.endObject();
				}
				c.endArray();

				// List of primitive values.
				JSONWriter d = o.key("d").array();
				for (int di = 1; di < rand.nextInt(10) + 1; di++) {
					d.value("d" + di);
				}
				d.endArray();

				JSONWriter e = o.key("e").array();
				for (int ei = 1; ei < rand.nextInt(10) + 1; ei++) {
					if (rand.nextBoolean()) {
						e.value("e" + ei + "+" + i);
					} else {
						e.object().key("e.1").value("e.1_" + ei).key("e.2")
								.value("e.2_" + ei).endObject();
					}
				}
				e.endArray();

				o.endObject();
			}
			top.endArray();
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return x.toString();
	}

	public static String getJsonForUnitTest4() {
		JSONStringer x = new JSONStringer();
		Random rand = new Random(0);
		try {
			JSONWriter top = x.array();
			for (int i = 1; i <= 1; i++) {
				JSONWriter o = top.object();

				JSONWriter c = o.key("c").array();
				for (int ci = 1; ci <= 3; ci++) {
					JSONWriter co = c.object();
					if (rand.nextBoolean()) {
						co.key("c.2").value("c.2_" + ci);
					}

					if (rand.nextBoolean()) {
						JSONWriter c3a = co.key("c.3").array();

						for (int c3i = 1; c3i < 3; c3i++) {
							JSONWriter c3o = c3a.object();
							if (rand.nextBoolean()) {
								co.key("c.3.1").value(
										"c.3.1_" + c3i + "+" + ci + "+" + i);
							}
							if (rand.nextBoolean()) {
								co.key("c.3.3").value("c.3.3_" + c3i);
							}
							if (rand.nextBoolean()) {
								JSONWriter c34o = co.key("c.3.4").object();
								c34o.key("c.3.4.2").value("c.3.4.1_Y");
								c34o.endObject();
							}
							c3o.endObject();
						}

						c3a.endArray();
					}

					co.endObject();
				}
				c.endArray();

				o.endObject();
			}
			top.endArray();
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return x.toString();
	}

	public static String getJsonForUnitTest5() {
		JSONStringer x = new JSONStringer();
		Random rand = new Random(0);
		try {
			JSONWriter top = x.array();
			for (int i = 1; i <= 1; i++) {
				JSONWriter o = top.object();

				JSONWriter c = o.key("c").array();
				for (int ci = 1; ci <= 4; ci++) {
					JSONWriter co = c.object();
					co.key("c.1").value("c.1_" + ci);
					if (rand.nextBoolean()) {
						co.key("c.2").value("c.2_" + ci);
					}

					if (rand.nextBoolean()) {
						JSONWriter c3a = co.key("c.3").array();

						for (int c3i = 1; c3i < rand.nextInt(10) + 1; c3i++) {
							JSONWriter c3o = c3a.object();
							if (rand.nextBoolean()) {
								co.key("c.3.1").value(
										"c.3.1_" + c3i + "+" + ci + "+" + i);
							}
							if (rand.nextBoolean()) {
								co.key("c.3.2").value("c.3.2_" + c3i);
							}
							if (rand.nextBoolean()) {
								co.key("c.3.3").value("c.3.3_" + c3i);
							}
							if (rand.nextBoolean()) {
								JSONWriter c34o = co.key("c.3.4").object();
								c34o.key("c.3.4.1").value("c.3.4.1_X");
								c34o.key("c.3.4.2").value("c.3.4.1_Y");
								c34o.endObject();
							}
							c3o.endObject();
						}

						c3a.endArray();
					}

					co.endObject();
				}
				c.endArray();

				JSONWriter e = o.key("e").array();
				for (int ei = 1; ei < 6; ei++) {
					if (rand.nextBoolean()) {
						e.value("e" + ei + "+" + i);
					} else {
						e.object().key("e.1").value("e.1_" + ei).key("e.2")
								.value("e.2_" + ei).endObject();
					}
				}
				e.endArray();

				o.endObject();
			}
			top.endArray();
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return x.toString();
	}

	
	
	
	public static Worksheet createUnitTest1(Workspace workspace) {
		return createWorksheetFromJsonString("unit-test-1",
				getJsonForUnitTest1(), workspace);
	}

	public static Worksheet createUnitTest2(Workspace workspace) {
		String s = "[{\"e\":[],\"c\":[{\"c.1\":\"c.1_1\"}]},{\"d\":[\"d1\"],\"e\":[\"e1+2\"],\"c\":[{\"c.1\":\"c.1_1\"}]}]";
		return createWorksheetFromJsonString("unit-test-2", s, workspace);
	}

	public static Worksheet createUnitTest3(Workspace workspace) {
		String s = "[{\"d\":[\"d4\"],\"e\":[],\"c\":[{\"c.1\":\"c.1_1\"}]},{\"d\":[\"d1\"],\"e\":[\"e1+2\"],\"c\":[{\"c.1\":\"c.1_1\"}]}]";
		return createWorksheetFromJsonString("unit-test-3", s, workspace);
	}

	public static Worksheet createUnitTest4(Workspace workspace) {
		return createWorksheetFromJsonString("unit-test-4",
				getJsonForUnitTest4(), workspace);
	}

	public static Worksheet createUnitTest5(Workspace workspace) {
		return createWorksheetFromJsonString("unit-test-5",
				getJsonForUnitTest5(), workspace);
	}
	
	public static Worksheet createWorksheetFromJsonString(String name,
			String jsonString, Workspace workspace) {
		try {
			Object o = Util.createJson(jsonString);
			Util.writeJsonFile(o, name + ".json");
			JsonImport ji = new JsonImport(o, name, workspace);
			Worksheet w = ji.generateWorksheet();
			return w;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Worksheet createSampleJson(Workspace workspace, int numRows) {
		try {
			Object o = Util.createJson(getSampleJsonString(numRows));
			JsonImport ji = new JsonImport(o, "Sample JSON", workspace);
			Worksheet w = ji.generateWorksheet();
			return w;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Worksheet createJsonWithFunnyCharacters(Workspace workspace) {
		try {

			JSONStringer x = new JSONStringer();
			String html = "<ul><li>item 1</li></ul>";
			x.object().key("double quote")
					.value("string with \"double quotes\"")
					.key("single quotes")
					.value("'starts has ' and \"ends\" with '").key("tabs")
					.value("there is a tab between \"x\" and \"y\": x\ty")
					.key("newlines").value("there sho\nuld be a newline")
					.key("HTML").value(html).key("URL").value("http://cnn.com")
					.endObject();

			JsonImport ji = new JsonImport(Util.createJson(x.toString()),
					"Funny Characters", workspace);
			Worksheet w = ji.generateWorksheet();
			return w;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Worksheet createSampleJsonWithEmptyNestedTable1(
			Workspace workspace) {
		try {

			JSONStringer x = new JSONStringer();

			JSONWriter topA = x.array();
			topA.object().key("a").value("a_1").key("b").array()
					//
					.object().key("b.1").value("b.1_1").key("b.2")
					.value("b.2_1").endObject()
					//
					// .object().key("b.1").value("b.1_2").key("b.2")
					// .value("b.2_2").endObject()
					//
					.endArray().endObject();

			topA.object().key("a").value("a_2").endObject();

			topA.endArray();

			JsonImport ji = new JsonImport(Util.createJson(x.toString()),
					"Empty Nested Table 1", workspace);
			Worksheet w = ji.generateWorksheet();
			return w;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Worksheet createSampleJsonWithEmptyNestedTable2(
			Workspace workspace) {
		try {

			JSONStringer x = new JSONStringer();

			JSONWriter topA = x.array();
			topA.object().key("a").value("a_1").key("b").array()
					//
					.object().key("b.1").value("b.1_1").key("b.2")
					.value("b.2_1").endObject()
					//
					.object().key("b.1").value("b.1_2").endObject()
					//
					.endArray().endObject();

			topA.object().key("a").value("a_2").endObject();

			topA.endArray();

			JsonImport ji = new JsonImport(Util.createJson(x.toString()),
					"Empty Nested Table 2", workspace);
			Worksheet w = ji.generateWorksheet();
			return w;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Worksheet createSampleJsonWithEmptyNestedTable3(
			Workspace workspace) {
		try {

			JSONStringer x = new JSONStringer();

			JSONWriter topA = x.array();
			topA.object().key("a").value("a_1").key("b").array()
					//
					.object().key("b.1").value("b.1_1").key("b.2")
					.value("b.2_1").endObject()
					//
					.endArray().endObject();

			topA.object().key("b").array().endArray().endObject();

			topA.endArray();

			JsonImport ji = new JsonImport(Util.createJson(x.toString()),
					"Empty Nested Table 3", workspace);
			Worksheet w = ji.generateWorksheet();
			return w;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Worksheet createSampleJsonWithEmptyNestedTable4(
			Workspace workspace) {
		try {

			JSONStringer x = new JSONStringer();

			JSONWriter topA = x.array();
			topA.object().key("a").value("a_1").key("b").array()
					//
					.object().key("b.1").value("b.1_1").key("b.2")
					.value("b.2_1").endObject()
					//
					.object().key("b.2").value("b.2_2").endObject()
					//
					.object().key("b.1").value("b.1_3").endObject()
					//
					.endArray().endObject();

			topA.object().key("a").value("a_2").endObject();

			topA.endArray();

			JsonImport ji = new JsonImport(Util.createJson(x.toString()),
					"Empty Nested Table 4", workspace);
			Worksheet w = ji.generateWorksheet();
			return w;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Worksheet createSampleJsonWithNestedTable1(Workspace workspace) {
		try {

			JSONStringer x = new JSONStringer();

			JSONWriter topA = x.array();
			topA.object().key("a").value("a/1").key("b")
					.array()
					//
					.object().key("b.1").value("b.1/1,1").key("b.2")
					.value("b.2/1,1").endObject()
					//
					.object().key("b.1").value("b.1/1,2").key("b.2")
					.value("b.2/1,2").endObject()
					//
					.endArray().endObject();

			topA.object().key("a").value("a/2").key("b")
					.array()
					//
					.object().key("b.1").value("b.1/2,1").key("b.2")
					.value("b.2/2,1").endObject()
					//
					.object().key("b.1").value("b.1/2,2").key("b.2")
					.value("b.2/2,2").endObject()
					//
					.endArray().endObject();

			topA.endArray();

			JsonImport ji = new JsonImport(Util.createJson(x.toString()),
					"Nested Table 1", workspace);
			Worksheet w = ji.generateWorksheet();
			return w;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Worksheet createSampleJsonWithNestedTable2(
			boolean hasTwoRows, Workspace workspace) {
		try {

			JSONStringer x = new JSONStringer();

			JSONWriter topA = x.array();
			topA.object().key("a").value("a/1").key("b").array()
			//
					.object()//
					.key("b.1").value("b.1/1,1")//
					.key("b.2").array()//
					.object()//
					.key("b.2.1").value("b.2.1/1,1,1")//
					.key("b.2.2").value("b.2.2/1,1,1")//
					.endObject()//
					.object()//
					.key("b.2.1").value("b.2.1/1,1,2")//
					.key("b.2.2").value("b.2.2/1,1,2")//
					.endObject()//
					.endArray()//
					.endObject()//
					//
					.object()//
					.key("b.1").value("b.1/1,2")//
					.key("b.2").array()//
					.object()//
					.key("b.2.1").value("b.2.1/1,2,1")//
					.key("b.2.2").value("b.2.2/1,2,1")//
					.endObject()//
					.endArray()//
					.endObject();//
			topA.endArray().endObject();

			if (hasTwoRows) {
				topA.object().key("a").value("a/2").key("b").array()
				//
						.object()//
						.key("b.1").value("b.1/2,1")//
						.key("b.2").array()//
						.object()//
						.key("b.2.1").value("b.2.1/2,1,1")//
						.key("b.2.2").value("b.2.2/2,1,1")//
						.endObject()//
						.endArray()//
						.endObject()//
						//
						.object()//
						.key("b.1").value("b.1/2,2")//
						.key("b.2").array()//
						.object()//
						.key("b.2.1").value("b.2.1/2,2,1")//
						.key("b.2.2").value("b.2.2/2,2,1")//
						.endObject()//
						.endArray()//
						.endObject()//
				//
				;

				topA.endArray().endObject();
			}

			topA.endArray();

			JsonImport ji = new JsonImport(Util.createJson(x.toString()),
					"Nested Table 2", workspace);
			Worksheet w = ji.generateWorksheet();
			return w;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}
