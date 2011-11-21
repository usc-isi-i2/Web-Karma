package edu.isi.karma.view.tableheadings;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.rep.hierarchicalheadings.TNode;

public class VTHNode implements TNode {
	private final String id;
	private final String label;
	List<TNode> children;
	
	private enum JsonKeys {
		id, label
	}

	public VTHNode (String id, String label) {
		this.id = id;
		this.label = label;
		children = new ArrayList<TNode>();
	}
	
	@Override
	public List<TNode> getChildren() {
		return children;
	}

	@Override
	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public void generateContentJson(PrintWriter pw) {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put(JsonKeys.id.name(), id);
			obj.put(JsonKeys.label.name(), label);
			pw.print(obj.toString(4));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getColorKey() {
		return null;
	}

	@Override
	public JSONObject generateJsonObject() {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put(JsonKeys.id.name(), id);
			obj.put(JsonKeys.label.name(), label);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}

	void addChild(VTHNode childNode) {
		if(!children.contains(childNode))
			children.add(childNode);
	}
}
