package edu.isi.karma.controller.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import edu.isi.karma.controller.update.CSVImportPreviewUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class ImportCSVFileCommand extends CommandWithPreview {
	
	File csvFile;
	
	// Index of the column headers row 
	private int headerRowIndex = 1;
	
	// Index of the row from where data starts
	private int dataStartRowIndex =2;
	
	// Column delimiter
	private char delimiter = ',';
	
	// Quote character
	private char quoteCharacter = '"';
	
	// Escape character
	private char escapeCharacter = '\\';
	
	private VWorkspace vWorkspace;
	
	protected enum InteractionType {
		generatePreview, importTable
	}
	
	// Logger object
	private static Logger logger = LoggerFactory.getLogger(ImportCSVFileCommand.class.getSimpleName());
	
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

	public ImportCSVFileCommand(String id, File file, VWorkspace vWorkspace) {
		super(id);
		this.csvFile = file;
		this.vWorkspace = vWorkspace;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Import CSV File";
	}

	@Override
	public String getDescription() {
		if (isExecuted()) {
			return csvFile.getName() + " imported";
		} else {
			return "";
		}
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Workspace ws = vWorkspace.getWorkspace();
		Worksheet wsht = generateWorksheet(ws);
		UpdateContainer c = new UpdateContainer();
		
		vWorkspace.addAllWorksheets();
		
		c.add(new WorksheetListUpdate(vWorkspace.getVWorksheetList()));
		VWorksheet vw = vWorkspace.getVWorksheet(wsht.getId());
		vw.update(c);
		return c;
	}

	private Worksheet generateWorksheet(Workspace ws) {
		RepFactory fac = ws.getFactory();
		Worksheet worksheet = fac.createWorksheet(csvFile.getName(), ws);
		Table dataTable = worksheet.getDataTable();
		
		// Prepare the scanner for reading file line by line
		Scanner scanner = null;
		try {
			scanner = new Scanner(csvFile);
		} catch (FileNotFoundException e1) {
			logger.error("Problem occured while reading CSV File " + csvFile.getName() ,e1);
			return null;
		}
		
		// Index for row currently being read
		int rowCount = 0;
		ArrayList<String> hNodeIdList = new ArrayList<String>();
		
		// If no row is present for the column headers
		if(headerRowIndex == 0)
			hNodeIdList = addEmptyHeaders(worksheet, fac);
		
		// Populate the worksheet model
		while (scanner.hasNextLine()) {
        	// Check for the header row
        	if(rowCount+1 == headerRowIndex){
        		String line = scanner.nextLine();
        		hNodeIdList = addHeaders(worksheet, fac, line);     
	            rowCount++;
	            continue;
        	}
            
	        // Populate the model with data rows
        	if(rowCount+1 >= dataStartRowIndex) {
        		String line = scanner.nextLine();
        		addRow(worksheet, fac, line, hNodeIdList, dataTable);
	            rowCount++;
	            continue;
        	}
        	
            rowCount++;
            scanner.nextLine();
        }
		
		return worksheet;
	}

	private ArrayList<String> addEmptyHeaders(Worksheet worksheet,
			RepFactory fac) {
		HTable headers = worksheet.getHeaders();
		ArrayList<String> headersList = new ArrayList<String>();
		
		Scanner scanner = null;
		try {
			scanner = new Scanner(csvFile);
		} catch (FileNotFoundException e1) {
			logger.error("Problem occured while reading CSV File " + csvFile.getName() ,e1);
			return null;
		}
		
		// Use the first data row to count the number of columns we need to add
		int rowCount = 0;
		while(scanner.hasNext()){
			if(rowCount+1 == dataStartRowIndex) {
				String line = scanner.nextLine();
				CSVReader reader = new CSVReader(new StringReader(line), delimiter, quoteCharacter, escapeCharacter);
		        String[] rowValues = null;
				try {
					rowValues = reader.readNext();
				} catch (IOException e) { 
					logger.error("Error reading Line:" + line, e);
				}
		        for(int i=0; i<rowValues.length; i++){
		        	HNode hNode = headers.addHNode("Column_" + (i+1), worksheet, fac);
		        	headersList.add(hNode.getId());
		        }
			}
			rowCount++;
			scanner.nextLine();
		}
		return headersList;
	}

	private ArrayList<String> addHeaders(Worksheet worksheet, RepFactory fac, String line) {
		HTable headers = worksheet.getHeaders();
		ArrayList<String> headersList = new ArrayList<String>();
		CSVReader reader = new CSVReader(new StringReader(line), delimiter, quoteCharacter, escapeCharacter);
        String[] rowValues = null;
		try {
			rowValues = reader.readNext();
		} catch (IOException e) { 
			logger.error("Error reading Headers Line:" + line, e);
		}
		if(rowValues == null || rowValues.length == 0)
			return addEmptyHeaders(worksheet, fac);
        for(int i=0; i<rowValues.length; i++){
        	HNode hNode = null;
        	if(headerRowIndex == 0)
        		hNode = headers.addHNode("Column_" + (i+1), worksheet, fac);
        	else
        		hNode = headers.addHNode(rowValues[i], worksheet, fac);
        	headersList.add(hNode.getId());
        }
		return headersList;
	}
	
	private void addRow(Worksheet worksheet, RepFactory fac, String line,
			ArrayList<String> hNodeIdList, Table dataTable) {
		CSVReader reader = new CSVReader(new StringReader(line), delimiter, quoteCharacter, escapeCharacter);
        String[] rowValues = null;
		try {
			rowValues = reader.readNext();
			if(rowValues == null || rowValues.length == 0)
				return;
			Row row = dataTable.addRow(fac);
			for(int i=0; i<rowValues.length; i++) {
				if(i < hNodeIdList.size())
					row.setValue(hNodeIdList.get(i), rowValues[i]);
				else {
					// TODO Our model does not allow a value to be added to a row without its
					// associated HNode. In CSVs, there could be case where values in rows are
					// greater than number of column names.
					logger.error("More data elements detected in the row than number of headers!");
				}
			}
		} catch (IOException e) {
			logger.error("Error reading Line: " + line, e);
		}
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// Do nothing!
		return null;
	}

	@Override
	public UpdateContainer showPreview(VWorkspace vWorkspace)
			throws CommandException {
		UpdateContainer c = new UpdateContainer();
		c.add(new CSVImportPreviewUpdate(delimiter, 
				quoteCharacter, escapeCharacter, csvFile, headerRowIndex, dataStartRowIndex, id));
		return c;
	}

	@Override
	public UpdateContainer handleUserActions(HttpServletRequest request) {
		/** Set the parameters **/
		// Set the delimiter
		if(request.getParameter("delimiter").equals("comma"))
			setDelimiter(',');
		else if(request.getParameter("delimiter").equals("tab"))
			setDelimiter('\t');
		else if(request.getParameter("delimiter").equals("space"))
			setDelimiter(' ');
		else {
			// TODO What to do with manual text delimiter
		}
		
		// Set the Header row index
		String headerIndex = request.getParameter("CSVHeaderLineIndex");
		if(headerIndex != "") {
			try{
				int index = Integer.parseInt(headerIndex);
				setHeaderRowIndex(index);
			} catch(Throwable t) {
				// TODO How t do handle illegal user inputs?
				logger.error("Wrong user input for CSV Header line index");
				return null;
			}
		} else
			setHeaderRowIndex(0);
		
		// Set the data start row index
		String dataIndex = request.getParameter("startRowIndex");
		if(dataIndex != "") {
			try{
				int index = Integer.parseInt(dataIndex);
				setDataStartRowIndex(index);
			} catch(Throwable t) {
				logger.error("Wrong user input for Data start line index");
				return null;
			}
		} else
			setDataStartRowIndex(2);
		
		/** Send response based on the interaction type **/
		UpdateContainer c = null;
		InteractionType type = InteractionType.valueOf(request.getParameter("interactionType"));
		switch (type) {
		case generatePreview: {
			try {
				c = showPreview(vWorkspace);
			} catch (CommandException e) {
				logger.error("Error occured while creating utput JSON for CSV Import", e);
			}
			return c;
		}
		case importTable: 
			return c;
		}
		return c;
	}
}
