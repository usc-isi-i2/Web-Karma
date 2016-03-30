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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;

public class DoublyAnchoredTemplateTermSetPopulator extends
		SinglyAnchoredTemplateTermSetPopulatorPlan {

	public DoublyAnchoredTemplateTermSetPopulator(
			Map<ColumnTemplateTerm, HNodePath> termToPath,
			LinkedList<ColumnTemplateTerm> columnTerms,
			List<ColumnTemplateTerm> comparisonTerms, 
			SuperSelection sel) {
		super(termToPath, columnTerms, comparisonTerms, sel);
		
	}

	
	public List<PartiallyPopulatedTermSet> execute(Row topRow, PopulatedTemplateTermSet subject, PopulatedTemplateTermSet object)
	{
		
		if(columnTerms == null || columnTerms.isEmpty())
		{
			List<PartiallyPopulatedTermSet> predicates = new LinkedList<>();
			predicates.add(new PartiallyPopulatedTermSet());
			return predicates;
		}
		List<PartiallyPopulatedTermSet> independentResults = this.execute(topRow);
		
		
		List<List<PartiallyPopulatedTermSet>> toMerge = new LinkedList<>();
		if(!independentResults.isEmpty())
		{
			toMerge.add(independentResults);
		}
		executeForReference(topRow, toMerge, subject.getReferences());
		executeForReference(topRow, toMerge, object.getReferences());
		return TemplateTermSetPopulatorWorker.combinePartialResults(toMerge);
	
	}


	private void executeForReference(Row topRow,
			List<List<PartiallyPopulatedTermSet>> toMerge,
			Map<ColumnTemplateTerm, Node> references) {
		for(Entry<ColumnTemplateTerm, Node> reference : references.entrySet())
		{
			TemplateTermSetPopulatorPlan plan = nestedPlans.get(reference.getKey());
			if(plan != null)
			{
				toMerge.add(plan.executeComplicated(topRow, reference.getValue()));
			}
		}
	}
}
