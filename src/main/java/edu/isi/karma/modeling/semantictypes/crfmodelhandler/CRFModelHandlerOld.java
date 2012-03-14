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

package edu.isi.karma.modeling.semantictypes.crfmodelhandler ;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.semantictypes.mycrf.crfmodel.CRFModelFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.fieldonly.LblFtrPair;
import edu.isi.karma.modeling.semantictypes.mycrf.globaldata.GlobalDataFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.graph.GraphFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.graph.GraphInterface;
import edu.isi.karma.modeling.semantictypes.mycrf.map.MAPFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.math.Matrix;
import edu.isi.karma.modeling.semantictypes.mycrf.optimization.OptimizeFieldOnly;
import edu.isi.karma.modeling.semantictypes.myutils.ListOps;
import edu.isi.karma.modeling.semantictypes.myutils.Prnt;
import edu.isi.karma.modeling.semantictypes.sl.Lexer;
import edu.isi.karma.modeling.semantictypes.sl.Part;
import edu.isi.karma.modeling.semantictypes.sl.RegexFeatureExtractor;


public class CRFModelHandlerOld {

	public static enum ColumnFeature {
		ColumnHeaderName ,
		TableName
	} ;
	static String file = null ;
	static HashMap<String, ArrayList<String>> labelToExamplesMap = null ;
	static GlobalDataFieldOnly globalData = null ;
	static Logger logger = LoggerFactory.getLogger(CRFModelHandler.class.getSimpleName()) ;
	
