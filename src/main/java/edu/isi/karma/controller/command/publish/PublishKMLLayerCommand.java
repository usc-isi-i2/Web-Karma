package edu.isi.karma.controller.command.publish;

import java.io.FileNotFoundException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.geospatial.WorksheetGeospatialContent;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorkspace;

public class PublishKMLLayerCommand extends Command {
	private final String vWorksheetId;

	protected PublishKMLLayerCommand(String id, String vWorksheetId) {
		super(id);
		this.vWorksheetId = vWorksheetId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Publish KML Layer";
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
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();

		WorksheetGeospatialContent geo = new WorksheetGeospatialContent(
				worksheet);
		// Send an error update if no geospatial data found!
		if (geo.hasNoGeospatialData()) {
			return new UpdateContainer(new ErrorUpdate("PublishKMLError",
					"No geospatial data found in the worksheet!"));
		}

		try {
			geo.publishKML();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return new UpdateContainer();
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
