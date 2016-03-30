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

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.selection.SuperSelection;
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
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.modeling.Prefixes;
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
	private Deque<PredicateObjectMappingPlan> constantLinksPlans;
	private SuperSelection selection;
	private URIFormatter uriFormatter;

	private KR2RMLMappingColumnNameHNodeTranslator translator;

	private boolean generateContext;

	private Map<String, String> hNodeToContextUriMap;

	private Map<TriplesMapGraph, List<String>> graphTriplesMapsProcessingOrder;
	public TriplesMapWorkerPlan(RepFactory factory, TriplesMap triplesMap, 
			KR2RMLMapping kr2rmlMapping, URIFormatter uriFormatter, 
			KR2RMLMappingColumnNameHNodeTranslator translator, 
			boolean generateContext, 
			Map<String, String> hNodeToContextUriMap, 
			SuperSelection sel, Map<TriplesMapGraph, List<String>> graphTriplesMapsProcessingOrder) throws HNodeNotFoundKarmaException
	{
		this.factory = factory;
		this.triplesMap = triplesMap;
		this.kr2rmlMapping = kr2rmlMapping;
		this.uriFormatter = uriFormatter;
		this.translator = translator;
		this.generateContext = generateContext;
		this.hNodeToContextUriMap = hNodeToContextUriMap;
		this.selection = sel;
		this.graphTriplesMapsProcessingOrder = graphTriplesMapsProcessingOrder;
		generate();
	}
	
	public void generate() throws HNodeNotFoundKarmaException
	{ 
		subjectMapPlan = new SubjectMapPlan(triplesMap, kr2rmlMapping,uriFormatter, factory, translator, selection);
		
		internalLinksPlans = new LinkedList<>();
		
		List<TriplesMapLink> links = null;
		for(TriplesMapGraph graph : graphTriplesMapsProcessingOrder.keySet())
		{
			if(graph.getTriplesMapIds().contains(triplesMap.getId()))
			{
				links = graph.getAllNeighboringTriplesMap(triplesMap.getId());
				break;
			}
		}
	
		for(TriplesMapLink link : links) {
			try {
				if(link.getSourceMap().getId().compareTo(triplesMap.getId()) ==0  && !link.isFlipped() ||
						link.getTargetMap().getId().compareTo(triplesMap.getId()) == 0 && link.isFlipped())
				{
					PredicateObjectMap pom = link.getPredicateObjectMapLink();
					TriplesMap objectTriplesMap;
					if(link.isFlipped())
					{
						objectTriplesMap = link.getSourceMap();
					}
					else
					{
						objectTriplesMap = link.getTargetMap();
					}
					PredicateObjectMappingPlan pomPlan = new InternalPredicateObjectMappingPlan(subjectMapPlan.getTemplate(), pom, objectTriplesMap, subjectMapPlan.getSubjectTermsToPaths(),link.isFlipped(), kr2rmlMapping,uriFormatter, factory, translator, selection);
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
			catch (Exception e)
			{
				LOG.error("Unable to generate plan for link " + link.getSourceMap().getId() + " " + link.getPredicateObjectMapLink().getPredicate().getId() + " " + link.getTargetMap().getId(), e);
			}
		}
		
		columnLinksPlans = new LinkedList<>();
		constantLinksPlans = new LinkedList<>();
		// Subject 
		// Generate triples for specifying the types
		for (TemplateTermSet typeTerm:triplesMap.getSubject().getRdfsType()) {
			
			PredicateObjectMap pom = new PredicateObjectMap(Prefixes.KARMA_DEV + PredicateObjectMap.getNewId(),triplesMap);
			pom.setObject(new ObjectMap(factory.getNewId("objectmap"), typeTerm, null));
			Predicate typePredicate = new Predicate(factory.getNewId("predicate"));
			TemplateTermSet typeTemplate = new TemplateTermSet();
			typeTemplate.addTemplateTermToSet(new StringTemplateTerm(Uris.RDF_TYPE_URI));
			typePredicate.setTemplate(typeTemplate);
			pom.setPredicate(typePredicate);
			PredicateObjectMappingPlan pomPlan = new ColumnPredicateObjectMappingPlan(subjectMapPlan.getTemplate(), pom, subjectMapPlan.getSubjectTermsToPaths(), kr2rmlMapping,uriFormatter, factory, translator, hNodeToContextUriMap, generateContext, selection);
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
			try{
				PredicateObjectMappingPlan pomPlan;
				if(pom.getObject().getTemplate().getAllColumnNameTermElements().isEmpty())
				{
					pomPlan = new ConstantPredicateObjectMappingPlan(subjectMapPlan.getTemplate(), pom, kr2rmlMapping,subjectMapPlan.getSubjectTermsToPaths(), uriFormatter, factory, translator, selection);
					constantLinksPlans.add(pomPlan);
				}
				else
				{
					pomPlan = new ColumnPredicateObjectMappingPlan(subjectMapPlan.getTemplate(), pom, subjectMapPlan.getSubjectTermsToPaths(), kr2rmlMapping,uriFormatter, factory, translator, hNodeToContextUriMap, generateContext, selection);
					columnLinksPlans.add(pomPlan);
				}
			}
			catch (Exception e)
			{
				LOG.error("Unable to generate plan for pom " + pom.getId(), e);
			}
			
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
		
		for(PredicateObjectMappingPlan constantLinkPlan : constantLinksPlans)
		{
			constantLinkPlan.outputTriples(outWriters, constantLinkPlan.execute(r, subjects), r);
		}
	}
}