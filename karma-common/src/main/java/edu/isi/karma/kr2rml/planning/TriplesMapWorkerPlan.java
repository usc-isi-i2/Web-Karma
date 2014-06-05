package edu.isi.karma.kr2rml.planning;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.ObjectMap;
import edu.isi.karma.kr2rml.Predicate;
import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.exception.HNodeNotFoundKarmaException;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.KR2RMLMappingColumnNameHNodeTranslator;
import edu.isi.karma.kr2rml.template.PopulatedTemplateTermSet;
import edu.isi.karma.kr2rml.template.StringTemplateTerm;
import edu.isi.karma.kr2rml.template.TemplateTermSet;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;

public class TriplesMapWorkerPlan {
	private Logger LOG = LoggerFactory.getLogger(TriplesMapWorkerPlan.class);
	
	private RepFactory factory;
	private TriplesMap triplesMap;
	private KR2RMLMapping kr2rmlMapping;
	private SubjectMapPlan subjectMapPlan;
	private Deque<PredicateObjectMappingPlan> internalLinksPlans;
	private Deque<PredicateObjectMappingPlan> columnLinksPlans;

	private URIFormatter uriFormatter;

	private KR2RMLMappingColumnNameHNodeTranslator translator;

	private boolean generateContext;

	private Map<String, String> hNodeToContextUriMap;
	public TriplesMapWorkerPlan(RepFactory factory, TriplesMap triplesMap, KR2RMLMapping kr2rmlMapping, URIFormatter uriFormatter, KR2RMLMappingColumnNameHNodeTranslator translator, boolean generateContext, Map<String, String> hNodeToContextUriMap) throws HNodeNotFoundKarmaException
	{
		this.factory = factory;
		this.triplesMap = triplesMap;
		this.kr2rmlMapping = kr2rmlMapping;
		this.uriFormatter = uriFormatter;
		this.translator = translator;
		this.generateContext = generateContext;
		this.hNodeToContextUriMap = hNodeToContextUriMap;
		generate();
	}
	
	public void generate() throws HNodeNotFoundKarmaException
	{
		subjectMapPlan = new SubjectMapPlan(triplesMap, kr2rmlMapping,uriFormatter, factory, translator);
		
		internalLinksPlans = new LinkedList<PredicateObjectMappingPlan>();
		
		List<TriplesMapLink> links = kr2rmlMapping.getAuxInfo().getTriplesMapGraph().getTriplesMapGraph(triplesMap.getId()).getAllNeighboringTriplesMap(triplesMap.getId());
		for(TriplesMapLink link : links) {
			if(link.getSourceMap().getId().compareTo(triplesMap.getId()) ==0  && !link.isFlipped() ||
					link.getTargetMap().getId().compareTo(triplesMap.getId()) == 0 && link.isFlipped())
			{
				PredicateObjectMap pom = link.getPredicateObjectMapLink();
				TriplesMap objectTriplesMap = null;
				if(link.isFlipped())
				{
					objectTriplesMap = link.getSourceMap();
				}
				else
				{
					objectTriplesMap = link.getTargetMap();
				}
				PredicateObjectMappingPlan pomPlan = new InternalPredicateObjectMappingPlan(subjectMapPlan.getTemplate(), pom, objectTriplesMap, subjectMapPlan.getSubjectTermsToPaths(),link.isFlipped(), kr2rmlMapping,uriFormatter, factory, translator);
				if(link.isFlipped())
				{
					internalLinksPlans.addFirst(pomPlan);
				}
				else
				{
					internalLinksPlans.addLast(pomPlan);
				}
			}	
		}
		
		columnLinksPlans = new LinkedList<PredicateObjectMappingPlan>();
		// Subject 
		// Generate triples for specifying the types
		for (TemplateTermSet typeTerm:triplesMap.getSubject().getRdfsType()) {
			
			PredicateObjectMap pom = new PredicateObjectMap(triplesMap);
			pom.setObject(new ObjectMap(factory.getNewId("objectmap"), typeTerm, null));
			Predicate typePredicate = new Predicate(factory.getNewId("predicate"));
			TemplateTermSet typeTemplate = new TemplateTermSet();
			typeTemplate.addTemplateTermToSet(new StringTemplateTerm(Uris.RDF_TYPE_URI));
			typePredicate.setTemplate(typeTemplate);
			pom.setPredicate(typePredicate);
			PredicateObjectMappingPlan pomPlan = new ColumnPredicateObjectMappingPlan(subjectMapPlan.getTemplate(), pom, subjectMapPlan.getSubjectTermsToPaths(), kr2rmlMapping,uriFormatter, factory, translator, hNodeToContextUriMap, generateContext);
			columnLinksPlans.add(pomPlan);
		}
		
		for(PredicateObjectMap pom : triplesMap.getPredicateObjectMaps())
		{
			LOG.debug("Processing " + pom.toString());
			if(pom.getObject().hasRefObjectMap())
			{
				LOG.debug("Skipping " + pom.toString());
				continue;
			}
			if(pom.getPredicate().toString().contains("classLink"))
			{
				LOG.debug("Skipping " + pom.toString());
				continue;
			}
			PredicateObjectMappingPlan pomPlan = new ColumnPredicateObjectMappingPlan(subjectMapPlan.getTemplate(), pom, subjectMapPlan.getSubjectTermsToPaths(), kr2rmlMapping,uriFormatter, factory, translator, hNodeToContextUriMap, generateContext);
			columnLinksPlans.add(pomPlan);
		}
	}
	
	public void execute(Row r, List<KR2RMLRDFWriter> outWriters)
	{

		List<PopulatedTemplateTermSet> subjects = subjectMapPlan.execute(r);
		
		for(PredicateObjectMappingPlan internalLinkPlan : internalLinksPlans)
		{
			internalLinkPlan.outputTriples(outWriters, internalLinkPlan.execute(r, subjects), r);
		}
		
		for(PredicateObjectMappingPlan columnLinkPlan : columnLinksPlans)
		{
			
			columnLinkPlan.outputTriples(outWriters, columnLinkPlan.execute(r, subjects), r);
		}
	}
}
