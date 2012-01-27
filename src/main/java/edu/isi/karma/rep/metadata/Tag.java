package edu.isi.karma.rep.metadata;

import java.util.HashSet;
import java.util.Set;

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
	
	public void addNodeIds(Set<String> nodes) {
		nodeIdList.addAll(nodes);
	}
	
	public void removeNodeId(String nodeId) {
		nodeIdList.add(nodeId);
	}
	
	public void removeNodeIds(Set<String> nodes) {
		nodeIdList.removeAll(nodes);
	}
}