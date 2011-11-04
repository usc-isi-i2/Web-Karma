package edu.isi.karma.modeling.semantictypes.mycrf.optimization ;

import java.util.ArrayList;

import edu.isi.karma.modeling.semantictypes.mycrf.common.Constants;
import edu.isi.karma.modeling.semantictypes.mycrf.math.Matrix;


public class LBFGS {
	
	ArrayList<double[]> s ;
	ArrayList<double[]> y ;
	ArrayList<Double> r ;
	
	double[] xOld ;
	double[] gOld ;
	
	int iter = 1 ;
	
	int dim ;
	
	public LBFGS(int dim) {
		this.dim = dim ;
		s = new ArrayList<double[]>() ;
		y = new ArrayList<double[]>() ;
		r = new ArrayList<Double>() ;
		xOld = new double[dim] ;
		gOld = new double[dim] ;
		iter = 1 ;
	}
	
	public void searchDir(double[] x, double[] g, double[] d) {
		if (iter == 1) {
			System.arraycopy(g, 0, d, 0, dim) ;
			for(int i=0;i<dim;i++) {
				d[i] = -d[i] ;
			}
		}
		else {
			double[] sNew = new double[dim] ;
			for (int i=0;i<dim;i++) {
				sNew[i] = x[i] - xOld[i] ;
			}
			double[] yNew = new double[dim] ;
			for(int i=0;i<dim;i++) {
				yNew[i] = g[i] - gOld[i] ;
			}
			double rNew = 1 / Matrix.dotProduct(sNew, yNew);
			if (s.size() == Constants.MEMORY_FOR_L_BFGS) {
				s.remove(Constants.MEMORY_FOR_L_BFGS -1) ;
				y.remove(Constants.MEMORY_FOR_L_BFGS -1) ;
				r.remove(Constants.MEMORY_FOR_L_BFGS -1) ;
			}
			s.add(0, sNew) ;
			y.add(0, yNew) ;
			r.add(0, rNew) ;
			
			int ilMax = s.size() < Constants.MEMORY_FOR_L_BFGS ? s.size() : Constants.MEMORY_FOR_L_BFGS ;
			
			double[] q = new double[dim] ;
			System.arraycopy(g, 0, q, 0, dim) ;
			double[] a = new double[ilMax] ;
			for(int il = 0 ; il < ilMax ; il++) {
				a[il] = r.get(il) * Matrix.dotProduct(s.get(il), q) ;
				Matrix.plusEquals(q, y.get(il), -a[il]) ;
			}
			double[] z = new double[dim] ;
			System.arraycopy(q, 0, z, 0, dim) ;
			for(int il=ilMax-1;il>=0 ; il--) {
				double b = r.get(il) * Matrix.dotProduct(y.get(il), z) ;
				Matrix.plusEquals(z, s.get(il), (a[il] - b)) ;
			}
			for(int i=0;i<dim;i++) {
				d[i] = -z[i] ;
			}
		}
		System.arraycopy(x, 0, xOld, 0, dim);
		System.arraycopy(g, 0, gOld, 0, dim);
		iter++ ;
	}
	
	
	
	
	
	
	
}