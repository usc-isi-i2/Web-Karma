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

import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.service.json.JsonManager;

public class PopulateWorksheet {
	
	private List<Invocation> invocations = new ArrayList<Invocation>();
	
	public PopulateWorksheet(List<Invocation> invocations) {
		this.invocations = invocations;
	}
	
	/**
	 * Each service invocation might have different columns than other other invocations.
	 * This method integrates all results into one table.
	 * For the invocations that don't have a specific column, we put null values in corresponding column. 
	 */
	public void integrateAllResults() {
    	
		List<List<String>> columns = new ArrayList<List<String>>();
    	List<List<List<String>>> values = new ArrayList<List<List<String>>>();
    	
    	for (Invocation inv : invocations) {
    		columns.add(inv.getJointInputAndOutput().getColumns());
    		values.add(inv.getJointInputAndOutput().getValues());
    	}
    	
		List<String> joinColumns = new ArrayList<String>();
		List<List<String>> joinValues = new ArrayList<List<String>>();
		JsonManager.union(columns, values, joinColumns, joinValues);

		String csv = URLManager.printParamsCSV(null, joinColumns, joinValues);
		System.out.println(csv);
	}

}
