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
package edu.isi.karma.view;


import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.TablePager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.view.ViewPreferences.ViewPreference;


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
	
	//private final List<HNodePath> columns;

	private ArrayList<VHNode> headerViewNodes;
	
	/**
	 * The maximum number of rows to show in the nested tables.
	 */
	private int maxRowsToShowInNestedTables;


	/**
	 * We create a TablePager for the top level table and every nested table we
	 * see. It records how the table is scrolled.
	 */
	private Map<String, TablePager> tableId2TablePager = new HashMap<>();
	
	private static Logger logger = LoggerFactory
			.getLogger(VWorksheet.class);
	
	VWorksheet(String id, Worksheet worksheet, List<HNodePath> columns,
			VWorkspace vWorkspace) {
		super(id);
		this.worksheet = worksheet;
		//this.columns = columns;
		this.maxRowsToShowInNestedTables = vWorkspace.getPreferences()
				.getIntViewPreferenceValue(
						ViewPreference.maxRowsToShowInNestedTables);

		// Force creation of the TablePager for the top table.
		getTablePager(worksheet.getDataTable(),
				vWorkspace.getPreferences().getIntViewPreferenceValue(
						ViewPreference.defaultRowsToShowInTopTables));
		
		
		this.headerViewNodes = initHeaderViewNodes(worksheet.getHeaders());
	}

	
	private ArrayList<VHNode> initHeaderViewNodes(HTable table) {
		ArrayList<VHNode> vNodes = new ArrayList<>();
		for(String hNodeId : table.getOrderedNodeIds()) {
			HNode node = table.getHNode(hNodeId);
			VHNode vNode = new VHNode(node.getId(), node.getColumnName());
			if(node.hasNestedTable()) {
				HTable nestedTable = node.getNestedTable();
				ArrayList<VHNode> nestedNodes = initHeaderViewNodes(nestedTable);
				for(VHNode nestedVNode : nestedNodes) {
					vNode.addNestedNode(nestedVNode);
				}
			}
			vNodes.add(vNode);
		}
		return vNodes;
	}
	
	
	private TablePager getTablePager(Table table, int size) {
		TablePager tp = tableId2TablePager.get(table.getId());
		if (tp != null) {
			return tp;
		} else {
			tp = new TablePager(table, size);
			tableId2TablePager.put(table.getId(), tp);
			return tp;
		}
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

	
	
	public Map<String, TablePager> getTableId2TablePager() {
		return tableId2TablePager;
	}

	public void setTableId2TablePager(Map<String, TablePager> tableId2TablePager) {
		this.tableId2TablePager = tableId2TablePager;
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
	
	public ArrayList<VHNode> getHeaderViewNodes() {
		return this.headerViewNodes;
	}
	
	public JSONArray getHeaderViewNodesJSON() {
		JSONArray newColumns = new JSONArray();
		for(VHNode node : this.headerViewNodes) {
			newColumns.put(getJSONRep(node));
		}
		return newColumns;
	}
	
	private JSONObject getJSONRep(VHNode node) {
		JSONObject obj = new JSONObject();
		obj.put("id", node.getId());
		obj.put("name", node.getColumnName());
		obj.put("visible", node.isVisible());
		if(node.hasNestedTable()) {
			JSONArray children = new JSONArray();
			for(VHNode childNode : node.getNestedNodes()) {
				children.put(getJSONRep(childNode));
			}
			obj.put("children", children);
		}
		return obj;
	}
	
	public ArrayList<String> getHeaderVisibleNodes() {
		return getVisibleViewNodes(this.headerViewNodes);
	}
	
	private ArrayList<String> getVisibleViewNodes(ArrayList<VHNode> list) {
		ArrayList<String> visibleNodeIds = new ArrayList<>();
		for(VHNode node : list) {
			if(node.isVisible()) {
				visibleNodeIds.add(node.getId());
				visibleNodeIds.addAll(getVisibleViewNodes(node.getNestedNodes()));
			}
		}
		return visibleNodeIds;
	}
	
	public ArrayList<String> getHeaderVisibleLeafNodes() {
		return getHeaderVisibleLeafNodes(this.headerViewNodes);
	}
	
	private ArrayList<String> getHeaderVisibleLeafNodes(ArrayList<VHNode> list) {
		ArrayList<String> visibleNodeIds = new ArrayList<>();
		for(VHNode node : list) {
			if(node.isVisible()) {
				if(node.hasNestedTable()) {
					visibleNodeIds.addAll(getHeaderVisibleLeafNodes(node.getNestedNodes()));
				} else {
					visibleNodeIds.add(node.getId());
				}
			}
		}
		return visibleNodeIds;
	}
	
//	public ArrayList<VHNode> getHeaderViewNodes(String id) {
//		if(id.equals(worksheet.getHeaders().getId()))
//			return this.headerViewNodes;
//		
//		return getChildHeaderNodes(this.headerViewNodes, id);
//	}
//	
//	private ArrayList<VHNode> getChildHeaderNodes(ArrayList<VHNode> vNodes, String id) {
//		ArrayList<VHNode> children = null;
//		for(VHNode node : vNodes) {
//			if(node.getId().equals(id)) {
//				children = node.getNestedNodes();
//				break;
//			}
//			
//			if(node.hasNestedTable()) {
//				ArrayList<VHNode> matchingChild = getChildHeaderNodes(node.getNestedNodes(), id);
//				if(matchingChild != null) {
//					children = matchingChild;
//					break;
//				}
//			}
//		}
//		return children;
//	}
//	
	public boolean isHeaderNodeVisible(String hnodeId) {
		return getHeaderVisibleNodes().contains(hnodeId);
	}

	
	private ArrayList<VHNode> generateOrganizedColumns(JSONArray columns) {
		ArrayList<VHNode> vNodes = new ArrayList<>();
		
		for(int i=0; i<columns.length(); i++) {
			JSONObject column = columns.getJSONObject(i);
			
			if(column.has("id") && column.get("id") instanceof String) {
				try {
					VHNode vNode = new VHNode(column.getString("id"), column.get("name").toString());
					vNode.setVisible(column.getBoolean("visible"));
					if(column.has("children")) {
						ArrayList<VHNode> nestedNodes = generateOrganizedColumns(column.getJSONArray("children"));
						for(VHNode nestedVNode : nestedNodes) {
							vNode.addNestedNode(nestedVNode);
						}
					}
					vNodes.add(vNode);
				} catch(Exception e) {
					logger.error("Error organizing column:" + column.toString(), e);
				}
			}
		}
		
		return vNodes;
	}
	
	public void organizeColumns(JSONArray columns) {
		this.headerViewNodes = generateOrganizedColumns(columns);
	}
	
	public void organizeColumns(ArrayList<VHNode> columns) {
		this.headerViewNodes.clear();
		this.headerViewNodes.addAll(columns);
	}
	
	public void updateHeaderViewNodes(ArrayList<VHNode> oldOrderedNodes) {
		
		ArrayList<String> oldPaths = new ArrayList<>();
		for(VHNode node : oldOrderedNodes)
			oldPaths.addAll(node.getAllPaths());
		
		ArrayList<String> newPaths = new ArrayList<>();
		for(VHNode node : this.headerViewNodes)
			newPaths.addAll(node.getAllPaths());
		
		//1/. Get all paths in old that are not in new
		ArrayList<String> pathsToDel = new ArrayList<>();
		for(String oldPath : oldPaths) {
			if(!newPaths.contains(oldPath)) {
				pathsToDel.add(oldPath);
			}
		}
		
		//2. Get all paths in new that are not in old
		ArrayList<String> pathsToAdd = new ArrayList<>();
		for(int i=0; i<newPaths.size(); i++) {
			String newPath = newPaths.get(i);
			if(!oldPaths.contains(newPath)) {
				if (i != 0) {
					String after = newPaths.get(i-1);
					int difference = StringUtils.countMatches(after, "/") - StringUtils.countMatches(newPath, "/");
					for (int k = 0; k < difference; k++) {
						after = after.substring(0, after.lastIndexOf("/"));
					}
					pathsToAdd.add(newPath + "$$" + after);
				}
				else {
					pathsToAdd.add(newPath + "$$" + "null");
				}
			}
		}
		
		ArrayList<VHNode> allNodes = new ArrayList<>();
		allNodes.addAll(oldOrderedNodes);
		this.headerViewNodes = allNodes;
		
		for(String path : pathsToDel) {
			deleteHeaderViewPath(this.headerViewNodes, path);
		}
		
		for(String path : pathsToAdd) {
			addHeaderViewPath(path);
		}
	}
	
	private void addHeaderViewPath(String path) {
		int after = path.indexOf("$$");
		String afterPath = path.substring(after + 2);
		path = path.substring(0, after);
		
		ArrayList<VHNode> parentList = this.headerViewNodes;
		int idx;
		
		String pathStart = path;
		
		if((idx = pathStart.lastIndexOf("/")) != -1) {
			String parentSig = path.substring(0, idx);
			pathStart = path.substring(idx+1);
		
			VHNode parentNode = VHNode.getVHNodeFromPath(parentSig, this.headerViewNodes);
			parentList = parentNode.getNestedNodes();
		}
		
		idx = pathStart.indexOf(":");
		String id = pathStart.substring(0, idx);
		String name = pathStart.substring(idx+1);
		VHNode newNode = new VHNode(id, name);
		
		int insertIdx = -1;
		idx = afterPath.lastIndexOf("/");
		if(idx != -1)
			afterPath = afterPath.substring(idx + 1);
		for(int i=0; i<parentList.size(); i++) {
			VHNode node = parentList.get(i);
			if(node.getNodePathSignature().equals(afterPath)) {
				insertIdx = i;
				break;
			}
		}
		parentList.add(insertIdx+1, newNode);
	}
	
	private void deleteHeaderViewPath(ArrayList<VHNode> nodes, String path) {
		int idx = path.indexOf("/");
		String pathStart = path, pathEnd = null;
		if(idx != -1) {
			pathStart = path.substring(0, idx);
			pathEnd = path.substring(idx+1);
		}
		
		for(VHNode node : nodes) {
			if(node.getNodePathSignature().equals(pathStart)) {
				if(pathEnd == null) {
					nodes.remove(node);
				} else {
					deleteHeaderViewPath(node.getNestedNodes(), pathEnd);
				}
				break;
			}
			
		}
	}
	
	public void generateWorksheetListJson(String prefix, PrintWriter pw) {
		pw.println(prefix + "{");
		String newPref = prefix + "  ";

		pw.println(newPref
				+ JSONUtil.json(WorksheetListUpdate.JsonKeys.worksheetId, this.getWorksheetId()));
		pw.println(newPref
				+ JSONUtil.json(WorksheetListUpdate.JsonKeys.isUpToDate, upToDate));
		pw.println(newPref
				+ JSONUtil.json(WorksheetListUpdate.JsonKeys.isCollapsed, collapsed));

		pw.println(newPref
				+ JSONUtil.json(WorksheetListUpdate.JsonKeys.encoding,
						worksheet.getEncoding()));
		pw.println(newPref
				+ JSONUtil.jsonLast(WorksheetListUpdate.JsonKeys.title,
						worksheet.getTitle()));

		
		pw.println(prefix + "}");
	}
}
