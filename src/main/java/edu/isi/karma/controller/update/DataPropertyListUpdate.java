package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.view.VWorkspace;

public class DataPropertyListUpdate extends AbstractUpdate {

	private static Logger logger = LoggerFactory
			.getLogger(DatabaseTablesListUpdate.class.getSimpleName());

	public enum JsonKeys {
		data, URI, metadata, children
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		Map<String, Set<String>> domainMap = new HashMap<String, Set<String>>();
		OntModel model = OntologyManager.Instance().getOntModel();

//		File f1 = new File("./Sample Data/OWL/Wiki.owl");
//		new ImportOntology(model, f1);

		ExtendedIterator<DatatypeProperty> props = model
				.listDatatypeProperties();

		while (props.hasNext()) {
			DatatypeProperty prop = props.next();
			System.out.println("Prop: " + prop.getURI());
			List<String> domains = OntologyManager.Instance()
					.getDomainsGivenProperty(prop.getURI(), true);
			for (String label : domains) {
				Set<String> map = domainMap.get(label);

				if (map == null) {
					map = new HashSet<String>();
					domainMap.put(label, map);
				}
				map.add(prop.getURI());
			}
		}

		try {
			JSONArray dataArray = new JSONArray();
			for (String domain : domainMap.keySet()) {
				Set<String> properties = domainMap.get(domain);
				if (properties != null && properties.size() != 0) {
					JSONObject domainObj = new JSONObject();
					domainObj.put(JsonKeys.data.name(), "Domain: "
							+ model.getResource(domain).getLocalName());

					JSONObject metadataObject = new JSONObject();
					metadataObject.put(JsonKeys.URI.name(), domain);
					domainObj.put(JsonKeys.metadata.name(), metadataObject);

					JSONArray childrenObj = new JSONArray();
					for (String property : properties) {
						JSONObject propObject = new JSONObject();
						propObject.put(JsonKeys.data.name(), model.getResource(property)
								.getLocalName());

						JSONObject metadataObjectProp = new JSONObject();
						metadataObjectProp.put(JsonKeys.URI.name(), property);
						propObject.put(JsonKeys.metadata.name(), metadataObjectProp);
						childrenObj.put(propObject);
					}

					domainObj.put(JsonKeys.children.name(), childrenObj);
					dataArray.put(domainObj);
				}
			}

			// Prepare the output JSON
			JSONObject outputObject = new JSONObject();
			outputObject.put(GenericJsonKeys.updateType.name(),
					"DataPropertyListUpdate");
			outputObject.put(JsonKeys.data.name(), dataArray);

			pw.println(outputObject.toString(4));
		} catch (JSONException e) {
			logger.error("Error occured while creating JSON!", e);
		}
	}

}
