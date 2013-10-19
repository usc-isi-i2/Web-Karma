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

package edu.isi.karma.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class HTTPUtil {
	public enum HTTP_METHOD {
		GET, POST, PUT, DELETE, HEAD
	}
	
	public enum HTTP_HEADERS {
		Accept, 
	}
	
	public static String executeHTTPPostRequest(String serviceURL, String contentType, 
			String acceptContentType, Map<String, String> formParameters) 
					throws ClientProtocolException, IOException {
		
		// Prepare the message body parameters
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		for (String param:formParameters.keySet()) {
			formParams.add(new BasicNameValuePair(param, formParameters.get(param)));
		}
		
		// Prepare the headers
		HttpPost httpPost = new HttpPost(serviceURL);
		httpPost.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8"));
		return invokeHTTPRequest(httpPost, contentType, acceptContentType);
	}
	
	public static String executeHTTPPostRequest(String serviceURL, String contentType, 
			String acceptContentType, String rawPostBodyData) 
					throws ClientProtocolException, IOException {
		
		// Prepare the headers
		HttpPost httpPost = new HttpPost(serviceURL);
		httpPost.setEntity(new StringEntity(rawPostBodyData));
		return invokeHTTPRequest(httpPost, contentType, acceptContentType);
	}
	
	private static String invokeHTTPRequest(HttpPost httpPost, String contentType, 
			String acceptContentType) throws ClientProtocolException, IOException {
		HttpClient httpClient = new DefaultHttpClient();
		
		if (acceptContentType != null && !acceptContentType.isEmpty()) {
			httpPost.setHeader(HTTP_HEADERS.Accept.name(), acceptContentType);	
		}
		if (contentType != null && !contentType.isEmpty()) {
			httpPost.setHeader("Content-Type", contentType);
		}
		
		// Execute the request
		HttpResponse response = httpClient.execute(httpPost);
		
		// Parse the response and store it in a String
		HttpEntity entity = response.getEntity();
		StringBuilder responseString = new StringBuilder();
		if (entity != null) {
			BufferedReader buf = new BufferedReader(new InputStreamReader(entity.getContent()));
			
			String line = buf.readLine();
			while(line != null) {
				responseString.append(line);
				line = buf.readLine();
			}
		}
		return responseString.toString();
	}
	
	public static String executeHTTPGetRequest(String uri, String acceptContentType) 
			throws ClientProtocolException, IOException {
		HttpClient httpClient = new DefaultHttpClient();
		
		HttpGet request = new HttpGet(uri);
		if(acceptContentType != null && !acceptContentType.isEmpty()) {
			request.setHeader(HTTP_HEADERS.Accept.name(), acceptContentType);
		}
		HttpResponse response = httpClient.execute(request);
		
		// Parse the response and store it in a String
		HttpEntity entity = response.getEntity();
		StringBuilder responseString = new StringBuilder();
		if (entity != null) {
			BufferedReader buf = new BufferedReader(new InputStreamReader(entity.getContent()));
			
			String line = buf.readLine();
			while(line != null) {
				responseString.append(line);
				line = buf.readLine();
			}
		}
		return responseString.toString();
	}
}
