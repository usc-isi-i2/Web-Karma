/**
 * 
 */
package edu.isi.karma.view.tableheadings;

import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.cellType;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.colSpan;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.columnNameFull;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.columnNameShort;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.fillId;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.heading;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.headingPadding;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.leftBorder;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.rightBorder;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.topBorder;
import static edu.isi.karma.view.Stroke.StrokeStyle.inner;
import static edu.isi.karma.view.Stroke.StrokeStyle.none;
import static edu.isi.karma.view.Stroke.StrokeStyle.outer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.TablePager;
import edu.isi.karma.util.Util;
import edu.isi.karma.view.Border;
import edu.isi.karma.view.Margin;
import edu.isi.karma.view.Stroke;
import edu.isi.karma.view.Stroke.StrokeStyle;
import edu.isi.karma.view.VTableCssTags;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.tabledata.VDRow;
import edu.isi.karma.view.tabledata.VDTreeNode;

/**
 * @author szekely
 * 
 */
public class VHTreeNode {

	/**
	 * The hNode for this node, it can have nested subtables.
	 */
	private final HNode hNode;

	/**
	 * If the hNode has a nested table, the id of this nested table. Otherwise a
	 * special string "leaf" to mark leaf nodes.
	 */
	private final String hTableId;

	/**
	 * Position of a node within it's parent.
	 */
	private boolean isFirst, isLast;

	/**
	 * Number of leaf children.
	 */
	private int width = 0;

	/**
	 * Number of margins defined by this node and all children below.
	 */
	private int numSubtreeMargins = 0;

	/**
	 * Depth of the node in the tree, root has depth 0;
	 */
	private int depth = 0;

	/**
	 * Left and right strokes defined by the position of this node within the
	 * parent. It can be the stroke for a nested table.
	 */
	private Stroke leftStroke, rightStroke;

	/**
	 * The strokes that will be drawn before the margins are drawn.
	 */
	private Stroke leftInnerStroke, rightInnerStroke;

	/**
	 * Strokes are propagated down the tree as they must be drawn around nested
	 * tables.
	 */
	private List<Stroke> leftSubtreeStrokes = new LinkedList<Stroke>(),
			rightSubtreeStrokes = new LinkedList<Stroke>();

	/**
	 * Margins are the spaces around tables.
	 */
	private Margin leftMargin, rightMargin;

	/**
	 * Bottom up accumulation of margins.
	 */
	private List<Margin> leftSubtreeMargins = new LinkedList<Margin>(),
			rightSubtreeMargins = new LinkedList<Margin>();

	/**
	 * Top down accumulation of subtreeMargins.
	 */
	private List<Margin> leftFullMargins = new LinkedList<Margin>(),
			rightFullMargins = new LinkedList<Margin>();

	/**
	 * Borders listed in the order they should appear in the html table, ie in
	 * painting order from left to right.
	 */
	private List<Border> leftBorders, rightBorders;

	/**
	 * The children are empty if the node is a leaf node.
	 */
	private final ArrayList<VHTreeNode> children = new ArrayList<VHTreeNode>();

	VHTreeNode(HNode hNode, String hTableId) {
		this.hNode = hNode;
		this.hTableId = hTableId;
	}

	VHTreeNode(String hTableId) {
		this(null, hTableId);
	}

	HNode getHNode() {
		return hNode;
	}

	boolean isLeaf() {
		return children.isEmpty();
	}

	boolean hasChildren() {
		return !children.isEmpty();
	}

	boolean isRoot() {
		return hNode == null;
	}

	ArrayList<VHTreeNode> getChildren() {
		return children;
	}

