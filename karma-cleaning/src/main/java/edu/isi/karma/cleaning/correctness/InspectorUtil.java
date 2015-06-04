package edu.isi.karma.cleaning.correctness;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.tuple.Pair;

import edu.isi.karma.cleaning.DataPreProcessor;
import edu.isi.karma.cleaning.DataRecord;
import edu.isi.karma.cleaning.UtilTools;

public class InspectorUtil {
	public static String genKey(DataRecord record){
		return record.origin + " " + record.transformed;
	}
	public static double[] getMeanVector(DataPreProcessor dpp, ArrayList<DataRecord> records){
		ArrayList<double[]> classVectors = new ArrayList<double[]>();
		for(DataRecord r: records){
			String dr = genKey(r);
			classVectors.add(dpp.getFeatureArray(dr));
		}
		double[] tmean = UtilTools.sum(classVectors);
		tmean = UtilTools.produce(1.0/records.size(), tmean);
		return tmean;
	}
	public static double getDistance(DataPreProcessor dpp, DataRecord rec, double[] reference, double[] weight){
		String dr = genKey(rec);
		double[] self = dpp.getFeatureArray(dr);
		double ret = UtilTools.distance(self, reference, weight);
		return ret;		
	}
	public static Pair<Double, double[]> getMeanVectorandStdevDistance(DataPreProcessor dpp, ArrayList<DataRecord> records){
		ArrayList<double[]> classVectors = new ArrayList<double[]>();
		for(DataRecord r: records){
			String dr = genKey(r);
			classVectors.add(dpp.getFeatureArray(dr));
		}
		double[] tmean = UtilTools.sum(classVectors);
		tmean = UtilTools.produce(1.0/records.size(), tmean);
		
		double d_mu = 0;
		double[] alldists = new double[records.size()];
		for(int i =0; i< records.size(); i++)
		{
			alldists[i]= UtilTools.distance(dpp.getFeatureArray(genKey(records.get(i))), tmean, null);
		}
		Arrays.sort(alldists);
		double d_median = alldists.length > 0 ? alldists[alldists.length/2] : 0;
		for(int i =0; i< records.size(); i++)
		{
			d_mu += Math.pow(UtilTools.distance(dpp.getFeatureArray(genKey(records.get(i))), tmean, null)-d_median, 2);
		}
		d_mu = Math.sqrt(d_mu/records.size());
		Pair<Double, double[]> ret = Pair.of(d_mu, tmean);
		return ret;
	}
	
}
