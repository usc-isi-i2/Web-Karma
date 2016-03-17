/*******************************************************************************
 * Copyright 2014 University of Southern California
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
package edu.isi.karma.kr2rml.mapping;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

public class R2RMLMappingIdentifier {

	private String name;
	private String content;
	private URL location;
	
	public R2RMLMappingIdentifier(String name, URL location, String content)
	{
		this.name = name;
		this.location = location;
		this.content = content;
	}
	
	public R2RMLMappingIdentifier(String name, URL location)
	{
		this.name = name;
		this.location = location;
		this.content = null;
	}
	
	public R2RMLMappingIdentifier(JSONObject id) throws MalformedURLException, JSONException
	{
		this.name = id.getString("name");
		this.location = new URL(id.getString("location"));
		this.content = id.getString("content");
		if(this.content.equals("null"))
			this.content = null;
	}
	
	public String getName() {
		return name;
	}
	
	public URL getLocation() {
		return location;
	}
	
	public String getContent() {
		return content;
	}
	
	public JSONObject toJSON()
	{
		JSONObject id = new JSONObject();
		id.put("name", name);
		id.put("location", location.toString());
		id.put("content", content);
		return id;
	}
	
}
