package edu.isi.karma.controller.command.alignment;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

/**
 * Created by alse on 11/1/16.
 */
public class GithubPublishCommand extends Command {
    private static Logger logger = LoggerFactory.getLogger(GithubPublishCommand.class);
    private String modelName;

    protected GithubPublishCommand(String id, String model, String modelName) {
        super(id, model);
        this.modelName = modelName;
    }

    @Override
    public String getCommandName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getTitle() {
        return "Github Publish Command";
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
                        String dotFile = ContextParametersRegistry.getInstance()
                                .getContextParameters(ContextParametersRegistry.getInstance().getDefault().getId())
                                .getParameterValue(ServletContextParameterMap.ContextParameter.GRAPHVIZ_MODELS_DIR)
                                + modelName + ".model.dot";

                        String modelJson = ContextParametersRegistry.getInstance()
                                .getContextParameters(ContextParametersRegistry.getInstance().getDefault().getId())
                                .getParameterValue(ServletContextParameterMap.ContextParameter.JSON_PUBLISH_DIR) // or JSON_MODELS_DIR ?
                                + modelName + ".model.json";

                        JSONWriter writer = new JSONStringer().object();
                        writer.key("updateType").value(this.getClass().getName());
                        writer.endObject();
                        pw.print(writer.toString());
                    } catch (Exception e) {
                        logger.error("Error pushing to github", e);
                    }
                }
            });
        }  catch (Exception e) {
            logger.error("Error pushing to github" , e);
            uc.add(new ErrorUpdate("Error pushing to github"));
        }
        return uc;
    }

    @Override
    public UpdateContainer undoIt(Workspace workspace) {
        return null;
    }
}


