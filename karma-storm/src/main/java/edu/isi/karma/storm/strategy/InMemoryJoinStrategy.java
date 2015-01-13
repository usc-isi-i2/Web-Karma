package edu.isi.karma.storm.strategy;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryJoinStrategy implements JoinStrategy {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private static Logger LOG = LoggerFactory.getLogger(InMemoryJoinStrategy.class);
	private Map<String, JSONObject> joinTable = new HashMap<String, JSONObject>();

	@Override
	public JSONObject get(String uri) {
		return joinTable.get(uri);
	}

	@Override
	public void config(@SuppressWarnings("rawtypes") Map configMap) {
		String filePath = configMap.get("karma.storm.join.source").toString();
		String atId = configMap.get("karma.context.atid").toString();
//		Configuration conf = new Configuration();
//		try {
//			SequenceFile.Reader reader = new SequenceFile.Reader(conf, SequenceFile.Reader.file(new Path(filePath)));
//			Text key = new Text();
//			Text val = new Text();
//			while(reader.next(key, val))
//			{
//				JSONObject obj = new JSONObject(val.toString());
//				joinTable.put(obj.getString(atId), obj);
//			}
//			reader.close();		
//		}catch(Exception e)
//		{
//			LOG.error("Cannot read source table", e);
//		}

	}

}