	/**
	 * Find a VHTreeNode for a given HNode among the children.
	 * 
	 * @param aHNode
	 * @return the VHTreeNode. It will be created if needed.
	 */
	private VHTreeNode getVHNode(HNode aHNode) {
		// If we find it, then return it.
		for (VHTreeNode child : children) {
			if (child.hNode.getId().equals(aHNode.getId())) {
				return child;
			}
		}
		// Couldn't find it, so create a new one.
		VHTreeNode newVHNode = new VHTreeNode(aHNode, aHNode.getNestedTable()
				.getId());
		children.add(newVHNode);
		return newVHNode;
	}

	boolean isFirst() {
		return isFirst;
	}

	boolean isLast() {
		return isLast;
	}

	boolean isMiddle() {
		return !isFirst && !isLast;
	}

	boolean isFirstAndLast() {
		return isFirst() && isLast();
	}

	/**
	 * Add a path of HNodes to the tree, creating nodes as needed.
	 * 
	 * @param path
	 */
	void addColumn(HNodePath path) {
		HNode hNode = path.getFirst();
		HNodePath rest = path.getRest();

		// hNode is a leaf.
		if (rest.isEmpty()) {
			children.add(new VHTreeNode(hNode, "leaf"));
		}
		// hNode is an internal node.
		else {
			VHTreeNode frontierVHNode = getVHNode(hNode);
			frontierVHNode.addColumn(rest);
		}
	}

	void addColumns(List<HNodePath> paths) {
		for (HNodePath p : paths) {
			addColumn(p);
		}
	}

	/**
	 * @return the number of children at the leaves of the tree.
	 */
	int assignWidths() {
		if (isLeaf()) {
			width = 1;
		} else {
			int result = 0;
			for (VHTreeNode vHNode : children) {
				result += vHNode.assignWidths();
			}
			width = result;
		}
		return width;
	}

	void assignDepths(int depth) {
		this.depth = depth;
		for (VHTreeNode n : children) {
			n.assignDepths(depth + 1);
		}
	}

	/**
	 * Convenient to explicitly store the positions of each node among it's
	 * siblings given the pain to deal with this using while or for loops.
	 */
	void assignPositions() {
		if (isRoot()) {
			isFirst = true;
			isLast = true;
		}
		boolean isFirstChild = true;
		Iterator<VHTreeNode> it = children.iterator();
		while (it.hasNext()) {
			VHTreeNode child = it.next();
			child.isFirst = isFirstChild;
			isFirstChild = false;
			child.isLast = !it.hasNext();
			child.assignPositions();
		}
	}

	/**
	 * Record the immediate strokes around each node.
	 */
	void assignLeftRightStrokes() {
		if (isRoot()) {
			leftStroke = Stroke.getRootStroke();
			rightStroke = Stroke.getRootStroke();
			leftMargin = Margin.getRootMargin();
			rightMargin = Margin.getRootMargin();
		}
		if (hasChildren()) {
			for (VHTreeNode child : children) {
				if (child.isFirst()) {
					child.leftStroke = new Stroke(outer, hTableId, depth);
				} else {
					child.leftStroke = new Stroke(inner, hTableId, depth);
				}

				if (child.isLast()) {
					child.rightStroke = new Stroke(outer, hTableId, depth);
				} else {
					child.rightStroke = new Stroke(none, hTableId, depth);
				}

				child.assignLeftRightStrokes();
			}
		}
	}

	/**
	 * Assign the margins that go around individual nodes
	 */
	void assignLeftRightMargins() {
		if (isRoot()) {
			leftMargin = Margin.getRootMargin();
			rightMargin = Margin.getRootMargin();
		}
		if (hasChildren()) {
			for (VHTreeNode child : children) {
				if (child.isLeaf()) {
					child.leftMargin = Margin.getleafMargin();
					child.rightMargin = Margin.getleafMargin();
				} else {
					child.leftMargin = new Margin(hTableId, depth);
					child.rightMargin = new Margin(hTableId, depth);
				}

				child.assignLeftRightMargins();
			}
		}
	}

	private void addMargin(Collection<Margin> container, Margin margin) {
		if (!margin.isRootMargin(margin)) {
			container.add(margin);
		}
	}

