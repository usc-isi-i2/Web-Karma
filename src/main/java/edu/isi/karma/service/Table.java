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

public class Table {
	
	private List<Param> headers;
	private List<List<String>> values;
	
	public Table() {
		headers = new ArrayList<Param>();
		values = new ArrayList<List<String>>();
	}
	
	public Table(Table table) {
		this.headers = new ArrayList<Param>(table.headers);
		this.values = new ArrayList<List<String>>(table.values);
	}
	

	public List<Param> getHeaders() {
		return headers;
	}

	public void setHeaders(List<Param> headers) {
		this.headers = headers;
	}

	public List<List<String>> getValues() {
		return values;
	}
	public void setValues(List<List<String>> values) {
		this.values = values;
	}
	
	public void print() {
		for (Param p: headers) {
			System.out.print(p.getIOType() + ",");
		}
		System.out.println();

		for (Param p: headers) {
			System.out.print(p.getName() + ",");
		}
		System.out.println();

		for (Param p: headers) {
			System.out.print(p.getId() + ",");
		}
		System.out.println();

		for (List<String> value: values) {
			for (String v:value) {
				System.out.print(v + ",");
			}
			System.out.println();
		}
	}


	/**
	 * Each service invocation might have different columns than other other invocations.
	 * This method integrates all results into one table.
	 * For the invocations that don't have a specific column, we put null values in corresponding column. 
	 */
	
    public static Table union(List<Table> srcTables) {
    	
    	if (srcTables == null)
    		return null;
    	
    	Table resultTable = new Table();
    	
		String paramId = "";
		List<String> uniqueParamIDs = new ArrayList<String>();
		
		List<List<Param>> srcParams = new ArrayList<List<Param>>();
		List<List<List<String>>> srcValues = new ArrayList<List<List<String>>>();
		
		List<Param> resultParams = new ArrayList<Param>();
		List<List<String>> resultValues = new ArrayList<List<String>>();
		
		for (Table t : srcTables) {
			srcParams.add(t.getHeaders());
			srcValues.add(t.getValues());
		}

		for (int i = 0; i < srcParams.size(); i++) {
			for (int j = 0; j < srcParams.get(i).size(); j++)
			{
				paramId = srcParams.get(i).get(j).getId().toString();
				if (uniqueParamIDs.indexOf(paramId) == -1) {
					uniqueParamIDs.add(paramId);
					resultParams.add(srcParams.get(i).get(j));
				}
			}
		}
		
		List<Param> rawParams = null;
		List<String> rawParamIDs = new ArrayList<String>();
		List<String> rawValues = null;
		String singleValue = null;
		for (int i = 0; i < srcParams.size(); i++) {
			
			rawParams = srcParams.get(i);
			rawParamIDs.clear();
			for (Param p : rawParams)
				rawParamIDs.add(p.getId());
			
			for (int j = 0; j < srcValues.get(i).size(); j++) {
				
				List<String> populatedValues = new ArrayList<String>();
				rawValues = srcValues.get(i).get(j);
				
				for (int k = 0; k < resultParams.size(); k++) {
					int index = rawParamIDs.indexOf(resultParams.get(k).getId());
					if (index == -1)
//						singleValue = null;
						singleValue = "";
					else
						singleValue = rawValues.get(index);
					populatedValues.add(singleValue);
				}
				
				resultValues.add(populatedValues);
			}
			
		}
		
		resultTable.setHeaders(resultParams);
		resultTable.setValues(resultValues);
		
		return resultTable;
    }
    
}
