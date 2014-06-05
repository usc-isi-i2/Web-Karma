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
import edu.isi.karma.kr2rml.template.SinglyAnchoredTemplateTermSetPopulatorPlan;
import edu.isi.karma.kr2rml.template.DoublyAnchoredTemplateTermSetPopulator;
import edu.isi.karma.kr2rml.template.PartiallyPopulatedTermSet;
import edu.isi.karma.kr2rml.template.PopulatedTemplateTermSet;
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

	protected SinglyAnchoredTemplateTermSetPopulatorPlan complicatedPlan;
	protected DoublyAnchoredTemplateTermSetPopulator predicatePlan;
	protected Map<ColumnTemplateTerm, HNodePath> combinedSubjectObjectTermsToPaths ;
	protected TemplateTermSetPopulator objectTemplateTermSetPopulator;
	protected TemplateTermSetPopulator predicateTemplateTermSetPopulator;
	protected boolean isFlipped = false;
	protected boolean isLiteral;
	protected String literalTemplateValue;
	protected PredicateObjectMap pom; 
	
	protected void generateInternal(TemplateTermSet subjectMapTemplate,
			PredicateObjectMap pom,
			Map<ColumnTemplateTerm, HNodePath> subjectTermsToPaths)
			throws HNodeNotFoundKarmaException {
		
		this.pom = pom;
		combinedSubjectObjectTermsToPaths = new HashMap<ColumnTemplateTerm, HNodePath>();
		combinedSubjectObjectTermsToPaths.putAll(subjectTermsToPaths);
		Map<ColumnTemplateTerm, HNodePath> objectTermsToPaths = new HashMap<ColumnTemplateTerm, HNodePath>();
		
		populateTermsToPathForSubject(objectTermsToPaths, objectTemplateTermSetPopulator.getTerms());
		combinedSubjectObjectTermsToPaths.putAll(objectTermsToPaths);
		LinkedList<ColumnTemplateTerm> objectColumnTerms = new LinkedList<ColumnTemplateTerm>();
		objectColumnTerms.addAll(objectTemplateTermSetPopulator.getTerms().getAllColumnNameTermElements());
		complicatedPlan = new SinglyAnchoredTemplateTermSetPopulatorPlan(combinedSubjectObjectTermsToPaths, objectColumnTerms, subjectMapTemplate.getAllColumnNameTermElements());
		generatePredicatesForPom(pom);
	}

	private void generatePredicatesForPom(PredicateObjectMap pom) throws HNodeNotFoundKarmaException {
		this.pom = pom;
		List<ColumnTemplateTerm> subjectAndObjectTemplateTerms = new LinkedList<ColumnTemplateTerm>();
		subjectAndObjectTemplateTerms.addAll(this.combinedSubjectObjectTermsToPaths.keySet());
		LinkedList<ColumnTemplateTerm> predicateColumnTemplateTerms = new LinkedList<ColumnTemplateTerm>();
		predicateColumnTemplateTerms.addAll(pom.getPredicate().getTemplate().getAllColumnNameTermElements());
		predicateTemplateTermSetPopulator = new TemplateTermSetPopulator(pom.getPredicate().getTemplate(), new StringBuilder(), uriFormatter, true, true);
		Map<ColumnTemplateTerm, HNodePath> combinedSubjectObjectPredicateTermsToPaths = new HashMap<ColumnTemplateTerm, HNodePath>();
		combinedSubjectObjectPredicateTermsToPaths.putAll(combinedSubjectObjectTermsToPaths);
		Map<ColumnTemplateTerm, HNodePath> predicateTermsToPaths = new HashMap<ColumnTemplateTerm, HNodePath>();
		
		populateTermsToPathForSubject(predicateTermsToPaths, pom.getPredicate().getTemplate());
		combinedSubjectObjectTermsToPaths.putAll(predicateTermsToPaths);
		
		predicatePlan = new DoublyAnchoredTemplateTermSetPopulator(combinedSubjectObjectTermsToPaths, predicateColumnTemplateTerms, subjectAndObjectTemplateTerms);
		
	}
	
	
	public Map<PopulatedTemplateTermSet, List<PartiallyPopulatedTermSet>> execute(Row r, List<PopulatedTemplateTermSet> subjects)
	{

		Map<PopulatedTemplateTermSet, List<PartiallyPopulatedTermSet>> subjectsToObjects = complicatedPlan.execute(r, subjects);
		return subjectsToObjects;
	}
	
	public void outputTriples(List<KR2RMLRDFWriter> outWriters, Map<PopulatedTemplateTermSet, List<PartiallyPopulatedTermSet>> subjectsToObjects, Row r)
	{
		for(Entry<PopulatedTemplateTermSet, List<PartiallyPopulatedTermSet>> subjectToObjects : subjectsToObjects.entrySet())
		{
			PopulatedTemplateTermSet subject = subjectToObjects.getKey();
			List<PopulatedTemplateTermSet> objects = objectTemplateTermSetPopulator.generatePopulatedTemplatesFromPartials( subjectToObjects.getValue());
			for(PopulatedTemplateTermSet object : objects )
			{
				List<PopulatedTemplateTermSet> predicates = predicateTemplateTermSetPopulator.generatePopulatedTemplatesFromPartials(predicatePlan.execute(r, subject, object));
				for(PopulatedTemplateTermSet predicate : predicates)
				{
					for(KR2RMLRDFWriter outWriter : outWriters)
					{
						outputTriple(outWriter, subject, predicate, object);
					}
				}
			}
		}
	}

	protected abstract void outputTriple(KR2RMLRDFWriter outWriter, PopulatedTemplateTermSet subject,
			PopulatedTemplateTermSet predicate, PopulatedTemplateTermSet object);
	
}
