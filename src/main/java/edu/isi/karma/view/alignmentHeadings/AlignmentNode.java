package edu.isi.karma.view.alignmentHeadings;

import java.io.PrintWriter;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.hierarchicalheadings.TNode;

public class AlignmentNode implements TNode {
	private final String id;
	private List<TNode> children;
	private AlignmentLink parentLink;
	private final String label;
	
	private enum JsonKeys {
		id, parentLinkLabel, parentLinkId, label
	}

	public AlignmentNode(String id, List<TNode> children,
			AlignmentLink parentLink, String label) {
		super();
		this.id = id;
		this.children = children;
		this.parentLink = parentLink;
		this.label = label;
	}

	@Override
	public List<TNode> getChildren() {
		return children;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void generateContentJson(PrintWriter pw) {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put(JsonKeys.id.name(), id);
			obj.put(JsonKeys.label.name(), SemanticTypeUtil.removeNamespace(label));
			obj.put(JsonKeys.parentLinkId.name(), parentLink.getId());
			obj.put(JsonKeys.parentLinkLabel.name(), parentLink.getLabel());
			
			pw.println(obj.toString(4));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public JSONObject generateJsonObject() {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put(JsonKeys.id.name(), id);
			obj.put(JsonKeys.label.name(), SemanticTypeUtil.removeNamespace(label));
			if(parentLink != null){
				obj.put(JsonKeys.parentLinkId.name(), parentLink.getId());
				obj.put(JsonKeys.parentLinkLabel.name(), parentLink.getLabel());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}

	@Override
	public String getColorKey() {
		return null;
	}

}
