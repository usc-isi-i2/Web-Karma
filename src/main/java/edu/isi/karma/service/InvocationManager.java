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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpMethods;

import edu.isi.karma.modeling.Test;
import edu.isi.karma.util.RandomGUID;
import edu.isi.karma.webserver.KarmaException;

public class InvocationManager {

	static Logger logger = Logger.getLogger(InvocationManager.class);

	private List<URL> requestURLs;
	private List<String> idList;
	private List<Invocation> invocations;
	private Table serviceData;
	
	public InvocationManager(List<String> idList, List<String> requestURLStrings) 
	throws MalformedURLException, KarmaException {
		this.idList = idList;
		requestURLs = URLManager.getURLsFromStrings(requestURLStrings);
		if (requestURLs == null || requestURLs.size() == 0)
			throw new KarmaException("Cannot model a service without any request example.");
		
		this.serviceData = null;
		this.invocations = new ArrayList<Invocation>();
	}
	
	public InvocationManager(String requestURLString) 
	throws MalformedURLException, KarmaException {
		this.idList = new ArrayList<String>();
		this.idList.add("1");
		List<String> requestURLList = new ArrayList<String>();
		requestURLList.add(requestURLString);
		requestURLs = URLManager.getURLsFromStrings(requestURLList);
		if (requestURLs == null || requestURLs.size() == 0)
			throw new KarmaException("Cannot model a service without any request example.");
		
		this.serviceData = null;
		this.invocations = new ArrayList<Invocation>();
	}
	
	private void invokeAndGetResponse() {
		for (int i = 0; i < requestURLs.size(); i++) {
			URL url = requestURLs.get(i);
			String requestId = null;
			if (idList != null)
				requestId = idList.get(i);
			Request request = new Request(url);
			Invocation invocation = new Invocation(requestId, request);
			logger.info("Invoking the service " + request.getUrl().toString() + " ...");
			invocation.invokeAPI();
			invocations.add(invocation);
		}
		List<Table> invocationData = new ArrayList<Table>();
		for (Invocation inv : this.invocations) {
			invocationData.add(inv.getJointInputAndOutput());
		}
		
		logger.info("Integrating the results of all invocations ...");
		Table result = Table.union(invocationData);
		logger.info("Integrating finished.");
		this.serviceData = result;
	}
	
	public Table getServiceData(boolean includeURL, boolean includeInputAttributes, boolean includeOutputAttributes) {
		
		if (this.serviceData == null)
			invokeAndGetResponse();
		
		if (includeURL && includeInputAttributes && includeOutputAttributes)
			return this.serviceData;
		
		List<Attribute> headers = this.serviceData.getHeaders();
		List<List<String>> values = this.serviceData.getValues();

		Table newTable = new Table();
		List<Attribute> newHeader = new ArrayList<Attribute>();
		List<List<String>> newValues = new ArrayList<List<String>>();
		List<String> newRowIds = new ArrayList<String>(this.serviceData.getRowIds());
		
		List<Integer> includingColumns = new ArrayList<Integer>();
		
		if (headers != null) {
			if (includeURL && headers.size() > 0)
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
			List<String> rowVals = new ArrayList<String>();
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
	
	private List<Attribute> getInputAttributes() {
		List<Attribute> inAttributes = new ArrayList<Attribute>();
		
		Table serviceTable = getServiceData();
		for (Attribute p : serviceTable.getHeaders()) {
			if (p.getIOType().equalsIgnoreCase(IOType.INPUT)) {
				inAttributes.add(p);
			}
		}

		return inAttributes;
	}
	
	private List<Attribute> getOutputAttributes() {
		List<Attribute> outAttributes = new ArrayList<Attribute>();
		
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
	public Service getInitialServiceModel(String serviceName) {
		
		String guid = new RandomGUID().toString();
//		guid = "E9C3F8D3-F778-5C4B-E089-C1749D50AE1F";
		URL sampleUrl = requestURLs.get(0);
		
		if (sampleUrl == null)
			return null;

		Service service = null;
		if (serviceName == null || serviceName.trim().length() == 0)
			service = new Service(guid, sampleUrl);
		else
			service = new Service(guid, serviceName, sampleUrl);

		service.setMethod(HttpMethods.GET);

		service.setInputAttributes(getInputAttributes());
		service.setOutputAttributes(getOutputAttributes());
		
		return service;
	}
	
	
	public static void main(String[] args) {
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
			InvocationManager sb = new InvocationManager(ids, urls);
			Table tb = sb.getServiceData(false, false, true);
			
//			String str = tb.asCSV();
//			File f = new File("csv");
//			PrintWriter pw = new PrintWriter(f);
//			pw.write(str);
//			pw.close();
			
			logger.debug(tb.getPrintInfo());

			Service service = sb.getInitialServiceModel(null);
			
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

	}


}