	/**
	 * Making the empty constructor private to prevent instantiation of this class.
	 * This class should only be used to access its static methods.
	 */
	private CRFModelHandlerOld() {
		
	}

	
	/**
	 * @param label True label
	 * @param examples list of examples
	 * @param columnFeatures List of column features
	 * @return True if success, else False
	 */
	public static boolean addOrUpdateLabel(String label, List<String> examples, Map<ColumnFeature, Collection<String>> columnFeatures) {
		if (file == null) {
			logger.debug("CRF Model is not ready, either because it was never read or an error happened while reading it previously. Please try reading the model file again.");
			return false ;
		}
		ArrayList<String> cleanedExamples ;
		int labelIndex ;
		ArrayList<String> allFeatures;
		HashSet<String> selectedFeatures;
		ArrayList<LblFtrPair> ffsOfLabel, otherFFs;
		ArrayList<Double> weightsOfFFsOfLabel, weightsOfOtherFFs;
		OptimizeFieldOnly optimizationObject;
		boolean savingSuccessful ;
		// running basic sanity checks in the input arguments
		label = label.trim() ;
		if (label.length() == 0 || examples.size() == 0) {
			logger.debug("@label argument cannot be an empty string and the @examples list cannot be empty.") ;
			return false ;
		}
		cleanedExamples = new ArrayList<String>() ;
		cleanedExamplesList(examples, cleanedExamples);
		examples = cleanedExamples ;
		// making sure that the condition where the examples list is not empty but contains junk only is not accepted
		if (examples.size() == 0) {
			logger.debug("@examples list contains forbidden characters only. The allowed characters are " + allowedCharacters()) ;
			return false ;
		}
		// if label does not already exist in the model, add new label. Also, add an entry in the map for the new label.
		labelIndex = globalData.labels.indexOf(label) ;
		if (labelIndex == -1) {
			globalData.labels.add(label) ;
			labelIndex = globalData.labels.indexOf(label) ;
			labelToExamplesMap.put(label, new ArrayList<String>()) ;
		}
		allFeatures = new ArrayList<String>();
		// get features of existing examples
		for(String example: labelToExamplesMap.get(label)) {
			allFeatures.addAll(featureSet(example)) ;
		}
		// add examples to the existing list of examples
		// create new Graph for each example and add it to the list of trainingGraphs in globalData
		// save a list of new features so that we can create new feature functions for the label
		for(String example : examples) {
			HashSet<String> exampleFtrs;
			GraphFieldOnly newGraph ;
			labelToExamplesMap.get(label).add(example) ;
			exampleFtrs = featureSet(example, columnFeatures) ;
			newGraph = new GraphFieldOnly(example, label, new ArrayList<String>(exampleFtrs), globalData) ;
			globalData.trainingGraphs.add(newGraph) ;
			allFeatures.addAll(exampleFtrs) ;
		}
		// if the total number of features is > 100, then randomly select 100 from them.
		selectedFeatures = new HashSet<String>(allFeatures);
		if (selectedFeatures.size() > 100) {
			ArrayList<String> tmpAllFeatures;
			Random random ;
			tmpAllFeatures = new ArrayList<String>(allFeatures);
			random = new Random();
			selectedFeatures = new HashSet<String>();
			for(int i=0;i<100;i++) {
				String ftr;
				ftr = tmpAllFeatures.get(random.nextInt(tmpAllFeatures.size()));
				selectedFeatures.add(ftr);
				while (tmpAllFeatures.contains(ftr)) {
					tmpAllFeatures.remove(ftr);
				}
			}
		}
		// separate the label ffs and weights from other ffs and weights
		ffsOfLabel = new ArrayList<LblFtrPair>() ;
		otherFFs = new ArrayList<LblFtrPair>() ;
		weightsOfFFsOfLabel = new ArrayList<Double>() ;
		weightsOfOtherFFs = new ArrayList<Double>() ;
		for(int ffIndex=0;ffIndex<globalData.crfModel.ffs.size();ffIndex++) {
			LblFtrPair ff;
			ff = globalData.crfModel.ffs.get(ffIndex);
			if (ff.labelIndex == labelIndex) {
				ffsOfLabel.add(ff) ;
				weightsOfFFsOfLabel.add(globalData.crfModel.weights[ffIndex]);
			}
			else {
				otherFFs.add(ff) ;
				weightsOfOtherFFs.add(globalData.crfModel.weights[ffIndex]);
			}
		}
		// from the existing ffs of this label, if any of them have a selected feature, then add it to the other ffs and its learned weight
		for(int ffIndex=0;ffIndex<ffsOfLabel.size();ffIndex++) {
			LblFtrPair ff;
			ff = ffsOfLabel.get(ffIndex);
			if (selectedFeatures.contains(ff.feature)) {
				otherFFs.add(ff);
				weightsOfOtherFFs.add(weightsOfFFsOfLabel.get(ffIndex)) ;
				selectedFeatures.remove(ff.feature);
			}
		}
		// create new ffs for all other selected features and add zero as their weight
		for(String ftr : selectedFeatures) {
			otherFFs.add(new LblFtrPair(labelIndex, ftr));
			weightsOfOtherFFs.add(0.0);
		}
		// reset the ffs and the weights array
		globalData.crfModel.ffs = otherFFs ;
		globalData.crfModel.weights = new double[otherFFs.size()];
		for(int i=0;i<otherFFs.size();i++) {
			globalData.crfModel.weights[i] = weightsOfOtherFFs.get(i) ;
		}
		// optimize the model to adjust to the new label/examples/ffs
		optimizationObject = new OptimizeFieldOnly(globalData.crfModel, globalData) ;
		optimizationObject.optimize(5) ;
		// save the model to file with the new weights
		savingSuccessful = saveModel() ;
		if (!savingSuccessful) {
			CRFModelHandlerOld.file = null ;
		}
		return savingSuccessful ;
	}

	
	/**
	 * @param label - the label for which examples are being requested
	 * @param examples - the list argument that will contain all the examples of the supplied label in the model.
	 * @return
	 */
	public static boolean getExamplesForLabel(String label, ArrayList<String> examples) {
		if (CRFModelHandlerOld.file == null) {
			logger.debug("CRF Model is not ready, either because it was never read or an error happened while reading it previously. Please try reading the model file again.");
			return false ;
		}
		if (label == null || label.trim().length() == 0 || examples == null) {
			Prnt.prn("CRFModelHandlerOld.getExamplesForLabel: Either the label is null, or it is an empty string or examples is null") ;
			return false ;
		}
		label = label.trim();
		if (!globalData.labels.contains(label)) {
			Prnt.prn("CRFModelHandler.getExamplesForLabel: Label " + label + " does not exist in the model.") ;
			return false ;
		}
		examples.clear() ;
		examples.addAll(labelToExamplesMap.get(label));
		return true ;
	}

	
	/**
	 * @param labels, the ordered list of labels is returned in this argument
	 * @return
	 */
	public static boolean getLabels(List<String> labels) {
		if (CRFModelHandlerOld.file == null) {
			logger.debug("CRF Model is not ready, either because it was never read or an error happened while reading it previously. Please try reading the model file again.");
			return false ;
		}
		if (labels == null) {
			Prnt.prn("Invalid argument @labels. It is null.") ;
			return false ;
		}
		labels.clear() ;
		labels.addAll(globalData.labels);
		return true ;
	}


