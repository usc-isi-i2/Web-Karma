package edu.isi.karma.kr2rml;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TriplesMapPlanExecutor {

	public void execute(TriplesMapPlan plan)
	{
		ExecutorService service = Executors.newFixedThreadPool(10);
		try {
			List<Future<Boolean>> results = service.invokeAll(plan.workers);
			for(Future<Boolean> result : results)
			{
				result.get();
				//result.get(1000, TimeUnit.MILLISECONDS);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
