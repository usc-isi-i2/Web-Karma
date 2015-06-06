/**
 * *****************************************************************************
 * Copyright 2012 University of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * This code was developed by the Information Integration Group as part of the
 * Karma project at the Information Sciences Institute of the University of
 * Southern California. For more information, publications, and related
 * projects, please see: http://www.isi.edu/integration
 * ****************************************************************************
 */
package edu.isi.karma.controller.command.importdata;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.controller.update.CSVImportPreviewUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.csv.CSVFileImport;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.EncodingDetector;

public class ImportCSVFileCommand extends ImportFileCommand implements
IPreviewable {

	// Index of the column headers row
	private int headerRowIndex = 1;
	// Index of the row from where data starts
	private int dataStartRowIndex = 2;
	// Column delimiter
	private char delimiter = ',';
	// Quote character
	private char quoteCharacter = '"';
	// Escape character
	private char escapeCharacter = '\\';

	private static Logger logger = LoggerFactory
			.getLogger(ImportCSVFileCommand.class.getSimpleName());

	public void setHeaderRowIndex(int headerRowIndex) {
		this.headerRowIndex = headerRowIndex;
	}

	public void setDataStartRowIndex(int dataStartRowIndex) {
		this.dataStartRowIndex = dataStartRowIndex;
	}

	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}

	public void setQuoteCharacter(char escapeCharacter) {
		this.quoteCharacter = escapeCharacter;
	}

	public void setEscapeCharacter(char escapeCharacter) {
		this.escapeCharacter = escapeCharacter;
	}

	public ImportCSVFileCommand(String id, String model, File file) {
		super(id, model, file);
	}

	public ImportCSVFileCommand(String id, String model, String revisedId, File file) {
		super(id, model, revisedId, file);
	}

	@Override
	public String getTitle() {
		return "Import CSV File";
	}

	@Override
	public String getDescription() {
		if (isExecuted()) {
			return getFile().getName();
		}
		return "";
	}

	@Override
	protected Import createImport(Workspace workspace) {
		JSONArray tree = generateSelectTree(columnsJson, true);
		try {
			return new CSVFileImport(headerRowIndex, dataStartRowIndex,
					delimiter, quoteCharacter, encoding, maxNumLines,
					getFile(), workspace, tree);
		} catch (IOException e) {
			logger.error("Unable to import csv file: " + e.getMessage());
			return null;
		}
	}

	@Override
	protected Import createImport(Workspace workspace, int sampleSize) {
		try {
			return new CSVFileImport(headerRowIndex, dataStartRowIndex,
					delimiter, quoteCharacter, encoding, sampleSize, getFile(),
					workspace, null);
		} catch (IOException e) {
			logger.error("Unable to import csv file: " + e.getMessage());
			return null;
		}
	}

	@Override
	public UpdateContainer showPreview(HttpServletRequest request)
			throws CommandException {
		UpdateContainer c = new UpdateContainer(new CSVImportPreviewUpdate(
				delimiter, quoteCharacter, escapeCharacter, encoding,
				maxNumLines, getFile(), headerRowIndex, dataStartRowIndex, id));
		return c;
	}

	@Override
	public UpdateContainer handleUserActions(HttpServletRequest request) {
		/**
		 * Set the parameters *
		 */
		columnsJson = request.getParameter("columnsJson");
		savePreset = Boolean.parseBoolean(request.getParameter("savePreset"));

		// Set the delimiter
		if (request.getParameter("delimiter").equals("comma")) {
			setDelimiter(',');
		} else if (request.getParameter("delimiter").equals("tab")) {
			setDelimiter('\t');
		} else if (request.getParameter("delimiter").equals("space")) {
			setDelimiter(' ');
		} else if (request.getParameter("delimiter").equals("pipe")) {
			setDelimiter('|');
		} else if (request.getParameter("delimiter").equals("semicolon")) {
			setDelimiter(';');
		} else {
			// TODO What to do with manual text delimiter
		}

		// Set the Header row index
		String headerIndex = request.getParameter("CSVHeaderLineIndex");
		if (headerIndex != "") {
			try {
				int index = Integer.parseInt(headerIndex);
				setHeaderRowIndex(index);
			} catch (Throwable t) {
				// TODO How t do handle illegal user inputs?
				logger.error("Wrong user input for CSV Header line index");
				return null;
			}
		} else {
			setHeaderRowIndex(0);
		}

		// Set the data start row index
		String dataIndex = request.getParameter("startRowIndex");
		if (dataIndex != "") {
			try {
				int index = Integer.parseInt(dataIndex);
				setDataStartRowIndex(index);
			} catch (Throwable t) {
				logger.error("Wrong user input for Data start line index");
				return null;
			}
		} else {
			setDataStartRowIndex(2);
		}

		String strEncoding = request.getParameter("encoding");
		if (strEncoding == null || strEncoding == "") {
			try {
				strEncoding = EncodingDetector.detect(getFile());
			} catch (Exception e) {
				strEncoding = EncodingDetector.DEFAULT_ENCODING;
			}
		}
		setEncoding(strEncoding);

		String maxNumLines = request.getParameter("maxNumLines");
		if (maxNumLines != null && maxNumLines != "") {
			try {
				int num = Integer.parseInt(maxNumLines);
				setMaxNumLines(num);
			} catch (Throwable t) {
				logger.error("Wrong user input for Data Number of Lines to import");
				return null;
			}
		}
		/**
		 * Send response based on the interaction type *
		 */
		UpdateContainer c = null;
		ImportFileInteractionType type = ImportFileInteractionType
				.valueOf(request.getParameter("interactionType"));
		switch (type) {
		case generatePreview: {
			try {
				c = showPreview(request);
			} catch (CommandException e) {
				logger.error("Error occured while creating output", e);
			}
			return c;
		}
		case importTable:
			return c;
		case generateFilter: 
			try {
				return super.showPreview(request);
			} catch (CommandException e) {
				
			}
		}
		return c;
	}
}
