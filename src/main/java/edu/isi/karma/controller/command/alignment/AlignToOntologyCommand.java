package edu.isi.karma.controller.command.alignment;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.alignment.Vertex;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.rep.semantictypes.SemanticTypes;
import edu.isi.karma.view.VWorkspace;

public class AlignToOntologyCommand extends WorksheetCommand {

	protected AlignToOntologyCommand(String id, String worksheetId) {
		super(id, worksheetId);
	}

		@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Align to Ontology";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Worksheet worksheet = vWorkspace.getRepFactory().getWorksheet(worksheetId);
		
		// Creating a list of NameSet
		//ArrayList<SemanticType> semanticTypes = new ArrayList<SemanticType>();
		SemanticTypes semTypes = worksheet.getSemanticTypes();

		List<SemanticType> types = new ArrayList<SemanticType>();
		for(SemanticType type:semTypes.getTypes().values()) {
			types.add(type);
		}
		// Get the Alignment
		Alignment alignment = new Alignment(types);
		DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> tree = alignment.getSteinerTree();
		
		GraphUtil.printGraph(tree);
		
		UpdateContainer c = new UpdateContainer();
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// Not required
		return null;
	}
}
