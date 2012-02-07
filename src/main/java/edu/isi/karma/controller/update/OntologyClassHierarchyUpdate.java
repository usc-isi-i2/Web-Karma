package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.HashSet;
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

		try {
			JSONArray dataArray = new JSONArray();

			ExtendedIterator<OntClass> iter = model.listNamedClasses();
			while (iter.hasNext()) {
				OntClass cls = iter.next();
				if ((cls.hasSuperClass())
						|| classesAdded.contains(cls.getLocalName())) {
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
					addSubclassChildren(cls, childrenArray, 0, classesAdded);
					classObject.put(JsonKeys.children.name(), childrenArray);
				}

				classObject.put(JsonKeys.data.name(), cls.getLocalName());
				classesAdded.add(cls.getLocalName());

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
			int level, Set<String> classesAdded) throws JSONException {

		// logger.debug("Adding children for " + clazz.getLocalName() +
		// " at level " + level);

		ExtendedIterator<OntClass> subclasses = clazz.listSubClasses();
		while (subclasses.hasNext()) {
			OntClass subclass = subclasses.next();
			classesAdded.add(subclass.getLocalName());

			JSONObject classObject = new JSONObject();
			classObject.put(JsonKeys.data.name(), subclass.getLocalName());
			JSONObject metadataObject = new JSONObject();
			metadataObject.put(JsonKeys.URI.name(), subclass.getURI());
			classObject.put(JsonKeys.metadata.name(), metadataObject);

			if (subclass.hasSubClass()) {
				JSONArray childrenArraySubClass = new JSONArray();
				addSubclassChildren(subclass, childrenArraySubClass, level + 1,
						classesAdded);
				classObject
						.put(JsonKeys.children.name(), childrenArraySubClass);
			}
			childrenArray.put(classObject);
		}
	}
}
