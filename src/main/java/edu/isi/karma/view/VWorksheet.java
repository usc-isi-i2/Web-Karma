package edu.isi.karma.view;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONWriter;

import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetDataUpdate;
import edu.isi.karma.controller.update.WorksheetHeadersUpdate;
import edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate;
import edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.TablePager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.view.ViewPreferences.ViewPreference;
import edu.isi.karma.view.tabledata.VDTableData;
import edu.isi.karma.view.tableheadings.VColumnHeader;
import edu.isi.karma.view.tableheadings.VTHeaderForest;
import edu.isi.karma.view.tableheadings.VTableHeadings;

public class VWorksheet extends ViewEntity {

	private final Worksheet worksheet;

	/**
	 * Marks whether the data in the view is consistent with the data in the
	 * memory representation. When false, it means that the view should be
	 * refreshed, and an indication should be shown to the user to indicate that
	 * an explicit refresh is needed.
	 */
	private boolean upToDate = true;

	/**
	 * When true, the view should show the worksheet collapsed so that the
	 * headers are visible but the data is hidden.
	 */
	private boolean collapsed = false;

	/**
	 * The column headers shown in this view. The hidden columns do not appear
	 * in this list. A roundtrip to the server is required to make hidden
	 * columns appear.
	 */
	private final List<VColumnHeader> columnHeaders = new LinkedList<VColumnHeader>();
	private final List<HNodePath> columns;

	/**
	 * The maximum number of rows to show in the nested tables.
	 */
	private int maxRowsToShowInNestedTables;

	/**
	 * Here we store the data.
	 */
	private final VTable viewDataTable; /* obsolete */
	private final VDTableData vdTableData;

	/**
	 * Here we store the table headings.
	 */
	private final VTableHeadings viewTableHeadings;

	private final VTHeaderForest vHeaderForest;
	/**
	 * We create a TablePager for the top level table and every nested table we
	 * see. It records how the table is scrolled.
	 */
	private final Map<String, TablePager> tableId2TablePager = new HashMap<String, TablePager>();

	VWorksheet(String id, Worksheet worksheet, List<HNodePath> columns,
			VWorkspace vWorkspace) {
		super(id);
		this.worksheet = worksheet;
		this.maxRowsToShowInNestedTables = vWorkspace.getPreferences()
				.getIntViewPreferenceValue(
						ViewPreference.maxRowsToShowInNestedTables);
		
		//TODO: delete VTable for the new table display.
		this.viewDataTable = new VTable(worksheet.getDataTable().getId());

		this.columns = columns;

		this.viewTableHeadings = new VTableHeadings(columns, worksheet
				.getHeaders().getId());
		
		vHeaderForest = new VTHeaderForest();
		vHeaderForest.constructFromHNodePaths(columns);
		
		for (HNodePath p : columns) {
			addColumnHeader(vWorkspace.getViewFactory().createVColumnHeader(p,
					vWorkspace.getPreferences()));
		}

		// Force creation of the TablePager for the top table.
		getTablePager(
				worksheet.getDataTable(),
				vWorkspace.getPreferences().getIntViewPreferenceValue(
						ViewPreference.defaultRowsToShowInTopTables));

		udateDataTable(vWorkspace.getViewFactory());
		
		this.vdTableData = new VDTableData(viewTableHeadings, this, vWorkspace);

	}

	private TablePager getTablePager(Table table, int size) {
		TablePager tp = tableId2TablePager.get(table.getId());
		if (tp != null) {
			return tp;
		} else {
			tp = new TablePager(table, 0, size);
			tableId2TablePager.put(table.getId(), tp);
			return tp;
		}
	}

	public void udateDataTable(ViewFactory viewFactory) {
		//TODO: do the same thing with the vdTableData.
		viewDataTable.clear();
		viewDataTable.addRows(getTopTablePager().getRows(), columns, this,
				viewFactory);
	}

	public TablePager getTopTablePager() {
		return tableId2TablePager.get(worksheet.getDataTable().getId());
	}

	public TablePager getNestedTablePager(Table table) {
		return getTablePager(table, maxRowsToShowInNestedTables);
	}

	public TablePager getTablePager(String tableId) {
		return tableId2TablePager.get(tableId);
	}

	public String getWorksheetId() {
		return worksheet.getId();
	}

	public Worksheet getWorksheet() {
		return worksheet;
	}

	public boolean isUpToDate() {
		return upToDate;
	}

