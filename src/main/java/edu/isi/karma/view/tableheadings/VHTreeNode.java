/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
/**
 * 
 */
package edu.isi.karma.view.tableheadings;

import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.cellType;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.colSpan;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.columnNameFull;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.columnNameShort;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.fillId;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.hNodeId;
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
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.view.Border;
import edu.isi.karma.view.Margin;
import edu.isi.karma.view.Stroke;
import edu.isi.karma.view.Stroke.StrokeStyle;
import edu.isi.karma.view.VTableCssTags;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.ViewPreferences.ViewPreference;
import edu.isi.karma.view.tabledata.VDIndexTable;
import edu.isi.karma.view.tabledata.VDRow;
import edu.isi.karma.view.tabledata.VDTreeNode;
import edu.isi.karma.view.tabledata.VDVerticalSeparator;
import edu.isi.karma.view.tabledata.VDVerticalSeparators;

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
	 * The (left/right) index within it's parent.
	 */
	private int index;

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

	public HNode getHNode() {
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

	public int getDepth() {
		return depth;
	}

	String getHNodeId() {
		if (isRoot()) {
			return "root";
		} else {
			return hNode.getId();
		}
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

	public boolean isFirst() {
		return isFirst;
	}

	public boolean isLast() {
		return isLast;
	}

	public boolean isMiddle() {
		return !isFirst && !isLast;
	}

	boolean isFirstAndLast() {
		return isFirst() && isLast();
	}

	int getIndex() {
		return index;
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
			index = 0;
		}
		boolean isFirstChild = true;
		int currentIndex = 0;
		Iterator<VHTreeNode> it = children.iterator();
		while (it.hasNext()) {
			VHTreeNode child = it.next();
			child.index = currentIndex;
			currentIndex++;
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
			result = JSONUtil.truncateForHeader(string, maxLength);
		}
		return result;
	}

	private void generateHeadingJson(JSONWriter jw, VWorksheet vWorksheet,
			VWorkspace vWorkspace) throws JSONException {
		VTableCssTags css = vWorkspace.getViewFactory().getTableCssTags();

		String topBorderCss = isRoot() ? css.getCssTag("root", 0) : css
				.getCssTag(hNode.getHTableId(), depth-1);

		jw.object()
				//
				.key(cellType.name())
				.value(heading.name())
				//
				.key(hNodeId.name())
				.value(hNode.getId())
				//
				.key(columnNameFull.name())
				.value(hNode.getColumnName())
				//
				.key(columnNameShort.name())
				.value(shorten(
						hNode.getColumnName(),
						vWorkspace.getPreferences().getIntViewPreferenceValue(
								ViewPreference.maxCharactersInHeader)))
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
						css.getCssTag(hTableId, depth-1)))
		//
		;
		generateHeadingCommonField(jw, vWorksheet, vWorkspace);
		jw.endObject();
	}

	private void generateHeadingCommonField(JSONWriter jw,
			VWorksheet vWorksheet, VWorkspace vWorkspace) throws JSONException {
		VTableCssTags css = vWorkspace.getViewFactory().getTableCssTags();

		String hTableId = isRoot() ? "root" : hNode.getHTableId();
		String fillCssTag = css.getCssTag(hTableId, depth-1);

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
						css.getCssTag(leftInnerStroke.getHTableId(), depth-1)))
				//
				.key(rightBorder.name())
				.value(Border.encodeBorder(rightInnerStroke.getStyle(),
						css.getCssTag(rightInnerStroke.getHTableId(), depth-1)))//
				//
				.key("_hTableId").value(hTableId);
		;
	}

	/**
	 * For each row in the TablePager, create a VDRow and fill it in. Deals with
	 * nested tables.
	 * 
	 * @param vDRows
	 * @param tablePager
	 */
	public void populateVDRows(VDTreeNode parent, List<VDRow> vDRows,
			TablePager tablePager, VWorksheet vWorksheet) {
		boolean isFirst = true;
		Iterator<Row> it = tablePager.getRows().iterator();
		while (it.hasNext()) {
			Row r = it.next();
			VDRow vdRow = new VDRow(r, this, parent, isFirst, !it.hasNext());
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
			VDTreeNode vdNode = new VDTreeNode(n, vhNode, vdRow);
			vdRow.add(vdNode);

			if (vhNode.hasChildren()) {
				vhNode.populateVDRows(vdNode, vdNode.getNestedTableRows(),
						vWorksheet.getTablePager(n.getNestedTable().getId()),
						vWorksheet);
			}
		}

	}

	/**
	 * Collect all the leaves of the tree in left to right order.
	 * 
	 * @param result
	 */
	public void collectLeaves(List<VHTreeNode> result) {
		if (isLeaf()) {
			result.add(this);
		} else {
			for (VHTreeNode n : children) {
				n.collectLeaves(result);
			}
		}
	}

	public void populateVDIndexTable(VDIndexTable vdIndexTable) {
		for (VHTreeNode n : children) {
			if (!n.isLeaf()) {
				List<VHTreeNode> list = new LinkedList<VHTreeNode>();
				n.collectLeaves(list);
				vdIndexTable.addIndex(n.getHNode(), list);
			}
			n.populateVDIndexTable(vdIndexTable);
		}
	}

	public void populateVDVerticalSeparators(
			VDVerticalSeparators vdVerticalSeparators) {
		VDVerticalSeparator s = vdVerticalSeparators.get(getHNodeId());

		for (VHTreeNode n : children) {

			VDVerticalSeparator vs = new VDVerticalSeparator();
			if (n.isFirst) {
				vs.addLeft(s.getLeftStrokes());
			} else {
				vs.addLeft(depth, hTableId);
			}
			if (n.isLast) {
				vs.addRight(s.getRightStrokes());
			} else {
				vs.addRight(depth, hTableId);
			}

			if (!n.isLeaf()) {
				vs.add(n.depth, n.hTableId);
			}
			vdVerticalSeparators.put(n.hNode.getId(), vs);

			n.populateVDVerticalSeparators(vdVerticalSeparators);
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

	public void prettyPrintJson(JSONWriter w, boolean printChildren,
			boolean verbose) {
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
			;
			if (verbose) {
				a//
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
			}
			if (!isLeaf() && printChildren) {
				a.key("xchildren").array();
				for (VHTreeNode n : children) {
					n.prettyPrintJson(w, printChildren, verbose);
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
		prettyPrintJson(js, true, true);
		try {
			JSONObject o = new JSONObject(js.toString());
			return o.toString(3);
		} catch (JSONException e) {
			e.printStackTrace();
			return "error";
		}
	}

}
