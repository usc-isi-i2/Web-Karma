package edu.isi.karma.kr2rml;

import java.util.List;

import edu.isi.karma.rep.Node;

public class PopulatedTemplateTermSet {

	protected TemplateTermSet terms;
	protected List<Node> references;
	protected String uri;
	
	public PopulatedTemplateTermSet(TemplateTermSet terms, List<Node> references, String uri)
	{
		this.terms = terms;
		this.references = references;
		this.uri = uri;
	}
	
	public TemplateTermSet getTerms()
	{
		return terms;
	}
	
	public String getURI()
	{
		return uri;
	}
	
	public List<Node> getReferences()
	{
		return references;
	}
}
