package edu.isi.karma.kr2rml.planning;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import edu.isi.karma.kr2rml.template.SinglyAnchoredTemplateTermSetPopulatorPlan;
import edu.isi.karma.kr2rml.template.TemplateTermSet;
import edu.isi.karma.kr2rml.template.TemplateTermSetPopulator;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;

public class ConstantPredicateObjectMappingPlan extends
PredicateObjectMappingPlan {
	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void generateInternal(TemplateTermSet subjectMapTemplate,
			PredicateObjectMap pom,
			Map<ColumnTemplateTerm, HNodePath> subjectTermsToPaths)
			throws HNodeNotFoundKarmaException {
		
		this.pom = pom;
		combinedSubjectObjectTermsToPaths = new HashMap<ColumnTemplateTerm, HNodePath>();
		combinedSubjectObjectTermsToPaths.putAll(subjectTermsToPaths);
		
		generatePredicatesForPom(pom);
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
			outWriter.outputTripleWithLiteralObject(pom, subject.getURI(), predicate.getURI(), object.getURI(), literalTemplateValue);
		}
		
	}
}
