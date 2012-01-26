package edu.isi.karma.rep.metadata;

import java.util.HashSet;

import com.sun.tools.javac.util.List;

import edu.isi.karma.rep.metadata.TagsContainer.Color;
import edu.isi.karma.rep.metadata.TagsContainer.TagName;

public class Tag {
	private HashSet<String> nodeIdList;
	private final TagName label;
	private final Color color;
	
	
	public Tag(TagName label, Color color) {
		super();
		this.label = label;
		this.color = color;
		nodeIdList = new HashSet<String>();
	}
	
	public Color getColor() {
		return color;
	}

	public TagName getLabel() {
		return label;
	}

	public HashSet<String> getNodeIdList() {
		return nodeIdList;
	}

	public void addNodeId(String nodeId) {
		nodeIdList.add(nodeId);
	}
	
	public void addNodeList(List<String> nodeList) {
		nodeList.addAll(nodeList);
	}
	
	public void removeNodeId(String nodeId) {
		nodeIdList.add(nodeId);
	}
}