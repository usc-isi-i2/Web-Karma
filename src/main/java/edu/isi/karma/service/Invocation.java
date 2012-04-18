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
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import edu.isi.karma.service.json.JsonManager;


public class Invocation {

	private static final String REQUEST_COLUMN_NAME = "request";
	static Logger logger = Logger.getLogger(Invocation.class);
	
	private String requestId;

	public Invocation(String requestId, Request request) {
		this.requestId = requestId;
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
		joinInputAndOutput();
		for (int i = 0; i < this.jointInputAndOutput.getValues().size(); i++)
			this.jointInputAndOutput.getRowIds().add(this.requestId);
		return jointInputAndOutput;
	}


	private void updateRequest() {
		request.setEndPoint(URLManager.getEndPoint(request.getUrl()));
		request.setAttributes(URLManager.getQueryAttributes(request.getUrl()));
	}
	
	private static String getId(String name, HashMap<String, Integer> nameCounter) {
		if (nameCounter == null)
			return null;

		Integer count = nameCounter.get(name);
		if (count == null) {
			nameCounter.put(name, 1);
			return Attribute.OUTPUT_PREFIX + name;
		} else {
			nameCounter.put(name, count.intValue() + 1);
			return (Attribute.OUTPUT_PREFIX + name + "_" + String.valueOf(count.intValue()));
		}
	}
	
	private void updateResponse() {
		
		Table results = new Table();
		List<String> columns = new ArrayList<String>();
        HashMap<String, Integer> attributeNameCounter = new HashMap<String, Integer>();

		if (response.getType().indexOf("xml") != -1) { // XML content
			// The library has a bug, some values are wrong, e.g., adminCode2: 061 --> 49 			
			String json = JsonManager.convertXML2JSON(response.getStream());
	        JsonManager.getJsonFlat(json, columns, results.getValues());
		} else if (response.getType().indexOf("json") != -1) { // JSON content
	        JsonManager.getJsonFlat(response.getStream(), columns, results.getValues());
		} else {
			logger.info("The output is neither JSON nor XML.");
		}

		for (String c : columns) {
			Attribute p = new Attribute(getId(c, attributeNameCounter), c, IOType.OUTPUT);
			results.getHeaders().add(p);
		}
		
		logger.info("Service response converted to a flat table.");
		this.response.setTable(results);
	}
	
	public void invokeAPI() {
		
		int code = -1;
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

			   code = httpConnection.getResponseCode();

//			   System.out.println(type);
//			   System.out.println(code);
			   // do something with code .....
			}
			else
			{
			  logger.error("error - not a http request!");
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
			this.response.setCode(code);
			this.response.setStream(outString.toString());
			
			logger.debug(response.getStream());
			logger.info("Service response is ready as string stream.");
			
		}catch(Exception e){

			logger.error("Error in invoking the service with request " + this.request.getUrl().toString());
			
			this.response.setType("application/json");
			this.response.setCode(code);
			this.response.setStream("{\"code\":" + code + ",\"msg\":\"" + e.getMessage() +  "\"}");
			
			System.out.println(e.getMessage());
			
		} finally {

			updateResponse();
			
		}
		
	}
	
	public void joinInputAndOutput() {
    	
		jointInputAndOutput = new Table(this.response.getTable());
		
		List<String> inputValues = new ArrayList<String>();
		if (this.request.getAttributes() != null)
		for (int j = this.request.getAttributes().size() - 1; j >= 0; j--) {
			
			Attribute p = this.request.getAttributes().get(j);
			if (p != null && p.getName() != null && p.getName().toString().trim().length() == 0)
				continue;
				
				jointInputAndOutput.getHeaders().add(0, p);
				inputValues.add(0, p.getValue());
				for (int k = 0; k < this.response.getTable().getValues().size(); k++)
					this.response.getTable().getValues().get(k).add(0, p.getValue());
		}
		
		// there is no output
		if (this.response.getTable().getHeaders().size() == 0) {
			jointInputAndOutput.getValues().add(inputValues);
		}


		// Include the request URLs in the invocation table
		jointInputAndOutput.getHeaders().add(0, new Attribute(REQUEST_COLUMN_NAME, REQUEST_COLUMN_NAME,IOType.NONE));
		for (int k = 0; k < jointInputAndOutput.getValues().size(); k++)
			jointInputAndOutput.getValues().get(k).add(0, this.request.getUrl().toString());
		
	}
}
