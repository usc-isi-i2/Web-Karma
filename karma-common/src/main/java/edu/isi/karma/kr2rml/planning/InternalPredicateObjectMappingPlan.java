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

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.exception.HNodeNotFoundKarmaException;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.KR2RMLMappingColumnNameHNodeTranslator;
import edu.isi.karma.kr2rml.template.ColumnTemplateTerm;
import edu.isi.karma.kr2rml.template.PopulatedTemplateTermSet;
import edu.isi.karma.kr2rml.template.TemplateTermSet;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.RepFactory;

public class InternalPredicateObjectMappingPlan extends
		PredicateObjectMappingPlan {

	public InternalPredicateObjectMappingPlan(TemplateTermSet subjectMapTemplate, PredicateObjectMap pom, 
			TriplesMap objectTriplesMap, 
			Map<ColumnTemplateTerm, HNodePath> subjectTermsToPaths,
			boolean isFlipped, KR2RMLMapping kr2rmlMapping, 
			URIFormatter uriFormatter, RepFactory factory, 
			KR2RMLMappingColumnNameHNodeTranslator translator, 
			SuperSelection sel) throws HNodeNotFoundKarmaException
	{
		super(kr2rmlMapping, uriFormatter, factory, translator, sel);
		this.isFlipped = isFlipped;
		generateInternal(subjectMapTemplate, pom, objectTriplesMap, subjectTermsToPaths);
	}
	public void generateInternal(TemplateTermSet subjectMapTemplate, PredicateObjectMap pom, TriplesMap objectTriplesMap, Map<ColumnTemplateTerm, HNodePath> subjectTermsToPaths) throws HNodeNotFoundKarmaException
	{
		this.isLiteral = false;
		objectTemplateTermSetPopulator = generateTemplateTermSetPopulatorForSubjectMap(objectTriplesMap.getSubject());
		generateInternal(subjectMapTemplate, pom, subjectTermsToPaths);
	}
	@Override
	protected void outputTriple(KR2RMLRDFWriter outWriter, PopulatedTemplateTermSet subject,
			PopulatedTemplateTermSet predicate, PopulatedTemplateTermSet object) {
	
		if(isFlipped)
		{
			outWriter.outputTripleWithURIObject(pom, object.getURI(), predicate.getURI(), subject.getURI());
		}
		else
		{
			outWriter.outputTripleWithURIObject(pom, subject.getURI(), predicate.getURI(), object.getURI());
		}
	
	}
}
