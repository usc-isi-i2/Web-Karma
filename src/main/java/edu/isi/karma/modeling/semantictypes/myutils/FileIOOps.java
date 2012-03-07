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
package edu.isi.karma.modeling.semantictypes.myutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class FileIOOps {

	public static ArrayList<String> allLinesFromFile(String file, boolean removeEmptyLines)  {
		ArrayList<String> lines = new ArrayList<String>() ;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file)) ;
			String line = "" ;

			while(true) {
				if ((line = br.readLine()) == null) {
					break ;
				}
				line = line.trim() ;
				if (line.length() != 0 || !removeEmptyLines) {
					lines.add(line) ;
				}
			}

			br.close();
		}
		catch (Exception e) {
			Prnt.endIt("FileIOOps.allLinesFromFile: Error in reading file " + file + ". Exiting.") ;
		}
		return lines ;
	}
	
	
	public static boolean writeToFileAndClose(String fileName, String content) {
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(fileName));
			bw.write(content);
			bw.close();
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}