	public static boolean getWeightedFeatureFunctionSums(String example, Map<ColumnFeature, Collection<String>> columnFeatures, List<Double> sums) {
		GraphFieldOnly exampleGraph ;
		double[] ffSums;
		MAPFieldOnly mapPredictor;
		exampleGraph = new GraphFieldOnly(example, null, new ArrayList<String>(featureSet(example, columnFeatures)), globalData) ;
		mapPredictor = new MAPFieldOnly(globalData);
		ffSums = mapPredictor.weightedFeatureFunctionSums(exampleGraph);
		sums.clear();
		for(double sum : ffSums) {
			sums.add(sum);
		}
		return true;
	}
	
	
	
	/**
	 * @param examples - list of examples of an unknown type
	 * @param numPredictions - required number of predictions in descending order
	 * @param predictedLabels - the argument in which the ordered list of labels is returned. the size of this list could be smaller than numPredictions
	 * 							if there aren't that many labels in the model already
	 * @param confidenceScores - the probability of the examples belonging to the labels returned.
	 * @param exampleProbabilities - the size() == examples.size(). It contains, for each example, in the same order, a double array that contains the probability 
	 * 									of belonging to the labels returned in predictedLabels.	 
	 * @param columnFeatures - this Map supplies ColumnFeatures such as ColumnName, etc.
	 * @return
	 */
	public static boolean predictLabelForExamples(
			List<String> examples,
			int numPredictions,
			List<String> predictedLabels,
			List<Double> confidenceScores,
			List<double[]> exampleProbabilities,
			Map<ColumnFeature, Collection<String>> columnFeatures
			) {
		ArrayList<ArrayList<Double>> exampleProbabilitiesFullList ;
		MAPFieldOnly MAPPredictor ;
		double[] columnProbabilities ;
		ArrayList<String> labels ;
		ArrayList<Double> columnProbabilitiesList ;
		if (CRFModelHandlerOld.file == null) {
			logger.debug("CRF Model is not ready, either because it was never read or an error happened while reading it previously. Please try reading the model file again.");
			return false ;
		}
		// Sanity checks for arguments
		if (examples == null || examples.size() == 0 || numPredictions <= 0 || predictedLabels == null || confidenceScores == null) {
			logger.debug("Invalid arguments. Possible problems: examples list size is zero, numPredictions is non-positive, predictedLabels or confidenceScores list is null.") ;
			return false ;
		}
		// Making sure that there exists a model.
		if(globalData.labels.size() == 0) {
			logger.debug("The model does have not any semantic types. Please add some labels with their examples before attempting to predict using this model.") ;
			return false ;
		}
		exampleProbabilitiesFullList = new ArrayList<ArrayList<Double>>() ;
		MAPPredictor = new MAPFieldOnly(globalData) ;
		columnProbabilities = new double[globalData.labels.size()] ;
		// for each example, get the probability of each label.
		// add the probabilities to an accumulator probabilities array
		// the label that gets highest accumulated probability, is the most likely label for all examples combined
		for(String example : examples) {
			GraphFieldOnly exampleGraph ;
			double[] probabilitiesForExample ;
			exampleGraph = new GraphFieldOnly(example, null, new ArrayList<String>(featureSet(example, columnFeatures)), globalData) ;
			probabilitiesForExample = MAPPredictor.probabilitiesForLabels(exampleGraph) ;
			Matrix.plusEquals(columnProbabilities, probabilitiesForExample, 1.0) ;
			if (exampleProbabilities != null) {
				exampleProbabilitiesFullList.add(newListFromDoubleArray(probabilitiesForExample)) ;
			}
		}
		// the sum of all values in the probabilies array is going to be examples.size()
		// normalize to get values that have a probabilistic interpretation
		for(int i=0;i<globalData.labels.size();i++) {
			columnProbabilities[i]/=examples.size() ;
		}
		// Sort both lists such that labels are listed according to their descending order of probability
		// and probabilityList has the probabilities in the descending order 
		// The label at index i has the probability at index i
		labels = new ArrayList<String>(globalData.labels) ;
		columnProbabilitiesList = newListFromDoubleArray(columnProbabilities) ;
		ListOps.sortListOnValues(labels, columnProbabilitiesList) ;
		// Preparing to return values now
		predictedLabels.clear() ;
		confidenceScores.clear() ;
		if (exampleProbabilities != null) {
			exampleProbabilities.clear() ;
			int minPreds = Math.min(numPredictions, globalData.labels.size()) ;
			for(int i=0;i<examples.size();i++) {
				exampleProbabilities.add(new double[minPreds]) ;
			}
		}
		for(int index=0;index < globalData.labels.size() && index < numPredictions;index++) {
			predictedLabels.add(labels.get(index)) ;
			confidenceScores.add(columnProbabilitiesList.get(index)) ;
			if (exampleProbabilities != null) {
				int li = globalData.labels.indexOf(labels.get(index)) ;
				for(int i=0;i<examples.size();i++) {
					exampleProbabilities.get(i)[index] = exampleProbabilitiesFullList.get(i).get(li) ;
				}
			}
		}
		return true ;
	}


	
		
