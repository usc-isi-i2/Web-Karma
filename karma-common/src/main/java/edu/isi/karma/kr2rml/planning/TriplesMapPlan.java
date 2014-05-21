package edu.isi.karma.kr2rml.planning;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.isi.karma.kr2rml.template.PopulatedTemplateTermSet;
import edu.isi.karma.rep.Row;

public class TriplesMapPlan {

	protected Collection<TriplesMapWorker> workers;
	protected Row r;
	protected Map<String, List<PopulatedTemplateTermSet>> triplesMapSubjects;
	
	public TriplesMapPlan(Collection<TriplesMapWorker> workers, Row r, Map<String, List<PopulatedTemplateTermSet>>triplesMapSubjects)
	{
		this.workers = workers;
		this.r = r;
		this.triplesMapSubjects = triplesMapSubjects;
	}
}
