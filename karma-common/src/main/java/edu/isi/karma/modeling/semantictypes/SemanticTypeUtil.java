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
package edu.isi.karma.modeling.semantictypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.Origin;
import edu.isi.karma.rep.metadata.Tag;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

/**
 * This class provides various utility methods that can be used by the semantic
 * typing module.
 * 
 * @author Shubham Gupta
 * 
 */
public class SemanticTypeUtil {

	private static Logger logger = LoggerFactory
			.getLogger(SemanticTypeUtil.class);
	
	private static TrainingFactory trainingFactory = null;

	private static boolean isSemanticTypeTrainingEnabled = true;
	
	/**
	 * Prepares and returns a collection of training examples to be used in
	 * semantic types training. Parameter TRAINING_EXAMPLE_MAX_COUNT specifies
	 * the count of examples. The examples are randomly chosen to get a uniform
	 * distribution of values across the column. Empty values are currently not
	 * included in the set.
	 * 
	 * @param worksheet
	 *            The target worksheet
	 * @param path
	 *            Path to the target column
	 * @return Collection of training examples
	 */
	public static ArrayList<String> getTrainingExamples(Workspace workspace, Worksheet worksheet,
			HNodePath path, SuperSelection sel) {
		if(!getSemanticTypeTrainingEnabled() || path == null)
		{
			return new ArrayList<>();
		}
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		int TRAINING_EXAMPLE_MAX_COUNT = Integer
		.parseInt(contextParameters
				.getParameterValue(ContextParameter.TRAINING_EXAMPLE_MAX_COUNT));
		ArrayList<Node> nodes = new ArrayList<>(Math.max(100, worksheet.getDataTable().getNumRows()));
		worksheet.getDataTable().collectNodes(path, nodes, sel);

		Random r = new Random();
		ArrayList<String> subset = new ArrayList<>(TRAINING_EXAMPLE_MAX_COUNT);
		if (nodes.size() > TRAINING_EXAMPLE_MAX_COUNT *2) {
			HashSet<Integer> seenValues = new HashSet<>(TRAINING_EXAMPLE_MAX_COUNT);
			// SubList method of ArrayList causes ClassCast exception
			int attempts = 0;
			while(subset.size() < TRAINING_EXAMPLE_MAX_COUNT && attempts < Math.min(nodes.size(), TRAINING_EXAMPLE_MAX_COUNT*2))
			{
				int randValue = r.nextInt(nodes.size());
				String nodeValue = nodes.get(randValue).getValue().asString();
				if(seenValues.add(randValue) && (nodeValue != null && !nodeValue.isEmpty()))
				{
					subset.add(nodeValue);
				}
				attempts++;
			}
			
		}
		else
		{
			Collections.shuffle(nodes);
			for(int i = 0; i < nodes.size() && subset.size() < TRAINING_EXAMPLE_MAX_COUNT; i++)
			{
				String nodeValue = nodes.get(i).getValue().asString();
				if (nodeValue != null && !nodeValue.equals(""))
					subset.add(nodeValue);
			}
		}
		return subset;
	}
	
	private class TrainingFactory extends Thread {
		
		private ArrayList<TrainingJob> tasks;
		private Lock lock;
		private Condition taskAvailable;
		
		TrainingFactory() {
			this.tasks = new ArrayList<>();
			this.lock = new ReentrantLock();
			this.taskAvailable = this.lock.newCondition();
			this.start();
		}
		
		void addTrainingJob(TrainingJob trainingJob) {
			this.lock.lock();
			if (this.tasks.add(trainingJob)) {
				this.taskAvailable.signalAll();
			}
			this.lock.unlock();
		}
		