	private void addAllMargins(Collection<Margin> container,
			Collection<Margin> listToAdd) {
		for (Margin m : listToAdd) {
			addMargin(container, m);
		}
	}

	void bottomUpPropagation() {
		if (hasChildren()) {
			int numChildrenMargins = 0;
			for (VHTreeNode child : children) {
				child.bottomUpPropagation();

				if (child.isFirst()) {
					addMargin(leftSubtreeMargins, leftMargin);
					addAllMargins(leftSubtreeMargins, child.leftSubtreeMargins);
				} else {

				}

				if (child.isLast()) {
					addMargin(rightSubtreeMargins, rightMargin);
					addAllMargins(rightSubtreeMargins,
							child.rightSubtreeMargins);
				} else {

				}

				numChildrenMargins += child.numSubtreeMargins;
			}

			numSubtreeMargins = numChildrenMargins + 2;
		}

		else {
			// leftSubtreeMargins = rightSubtreeMargins = empty;

			numSubtreeMargins = 0;
		}
	}

	void topDownPropagation() {
		if (isRoot()) {
			// leftSubtreeStrokes, rightSubtreeStrokes remain unmodified.

			leftFullMargins.addAll(leftSubtreeMargins);
			rightFullMargins.addAll(rightSubtreeMargins);
		}
		if (hasChildren()) {
			for (VHTreeNode child : children) {
				if (child.isFirst()) {
					child.leftSubtreeStrokes.add(child.leftStroke);
					child.leftSubtreeStrokes.addAll(leftSubtreeStrokes);

					child.leftFullMargins.addAll(leftFullMargins);
				} else {
					child.leftSubtreeStrokes.add(child.leftStroke);

					child.leftFullMargins.addAll(child.leftSubtreeMargins);
				}

				if (child.isLast()) {
					child.rightSubtreeStrokes.add(child.rightStroke);
					child.rightSubtreeStrokes.addAll(rightSubtreeStrokes);

					child.rightFullMargins.addAll(rightFullMargins);
				} else {
					child.rightSubtreeStrokes.add(child.rightStroke);

					child.rightFullMargins.addAll(child.rightSubtreeMargins);
				}

				child.topDownPropagation();
			}
		}
	}

	void assignInnerStrokesAndBorders() {
		if (isRoot()) {
			leftInnerStroke = new Stroke(outer, hTableId, 0);
			rightInnerStroke = new Stroke(outer, hTableId, 0);

			leftBorders = Border.getRootBorderList();
			rightBorders = Border.getRootBorderList();
		} else {
			if (hasChildren()) {
				leftInnerStroke = new Stroke(none, hNode.getHTableId(), depth);
				rightInnerStroke = new Stroke(none, hNode.getHTableId(), depth);
			} else {
				leftInnerStroke = leftStroke;
				rightInnerStroke = rightStroke;
			}

			leftBorders = Border.constructBorderList(leftFullMargins,
					leftSubtreeStrokes, depth, true);
			rightBorders = Border.constructBorderList(rightFullMargins,
					rightSubtreeStrokes, depth, false);
		}

		for (VHTreeNode n : children) {
			n.assignInnerStrokesAndBorders();
		}
	}

	void assignTopBorderStrokes() {
		for (Border b : leftBorders) {
			boolean sameColor = b.getMargin().getHTableId()
					.equals(hNode.getHTableId());
			b.setHasTopStroke(sameColor);
		}
		for (Border b : rightBorders) {
			boolean sameColor = b.getMargin().getHTableId()
					.equals(hNode.getHTableId());
			b.setHasTopStroke(sameColor);
		}

		for (VHTreeNode n : children) {
			n.assignTopBorderStrokes();
		}
	}

