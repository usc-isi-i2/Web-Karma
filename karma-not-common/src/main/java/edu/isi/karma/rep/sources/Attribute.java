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

package edu.isi.karma.rep.sources;

public class Attribute {

	private String baseUri;
	private String id;
	private String name;
	private String ioType;
	private String value;
	private String hNodeId;
	private String groundedIn;
	private AttributeRequirement requirement;
	
	public static final String INPUT_PREFIX = "in_"; 
	public static final String OUTPUT_PREFIX = "out_"; 

	public Attribute() {
	}
	public Attribute(Attribute attribute) {
		this.id = attribute.id;
		this.name = attribute.name;
		this.ioType = attribute.ioType;
		this.value = attribute.value;
		this.requirement = attribute.requirement;
		this.groundedIn = attribute.groundedIn;
		this.hNodeId = attribute.hNodeId;
		this.baseUri = attribute.baseUri;
	}
	
	public Attribute(String id, String name) {
		this.id = id;
		this.name = name;
		this.ioType = IOType.NONE;
		this.requirement = AttributeRequirement.NONE;
	}
	
	public Attribute(String id, String name, String ioType) {
		this.id = id;
		this.name = name;
		this.ioType = ioType;
		this.requirement = AttributeRequirement.NONE;
	}

	
	public Attribute(String id, String baseUri, String name, String ioType, AttributeRequirement requirement) {
		this.baseUri = baseUri;
		this.id = id;
		this.name = name;
		this.ioType = ioType;
		this.requirement = requirement;
	}

	public Attribute(String id, String baseUri, String name, String ioType, AttributeRequirement requirement, String groundedIn) {
		this.baseUri = baseUri;
		this.id = id;
		this.name = name;
		this.ioType = ioType;
		this.requirement = requirement;
		this.groundedIn = groundedIn;
	}
	
	public Attribute(String id, String name, String ioType, String value) {
		this.id = id;
		this.name = name;
		this.ioType = ioType;
		this.value = value;
		this.requirement = AttributeRequirement.NONE;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public String getUri() {
		String uri = "";
		if (getBaseUri() != null) uri += getBaseUri();
		if (getId() != null) uri += getId();
		return uri;
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
		return ioType;
	}
	public void setIOType(String ioType) {
		this.ioType = ioType;
	}


	public AttributeRequirement getRequirement() {
		return requirement;
	}

	public void setRequirement(AttributeRequirement requirement) {
		this.requirement = requirement;
	}

	public String getGroundedIn() {
		return groundedIn;
	}

	public void setGroundedIn(String groundedIn) {
		this.groundedIn = groundedIn;
	}

	public void print() {
		System.out.println("Attribute: " + this.getUri());
		System.out.println(getInfo());
//		if (this.id != null && this.id.length() > 0) System.out.println("id: " + this.id);
//		if (this.hNodeId != null && this.hNodeId.length() > 0) System.out.println("hNodeId: " + this.hNodeId);
//		if (this.name != null && this.name.length() > 0) System.out.println("name: " + this.name);
//		if (this.ioType != null && this.ioType.length() > 0) System.out.println("IOType: " + this.ioType);
//		if (this.groundedIn != null && this.groundedIn.length() > 0) System.out.println("groundedIn: " + this.groundedIn);
//		if (this.requirement != null) System.out.println("requirement: " + this.requirement);
//		if (this.value != null && this.value.length() > 0) System.out.println("value: " + this.value);
	}
	
	public String getInfo() {
		String s = "";
		s += "id=" + this.id + ", ";
		s += "name=" + this.name + ", ";
		s += "ioType=" + this.ioType + ", ";
		s += "requirement=" + this.requirement + ", ";
		s += "groundedIn=" + this.groundedIn + ", ";
		s += "value= " + this.value + ", ";
		return s;
	}
}
