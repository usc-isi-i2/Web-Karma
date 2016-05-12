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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.exception.HNodeNotFoundKarmaException;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.KR2RMLMappingColumnNameHNodeTranslator;
import edu.isi.karma.kr2rml.template.ColumnTemplateTerm;
import edu.isi.karma.kr2rml.template.DoublyAnchoredTemplateTermSetPopulator;
import edu.isi.karma.kr2rml.template.PartiallyPopulatedTermSet;
import edu.isi.karma.kr2rml.template.PopulatedTemplateTermSet;
import edu.isi.karma.kr2rml.template.SinglyAnchoredTemplateTermSetPopulatorPlan;
import edu.isi.karma.kr2rml.template.TemplateTermSet;
import edu.isi.karma.kr2rml.template.TemplateTermSetPopulator;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;

public abstract class PredicateObjectMappingPlan extends MapPlan {

	public PredicateObjectMappingPlan(KR2RMLMapping kr2rmlMapping,
			URIFormatter uriFormatter, RepFactory factory,
			KR2RMLMappingColumnNameHNodeTranslator translator, 
			SuperSelection sel) {
		super(kr2rmlMapping, uriFormatter, factory, translator, sel);
	}

	protected SinglyAnchoredTemplateTermSetPopulatorPlan complicatedPlan;
	protected DoublyAnchoredTemplateTermSetPopulator predicatePlan;
	protected Map<ColumnTemplateTerm, HNodePath> combinedSubjectObjectTermsToPaths ;
	protected TemplateTermSetPopulator objectTemplateTermSetPopulator;
	protected TemplateTermSetPopulator predicateTemplateTermSetPopulator;
	protected boolean isFlipped = false;
	protected boolean isLiteral;
	protected String literalTemplateValue;
	protected String literalLanguage;
	protected PredicateObjectMap pom; 
	
	protected void generateInternal(TemplateTermSet subjectMapTemplate,
			PredicateObjectMap pom,
			Map<ColumnTemplateTerm, HNodePath> subjectTermsToPaths)
			throws HNodeNotFoundKarmaException {
		
		this.pom = pom;
		combinedSubjectObjectTermsToPaths = new HashMap<>();
		combinedSubjectObjectTermsToPaths.putAll(subjectTermsToPaths);
		Map<ColumnTemplateTerm, HNodePath> objectTermsToPaths = new HashMap<>();
		
		populateTermsToPathForSubject(objectTermsToPaths, objectTemplateTermSetPopulator.getTerms());
		combinedSubjectObjectTermsToPaths.putAll(objectTermsToPaths);
		LinkedList<ColumnTemplateTerm> objectColumnTerms = new LinkedList<>();
		objectColumnTerms.addAll(objectTemplateTermSetPopulator.getTerms().getAllColumnNameTermElements());
		complicatedPlan = new SinglyAnchoredTemplateTermSetPopulatorPlan(combinedSubjectObjectTermsToPaths, objectColumnTerms, subjectMapTemplate.getAllColumnNameTermElements(), selection);
		generatePredicatesForPom(pom);
	}

	protected void generatePredicatesForPom(PredicateObjectMap pom) throws HNodeNotFoundKarmaException {
		this.pom = pom;
		List<ColumnTemplateTerm> subjectAndObjectTemplateTerms = new LinkedList<>();
		subjectAndObjectTemplateTerms.addAll(this.combinedSubjectObjectTermsToPaths.keySet());
		LinkedList<ColumnTemplateTerm> predicateColumnTemplateTerms = new LinkedList<>();
		predicateColumnTemplateTerms.addAll(pom.getPredicate().getTemplate().getAllColumnNameTermElements());
		predicateTemplateTermSetPopulator = new TemplateTermSetPopulator(pom.getPredicate().getTemplate(), new StringBuilder(), uriFormatter, true, true);
		Map<ColumnTemplateTerm, HNodePath> combinedSubjectObjectPredicateTermsToPaths = new HashMap<>();
		combinedSubjectObjectPredicateTermsToPaths.putAll(combinedSubjectObjectTermsToPaths);
		Map<ColumnTemplateTerm, HNodePath> predicateTermsToPaths = new HashMap<>();
		
		populateTermsToPathForSubject(predicateTermsToPaths, pom.getPredicate().getTemplate());
		combinedSubjectObjectTermsToPaths.putAll(predicateTermsToPaths);
		
		predicatePlan = new DoublyAnchoredTemplateTermSetPopulator(combinedSubjectObjectTermsToPaths, predicateColumnTemplateTerms, subjectAndObjectTemplateTerms, selection);
		
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
