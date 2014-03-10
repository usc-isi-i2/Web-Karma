package edu.isi.karma.kr2rml.mapping;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.exception.HNodeNotFoundKarmaException;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;

public class KR2RMLMappingColumnNameHNodeTranslator {
	
	private static Logger logger = LoggerFactory.getLogger(KR2RMLMappingGenerator.class);
	
	private RepFactory factory;
	private Worksheet worksheet;
	private Map<String, String> hNodeIdToColumnName = new HashMap<String, String>();
	private Map<String, String> columnNameToHNodeId  = new HashMap<String, String>();
	
	public KR2RMLMappingColumnNameHNodeTranslator(RepFactory factory, Worksheet worksheet)
	{
		this.factory = factory;
		this.worksheet = worksheet;
		this.populateHNodeIdAndColumnNameMaps();
	}
	
	public String getColumnNameForHNodeId(String hNodeId)
	{
		if(!this.hNodeIdToColumnName.containsKey(hNodeId))
		{
			String columnName = translateHNodeIdToColumnName(hNodeId);
			if(null != columnName)
			{
				hNodeIdToColumnName.put(hNodeId, columnName);
				columnNameToHNodeId.put(columnName, hNodeId);
			}
		}
		return hNodeIdToColumnName.get(hNodeId);
	}
	
	public String translateHNodeIdToColumnName(String hNodeId)
	{
		HNode hNode = factory.getHNode(hNodeId);
		String colNameStr = "";
		try {
			JSONArray colNameArr = hNode.getJSONArrayRepresentation(factory);
			if (colNameArr.length() == 1) {
				colNameStr = (String) 
						(((JSONObject)colNameArr.get(0)).get("columnName"));
			} else {
				JSONArray colNames = new JSONArray();
				for (int i=0; i<colNameArr.length();i++) {
					colNames.put((String)
							(((JSONObject)colNameArr.get(i)).get("columnName")));
				}
				colNameStr = colNames.toString();
			}
			return colNameStr;
		} catch (JSONException e) {
			logger.debug("unable to find hnodeid to column name mapping for hnode: " + hNode.getId() + " " + hNode.getColumnName(), e);
		}
		return null;
	}

	public void populateHNodeIdAndColumnNameMaps()
	{
		HTable hTable = worksheet.getHeaders();
		populateHNodeIdAndColumnNameMapsForHTable(hTable);
	}
	private void populateHNodeIdAndColumnNameMapsForHTable(HTable hTable) {
		for(HNode hNode : hTable.getHNodes())
		{
			if(hNode.hasNestedTable())
			{
				populateHNodeIdAndColumnNameMapsForHTable(hNode.getNestedTable());
			}
			else
			{
				String hNodeId = hNode.getId();
				String columnName = translateHNodeIdToColumnName(hNodeId);
				columnNameToHNodeId.put(columnName, hNodeId);
				hNodeIdToColumnName.put(hNodeId, columnName);
			}
		}
	}
	public String getHNodeIdForColumnName(String templateTermValue) throws HNodeNotFoundKarmaException {
		if(!this.columnNameToHNodeId.containsKey(templateTermValue))
		{
			try {
				String hNodeId = translateColumnNameToHNodeId(templateTermValue);
				columnNameToHNodeId.put(templateTermValue, hNodeId);
				hNodeIdToColumnName.put(hNodeId, templateTermValue);
				
			} catch (JSONException e) {
				throw new HNodeNotFoundKarmaException("Unable to find HNodeId for column name", templateTermValue);
			}
		}
		return this.columnNameToHNodeId.get(templateTermValue);
	}
	
	private String translateColumnNameToHNodeId(String colTermVal) throws JSONException
	{
		HTable hTable = worksheet.getHeaders();
    	// If hierarchical columns
    	if (colTermVal.startsWith("[") && colTermVal.endsWith("]") && colTermVal.contains(",")) {
    		JSONArray strArr = new JSONArray(colTermVal);
    		for (int i=0; i<strArr.length(); i++) {
				String cName = (String) strArr.get(i);
				
				logger.debug("Column being normalized: "+ cName);
				HNode hNode = hTable.getHNodeFromColumnName(cName);
				if(hNode == null || hTable == null) {
					logger.error("Error retrieving column: " + cName);
					return null;
				}
				
				if (i == strArr.length()-1) {		// Found!
					return hNode.getId();
				} else {
					hTable = hNode.getNestedTable();
				}
    		}
    	} else {
    		HNode hNode = hTable.getHNodeFromColumnName(colTermVal);
    		logger.debug("Column" +colTermVal);
    		return hNode.getId();
    	}
    	return null;
	}
}
