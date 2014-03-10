package edu.isi.karma.kr2rml.planning;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.isi.karma.kr2rml.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.exception.HNodeNotFoundKarmaException;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.KR2RMLMappingColumnNameHNodeTranslator;
import edu.isi.karma.kr2rml.template.ColumnTemplateTerm;
import edu.isi.karma.kr2rml.template.ComplicatedTemplateTermSetPopulatorPlan;
import edu.isi.karma.kr2rml.template.PartiallyPopulatedTermSet;
import edu.isi.karma.kr2rml.template.PopulatedTemplateTermSet;
import edu.isi.karma.kr2rml.template.TemplateTerm;
import edu.isi.karma.kr2rml.template.TemplateTermSet;
import edu.isi.karma.kr2rml.template.TemplateTermSetPopulator;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;

public abstract class PredicateObjectMappingPlan extends MapPlan {

	public PredicateObjectMappingPlan(KR2RMLMapping kr2rmlMapping,
			URIFormatter uriFormatter, RepFactory factory,
			KR2RMLMappingColumnNameHNodeTranslator translator) {
		super(kr2rmlMapping, uriFormatter, factory, translator);
	}

	protected List<PopulatedTemplateTermSet> predicates;
	protected ComplicatedTemplateTermSetPopulatorPlan complicatedPlan;
	protected Map<ColumnTemplateTerm, HNodePath> combinedSubjectObjectTermsToPaths ;
	protected TemplateTermSetPopulator objectTemplateTermSetPopulator;
	protected boolean isFlipped = false;
	protected boolean isLiteral;
	protected String literalTemplateValue;
	
	protected void generateInternal(TemplateTermSet subjectMapTemplate,
			PredicateObjectMap pom,
			Map<ColumnTemplateTerm, HNodePath> subjectTermsToPaths)
			throws HNodeNotFoundKarmaException {
		predicates = generatePredicatesForPom(pom);
		
		combinedSubjectObjectTermsToPaths = new HashMap<ColumnTemplateTerm, HNodePath>();
		combinedSubjectObjectTermsToPaths.putAll(subjectTermsToPaths);
		Map<ColumnTemplateTerm, HNodePath> objectTermsToPaths = new HashMap<ColumnTemplateTerm, HNodePath>();
		
		populateTermsToPathForSubject(objectTermsToPaths, objectTemplateTermSetPopulator.getTerms());
		combinedSubjectObjectTermsToPaths.putAll(objectTermsToPaths);
		LinkedList<ColumnTemplateTerm> objectColumnTerms = new LinkedList<ColumnTemplateTerm>();
		objectColumnTerms.addAll(objectTemplateTermSetPopulator.getTerms().getAllColumnNameTermElements());
		complicatedPlan = new ComplicatedTemplateTermSetPopulatorPlan(combinedSubjectObjectTermsToPaths, objectColumnTerms, subjectMapTemplate.getAllColumnNameTermElements());
	}

	private List<PopulatedTemplateTermSet> generatePredicatesForPom(PredicateObjectMap pom) {
		List<ColumnTemplateTerm> predicateTemplateTerms = pom.getPredicate().getTemplate().getAllColumnNameTermElements();
		LinkedList<TemplateTerm> allPredicateTemplateTerms = new LinkedList<TemplateTerm>();
		allPredicateTemplateTerms.addAll(pom.getPredicate().getTemplate().getAllTerms());
		List<PopulatedTemplateTermSet> predicates = new LinkedList<PopulatedTemplateTermSet>();
		if(predicateTemplateTerms == null || predicateTemplateTerms.isEmpty())
		{
			TemplateTermSetPopulator ttsPopulator = new TemplateTermSetPopulator(pom.getPredicate().getTemplate(), new StringBuilder(), uriFormatter);
			predicates = ttsPopulator.generatePopulatedTemplates(null);			
		}
		else
		{
			//dynamic predicates;
		}
		return predicates; 
	}
	
	
	public Map<PopulatedTemplateTermSet, List<PartiallyPopulatedTermSet>> execute(Row r, List<PopulatedTemplateTermSet> subjects)
	{

		Map<PopulatedTemplateTermSet, List<PartiallyPopulatedTermSet>> subjectsToObjects = complicatedPlan.execute(r, subjects);
		return subjectsToObjects;
	}
	
	public void outputTriples(KR2RMLRDFWriter outWriter, Map<PopulatedTemplateTermSet, List<PartiallyPopulatedTermSet>> subjectsToObjects)
	{
		for(Entry<PopulatedTemplateTermSet, List<PartiallyPopulatedTermSet>> subjectToObjects : subjectsToObjects.entrySet())
		{
			PopulatedTemplateTermSet subject = subjectToObjects.getKey();
			List<PopulatedTemplateTermSet> objects = objectTemplateTermSetPopulator.generatePopulatedTemplatesFromPartials( subjectToObjects.getValue());
			for(PopulatedTemplateTermSet object : objects )
			{
				for(PopulatedTemplateTermSet predicate : predicates)
				{
					outputTriple(outWriter, subject, predicate, object);	
				}
			}
		}
	}

	protected abstract void outputTriple(KR2RMLRDFWriter outWriter, PopulatedTemplateTermSet subject,
			PopulatedTemplateTermSet predicate, PopulatedTemplateTermSet object);
	
}
