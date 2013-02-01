package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.view.VWorkspace;

public class GetPropertiesAndClassesList extends Command {

	private final String vWorksheetId;
	private static Logger logger = LoggerFactory.getLogger(GetPropertiesAndClassesList.class);

	private enum JsonKeys {
		classList, classMap, propertyList, propertyMap, label, category
	}
	
	private enum JsonValues {
		Class, Instance
	}

	public GetPropertiesAndClassesList(String id, String vWorksheetId) {
		super(id);
		this.vWorksheetId = vWorksheetId;
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
		
//		Map<String, String> prefixMap = vWorkspace.getWorkspace().getOntologyManager().getPrefixMap();


		final JSONObject outputObj = new JSONObject();

		try {
			/** Add all the class instances **/
			String alignmentId = AlignmentManager.Instance().constructAlignmentId(vWorkspace.getWorkspace().getId(), vWorksheetId);
			Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
			if (alignment != null && !alignment.isEmpty()) {
				Set<Node> nodes = alignment.getGraphNodes();
				for (Node node: nodes) {
					if (node.getType() == NodeType.InternalNode) {
						JSONObject nodeKey = new JSONObject();
						nodeKey.put(node.getLocalId(), node.getId());
						classesMap.put(nodeKey);
						
						JSONObject instanceCatObject = new JSONObject();
						instanceCatObject.put(JsonKeys.label.name(), node.getLocalId());
						instanceCatObject.put(JsonKeys.category.name(), JsonValues.Instance.name());
						classesList.put(instanceCatObject);
					}
				}
			}
			
			/** Adding all the classes **/
			for (Label clazz: ontMgr.getClasses().values()) {
				JSONObject classKey = new JSONObject();
				classKey.put(clazz.getLocalNameWithPrefix(), clazz.getUri());
				classesMap.put(classKey);
				
				JSONObject labelObj = new JSONObject();
				labelObj.put(JsonKeys.label.name(), clazz.getLocalNameWithPrefix());
				labelObj.put(JsonKeys.category.name(), JsonValues.Class.name());
				classesList.put(labelObj);
			}
			
			/** Adding all the properties **/
			for (Label prop: ontMgr.getDataProperties().values()) {
				JSONObject propKey = new JSONObject();
				propKey.put(prop.getLocalNameWithPrefix(), prop.getUri());
				propertiesMap.put(propKey);
				propertiesList.put(prop.getLocalNameWithPrefix());
			}

			// Populate the JSON object that will hold everything in output
			outputObj.put(JsonKeys.classList.name(), classesList);
			outputObj.put(JsonKeys.classMap.name(), classesMap);
			outputObj.put(JsonKeys.propertyList.name(), propertiesList);
			outputObj.put(JsonKeys.propertyMap.name(), propertiesMap);
		} catch (JSONException e) {
			logger.error("Error populating JSON!", e);
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
		return null;
	}

}
