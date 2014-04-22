package edu.isi.karma.kr2rml.planning;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.isi.karma.kr2rml.SubjectMap;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.exception.HNodeNotFoundKarmaException;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.KR2RMLMappingColumnNameHNodeTranslator;
import edu.isi.karma.kr2rml.template.ColumnTemplateTerm;
import edu.isi.karma.kr2rml.template.PopulatedTemplateTermSet;
import edu.isi.karma.kr2rml.template.TemplateTermSet;
import edu.isi.karma.kr2rml.template.TemplateTermSetPopulator;
import edu.isi.karma.kr2rml.template.TemplateTermSetPopulatorPlan;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;

public class SubjectMapPlan extends MapPlan {
	TemplateTermSetPopulator subjectMapTTSPopulator;
	TemplateTermSetPopulatorPlan subjectPlan;
	Map<ColumnTemplateTerm, HNodePath> subjectTermsToPaths;
	
	public SubjectMapPlan(TriplesMap triplesMap, KR2RMLMapping kr2rmlMapping, URIFormatter uriFormatter, RepFactory factory, KR2RMLMappingColumnNameHNodeTranslator translator) throws HNodeNotFoundKarmaException
	{
		super(kr2rmlMapping, uriFormatter, factory, translator);
		configureSubjectMapPlan(triplesMap);
	}
	public SubjectMap configureSubjectMapPlan(TriplesMap triplesMap)
			throws HNodeNotFoundKarmaException {
		subjectTermsToPaths = new HashMap<ColumnTemplateTerm, HNodePath>();
		SubjectMap subjMap = triplesMap.getSubject();
		subjectMapTTSPopulator = generateTemplateTermSetPopulatorForSubjectMap(subjMap);
		populateTermsToPathForSubject(subjectTermsToPaths, subjectMapTTSPopulator.getTerms());
		subjectPlan = new TemplateTermSetPopulatorPlan(subjectTermsToPaths, subjectTermsToPaths.keySet());
		return subjMap;
	}
	
	public List<PopulatedTemplateTermSet> execute(Row r)
	{
		List<PopulatedTemplateTermSet> subjects = new LinkedList<PopulatedTemplateTermSet>();
		subjects.addAll(subjectMapTTSPopulator.populate(r, subjectPlan));
		return subjects;
	}
	
	public TemplateTermSet getTemplate()
	{
		return subjectMapTTSPopulator.getTerms();
	}
	public Map<ColumnTemplateTerm, HNodePath> getSubjectTermsToPaths() {
		return subjectTermsToPaths;
	}
}
