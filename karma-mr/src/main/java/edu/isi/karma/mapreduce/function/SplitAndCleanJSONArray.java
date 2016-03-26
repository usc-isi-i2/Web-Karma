package edu.isi.karma.mapreduce.function;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SplitAndCleanJSONArray extends GenericUDF {
	private static Logger LOG = LoggerFactory.getLogger(MergeJSON.class);
	private ObjectInspectorConverters.Converter[] converters;
	@Override
	public Object evaluate(DeferredObject[] arguments) throws HiveException
	{
		assert(arguments.length == 1);
		Text arrayToCleanText = (Text)converters[0].convert(arguments[0].get());
		List<Text> cleanedValues = new LinkedList<>();
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
			
			Set<Text> cleanedValuesSet = new HashSet<>();
			
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

	@Override
	public String getDisplayString(String[] arguments) {
		assert (arguments.length == 1);
		return "SplitAndCleanJSONArray(" + arguments[0] + ")";
	}
	@Override
	public ObjectInspector initialize(ObjectInspector[] arguments)
			throws UDFArgumentException {
		if (arguments.length != 1) {
			throw new UDFArgumentLengthException("The SplitAndCleanJSONArray takes only one argument");
		}
		converters = new ObjectInspectorConverters.Converter[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			 converters[i] = ObjectInspectorConverters.getConverter(arguments[i], PrimitiveObjectInspectorFactory.writableStringObjectInspector);
		}
		return ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.writableStringObjectInspector);
	}
}
