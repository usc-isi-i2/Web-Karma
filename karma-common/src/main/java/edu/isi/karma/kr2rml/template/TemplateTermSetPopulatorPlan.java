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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.kr2rml.affinity.ColumnAffinity;
import edu.isi.karma.kr2rml.affinity.CommonParentRowColumnAffinity;
import edu.isi.karma.kr2rml.affinity.NoColumnAffinity;
import edu.isi.karma.kr2rml.affinity.ParentRowColumnAffinity;
import edu.isi.karma.kr2rml.affinity.RowColumnAffinity;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;

public class TemplateTermSetPopulatorPlan {
	
	protected Map<ColumnTemplateTerm, TemplateTermSetPopulatorWorker> independentWorkers = new HashMap<>();
	protected Map<ColumnTemplateTerm, TemplateTermSetPopulatorWorker> workers = new HashMap<>();
	protected Map<ColumnTemplateTerm, List<TemplateTermSetPopulatorWorker>> workerDependencyPlaceholder = new HashMap<>();
	protected Map<ColumnTemplateTerm, HNodePath> termToPath;
	protected TemplateTermSetPopulatorWorker firstWorker;
	protected LinkedList<ColumnTemplateTerm> columnTerms;
	protected List<ColumnTemplateTerm> comparisonTerms;
	protected SuperSelection selection;
	private static final List<ColumnAffinity> affinities;
	static {
		affinities = new LinkedList<>();
		affinities.add(RowColumnAffinity.INSTANCE);
		affinities.add(ParentRowColumnAffinity.INSTANCE);
		affinities.add(CommonParentRowColumnAffinity.INSTANCE);
	}	
	protected TemplateTermSetPopulatorPlan(SuperSelection sel)
	{
		this.selection = sel;
	}
	public TemplateTermSetPopulatorPlan(Map<ColumnTemplateTerm, HNodePath> termToPath, Collection<ColumnTemplateTerm> columnTerms, SuperSelection sel)
	{
		this.termToPath = termToPath;
		this.columnTerms = new LinkedList<>();
		this.comparisonTerms = this.columnTerms;
		this.columnTerms.addAll(columnTerms);
		this.firstWorker = null;
		this.selection = sel;
		generate();
	}
	public TemplateTermSetPopulatorPlan(Map<ColumnTemplateTerm, HNodePath> termToPath, Collection<ColumnTemplateTerm> columnTerms, TemplateTermSetPopulatorWorker firstWorker, SuperSelection sel)
	{
		this.termToPath = termToPath;
		this.columnTerms = new LinkedList<>();
		this.comparisonTerms = this.columnTerms;
		this.columnTerms.addAll(columnTerms);
		this.firstWorker = firstWorker;
		this.selection = sel;
		generate();
	}	

	public TemplateTermSetPopulatorPlan(
			Map<ColumnTemplateTerm, HNodePath> termToPath,
			LinkedList<ColumnTemplateTerm> columnTerms,
			List<ColumnTemplateTerm> comparisonTerms) {
		this.termToPath = termToPath;
		this.columnTerms = columnTerms;
		this.comparisonTerms = comparisonTerms;
		this.firstWorker = null;
		generate();
	}


	private void generate()
	{
		LinkedList<ColumnTemplateTerm> columnTermsLocal = new LinkedList<>();
		columnTermsLocal.addAll(columnTerms);
		sortColumnTermsByHNodePathDepth(columnTermsLocal);
		Map<ColumnTemplateTerm, ColumnTemplateTerm> termsToTermDependentOn = generateTermsToTermDependentOn(columnTermsLocal, comparisonTerms);
		
		generateWorkers(termsToTermDependentOn);
		findFirstWorker();
		
	}

	protected void sortColumnTermsByHNodePathDepth(LinkedList<ColumnTemplateTerm> columnTerms) {
		Collections.sort(columnTerms, new Comparator<ColumnTemplateTerm>(){

			@Override
			public int compare(ColumnTemplateTerm o1, ColumnTemplateTerm o2) {
				
				return -(o1.calculateColumnPathLength() - o2.calculateColumnPathLength());
			}
			
		});
	}

	protected void generateWorkers(
			Map<ColumnTemplateTerm, ColumnTemplateTerm> termsToTermDependentOn) {
		for(Entry<ColumnTemplateTerm, ColumnTemplateTerm> termToTermDependentOn : termsToTermDependentOn.entrySet())
		{
			ColumnTemplateTerm currentTerm = termToTermDependentOn.getKey();
			ColumnTemplateTerm dependentTerm = termToTermDependentOn.getValue();
			generateWorker(currentTerm, dependentTerm);
		}
	}

	protected Map<ColumnTemplateTerm, ColumnTemplateTerm> generateTermsToTermDependentOn(LinkedList<ColumnTemplateTerm> columnTermsLocal, List<ColumnTemplateTerm> comparisonTermsLocal) {
		Map<ColumnTemplateTerm, ColumnTemplateTerm> termsToTermDependentOn = new HashMap<>();
		while(!columnTermsLocal.isEmpty())
		{
			
			ColumnTemplateTerm currentTerm = columnTermsLocal.pop();
			ColumnAffinity closestAffinity = NoColumnAffinity.INSTANCE;
			ColumnTemplateTerm dependentTerm = null;
			for(ColumnTemplateTerm comparisonTerm : comparisonTermsLocal)
			{
				if(comparisonTerm == currentTerm)
				{
					continue;
				}
				ColumnAffinity affinity = findAffinity(currentTerm, comparisonTerm, termToPath);
				if(affinity.isCloserThan(closestAffinity) && !isTransitivelyDependentOn(termsToTermDependentOn, currentTerm, comparisonTerm))
				{
					closestAffinity = affinity;
					dependentTerm = comparisonTerm;
				}
			}
			termsToTermDependentOn.put(currentTerm, dependentTerm);

		}
		return termsToTermDependentOn;
	}
	