	void computeDerivedInformation() {
		if (isRoot()) {
			assignPositions();
			assignWidths();
			assignDepths(0);
			assignLeftRightStrokes();
			assignLeftRightMargins();
			bottomUpPropagation();
			topDownPropagation();
			assignInnerStrokesAndBorders();
			assignTopBorderStrokes();
		} else {
			throw new Error("Should be called on the tree root.");
		}
	}

	void generateJson(JSONWriter jw, int levelDepth, VWorksheet vWorksheet,
			VWorkspace vWorkspace) throws JSONException {
		for (Border b : leftBorders) {
			b.generateJson(jw, true, vWorksheet, vWorkspace);
		}

		if (levelDepth > depth) {
			generateHeadingPaddingJson(jw, vWorksheet, vWorkspace);
		} else {
			generateHeadingJson(jw, vWorksheet, vWorkspace);
		}

		for (Border b : rightBorders) {
			b.generateJson(jw, false, vWorksheet, vWorkspace);
		}
	}

	private String shorten(String string, int maxLength) {
		String result = string;
		if (string.length() > maxLength) {
			result = Util.truncateForHeader(string, maxLength);
		}
		return result;
	}

	private void generateHeadingJson(JSONWriter jw, VWorksheet vWorksheet,
			VWorkspace vWorkspace) throws JSONException {
		VTableCssTags css = vWorkspace.getViewFactory().getTableCssTags();

		String topBorderCss = isRoot() ? css.getCssTag("root") : css
				.getCssTag(hNode.getHTableId());

		jw.object()
				//
				.key(cellType.name())
				.value(heading.name())
				//
				.key(columnNameFull.name())
				.value(hNode.getColumnName())
				//
				.key(columnNameShort.name())
				.value(shorten(hNode.getColumnName(), vWorkspace
						.getPreferences().getMaxCharactersInHeader()))
				//
				.key(topBorder.name())
				.value(Border.encodeBorder(StrokeStyle.outer, topBorderCss))//
		;
		generateHeadingCommonField(jw, vWorksheet, vWorkspace);
		jw.endObject();

	}

	private void generateHeadingPaddingJson(JSONWriter jw,
			VWorksheet vWorksheet, VWorkspace vWorkspace) throws JSONException {
		VTableCssTags css = vWorkspace.getViewFactory().getTableCssTags();

		jw.object()
				//
				.key(cellType.name()).value(headingPadding.name())
				//
				.key(topBorder.name())
				.value(Border.encodeBorder(StrokeStyle.none,
						css.getCssTag(hTableId)))
		//
		;
		generateHeadingCommonField(jw, vWorksheet, vWorkspace);
		jw.endObject();
	}

	private void generateHeadingCommonField(JSONWriter jw,
			VWorksheet vWorksheet, VWorkspace vWorkspace) throws JSONException {
		VTableCssTags css = vWorkspace.getViewFactory().getTableCssTags();

		String fillCssTag = isRoot() ? css.getCssTag("root") : css
				.getCssTag(hNode.getHTableId());

		int span = 1;
		if (hasChildren()) {
			span = numSubtreeMargins + width - leftSubtreeMargins.size()
					- rightSubtreeMargins.size();
		}

		jw.key(fillId.name())
				.value(fillCssTag)
				//
				.key(colSpan.name())
				.value(span)
				//
				.key(leftBorder.name())
				.value(Border.encodeBorder(leftInnerStroke.getStyle(),
						css.getCssTag(leftInnerStroke.gethTableId())))
				//
				.key(rightBorder.name())
				.value(Border.encodeBorder(rightInnerStroke.getStyle(),
						css.getCssTag(rightInnerStroke.gethTableId())))//
		;
	}

	/**
	 * For each row in the TablePager, create a VDRow and fill it in. Deals with
	 * nested tables.
	 * 
	 * @param vDRows
	 * @param tablePager
	 */
	public void populateVDRows(List<VDRow> vDRows, TablePager tablePager,
			VWorksheet vWorksheet) {
		boolean isFirst = true;
		Iterator<Row> it = tablePager.getRows().iterator();
		while (it.hasNext()) {
			Row r = it.next();
			VDRow vdRow = new VDRow(r, isFirst, !it.hasNext());
			isFirst = false;
			vDRows.add(vdRow);
			populateVDDataRow(vdRow, r, vWorksheet);
		}
	}

