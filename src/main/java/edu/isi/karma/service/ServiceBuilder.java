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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.http.HttpMethods;

import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

public class ServiceBuilder {

	private static final String SERVICE_NAME_PREFIX = "service";
	private static final String OPERATION_NAME_PREFIX = "operation";

	private List<URL> requestURLs;
	private List<Invocation> invocations;
	private String serviceName;
	
	public ServiceBuilder(String serviceName, List<String> requestURLStrings) 
	throws MalformedURLException, KarmaException {

		requestURLs = URLManager.getURLsFromStrings(requestURLStrings);
		if (requestURLs == null || requestURLs.size() == 0)
			throw new KarmaException("Cannot model a service without any request example.");
		
		this.serviceName = serviceName;
		this.invocations = new ArrayList<Invocation>();
	}
	
	private Table getServiceData(boolean includeURL) {
		for (URL url : requestURLs) {
			Request request = new Request(url);
			Invocation invocation = new Invocation(request);
			invocation.invokeAPI();
			invocations.add(invocation);
		}
		List<Table> invocationData = new ArrayList<Table>();
		for (Invocation inv : this.invocations) {
			invocationData.add(inv.getJointInputAndOutput(includeURL));
		}
		
		Table result = Table.union(invocationData);
		return result;
	}
	
	private List<Param> getInputParams() {
		List<Param> inParams = new ArrayList<Param>();
		
		//FIXME
//		List<String> uniqueParams = new ArrayList<String>();
//		
//		for (Invocation invocation : invocations) {
//			if (invocation == null || invocation.getRequest() == null ||
//					invocation.getRequest().getParams() == null)
//				continue;
//			
//			for (Param p : invocation.getRequest().getParams()) {
//				
//				if (p.getName() == null || p.getName().trim().length() == 0)
//					continue;
//				
//				if (uniqueParams.indexOf(p.getName()) == -1) {
//					inParams.add(new Param(p.getName()));
//					uniqueParams.add(p.getName());
//				}
//			}
//		}
		
		return inParams;
	}
	
	private List<Param> getOutputParams() {
		List<Param> outParams = new ArrayList<Param>();
		
		//FIXME
//		List<String> uniqueParams = new ArrayList<String>();
//		
//		for (Invocation invocation : invocations) {
//			if (invocation == null || invocation.getResponse() == null ||
//					invocation.getResponse().getParams() == null)
//				continue;
//			
//			for (Param p : invocation.getResponse().getParams()) {
//				
//				if (p.getName() == null || p.getName().trim().length() == 0)
//					continue;
//				
//				if (uniqueParams.indexOf(p.getName()) == -1) {
//					outParams.add(new Param(p));
//					uniqueParams.add(p.getName());
//				}
//			}
//		}
//		
		return outParams;
	}
	
	/**
	 * This method creates a new service model which includes only the 
	 * service endpoint, http method, input and output parameters
	 * @return
	 */
	private Service getInitialServiceModel() {
		
		Service service = new Service();
		
		URL sampleUrl = requestURLs.get(0);
		
		if (sampleUrl == null)
			return null;
		
		String address = URLManager.getServiceAddress(sampleUrl);
		
		service.setName(SERVICE_NAME_PREFIX + "_" + this.serviceName + "_");
		service.setDescription("");
		service.setAddress(address);
		
		Operation op = new Operation();
		
		String operationName = URLManager.getOperationName(sampleUrl);

		op.setName(OPERATION_NAME_PREFIX + "_" + operationName + "_");
		op.setDescription("");

		op.setMethod(HttpMethods.GET);
		op.setInputParams(getInputParams());
		op.setOutputParams(getOutputParams());
		
		List<Operation> opList = new ArrayList<Operation>();
		opList.add(op);
		
		service.setOperations(opList);
		return service;
	}
	
	public Worksheet generateWorksheet(Workspace workspace) throws KarmaException, IOException {

		if (workspace == null)
			throw new KarmaException("Workspace is null.");
		
		Worksheet worksheet = workspace.getFactory().createWorksheet(this.serviceName, workspace);
		Table serviceData = getServiceData(true);
		serviceData.populateEmptyWorksheet(worksheet, workspace.getFactory());
		
		
//		Service service = getInitialServiceModel();
		//TODO
//		worksheet.getMetadataContainer().setService(service);
		
		return worksheet;
	}
	
	public void populateWorksheet(Worksheet worksheet, RepFactory factory, String urlHNodeId) throws KarmaException {

		Table serviceData = getServiceData(true);
		serviceData.populateWorksheet(worksheet, factory, urlHNodeId);
		
		
//		Service service = getInitialServiceModel();
		//TODO
//		worksheet.getMetadataContainer().setService(service);
		
	}
	
	public static void main(String[] args) {
//		String s1 = "http://api.geonames.org/neighbourhood";
		String s1 = "http://api.geonames.org/neighbourhood?lat=40.78343&lng=-73.96625&username=taheriyan";
		String s2 = "http://api.geonames.org/neighbourhood?lat=40.7&lng=-73.9&username=taheriyan";
//		String s3 = "http://api.geonames.org/neighbourhood?lat=40.9&lng=-73.9&username=taheriyan";

		List<String> urls = new ArrayList<String>();
		urls.add(s1);
		urls.add(s2);
		
		try {
			ServiceBuilder sb = new ServiceBuilder("myService", urls);
			Table tb = sb.getServiceData(true);
			tb.print();

			Service service = sb.getInitialServiceModel();
			service.print();
			
//			service.publish(ServiceRepository.Instance().SERVICE_REPOSITORY_DIR);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


}
