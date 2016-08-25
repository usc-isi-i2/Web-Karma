/*******************************************************************************
 * Copyright 2014 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.kr2rml.planning;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.selection.SuperSelection;
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
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.RepFactory;

public class ColumnPredicateObjectMappingPlan extends
		PredicateObjectMappingPlan {

	private static Logger LOG = LoggerFactory.getLogger(ColumnPredicateObjectMappingPlan.class);
	protected Map<String, String> hNodeToContextUriMap;
	protected boolean generateContext;
	
	public ColumnPredicateObjectMappingPlan(TemplateTermSet subjectMapTemplate, 
			PredicateObjectMap pom, 
			Map<ColumnTemplateTerm, HNodePath> subjectTermsToPaths, 
			KR2RMLMapping kr2rmlMapping, URIFormatter uriFormatter, 
			RepFactory factory, KR2RMLMappingColumnNameHNodeTranslator translator, 
			Map<String, String> hNodeToContextUriMap,  boolean generateContext, 
			SuperSelection sel) throws HNodeNotFoundKarmaException
	{
		super(kr2rmlMapping, uriFormatter, factory, translator, sel);
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
			literalTemplateValue = generateStringValueForTemplate(literalTemplate);
		
		TemplateTermSet languageTemplate = pom.getObject().getLanguage();
		literalLanguage = null;
		if(languageTemplate != null)
			literalLanguage = generateStringValueForTemplate(languageTemplate);
		
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
		if(objectTemplateTermSetPopulator.getTerms().isSingleUriString() || predicate.getURI().equals("<" + Uris.RDF_TYPE_URI + ">"))
		{
			outWriter.outputTripleWithURIObject(pom, subject.getURI(), predicate.getURI(), uriFormatter.getExpandedAndNormalizedUri(object.getURI()));
		}
		else if(generateContext && objectTemplateTermSetPopulator.getTerms().isSingleColumnTerm())
		{
			try {
				outWriter.outputQuadWithLiteralObject(pom, subject.getURI(), predicate.getURI(), object.getURI(), 
						literalTemplateValue, literalLanguage,
						getColumnContextUri(translator.getHNodeIdForColumnName(objectTemplateTermSetPopulator.getTerms().getAllTerms().get(0).getTemplateTermValue())));
			} catch (HNodeNotFoundKarmaException e) {
				LOG.error("No hnode found for context " +objectTemplateTermSetPopulator.getTerms().getAllTerms().get(0).getTemplateTermValue() + " " + e);
			}
		}
		else
		{
			outWriter.outputTripleWithLiteralObject(pom, subject.getURI(), predicate.getURI(), object.getURI(), literalTemplateValue, literalLanguage);
		}
		
	}

	protected String getColumnContextUri (String hNodeId) {
		if(hNodeId != null) {
			if (hNodeToContextUriMap.containsKey(hNodeId))
				return hNodeToContextUriMap.get(hNodeId);
			else {
				String randomId = UUID.randomUUID().toString();
				String uri = Namespaces.KARMA_DEV + randomId + "_" + hNodeId;
				hNodeToContextUriMap.put(hNodeId, uri);
				return uri;
			}
		}
		return null;
	}
}
