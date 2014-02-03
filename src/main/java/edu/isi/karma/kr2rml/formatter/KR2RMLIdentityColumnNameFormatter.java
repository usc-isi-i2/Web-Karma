package edu.isi.karma.kr2rml.formatter;


public class KR2RMLIdentityColumnNameFormatter extends
		KR2RMLColumnNameFormatter {

	protected String format(String columnName) {
		return columnName;
	}

	public String removeFormatting(String formattedColumnName)
	{
		return formattedColumnName;
	}


}
