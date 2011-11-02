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

import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.view.VWorkspace;

public class OntologyClassHieararchyUpdate extends AbstractUpdate {

	private static Logger logger = LoggerFactory
			.getLogger(OntologyClassHieararchyUpdate.class.getSimpleName());

	public enum JsonKeys {
		data, URI, metadata, children
	}
	
	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		OntModel model = OntologyManager.Instance().getOntModel();
		Set<String> classesAdded = new HashSet<String>();

//		File f1 = new File("./Sample Data/OWL/Wiki.owl");
//		new ImportOntology(model, f1);
		
		try {
			JSONArray dataArray = new JSONArray();
			
			ExtendedIterator<OntClass> iter = model.listNamedClasses();
			while (iter.hasNext()) {
				OntClass cls = iter.next();
//				System.out.println("Class: " + cls.getURI());
				if ((cls.hasSuperClass())
						|| classesAdded.contains(cls.getLocalName())) {
					// Need to check if it has a non-anonymous superclass
					boolean flag = false;
					ExtendedIterator<OntClass> superClasses = cls
							.listSuperClasses();
					while (superClasses.hasNext()) {
						OntClass clss = superClasses.next();
						if (!clss.isAnon())
							flag = true;
					}
					if (flag) {
//						System.out.println("Skipping!");
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
			outputObject.put(GenericJsonKeys.updateType.name(), "OntologyClassHieararchyUpdate");
			outputObject.put(JsonKeys.data.name(), dataArray);

			pw.println(outputObject.toString(4));

		} catch (JSONException e) {
			logger.error("Error occured while creating JSON", e);
		}

	}

	private void addSubclassChildren(OntClass clazz, JSONArray childrenArray,
			int level, Set<String> classesAdded) throws JSONException {

		// System.out.println("Adding children for " + clazz.getLocalName() +
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
				classObject.put(JsonKeys.children.name(), childrenArraySubClass);
			}
			childrenArray.put(classObject);
		}
	}
}
