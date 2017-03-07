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
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;

public class UpdateModelConfigurationCommand extends Command {

	private boolean r2rml_export_superclass;
	
	private static Logger logger = LoggerFactory.getLogger(UpdateModelConfigurationCommand.class);
	
	protected UpdateModelConfigurationCommand(String id, String model, boolean r2rml_export_superclass) {
		super(id, model);
		this.r2rml_export_superclass = r2rml_export_superclass;
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
                            ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(vWorkspace.getWorkspace().getContextId());
							ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().register(contextParameters.getId());
							modelingConfiguration.setR2rmlExportSuperClass(r2rml_export_superclass);
							JSONStringer jsonStr = new JSONStringer();
							
							JSONWriter writer = jsonStr.object();
							writer.key("updateType").value("UpdateModelConfigurationUpdate");	
							writer.key("r2rml_export_superclass").value(r2rml_export_superclass);
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
