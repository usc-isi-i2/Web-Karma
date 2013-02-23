package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;
import java.util.HashSet;
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
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.view.VWorkspace;

public class GetPropertiesAndClassesList extends Command {

	private final String vWorksheetId;
	private static Logger logger = LoggerFactory.getLogger(GetPropertiesAndClassesList.class);

	private enum JsonKeys {
		classList, classMap, propertyList, propertyMap, label, category, existingDataPropertyInstances, id
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
		JSONArray existingDataPropertyInstances = new JSONArray();
		
//		Map<String, String> prefixMap = vWorkspace.getWorkspace().getOntologyManager().getPrefixMap();


		final JSONObject outputObj = new JSONObject();

		try {
			/** Add all the class instances and property instances (existing links) **/
			String alignmentId = AlignmentManager.Instance().constructAlignmentId(vWorkspace.getWorkspace().getId(), vWorksheetId);
			Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
			Set<String> steinerTreeNodeIds = new HashSet<String>();
			if (alignment != null && !alignment.isEmpty()) {
//				Set<Node> nodes = alignment.getGraphNodes();
				for (Node node: alignment.getSteinerTree().vertexSet()) {
					if (node.getType() == NodeType.InternalNode) {
						String nodeDisplayLabel = (node.getLabel().getPrefix() != null || (!node.getLabel().getPrefix().equals(""))) ?
								(node.getLabel().getPrefix() + ":" + node.getLocalId()) : node.getLocalId(); 
						JSONObject nodeKey = new JSONObject();
						nodeKey.put(nodeDisplayLabel, node.getId());
						classesMap.put(nodeKey);
						
						JSONObject instanceCatObject = new JSONObject();
						instanceCatObject.put(JsonKeys.label.name(), nodeDisplayLabel);
						instanceCatObject.put(JsonKeys.category.name(), JsonValues.Instance.name());
						classesList.put(instanceCatObject);
						
						steinerTreeNodeIds.add(node.getId());
					}
				}
				
				// Store the data property links for specialized edge link options
				for (Link link:alignment.getGraphLinks()) {
					if (link instanceof DataPropertyLink) {
						JSONObject linkObj = new JSONObject();
						linkObj.put(JsonKeys.label.name(), link.getLocalId());
						linkObj.put(JsonKeys.id.name(), link.getId());
						existingDataPropertyInstances.put(linkObj);
					}
				}
			}
			
			/** Adding all the classes **/
			for (Label clazz: ontMgr.getClasses().values()) {
				int graphLastIndex = alignment.getLastIndexOfNodeUri(clazz.getUri());
				String clazzId = null;
				String clazzDisplayLabel = null;
				String clazzLocalNameWithPrefix = clazz.getLocalNameWithPrefix();
				if (graphLastIndex == -1) { // No instance present in the graph
					clazzDisplayLabel = clazzLocalNameWithPrefix + "1 (add)";
					clazzId = clazz.getUri();
				} else {
					// Check if already present in the steiner tree
					if (steinerTreeNodeIds.contains(clazz.getUri() + (graphLastIndex))) {
						clazzDisplayLabel = clazzLocalNameWithPrefix + (graphLastIndex+1) + " (add)";
						clazzId = clazz.getUri();
					} else {
						// Check if present in graph and not tree
						Node graphNode = alignment.getNodeById(clazz.getUri() + (graphLastIndex));
						if (graphNode != null) {
							clazzDisplayLabel = clazzLocalNameWithPrefix + (graphLastIndex) + " (add)";
							clazzId = graphNode.getId();
						} else {
							clazzDisplayLabel = clazzLocalNameWithPrefix + (graphLastIndex+1) + " (add)";
							clazzId = clazz.getUri();
						}
					}
				}
				JSONObject classKey = new JSONObject();
				classKey.put(clazzDisplayLabel, clazzId);
				classesMap.put(classKey);
				
				JSONObject labelObj = new JSONObject();
				labelObj.put(JsonKeys.label.name(), clazzDisplayLabel);
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
			outputObj.put(JsonKeys.existingDataPropertyInstances.name(), existingDataPropertyInstances);
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
