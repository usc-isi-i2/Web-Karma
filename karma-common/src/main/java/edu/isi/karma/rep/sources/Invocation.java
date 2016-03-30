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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.service.json.JsonManager;


public class Invocation {

	private static final String REQUEST_COLUMN_NAME = "request";
	static Logger logger = LoggerFactory.getLogger(Invocation.class);
	
	private String requestId;
	private String encoding;
	
	public Invocation(String requestId, Request request, String encoding) {
		this.requestId = requestId;
		this.request = request;
		this.encoding = encoding;
		
		updateRequest();
	}
	
	private Request request;
	private Response response;
	private Table jointInputAndOutput;
	private String jsonResponse;
	
	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}


	public Response getResponse() {
		return response;
	}

	public String getJsonResponse() {
		return jsonResponse;
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
		List<String> columns = new ArrayList<>();
        HashMap<String, Integer> attributeNameCounter = new HashMap<>();

        logger.info("output type is: " + response.getType());
        if (response.getType() == null) {
			logger.info("The output does not have a type.");
        } else if (response.getType().indexOf("xml") != -1) { // XML content
			// The library has a bug, some values are wrong, e.g., adminCode2: 061 --> 49 			
			String json = JsonManager.convertXML2JSON(response.getStream());
			this.jsonResponse = json;
	        JsonManager.getJsonFlat(json, columns, results.getValues());
		} else if (response.getType().indexOf("json") != -1) { // JSON content
			this.jsonResponse = response.getStream();
	        JsonManager.getJsonFlat(response.getStream(), columns, results.getValues());
		} else {
			logger.info("The output type is neither JSON nor XML.");
			if (JsonManager.parse(response.getStream())) {
	        	this.jsonResponse = response.getStream();
	        	JsonManager.getJsonFlat(response.getStream(), columns, results.getValues());
	        } else {
				logger.info("Malformed json output.");
	        }
		}

		for (String c : columns) {
			Attribute p = new Attribute(getId(c, attributeNameCounter), c, IOType.OUTPUT);
			results.getHeaders().add(p);
		}
		
		logger.info("Service response converted to a flat table.");
		this.response.setTable(results);
		
		//FIXME : just to rule out some attributes in the GUI for the paper figure
//		int i = 0;
//		while (i < this.response.getTable().getHeaders().size()) {
//			String h = this.response.getTable().getHeaders().get(i).getName();
//			if (h.indexOf("admin") == -1) {i++; continue;}
//			this.response.getTable().getHeaders().remove(i);
//			for (List<String> values: this.response.getTable().getValues()) {
//				values.remove(i);
//			}
//		}
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
							connection.getInputStream(), encoding));
			
			String outLine;
			while ((outLine = in.readLine()) != null) {
				outString.append(outLine + "\n");
			}
			in.close();
			
//			System.out.println(outString);
			
			this.response.setType(type);
			this.response.setCode(code);
			this.response.setStream(outString.toString());
			
//			logger.debug(response.getStream());
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
		
		List<String> inputValues = new ArrayList<>();
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
		if (this.response.getTable().getHeaders().isEmpty()) {
			jointInputAndOutput.getValues().add(inputValues);
		}


		// Include the request URLs in the invocation table
		jointInputAndOutput.getHeaders().add(0, new Attribute(REQUEST_COLUMN_NAME, REQUEST_COLUMN_NAME,IOType.NONE));
		for (int k = 0; k < jointInputAndOutput.getValues().size(); k++)
			jointInputAndOutput.getValues().get(k).add(0, this.request.getUrl().toString());
		
	}
}
