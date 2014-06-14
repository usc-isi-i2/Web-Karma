package edu.isi.karma.kr2rml.planning;

import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.exception.HNodeNotFoundKarmaException;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.KR2RMLMappingColumnNameHNodeTranslator;
import edu.isi.karma.kr2rml.template.ColumnTemplateTerm;
import edu.isi.karma.kr2rml.template.PopulatedTemplateTermSet;
import edu.isi.karma.kr2rml.template.TemplateTerm;
import edu.isi.karma.kr2rml.template.TemplateTermSet;
import edu.isi.karma.kr2rml.template.TemplateTermSetPopulator;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.RepFactory;

public class ColumnPredicateObjectMappingPlan extends
		PredicateObjectMappingPlan {

	private static Logger LOG = LoggerFactory.getLogger(ColumnPredicateObjectMappingPlan.class);
	protected Map<String, String> hNodeToContextUriMap;
	protected boolean generateContext;
	
	public ColumnPredicateObjectMappingPlan(TemplateTermSet subjectMapTemplate, PredicateObjectMap pom, Map<ColumnTemplateTerm, HNodePath> subjectTermsToPaths,KR2RMLMapping kr2rmlMapping, URIFormatter uriFormatter, RepFactory factory, KR2RMLMappingColumnNameHNodeTranslator translator, Map<String, String> hNodeToContextUriMap,  boolean generateContext) throws HNodeNotFoundKarmaException
	{
		super(kr2rmlMapping, uriFormatter, factory, translator);
		this.hNodeToContextUriMap = hNodeToContextUriMap;
		this.generateContext = generateContext;
		generateLiteral(subjectMapTemplate, pom, subjectTermsToPaths);
		
	}
	
	public void generateLiteral(TemplateTermSet subjectMapTemplate, PredicateObjectMap pom, Map<ColumnTemplateTerm, HNodePath> subjectTermsToPaths) throws HNodeNotFoundKarmaException
	{
		this.isLiteral = true;
		TemplateTermSet literalTemplate = pom.getObject().getRdfLiteralType();
		literalTemplateValue = null;
		if(literalTemplate != null)
		{
			literalTemplateValue = generateStringValueForTemplate(literalTemplate);
		}
		objectTemplateTermSetPopulator = new TemplateTermSetPopulator(pom.getObject().getTemplate(), new StringBuilder(), uriFormatter, false, true);
		generateInternal(subjectMapTemplate, pom, subjectTermsToPaths);
		if(generateContext && objectTemplateTermSetPopulator.getTerms().isSingleColumnTerm())
		{
			getColumnContextUri(translator.getHNodeIdForColumnName(objectTemplateTermSetPopulator.getTerms().getAllTerms().get(0).getTemplateTermValue()));
		}
	}

	private String generateStringValueForTemplate(
			TemplateTermSet objMapTemplate) {
		StringBuilder sb = new StringBuilder();
		for(TemplateTerm term : objMapTemplate.getAllTerms())
		{
			sb.append(term.getTemplateTermValue());
		}
		return sb.toString();
	}

	@Override
	protected void outputTriple(KR2RMLRDFWriter outWriter,
			PopulatedTemplateTermSet subject,
			PopulatedTemplateTermSet predicate, PopulatedTemplateTermSet object) {
		if(objectTemplateTermSetPopulator.getTerms().isSingleUriString())
		{
			outWriter.outputTripleWithURIObject(pom.getTriplesMap().getId(), subject.getURI(), pom.getId(), predicate.getURI(), uriFormatter.getExpandedAndNormalizedUri(object.getURI()));
		}
		else if(generateContext && objectTemplateTermSetPopulator.getTerms().isSingleColumnTerm())
		{
			try {
				outWriter.outputQuadWithLiteralObject(pom.getTriplesMap().getId(), subject.getURI(), pom.getId(), predicate.getURI(), object.getURI(), literalTemplateValue,getColumnContextUri(translator.getHNodeIdForColumnName(objectTemplateTermSetPopulator.getTerms().getAllTerms().get(0).getTemplateTermValue())));
			} catch (HNodeNotFoundKarmaException e) {
				LOG.error("No hnode found for context " +objectTemplateTermSetPopulator.getTerms().getAllTerms().get(0).getTemplateTermValue() + " " + e);
			}
		}
		else
		{
			outWriter.outputTripleWithLiteralObject(pom.getTriplesMap().getId(), subject.getURI(), pom.getId(), predicate.getURI(), object.getURI(), literalTemplateValue);
		}
		
	}

	protected String getColumnContextUri (String hNodeId) {
		
		if (hNodeToContextUriMap.containsKey(hNodeId))
			return hNodeToContextUriMap.get(hNodeId);
		else {
			String randomId = RandomStringUtils.randomAlphanumeric(10);
			String uri = Namespaces.KARMA_DEV + randomId + "_" + hNodeId;
			hNodeToContextUriMap.put(hNodeId, uri);
			return uri;
		}
	}
}
