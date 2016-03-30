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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.isi.karma.common.HttpMethods;
import edu.isi.karma.util.RandomGUID;
import edu.isi.karma.webserver.KarmaException;

public class InvocationManager {

	static Logger logger = LoggerFactory.getLogger(InvocationManager.class);

	private List<URL> requestURLs;
	private List<String> idList;
	private List<Invocation> invocations;
	private Table serviceData;
	private String urlColumnName;
	
	private JsonArray json;
	private JsonArray jsonUrl;
	private JsonArray jsonInputs;
	private JsonArray jsonOutputs;
	private JsonArray jsonUrlAndInputs;
	private JsonArray jsonUrlAndOutputs;
	private JsonArray jsonInputsAndOutputs;
	
	private String encoding;
	
	public InvocationManager(String urlColumnName, List<String> idList, List<String> requestURLStrings, String encoding) 
	throws MalformedURLException, KarmaException {
		this.urlColumnName = (urlColumnName == null || urlColumnName.trim().length() == 0) ? "url" : urlColumnName;
		this.idList = idList;
		this.encoding = encoding;
		requestURLs = URLManager.getURLsFromStrings(requestURLStrings);
		if (requestURLs == null || requestURLs.isEmpty())
			throw new KarmaException("Cannot model a service without any request example.");
		
		this.serviceData = null;
		this.invocations = new ArrayList<>();
		
		json = new JsonArray();
		jsonUrl = new JsonArray();
		jsonInputs = new JsonArray();
		jsonOutputs = new JsonArray();
		jsonUrlAndInputs = new JsonArray();
		jsonUrlAndOutputs = new JsonArray();
		jsonInputsAndOutputs = new JsonArray();
		
		invokeAndGetResponse();
	}
	
	public InvocationManager(String urlColumnName, String requestURLString) 
	throws MalformedURLException, KarmaException {
		this.urlColumnName = (urlColumnName == null || urlColumnName.trim().length() == 0) ? "url" : urlColumnName;
		this.idList = new ArrayList<>();
		this.idList.add("1");
		List<String> requestURLList = new ArrayList<>();
		requestURLList.add(requestURLString);
		requestURLs = URLManager.getURLsFromStrings(requestURLList);
		if (requestURLs == null || requestURLs.isEmpty())
			throw new KarmaException("Cannot model a service without any request example.");
		
		this.serviceData = null;
		this.invocations = new ArrayList<>();
		
		json = new JsonArray();
		jsonUrl = new JsonArray();
		jsonInputs = new JsonArray();
		jsonOutputs = new JsonArray();
		jsonUrlAndInputs = new JsonArray();
		jsonUrlAndOutputs = new JsonArray();
		jsonInputsAndOutputs = new JsonArray();
		
		invokeAndGetResponse();
	}
	
