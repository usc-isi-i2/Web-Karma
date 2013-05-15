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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TemplateTermSet {
	
	private final List<TemplateTerm> termSet;
	
	public TemplateTermSet() {
		termSet = new LinkedList<TemplateTerm>();
	}
	
	public void addTemplateTermToSet(TemplateTerm term) {
		this.termSet.add(term);
	}
	
	public List<TemplateTerm> getAllTerms() {
		return this.termSet;
	}
	
	public List<ColumnTemplateTerm> getAllColumnNameTermElements() {
		List<ColumnTemplateTerm> cnList = new ArrayList<ColumnTemplateTerm>();
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
		return termSet.size() == 0;
	}
}