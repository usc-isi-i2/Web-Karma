package edu.isi.karma.storm.function;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;

import backtype.storm.tuple.Tuple;
import edu.isi.karma.util.JSONLDUtilSimple;

public class JSONToMerge {

	protected long lastUpdateTimestamp;
	protected Map<String, String> jsonBySource;
	protected int expectedSourcesToMerge;
	protected List<Tuple> tuplesToAck;
	public JSONToMerge(int expectedSourcesToMerge)
	{
		this.tuplesToAck = new LinkedList<>();
		this.expectedSourcesToMerge = expectedSourcesToMerge;
		this.jsonBySource = new HashMap<>(expectedSourcesToMerge);
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
	
	public String merge() throws ParseException
	{
		return JSONLDUtilSimple.mergeJSONObjects(jsonBySource.values().iterator()).toString();
	}

	public List<Tuple> getTuplesToAck() {
		return tuplesToAck;
	}
}
