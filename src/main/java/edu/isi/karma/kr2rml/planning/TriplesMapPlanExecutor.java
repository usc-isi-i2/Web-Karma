package edu.isi.karma.kr2rml.planning;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TriplesMapPlanExecutor {

	public void execute(TriplesMapPlan plan)
	{
		ExecutorService service = Executors.newFixedThreadPool(10);
		try {
			List<Future<Boolean>> results = new LinkedList<Future<Boolean>>();
			for(TriplesMapWorker worker : plan.workers)
			{
				results.add(service.submit(worker));
			}
			for(Future<Boolean> result : results)
			{
				result.get();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			
			service.shutdownNow();
		}
	}
}
