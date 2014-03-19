package edu.isi.karma.kr2rml.planning;

import java.util.LinkedList;
import java.util.List;
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
	ExecutorService service = Executors.newFixedThreadPool(10);
	public ErrorReport execute(TriplesMapPlan plan)
	{
		ErrorReport errorReport = new ErrorReport();
		try {
			List<Future<Boolean>> results = new LinkedList<Future<Boolean>>();
			for(TriplesMapWorker worker : plan.workers)
			{
				results.add(service.submit(worker));
			}
			for(Future<Boolean> result : results)
			{
				result.get(1, TimeUnit.MINUTES);
			}
		} catch (Exception e) {
			LOG.error("Unable to finish executing plan", e);
			errorReport.addReportMessage(new ReportMessage("Triples Map Plan Execution Error", e.getMessage(), Priority.high));
			
			shutdown(errorReport);
			service = Executors.newFixedThreadPool(10);
			
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
