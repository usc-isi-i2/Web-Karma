package edu.isi.karma.kr2rml;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Row;

public class TemplateTermSetPopulatorPlan {
	
	private Map<ColumnTemplateTerm, TemplateTermSetPopulatorWorker> independentWorkers = new HashMap<ColumnTemplateTerm, TemplateTermSetPopulatorWorker>();
	private Map<ColumnTemplateTerm, TemplateTermSetPopulatorWorker> workers = new HashMap<ColumnTemplateTerm, TemplateTermSetPopulatorWorker>();
	private Map<ColumnTemplateTerm, List<TemplateTermSetPopulatorWorker>> workerDependencyPlaceholder = new HashMap<ColumnTemplateTerm, List<TemplateTermSetPopulatorWorker>>();
	private Map<ColumnTemplateTerm, HNodePath> termToPath;
	private TemplateTermSetPopulatorWorker firstWorker;
	private LinkedList<ColumnTemplateTerm> columnTerms;
	private List<ColumnTemplateTerm> comparisonTerms;
	
	private static List<ColumnAffinity> affinities;
	{
		affinities = new LinkedList<ColumnAffinity>();
		affinities.add(RowColumnAffinity.INSTANCE);
		affinities.add(ParentRowColumnAffinity.INSTANCE);
		affinities.add(CommonParentRowColumnAffinity.INSTANCE);
	}	
	public TemplateTermSetPopulatorPlan(Map<ColumnTemplateTerm, HNodePath> termToPath)
	{
		this.termToPath = termToPath;
		this.columnTerms = new LinkedList<ColumnTemplateTerm>();
		this.comparisonTerms = columnTerms;
		this.columnTerms.addAll(termToPath.keySet());
		generate();
	}
			
	public TemplateTermSetPopulatorPlan(Map<ColumnTemplateTerm, HNodePath> termToPath,
			LinkedList<ColumnTemplateTerm> columnTerms,
			List<ColumnTemplateTerm> comparisonTerms)
	{
		this.comparisonTerms = comparisonTerms;
		this.columnTerms = columnTerms;
		this.termToPath = termToPath;
		generate();
	}
	
	private void generate()
	{
	
		while(!columnTerms.isEmpty())
		{
			
			ColumnTemplateTerm currentTerm = columnTerms.pop();
			ColumnAffinity closestAffinity = NoColumnAffinity.INSTANCE;
			ColumnTemplateTerm dependentTerm = null;
			for(ColumnTemplateTerm comparisonTerm : comparisonTerms)
			{
				ColumnAffinity affinity = findAffinity(currentTerm, comparisonTerm, termToPath);
				if(affinity.isCloserThan(closestAffinity))
				{
					closestAffinity = affinity;
					dependentTerm = comparisonTerm;
				}
			}
			
			generateWorker(currentTerm, dependentTerm);
			
		}
		findFirstWorker();
		
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
		if(dependentTerm == null) 
		{
			strategy = new MemoizedTemplateTermSetPopulatorStrategy(termToPath.get(currentTerm));
		}
		else
		{
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
			independentWorkers.put(currentTerm, worker);
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

	private void findFirstWorker() {
		firstWorker = null;
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
		return firstWorker.work(topRow); 
	}
	
	

}
