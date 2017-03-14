package edu.isi.karma.controller.command.alignment;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.command.worksheet.AddValuesCommand;
import edu.isi.karma.controller.command.worksheet.AddValuesCommandFactory;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.er.helper.CloneTableUtils;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.HashValueManager;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.SemanticType.ClientJsonKeys;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.webserver.KarmaException;

public class AugmentDataCommand extends WorksheetSelectionCommand{

	private static Logger LOG = LoggerFactory.getLogger(AugmentDataCommand.class);
	private String predicate;
	private String columnUri;
	private String alignmentId;
	private String otherClass;
	private String dataRepoUrl;
	private String hNodeId;
	private String newhNodeId;
	private boolean incoming;
	private String sameAsPredicate;
	private final Integer limit = 200;
	Stack<Command> appliedCommands;
	public AugmentDataCommand(String id, String model, String dataRepoUrl, String worksheetId, String columnUri, String predicate, String otherClass, String hNodeId, Boolean incoming, String sameAsPredicate, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.predicate = predicate;
		this.columnUri = columnUri;
		this.otherClass = otherClass;
		this.dataRepoUrl = dataRepoUrl;
		this.hNodeId = hNodeId;
		newhNodeId = hNodeId;
		this.incoming = incoming;
		this.sameAsPredicate = sameAsPredicate;
		appliedCommands = new Stack<>();
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
		appliedCommands.clear();
		inputColumns.clear();
		outputColumns.clear();
		UpdateContainer c =  new UpdateContainer();
		alignmentId = AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		RepFactory factory = workspace.getFactory();
		Worksheet worksheet = factory.getWorksheet(worksheetId);
		SuperSelection selection = getSuperSelection(worksheet);
		if (alignment.GetTreeRoot() != null)
			hNodeId = FetchHNodeIdFromAlignmentCommand.gethNodeId(alignmentId, columnUri);
		if (hNodeId == null) {
			c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(worksheet), workspace.getContextId()));
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
			return c;
		}
		HNode hnode = factory.getHNode(hNodeId);
		List<String> hNodeIds = new LinkedList<>();
		hNodeIds.add(hNodeId);
		inputColumns.addAll(hNodeIds);
		List<Table> dataTables = new ArrayList<>();
		CloneTableUtils.getDatatable(worksheet.getDataTable(), factory.getHTable(hnode.getHTableId()), dataTables, selection);
		Map<String, String> rowHashToSubjectURI = new HashMap<>();
		Map<String, List<String>> SubjectURIToRowId = new HashMap<>();
		for(Table t : dataTables) {
			for(Row r : t.getRows(0, t.getNumRows(), selection)) {
				Node n = r.getNode(hNodeId);
				if(n != null && n.getValue() != null && !n.getValue().isEmptyValue() && n.getValue().asString() != null && !n.getValue().asString().trim().isEmpty() ) {
					String uri = n.getValue().asString().trim().replace(" ", "");
					String baseURI = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(Property.baseURI);
					try {
						URI t1 = new URI(uri);
						if (!t1.isAbsolute() && baseURI != null) {
							uri = baseURI + uri;
						}
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
					}
//					n.setValue(uri, n.getStatus(), factory);
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
		List<String> subjects = new LinkedList<>();
		subjects.addAll(rowHashToSubjectURI.values());
		List<String> predicates = new LinkedList<>();
		List<String> otherClasses = new LinkedList<>();
		Map<String, List<String>> results = new HashMap<>();

		URIFormatter uriFormatter = new URIFormatter(workspace.getOntologyManager(), new ErrorReport());
		if(sameAsPredicate!= null && !sameAsPredicate.trim().isEmpty())
		{
			sameAsPredicate = uriFormatter.getExpandedAndNormalizedUri(sameAsPredicate);
		}

		JSONArray predicatesarray = new JSONArray(predicate);
		JSONArray otherClassarray = new JSONArray(otherClass);

		for(int i = 0; i < predicatesarray.length(); i++) {
			predicates.add(predicatesarray.getJSONObject(i).getString("predicate"));
			otherClasses.add(otherClassarray.getJSONObject(i).getString("otherClass"));
		}

		while (!subjects.isEmpty()) {
			ListIterator<String> subjectsIterator = subjects.listIterator();
			LinkedList<String> tempSubjects = new LinkedList<>();
			while(tempSubjects.size() < limit && !subjects.isEmpty())
			{
				tempSubjects.add(subjectsIterator.next());
				subjectsIterator.remove();
			}
			try {
				Map<String, List<String>> temp;
				if (!incoming)
					temp = util.getObjectsForSubjectsAndPredicates(dataRepoUrl, null, tempSubjects , predicates, otherClasses, sameAsPredicate);
				else
					temp = util.getSubjectsForPredicatesAndObjects(dataRepoUrl, null, tempSubjects , predicates, otherClasses, sameAsPredicate);
				addMappingToResults(results, temp);
				//				predicates.clear();
				//				otherClasses.clear();
			} catch (KarmaException e) {
				LOG.error("Unable to load data to augment: ", e);
				return new UpdateContainer(new ErrorUpdate(e.getMessage()));
			}
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
				if (otherClass != null && !otherClass.trim().isEmpty())
					obj.put("URIs", incoming ? resultSubjects.get(i) : resultObjects.get(i));
				else
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
					OntologyManager ontMgr = workspace.getOntologyManager();
					Label label = ontMgr.getUriLabel(incoming ? otherClass : predicate);
					AddValuesCommand command = (AddValuesCommand) addFactory.createCommand(input, 
							model, workspace, hNodeId, worksheetId, hnode.getHTableId(), label.getDisplayName(), HNodeType.AugmentData, selection.getName());
					command.doIt(workspace);
					outputColumns.addAll(command.getOutputColumns());
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
				if (nestedHNodeId == null)
					nestedHNodeId = tableHNode.getNestedTable().getHNodeIdFromColumnName("URIs");
				SetSemanticTypeCommandFactory sstFactory = new SetSemanticTypeCommandFactory();
				JSONArray semanticTypesArray = new JSONArray();
				JSONObject semanticType = new JSONObject();
				edu.isi.karma.rep.alignment.Node n = alignment.getNodeById(columnUri);

				semanticType.put(ClientJsonKeys.isPrimary.name(), "true");
				Set<edu.isi.karma.rep.alignment.Node> oldNodes = new HashSet<>(); 
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
				Command sstCommand = sstFactory.createCommand(model, workspace, worksheetId, nestedHNodeId, 
						false, semanticTypesArray, false, "", "", selection.getName());
				appliedCommands.push(sstCommand);
				sstCommand.doIt(workspace);
				if(!resultClass.get(i).trim().isEmpty())
				{
					ChangeInternalNodeLinksCommandFactory cinlcf = new ChangeInternalNodeLinksCommandFactory();
					SetMetaPropertyCommandFactory smpcf = new SetMetaPropertyCommandFactory();
					JSONArray newEdges = new JSONArray();
					JSONObject newEdge = new JSONObject();
					String sourceId = n.getId();
					Set<edu.isi.karma.rep.alignment.Node> tempnodes = new HashSet<>();
					tempnodes.addAll(alignment.getNodesByUri(resultClass.get(i)));
					tempnodes.removeAll(oldNodes);

					edu.isi.karma.rep.alignment.Node target = tempnodes.iterator().next();
					String targetId = target.getId();
					String targetUri = target.getLabel().getUri();
					String edgeUri = resultPredicates.get(i);
					if (!incoming) {
						newEdge.put(ChangeInternalNodeLinksCommand.LinkJsonKeys.edgeSourceId.name(), sourceId);
						newEdge.put(ChangeInternalNodeLinksCommand.LinkJsonKeys.edgeTargetId.name(), targetId);
						newEdge.put(ChangeInternalNodeLinksCommand.LinkJsonKeys.edgeId.name(), edgeUri);
					}
					else {
						newEdge.put(ChangeInternalNodeLinksCommand.LinkJsonKeys.edgeSourceId.name(), targetId);
						newEdge.put(ChangeInternalNodeLinksCommand.LinkJsonKeys.edgeTargetId.name(), sourceId);
						newEdge.put(ChangeInternalNodeLinksCommand.LinkJsonKeys.edgeId.name(), edgeUri);
					}
					newEdges.put(newEdge);
					Command changeInternalNodeLinksCommand = cinlcf.createCommand(worksheetId, alignmentId, new JSONArray(), newEdges, model, workspace);
					changeInternalNodeLinksCommand.doIt(workspace);
					appliedCommands.push(changeInternalNodeLinksCommand);
					Command setMetaDataCommand = smpcf.createCommand(model, workspace, nestedHNodeId, worksheetId, "isUriOfClass", 
							targetUri, targetId, false, "", "", selection.getName());
					setMetaDataCommand.doIt(workspace);
					appliedCommands.push(setMetaDataCommand);
				}
			}

		}

		WorksheetUpdateFactory.detectSelectionStatusChange(worksheetId, workspace, this);
		c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(worksheet), workspace.getContextId()));
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
		c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(workspace), workspace.getContextId()));
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}

	private void addMappingToResults(Map<String, List<String>> results, Map<String, List<String>> temp) {
		List<String> resultSubjects = results.get("resultSubjects");
		List<String> resultPredicates = results.get("resultPredicates");
		List<String> resultObjects = results.get("resultObjects");
		List<String> resultClasses = results.get("resultClasses");
		if (resultSubjects == null)
			resultSubjects = new LinkedList<>();
		if (resultPredicates == null)
			resultPredicates = new LinkedList<>();
		if (resultObjects == null)
			resultObjects = new LinkedList<>();
		if (resultClasses == null)
			resultClasses = new LinkedList<>();
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
