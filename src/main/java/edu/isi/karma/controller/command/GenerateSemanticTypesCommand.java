package edu.isi.karma.controller.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorkspace;

public class GenerateSemanticTypesCommand extends Command {
	
	private final String vWorksheetIdArg;

	protected GenerateSemanticTypesCommand(String id, String vWorksheetId) {
		super(id);
		this.vWorksheetIdArg = vWorksheetId;
	}

	@Override
	public String getCommandName() {
		return "Generate Semantic Types";
	}

	@Override
	public String getTitle() {
		return "Generate Semantic Types";
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
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Worksheet worksheet = vWorkspace.getViewFactory().getVWorksheet(
				vWorksheetIdArg).getWorksheet();
		Table table = worksheet.getDataTable();
		List<HNodePath> paths = worksheet.getHeaders().getAllPaths();
		
		for(HNodePath path: paths){
			Collection<Node> nodes = new ArrayList<Node>();
			//colle
		}
		return null;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}

}
