/*******************************************************************************
 * Copyright 2014 University of Southern California
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

import java.util.Map;

import edu.isi.karma.rep.Node;

public class PopulatedTemplateTermSet {

	protected TemplateTermSet terms;
	protected Map<ColumnTemplateTerm, Node> references;
	protected String uri;
	
	public PopulatedTemplateTermSet(TemplateTermSet terms,  Map<ColumnTemplateTerm, Node> references, String uri)
	{
		this.terms = terms;
		this.references = references;
		this.uri = uri;
	}
	
	public TemplateTermSet getTerms()
	{
		return terms;
	}
	
	public String getURI()
	{
		return uri;
	}
	
	public  Map<ColumnTemplateTerm, Node> getReferences()
	{
		return references;
	}
}
