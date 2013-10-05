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
import java.util.List;


/**
 * This class simulated a normal rectangular table.
 * 
 * @author amangoel
 *
 */
public class DBTable {
	
	private int numColumns;
	ArrayList<String> columnNames;
	ArrayList<ArrayList<String>> tableData;
	
	public DBTable(int numColumns) {
		this.numColumns = numColumns;
		columnNames = new ArrayList<String>();
		for(int i=0;i<numColumns;i++) {
			columnNames.add("");
		}
		tableData = new ArrayList<ArrayList<String>>();
	}
	
	public void setColumnNames(List<String> columnNames) {
		if (columnNames.size() != numColumns) {
			Prnt.endIt("Number of column names given is not equal to the number of columns. Exiting.");
		}
		else {
			this.columnNames.clear();
			this.columnNames.addAll(columnNames);
		}
	}
	
	
	/**
	 * @param row - a list of strings that represent the values in a row
	 * This method adds the supplied row at the end of the tableData list of rows.
	 * It makes sure that the number of items in the list = numColumns
	 */
	public void addRow(List<String> row) {
		if (row.size() != numColumns) {
			Prnt.endIt("Number of items in the supplied row = " + row.size() + " is not equal to the number of columns = " + numColumns + " in the table. Exiting.");
		}
		else {
			tableData.add(new ArrayList<String>(row));
		}
	}
	
	/**
	 * @param row Row Index
	 * @param col Col Index
	 * @return Value at row, col
	 */
	public String getValue(int row, int col) {
		return tableData.get(row).get(col);
	}
	
	/**
	 * @param col The column index for which the values are required.
	 * @param columnValues The list in which the values will be returned.
	 */
	public void getColumn(int col, List<String> columnValues) {
		if (col >= numColumns()) {
			Prnt.endIt("Wrong col value.");
		}
		else {
			columnValues.clear();
			for(int rowIndex=0;rowIndex<numRows();rowIndex++) {
				columnValues.add(getValue(rowIndex, col));
			}
		}
	}
	
	
	/**
	 * @param file The file to which the object should be written
	 * @return True, if the operation was successful, False, otherwise.
	 */
	public boolean writeObjectToFile(String file) throws Exception {
		BufferedWriter bw;
		bw = new BufferedWriter(new FileWriter(file));
		// write the number of columns and then a blank line
		bw.write(numColumns + "\n\n");
		// write the column names. and an empty line at the end.
		for(String columnName : columnNames) {
			bw.write(columnName.length() + " " + columnName + "\n");
		}
		bw.write("\n");
		// write the number of rows
		bw.write(tableData.size() + "\n\n");
		// write each row, separated by newlines
		for(List<String> rowValues : tableData) {
			for(String val : rowValues) {
				bw.write(val.length() + " " + val + "\n");
			}
			bw.write("\n");
		}
		bw.close();
		return true;
	}
	
	/**
	 * @param file - the file from which the object would be created.
	 * @return The new created object that represents the table.
	 */
	public static DBTable readDBTableFromFile(String file) throws Exception {
		DBTable newTable;
		BufferedReader br;
		ArrayList<String> row;
		int numRows, numColumns;
		row = new ArrayList<String>();
		br = new BufferedReader(new FileReader(file));
		// read the number of columns in the table
		numColumns = Integer.parseInt(br.readLine());
		newTable = new DBTable(numColumns);
		// comsume the empty line
		br.readLine();
		// read in the column names
		row.clear();
		for(int colCount=0;colCount<numColumns;colCount++) {
			row.add(parseLine(br));
		}
		// set the column names
		newTable.setColumnNames(row);
		// consume empty line after column names
		br.readLine();
		// read the number of rows
		numRows = Integer.parseInt(br.readLine());
		// consume empty line after column names
		for(int rowIndex=0;rowIndex<numRows;rowIndex++) {
			br.readLine();
			row.clear();
			for(int colCount=0;colCount<numColumns;colCount++) {
				row.add(parseLine(br));
			}
			newTable.addRow(row);
		}
		br.close();
		return newTable;
	}
	
	private static String parseLine(BufferedReader br) throws Exception {
		String content, lenHeader;
		int numDigits, contentLen;
		char c;
		lenHeader = "";
		numDigits = 0;
		while(true) {
			c = (char)br.read();
			numDigits++;
			if (c != ' ') {
				lenHeader = lenHeader + c;
				if (numDigits > 5) {
					Prnt.endIt("Length marker has more than 5 digits. The program doesn't expect such large entries. Signaling parsing error. Exiting.");
				}
			}
			else {
				contentLen = Integer.parseInt(lenHeader);
				break;
			}
		}
		content = "";
		for(int i=0;i<contentLen;i++) {
			c = (char)br.read();
			content = content + c;
		}
		// consume the newline char at the end.
		br.read();
		return content;
	}
	
	
	public int numColumns() {
		return numColumns;
	}
	
	public int numRows() {
		return tableData.size();
	}
	
	
	
	
	
}
