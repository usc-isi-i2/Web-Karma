package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;
import java.util.List;

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
import edu.isi.karma.view.VWorkspace;

public class GetSemanticSuggestionsCommand extends WorksheetSelectionCommand {
	private final String hNodeId;
	private static Logger logger = LoggerFactory.getLogger(GetSemanticSuggestionsCommand.class.getSimpleName());
	
	protected GetSemanticSuggestionsCommand(String id, String worksheetId, String hNodeId, String selectionId) {
		super(id, worksheetId, selectionId);
		this.hNodeId = hNodeId;
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
				if(model != null) {
					OntologyManager ontMgr = workspace.getOntologyManager();
					Alignment alignment = AlignmentManager.Instance().getAlignment(workspace.getId(), worksheetId);
					JSONObject json = model.getAsJSONObject(ontMgr, alignment);
					pw.print(json.toString());
				}
				
			}
			
		});
		return uc;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
		
	}

}
