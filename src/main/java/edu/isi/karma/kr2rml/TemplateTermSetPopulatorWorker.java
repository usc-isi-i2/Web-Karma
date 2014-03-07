package edu.isi.karma.kr2rml;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;

public class TemplateTermSetPopulatorWorker {

	protected ColumnTemplateTerm term;
	protected HNodePath path;
	protected TemplateTermSetPopulatorStrategy strategy;
	protected List<TemplateTermSetPopulatorWorker> dependentWorkers;
	
	public TemplateTermSetPopulatorWorker(	ColumnTemplateTerm term,
	HNodePath path,
	TemplateTermSetPopulatorStrategy strategy)
	{
		this.term =term;
		this.path = path;
		this.strategy = strategy;
		dependentWorkers = new LinkedList<TemplateTermSetPopulatorWorker>();
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
		List<PartiallyPopulatedTermSet> results = new LinkedList<PartiallyPopulatedTermSet>();
		Collection<Node> nodes = strategy.getNodes(topRow, currentRow);
		
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
		List<List<PartiallyPopulatedTermSet>> partialResults = new LinkedList<List<PartiallyPopulatedTermSet>>();
		for(TemplateTermSetPopulatorWorker dependentWorker: dependentWorkers)
		{
			partialResults.add(dependentWorker.work(topRow, n.getBelongsToRow()));
		}
		
		List<PartiallyPopulatedTermSet> combinedPartialResults = combinePartialResults(partialResults);
		return combinedPartialResults;
	}
	
	protected static List<PartiallyPopulatedTermSet> combinePartialResults(List<List<PartiallyPopulatedTermSet>> partialResults) {
		List<PartiallyPopulatedTermSet> combinedResults = new LinkedList<PartiallyPopulatedTermSet>();
		
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
				List<List<PartiallyPopulatedTermSet>> rightHalf = partialResults.subList(partialResults.size()/2, partialResults.size()-1);
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
