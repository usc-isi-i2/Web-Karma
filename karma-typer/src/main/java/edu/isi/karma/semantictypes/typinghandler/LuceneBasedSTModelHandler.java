package edu.isi.karma.semantictypes.typinghandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.semantictypes.ISemanticTypeModelHandler;
import edu.isi.karma.modeling.semantictypes.SemanticTypeLabel;
import edu.isi.karma.modeling.semantictypes.myutils.Prnt;
import edu.isi.karma.semantictypes.tfIdf.Indexer;
import edu.isi.karma.semantictypes.tfIdf.Searcher;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

/**
 * This is the API class for the semantic typing module, implementing the combined approach of 
 * TF-IDF based cosine similarity and Kolmogorov-Smirnov test approaches for textual and numeric 
 * respectively by Ramnandan.S.K and Amol Mittal.
 * 
 * @author ramnandan
 *
 */

public class LuceneBasedSTModelHandler implements ISemanticTypeModelHandler {

	/**
	 * This internal class represents an example.
	 */
	static class Example {
		String exampleString;

		/**
		 * @param exampleString The string that the example represents
		 * No ColumnFeatures specified.
		 */
		public Example(String exampleString) {
			this.exampleString = exampleString;
		}


		public String getString() {
			return exampleString;
		}

		
	}
	
	//****************************************************************************************//
	
	// instance variables
	static Logger logger = LoggerFactory.getLogger(LuceneBasedSTModelHandler.class.getSimpleName()) ;
	private ArrayList<String> allowedCharacters;
	private Indexer indexer; // responsible for indexing textual data
	private boolean modelEnabled = false;
	private String indexDirectory;
	
	/**
	 * NOTE: Currently, TF-IDF based approach is used for both textual and numeric data
	 * due to bug in KS test on Apache Commons Math.
	 * 
	 * TODO: Integrate KS test when this bug is resolved : https://issues.apache.org/jira/browse/MATH-1131
	 */
	
	
	public LuceneBasedSTModelHandler()
	{
		indexer = new Indexer(); 
		allowedCharacters = allowedCharacters(); 
		indexDirectory = ServletContextParameterMap.getParameterValue(ContextParameter.SEMTYPE_MODEL_DIRECTORY);
		
//		// clear any previous index remaining
//		try {
//			indexer.openIndexWriter(indexDirectory);
//			indexer.deleteDocuments();
//			indexer.commit();
//			indexer.closeIndexWriter();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	/**
	 * Adds the passed list of examples for training 
	 * 
	 * @param label True label for the list of example.
	 * @param examples List of example strings.
	 * @return True if success, else False
	 */
	@Override
	public synchronized boolean addType(String label, List<String> examples) {
		ArrayList<String> cleanedExamples;
		ArrayList<Example> selectedExamples;
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
		
		// adding all the new examples to list of existing examples for the arg label.
		selectedExamples = new ArrayList<Example>();
		for(String newExampleString : examples) {
			Example newExample = new Example(newExampleString);
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
		indexer.openIndexWriter(indexDirectory);
		
		// add documents to index
    	StringBuilder sb = new StringBuilder();
    	for (Example ex : selectedExamples)
    	{
    	    sb.append(ex.getString());
    	    sb.append(" ");
    	}
    	
    	// check if semantic label already exists in index
    	Searcher searcher = new Searcher(indexDirectory,Indexer.LABEL_FIELD_NAME);
    	boolean labelExists = searcher.existsSemanticLabel(label);
    	
    	if(labelExists==true)
    	{
    		indexer.updateDocument(sb.toString(), label);
    	}
    	else
    	{
    		indexer.addDocument(sb.toString(),label);
    	}
		indexer.commit();
		indexer.closeIndexWriter();
	
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
	 * @return True, if successful, else False
	 */
	@Override
	public List<SemanticTypeLabel> predictType(List<String> examples,
			int numPredictions) {

		if(!this.modelEnabled) {
			logger.warn("Semantic Type Modeling is not enabled") ;
			return null;
		}
		
		// Sanity checks for arguments
		if (examples == null || examples.size() == 0 || numPredictions <= 0) {
			logger.warn("Invalid arguments. Possible problems: examples list size is zero, numPredictions is non-positive") ;
			return null;
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
	    	Searcher predictor = new Searcher(indexDirectory,Indexer.CONTENT_FIELD_NAME);
			return predictor.getTopK(numPredictions, sb.toString());
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
	    
		return null;
	}

	/**
	 * @return True if successfully cleared the model. False, otherwise.
	 * This method removes all labels from the model. 
	 * 
	 * Currently, when only TF-IDF is used, equivalent to deleting all documents
	 */
	@Override
	public boolean removeAllLabels() {
		
		try {
			indexer.openIndexWriter(indexDirectory);
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

	@Override
	public boolean readModelFromFile(String filepath) {
		indexDirectory = filepath;
		return true;
	}

	@Override
	public void setModelHandlerEnabled(boolean enabled) {
		this.modelEnabled = enabled;
		
	}

	
	
}
