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

package edu.isi.karma.kr2rml.template;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.kr2rml.formatter.KR2RMLColumnNameFormatter;
import edu.isi.karma.kr2rml.formatter.KR2RMLColumnNameFormatterFactory;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.metadata.WorksheetProperties.SourceTypes;

public class TemplateTermSet {
	
	private final List<TemplateTerm> termSet;
	
	public TemplateTermSet() {
		termSet = new LinkedList<>();
	}
	
	public void addTemplateTermToSet(TemplateTerm term) {
		this.termSet.add(term);
	}
	
	public List<TemplateTerm> getAllTerms() {
		return this.termSet;
	}
	
	public List<ColumnTemplateTerm> getAllColumnNameTermElements() {
		List<ColumnTemplateTerm> cnList = new ArrayList<>();
		for (TemplateTerm term:termSet) {
			if (term instanceof ColumnTemplateTerm) {
				cnList.add((ColumnTemplateTerm)term);
			}
		}
		return cnList;
	}

	public TemplateTermSet clear() {
		termSet.clear();
		return this;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (TemplateTerm term:termSet) {
			if (term instanceof StringTemplateTerm)
				str.append("<" + term.getTemplateTermValue() + ">");
			else if (term instanceof ColumnTemplateTerm)
				str.append("<ColumnHNodeId:" + term.getTemplateTermValue() + ">");
		}
		return str.toString();
	}
	
	public boolean isEmpty() {
		return termSet.isEmpty();
	}
	
	public String getR2rmlTemplateString(RepFactory factory) {
		//TODO fix this
		return getR2rmlTemplateString(factory, KR2RMLColumnNameFormatterFactory.getFormatter(SourceTypes.CSV));
	}
	public String getR2rmlTemplateString(RepFactory factory, KR2RMLColumnNameFormatter formatter) {
		StringBuilder str = new StringBuilder();
		for (TemplateTerm term:termSet) {
			if (term instanceof StringTemplateTerm) {
				str.append(term.getTemplateTermValue());
			} else if (term instanceof ColumnTemplateTerm) {
				HNode hNode = factory.getHNode(term.getTemplateTermValue());
				if (hNode != null) {
					String colNameStr;
					try {
						JSONArray colNameArr = hNode.getJSONArrayRepresentation(factory);
						if (colNameArr.length() == 1) {
							colNameStr = (String) 
									(((JSONObject)colNameArr.get(0)).get("columnName"));
						} else {
							JSONArray colNames = new JSONArray();
							for (int i=0; i<colNameArr.length();i++) {
								colNames.put((String)
										(((JSONObject)colNameArr.get(i)).get("columnName")));
							}
							colNameStr = colNames.toString();
						}
						str.append("{" + formatter.getFormattedColumnName(colNameStr) + "}");
					} catch (JSONException e) {
						continue;
					}
				}
				else {
					str.append("{" + formatter.getFormattedColumnName(term.getTemplateTermValue()) + "}");
				}
			}
		}
		return str.toString();
	}
	
	public String getColumnNameR2RMLRepresentation(RepFactory factory)
	{
		return getColumnNameR2RMLRepresentation(factory, KR2RMLColumnNameFormatterFactory.getFormatter(SourceTypes.CSV));
	}
	public String getColumnNameR2RMLRepresentation(RepFactory factory, KR2RMLColumnNameFormatter formatter) {
		StringBuilder str = new StringBuilder();
		for (TemplateTerm term:termSet) {
			if (term instanceof StringTemplateTerm) {
				str.append(term.getTemplateTermValue());
			} else if (term instanceof ColumnTemplateTerm) {
				HNode hNode = factory.getHNode(term.getTemplateTermValue());
				if (hNode != null) {
					String colNameStr;
					try {
						JSONArray colNameArr = hNode.getJSONArrayRepresentation(factory);
						if (colNameArr.length() == 1) {
							colNameStr = (String) 
									(((JSONObject)colNameArr.get(0)).get("columnName"));
						} else {
							JSONArray colNames = new JSONArray();
							for (int i=0; i<colNameArr.length();i++) {
								colNames.put((String)
										(((JSONObject)colNameArr.get(i)).get("columnName")));
							}
							colNameStr = colNames.toString();
						}
						str.append(formatter.getFormattedColumnName(colNameStr));
					} catch (JSONException e) {
						continue;
					}
				}
				else {
					str.append(formatter.getFormattedColumnName(term.getTemplateTermValue()));
				}
			}
		}
		return str.toString();
	}

	public boolean isSingleUriString() {
		if (termSet.size() == 1 && termSet.get(0) instanceof StringTemplateTerm)
			return ((StringTemplateTerm)termSet.get(0)).hasFullUri();
		
		return false;
	}

	public boolean isSingleColumnTerm() {
		if (termSet.size() == 1 && termSet.get(0) instanceof ColumnTemplateTerm)
			return true;
		return false;
	}
}