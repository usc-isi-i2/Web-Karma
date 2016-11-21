package edu.isi.karma.controller.command.publish;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Worksheet;
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
 * Created by alse on 11/21/16.
 */
public class PublishGithubCommand extends Command {
    private static Logger logger = LoggerFactory.getLogger(PublishGithubCommand.class);
    private String worksheetID;
    private String repo;
    private String branch;
    private String username;
    private String password;

    public PublishGithubCommand(String id, String model, String worksheetID, String repo, String branch, String username, String password) {
        super(id, model);
        this.worksheetID = worksheetID;
        this.repo = repo;
        this.branch = branch;
        this.username = username;
        this.password = password;
    }
    @Override
    public String getCommandName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getTitle() {
        return "Publish Github Command";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public CommandType getCommandType() {
        return null;
    }
    @Override
    public UpdateContainer doIt(Workspace workspace) throws CommandException {
        UpdateContainer uc = new UpdateContainer();
        try{

            Worksheet worksheet = workspace.getWorksheet(this.worksheetID);

            String modelName = worksheet.getTitle();

            String dotFile = ContextParametersRegistry.getInstance()
                    .getContextParameters(ContextParametersRegistry.getInstance().getDefault().getId())
                    .getParameterValue(ServletContextParameterMap.ContextParameter.GRAPHVIZ_MODELS_DIR)
                    + modelName + ".model.dot";

            String modelFile = ContextParametersRegistry.getInstance()
                    .getContextParameters(ContextParametersRegistry.getInstance().getDefault().getId())
                    .getParameterValue(ServletContextParameterMap.ContextParameter.JSON_PUBLISH_DIR)
                    + modelName + ".model.json";


            System.out.println("TODO " + dotFile + " " + modelFile);

            uc.add(new AbstractUpdate() {
                @Override
                public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
                    try {
                        JSONWriter writer = new JSONStringer().object();
                        writer.key("updateType").value(this.getClass().getName());
                        writer.endObject();
                        pw.print(writer.toString());
                    } catch (Exception e) {
                        logger.error("Error unable to set Github", e);
                    }
                }
            });
        }  catch (Exception e) {
            logger.error("Error unable to set Github" , e);
            uc.add(new ErrorUpdate("Error unable to set Github"));
        }
        return uc;
    }

    @Override
    public UpdateContainer undoIt(Workspace workspace) {
        return null;
    }
}
