package edu.isi.karma.kr2rml.planning;

import java.util.Map;

import edu.isi.karma.kr2rml.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.exception.HNodeNotFoundKarmaException;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.KR2RMLMappingColumnNameHNodeTranslator;
import edu.isi.karma.kr2rml.template.ColumnTemplateTerm;
import edu.isi.karma.kr2rml.template.PopulatedTemplateTermSet;
import edu.isi.karma.kr2rml.template.TemplateTermSet;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.RepFactory;

public class InternalPredicateObjectMappingPlan extends
		PredicateObjectMappingPlan {

	public InternalPredicateObjectMappingPlan(TemplateTermSet subjectMapTemplate, PredicateObjectMap pom, TriplesMap objectTriplesMap, Map<ColumnTemplateTerm, HNodePath> subjectTermsToPaths, boolean isFlipped,KR2RMLMapping kr2rmlMapping, URIFormatter uriFormatter, RepFactory factory, KR2RMLMappingColumnNameHNodeTranslator translator) throws HNodeNotFoundKarmaException
	{
		super(kr2rmlMapping, uriFormatter, factory, translator);
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
			outWriter.outputTripleWithURIObject(pom.getTriplesMap().getId(), object.getURI(), pom.getId(), predicate.getURI(),pom.getObject().getRefObjectMap().getParentTriplesMap().getId(),  subject.getURI());
		}
		else
		{
			outWriter.outputTripleWithURIObject(pom.getTriplesMap().getId(), subject.getURI(), pom.getId(), predicate.getURI(), pom.getObject().getRefObjectMap().getParentTriplesMap().getId(),object.getURI());
		}
	
	}
}
