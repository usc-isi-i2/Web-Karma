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


public abstract class CRFModelHandler {

	public static enum ColumnFeature {
		ColumnHeaderName
	} ;
	
	
	static String file = null ;
	static HashMap<String, ArrayList<String>> labelToExamplesMap ;
	static GlobalDataFieldOnly globalData ;

	
	

	// This function takes the path of file as input and
	// creates an environment that consists of globalData, crfModel, list of examples of each label, etc.
	// It reads an empty file also.
	public static boolean readModelFromFile(String file) {
		if (CRFModelHandler.file != null) {
			return false ;
		}
		globalData = new GlobalDataFieldOnly() ;
		labelToExamplesMap = new HashMap<String, ArrayList<String>>() ;
		BufferedReader br = null ;
		String line = null ;
		int numLabels = -1 ;
		try {
			br = new BufferedReader(new FileReader(file)) ;
			boolean emptyFile = true ;
			while((line = br.readLine()) != null) {
				line = line.trim() ;
				if (line.length() != 0) {
					emptyFile = false ;
					break ;
				}
			}
			br.close() ;
			if (emptyFile) {
				globalData.trainingGraphs = new ArrayList<GraphInterface>() ;
				CRFModelFieldOnly crfModel = new CRFModelFieldOnly(globalData) ;
				crfModel.ffs = new ArrayList<LblFtrPair>() ;
				crfModel.weights = new double[0] ;
				globalData.crfModel = crfModel ;
				CRFModelHandler.file = file ;
				return true ;
			}
		}
		catch(Exception e) {
			return false ;
		}
		try {
			br = new BufferedReader(new FileReader(file)) ;
			// Read the number of labels in the model file
			numLabels = Integer.parseInt(br.readLine().trim()) ;
			br.readLine() ; // consume the empty line after the first line

			// read numLabels labels and their examples
			for(int labelNumber = 0 ; labelNumber < numLabels ; labelNumber++) {
				String newLabel = br.readLine().trim() ;
				if (globalData.labels.contains(newLabel)) {
					Prnt.prn("The label " + newLabel + " was already added to the model. " +
							"Later in the file, we found another list that had the same label and a set of examples underneath it. This is an error. " + 
							"A label can only occur one in the file. All its examples have to be listed underneath it at one place.") ;
					return false ;
				}
				globalData.labels.add(newLabel) ;
				ArrayList<String> examples = new ArrayList<String>() ;
				labelToExamplesMap.put(newLabel, examples) ;
				int numExamples = Integer.parseInt(br.readLine().trim()) ;
				for(int egNumber = 0 ; egNumber < numExamples ; egNumber++) {
					String example = br.readLine().trim() ;
					if (example.length() == 0) {
						Prnt.prn("While reading " + numExamples + " examples for the label " + newLabel + ", we encountered an empty line before all the examples before read. " +
								"This is an error. All examples for a label have to appear together under the name of the label. "+
								"No blank lines are allowed between examples of a label.");
						return false ;
					}
					examples.add(example) ;
				}
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
			int numFFs = Integer.parseInt(br.readLine().trim()) ;
			ArrayList<LblFtrPair> ffs = new ArrayList<LblFtrPair>() ;
			double[] weights = new double[numFFs] ;
			for(int ffNumber = 0 ; ffNumber < numFFs ; ffNumber++) {
				line = br.readLine().trim() ;
				if (line.length() == 0) {
					Prnt.prn("While reading " + numFFs + " feature functions, we encountered an empty line. This is an error. " +
							"All feature functions have to be listed continuously without any blank lines in between.") ;
					return false ;
				}
				String[] lineParts = line.split("\\s+") ;
				ffs.add(new LblFtrPair(globalData.labels.indexOf(lineParts[0]), lineParts[1])) ;
				weights[ffNumber] = Double.parseDouble(lineParts[2]) ;
			}
			CRFModelFieldOnly crfModel = new CRFModelFieldOnly(globalData) ;
			crfModel.ffs = ffs ;
			crfModel.weights = weights ;
			globalData.crfModel = crfModel ;
			br.close() ;
			CRFModelHandler.file = file ;
			return true ;
		}
		catch(Exception e) {
			return false ;
		}
	}

	public static boolean addOrUpdateLabel(String label, List<String> examples) {
		return addOrUpdateLabel(label, examples, null) ;
	}

	public static boolean addOrUpdateLabel(String label, List<String> examples, Map<ColumnFeature, Collection<String>> columnFeatures) {
		ArrayList<String> cleanedExamples, allowedCharacters ;
		int labelIndex ;
		HashSet<String> newFeatures, existingFeatures ;
		OptimizeFieldOnly optimizationObject;
		double[] newWeights;
		boolean savingSuccessful ;
		// running basic sanity checks in the input arguments
		label = label.trim() ;
		if (label.length() == 0 || examples.size() == 0) {
			Prnt.prn("label argument cannot be an empty string and the examples list cannot be empty.") ;
			return false ;
		}
		cleanedExamples = new ArrayList<String>() ;
		allowedCharacters = allowedCharacters() ;
		for(String example : examples) {
			String trimmedExample ;
			if (example != null) {
				trimmedExample = "" ;
				for(int i=0;i<example.length();i++) {
					String charAtIndex;
					charAtIndex = example.substring(i,i+1) ;
					if (allowedCharacters.contains(charAtIndex)) {
						trimmedExample+=charAtIndex ;
					}
				}
				if (trimmedExample.length() != 0) {
					cleanedExamples.add(trimmedExample) ;
				}
			}
		}
		examples = cleanedExamples ;
		// making sure that the condition where the examples list is not empty but contains junk only is not accepted
		if (examples.size() == 0) {
			Prnt.prn("examples list contains forbidden characters only. the allowed characters are " + allowedCharacters) ;
			return false ;
		}
		existingFeatures = new HashSet<String>() ;
		newFeatures = new HashSet<String>() ;
		labelIndex = globalData.labels.indexOf(label) ;
		if (globalData.labels.contains(label)) {
			for(LblFtrPair ff : globalData.crfModel.ffs) {
				if (ff.labelIndex == labelIndex) {
					existingFeatures.add(ff.feature) ;
				}
			}
		}
		// if label does not already exist in the model, add new label. Also, add an entry in the map for the new label.
		if (labelIndex == -1) {
			globalData.labels.add(label) ;
			labelIndex = globalData.labels.indexOf(label) ;
			labelToExamplesMap.put(label, new ArrayList<String>()) ;
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
			newFeatures.addAll(exampleFtrs) ;
		}
		// add new feature functions to the crfModel
		newFeatures.removeAll(existingFeatures) ;
		for(String ftr : newFeatures) {
			globalData.crfModel.ffs.add(new LblFtrPair(labelIndex, ftr)) ;
		}
		// If we added any new feature functions, then we expand the weights array to match the length of the ff list in CRFModelFieldOnly
		// we copy the old values of weights for the old ffs and set the new weights to be zero.
		if (globalData.crfModel.ffs.size() != globalData.crfModel.weights.length) {
			newWeights = new double[globalData.crfModel.ffs.size()] ;
			System.arraycopy(globalData.crfModel.weights, 0, newWeights, 0, globalData.crfModel.weights.length) ;
			globalData.crfModel.weights = newWeights ;
		}
		// optimize the model to adjust to the new label/examples/ffs
		optimizationObject = new OptimizeFieldOnly(globalData.crfModel, globalData) ;
		optimizationObject.optimize(5) ;
		// save the model to file with the new weights
		savingSuccessful = saveModel() ;
		return savingSuccessful ;
	}
	
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

	public static boolean removeLabel(String label) {
		if (file == null) {
			Prnt.prn("CRFModelHandler.removeLabel: A model has not been created yet.") ;
			return false ;
		}
		if (label == null) {
			return false ;
		}
		label = label.trim() ;
		int labelIndex = globalData.labels.indexOf(label) ;
		if (labelIndex == -1) {
			return false ;
		}
		globalData.labels.remove(labelIndex) ;
		labelToExamplesMap.remove(label) ;
		for(int i=0;i<globalData.trainingGraphs.size();i++) {
			GraphFieldOnly graph = (GraphFieldOnly) globalData.trainingGraphs.get(i) ;
			if (graph.node.labelIndex == labelIndex) {
				globalData.trainingGraphs.remove(i) ;
				i-- ;
			}
			else if(graph.node.labelIndex > labelIndex) {
				graph.node.labelIndex-- ;
			}
		}

		ArrayList<Double> weightsList = new ArrayList<Double>() ;
		ArrayList<LblFtrPair> otherFFs = new ArrayList<LblFtrPair>() ;
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
		double[] newWeights = new double[weightsList.size()] ;
		for(int i=0;i<weightsList.size();i++) {
			newWeights[i] = weightsList.get(i) ;
		}
		globalData.crfModel.ffs = otherFFs ;
		globalData.crfModel.weights = newWeights ;
		OptimizeFieldOnly optimizationObject = new OptimizeFieldOnly(globalData.crfModel, globalData) ;
		optimizationObject.optimize(10) ;
		boolean savingSuccessful = false ;
		savingSuccessful = saveModel() ;
		return savingSuccessful ;
	}

	public static boolean removeAllLabels() {
		try {
			if (file == null) {
				Prnt.prn("CRFModelHandler.removeAllLabels(): A model has not been created") ;
				return false ;
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file)) ;
			bw.write("") ;
			bw.close() ;
			labelToExamplesMap = new HashMap<String, ArrayList<String>>() ;
			globalData = new GlobalDataFieldOnly() ;
			globalData.trainingGraphs = new ArrayList<GraphInterface>() ;
			CRFModelFieldOnly crfModel = new CRFModelFieldOnly(globalData) ;
			crfModel.ffs = new ArrayList<LblFtrPair>() ;
			crfModel.weights = new double[0] ;
			globalData.crfModel = crfModel ;
			return true ;
		}
		catch(Exception e) {
			return false ;
		}
	}

