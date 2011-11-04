package edu.isi.karma.rep.semantictypes;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.util.Jsonizable;

public class SemanticType implements Jsonizable{
	private final String hNodeId;
	private final String type;
	private final String domain;
	private final Origin origin;
	
	public enum Origin {
		User, CRFModel
	}
	
	public SemanticType(String hNodeId, String type, Origin origin) {
		this.hNodeId = hNodeId;
		this.type = type;
		this.origin = origin;
		this.domain = "";
	}

	public SemanticType(String hNodeId, String type, String domain, Origin origin) {
		this.hNodeId = hNodeId;
		this.type = type;
		this.origin = origin;
		this.domain = domain;
	}

	public String gethNodeId() {
		return hNodeId;
	}

	public String getType() {
		return type;
	}
	
	public String getDomain() {
		return domain;
	}
	
	public Origin getOrigin() {
		return origin;
	}

	@Override
	public void write(JSONWriter writer) throws JSONException {
		writer.object();
		writer.key("HNodeId").value(hNodeId);
		writer.key("Type").value(type);
		writer.key("Origin").value(origin.name());
		writer.endObject();
	}
}