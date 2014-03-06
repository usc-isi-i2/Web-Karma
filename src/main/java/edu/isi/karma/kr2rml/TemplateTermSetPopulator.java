package edu.isi.karma.kr2rml;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	
	
	public List<PopulatedTemplateTermSet> populate(Row topRow, TemplateTermSetPopulatorPlan plan)
	{
		List<PartiallyPopulatedTermSet> partials = plan.execute(topRow);
		
		return generatePopulatedTemplatesFromPartials(partials);
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
					if(n == null || n.getValue().isEmptyValue() || n.getValue().asString().trim().isEmpty())
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
