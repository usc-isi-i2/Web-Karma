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


public class CRFModelHandler {

	// ***********************************************************************************************
	/**
	 * @author amangoel
	 * The ColumnFeature enum with members representing the possible features that could be passed.
	 *
	 */
	public enum ColumnFeature {
		ColumnHeaderName ,
		TableName
	} ;

	// ***********************************************************************************************

	// ***********************************************************************************************
	/**
	 * @author amangoel
	 * This internal class represents an example.
	 */
	static class Example {
		String exampleString;
		HashMap<ColumnFeature, String> columnFeatures;

		/**
		 * @param exampleString The string that the example represents
		 * No ColumnFeatures specified.
		 */
		public Example(String exampleString) {
			this.exampleString = exampleString;
			columnFeatures = new HashMap<CRFModelHandler.ColumnFeature, String>();
		}

		/**
		 * @param exampleString The example string
		 * @param columnFeatures Associated ColumnFeatures
		 * It takes in a collection of feature values for each ColumnFeature, 
		 * but only picks the first value to store in the Example, 
		 * as I don't see yet why more than one String should be associated with a ColumnFeature.
		 */
		public Example(String exampleString, Map<ColumnFeature, Collection<String>> columnFeatures) {
			this.exampleString = exampleString;
			this.columnFeatures = new HashMap<ColumnFeature, String>();
			if (columnFeatures != null) {
				for(Map.Entry<ColumnFeature, Collection<String>> entry : columnFeatures.entrySet()) {
					if (entry.getValue() != null && entry.getValue().size() > 0) {
						String featureValue;
						featureValue = null;
						for(String str : entry.getValue()) {
							featureValue = str;
							break;
						}
						if (featureValue != null) {
							this.columnFeatures.put(entry.getKey(), featureValue);
						}
					}
				}
			}
		}

		/**
		 * @param columnFeature A ColumnFeature
		 * @param featureValue Corresponding Value to the ColumnFeature
		 */
		public void addColumnFeature(ColumnFeature columnFeature, String featureValue) {
			if (columnFeature != null && featureValue != null) {
				columnFeatures.put(columnFeature, featureValue);
			}
		}

		public String getString() {
			return exampleString;
		}

		/**
		 * @param colFeature ColumnFeature for which the value is required.
		 * @return The value corresponding to the ColumnFeature, or null if the example doesn't have the passed ColumnFeature. 
		 * Checking the return value from this method is therefore important.
		 */
		public String getValueForColumnFeature(ColumnFeature colFeature) {
			if (columnFeatures.containsKey(colFeature)) {
				return columnFeatures.get(colFeature);
			}
			else {
				return null;
			}
		}
	}

	// ***********************************************************************************************
	// instance variables
	String file;
	HashMap<String, ArrayList<Example>> labelToExamplesMap;
	GlobalDataFieldOnly globalData;
	ArrayList<String> allowedCharacters;
	static Logger logger = LoggerFactory.getLogger(CRFModelHandler.class.getSimpleName()) ;

	/**
	 * Making the empty constructor private to prevent instantiation of this class.
	 * This class should only be used to access its static methods.
	 */
	public CRFModelHandler() {
		file = null ;
		labelToExamplesMap = null ;
		globalData = null ;
		allowedCharacters = allowedCharacters();
	}

	/**
	 * Returns the path to the file that the CRF Model is using
	 * @return Path to the Model file 
	 */
	public String getModelFilePath() {
		return file;
	}


