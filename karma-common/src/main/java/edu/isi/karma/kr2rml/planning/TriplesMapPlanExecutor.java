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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.ErrorReport.Priority;
import edu.isi.karma.kr2rml.ReportMessage;

public class TriplesMapPlanExecutor {

	private static Logger LOG = LoggerFactory.getLogger(TriplesMapPlanExecutor.class);
	private ExecutorService service;
	private boolean submitPlansIndividually = false;
	
	public TriplesMapPlanExecutor(boolean submitPlansIndividually){
		this.submitPlansIndividually = submitPlansIndividually;
		if(this.submitPlansIndividually)
		{
			service = Executors.newFixedThreadPool(10);
		}
		else
		{
			service = Executors.newSingleThreadExecutor();
		}
	}
	
	public ErrorReport execute(final TriplesMapPlan plan)
	{
		ErrorReport errorReport = new ErrorReport();
		//TODO Handle exceptions/waiting for results better
		try {
			
			if(submitPlansIndividually)
			{
				List<Future<Boolean>> results = service.invokeAll(plan.workers);
				for(Future<Boolean> result : results)
				{
					result.get(10, TimeUnit.MINUTES);
				}
			}
			else
			{
				Future<Boolean> result =	service.submit(new Callable<Boolean>(){

						@Override
						public Boolean call() throws Exception {
							for(TriplesMapWorker worker : plan.workers)
							{
								worker.call();
							}
							return true;
						}
						
					}); 
				result.get(10, TimeUnit.MINUTES);
			}
			
		} catch (Exception e) {
			LOG.error("Unable to finish executing plan", e);
			errorReport.addReportMessage(new ReportMessage("Triples Map Plan Execution Error", e.getMessage(), Priority.high));
			
			shutdown(errorReport);
			if(submitPlansIndividually)
			{
				service = Executors.newFixedThreadPool(10);
			}
			else
			{
				service = Executors.newSingleThreadExecutor();
			}
		}
		return errorReport;
	}
	public  void shutdown(ErrorReport errorReport) {
		
		List<Runnable> unfinishedWorkers = service.shutdownNow();
		for(Runnable unfinishedWorker : unfinishedWorkers)
		{
			if(unfinishedWorker instanceof TriplesMapWorker)
			{
				TriplesMapWorker unfinishedTriplesMapWorker = (TriplesMapWorker) unfinishedWorker;
				errorReport.addReportMessage(new ReportMessage("Triples Map Plan Execution Error", unfinishedTriplesMapWorker.toString() + " was unable to complete", Priority.high));
			}
		}
	
	}

}
