/*******************************************************************************
 * Copyright 2012 University of Southern California
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this.source file except in compliance with the License.
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
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.metadata.SourceInformation;
import edu.isi.karma.rep.metadata.SourceInformation.InfoAttribute;

public class SourcePublisher {

	static Logger logger = Logger.getLogger(SourcePublisher.class);

	private Source source;
	private Model model = null;
	private String sourceDescription;
	private RepFactory factory;
	private List<String> transformationCommandsJSON;
	private SourceInformation sourceInfo;
	
	//MARIAM
	//I had to add factory, so that I can get to the columnName
	//I tried to do it in a nicer way but couldn't figure out how to add it to the Attribute
	public SourcePublisher(Source source, String sourceDescription, RepFactory factory, List<String> transformationCommandJSON, SourceInformation sourceInfo) {
		this.source = source;
		this.sourceDescription=sourceDescription;
		this.factory=factory;
		this.transformationCommandsJSON = transformationCommandJSON;
		this.sourceInfo = sourceInfo;
	}
	
	public Model generateModel() {
		
		Model model = ModelFactory.createDefaultModel();
		
		String baseNS = source.getUri();
		model.setNsPrefix("", baseNS);
		model.setNsPrefix(Prefixes.KARMA, Namespaces.KARMA);
		model.setNsPrefix(Prefixes.RDF, Namespaces.RDF);
		model.setNsPrefix(Prefixes.RDFS, Namespaces.RDFS);
		model.setNsPrefix(Prefixes.SWRL, Namespaces.SWRL);

		addTransformationCommandsHistory(model);
		addSourceInfoPart(model);
		
		return model;
		
	}
	
	/**
	 * 
	 * @param lang The language in which to write the model is specified by the lang argument. 
	 * Predefined values are "RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE", (and "TTL") and "N3". 
	 * The default value, represented by null is "RDF/XML".
	 * @throws FileNotFoundException
	 */
	public void publish(String lang, boolean writeToFile) throws FileNotFoundException {
		
		//TODO: how to see if a model of the same source exists or not
		// maybe we can use a combination of source name, address, ...
		
		if (this.model == null)
			model = generateModel();
		
		// update the repository active model
		Repository.Instance().addModel(this.model, source.getUri());

		// write the model to the file
		if (writeToFile)
			writeToFile(lang);
	}
	
	public void writeToFile(String lang) throws FileNotFoundException {
		if (this.model == null)
			model = generateModel();
		
		String source_desc_file = Repository.Instance().SOURCE_REPOSITORY_DIR + 
		 							this.source.getName() + "_" + this.source.getId() +
									Repository.Instance().getFileExtension(lang);


		OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(source_desc_file));
		model.write(output,lang);		
		
	}
	
	public void addSourceInfoPart(Model model) {
		
		String baseNS = model.getNsPrefixURI("");
		// resources
		Resource source_resource = model.createResource(Namespaces.KARMA + "Source");
		Resource attribute_resource = model.createResource(Namespaces.KARMA + "Attribute");
		Resource variavle_resource = model.createResource(Namespaces.SWRL + "Variable");

		// properties
		Property rdf_type = model.createProperty(Namespaces.RDF , "type");
		Property has_attribute = model.createProperty(Namespaces.KARMA, "hasAttribute");
		Property has_name = model.createProperty(Namespaces.KARMA, "hasName");
		Property has_columnName = model.createProperty(Namespaces.KARMA, "hasColumnName");
		Property has_variable = model.createProperty(Namespaces.KARMA, "hasVariable");

		Resource my_source = model.createResource(baseNS + "");
		my_source.addProperty(rdf_type, source_resource);
		if (this.source.getName().length() > 0)
			my_source.addProperty(has_name, this.source.getName());

		if (this.source.getVariables() != null)
		for (int i = 0; i < this.source.getVariables().size(); i++) {
			Resource my_variable = model.createResource(baseNS + this.source.getVariables().get(i).toString());
			my_variable.addProperty(rdf_type, variavle_resource);
			my_source.addProperty(has_variable, my_variable);
		}

		if (this.source.getAttributes() != null) {
			for (int i = 0; i < this.source.getAttributes().size(); i++) {
				
				Attribute att = this.source.getAttributes().get(i);
				Resource my_attribute = model.createResource(baseNS + att.getId());
				
				my_source.addProperty(has_attribute, my_attribute);
				my_attribute.addProperty(rdf_type, attribute_resource);
				my_attribute.addProperty(has_name, att.getName());
				my_attribute.addProperty(has_columnName, factory.getHNode(att.gethNodeId()).getColumnName());

			}
			addModelPart(model, my_source, this.source.getModel());
		}
		//for source description
		Property hasSourceDesc = model.createProperty(Namespaces.KARMA, "hasSourceDescription");
		sourceDescription=sourceDescription.replaceAll("\n", " ").replaceAll("\r", " ");
		my_source.addProperty(hasSourceDesc, sourceDescription);
		
		// Add transformations
		Property has_columnTransformation = model.createProperty(Namespaces.KARMA, "hasColumnTransformation");
		for(String commJson : transformationCommandsJSON)
			my_source.addProperty(has_columnTransformation, commJson);
		
		// Add source information if any present
		if(sourceInfo != null) {
			Map<InfoAttribute, String> attributeValueMap = sourceInfo.getAttributeValueMap();
			for (InfoAttribute attr : attributeValueMap.keySet()) {
				Property attrProp = model.createProperty(Namespaces.KARMA, "hasSourceInfo_" + attr.name());
				my_source.addProperty(attrProp, attributeValueMap.get(attr));
			}
		}
		
	}
	
	private void addTransformationCommandsHistory(Model model2) {
		
	}
	public void addModelPart(Model model, Resource resource, edu.isi.karma.service.Model semanticModel) {

		if (semanticModel == null) {
			logger.info("The semantic model is null");
			return;
		}
		
		String baseNS = model.getNsPrefixURI("");
		
		Resource model_resource = model.createResource(Namespaces.KARMA + "Model");
		Resource class_atom_resource = model.createResource(Namespaces.SWRL + "ClassAtom");
		Resource individual_property_atom_resource = model.createResource(Namespaces.SWRL + "IndividualPropertyAtom");

		Property rdf_type = model.createProperty(Namespaces.RDF , "type");
		Property has_model = model.createProperty(Namespaces.KARMA, "hasModel");
		Property has_atom = model.createProperty(Namespaces.KARMA, "hasAtom");

		Property class_predicate = model.createProperty(Namespaces.SWRL, "classPredicate");
		Property property_predicate = model.createProperty(Namespaces.SWRL, "propertyPredicate");
		Property has_argument1 = model.createProperty(Namespaces.SWRL, "argument1");
		Property has_argument2 = model.createProperty(Namespaces.SWRL, "argument2");

		Resource my_model = model.createResource(baseNS + semanticModel.getId());
		my_model.addProperty(rdf_type, model_resource);
		resource.addProperty(has_model, my_model);
		
		if (semanticModel != null) {
			for (Atom atom : semanticModel.getAtoms()) {
				if (atom instanceof ClassAtom) {
					ClassAtom classAtom = (ClassAtom)atom;
					
					Resource r = model.createResource();
					r.addProperty(rdf_type, class_atom_resource);
					
					if (classAtom.getClassPredicate().getPrefix() != null && classAtom.getClassPredicate().getNs() != null)
						model.setNsPrefix(classAtom.getClassPredicate().getPrefix(), classAtom.getClassPredicate().getNs());
					Resource className = model.createResource(classAtom.getClassPredicate().getUriString());
					r.addProperty(class_predicate, className);
					
					Resource arg1 = model.getResource(baseNS + classAtom.getArgument1().getAttOrVarId());
					r.addProperty(has_argument1, arg1);
					
					my_model.addProperty(has_atom, r);
				}
				else if (atom instanceof PropertyAtom) {
					PropertyAtom propertyAtom = (PropertyAtom)atom;
					
					Resource r = model.createResource();
					r.addProperty(rdf_type, individual_property_atom_resource);
					
					if (propertyAtom.getPropertyPredicate().getPrefix() != null && propertyAtom.getPropertyPredicate().getNs() != null)
						model.setNsPrefix(propertyAtom.getPropertyPredicate().getPrefix(), propertyAtom.getPropertyPredicate().getNs());
					Resource propertyName = model.createResource(propertyAtom.getPropertyPredicate().getUriString());
					r.addProperty(property_predicate, propertyName);
					
					Resource arg1 = model.getResource(baseNS + propertyAtom.getArgument1().getAttOrVarId());
					r.addProperty(has_argument1, arg1);
					
					Resource arg2 = model.getResource(baseNS + propertyAtom.getArgument2().getAttOrVarId());
					r.addProperty(has_argument2, arg2);
					
					my_model.addProperty(has_atom, r);
				}
			}
		}
	}
	/*
	public static void main(String[] args) {
		Source source = new Source("mySource", Test.getGeoNamesNeighbourhoodTree());
		source.print();
		SourcePublisher sourcePublisher = new SourcePublisher(source,"");
		try {
			sourcePublisher.publish(Repository.Instance().LANG, true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
	*/
}
