package edu.isi.karma.kr2rml.template;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;

public class DoublyAnchoredTemplateTermSetPopulator extends
		SinglyAnchoredTemplateTermSetPopulatorPlan {

	public DoublyAnchoredTemplateTermSetPopulator(
			Map<ColumnTemplateTerm, HNodePath> termToPath,
			LinkedList<ColumnTemplateTerm> columnTerms,
			List<ColumnTemplateTerm> comparisonTerms) {
		super(termToPath, columnTerms, comparisonTerms);
		
	}

	
	public List<PartiallyPopulatedTermSet> execute(Row topRow, PopulatedTemplateTermSet subject, PopulatedTemplateTermSet object)
	{
		
		if(columnTerms == null || columnTerms.isEmpty())
		{
			List<PartiallyPopulatedTermSet> predicates = new LinkedList<PartiallyPopulatedTermSet>();
			predicates.add(new PartiallyPopulatedTermSet());
			return predicates;
		}
		List<PartiallyPopulatedTermSet> independentResults = this.execute(topRow);
		
		
		List<List<PartiallyPopulatedTermSet>> toMerge = new LinkedList<List<PartiallyPopulatedTermSet>>();
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
