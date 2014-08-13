package edu.isi.karma.imp.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import edu.isi.karma.imp.Import;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.rep.metadata.WorksheetProperties.SourceTypes;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.webserver.KarmaException;

public class CSVImport extends Import {
    private static Logger logger = LoggerFactory.getLogger(CSVImport.class);
	protected final int headerRowIndex;
    protected final int dataStartRowIndex;
    protected final char delimiter;
    protected final char quoteCharacter;
    protected final char escapeCharacter = '\\';
    protected final InputStream is;
    protected final String encoding;
    protected final int maxNumLines;
    protected final JSONArray columnsJson;
    public CSVImport(int headerRowIndex, int dataStartRowIndex,
            char delimiter, char quoteCharacter, String encoding,
            int maxNumLines,
            String sourceName,
            InputStream is,
            Workspace workspace, 
            JSONArray columnsJson) {

        super(sourceName, workspace, encoding);
        this.headerRowIndex = headerRowIndex;
        this.dataStartRowIndex = dataStartRowIndex;
        this.delimiter = delimiter;
        this.quoteCharacter = quoteCharacter;
        this.encoding = encoding;
        this.maxNumLines = maxNumLines;
        this.is = is;
        this.columnsJson = columnsJson;
        
    }
    
    @Override

    public Worksheet generateWorksheet() throws IOException, KarmaException {
        Table dataTable = getWorksheet().getDataTable();

        BufferedReader br = getLineReader();


        // Index for row currently being read
        int rowCount = 0;
        Map<Integer, String> hNodeIdList = new HashMap<Integer, String>();

        // If no row is present for the column headers
        if (headerRowIndex == 0) {
            hNodeIdList = addEmptyHeaders(getWorksheet(), getFactory(), br);
            if (hNodeIdList == null || hNodeIdList.size() == 0) {
                br.close();
                throw new KarmaException("Error occured while counting header "
                        + "nodes for the worksheet!");
            }
        }

        // Populate the worksheet model
        String line = null;
        while ((line = br.readLine()) != null) {
        	logger.debug("Read line: '" + line + "'");
            // Check for the header row
            if (rowCount + 1 == headerRowIndex) {
                hNodeIdList = addHeaders(getWorksheet(), getFactory(), line, br);
                rowCount++;
                continue;
            }

            // Populate the model with data rows
            if (rowCount + 1 >= dataStartRowIndex) {
                boolean added = addRow(getWorksheet(), getFactory(), line, hNodeIdList, dataTable);
                if(added) {
	                rowCount++;
	               
	                if(maxNumLines > 0 && (rowCount - dataStartRowIndex) >= maxNumLines-1) {
	                	break;
	                }
                }
                continue;
            }

            rowCount++;
        }
        br.close();
        getWorksheet().getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.sourceType, SourceTypes.CSV.toString());
        return getWorksheet();
    }

	protected BufferedReader getLineReader() throws IOException {
		// Prepare the reader for reading file line by line
        
        InputStreamReader isr = EncodingDetector.getInputStreamReader(is, encoding);
        
        return new BufferedReader(isr);
	}

    private Map<Integer, String> addHeaders(Worksheet worksheet, RepFactory fac,
            String line, BufferedReader br) throws IOException {
        HTable headers = worksheet.getHeaders();
        Map<Integer, String> headersMap = new HashMap<Integer, String>();
        CSVReader reader = new CSVReader(new StringReader(line), delimiter,
                quoteCharacter, escapeCharacter);
        String[] rowValues = null;
        rowValues = reader.readNext();

        if (rowValues == null || rowValues.length == 0) {
            reader.close();
            return addEmptyHeaders(worksheet, fac, br);
        }

        for (int i = 0; i < rowValues.length; i++) {
            HNode hNode = null;
            if (headerRowIndex == 0) {
            	if (isVisible("Column_" + (i + 1)))
            		hNode = headers.addHNode("Column_" + (i + 1), HNodeType.Regular, worksheet, fac);
            } else {
            	if (isVisible(rowValues[i]))
            		hNode = headers.addHNode(rowValues[i], HNodeType.Regular, worksheet, fac);
            }
            if (hNode != null)
            	headersMap.put(i, hNode.getId());
        }
        reader.close();
        return headersMap;
    }

    private boolean addRow(Worksheet worksheet, RepFactory fac, String line,
    		Map<Integer, String> hNodeIdMap, Table dataTable) throws IOException {
        CSVReader reader = new CSVReader(new StringReader(line), delimiter,
                quoteCharacter, escapeCharacter);
        String[] rowValues = null;
        rowValues = reader.readNext();
        if (rowValues == null || rowValues.length == 0) {
            reader.close();
            return false;
        }

        Row row = dataTable.addRow(fac);
        int size = hNodeIdMap.size();
        if (columnsJson != null)
        	size = columnsJson.length();
        for (int i = 0; i < rowValues.length; i++) {
            if (i < size) {
            	String hNodeId = hNodeIdMap.get(i);
            	if (hNodeId != null)
            		row.setValue(hNodeId, rowValues[i], fac);
            } else {
                // TODO Our model does not allow a value to be added to a row
                // without its associated HNode. In CSVs, there could be case
                // where values in rows are greater than number of column names.
                logger.error("More data elements detected in the row than number of headers!");
            }
        }
        reader.close();
        return true;
    }
    private Map<Integer, String> addEmptyHeaders(Worksheet worksheet,
            RepFactory fac, BufferedReader br) throws IOException {
        HTable headers = worksheet.getHeaders();
        Map<Integer, String> headersMap = new HashMap<Integer, String>();

        
        br.mark(1000000);
        br.readLine();
        
        // Use the first data row to count the number of columns we need to add
        int rowCount = 0;
        String line = null;
        while (null != (line = br.readLine())) {
            if (rowCount + 1 == dataStartRowIndex) {
                line = br.readLine();
                CSVReader reader = new CSVReader(new StringReader(line),
                        delimiter, quoteCharacter, escapeCharacter);
                String[] rowValues = null;
                try {
                    rowValues = reader.readNext();
                } catch (IOException e) {
                    logger.error("Error reading Line:" + line, e);
                }
                for (int i = 0; i < rowValues.length; i++) {
                	
                    HNode hNode = null;
                    if (isVisible("Column_" + (i + 1)))
                    	hNode = headers.addHNode("Column_" + (i + 1), HNodeType.Regular, 
                            worksheet, fac);
                    if (hNode != null)
                    	headersMap.put(i, hNode.getId());
                }
                reader.close();
                break;
            }
            rowCount++;
        }
        br.reset();
        return headersMap;
    }
    
    private boolean isVisible(String key) {
		if (columnsJson == null)
			return true;
		for (int i = 0; i < columnsJson.length(); i++) {
			JSONObject obj = columnsJson.getJSONObject(i);
			if (obj.has(key))
				return obj.getBoolean(key);
		}
		return false;
	}
}