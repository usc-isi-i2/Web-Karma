package edu.isi.karma.cleaning.Correctness;

import java.util.ArrayList;
import java.util.HashMap;

import edu.isi.karma.cleaning.UtilTools;

/*
 * unseen formats detections: 
 * distance to the class center and find the largest distance as the threshold
 * 
 * boundary formats:
 * difference of distances to two classes are below 5% 
 *
 * record = [id, org, tar, label]
 * 
 */

public class FormatFunc implements VerificationFunc {
	private int funid = 1;
	private HashMap<String, double[]> cmeans = new HashMap<String, double[]>();
	private HashMap<String, double[]> mean_var = new HashMap<String, double[]>();
	private double[] dmetric= null;
	public FormatFunc(ArrayList<TransRecord> records, double[] dmetric)
	{
		dmetric = UtilTools.initArray(dmetric, 1.0);
		this.dmetric = dmetric;
		getMeanandDists(records, dmetric);
		
	}
	//identify the mean vector of each cluster
	private void getMeanandDists(ArrayList<TransRecord> records, double[] dmetric)
	{
		HashMap<String, ArrayList<TransRecord>> tmp = new HashMap<String,ArrayList<TransRecord>>();
		for(TransRecord rec:records)
		{
			if(tmp.containsKey(rec.label))
			{
				tmp.get(rec.label).add(rec);
			}
			else
			{
				ArrayList<TransRecord> x = new ArrayList<TransRecord>();
				x.add(rec);
				tmp.put(rec.label, x);
			}
		}
		// find the means 
		for(String key: tmp.keySet())
		{
			ArrayList<TransRecord> tdata = tmp.get(key);
			if(tdata.size() > 0 || tdata.get(0).features.length > 0)
			{
				ArrayList<double[]> tcl = new ArrayList<double[]>();
				for(int i =0; i< tdata.size(); i++)
				{
					 tcl.add(tdata.get(i).features); 
				}
				double[] tmean = UtilTools.sum(tcl);
				tmean = UtilTools.produce(1.0/tdata.size(), tmean);
				cmeans.put(key, tmean);
				// find the max distances
				// strictly bigger or smaller than [mean-3*delta, mean+3*delta]
				double d_mean = 0;
				double d_mu = 0;
				for(int i =0; i< tdata.size(); i++)
				{
					d_mean += UtilTools.distance(tdata.get(i).features, tmean, dmetric);
				}
				d_mean = d_mean*1.0/tdata.size();
				for(int i =0; i< tdata.size(); i++)
				{
					d_mu += Math.pow(UtilTools.distance(tdata.get(i).features, tmean, dmetric)-d_mean, 2);
				}
				d_mu = Math.sqrt(d_mu/tdata.size());
				double[] x = {d_mean,d_mu};
				mean_var.put(key, x);
			}
		}
		
		//Prober.printFeatureandWeight(tmp, cmeans, dmetric);
	}
			
	public String verify(TransRecord record) {
		double dist = UtilTools.distance(record.features, cmeans.get(record.label), dmetric);
		//difference STRICTLY bigger than 2 standard deviations [68, 95, 99.7] rule
		if(Math.abs(dist - mean_var.get(record.label)[0]) > 2.0*mean_var.get(record.label)[1])
		{
			return this.funid+"";
		}
		return "0";
	}

}
