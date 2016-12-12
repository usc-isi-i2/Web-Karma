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
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;
import edu.isi.karma.util.EncodingDetector;
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

	private String encoding;

	private int maxNumLines;

	private static Logger logger = LoggerFactory
			.getLogger(CSVImportPreviewUpdate.class.getSimpleName());

	public enum JsonKeys {
		commandId, headers, rows, fileName
	}

	public CSVImportPreviewUpdate() {
		super();
	}

	public CSVImportPreviewUpdate(char delimiterChar, char quoteChar,
			char escapeChar, String encoding, int maxNumLines, File csvFile,
			int headerRowIndex, int dataStartRowIndex, String id) {
		this.csvFile = csvFile;
		this.headerRowIndex = headerRowIndex;
		this.dataStartRowIndex = dataStartRowIndex;
		this.quoteCharacter = quoteChar;
		this.escapeCharacter = escapeChar;
		this.delimiter = delimiterChar;
		this.commandId = id;
		this.encoding = encoding;
		this.maxNumLines = maxNumLines;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		// Scanner scanner = null;
		int rowCount = 0;
		int previewRowCounter = 0;

		// Hold the updateContainer JSON
		JSONStringer jsonStr = new JSONStringer();

		try {
			logger.info("CSVFileImportPreview: Got encoding: " + encoding);

			if (encoding == null) {
				encoding = EncodingDetector.detect(csvFile);
			}
			// scanner = new Scanner(csvFile, encoding);
			JSONWriter writer = jsonStr.object().key(JsonKeys.commandId.name())
					.value(commandId).key(GenericJsonKeys.updateType.name())
					.value("ImportCSVPreview").key(JsonKeys.fileName.name())
					.value(csvFile.getName()).key("encoding").value(encoding)
					.key("maxNumLines").value(maxNumLines);

			JSONArray dataRows = new JSONArray();
			CSVReader reader = new CSVReader(
				new InputStreamReader(new FileInputStream(csvFile), encoding), 
				delimiter, quoteCharacter, escapeCharacter);
			String[] rowValues = null;
			while ((rowValues = reader.readNext()) != null) {
				// Check for the header row
				if (rowCount + 1 == headerRowIndex) {
					List<String> headers = new ArrayList<>();
					if (rowValues == null || rowValues.length == 0) {
						logger.error("No data found in the Header row!");
						rowCount++;
						continue;
					}
					for (int i = 0; i < rowValues.length; i++) {						
						headers.add(rowValues[i]);
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
					List<String> vals = new ArrayList<>();
					if (rowValues != null) {
						for (int i = 0; i < rowValues.length; i++) {
							vals.add(rowValues[i]);
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
			}

			reader.close();
			writer.key(JsonKeys.rows.name()).value(dataRows);
			writer.endObject();
			pw.println(jsonStr.toString());
		} catch (FileNotFoundException e) {
			logger.error("File not found!", e);
		} catch (IOException e) {
			logger.error("Error occured while reading the file!", e);
		} catch (JSONException e) {
			logger.error("Error occured while writing to JSON", e);
		} finally {
			// if(scanner != null)
			// 	scanner.close();
		}
	}
	
	public boolean equals(Object o) {
		return false;
	}

}
