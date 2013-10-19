/*******************************************************************************
 * Copyright 2012 University of Southern California
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
package edu.isi.karma.modeling.semantictypes.myutils;

import java.util.ArrayList;

/**
 * This is multi-threading utility class.
 * 
 * @author amangoel
 *
 */
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
