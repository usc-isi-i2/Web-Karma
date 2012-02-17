package edu.isi.karma.modeling.semantictypes.myutils;

import java.util.ArrayList;

public class ThreadOps {
	
	public static void run_in_parallel(ArrayList<? extends Thread> threadObjs, int max) {
		
		int threadIdx = 0 ;
		
		ArrayList<Thread> runningThreads = new ArrayList<Thread>() ;
		
		while(true) {
			
			// Remove threads that have ended
			for(int t=0;t<runningThreads.size();t++) {
				if (!runningThreads.get(t).isAlive()) {
					runningThreads.remove(t) ;
					t-- ;
				}
			}
			
			
			// Start new threads until the total number of threads becomes @max
			for(int r=runningThreads.size();r<max && threadIdx < threadObjs.size();r++) {
				Thread new_thread = threadObjs.get(threadIdx) ;
				new_thread.start() ;
				runningThreads.add(new_thread) ;
				threadIdx++ ;
			}
			
			
			if (runningThreads.size() > 0) {
				try {
					Thread.sleep(2000) ;
				}
				catch(Exception e) {
					Prnt.prn("Sleep interrupted at somepoint.") ;
				}
			}
			else {
				break ;
			}
		}
		
	}
	
	
}