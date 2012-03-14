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
package edu.isi.karma.rep.cleaning;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import org.json.JSONArray;


public class RamblerValueCollection implements ValueCollection {
	private HashMap<String,String> data;
	public RamblerValueCollection(HashMap<String,String> data)
	{
		this.data = data;
	}
	public void setValue(String id, String val)
	{
		if(data.containsKey(id))
		{
			data.put(id, val);
		}
	}
	@Override
	public String getValue(String id) {
		// TODO Auto-generated method stub
		if(data.containsKey(id))
		{
			return data.get(id);
		}
		else
			return "";
	}

	@Override
	public Collection<String> getValues() {
		// TODO Auto-generated method stub
		return data.values();
	}

	@Override
	public Collection<String> getNodeIDs() {
		// TODO Auto-generated method stub
		return data.keySet();
	}

	@Override
	public JSONArray getJson() {
		// TODO Auto-generated method stub
		return null;
	}

}
