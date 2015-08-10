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

package edu.isi.karma.modeling.alignment;

import java.text.DecimalFormat;

public class ModelEvaluation {

	private double precision;
	private double recall;
	private double distance;
	private double jaccard;
	
	public ModelEvaluation(Double distance, Double precision, Double recall, Double jaccard) {
		this.distance = distance == null ? -1 : distance;
		this.precision = precision == null ? 0 : precision;
		this.recall = recall == null ? 0 : recall;;
		this.jaccard = jaccard == null ? 0 : jaccard;;
	}
	public double getPrecision() {
		return roundTwoDecimals(precision);
	}
	public double getRecall() {
		return roundTwoDecimals(recall);
	}
	public double getDistance() {
		return distance;
	}
	public double getJaccard() {
		return roundTwoDecimals(jaccard);
	}
	
	private static double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
	}
	
}
