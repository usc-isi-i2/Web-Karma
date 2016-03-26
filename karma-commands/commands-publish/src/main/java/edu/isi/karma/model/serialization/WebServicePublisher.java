/*******************************************************************************
 * Copyright 2012 University of Southern California
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this.service file except in compliance with the License.
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

package edu.isi.karma.model.serialization;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.rep.sources.Attribute;
import edu.isi.karma.rep.sources.AttributeRequirement;
import edu.isi.karma.rep.sources.IOType;
import edu.isi.karma.rep.sources.WebService;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class WebServicePublisher extends SourcePublisher {

	static Logger logger = LoggerFactory.getLogger(WebServicePublisher.class);
	
	private WebService service;
	private Model model = null;
	private String contextId;
	public WebServicePublisher(WebService service, String contextId) {
		this.service = service;
		this.contextId = contextId;
	}

	@Override
	public Model exportToJenaModel() {
		
		Model model = ModelFactory.createDefaultModel();
		
		String baseNS = service.getUri();
		model.setNsPrefix("", baseNS);
		model.setNsPrefix(Prefixes.KARMA, Namespaces.KARMA);
		model.setNsPrefix(Prefixes.RDF, Namespaces.RDF);
		model.setNsPrefix(Prefixes.RDFS, Namespaces.RDFS);
		model.setNsPrefix(Prefixes.HRESTS, Namespaces.HRESTS);
		model.setNsPrefix(Prefixes.SWRL, Namespaces.SWRL);

		addInvocationPart(model);
		
		return model;
		
	}
	
	public Model generateInputPart() {
		
		Model model = ModelFactory.createDefaultModel();
		
		String baseNS = service.getUri();
		model.setNsPrefix("", baseNS);
		model.setNsPrefix(Prefixes.KARMA, Namespaces.KARMA);
		model.setNsPrefix(Prefixes.RDF, Namespaces.RDF);
		model.setNsPrefix(Prefixes.RDFS, Namespaces.RDFS);
		model.setNsPrefix(Prefixes.HRESTS, Namespaces.HRESTS);
		model.setNsPrefix(Prefixes.SWRL, Namespaces.SWRL);

		addInput(model, false);
		
		return model;
		
	}

	public Model generateOutputPart() {
		
		Model model = ModelFactory.createDefaultModel();
		
		String baseNS = service.getUri();
		model.setNsPrefix("", baseNS);
		model.setNsPrefix(Prefixes.KARMA, Namespaces.KARMA);
		model.setNsPrefix(Prefixes.RDF, Namespaces.RDF);
		model.setNsPrefix(Prefixes.RDFS, Namespaces.RDFS);
		model.setNsPrefix(Prefixes.SWRL, Namespaces.SWRL);

		addOutput(model, false);
		
		return model;
		
	}
	
	/**
	 * 
	 * @param lang The language in which to write the model is specified by the lang argument. 
	 * Predefined values are "RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE", (and "TTL") and "N3". 
	 * The default value, represented by null is "RDF/XML".
	 * @throws FileNotFoundException
	 */
	@Override
	public void publish(String lang, boolean writeToFile) throws FileNotFoundException {
		
		WebService existingService = WebServiceLoader.getInstance().getServiceByAddress(service.getAddress());
		if (existingService != null) 
			WebServiceLoader.getInstance().deleteSourceByUri(existingService.getUri());
		
		if (this.model == null)
			model = exportToJenaModel();
		
		// update the repository active model
		Repository.Instance().addModel(model, service.getUri());

		// write the model to the file
		if (writeToFile) 
			writeToFile(lang);
	}
	
	@Override
	public void writeToFile(String lang) throws FileNotFoundException {
		if (this.model == null)
			model = exportToJenaModel();
		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(contextId);
		String service_desc_file = contextParameters.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) +
										Repository.Instance().SERVICE_REPOSITORY_REL_DIR + 
		 							this.service.getId() +
									Repository.Instance().getFileExtension(lang);


		OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(service_desc_file));
		model.write(output,lang);		
		
	}
	
	private void addInvocationPart(Model model) {
		
		String baseNS = model.getNsPrefixURI("");
		// resources
		Resource service_resource = model.createResource(Namespaces.KARMA + "Service");
		Resource variavle_resource = model.createResource(Namespaces.SWRL + "Variable");

		// properties
		Property rdf_type = model.createProperty(Namespaces.RDF , "type");
		Property has_address = model.createProperty(Namespaces.HRESTS, "hasAddress");
		Property has_method = model.createProperty(Namespaces.HRESTS, "hasMethod");
		Property has_input = model.createProperty(Namespaces.KARMA, "hasInput");
		Property has_output = model.createProperty(Namespaces.KARMA, "hasOutput");
		Property has_name = model.createProperty(Namespaces.KARMA, "hasName");
		Property has_variable = model.createProperty(Namespaces.KARMA, "hasVariable");
		
		// rdf datatypes
		String uri_template = Namespaces.HRESTS + "URITemplate";

		Resource myservice = model.createResource(baseNS + "");
		Literal service_address = model.createTypedLiteral(service.getAddress(), uri_template);
		myservice.addProperty(rdf_type, service_resource);
		myservice.addProperty(has_address, service_address);
		if (service.getName().length() > 0)
			myservice.addProperty(has_name, service.getName());
		
		if (service.getMethod().length() > 0)
			myservice.addProperty(has_method, service.getMethod());
		if (service.getAddress().length() > 0) {
			Literal operation_address_literal = model.createTypedLiteral(service.getAddress(), uri_template);
			myservice.addLiteral(has_address, operation_address_literal);
		}
		
		if (service.getVariables() != null)
		for (int i = 0; i < service.getVariables().size(); i++) {
			Resource my_variable = model.createResource(baseNS + service.getVariables().get(i).toString());
			my_variable.addProperty(rdf_type, variavle_resource);
			myservice.addProperty(has_variable, my_variable);
		}
			
		Resource my_input = addInput(model, true);
		if (my_input != null) myservice.addProperty(has_input, my_input);

		Resource my_output = addOutput(model, true);
		if (my_output != null) myservice.addProperty(has_output, my_output);

	}
	
	private Resource addInput(Model model, boolean includeAttributeDetails) {
		
		Resource my_input = null;
		
		String baseNS = model.getNsPrefixURI("");
		// resources
		Resource input_resource = model.createResource(Namespaces.KARMA + "Input");

		// properties
		Property rdf_type = model.createProperty(Namespaces.RDF , "type");
		Property has_attribute = model.createProperty(Namespaces.KARMA, "hasAttribute");
		Property has_mandatory_attribute = model.createProperty(Namespaces.KARMA, "hasMandatoryAttribute");
		Property has_optional_attribute = model.createProperty(Namespaces.KARMA, "hasOptionalAttribute");

		if (service.getInputAttributes() != null) {
			
			my_input = model.createResource(baseNS + "input");  
			if (!service.getInputAttributes().isEmpty()) {
				my_input.addProperty(rdf_type, input_resource);
			}
			for (int i = 0; i < service.getInputAttributes().size(); i++) {
				
				Attribute att = service.getInputAttributes().get(i);
				Resource my_attribute = model.createResource(baseNS + att.getId());
				
				if (includeAttributeDetails)
					addAttributeDetails(model, my_attribute, att);
				
				if (att.getRequirement() == AttributeRequirement.NONE)
					my_input.addProperty(has_attribute, my_attribute);
				else if (att.getRequirement() == AttributeRequirement.MANDATORY)
					my_input.addProperty(has_mandatory_attribute, my_attribute);
				else if (att.getRequirement() == AttributeRequirement.OPTIONAL)
					my_input.addProperty(has_optional_attribute, my_attribute);

			}

			addModelPart(model, my_input, service.getInputModel());
		}
		
		return my_input;
	}

	private Resource addOutput(Model model, boolean includeAttributeDetails) {
		
		Resource my_output = null;
		
		String baseNS = model.getNsPrefixURI("");
		// resources
		Resource output_resource = model.createResource(Namespaces.KARMA + "Output");

		// properties
		Property rdf_type = model.createProperty(Namespaces.RDF , "type");
		Property has_attribute = model.createProperty(Namespaces.KARMA, "hasAttribute");
		Property has_mandatory_attribute = model.createProperty(Namespaces.KARMA, "hasMandatoryAttribute");
		Property has_optional_attribute = model.createProperty(Namespaces.KARMA, "hasOptionalAttribute");

		if (service.getOutputAttributes() != null) {
			my_output = model.createResource(baseNS + "output");  
			if (!service.getOutputAttributes().isEmpty()) {
				my_output.addProperty(rdf_type, output_resource);
			}
			for (int i = 0; i < service.getOutputAttributes().size(); i++) {
				
				Attribute att = service.getOutputAttributes().get(i);
				Resource my_attribute = model.createResource(baseNS + att.getId());
				
				if (includeAttributeDetails)
					addAttributeDetails(model, my_attribute, att);

				if (att.getRequirement() == AttributeRequirement.NONE)
					my_output.addProperty(has_attribute, my_attribute);
				else if (att.getRequirement() == AttributeRequirement.MANDATORY)
					my_output.addProperty(has_mandatory_attribute, my_attribute);
				else if (att.getRequirement() == AttributeRequirement.OPTIONAL)
					my_output.addProperty(has_optional_attribute, my_attribute);

			}
			addModelPart(model, my_output, service.getOutputModel());
		}
		return my_output;
	}

	private void addAttributeDetails(Model model, Resource my_attribute, Attribute att) {

		Resource attribute_resource = model.createResource(Namespaces.KARMA + "Attribute");

		Property rdf_type = model.createProperty(Namespaces.RDF , "type");
		Property has_name = model.createProperty(Namespaces.KARMA, "hasName");
		Property is_grounded_in = model.createProperty(Namespaces.HRESTS, "isGroundedIn");

		my_attribute.addProperty(rdf_type, attribute_resource);
		my_attribute.addProperty(has_name, att.getName());
//			my_part.addProperty(model_reference, XSDDatatype.XSDstring.getURI());
		
		if (att.getIOType() == IOType.INPUT) {
			String rdf_plain_literal = Namespaces.RDF + "PlainLiteral";
			Literal ground_literal = model.createTypedLiteral(att.getGroundedIn(), rdf_plain_literal);
			my_attribute.addLiteral(is_grounded_in, ground_literal);
		}
		
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getDefault();
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(contextParameters.getId());
		String serviceUri = modelingConfiguration.getKarmaServicePrefix() + "CDA81BE4-DD77-E0D3-D033-FC771B2F4800" + "#";
		WebService service = WebServiceLoader.getInstance().getSourceByUri(serviceUri);
		
		String service_file = contextParameters.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) +
									Repository.Instance().SERVICE_REPOSITORY_REL_DIR + 
									"service" + 
									Repository.Instance().getFileExtension(SerializationLang.N3);
		
		String service_input_file = contextParameters.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) +
									Repository.Instance().SERVICE_REPOSITORY_REL_DIR + 
									"input" + 
									Repository.Instance().getFileExtension(SerializationLang.N3);

		String service_output_file = contextParameters.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) +
									Repository.Instance().SERVICE_REPOSITORY_REL_DIR + 
									"output" + 
									Repository.Instance().getFileExtension(SerializationLang.N3);

		OutputStreamWriter outputService = new OutputStreamWriter(new FileOutputStream(service_file));
		OutputStreamWriter outputServiceInput = new OutputStreamWriter(new FileOutputStream(service_input_file));
		OutputStreamWriter outputServiceOutput = new OutputStreamWriter(new FileOutputStream(service_output_file));

		WebServicePublisher webServicePublisher = new WebServicePublisher(service, contextParameters.getId());
		com.hp.hpl.jena.rdf.model.Model model = webServicePublisher.exportToJenaModel();
		com.hp.hpl.jena.rdf.model.Model inputModel = webServicePublisher.generateInputPart();
		com.hp.hpl.jena.rdf.model.Model outputModel = webServicePublisher.generateOutputPart();
		
		model.write(outputService,SerializationLang.N3);		
		inputModel.write(outputServiceInput,SerializationLang.N3);		
		outputModel.write(outputServiceOutput,SerializationLang.N3);		
	}
}