	static private boolean saveModel() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file)) ;
			// Write the number of labels and then a blank line
			bw.write(globalData.labels.size() + "\n\n") ;
			for(String label : globalData.labels) {
				bw.write(label + "\n") ;
				ArrayList<String> examples = labelToExamplesMap.get(label) ;
				bw.write(examples.size() + "\n") ;
				for(String example : examples) {
					bw.write(example + "\n") ;
				}
				bw.write("\n") ;
			}
			bw.write(globalData.crfModel.ffs.size() + "\n") ;
			for(int ffIndex = 0;ffIndex<globalData.crfModel.ffs.size();ffIndex++) {
				LblFtrPair ff = globalData.crfModel.ffs.get(ffIndex) ;
				bw.write(globalData.labels.get(ff.labelIndex) + " " + ff.feature + " " + globalData.crfModel.weights[ffIndex] + "\n") ;
			}
			bw.close() ;
			return true ;
		}
		catch(Exception e) {
			return false ;
		}
	}

	public static boolean predictLabelForExamples(
			List<String> examples,
			int numPredictions,
			List<String> predictedLabels,
			List<Double> confidenceScores
			) {
		return predictLabelForExamples(examples, numPredictions, predictedLabels, confidenceScores, null) ;
	}



	/**
	 * @param examples
	 * @param numPredictions
	 * @param predictedLabels
	 * @param confidenceScores
	 * @param exampleProbabilities
	 * @return
	 */
	public static boolean predictLabelForExamples(
			List<String> examples,
			int numPredictions, 
			List<String> predictedLabels, 
			List<Double> confidenceScores, 
			List<double[]> exampleProbabilities
			) {
		return predictLabelForExamples(examples, numPredictions, predictedLabels, confidenceScores, exampleProbabilities, null) ;
	}

	/**
	 * @param examples
	 * @param numPredictions
	 * @param predictedLabels
	 * @param confidenceScores
	 * @param exampleProbs
	 * @param columnFeatures
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
		// Sanity checks for arguments
		if (examples == null || examples.size() == 0 || numPredictions <= 0 || predictedLabels == null || confidenceScores == null) {
			Prnt.prn("Invalid arguments. Possible problems: examples list size is zero, numPredictions is non-positive, predictedLabels or confidenceScores list is null.") ;
			return false ;
		}
		// Making sure that there exists a model.
		if(file == null || globalData.labels.size() == 0) {
			Prnt.prn("The model is not ready to make predictions. Either a model has not been created from a file, or the file was empty.") ;
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


	public static boolean getLabels(ArrayList<String> labels) {
		if (labels == null) {
			Prnt.prn(" CRFModelHandler.getLabels: Supplied argument is null.") ;
			return false ;
		}
		labels.clear() ;
		for(String label : globalData.labels) {
			labels.add(label) ;
		}
		return true ;
	}

	public static boolean getExamplesForLabel(String label, ArrayList<String> examples) {
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
		for(String example : labelToExamplesMap.get(label)) {
			examples.add(example) ;
		}
		return true ;
	}

	private static HashSet<String> featureSet(String field) {
		ArrayList<Part> tokens = Lexer.tokenizeField(field);
		HashSet<String> features = new HashSet<String>() ;
		for(Part token : tokens) {
			features.addAll(RegexFeatureExtractor.getTokenFeatures(token)) ;
		}
		return features ;
	}

	
	private static HashSet<String> featureSet(String field, Map<ColumnFeature, Collection<String>> columnFeatures) {
		String columnName ;
		HashSet<String> features ;
		Collection<String> columnNameList ;
		String[] tokens ;
		columnName = null ;
		features = featureSet(field) ;
		if (columnFeatures != null && columnFeatures.containsKey(ColumnFeature.ColumnHeaderName)) {
			columnNameList = columnFeatures.get(ColumnFeature.ColumnHeaderName) ;
			if (columnNameList != null && columnNameList.size() == 1) {
				for(String str : columnNameList) {
					columnName = str ;
				}
				tokens = columnName.split("\\s+") ;
				for(String token : tokens) {
					features.add(token) ;
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


}