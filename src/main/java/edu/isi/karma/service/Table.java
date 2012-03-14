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
	
	private List<String> columns;
	private List<String> types;
	private List<List<String>> values;
	
	public Table() {
		columns = new ArrayList<String>();
		types = new ArrayList<String>();
		values = new ArrayList<List<String>>();
	}
	
	public Table(Table table) {
		this.columns = new ArrayList<String>(table.columns);
		this.types = new ArrayList<String>(table.types);
		this.values = new ArrayList<List<String>>(table.values);
	}
	
	public List<String> getColumns() {
		return columns;
	}
	public void setColumns(List<String> columns) {
		this.columns = columns;
	}
	
	public List<String> getTypes() {
		return types;
	}

	public void setTypes(List<String> types) {
		this.types = types;
	}

	public List<List<String>> getValues() {
		return values;
	}
	public void setValues(List<List<String>> values) {
		this.values = values;
	}
	
	public void print() {
		for (String column: columns) {
			System.out.print(column + ",");
		}
		System.out.println();
		for (String type: types) {
			System.out.print(type + ",");
		}
		System.out.println();
		for (List<String> value: values) {
			for (String v:value) {
				System.out.print(v + ",");
			}
			System.out.println();
		}
	}
}
