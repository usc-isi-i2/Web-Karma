package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.util.Util;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.Stroke.StrokeStyle;
import edu.isi.karma.view.tabledata.VDCell.Position;
import edu.isi.karma.webserver.SampleDataFactory;
import junit.framework.TestCase;

import edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.getStrokePositionKey;

import static edu.isi.karma.view.tabledata.VDCell.Position.left;
import static edu.isi.karma.view.tabledata.VDCell.Position.top;
import static edu.isi.karma.view.tabledata.VDCell.Position.right;
import static edu.isi.karma.view.tabledata.VDCell.Position.bottom;

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

	private void assertOuter(JSONObject c, Position position)
			throws JSONException {
		assertEquals(StrokeStyle.outer.name(),
				c.getString(getStrokePositionKey(position)));
	}

	private void assertInner(JSONObject c, Position position)
			throws JSONException {
		assertEquals(StrokeStyle.inner.name(),
				c.getString(getStrokePositionKey(position)));
	}

	private void assertNone(JSONObject c, Position position)
			throws JSONException {
		assertEquals(StrokeStyle.none.name(),
				c.getString(getStrokePositionKey(position)));
	}

	private void assertHTable(JSONObject c, String hTableId)
			throws JSONException {
		assertEquals(hTableId, c.getString(JsonKeys.hTableId.name()));
	}

	private void assertSeparatorRow(JSONObject r) throws JSONException {
		assertEquals(JsonKeys.separatorRow.name(),
				r.getString(JsonKeys.rowType.name()));
	}

	private void assertContentRow(JSONObject r) throws JSONException {
		assertEquals(JsonKeys.contentRow.name(),
				r.getString(JsonKeys.rowType.name()));
	}

	private void assertPosition(JSONObject c, int row, int col)
			throws JSONException {
		assertEquals(row, c.getInt("_row"));
		assertEquals(col, c.getInt("_col"));
	}

	private JSONObject getCell(JSONObject rObj, int index) throws JSONException {
		JSONArray r = rObj.getJSONArray(JsonKeys.rowCells.name());
		JSONObject c = r.getJSONObject(index);
		return c;
	}

	public void testGenerateJson() throws JSONException {
		@SuppressWarnings("unused")
		Worksheet ws = SampleDataFactory//
				// .createSampleJsonWithNestedTable1(vwsp.getWorkspace())//
				.createSampleJsonWithNestedTable2(true/* true: 2 rows */,
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

		StringWriter sw1 = new StringWriter();
		PrintWriter pw1 = new PrintWriter(sw1);
		uc.generateJson("", pw1, vwsp);
		System.err.println(Util.prettyPrintJson(sw1.toString()));

		JSONObject o = new JSONObject(sw1.toString());
		JSONArray rows = o.getJSONArray("elements").getJSONObject(0)
				.getJSONArray(JsonKeys.rows.name());
		assertEquals(17, rows.length());

		{ // r0 top separator 1.
			JSONObject r = rows.getJSONObject(0);
			assertSeparatorRow(r);
			{ // c0
				JSONObject c = getCell(r, 0);
				assertPosition(c, 0, 0);
				assertHTable(c, "HT3");
				assertOuter(c, top);
				assertNone(c, bottom);
			}
			{ // c1
				JSONObject c = getCell(r, 1);
				assertPosition(c, 0, 1);
				assertHTable(c, "HT3");
				assertOuter(c, top);
				assertNone(c, bottom);
			}
			{ // c2
				JSONObject c = getCell(r, 2);
				assertPosition(c, 0, 2);
				assertHTable(c, "HT3");
				assertOuter(c, top);
				assertNone(c, bottom);
			}
			{ // c3
				JSONObject c = getCell(r, 3);
				assertPosition(c, 0, 3);
				assertHTable(c, "HT3");
				assertOuter(c, top);
				assertNone(c, bottom);
			}
		}

		{ // r0 top separator 2.
			JSONObject r = rows.getJSONObject(1);
			assertSeparatorRow(r);
			{ // c0
				JSONObject c = getCell(r, 0);
				assertPosition(c, 0, 0);
				assertHTable(c, "HT8");
				assertOuter(c, top);
				assertNone(c, bottom);
			}
			{ // c1
				JSONObject c = getCell(r, 1);
				assertPosition(c, 0, 1);
				assertHTable(c, "HT8");
				assertOuter(c, top);
				assertNone(c, bottom);
			}
			{ // c2
				JSONObject c = getCell(r, 2);
				assertPosition(c, 0, 2);
				assertHTable(c, "HT8");
				assertOuter(c, top);
				assertNone(c, bottom);
			}
			{ // c3
				JSONObject c = getCell(r, 3);
				assertPosition(c, 0, 3);
				assertHTable(c, "HT3");
				assertNone(c, top);
				assertNone(c, bottom);
			}
		}

		{ // r0 content.
			JSONObject r = rows.getJSONObject(2);
			assertContentRow(r);
			{ // c0
				JSONObject c = getCell(r, 0);
				assertPosition(c, 0, 0);
				assertHTable(c, "HT13");
				assertOuter(c, top);
				assertNone(c, bottom);
			}
			{ // c1
				JSONObject c = getCell(r, 1);
				assertPosition(c, 0, 1);
				assertHTable(c, "HT13");
				assertOuter(c, top);
				assertNone(c, bottom);
			}
			{ // c2
				JSONObject c = getCell(r, 2);
				assertPosition(c, 0, 2);
				assertHTable(c, "HT8");
				assertNone(c, top);
				assertNone(c, bottom);
			}
			{ // c3
				JSONObject c = getCell(r, 3);
				assertPosition(c, 0, 3);
				assertHTable(c, "HT3");
				assertNone(c, top);
				assertNone(c, bottom);
			}
		}

		{ // r1 content.
			JSONObject r = rows.getJSONObject(3);
			assertContentRow(r);
			{ // c0
				JSONObject c = getCell(r, 0);
				assertPosition(c, 1, 0);
				assertHTable(c, "HT13");
				assertInner(c, top);
				assertOuter(c, bottom);
			}
			{ // c1
				JSONObject c = getCell(r, 1);
				assertPosition(c, 1, 1);
				assertHTable(c, "HT13");
				assertInner(c, top);
				assertOuter(c, bottom);
			}
			{ // c2
				JSONObject c = getCell(r, 2);
				assertPosition(c, 1, 2);
				assertHTable(c, "HT8");
				assertNone(c, top);
				assertNone(c, bottom);
			}
			{ // c3
				JSONObject c = getCell(r, 3);
				assertPosition(c, 1, 3);
				assertHTable(c, "HT3");
				assertNone(c, top);
				assertNone(c, bottom);
			}
		}
		
		{ // r1 separator bottom.
			JSONObject r = rows.getJSONObject(4);
			assertSeparatorRow(r);
			{ // c0
				JSONObject c = getCell(r, 0);
				assertPosition(c, 1, 0);
				assertHTable(c, "HT8");
				assertNone(c, top);
				assertNone(c, bottom);
			}
			{ // c1
				JSONObject c = getCell(r, 1);
				assertPosition(c, 1, 1);
				assertHTable(c, "HT8");
				assertNone(c, top);
				assertNone(c, bottom);
			}
			{ // c2
				JSONObject c = getCell(r, 2);
				assertPosition(c, 1, 2);
				assertHTable(c, "HT8");
				assertNone(c, top);
				assertNone(c, bottom);
			}
			{ // c3
				JSONObject c = getCell(r, 3);
				assertPosition(c, 1, 3);
				assertHTable(c, "HT3");
				assertNone(c, top);
				assertNone(c, bottom);
			}
		}
		
		{ // r2 separator top.
			JSONObject r = rows.getJSONObject(5);
			assertSeparatorRow(r);
			{ // c0
				JSONObject c = getCell(r, 0);
				assertPosition(c, 2, 0);
				assertHTable(c, "HT8");
				assertInner(c, top);
				assertNone(c, bottom);
			}
			{ // c1
				JSONObject c = getCell(r, 1);
				assertPosition(c, 2, 1);
				assertHTable(c, "HT8");
				assertInner(c, top);
				assertNone(c, bottom);
			}
			{ // c2
				JSONObject c = getCell(r, 2);
				assertPosition(c, 2, 2);
				assertHTable(c, "HT8");
				assertInner(c, top);
				assertNone(c, bottom);
			}
			{ // c3
				JSONObject c = getCell(r, 3);
				assertPosition(c, 2, 3);
				assertHTable(c, "HT3");
				assertNone(c, top);
				assertNone(c, bottom);
			}
		}
		
		{ // r2 content.
			JSONObject r = rows.getJSONObject(6);
			assertContentRow(r);
			{ // c0
				JSONObject c = getCell(r, 0);
				assertPosition(c, 2, 0);
				assertHTable(c, "HT13");
				assertOuter(c, top);
				assertOuter(c, bottom);
			}
			{ // c1
				JSONObject c = getCell(r, 1);
				assertPosition(c, 2, 1);
				assertHTable(c, "HT13");
				assertOuter(c, top);
				assertOuter(c, bottom);
			}
			{ // c2
				JSONObject c = getCell(r, 2);
				assertPosition(c, 2, 2);
				assertHTable(c, "HT8");
				assertNone(c, top);
				assertNone(c, bottom);
			}
			{ // c3
				JSONObject c = getCell(r, 3);
				assertPosition(c, 2, 3);
				assertHTable(c, "HT3");
				assertNone(c, top);
				assertNone(c, bottom);
			}
		}
		
		{ // r2 separator bottom.
			JSONObject r = rows.getJSONObject(7);
			assertSeparatorRow(r);
			{ // c0
				JSONObject c = getCell(r, 0);
				assertPosition(c, 2, 0);
				assertHTable(c, "HT8");
				assertNone(c, top);
				assertOuter(c, bottom);
			}
			{ // c1
				JSONObject c = getCell(r, 1);
				assertPosition(c, 2, 1);
				assertHTable(c, "HT8");
				assertNone(c, top);
				assertOuter(c, bottom);
			}
			{ // c2
				JSONObject c = getCell(r, 2);
				assertPosition(c, 2, 2);
				assertHTable(c, "HT8");
				assertNone(c, top);
				assertOuter(c, bottom);
			}
			{ // c3
				JSONObject c = getCell(r, 3);
				assertPosition(c, 2, 3);
				assertHTable(c, "HT3");
				assertNone(c, top);
				assertNone(c, bottom);
			}
		}

		{ // r2 separator bottom.
			JSONObject r = rows.getJSONObject(8);
			assertSeparatorRow(r);
			{ // c0
				JSONObject c = getCell(r, 0);
				assertPosition(c, 2, 0);
				assertHTable(c, "HT3");
				assertNone(c, top);
				assertNone(c, bottom);
			}
			{ // c1
				JSONObject c = getCell(r, 1);
				assertPosition(c, 2, 1);
				assertHTable(c, "HT3");
				assertNone(c, top);
				assertNone(c, bottom);
			}
			{ // c2
				JSONObject c = getCell(r, 2);
				assertPosition(c, 2, 2);
				assertHTable(c, "HT3");
				assertNone(c, top);
				assertNone(c, bottom);
			}
			{ // c3
				JSONObject c = getCell(r, 3);
				assertPosition(c, 2, 3);
				assertHTable(c, "HT3");
				assertNone(c, top);
				assertNone(c, bottom);
			}
		}
		
		{ // r3 separator top.
			JSONObject r = rows.getJSONObject(9);
			assertSeparatorRow(r);
			{ // c0
				JSONObject c = getCell(r, 0);
				assertPosition(c, 3, 0);
				assertHTable(c, "HT3");
				assertInner(c, top);
				assertNone(c, bottom);
			}
			{ // c1
				JSONObject c = getCell(r, 1);
				assertPosition(c, 3, 1);
				assertHTable(c, "HT3");
				assertInner(c, top);
				assertNone(c, bottom);
			}
			{ // c2
				JSONObject c = getCell(r, 2);
				assertPosition(c, 3, 2);
				assertHTable(c, "HT3");
				assertInner(c, top);
				assertNone(c, bottom);
			}
			{ // c3
				JSONObject c = getCell(r, 3);
				assertPosition(c, 3, 3);
				assertHTable(c, "HT3");
				assertInner(c, top);
				assertNone(c, bottom);
			}
		}

		
		//
	}

}
