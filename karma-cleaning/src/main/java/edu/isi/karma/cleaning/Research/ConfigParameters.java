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

package edu.isi.karma.cleaning.Research;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Properties;

import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ConfigParameters {
	public static int debug = 0;
	Properties properties = new Properties();
	String res = "";

	private static final String newLine = System.getProperty("line.separator").toString();
	private String defaultTransformationProperties  = "cxt_size: 3" + newLine + 
														"debug:1" + newLine + 
														"temp_cap: 3096" + newLine + 
														"time_limit: 3" + newLine + 
														"iter_end: 400" + newLine + 
														"exmp_sel: 7" + newLine + 
														"supermode:1" + newLine + 
														"fixedlength:0" + newLine;
														
	public ConfigParameters() {
	}

	public void initeParameters() {
		// load property file and initialize the parameters;
		
		try {
			
			File file = new File(ServletContextParameterMap.getParameterValue(ContextParameter.USER_CONFIG_DIRECTORY) + "/transformation.properties");
			if(!file.exists()) {
				file.createNewFile();
				OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
		        BufferedWriter bw = new BufferedWriter(fw);
		        PrintWriter outWriter = new PrintWriter(bw);
		        outWriter.println(defaultTransformationProperties);
		        outWriter.close();
			}
			// load a properties file
			properties.load(new FileInputStream(file));
			// get the property value and print it out
			// Segment.cxtsize_limit =
			// Integer.parseInt(properties.getProperty("cxt_size").trim());
			// Template.temp_limit =
			// Integer.parseInt(properties.getProperty("temp_cap").trim());
			// Traces.time_limit =
			// Integer.parseInt(properties.getProperty("time_limit").trim());
			// ProgSynthesis.time_limit =
			// Integer.parseInt(properties.getProperty("time_limit").trim());
			// Segment.time_limit =
			// Integer.parseInt(properties.getProperty("time_limit").trim());
			// Section.time_limit =
			// Integer.parseInt(properties.getProperty("time_limit").trim());
			// properties.getProperty("iter_end");
			// ExampleSelection.way =
			// Integer.parseInt(properties.getProperty("exmp_sel").trim());
			debug = Integer.parseInt(properties.getProperty("debug").trim());
			// Template.supermode =
			// Integer.parseInt(properties.getProperty("supermode").trim());
			// Section.supermode =
			// Integer.parseInt(properties.getProperty("supermode").trim());
			// Position.fixedlength =
			// Integer.parseInt(properties.getProperty("fixedlength").trim());
			// res =
			// String.format("cxt_limit:%d temp_limit: %d, time_limit:%d, exp_sel:%d, supermode:%d,fixedlength:%d\n",Segment.cxtsize_limit,Template.temp_limit,Traces.time_limit,ExampleSelection.way,Template.supermode,Position.fixedlength);

		} catch (IOException ex) {
			return;
		}
	}

	public String getString() {
		return res;
	}
}
