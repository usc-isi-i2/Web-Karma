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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.sources.Attribute;
import edu.isi.karma.rep.sources.Table;

public class ServiceTableUtil {

	private static Logger logger = LoggerFactory.getLogger(ServiceTableUtil.class);

	private ServiceTableUtil() {
	}


	public static void populateEmptyWorksheet(Table table, Worksheet worksheet, RepFactory factory) {
		logger.info("Populating an empty worksheet with the service data ...");
		List<String> hNodeIdList = addHeaders(table.getHeaders(), worksheet, factory);
		edu.isi.karma.rep.Table dataTable = worksheet.getDataTable();
		addRows(table.getValues(), worksheet, factory, hNodeIdList, dataTable);

	}
	
	public static void populateWorksheet(Table table, Worksheet worksheet, RepFactory factory, SuperSelection sel) {
		logger.info("Populating existing worksheet with the service data ...");
		List<String> oldHNodeIdList = new ArrayList<>(worksheet.getHeaders().getHNodeIds());
		List<String> hNodeIdList = addHeaders(table.getHeaders(), worksheet, factory);
		edu.isi.karma.rep.Table dataTable = worksheet.getDataTable();
		updateRows(table.getValues(), table.getRowIds(), worksheet, factory, oldHNodeIdList, hNodeIdList, dataTable, sel);

	}
	
	private static List<String> addHeaders(List<Attribute> tableHeader, Worksheet worksheet, RepFactory factory) {
		HTable headers = worksheet.getHeaders();
		List<String> headersList = new ArrayList<>();
		
		for (int i = 0; i < tableHeader.size(); i++) {
			Attribute att = tableHeader.get(i);
			HNode hNode = headers.addHNode(att.getName(), HNodeType.Regular, worksheet, factory);
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
			if (rowValues == null || rowValues.isEmpty())
				continue;
			Row row = dataTable.addRow(factory);
			for (int i = 0; i < rowValues.size(); i++) 
				row.setValue(hNodeIdList.get(i), rowValues.get(i), factory);
		}
		
	}

	private static void updateRows(List<List<String>> tableValues, List<String> tableRowIds, 
			Worksheet worksheet, RepFactory factory, 
			List<String> oldHNodeIdList, List<String> hNodeIdList, 
			edu.isi.karma.rep.Table dataTable, SuperSelection sel) {
		
		int rowsCount = dataTable.getNumRows();
		List<Row> oldRows = dataTable.getRows(0, rowsCount, sel);
		List<HashMap<String, String>> oldRowValues = new ArrayList<>();
		List<String> oldRowIds = new ArrayList<>();
		
		for (Row r : oldRows) {
			HashMap<String, String> vals = new HashMap<>();
			for (Node n : r.getNodes()) {
				vals.put(n.getHNodeId(), n.getValue().asString());
			}
			oldRowValues.add(vals);
			oldRowIds.add(r.getId());
		}
		
		HashMap<String, String> currentRow;
		String currentRowId;
		
		int addedRowsCount = 0; 
		for (int i = 0; i < oldRowValues.size(); i++) {
			currentRow = oldRowValues.get(i);
			currentRowId = oldRowIds.get(i);
			
			for (int k = 0; k < tableValues.size(); k++) {
				List<String> rowValues = tableValues.get(k);
				if (rowValues == null || rowValues.isEmpty())
					continue;
				
				String tableRowId = tableRowIds.get(k);
				if (tableRowId == null || !tableRowId.trim().equalsIgnoreCase(currentRowId.trim())) {
					continue;
				}
				
				addedRowsCount ++;
				
				Row row;
				if (addedRowsCount <= rowsCount)
					row = dataTable.getRows(addedRowsCount - 1, 1, sel).get(0);
				else 
					row = dataTable.addRow(factory);
				
//				logger.debug(hNodeIdList.size());
				for (int j = 0; j < hNodeIdList.size(); j++) {
					// the first column in the table is the url column which should not be added to the table
//					logger.debug("j:" + j);
//					logger.debug(hNodeIdList.get(j));
//					logger.debug(rowValues.get(j + 1));
					row.setValue(hNodeIdList.get(j), rowValues.get(j), factory);
				}
				
				for (String id: oldHNodeIdList) {
					row.setValue(id, currentRow.get(id), factory);
				}
				
			}
			
		}
		
	}

}
