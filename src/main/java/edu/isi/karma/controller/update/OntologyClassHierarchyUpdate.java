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
package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.isi.karma.view.VWorkspace;

public class OntologyClassHierarchyUpdate extends AbstractUpdate {

	private OntModel model;
	
	private static Logger logger = LoggerFactory
			.getLogger(OntologyClassHierarchyUpdate.class.getSimpleName());

	public enum JsonKeys {
		data, URI, metadata, children
	}

	public OntologyClassHierarchyUpdate(OntModel model) {
		this.model = model;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		Set<String> classesAdded = new HashSet<String>();
		
		Map<String, String> prefixMap = vWorkspace.getWorkspace().getOntologyManager().getPrefixMap();

		try {
			JSONArray dataArray = new JSONArray();

			ExtendedIterator<OntClass> iter = model.listNamedClasses();
			while (iter.hasNext()) {
				OntClass cls = iter.next();
				if ((cls.hasSuperClass())
						|| classesAdded.contains(cls.getURI())) {
					// Need to check if it has a non-anonymous superclass
					boolean flag = false;
					ExtendedIterator<OntClass> superClasses = cls
							.listSuperClasses();
					while (superClasses.hasNext()) {
						OntClass clss = superClasses.next();
						if (!clss.isAnon() && !clss.getURI().equals("http://www.w3.org/2000/01/rdf-schema#Resource"))
							flag = true;
					}
					if (flag) {
						continue;
					}
				}

				JSONObject classObject = new JSONObject();

				if (cls.hasSubClass()) {
					JSONArray childrenArray = new JSONArray();
					addSubclassChildren(cls, childrenArray, 0, classesAdded, prefixMap);
					classObject.put(JsonKeys.children.name(), childrenArray);
				}

				String pr = prefixMap.get(cls.getNameSpace());
				if(pr != null && !pr.equals(""))
					classObject.put(JsonKeys.data.name(), pr + ":" + cls.getLocalName());
				else
					classObject.put(JsonKeys.data.name(), cls.getLocalName());
				classesAdded.add(cls.getURI());

				JSONObject metadataObject = new JSONObject();
				metadataObject.put(JsonKeys.URI.name(), cls.getURI());
				classObject.put(JsonKeys.metadata.name(), metadataObject);

				dataArray.put(classObject);

			}

			// Prepare the output JSON
			JSONObject outputObject = new JSONObject();
			outputObject.put(GenericJsonKeys.updateType.name(),
					"OntologyClassHierarchyUpdate");
			outputObject.put(JsonKeys.data.name(), dataArray);

			pw.println(outputObject.toString(4));

		} catch (JSONException e) {
			logger.error("Error occured while creating JSON", e);
		}

	}

	private void addSubclassChildren(OntClass clazz, JSONArray childrenArray,
			int level, Set<String> classesAdded, Map<String, String> prefixMap) throws JSONException {

		// logger.debug("Adding children for " + clazz.getLocalName() +
		// " at level " + level);

		ExtendedIterator<OntClass> subclasses = clazz.listSubClasses();
		while (subclasses.hasNext()) {
			OntClass subclass = subclasses.next();
			classesAdded.add(subclass.getLocalName());

			JSONObject classObject = new JSONObject();
			String pr = prefixMap.get(subclass.getNameSpace());
			if (pr != null && !pr.equals(""))
				classObject.put(JsonKeys.data.name(), pr + ":" + subclass.getLocalName());
			else
				classObject.put(JsonKeys.data.name(), subclass.getLocalName());
			JSONObject metadataObject = new JSONObject();
			metadataObject.put(JsonKeys.URI.name(), subclass.getURI());
			classObject.put(JsonKeys.metadata.name(), metadataObject);

			if (subclass.hasSubClass()) {
				JSONArray childrenArraySubClass = new JSONArray();
				addSubclassChildren(subclass, childrenArraySubClass, level + 1,
						classesAdded, prefixMap);
				classObject
						.put(JsonKeys.children.name(), childrenArraySubClass);
			}
			childrenArray.put(classObject);
		}
	}
}
