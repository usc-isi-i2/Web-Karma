package edu.isi.karma.controller.command.alignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.SemanticType.ClientJsonKeys;

public class AugmentDataCommand extends WorksheetCommand{
	private String predicate;
	private String columnUri;
	private String alignmentId;

	public AugmentDataCommand(String id, String worksheetId, String columnUri, String alignmentId, String predicate, String triplesMap) {
		super(id, worksheetId);
		this.predicate = predicate;
		this.columnUri = columnUri;
		this.alignmentId = alignmentId;
	}

	@Override
	public String getCommandName() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Augment Data";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommandType getCommandType() {
		// TODO Auto-generated method stub
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		// TODO Auto-generated method stub
		UpdateContainer c =  new UpdateContainer();		
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		RepFactory factory = workspace.getFactory();
		Worksheet worksheet = factory.getWorksheet(worksheetId);
		Set<LabeledLink> tmp = alignment.getCurrentOutgoingLinksToNode(columnUri);
		String hNodeId = null;
		for (LabeledLink link : tmp) {
			if (link.getKeyType() == LinkKeyInfo.UriOfInstance) {
				hNodeId = link.getTarget().getId();
			}
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
					rowHashToSubjectURI.put(HashValueManager.getHashValue(r, hNodeIds), n.getValue().asString());

					if (SubjectURIToRowId.get(n.getValue().asString()) == null)
						SubjectURIToRowId.put(n.getValue().asString(), new ArrayList<String>());
					List<String> rowIds = SubjectURIToRowId.get(n.getValue().asString());	
					rowIds.add(r.getId());
				}
			}
		}
		TripleStoreUtil util = new TripleStoreUtil();

		String modelRepoUrl =TripleStoreUtil.defaultDataRepoUrl ;
		//String modelContext = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(Property.modelContext);
		List<String> subjects = new LinkedList<String>();
		subjects.addAll(rowHashToSubjectURI.values());
		JSONArray predicatesarray = new JSONArray(predicate);
		List<String> predicates = new LinkedList<String>();
		Map<String, List<String>> results = null;
		for(int i = 0; i < predicatesarray.length(); i++) {
			predicates.add(predicatesarray.getJSONObject(i).getString("predicate"));
		}
		try{
			results = util.getObjectsForSubjectsAndPredicates(modelRepoUrl, null, subjects , predicates);
			//System.out.println(results);
		} catch (Exception e) {
			e.printStackTrace();
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
		List<String> resultSubjects = results.get("resultSubjects");
		List<String> resultPredicates = results.get("resultPredicates");
		List<String> resultObjects = results.get("resultObjects");		
		AddValuesCommandFactory addFactory = new AddValuesCommandFactory();
		for (int i = 0; i < resultPredicates.size(); i++) {
			String subject = resultSubjects.get(i);
			List<String> rowIds = SubjectURIToRowId.get(subject);
			boolean isNewNode = false;
			for (String RowId : rowIds) {
				String predicate = resultPredicates.get(i);
				JSONArray array = new JSONArray();
				JSONObject obj = new JSONObject();
				JSONObject obj2 = new JSONObject();
				obj.put("values", resultObjects.get(i));
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
					AddValuesCommand command = (AddValuesCommand) addFactory.createCommand(input, workspace, hNodeId, worksheetId, hnode.getHTableId(), predicate.substring(predicate.lastIndexOf("/") + 1));
					command.doIt(workspace);
					isNewNode |= command.isNewNode();
					hNodeId = command.getNewHNodeId();


				} catch(Exception e) {
					e.printStackTrace();
					return new UpdateContainer(new ErrorUpdate(e.getMessage()));
				}
			}
			if (isNewNode) {
				HNode tableHNode =workspace.getFactory().getHNode(hNodeId);
				String nestedHNodeId = tableHNode.getNestedTable().getHNodeIdFromColumnName("values");
				SetSemanticTypeCommandFactory sstFactory = new SetSemanticTypeCommandFactory();
				JSONArray semanticTypesArray = new JSONArray();
				JSONObject semanticType = new JSONObject();
				edu.isi.karma.rep.alignment.Node n = alignment.getNodeById(columnUri);
				semanticType.put(ClientJsonKeys.FullType.name(), resultPredicates.get(i));
				semanticType.put(ClientJsonKeys.isPrimary.name(), "true");
				semanticType.put(ClientJsonKeys.DomainId.name(), n.getId());
				semanticType.put(ClientJsonKeys.DomainUri.name(), n.getUri());


				semanticTypesArray.put(semanticType);
				Command sstCommand = sstFactory.createCommand(workspace, worksheetId, nestedHNodeId, false, semanticTypesArray, false, "");
				sstCommand.doIt(workspace);
			}

		}


		c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId));
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
