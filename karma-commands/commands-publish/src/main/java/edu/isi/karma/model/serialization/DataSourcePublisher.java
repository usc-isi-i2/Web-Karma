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

package edu.isi.karma.model.serialization;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.metadata.SourceInformation;
import edu.isi.karma.rep.metadata.SourceInformation.InfoAttribute;
import edu.isi.karma.rep.sources.Attribute;
import edu.isi.karma.rep.sources.DataSource;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class DataSourcePublisher extends SourcePublisher {

	static Logger logger = LoggerFactory.getLogger(DataSourcePublisher.class);

	private DataSource source;
	private Model model = null;
	private RepFactory factory;
	private SourceInformation sourceInfo;
	
	//MARIAM
	//I had to add factory, so that I can get to the columnName
	//I tried to do it in a nicer way but couldn't figure out how to add it to the Attribute
	public DataSourcePublisher(DataSource source, RepFactory factory, SourceInformation sourceInfo) {
		this.source = source;
		this.factory=factory;
		this.sourceInfo = sourceInfo;
	}
	
	@Override
	public Model exportToJenaModel() {
		
		Model model = ModelFactory.createDefaultModel();
		
		String baseNS = source.getUri();
		model.setNsPrefix("", baseNS);
		model.setNsPrefix(Prefixes.KARMA, Namespaces.KARMA);
		model.setNsPrefix(Prefixes.RDF, Namespaces.RDF);
		model.setNsPrefix(Prefixes.RDFS, Namespaces.RDFS);
		model.setNsPrefix(Prefixes.SWRL, Namespaces.SWRL);

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
	@Override
	public void publish(String lang, boolean writeToFile) throws FileNotFoundException {
		
		//TODO: how to see if a model of the same source exists or not
		// maybe we can use a combination of source name, address, ...
		
		if (this.model == null)
			model = exportToJenaModel();
		
		// update the repository active model
		Repository.Instance().addModel(this.model, source.getUri());

		// write the model to the file
		if (writeToFile)
			writeToFile(lang);
	}
	
	@Override
	public void writeToFile(String lang) throws FileNotFoundException {
		if (this.model == null)
			model = exportToJenaModel();

		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getDefault();
		String source_desc_file = contextParameters.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) +
									Repository.Instance().SOURCE_REPOSITORY_REL_DIR + 
		 							this.source.getName() + "_" + this.source.getId() +
									Repository.Instance().getFileExtension(lang);


		OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(source_desc_file));
		model.write(output,lang);		
		
	}
	
	private void addSourceInfoPart(Model model) {
		
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
			Resource my_variable = model.createResource(baseNS + this.source.getVariables().get(i));
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
		// Add source information if any present
		if(sourceInfo != null) {
			Map<InfoAttribute, String> attributeValueMap = sourceInfo.getAttributeValueMap();
			for (Map.Entry<InfoAttribute, String> infoAttributeStringEntry : attributeValueMap.entrySet()) {
				Property attrProp = model.createProperty(Namespaces.KARMA, "hasSourceInfo_" + infoAttributeStringEntry.getKey().name());
				my_source.addProperty(attrProp, infoAttributeStringEntry.getValue());
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
