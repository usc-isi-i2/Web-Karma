package edu.isi.karma.semantictypes.typinghandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.semantictypes.myutils.Prnt;
import edu.isi.karma.semantictypes.globaldata.GlobalData;
import edu.isi.karma.semantictypes.tfIdf.Indexer;
import edu.isi.karma.semantictypes.tfIdf.TopKSearcher;

/**
 * This is the API class for the semantic typing module, implementing the combined approach of 
 * TF-IDF based cosine similarity and Kolmogorov-Smirnov test approaches for textual and numeric 
 * respectively by Ramnandan.S.K and Amol Mittal.
 * 
 * @author ramnandan
 *
 */

public class TypingHandler {

	// Current approach doesn't exploit column feature information. Included for future enhancement.
	/**
	 * @author amangoel
	 * The ColumnFeature enum with members representing the possible features that could be passed.
	 *
	 */
	public enum ColumnFeature {
		ColumnHeaderName ,
		TableName
	} ;

	/**
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
			columnFeatures = new HashMap<ColumnFeature, String>();
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
	
	//****************************************************************************************//
	
	// instance variables
	static Logger logger = LoggerFactory.getLogger(TypingHandler.class.getSimpleName()) ;
	ArrayList<String> allowedCharacters;
	GlobalData globalData;
	Indexer indexer; // responsible for indexing textual data
	
	/**
	 * NOTE: Currently, TF-IDF based approach is used for both textual and numeric data
	 * due to bug in KS test on Apache Commons Math.
	 * 
	 * TODO: Integrate KS test when this bug is resolved : https://issues.apache.org/jira/browse/MATH-1131
	 */
	
	public TypingHandler()
	{
		indexer = new Indexer(); 
		globalData = new GlobalData();
		allowedCharacters = allowedCharacters(); 
		
		// clear any previous index remaining
		try {
			indexer.openIndexWriter();
			indexer.deleteDocuments();
			indexer.commit();
			indexer.closeIndexWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds the passed list of examples for training 
	 * 
	 * @param label True label for the list of example.
	 * @param examples List of example strings.
	 * @param columnFeatures Map of column features --> currently not being used
	 * @return True if success, else False
	 */
	public synchronized boolean addOrUpdateLabel(String label, List<String> examples, Map<ColumnFeature, Collection<String>> columnFeatures) {
		ArrayList<String> cleanedExamples;
		ArrayList<Example> selectedExamples;
		int labelIndex;
		boolean savingSuccessful=false;
		
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
		}
		// adding all the new examples to list of existing examples for the arg label.
		selectedExamples = new ArrayList<Example>();
		for(String newExampleString : examples) {
			Example newExample = new Example(newExampleString, columnFeatures);
			selectedExamples.add(newExample);
		}

		// if the column is textual
		try {
			savingSuccessful = indexTrainingColumn(label, selectedExamples);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return savingSuccessful ;
	}
	
	/**
	 * Indexes the given training column for a specific label 
	 * 
	 * @param label
	 * @param selectedExamples
	 * @return
	 * @throws IOException
	 */
	private boolean indexTrainingColumn(String label, ArrayList<Example> selectedExamples) throws IOException
	{
		indexer.openIndexWriter();
		
		// add documents to index
    	StringBuilder sb = new StringBuilder();
    	for (Example ex : selectedExamples)
    	{
    	    sb.append(ex.getString());
    	    sb.append(" ");
    	}
    	
    	indexer.addDocument(sb.toString(),label);
		indexer.commit();
		indexer.closeIndexWriter();
	
		return true;
	}
	
	/**
	 * @param labels The ordered list of labels is returned in this argument.
	 * @return True, if successful, else False
	 */
	public boolean getLabels(List<String> labels) {
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
	 * @return True, if successful, else False
	 */
	public boolean predictLabelForExamples(
			List<String> examples,
			int numPredictions,
			List<String> predictedLabels,
			List<Double> confidenceScores,
			List<double[]> exampleProbabilities,
			Map<ColumnFeature, Collection<String>> columnFeatures
			) {

		// Sanity checks for arguments
		if (examples == null || examples.size() == 0 || numPredictions <= 0 || predictedLabels == null || confidenceScores == null) {
			logger.warn("Invalid arguments. Possible problems: examples list size is zero, numPredictions is non-positive, predictedLabels or confidenceScores list is null.") ;
			return false ;
		}
		// Making sure that there exists a model.
		if(globalData.labels.size() == 0) {
			logger.warn("The model does have not any semantic types. Please add some labels with their examples before attempting to predict using this model.") ;
			return false ;
		}
		
		// construct single text for test column
	    StringBuilder sb = new StringBuilder();
	    for (String ex : examples)
	    {
	        sb.append(ex);
	        sb.append(" ");
	    }
		
	    // get top-k suggestions
	    try {
	    	TopKSearcher predictor = new TopKSearcher();
			predictor.getTopK(numPredictions, sb.toString(), predictedLabels, confidenceScores);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
	    
		return true ;
	}

	/**
	 * @return True if successfully cleared the model. False, otherwise.
	 * This method removes all labels from the model. 
	 * 
	 * Currently, when only TF-IDF is used, equivalent to deleting all documents
	 */
	public boolean removeAllLabels() {
		
		try {
			indexer.openIndexWriter();
			indexer.deleteDocuments();
			indexer.commit();
			indexer.closeIndexWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true ;
	}

	
	/**
	 * @param uncleanList List of all examples
	 * @param cleanedList List with examples that don't have unallowed chars and others such as nulls or empty strings
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
	 * @param unsanitizedString String to be sanitized
	 * @return sanitizedString
	 */
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

	
	
}
