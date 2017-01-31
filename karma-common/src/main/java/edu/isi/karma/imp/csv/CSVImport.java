package edu.isi.karma.imp.csv;

import com.opencsv.CSVReader;

import edu.isi.karma.imp.Import;
import edu.isi.karma.rep.*;
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.rep.metadata.WorksheetProperties.SourceTypes;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.webserver.KarmaException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CSVImport extends Import {
    private static Logger logger = LoggerFactory.getLogger(CSVImport.class);
    protected final int headerRowIndex;
    protected final int dataStartRowIndex;
    protected final char delimiter;
    protected final char quoteCharacter;
    protected final char escapeCharacter;
    protected final InputStream is;
    protected final String encoding;
    protected final int maxNumLines;
    protected final JSONArray columnsJson;
    protected final String sourceName;
    
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
        this.sourceName = sourceName;
        this.delimiter = delimiter;
        // Trick:
        // Passing quoteCharacter as $ signals that we don't want any quote character
        // Required because CSVReader constructor doesn't take ignoreQuotation (as does CSVParser), sigh
        if(quoteCharacter == '$') {
            this.quoteCharacter = '\0';
            this.escapeCharacter = '\0';
        } else {
            this.escapeCharacter = '\\';
            this.quoteCharacter = quoteCharacter;
        }
        
        this.encoding = encoding;
        this.maxNumLines = maxNumLines;
        this.is = is;
        this.columnsJson = columnsJson;
        
    }
    
    public CSVImport duplicate() throws IOException {
    	return new CSVImport(headerRowIndex, dataStartRowIndex, delimiter, quoteCharacter, encoding, maxNumLines, sourceName, is, workspace, columnsJson);
    }
    
    @Override

    public Worksheet generateWorksheet() throws IOException, KarmaException {
        Table dataTable = getWorksheet().getDataTable();

        // Index for row currently being read
        int rowCount = 0;
        Map<Integer, String> hNodeIdList = new HashMap<>();

        CSVReader reader = getCSVReader();

        // Populate the worksheet model
        String[] rowValues = null;
        while ((rowValues = reader.readNext()) != null) {
            // logger.debug("Read line: '" + line + "'");
            // Check for the header row
            if (rowCount + 1 == headerRowIndex) {
                hNodeIdList = addHeaders(getWorksheet(), getFactory(), rowValues, reader);
                rowCount++;
                continue;
            }

            // Populate the model with data rows
            if (rowCount + 1 >= dataStartRowIndex) {
                boolean added = addRow(getWorksheet(), getFactory(), rowValues, hNodeIdList, dataTable);
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
        reader.close();
        getWorksheet().getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.sourceType, SourceTypes.CSV.toString());
        return getWorksheet();
    }

    protected BufferedReader getLineReader() throws IOException {
        // Prepare the reader for reading file line by line
        InputStreamReader isr = EncodingDetector.getInputStreamReader(is, encoding);
        return new BufferedReader(isr);
    }

    protected CSVReader getCSVReader() throws IOException {
        BufferedReader br = getLineReader();
        return new CSVReader(br, delimiter, quoteCharacter, escapeCharacter);
    }

    private Map<Integer, String> addHeaders(Worksheet worksheet, RepFactory fac,
            String[] rowValues, CSVReader reader) throws IOException {
        HTable headers = worksheet.getHeaders();
        Map<Integer, String> headersMap = new HashMap<>();

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

        return headersMap;
    }

    private boolean addRow(Worksheet worksheet, RepFactory fac, String[] rowValues,
            Map<Integer, String> hNodeIdMap, Table dataTable) throws IOException {

        if (rowValues == null || rowValues.length == 0) {
            return false;
        }

        Row row = dataTable.addRow(fac);
        int size = hNodeIdMap.size();
        if (columnsJson != null)
            size = columnsJson.length();
        for (int i = 0; i < rowValues.length; i++) {
            if(i >= size) {
                HTable headers = worksheet.getHeaders();
                HNode hNode = headers.addHNode("Column_" + (i + 1), HNodeType.Regular, worksheet, fac);
                hNodeIdMap.put(i, hNode.getId());
                size = hNodeIdMap.size();
            }
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
        return true;
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