	private boolean isTransitivelyDependentOn(Map<ColumnTemplateTerm, ColumnTemplateTerm> termsToTermDependentOn, ColumnTemplateTerm currentTerm, ColumnTemplateTerm comparisonTerm)
	{
		if(!termsToTermDependentOn.containsKey(comparisonTerm))
		{
			return false;
		}
		else if(termsToTermDependentOn.get(comparisonTerm) == currentTerm)
		{
			return true;
		}
		else
		{
			return isTransitivelyDependentOn(termsToTermDependentOn, currentTerm, termsToTermDependentOn.get(comparisonTerm) );
		}
	}

	private TemplateTermSetPopulatorWorker generateWorker(
			ColumnTemplateTerm currentTerm,
			ColumnTemplateTerm dependentTerm) {
		TemplateTermSetPopulatorStrategy strategy = generateStrategy(
				currentTerm, dependentTerm);
		TemplateTermSetPopulatorWorker worker = new TemplateTermSetPopulatorWorker(currentTerm,termToPath.get(currentTerm), strategy, selection);
		if(workerDependencyPlaceholder.containsKey(currentTerm))
		{
			for(TemplateTermSetPopulatorWorker dependentWorker : workerDependencyPlaceholder.get(currentTerm)){
				worker.addDependentWorker(dependentWorker);
			}
		}
		workers.put(currentTerm, worker);
		manageDependencies(currentTerm, dependentTerm, worker);
		return worker;
	}

	private TemplateTermSetPopulatorStrategy generateStrategy(
			ColumnTemplateTerm currentTerm, ColumnTemplateTerm dependentTerm) {
		TemplateTermSetPopulatorStrategy strategy;
		if(dependentTerm == null && firstWorker == null) 
		{
			strategy = new MemoizedTemplateTermSetPopulatorStrategy(termToPath.get(currentTerm));
		}
		else
		{
			if(firstWorker!= null && dependentTerm == null)
				dependentTerm = firstWorker.term;
			strategy = new DynamicTemplateTermSetPopulatorStrategy(termToPath.get(currentTerm), termToPath.get(dependentTerm));
		}
		return strategy;
	}

	private void manageDependencies(ColumnTemplateTerm currentTerm,
			ColumnTemplateTerm dependentTerm,
			TemplateTermSetPopulatorWorker worker) {
		if(dependentTerm != null)
		{
			generateDependency(dependentTerm, worker);
		}
		else
		{
			if(firstWorker != null)
			{
				firstWorker.addDependentWorker(worker);
			}
			else
			{
				independentWorkers.put(currentTerm, worker);
			}
		}
	}

	private void generateDependency(ColumnTemplateTerm dependentTerm,
			TemplateTermSetPopulatorWorker worker) {
		TemplateTermSetPopulatorWorker dependentOnWorker;
		dependentOnWorker = workers.get(dependentTerm);
		if(dependentOnWorker != null)
		{
			dependentOnWorker.addDependentWorker(worker);
		}
		else
		{
			addDependencyPlaceholder(dependentTerm, worker);
			
		}
	}
	
	private void addDependencyPlaceholder(ColumnTemplateTerm dependentTerm,
			TemplateTermSetPopulatorWorker worker) {
		if(!workerDependencyPlaceholder.containsKey(dependentTerm))
		{
			workerDependencyPlaceholder.put(dependentTerm, new LinkedList<TemplateTermSetPopulatorWorker>());
		}
		List<TemplateTermSetPopulatorWorker> dependencyPlaceholder = workerDependencyPlaceholder.get(dependentTerm);
		dependencyPlaceholder.add(worker);
	}

	protected void findFirstWorker() {
		if(firstWorker != null)
		{
			return;
		}
		
		TemplateTermSetPopulatorWorker previousWorker = null; 
		for(TemplateTermSetPopulatorWorker worker : independentWorkers.values())
		{
			if(firstWorker == null)
			{
				firstWorker = worker;
			}
			if(previousWorker != null)
			{
				previousWorker.addDependentWorker(worker);
			}
			previousWorker = worker;
		}
	}
	
	private ColumnAffinity findAffinity(ColumnTemplateTerm currentTerm,
			ColumnTemplateTerm comparisonTerm, Map<ColumnTemplateTerm, HNodePath> termToPath) {
		ColumnAffinity closestAffinity = NoColumnAffinity.INSTANCE;
		for(ColumnAffinity affinity : affinities)
		{
			HNodePath currentPath = termToPath.get(currentTerm);
			HNodePath comparisonPath= termToPath.get(comparisonTerm);
			if(affinity.isValidFor(currentPath, comparisonPath))
			{
				ColumnAffinity generatedAffinity = affinity.generateAffinity(currentPath, comparisonPath);
				if(generatedAffinity.isCloserThan(closestAffinity))
				{
					closestAffinity = generatedAffinity;
				}
			}
		}
		return closestAffinity;
	}
	
	public List<PartiallyPopulatedTermSet> execute(Row topRow)
	{
		if(columnTerms == null || columnTerms.isEmpty())
		{
			List<PartiallyPopulatedTermSet> predicates = new LinkedList<>();
			predicates.add(new PartiallyPopulatedTermSet());
			return predicates;
		}
		if(firstWorker != null)
		{
			return firstWorker.work(topRow);
		}
		return new LinkedList<>();
	}
	public List<PartiallyPopulatedTermSet> executeComplicated(Row topRow, Node value) {
		return firstWorker.work(topRow, value);
		
	}
	
	

}
