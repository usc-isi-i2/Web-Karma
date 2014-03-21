package edu.isi.karma.kr2rml.planning;

import java.util.List;
import java.util.Map;

import edu.isi.karma.kr2rml.SubjectMap;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.exception.HNodeNotFoundKarmaException;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.KR2RMLMappingColumnNameHNodeTranslator;
import edu.isi.karma.kr2rml.template.ColumnTemplateTerm;
import edu.isi.karma.kr2rml.template.StringTemplateTerm;
import edu.isi.karma.kr2rml.template.TemplateTermSet;
import edu.isi.karma.kr2rml.template.TemplateTermSetPopulator;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.RepFactory;

public abstract class MapPlan {
	
	protected KR2RMLMapping kr2rmlMapping;
	protected URIFormatter uriFormatter;
	protected RepFactory factory;
	protected KR2RMLMappingColumnNameHNodeTranslator translator;
	
	public MapPlan(KR2RMLMapping kr2rmlMapping, URIFormatter uriFormatter, RepFactory factory, KR2RMLMappingColumnNameHNodeTranslator translator)
	{
		this.factory = factory;
		this.uriFormatter = uriFormatter;
		this.kr2rmlMapping = kr2rmlMapping;
		this.translator = translator;
	}
	protected TemplateTermSetPopulator generateTemplateTermSetPopulatorForSubjectMap(SubjectMap subjMap) throws HNodeNotFoundKarmaException {
		TemplateTermSet subjMapTemplate = null;
		TemplateTermSetPopulator subjectMapTTSPopulator = null;
		if (subjMap.isBlankNode()) {
			List<String> columnsCovered  = kr2rmlMapping.getAuxInfo().getBlankNodesColumnCoverage().get(subjMap.getId());
			subjMapTemplate = generateSubjectMapTemplateForBlankNode(subjMap, columnsCovered);	
			subjectMapTTSPopulator = new TemplateTermSetPopulator(subjMapTemplate, new StringBuilder(), uriFormatter, true, false);
		} else {
			subjMapTemplate = subjMap.getTemplate();
			subjectMapTTSPopulator = new TemplateTermSetPopulator(subjMapTemplate, new StringBuilder(), uriFormatter);
		}
		return subjectMapTTSPopulator;
	}
	protected void populateTermsToPathForSubject(
			Map<ColumnTemplateTerm, HNodePath> subjectTermsToPaths,
			TemplateTermSet subjMapTemplate)
			throws HNodeNotFoundKarmaException {
		for(ColumnTemplateTerm term : subjMapTemplate.getAllColumnNameTermElements())
		{
			HNodePath path = factory.getHNode(translator.getHNodeIdForColumnName(term.getTemplateTermValue())).getHNodePath(factory);
			subjectTermsToPaths.put(term, path);
		}
	}
	protected TemplateTermSet generateSubjectMapTemplateForBlankNode(SubjectMap subjectMap, 
			List<String> columnsCovered) {
		TemplateTermSet subjectTerms = new TemplateTermSet();
		subjectTerms.addTemplateTermToSet(new StringTemplateTerm(Uris.BLANK_NODE_PREFIX));
		subjectTerms.addTemplateTermToSet(new StringTemplateTerm(kr2rmlMapping.getAuxInfo().getBlankNodesUriPrefixMap().get(subjectMap.getId()).replaceAll(":", "_")));
		for(String columnCovered : columnsCovered)
		{
			ColumnTemplateTerm term = new ColumnTemplateTerm(columnCovered);
			subjectTerms.addTemplateTermToSet(term);
		}
		return subjectTerms;
	}
	
	
}
