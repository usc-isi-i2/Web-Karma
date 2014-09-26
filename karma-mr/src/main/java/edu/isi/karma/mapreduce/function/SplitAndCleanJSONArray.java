package edu.isi.karma.mapreduce.function;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SplitAndCleanJSONArray extends UDF {
	private static Logger LOG = LoggerFactory.getLogger(MergeJSON.class);
	public List<Text> evaluate(Text arrayToCleanText)
	{
		List<Text> cleanedValues = new LinkedList<Text>();
		if(arrayToCleanText == null)
		{
			return cleanedValues;
		}
		try{
			String arrayToClean = arrayToCleanText.toString();
			if(arrayToClean.startsWith("[") && arrayToClean.endsWith("]"))
			{
				arrayToClean = arrayToClean.substring(1,arrayToClean.length()-1);
			}
			
			Set<Text> cleanedValuesSet = new HashSet<Text>();
			
			String[] values = arrayToClean.split(",");
			for(String value : values)
			{
				cleanedValuesSet.add(new Text(value.replace("\"", "")));
			}
			
			cleanedValues.addAll(cleanedValuesSet);
		}
		catch(Exception e)
		{
			LOG.error("Unabled to split and clean array",e.getMessage());
		}
		return cleanedValues;
		
	}
}
