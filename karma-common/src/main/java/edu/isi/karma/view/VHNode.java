package edu.isi.karma.view;

import java.util.ArrayList;

public class VHNode {
	private String id;
	private String columnName;
	private boolean visible;
	private ArrayList<VHNode> nestedNodes;
	
	
	public VHNode(String id, String columnName) {
		this.id = id;
		this.columnName = columnName;
		this.visible = true;
		this.nestedNodes = new ArrayList<>();
	}
	
	public String getId() {
		return id;
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	public boolean hasNestedTable() {
		return !this.nestedNodes.isEmpty();
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public ArrayList<VHNode> getNestedNodes() {
		return nestedNodes;
	}
	
	public void addNestedNode(VHNode node) {
		this.nestedNodes.add(node);
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public ArrayList<String> getAllPaths() {
		ArrayList<String> paths = new ArrayList<>();
		paths.add(getNodePathSignature());
		
		
		for(VHNode nestedNode : getNestedNodes()) {
			ArrayList<String> nestedPaths = nestedNode.getAllPaths();
			for(String nestedPath : nestedPaths) {
				paths.add(getNodePathSignature() + "/" + nestedPath);
			}
		}
		return paths;
	}
	
	public static VHNode getVHNodeFromPath(String path, ArrayList<VHNode> nodes) {
		int idx = path.indexOf("/");
		String pathStart = path, pathEnd = null;
		if(idx != -1) {
			pathStart = path.substring(0, idx);
			pathEnd = path.substring(idx+1);
		}
		
		VHNode startNode = null;
		
		for(VHNode node : nodes) {
			if(node.getNodePathSignature().equals(pathStart)) {
				startNode =  node;
				break;
			}
		}
		
		if(startNode != null) {
			if(pathEnd == null)
				return startNode;
			else
				return getVHNodeFromPath(pathEnd, startNode.getNestedNodes());
		}
		return null;
	}
	
	
	public String getNodePathSignature() {
		return getId() + ":" + getColumnName();
	}
}
