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

package edu.isi.karma.modeling;


public interface ModelingParams {

	// Building Graph
	public static double PROPERTY_USER_PREFERRED_WEIGHT = 0.01;
	public static double PROPERTY_UI_PREFERRED_WEIGHT = 0.1;
	public static double PROPERTY_DIRECT_WEIGHT = 100;	
	public static double PROPERTY_INDIRECT_WEIGHT = 100.01;
	public static double PROPERTY_WITH_ONLY_DOMAIN_WEIGHT = 1000;
	public static double PROPERTY_WITH_ONLY_RANGE_WEIGHT = 1000;
	public static double SUBCLASS_WEIGHT = 10000;
	public static double PROPERTY_WITHOUT_DOMAIN_RANGE_WEIGHT = 5000;
	public static double PATTERN_LINK_WEIGHT = 1;
	public static double DATA_PROPERTY_WEIGHT = 100;
	
}