	// This function takes the path of file as input and
	// creates an environment that consists of globalData, crfModel, list of examples of each label, etc.
	// It reads an empty file also.
	public static boolean readModelFromFile(String file) {
		if (file == null) {
			logger.debug("Invalid argument value. Argument @file is null.") ;
			CRFModelHandlerOld.file = null ;
			return false ;
		}
		BufferedReader br ;
		String line ;
		int numLabels ;
		boolean emptyFile ;
		int numFFs  ;
		ArrayList<LblFtrPair> ffs ;
		double[] weights ;
		CRFModelFieldOnly crfModel  ;
		// beginning execution
		br = null ;
		line = null ;
		numLabels = -1 ;
		try {
			br = new BufferedReader(new FileReader(file)) ;
			emptyFile = true ;
			while((line = br.readLine()) != null) {
				if (line.trim().length() != 0) {
					emptyFile = false ;
					break ;
				}
			}
			br.close() ;
		}
		catch(Exception e) {
			logger.debug("Error reading model file " + file + ".") ;
			CRFModelHandlerOld.file = null ;
			return false ;
		}
		if (emptyFile) {
			globalData = new GlobalDataFieldOnly() ;
			labelToExamplesMap = new HashMap<String, ArrayList<String>>() ;
			globalData.trainingGraphs = new ArrayList<GraphInterface>() ;
			crfModel = new CRFModelFieldOnly(globalData) ;
			crfModel.ffs = new ArrayList<LblFtrPair>() ;
			crfModel.weights = new double[0] ;
			globalData.crfModel = crfModel ;
			CRFModelHandlerOld.file = file ;
			return true ;
		}
		else {
			globalData = new GlobalDataFieldOnly() ;
			labelToExamplesMap = new HashMap<String, ArrayList<String>>() ;
			try {
				br = new BufferedReader(new FileReader(file)) ;
				// Read the number of labels in the model file
				numLabels = Integer.parseInt(br.readLine().trim()) ;
				br.readLine() ; // consume the empty line after the first line

				// read numLabels labels and their examples
				for(int labelNumber = 0 ; labelNumber < numLabels ; labelNumber++) {
					String newLabel;
					ArrayList<String> examples  ;
					int numExamples ;
					newLabel = br.readLine().trim() ;
					if (globalData.labels.contains(newLabel)) {
						logger.debug("The label " + newLabel + " was already added to the model. " +
								"Later in the file, we found another list that had the same label and a set of examples underneath it. This is an error. " + 
								"A label can only occur one in the file. All its examples have to be listed underneath it at one place.") ;
						CRFModelHandlerOld.file = null ;
						br.close() ;
						return false ;
					}
					globalData.labels.add(newLabel) ;
					examples = new ArrayList<String>() ;
					numExamples = Integer.parseInt(br.readLine().trim()) ;
					for(int egNumber = 0 ; egNumber < numExamples ; egNumber++) {
						String example;
						example = br.readLine().trim() ;
						if (example.length() == 0) {
							logger.debug("While reading " + numExamples + " examples for the label " + newLabel + ", we encountered an empty line before all the examples before read. " +
									"This is an error. All examples for a label have to appear together under the name of the label. "+
									"No blank lines are allowed between examples of a label.");
							CRFModelHandlerOld.file = null ;
							br.close() ;
							return false ;
						}
						examples.add(example) ;
					}
					labelToExamplesMap.put(newLabel, examples) ;
					br.readLine() ; // consuming the empty line after each list of label and its examples
				}
				// Creating trainingGraphs for all the examples
				globalData.trainingGraphs = new ArrayList<GraphInterface>() ;
				for(String lbl : globalData.labels) {
					for(String example : labelToExamplesMap.get(lbl)) {
						globalData.trainingGraphs.add(new GraphFieldOnly(example, lbl, new ArrayList<String>(featureSet(example)), globalData)) ;
					}
				}
				// starting to read in feature-functions and their weights. the first line is the number of such ffs. 
				numFFs = Integer.parseInt(br.readLine().trim()) ;
				ffs = new ArrayList<LblFtrPair>() ;
				weights = new double[numFFs] ;
				for(int ffNumber = 0 ; ffNumber < numFFs ; ffNumber++) {
					String[] lineParts ;
					line = br.readLine().trim() ;
					if (line.length() == 0) {
						logger.debug("While reading " + numFFs + " feature functions, we encountered an empty line. This is an error. " +
								"All feature functions have to be listed continuously without any blank lines in between.") ;
						CRFModelHandlerOld.file = null ;
						br.close() ;
						return false ;
					}
					lineParts = line.split("\\s+") ;
					ffs.add(new LblFtrPair(globalData.labels.indexOf(lineParts[0]), lineParts[1])) ;
					weights[ffNumber] = Double.parseDouble(lineParts[2]) ;
				}
				crfModel = new CRFModelFieldOnly(globalData) ;
				crfModel.ffs = ffs ;
				crfModel.weights = weights ;
				globalData.crfModel = crfModel ;
				br.close() ;
				CRFModelHandlerOld.file = file ;
				return true ;
			}
			catch(Exception e) {
				logger.debug("Error parsing model file " + file + ".") ;
				CRFModelHandlerOld.file = null ;
				return false ;
			}
		}
	}

	
	public static boolean removeAllLabels() {
		BufferedWriter bw;
		CRFModelFieldOnly crfModel;
		if (file == null) {
			logger.debug("CRF Model is not ready, either because it was never read or an error happened while reading it previously. Please try reading the model file again.");
			return false ;
		}
		try {
			bw = new BufferedWriter(new FileWriter(file)) ;
			bw.write("") ;
			bw.close() ;
		}
		catch(Exception e) {
			logger.debug("Clearing the contents of the model file failed.") ;
			CRFModelHandlerOld.file = null ;
			return false ;
		}
		labelToExamplesMap = new HashMap<String, ArrayList<String>>() ;
		globalData = new GlobalDataFieldOnly() ;
		globalData.trainingGraphs = new ArrayList<GraphInterface>() ;
		crfModel = new CRFModelFieldOnly(globalData) ;
		crfModel.ffs = new ArrayList<LblFtrPair>() ;
		crfModel.weights = new double[0] ;
		globalData.crfModel = crfModel ;
		return true ;
	}
	
	
	