		public void run() {
			while (true) {
				this.lock.lock();
				try {
					while (this.tasks.isEmpty()) {
						this.taskAvailable.await();
					}
					
					TrainingJob trainingJob = this.tasks.remove(0);
					this.lock.unlock();
					Workspace workspace = trainingJob.workspace;
					Worksheet worksheet = trainingJob.worksheet;
					ArrayList<SemanticType> newTypes = trainingJob.newTypes;
					SuperSelection sel = trainingJob.sel;
					if(newTypes.size() > 0) {
					
						HNodePath currentColumnPath = null;
						List<HNodePath> paths = worksheet.getHeaders().getAllPaths();
						String hNodeId = newTypes.get(0).getHNodeId();
						for (HNodePath path : paths) {
							if (path.getLeaf().getId().equals(hNodeId)) {
								currentColumnPath = path;
								break;
							}
						}
	
						ArrayList<String> examples = SemanticTypeUtil.getTrainingExamples(workspace, worksheet, currentColumnPath, sel);
						ISemanticTypeModelHandler modelHandler = workspace.getSemanticTypeModelHandler();
						for(SemanticType newType : newTypes) {
							String label = newType.getModelLabelString();
							modelHandler.addType(label, examples);
						}
					}

				} catch (InterruptedException ie) {
					ie.printStackTrace();
					this.lock.unlock();
				}
			}
		}

	}
	
	private class TrainingJob {
		public Workspace workspace;
		public Worksheet worksheet;
		public ArrayList<SemanticType> newTypes;
		public SuperSelection sel;
		TrainingJob(Workspace workspace, Worksheet worksheet, List<SemanticType> newTypes, SuperSelection sel) {
			this.workspace = workspace;
			this.worksheet = worksheet;
			this.newTypes = new ArrayList<>();
			this.newTypes.addAll(newTypes);
			this.sel = sel;
		}
	}
	
	public void trainOnColumn(Workspace workspace, Worksheet worksheet, List<SemanticType> newTypes, SuperSelection sel) {
		trainingFactory = trainingFactory == null ? new TrainingFactory() : trainingFactory;
		trainingFactory.addTrainingJob(new TrainingJob(workspace, worksheet, newTypes, sel));
	}
	
	public SemanticTypeColumnModel predictColumnSemanticType(Workspace workspace, Worksheet worksheet, String hNodeId, int numSuggestions, SuperSelection sel) {
		HNodePath currentColumnPath = null;
		List<HNodePath> paths = worksheet.getHeaders().getAllPaths();
		for (HNodePath path : paths) {
			if (path.getLeaf().getId().equals(hNodeId)) {
				currentColumnPath = path;
				break;
			}
		}
		if(currentColumnPath != null)
			return predictColumnSemanticType(workspace, worksheet,currentColumnPath, numSuggestions, sel);
		return null;
	}
	
	public SemanticTypeColumnModel predictColumnSemanticType(Workspace workspace, Worksheet worksheet, HNodePath path, int numSuggestions, SuperSelection sel) {
		ArrayList<String> trainingExamples = SemanticTypeUtil.getTrainingExamples(workspace, worksheet,
				path, sel);
		if (trainingExamples.isEmpty())
			return null;

		ISemanticTypeModelHandler modelHandler = workspace.getSemanticTypeModelHandler();
		OntologyManager ontologyManager = workspace.getOntologyManager();
		
		List<SemanticTypeLabel> result = modelHandler.predictType(trainingExamples, numSuggestions);
		if (result == null) {
			logger.debug("Error occured while predicting semantic type.");
			return null;
		}
		if (result.isEmpty()) {
			return null;
		}

		/** Remove the labels that are not in the ontology or are already used as the semantic type **/
		List<SemanticTypeLabel> removeLabels = new ArrayList<>();
		String domainUri, typeUri;
		Label domain, type;
		for (int i=0; i<result.size(); i++) {
			SemanticTypeLabel semLabel = result.get(i);
			String label = semLabel.getLabel();
			/** Check if not in ontology **/
			if (label.contains("|")) {
				
				domainUri = label.split("\\|")[0].trim();
				typeUri = label.split("\\|")[1].trim();
				
				domain = ontologyManager.getUriLabel(domainUri);
				type = ontologyManager.getUriLabel(typeUri);
				
				// Remove from the list if URI not present in the model
				if (domain == null || type == null) {
					removeLabels.add(semLabel);
					continue;
				}
								
			} else {
				domain = ontologyManager.getUriLabel(label);
				// Remove from the list if URI not present in the model
				if (domain == null) {
					removeLabels.add(semLabel);
					continue;
				}
			}
		}
		for (SemanticTypeLabel removeLabel : removeLabels) {
			result.remove(removeLabel);
		}
		if (result.isEmpty()) {
			return null;
		}

		return new SemanticTypeColumnModel(result);
	}

