/*******************************************************************************
,. * Copyright 2012 University of Southern California
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;


public class ColumnNode extends Node {

	private final String hNodeId;
	private final String columnName;
	private Label rdfLiteralType;

	private InternalNode domainNode;
	private LabeledLink domainLink;
	
	private SemanticType userSelectedSemanticType;
	private List<SemanticType> suggestedSemanticTypes;
	
	public ColumnNode(String id, String hNodeId, String columnName, Label rdfLiteralType) {
		super(id, new Label(hNodeId), NodeType.ColumnNode);
		this.hNodeId = hNodeId;
		this.columnName = columnName;
		this.setRdfLiteralType(rdfLiteralType);
		this.userSelectedSemanticType = null;
		this.suggestedSemanticTypes = null;
	}

	public boolean hasUserType() {
		return this.userSelectedSemanticType == null ? false : true;
	}
	
	public InternalNode getDomainNode() {
		return domainNode;
	}


	public void setDomainNode(InternalNode domainNode) {
		this.domainNode = domainNode;
	}


	public LabeledLink getDomainLink() {
		return domainLink;
	}


	public void setDomainLink(LabeledLink domainLink) {
		this.domainLink = domainLink;
	}


	public List<SemanticType> getSuggestedSemanticTypes() {
		return suggestedSemanticTypes;
	}

	public void setSuggestedSemanticTypes(List<SemanticType> suggestedSemanticTypes) {
		double sum = 0.0;
		// normalizing the confidence scores
		if (suggestedSemanticTypes != null) {
			for (SemanticType st : suggestedSemanticTypes) {
				sum += st.getConfidenceScore() != null ? st.getConfidenceScore().doubleValue() : 0.0;
			}
			double confidence;
			this.suggestedSemanticTypes = new ArrayList<SemanticType>();
			for (SemanticType st : suggestedSemanticTypes) {
				confidence = st.getConfidenceScore() != null ? st.getConfidenceScore() : 0.0;
				SemanticType semType = new SemanticType(st.getHNodeId(), 
						st.getType(), 
						st.getDomain(), 
						st.getOrigin(), 
						confidence / sum);
				this.suggestedSemanticTypes.add(semType);
			}
		}
	}

	public String getHNodeId() {
		return hNodeId;
	}

	public String getColumnName() {
		return columnName;
	}

	public Label getRdfLiteralType() {
		return rdfLiteralType;
	}
	
	public void setRdfLiteralType(String rdfLiteralType) {
		if (rdfLiteralType != null && rdfLiteralType.trim().length() > 0) {
			rdfLiteralType = rdfLiteralType.replace(Prefixes.XSD + ":", Namespaces.XSD);
			this.rdfLiteralType = new Label(rdfLiteralType, Namespaces.XSD, Prefixes.XSD);
		} else {
			this.rdfLiteralType = null;
		}
	}

	public void setRdfLiteralType(Label rdfLiteralType) {
		this.rdfLiteralType = rdfLiteralType;
	}
	
	public SemanticType getUserSelectedSemanticType() {
		return userSelectedSemanticType;
	}

	public void setUserSelectedSemanticType(SemanticType userSelectedSemanticType) {
		this.userSelectedSemanticType = userSelectedSemanticType;
	}

	public List<SemanticType> getTopKSuggestions(int k) {
		
		List<SemanticType> semanticTypes = new ArrayList<SemanticType>();
		if (this.suggestedSemanticTypes == null || this.suggestedSemanticTypes.isEmpty())
			return semanticTypes;
		
		int n = Math.min(k, this.suggestedSemanticTypes.size());
		Collections.sort(this.suggestedSemanticTypes, Collections.reverseOrder());
		return this.suggestedSemanticTypes.subList(0, n);
	}
	
}