	public static boolean removeLabel(String label) {
		int labelIndex;
		ArrayList<Double> weightsList;
		ArrayList<LblFtrPair> otherFFs ;
		double[] newWeights ;
		OptimizeFieldOnly optimizationObject;
		boolean savingSuccessful;
		if (file == null) {
			logger.debug("CRF Model is not ready, either because it was never read or an error happened while reading it previously. Please try reading the model file again.");
			return false ;
		}
		if (label == null) {
			logger.debug("Illegal value, null, passed for argument @label") ;
			return false ;
		}
		label = label.trim() ;
		labelIndex = globalData.labels.indexOf(label) ;
		if (labelIndex == -1) {
			logger.debug("Label " + label + " does not exist in the CRF model.") ;
			return false ;
		}
		globalData.labels.remove(labelIndex) ;
		labelToExamplesMap.remove(label) ;
		for(int i=0;i<globalData.trainingGraphs.size();i++) {
			GraphFieldOnly graph;
			graph = (GraphFieldOnly) globalData.trainingGraphs.get(i) ;
			if (graph.node.labelIndex == labelIndex) {
				globalData.trainingGraphs.remove(i) ;
				i-- ;
			}
			else if(graph.node.labelIndex > labelIndex) {
				graph.node.labelIndex-- ;
			}
		}
		weightsList = new ArrayList<Double>() ;
		otherFFs = new ArrayList<LblFtrPair>() ;
		for(int i=0;i<globalData.crfModel.ffs.size();i++) {
			if (globalData.crfModel.ffs.get(i).labelIndex != labelIndex) {
				otherFFs.add(globalData.crfModel.ffs.get(i)) ;
				weightsList.add(globalData.crfModel.weights[i]) ;
			}
		}
		// Since the label has been removed from dataModel.labels
		// the labels that were after this label in dataModel.labels list
		// will now have their index reduced by 1.
		// Therefore, all ffs that had labelIndex > the index of the label to be removed
		// should have their
		for(LblFtrPair ff : otherFFs) {
			if (ff.labelIndex > labelIndex) {
				ff.labelIndex-- ;
			}
		}
		newWeights = new double[weightsList.size()] ;
		for(int i=0;i<weightsList.size();i++) {
			newWeights[i] = weightsList.get(i) ;
		}
		globalData.crfModel.ffs = otherFFs ;
		globalData.crfModel.weights = newWeights ;
		optimizationObject = new OptimizeFieldOnly(globalData.crfModel, globalData) ;
		optimizationObject.optimize(10) ;
		savingSuccessful = saveModel() ;
		if (!savingSuccessful) {
			CRFModelHandlerOld.file = null ;
		}
		return savingSuccessful ;
	}

	
	
	
	/**
	 * @param label
	 * @param examples
	 * @return
	 */
	private static boolean addOrUpdateLabel(String label, List<String> examples) {
		if (file == null) {
			logger.debug("CRF Model is not ready, either because it was never read or an error happened while reading it previously. Please try reading the model file again.");
			return false ;
		}
		else {
			return addOrUpdateLabel(label, examples, null) ;
		}
	}


	
	/**
	 * @return Returns list of allowed Characters
	 */
	private static ArrayList<String> allowedCharacters() {
		ArrayList<String> allowed = new ArrayList<String>() ;
		// Adding A-Z
		for(int c=65;c<=90;c++) {
			allowed.add(new Character((char) c).toString()) ;
		}
		// Adding a-z
		for(int c=97;c<=122;c++) {
			allowed.add(new Character((char) c).toString()) ;
		}
		// Adding 0-9
		for(int c=48;c<=57;c++) {
			allowed.add(new Character((char) c).toString()) ;
		}
		allowed.add(" ") ;  // adding space
		allowed.add(".") ;  // adding dot
		allowed.add("%") ;  
		allowed.add("@") ;  
		allowed.add("_") ;  
		allowed.add("-") ;  
		allowed.add("*") ;  
		allowed.add("(") ;
		allowed.add(")") ;
		allowed.add("[") ;
		allowed.add("]") ;
		allowed.add("+") ;
		allowed.add("/") ;
		allowed.add("&") ;
		allowed.add(":") ;
		allowed.add(",") ;
		allowed.add(";") ;
		allowed.add("?") ;
		return allowed ;
	}

	
	
