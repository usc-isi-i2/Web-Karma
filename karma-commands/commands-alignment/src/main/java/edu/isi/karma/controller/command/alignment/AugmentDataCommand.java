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
import edu.isi.karma.controller.command.worksheet.AddValuesCommandFactory;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.er.helper.CloneTableUtils;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
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

public class AugmentDataCommand extends WorksheetCommand{
	private String predicate;
	private String triplesMap;
	private String columnUri;
	private String alignmentId;

	public AugmentDataCommand(String id, String worksheetId, String columnUri, String alignmentId, String predicate, String triplesMap) {
		super(id, worksheetId);
		this.predicate = predicate;
		this.triplesMap = triplesMap;
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
		Map<String, String> SubjectURIToRowId = new HashMap<String, String>();
		for(Table t : dataTables) {
			for(Row r : t.getRows(0, t.getNumRows())) {
				Node n = r.getNode(hNodeId);
				if(n != null && n.getValue() != null && !n.getValue().isEmptyValue() && n.getValue().asString() != null && !n.getValue().asString().trim().isEmpty() ) {
					rowHashToSubjectURI.put(HashValueManager.getHashValue(r, hNodeIds), n.getValue().asString());
					SubjectURIToRowId.put(n.getValue().asString(), r.getId());
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
			System.out.println(results);
		} catch (Exception e) {
			e.printStackTrace();
			return new UpdateContainer();
		}
		List<String> resultSubjects = results.get("resultSubjects");
		List<String> resultPredicates = results.get("resultPredicates");
		List<String> resultObjects = results.get("resultObjects");
		String predicate = "";
		JSONArray array = null;
		AddValuesCommandFactory addFactory = new AddValuesCommandFactory();
		for (int i = 0; i < resultPredicates.size(); i++) {
			JSONObject obj = new JSONObject();
			JSONObject obj2 = new JSONObject();
			String subject = resultSubjects.get(i);
			obj.put("values", resultObjects.get(i));
			obj2.put("rowId", SubjectURIToRowId.get(subject));
			obj2.put("rowIdHash", "");
			obj2.put("values", obj);
			if (predicate.compareTo(resultPredicates.get(i)) != 0) {
				if (!predicate.isEmpty()) {
					JSONArray input = new JSONArray();
					JSONObject obj3 = new JSONObject();
					obj3.put("name", "AddValues");
					obj3.put("value", array.toString());
					obj3.put("type", "other");
					input.put(obj3);
					try {
						c.append(addFactory.createCommand(input, workspace, hNodeId, worksheetId, hnode.getHTableId(), predicate.substring(predicate.lastIndexOf("/") + 1)).doIt(workspace));
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				array = new JSONArray();
				predicate = resultPredicates.get(i);
				array.put(obj2);
			}
			else {
				array.put(obj2);
			}

		}
		if (array.length() != 0) {
			JSONArray input = new JSONArray();
			JSONObject obj3 = new JSONObject();
			obj3.put("name", "AddValues");
			obj3.put("value", array.toString());
			obj3.put("type", "other");
			input.put(obj3);
			try {
				c.append(addFactory.createCommand(input, workspace, hNodeId, worksheetId, hnode.getHTableId(), predicate.substring(predicate.lastIndexOf("/") + 1)).doIt(workspace));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return c;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
