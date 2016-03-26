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

import java.util.HashMap;
import java.util.Map;

import edu.isi.karma.rep.Node;

public class PartiallyPopulatedTermSet {

	Map<ColumnTemplateTerm, Node> termsToNode;
	
	PartiallyPopulatedTermSet()
	{
		termsToNode = new HashMap<>();
	}
	PartiallyPopulatedTermSet(ColumnTemplateTerm term, Node node)
	{
		termsToNode = new HashMap<>();
		termsToNode.put(term, node);
	}
	
	private Map<ColumnTemplateTerm, Node> getTermsToNode()
	{
		return termsToNode;
	}
	public static PartiallyPopulatedTermSet combine(PartiallyPopulatedTermSet a, PartiallyPopulatedTermSet b)
	{
		PartiallyPopulatedTermSet newSet = new PartiallyPopulatedTermSet();
		newSet.addTerms(a.getTermsToNode());
		newSet.addTerms(b.getTermsToNode());
		return newSet;
	}
	private void addTerms(Map<ColumnTemplateTerm, Node> termsToNode2) {
		termsToNode.putAll(termsToNode2);
		
	}
	public Node getValue(ColumnTemplateTerm term) {
		return termsToNode.get(term);
	}
}