	private void invokeAndGetResponse() {
		for (int i = 0; i < requestURLs.size(); i++) {
			URL url = requestURLs.get(i);
			String requestId = null;
			if (idList != null)
				requestId = idList.get(i);
			Request request = new Request(url);
			Invocation invocation = new Invocation(requestId, request, encoding);
			logger.info("Invoking the service " + request.getUrl().toString() + " ...");
			invocation.invokeAPI();
			invocations.add(invocation);
		}
		List<Table> invocationData = new ArrayList<>();
		for (Invocation inv : this.invocations) {
			populateJsonArraysFromInvocation(inv);
			invocationData.add(inv.getJointInputAndOutput());
		}
		
		logger.info("Integrating the results of all invocations ...");
		Table result = Table.union(invocationData);
		logger.info("Integrating finished.");
		this.serviceData = result;
	}
	
	
	private void populateJsonArraysFromInvocation(Invocation inv) {
		
		try {
			JsonElement out = new JsonParser().parse(inv.getJsonResponse());
//			JsonArray outArray = new JsonArray();
//			outArray.add(out);
			this.jsonOutputs.add(out);
			
			JsonObject url = new JsonObject();
			url.addProperty(this.urlColumnName, inv.getRequest().getUrl().toString());
//			JsonArray urlArray = new JsonArray();
//			urlArray.add(url);
			this.jsonUrl.add(url);
			
			JsonObject in = new JsonObject();
			for (Attribute att : inv.getRequest().getAttributes()) 
				in.addProperty(att.getName(), att.getValue());
//			JsonArray inArray = new JsonArray();
//			inArray.add(in);
			this.jsonInputs.add(in);
			
			JsonObject urlAndIn = new JsonObject();
			urlAndIn.addProperty(this.urlColumnName, inv.getRequest().getUrl().toString());
			for (Attribute att : inv.getRequest().getAttributes()) 
				urlAndIn.addProperty(att.getName(), att.getValue());
			this.jsonUrlAndInputs.add(urlAndIn);
			
			JsonArray urlAndOut = new JsonArray();
			urlAndOut.add(url);
			urlAndOut.add(out);
			this.jsonUrlAndOutputs.add(urlAndOut);
			
			JsonArray inAndOut = new JsonArray();
			inAndOut.add(in);
			inAndOut.add(out);
			this.jsonInputsAndOutputs.add(inAndOut);
			
			JsonArray all = new JsonArray();
			all.add(urlAndIn);
			all.add(out);
			this.json.add(all);
			
			
		} catch (Exception e) {
			logger.debug("Error in parsing json returned by the invocation " + inv.getRequest().getUrl().toString());
		}
	}
	
	public String getServiceJson(boolean includeURL, boolean includeInputAttributes, boolean includeOutputAttributes) {
		if (includeURL && includeInputAttributes && includeOutputAttributes)		
			return this.json.toString();
		else if (includeURL && includeInputAttributes)
			return this.jsonUrlAndInputs.toString();
		else if (includeURL && includeOutputAttributes)
			return this.jsonUrlAndOutputs.toString();
		else if (includeInputAttributes && includeOutputAttributes)
			return this.jsonInputsAndOutputs.toString();
		else if (includeURL)
			return this.jsonUrl.toString();
		else if (includeInputAttributes)
			return this.jsonInputs.toString();
		else if (includeOutputAttributes)
			return this.jsonOutputs.toString();
		else 
			return "";
	}
	
	public Table getServiceData(boolean includeURL, boolean includeInputAttributes, boolean includeOutputAttributes) {

		if (includeURL && includeInputAttributes && includeOutputAttributes)
			return this.serviceData;
		
		List<Attribute> headers = this.serviceData.getHeaders();
		List<List<String>> values = this.serviceData.getValues();

		Table newTable = new Table();
		List<Attribute> newHeader = new ArrayList<>();
		List<List<String>> newValues = new ArrayList<>();
		List<String> newRowIds = new ArrayList<>(this.serviceData.getRowIds());
		
		List<Integer> includingColumns = new ArrayList<>();
		
		if (headers != null) {
			if (includeURL && !headers.isEmpty())
				includingColumns.add(0);
			
			for (int i = 1; i < this.serviceData.getHeaders().size(); i++) {
				if (includeInputAttributes && headers.get(i).getIOType() == IOType.INPUT)
					includingColumns.add(i);
				if (includeOutputAttributes && headers.get(i).getIOType() == IOType.OUTPUT)
					includingColumns.add(i);
			}
		}
		
		for (Integer colIndex : includingColumns) {
			newHeader.add(headers.get(colIndex));
		}
		for (List<String> vals : values) {
			List<String> rowVals = new ArrayList<>();
			for (Integer colIndex : includingColumns)
				rowVals.add(vals.get(colIndex));
			newValues.add(rowVals);
		}
		
		newTable.setHeaders(newHeader);
		newTable.setValues(newValues);
		newTable.setRowIds(newRowIds);
		
		return newTable;
	}
	
