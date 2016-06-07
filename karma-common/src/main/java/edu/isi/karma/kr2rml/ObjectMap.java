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

import edu.isi.karma.kr2rml.template.TemplateTermSet;

public class ObjectMap extends TermMap {
	
	private final RefObjectMap refObjectMap;
	private TemplateTermSet rdfLiteralType;
	private TemplateTermSet language;

//	public ObjectMap(String id, TemplateTermSet template) {
//		super(id);
//		this.template = template;
//		this.refObjectMap = null;
//		this.rdfLiteralType = null;
//	}
	
	public ObjectMap(String id, RefObjectMap refObjectMap) {
		super(id);
		this.refObjectMap = refObjectMap;
		this.template = null;
		this.rdfLiteralType = null;
		this.language = null;
	}

	public ObjectMap(String id, TemplateTermSet template, 
			TemplateTermSet rdfLiteralTye) {
		super(id);
		this.template = template;
		this.refObjectMap = null;
		this.rdfLiteralType = rdfLiteralTye;
		this.language = null;
	}
	
	public ObjectMap(String id, TemplateTermSet template, 
			TemplateTermSet rdfLiteralTye, TemplateTermSet language) {
		super(id);
		this.template = template;
		this.refObjectMap = null;
		this.rdfLiteralType = rdfLiteralTye;
		this.language = language;
	}

	@Override
	public String toString() {
		if (template != null)
			return "ObjectMap [\n" +
					"template=" + template + ",\n" +
					"rdfLiteralType=" + rdfLiteralType + ",\n" +
					"language=" + language + "]";
		else if (refObjectMap != null)
			return "RefObjectMap [" + refObjectMap.getParentTriplesMap().getId() + "]";
		else return "<No ObjectMap or RefObjectMap found for the ObjectMap!>";
	}
	
	public boolean hasRefObjectMap() {
		return refObjectMap != null;
	}

	public RefObjectMap getRefObjectMap() {
		return refObjectMap;
	}

	public TemplateTermSet getRdfLiteralType() {
		return rdfLiteralType;
	}
	
	public TemplateTermSet getLanguage() {
		return language;
	}
	
}
