package edu.isi.karma.cleaning.correctness;

import java.util.ArrayList;
import java.util.HashMap;

import edu.isi.karma.cleaning.DataPreProcessor;
import edu.isi.karma.cleaning.DataRecord;
import edu.isi.karma.cleaning.UtilTools;

public class ClasscenterInspector implements Inspector {
	private double[] weights; 
	private HashMap<String, Double> stdevs = new HashMap<String, Double>();
	private HashMap<String, double[]> means = new HashMap<String, double[]>();
	private double scale = 1.8;
	private DataPreProcessor dpp;
	private int targetContent = InspectorUtil.input_output;
	public ArrayList<DataRecord> findExamples(ArrayList<String> expids, ArrayList<DataRecord> all){
		ArrayList<DataRecord> ret = new ArrayList<DataRecord>();
		for(DataRecord r: all){
			if(expids.contains(r.id)){
				ret.add(r);
			}
		}
		return ret;
	}
	public double getStdevdistanceForOneClass(ArrayList<DataRecord> all, DataPreProcessor dpp, String clabel){
		double stdev = Double.MAX_VALUE;
		if(all.size()  == 0){
			return stdev;
		}
		ArrayList<Double> alldists = new ArrayList<Double>();
		double sum = 0.0;		
		for(DataRecord record: all){
			if(record.classLabel.compareTo(clabel) == 0){
				double val = InspectorUtil.getDistance(dpp, record, means.get(clabel), weights, targetContent);
				sum += val;		
				alldists.add(val);
			}
		}
		double valmean = sum * 1.0 / alldists.size();
		double squareDiff = 0.0;
		for(double dist: alldists){
			squareDiff += Math.pow((dist - valmean), 2);
		}
		return Math.sqrt(squareDiff * 1.0 / all.size());
	}
	public ClasscenterInspector(DataPreProcessor dpp, HashMap<String, ArrayList<DataRecord>> expgroups, ArrayList<DataRecord> all, double[] weights, double scale, int option){
		this.weights = weights;
		this.scale = scale;
		this.dpp = dpp;
		this.targetContent = option;
		for(String key: expgroups.keySet()){
			double[] mean = InspectorUtil.getMeanVector(dpp, expgroups.get(key), targetContent);
			means.put(key, mean);
			double stdev = getStdevdistanceForOneClass(expgroups.get(key), dpp, key);
			stdevs.put(key, stdev);
		}
	}
	@Override
	public double getActionLabel(DataRecord record) {
		if(!means.containsKey(record.classLabel)){
			return -1;
		}
		double dist = InspectorUtil.getDistance(dpp, record, means.get(record.classLabel), weights, targetContent);
		if(dist > scale * stdevs.get(record.classLabel)){
			return -1;
		}
		else
		{
			return 1;
		}
	}
	public double getScore(DataRecord record){
		if(!means.containsKey(record.classLabel)){
			return -1;
		}
		double dist = InspectorUtil.getDistance(dpp, record, means.get(record.classLabel), weights, targetContent);
		return -dist;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return String.format("%s|%.1f|%d", this.getClass().getName(), this.scale, this.targetContent);
	}
}
