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

package edu.isi.karma.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

public class ServiceManager extends Thread {
	
	String vWorksheetId;
	String requestURLsJSONArray;
	
	public ServiceManager(String vWorksheetId, String requestURLsJSONArray) {
		this.vWorksheetId = vWorksheetId;
		this.requestURLsJSONArray = requestURLsJSONArray;
		this.start();
	}
	
	public void run() {
		try {
			
			List<URL> requestURLs = URLManager.getURLsFromJSON(requestURLsJSONArray);
			List<Invocation> invocations = new ArrayList<Invocation>();
			
			for (URL url : requestURLs) {
				Request request = new Request(url);
				Invocation invocation = new Invocation(request);
				invocation.invokeAPI();
				invocations.add(invocation);
//				System.out.println(invocation.getResponse().getStream());
//				invocation.getJointInputAndOutput().print();
			}
			
			PopulateWorksheet populateWorksheet = new PopulateWorksheet(invocations);
			populateWorksheet.integrateAllResults();
			
		} catch (Exception e) {
			
		}
	}
	
	public static void main(String[] args) {
		String s1 = "http://api.geonames.org/neighbourhood?lat=40.78343&lng=-73.96625&username=taheriyan";
		String s2 = "http://api.geonames.org/neighbourhood?lat=37.78&lng=-75.95&username=taheriyan";
		String s3 = "http://api.geonames.org/neighbourhood?lat=35.79&lng=-77.97&username=taheriyan";

		JSONArray jsonArray = new JSONArray();
		jsonArray.put(s1);
//		jsonArray.put(s2);
//		jsonArray.put(s3);
		
		new ServiceManager("", jsonArray.toString());
		


	}
}
