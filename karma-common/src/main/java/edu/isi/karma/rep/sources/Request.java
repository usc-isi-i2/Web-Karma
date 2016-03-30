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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.common.HttpMethods;

public class Request {
	
	private String type;
	private URL url;
	private String endPoint;
	private List<Attribute> attributes;

	public Request(Request request) {
		this.type = request.type;
		this.url = request.url;
		this.endPoint = request.endPoint;
		this.attributes = new ArrayList<>(request.getAttributes());
	}
	
	public Request(URL url) {
		this.type = HttpMethods.GET.name();
		this.url = url;
	}
	
	public Request(String urlString) throws MalformedURLException {
		this.type = HttpMethods.GET.name();
		setUrl(urlString);
	}
	
	public URL getUrl() {
		return url;
	}

	public void setUrl(String urlString) throws MalformedURLException {
		URL url = new URL(urlString);
		this.url = url;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public String getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

//	public void createTableFromAttributeList() {
//		
//		this.attributeTable = new Table();
//		
//		for (Attribute p : attributes) {
//			attributeTable.getColumns().add(p.getName());
//			attributeTable.getTypes().add(IOType.INPUT);
//			List<String> values = new ArrayList<String>();
//			values.add(p.getValue());
//			attributeTable.getValues().add(values);
//		}
//	}
	
}
