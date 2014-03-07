package edu.isi.karma.kr2rml;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;

public class TemplateTermSetPopulatorPlan {
	
	protected Map<ColumnTemplateTerm, TemplateTermSetPopulatorWorker> independentWorkers = new HashMap<ColumnTemplateTerm, TemplateTermSetPopulatorWorker>();
	protected Map<ColumnTemplateTerm, TemplateTermSetPopulatorWorker> workers = new HashMap<ColumnTemplateTerm, TemplateTermSetPopulatorWorker>();
	protected Map<ColumnTemplateTerm, List<TemplateTermSetPopulatorWorker>> workerDependencyPlaceholder = new HashMap<ColumnTemplateTerm, List<TemplateTermSetPopulatorWorker>>();
	protected Map<ColumnTemplateTerm, HNodePath> termToPath;
	protected TemplateTermSetPopulatorWorker firstWorker;
	protected LinkedList<ColumnTemplateTerm> columnTerms;
	protected List<ColumnTemplateTerm> comparisonTerms;
	
	private static List<ColumnAffinity> affinities;
	{
		affinities = new LinkedList<ColumnAffinity>();
		affinities.add(RowColumnAffinity.INSTANCE);
		affinities.add(ParentRowColumnAffinity.INSTANCE);
		affinities.add(CommonParentRowColumnAffinity.INSTANCE);
	}	
	protected TemplateTermSetPopulatorPlan()
	{
		
	}
	public TemplateTermSetPopulatorPlan(Map<ColumnTemplateTerm, HNodePath> termToPath, Collection<ColumnTemplateTerm> columnTerms)
	{
		this.termToPath = termToPath;
		this.columnTerms = new LinkedList<ColumnTemplateTerm>();
		this.comparisonTerms = this.columnTerms;
		this.columnTerms.addAll(columnTerms);
		this.firstWorker = null;
		generate();
	}
	public TemplateTermSetPopulatorPlan(Map<ColumnTemplateTerm, HNodePath> termToPath, Collection<ColumnTemplateTerm> columnTerms, TemplateTermSetPopulatorWorker firstWorker)
	{
		this.termToPath = termToPath;
		this.columnTerms = new LinkedList<ColumnTemplateTerm>();
		this.comparisonTerms = this.columnTerms;
		this.columnTerms.addAll(columnTerms);
		this.firstWorker = firstWorker;
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
		LinkedList<ColumnTemplateTerm> columnTermsLocal = new LinkedList<ColumnTemplateTerm>();
		columnTermsLocal.addAll(columnTerms);
		sortColumnTermsByHNodePathDepth(columnTermsLocal);
		Map<ColumnTemplateTerm, ColumnTemplateTerm> termsToTermDependentOn = generateTermsToTermDependentOn(columnTermsLocal, columnTermsLocal);
		
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
		Map<ColumnTemplateTerm, ColumnTemplateTerm> termsToTermDependentOn = new HashMap<ColumnTemplateTerm, ColumnTemplateTerm>();
		while(!columnTermsLocal.isEmpty())
		{
			
			ColumnTemplateTerm currentTerm = columnTermsLocal.pop();
			ColumnAffinity closestAffinity = NoColumnAffinity.INSTANCE;
			ColumnTemplateTerm dependentTerm = null;
			for(ColumnTemplateTerm comparisonTerm : comparisonTermsLocal)
			{
				ColumnAffinity affinity = findAffinity(currentTerm, comparisonTerm, termToPath);
				if(affinity.isCloserThan(closestAffinity))
				{
					closestAffinity = affinity;
					dependentTerm = comparisonTerm;
				}
			}
			termsToTermDependentOn.put(currentTerm, dependentTerm);

		}
		return termsToTermDependentOn;
	}

	private TemplateTermSetPopulatorWorker generateWorker(
			ColumnTemplateTerm currentTerm,
			ColumnTemplateTerm dependentTerm) {
		TemplateTermSetPopulatorStrategy strategy = generateStrategy(
				currentTerm, dependentTerm);
		TemplateTermSetPopulatorWorker worker = new TemplateTermSetPopulatorWorker(currentTerm,termToPath.get(currentTerm), strategy);
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
		TemplateTermSetPopulatorStrategy strategy = null;
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
		TemplateTermSetPopulatorWorker dependentOnWorker = null;
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
		if(firstWorker != null)
		{
			return firstWorker.work(topRow);
		}
		return new LinkedList<PartiallyPopulatedTermSet>();
	}
	public List<PartiallyPopulatedTermSet> executeComplicated(Row topRow, Node value) {
		return firstWorker.work(topRow, value);
		
	}
	
	

}
