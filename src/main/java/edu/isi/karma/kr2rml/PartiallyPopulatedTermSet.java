package edu.isi.karma.kr2rml;

import java.util.HashMap;
import java.util.Map;

import edu.isi.karma.rep.Node;

public class PartiallyPopulatedTermSet {

	Map<ColumnTemplateTerm, Node> termsToNode;
	
	PartiallyPopulatedTermSet()
	{
		termsToNode = new HashMap<ColumnTemplateTerm, Node>();
	}
	PartiallyPopulatedTermSet(ColumnTemplateTerm term, Node node)
	{
		termsToNode = new HashMap<ColumnTemplateTerm, Node>();
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
