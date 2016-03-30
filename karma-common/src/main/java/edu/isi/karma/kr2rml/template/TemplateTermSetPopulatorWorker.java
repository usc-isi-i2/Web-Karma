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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;

public class TemplateTermSetPopulatorWorker {

	protected ColumnTemplateTerm term;
	protected HNodePath path;
	protected TemplateTermSetPopulatorStrategy strategy;
	protected List<TemplateTermSetPopulatorWorker> dependentWorkers;
	protected SuperSelection selection;
	public TemplateTermSetPopulatorWorker(	ColumnTemplateTerm term,
	HNodePath path,
	TemplateTermSetPopulatorStrategy strategy, 
	SuperSelection sel)
	{
		this.term =term;
		this.path = path;
		this.strategy = strategy;
		this.selection = sel;
		dependentWorkers = new LinkedList<>();
	}
	
	public void addDependentWorker(TemplateTermSetPopulatorWorker worker)
	{
		this.dependentWorkers.add(worker);
	}
	
	public List<PartiallyPopulatedTermSet> work(Row topRow)
	{
		//TODO is this necessary
		return work(topRow, topRow);
	}
	protected List<PartiallyPopulatedTermSet> work(Row topRow, Row currentRow)
	{
		List<PartiallyPopulatedTermSet> results = new LinkedList<>();
		Collection<Node> nodes = strategy.getNodes(topRow, currentRow, selection);
		
		for(Node n : nodes)
		{
			
			PartiallyPopulatedTermSet newTermSet = new PartiallyPopulatedTermSet(term, n);
			if(dependentWorkers.isEmpty())
			{
				results.add(newTermSet);
			}
			else
			{
				List<PartiallyPopulatedTermSet> combinedPartialResults = work(
						topRow, n);
				for(PartiallyPopulatedTermSet otherTermSet : combinedPartialResults)
				{
					results.add(PartiallyPopulatedTermSet.combine(newTermSet, otherTermSet));
				}
			}
		}
		return results;
	}

	protected List<PartiallyPopulatedTermSet> work(Row topRow, Node n) {
		List<List<PartiallyPopulatedTermSet>> partialResults = new LinkedList<>();
		for(TemplateTermSetPopulatorWorker dependentWorker: dependentWorkers)
		{
			partialResults.add(dependentWorker.work(topRow, n.getBelongsToRow()));
		}
		
		List<PartiallyPopulatedTermSet> combinedPartialResults = combinePartialResults(partialResults);
		return combinedPartialResults;
	}
	
	protected static List<PartiallyPopulatedTermSet> combinePartialResults(List<List<PartiallyPopulatedTermSet>> partialResults) {
		List<PartiallyPopulatedTermSet> combinedResults = new LinkedList<>();
		
		if(!partialResults.isEmpty())
		{
			if(partialResults.size() == 1)
			{
				combinedResults.addAll(partialResults.get(0));
				return combinedResults;
			}
			else if(partialResults.size() == 2)
			{
				List<PartiallyPopulatedTermSet> left = partialResults.get(0);
				List<PartiallyPopulatedTermSet> right = partialResults.get(1);
				combinePartialResults(combinedResults, left, right);
			}
			else
			{
				List<List<PartiallyPopulatedTermSet>> leftHalf = partialResults.subList(0, partialResults.size()/2);
				List<List<PartiallyPopulatedTermSet>> rightHalf = partialResults.subList(partialResults.size()/2, partialResults.size());
				List<PartiallyPopulatedTermSet> left = combinePartialResults(leftHalf);
				List<PartiallyPopulatedTermSet> right = combinePartialResults(rightHalf);
				combinePartialResults(combinedResults, left, right);
			}
		}
	
		return combinedResults;
	}

	private static void combinePartialResults(
			List<PartiallyPopulatedTermSet> combinedResults,
			List<PartiallyPopulatedTermSet> left,
			List<PartiallyPopulatedTermSet> right) {
		for(PartiallyPopulatedTermSet leftTermSet : left)
		{
			for(PartiallyPopulatedTermSet rightTermSet : right)
			{
				combinedResults.add(PartiallyPopulatedTermSet.combine(leftTermSet, rightTermSet));
			}
		}
	}
	
	
}
