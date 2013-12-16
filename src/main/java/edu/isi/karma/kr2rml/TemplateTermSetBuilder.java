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

package edu.isi.karma.kr2rml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Worksheet;

public class TemplateTermSetBuilder {

	private static Logger logger = LoggerFactory.getLogger(TemplateTermSetBuilder.class);
	private Worksheet worksheet;
	
	public TemplateTermSetBuilder(Worksheet worksheet)
	{
		this.worksheet = worksheet;
	}
	public  TemplateTermSet constructTemplateTermSetFromR2rmlTemplateString(
			String templStr) throws JSONException {
		TemplateTermSet termSet = new TemplateTermSet();
		
		Pattern p = Pattern.compile("\\{\\\".*?\\\"\\}");
	    Matcher matcher = p.matcher(templStr);
	    int startIndex = 0;
	    
	    if (matcher.find()) {
	    	matcher.reset();
	    	while (matcher.find()) {
		    	// We have a String template term before this column template term
		    	if (matcher.start() != startIndex) {
		    		String strTermVal = templStr.substring(startIndex, matcher.start());
		    		logger.debug("String templ term: " + strTermVal);
		    		termSet.addTemplateTermToSet(new StringTemplateTerm(strTermVal));
		    	}
		    	
		    	String colTermVal = removeR2rmlFormatting(matcher.group());
		    	logger.debug("Col name templ term: " + colTermVal);
		    	HTable hTable = worksheet.getHeaders();
		    	// If hierarchical columns
		    	if (colTermVal.startsWith("[") && colTermVal.endsWith("]") && colTermVal.contains(",")) {
		    		JSONArray strArr = new JSONArray(colTermVal);
		    		for (int i=0; i<strArr.length(); i++) {
						String cName = (String) strArr.get(i);
						
						logger.debug("Column being normalized: "+ cName);
						HNode hNode = hTable.getHNodeFromColumnName(cName);
						if(hNode == null || hTable == null) {
							logger.error("Error retrieving column: " + cName);
							return null;
						}
						
						if (i == strArr.length()-1) {		// Found!
							String hNodeId = hNode.getId();
							termSet.addTemplateTermToSet(new ColumnTemplateTerm(hNodeId));
						} else {
							hTable = hNode.getNestedTable();
						}
		    		}
		    	} else {
		    		HNode hNode = hTable.getHNodeFromColumnName(colTermVal);
		    		logger.debug("Column" + removeR2rmlFormatting(colTermVal));
		    		termSet.addTemplateTermToSet(new ColumnTemplateTerm(hNode.getId()));
		    	}
		    	
	    		startIndex = matcher.end();
		      }
	    } else {
	    	termSet.addTemplateTermToSet(new StringTemplateTerm(templStr));
	    }
	    
		return termSet;
	}
	
	public TemplateTermSet constructTemplateTermSetFromR2rmlColumnString(
			String colTermVal) throws JSONException {
		TemplateTermSet termSet = new TemplateTermSet();
		HTable hTable = worksheet.getHeaders();
		
    	// If hierarchical columns
    	if (colTermVal.startsWith("[") && colTermVal.endsWith("]")) {
    		JSONArray strArr = new JSONArray(colTermVal);
    		for (int i=0; i<strArr.length(); i++) {
				String cName = (String) strArr.get(i);
				
				logger.debug("Column being normalized: "+ cName);
				HNode hNode = hTable.getHNodeFromColumnName(cName);
				if(hNode == null || hTable == null) {
					logger.error("Error retrieving column: " + cName);
					return null;
				}
				
				if (i == strArr.length()-1) {		// Found!
					String hNodeId = hNode.getId();
					termSet.addTemplateTermToSet(new ColumnTemplateTerm(hNodeId));
				} else {
					hTable = hNode.getNestedTable();
				}
    		}
    	} else {
    		HNode hNode = hTable.getHNodeFromColumnName(
    				removeR2rmlFormatting(colTermVal));
    		logger.debug("Column" + removeR2rmlFormatting(colTermVal));
    		termSet.addTemplateTermToSet(new ColumnTemplateTerm(hNode.getId()));
    	}
	    	
		return termSet;
	}
	
	private String removeR2rmlFormatting(String r2rmlColName) {
		if (r2rmlColName.startsWith("{\"") && r2rmlColName.endsWith("\"}"))
			return r2rmlColName.substring(2, r2rmlColName.length()-2);
		else return r2rmlColName;
	}
}