	private void populateVDDataRow(VDRow vdRow, Row dataRow,
			VWorksheet vWorksheet) {
		Iterator<VHTreeNode> it = children.iterator();
		while (it.hasNext()) {
			VHTreeNode vhNode = it.next();
			Node n = dataRow.getNode(vhNode.hNode.getId());
			VDTreeNode vdNode = new VDTreeNode(n);
			vdRow.add(vdNode);

			if (vhNode.hasChildren()) {
				vhNode.populateVDRows(vdNode.getNestedTableRows(),
						vWorksheet.getTablePager(n.getNestedTable().getId()),
						vWorksheet);
			}
		}

	}

	/*****************************************************************
	 * 
	 * Debugging Support
	 * 
	 *****************************************************************/

	@SuppressWarnings("unused")
	private String getPositionString() {
		if (isFirst() && isLast()) {
			return "first/last";
		} else if (isFirst()) {
			return "first";
		} else if (isLast()) {
			return "last";
		} else {
			return "BAD_POSITION_STRING";
		}
	}

	private String getStrokesString() {
		return leftStroke.toString() + "||" + rightStroke.toString();
	}

	private String getInnerStrokesString() {
		return leftInnerStroke.toString() + "||" + rightInnerStroke.toString();
	}

	private String getSubtreeStrokesString() {
		return Stroke.toString(leftSubtreeStrokes) + "||"
				+ Stroke.toString(rightSubtreeStrokes);
	}

	@SuppressWarnings("unused")
	private String getMarginsString() {
		return leftMargin.toString() + "||" + rightMargin.toString();
	}

	private String getSubtreeMarginsString() {
		return Margin.toString(leftSubtreeMargins) + "||"
				+ Margin.toString(rightSubtreeMargins);
	}

	private String getFullMarginsString() {
		return Margin.toString(leftFullMargins) + "||"
				+ Margin.toString(rightFullMargins);
	}

	private String getBordersString() {
		return Border.getBordersString(leftBorders) + "||"
				+ Border.getBordersString(rightBorders);
	}

	void prettyPrintJson(JSONWriter w) {
		try {
			String hNodeId = "NH__";
			String containingTable = "root";
			String columnName = "root";
			if (hNode != null) {
				hNodeId = hNode.getId();
				columnName = hNode.getColumnName();
				containingTable = hNode.getHTableId();
			}
			JSONWriter a = w.object().key("hNode")
					.value(hNodeId + "(" + containingTable + ")")//
					.key("hTableId").value(hTableId)//
					.key("column").value(columnName)//
					.key("width").value(width)//
					.key("depth").value(depth)//
					// .key("position").value(getPositionString())//
					.key("strokes").value(getStrokesString())//
					.key("subtreeStrokes").value(getSubtreeStrokesString())//
					// .key("margins").value(getMarginsString())//
					.key("subtreeMargins").value(getSubtreeMarginsString())//
					.key("fullMargins").value(getFullMarginsString())//
					.key("innerStrokes").value(getInnerStrokesString())//
					.key("borders").value(getBordersString())//
					.key("numMargins").value(numSubtreeMargins)//
			;
			if (!isLeaf()) {
				a.key("xchildren").array();
				for (VHTreeNode n : children) {
					n.prettyPrintJson(w);
				}
				a.endArray();
			}
			a.endObject();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	String prettyPrint() {
		JSONStringer js = new JSONStringer();
		prettyPrintJson(js);
		try {
			JSONObject o = new JSONObject(js.toString());
			return o.toString(3);
		} catch (JSONException e) {
			e.printStackTrace();
			return "error";
		}
	}

}
