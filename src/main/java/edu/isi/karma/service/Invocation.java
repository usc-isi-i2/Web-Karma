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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.service.json.JsonManager;


public class Invocation {

	private static final String REQUEST_COLUMN_NAME = "request";
	
	public Invocation(Request request) {
		this.request = request;
		updateRequest();
	}
	
	private Request request;
	private Response response;
	private Table jointInputAndOutput;
	
	public Request getRequest() {
		return request;
	}


	public void setRequest(Request request) {
		this.request = request;
	}


	public Response getResponse() {
		return response;
	}


	public void setResponse(Response response) {
		this.response = response;
	}


	public Table getJointInputAndOutput() {
		return jointInputAndOutput;
	}


	private void updateRequest() {
		request.setEndPoint(URLManager.getEndPoint(request.getUrl()));
		request.setParams(URLManager.getQueryParams(request.getUrl()));
	}
	
	private void updateResponse() {
		
		Table results = new Table();
		
		if (response.getType().indexOf("xml") != -1) { // XML content
			String json = JsonManager.convertXML2JSON(response.getStream());
	        JsonManager.getJsonFlat(json, results.getColumns(), results.getValues());
		} else if (response.getType().indexOf("json") != -1) { // JSON content
	        JsonManager.getJsonFlat(response.getStream(), results.getColumns(), results.getValues());
		}
		
		List<String> types = new ArrayList<String>();
		for (int i = 0; i < results.getColumns().size(); i++)
			types.add(IOType.OUTPUT);
		results.setTypes(types);
		
		this.response.setTable(results);
	}

	public void invokeAPI() {
		try{
			this.response = new Response();
			
			URL url = request.getUrl();
			URLConnection connection = url.openConnection();
			String type = connection.getContentType();
			
			//connection.connect();

			// Cast to a HttpURLConnection
			if ( connection instanceof HttpURLConnection)
			{
			   HttpURLConnection httpConnection = (HttpURLConnection) connection;
//			   connection.setRequestProperty("Content-Type", "application/json");

			   int code = httpConnection.getResponseCode();

			   System.out.println(type);
			   System.out.println(code);
			   // do something with code .....
			}
			else
			{
			   System.err.println ("error - not a http request!");
			}
			
			StringBuffer outString = new StringBuffer();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							connection.getInputStream(), "UTF-8"));
			
			String outLine;
			while ((outLine = in.readLine()) != null) {
				outString.append(outLine + "\n");
			}
			in.close();
			
//			System.out.println(outString);
			this.response.setType(type);
			this.response.setStream(outString.toString());
			
			updateResponse();
			
			joinInputAndOutput();
			
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		
	}
	
	public void joinInputAndOutput() {
    	
		jointInputAndOutput = new Table(this.response.getTable());
		
		String column = "";
		for (int j = this.request.getParams().size() - 1; j >= 0; j--) {
			
			Param p = this.request.getParams().get(j);
			if (p != null && p.getName() != null && p.getName().toString().trim().length() == 0)
				continue;
				
				column = p.getName().toString().trim();
				jointInputAndOutput.getColumns().add(0, column);
				jointInputAndOutput.getTypes().add(0, IOType.INPUT);
				for (int k = 0; k < this.response.getTable().getValues().size(); k++)
					this.response.getTable().getValues().get(k).add(0, p.getValue());
		}
		
		jointInputAndOutput.getColumns().add(0, REQUEST_COLUMN_NAME);
		jointInputAndOutput.getTypes().add(0, IOType.NONE);
		for (int k = 0; k < this.response.getTable().getValues().size(); k++)
			this.response.getTable().getValues().get(k).add(0, this.request.getUrl().toString());
		
	}
}
