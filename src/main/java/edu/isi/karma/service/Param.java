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




public class Param {

	private String id;
	private String name;
	private String IOType;
	private String value;
	private String hNodeId;

	public Param(Param param) {
		this.id = param.id;
		this.name = param.name;
		this.IOType = param.IOType;
		this.value = param.value;
		this.hNodeId = param.hNodeId;
	}
	
	public Param(String id, String name, String IOType) {
		this.id = id;
		this.name = name;
		this.IOType = IOType;
	}

	public Param(String id, String name, String IOType, String value) {
		this.id = id;
		this.name = name;
		this.IOType = IOType;
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String gethNodeId() {
		return hNodeId;
	}

	public void sethNodeId(String hNodeId) {
		this.hNodeId = hNodeId;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getIOType() {
		return IOType;
	}
	public void setIOType(String IOType) {
		this.IOType = IOType;
	}

	public void print() {
		if (this.id != null && this.id.length() > 0) System.out.println("id: " + this.id);
		if (this.name != null && this.name.length() > 0) System.out.println("name: " + this.name);
		if (this.IOType != null && this.IOType.length() > 0) System.out.println("IOType: " + this.IOType);
		if (this.value != null && this.value.length() > 0) System.out.println("value: " + this.value);
	}
	
	public String getPrintInfo() {
		String s = "";
//		if (this.id != null && this.id.length() > 0) s += "id: " + this.id + ",";
		if (this.name != null && this.name.length() > 0) s += "param name: " + this.name + ", ";
		if (this.IOType != null && this.IOType.length() > 0) s += "IOType: " + this.IOType + ", ";
		if (this.value != null && this.value.length() > 0) s += "value: " + this.value;
		return s;
	}
}
