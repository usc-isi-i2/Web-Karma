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

import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ModelingConfiguration {

	private static Logger logger = LoggerFactory.getLogger(ModelingConfiguration.class);

	private static Boolean manualAlignment;
	private static Boolean thingNode;
	private static Boolean nodeClosure;
	private static Boolean propertiesDirect;
	private static Boolean propertiesIndirect;
	private static Boolean propertiesWithOnlyDomain;
	private static Boolean propertiesWithOnlyRange;
	private static Boolean propertiesWithoutDomainRange;
	private static Boolean propertiesSubClass;

	private static String karmaSourcePrefix;
	private static String karmaServicePrefix; 

	private static String modelsJsonDir;
	private static String modelsGraphvizDir;
	private static String alignmentGraphDir; 

	private static Integer maxCandidateModels;
	private static Integer maxQueuedMappigs;

	private static Double scoringConfidenceCoefficient;
	private static Double scoringCoherenceSCoefficient;
	private static Double scoringSizeCoefficient;

	private static Boolean learnerEnabled;
	private static Boolean multipleSamePropertyPerNode;

	private static Boolean storeOldHistory;

	private static Boolean showModelsWithoutMatching;

	private static final String newLine = System.getProperty("line.separator").toString();
	private static String defaultModelingProperties = 
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
			"max.queued.mappings=100" + newLine + 
			"max.candidate.models=5" + newLine + 
			"multiple.same.property.per.node=true" + newLine + 
			"" + newLine + 
			"# scoring coefficients, should be in range [0..1]" + newLine + 
			"scoring.confidence.coefficient=1.0" + newLine + 
			"scoring.coherence.coefficient=1.0" + newLine + 
			"scoring.size.coefficient=1.0" + newLine + 
			"" + newLine + 
			"models.json.dir=JSON/" + newLine + 
			"models.graphviz.dir=GRAPHVIZ/" + newLine + 
			"alignment.graph.dir=AlignmentGraph/" + newLine +
			"" + newLine + 
			"##########################################################################################" + newLine + 
			"#" + newLine + 
			"# Other Settings" + newLine + 
			"#" + newLine + 
			"##########################################################################################" + newLine + 
			"models.display.nomatching=false" + newLine +
			"history.store.old=false"
			;

	public static void load() {
		try {
			Properties modelingProperties = loadParams();

			if(modelingProperties.getProperty("manual.alignment") != null)
				manualAlignment = Boolean.parseBoolean(modelingProperties.getProperty("manual.alignment"));

			if(modelingProperties.getProperty("thing.node") != null)
				thingNode = Boolean.parseBoolean(modelingProperties.getProperty("thing.node"));

			if(modelingProperties.getProperty("node.closure") != null)
				nodeClosure = Boolean.parseBoolean(modelingProperties.getProperty("node.closure"));

			if(modelingProperties.getProperty("properties.direct") != null)
				propertiesDirect = Boolean.parseBoolean(modelingProperties.getProperty("properties.direct"));

			if(modelingProperties.getProperty("properties.indirect") != null)
				propertiesIndirect = Boolean.parseBoolean(modelingProperties.getProperty("properties.indirect"));

			if(modelingProperties.getProperty("properties.with.only.domain") != null)
				propertiesWithOnlyDomain = Boolean.parseBoolean(modelingProperties.getProperty("properties.with.only.domain"));

			if(modelingProperties.getProperty("properties.with.only.range") != null)
				propertiesWithOnlyRange = Boolean.parseBoolean(modelingProperties.getProperty("properties.with.only.range"));

			if(modelingProperties.getProperty("properties.without.domain.range") != null)
				propertiesWithoutDomainRange = Boolean.parseBoolean(modelingProperties.getProperty("properties.without.domain.range"));

			if(modelingProperties.getProperty("properties.subclass") != null)
				propertiesSubClass = Boolean.parseBoolean(modelingProperties.getProperty("properties.subclass"));

			karmaSourcePrefix = modelingProperties.getProperty("karma.source.prefix");
			karmaServicePrefix = modelingProperties.getProperty("karma.service.prefix");

			if(modelingProperties.getProperty("learner.enabled") != null)
				learnerEnabled = Boolean.parseBoolean(modelingProperties.getProperty("learner.enabled"));

			if(modelingProperties.getProperty("multiple.same.property.per.node") != null)
				multipleSamePropertyPerNode = Boolean.parseBoolean(modelingProperties.getProperty("multiple.same.property.per.node"));

			modelsJsonDir = modelingProperties.getProperty("models.json.dir");
			modelsGraphvizDir = modelingProperties.getProperty("models.graphviz.dir");
			alignmentGraphDir = modelingProperties.getProperty("alignment.graph.dir");

			if(modelingProperties.getProperty("max.queued.mappings") != null)
				maxQueuedMappigs = Integer.parseInt(modelingProperties.getProperty("max.queued.mappings"));

			if(modelingProperties.getProperty("max.candidate.models") != null)
				maxCandidateModels = Integer.parseInt(modelingProperties.getProperty("max.candidate.models"));

			if(modelingProperties.getProperty("scoring.confidence.coefficient") != null)
				scoringConfidenceCoefficient = Double.parseDouble(modelingProperties.getProperty("scoring.confidence.coefficient"));

			if(modelingProperties.getProperty("scoring.coherence.coefficient") != null)
				scoringCoherenceSCoefficient = Double.parseDouble(modelingProperties.getProperty("scoring.coherence.coefficient"));

			if(modelingProperties.getProperty("scoring.size.coefficient") != null)
				scoringSizeCoefficient = Double.parseDouble(modelingProperties.getProperty("scoring.size.coefficient"));

			storeOldHistory = false;
			if(modelingProperties.getProperty("history.store.old") != null)
				storeOldHistory = Boolean.parseBoolean(modelingProperties.getProperty("history.store.old"));

			showModelsWithoutMatching = false;
			if(modelingProperties.getProperty("models.display.nomatching") != null)
				showModelsWithoutMatching = Boolean.parseBoolean(modelingProperties.getProperty("models.display.nomatching"));

		} catch (IOException e) {
			logger.error("Error occured while reading config file ...");
			System.exit(1);
		}
	}

	private static Properties loadParams()
			throws IOException {
		Properties prop = new Properties();

		File file = new File(ServletContextParameterMap.getParameterValue(ContextParameter.USER_CONFIG_DIRECTORY) + "/modeling.properties");
		logger.info("Load modeling.properties: " + file.getAbsolutePath() + ":" + file.exists());
		if(!file.exists()) {
			file.createNewFile();
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			BufferedWriter bw = new BufferedWriter(fw);
			logger.info(defaultModelingProperties);
			bw.write(defaultModelingProperties);
			bw.close();
			logger.info("Written default properties to modeling.properties");
		}

		prop.load(new FileInputStream(file));
		logger.info("Done Loading modeling.properties");


		return prop;
	}

	public static Boolean getThingNode() {

		if (getManualAlignment() == true)
			return false;

		if (thingNode == null)
			load();

		return thingNode;
	}

	public static Boolean getNodeClosure() {

		if (getManualAlignment() == true)
			return false;

		if (nodeClosure == null)
			load();

		return nodeClosure;
	}

	public static Boolean getManualAlignment() {
		if (manualAlignment == null)
			load();
		logger.info("Manual Alignment:" + manualAlignment);
		return manualAlignment;
	}

	public static Boolean getPropertiesDirect() {
		if (propertiesDirect == null)
			load();
		return propertiesDirect;
	}

	public static Boolean getPropertiesIndirect() {
		if (propertiesIndirect == null)
			load();
		return propertiesIndirect;
	}

	public static Boolean getPropertiesWithOnlyDomain() {
		if (propertiesWithOnlyDomain == null)
			load();
		return propertiesWithOnlyDomain;
	}

	public static Boolean getPropertiesWithOnlyRange() {
		if (propertiesWithOnlyRange == null)
			load();
		return propertiesWithOnlyRange;
	}

	public static Boolean getPropertiesWithoutDomainRange() {
		if (propertiesWithoutDomainRange == null)
			load();
		return propertiesWithoutDomainRange;
	}

	public static Boolean getPropertiesSubClass() {
		if (propertiesSubClass == null)
			load();
		return propertiesSubClass;
	}

	public static String getKarmaSourcePrefix() {
		if (karmaSourcePrefix == null)
			load();
		return karmaSourcePrefix.trim();
	}

	public static String getKarmaServicePrefix() {
		if (karmaServicePrefix == null)
			load();
		return karmaServicePrefix.trim();
	}

	public static String getModelsJsonDir() {
		if (modelsJsonDir == null)
			load();
		return modelsJsonDir;
	}

	public static String getModelsGraphvizDir() {
		if (modelsGraphvizDir == null)
			load();
		return modelsGraphvizDir;
	}

	public static String getAlignmentGraphDir() {
		if (alignmentGraphDir == null)
			load();
		return alignmentGraphDir;
	}

	public static Integer getMaxCandidateModels() {
		if (maxCandidateModels == null)
			load();
		return maxCandidateModels;
	}

	public static Integer getMaxQueuedMappigs() {
		if (maxQueuedMappigs == null)
			load();
		return maxQueuedMappigs;
	}

	public static Double getScoringConfidenceCoefficient() {
		if (scoringConfidenceCoefficient == null)
			load();
		return scoringConfidenceCoefficient;
	}

	public static Double getScoringCoherenceSCoefficient() {
		if (scoringCoherenceSCoefficient == null)
			load();
		return scoringCoherenceSCoefficient;
	}

	public static Double getScoringSizeCoefficient() {
		if (scoringSizeCoefficient == null)
			load();
		return scoringSizeCoefficient;
	}

	public static boolean isLearnerEnabled() {
		if (learnerEnabled == null)
			load();
		return learnerEnabled;
	}

	public static boolean isStoreOldHistoryEnabled() {
		if (storeOldHistory == null)
			load();
		return storeOldHistory;
	}

	public static boolean isShowModelsWithoutMatching() {
		if (showModelsWithoutMatching == null)
			load();
		return showModelsWithoutMatching;
	}

	public static void setLearnerEnabled(Boolean learnerEnabled) {
		ModelingConfiguration.learnerEnabled = learnerEnabled;
	}

	public static boolean isMultipleSamePropertyPerNode() {
		if (multipleSamePropertyPerNode == null)
			load();
		return multipleSamePropertyPerNode;
	}

	public static void setManualAlignment(Boolean newManualAlignment)
	{
		manualAlignment = newManualAlignment;
	}

}
