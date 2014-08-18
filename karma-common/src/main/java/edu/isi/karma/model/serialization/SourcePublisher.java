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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.rep.model.Atom;
import edu.isi.karma.rep.model.ClassAtom;
import edu.isi.karma.rep.model.IndividualPropertyAtom;

public abstract class SourcePublisher {
	static Logger logger = LoggerFactory.getLogger(SourcePublisher.class);

	public abstract Model exportToJenaModel();
	public abstract void publish(String lang, boolean writeToFile) throws FileNotFoundException;	
	public abstract void writeToFile(String lang) throws FileNotFoundException;


	protected void addModelPart(Model model, Resource resource, edu.isi.karma.rep.model.Model semanticModel) {

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
					Resource className = model.createResource(classAtom.getClassPredicate().getUri());
					r.addProperty(class_predicate, className);

					Resource arg1 = model.getResource(baseNS + classAtom.getArgument1().getAttOrVarId());
					r.addProperty(has_argument1, arg1);

					my_model.addProperty(has_atom, r);
				}
				else if (atom instanceof IndividualPropertyAtom) {
					IndividualPropertyAtom propertyAtom = (IndividualPropertyAtom)atom;

					Resource r = model.createResource();
					r.addProperty(rdf_type, individual_property_atom_resource);

					if (propertyAtom.getPropertyPredicate().getPrefix() != null && propertyAtom.getPropertyPredicate().getNs() != null)
						model.setNsPrefix(propertyAtom.getPropertyPredicate().getPrefix(), propertyAtom.getPropertyPredicate().getNs());
					Resource propertyName = model.createResource(propertyAtom.getPropertyPredicate().getUri());
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
}