	public Table getServiceData() {
		return getServiceData(true, true, true);
	}
	
	public String getServiceJson(boolean includeInputAttributes) {
		if (includeInputAttributes)
			return getServiceJson(true, true, true);
		return getServiceJson(false, false, true);
	}
	
	private List<Attribute> getInputAttributes() {
		List<Attribute> inAttributes = new ArrayList<>();
		
		Table serviceTable = getServiceData();
		for (Attribute p : serviceTable.getHeaders()) {
			if (p.getIOType().equalsIgnoreCase(IOType.INPUT)) {
				inAttributes.add(p);
			}
		}

		return inAttributes;
	}
	
	private List<Attribute> getOutputAttributes() {
		List<Attribute> outAttributes = new ArrayList<>();
		
		Table serviceTable = getServiceData();
		for (Attribute p : serviceTable.getHeaders()) {
			if (p.getIOType().equalsIgnoreCase(IOType.OUTPUT))
				outAttributes.add(p);
		}

		return outAttributes;
	}
	
	/**
	 * This method creates a new service model which includes only the 
	 * service endpoint, http method, input and output attributes
	 * @return
	 */
	public WebService getInitialServiceModel(String serviceName) {
		
		String guid = new RandomGUID().toString();
//		guid = "E9C3F8D3-F778-5C4B-E089-C1749D50AE1F";
		URL sampleUrl = requestURLs.get(0);
		
		if (sampleUrl == null)
			return null;

		WebService service = null;
		if (serviceName == null || serviceName.trim().length() == 0)
			service = new WebService(guid, sampleUrl);
		else
			service = new WebService(guid, serviceName, sampleUrl);

		service.setMethod(HttpMethods.GET.name());

		service.setInputAttributes(getInputAttributes());
		service.setOutputAttributes(getOutputAttributes());
		
		return service;
	}
	
	
	/*public static void main(String[] args) {
//		String s1 = "http://colo-vm10.isi.edu:8080/DovetailService/GetSampleData?sourceName=KDD-02-B-TOSIG";
		String s1 = "http://api.geonames.org/neighbourhood?lat=40.78343&lng=-73.96625&username=karma";
//		String s1 = "http://api.geonames.org/postalCodeCountryInfo?username=karma";
//		String s2 = "http://api.geonames.org/neighbourhood?lat=40.7&lng=-73.9&username=karma";
//		String s3 = "http://api.geonames.org/neighbourhood?lat=40.9&lng=-73.9&username=karma";

		List<String> urls = new ArrayList<String>();
		urls.add(s1);
//		urls.add(s2);
//		urls.add(s3);

		List<String> ids = new ArrayList<String>();
		ids.add("1"); 
//		ids.add("2"); 
//		ids.add("3");

		try {
			InvocationManager sb = new InvocationManager(null, ids, urls, "UTF-8");
			Table tb = sb.getServiceData(false, false, true);
			
//			String str = tb.asCSV();
//			File f = new File("csv");
//			PrintWriter pw = new PrintWriter(f);
//			pw.write(str);
//			pw.close();
			
			logger.debug(tb.getPrintInfo());

			WebService service = sb.getInitialServiceModel(null);
			
			// just for test
			service.getInputAttributes().get(0).sethNodeId("HN1");
			service.getInputAttributes().get(1).sethNodeId("HN2");
			service.getOutputAttributes().get(4).sethNodeId("HN3");
			service.getOutputAttributes().get(6).sethNodeId("HN4");
			service.getOutputAttributes().get(5).sethNodeId("HN5");
			service.getOutputAttributes().get(3).sethNodeId("HN6");
			service.print();
			
			service.updateModel(Test.getGeoNamesNeighbourhoodTree());
			
			String dir = Repository.Instance().SOURCE_REPOSITORY_DIR;
			service.getInputModel().writeJenaModelToFile(dir + "model", "N3");
			
			System.out.println(service.getInputModel().getSparql(null));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}*/


}
