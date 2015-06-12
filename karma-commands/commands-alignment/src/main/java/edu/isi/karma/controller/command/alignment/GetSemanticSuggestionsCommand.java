package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

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
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.Origin;
import edu.isi.karma.view.VWorkspace;

public class GetSemanticSuggestionsCommand extends WorksheetSelectionCommand {
	private final String hNodeId;
	private static Logger logger = LoggerFactory.getLogger(GetSemanticSuggestionsCommand.class.getSimpleName());
	
	protected GetSemanticSuggestionsCommand(String id, String model, String worksheetId, String hNodeId, String selectionId) {
		super(id, model, worksheetId, selectionId);
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
				OntologyManager ontMgr = workspace.getOntologyManager();
				Alignment alignment = AlignmentManager.Instance().getAlignment(workspace.getId(), worksheetId);
				ColumnNode columnNode = alignment.getColumnNodeByHNodeId(hNodeId);
				if (columnNode.getLearnedSemanticTypes() == null) {
					// do this only one time: if user assigns a semantic type to the column, 
					// and later clicks on Set Semantic Type button, we should not change the initially learned types 
					logger.debug("adding learned semantic types to the column " + hNodeId);
					columnNode.setLearnedSemanticTypes(getSuggestedTypes(ontMgr, model));
					if (columnNode.getLearnedSemanticTypes().isEmpty()) {
						logger.info("no semantic type learned for the column " + hNodeId);
					}
				}
				if(model != null) {
					JSONObject json = model.getAsJSONObject(ontMgr, alignment);
					pw.print(json.toString());
				}
				
			}
			
		});
		return uc;
	}
	
	private List<SemanticType> getSuggestedTypes(OntologyManager ontologyManager, 
			SemanticTypeColumnModel columnModel) {
		
		ArrayList<SemanticType> suggestedSemanticTypes = new ArrayList<SemanticType>();
		if (columnModel == null)
			return suggestedSemanticTypes;
		
		for (Entry<String, Double> entry : columnModel.getScoreMap().entrySet()) {
			
			String key = entry.getKey();
			Double confidence = entry.getValue();
			if (key == null || key.isEmpty()) continue;

			String[] parts = key.split("\\|");
			if (parts == null || parts.length != 2) continue;

			String domainUri = parts[0].trim();
			String propertyUri = parts[1].trim();

			Label domainLabel = ontologyManager.getUriLabel(domainUri);
			if (domainLabel == null) continue;

			Label propertyLabel = ontologyManager.getUriLabel(propertyUri);
			if (propertyLabel == null) continue;

			SemanticType semanticType = new SemanticType(hNodeId, propertyLabel, domainLabel, Origin.TfIdfModel, confidence);
			logger.info("\t" + propertyUri + " of " + domainUri + ": " + confidence);
			suggestedSemanticTypes.add(semanticType);
		}
		Collections.sort(suggestedSemanticTypes, Collections.reverseOrder());
		return suggestedSemanticTypes;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
		
	}

}
