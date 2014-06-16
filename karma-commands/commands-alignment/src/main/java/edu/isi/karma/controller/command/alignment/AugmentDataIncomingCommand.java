package edu.isi.karma.controller.command.alignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.worksheet.AddValuesCommand;
import edu.isi.karma.controller.command.worksheet.AddValuesCommandFactory;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.er.helper.CloneTableUtils;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HashValueManager;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.SemanticType.ClientJsonKeys;

public class AugmentDataIncomingCommand extends WorksheetCommand{
	private String predicate;
	private String columnUri;
	private String alignmentId;
	private String otherClass;
	private String dataRepoUrl;
	private String hNodeId;
	private String newhNodeId;
	Stack<Command> appliedCommands;
	public AugmentDataIncomingCommand(String id, String dataRepoUrl, String worksheetId, String columnUri, String predicate, String triplesMap, String otherClass, String hNodeId) {
		super(id, worksheetId);
		this.predicate = predicate;
		this.columnUri = columnUri;
		this.otherClass = otherClass;
		this.dataRepoUrl = dataRepoUrl;
		this.hNodeId = hNodeId;
		newhNodeId = hNodeId;
		appliedCommands = new Stack<Command>();
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Augment Data";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		// TODO Auto-generated method stub
		appliedCommands.clear();
		UpdateContainer c =  new UpdateContainer();
		alignmentId = AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		RepFactory factory = workspace.getFactory();
		Worksheet worksheet = factory.getWorksheet(worksheetId);
		HNode hnode = factory.getHNode(hNodeId);
		List<String> hNodeIds = new LinkedList<String>();
		hNodeIds.add(hNodeId);
		List<Table> dataTables = new ArrayList<Table>();
		CloneTableUtils.getDatatable(worksheet.getDataTable(), factory.getHTable(hnode.getHTableId()), dataTables);
		Map<String, String> rowHashToSubjectURI = new HashMap<String, String>();
		Map<String, List<String>> SubjectURIToRowId = new HashMap<String, List<String>>();
		for(Table t : dataTables) {
			for(Row r : t.getRows(0, t.getNumRows())) {
				Node n = r.getNode(hNodeId);
				if(n != null && n.getValue() != null && !n.getValue().isEmptyValue() && n.getValue().asString() != null && !n.getValue().asString().trim().isEmpty() ) {
					rowHashToSubjectURI.put(HashValueManager.getHashValue(r, hNodeIds), n.getValue().asString());

					if (SubjectURIToRowId.get(n.getValue().asString()) == null)
						SubjectURIToRowId.put(n.getValue().asString(), new ArrayList<String>());
					List<String> rowIds = SubjectURIToRowId.get(n.getValue().asString());	
					rowIds.add(r.getId());
				}
			}
		}
		TripleStoreUtil util = new TripleStoreUtil();

		//String modelContext = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(Property.modelContext);
		List<String> subjects = new LinkedList<String>();
		subjects.addAll(rowHashToSubjectURI.values());
		JSONArray predicatesarray = new JSONArray(predicate);
		JSONArray otherClassarray = new JSONArray(otherClass);
		List<String> predicates = new LinkedList<String>();
		List<String> otherClasses = new LinkedList<String>();
		Map<String, List<String>> results = null;
		for(int i = 0; i < predicatesarray.length(); i++) {
			predicates.add(predicatesarray.getJSONObject(i).getString("predicate"));
			otherClasses.add(otherClassarray.getJSONObject(i).getString("otherClass"));
		}
		
		try{
			results = util.getSubjectsForPredicatesAndObjects(dataRepoUrl, null, subjects , predicates, otherClasses);
		} catch (Exception e) {
			e.printStackTrace();
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
		List<String> resultSubjects = results.get("resultSubjects");
		List<String> resultPredicates = results.get("resultPredicates");
		List<String> resultObjects = results.get("resultObjects");
		List<String> resultClass = results.get("resultClasses");
		AddValuesCommandFactory addFactory = new AddValuesCommandFactory();
		
		for (int i = 0; i < resultPredicates.size(); i++) {
			String subject = resultObjects.get(i);
			List<String> rowIds = SubjectURIToRowId.get(subject);
			boolean isNewNode = false;
			for (String RowId : rowIds) {
				String otherClass = resultClass.get(i);
				JSONArray array = new JSONArray();
				JSONObject obj = new JSONObject();
				JSONObject obj2 = new JSONObject();
				obj.put("values", resultSubjects.get(i));
				obj2.put("rowId", RowId);
				obj2.put("rowIdHash", "");
				obj2.put("values", obj);
				array.put(obj2);
				JSONArray input = new JSONArray();
				JSONObject obj3 = new JSONObject();
				obj3.put("name", "AddValues");
				obj3.put("value", array.toString());
				obj3.put("type", "other");
				input.put(obj3);
				try {
					AddValuesCommand command = (AddValuesCommand) addFactory.createCommand(input, workspace, hNodeId, worksheetId, hnode.getHTableId(), otherClass.substring(otherClass.lastIndexOf("/") + 1));
					command.doIt(workspace);
					isNewNode |= command.isNewNode();
					if (command.isNewNode())
						appliedCommands.push(command);
					newhNodeId = command.getNewHNodeId();


				} catch(Exception e) {
					e.printStackTrace();
					return new UpdateContainer(new ErrorUpdate(e.getMessage()));
				}
			}
			if (isNewNode && alignment.GetTreeRoot() != null) {
				HNode tableHNode =workspace.getFactory().getHNode(newhNodeId);
				String nestedHNodeId = tableHNode.getNestedTable().getHNodeIdFromColumnName("values");
				SetSemanticTypeCommandFactory sstFactory = new SetSemanticTypeCommandFactory();
				JSONArray semanticTypesArray = new JSONArray();
				JSONObject semanticType = new JSONObject();
				edu.isi.karma.rep.alignment.Node n = alignment.getNodeById(columnUri);
				
				semanticType.put(ClientJsonKeys.isPrimary.name(), "true");
				Set<edu.isi.karma.rep.alignment.Node> oldNodes = new HashSet<edu.isi.karma.rep.alignment.Node>(); 
				if(resultClass.get(i).trim().isEmpty())
				{
					semanticType.put(ClientJsonKeys.DomainId.name(), n.getId());
					semanticType.put(ClientJsonKeys.FullType.name(), resultPredicates.get(i));
					semanticType.put(ClientJsonKeys.DomainUri.name(), n.getUri());
				}
				else
				{
					oldNodes.addAll( alignment.getNodesByUri(resultClass.get(i)));
					semanticType.put(ClientJsonKeys.DomainId.name(), resultClass.get(i));
					semanticType.put(ClientJsonKeys.FullType.name(), Uris.CLASS_INSTANCE_LINK_URI);
				}


				semanticTypesArray.put(semanticType);
				Command sstCommand = sstFactory.createCommand(workspace, worksheetId, nestedHNodeId, false, semanticTypesArray, false, "");
				appliedCommands.push(sstCommand);
				sstCommand.doIt(workspace);
				if(!resultClass.get(i).trim().isEmpty())
				{
					ChangeInternalNodeLinksCommandFactory cinlcf = new ChangeInternalNodeLinksCommandFactory();
					SetMetaPropertyCommandFactory smpcf = new SetMetaPropertyCommandFactory();
					JSONArray newEdges = new JSONArray();
					JSONObject newEdge = new JSONObject();
					String sourceId = n.getId();
					Set<edu.isi.karma.rep.alignment.Node> tempnodes = new HashSet<edu.isi.karma.rep.alignment.Node>();
					tempnodes.addAll(alignment.getNodesByUri(resultClass.get(i)));
					tempnodes.removeAll(oldNodes);
					
					String targetId = tempnodes.iterator().next().getId();
					String edgeUri = resultPredicates.get(i);
					
					newEdge.put(ChangeInternalNodeLinksCommand.JsonKeys.edgeSourceId.name(), targetId);
					newEdge.put(ChangeInternalNodeLinksCommand.JsonKeys.edgeTargetId.name(), sourceId);
					newEdge.put(ChangeInternalNodeLinksCommand.JsonKeys.edgeId.name(), edgeUri);
					newEdges.put(newEdge);
					Command changeInternalNodeLinksCommand = cinlcf.createCommand(worksheetId, alignmentId, new JSONArray(), newEdges, workspace);
					changeInternalNodeLinksCommand.doIt(workspace);
					appliedCommands.push(changeInternalNodeLinksCommand);
					Command setMetaDataCommand = smpcf.createCommand(workspace, nestedHNodeId, worksheetId, "isUriOfClass", targetId, "");
					setMetaDataCommand.doIt(workspace);
					appliedCommands.push(setMetaDataCommand);
				}
			}

		}


		c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId));
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		UpdateContainer c =  new UpdateContainer();
		while (!appliedCommands.isEmpty()) {
			Command command = appliedCommands.pop();
			command.undoIt(workspace);
		}
		c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId));
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}

}
