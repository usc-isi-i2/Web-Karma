package edu.isi.karma.mapreduce.function;

import java.util.List;

import org.apache.hadoop.hive.ql.exec.UDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MergeArrayOfJSON extends UDF {
	
	private static Logger LOG = LoggerFactory.getLogger(MergeJSON.class);
	public String evaluate(String target, List<String> sources, String jsonPath, String atId)
	{
		try
		{
			return MergeJSON.mergeJSON(sources, target, jsonPath, atId);
		}catch(Exception e) {
			LOG.error("something wrong",e );
			return target;
		}
	}
}
