package edu.isi.karma.kr2rml.formatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KR2RMLJSONPathColumnNameFormatter extends
		KR2RMLColumnNameFormatter {

	private static Logger logger = LoggerFactory.getLogger(KR2RMLJSONPathColumnNameFormatter.class);
	
	@Override
	protected String format(String columnName) {
		StringBuilder result = new StringBuilder();
		try {	
			if (columnName.startsWith("[") && columnName.endsWith("]") && columnName.contains(",")) {
	    		JSONArray strArr = new JSONArray(columnName);
				for (int i=0; i<strArr.length(); i++) {
					result.append(strArr.getString(i));
					if(i < strArr.length() - 1)
					{
						result.append(".");
					}
	    		}
				return result.toString();
			}
			else
			{
				return columnName;
			}
		} catch (JSONException e) {
			logger.error("Unable to format column name: " + columnName);
		}
		return result.toString();
	}

	@Override
	protected String removeFormatting(String formattedColumnName) {
		
		String[] components = formattedColumnName.split(".");
		if(components.length == 1)
		{
			//TODO test for array index
			return formattedColumnName;
		}
		else
		{
			JSONArray strArr = new JSONArray();
			for(String component : components)
			{
				//TODO test for array index
				strArr.put(component);
			}
			return strArr.toString();
		}
	}


}
