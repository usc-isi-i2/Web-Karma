package edu.isi.karma.semantictypes.typinghandler;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.semantictypes.ISemanticTypeModelHandler;
import edu.isi.karma.modeling.semantictypes.SemanticTypeLabel;
import edu.isi.karma.semantictypes.numeric.KSTest;
import edu.isi.karma.semantictypes.tfIdf.Indexer;
import edu.isi.karma.semantictypes.tfIdf.Searcher;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

/**
 * LATEST
 * 
 * This is the API class for the semantic typing module, implementing the
 * combined approach of TF-IDF based cosine similarity and Kolmogorov-Smirnov
 * test approaches for textual and numeric respectively by 
 * Ramnandan.S.K and Amol Mittal.
 * 
 * @author ramnandan
 * 
 */

public class HybridSTModelHandler implements ISemanticTypeModelHandler {

	static Logger logger = LoggerFactory
			.getLogger(HybridSTModelHandler.class.getSimpleName());
	private ArrayList<String> allowedCharacters;

	private boolean modelEnabled = false;
	private String contextId;

	/*
	 * While training,
	 * 	if numericCount >= pureNumeric -> train as NUMERIC COLUMN
	 * 	else if numericCount <= pureText -> train as TEXTUAL COLUMN
	 * 	else (pureText<numericCount<pureNumeric) -> train as NUMERIC as well as TEXTUAL 
	 * 
	 * While testing,
	 * 	if numericCount >= pureNumeric -> test as NUMERIC COLUMN
	 * 	else (numericCount<pureNumeric) -> test as TEXTUAL COLUMN
	 */
	private double trainNumericThreshold = 0.8;
	private double trainTextualThreshold = 0.6;
	private double testThreshold = 0.7;
	
	private String numericRegEx = "((\\-)?[0-9]{1,3}(,[0-9]{3})+(\\.[0-9]+)?)|((\\-)?[0-9]*\\.[0-9]+)|((\\-)?[0-9]+)|((\\-)?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)";
	
	public HybridSTModelHandler(String contextId) {
		allowedCharacters = allowedCharacters();
		this.contextId = contextId;
	}

