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
package edu.isi.karma.controller.update;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import edu.isi.karma.view.VWorkspace;

public class CSVImportPreviewUpdate extends AbstractUpdate {
	File csvFile;

	// Index of the column headers row
	private int headerRowIndex;

	// Index of the row from where data starts
	private int dataStartRowIndex;

	// Column delimiter
	private char delimiter;

	// Quote character
	private char quoteCharacter;

	// Escape character
	private char escapeCharacter;

	private String commandId;

	private static Logger logger = LoggerFactory
			.getLogger(CSVImportPreviewUpdate.class.getSimpleName());

	public enum JsonKeys {
		commandId, headers, rows, fileName
	}

	public CSVImportPreviewUpdate() {
		super();
	}

	public CSVImportPreviewUpdate(char delimiterChar, char quoteChar,
			char escapeChar, File csvFile, int headerRowIndex,
			int dataStartRowIndex, String id) {
		this.csvFile = csvFile;
		this.headerRowIndex = headerRowIndex;
		this.dataStartRowIndex = dataStartRowIndex;
		this.quoteCharacter = quoteChar;
		this.escapeCharacter = escapeChar;
		this.delimiter = delimiterChar;
		this.commandId = id;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		Scanner scanner;
		int rowCount = 0;
		int previewRowCounter = 0;

		// Hold the updateContainer JSON
		JSONStringer jsonStr = new JSONStringer();

		try {
			scanner = new Scanner(csvFile);
			JSONWriter writer = jsonStr.object().key(JsonKeys.commandId.name())
					.value(commandId).key(GenericJsonKeys.updateType.name())
					.value("ImportCSVPreview").key(JsonKeys.fileName.name())
					.value(csvFile.getName());

			JSONArray dataRows = new JSONArray();
			while (scanner.hasNextLine()) {
				// Check for the header row
				if (rowCount + 1 == headerRowIndex) {
					String line = scanner.nextLine();
					CSVReader reader = new CSVReader(new StringReader(line),
							delimiter, quoteCharacter, escapeCharacter);
					String[] rowValues = reader.readNext();
					ArrayList<String> headers = new ArrayList<String>();
					if (rowValues == null || rowValues.length == 0) {
						logger.error("No data found in the Header row!");
						rowCount++;
						scanner.nextLine();
						continue;
					}
					for (String val : rowValues) {
						headers.add(val);
					}
					// Add the row index
					headers.add(0, Integer.toString(rowCount + 1));

					// Add to the output JSON
					JSONArray arr = new JSONArray(headers);
					writer.key(JsonKeys.headers.name()).value(arr);
					rowCount++;
					continue;
				}

				// Check for the data rows. We choose the first five for preview
				if (rowCount + 1 >= dataStartRowIndex
						&& rowCount + 1 < dataStartRowIndex + 5) {
					if (previewRowCounter++ > 5) {
						break;
					}
					String line = scanner.nextLine();
					CSVReader reader = new CSVReader(new StringReader(line),
							delimiter, quoteCharacter, escapeCharacter);
					String[] rowValues = reader.readNext();
					ArrayList<String> vals = new ArrayList<String>();
					if(rowValues != null) {
						for (String val : rowValues) {
							vals.add(val);
						}
					} else 
						vals.add("");
					// Add the row index
					vals.add(0, Integer.toString(rowCount + 1));

					// Add to the data rows JSON
					dataRows.put(vals);
					rowCount++;
					continue;
				}

				rowCount++;
				scanner.nextLine();
			}

			writer.key(JsonKeys.rows.name()).value(dataRows);
			writer.endObject();
			pw.println(jsonStr.toString());
		} catch (FileNotFoundException e) {
			logger.error("File not found!", e);
		} catch (IOException e) {
			logger.error("Error occured while reading the file!", e);
		} catch (JSONException e) {
			logger.error("Error occured while writing to JSON", e);
		}
	}

}
