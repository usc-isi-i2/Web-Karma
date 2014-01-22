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
package edu.isi.karma.service.json;

public class SingleValue extends Value{
	
	private String valueString;
	
	public SingleValue(String s) {
		this.valueString = s;
	}
	
	public String getValueString() {
		valueString = valueString.trim();
		if (this.valueString.startsWith("\""))
			valueString = valueString.substring(1);
		if (this.valueString.endsWith("\""))
			valueString = valueString.substring(0, valueString.length() - 1);
		return this.valueString;
	}
}
