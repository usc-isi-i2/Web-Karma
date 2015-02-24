package edu.isi.karma.cleaning;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GradientDecendOptimizer {
	public double c_coef = 1;
	public double relativeCoef = 1;
	public static double ratio =0.4;
	public double stepsize = 0.1;
	public double maximalIternumber = 50;
	public double wIternumber = 100;
	public boolean coef_initialized = false;
	public double manualCoef = 1;

	Logger ulogger = LoggerFactory.getLogger(GradientDecendOptimizer.class);

	public GradientDecendOptimizer() {
	}

	// used to calculate the value of objective function.
	public double objectiveFunction(ArrayList<double[]> r,
			ArrayList<double[]> s, ArrayList<double[]> t_star,
			double[] w) {
		double res = 0.0;
		for (double[] x : r) {
			res += UtilTools.product(x, w);
		}
		double ml = 0.0;
		
		for (double[] e : s) {
			ml += Math.pow(UtilTools.product(e, w), 0.5);
		}
		double tmp = 0.0;
		for (double[] x : t_star) {
			tmp += Math.pow(UtilTools.product(x, w), 0.5);
		}
		return res +  c_coef * Math.log(ml) - c_coef
				* Math.log(tmp);
	}

	// compute the gradient according to the formula in slides
	public double[] getGradient(ArrayList<double[]> r, ArrayList<double[]> s,
			ArrayList<double[]> t_star, double[] w_old) {

		int featuresize = w_old.length;
		double[] r_sum = UtilTools.sum(r);
		double[] res = new double[featuresize];
		// calculate the first component
		double t_scalar_sqrt_sum = 0;
		for (double[] e : t_star) {
			t_scalar_sqrt_sum += Math.sqrt(UtilTools.product(e, w_old));
		}
		// System.out.println("scalar_sqrt_sum: "+scalar_sqrt_sum);
		t_scalar_sqrt_sum = 1.0 / t_scalar_sqrt_sum;
		// calculate the coefficent for each t_i
		double[] vects = new double[featuresize];
		for (int i = 0; i < vects.length; i++) {
			vects[i] = 0;
		}
		for (double[] e : t_star) {
			double cof = Math.pow(UtilTools.product(e, w_old) + (1e-4), -0.5);
			double[] x = UtilTools.produce(cof, e);
			for (int j = 0; j < x.length; j++) {
				vects[j] += x[j];
			}
		}
		// calculate the coefficent for each t_i
		double s_scalar_sqrt_sum = 0;
		for (double[] e : s) {
			s_scalar_sqrt_sum += Math.sqrt(UtilTools.product(e, w_old));
		}
		// System.out.println("scalar_sqrt_sum: "+scalar_sqrt_sum);
		s_scalar_sqrt_sum = 1.0 / (s_scalar_sqrt_sum+1e-6);
		double[] svects = new double[featuresize];
		for (int i = 0; i < svects.length; i++) {
			svects[i] = 0;
		}
		for (double[] e : s) {
			double cof = Math.pow(UtilTools.product(e, w_old) + (1e-4), -0.5);
			//System.out.println("coef: "+cof);
			//System.out.println("e: "+Arrays.toString(e));
			double[] x = UtilTools.produce(cof, e);
			//System.out.println("x: "+Arrays.toString(x));
			for (int j = 0; j < x.length; j++) {
				svects[j] += x[j];
			}
		}
		//
		// System.out.println("scalar_sum: "+scalar_sqrt_sum);
		// System.out.println("vects: "+Arrays.toString(vects));

		if (!coef_initialized) {
			this.manualCoef = selectcoef(r_sum, vects);
			coef_initialized = true;
			this.c_coef = 2.0 * manualCoef / t_scalar_sqrt_sum;
		}
		// computer the gradient using subcomponents
		for (int i = 0; i < res.length; i++) {
			res[i] = r_sum[i] + this.relativeCoef * manualCoef * svects[i]- manualCoef * vects[i];
			//res[i] = this.relativeCoef * manualCoef *s_scalar_sqrt_sum*svects[i]- manualCoef *t_scalar_sqrt_sum*vects[i];;
			//res[i] = this.relativeCoef * manualCoef *svects[i]- manualCoef *vects[i];
		}

		return res;
	}

	public double selectcoef(double[] r_rum, double[] tvec) {
		ArrayList<Double> ratios = new ArrayList<Double>();
		for (int i = 0; i < r_rum.length; i++) {
			if (tvec[i] != 0 && r_rum[i] != 0) {
				double x = r_rum[i] * 1.0 / tvec[i];
				ratios.add(x);
			}
		}
		if (ratios.size() == 0)
			return 1;
		Collections.sort(ratios, Collections.reverseOrder());
		// System.out.println("ratios: "+ratios);
		Double result = 0.0;
		/*
		 * double smallest = ratios.get(ratios.size() - 1); for (Double right :
		 * ratios) { if (right / smallest <= 10) { result = right; } }
		 */
		result = ratios.get(0);
		return result;
	}
	//approximate the largrange multiplier
	public double[] normalizeW(double[] w)
	{
		double sum = 0.0;
		double goal = w.length;
		double[] nw = new double[w.length];
		for(int i = 0; i < w.length; i++)
		{
			sum += w[i];
		}
		for(int i = 0; i < w.length; i++)
		{
			nw[i] =w[i]*goal*1.0/sum;
		}
		return nw;
	}
	// as the objective function is convex. Could check the trend each step
	public double[] doOptimize(ArrayList<double[]> examples,
			ArrayList<double[]> instances,
			ArrayList<ArrayList<double[]>> constraints,
			ArrayList<ArrayList<double[]>> individualExps, double[] w) {

		double[] w_0 = new double[examples.get(0).length];
		double[] oldGradient = new double[examples.get(0).length];
		if (w == null)
		// init it to 1
		{
			for (int i = 0; i < w_0.length; i++) {
				w_0[i] = 1;
				oldGradient[i] = Double.MAX_VALUE;
			}
		} else {
			w_0 = w;
		}
		int cntor = 0;
		while (cntor < maximalIternumber) {
			ArrayList<double[]> r = compute_r(examples, instances, w_0);
			//ArrayList<double[]> r = new ArrayList<double[]>();
			ArrayList<double[]> t = compute_t(constraints, w_0);
			ArrayList<double[]> s = compute_s(individualExps, w_0);
			// System.out.println("t>>>");
			// TestTools.print(t);
			// System.out.println("s>>>");
			// TestTools.print(s);
			if (s.size() > 0)
			{
				this.relativeCoef = t.size()*ratio/s.size();
				//relativeCoef = 2;
			}
			double[] gradient = getGradient(r, s, t, w_0);
			if(UtilTools.distance(gradient, oldGradient) < 10e-5)
			{
				break;
			}
			oldGradient = gradient;
			if(s.size() > 0)
			{
				int negtiveCnt = 0;
				for(int i = 0; i < gradient.length; i++)
				{
					if(gradient[i] < 0)
					{
						negtiveCnt ++;
					}
				}
				double offset = 1.0;
				for(int x = 0; x < individualExps.size(); x++)
				{
					offset += individualExps.get(x).size();
				}
				offset = offset/ (individualExps.size());
				//String sx = String.format("s: %d, t: %d,nG: %f, relative: %f, offset:%f", s.size(), t.size(),(negtiveCnt*1.0/gradient.length), this.relativeCoef,offset );
			}
			int wcnt = 0;
			double rstepsize = this.stepsize;
			while (wcnt < wIternumber) {

				double[] w_1 = new double[w_0.length];
				for (int i = 0; i < w_0.length; i++) {
					w_1[i] = w_0[i] - rstepsize * gradient[i];
				}
				for (int i = 0; i < w_1.length; i++) {
					if (w_1[i] < 0) {
						w_1[i] = 0;
					}
				}
				w_1 = normalizeW(w_1);
				// the function should always decline
				double v_old = objectiveFunction(r, s, t, w_0);
				double v_new = objectiveFunction(r, s, t, w_1);
				// System.out.println(String.format("values: %f, %f", v_old,
				// v_new));
				if (v_new > v_old) {
					rstepsize = 0.1 * rstepsize; // jump over optimal point.
													// reduce step size an then
					wcnt++; // redo
					continue;
				}
				// check stop condition
				if (UtilTools.distance(w_0, w_1) < 10e-4) {
					w_0 = w_1;
					break;
				}
				w_0 = w_1;
				wcnt++;
			}
			cntor++;
		}
		return w_0;
	}

	// compute the r vectors
	public ArrayList<double[]> compute_r(ArrayList<double[]> centers,
			ArrayList<double[]> instances, double[] w) {
		ArrayList<double[]> res = new ArrayList<double[]>();
		// assign instance to different centers
		for (double[] e : instances) {
			double[] cent_vec = null;
			double min_dist = Double.MAX_VALUE;
			for (double[] o : centers) {
				double tmpdist = UtilTools.distance(e, o, w);
				if (tmpdist <= min_dist) {
					min_dist = tmpdist;
					cent_vec = o;
				}
			}
			// calculate a r vector
			double[] r1 = new double[e.length];
			for (int i = 0; i < r1.length; i++) {
				r1[i] = Math.pow(e[i] - cent_vec[i], 2);
			}
			res.add(r1);
		}
		return res;
	}

	// compute the t vectors
	public ArrayList<double[]> compute_t(
			ArrayList<ArrayList<double[]>> constraints, double[] w) {
		ArrayList<double[]> res = new ArrayList<double[]>();
		// for each constraint group, find most apart two points
		for (ArrayList<double[]> group : constraints) {
			double max_dist = 0;
			double[] x = null;
			double[] y = null;
			for (int i = 0; i < group.size(); i++) {
				for (int j = i + 1; j < group.size(); j++) {
					double tmp = UtilTools.distance(group.get(i), group.get(j),
							w);
					if (tmp >= max_dist) {
						max_dist = tmp;
						x = group.get(i);
						y = group.get(j);
					}
				}
			}
			double[] t = new double[w.length];
			for (int i = 0; i < t.length; i++) {
				t[i] = Math.pow(x[i] - y[i], 2);
			}
			res.add(t);
		}
		return res;
	}

	// computer the s vectors
	public ArrayList<double[]> compute_s(ArrayList<ArrayList<double[]>> groups,
			double[] w) {
		ArrayList<double[]> res = new ArrayList<double[]>();
		for (ArrayList<double[]> group : groups) {
			if (group.size() <= 1)
				continue;
			ArrayList<double[]> tmp = new ArrayList<double[]>();
			for (int i = 0; i < group.size(); i++) {
				double[] t = new double[w.length];
				if (i == group.size() - 1) {
					for (int k = 0; k < t.length; k++) {
						t[k] = Math.pow(group.get(i)[k] - group.get(0)[k], 2);
					}
					tmp.add(t);
					break;
				} else {
					for (int k = 0; k < t.length; k++) {
						t[k] = Math.pow(group.get(i)[k] - group.get(i + 1)[k],
								2);
					}
					tmp.add(t);
				}
			}
			double[] groupsum = UtilTools.sum(tmp);
			res.add(groupsum);
		}
		return res;
	}

	@Test
	public void test1() {
		double[] p1 = { 1, 2 };
		double[] p2 = { 50, 1 };
		double[] p3 = { 50, 2 };
		double[] p4 = { 50, 3 };
		double[] p5 = { 55, 1.5 };
		double[] p6 = { 60, 2 };
		double[] p7 = { 60, 3 };
		ArrayList<double[]> examples = new ArrayList<double[]>();
		double[] center1 = { 37.75, 1.75 };
		double[] center2 = { 60, 2.5 };
		examples.add(center1);
		examples.add(center2);
		ArrayList<double[]> instances = new ArrayList<double[]>();
		instances.add(p1);
		instances.add(p2);
		instances.add(p3);
		instances.add(p4);
		instances.add(p5);
		instances.add(p6);
		instances.add(p7);
		ArrayList<ArrayList<double[]>> mconstraints = new ArrayList<ArrayList<double[]>>();
		ArrayList<ArrayList<double[]>> cconstraints = new ArrayList<ArrayList<double[]>>();
		// group 1
		ArrayList<double[]> group1 = new ArrayList<double[]>();
		group1.add(p1);
		group1.add(p2);
		group1.add(p3);
		group1.add(p4);
		// group1.add(p3);
		// group1.add(p4);
		mconstraints.add(group1);
		ArrayList<double[]> group2 = new ArrayList<double[]>();
		group2.add(p6);
		group2.add(p7);
		mconstraints.add(group2);
		ArrayList<double[]> group3 = new ArrayList<double[]>();
		group3.add(p1);
		group3.add(p2);
		group3.add(p3);
		group3.add(p4);
		group3.add(p6);
		group3.add(p7);
		cconstraints.add(group3);
		double[] res = doOptimize(examples, instances, cconstraints,
				mconstraints, null);
		assert (res[0] < res[1]);
		assert (res[0] < 1e-5);
		// System.out.println("Final Weights: " + Arrays.toString(res));
	}
}
