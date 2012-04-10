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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

public class CSVFileImport {
	private final int headerRowIndex;
	private final int dataStartRowIndex;
	private final char delimiter;
	private final char quoteCharacter;
	private final char escapeCharacter = '\\';
	private final File csvFile;
	private final RepFactory factory;
	private final Worksheet worksheet;

	private static Logger logger = LoggerFactory.getLogger(CSVFileImport.class);

	public CSVFileImport(int headerRowIndex, int dataStartRowIndex,
			char delimiter, char quoteCharacter, File csvFile,
			RepFactory factory, Workspace workspace) {
		super();
		this.headerRowIndex = headerRowIndex;
		this.dataStartRowIndex = dataStartRowIndex;
		this.delimiter = delimiter;
		this.quoteCharacter = quoteCharacter;
		this.csvFile = csvFile;
		this.factory = factory;
		this.worksheet = factory.createWorksheet(csvFile.getName(), workspace);
	}

	public Worksheet generateWorksheet() throws IOException, KarmaException {
		Table dataTable = worksheet.getDataTable();

		// Prepare the scanner for reading file line by line
		Scanner scanner = new Scanner(csvFile);

		// Index for row currently being read
		int rowCount = 0;
		ArrayList<String> hNodeIdList = new ArrayList<String>();

		// If no row is present for the column headers
		if (headerRowIndex == 0){
			hNodeIdList = addEmptyHeaders(worksheet, factory);
			if(hNodeIdList == null || hNodeIdList.size() == 0){
				throw new KarmaException("Error occured while counting header " +
						"nodes for the worksheet!");
			}				
		}
			

		// Populate the worksheet model
		while (scanner.hasNextLine()) {
			// Check for the header row
			if (rowCount + 1 == headerRowIndex) {
				String line = scanner.nextLine();
				hNodeIdList = addHeaders(worksheet, factory, line);
				rowCount++;
				continue;
			}

			// Populate the model with data rows
			if (rowCount + 1 >= dataStartRowIndex) {
				String line = scanner.nextLine();
				addRow(worksheet, factory, line, hNodeIdList, dataTable);
				rowCount++;
				continue;
			}

			rowCount++;
			scanner.nextLine();
		}

		return worksheet;
	}

	private ArrayList<String> addHeaders(Worksheet worksheet, RepFactory fac,
			String line) throws IOException {
		HTable headers = worksheet.getHeaders();
		ArrayList<String> headersList = new ArrayList<String>();
		CSVReader reader = new CSVReader(new StringReader(line), delimiter,
				quoteCharacter, escapeCharacter);
		String[] rowValues = null;
		rowValues = reader.readNext();

		if (rowValues == null || rowValues.length == 0)
			return addEmptyHeaders(worksheet, fac);
		for (int i = 0; i < rowValues.length; i++) {
			HNode hNode = null;
			if (headerRowIndex == 0)
				hNode = headers.addHNode("Column_" + (i + 1), worksheet, fac);
			else
				hNode = headers.addHNode(rowValues[i], worksheet, fac);
			headersList.add(hNode.getId());
		}
		return headersList;
	}

	private void addRow(Worksheet worksheet, RepFactory fac, String line,
			ArrayList<String> hNodeIdList, Table dataTable) throws IOException {
		CSVReader reader = new CSVReader(new StringReader(line), delimiter,
				quoteCharacter, escapeCharacter);
		String[] rowValues = null;
		rowValues = reader.readNext();
		if (rowValues == null || rowValues.length == 0)
			return;
		Row row = dataTable.addRow(fac);
		for (int i = 0; i < rowValues.length; i++) {
			if (i < hNodeIdList.size())
				row.setValue(hNodeIdList.get(i), rowValues[i]);
			else {
				// TODO Our model does not allow a value to be added to a row
				// without its associated HNode. In CSVs, there could be case
				// where values in rows are greater than number of column names.
				logger.error("More data elements detected in the row than number of headers!");
			}
		}
	}

	private ArrayList<String> addEmptyHeaders(Worksheet worksheet,
			RepFactory fac) throws IOException {
		HTable headers = worksheet.getHeaders();
		ArrayList<String> headersList = new ArrayList<String>();

		Scanner scanner = null;
		scanner = new Scanner(csvFile);

		// Use the first data row to count the number of columns we need to add
		int rowCount = 0;
		while (scanner.hasNext()) {
			if (rowCount + 1 == dataStartRowIndex) {
				String line = scanner.nextLine();
				CSVReader reader = new CSVReader(new StringReader(line),
						delimiter, quoteCharacter, escapeCharacter);
				String[] rowValues = null;
				try {
					rowValues = reader.readNext();
				} catch (IOException e) {
					logger.error("Error reading Line:" + line, e);
				}
				for (int i = 0; i < rowValues.length; i++) {
					HNode hNode = headers.addHNode("Column_" + (i + 1),
							worksheet, fac);
					headersList.add(hNode.getId());
				}
				break;
			}
			rowCount++;
			if(scanner.hasNext())
				scanner.nextLine();
		}
		return headersList;
	}
}
