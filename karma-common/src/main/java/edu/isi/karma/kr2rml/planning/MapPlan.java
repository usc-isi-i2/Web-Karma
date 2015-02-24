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

import java.util.List;
import java.util.Map;

import edu.isi.karma.controller.command.selection.SuperSelection;
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
	protected SuperSelection selection;
	public MapPlan(KR2RMLMapping kr2rmlMapping, URIFormatter uriFormatter, 
			RepFactory factory, 
			KR2RMLMappingColumnNameHNodeTranslator translator, 
			SuperSelection sel)
	{
		this.factory = factory;
		this.uriFormatter = uriFormatter;
		this.kr2rmlMapping = kr2rmlMapping;
		this.translator = translator;
		this.selection = sel;
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
			String hNodeIdForColumnName = translator.getHNodeIdForColumnName(term.getTemplateTermValue());
			if(hNodeIdForColumnName != null) {
				HNodePath path = factory.getHNode(hNodeIdForColumnName).getHNodePath(factory);
				subjectTermsToPaths.put(term, path);
			}
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
