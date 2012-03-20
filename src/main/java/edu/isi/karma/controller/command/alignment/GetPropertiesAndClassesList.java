package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.view.VWorkspace;

public class GetPropertiesAndClassesList extends Command {

	private static Logger logger = LoggerFactory
			.getLogger(GetPropertiesAndClassesList.class.getSimpleName());

	private enum JsonKeys {
		classList, classMap, propertyList, propertyMap
	}

	public GetPropertiesAndClassesList(String id) {
		super(id);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Get Properties and Classes List";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		OntologyManager ontMgr = vWorkspace.getWorkspace().getOntologyManager();
		JSONArray classesList = new JSONArray();
		JSONArray classesMap = new JSONArray();
		JSONArray propertiesList = new JSONArray();
		JSONArray propertiesMap = new JSONArray();

		ExtendedIterator<OntClass> iter = ontMgr.getOntModel()
				.listNamedClasses();
		ExtendedIterator<DatatypeProperty> propsIter = ontMgr.getOntModel()
				.listDatatypeProperties();
		final JSONObject outputObj = new JSONObject();

		try {
			while (iter.hasNext()) {
				OntClass cls = iter.next();
				classesList.put(cls.getLocalName());
				JSONObject classKey = new JSONObject();
				classKey.put(cls.getLocalName(), cls.getURI());
				classesMap.put(classKey);
			}

			while (propsIter.hasNext()) {
				DatatypeProperty prop = propsIter.next();
				propertiesList.put(prop.getLocalName());
				JSONObject propKey = new JSONObject();
				propKey.put(prop.getLocalName(), prop.getURI());
				propertiesMap.put(propKey);
			}

			// Populate the JSON object that will hold everything in output
			outputObj.put(JsonKeys.classList.name(), classesList);
			outputObj.put(JsonKeys.classMap.name(), classesMap);
			outputObj.put(JsonKeys.propertyList.name(), propertiesList);
			outputObj.put(JsonKeys.propertyMap.name(), propertiesMap);

		} catch (JSONException e) {
			logger.error("Error populating JSON!");
		}
		
		UpdateContainer upd = new UpdateContainer(new AbstractUpdate() {
			@Override
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				pw.print(outputObj.toString());
			}
		});
		return upd;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
