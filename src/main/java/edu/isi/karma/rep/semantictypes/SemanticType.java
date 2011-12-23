package edu.isi.karma.rep.semantictypes;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.util.Jsonizable;

public class SemanticType implements Jsonizable {
	private final String hNodeId;
	private final String type;
	private final String domain;
	private final Origin origin;
	private final boolean isPartOfKey; 
	private final ConfidenceLevel confidenceLevel;

	public enum Origin {
		User, CRFModel
	}

	public enum ConfidenceLevel {
		High, Medium, Low
	}

	public SemanticType(String hNodeId, String type, Origin origin, Double probability, boolean isPartOfKey) {
		this.hNodeId = hNodeId;
		this.type = type;
		this.origin = origin;
		this.domain = "";
		this.isPartOfKey = isPartOfKey;
		
		if(probability > 0.8)
			confidenceLevel = ConfidenceLevel.High;
		else if (probability < 0.8 && probability > 0.4)
			confidenceLevel = ConfidenceLevel.Medium;
		else
			confidenceLevel = ConfidenceLevel.Low;
	}
	
	public SemanticType(String hNodeId, String type, String domain, Origin origin, Double probability, boolean isPartOfKey) {
		this.hNodeId = hNodeId;
		this.type = type;
		this.origin = origin;
		this.domain = domain;
		this.isPartOfKey = isPartOfKey;
		
		if(probability > 0.8)
			confidenceLevel = ConfidenceLevel.High;
		else if (probability < 0.8 && probability > 0.4)
			confidenceLevel = ConfidenceLevel.Medium;
		else
			confidenceLevel = ConfidenceLevel.Low;
	}

	public String getHNodeId() {
		return hNodeId;
	}

	public String getDomain() {
		return domain;
	}
	
	public String getType() {
		return type;
	}

	public Origin getOrigin() {
		return origin;
	}

	public ConfidenceLevel getConfidenceLevel() {
		return confidenceLevel;
	}

	public boolean isPartOfKey() {
		return isPartOfKey;
	}

	@Override
	public void write(JSONWriter writer) throws JSONException {
		writer.object();
		writer.key("HNodeId").value(hNodeId);
		writer.key("Type").value(type);
		writer.key("Origin").value(origin.name());
		writer.key("ConfidenceLevel").value(confidenceLevel.name());
		writer.endObject();
	}
}