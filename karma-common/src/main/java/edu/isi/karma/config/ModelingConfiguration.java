/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.karma.config;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ModelingConfiguration {

	private static Logger logger = LoggerFactory.getLogger(ModelingConfiguration.class);


	private String contextId;
	private Boolean thingNode;
	private Boolean nodeClosure;
	private Boolean propertiesDirect;
	private Boolean propertiesIndirect;
	private Boolean propertiesWithOnlyDomain;
	private Boolean propertiesWithOnlyRange;
	private Boolean propertiesWithoutDomainRange;
	private Boolean propertiesSubClass;

	private String karmaSourcePrefix;
	private String karmaServicePrefix; 

	private Boolean trainOnApplyHistory;
	private Boolean predictOnApplyHistory;

	private Boolean compatibleProperties;
	private Boolean ontologyAlignment;
	private Boolean knownModelsAlignment;


	private Integer numCandidateMappings;
	private Integer mappingBranchingFactor;
	private Integer topKSteinerTree;


	private Double scoringConfidenceCoefficient;
	private Double scoringCoherenceSCoefficient;
	private Double scoringSizeCoefficient;

	private Boolean learnerEnabled;
	private Boolean addOntologyPaths;
//	private Boolean learnAlignmentEnabled;
	private Boolean multipleSamePropertyPerNode;
	private Boolean storeOldHistory;

	private Boolean showModelsWithoutMatching;
	private String defaultProperty = null;
	private String graphvizServer = null;

	private Boolean r2rmlExportSuperclass;

	private final String newLine = System.getProperty("line.separator");
	
	private String defaultModelingProperties = 
			"##########################################################################################" + newLine + 
			"#" + newLine + 
			"# Semantic Typing" + newLine + 
			"#" + newLine + 
			"##########################################################################################" + newLine + 
			"" + newLine + 
			"train.on.apply.history=false" + newLine + 
			"predict.on.apply.history=false" + newLine + 
			"" + newLine + 
			"##########################################################################################" + newLine + 
			"#" + newLine + 
			"# Alignment" + newLine + 
			"#" + newLine + 
			"##########################################################################################" + newLine + 
			"" + newLine + 
//			"manual.alignment=false" + newLine + 
			"# turning off the next two flags is equal to manual alignment" + newLine + 
			"compatible.properties=true" + newLine + 
			"ontology.alignment=false" + newLine + 
			"knownmodels.alignment=true" + newLine + 
			"" + newLine + 
			"##########################################################################################" + newLine + 
			"#" + newLine + 
			"# Graph Builder" + newLine + 
			"# (the flags in this section will only take effect when the \"ontology.alignment\" is true)" + newLine +
			"#" + newLine + 
			"##########################################################################################" + newLine + 
			"" + newLine + 
			"thing.node=false" + newLine + 
			"" + newLine + 
			"node.closure=true" + newLine + 
			"" + newLine + 
			"properties.direct=true" + newLine + 
			"properties.indirect=true" + newLine + 
			"properties.subclass=true" + newLine + 
			"properties.with.only.domain=true" + newLine + 
			"properties.with.only.range=true" + newLine + 
			"properties.without.domain.range=false" + newLine + 
			"" + newLine + 
			"##########################################################################################" + newLine + 
			"#" + newLine + 
			"# Prefixes" + newLine + 
			"#" + newLine + 
			"##########################################################################################" + newLine + 
			"" + newLine + 
			"karma.source.prefix=http://isi.edu/integration/karma/sources/" + newLine + 
			"karma.service.prefix=http://isi.edu/integration/karma/services/" + newLine + 
			"default.property=http://schema.org/name" + newLine +
			"" + newLine + 
			"##########################################################################################" + newLine + 
			"#" + newLine + 
			"# Model Learner" + newLine + 
			"#" + newLine + 
			"##########################################################################################" + newLine + 
			"" + newLine + 
			"learner.enabled=true" + newLine + 
			"" + newLine + 
			"add.ontology.paths=false" + newLine + 
			"" + newLine + 
//			"learn.alignment.enabled=false" + newLine + 
//			"" + newLine + 
			"mapping.branching.factor=50" + newLine + 
			"num.candidate.mappings=10" + newLine + 
			"topk.steiner.tree=10" + newLine + 
			"multiple.same.property.per.node=false" + newLine + 
			"" + newLine + 
			"# scoring coefficients, should be in range [0..1]" + newLine + 
			"scoring.confidence.coefficient=1.0" + newLine + 
			"scoring.coherence.coefficient=1.0" + newLine + 
			"scoring.size.coefficient=0.5" + newLine + 
			"" + newLine + 
			"##########################################################################################" + newLine + 
			"#" + newLine + 
			"# Other Settings" + newLine + 
			"#" + newLine + 
			"##########################################################################################" + newLine + 
			"" + newLine + 
			"models.display.nomatching=false" + newLine +
			"history.store.old=false" + newLine + 
			"graphviz.server=http://karma-svc.isi.edu/graphviz/" + newLine +
			"r2rml.export.superclass=false"
			;

    private Properties modelingProperties;

	public void load() {
		try {
			this.modelingProperties = loadParams();
			trainOnApplyHistory = Boolean.parseBoolean(modelingProperties.getProperty("train.on.apply.history", "false"));
			predictOnApplyHistory = Boolean.parseBoolean(modelingProperties.getProperty("predict.on.apply.history", "false"));

//			ontologyAlignment = Boolean.parseBoolean(modelingProperties.getProperty("compatible.properties", "true"));

			String compatiblePropertiesStr = modelingProperties.getProperty("compatible.properties");
			if(compatiblePropertiesStr != null)
				compatibleProperties = Boolean.parseBoolean(compatiblePropertiesStr);
			else {
				//need to add this property to the end
				compatibleProperties = true;
				addProperty("compatible.properties", "true");
			}
			
//			ontologyAlignment = Boolean.parseBoolean(modelingProperties.getProperty("ontology.alignment", "false"));

			String ontologyAlignmentStr = modelingProperties.getProperty("ontology.alignment");
			if(ontologyAlignmentStr != null)
				ontologyAlignment = Boolean.parseBoolean(ontologyAlignmentStr);
			else {
				//need to add this property to the end
				ontologyAlignment = false;
				addProperty("ontology.alignment", "false");
			}
			
//			knownModelsAlignment = Boolean.parseBoolean(modelingProperties.getProperty("knownmodels.alignment", "false"));
			
			String knownModelsAlignmentStr = modelingProperties.getProperty("knownmodels.alignment");
			if(knownModelsAlignmentStr != null)
				knownModelsAlignment = Boolean.parseBoolean(knownModelsAlignmentStr);
			else {
				//need to add this property to the end
				knownModelsAlignment = true;
				addProperty("knownmodels.alignment", "true");
			}
			
//			learnerEnabled = Boolean.parseBoolean(modelingProperties.getProperty("learner.enabled", "true"));
			
			String learnerEnabledStr = modelingProperties.getProperty("learner.enabled");
			if(learnerEnabledStr != null)
				learnerEnabled = Boolean.parseBoolean(learnerEnabledStr);
			else {
				//need to add this property to the end
				learnerEnabled = true;
				addProperty("learner.enabled", "true");
			}

//			addOntologyPaths = Boolean.parseBoolean(modelingProperties.getProperty("add.ontology.paths", "true"));

			String addOntologyPathsStr = modelingProperties.getProperty("add.ontology.paths");
			if(addOntologyPathsStr != null)
				addOntologyPaths = Boolean.parseBoolean(addOntologyPathsStr);
			else {
				//need to add this property to the end
				addOntologyPaths = true;
				addProperty("add.ontology.paths", "false");
			}
			
			thingNode = Boolean.parseBoolean(modelingProperties.getProperty("thing.node", "false"));

			nodeClosure = Boolean.parseBoolean(modelingProperties.getProperty("node.closure", "true"));

			propertiesDirect = Boolean.parseBoolean(modelingProperties.getProperty("properties.direct", "true"));

			propertiesIndirect = Boolean.parseBoolean(modelingProperties.getProperty("properties.indirect", "true"));

			propertiesWithOnlyDomain = Boolean.parseBoolean(modelingProperties.getProperty("properties.with.only.domain", "true"));

			propertiesWithOnlyRange = Boolean.parseBoolean(modelingProperties.getProperty("properties.with.only.range", "true"));

			propertiesWithoutDomainRange = Boolean.parseBoolean(modelingProperties.getProperty("properties.without.domain.range", "false"));

			propertiesSubClass = Boolean.parseBoolean(modelingProperties.getProperty("properties.subclass", "true"));

			karmaSourcePrefix = modelingProperties.getProperty("karma.source.prefix", "http://isi.edu/integration/karma/sources/");
			karmaServicePrefix = modelingProperties.getProperty("karma.service.prefix", "http://isi.edu/integration/karma/services/");

			mappingBranchingFactor = Integer.parseInt(modelingProperties.getProperty("mapping.branching.factor", "10"));

			numCandidateMappings = Integer.parseInt(modelingProperties.getProperty("num.candidate.mappings", "10"));

			topKSteinerTree = Integer.parseInt(modelingProperties.getProperty("topk.steiner.tree", "20"));

			multipleSamePropertyPerNode = Boolean.parseBoolean(modelingProperties.getProperty("multiple.same.property.per.node", "false"));

			scoringConfidenceCoefficient = Double.parseDouble(modelingProperties.getProperty("scoring.confidence.coefficient", "1"));

			scoringCoherenceSCoefficient = Double.parseDouble(modelingProperties.getProperty("scoring.coherence.coefficient", "1"));

			scoringSizeCoefficient = Double.parseDouble(modelingProperties.getProperty("scoring.size.coefficient", "0.5"));

			storeOldHistory = Boolean.parseBoolean(modelingProperties.getProperty("history.store.old", "false"));

			showModelsWithoutMatching = Boolean.parseBoolean(modelingProperties.getProperty("models.display.nomatching", "false"));

			defaultProperty = modelingProperties.getProperty("default.property");
			if(defaultProperty == null) {
				//need to add this property to the end
				addProperty("default.property", "http://schema.org/name");
			}
			
			graphvizServer = modelingProperties.getProperty("graphviz.server");
			if(graphvizServer == null) {
				graphvizServer = "http://karma-svc.isi.edu/graphviz/";
				addProperty("graphviz.server", graphvizServer);
			} else if(graphvizServer.equals("http://52.38.65.60/graphviz/")) {
				graphvizServer = "http://karma-svc.isi.edu/graphviz/";
				updateProperty("graphviz.server", graphvizServer);
			}

			String r2rml_export_superclass = modelingProperties.getProperty("r2rml.export.superclass");
			if(r2rml_export_superclass!=null)
			{
				this.r2rmlExportSuperclass = Boolean.parseBoolean(r2rml_export_superclass);
			}
			else
			{
				this.r2rmlExportSuperclass = false;
				addProperty("r2rml.export.superclass", "false");
			}

		} catch (IOException e) {
			logger.error("Error occured while reading config file ...", e);
			System.exit(1);
		}
	}

	public ModelingConfiguration(String contextId)
	{
		this.contextId = contextId;
	}
	private Properties loadParams()
			throws IOException {
		Properties prop = new Properties();

		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(contextId);
		File file = new File(contextParameters.getParameterValue(ContextParameter.USER_CONFIG_DIRECTORY) + "/modeling.properties");
		logger.info("Load modeling.properties: " + file.getAbsolutePath() + ":" + file.exists());
		if(!file.exists()) {
			file.createNewFile();
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			BufferedWriter bw = new BufferedWriter(fw);
			logger.debug(defaultModelingProperties);
			bw.write(defaultModelingProperties);
			bw.close();
			logger.debug("Written default properties to modeling.properties");
		}

		FileInputStream fis = new FileInputStream(file);
		try {
			prop.load(fis);
		} finally {
			fis.close();
		}
		logger.debug("Done Loading modeling.properties");


		return prop;
	}

	public Boolean getThingNode() {

		if (getOntologyAlignment() == false)
			return false;

		if (thingNode == null)
			load();

		return thingNode;
	}

	public Boolean getNodeClosure() {

		if (getOntologyAlignment() == false)
			return false;

		if (nodeClosure == null)
			load();

		return nodeClosure;
	}

//	public static Boolean getManualAlignment() {
//		if (manualAlignment == null) {
//			load();
//			logger.debug("Manual Alignment:" + manualAlignment);
//		}
//		return manualAlignment;
//	}
	
	public Boolean getTrainOnApplyHistory() {
		if (trainOnApplyHistory == null) {
			load();
		}
		return trainOnApplyHistory;
	}
	
	public Boolean getPredictOnApplyHistory() {
		if (predictOnApplyHistory == null) {
			load();
		}
		return predictOnApplyHistory;
	}
	
	public void setTrainOnApplyHistory(Boolean trainOnApplyHistory) {
		this.trainOnApplyHistory = trainOnApplyHistory;
	}

	public void setPredictOnApplyHistory(Boolean predictOnApplyHistory) {
		this.predictOnApplyHistory = predictOnApplyHistory;
	}

	public Boolean getCompatibleProperties() {
		if (compatibleProperties == null) {
			load();
		}
		return compatibleProperties;
	}
	
	public void setCompatibleProperties(Boolean compatibleProperties) {
		this.compatibleProperties = compatibleProperties;
	}
	
	public Boolean getOntologyAlignment() {
		if (ontologyAlignment == null) {
			load();
			logger.debug("Use Ontology in Alignment:" + ontologyAlignment);
		}
		return ontologyAlignment;
	}
	
	public void setOntologyAlignment(Boolean ontologyAlignment) {
		this.ontologyAlignment = ontologyAlignment;
	}
	
	public Boolean getKnownModelsAlignment() {
		if (knownModelsAlignment == null) {
			load();
			logger.debug("Use Known Models in Alignment:" + knownModelsAlignment);
		}
		return knownModelsAlignment;
	}

	public void setKnownModelsAlignment(Boolean knownModelsAlignment) {
		this.knownModelsAlignment = knownModelsAlignment;
	}
	
	public Boolean getPropertiesDirect() {
		if (propertiesDirect == null)
			load();
		return propertiesDirect;
	}

	public Boolean getPropertiesIndirect() {
		if (propertiesIndirect == null)
			load();
		return propertiesIndirect;
	}

	public Boolean getPropertiesWithOnlyDomain() {
		if (propertiesWithOnlyDomain == null)
			load();
		return propertiesWithOnlyDomain;
	}

	public Boolean getPropertiesWithOnlyRange() {
		if (propertiesWithOnlyRange == null)
			load();
		return propertiesWithOnlyRange;
	}

	public Boolean getPropertiesWithoutDomainRange() {
		if (propertiesWithoutDomainRange == null)
			load();
		return propertiesWithoutDomainRange;
	}

	public Boolean getPropertiesSubClass() {
		if (propertiesSubClass == null)
			load();
		return propertiesSubClass;
	}

	public String getKarmaSourcePrefix() {
		if (karmaSourcePrefix == null)
			load();
		return karmaSourcePrefix.trim();
	}

	public String getKarmaServicePrefix() {
		if (karmaServicePrefix == null)
			load();
		return karmaServicePrefix.trim();
	}

	public Integer getNumCandidateMappings() {
		if (numCandidateMappings == null)
			load();
		return numCandidateMappings;
	}

	public Integer getMappingBranchingFactor() {
		if (mappingBranchingFactor == null)
			load();
		return mappingBranchingFactor;
	}
	
	public Integer getTopKSteinerTree() {
		if (topKSteinerTree == null)
			load();
		return topKSteinerTree;
	}

	public Double getScoringConfidenceCoefficient() {
		if (scoringConfidenceCoefficient == null)
			load();
		return scoringConfidenceCoefficient;
	}

	public Double getScoringCoherenceSCoefficient() {
		if (scoringCoherenceSCoefficient == null)
			load();
		return scoringCoherenceSCoefficient;
	}

	public Double getScoringSizeCoefficient() {
		if (scoringSizeCoefficient == null)
			load();
		return scoringSizeCoefficient;
	}

	public boolean isLearnerEnabled() {
		if (learnerEnabled == null)
			load();
		return learnerEnabled;
	}

	public void setLearnerEnabled(Boolean learnerEnabled) {
		this.learnerEnabled = learnerEnabled;
	}
	
	public boolean getAddOntologyPaths() {
		if (addOntologyPaths == null)
			load();
		return addOntologyPaths;
	}

	public void setAddOntologyPaths(Boolean addOntologyPaths) {
		this.addOntologyPaths = addOntologyPaths;
	}
	
	public boolean isStoreOldHistoryEnabled() {
		if (storeOldHistory == null)
			load();
		return storeOldHistory;
	}

	public boolean isShowModelsWithoutMatching() {
		if (showModelsWithoutMatching == null)
			load();
		return showModelsWithoutMatching;
	}

	public boolean isMultipleSamePropertyPerNode() {
		if (multipleSamePropertyPerNode == null)
			load();
		return multipleSamePropertyPerNode;
	}

	public String getDefaultProperty() {
		if(defaultProperty == null)
			load();
		return defaultProperty;
	}
	
	public void setManualAlignment()
	{
		ontologyAlignment = false;
		knownModelsAlignment = false;
	}

	public String getGraphvizServer() {
		return graphvizServer;
	}

	public void setR2rmlExportSuperClass(boolean r2rml_export_superclass) throws IOException {
		this.r2rmlExportSuperclass = r2rml_export_superclass;
		this.updateProperty("r2rml.export.superclass",Boolean.toString(r2rml_export_superclass));
	}
	
	public Boolean getR2rmlExportSuperClass() {
		if (r2rmlExportSuperclass == null)
			load();
		return r2rmlExportSuperclass;
	}

	private void addProperty(String key, String value) throws IOException {
		File file = new File(ContextParametersRegistry.getInstance().getContextParameters(contextId).getParameterValue(ContextParameter.USER_CONFIG_DIRECTORY) + "/modeling.properties");
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
		out.println(key + "=" + value);
		out.close();
		this.modelingProperties.put(key, value);
	}

	private void updateProperty(String key, String value) throws IOException {

		String fileName = ContextParametersRegistry.getInstance()
									.getContextParameters(contextId)
									.getParameterValue(ContextParameter.USER_CONFIG_DIRECTORY)
									+ "/modeling.properties";
		File file = new File(fileName);
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line = null;
		StringBuffer buf = new StringBuffer();
		try {
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.startsWith("#") && line.contains(key)) {
					buf.append(key + "=" + value);
				} else {
					buf.append(line);
				}
				buf.append(System.getProperty("line.separator"));
			}
			reader.close();
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(file, false)));
			out.write(buf.toString());
			out.close();
		} catch (IOException e) {
			logger.error("Error updating property: " + key, e);
		}
	}
}