	public List<SemanticType> getSuggestedTypes(OntologyManager ontologyManager, 
			ColumnNode columnNode, SemanticTypeColumnModel columnModel) {
		
		ArrayList<SemanticType> suggestedSemanticTypes = new ArrayList<>();
		if (columnModel == null)
			return suggestedSemanticTypes;
		
		for (Entry<String, Double> entry : columnModel.getScoreMap().entrySet()) {
			
			String key = entry.getKey();
			Double confidence = entry.getValue();
			if (key == null || key.isEmpty()) continue;

			String[] parts = key.split("\\|");
			if (parts == null || parts.length != 2) continue;

			String domainUri = parts[0].trim();
			String propertyUri = parts[1].trim();

			Label domainLabel = ontologyManager.getUriLabel(domainUri);
			if (domainLabel == null) continue;

			Label propertyLabel = ontologyManager.getUriLabel(propertyUri);
			if (propertyLabel == null) continue;

			SemanticType semanticType = new SemanticType(columnNode.getHNodeId(), propertyLabel, domainLabel, null, false, Origin.TfIdfModel, confidence);
			logger.info("\t" + propertyUri + " of " + domainUri + ": " + confidence);
			suggestedSemanticTypes.add(semanticType);
		}
		Collections.sort(suggestedSemanticTypes, Collections.reverseOrder());
		return suggestedSemanticTypes;
	}
	
	public ArrayList<SemanticType> getColumnSemanticSuggestions(Workspace workspace, Worksheet worksheet, ColumnNode columnNode, int numSuggestions, SuperSelection sel) {
		ArrayList<SemanticType> suggestedSemanticTypes = new ArrayList<>();
		logger.info("Column Semantic Suggestions for:" + columnNode.getColumnName());
		if(workspace != null && worksheet != null) {
			OntologyManager ontologyManager = workspace.getOntologyManager();
			String hNodeId = columnNode.getHNodeId();
			SemanticTypeColumnModel columnModel = predictColumnSemanticType(workspace, worksheet, hNodeId, numSuggestions, sel);
			
			if (columnModel != null) {
				for (Entry<String, Double> entry : columnModel.getScoreMap().entrySet()) {
	
					String key = entry.getKey();
					Double confidence = entry.getValue();
					if (key == null || key.isEmpty()) continue;
	
					String[] parts = key.split("\\|");
					if (parts == null || parts.length != 2) continue;
	
					String domainUri = parts[0].trim();
					String propertyUri = parts[1].trim();
	
					Label domainLabel = ontologyManager.getUriLabel(domainUri);
					if (domainLabel == null) continue;
	
					Label propertyLabel = ontologyManager.getUriLabel(propertyUri);
					if (propertyLabel == null) continue;
	
					SemanticType semanticType = new SemanticType(hNodeId, propertyLabel, domainLabel, null, false, Origin.TfIdfModel, confidence);
					logger.info("\t" + propertyUri + " of " + domainUri + ": " + confidence);
					suggestedSemanticTypes.add(semanticType);
				}
			}
		}
		Collections.sort(suggestedSemanticTypes, Collections.reverseOrder());
		return suggestedSemanticTypes;
	}
	
	