	/**
	 * @param label True label for the list of example.
	 * @param examples List of example strings.
	 * @param columnFeatures Map of column features.
	 * @return True if success, else False
	 * Adds the passed list of examples to the model. 
	 * Regenerates 100 feature functions to represent the label, 
	 * if examples of this label already exist in the model.
	 */
	public boolean addOrUpdateLabel(String label, List<String> examples, Map<ColumnFeature, Collection<String>> columnFeatures) {
		ArrayList<String> cleanedExamples, allFeatures;
		int labelIndex ;
		HashSet<String> selectedFeatures, tmpFeatures;
		ArrayList<LblFtrPair> ffsOfLabel, otherFFs;
		ArrayList<Double> weightsOfFFsOfLabel, weightsOfOtherFFs;
		OptimizeFieldOnly optimizationObject;
		boolean savingSuccessful ;
		final int NUM_FFs = 50;
		if (file == null) {
			Prnt.prn("CRF Model is not ready, either because it was never read or an error happened while reading it previously. Please try reading the model file again.");
			return false ;
		}
		// running basic sanity checks in the input arguments
		if (label == null || label.trim().length() == 0 || examples.size() == 0) {
			Prnt.prn("@label argument cannot be null or an empty string and the @examples list cannot be empty.") ;
			return false ;
		}
		label = label.trim() ;
		cleanedExamples = new ArrayList<String>() ;
		cleanedExamplesList(examples, cleanedExamples);
		examples = cleanedExamples ;
		// making sure that the condition where the examples list is not empty but contains junk only is not accepted
		if (examples.size() == 0) {
			Prnt.prn("@examples list contains forbidden characters only. The allowed characters are " + allowedCharacters) ;
			return false ;
		}
		// if label does not already exist in the model, add new label. Also, add an entry in the map for the new label.
		labelIndex = globalData.labels.indexOf(label) ;
		if (labelIndex == -1) {
			globalData.labels.add(label) ;
			labelIndex = globalData.labels.indexOf(label) ;
			labelToExamplesMap.put(label, new ArrayList<Example>()) ;
		}
		allFeatures = new ArrayList<String>();
		tmpFeatures = new HashSet<String>();
		// get features of existing examples
		for(Example example: labelToExamplesMap.get(label)) {
			featureSet(example, tmpFeatures);
			allFeatures.addAll(tmpFeatures) ;
		}
		// add examples to the existing list of examples
		// create new Graph for each example and add it to the list of trainingGraphs in globalData
		// save a list of new features so that we can create new feature functions for the label
		for(String exampleString : examples) {
			GraphFieldOnly newGraph ;
			Example newExample;
			newExample = new Example(exampleString, columnFeatures);
			labelToExamplesMap.get(label).add(newExample) ;
			featureSet(newExample, tmpFeatures) ;
			newGraph = new GraphFieldOnly(exampleString, label, new ArrayList<String>(tmpFeatures), globalData) ;
			globalData.trainingGraphs.add(newGraph) ;
			allFeatures.addAll(tmpFeatures) ;
		}
		// if the total number of features is > NUM_FFs, then randomly select NUM_FFs from them.
		selectedFeatures = new HashSet<String>(allFeatures);
		if (selectedFeatures.size() > NUM_FFs) {
			ArrayList<String> tmpAllFeatures;
			Random random ;
			tmpAllFeatures = new ArrayList<String>(allFeatures);
			random = new Random();
			selectedFeatures = new HashSet<String>();
			for(int i=0;i<NUM_FFs;i++) {
				String ftr;
				ftr = tmpAllFeatures.get(random.nextInt(tmpAllFeatures.size()));
				selectedFeatures.add(ftr);
				tmpFeatures.clear();
				tmpFeatures.add(ftr);
				tmpAllFeatures.removeAll(tmpFeatures);
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
		optimizationObject.optimize(3) ;
		// save the model to file with the new weights
		savingSuccessful = saveModel() ;
		if (!savingSuccessful) {
			file = null ;
		}
		return savingSuccessful ;
	}


	/**
	 * @param label The label for which examples are being requested.
	 * @param examples The list argument that will be used to return the list of examples in the model for the supplied label.
	 * @return
	 */
	public boolean getExamplesForLabel(String label, ArrayList<String> examples) {
		ArrayList<Example> examplesOfLabel;
		if (file == null) {
			Prnt.prn("CRF Model is not ready, either because it was never read or an error happened while reading it previously. Please try reading the model file again.");
			return false ;
		}
		if (label == null || label.trim().length() == 0 || examples == null) {
			Prnt.prn("CRFModelHandler.getExamplesForLabel: Either the label is null, or it is an empty string or examples is null") ;
			return false ;
		}
		label = label.trim();
		if (!globalData.labels.contains(label)) {
			Prnt.prn("CRFModelHandler.getExamplesForLabel: Label " + label + " does not exist in the model.") ;
			return false ;
		}
		examples.clear() ;
		examplesOfLabel = labelToExamplesMap.get(label);
		for(Example exampleObject : examplesOfLabel) {
			examples.add(exampleObject.exampleString);
		}
		return true ;
	}


	/**
	 * @param labels The ordered list of labels is returned in this argument.
	 * @return
	 */
	public boolean getLabels(List<String> labels) {
		if (file == null) {
			Prnt.prn("CRF Model is not ready, either because it was never read or an error happened while reading it previously. Please try reading the model file again.");
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
	public boolean predictLabelForExamples(
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
		HashSet<String> features;
		if (file == null) {
			Prnt.prn("CRF Model is not ready, either because it was never read or an error happened while reading it previously. Please try reading the model file again.");
			return false ;
		}
		// Sanity checks for arguments
		if (examples == null || examples.size() == 0 || numPredictions <= 0 || predictedLabels == null || confidenceScores == null) {
			Prnt.prn("Invalid arguments. Possible problems: examples list size is zero, numPredictions is non-positive, predictedLabels or confidenceScores list is null.") ;
			return false ;
		}
		// Making sure that there exists a model.
		if(globalData.labels.size() == 0) {
			Prnt.prn("The model does have not any semantic types. Please add some labels with their examples before attempting to predict using this model.") ;
			return false ;
		}
		exampleProbabilitiesFullList = new ArrayList<ArrayList<Double>>() ;
		MAPPredictor = new MAPFieldOnly(globalData) ;
		columnProbabilities = new double[globalData.labels.size()] ;
		features = new HashSet<String>();
		// for each example, get the probability of each label.
		// add the probabilities to an accumulator probabilities array
		// the label that gets highest accumulated probability, is the most likely label for all examples combined
		for(String example : examples) {
			GraphFieldOnly exampleGraph ;
			String sanitizedExample;
			double[] probabilitiesForExample ;
			sanitizedExample = getSanitizedString(example);
			if (sanitizedExample.length() == 0) {
				sanitizedExample = ".";
			}
			featureSet(sanitizedExample, columnFeatures, features);
			exampleGraph = new GraphFieldOnly(sanitizedExample, null, new ArrayList<String>(features), globalData) ;
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




	/**
	 * @param file The path of the file from which the model should be read.
	 * @return True is successfully read. False, otherwise.
	 * This function takes the path of file as input and
	 * creates an environment that consists of globalData, crfModel, list of examples of each label, etc.
	 * It reads an empty file also.
	 */
	public boolean readModelFromFile(String modelFile) {
		BufferedReader br ;
		String line ;
		int numLabels ;
		boolean emptyFile ;
		int numFFs  ;
		ArrayList<LblFtrPair> ffs ;
		HashSet<String> features;
		double[] weights ;
		CRFModelFieldOnly crfModel  ;
		if (modelFile == null) {
			Prnt.prn("Invalid argument value. Argument @file is null.") ;
			file = null ;
			return false ;
		}
		// beginning execution
		br = null ;
		line = null ;
		numLabels = -1 ;
		try {
			br = new BufferedReader(new FileReader(modelFile)) ;
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
			Prnt.prn("Error reading model file " + modelFile + ".") ;
			file = null ;
			return false ;
		}
		if (emptyFile) {
			globalData = new GlobalDataFieldOnly() ;
			labelToExamplesMap = new HashMap<String, ArrayList<Example>>() ;
			globalData.trainingGraphs = new ArrayList<GraphInterface>() ;
			crfModel = new CRFModelFieldOnly(globalData) ;
			crfModel.ffs = new ArrayList<LblFtrPair>() ;
			crfModel.weights = new double[0] ;
			globalData.crfModel = crfModel ;
			file = modelFile ;
			return true ;
		}
		else {
			features = new HashSet<String>();
			globalData = new GlobalDataFieldOnly() ;
			labelToExamplesMap = new HashMap<String, ArrayList<Example>>() ;
			try {
				br = new BufferedReader(new FileReader(modelFile)) ;
				// Read the number of labels in the model file
				numLabels = Integer.parseInt(br.readLine().trim()) ;
				br.readLine();
				// read numLabels labels and their examples
				for(int labelNumber = 0 ; labelNumber < numLabels ; labelNumber++) {
					String newLabel;
					ArrayList<Example> examples  ;
					int numExamples ;
					newLabel = br.readLine().trim() ;
					if (globalData.labels.contains(newLabel)) {
						Prnt.prn("The label " + newLabel + " was already added to the model. " +
								"Later in the file, we found another list that had the same label and a set of examples underneath it. This is an error. " + 
								"A label can only occur one in the file. All its examples have to be listed underneath it at one place.") ;
						file = null ;
						br.close() ;
						return false ;
					}
					globalData.labels.add(newLabel) ;
					examples = new ArrayList<Example>() ;
					numExamples = Integer.parseInt(br.readLine().trim()) ;
					for(int egNumber = 0 ; egNumber < numExamples ; egNumber++) {
						Example example;
						example = parseExample(br);
						if (example == null) {
							Prnt.prn("Parsing of file failed. Could not parse an example.");
							br.close();
							file = null;
							return false;
						}
						else {
							examples.add(example) ;
						}
					}
					labelToExamplesMap.put(newLabel, examples) ;
					br.readLine() ; // consuming the empty line after each list of label and its examples
				}
				// Creating trainingGraphs for all the examples
				globalData.trainingGraphs = new ArrayList<GraphInterface>() ;
				for(String lbl : globalData.labels) {
					for(Example example : labelToExamplesMap.get(lbl)) {
						featureSet(example, features);
						globalData.trainingGraphs.add(new GraphFieldOnly(example.exampleString, lbl, new ArrayList<String>(features), globalData)) ;
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
						Prnt.prn("While reading " + numFFs + " feature functions, we encountered an empty line. This is an error. " +
								"All feature functions have to be listed continuously without any blank lines in between.") ;
						file = null ;
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
				file = modelFile ;
				return true ;
			}
			catch(Exception e) {
				Prnt.prn("Error parsing model file " + modelFile + ".") ;
				file = null ;
				// SHOULD I CLOSE br HERE ?
				return false ;
			}
		}
	}


	/**
	 * @return True if successfully cleared the model. False, otherwise.
	 * This method removes all labels from the CRF model. 
	 * This is effectively same as setting the model to a state, 
	 * where an empty file has been read for the first time.
	 * Since, each change in the model is immediately reflected
	 * in the model file, this method also completely clears the 
	 * model file.
	 * 
	 */
	public boolean removeAllLabels() {
		BufferedWriter bw;
		CRFModelFieldOnly crfModel;
		if (file == null) {
			Prnt.prn("CRF Model is not ready, either because it was never read or an error happened while reading it previously. Please try reading the model file again.");
			return false ;
		}
		try {
			bw = new BufferedWriter(new FileWriter(file)) ;
			bw.write("") ;
			bw.close() ;
		}
		catch(Exception e) {
			Prnt.prn("Clearing the contents of the model file failed.") ;
			file = null ;
			return false ;
		}
		labelToExamplesMap = new HashMap<String, ArrayList<Example>>() ;
		globalData = new GlobalDataFieldOnly() ;
		globalData.trainingGraphs = new ArrayList<GraphInterface>() ;
		crfModel = new CRFModelFieldOnly(globalData) ;
		crfModel.ffs = new ArrayList<LblFtrPair>() ;
		crfModel.weights = new double[0] ;
		globalData.crfModel = crfModel ;
		return true ;
	}



	public boolean removeLabel(String label) {
		int labelIndex;
		ArrayList<Double> weightsList;
		ArrayList<LblFtrPair> otherFFs ;
		double[] newWeights ;
		OptimizeFieldOnly optimizationObject;
		boolean savingSuccessful;
		if (file == null) {
			Prnt.prn("CRF Model is not ready, either because it was never read or an error happened while reading it previously. Please try reading the model file again.");
			return false ;
		}
		if (label == null) {
			Prnt.prn("Illegal value, null, passed for argument @label") ;
			return false ;
		}
		label = label.trim() ;
		labelIndex = globalData.labels.indexOf(label) ;
		if (labelIndex == -1) {
			Prnt.prn("Label " + label + " does not exist in the CRF model.") ;
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
			file = null ;
		}
		return savingSuccessful ;
	}




	/**
	 * @return Returns list of allowed Characters
	 */
	private ArrayList<String> allowedCharacters() {
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
	private void cleanedExamplesList(List<String> uncleanList, List<String> cleanedList) {
		cleanedList.clear();
		for(String example : uncleanList) {
			if (example != null) {
				String trimmedExample ;
				trimmedExample = getSanitizedString(example);
				if (trimmedExample.length() != 0) {
					cleanedList.add(trimmedExample) ;
				}
			}
		}
	}


	/**
	 * @param columnName The value passed for the ColumnFeature ColumnHeaderName
	 * @param features The set in which the features extracted about this value will be returned.
	 */
	private void extractFeaturesFromColumnName(String columnName, HashSet<String> features) {
		ArrayList<String> parts;
		HashSet<String> nonDupParts;
		parts = new ArrayList<String>();
		nonDupParts = new HashSet<String>();
		features.clear();
		splitString(columnName, parts);
		nonDupParts.addAll(parts);
		for(String part : nonDupParts) {
			part = part.trim();
			if (part.length() > 0) {
				features.add(part.toLowerCase()) ;
			}
		}
	}

	/**
	 * @param tableName The value passed for the ColumnFeature TableName
	 * @param features The set in which the features extracted about this value will be returned.
	 */
	private void extractFeaturesFromTableName(String tableName, HashSet<String> features) {
		ArrayList<String> parts;
		HashSet<String> nonDupParts;
		parts = new ArrayList<String>();
		nonDupParts = new HashSet<String>();
		features.clear();
		splitString(tableName, parts);
		nonDupParts.addAll(parts);
		for(String part : nonDupParts) {
			part = part.trim();
			if (part.length() > 0) {
				features.add(part.toLowerCase()) ;
			}
		}
	}

	/**
	 * @param field A string from which syntactic features will be extracted
	 * @param features The arg used to return those features.
	 */
	private void featureSet(String field, HashSet<String> features) {
		ArrayList<Part> tokens;
		tokens = Lexer.tokenizeField(field);
		features.clear();
		for(Part token : tokens) {
			features.addAll(RegexFeatureExtractor.getTokenFeatures(token)) ;
		}
	}


	/**
	 * @param example The example for which the features have to extracted
	 * @param features The arg used to return those features.
	 */
	private void featureSet(Example example, HashSet<String> features) {
		HashSet<String> tmpFeatures;
		String featureValue;
		tmpFeatures = new HashSet<String>();
		features.clear();
		// add features about the example string itself
		featureSet(example.exampleString, tmpFeatures);
		features.addAll(tmpFeatures);
		// add ftrs about the example's columnname.
		featureValue = example.getValueForColumnFeature(ColumnFeature.ColumnHeaderName);
		if (featureValue != null) {
			extractFeaturesFromColumnName(featureValue, tmpFeatures);
			features.addAll(tmpFeatures);
		}
		// add ftrs about the example's tablename
		featureValue = example.getValueForColumnFeature(ColumnFeature.TableName);
		if (featureValue != null) {
			extractFeaturesFromTableName(featureValue, tmpFeatures);
			features.addAll(tmpFeatures);
		}
	}


	/**
	 * @param field Field for which features are to be extracted
	 * @param columnFeatures The columnFeatures of the field.
	 * @param features A set used to return the features.
	 * This method just uses the first string in every collection to construct an Example.
	 * It then uses featureSet(Example, HashSet<String>) method to return the features for this created example.
	 */
	private void featureSet(String field, Map<ColumnFeature, Collection<String>> columnFeatures, HashSet<String> features) {
		Example example;
		example = new Example(field);
		if (columnFeatures != null) {
			for(Map.Entry<ColumnFeature, Collection<String>> entry : columnFeatures.entrySet()) {
				Collection<String> ftrValues;
				ftrValues = entry.getValue();
				if (ftrValues != null && ftrValues.size() > 0) {
					for(String ftrValue : ftrValues) {
						example.addColumnFeature(entry.getKey(), ftrValue);
						break;
					}
				}
			}
		}
		featureSet(example, features);
	}


	/**
	 * @param array The array of doubles
	 * @return A list containing the same doubles in the same order
	 * A utility method to get a new list having the same values as an array
	 */
	private ArrayList<Double> newListFromDoubleArray(double[] array) {
		ArrayList<Double> newList ;
		newList = new ArrayList<Double>() ;
		for(double element : array) {
			newList.add(element) ;
		}
		return newList ;
	}


	/**
	 * @param br A BufferedReader instance
	 * @return Parsed Example instance.
	 * @throws Exception Mainly IOException
	 * This method starts from wherever the BufferedReader is and keeps reading till it has parsed an entire Example.
	 * Then it returns it.
	 */
	private Example parseExample(BufferedReader br) throws Exception {
		Example example;
		String exampleString;
		int contentLen;
		char c;
		contentLen = parseLengthHeader(br);
		if (contentLen == -1) {
			Prnt.prn("Parsing of file failed since lengthHeader could not be parsed.");
			return null;
		}
		// space has already been consumed
		exampleString = "";
		for(int i=0;i<contentLen;i++) {
			c = (char) br.read();
			exampleString = exampleString + c; 
		}
		example = new Example(exampleString);
		while (true) {
			c = (char) br.read();
			if (10 == (int) c) { // checking for newline character
				break;
			}
			else if (c == ' ') {
				contentLen = parseLengthHeader(br);
				if (contentLen == -1) {
					Prnt.prn("Parsing of file failed since lengthHeader could not be parsed.");
					return null;
				}
				else {
					String columnFeatureStringAndValue, columnFeatureString, columnFeatureValue;
					ColumnFeature columnFeature;
					columnFeatureStringAndValue = "";
					for(int i=0;i<contentLen;i++) {
						c = (char) br.read();
						columnFeatureStringAndValue = columnFeatureStringAndValue + c; 
					}
					columnFeatureString = columnFeatureStringAndValue.split(":")[0];
					columnFeatureValue = columnFeatureStringAndValue.substring(columnFeatureString.length() + 1) ; // to ignore the colon
					columnFeature = null;
					try {
						columnFeature = Enum.valueOf(ColumnFeature.class, columnFeatureString);
					}
					catch (Exception e) {
						Prnt.prn("Parsing of file failed. There is no ColumnFeature called " + columnFeatureString + ".");
						return null;
					}
					example.addColumnFeature(columnFeature, columnFeatureValue);
				}
			}
			else {
				Prnt.prn("Parsing of file failed because found a character other than space or newline after a column feature. The charcter is " + ((int) c));
				return null;
			}
		}
		return example;
	}


	/**
	 * @param br BufferedReader reading the model file.
	 * @return The int value of the string.
	 * @throws Exception
	 */
	private int parseLengthHeader(BufferedReader br) throws Exception {
		String lenHeader;
		char c ;
		int numDigits;
		numDigits = 0;
		lenHeader = "";
		while(true) {
			c = (char)br.read();
			if (c >= '0' && c<= '9') {
				numDigits++;
				lenHeader = lenHeader + c;
				if (numDigits > 5) {
					Prnt.prn("Length marker has more than 5 digits. The program doesn't expect such large entries. Signaling parsing error.");
					return -1;
				}
			}
			else if (c == ' ') {
				if (lenHeader.length() > 0) {
					return Integer.parseInt(lenHeader);
				}
				else {
					return -1;
				}
			}
			else {
				return -1;
			}
		}
	}


	private String getSanitizedString(String unsanitizedString) {
		String sanitizedString ;
		sanitizedString = "" ;
		for(int i=0;i<unsanitizedString.length();i++) {
			String charAtIndex;
			charAtIndex = unsanitizedString.substring(i,i+1) ;
			if (allowedCharacters.contains(charAtIndex)) {
				sanitizedString+=charAtIndex ;
			}
		}
		return sanitizedString;
	}


	/**
	 * This method writes the model in memory to the file that it was read from.
	 * @return true, if writing is successful, else return, false
	 */
	private boolean saveModel() {
		try {
			BufferedWriter bw;
			bw = new BufferedWriter(new FileWriter(file)) ;
			// Write the number of labels and then a blank line
			bw.write(globalData.labels.size() + "\n") ;
			// Insert an empty line
			bw.write("\n");
			// Write name of label and then list its examples.
			for(String label : globalData.labels) {
				ArrayList<Example> examples;
				bw.write(label + "\n") ;
				examples = labelToExamplesMap.get(label) ;
				bw.write(examples.size() + "\n") ;
				for(Example example : examples) {
					bw.write(example.exampleString.length() + " " + example.exampleString) ;
					for(Map.Entry<ColumnFeature, String> entry : example.columnFeatures.entrySet()) {
						if (entry.getValue() != null) {
							String featureValue;
							featureValue = entry.getKey().toString() + ":" + entry.getValue();
							bw.write(" " + featureValue.length() + " " + featureValue);
						}
					}
					bw.write("\n");
				}
				bw.write("\n") ;
			}
			// write all the feature functions
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
			Prnt.prn("Writing the model to file " + file + " failed. The file can be inconsistent with the model in memory until it is successfully written.") ;
			return false ;
		}
	}



	/**
	 * @param str The string to be split
	 * @param parts The list in which the parts will be returned
	 * @return True, if successful. False, if errors like null args.
	 */
	private boolean splitString(String str, ArrayList<String> parts) {
		HashSet<String> splitters;
		ArrayList<String> tmpParts;
		// basic argument sanity check
		if (str == null || parts == null) {
			return false;
		}
		// creating the preset splitters
		splitters = new HashSet<String>();
		splitters.add("\\s+");
		splitters.add("_");
		tmpParts = new ArrayList<String>();
		// setting up the arraylist for iterative processing
		parts.clear();
		parts.add(str);
		// iterate over all splitters
		for(String splitter : splitters) {
			tmpParts.clear();
			for(String part : parts) {
				String[] tokens;
				tokens = part.split(splitter);
				for(String token : tokens) {
					if (token.length() != 0) {
						tmpParts.add(token);
					}
				}
			}
			parts.clear();
			parts.addAll(tmpParts);
		}
		return true;
	}

} // end of class CRFModelHandlerNew



/*

public static boolean getWeightedFeatureFunctionSums(String example, Map<ColumnFeature, Collection<String>> columnFeatures, List<Double> sums) {
	GraphFieldOnly exampleGraph ;
	HashSet<String> features;
	double[] ffSums;
	MAPFieldOnly mapPredictor;
	features = new HashSet<String>();
	featureSet(example, columnFeatures, features);
	exampleGraph = new GraphFieldOnly(example, null, new ArrayList<String>(features), globalData) ;
	mapPredictor = new MAPFieldOnly(globalData);
	ffSums = mapPredictor.weightedFeatureFunctionSums(exampleGraph);
	sums.clear();
	for(double sum : ffSums) {
		sums.add(sum);
	}
	return true;
}


private static boolean addOrUpdateLabel(String label, List<String> examples) {
	if (file == null) {
		Prnt.prn("CRF Model is not ready, either because it was never read or an error happened while reading it previously. Please try reading the model file again.");
		return false ;
	}
	else {
		return addOrUpdateLabel(label, examples, null) ;
	}
}



 * @param examples - list of examples of an unknown type
 * @param numPredictions - required number of predictions in descending order
 * @param predictedLabels - the argument in which the ordered list of labels is returned. the size of this list could be smaller than numPredictions
 * 							if there aren't that many labels in the model already
 * @param confidenceScores - the probability of the examples belonging to the labels returned.
 * @return
private static boolean predictLabelForExamples(
		List<String> examples,
		int numPredictions,
		List<String> predictedLabels,
		List<Double> confidenceScores
		) {
	if (CRFModelHandler.file == null) {
		Prnt.prn("CRF Model is not ready, either because it was never read or an error happened while reading it previously. Please try reading the model file again.");
		return false ;
	}
	else {
		return predictLabelForExamples(examples, numPredictions, predictedLabels, confidenceScores, null) ;
	}
}

 * @param examples - list of examples of an unknown type
 * @param numPredictions - required number of predictions in descending order
 * @param predictedLabels - the argument in which the ordered list of labels is returned. the size of this list could be smaller than numPredictions
 * 							if there aren't that many labels in the model already
 * @param confidenceScores - the probability of the examples belonging to the labels returned.
 * @param exampleProbabilities - the size() == examples.size(). It contains, for each example, in the same order, a double array that contains the probability 
 * 									of belonging to the labels returned in predictedLabels.
 * @return
private static boolean predictLabelForExamples(
		List<String> examples,
		int numPredictions, 
		List<String> predictedLabels, 
		List<Double> confidenceScores, 
		List<double[]> exampleProbabilities
		) {
	if (CRFModelHandler.file == null) {
		Prnt.prn("CRF Model is not ready, either because it was never read or an error happened while reading it previously. Please try reading the model file again.");
		return false ;
	}
	else {
		return predictLabelForExamples(examples, numPredictions, predictedLabels, confidenceScores, exampleProbabilities, null) ;
	}
}

 */


