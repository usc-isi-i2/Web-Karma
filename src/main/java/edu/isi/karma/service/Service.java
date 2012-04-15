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

public class Service {
	
	public static final String KARMA_SERVICE_PREFIX = "http://isi.edu/integration/karma/services/";

	private String id;
	private String name;
	private String address;
	private String description;

	private List<Operation> operations;

	public Service() {
		this.operations = new ArrayList<Operation>();
	}
	
	public Service(String id, String name, String address) {
		this.setId(id);
		this.setName(name);
		this.setAddress(address);
		this.operations = new ArrayList<Operation>();
	}
	
	public String getUri() {
		return KARMA_SERVICE_PREFIX + getId() + "#";
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return this.address;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Operation> getOperations() {
		return operations;
	}

	public void setOperations(List<Operation> operations) {
		for (Operation op : operations)
			op.updateBaseUri(this.getUri());
		this.operations = operations;
	}

	public String getInfo() {
		String s = "";
		
		s += "uri" + this.getUri() + "\n";
		s += "id=" + this.getId() + ", ";
		s += "name=" + this.getName() + ", ";
		s += "address=" + this.getAddress();
		
		return s;
	}
	
	public void print() {
		System.out.println("********************************************");
		System.out.println("Service: " + getInfo());
//		System.out.println("id: " + this.getId());
//		System.out.println("local id: " + this.getLocalId());
//		System.out.println("name: " + this.getName());
//		System.out.println("address: " + this.getAddress());
//		System.out.println("description: " + this.getDescription());
		for (Operation op: getOperations())
			op.print();
	}
	
	public static void main(String[] args) {
		
		try {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