	/**
	 * Identifies the outlier nodes (table cells) for a given column.
	 * 
	 * @param worksheet
	 *            Target worksheet
	 * @param predictedType
	 *            Type which was user-assigned or predicted by the CRF model for
	 *            the given column. If the type for a given node is different
	 *            from the predictedType, it is tagged as outlier and it's id is
	 *            stored in the outlier tag object
	 * @param path
	 *            Path to the given column
	 * @param outlierTag
	 *            The outlier tag object which stores all the outlier node ids.
	 * @param columnFeatures
	 *            Features such as column name, table name that are required by
	 *            the CRF Model to predict the semantic type for a node (table
	 *            cell)
	 * @param crfModelHandler
	 */
	public static void identifyOutliers(Worksheet worksheet, String predictedType, HNodePath path, Tag outlierTag,
			ISemanticTypeModelHandler modelHandler, SuperSelection sel) {
		Collection<Node> nodes = new ArrayList<>();
		worksheet.getDataTable().collectNodes(path, nodes, sel);

		// Identify the top semantic type for each node
		// It it does not matches the predicted type, it is a outlier.
		Set<String> allNodeIds = new HashSet<>();
		Set<String> outlierNodeIds = new HashSet<>();

		int outlierCounter = 0;
		for (Node node : nodes) {
			allNodeIds.add(node.getId());

			// Compute the semantic type for the node value
			List<String> examples = new ArrayList<>();

			String nodeVal = node.getValue().asString();
			if (nodeVal != null && !nodeVal.equals("")) {
				examples.add(nodeVal);
				List<SemanticTypeLabel> result = modelHandler.predictType(examples, 1);
			
				if (result == null) {
					logger.error("Error while predicting type for " + nodeVal);
					continue;
				}
				// Check here if it is an outlier
//				System.out.println("Example: " + examples.get(0) + " Label: " + predictedLabels + " Score: " + confidenceScores);
				String predictedLabel = result.get(0).getLabel();
				if (!predictedLabel.equalsIgnoreCase(predictedType)) {
					outlierCounter++;
					outlierNodeIds.add(node.getId());
				}
			}
		}
		System.out.println("Total outliers: " + outlierCounter);
		// Remove the existing ones
		outlierTag.removeNodeIds(allNodeIds);
		// Add the new ones
		outlierTag.addNodeIds(outlierNodeIds);
	}

	/**
	 * Removes the namespace from a given URI. It makes a assumption that the
	 * namespace is until the last # or last '/' in the URI string, so it should
	 * be used only for interface purposes and not for reasoning or logic. The
	 * right way would be store the namespaces map in memory and use that to
	 * remove the namespace from a URI.
	 * 
	 * @param uri
	 *            Input URI
	 * @return URI string with namespace removed
	 */
	public static String removeNamespace(String uri) {
		if (uri.contains("#"))
			uri = uri.split("#")[1];
		else if (uri.contains("/"))
			uri = uri.substring(uri.lastIndexOf("/") + 1);
		return uri;
	}


//	public static void computeSemanticTypesForAutoModel(Worksheet worksheet,
//			ISemanticTypeModelHandler crfModelHandler, OntologyManager ontMgr) {
//		
//		String autoModelURI = ServletContextParameterMap
//				.getParameterValue(ContextParameter.AUTO_MODEL_URI);
//		String topClassURI = autoModelURI + worksheet.getTitle();
//		
//		List<HNodePath> paths = worksheet.getHeaders().getAllPaths();
//		for (HNodePath path : paths) {
//
//			// Prepare the column name feature
//			String columnName = path.getLeaf().getColumnName();
//			
//			
//			String label = topClassURI+"#"+worksheet.getTitle()+"|"+topClassURI+"#"+columnName;
//			ArrayList<SemanticTypeLabel> labels = new ArrayList<>();
//			labels.add(new SemanticTypeLabel(label, 1.0f));
//			
//			SemanticTypeColumnModel columnModel = new SemanticTypeColumnModel(labels);
//			worksheet.getSemanticTypeModel().addColumnModel(path.getLeaf().getId(), columnModel);
//		}
//	}
	
	public  static void setSemanticTypeTrainingStatus(boolean status)
	{
		isSemanticTypeTrainingEnabled = status;
	}
	
	public static boolean getSemanticTypeTrainingEnabled()
	{
		return isSemanticTypeTrainingEnabled;
	}
}
