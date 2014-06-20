package edu.isi.karma.kr2rml.planning;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.exception.HNodeNotFoundKarmaException;
import edu.isi.karma.kr2rml.exception.NoValueFoundInNodeException;
import edu.isi.karma.kr2rml.exception.ValueNotFoundKarmaException;
import edu.isi.karma.rep.Row;



public class TriplesMapWorker implements Callable<Boolean> {

	private Logger LOG = LoggerFactory.getLogger(TriplesMapWorker.class);
	protected List<CountDownLatch> dependentTriplesMapLatches;
	protected CountDownLatch latch;
	protected TriplesMap triplesMap;
	protected Row r;
	protected List<KR2RMLRDFWriter> outWriters;
	
	protected TriplesMapWorkerPlan plan;
	
	public TriplesMapWorker(TriplesMap triplesMap, CountDownLatch latch, Row r, TriplesMapWorkerPlan plan, List<KR2RMLRDFWriter> outWriters)
	{
		this.latch = latch;
		this.triplesMap = triplesMap;
		this.plan = plan;
		this.dependentTriplesMapLatches = new LinkedList<CountDownLatch>();
		this.r = r;
		this.outWriters = outWriters;
	}
	public void addDependentTriplesMapLatch(CountDownLatch latch)
	{
		dependentTriplesMapLatches.add(latch);
	}
	
	private void notifyDependentTriplesMapWorkers()
	{
		for(CountDownLatch latch : dependentTriplesMapLatches)
		{
			latch.countDown();
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