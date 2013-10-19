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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler.ColumnFeature;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticTypes;
import edu.isi.karma.rep.metadata.Tag;
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

	private final static int TRAINING_EXAMPLE_MAX_COUNT = Integer
			.parseInt(ServletContextParameterMap
					.getParameterValue(ContextParameter.TRAINING_EXAMPLE_MAX_COUNT));

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
	public static ArrayList<String> getTrainingExamples(Worksheet worksheet,
			HNodePath path) {
		Collection<Node> nodes = new ArrayList<Node>();
		worksheet.getDataTable().collectNodes(path, nodes);

		ArrayList<String> nodeValues = new ArrayList<String>();
		for (Node n : nodes) {
			String nodeValue = n.getValue().asString();
			if (nodeValue != null && !nodeValue.equals(""))
				nodeValues.add(nodeValue);
		}

		// Shuffling the values so that we get randomly chosen values to train
		Collections.shuffle(nodeValues);

		if (nodeValues.size() > TRAINING_EXAMPLE_MAX_COUNT) {
			ArrayList<String> subset = new ArrayList<String>();
			// SubList method of ArrayList causes ClassCast exception
			for (int i = 0; i < TRAINING_EXAMPLE_MAX_COUNT; i++)
				subset.add(nodeValues.get(i));
			return subset;
		}
		return nodeValues;
	}

	/**
	 * This method predicts semantic types for all the columns in a worksheet
	 * using CRF modeling technique developed by Aman Goel. It creates a
	 * SemanticType object for each column and puts it inside the SemanticTypes
	 * object for that worksheet. User-assigned semantic types are not replaced.
	 * It also identifies nodes (table cells) that are outliers and are stored
	 * in the outlierTag object.
	 * 
	 * @param worksheet
	 *            The target worksheet
	 * @param outlierTag
	 *            Tag object that stores outlier nodes
	 * @param crfModelHandler
	 *            The CRF Model Handler to use
	 * @return Returns a boolean value that shows if a semantic type object was
	 *         replaced or added for the worksheet. If nothing changed, false is
	 *         returned.
	 */
	public static boolean populateSemanticTypesUsingCRF(Worksheet worksheet,
			Tag outlierTag, CRFModelHandler crfModelHandler,
			OntologyManager ontMgr) {
		boolean semanticTypesChangedOrAdded = false;

		SemanticTypes types = worksheet.getSemanticTypes();

		List<HNodePath> paths = worksheet.getHeaders().getAllPaths();

		for (HNodePath path : paths) {
			boolean semanticTypeAdded = false;
			ArrayList<String> trainingExamples = getTrainingExamples(worksheet,
					path);
			if (trainingExamples.size() == 0)
				continue;

			Map<ColumnFeature, Collection<String>> columnFeatures = new HashMap<ColumnFeature, Collection<String>>();

			// Prepare the column name feature
			String columnName = path.getLeaf().getColumnName();
			Collection<String> columnNameList = new ArrayList<String>();
			columnNameList.add(columnName);
			columnFeatures.put(ColumnFeature.ColumnHeaderName, columnNameList);
			
			// // Prepare the table name feature
			// String tableName = worksheetName;
			// Collection<String> tableNameList = new ArrayList<String>();
			// tableNameList.add(tableName);
			// columnFeatures.put(ColumnFeature.TableName, tableNameList);

			// Stores the probability scores
			ArrayList<Double> scores = new ArrayList<Double>();
			// Stores the predicted labels
			ArrayList<String> labels = new ArrayList<String>();
			boolean predictResult = crfModelHandler.predictLabelForExamples(
					trainingExamples, 4, labels, scores, null, columnFeatures);
			if (!predictResult) {
				logger.debug("Error occured while predicting semantic type.");
				continue;
			}
			if (labels.size() == 0) {
				continue;
			}

			logger.debug("Examples: " + trainingExamples + " Type: " + labels
					+ " ProbL " + scores);

			// Create and add the semantic type to the semantic types set of the
			// worksheet
			String topLabel = labels.get(0);
			String domain = "";
			String type = topLabel;
			// Check if it contains domain information
			if (topLabel.contains("|")) {
				domain = topLabel.split("\\|")[0];
				type = topLabel.split("\\|")[1];
			}

			Label typeURI = ontMgr.getUriLabel(type);
			if(typeURI == null) {
				logger.error("Could not find the resource " + type + " in ontology model!");
				continue;
			}
			Label domainURI = null;
			if (!domain.equals(""))
				domainURI = ontMgr.getUriLabel(domain);
			SemanticType semtype = new SemanticType(path.getLeaf().getId(),typeURI, domainURI, SemanticType.Origin.CRFModel,scores.get(0), false);

			// Check if the user already provided a semantic type manually
			SemanticType existingType = types.getSemanticTypeForHNodeId(path
					.getLeaf().getId());
			if (existingType == null) {
				if (semtype.getConfidenceLevel() != SemanticType.ConfidenceLevel.Low) {
					worksheet.getSemanticTypes().addType(semtype);
					semanticTypeAdded = true;
					semanticTypesChangedOrAdded = true;
				}
			} else {
				if (existingType.getOrigin() != SemanticType.Origin.User) {
					worksheet.getSemanticTypes().addType(semtype);
					semanticTypeAdded = true;

					// Check if the new semantic type is different from the
					// older one
					if (!existingType.getType().equals(semtype.getType())
							|| !existingType.getDomain().equals(
									semtype.getDomain()))
						semanticTypesChangedOrAdded = true;
				}
			}

			// If the semantic type was added, then identify the outliers and
			// add the CRF model information for that column
			if (semanticTypeAdded) {
				// Identify the outliers
				identifyOutliers(worksheet, labels.get(0), path, outlierTag,
						columnFeatures, crfModelHandler);
				logger.debug("Outliers:" + outlierTag.getNodeIdList());

				// Add the scores information to the Full CRF Model of the
				// worksheet
				CRFColumnModel columnModel = new CRFColumnModel(labels, scores);
				worksheet.getCrfModel().addColumnModel(path.getLeaf().getId(),
						columnModel);
			}
		}
		return semanticTypesChangedOrAdded;
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
			Map<ColumnFeature, Collection<String>> columnFeatures, CRFModelHandler crfModelHandler) {
		Collection<Node> nodes = new ArrayList<Node>();
		worksheet.getDataTable().collectNodes(path, nodes);

		// Identify the top semantic type for each node
		// It it does not matches the predicted type, it is a outlier.
		Set<String> allNodeIds = new HashSet<String>();
		Set<String> outlierNodeIds = new HashSet<String>();

		int outlierCounter = 0;
		for (Node node : nodes) {
			allNodeIds.add(node.getId());

			// Compute the semantic type for the node value
			List<String> examples = new ArrayList<String>();
			List<String> predictedLabels = new ArrayList<String>();
			List<Double> confidenceScores = new ArrayList<Double>();

			String nodeVal = node.getValue().asString();
			if (nodeVal != null && !nodeVal.equals("")) {
				examples.add(nodeVal);
				boolean result = crfModelHandler.predictLabelForExamples(
						examples, 1, predictedLabels, confidenceScores, null,
						columnFeatures);
				 
				if (!result) {
					logger.error("Error while predicting type for " + nodeVal);
					continue;
				}
				// Check here if it is an outlier
//				System.out.println("Example: " + examples.get(0) + " Label: " + predictedLabels + " Score: " + confidenceScores);
				if (!predictedLabels.get(0).equalsIgnoreCase(predictedType)) {
					logger.info(nodeVal + ": " + predictedLabels + " Prob: " + confidenceScores);
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

	public static void computeSemanticTypesSuggestion(Worksheet worksheet,
			CRFModelHandler crfModelHandler, OntologyManager ontMgr, Alignment alignment) {
		
		List<HNodePath> paths = worksheet.getHeaders().getAllPaths();
		for (HNodePath path : paths) {
			ArrayList<String> trainingExamples = getTrainingExamples(worksheet, path);

			Map<ColumnFeature, Collection<String>> columnFeatures = new HashMap<ColumnFeature, Collection<String>>();

			// Prepare the column name feature
			String columnName = path.getLeaf().getColumnName();
			Collection<String> columnNameList = new ArrayList<String>();
			columnNameList.add(columnName);
			columnFeatures.put(ColumnFeature.ColumnHeaderName, columnNameList);
			
			// Stores the probability scores
			ArrayList<Double> scores = new ArrayList<Double>();
			// Stores the predicted labels
			ArrayList<String> labels = new ArrayList<String>();
			boolean predictResult = crfModelHandler.predictLabelForExamples(trainingExamples, 4, labels, scores, null, columnFeatures);
			if (!predictResult) {
				logger.debug("Error occured while predicting semantic type.");
				continue;
			}
			if (labels.size() == 0) {
				continue;
			}
			
			/** Remove the labels that are not in the ontology or are already used as the semantic type **/
			List<String> removeLabels = new ArrayList<String>();
			for (int i=0; i<labels.size(); i++) {
				String label = labels.get(i);
				SemanticType existingSemanticType = worksheet.getSemanticTypes().getSemanticTypeForHNodeId(path.getLeaf().getId());
				/** Check if not in ontology **/
				if (label.contains("|")) {
					Label domainUri = ontMgr.getUriLabel(label.split("\\|")[0]);
					Label typeUri = ontMgr.getUriLabel(label.split("\\|")[1]);
					
					// Remove from the list if URI not present in the model
					if (domainUri == null || typeUri == null) {
						removeLabels.add(label);
						continue;
					}
					// Check if it is being used as the semantic type already
					if (existingSemanticType != null && 
							existsInSemanticTypesCollection(typeUri, domainUri, existingSemanticType)) {
							removeLabels.add(label);
					}
					
				} else {
					Label typeUri = ontMgr.getUriLabel(label);
					// Remove from the list if URI not present in the model
					if (typeUri == null) {
						removeLabels.add(label);
						continue;
					}
					// Check if it is being used as the semantic type already
					if (existingSemanticType != null && 
							existsInSemanticTypesCollection(typeUri, null, existingSemanticType)) {
						removeLabels.add(label);
					}
				}
			}
			for (String removeLabel : removeLabels) {
				int idx = labels.indexOf(removeLabel);
//				System.out.println("Removing " + removeLabel);
//				logger.info("Removing " + removeLabel);
				labels.remove(removeLabel);
				scores.remove(idx);
			}
			if (labels.size() == 0) {
				continue;
			}

			CRFColumnModel columnModel = new CRFColumnModel(labels, scores);
			worksheet.getCrfModel().addColumnModel(path.getLeaf().getId(), columnModel);
		}
	}
	
	private static boolean existsInSemanticTypesCollection(Label typeLabel, Label domainLabel, SemanticType existingSemanticType) {
		if (typeLabel.getUri().equals(existingSemanticType.getType().getUri())) {
			if (domainLabel == null) {
				if(existingSemanticType.getDomain() == null)
					return true;
				return false;
			}
			if (existingSemanticType.getDomain() == null)
				return false;
			if (existingSemanticType.getDomain().getUri().equals(domainLabel.getUri()))
				return true;
			return false;
		}
		return false;
	}

	public static void computeSemanticTypesForAutoModel(Worksheet worksheet,
			CRFModelHandler crfModelHandler, OntologyManager ontMgr) {
		
		String autoModelURI = ServletContextParameterMap
				.getParameterValue(ContextParameter.AUTO_MODEL_URI);
		String topClassURI = autoModelURI + worksheet.getTitle();
		
		List<HNodePath> paths = worksheet.getHeaders().getAllPaths();
		for (HNodePath path : paths) {

			Map<ColumnFeature, Collection<String>> columnFeatures = new HashMap<ColumnFeature, Collection<String>>();

			// Prepare the column name feature
			String columnName = path.getLeaf().getColumnName();
			Collection<String> columnNameList = new ArrayList<String>();
			columnNameList.add(columnName);
			columnFeatures.put(ColumnFeature.ColumnHeaderName, columnNameList);
			
			// Stores the probability scores
			ArrayList<Double> scores = new ArrayList<Double>();
			// Stores the predicted labels
			ArrayList<String> labels = new ArrayList<String>();
			
			String label = topClassURI+"#"+worksheet.getTitle()+"|"+topClassURI+"#"+columnName;
			labels.add(label);
			scores.add(1.0);
			
			CRFColumnModel columnModel = new CRFColumnModel(labels, scores);
			worksheet.getCrfModel().addColumnModel(path.getLeaf().getId(), columnModel);
		}
	}
}