	/**
	 * @param uncleanList List of all examples
	 * @param cleanedList List with examples that dont have unallowed chars and others such as nulls or empty strings
	 * This method cleans the examples list passed to it. Generally, it is used by other methods to sanitize lists passed from outside.
	 */
	private static void cleanedExamplesList(List<String> uncleanList, List<String> cleanedList) {
		ArrayList<String> allowedCharacters;
		allowedCharacters = allowedCharacters() ;
		cleanedList.clear();
		for(String example : uncleanList) {
			if (example != null) {
				String trimmedExample ;
				trimmedExample = "" ;
				for(int i=0;i<example.length();i++) {
					String charAtIndex;
					charAtIndex = example.substring(i,i+1) ;
					if (allowedCharacters.contains(charAtIndex)) {
						trimmedExample+=charAtIndex ;
					}
				}
				if (trimmedExample.length() != 0) {
					cleanedList.add(trimmedExample) ;
				}
			}
		}
	}
	
	
	private static HashSet<String> featureSet(String field) {
		ArrayList<Part> tokens = Lexer.tokenizeField(field);
		HashSet<String> features = new HashSet<String>() ;
		for(Part token : tokens) {
			features.addAll(RegexFeatureExtractor.getTokenFeatures(token)) ;
		}
		return features ;
	}

	
	/**
	 * @param field - field for which features are to be extracted
	 * @param columnFeatures - the columnFeatures passed along
	 * @return - a set of features based on syntax as well as column features
	 */
	private static HashSet<String> featureSet(String field, Map<ColumnFeature, Collection<String>> columnFeatures) {
		HashSet<String> features ;
		features = featureSet(field) ;
		if (columnFeatures != null) {
			if (columnFeatures.containsKey(ColumnFeature.ColumnHeaderName)) {
				Collection<String> columnNameList ;
				columnNameList = columnFeatures.get(ColumnFeature.ColumnHeaderName) ;
				if (columnNameList != null && columnNameList.size() == 1) {
					String columnName ;
					List<String> parts;
					columnName = null ;
					for(String str : columnNameList) {
						columnName = str ;
					}
					parts = splitString(columnName);
					for(String part : parts) {
						features.add(part.toLowerCase()) ;
					}
				}
			}
			if (columnFeatures.containsKey(ColumnFeature.TableName)) {
				Collection<String> tableNamesList;
				tableNamesList = columnFeatures.get(ColumnFeature.TableName);
				if (tableNamesList != null && tableNamesList.size() == 1) {
					String tableName;
					List<String> parts;
					tableName = null ;
					for(String s : tableNamesList) {
						tableName = s;
					}
					parts = splitString(tableName);
					for(String part : parts) {
						features.add(part.toLowerCase());
					}
				}
			}
		}
		return features ;
	}
	
	
	private static ArrayList<Double> newListFromDoubleArray(double[] array) {
		ArrayList<Double> newList = new ArrayList<Double>() ;
		for(double element : array) {
			newList.add(element) ;
		}
		return newList ;
	}
	
	
	
