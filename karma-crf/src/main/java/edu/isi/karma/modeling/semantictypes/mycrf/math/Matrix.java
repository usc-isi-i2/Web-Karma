/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.modeling.semantictypes.mycrf.math ;

import edu.isi.karma.modeling.semantictypes.myutils.Prnt;

import java.util.ArrayList;


/**
 * This class represents matrices and vectors.
 * It is primarily used to perform vector computations, 
 * such as,
 * addition, dot product, and scalar multiplication.
 * 
 * @author amangoel
 *
 */
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
