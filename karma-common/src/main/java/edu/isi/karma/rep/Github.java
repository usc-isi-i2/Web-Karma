package edu.isi.karma.rep;

import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;

/**
 * Created by alse on 11/5/16.
 */
public class Github {
    private String repo;
    private String branch;
    private Boolean enabled;

    public Github(String repo, String branch) {
        this.repo = repo;
        this.branch = branch;
        this.enabled = true;
    }

    public void publish(Worksheet worksheet, String username, String password, String message){
        if (this.enabled && isValidConnection(username, password)) {
            String modelName = worksheet.getTitle();

            String dotFile = ContextParametersRegistry.getInstance()
                    .getContextParameters(ContextParametersRegistry.getInstance().getDefault().getId())
                    .getParameterValue(ServletContextParameterMap.ContextParameter.GRAPHVIZ_MODELS_DIR)
                    + modelName + ".model.dot";

            String modelFile = ContextParametersRegistry.getInstance()
                    .getContextParameters(ContextParametersRegistry.getInstance().getDefault().getId())
                    .getParameterValue(ServletContextParameterMap.ContextParameter.JSON_PUBLISH_DIR)
                    + modelName + ".model.json";
        }
    }

    // checks if user has write connection to the repository
    public Boolean isValidConnection(String username, String password){
        return false;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
