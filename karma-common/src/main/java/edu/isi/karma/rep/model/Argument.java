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

package edu.isi.karma.rep.model;

public class Argument {
	private String id;
	private String attOrVarId;
	private String argType;
	
	public Argument(String id, String attOrVarId, String argType) {
		this.id = id;
		this.attOrVarId = attOrVarId;
		this.argType = argType;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAttOrVarId() {
		return attOrVarId;
	}
	public void setAttOrVarId(String attOrVarId) {
		this.attOrVarId = attOrVarId;
	}
	public String getArgType() {
		return argType;
	}
	public void setArgType(String argType) {
		this.argType = argType;
	}
	
	
}
