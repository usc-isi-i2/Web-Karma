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
package edu.isi.karma.rep.alignment;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.isi.karma.util.Jsonizable;

public class SemanticType implements Jsonizable, Serializable, Comparable<SemanticType>  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String hNodeId;
	private final Label type;
	private final Label domain;
	private final String domainId;
	private final Origin origin; 
	private final ConfidenceLevel confidenceLevel;
	private Double confidenceScore;
	private boolean isProvenance;

	public enum Origin {
		AutoModel, User, CRFModel, TfIdfModel, RFModel
	}

	public enum ConfidenceLevel {
		High, Medium, Low
	}
	
	public enum ClientJsonKeys {
		isPrimary, DomainUri, DomainId, FullType, isProvenance
	}
	
	public SemanticType(String hNodeId, Label type, Label domain, String domainId, boolean isProvenance,
			Origin origin, Double probability) {
		this.hNodeId = hNodeId;
		this.type = type;
		this.origin = origin;
		this.domain = domain;
		this.domainId = domainId;
		this.isProvenance = isProvenance;
		this.confidenceScore = probability;
		
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

	public Label getDomain() {
		return domain;
	}
	
	public String getDomainId() {
		return domainId;
	}
	
	public Label getType() {
		return type;
	}

	public Origin getOrigin() {
		return origin;
	}

	public Double getConfidenceScore() {
		if (this.confidenceScore == null)
			return Double.MIN_VALUE;
		return confidenceScore;
	}

	public boolean isProvenance() {
		return this.isProvenance;
	}
	
	@Override
	public String toString() {
		if (isClass())
			return "SemanticType [hNodeId=" + hNodeId + ", type=" + type.getUri()
				+ ", origin=" + origin
				+ ", confidenceLevel="
				+ confidenceLevel + "]";
		else
			return "SemanticType [hNodeId=" + hNodeId + ", type=" + type.getUri()
					+ ", domain=" + domain.getUri() + ", origin=" + origin
					+ ", confidenceLevel="
					+ confidenceLevel + "]";
	}

	//mariam
	/**
	 * Returns true if this type is a Class; false if it is a data property.
	 * @return
	 * 		true if this type is a Class; false if it is a data property.
	 */
	public boolean isClass(){
		if(domain==null || domain.getUri().trim().isEmpty())
			return true;
		//it is a data property
		return false;
	}
	public ConfidenceLevel getConfidenceLevel() {
		return confidenceLevel;
	}

	public JSONObject getJSONArrayRepresentation() throws JSONException {
		JSONObject typeObj = new JSONObject();
		typeObj.put(ClientJsonKeys.FullType.name(), type.getUri());
		if(isClass())
			typeObj.put(ClientJsonKeys.DomainUri.name(), "");
		else
			typeObj.put(ClientJsonKeys.DomainUri.name(), domain.getUri());
		return typeObj;
	}

	public void write(JSONWriter writer) throws JSONException {
		writer.object();
		writer.key("HNodeId").value(hNodeId);
		writer.key("Type").value(type);
		writer.key("Origin").value(origin.name());
		writer.key("ConfidenceLevel").value(confidenceLevel.name());
		writer.endObject();
	}
	
	public String getModelLabelString() {
		return this.getDomain().getUri() + "|" + this.getType().getUri();
	}

	
	@Override
	public int compareTo(SemanticType o) {
		if (this.confidenceScore == null && o.confidenceScore == null)
			return 0;
		else if (this.confidenceScore == null)
			return -1;
		else if (o.confidenceScore == null)
			return 1;
		else
			return this.confidenceScore.compareTo(o.confidenceScore);
	}
}
