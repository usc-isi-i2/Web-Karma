package edu.isi.karma.controller.command.publish;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.geospatial.WorksheetGeospatialContent;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorkspace;

public class PublishKMLLayerCommand extends Command {
	private final String vWorksheetId;
	private String publicIPAddress;

	public enum JsonKeys {
		updateType, fileName
	}

	private static Logger logger = LoggerFactory
			.getLogger(PublishKMLLayerCommand.class);

	protected PublishKMLLayerCommand(String id, String vWorksheetId,
			String ipAddress) {
		super(id);
		this.vWorksheetId = vWorksheetId;
		this.publicIPAddress = ipAddress;
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
			final File file = geo.publishKML();
			return new UpdateContainer(new AbstractUpdate() {

				@Override
				public void generateJson(String prefix, PrintWriter pw,
						VWorkspace vWorkspace) {
					JSONObject outputObject = new JSONObject();
					try {
						outputObject.put(JsonKeys.updateType.name(),
								"PublishKMLUpdate");
						outputObject.put(JsonKeys.fileName.name(),
								"http://" + publicIPAddress + ":8080/KML/"
										+ file.getName());
						pw.println(outputObject.toString(4));
					} catch (JSONException e) {
						logger.error("Error occured while generating JSON!");
					}
				}
			});
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
