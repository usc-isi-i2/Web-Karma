/**
 * *****************************************************************************
 * Copyright 2012 University of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * This code was developed by the Information Integration Group as part of the
 * Karma project at the Information Sciences Institute of the University of
 * Southern California. For more information, publications, and related
 * projects, please see: http://www.isi.edu/integration
 * ****************************************************************************
 */
package edu.isi.karma.controller.command.alignment;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.publish.PublishRDFCommand;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.controller.history.WorksheetCommandHistoryReader;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLMappingGenerator;
import edu.isi.karma.kr2rml.KR2RMLModelGenerator;
import edu.isi.karma.kr2rml.WorksheetModelWriter;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;
import java.io.Writer;

public class GenerateR2RMLModelCommand extends Command {

    private final String worksheetId;
    private String worksheetName;
    private String tripleStoreUrl;
    private String graphContext;
    private static Logger logger = LoggerFactory.getLogger(GenerateR2RMLModelCommand.class);

    public enum JsonKeys {

        updateType, fileUrl, worksheetId
    }

    public enum PreferencesKeys {

        rdfPrefix, rdfNamespace, modelSparqlEndPoint
    }

    protected GenerateR2RMLModelCommand(String id, String worksheetId, String url, String context) {
        super(id);
        this.worksheetId = worksheetId;
        this.tripleStoreUrl = url;
        this.graphContext = context;
    }

    public String getTripleStoreUrl() {
        return tripleStoreUrl;
    }

    public void setTripleStoreUrl(String tripleStoreUrl) {
        this.tripleStoreUrl = tripleStoreUrl;
    }

    public String getGraphContext() {
        return graphContext;
    }

    public void setGraphContext(String graphContext) {
        this.graphContext = graphContext;
    }

    @Override
    public String getCommandName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getTitle() {
        return "Generate R2RML Model";
    }

    @Override
    public String getDescription() {
        return this.worksheetName;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.notUndoable;
    }

    @Override
    public UpdateContainer doIt(Workspace workspace) throws CommandException {

        //save the preferences 
        savePreferences(workspace);

        Worksheet worksheet = workspace.getWorksheet(worksheetId);
        this.worksheetName = worksheet.getTitle();

        // Prepare the model file path and names
        final String modelFileName = workspace.getCommandPreferencesId() + worksheetId + "-"
                + this.worksheetName + "-model.ttl";
        final String modelFileLocalPath = ServletContextParameterMap.getParameterValue(
                ContextParameter.USER_DIRECTORY_PATH) + "publish/R2RML/" + modelFileName;
        

        try {
            KR2RMLModelGenerator modelGenerator = new KR2RMLModelGenerator();
            
            KR2RMLMappingGenerator mappingGen = modelGenerator.generateModel(workspace, worksheet);
            // Write the model
            PrintWriter writer = new PrintWriter(modelFileLocalPath, "UTF-8");
            modelGenerator.writeModel(workspace, mappingGen, worksheet, writer);

            // Write the model to the triple store
            TripleStoreUtil utilObj = new TripleStoreUtil();

            // Get the graph name from properties
            String graphName = worksheet.getMetadataContainer().getWorksheetProperties()
                    .getPropertyValue(Property.graphName);
            if (graphName == null || graphName.isEmpty()) {
                // Set to default
                worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(
                        Property.graphName, WorksheetProperties.createDefaultGraphName(worksheet.getTitle()));
                graphName = WorksheetProperties.createDefaultGraphName(worksheet.getTitle());
            }

            boolean result = utilObj.saveToStore(modelFileLocalPath, tripleStoreUrl, graphName, true);
            if (result) {
                logger.info("Saved model to triple store");
                return new UpdateContainer(new AbstractUpdate() {
                    public void generateJson(String prefix, PrintWriter pw,
                            VWorkspace vWorkspace) {
                        JSONObject outputObject = new JSONObject();
                        try {
                            outputObject.put(JsonKeys.updateType.name(), "PublishR2RMLUpdate");
                            outputObject.put(JsonKeys.fileUrl.name(), "publish/R2RML/" + modelFileName);
                            outputObject.put(JsonKeys.worksheetId.name(), worksheetId);
                            pw.println(outputObject.toString());
                        } catch (JSONException e) {
                            logger.error("Error occured while generating JSON!");
                        }
                    }
                });
            }

            return new UpdateContainer(new ErrorUpdate("Error occured while generating R2RML model!"));

        } catch (NullPointerException e) {
            logger.info("Alignment is NULL for " + worksheet.getId());
            return new UpdateContainer(new ErrorUpdate(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return new UpdateContainer(new ErrorUpdate("Error occured while generating R2RML model!"));
        }
    }

    @Override
    public UpdateContainer undoIt(Workspace workspace) {
        // Not required
        return null;
    }

    

    private void savePreferences(Workspace workspace) {
        try {
            JSONObject prefObject = new JSONObject();
            prefObject.put(PreferencesKeys.modelSparqlEndPoint.name(), tripleStoreUrl);
            workspace.getCommandPreferences().setCommandPreferences(
                    "GenerateR2RMLModelCommandPreferences", prefObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