	/**
	 * @param examples - list of examples of an unknown type
	 * @param numPredictions - required number of predictions in descending order
	 * @param predictedLabels - the argument in which the ordered list of labels is returned. the size of this list could be smaller than numPredictions
	 * 							if there aren't that many labels in the model already
	 * @param confidenceScores - the probability of the examples belonging to the labels returned.
	 * @return
	 */
	private static boolean predictLabelForExamples(
			List<String> examples,
			int numPredictions,
			List<String> predictedLabels,
			List<Double> confidenceScores
			) {
		if (CRFModelHandlerOld.file == null) {
			logger.debug("CRF Model is not ready, either because it was never read or an error happened while reading it previously. Please try reading the model file again.");
			return false ;
		}
		else {
			return predictLabelForExamples(examples, numPredictions, predictedLabels, confidenceScores, null) ;
		}
	}

	/**
	 * @param examples - list of examples of an unknown type
	 * @param numPredictions - required number of predictions in descending order
	 * @param predictedLabels - the argument in which the ordered list of labels is returned. the size of this list could be smaller than numPredictions
	 * 							if there aren't that many labels in the model already
	 * @param confidenceScores - the probability of the examples belonging to the labels returned.
	 * @param exampleProbabilities - the size() == examples.size(). It contains, for each example, in the same order, a double array that contains the probability 
	 * 									of belonging to the labels returned in predictedLabels.
	 * @return
	 */
	private static boolean predictLabelForExamples(
			List<String> examples,
			int numPredictions, 
			List<String> predictedLabels, 
			List<Double> confidenceScores, 
			List<double[]> exampleProbabilities
			) {
		if (CRFModelHandlerOld.file == null) {
			logger.debug("CRF Model is not ready, either because it was never read or an error happened while reading it previously. Please try reading the model file again.");
			return false ;
		}
		else {
			return predictLabelForExamples(examples, numPredictions, predictedLabels, confidenceScores, exampleProbabilities, null) ;
		}
	}


	
	

