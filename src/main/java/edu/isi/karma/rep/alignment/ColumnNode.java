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

import java.util.List;



public class ColumnNode extends Node {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String hNodeId;
	private final String columnName;
	private String rdfLiteralType;
	
	private List<SemanticType> crfSuggestedSemanticTypes;
	private SemanticType userSelectedSemanticType;
	
	public ColumnNode(String id, String hNodeId, String columnName, String rdfLiteralType, List<SemanticType> crfSuggestedSemanticTypes) {
		super(id, new Label(hNodeId), NodeType.ColumnNode);
		this.hNodeId = hNodeId;
		this.columnName = columnName;
		this.rdfLiteralType = rdfLiteralType;
		this.crfSuggestedSemanticTypes = crfSuggestedSemanticTypes;
		this.userSelectedSemanticType = null;
	}
	
	public ColumnNode(String id, String hNodeId, String columnName, String rdfLiteralType) {
		super(id, new Label(hNodeId), NodeType.ColumnNode);
		this.hNodeId = hNodeId;
		this.columnName = columnName;
		this.rdfLiteralType = rdfLiteralType;
		this.crfSuggestedSemanticTypes = null;
		this.userSelectedSemanticType = null;
	}

	public String getHNodeId() {
		return hNodeId;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getRdfLiteralType() {
		return rdfLiteralType;
	}
	
	public void setRdfLiteralType(String rdfLiteralType) {
		this.rdfLiteralType = rdfLiteralType;
	}

	public SemanticType getUserSelectedSemanticType() {
		return userSelectedSemanticType;
	}

	public void setUserSelectedSemanticType(SemanticType userSelectedSemanticType) {
		this.userSelectedSemanticType = userSelectedSemanticType;
	}

	public List<SemanticType> getCrfSuggestedSemanticTypes() {
		return crfSuggestedSemanticTypes;
	}
	
	
}
