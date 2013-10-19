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

package edu.isi.karma.imp.csv;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class CSVFileExport {
	private Worksheet worksheet;
	private static final Logger logger = LoggerFactory
			.getLogger(CSVFileExport.class);
	public CSVFileExport(Worksheet worksheet) {
		this.worksheet = worksheet;
	}
	public String publishCSV() throws FileNotFoundException {
		String outputFile = "publish/CSV/" + worksheet.getTitle() + ".csv";
		logger.info("CSV file exported. Location:"
				+ outputFile);
		HashMap<String, String> modeledColumnTable = new HashMap<String, String>();
		for (SemanticType type : worksheet.getSemanticTypes().getListOfTypes()) {
			modeledColumnTable.put(type.getHNodeId(),"");
		}
		if(modeledColumnTable.size()==0) return null;
		int numRows = worksheet.getDataTable().getNumRows();
		if(numRows==0) 
			return "";
		StringBuilder sb = new StringBuilder();
		ArrayList<Row> rows =  worksheet.getDataTable().getRows(0, numRows);
		List<HNode> sortedLeafHNodes = new ArrayList<HNode>();
		List<String> hNodeIdList = new ArrayList<String>();
		worksheet.getHeaders().getSortedLeafHNodes(sortedLeafHNodes);
		for (HNode hNode : sortedLeafHNodes) {
			if(modeledColumnTable.containsKey(hNode.getId())) {
				if (sb.length() != 0)
					sb.append(",");
				sb.append(hNode.getColumnName());
				hNodeIdList.add(hNode.getId());
			}
		}
		sb.append("\n");
		for (Row row : rows) {
			boolean newRow = true;
			try {
				for(String hNodeId : hNodeIdList) {
					if(!newRow) 
						sb.append(",");
					else
						newRow = false;
					String colValue =row.getNode(hNodeId).getValue().asString();
					sb.append("\"");
					sb.append(colValue);
					sb.append("\"");
				}
			} catch (Exception e) {
				logger.error("Error reading a row! Skipping it.", e);
				continue;
			}
			sb.append("\n");
		}
		try {
	        Writer outUTF8=null;
			try {
				outUTF8 = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(ServletContextParameterMap.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) +outputFile), "UTF8"));
				outUTF8.append(sb.toString());
	    		outUTF8.flush();
	    		outUTF8.close();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
				
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputFile;
	}
}
