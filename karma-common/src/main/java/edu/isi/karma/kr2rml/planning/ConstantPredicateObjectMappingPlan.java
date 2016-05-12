package edu.isi.karma.kr2rml.planning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.exception.HNodeNotFoundKarmaException;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.KR2RMLMappingColumnNameHNodeTranslator;
import edu.isi.karma.kr2rml.template.ColumnTemplateTerm;
import edu.isi.karma.kr2rml.template.ConstantTemplateTermSetPopulatorPlan;
import edu.isi.karma.kr2rml.template.PartiallyPopulatedTermSet;
import edu.isi.karma.kr2rml.template.PopulatedTemplateTermSet;
import edu.isi.karma.kr2rml.template.TemplateTerm;
import edu.isi.karma.kr2rml.template.TemplateTermSet;
import edu.isi.karma.kr2rml.template.TemplateTermSetPopulator;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;

public class ConstantPredicateObjectMappingPlan extends
PredicateObjectMappingPlan {
	
	private static Logger LOG = LoggerFactory.getLogger(ConstantPredicateObjectMappingPlan.class);
	public ConstantPredicateObjectMappingPlan(TemplateTermSet subjectMapTemplate,PredicateObjectMap pom, KR2RMLMapping kr2rmlMapping,Map<ColumnTemplateTerm, HNodePath> subjectTermsToPaths,
			URIFormatter uriFormatter, RepFactory factory,
			KR2RMLMappingColumnNameHNodeTranslator translator,
			SuperSelection sel) {
		super(kr2rmlMapping, uriFormatter, factory, translator, sel);
		this.pom = pom;
		objectTemplateTermSetPopulator = new TemplateTermSetPopulator(pom.getObject().getTemplate(), new StringBuilder(), uriFormatter, false, true);
		try {
			generateInternal(subjectMapTemplate, pom, subjectTermsToPaths);
		} catch (HNodeNotFoundKarmaException e) {
			LOG.error("Unable to generate plan!", e);
		}
	}
	
	protected void generateInternal(TemplateTermSet subjectMapTemplate,
			PredicateObjectMap pom,
			Map<ColumnTemplateTerm, HNodePath> subjectTermsToPaths)
			throws HNodeNotFoundKarmaException {
		
		this.pom = pom;
		combinedSubjectObjectTermsToPaths = new HashMap<>();
		combinedSubjectObjectTermsToPaths.putAll(subjectTermsToPaths);
		this.isLiteral = true;
		TemplateTermSet literalTemplate = pom.getObject().getRdfLiteralType();
		literalTemplateValue = null;
		if(literalTemplate != null)
			literalTemplateValue = generateStringValueForTemplate(literalTemplate);
		
		TemplateTermSet languageTemplate = pom.getObject().getLanguage();
		literalLanguage = null;
		if(languageTemplate != null)
			literalLanguage = generateStringValueForTemplate(languageTemplate);
		
		generatePredicatesForPom(pom);
	}
	
	protected String generateStringValueForTemplate(
			TemplateTermSet objMapTemplate) {
		StringBuilder sb = new StringBuilder();
		for(TemplateTerm term : objMapTemplate.getAllTerms())
		{
			sb.append(term.getTemplateTermValue());
		}
		return sb.toString();
	}

	
	@Override
	public Map<PopulatedTemplateTermSet, List<PartiallyPopulatedTermSet>> execute(Row r, List<PopulatedTemplateTermSet> subjects)
	{

		Map<PopulatedTemplateTermSet, List<PartiallyPopulatedTermSet>> subjectsToObjects = new ConstantTemplateTermSetPopulatorPlan(this.selection).execute(r, subjects);
		return subjectsToObjects;
	}

	@Override
	protected void outputTriple(KR2RMLRDFWriter outWriter,
			PopulatedTemplateTermSet subject,
			PopulatedTemplateTermSet predicate, PopulatedTemplateTermSet object) {
		
		if(objectTemplateTermSetPopulator.getTerms().isSingleUriString())
		{
			outWriter.outputTripleWithURIObject(pom, subject.getURI(), predicate.getURI(), uriFormatter.getExpandedAndNormalizedUri(object.getURI()));
		}
		else
		{
			outWriter.outputTripleWithLiteralObject(pom, subject.getURI(), predicate.getURI(), object.getURI(), literalTemplateValue, literalLanguage);
		}
		
	}
}
