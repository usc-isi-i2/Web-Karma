package edu.isi.karma.kr2rml.formatter;

import edu.isi.karma.rep.metadata.WorksheetProperties.SourceTypes;

public class KR2RMLColumnNameFormatterFactory {

	public static KR2RMLColumnNameFormatter getFormatter(SourceTypes sourceType)
	{
		switch(sourceType)
		{
			case JSON:
			case XML:
			case DB:
			case CSV:
			default:
				return new KR2RMLIdentityColumnNameFormatter();
		}
	}
}
