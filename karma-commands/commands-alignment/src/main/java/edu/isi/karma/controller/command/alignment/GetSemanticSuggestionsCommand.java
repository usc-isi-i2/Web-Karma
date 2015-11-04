package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeColumnModel;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.view.VWorkspace;

public class GetSemanticSuggestionsCommand extends WorksheetSelectionCommand {
	private final String hNodeId;
	private final String classUri;
	private static Logger logger = LoggerFactory.getLogger(GetSemanticSuggestionsCommand.class.getSimpleName());
	
	protected GetSemanticSuggestionsCommand(String id, String model, String worksheetId, 
			String hNodeId, String classUri, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.hNodeId = hNodeId;
		this.classUri = classUri;
	}
	
	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(final Workspace workspace) throws CommandException {
		logger.info("Get Semantic Suggestions: " + worksheetId + "," + hNodeId);
		UpdateContainer uc = new UpdateContainer();
		final SuperSelection selection = getSuperSelection(workspace);
		uc.add(new AbstractUpdate() {

			@Override
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				HNodePath currentColumnPath = null;
				Worksheet worksheet = workspace.getWorksheet(worksheetId);
				List<HNodePath> paths = worksheet.getHeaders().getAllPaths();
				for (HNodePath path : paths) {
					if (path.getLeaf().getId().equals(hNodeId)) {
						currentColumnPath = path;
						break;
					}
				}
				
				SemanticTypeColumnModel model = new SemanticTypeUtil().predictColumnSemanticType(workspace, worksheet, currentColumnPath, 4, selection);
				OntologyManager ontMgr = workspace.getOntologyManager();
				Alignment alignment = AlignmentManager.Instance().getAlignment(workspace.getId(), worksheetId);
				ColumnNode columnNode = alignment.getColumnNodeByHNodeId(hNodeId);
				if (columnNode.getLearnedSemanticTypes() == null) {
					// do this only one time: if user assigns a semantic type to the column, 
					// and later clicks on Set Semantic Type button, we should not change the initially learned types 
					logger.debug("adding learned semantic types to the column " + hNodeId);
					columnNode.setLearnedSemanticTypes(new SemanticTypeUtil().getSuggestedTypes(ontMgr, columnNode, model));
					if (columnNode.getLearnedSemanticTypes().isEmpty()) {
						logger.info("no semantic type learned for the column " + hNodeId);
					}
				}
				JSONObject result;
				if(model != null) {
					if(classUri == null)
						result = model.getAsJSONObject(ontMgr, alignment);
					else {
						model = new SemanticTypeUtil().predictColumnSemanticType(workspace, worksheet, currentColumnPath, 4, selection);
						result = model.getAsJSONObject(classUri, ontMgr, 4);
					}
				} else {
					result = new JSONObject();
					result.put("Labels", new JSONArray());
				}
				pw.println(result.toString());
				
			}
			
		});
		return uc;
	}
	

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
		
	}

}
