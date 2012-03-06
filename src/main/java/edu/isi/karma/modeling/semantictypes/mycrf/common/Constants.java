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
package edu.isi.karma.modeling.semantictypes.mycrf.common ;

public class Constants {
	
	public final static int FIELD_TYPE = 0 ;
	public final static int TOKEN_TYPE = 1 ;
	
	public final static int MEMORY_FOR_L_BFGS = 4 ;
	
	public final static double STANDARD_DEVIATION = 100.0 ;
	
	public final static double BACKTRACKINGLINESEARCH_MAX_STEP = 100.0 ;
	
	public final static double EPSILON_GRADIENT = 0.001 ;
	public static final double EPSILON_DELTA_X_RELATIVE_TO_X = 0.0001 ;
	public static final double ALPHA = 0.0001 ;
	public static final double TOLX = 1.0e-5;
	
	public final static String tokenLinePrefix = "   " ;
	
	public final static int NUM_OF_LOOPY_BP_ITERATIONS = 10 ;
	
	public final static int MAX_CLIQUE_SIZE = 3 ;
	
	public final static int MAX_COMMON_NODES_BETWEEN_CLUSTERS = 2 ;
	
	public final static double FUNCTION_ON_VAL = 1.0 ;
	public final static double FUNCTION_OFF_VAL = 0.0 ;
	
	public final static int runMode_CALCULATE_LIKELIHOOD = 0 ;
	public final static int runMode_CALCULATE_GRADIENTOFLIKELIHOOD = 1 ;
	
	public final static int MAX_THREADS = 0 ;
	
	
	
	// paths
	
	public final static String WEIGHTS_FILE = "/Users/amangoel/Desktop/weights_file.txt" ;
	public final static String GRADIENT_LOG_FILE = "/Users/amangoel/Desktop/gradient-log.txt" ;
	public final static String CRF_DATA_SUBPATH = "/aman/" ;
	
	//public final static String WEIGHTS_FILE = "/home/rcf-proj3/ag1/outputs/weights_file.txt" ;
	//public final static String GRADIENT_LOG_FILE = "/home/rcf-proj3/ag1/outputs/gradient-log.txt" ;
	//public final static String CRF_DATA_SUBPATH = "/home/rcf-proj3/ag1/" ;

	
}

// Notes: The belief of a twonodeclique is the product of all messages into node1 , product of all messages into node2, 
// potential of onenodeclique of node1, potential of onenodeclique of node2 and potential of twonodeclique of node1 and node2
