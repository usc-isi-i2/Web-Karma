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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.service.json.JsonManager;

public class ServiceManager {//extends Thread {
	
	String vWorksheetId;
	String requestURLsJSONArray;
	
	public ServiceManager(String requestURLsJSONArray) {
		this.requestURLsJSONArray = requestURLsJSONArray;
	}
	
	public Table getResponse() throws MalformedURLException, JSONException {
		List<URL> requestURLs = URLManager.getURLsFromJSON(requestURLsJSONArray);
		List<Invocation> invocations = new ArrayList<Invocation>();
		
		for (URL url : requestURLs) {
			Request request = new Request(url);
			Invocation invocation = new Invocation(request);
			invocation.invokeAPI();
			invocations.add(invocation);
		}
		
		Table result = integrateAllResults(invocations);
		result.print();
		return result;
	}
	
	/**
	 * Each service invocation might have different columns than other other invocations.
	 * This method integrates all results into one table.
	 * For the invocations that don't have a specific column, we put null values in corresponding column. 
	 */
	public Table integrateAllResults(List<Invocation> invocations) {
    	
		List<List<String>> columns = new ArrayList<List<String>>();
		List<List<String>> types = new ArrayList<List<String>>();
    	List<List<List<String>>> values = new ArrayList<List<List<String>>>();
    	
    	for (Invocation inv : invocations) {
    		columns.add(inv.getJointInputAndOutput().getColumns());
    		types.add(inv.getJointInputAndOutput().getTypes());
    		values.add(inv.getJointInputAndOutput().getValues());
    	}
    	
		List<String> joinColumns = new ArrayList<String>();
		List<String> joinTypes = new ArrayList<String>();
		List<List<String>> joinValues = new ArrayList<List<String>>();
		JsonManager.union(columns, types, values, joinColumns, joinTypes, joinValues);

//		String csv = URLManager.printParamsCSV(null, joinColumns, joinValues);
//		System.out.println(csv);
		
		Table table = new Table();
		table.setColumns(joinColumns);
		table.setTypes(joinTypes);
		table.setValues(joinValues);
		
		return table;
	}
	
	public static void main(String[] args) {
		String s1 = "http://api.geonames.org/neighbourhood?lat=40.78343&lng=-73.96625&username=taheriyan";
//		String s2 = "http://api.geonames.org/neighbourhood?lat=37.78&lng=-75.95&username=taheriyan";
//		String s3 = "http://api.geonames.org/neighbourhood?lat=35.79&lng=-77.97&username=taheriyan";

		JSONArray jsonArray = new JSONArray();
		jsonArray.put(s1);
//		jsonArray.put(s2);
//		jsonArray.put(s3);
		
		try {
			ServiceManager sm = new ServiceManager(jsonArray.toString());
			sm.getResponse();
		} catch (Exception e) {
			
		}
		


	}
}
