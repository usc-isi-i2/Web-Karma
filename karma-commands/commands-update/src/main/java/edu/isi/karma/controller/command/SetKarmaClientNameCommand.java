package edu.isi.karma.controller.command;

import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

import java.io.*;
import java.util.Properties;

/**
 * Created by alse on 10/4/16.
 * Every karma client is going to be assigned a random string as an id.
 * This id - karma client name - can be changed to a given value by calling this command
 */
public class SetKarmaClientNameCommand extends Command {
    private String karmaClientName;
    private String property = "karma.client.name";

    private static Logger logger = LoggerFactory.getLogger(SetKarmaClientNameCommand.class);

    protected SetKarmaClientNameCommand(String id, String model, String name) {
        super(id, model);
        karmaClientName = name;
    }

    @Override
    public String getCommandName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getTitle() {
        return "Set Karma name Configuration";
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
        try{

            uc.add(new AbstractUpdate() {
                @Override
                public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
                    try {
                        // read modeling.properties file
                        String fileName = ContextParametersRegistry.getInstance()
                                .getContextParameters(ContextParametersRegistry.getInstance().getDefault().getId())
                                .getParameterValue(ServletContextParameterMap.ContextParameter.USER_CONFIG_DIRECTORY)
                                + "/modeling.properties";

                        BufferedReader file = new BufferedReader(new FileReader(fileName));
                        String line;
                        String modelingPropertiesContent = "";
                        while ((line = file.readLine()) != null) {
                            if (line.startsWith(property)) {
                                modelingPropertiesContent += property + "=" + karmaClientName + '\n';
                            } else {
                                modelingPropertiesContent += line + '\n';
                            }
                        }
                        file.close();
                        FileOutputStream fileOut = new FileOutputStream(fileName);
                        fileOut.write(modelingPropertiesContent.getBytes());
                        fileOut.close();

                        // reload modeling configuration
                        ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ContextParametersRegistry.getInstance().getDefault().getId()).load();
                        JSONStringer jsonStr = new JSONStringer();

                        JSONWriter writer = jsonStr.object();
                        writer.key("updateType").value(this.getClass().getName());
                        writer.endObject();
                        pw.print(writer.toString());
                    } catch (Exception e) {
                        logger.error("Error updating Modeling Configuraion", e);
                    }
                }
            });
        }  catch (Exception e) {
			logger.error("Error updating Modeling Configuraion:" , e);
			uc.add(new ErrorUpdate("Error updating Modeling Configuraion"));
		}
        return uc;
    }

    @Override
    public UpdateContainer undoIt(Workspace workspace) {
        return null;
    }
}
