package edu.isi.karma.modeling.semantictypes.mycrf.math ;

import java.util.ArrayList;

import edu.isi.karma.modeling.semantictypes.myutils.Prnt;


public class Matrix {
	
	int numOfRows ;
	int numOfCols ;
	double[][] data ;
	
	public Matrix(int numOfRows, int numOfCols) {
		this.numOfRows = numOfRows ;
		this.numOfCols = numOfCols ;
		data = new double[this.numOfRows][this.numOfCols] ;
	}
	
	public Matrix(ArrayList<ArrayList<Double>> newData) {
		numOfRows = newData.size() ;
		numOfCols = newData.get(0).size() ;
		data = new double[numOfRows][numOfCols] ;
		
		for(int row = 0 ; row < numOfRows ; row++) {
			ArrayList<Double> rowData = newData.get(row) ;
			for(int col = 0 ; col < numOfCols ; col++) {
				data[row][col] = rowData.get(col) ;
			}
		}
	}
		
	public Matrix(double[][] newData) {
		numOfRows = newData.length ;
		numOfCols = newData[0].length ;
		data = new double[numOfRows][numOfCols] ;
		
		for(int row = 0 ; row< numOfRows ; row++) {
			for(int col = 0 ; col < numOfCols ; col++) {
				data[row][col] = newData[row][col] ;
			}
		}
	}
	
	
	public Matrix multiply(Matrix otherMatrix) {
		if(numOfCols != otherMatrix.numOfRows) {
			Prnt.endIt("dimensions dont match") ;
		}
		Matrix resultMatrix = new Matrix(numOfRows, otherMatrix.numOfCols) ;
		for(int row = 0 ; row < numOfRows ; row++) {
			for(int col = 0 ; col < otherMatrix.numOfCols ; col++) {
				for(int i = 0 ; i < numOfCols ; i++) {
					resultMatrix.data[row][col] += data[row][i] * otherMatrix.data[i][col] ;
				}
			}
		}
		return resultMatrix ;
	}
	
	public Matrix transpose() {
		Matrix resultMatrix = new Matrix(numOfCols, numOfRows) ;
		for(int col = 0 ; col < numOfCols ; col++) {
			for(int row = 0 ; row < numOfRows ; row++) {
				resultMatrix.data[col][row] = data[row][col] ;
			}
		}
		return resultMatrix ;
	}
	
	public Matrix add(Matrix otherMatrix) {
		if(numOfRows != otherMatrix.numOfRows || numOfCols != otherMatrix.numOfCols)
			Prnt.endIt("dims dont match") ;
		Matrix resultMatrix = new Matrix(numOfRows, numOfCols) ;
		for(int row = 0 ; row < numOfRows ; row++) {
			for(int col = 0 ; col < numOfCols ; col++) {
				resultMatrix.data[row][col] = data[row][col] + otherMatrix.data[row][col] ;
			}
		}
		return resultMatrix ;
	}
	
	public Matrix subtract(Matrix otherMatrix) {
		if(numOfRows != otherMatrix.numOfRows || numOfCols != otherMatrix.numOfCols)
			Prnt.endIt("dims dont match") ;
		Matrix resultMatrix = new Matrix(numOfRows, numOfCols) ;
		for(int row = 0 ; row < numOfRows ; row++) {
			for(int col = 0 ; col < numOfCols ; col++) {
				resultMatrix.data[row][col] = data[row][col] - otherMatrix.data[row][col] ;
			}
		}
		return resultMatrix ;
	}
	
	public Matrix multiply_with_constant(double k) {
		Matrix resultMatrix = new Matrix(numOfRows, numOfCols) ;
		for(int row = 0 ; row < numOfRows ; row++) {
			for(int col = 0 ; col < numOfCols ; col++) {
				resultMatrix.data[row][col] = k * data[row][col] ; 
			}
		}
		return resultMatrix ;
	}
	
	public Matrix clone() {
		Matrix resultMatrix = new Matrix(numOfRows, numOfCols) ;
			for(int row = 0 ; row < numOfRows ; row++) {
				for(int col = 0 ; col < numOfCols ; col++) {
				resultMatrix.data[row][col] = data[row][col] ;
			}
		}
		return resultMatrix ;
	}
	
	public static double norm(Matrix m) {
		if(m.numOfCols != 1)
			Prnt.endIt("Matrix.norm got a non-vector matrix") ;
		double sum = 0.0 ;
		for(int i = 0 ; i < m.numOfRows ; i++) {
			sum = sum + m.data[i][0] * m.data[i][0] ;
		}
		sum = Math.sqrt(sum) ;
		return sum ;
	}
	
	public static void plusEquals(double[] x, double[] y, double m) {
		for(int i=0;i<x.length;i++)
			x[i] = x[i] + m * y[i] ;
	}
	
	public static double dotProduct(double[] x, double[] y) {
		double s = 0.0 ;
		for(int i=0;i<x.length;i++) 
			s+=(x[i] * y[i]) ;
		return s ;
	}
	
	public static double norm(double[] x) {
		double s = 0.0 ;
		for(int i=0;i<x.length;i++)
			s+=(x[i]*x[i]) ;
		s = Math.sqrt(s) ;
		return s ;
	}
	
}