	/**
	 * Adds the passed list of examples for training
	 * 
	 * @param label
	 *            True label for the list of example.
	 * @param examples
	 *            List of example strings.
	 * @return True if success, else False
	 */
	@Override
	public synchronized boolean addType(String label, List<String> examples) {
		boolean savingSuccessful = false;
		int countNumeric = 0;
		
		// running basic sanity checks in the input arguments
		if (label == null || label.trim().length() == 0 || examples.isEmpty()) {
			logger.warn("@label argument cannot be null or an empty string and the @examples list cannot be empty.");
			return false;
		}
		
		label = label.trim();
		ArrayList<String> cleanedExamples = new ArrayList<>();
		countNumeric = cleanedExamplesList(examples, cleanedExamples);
		
		// making sure that the condition where the examples list is not empty
		// but contains junk only is not accepted
		if (cleanedExamples.isEmpty()) {
			logger.warn("@examples list contains forbidden characters only. The allowed characters are "
					+ allowedCharacters);
			return false;
		}

		try {
			savingSuccessful = indexTrainingColumn(label, cleanedExamples, countNumeric);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return savingSuccessful;
	}

	/**
	 * Indexes the given training column for a specific label
	 * 
	 * @param label
	 * @param selectedExamples
	 * @param countNumeric
	 * @return
	 * @throws IOException
	 */
	
	
	private boolean indexTrainingColumn(String label,
			List<String> selectedExamples, int countNumeric) throws IOException {
		/**
		 * @patch applied
		 * @author pranav and aditi
		 * @date 12th June 2015
		 * 
		 * 
		 */
	
		/**
		 * TODO: Currently training exclusively as textual or numeric based on 0.7, 
		 * 		not as both if between 0.6 & 0.8
		 */
		// check if textual or numeric
		boolean isNumeric = false;
		double fractionNumeric = ((double)(countNumeric+0.0))/((double)(selectedExamples.size()+0.0));
		if (fractionNumeric >= testThreshold) {
			isNumeric = true;
		}
		
		logger.warn("-----------------------------------------------------------------------------");
		// if numeric, clean examples to remove text
		if (isNumeric) {
			selectedExamples = cleanExamplesNumeric(selectedExamples);
			logger.warn("Training Label "+label+" classified as numeric - fractionNumeric = "+fractionNumeric);
		}
		else {
			logger.warn("Training Label "+label+" classified as textual - fractionNumeric = "+fractionNumeric);
		}
		logger.warn("-----------------------------------------------------------------------------");
		
		// treat content of column as single document
		StringBuilder sb = new StringBuilder();
		for (String ex : selectedExamples) {
			sb.append(ex);
			sb.append(" ");
		}
		
		// check if semantic label already exists
		Document labelDoc = null; // document corresponding to existing semantic label if exists
		if (indexDirectoryExists(isNumeric)) {
			try {
				// check if semantic label already exists in index
				Searcher searcher = new Searcher(getIndexDirectory(isNumeric),
						Indexer.LABEL_FIELD_NAME);
				try {
					labelDoc = searcher.getDocumentForLabel(label);
				} finally {
					searcher.close();
				}
			} catch (Exception e) {
				// Ignore, the searcher might not work if index is empty.
			}
		}

		// index the document
		Indexer indexer = new Indexer(getIndexDirectory(isNumeric));
		try {
			indexer.open();
			if (labelDoc != null) {
				IndexableField[] existingContent = labelDoc.getFields(Indexer.CONTENT_FIELD_NAME);
				indexer.updateDocument(existingContent, sb.toString(), label);
			} else {
				indexer.addDocument(sb.toString(), label);
			}
			indexer.commit();
		} finally {
			indexer.close();
		}

		return true;
	}

	/**
	 * Check if index directory exists and contains files
	 * 
	 * @return
	 */
	private boolean indexDirectoryExists(boolean isNumeric) {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(contextId);
		String indexDirectory;
		if (isNumeric) {
			indexDirectory = contextParameters
					.getParameterValue(ContextParameter.NUMERIC_SEMTYPE_MODEL_DIRECTORY);
		}
		else {
			indexDirectory = contextParameters
					.getParameterValue(ContextParameter.TEXTUAL_SEMTYPE_MODEL_DIRECTORY);			
		}
		File dir = new File(indexDirectory);

		if (dir.exists() && dir.listFiles().length > 0) {
			String[] files = dir.list();
			for (String file : files) {
				if (file.equals("segments.gen"))
					return true;
			}
		}
		return false;
	}

	/**
	 * @param examples
	 *            - list of examples of an unknown type
	 * @param numPredictions
	 *            - required number of predictions in descending order
	 * @param predictedLabels
	 *            - the argument in which the ordered list of labels is
	 *            returned. the size of this list could be smaller than
	 *            numPredictions if there aren't that many labels in the model
	 *            already
	 * @param confidenceScores
	 *            - the probability of the examples belonging to the labels
	 *            returned.
	 * @param exampleProbabilities
	 *            - the size() == examples.size(). It contains, for each
	 *            example, in the same order, a double array that contains the
	 *            probability of belonging to the labels returned in
	 *            predictedLabels.
	 * @param columnFeatures
	 *            - this Map supplies ColumnFeatures such as ColumnName, etc.
	 * @return True, if successful, else False
	 */
	@Override
	public List<SemanticTypeLabel> predictType(List<String> examples,
			int numPredictions) {

		if (!this.modelEnabled) {
			logger.warn("Semantic Type Modeling is not enabled");
			return null;
		}

		// Sanity checks for arguments
		if (examples == null || examples.isEmpty() || numPredictions <= 0) {
			logger.warn("Invalid arguments. Possible problems: examples list size is zero, numPredictions is non-positive");
			return null;
		}

		logger.debug("Predic Type for " + examples.toArray().toString());

		logger.warn("-----------------------------------------------------------------------------");
		// decide if test column is textual or numeric
		boolean isNumeric = false;
		int countNumeric = 0;
		
		for (String example: examples) {
			if (example.matches(numericRegEx)) {
				countNumeric++;
			}			
		}
		double fractionNumeric = ((double)(countNumeric+0.0))/((double)(examples.size()+0.0));
		if (fractionNumeric >= testThreshold) {
			isNumeric = true;
			logger.warn("Test Label classified as numeric - fractionNumeric = "+fractionNumeric);
		}
		else {
			logger.warn("Test Label classified as textual - fractionNumeric = "+fractionNumeric);
		}
	
		// get top-k suggestions
		if (isNumeric) { // numeric test column
			if (indexDirectoryExists(isNumeric)) {
				KSTest test = new KSTest();
				logger.warn("KS test called");
				
				// extract distributions for each trained semantic label
				Map<String, List<Double>> trainingLabelToExamplesMap = new HashMap<>();
				try {
					IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(
							getIndexDirectory(isNumeric))));
					try {
						for (int i=0; i<reader.maxDoc(); i++) {
						    Document doc = reader.document(i);
						    String label = doc.get(Indexer.LABEL_FIELD_NAME);
						    List<Double> exampleList = new ArrayList<>(); 
						    String content = doc.get(Indexer.CONTENT_FIELD_NAME);
						    for (String example: content.split(" ")) {
						    	try {
									Number exampleNum = NumberFormat.getNumberInstance(java.util.Locale.US).parse(example);
									exampleList.add(exampleNum.doubleValue());
								} catch (ParseException e) {
									logger.warn("Could not add example:" + example + " for training");
								}
						    }
						    trainingLabelToExamplesMap.put(label, exampleList);
						}
					} finally {
						reader.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}				

				// extract test column distribution
				List<Double> testExamples = new ArrayList<>();
				for (String example: examples) {
					if(example.matches(numericRegEx)) {
						try {
							Number exampleNum = NumberFormat.getNumberInstance(java.util.Locale.US).parse(example);
							testExamples.add(exampleNum.doubleValue());
						} catch (ParseException e) {
							logger.warn("Could not add example:" + example + " for training");
						}
					}
				}				

				List<SemanticTypeLabel> result = test.predictLabelsForColumn(numPredictions, trainingLabelToExamplesMap, testExamples);
				logger.debug("Got " + result.size() + " predictions");
				return result;
			}			
		}
		else { // textual test column
			if (indexDirectoryExists(isNumeric)) {
				
				logger.warn("TF-IDF cosine similarity called");
				// construct single text for test column
				StringBuilder sb = new StringBuilder();
				for (String ex : examples) {
					sb.append(ex);
					sb.append(" ");
				}
				
				try {
					Searcher predictor = new Searcher(getIndexDirectory(isNumeric),
							Indexer.CONTENT_FIELD_NAME);
					try {
						List<SemanticTypeLabel> result = predictor.getTopK(numPredictions, sb.toString());
						logger.debug("Got " + result.size() + " predictions");
						return result;
					} finally {
						predictor.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		logger.warn("-----------------------------------------------------------------------------");

		return null;
	}

	/**
	 * @return True if successfully cleared the model. False, otherwise. This
	 *         method removes all labels from the model.
	 * 
	 *         Currently, when only TF-IDF is used, equivalent to deleting all
	 *         documents
	 */
	@Override
	public boolean removeAllLabels() {
		try {
			Indexer indexer = new Indexer(getIndexDirectory(true)); // remove numeric labels
			try {
				indexer.open();
				indexer.deleteAllDocuments();
				indexer.commit();
			} finally {
				indexer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			Indexer indexer = new Indexer(getIndexDirectory(false)); // remove textual labels
			try {
				indexer.open();
				indexer.deleteAllDocuments();
				indexer.commit();
			} finally {
				indexer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	public String getIndexDirectory(boolean isNumeric)
	{
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(contextId);
		String indexDirectory;
		if (isNumeric) {
			indexDirectory = contextParameters
					.getParameterValue(ContextParameter.NUMERIC_SEMTYPE_MODEL_DIRECTORY);
		}
		else {
			indexDirectory = contextParameters
					.getParameterValue(ContextParameter.TEXTUAL_SEMTYPE_MODEL_DIRECTORY);			
		}
		return indexDirectory;
	}
	/**
	 * @param uncleanList
	 *            List of all examples
	 * @param cleanedList
	 *            List with examples that don't have unallowed chars and others
	 *            such as nulls or empty strings This method cleans the examples
	 *            list passed to it. Generally, it is used by other methods to
	 *            sanitize lists passed from outside.
	 * @return countNumeric            
	 */
	private int cleanedExamplesList(List<String> uncleanList,
			List<String> cleanedList) {
		int countNumeric = 0;
		cleanedList.clear();
		for (String example : uncleanList) {
			if (example != null) {
				String trimmedExample;
				trimmedExample = getSanitizedString(example);
				if (trimmedExample.length() != 0) {
					cleanedList.add(trimmedExample);
					if (trimmedExample.matches(numericRegEx)) {
						countNumeric++;
					}
				}
			}
		}
		return countNumeric;
	}

	/**
	 * @param unsanitizedString
	 *            String to be sanitized
	 * @return sanitizedString
	 */
	private String getSanitizedString(String unsanitizedString) {
		String sanitizedString;
		sanitizedString = "";
		for (int i = 0; i < unsanitizedString.length(); i++) {
			String charAtIndex;
			charAtIndex = unsanitizedString.substring(i, i + 1);
			if (allowedCharacters.contains(charAtIndex)) {
				sanitizedString += charAtIndex;
			}
		}
		return sanitizedString;
	}

	/**
	 * @return Returns list of allowed Characters
	 */
	private ArrayList<String> allowedCharacters() {
		ArrayList<String> allowed = new ArrayList<>();
		// Adding A-Z
		for (int c = 65; c <= 90; c++) {
			allowed.add(String.valueOf((char) c));
		}
		// Adding a-z
		for (int c = 97; c <= 122; c++) {
			allowed.add(String.valueOf((char) c));
		}
		// Adding 0-9
		for (int c = 48; c <= 57; c++) {
			allowed.add(String.valueOf((char) c));
		}
		allowed.add(" "); // adding space
		allowed.add("."); // adding dot
		allowed.add("%");
		allowed.add("@");
		allowed.add("_");
		allowed.add("-");
		allowed.add("*");
		allowed.add("(");
		allowed.add(")");
		allowed.add("[");
		allowed.add("]");
		allowed.add("+");
		allowed.add("/");
		allowed.add("&");
		allowed.add(":");
		allowed.add(",");
		allowed.add(";");
		allowed.add("?");
		return allowed;
	}

	public List<String> cleanExamplesNumeric(List<String> exampleList)
	{
		List<String> cleanedExamples = new ArrayList<>();
		Iterator<String> itr = exampleList.iterator();
		while(itr.hasNext()) {
			String ex = itr.next();
			if(ex.matches(numericRegEx)) {
				cleanedExamples.add(ex);
			}
		}
		return cleanedExamples;
	}
	
	@Override
	public boolean readModelFromFile(String filepath, boolean isNumeric) {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(contextId);
		if (isNumeric) {
			contextParameters
			.setParameterValue(ContextParameter.NUMERIC_SEMTYPE_MODEL_DIRECTORY, filepath);			
		}
		else {
			contextParameters
			.setParameterValue(ContextParameter.TEXTUAL_SEMTYPE_MODEL_DIRECTORY, filepath);				
		}
		return true;
	}

	@Override
	public void setModelHandlerEnabled(boolean enabled) {
		this.modelEnabled = enabled;

	}

	@Override
	public boolean readModelFromFile(String filepath) {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(contextId);
		contextParameters
			.setParameterValue(ContextParameter.SEMTYPE_MODEL_DIRECTORY, filepath);			
		return true;
	}

}
