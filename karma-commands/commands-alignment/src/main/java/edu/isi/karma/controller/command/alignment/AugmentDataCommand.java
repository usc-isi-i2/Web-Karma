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
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.alignment.SemanticType.ClientJsonKeys;
import edu.isi.karma.webserver.KarmaException;

public class AugmentDataCommand extends WorksheetCommand{
	private String predicate;
	private String columnUri;
	private String alignmentId;
	private String otherClass;
	private String dataRepoUrl;
	private String hNodeId;
	private String newhNodeId;
	private boolean incoming;
	private final Integer limit = 100;
	Stack<Command> appliedCommands;
	public AugmentDataCommand(String id, String dataRepoUrl, String worksheetId, String columnUri, String predicate, String triplesMap, String otherClass, String hNodeId, Boolean incoming) {
		super(id, worksheetId);
		this.predicate = predicate;
		this.columnUri = columnUri;
		this.otherClass = otherClass;
		this.dataRepoUrl = dataRepoUrl;
		this.hNodeId = hNodeId;
		newhNodeId = hNodeId;
		this.incoming = incoming;
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
		JSONArray predicatesarray = new JSONArray(predicate);
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < predicatesarray.length() - 1; i++) 
			builder.append(predicatesarray.getJSONObject(i).getString("predicate")).append(" ");
		builder.append(predicatesarray.getJSONObject(predicatesarray.length() - 1).getString("predicate"));
		return builder.toString();
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
		if (alignment.GetTreeRoot() != null)
			hNodeId = FetchHNodeIdFromAlignmentCommand.gethNodeId(alignmentId, columnUri);
		if (hNodeId == null) {
			c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId));
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
			return c;
		}
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
					String uri = n.getValue().asString();
					rowHashToSubjectURI.put(HashValueManager.getHashValue(r, hNodeIds), uri);

					if (SubjectURIToRowId.get(uri) == null)
						SubjectURIToRowId.put(uri, new ArrayList<String>());
					List<String> rowIds = SubjectURIToRowId.get(uri);	
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
		Map<String, List<String>> results = new HashMap<String, List<String>>();
		for(int i = 0; i < predicatesarray.length(); i++) {
			predicates.add(predicatesarray.getJSONObject(i).getString("predicate"));
			otherClasses.add(otherClassarray.getJSONObject(i).getString("otherClass"));
			if (predicates.size() > limit) {
				try {
					Map<String, List<String>> temp = null;
					if (!incoming)
						temp = util.getObjectsForSubjectsAndPredicates(dataRepoUrl, null, subjects , predicates, otherClasses);
					else
						temp = util.getSubjectsForPredicatesAndObjects(dataRepoUrl, null, subjects , predicates, otherClasses);
					addMappingToResults(results, temp);
					predicates.clear();
					otherClasses.clear();
				} catch (KarmaException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		try{
			Map<String, List<String>> temp = null;
			if (!incoming)
				temp = util.getObjectsForSubjectsAndPredicates(dataRepoUrl, null, subjects , predicates, otherClasses);
			else
				temp = util.getSubjectsForPredicatesAndObjects(dataRepoUrl, null, subjects , predicates, otherClasses);
			addMappingToResults(results, temp);
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
			String subject = incoming ? resultObjects.get(i) : resultSubjects.get(i);
			List<String> rowIds = SubjectURIToRowId.get(subject);
			boolean isNewNode = false;
			for (String RowId : rowIds) {
				String predicate = resultPredicates.get(i);
				String otherClass = resultClass.get(i);
				JSONArray array = new JSONArray();
				JSONObject obj = new JSONObject();
				JSONObject obj2 = new JSONObject();
				obj.put("values", incoming ? resultSubjects.get(i) : resultObjects.get(i));
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
					AddValuesCommand command = (AddValuesCommand) addFactory.createCommand(input, workspace, hNodeId, worksheetId, hnode.getHTableId(), incoming ? otherClass.substring(otherClass.lastIndexOf("/") + 1) : predicate.substring(predicate.lastIndexOf("/") + 1), HNodeType.AugmentData);
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
					if (alignment.getNodesByUri(resultClass.get(i)) != null)
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
					if (!incoming) {
						newEdge.put(ChangeInternalNodeLinksCommand.JsonKeys.edgeSourceId.name(), sourceId);
						newEdge.put(ChangeInternalNodeLinksCommand.JsonKeys.edgeTargetId.name(), targetId);
						newEdge.put(ChangeInternalNodeLinksCommand.JsonKeys.edgeId.name(), edgeUri);
					}
					else {
						newEdge.put(ChangeInternalNodeLinksCommand.JsonKeys.edgeSourceId.name(), targetId);
						newEdge.put(ChangeInternalNodeLinksCommand.JsonKeys.edgeTargetId.name(), sourceId);
						newEdge.put(ChangeInternalNodeLinksCommand.JsonKeys.edgeId.name(), edgeUri);
					}
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

	private void addMappingToResults(Map<String, List<String>> results, Map<String, List<String>> temp) {
		List<String> resultSubjects = results.get("resultSubjects");
		List<String> resultPredicates = results.get("resultPredicates");
		List<String> resultObjects = results.get("resultObjects");
		List<String> resultClasses = results.get("resultClasses");
		if (resultSubjects == null)
			resultSubjects = new LinkedList<String>();
		if (resultPredicates == null)
			resultPredicates = new LinkedList<String>();
		if (resultObjects == null)
			resultObjects = new LinkedList<String>();
		if (resultClasses == null)
			resultClasses = new LinkedList<String>();
		resultSubjects.addAll(temp.get("resultSubjects"));
		resultPredicates.addAll(temp.get("resultPredicates"));
		resultObjects.addAll(temp.get("resultObjects"));
		resultClasses.addAll(temp.get("resultClasses"));
		results.put("resultSubjects", resultSubjects);
		results.put("resultPredicates", resultPredicates);
		results.put("resultObjects", resultObjects);
		results.put("resultClasses", resultClasses);
	}

}
