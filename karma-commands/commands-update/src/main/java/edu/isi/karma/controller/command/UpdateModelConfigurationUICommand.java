package edu.isi.karma.controller.command;

import java.io.PrintWriter;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;

public class UpdateModelConfigurationUICommand extends Command {

	private boolean show_super_class;
	
	private static Logger logger = LoggerFactory.getLogger(UpdateUIConfigurationCommand.class);
	
	protected UpdateUIConfigurationCommand(String id, String model, boolean show_super_class) {
		super(id, model);
		this.show_super_class = show_super_class;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Update Model Configuration";
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
							ContextParametersRegistry contextParametersRegistry = ContextParametersRegistry.getInstance();
							contextParameters = contextParametersRegistry.registerByKarmaHome(null);
							ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().register(contextParameters.getId());
							modelingConuiConfiguration.setShowSuperclass(this.show_super_class);
							JSONStringer jsonStr = new JSONStringer();
							
							JSONWriter writer = jsonStr.object();
							writer.key("updateType").value("UpdateModelConfigurationUIUpdate");	
							writer.key("show_super_class").value(show_super_class);
							writer.endObject();
							pw.print(writer.toString());
						} catch (Exception e) {
							logger.error("Error updating Model Configuraion", e);
						}
						
					}
					
				});
			
				return uc;
		} catch (Exception e) {
			logger.error("Error updating Model Configuraion:" , e);
			uc.add(new ErrorUpdate("Error updating Model Configuraion"));
			return uc;
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
