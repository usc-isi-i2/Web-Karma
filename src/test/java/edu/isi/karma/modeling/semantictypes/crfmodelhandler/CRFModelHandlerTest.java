package edu.isi.karma.modeling.semantictypes.crfmodelhandler;

import java.util.ArrayList;
import java.util.Date;
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
	
	
	
	
	private void testTrain() throws Exception {
		DBTable dbTable;
		ArrayList<String> dataFileNames, colValues, selectedColValues, columnLabels, allLabels, valueList;
		ArrayList<ArrayList<String>> columns;
		ArrayList<Double> sums, sumTotals;
		final String modelFileName = "/Users/amangoel/Desktop/crfmodel.txt";
		final String dataDir = "/data/biodb/";
		colValues = new ArrayList<String>();
		selectedColValues = new ArrayList<String>();
		columnLabels = new ArrayList<String>();
		allLabels = new ArrayList<String>();
		valueList = new ArrayList<String>();
		columns = new ArrayList<ArrayList<String>>();
		sums = new ArrayList<Double>();
		sumTotals = new ArrayList<Double>();
		CRFModelHandler.readModelFromFile(modelFileName);
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
				selectValues(colValues, selectedColValues);
				Prnt.prn(new Date() + ": Adding column: " + colIndex + " with label: " + label + " from file " + dataFile);
				CRFModelHandler.addOrUpdateLabel(label, selectedColValues, null);
				columns.add(new ArrayList<String>(selectedColValues));
				columnLabels.add(label);
			}
		}
		CRFModelHandler.getLabels(allLabels);
		for(int index=0;index<allLabels.size();index++) {
			String label;
			label = allLabels.get(index);
			CRFModelHandler.getExamplesForLabel(label, valueList);
			sumTotals.add(sumOfFFSumForLabel(valueList, label));
			if (valueList.size() > 0) {
				sumTotals.set(index, sumTotals.get(index)/valueList.size());
			}
			else {
				sumTotals.set(index, -12345678.0);
			}
			Prnt.prn("Average FFSum for label = " + label + " = " + sumTotals.get(index));
		}
		labelColumns(columns, columnLabels, 1, sumTotals);
		Prnt.prn("");
		labelColumns(columns, columnLabels, 2, sumTotals);
		Prnt.prn("");
		labelColumns(columns, columnLabels, 3, sumTotals);
		Prnt.prn("Done CRFModelHandlerTest.");
	}
	
	
	
	private void testLabel() throws Exception {
		DBTable dbTable;
		ArrayList<String> dataFileNames, colValues, selectedColValues, predictedLabels;
		ArrayList<Double> confidenceScores;
		int total, correct;
		final String modelFileName = "/Users/amangoel/Desktop/crfmodel.txt";
		final String dataDir = "/data/biodb/";
		colValues = new ArrayList<String>();
		predictedLabels = new ArrayList<String>();
		selectedColValues = new ArrayList<String>();
		confidenceScores = new ArrayList<Double>();
		CRFModelHandler.readModelFromFile(modelFileName);
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
				CRFModelHandler.predictLabelForExamples(selectedColValues, 4, predictedLabels, confidenceScores, null, null);
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
	
	
	private static void labelColumns(ArrayList<ArrayList<String>> columns, ArrayList<String> columnLabels, int topN, ArrayList<Double> avgSumFFs) {
		ArrayList<String> predictedLabels, allLabels;
		ArrayList<Double> confidenceScores, sums;
		ArrayList<double[]> exampleConfidenceScores;
		int correct, total;
		correct = total = 0 ;
		predictedLabels = new ArrayList<String>();
		allLabels = new ArrayList<String>();
		confidenceScores = new ArrayList<Double>();
		sums = new ArrayList<Double>();
		exampleConfidenceScores = new ArrayList<double[]>();
		CRFModelHandler.getLabels(allLabels);
		for(int index=0;index<columns.size();index++) {
			ArrayList<String> column;
			String label, topPredictedLabel;
			double avgModel, avgCol;
			column = columns.get(index);
			label = columnLabels.get(index);
			CRFModelHandler.predictLabelForExamples(column, 4, predictedLabels, confidenceScores, exampleConfidenceScores, null);
			topPredictedLabel = predictedLabels.get(0);
			Prnt.prn("Prediction for column " + index + " with true label " + label + " --- " + predictedLabels);
			avgModel = avgSumFFs.get(allLabels.indexOf(topPredictedLabel)) ;
			avgCol = sumOfFFSumForLabel(column, topPredictedLabel);
			if (column.size() > 0) {
				avgCol/= column.size();
			}
			else {
				avgCol = -12345678.0;
			}
			Prnt.prn("Avg SumFFs in model for label " + topPredictedLabel + " = " + avgModel + " and Avg SumFFs for this column = " + avgCol + " and diff = " + (avgCol - avgModel)); 
			total++;
			if (predictedLabels.indexOf(label) >= 0 && predictedLabels.indexOf(label) < topN) {
				correct++;
				Prnt.prn("Correct\n");
			}
			else {
				Prnt.prn("Wrong");
				Prnt.prn("Column prediction confidence scores = " + confidenceScores);
				for(int exampleIndex=0;exampleIndex<column.size();exampleIndex++) {
					String line = "";
					line = "For " + column.get(exampleIndex) + " confidence scores are = ";
					for(double d : exampleConfidenceScores.get(exampleIndex)) {
						line+=(d + " ");
					}
					line+="\n";
					CRFModelHandler.getWeightedFeatureFunctionSums(column.get(exampleIndex), null, sums);
					line += "For " + column.get(exampleIndex) + " ff sums = ";
					for(String predictedLabel : predictedLabels) {
						line += (sums.get(allLabels.indexOf(predictedLabel)) + " ");
					}
					line+="\n";
					Prnt.prn(line);
				}
				Prnt.prn("\n");
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
			CRFModelHandler.getWeightedFeatureFunctionSums(value, null, sums);
			sum+=sums.get(labelIndex);
		}
		return sum;
	}
	
	
	
}