package edu.isi.karma.cleaning.correctness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;

import edu.isi.karma.cleaning.DataPreProcessor;
import edu.isi.karma.cleaning.DataRecord;
import edu.isi.karma.cleaning.UtilTools;

public class OutlierInspector implements Inspector {

	private HashMap<String, double[]> cmeans = new HashMap<String, double[]>();
	private HashMap<String, double[]> mean_var = new HashMap<String, double[]>();
	private double[] dmetric= null;
	private DataPreProcessor dpp;
	private ArrayList<DataRecord> allrecords = new ArrayList<DataRecord>();
	private double scale = 1.8; // 1.8 time stdev to the mean
	private int targetContent = InspectorUtil.input_output;
	public OutlierInspector(DataPreProcessor dpp, ArrayList<DataRecord> records, double[] dmetric, double scale, int targetContent)
	{
		//dmetric = UtilTools.initArray(dmetric, 1.0);
		this.dmetric = dmetric;
		allrecords = records;
		this.scale = scale;
		this.dpp = dpp;
		this.targetContent = targetContent;
		getMeanandDists(records, dmetric);
	}
	/*private String genKey(DataRecord record){
		return record.origin + " " + record.transformed;
	}*/
	private double getMedian(double[] values){
		if(values.length == 0){
			return 0.0;
		}
		double ret = 0.0;
		if(values.length %2 == 0){
			int ind = values.length /2 -1;
			ret = (values[ind] + values[ind+1]) *1.0/2;
		}
		else{
			int ind = values.length /2 ;
			ret = values[ind];
		}
		return ret;
	}
	//identify the mean vector of each cluster
	private void getMeanandDists(ArrayList<DataRecord> records, double[] dmetric)
	{
		HashMap<String, ArrayList<DataRecord>> tmp = new HashMap<String,ArrayList<DataRecord>>();
		ArrayList<String> allpairs = new ArrayList<String>();
		for(DataRecord rec:records)
		{			
			String pair = InspectorUtil.genKey(rec, targetContent);
			allpairs.add(pair);
			if(tmp.containsKey(rec.classLabel))
			{
				tmp.get(rec.classLabel).add(rec);
			}
			else
			{
				ArrayList<DataRecord> x = new ArrayList<DataRecord>();
				x.add(rec);
				tmp.put(rec.classLabel, x);
			}
		}
		// find the means 
		for(String key: tmp.keySet())
		{
			/*ArrayList<double[]> classVectors = new ArrayList<double[]>();
			ArrayList<DataRecord> tdata = tmp.get(key);
			for(DataRecord dr: tdata){
				String pair = genKey(dr);
				classVectors.add(dpp.getFeatureArray(pair));
			}
			double[] tmean = UtilTools.sum(classVectors);
			tmean = UtilTools.produce(1.0/tdata.size(), tmean);*/
			Pair<double[], double[]> pair = InspectorUtil.getMeanVectorandStdevDistance(dpp, tmp.get(key), targetContent);
			cmeans.put(key, pair.getValue());
			
			/*double d_median = 0;
			double d_mu = 0;
			double[] alldists = new double[tdata.size()];
			for(int i =0; i< tdata.size(); i++)
			{
				alldists[i]= UtilTools.distance(dpp.getFeatureArray(genKey(tdata.get(i))), tmean, dmetric);
			}
			Arrays.sort(alldists);
			d_median = getMedian(alldists);
			for(int i =0; i< tdata.size(); i++)
			{
				d_mu += Math.pow(UtilTools.distance(dpp.getFeatureArray(genKey(tdata.get(i))), tmean, dmetric)-d_median, 2);
			}
			d_mu = Math.sqrt(d_mu/tdata.size());*/
			double[] x = pair.getKey();
			mean_var.put(key, x);	
		}		
	}
	public ArrayList<DataRecord> getAllOutliers(){
		ArrayList<DataRecord> ret = new ArrayList<DataRecord>();
		for(DataRecord record:allrecords){
			if(isoutlier(record)){
				ret.add(record);
			}
		}
		return ret;
	}
	private boolean isoutlier(DataRecord record) {
		double dist = InspectorUtil.getDistance(dpp, record, cmeans.get(record.classLabel), dmetric, targetContent);
		//System.out.println(record.origin+": "+dist +", "+ Arrays.toString(mean_var.get(record.classLabel)));
		if(Math.abs(dist - mean_var.get(record.classLabel)[0]) > scale*mean_var.get(record.classLabel)[1])
		{
			return true;
		}
		//check if it has ambivalent label
		/*double[] alldist = new double[cmeans.keySet().size()];
		int cnt = 0;
		for(String key : cmeans.keySet()){
			alldist[cnt] = UtilTools.distance(vector, cmeans.get(key), dmetric);
			cnt ++;
		}
		Arrays.sort(alldist);
		if(isAmbivalent(alldist)){
			return true;
		}*/
		return false;
	}
	@Override
	public double getActionLabel(DataRecord record) {
		if(isoutlier(record)){
			return -1;
		}
		else{
			return 1;
		}
	}
	@Override
	public String getName() {
		return String.format("%s|%.1f|%d", this.getClass().getName(), scale, targetContent);
	}
}
