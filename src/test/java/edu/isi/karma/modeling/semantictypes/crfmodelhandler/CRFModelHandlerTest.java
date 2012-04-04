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

package edu.isi.karma.modeling.semantictypes.crfmodelhandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import edu.isi.karma.modeling.semantictypes.myutils.DBTable;
import edu.isi.karma.modeling.semantictypes.myutils.FileIOOps;
import edu.isi.karma.modeling.semantictypes.myutils.FileSystemOps;
import edu.isi.karma.modeling.semantictypes.myutils.Prnt;
import edu.isi.karma.modeling.semantictypes.myutils.RandOps;
import junit.framework.TestCase;

public class CRFModelHandlerTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	private void testNewFormat() throws Exception {
		CRFModelHandler crfModelHandler;
		DBTable dbTable;
		ArrayList<String> dataFiles, labels, colValues, selectedColValues, predictedLabels;
		ArrayList<Double> columnProbs;
		boolean success;
		int correct ;
		dataFiles = FileSystemOps.filesFromFolder("/data/biodb/", ".txt");
		dataFiles.removeAll(FileSystemOps.filesFromFolder("/data/biodb/", "-labels.txt"));
		crfModelHandler = new CRFModelHandler();
		success = crfModelHandler.readModelFromFile("/Users/amangoel/Desktop/crfmodel.txt");
		if (!success) {
			Prnt.prn("Reading model file failed.");
		}
		colValues = new ArrayList<String>();
		selectedColValues = new ArrayList<String>();
		predictedLabels = new ArrayList<String>();
		columnProbs = new ArrayList<Double>();
		correct = 0 ;
		for(String dataFile : dataFiles) {
			Prnt.prn("Adding columns from file " + dataFile);
			dbTable = DBTable.readDBTableFromFile(dataFile);
			labels = FileIOOps.allLinesFromFile(dataFile.substring(0, dataFile.length()-4) + "-labels.txt", true);
			for(int colIndex=0;colIndex<dbTable.numColumns();colIndex++) {
				HashMap<CRFModelHandler.ColumnFeature, Collection<String>> columnFeatures;
				ArrayList<String> columnNameCollection;
				dbTable.getColumn(colIndex, colValues);
				if (colValues.size() > 100) {
					RandOps.getRandomlySelectedItemsFromList(colValues, selectedColValues, 100);
				}
				else {
					selectedColValues.clear();
					selectedColValues.addAll(colValues);
				}
				Prnt.prn("Adding column " + colIndex + " with number of examples = " + selectedColValues.size() + " with label " + labels.get(colIndex));
				columnFeatures = new HashMap<CRFModelHandler.ColumnFeature, Collection<String>>();
				columnNameCollection = new ArrayList<String>();
				columnNameCollection.add("ColumnNameIs" + labels.get(colIndex));
				columnFeatures.put(CRFModelHandler.ColumnFeature.ColumnHeaderName, columnNameCollection);
				/*
				success = crfModelHandler.addOrUpdateLabel(labels.get(colIndex), selectedColValues, columnFeatures);
				if (!success) {
					Prnt.prn("Adding column failed.");
				}
				*/
					
				success = crfModelHandler.predictLabelForExamples(selectedColValues, 4, predictedLabels, columnProbs, null, columnFeatures);
				if (!success) {
					Prnt.prn("Predicting failed.");
				}
				else {
					if (predictedLabels.indexOf(labels.get(colIndex)) == 0) {
						Prnt.prn("CORRECT.");
						correct++;
					}
					else {
						Prnt.prn("WRONG.");
					}
				}
				
				
			}
		}
		Prnt.prn("Correct = " + correct);
	}

	
	public void testTrain() throws Exception {
		DBTable dbTable;
		CRFModelHandler crfModelHandler;
		ArrayList<String> dataFileNames, colValues, selectedColValues1, selectedColValues2, columnLabels;
		ArrayList<ArrayList<String>> columns1, columns2;
		final String modelFileName1 = "/Users/amangoel/Desktop/crfmodel1.txt";
		final String modelFileName2 = "/Users/amangoel/Desktop/crfmodel2.txt";
		final String dataDir = "/data/dovetail/";
		crfModelHandler = new CRFModelHandler();
		colValues = new ArrayList<String>();
		selectedColValues1 = new ArrayList<String>();
		selectedColValues2 = new ArrayList<String>();
		columnLabels = new ArrayList<String>();
		columns1 = new ArrayList<ArrayList<String>>();
		columns2 = new ArrayList<ArrayList<String>>();
		dataFileNames = FileSystemOps.filesFromFolder(dataDir, ".txt");
		dataFileNames.removeAll(FileSystemOps.filesFromFolder(dataDir, "-labels.txt"));
		for(String dataFile: dataFileNames) {
			String labelFile;
			ArrayList<String> labels;
			labelFile = dataFile.substring(0, dataFile.length()-4) + "-labels.txt";
			labels = FileIOOps.allLinesFromFile(labelFile, true);
			dbTable = DBTable.readDBTableFromFile(dataFile);
			for(int colIndex=0;colIndex<dbTable.numColumns();colIndex++) {
				String label;
				label = labels.get(colIndex);
				dbTable.getColumn(colIndex, colValues);
				selectValues(colValues, selectedColValues1, selectedColValues2);
				columns1.add(new ArrayList<String>(selectedColValues1));
				columns2.add(new ArrayList<String>(selectedColValues2));
				columnLabels.add(label);
			}
		}
		// 2 fold cross validation
		// 1st fold
		crfModelHandler.readModelFromFile(modelFileName1);
		addColumnsToModel(crfModelHandler, columns1, columnLabels);
		labelColumns(crfModelHandler, columns2, columnLabels, 1);
		Prnt.prn("");
		labelColumns(crfModelHandler, columns2, columnLabels, 2);
		Prnt.prn("");
		labelColumns(crfModelHandler, columns2, columnLabels, 3);
		// 2nd fold
		crfModelHandler.readModelFromFile(modelFileName2);
		addColumnsToModel(crfModelHandler, columns2, columnLabels);
		labelColumns(crfModelHandler, columns1, columnLabels, 1);
		Prnt.prn("");
		labelColumns(crfModelHandler, columns1, columnLabels, 2);
		Prnt.prn("");
		labelColumns(crfModelHandler, columns1, columnLabels, 3);
		Prnt.prn("Done CRFModelHandlerTest.");
	}
	
	
	private static void addColumnsToModel(CRFModelHandler crfModelHandler, ArrayList<ArrayList<String>> columns, ArrayList<String> columnsLabels) {
		for(int i=0;i<columns.size();i++) {
			Prnt.prn(new Date() + ": Adding column: " + i + " with label: " + columnsLabels.get(i) + " and number of values = " + columns.get(i).size());
			crfModelHandler.addOrUpdateLabel(columnsLabels.get(i), columns.get(i), null);
		}
	}
	
	private static void labelColumns(CRFModelHandler crfModelHandler, ArrayList<ArrayList<String>> columns, ArrayList<String> columnLabels, int topN) {
		ArrayList<String> predictedLabels;
		ArrayList<Double> confidenceScores;
		ArrayList<double[]> exampleConfidenceScores;
		int correct, total;
		correct = total = 0 ;
		predictedLabels = new ArrayList<String>();
		confidenceScores = new ArrayList<Double>();
		exampleConfidenceScores = new ArrayList<double[]>();
		for(int index=0;index<columns.size();index++) {
			ArrayList<String> column;
			String label;
			column = columns.get(index);
			label = columnLabels.get(index);
			crfModelHandler.predictLabelForExamples(column, 4, predictedLabels, confidenceScores, exampleConfidenceScores, null);
			Prnt.prn("Prediction for column " + index + " having " + column.size() + " values, with true label " + label + " --- " + predictedLabels);
			total++;
			Prnt.prn("Column prediction confidence scores = " + confidenceScores);
			if (predictedLabels.indexOf(label) >= 0 && predictedLabels.indexOf(label) < topN) {
				correct++;
				Prnt.prn("Correct\n");
			}
			else {
				Prnt.prn("Wrong\n");
			}
		}
		Prnt.prn("Total = " + total);
		Prnt.prn("Correct = " + correct);
		Prnt.prn("Accuracy = " + (correct * 1.0 / total));
	}
	
	private static void selectValues(List<String> dirtyList, List<String> cleanedSelectedList) {
		ArrayList<String> cleanedList;
		cleanedList = new ArrayList<String>();
		cleanedList.clear();
		for(String value : dirtyList) {
			if (value.trim().length() > 0) {
				cleanedList.add(value);
			}
		}
		cleanedSelectedList.clear();
		if (cleanedList.size() > 100) {
			RandOps.getRandomlySelectedItemsFromList(cleanedList, cleanedSelectedList, 100);
		}
		else {
			cleanedSelectedList.addAll(cleanedList);
		}
	}
	
	private static void selectValues(List<String> dirtyList, List<String> cleanedSelectedList1, List<String> cleanedSelectedList2) {
		ArrayList<String> cleanedList;
		cleanedList = new ArrayList<String>();
		for(String value : dirtyList) {
			value = value.trim();
			if (value.length() > 0) {
				cleanedList.add(value);
			}
		}
		cleanedSelectedList1.clear();
		cleanedSelectedList2.clear();
		if (cleanedList.size() > 200) {
			RandOps.getRandomlySelectedItemsFromList(cleanedList, cleanedSelectedList1, 200);
		}
		else {
			cleanedSelectedList1.addAll(cleanedList);
		}
		RandOps.getRandomlySelectedItemsFromList(cleanedSelectedList1, cleanedSelectedList2, cleanedSelectedList1.size()/2);
		for(String str : cleanedSelectedList2) {
			cleanedSelectedList1.remove(str);
		}
	}
	
	
	
	private void testLabel() throws Exception {
		DBTable dbTable;
		CRFModelHandler crfModelHandler;
		ArrayList<String> dataFileNames, colValues, selectedColValues, predictedLabels;
		ArrayList<Double> confidenceScores;
		int total, correct;
		final String modelFileName = "/Users/amangoel/Desktop/crfmodel.txt";
		final String dataDir = "/data/biodb/";
		crfModelHandler = new CRFModelHandler();
		colValues = new ArrayList<String>();
		predictedLabels = new ArrayList<String>();
		selectedColValues = new ArrayList<String>();
		confidenceScores = new ArrayList<Double>();
		crfModelHandler.readModelFromFile(modelFileName);
		dataFileNames = FileSystemOps.filesFromFolder(dataDir, ".txt");
		dataFileNames.removeAll(FileSystemOps.filesFromFolder(dataDir, "-labels.txt"));
		total = correct = 0 ;
		for(String dataFile: dataFileNames) {
			String labelFile;
			ArrayList<String> labels;
			labelFile = dataFile.substring(0, dataFile.length()-4) + "-labels.txt";
			labels = FileIOOps.allLinesFromFile(labelFile, true);
			dbTable = DBTable.readDBTableFromFile(dataFile);
			for(int colIndex=0;colIndex<dbTable.numColumns();colIndex++) {
				String label;
				label = labels.get(colIndex);
				dbTable.getColumn(colIndex, colValues);
				selectValues(colValues, selectedColValues);
				Prnt.prn(new Date() + ": Labeling column: " + colIndex + " with label: " + label + " from file " + dataFile);
				crfModelHandler.predictLabelForExamples(selectedColValues, 4, predictedLabels, confidenceScores, null, null);
				Prnt.prn("Prediction for file " + dataFile + " column " + colIndex + " with true label " + label + " --- " + predictedLabels);
				total++;
				if (predictedLabels.indexOf(label) >= 0 && predictedLabels.indexOf(label) < 3) {
					correct++;
					Prnt.prn("Correct\n");
				}
				else {
					Prnt.prn("Wrong");
					Prnt.prn("\n");
				}
			}
		}
		Prnt.prn("Total = " + total);
		Prnt.prn("Correct = " + correct);
		Prnt.prn("Accuracy = " + (correct * 1.0 / total));
		Prnt.prn("Done CRFModelHandlerTest.");
	}
	
	
	
	
}

/*

private static double sumOfFFSumForLabel(List<String> examples, String label) {
ArrayList<Double> sums;
ArrayList<String> allLabels;
int labelIndex;
double sum;
sums = new ArrayList<Double>();
allLabels = new ArrayList<String>();
CRFModelHandler.getLabels(allLabels);
labelIndex = allLabels.indexOf(label);
sum = 0.0 ;
for(String value : examples) {
	//CRFModelHandler.getWeightedFeatureFunctionSums(value, null, sums); ERROR ERROR ERROR
	sum+=sums.get(labelIndex);
}
return sum;
}

*/