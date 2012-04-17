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

package edu.isi.karma.controller.command.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.service.Attribute;
import edu.isi.karma.service.Table;

public class ServiceTableUtil {

	static Logger logger = Logger.getLogger(ServiceTableUtil.class);

	public static void populateEmptyWorksheet(Table table, Worksheet worksheet, RepFactory factory) {

		logger.info("Populating an empty worksheet with the service data ...");

		List<String> hNodeIdList = new ArrayList<String>();
		hNodeIdList = addHeaders(table.getHeaders(), worksheet, factory, true);

		edu.isi.karma.rep.Table dataTable = worksheet.getDataTable();
		addRows(table.getValues(), worksheet, factory, hNodeIdList, dataTable);

	}
	
	public static void populateWorksheet(Table table, Worksheet worksheet, RepFactory factory, String urlHNodeId) {
		
		logger.info("Populating existing worksheet with the service data ...");
		
		List<String> oldHNodeIdList = new ArrayList<String>(worksheet.getHeaders().getHNodeIds());

		List<String> hNodeIdList = new ArrayList<String>();
		hNodeIdList = addHeaders(table.getHeaders(), worksheet, factory, false);

		edu.isi.karma.rep.Table dataTable = worksheet.getDataTable();
		updateRows(table.getValues(), worksheet, factory, oldHNodeIdList, hNodeIdList, dataTable, urlHNodeId);

	}
	
	private static List<String> addHeaders(List<Attribute> tableHeader, Worksheet worksheet, RepFactory factory, boolean includeURL) {
		HTable headers = worksheet.getHeaders();
		List<String> headersList = new ArrayList<String>();
		
		// eliminate the url header if the flag is true
		int startIndex = 0;
		if (!includeURL) startIndex = 1;
		for (int i = startIndex; i < tableHeader.size(); i++) {
			Attribute att = tableHeader.get(i);
			HNode hNode = headers.addHNode(att.getName(), worksheet, factory);
			headersList.add(hNode.getId());
			
			// very important 
			// update the hNodeId of the input/output attributes
			att.sethNodeId(hNode.getId());
		}
		return headersList;
	}

	private static void addRows(List<List<String>> tableValues, Worksheet worksheet, RepFactory factory, 
			List<String> hNodeIdList, edu.isi.karma.rep.Table dataTable) {
		
		for (List<String> rowValues : tableValues) {
			if (rowValues == null || rowValues.size() == 0)
				continue;
			Row row = dataTable.addRow(factory);
			for (int i = 0; i < rowValues.size(); i++) 
				row.setValue(hNodeIdList.get(i), rowValues.get(i));
		}
		
	}

	private static void updateRows(List<List<String>> tableValues, Worksheet worksheet, RepFactory factory, 
			List<String> oldHNodeIdList, List<String> hNodeIdList, 
			edu.isi.karma.rep.Table dataTable, String urlHNodeId) {
		
		int rowsCount = dataTable.getNumRows();
		List<Row> oldRows = dataTable.getRows(0, rowsCount);
		List<HashMap<String, String>> oldRowValues = new ArrayList<HashMap<String,String>>();
		for (Row r : oldRows) {
			HashMap<String, String> vals = new HashMap<String, String>();
			for (Node n : r.getNodes()) {
				vals.put(n.getHNodeId(), n.getValue().asString());
			}
			oldRowValues.add(vals);
		}
		
		HashMap<String, String> currentRow = null;
		String currentRowURL = "";
		
		int addedRowsCount = 0; 
		for (int i = 0; i < oldRowValues.size(); i++) {
			currentRow = oldRowValues.get(i);
			currentRowURL = currentRow.get(urlHNodeId);
			
			for (List<String> rowValues : tableValues) {
				if (rowValues == null || rowValues.size() == 0)
					continue;
				
				String urlValue = rowValues.get(0);
				if (!urlValue.trim().equalsIgnoreCase(currentRowURL.trim())) {
					continue;
				}
				
				addedRowsCount ++;
				
				Row row = null;
				if (addedRowsCount <= rowsCount)
					row = dataTable.getRows(addedRowsCount - 1, 1).get(0);
				else 
					row = dataTable.addRow(factory);
				
				System.out.println(hNodeIdList.size());
				for (int j = 0; j < hNodeIdList.size(); j++) {
					// the first column in the table is the url column which should not be added to the table
					System.out.println("j:" + j);
					System.out.println(hNodeIdList.get(j));
					System.out.println(rowValues.get(j + 1));
					row.setValue(hNodeIdList.get(j), rowValues.get(j + 1));
				}
				
				for (String id: oldHNodeIdList) {
					row.setValue(id, currentRow.get(id).toString());
				}
				
			}
			
		}
		
	}

}
