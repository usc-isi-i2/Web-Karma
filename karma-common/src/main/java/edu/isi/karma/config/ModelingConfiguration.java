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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ModelingConfiguration {

	private static Logger logger = LoggerFactory.getLogger(ModelingConfiguration.class);
	private String contextId;
	private Boolean manualAlignment;
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

//	private static String modelsJsonDir;
//	private static String modelsGraphvizDir;
//	private static String alignmentGraphDir; 

	private Integer maxCandidateModels;
	private Integer maxQueuedMappigs;

	private Double scoringConfidenceCoefficient;
	private Double scoringCoherenceSCoefficient;
	private Double scoringSizeCoefficient;

	private Boolean learnerEnabled;
	private Boolean learnAlignmentEnabled;
	private Boolean multipleSamePropertyPerNode;

	private Boolean storeOldHistory;

	private Boolean showModelsWithoutMatching;

	private final String newLine = System.getProperty("line.separator").toString();
	
	private String defaultModelingProperties = 
			"##########################################################################################" + newLine + 
			"#" + newLine + 
			"# Graph Builder" + newLine + 
			"#" + newLine + 
			"##########################################################################################" + newLine + 
			"manual.alignment=false" + newLine + 
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
			"karma.source.prefix=http://isi.edu/integration/karma/sources/" + newLine + 
			"karma.service.prefix=http://isi.edu/integration/karma/services/" + newLine + 
			"" + newLine + 
			"##########################################################################################" + newLine + 
			"#" + newLine + 
			"# Model Learner" + newLine + 
			"#" + newLine + 
			"##########################################################################################" + newLine + 
			"learner.enabled=true" + newLine + 
			"" + newLine + 
			"learn.alignment.enabled=false" + newLine + 
			"" + newLine + 
			"max.queued.mappings=100" + newLine + 
			"max.candidate.models=5" + newLine + 
			"multiple.same.property.per.node=false" + newLine + 
			"" + newLine + 
			"# scoring coefficients, should be in range [0..1]" + newLine + 
			"scoring.confidence.coefficient=1.0" + newLine + 
			"scoring.coherence.coefficient=1.0" + newLine + 
			"scoring.size.coefficient=1.0" + newLine + 
			"" + newLine + 
//			"models.json.dir=models-json/" + newLine + 
//			"models.graphviz.dir=models-graphviz/" + newLine + 
//			"alignment.graph.dir=alignment-graph/" + newLine +
//			"" + newLine + 
			"##########################################################################################" + newLine + 
			"#" + newLine + 
			"# Other Settings" + newLine + 
			"#" + newLine + 
			"##########################################################################################" + newLine + 
			"models.display.nomatching=false" + newLine +
			"history.store.old=false"
			;


	public void load() {
		try {
			Properties modelingProperties = loadParams();

			manualAlignment = Boolean.parseBoolean(modelingProperties.getProperty("manual.alignment", "false"));

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

			learnerEnabled = Boolean.parseBoolean(modelingProperties.getProperty("learner.enabled", "true"));

			learnAlignmentEnabled = Boolean.parseBoolean(modelingProperties.getProperty("learn.alignment.enabled", "false"));

//			modelsJsonDir = modelingProperties.getProperty("models.json.dir", "models-json/");
//			modelsGraphvizDir = modelingProperties.getProperty("models.graphviz.dir", "models-graphviz/");
//			alignmentGraphDir = modelingProperties.getProperty("alignment.graph.dir", "alignment-graph/");

			maxQueuedMappigs = Integer.parseInt(modelingProperties.getProperty("max.queued.mappings", "100"));

			maxCandidateModels = Integer.parseInt(modelingProperties.getProperty("max.candidate.models", "5"));

			multipleSamePropertyPerNode = Boolean.parseBoolean(modelingProperties.getProperty("multiple.same.property.per.node", "false"));

			scoringConfidenceCoefficient = Double.parseDouble(modelingProperties.getProperty("scoring.confidence.coefficient", "1"));

			scoringCoherenceSCoefficient = Double.parseDouble(modelingProperties.getProperty("scoring.coherence.coefficient", "1"));

			scoringSizeCoefficient = Double.parseDouble(modelingProperties.getProperty("scoring.size.coefficient", "1"));

			storeOldHistory = Boolean.parseBoolean(modelingProperties.getProperty("history.store.old", "false"));

			showModelsWithoutMatching = Boolean.parseBoolean(modelingProperties.getProperty("models.display.nomatching", "false"));

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

		prop.load(new FileInputStream(file));
		logger.debug("Done Loading modeling.properties");


		return prop;
	}

	public Boolean getThingNode() {

		if (getManualAlignment() == true)
			return false;

		if (thingNode == null)
			load();

		return thingNode;
	}

	public Boolean getNodeClosure() {

		if (getManualAlignment() == true)
			return false;

		if (nodeClosure == null)
			load();

		return nodeClosure;
	}

	public Boolean getManualAlignment() {
		if (manualAlignment == null) {
			load();
			logger.debug("Manual Alignment:" + manualAlignment);
		}
		return manualAlignment;
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

//	public static String getModelsJsonDir() {
//		if (modelsJsonDir == null)
//			load();
//		return modelsJsonDir;
//	}
//
//	public static String getModelsGraphvizDir() {
//		if (modelsGraphvizDir == null)
//			load();
//		return modelsGraphvizDir;
//	}
//
//	public static String getAlignmentGraphDir() {
//		if (alignmentGraphDir == null)
//			load();
//		return alignmentGraphDir;
//	}

	public Integer getMaxCandidateModels() {
		if (maxCandidateModels == null)
			load();
		return maxCandidateModels;
	}

	public Integer getMaxQueuedMappigs() {
		if (maxQueuedMappigs == null)
			load();
		return maxQueuedMappigs;
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

	public boolean isLearnAlignmentEnabled() {
		if (learnAlignmentEnabled == null)
			load();
		return learnAlignmentEnabled;
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

	public void setLearnerEnabled(Boolean learnerEnabled) {
		this.learnerEnabled = learnerEnabled;
	}

	public boolean isMultipleSamePropertyPerNode() {
		if (multipleSamePropertyPerNode == null)
			load();
		return multipleSamePropertyPerNode;
	}

	public void setManualAlignment(Boolean newManualAlignment)
	{
		manualAlignment = newManualAlignment;
	}

}
