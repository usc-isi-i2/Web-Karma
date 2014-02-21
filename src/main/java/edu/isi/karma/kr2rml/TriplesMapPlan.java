package edu.isi.karma.kr2rml;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.isi.karma.kr2rml.KR2RMLWorksheetRDFGenerator.TriplesMapWorker;
import edu.isi.karma.rep.Row;

public class TriplesMapPlan {

	Collection<TriplesMapWorker> workers;
	Row r;
	Map<String, List<String>> triplesMapURIs;
	
	public TriplesMapPlan(Collection<TriplesMapWorker> workers, Row r, Map<String, List<String>>triplesMapURIs)
	{
		this.workers = workers;
		this.r = r;
		this.triplesMapURIs = triplesMapURIs;
	}
}
