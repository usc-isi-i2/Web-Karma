package edu.isi.karma.semantictypes.evaluation;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author pranav and aditi
 * @date 11th June 2015
 */

public class ReadEvaluatedFile {
	
	private JSONObject allObjects;
	private double accuracy;
	private double mrr;
	private JSONArray columns;
	
	public ReadEvaluatedFile(String filename) {
		JSONParser parser = new JSONParser();
		try {
			allObjects = (JSONObject)parser.parse(new FileReader(filename));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		accuracy = (double)allObjects.get(EvaluatedJSONLabels.ACCURACY_NAME);
		mrr = (double)allObjects.get(EvaluatedJSONLabels.MRR_NAME);
		columns = (JSONArray)allObjects.get(EvaluatedJSONLabels.COLUMNS_NAME);
	}
	
	public JSONObject getAllObjects(){return allObjects;}
	public double getAccuracy(){return accuracy;}
	public double getMRR(){return mrr;}
	public JSONArray getColumnArray(){return columns;}
	public double getReciprocalRank(JSONObject columnObject){return (double)columnObject.get(EvaluatedJSONLabels.RECIPROCAL_RANK_NAME);}
	public boolean isFound(JSONObject columnObject){
		String val = (String)columnObject.get(EvaluatedJSONLabels.FOUND_NAME);
		return val.equals("true");		
	}
	
	public static void main(String[] args){
		/*
		String dirName = "/home/pranav/workspace_karma2/karma-evaluate/evaluation-results/";
		String filename = "cbev2.WebConAltNames.csv.model.MRR.json";
		String file = dirName + filename;
		
		
		ReadEvaluatedFile r1 = new ReadEvaluatedFile(file);
		
		JSONObject o1 = r1.getAllObjects();
		JSONArray a1 = r1.getColumnArray();
		double acc = r1.getAccuracy();
		double mrr = r1.getMRR();
		
		
		
		System.out.println(o1 + "\n" + a1 + "\n" + acc + "\n" + mrr);
		
		JSONObject c1 = (JSONObject)a1.get(0);
		System.out.println();
		System.out.println(c1);
		JSONObject c11 = (JSONObject)c1.get("PersonInstitutionURI");
		System.out.println(c11);
		double rr = r1.getReciprocalRank(c11);
		boolean found = r1.isFound(c11);
		System.out.println();
		//System.out.println(found);
		System.out.println(rr +"\n" + found);
		*/
		
		
	}
	
}
