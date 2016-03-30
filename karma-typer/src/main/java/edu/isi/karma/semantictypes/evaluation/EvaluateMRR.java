package edu.isi.karma.semantictypes.evaluation;

/**
 * 
 * 
 * 
 * @author aditi and pranav
 * @Date 6th June 2015
 * @AIM:Evaluation code for models published in karma using Mean Reciprocal Rank Metric and Accuracy
 */



import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EvaluateMRR {

	//	private final static String INPUT_DIR_NAME="/home/v/karma-dev-home/models-json/"; //path to models-json in karma home
	//	private final static String OUTPUT_DIR_NAME="evaluation-results";

	public static MRRItem calculateMRRValue(String inputFile, Integer numberOfCandidates){

		JSONParser parser = new JSONParser(); 

		int noOfAttributes=0; // No of columns in data source
		double sumRR=0; // Sum of Reciprocal ranks of all columns in data source
		int correctHits=0;



		try {
			//String tokens[]=inputFile.split("/");
			//String fileName= tokens[tokens.length -1];

			//FileWriter file = new FileWriter(outputDir+"//"+fileName.split(".json",2)[0]+".MRR"+".json");

			JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(inputFile));
			JSONObject graph = (JSONObject) jsonObject.get("graph"); 
			JSONArray jsonArray = (JSONArray) graph.get("nodes"); 

			for (Object o: jsonArray){

				JSONObject obj= (JSONObject) o;
				// Reading column attribute nodes from the JSON Object
				if(obj.get("type").equals("ColumnNode")){

					noOfAttributes++;

					// Reading correct Semantic labels and storing in an array list 
					JSONArray userArray = (JSONArray) obj.get("userSemanticTypes");
					List<String> correctTypes= new ArrayList<>();

					for(Object o1: userArray){
						JSONObject userObj= (JSONObject) o1;
						correctTypes.add(userObj.get("domain").toString() + userObj.get("type").toString());
					}

					// Reading learned Semantic labels and storing in an array list 
					JSONArray learnedArray = (JSONArray) obj.get("learnedSemanticTypes");
					List<String> learnedTypes= new ArrayList<>();

					if (learnedArray != null)
					for(Object o2: learnedArray){
						JSONObject learnedObj= (JSONObject) o2;
						learnedTypes.add(learnedObj.get("domain").toString() + learnedObj.get("type").toString()); 
					}

					int rank=1; // rank of correct semantic type in the learned semantic labels ordered list
					boolean attributeFound=false;
					int count = 0;
					// Calculating reciprocal rank for each column attribute
					for(String correctLabel :correctTypes ){
						attributeFound=false;

						for(String suggestedLabel: learnedTypes){

							if (numberOfCandidates != null && count == numberOfCandidates.intValue())
								break;
							
							count ++;
							if(correctLabel.equals(suggestedLabel)){


								attributeFound=true;
								sumRR+=1/(double)rank;
								if(rank==1){
									correctHits++;
								}
								break;
							}
							rank++;

						}
						if(attributeFound==false){

							sumRR+=0;
						}
					}
				}


			}	
			// Calculating MRR 

			double accuracy = (double)correctHits/noOfAttributes;
			double mrr = (double)sumRR/noOfAttributes;
			MRRItem mrrItem = new MRRItem(accuracy, mrr);
			return mrrItem;


		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}

	}


	@SuppressWarnings("unchecked")
	public static JSONObject calculateMRR(String inputFile){

		JSONObject printObj=new JSONObject();
		JSONParser parser = new JSONParser(); 

		int noOfAttributes=0; // No of columns in data source
		double sumRR=0; // Sum of Reciprocal ranks of all columns in data source
		int correctHits=0;



		try {
			//String tokens[]=inputFile.split("/");
			//String fileName= tokens[tokens.length -1];

			//FileWriter file = new FileWriter(outputDir+"//"+fileName.split(".json",2)[0]+".MRR"+".json");

			JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(inputFile));
			JSONObject graph = (JSONObject) jsonObject.get("graph"); 
			JSONArray jsonArray = (JSONArray) graph.get("nodes"); 

			JSONArray columns = new JSONArray();


			for (Object o: jsonArray){

				JSONObject obj= (JSONObject) o;
				JSONObject columnNode= new JSONObject();
				JSONObject columnNameNode= new JSONObject();
				// Reading column attribute nodes from the JSON Object
				if(obj.get("type").equals("ColumnNode")){

					noOfAttributes++;

					// Reading correct Semantic labels and storing in an array list 
					JSONArray userArray = (JSONArray) obj.get("userSemanticTypes");
					List<String> correctTypes= new ArrayList<>();

					for(Object o1: userArray){
						JSONObject userObj= (JSONObject) o1;
						correctTypes.add(userObj.get("domain").toString() + userObj.get("type").toString());
					}

					// Reading learned Semantic labels and storing in an array list 
					JSONArray learnedArray = (JSONArray) obj.get("learnedSemanticTypes");
					List<String> learnedTypes= new ArrayList<>();

					for(Object o2: learnedArray){
						JSONObject learnedObj= (JSONObject) o2;
						learnedTypes.add(learnedObj.get("domain").toString() + learnedObj.get("type").toString()); 
					}

					int rank=1; // rank of correct semantic type in the learned semantic labels ordered list
					boolean attributeFound=false;
					// Calculating reciprocal rank for each column attribute
					for(String correctLabel :correctTypes ){
						attributeFound=false;

						for(String suggestedLabel: learnedTypes){

							if(correctLabel.equals(suggestedLabel)){


								columnNameNode.put(EvaluatedJSONLabels.FOUND_NAME,"true");
								columnNameNode.put(EvaluatedJSONLabels.RECIPROCAL_RANK_NAME,new Double((double)1/rank));
								columnNode.put(obj.get("columnName"),columnNameNode);

								columns.add(columnNode);

								attributeFound=true;
								sumRR+=1/(double)rank;
								if(rank==1){
									correctHits++;
								}
								break;
							}
							rank++;

						}
						if(attributeFound==false){

							columnNameNode.put(EvaluatedJSONLabels.FOUND_NAME,"false");
							columnNameNode.put(EvaluatedJSONLabels.RECIPROCAL_RANK_NAME,new Double(0));
							columnNode.put(obj.get("columnName"),columnNameNode);

							columns.add(columnNode);

							sumRR+=0;
						}


					}
				}


			}	
			// Calculating MRR 

			printObj.put(EvaluatedJSONLabels.COLUMNS_NAME, columns);
			printObj.put(EvaluatedJSONLabels.MRR_NAME, (double)sumRR/noOfAttributes);
			printObj.put(EvaluatedJSONLabels.ACCURACY_NAME, (double)correctHits/noOfAttributes);



		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return printObj;  


	}

	public static void printEvaluatedJSON(String inputFileName,String outputFileName){
		JSONObject obtainedObject = calculateMRR(inputFileName);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(obtainedObject);

		try {
			PrintWriter fw = new PrintWriter(outputFileName);
			fw.write(json);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	public static void printAllEvaluatedJSON(File inputDir,File outputDir){
		String[] inputFileNameList = inputDir.list();
		String extension = ".mrr.json";
		for(String inputFileName : inputFileNameList){
			printEvaluatedJSON(inputDir.getAbsolutePath() + inputFileName, outputDir.getAbsolutePath() + inputFileName + extension);
		}
	}

	public static void main(String[] args) {

		/*
		 * For evaluating the json file after model is published
		 * Call the function calculateMRR after the 'PUBLISH MODEL' command 
		 * passing the inputFile path and Output Directory path as parameters to the function
		  Example:

		  'evaluation-results' must be an existing folder in the workspace  

		 */

		/*
		 * Call for Single File
		 * 
		 * 
		String inputFileName = "cbev2.WebArtistBio.csv.model.json";
		String file = EvaluateMRR.INPUT_DIR_NAME + inputFileName;
		printEvaluatedJSON(file, EvaluateMRR.OUTPUT_DIR_NAME);
		 */

		/*
		 * Call for Entire Directory
		 */

		//		printAllEvaluatedJSON(EvaluateMRR.INPUT_DIR_NAME, EvaluateMRR.OUTPUT_DIR_NAME);




	}

}