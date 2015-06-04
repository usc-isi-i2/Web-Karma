package edu.isi.karma.cleaning.correctness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import edu.isi.karma.cleaning.DataPreProcessor;
import edu.isi.karma.cleaning.DataRecord;

public class MembershipAmbiguityInspector implements Inspector {
	public HashMap<String, double[]> means = new HashMap<String, double[]>();
	public DataPreProcessor dpp;
	public double[] weights;
	public double scale = 0.05;

	public MembershipAmbiguityInspector(DataPreProcessor dpp, HashMap<String, ArrayList<DataRecord>> groups, double scale, double[] weights) {
		this.dpp = dpp;
		this.weights = weights;
		this.scale = scale;
		for (String clabel : groups.keySet()) {
			ArrayList<DataRecord> g = groups.get(clabel);
			double[] m = InspectorUtil.getMeanVector(dpp, g);
			means.put(clabel, m);
		}
	}

	public double getActionLabel(DataRecord record) {
		double[] alldists = new double[means.keySet().size()];
		int cnt = 0;
		for (String c : means.keySet()) {
			double dist = InspectorUtil.getDistance(dpp, record, means.get(c), weights);
			alldists[cnt] = dist;
			cnt++;
		}
		Arrays.sort(alldists);
		for (int i = 0; i < alldists.length - 1; i++) {
			double x = 2.0 * (alldists[i + 1] - alldists[i]) / (alldists[i + 1] + alldists[i]);
			if (x < this.scale) {
				return -1;
			}
		}
		return 1;
	}

	@Override
	public String getName() {
		return this.getClass().getName()+"|"+this.scale;
	}


}
