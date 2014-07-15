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

import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticTypes;
import edu.isi.karma.rep.metadata.Tag;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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

	private static boolean isSemanticTypeTrainingEnabled = true;
	
	private final static int TRAINING_EXAMPLE_MAX_COUNT ;
	static {
		int temp = 200;
		try
		{
			temp = Integer
				.parseInt(ServletContextParameterMap
						.getParameterValue(ContextParameter.TRAINING_EXAMPLE_MAX_COUNT));
		}
		catch (Exception e)
		{
		}
		
		TRAINING_EXAMPLE_MAX_COUNT = temp;
	}
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
		if(!getSemanticTypeTrainingEnabled())
		{
			return new ArrayList<String>();
		}
		ArrayList<Node> nodes = new ArrayList<Node>(Math.max(100, worksheet.getDataTable().getNumRows()));
		worksheet.getDataTable().collectNodes(path, nodes);

		Random r = new Random();
		ArrayList<String> subset = new ArrayList<String>(TRAINING_EXAMPLE_MAX_COUNT);
		if (nodes.size() > TRAINING_EXAMPLE_MAX_COUNT *2) {
			HashSet<Integer> seenValues = new HashSet<Integer>(TRAINING_EXAMPLE_MAX_COUNT);
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

	/**
	 * This method predicts semantic types for all the columns in a worksheet
	 * using  modeling technique . It creates a
	 * SemanticType object for each column and puts it inside the SemanticTypes
	 * object for that worksheet. User-assigned semantic types are not replaced.
	 * It also identifies nodes (table cells) that are outliers and are stored
	 * in the outlierTag object.
	 * 
	 * @param worksheet
	 *            The target worksheet
	 * @param outlierTag
	 *            Tag object that stores outlier nodes
	 * @param modelHandler
	 *            The Semnatic Typing Model Handler to use
	 * @return Returns a boolean value that shows if a semantic type object was
	 *         replaced or added for the worksheet. If nothing changed, false is
	 *         returned.
	 */
	public static boolean populateSemanticTypesUsingCRF(Worksheet worksheet,
			Tag outlierTag, ISemanticTypeModelHandler modelHandler,
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

			List<SemanticTypeLabel> result = modelHandler.predictType(trainingExamples, 4);
			if (result == null) {
				logger.debug("Error occured while predicting semantic type.");
				continue;
			}
			if (result.size() == 0) {
				continue;
			}


			// Create and add the semantic type to the semantic types set of the
			// worksheet
			SemanticTypeLabel topResult = result.get(0);
			String topLabel = topResult.getLabel();
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
			SemanticType semtype = new SemanticType(path.getLeaf().getId(),typeURI, domainURI, SemanticType.Origin.TfIdfModel, (double)topResult.getScore(), false);

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
				identifyOutliers(worksheet, topLabel, path, outlierTag,
					modelHandler);
				logger.debug("Outliers:" + outlierTag.getNodeIdList());

				// Add the scores information to the Full CRF Model of the
				// worksheet
				SemanticTypeColumnModel columnModel = new SemanticTypeColumnModel(result);
				worksheet.getSemanticTypeModel().addColumnModel(path.getLeaf().getId(),
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
			ISemanticTypeModelHandler modelHandler) {
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

	public static void computeSemanticTypesSuggestion(Worksheet worksheet,
			ISemanticTypeModelHandler modelHandler, OntologyManager ontMgr) {
		if(!getSemanticTypeTrainingEnabled())
		{
			return;
		}
		List<HNodePath> paths = worksheet.getHeaders().getAllPaths();
		for (HNodePath path : paths) {
			computeSemanticTypesSuggestion(worksheet, modelHandler, ontMgr, path);
		}
	}
	
	public static void computeSemanticTypesSuggestion(Worksheet worksheet,
			ISemanticTypeModelHandler modelHandler, OntologyManager ontMgr, HNodePath path)
	{
		
		if(!getSemanticTypeTrainingEnabled())
		{
			return;
		}
		ArrayList<String> trainingExamples = getTrainingExamples(worksheet, path);

		List<SemanticTypeLabel> result = modelHandler.predictType(trainingExamples, 4);
		
		if (result == null) {
			logger.debug("Error occured while predicting semantic type.");
			return;
		}
		if (result.size() == 0) {
			return;
		}
		
		/** Remove the labels that are not in the ontology or are already used as the semantic type **/
		List<SemanticTypeLabel> removeLabels = new ArrayList<SemanticTypeLabel>();
		String domainUri, typeUri;
		Label domain, type;		
		for (int i=0; i<result.size(); i++) {
			SemanticTypeLabel resultLabel = result.get(i);
			String label = resultLabel.getLabel();
			SemanticType existingSemanticType = worksheet.getSemanticTypes().getSemanticTypeForHNodeId(path.getLeaf().getId());
			/** Check if not in ontology **/
			if (label.contains("|")) {

				domainUri = label.split("\\|")[0].trim();
				typeUri = label.split("\\|")[1].trim();
				
				domain = ontMgr.getUriLabel(domainUri);
				type = ontMgr.getUriLabel(typeUri);
				
				// Remove from the list if URI not present in the model
				if (domain == null || type == null) {
					removeLabels.add(resultLabel);
					continue;
				}
				// Check if it is being used as the semantic type already
				if (existingSemanticType != null && 
						existsInSemanticTypesCollection(type, domain, existingSemanticType)) {
						removeLabels.add(resultLabel);
				}
				
			} else {
				domain = ontMgr.getUriLabel(label);
				// Remove from the list if URI not present in the model
				if (domain == null) {
					removeLabels.add(resultLabel);
					continue;
				}
				// Check if it is being used as the semantic type already
				if (existingSemanticType != null && 
						existsInSemanticTypesCollection(domain, null, existingSemanticType)) {
					removeLabels.add(resultLabel);
				}
			}
		}
		for (SemanticTypeLabel removeLabel : removeLabels) {
			result.remove(removeLabel);
		}
		if (result.size() == 0) {
			return;
		}

		SemanticTypeColumnModel columnModel = new SemanticTypeColumnModel(result);
		worksheet.getSemanticTypeModel().addColumnModel(path.getLeaf().getId(), columnModel);
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
			ISemanticTypeModelHandler crfModelHandler, OntologyManager ontMgr) {
		
		String autoModelURI = ServletContextParameterMap
				.getParameterValue(ContextParameter.AUTO_MODEL_URI);
		String topClassURI = autoModelURI + worksheet.getTitle();
		
		List<HNodePath> paths = worksheet.getHeaders().getAllPaths();
		for (HNodePath path : paths) {

			// Prepare the column name feature
			String columnName = path.getLeaf().getColumnName();
			
			
			String label = topClassURI+"#"+worksheet.getTitle()+"|"+topClassURI+"#"+columnName;
			ArrayList<SemanticTypeLabel> labels = new ArrayList<>();
			labels.add(new SemanticTypeLabel(label, 1.0f));
			
			SemanticTypeColumnModel columnModel = new SemanticTypeColumnModel(labels);
			worksheet.getSemanticTypeModel().addColumnModel(path.getLeaf().getId(), columnModel);
		}
	}
	
	public  static void setSemanticTypeTrainingStatus(boolean status)
	{
		isSemanticTypeTrainingEnabled = status;
	}
	
	public static boolean getSemanticTypeTrainingEnabled()
	{
		return isSemanticTypeTrainingEnabled;
	}
}
