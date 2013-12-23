package edu.isi.karma.kr2rml.formatter;

import org.json.JSONArray;
import org.json.JSONException;

public class KR2RMLJSONPathColumnNameFormatter extends
		KR2RMLColumnNameFormatter {

	@Override
	protected String format(String columnName) {
		StringBuilder result = new StringBuilder();
		try {	
			if (columnName.startsWith("[") && columnName.endsWith("]") && columnName.contains(",")) {
	    		JSONArray strArr = new JSONArray(columnName);
				for (int i=0; i<strArr.length(); i++) {
					result.append(strArr.getString(i));
					result.append(".");
	    		}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected String removeFormatting(String formattedColumnName) {
		// TODO Auto-generated method stub
		return null;
	}


}
