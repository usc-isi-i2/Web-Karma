package edu.isi.karma.kr2rml.formatter;

import edu.isi.karma.rep.metadata.WorksheetProperties.SourceTypes;

public class KR2RMLColumnNameFormatterFactory {

	public static KR2RMLColumnNameFormatter getFormatter(SourceTypes sourceType)
	{
		switch(sourceType)
		{
			case JSON:
				return new KR2RMLJSONPathColumnNameFormatter();
			case XML:
				return new KR2RMLXPathColumnNameFormatter();
			case DB:
			case CSV:
			default:
				return new KR2RMLIdentityColumnNameFormatter();
		}
	}
}
