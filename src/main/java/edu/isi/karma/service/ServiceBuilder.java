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

public class ServiceBuilder {

	static Logger logger = Logger.getLogger(ServiceBuilder.class);

	private List<URL> requestURLs;
	private List<Invocation> invocations;
	private String serviceName;
	private Table serviceData;
	
	public ServiceBuilder(String serviceName, List<String> requestURLStrings) 
	throws MalformedURLException, KarmaException {

		requestURLs = URLManager.getURLsFromStrings(requestURLStrings);
		if (requestURLs == null || requestURLs.size() == 0)
			throw new KarmaException("Cannot model a service without any request example.");
		
		this.serviceData = null;
		this.serviceName = serviceName;
		this.invocations = new ArrayList<Invocation>();
	}
	
	private void invokeAndGetResponse() {
		for (URL url : requestURLs) {
			Request request = new Request(url);
			Invocation invocation = new Invocation(request);
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
	public Service getInitialServiceModel() {
		
		Service service = new Service();
		
		URL sampleUrl = requestURLs.get(0);
		
		if (sampleUrl == null)
			return null;
		
		String address = URLManager.getServiceAddress(sampleUrl);
		
		String guid = new RandomGUID().toString();
//		guid = "E9C3F8D3-F778-5C4B-E089-C1749D50AE1F";
		service.setId(guid);
		service.setName(this.serviceName);
		service.setDescription("");
		service.setAddress(address);
		
		Operation op = new Operation("op1");
		
		String operationName = URLManager.getOperationName(sampleUrl);
		String operationAddress = URLManager.getOperationAddress(sampleUrl);

		op.setName(operationName);
		op.setAddress(operationAddress);
		op.setDescription("");
		op.setMethod(HttpMethods.GET);
		op.setInputAttributes(getInputAttributes());
		for (Attribute att : op.getInputAttributes())
			att.setId(op.getId() + "_" + att.getId());
		op.setOutputAttributes(getOutputAttributes());
		for (Attribute att : op.getOutputAttributes())
			att.setId(op.getId() + "_" + att.getId());
		
		List<Operation> opList = new ArrayList<Operation>();
		opList.add(op);
		
		service.setOperations(opList);
		return service;
	}
	
	
	public static void main(String[] args) {
//		String s1 = "http://colo-vm10.isi.edu:8080/DovetailService/GetSampleData?sourceName=KDD-02-B-TOSIG";
		String s1 = "http://api.geonames.org/neighbourhood?lat=40.78343&lng=-73.96625&username=taheriyan";
//		String s2 = "http://api.geonames.org/neighbourhood?lat=40.7&lng=-73.9&username=taheriyan";
//		String s3 = "http://api.geonames.org/neighbourhood?lat=40.9&lng=-73.9&username=taheriyan";

		List<String> urls = new ArrayList<String>();
		urls.add(s1);
//		urls.add(s2);
		
		try {
			ServiceBuilder sb = new ServiceBuilder("myService", urls);
			Table tb = sb.getServiceData(true, true, true);

			logger.debug(tb.getPrintInfo());

			Service service = sb.getInitialServiceModel();
			
			// just for test
			service.getOperations().get(0).getInputAttributes().get(0).sethNodeId("h1");
			service.getOperations().get(0).getInputAttributes().get(1).sethNodeId("h2");
			service.getOperations().get(0).getOutputAttributes().get(4).sethNodeId("h3");
			service.getOperations().get(0).getOutputAttributes().get(6).sethNodeId("h4");
			service.getOperations().get(0).getOutputAttributes().get(5).sethNodeId("h5");
			service.getOperations().get(0).getOutputAttributes().get(3).sethNodeId("h6");
			service.print();
			
			service.getOperations().get(0).updateModel(Test.getGeoNamesNeighbourhoodTree());
			
			ServicePublisher servicePublisher = new ServicePublisher(service);
			servicePublisher.publish();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


}
