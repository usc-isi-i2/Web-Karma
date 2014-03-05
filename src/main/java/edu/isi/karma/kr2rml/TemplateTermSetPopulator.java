package edu.isi.karma.kr2rml;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;

public class TemplateTermSetPopulator {
	
	protected TemplateTermSet originalTerms;
	private boolean URIify;
	private boolean useNodeValue;
	private StringBuilder baseTemplate;
	private URIFormatter formatter;
	
	public TemplateTermSetPopulator(TemplateTermSet originalTerms, StringBuilder baseTemplate, URIFormatter formatter)
	{
		configure(originalTerms, baseTemplate, formatter);
		this.URIify = true;
		this.useNodeValue = true;
	}
	
	public TemplateTermSetPopulator(TemplateTermSet originalTerms, StringBuilder baseTemplate, URIFormatter formatter, boolean URIify, boolean useNodeValue)
	{
		configure(originalTerms, baseTemplate, formatter);
		this.URIify = URIify;
		this.useNodeValue = useNodeValue;
	}
	
	private void configure(TemplateTermSet originalTerms, StringBuilder baseTemplate, URIFormatter formatter)
	{
		this.originalTerms = originalTerms;
		this.baseTemplate = baseTemplate;
		this.formatter = formatter;
	}
	
	
	public List<PopulatedTemplateTermSet> populate(Row topRow, Map<ColumnTemplateTerm, HNodePath> termToPath)
	{
		//find column templates at the same level
		//find nested column templates
		//find column templates in different paths.
		
		
		//TODO populate these
		
		LinkedList<ColumnTemplateTerm> columnTerms = new LinkedList<ColumnTemplateTerm>();
		LinkedList<HNodePath> paths = new LinkedList<HNodePath>();
		columnTerms.addAll(termToPath.keySet());
		paths.addAll(termToPath.values());
		
		//Start with the deepest nodes;
		Collections.sort(paths, new Comparator<HNodePath>(){

			@Override
			public int compare(HNodePath o1, HNodePath o2) {
				int pathLengthDifference =  o1.length() - o2.length();
				return pathLengthDifference;
			}
	
		});
		Map<ColumnTemplateTerm, TemplateTermSetPopulatorWorker> independentWorkers = new HashMap<ColumnTemplateTerm, TemplateTermSetPopulatorWorker>();
		Map<ColumnTemplateTerm, TemplateTermSetPopulatorWorker> workers = new HashMap<ColumnTemplateTerm, TemplateTermSetPopulatorWorker>();
		
		Map<ColumnTemplateTerm, List<TemplateTermSetPopulatorWorker>> workerDependencyPlaceholder = new HashMap<ColumnTemplateTerm, List<TemplateTermSetPopulatorWorker>>();
		while(!columnTerms.isEmpty())
		{
			
			ColumnTemplateTerm currentTerm = columnTerms.pop();
			ColumnAffinity closestAffinity = NoColumnAffinity.INSTANCE;
			ColumnTemplateTerm dependentTerm = null;
			for(ColumnTemplateTerm comparisonTerm : columnTerms)
			{
				ColumnAffinity affinity = findAffinity(currentTerm, comparisonTerm, termToPath);
				if(affinity.isCloserThan(closestAffinity))
				{
					closestAffinity = affinity;
					dependentTerm = comparisonTerm;
				}
			}
			if(closestAffinity == NoColumnAffinity.INSTANCE || dependentTerm == null)
			{
				TemplateTermSetPopulatorWorker worker = new TemplateTermSetPopulatorWorker(currentTerm,termToPath.get(currentTerm), new MemoizedTemplateTermSetPopulatorStrategy(termToPath.get(currentTerm)));
				if(workerDependencyPlaceholder.containsKey(currentTerm))
				{
					for(TemplateTermSetPopulatorWorker dependentWorker : workerDependencyPlaceholder.get(currentTerm)){
						worker.addDependentWorker(dependentWorker);
					}
				}
				independentWorkers.put(currentTerm, worker);
				workers.put(currentTerm, worker);
			}
			else
			{
				TemplateTermSetPopulatorWorker dependentOnWorker = null;
				dependentOnWorker = workers.get(dependentTerm);
				if(dependentOnWorker != null)
				{
					TemplateTermSetPopulatorWorker worker = new TemplateTermSetPopulatorWorker(currentTerm,termToPath.get(currentTerm), new DynamicTemplateTermSetPopulatorStrategy(termToPath.get(currentTerm), dependentOnWorker.path));
					if(workerDependencyPlaceholder.containsKey(currentTerm))
					{
						for(TemplateTermSetPopulatorWorker dependentWorker : workerDependencyPlaceholder.get(currentTerm)){
							worker.addDependentWorker(dependentWorker);
						}
					}
					dependentOnWorker.addDependentWorker(worker);
					workers.put(currentTerm, worker);
				}
				else
				{
					if(!workerDependencyPlaceholder.containsKey(dependentTerm))
					{
						workerDependencyPlaceholder.put(dependentTerm, new LinkedList<TemplateTermSetPopulatorWorker>());
					}
					TemplateTermSetPopulatorWorker worker = new TemplateTermSetPopulatorWorker(currentTerm,termToPath.get(currentTerm), new DynamicTemplateTermSetPopulatorStrategy(termToPath.get(currentTerm), termToPath.get(dependentTerm)));
					if(workerDependencyPlaceholder.containsKey(currentTerm))
					{
						for(TemplateTermSetPopulatorWorker dependentWorker : workerDependencyPlaceholder.get(currentTerm)){
							worker.addDependentWorker(dependentWorker);
						}
					}
					List<TemplateTermSetPopulatorWorker> dependencyPlaceholder = workerDependencyPlaceholder.get(dependentTerm);
					dependencyPlaceholder.add(worker);
					workers.put(currentTerm, worker);
				}
				
				
			}
		}
		TemplateTermSetPopulatorWorker firstWorker = null;
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
		if(firstWorker == null)
		{
			System.out.println("we gotta problem");
		}
		List<PartiallyPopulatedTermSet> partials = firstWorker.work(topRow);
		
		return generatePopulatedTemplatesFromPartials(partials);
	}
	
