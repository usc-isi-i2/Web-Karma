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
	private String language;
	private ColumnSemanticTypeStatus semanticTypeStatus; 

	private List<SemanticType> userSemanticTypes;
	private List<SemanticType> learnedSemanticTypes;
	
	public ColumnNode(String id, String hNodeId, String columnName, Label rdfLiteralType, String language) {
		super(id, new Label(hNodeId), NodeType.ColumnNode);
		this.hNodeId = hNodeId;
		this.columnName = columnName;
		this.setRdfLiteralType(rdfLiteralType);
		this.setLanguage(language);
		this.userSemanticTypes = null;
		this.learnedSemanticTypes = null;
		this.semanticTypeStatus = ColumnSemanticTypeStatus.NotAssigned;
	}
	
	public void includeInAutoModel() {
		this.semanticTypeStatus = ColumnSemanticTypeStatus.AutoAssigned;
	}
	
	public ColumnSemanticTypeStatus getSemanticTypeStatus() {
		return semanticTypeStatus;
	}

	public List<SemanticType> getLearnedSemanticTypes() {
		return learnedSemanticTypes;
	}

	public void setLearnedSemanticTypes(List<SemanticType> learnedSemanticTypes) {
		double sum = 0.0;
		// normalizing the confidence scores
		if (learnedSemanticTypes != null) {
			for (SemanticType st : learnedSemanticTypes) {
				sum += st.getConfidenceScore() != null ? st.getConfidenceScore().doubleValue() : 0.0;
			}
			double confidence;
			this.learnedSemanticTypes = new ArrayList<>();
			for (SemanticType st : learnedSemanticTypes) {
				confidence = st.getConfidenceScore() != null ? st.getConfidenceScore() : 0.0;
				SemanticType semType = new SemanticType(st.getHNodeId(), 
						st.getType(), 
						st.getDomain(), 
						st.getDomainId(),
						st.isProvenance(),
						st.getOrigin(), 
						confidence / sum);
				this.learnedSemanticTypes.add(semType);
			}
		}
		if (this.learnedSemanticTypes != null)
			Collections.sort(this.learnedSemanticTypes, Collections.reverseOrder());
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
	
	public String getLanguage() {
		return language;
	}
	
	public void setRdfLiteralType(String rdfLiteralType) {
		if (rdfLiteralType != null && rdfLiteralType.trim().length() > 0) {
			rdfLiteralType = rdfLiteralType.replace(Prefixes.XSD + ":", Namespaces.XSD);
			this.rdfLiteralType = new Label(rdfLiteralType, Namespaces.XSD, Prefixes.XSD);
		} else {
			this.rdfLiteralType = null;
		}
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
	public void setRdfLiteralType(Label rdfLiteralType) {
		this.rdfLiteralType = rdfLiteralType;
	}
	
	public List<SemanticType> getUserSemanticTypes() {
		if (userSemanticTypes == null)
			this.userSemanticTypes = new ArrayList<>();
		return Collections.unmodifiableList(userSemanticTypes);
	}

//	public void setUserSemanticTypes(List<SemanticType> userSemanticTypes) {
//		if (userSemanticTypes != null && !userSemanticTypes.isEmpty())
//			this.semanticTypeStatus = ColumnSemanticTypeStatus.Assigned;
//		this.userSemanticTypes = userSemanticTypes;
//	}
	public void unassignUserTypes() {
		if (userSemanticTypes == null)
			this.userSemanticTypes = new ArrayList<>();
		this.userSemanticTypes.clear();
	}
	
	public void assignUserType(SemanticType newType) {
		
		if (newType == null)
			return;
		
		if (userSemanticTypes == null)
			this.userSemanticTypes = new ArrayList<>();
		
		//FIXME: when user invokes SetSemanticType, we should unassign the old one, otherwise
		// we don't know if user wants to add more types or replace the existing one
		// currently, I assume that we replace the old one when a new type is assigned
		//this.userSemanticTypes.clear();
		
		this.userSemanticTypes.add(newType);
		this.semanticTypeStatus = ColumnSemanticTypeStatus.UserAssigned;
	}
	
	public void unassignUserType(SemanticType semanticType) {

		if (semanticType == null) {
			if (userSemanticTypes == null || userSemanticTypes.isEmpty())
				this.semanticTypeStatus = ColumnSemanticTypeStatus.Unassigned;
			return;
		}
		
		int tobeDeletedIndex = -1;
		if (userSemanticTypes != null) {
			SemanticType st;
			for (int i = 0; i < userSemanticTypes.size(); i++) {
				st = userSemanticTypes.get(i); 
				if (st.getModelLabelString().equalsIgnoreCase(semanticType.getModelLabelString())) {
					tobeDeletedIndex = i;
					break;
				}
			}
		}
		if (tobeDeletedIndex != -1 && userSemanticTypes != null) {
			userSemanticTypes.remove(tobeDeletedIndex);
		}
		if (userSemanticTypes == null || userSemanticTypes.isEmpty())
			this.semanticTypeStatus = ColumnSemanticTypeStatus.Unassigned;
	}

	public List<SemanticType> getTopKLearnedSemanticTypes(int k) {
		
		List<SemanticType> semanticTypes = new ArrayList<>();
		if (this.learnedSemanticTypes == null || this.learnedSemanticTypes.isEmpty())
			return semanticTypes;
		
		int n = Math.min(k, this.learnedSemanticTypes.size());
		Collections.sort(this.learnedSemanticTypes, Collections.reverseOrder());
		return this.learnedSemanticTypes.subList(0, n);
	}
	
}
