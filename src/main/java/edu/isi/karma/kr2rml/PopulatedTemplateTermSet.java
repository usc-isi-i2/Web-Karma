package edu.isi.karma.kr2rml;

import java.util.Map;

import edu.isi.karma.rep.Node;

public class PopulatedTemplateTermSet {

	protected TemplateTermSet terms;
	protected Map<ColumnTemplateTerm, Node> references;
	protected String uri;
	
	public PopulatedTemplateTermSet(TemplateTermSet terms,  Map<ColumnTemplateTerm, Node> references, String uri)
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
	
	public  Map<ColumnTemplateTerm, Node> getReferences()
	{
		return references;
	}
}
