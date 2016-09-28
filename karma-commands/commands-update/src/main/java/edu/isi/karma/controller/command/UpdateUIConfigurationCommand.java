package edu.isi.karma.controller.command;

import java.io.PrintWriter;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.config.UIConfiguration;
import edu.isi.karma.config.UIConfigurationRegistry;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;

public class UpdateUIConfigurationCommand extends Command {

	private boolean show_rdfs_label_first;
	private boolean show_rdfs_id_first;
	
	private static Logger logger = LoggerFactory.getLogger(UpdateUIConfigurationCommand.class);
	
	protected UpdateUIConfigurationCommand(String id, String model, boolean show_rdfs_label_first, boolean show_rdfs_id_first) {
		super(id, model);
		this.show_rdfs_label_first = show_rdfs_label_first;
		this.show_rdfs_id_first = show_rdfs_id_first;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Set UI Configuration";
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
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		UpdateContainer uc = new UpdateContainer();
		try {
				
				uc.add(new AbstractUpdate() {

					@Override
					public void generateJson(String prefix, PrintWriter pw,
							VWorkspace vWorkspace) {
						try {
							UIConfiguration uiConfiguration = UIConfigurationRegistry.getInstance().getUIConfiguration(vWorkspace.getWorkspace().getContextId());
							uiConfiguration.updateShowRDFSLabelWithIDFirst(show_rdfs_id_first);
							uiConfiguration.updateShowRDFSLabelWithLabelFirst(show_rdfs_label_first);
							JSONStringer jsonStr = new JSONStringer();
							
							JSONWriter writer = jsonStr.object();
							writer.key("updateType").value("UpdateUIConfigurationUpdate");	
							writer.key("show_rdfs_id_first").value(show_rdfs_id_first);
							writer.key("show_rdfs_label_first").value(show_rdfs_label_first);
							writer.endObject();
							pw.print(writer.toString());
						} catch (Exception e) {
							logger.error("Error updating UI Configuraion", e);
						}
						
					}
					
				});
			
				return uc;
		} catch (Exception e) {
			logger.error("Error updating UI Configuraion:" , e);
			uc.add(new ErrorUpdate("Error updating UI Configuraion"));
			return uc;
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