	private static List<ColumnAffinity> affinities;
	{
		affinities = new LinkedList<ColumnAffinity>();
		affinities.add(RowColumnAffinity.INSTANCE);
		affinities.add(ParentRowColumnAffinity.INSTANCE);
		affinities.add(CommonParentRowColumnAffinity.INSTANCE);
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

	public List<PopulatedTemplateTermSet> generatePopulatedTemplatesFromPartials(List<PartiallyPopulatedTermSet> partials)
	{
		return generatePopulatedTemplates(partials, baseTemplate, originalTerms.getAllTerms());
	}
	protected List<PopulatedTemplateTermSet> generatePopulatedTemplates(List<PartiallyPopulatedTermSet> partials, StringBuilder output,
			List<TemplateTerm> terms) {
		List<PopulatedTemplateTermSet> templates = new LinkedList<PopulatedTemplateTermSet>();
		
		for(PartiallyPopulatedTermSet partial : partials)
		{
			StringBuilder uri = new StringBuilder();
			List<Node> references = new LinkedList<Node>();
			boolean termsSatisifed = true;
			for(TemplateTerm term : terms)
			{
				if(term instanceof ColumnTemplateTerm)
				{
					Node n = partial.getValue((ColumnTemplateTerm)term);
					if(n == null)
					{
						termsSatisifed = false;
						break;
					}
					references.add(n);
					if(useNodeValue)
					{
						uri.append(n.getValue().asString());
					}
					else
					{
						uri.append("_");
						uri.append(n.getId());
					}
				}
				else
				{
					uri.append(term.getTemplateTermValue());
				}
			}
			if(termsSatisifed)
			{
			String value = uri.toString();
			if(URIify)
			{
				value = formatter.getExpandedAndNormalizedUri(value);
			}
			templates.add(new PopulatedTemplateTermSet(originalTerms, references, value));
			}
			
		}
		return templates;
	}
	public List<PopulatedTemplateTermSet> generatePopulatedTemplates(Map<ColumnTemplateTerm, Collection<Node>> columnsToNodes)
	{
		return generateSubjectsForTemplates(columnsToNodes, baseTemplate, originalTerms.getAllTerms(),new LinkedList<Node>());
	}
	protected List<PopulatedTemplateTermSet> generateSubjectsForTemplates(Map<ColumnTemplateTerm, Collection<Node>> columnsToNodes, StringBuilder output,
			List<TemplateTerm> terms, List<Node> references) {
		List<PopulatedTemplateTermSet> subjects = new LinkedList<PopulatedTemplateTermSet>();
		
		if(!terms.isEmpty())
		{
			List<TemplateTerm> tempTerms = new LinkedList<TemplateTerm>();
			tempTerms.addAll(terms);
			TemplateTerm term = tempTerms.remove(0);
			boolean recurse = false;
			if(!tempTerms.isEmpty())
			{
				recurse = true;
			}
				
				if(term instanceof ColumnTemplateTerm)
				{
					for(Node node : columnsToNodes.get(term))
					{
						if(node.getValue().isEmptyValue() || node.getValue().asString().trim().isEmpty())
						{
							continue;
						}
						StringBuilder newPrefix = new StringBuilder(output);
						if(useNodeValue)
						{
							newPrefix.append(node.getValue().asString());
						}
						else
						{
							newPrefix.append("_");
							newPrefix.append(node.getId());
						}

						List<Node> newReferences = new LinkedList<Node>();
						newReferences.addAll(references);
						newReferences.add(node);
						if(recurse)
						{
							subjects.addAll(generateSubjectsForTemplates(columnsToNodes, newPrefix, tempTerms, newReferences));
						}
						else
						{
							String value = newPrefix.toString();
							if(URIify)
							{
								value = formatter.getExpandedAndNormalizedUri(value);
							}
							subjects.add(new PopulatedTemplateTermSet(originalTerms, newReferences, value));
						}
					}
				}
				else
				{
					StringBuilder newPrefix = new StringBuilder(output);
					newPrefix.append(term.getTemplateTermValue());
					if(recurse)
					{
						subjects.addAll(generateSubjectsForTemplates(columnsToNodes, newPrefix, tempTerms, references));
					}
					else
					{
						String value = newPrefix.toString();
						if(URIify)
						{
							value = formatter.getExpandedAndNormalizedUri(value);
						}
						subjects.add(new PopulatedTemplateTermSet(originalTerms, references, value));
					}
				}
			
		}
	
		return subjects;
	}
}