	/**
	 * This method writes the model in memory to the file that it was read from.
	 * @return true, if writing is successful, else return, false
	 */
	static private boolean saveModel() {
		try {
			BufferedWriter bw;
			bw = new BufferedWriter(new FileWriter(file)) ;
			// Write the number of labels and then a blank line
			bw.write(globalData.labels.size() + "\n\n") ;
			for(String label : globalData.labels) {
				ArrayList<String> examples;
				bw.write(label + "\n") ;
				examples = labelToExamplesMap.get(label) ;
				bw.write(examples.size() + "\n") ;
				for(String example : examples) {
					bw.write(example + "\n") ;
				}
				bw.write("\n") ;
			}
			bw.write(globalData.crfModel.ffs.size() + "\n") ;
			for(int ffIndex = 0;ffIndex<globalData.crfModel.ffs.size();ffIndex++) {
				LblFtrPair ff;
				ff = globalData.crfModel.ffs.get(ffIndex) ;
				bw.write(globalData.labels.get(ff.labelIndex) + " " + ff.feature + " " + globalData.crfModel.weights[ffIndex] + "\n") ;
			}
			bw.close() ;
			return true ;
		}
		catch(Exception e) {
			logger.debug("Writing the model to file " + file + " failed. The file can be inconsistent with the model in memory until it is successfully written.") ;
			return false ;
		}
	}

	
	static private boolean saveModelNew() {
		
		
		return true;
	}
	

	/**
	 * This method is a custom tokenizer meant to tokenize column and table names
	 * @param str - string to be split
	 * @return list of parts of str
	 */
	private static ArrayList<String> splitString(String str) {
		HashSet<String> splitters;
		ArrayList<String> parts;
		// basic argument sanity check
		if (str == null) {
			return null ;
		}
		// creating the preset splitters
		splitters = new HashSet<String>();
		splitters.add("\\s+");
		splitters.add("_");
		parts = new ArrayList<String>();
		// setting up the arraylist for iterative processing 
		parts.add(str);
		// iterate over all splitters
		for(String splitter : splitters) {
			ArrayList<String> tmpParts;
			tmpParts = new ArrayList<String>();
			for(String part : parts) {
				String[] tokens;
				tokens = part.split(splitter);
				tmpParts.addAll(Arrays.asList(tokens));
			}
			parts = tmpParts;
		}
		return parts;
	}

}
