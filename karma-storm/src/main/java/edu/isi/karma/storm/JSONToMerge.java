package edu.isi.karma.storm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import backtype.storm.tuple.Tuple;
import edu.isi.karma.mapreduce.driver.JSONReducer;

public class JSONToMerge {

	protected long lastUpdateTimestamp;
	protected Map<String, String> jsonBySource;
	protected int expectedSourcesToMerge;
	protected List<Tuple> tuplesToAck;
	public JSONToMerge(int expectedSourcesToMerge)
	{
		this.tuplesToAck = new LinkedList<Tuple>();
		this.expectedSourcesToMerge = expectedSourcesToMerge;
		this.jsonBySource = new HashMap<String, String>(expectedSourcesToMerge);
		this.lastUpdateTimestamp = System.currentTimeMillis();
	}

	public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
		this.lastUpdateTimestamp = lastUpdateTimestamp;
	}
	

	public boolean isReadyToMerge()
	{
		return expectedSourcesToMerge == jsonBySource.size();
	}
	
	public void addJSON(Tuple tuple, String source, String data)
	{
		tuplesToAck.add(tuple);
		jsonBySource.put(source, data);
	}
	
	public String merge()
	{
		return JSONReducer.mergeJSONObjectsFromStrings(jsonBySource.values().iterator()).toString();
	}

	public List<Tuple> getTuplesToAck() {
		return tuplesToAck;
	}
}
