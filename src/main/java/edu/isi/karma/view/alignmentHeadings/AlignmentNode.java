package edu.isi.karma.view.alignmentHeadings;

import java.io.PrintWriter;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.hierarchicalheadings.TNode;
import edu.isi.karma.rep.semantictypes.SemanticType;

public class AlignmentNode implements TNode {
	private final String id;
	private List<TNode> children;
	private AlignmentLink parentLink;
	private final String label;
	private SemanticType semanticType;

	private enum JsonKeys {
		id, parentLinkLabel, parentLinkId, label
	}

	public AlignmentNode(String id, List<TNode> children,
			AlignmentLink parentLink, String label, SemanticType semanticType) {
		super();
		this.id = id;
		this.children = children;
		this.parentLink = parentLink;
		this.label = label;
		this.semanticType = semanticType;
	}

	public SemanticType getType() {
		return semanticType;
	}

	@Override
	public List<TNode> getChildren() {
		return children;
	}

	@Override
	public String getId() {
		return id;
	}
	
	public boolean hasSemanticType() {
		if(semanticType != null)
			return true;
		return false;
	}
	
	public boolean hasChildren() {
		if(children != null && children.size() != 0)
			return true;
		return false;
	}
	
	public String getSemanticTypeHNodeId() {
		if(semanticType != null) {
			return semanticType.getHNodeId();
		} else
			return null;
	}

	@Override
	public void generateContentJson(PrintWriter pw) {
		JSONObject obj = new JSONObject();

		try {
			obj.put(JsonKeys.id.name(), id);
			obj.put(JsonKeys.label.name(),
					SemanticTypeUtil.removeNamespace(label));
			obj.put(JsonKeys.parentLinkId.name(), parentLink.getId());
			obj.put(JsonKeys.parentLinkLabel.name(),
					SemanticTypeUtil.removeNamespace(parentLink.getLabel()));

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
			obj.put(JsonKeys.label.name(),
					SemanticTypeUtil.removeNamespace(label));
			if (parentLink != null) {
				obj.put(JsonKeys.parentLinkId.name(), parentLink.getId());
				obj.put(JsonKeys.parentLinkLabel.name(),
						SemanticTypeUtil.removeNamespace(parentLink.getLabel()));
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
