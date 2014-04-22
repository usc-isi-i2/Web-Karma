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

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;


public class RamblerValueCollection implements ValueCollection {
	private static Logger logger = LoggerFactory.getLogger(RamblerValueCollection.class);
	private HashMap<String,String> data;
	private HashMap<String, String> keyClass = new HashMap<String, String>();
	public RamblerValueCollection(HashMap<String,String> data)
	{
		this.data = data;
	}
	public RamblerValueCollection()
	{
		data = new HashMap<String,String>();
	}
	public void setKeyClass(String key,String cLabel)
	{
		keyClass.put(key, cLabel);
	}
	public String getClass(String key)
	{
		if(keyClass.containsKey(key))
		{
			return keyClass.get(key);
		}
		else {
			return "";
		}
	}
	public void setValue(String id, String val)
	{
		if(data.containsKey(id))
		{
			data.put(id, val);
		}
		else
		{
			data.put(id, val);
		}
	}
	public String getValue(String id) {
		// TODO Auto-generated method stub
		if(data.containsKey(id))
		{
			return data.get(id);
		}
		else
			return "";
	}

	public Collection<String> getValues() {
		// TODO Auto-generated method stub
		return data.values();
	}

	public Collection<String> getNodeIDs() {
		// TODO Auto-generated method stub
		return data.keySet();
	}

	public JSONObject getJson() {
		// TODO Auto-generated method stub
		try
		{
			JSONObject jb = new JSONObject();
			Set<String> ss = data.keySet();
			for(String key:ss)
			{
				String val = data.get(key);
				jb.put(key, val);
			}
			return jb;
		}
		catch(Exception ex)
		{
			logger.error(""+ex.toString());
			return null;
		}
	}
	public String representation()
	{
		String result = "";
		Set<String> res = data.keySet();
		for(String s:res)
		{
			result += data.get(s);
			result += " ";
		}
		return result;
	}
}