	public void setUpToDate(boolean upToDate) {
		this.upToDate = upToDate;
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}

	public int getMaxRowsToShowInNestedTables() {
		return maxRowsToShowInNestedTables;
	}

	public void setMaxRowsToShowInNestedTables(int maxRowsToShowInNestedTables) {
		this.maxRowsToShowInNestedTables = maxRowsToShowInNestedTables;
	}

	public List<VColumnHeader> getColumnHeaders() {
		return columnHeaders;
	}

	public List<HNodePath> getColumns() {
		return columns;
	}

	public VTable getDataTable() {
		return viewDataTable;
	}
	
	public VDTableData getVDTableData() {
		return vdTableData;
	}

	public VTableHeadings getViewTableHeadings() {
		return viewTableHeadings;
	}

	public VTHeaderForest getvHeaderForest() {
		return vHeaderForest;
	}

	void addColumnHeader(VColumnHeader vch) {
		columnHeaders.add(vch);
	}

	public void update(UpdateContainer c) {
		c.add(new WorksheetHierarchicalHeadersUpdate(this));
		c.add(new WorksheetHierarchicalDataUpdate(this));
	}

	public void updateHeaders(UpdateContainer c) {
		c.add(new WorksheetHierarchicalHeadersUpdate(this));
	}

	public void updateContent(UpdateContainer c) {
		c.add(new WorksheetHierarchicalDataUpdate(this));
	}
	
	public void generateWorksheetHeadersJson(String prefix, PrintWriter pw,
			ViewFactory factory) {
		pw.println(prefix + "{");
		String newPref = prefix + "  ";

		pw.println(newPref
				+ JSONUtil.json(AbstractUpdate.GenericJsonKeys.updateType,
						WorksheetHeadersUpdate.class.getSimpleName()));
		pw.println(newPref
				+ JSONUtil.json(WorksheetDataUpdate.JsonKeys.worksheetId, getId()));

		pw.println(newPref
				+ JSONUtil.jsonStartList(WorksheetHeadersUpdate.JsonKeys.columns));
		Iterator<VColumnHeader> itCols = columnHeaders.iterator();
		while (itCols.hasNext()) {
			VColumnHeader ch = itCols.next();
			ch.generateJson(newPref + "  ", pw, factory, itCols.hasNext());
		}
		pw.println(newPref + "] ");

		pw.println(prefix + "}");
	}

	public void generateWorksheetDataJson(String prefix, PrintWriter pw,
			ViewFactory factory) {
		pw.println(prefix + "{");
		String newPref = prefix + "  ";

		pw.println(newPref
				+ JSONUtil.json(AbstractUpdate.GenericJsonKeys.updateType,
						WorksheetDataUpdate.class.getSimpleName()));
		pw.println(newPref
				+ JSONUtil.json(WorksheetDataUpdate.JsonKeys.worksheetId, getId()));

		pw.println(newPref
				+ JSONUtil.jsonStartList(WorksheetDataUpdate.JsonKeys.rows));

		viewDataTable.generateJson(newPref, pw, this, factory);

		pw.println(newPref + "],");
		pw.println(newPref
				+ JSONUtil.jsonStartObject(WorksheetDataUpdate.JsonKeys.pager));
		getTopTablePager().generateJson(newPref + "  ", pw);
		pw.println(prefix + "}");
	}

	public void generateWorksheetListJson(String prefix, PrintWriter pw,
			ViewFactory factory) {
		pw.println(prefix + "{");
		String newPref = prefix + "  ";

		pw.println(newPref
				+ JSONUtil.json(WorksheetListUpdate.JsonKeys.worksheetId, getId()));
		pw.println(newPref
				+ JSONUtil.json(WorksheetListUpdate.JsonKeys.isUpToDate, upToDate));
		pw.println(newPref
				+ JSONUtil.json(WorksheetListUpdate.JsonKeys.isCollapsed, collapsed));

		pw.println(newPref
				+ JSONUtil.jsonLast(WorksheetListUpdate.JsonKeys.title,
						worksheet.getTitle()));

		pw.println(prefix + "}");
	}

	public void generateWorksheetHierarchicalHeadersJson(Writer w,
			VWorkspace vWorkspace) {
		viewTableHeadings.generateJson(new JSONWriter(w), this, vWorkspace);
	}
	
	public void generateWorksheetHierarchicalDataJson(Writer w,
			VWorkspace vWorkspace) {
		vdTableData.generateJson(new JSONWriter(w), this, vWorkspace);
	}

}
