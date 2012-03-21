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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.jetty.http.HttpMethods;

public class Request {
	
	private String type;
	private URL url;
	private String endPoint;
	private List<Param> params;

	public Request(URL url) {
		this.type = HttpMethods.GET;
		this.url = url;
	}
	
	public Request(String urlString) throws MalformedURLException {
		this.type = HttpMethods.GET;
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

	public List<Param> getParams() {
		return params;
	}

	public void setParams(List<Param> params) {
		this.params = params;
	}

//	public void createTableFromParamList() {
//		
//		this.paramTable = new Table();
//		
//		for (Param p : params) {
//			paramTable.getColumns().add(p.getName());
//			paramTable.getTypes().add(IOType.INPUT);
//			List<String> values = new ArrayList<String>();
//			values.add(p.getValue());
//			paramTable.getValues().add(values);
//		}
//	}
	
}
