package edu.isi.karma.cleaning.correctness;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.tuple.Pair;

import edu.isi.karma.cleaning.DataPreProcessor;
import edu.isi.karma.cleaning.DataRecord;
import edu.isi.karma.cleaning.UtilTools;

public class InspectorUtil {
	public static final int input_output = 1;
	public static final int input = 2;
	public static final int output = 3;
	public static String genKey(DataRecord record, int option){
		if(option == input_output)
			return record.origin + " " + record.transformed;
		else if(option == input){
			return record.origin;
		}
		else if(option == output){
			return record.transformed;
		}
		else{
			return "";
		}
	}
	public static double[] getMeanVector(DataPreProcessor dpp, ArrayList<DataRecord> records, int targetContent){
		ArrayList<double[]> classVectors = new ArrayList<double[]>();
		for(DataRecord r: records){
			String dr = genKey(r, targetContent);
			classVectors.add(dpp.getFeatureArray(dr));
		}
		double[] tmean = UtilTools.sum(classVectors);
		tmean = UtilTools.produce(1.0/records.size(), tmean);
		return tmean;
	}
	public static double getDistance(DataPreProcessor dpp, DataRecord rec, double[] reference, double[] weight, int targetContent){
		String dr = genKey(rec, targetContent);
		double[] self = dpp.getFeatureArray(dr);
		double ret = UtilTools.distance(self, reference, weight);
		return ret;		
	}
	public static Pair<double[], double[]> getMeanVectorandStdevDistance(DataPreProcessor dpp, ArrayList<DataRecord> records, int targetContent){
		ArrayList<double[]> classVectors = new ArrayList<double[]>();
		for(DataRecord r: records){
			String dr = genKey(r, targetContent);
			classVectors.add(dpp.getFeatureArray(dr));
		}
		double[] tmean = UtilTools.sum(classVectors);
		tmean = UtilTools.produce(1.0/records.size(), tmean);
		
		double d_mu = 0;
		double[] alldists = new double[records.size()];
		for(int i =0; i< records.size(); i++)
		{
			alldists[i]= UtilTools.distance(dpp.getFeatureArray(genKey(records.get(i), targetContent)), tmean, null);
		}
		Arrays.sort(alldists);
		double d_median = alldists.length > 0 ? alldists[alldists.length/2] : 0;
		for(int i =0; i< records.size(); i++)
		{
			d_mu += Math.pow(UtilTools.distance(dpp.getFeatureArray(genKey(records.get(i), targetContent)), tmean, null)-d_median, 2);
		}
		d_mu = Math.sqrt(d_mu/records.size());
		double[] x = {d_median, d_mu};
		Pair<double[], double[]> ret = Pair.of(x, tmean);
		return ret;
	}
	
}
