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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.isi.karma.util.RandomGUID;

public class Service {
	
	private String name;
	private String address;
	private String description;

	private List<Operation> operations;

	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return this.address;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Operation> getOperations() {
		return operations;
	}

	public void setOperations(List<Operation> operations) {
		this.operations = operations;
	}

	public Model publish(String path) throws FileNotFoundException {
		
		OntModel model = ModelFactory.createOntologyModel();
		
		String serviceGUID = new RandomGUID().toString();
		String defaultNS = Namespaces.KARMA + serviceGUID + "#";
		model.setNsPrefix("", defaultNS);
		model.setNsPrefix(Prefixes.RDF, Namespaces.RDF);
		model.setNsPrefix(Prefixes.RDFS, Namespaces.RDFS);
		model.setNsPrefix(Prefixes.SAWSDL, Namespaces.SAWSDL);
		model.setNsPrefix(Prefixes.MSM, Namespaces.MSM);
		model.setNsPrefix(Prefixes.HRESTS, Namespaces.HRESTS);

		// resources
		Resource service = model.createResource(Namespaces.MSM + "Service");
		Resource operation = model.createResource(Namespaces.MSM + "Operation");
		Resource message_content = model.createResource(Namespaces.MSM + "MessageContent");
		Resource message_part = model.createResource(Namespaces.MSM + "MessagePart");

		// properties
		Property rdf_type = model.createProperty(Namespaces.RDF , "type");
		Property has_operation = model.createProperty(Namespaces.MSM, "hasOperation");
		Property has_address = model.createProperty(Namespaces.MSM, "hasAddress");
		Property has_input = model.createProperty(Namespaces.MSM, "hasInput");
		Property has_output = model.createProperty(Namespaces.MSM, "hasOutput");
		Property has_part = model.createProperty(Namespaces.MSM, "hasPart");
		Property has_name = model.createProperty(Namespaces.MSM, "hasName");
//		Property model_reference = model.createProperty(Namespaces.SAWSDL, "modelReference");
		Property is_grounded_in = model.createProperty(Namespaces.MSM, "isGroundedIn");
		
		// rdf datatypes
		String uri_template = Namespaces.HRESTS + "URITemplate";
		String rdf_plain_literal = Namespaces.RDF + "PlainLiteral";


		Resource my_service = model.createResource(defaultNS + "service");
		Literal service_address = model.createTypedLiteral(this.getAddress(), uri_template);
		my_service.addProperty(rdf_type, (RDFNode)service);
		my_service.addProperty(has_address, service_address);
		
		if (this.getOperations() != null)
		for (Operation op: this.getOperations()) {
			Resource my_operation = model.createResource(defaultNS + op.getName());
			my_service.addProperty(has_operation, (RDFNode)my_operation);
			my_operation.addProperty(rdf_type, (RDFNode)operation);
			
			String operation_address = "";
			
			if (op.getInputParams() != null) {
				Resource my_input = model.createResource(defaultNS + op.getName() + "_input");  
				if (op.getInputParams().size() > 0) {
					my_operation.addProperty(has_input, (RDFNode)my_input);
					my_input.addProperty(rdf_type, (RDFNode)message_content);
				}
				for (int i = 0; i < op.getInputParams().size(); i++) {
					
					// building the operation address template
					String groundVar = "p" + String.valueOf(i);
					if (operation_address.trim().length() > 0) operation_address += "&";
					operation_address += op.getInputParams().get(i).getName();
					operation_address += "={" + groundVar + "}";
					
					Resource my_part = model.createResource(my_input.getURI() + "_part" + String.valueOf(i+1));
					my_input.addProperty(has_part, (RDFNode)my_part);
					my_part.addProperty(rdf_type, (RDFNode)message_part);
					my_part.addProperty(has_name, op.getInputParams().get(i).getName());
//					my_part.addProperty(model_reference, XSDDatatype.XSDstring.getURI());
					
					Literal ground_literal = model.createTypedLiteral(groundVar, rdf_plain_literal);
					my_part.addLiteral(is_grounded_in, ground_literal);
				}
			}
			if (op.getOutputParams() != null) {
				Resource my_output = model.createResource(defaultNS + op.getName() + "_output");  
				if (op.getOutputParams().size() > 0) {
					my_operation.addProperty(has_output, (RDFNode)my_output);
					my_output.addProperty(rdf_type, (RDFNode)message_content);
				}
				for (int i = 0; i < op.getOutputParams().size(); i++) {
					Resource my_part = model.createResource(my_output.getURI() + "_part" + String.valueOf(i+1));
					my_output.addProperty(has_part, (RDFNode)my_part);
					my_part.addProperty(rdf_type, (RDFNode)message_part);
					my_part.addProperty(has_name, op.getOutputParams().get(i).getName());
//					my_part.addProperty(model_reference, XSDDatatype.XSDstring.getURI());
				}
			}
			
			Literal operation_address_literal = model.createTypedLiteral(operation_address, uri_template);
			my_operation.addLiteral(has_address, operation_address_literal);
			
		}
		
		String service_desc_file = ServiceRepository.Instance().SERVICE_REPOSITORY_DIR + serviceGUID + ".n3";
		OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(service_desc_file));

		model.write(output,"N3");
		return model;
		
	}
	
	public void print() {
		System.out.println("address: " + this.getAddress());
		System.out.println("name: " + this.getName());
		System.out.println("description: " + this.getDescription());
		System.out.println("----------------------");
		System.out.println("operations: ");
		for (Operation op: getOperations())
			op.print();
	}
	
	public static void main(String[] args) {
		
		try {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
