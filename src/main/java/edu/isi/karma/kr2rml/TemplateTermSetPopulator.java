package edu.isi.karma.kr2rml;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.isi.karma.rep.Node;

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
