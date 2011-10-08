package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.CellType;
import edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.util.Util;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.SampleDataFactory;

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

	private void assertSeparatorRow(JSONObject r) throws JSONException {
		assertEquals(JsonKeys.separatorRow.name(),
				r.getString(JsonKeys.rowType.name()));
	}

	private void assertContentRow(JSONObject r) throws JSONException {
		assertEquals(JsonKeys.contentRow.name(),
				r.getString(JsonKeys.rowType.name()));
	}

	//
	private void assertPosition(JSONObject c, int row, int col)
			throws JSONException {
		assertEquals(row, c.getInt("_row"));
		assertEquals(col, c.getInt("_col"));
	}

	//
	private JSONObject getCell(JSONObject rObj, int index) throws JSONException {
		JSONArray r = rObj.getJSONArray(JsonKeys.rowCells.name());
		JSONObject c = r.getJSONObject(index);
		return c;
	}

	private void assertAttributes(JSONObject c, CellType cellType,
			String hTableId, String encodedStyles) throws JSONException {
		String attributes = c.getString(JsonKeys.attr.name());
		String[] elements = attributes.split(":");
		String[] styles = encodedStyles.split(":");
		assertEquals(cellType.code(), elements[0]);
		assertEquals(hTableId, elements[1]);
		assertEquals(styles[0], elements[3]);
		assertEquals(styles[1], elements[4]);
		assertEquals(styles[2], elements[5]);
		assertEquals(styles[3], elements[6]);
	}

	public void testGenerateJson1() throws JSONException {
		@SuppressWarnings("unused")
		Worksheet ws = SampleDataFactory//
				// .createSampleJsonWithNestedTable1(vwsp.getWorkspace())//
				.createSampleJsonWithNestedTable2(false/* true: 2 rows */,
						vwsp.getWorkspace())//
		// .createSampleJson(vwsp.getWorkspace(), 1)//
		// .createJsonWithFunnyCharacters(vwsp.getWorkspace())//
		// .createFlatWorksheet(vwsp.getWorkspace(), 2, 2)
		;

		vwsp.addAllWorksheets();
		UpdateContainer uc = new UpdateContainer();
		for (VWorksheet vw : vwsp.getVWorksheetList().getVWorksheets()) {
			uc.add(new WorksheetHierarchicalDataUpdate(vw));
		}

		@SuppressWarnings("unused")
		VWorksheet vw = vwsp.getVWorksheetList().getVWorksheets().get(0);
		// System.err.println(Util.prettyPrintJson(vw.getViewTableHeadings()
		// .prettyPrintJson(new JSONStringer()).toString()));
		//
		// System.err.println(Util.prettyPrintJson(vw.getVDTableData()
		// .prettyPrintJson(new JSONStringer(), /* verbose */false, vwsp)
		// .toString()));

		StringWriter sw1 = new StringWriter();
		PrintWriter pw1 = new PrintWriter(sw1);
		uc.generateJson("", pw1, vwsp);
		// System.err.println(Util.prettyPrintJson(sw1.toString()));

		JSONObject o = new JSONObject(sw1.toString());
		Util.writeJsonFile(o, "./testGenerateJson1.json");

		JSONArray rows = o.getJSONArray("elements").getJSONObject(0)
				.getJSONArray(JsonKeys.rows.name());
		assertEquals(9, rows.length());

		{ // r0 top separator 1.
			JSONObject r = rows.getJSONObject(0);
			assertSeparatorRow(r);
			{ // c0 vertical separator
				JSONObject c = getCell(r, 0);
				assertPosition(c, 0, 0);
				assertAttributes(c, CellType.columnSpace, "HT3", "o:_:o:_");
			}
			{ // c0 vertical separator
				JSONObject c = getCell(r, 1);
				assertPosition(c, 0, 0);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:o:_");
			}
			{ // c0
				JSONObject c = getCell(r, 2);
				assertPosition(c, 0, 0);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:o:_");
			}
			{ // c1
				JSONObject c = getCell(r, 3);
				assertPosition(c, 0, 1);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:o:_");
			}
			{ // c1/c2 vertical separator
				JSONObject c = getCell(r, 4);
				assertPosition(c, 0, 1);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:o:_");
			}
			{ // c2
				JSONObject c = getCell(r, 5);
				assertPosition(c, 0, 2);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:o:_");
			}
			{ // c2/c3 vertical separator
				JSONObject c = getCell(r, 6);
				assertPosition(c, 0, 2);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:o:_");
			}
			{ // c3
				JSONObject c = getCell(r, 7);
				assertPosition(c, 0, 3);
				assertAttributes(c, CellType.rowSpace, "HT3", "i:o:o:_");
			}
		}

		{ // r0 top separator 2.
			JSONObject r = rows.getJSONObject(1);
			assertSeparatorRow(r);
			{ // c0 vertical separator
				JSONObject c = getCell(r, 0);
				assertPosition(c, 0, 0);
				assertAttributes(c, CellType.columnSpace, "HT3", "o:_:_:_");
			}
			{ // c0 vertical separator
				JSONObject c = getCell(r, 1);
				assertPosition(c, 0, 0);
				assertAttributes(c, CellType.columnSpace, "HT8", "o:_:o:_");
			}
			{ // c0
				JSONObject c = getCell(r, 2);
				assertPosition(c, 0, 0);
				assertAttributes(c, CellType.rowSpace, "HT8", "_:_:o:_");
			}
			{ // c1
				JSONObject c = getCell(r, 3);
				assertPosition(c, 0, 1);
				assertAttributes(c, CellType.rowSpace, "HT8", "_:_:o:_");
			}
			{ // c1/c2 vertical separator
				JSONObject c = getCell(r, 4);
				assertPosition(c, 0, 1);
				assertAttributes(c, CellType.columnSpace, "HT8", "_:_:o:_");
			}
			{ // c2
				JSONObject c = getCell(r, 5);
				assertPosition(c, 0, 2);
				assertAttributes(c, CellType.rowSpace, "HT8", "i:o:o:_");
			}
			{ // c2/c3 vertical separator
				JSONObject c = getCell(r, 6);
				assertPosition(c, 0, 2);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:_");
			}
			{ // c3
				JSONObject c = getCell(r, 7);
				assertPosition(c, 0, 3);
				assertAttributes(c, CellType.rowSpace, "HT3", "i:o:_:_");
			}
		}

		{ // r0 content.
			JSONObject r = rows.getJSONObject(2);
			assertContentRow(r);
			{ // c0 vertical separator
				JSONObject c = getCell(r, 0);
				assertPosition(c, 0, 0);
				assertAttributes(c, CellType.columnSpace, "HT3", "o:_:_:_");
			}
			{ // c0 vertical separator
				JSONObject c = getCell(r, 1);
				assertPosition(c, 0, 0);
				assertAttributes(c, CellType.columnSpace, "HT8", "o:_:_:_");
			}
			{ // c0
				JSONObject c = getCell(r, 2);
				assertPosition(c, 0, 0);
				assertAttributes(c, CellType.content, "HT13", "o:_:o:_");
			}
			{ // c1
				JSONObject c = getCell(r, 3);
				assertPosition(c, 0, 1);
				assertAttributes(c, CellType.content, "HT13", "i:o:o:_");
			}
			{ // c1/c2 vertical separator
				JSONObject c = getCell(r, 4);
				assertPosition(c, 0, 1);
				assertAttributes(c, CellType.columnSpace, "HT8", "_:_:_:_");
			}
			{ // c2
				JSONObject c = getCell(r, 5);
				assertPosition(c, 0, 2);
				assertAttributes(c, CellType.content, "HT8", "i:o:_:_");
			}
			{ // c2/c3 vertical separator
				JSONObject c = getCell(r, 6);
				assertPosition(c, 0, 2);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:_");
			}
			{ // c3
				JSONObject c = getCell(r, 7);
				assertPosition(c, 0, 3);
				assertAttributes(c, CellType.content, "HT3", "i:o:_:_");
			}
		}

		{ // r1 content.
			JSONObject r = rows.getJSONObject(3);
			assertContentRow(r);
			{ // c0 vertical separator
				JSONObject c = getCell(r, 0);
				assertPosition(c, 1, 0);
				assertAttributes(c, CellType.columnSpace, "HT3", "o:_:_:_");
			}
			{ // c0 vertical separator
				JSONObject c = getCell(r, 1);
				assertPosition(c, 1, 0);
				assertAttributes(c, CellType.columnSpace, "HT8", "o:_:_:_");
			}
			{ // c0
				JSONObject c = getCell(r, 2);
				assertPosition(c, 1, 0);
				assertAttributes(c, CellType.content, "HT13", "o:_:i:o");
			}
			{ // c1
				JSONObject c = getCell(r, 3);
				assertPosition(c, 1, 1);
				assertAttributes(c, CellType.content, "HT13", "i:o:i:o");
			}
			{ // c1/c2 vertical separator
				JSONObject c = getCell(r, 4);
				assertPosition(c, 1, 1);
				assertAttributes(c, CellType.columnSpace, "HT8", "_:_:_:_");
			}
			{ // c2
				JSONObject c = getCell(r, 5);
				assertPosition(c, 1, 2);
				assertAttributes(c, CellType.dummyContent, "HT8", "i:o:_:_");
			}
			{ // c2/c3 vertical separator
				JSONObject c = getCell(r, 6);
				assertPosition(c, 1, 2);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:_");
			}
			{ // c3
				JSONObject c = getCell(r, 7);
				assertPosition(c, 1, 3);
				assertAttributes(c, CellType.dummyContent, "HT3", "i:o:_:_");
			}
		}

		{ // r1 separator bottom.
			JSONObject r = rows.getJSONObject(4);
			assertSeparatorRow(r);
			{ // c0 vertical separator
				JSONObject c = getCell(r, 0);
				assertPosition(c, 1, 0);
				assertAttributes(c, CellType.columnSpace, "HT3", "o:_:_:_");
			}
			{ // c0 vertical separator
				JSONObject c = getCell(r, 1);
				assertPosition(c, 1, 0);
				assertAttributes(c, CellType.columnSpace, "HT8", "o:_:_:_");
			}
			{ // c0
				JSONObject c = getCell(r, 2);
				assertPosition(c, 1, 0);
				assertAttributes(c, CellType.rowSpace, "HT8", "_:_:_:_");
			}
			{ // c1
				JSONObject c = getCell(r, 3);
				assertPosition(c, 1, 1);
				assertAttributes(c, CellType.rowSpace, "HT8", "_:_:_:_");
			}
			{ // c1/c2 vertical separator
				JSONObject c = getCell(r, 4);
				assertPosition(c, 1, 1);
				assertAttributes(c, CellType.columnSpace, "HT8", "_:_:_:_");
			}
			{ // c2
				JSONObject c = getCell(r, 5);
				assertPosition(c, 1, 2);
				assertAttributes(c, CellType.rowSpace, "HT8", "i:o:_:_");
			}
			{ // c2/c3 vertical separator
				JSONObject c = getCell(r, 6);
				assertPosition(c, 1, 2);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:_");
			}
			{ // c3
				JSONObject c = getCell(r, 7);
				assertPosition(c, 1, 3);
				assertAttributes(c, CellType.rowSpace, "HT3", "i:o:_:_");
			}
		}

		{ // r2 separator top.
			JSONObject r = rows.getJSONObject(5);
			assertSeparatorRow(r);
			{ // c0 vertical separator
				JSONObject c = getCell(r, 0);
				assertPosition(c, 2, 0);
				assertAttributes(c, CellType.columnSpace, "HT3", "o:_:_:_");
			}
			{ // c0 vertical separator
				JSONObject c = getCell(r, 1);
				assertPosition(c, 2, 0);
				assertAttributes(c, CellType.columnSpace, "HT8", "o:_:i:_");
			}
			{ // c0
				JSONObject c = getCell(r, 2);
				assertPosition(c, 2, 0);
				assertAttributes(c, CellType.rowSpace, "HT8", "_:_:i:_");
			}
			{ // c1
				JSONObject c = getCell(r, 3);
				assertPosition(c, 2, 1);
				assertAttributes(c, CellType.rowSpace, "HT8", "_:_:i:_");
			}
			{ // c1/c2 vertical separator
				JSONObject c = getCell(r, 4);
				assertPosition(c, 2, 1);
				assertAttributes(c, CellType.columnSpace, "HT8", "_:_:i:_");
			}
			{ // c2
				JSONObject c = getCell(r, 5);
				assertPosition(c, 2, 2);
				assertAttributes(c, CellType.rowSpace, "HT8", "i:o:i:_");
			}
			{ // c2/c3 vertical separator
				JSONObject c = getCell(r, 6);
				assertPosition(c, 2, 2);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:_");
			}
			{ // c3
				JSONObject c = getCell(r, 7);
				assertPosition(c, 2, 3);
				assertAttributes(c, CellType.rowSpace, "HT3", "i:o:_:_");
			}
		}

		{ // r2 content.
			JSONObject r = rows.getJSONObject(6);
			assertContentRow(r);
			{ // c0 vertical separator
				JSONObject c = getCell(r, 0);
				assertPosition(c, 2, 0);
				assertAttributes(c, CellType.columnSpace, "HT3", "o:_:_:_");
			}
			{ // c0 vertical separator
				JSONObject c = getCell(r, 1);
				assertPosition(c, 2, 0);
				assertAttributes(c, CellType.columnSpace, "HT8", "o:_:_:_");
			}
			{ // c0
				JSONObject c = getCell(r, 2);
				assertPosition(c, 2, 0);
				assertAttributes(c, CellType.content, "HT13", "o:_:o:o");
			}
			{ // c1
				JSONObject c = getCell(r, 3);
				assertPosition(c, 2, 1);
				assertAttributes(c, CellType.content, "HT13", "i:o:o:o");
			}
			{ // c1/c2 vertical separator
				JSONObject c = getCell(r, 4);
				assertPosition(c, 2, 1);
				assertAttributes(c, CellType.columnSpace, "HT8", "_:_:_:_");
				// TODO: should be the following. Perhaps the empty row causes
				// there to be no margins.
				//
				// assertAttributes(c, CellType.columnSpace, "HT8", "_:i:_:_");
			}
			{ // c2
				JSONObject c = getCell(r, 5);
				assertPosition(c, 2, 2);
				assertAttributes(c, CellType.content, "HT8", "i:o:_:_");
				// TODO: there should be a top outline, but the empty row is not
				// generating it.
				//
				// assertAttributes(c, CellType.content, "HT8", "i:o:o:_");
			}
			{ // c2/c3 vertical separator
				JSONObject c = getCell(r, 6);
				assertPosition(c, 2, 2);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:_");
			}
			{ // c3
				JSONObject c = getCell(r, 7);
				assertPosition(c, 2, 3);
				assertAttributes(c, CellType.dummyContent, "HT3", "i:o:_:_");
			}
		}

		{ // r2 separator bottom.
			JSONObject r = rows.getJSONObject(7);
			assertSeparatorRow(r);
			{ // c0 vertical separator
				JSONObject c = getCell(r, 0);
				assertPosition(c, 2, 0);
				assertAttributes(c, CellType.columnSpace, "HT3", "o:_:_:_");
			}
			{ // c0 vertical separator
				JSONObject c = getCell(r, 1);
				assertPosition(c, 2, 0);
				assertAttributes(c, CellType.columnSpace, "HT8", "o:_:_:o");
			}
			{ // c0
				JSONObject c = getCell(r, 2);
				assertPosition(c, 2, 0);
				assertAttributes(c, CellType.rowSpace, "HT8", "_:_:_:o");
			}
			{ // c1
				JSONObject c = getCell(r, 3);
				assertPosition(c, 2, 1);
				assertAttributes(c, CellType.rowSpace, "HT8", "_:_:_:o");
			}
			{ // c1/c2 vertical separator
				JSONObject c = getCell(r, 4);
				assertPosition(c, 2, 1);
				assertAttributes(c, CellType.columnSpace, "HT8", "_:_:_:o");
			}
			{ // c2
				JSONObject c = getCell(r, 5);
				assertPosition(c, 2, 2);
				assertAttributes(c, CellType.rowSpace, "HT8", "i:o:_:o");
			}
			{ // c2/c3 vertical separator
				JSONObject c = getCell(r, 6);
				assertPosition(c, 2, 2);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:_");
			}
			{ // c3
				JSONObject c = getCell(r, 7);
				assertPosition(c, 2, 3);
				assertAttributes(c, CellType.rowSpace, "HT3", "i:o:_:_");
			}
		}

		{ // r2 separator bottom.
			JSONObject r = rows.getJSONObject(8);
			assertSeparatorRow(r);
			{ // c0 vertical separator
				JSONObject c = getCell(r, 0);
				assertPosition(c, 2, 0);
				assertAttributes(c, CellType.columnSpace, "HT3", "o:_:_:o");
			}
			{ // c0 vertical separator
				JSONObject c = getCell(r, 1);
				assertPosition(c, 2, 0);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:o");
			}
			{ // c0
				JSONObject c = getCell(r, 2);
				assertPosition(c, 2, 0);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:_:o");
			}
			{ // c1
				JSONObject c = getCell(r, 3);
				assertPosition(c, 2, 1);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:_:o");
			}
			{ // c1/c2 vertical separator
				JSONObject c = getCell(r, 4);
				assertPosition(c, 2, 1);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:o");
			}
			{ // c2
				JSONObject c = getCell(r, 5);
				assertPosition(c, 2, 2);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:_:o");
			}
			{ // c2/c3 vertical separator
				JSONObject c = getCell(r, 6);
				assertPosition(c, 2, 2);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:o");
			}
			{ // c3
				JSONObject c = getCell(r, 7);
				assertPosition(c, 2, 3);
				assertAttributes(c, CellType.rowSpace, "HT3", "i:o:_:o");
			}
		}
		//
	}

	public void testGenerateJson2() throws JSONException {
		@SuppressWarnings("unused")
		Worksheet ws = SampleDataFactory
				.createSample1small(vwsp.getWorkspace());

		vwsp.addAllWorksheets();
		UpdateContainer uc = new UpdateContainer();
		for (VWorksheet vw : vwsp.getVWorksheetList().getVWorksheets()) {
			uc.add(new WorksheetHierarchicalDataUpdate(vw));
		}

		@SuppressWarnings("unused")
		VWorksheet vw = vwsp.getVWorksheetList().getVWorksheets().get(0);
		// System.err.println(Util.prettyPrintJson(vw.getViewTableHeadings()
		// .prettyPrintJson(new JSONStringer()).toString()));
		// System.err.println(Util.prettyPrintJson(vw.getVDTableData()
		// .prettyPrintJson(new JSONStringer(), /* verbose */false, vwsp)
		// .toString()));

		String ucJson = uc.generateJson(vwsp);
		// System.err.println(Util.prettyPrintJson(ucJson));

		JSONObject o = new JSONObject(ucJson);
		JSONArray rows = o.getJSONArray("elements").getJSONObject(0)
				.getJSONArray(JsonKeys.rows.name());
		assertEquals(5, rows.length());

		{ // r0 top separator 1.
			JSONObject r = rows.getJSONObject(0);
			assertSeparatorRow(r);
			{ // c0
				JSONObject c = getCell(r, 0);
				assertPosition(c, 0, 0);
				assertAttributes(c, CellType.rowSpace, "HT3", "o:_:o:_");
			}
			{ // c0/c1 vertical separator
				JSONObject c = getCell(r, 1);
				assertPosition(c, 0, 1);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:o:_");
			}
			{ // c1
				JSONObject c = getCell(r, 2);
				assertPosition(c, 0, 1);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:o:_");
			}
			{ // c2
				JSONObject c = getCell(r, 3);
				assertPosition(c, 0, 2);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:o:_");
			}
			{ // c1/c2 vertical separator
				JSONObject c = getCell(r, 4);
				assertPosition(c, 0, 2);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:o:_");
			}
			{ // c1/c2 vertical separator
				JSONObject c = getCell(r, 5);
				assertPosition(c, 0, 3);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:o:_");
			}
			{ // c3
				JSONObject c = getCell(r, 6);
				assertPosition(c, 0, 3);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:o:_");
			}
			{ // c4
				JSONObject c = getCell(r, 7);
				assertPosition(c, 0, 4);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:o:_");
			}
			{ // c4/c5 vertical separator
				JSONObject c = getCell(r, 8);
				assertPosition(c, 0, 4);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:o:_");
			}
			{ // c4/c5 vertical separator
				JSONObject c = getCell(r, 9);
				assertPosition(c, 0, 5);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:o:_");
			}
			{ // c5
				JSONObject c = getCell(r, 10);
				assertPosition(c, 0, 5);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:o:_");
			}
			{ // c6
				JSONObject c = getCell(r, 11);
				assertPosition(c, 0, 6);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:o:_");
			}
			{ // c6 vertical separator
				JSONObject c = getCell(r, 12);
				assertPosition(c, 0, 6);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:o:o:_");
			}
		}

		{ // r0
			JSONObject r = rows.getJSONObject(1);
			assertContentRow(r);
			{ // c0
				JSONObject c = getCell(r, 0);
				assertPosition(c, 0, 0);
				assertAttributes(c, CellType.content, "HT3", "o:_:_:_");
			}
			{ // c0/c1 vertical separator
				JSONObject c = getCell(r, 1);
				assertPosition(c, 0, 1);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:_:_");
			}
			{ // c1
				JSONObject c = getCell(r, 2);
				assertPosition(c, 0, 1);
				assertAttributes(c, CellType.content, "HT9", "o:_:o:o");
			}
			{ // c2
				JSONObject c = getCell(r, 3);
				assertPosition(c, 0, 2);
				assertAttributes(c, CellType.content, "HT9", "i:o:o:o");
			}
			{ // c2/c3 vertical separator
				JSONObject c = getCell(r, 4);
				assertPosition(c, 0, 2);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:_");
			}
			{ // c2/c3 vertical separator
				JSONObject c = getCell(r, 5);
				assertPosition(c, 0, 3);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:_:_");
			}
			{ // c3
				JSONObject c = getCell(r, 6);
				assertPosition(c, 0, 3);
				assertAttributes(c, CellType.content, "HT12", "o:_:o:_");
			}
			{ // c4
				JSONObject c = getCell(r, 7);
				assertPosition(c, 0, 4);
				assertAttributes(c, CellType.content, "HT12", "i:o:o:_");
			}
			{ // c4/c5 vertical separator
				JSONObject c = getCell(r, 8);
				assertPosition(c, 0, 4);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:_");
			}
			{ // c4/c5 vertical separator
				JSONObject c = getCell(r, 9);
				assertPosition(c, 0, 5);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:_:_");
			}
			{ // c5
				JSONObject c = getCell(r, 10);
				assertPosition(c, 0, 5);
				assertAttributes(c, CellType.content, "HT15", "o:_:o:_");
			}
			{ // c6
				JSONObject c = getCell(r, 11);
				assertPosition(c, 0, 6);
				assertAttributes(c, CellType.content, "HT15", "i:o:o:_");
			}
			{ // c6 vertical separator
				JSONObject c = getCell(r, 12);
				assertPosition(c, 0, 6);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:o:_:_");
			}
		}

		{ // r1
			JSONObject r = rows.getJSONObject(2);
			assertContentRow(r);
			{ // c0
				JSONObject c = getCell(r, 0);
				assertPosition(c, 1, 0);
				assertAttributes(c, CellType.dummyContent, "HT3", "o:_:_:_");
			}
			{ // c0/c1 vertical separator
				JSONObject c = getCell(r, 1);
				assertPosition(c, 1, 1);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:_:_");
			}
			{ // c1
				JSONObject c = getCell(r, 2);
				assertPosition(c, 1, 1);
				assertAttributes(c, CellType.dummyContent, "HT3", "_:_:_:_");
			}
			{ // c2
				JSONObject c = getCell(r, 3);
				assertPosition(c, 1, 2);
				assertAttributes(c, CellType.dummyContent, "HT3", "_:_:_:_");
			}
			{ // c2/c3 vertical separator
				JSONObject c = getCell(r, 4);
				assertPosition(c, 1, 2);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:_");
			}
			{ // c2/c3 vertical separator
				JSONObject c = getCell(r, 5);
				assertPosition(c, 1, 3);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:_:_");
			}
			{ // c3
				JSONObject c = getCell(r, 6);
				assertPosition(c, 1, 3);
				assertAttributes(c, CellType.content, "HT12", "o:_:i:o");
			}
			{ // c4
				JSONObject c = getCell(r, 7);
				assertPosition(c, 1, 4);
				assertAttributes(c, CellType.content, "HT12", "i:o:i:o");
			}
			{ // c4/c5 vertical separator
				JSONObject c = getCell(r, 8);
				assertPosition(c, 1, 4);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:_");
			}
			{ // c4/c5 vertical separator
				JSONObject c = getCell(r, 9);
				assertPosition(c, 1, 5);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:_:_");
			}
			{ // c5
				JSONObject c = getCell(r, 10);
				assertPosition(c, 1, 5);
				assertAttributes(c, CellType.content, "HT15", "o:_:i:_");
			}
			{ // c6
				JSONObject c = getCell(r, 11);
				assertPosition(c, 1, 6);
				assertAttributes(c, CellType.content, "HT15", "i:o:i:_");
			}
			{ // c6 vertical separator
				JSONObject c = getCell(r, 12);
				assertPosition(c, 1, 6);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:o:_:_");
			}
		}

		{ // r2
			JSONObject r = rows.getJSONObject(3);
			assertContentRow(r);
			{ // c0
				JSONObject c = getCell(r, 0);
				assertPosition(c, 2, 0);
				assertAttributes(c, CellType.dummyContent, "HT3", "o:_:_:_");
			}
			{ // c0/c1 vertical separator
				JSONObject c = getCell(r, 1);
				assertPosition(c, 2, 1);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:_:_");
			}
			{ // c1
				JSONObject c = getCell(r, 2);
				assertPosition(c, 2, 1);
				assertAttributes(c, CellType.dummyContent, "HT3", "_:_:_:_");
			}
			{ // c2
				JSONObject c = getCell(r, 3);
				assertPosition(c, 2, 2);
				assertAttributes(c, CellType.dummyContent, "HT3", "_:_:_:_");
			}
			{ // c2/c3 vertical separator
				JSONObject c = getCell(r, 4);
				assertPosition(c, 2, 2);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:_");
			}
			{ // c2/c3 vertical separator
				JSONObject c = getCell(r, 5);
				assertPosition(c, 2, 3);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:_:_");
			}
			{ // c3
				JSONObject c = getCell(r, 6);
				assertPosition(c, 2, 3);
				assertAttributes(c, CellType.dummyContent, "HT3", "_:_:_:_");
			}
			{ // c4
				JSONObject c = getCell(r, 7);
				assertPosition(c, 2, 4);
				assertAttributes(c, CellType.dummyContent, "HT3", "_:_:_:_");
			}
			{ // c4/c5 vertical separator
				JSONObject c = getCell(r, 8);
				assertPosition(c, 2, 4);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:_");
			}
			{ // c4/c5 vertical separator
				JSONObject c = getCell(r, 9);
				assertPosition(c, 2, 5);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:_:_");
			}
			{ // c5
				JSONObject c = getCell(r, 10);
				assertPosition(c, 2, 5);
				assertAttributes(c, CellType.content, "HT15", "o:_:i:o");
			}
			{ // c6
				JSONObject c = getCell(r, 11);
				assertPosition(c, 2, 6);
				assertAttributes(c, CellType.content, "HT15", "i:o:i:o");
			}
			{ // c6 vertical separator
				JSONObject c = getCell(r, 12);
				assertPosition(c, 2, 6);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:o:_:_");
			}
		}

		{ // r2 bottom separator 1.
			JSONObject r = rows.getJSONObject(4);
			assertSeparatorRow(r);
			{ // c0
				JSONObject c = getCell(r, 0);
				assertPosition(c, 2, 0);
				assertAttributes(c, CellType.rowSpace, "HT3", "o:_:_:o");
			}
			{ // c0/c1 vertical separator
				JSONObject c = getCell(r, 1);
				assertPosition(c, 2, 1);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:_:o");
			}
			{ // c1
				JSONObject c = getCell(r, 2);
				assertPosition(c, 2, 1);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:_:o");
			}
			{ // c2
				JSONObject c = getCell(r, 3);
				assertPosition(c, 2, 2);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:_:o");
			}
			{ // c1/c2 vertical separator
				JSONObject c = getCell(r, 4);
				assertPosition(c, 2, 2);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:o");
			}
			{ // c1/c2 vertical separator
				JSONObject c = getCell(r, 5);
				assertPosition(c, 2, 3);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:_:o");
			}
			{ // c3
				JSONObject c = getCell(r, 6);
				assertPosition(c, 2, 3);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:_:o");
			}
			{ // c4
				JSONObject c = getCell(r, 7);
				assertPosition(c, 2, 4);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:_:o");
			}
			{ // c4/c5 vertical separator
				JSONObject c = getCell(r, 8);
				assertPosition(c, 2, 4);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:o");
			}
			{ // c4/c5 vertical separator
				JSONObject c = getCell(r, 9);
				assertPosition(c, 2, 5);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:_:o");
			}
			{ // c5
				JSONObject c = getCell(r, 10);
				assertPosition(c, 2, 5);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:_:o");
			}
			{ // c6
				JSONObject c = getCell(r, 11);
				assertPosition(c, 2, 6);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:_:o");
			}
			{ // c6 vertical separator
				JSONObject c = getCell(r, 12);
				assertPosition(c, 2, 6);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:o:_:o");
			}
		}
	}

	public void testGenerateUnitTest1() throws JSONException {
		@SuppressWarnings("unused")
		Worksheet ws = SampleDataFactory.createUnitTest1(vwsp.getWorkspace());

		vwsp.addAllWorksheets();
		UpdateContainer uc = new UpdateContainer();
		for (VWorksheet vw : vwsp.getVWorksheetList().getVWorksheets()) {
			uc.add(new WorksheetHierarchicalDataUpdate(vw));
		}

		VWorksheet vw = vwsp.getVWorksheetList().getVWorksheets().get(0);
//		System.err.println(Util.prettyPrintJson(vw.getViewTableHeadings()
//				.prettyPrintJson(new JSONStringer()).toString()));
//		System.err.println(Util.prettyPrintJson(vw.getVDTableData()
//				.prettyPrintJson(new JSONStringer(), /* verbose */false, vwsp)
//				.toString()));

		String ucJson = uc.generateJson(vwsp);
//		System.err.println(Util.prettyPrintJson(ucJson));

		JSONObject o = new JSONObject(ucJson);
		Util.writeJsonFile(o, "./testGenerateUnitTest1.json");
		
		JSONArray rows = o.getJSONArray("elements").getJSONObject(0)
				.getJSONArray(JsonKeys.rows.name());
		assertEquals(16, rows.length());
	}
	
	public void testGenerateUnitTest2() throws JSONException {
		@SuppressWarnings("unused")
		Worksheet ws = SampleDataFactory.createUnitTest2(vwsp.getWorkspace());

		vwsp.addAllWorksheets();
		UpdateContainer uc = new UpdateContainer();
		for (VWorksheet vw : vwsp.getVWorksheetList().getVWorksheets()) {
			uc.add(new WorksheetHierarchicalDataUpdate(vw));
		}

		@SuppressWarnings("unused")
		VWorksheet vw = vwsp.getVWorksheetList().getVWorksheets().get(0);
//		System.err.println(Util.prettyPrintJson(vw.getViewTableHeadings()
//				.prettyPrintJson(new JSONStringer()).toString()));
//		System.err.println(Util.prettyPrintJson(vw.getVDTableData()
//				.prettyPrintJson(new JSONStringer(), /* verbose */false, vwsp)
//				.toString()));

		String ucJson = uc.generateJson(vwsp);
//		System.err.println(Util.prettyPrintJson(ucJson));

		JSONObject o = new JSONObject(ucJson);
		Util.writeJsonFile(o, "./testGenerateUnitTest2.json");
		
		JSONArray rows = o.getJSONArray("elements").getJSONObject(0)
				.getJSONArray(JsonKeys.rows.name());
		assertEquals(6, rows.length());

		{ // r0 top separator 1.
			JSONObject r = rows.getJSONObject(0);
			assertSeparatorRow(r);
			{ // c0 vertical separator
				JSONObject c = getCell(r, 0);
				assertPosition(c, 0, 0);
				assertAttributes(c, CellType.columnSpace, "HT3", "o:_:o:_");
			}
			{ // c0
				JSONObject c = getCell(r, 1);
				assertPosition(c, 0, 0);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:o:_");
			}
			{ // c0/c1 vertical separator
				JSONObject c = getCell(r, 2);
				assertPosition(c, 0, 0);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:o:_");
			}
			{ // c0/c1 vertical separator
				JSONObject c = getCell(r, 3);
				assertPosition(c, 0, 1);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:o:_");
			}
			{ // c1
				JSONObject c = getCell(r, 4);
				assertPosition(c, 0, 1);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:o:_");
			}
			{ // c1/c2 vertical separator
				JSONObject c = getCell(r, 5);
				assertPosition(c, 0, 1);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:o:_");
			}
			{ // c1/c2 vertical separator
				JSONObject c = getCell(r, 6);
				assertPosition(c, 0, 2);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:o:_");
			}
			{ // c2
				JSONObject c = getCell(r, 7);
				assertPosition(c, 0, 2);
				assertAttributes(c, CellType.rowSpace, "HT3", "_:_:o:_");
			}
			{ // c2 vertical separator
				JSONObject c = getCell(r, 8);
				assertPosition(c, 0, 2);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:o:o:_");
			}
		}

		{ // r0
			JSONObject r = rows.getJSONObject(1);
			assertContentRow(r);
			{ // c0 vertical separator
				JSONObject c = getCell(r, 0);
				assertPosition(c, 0, 0);
				assertAttributes(c, CellType.columnSpace, "HT3", "o:_:_:_");
			}
			{ // c0
				JSONObject c = getCell(r, 1);
				assertPosition(c, 0, 0);
				assertAttributes(c, CellType.dummyContent, "HT8", "o:o:o:o");
			}
			{ // c0/c1 vertical separator
				JSONObject c = getCell(r, 2);
				assertPosition(c, 0, 0);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:_");
			}
			{ // c0/c1 vertical separator
				JSONObject c = getCell(r, 3);
				assertPosition(c, 0, 1);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:_:_");
			}
			{ // c1
				JSONObject c = getCell(r, 4);
				assertPosition(c, 0, 1);
				assertAttributes(c, CellType.content, "HT12", "o:o:o:o");
			}
			{ // c1/c2 vertical separator
				JSONObject c = getCell(r, 5);
				assertPosition(c, 0, 1);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:_:_:_");
			}
			{ // c1/c2 vertical separator
				JSONObject c = getCell(r, 6);
				assertPosition(c, 0, 2);
				assertAttributes(c, CellType.columnSpace, "HT3", "i:_:_:_");
			}
			{ // c2
				JSONObject c = getCell(r, 7);
				assertPosition(c, 0, 2);
				assertAttributes(c, CellType.dummyContent, "HT25", "o:o:o:o");
			}
			{ // c2 vertical separator
				JSONObject c = getCell(r, 8);
				assertPosition(c, 0, 2);
				assertAttributes(c, CellType.columnSpace, "HT3", "_:o:_:_");
			}
		}
	}
	
	public void testGenerateUnitTest4() throws JSONException {
		@SuppressWarnings("unused")
		Worksheet ws = SampleDataFactory.createUnitTest4(vwsp.getWorkspace());

		vwsp.addAllWorksheets();
		UpdateContainer uc = new UpdateContainer();
		for (VWorksheet vw : vwsp.getVWorksheetList().getVWorksheets()) {
			uc.add(new WorksheetHierarchicalDataUpdate(vw));
		}

		@SuppressWarnings("unused")
		VWorksheet vw = vwsp.getVWorksheetList().getVWorksheets().get(0);
//		System.err.println(vw.getWorksheet().getDataTable().prettyPrint(f));
//		System.err.println(Util.prettyPrintJson(vw.getViewTableHeadings()
//				.prettyPrintJson(new JSONStringer()).toString()));
//		System.err.println(Util.prettyPrintJson(vw.getVDTableData()
//				.prettyPrintJson(new JSONStringer(), /* verbose */false, vwsp)
//				.toString()));

		String ucJson = uc.generateJson(vwsp);
//		System.err.println(Util.prettyPrintJson(ucJson));

		JSONObject o = new JSONObject(ucJson);
		Util.writeJsonFile(o, "./testGenerateUnitTest4.json");
		
		JSONArray rows = o.getJSONArray("elements").getJSONObject(0)
				.getJSONArray(JsonKeys.rows.name());
		assertEquals(21, rows.length());
		
		{ // r0 top separator 1.
			JSONObject r = rows.getJSONObject(6);
			assertContentRow(r);
			{ // c0 vertical separator
				JSONObject c = getCell(r, 5);
				assertPosition(c, 1, 2);
				assertAttributes(c, CellType.dummyContent, "HT22", "o:o:o:o");
			}
		}
	}
	
	public void testSampleJsonWithEmptyNestedTable1() throws JSONException {
		@SuppressWarnings("unused")
		Worksheet ws = SampleDataFactory.createSampleJsonWithEmptyNestedTable1(vwsp.getWorkspace());

		vwsp.addAllWorksheets();
		UpdateContainer uc = new UpdateContainer();
		for (VWorksheet vw : vwsp.getVWorksheetList().getVWorksheets()) {
			uc.add(new WorksheetHierarchicalDataUpdate(vw));
		}

		@SuppressWarnings("unused")
		VWorksheet vw = vwsp.getVWorksheetList().getVWorksheets().get(0);
//		System.err.println(vw.getWorksheet().getDataTable().prettyPrint(f));
//		System.err.println(Util.prettyPrintJson(vw.getViewTableHeadings()
//				.prettyPrintJson(new JSONStringer()).toString()));
//		System.err.println(Util.prettyPrintJson(vw.getVDTableData()
//				.prettyPrintJson(new JSONStringer(), /* verbose */false, vwsp)
//				.toString()));

		String ucJson = uc.generateJson(vwsp);
//		System.err.println(Util.prettyPrintJson(ucJson));

		JSONObject o = new JSONObject(ucJson);
		Util.writeJsonFile(o, "./testSampleJsonWithEmptyNestedTable1.json");
		
		JSONArray rows = o.getJSONArray("elements").getJSONObject(0)
				.getJSONArray(JsonKeys.rows.name());
		assertEquals(6, rows.length());
		
		{ // r0 top separator 1.
			JSONObject r = rows.getJSONObject(4);
			assertContentRow(r);
			{ // c0 vertical separator
				JSONObject c = getCell(r, 0);
				assertPosition(c, 1, 0);
				assertAttributes(c, CellType.columnSpace, "HT3", "o:_:_:_");
			}
			{ // c0 empty content
				JSONObject c = getCell(r, 1);
				assertPosition(c, 1, 0);
				assertAttributes(c, CellType.dummyContent, "HT8", "o:_:o:o");
			}
		}
	}

}
