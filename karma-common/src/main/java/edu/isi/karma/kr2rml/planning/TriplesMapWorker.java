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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.exception.HNodeNotFoundKarmaException;
import edu.isi.karma.kr2rml.exception.NoValueFoundInNodeException;
import edu.isi.karma.kr2rml.exception.ValueNotFoundKarmaException;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.rep.Row;



public class TriplesMapWorker implements Callable<Boolean> {

	private Logger LOG = LoggerFactory.getLogger(TriplesMapWorker.class);
	protected List<TriplesMapWorker> dependentTriplesMapWorkers;
	protected List<TriplesMapWorker> workersDependentOn;
	protected CountDownLatch latch;
	protected TriplesMap triplesMap;
	protected Row r;
	protected List<KR2RMLRDFWriter> outWriters;
	
	protected TriplesMapWorkerPlan plan;
	
	public TriplesMapWorker(TriplesMap triplesMap, List<TriplesMapWorker> workersDependentOn, Row r, TriplesMapWorkerPlan plan, List<KR2RMLRDFWriter> outWriters)
	{
		this.workersDependentOn = workersDependentOn;
		this.latch = new CountDownLatch(workersDependentOn.size());
		this.triplesMap = triplesMap;
		this.plan = plan;
		this.dependentTriplesMapWorkers = new LinkedList<>();
		this.r = r;
		this.outWriters = outWriters;
	}
	public void addDependentTriplesMapWorker(TriplesMapWorker dependentTriplesMapWorker)
	{
		dependentTriplesMapWorkers.add(dependentTriplesMapWorker);
	}
	
	private void notifyDependentTriplesMapWorkers()
	{
		for(TriplesMapWorker dependentWorker : dependentTriplesMapWorkers)
		{
			dependentWorker.getLatch().countDown();
		}
	}
	
	
	@Override
	public Boolean call() throws HNodeNotFoundKarmaException, ValueNotFoundKarmaException, NoValueFoundInNodeException {
		
		
		try{
			latch.await();
		}
		catch (Exception e )
		{
			LOG.error("Error while waiting for dependent triple maps to process", e);
			notifyDependentTriplesMapWorkers();
			return false;
		}
		LOG.debug("Processing " + triplesMap.getId() + " " +triplesMap.getSubject().getId());
		try
		{
		
			plan.execute(r, outWriters);
		
		}
		catch (Exception e)
		{
			e.printStackTrace();
			LOG.error("Something went wrong " + e.getMessage() );
		}
			
		LOG.debug("Processed " + triplesMap.getId() + " " +triplesMap.getSubject().getId());
		notifyDependentTriplesMapWorkers();
		return true;
	}


	public CountDownLatch getLatch() {
		return latch;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("TriplesMapWorker: ");
		sb.append("row = ");
		sb.append(r.getId());
		sb.append("triplesMap = ");
		sb.append(triplesMap.getId());
		return sb.toString();
	}
}