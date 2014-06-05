package edu.isi.karma.kr2rml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.util.bloom.Key;
import org.apache.hadoop.util.hash.Hash;
import org.json.JSONObject;

public class TriplesMapBloomFilterManager {

	ConcurrentHashMap<String, KR2RMLBloomFilter> triplesMapsIdToBloomFilter;
	
	public TriplesMapBloomFilterManager()
	{
		triplesMapsIdToBloomFilter = new ConcurrentHashMap<String, KR2RMLBloomFilter>();
	}
	public TriplesMapBloomFilterManager(JSONObject serializedManager) throws IOException
	{
		triplesMapsIdToBloomFilter = new ConcurrentHashMap<String, KR2RMLBloomFilter>();
		String triplesMapsIdsConcatenated = serializedManager.getString("triplesMapsIds");
		String[] triplesMapsIds = triplesMapsIdsConcatenated.split(",");
		for(String triplesMapsId : triplesMapsIds)
		{
			String base64EncodedBloomFilter = serializedManager.getString(triplesMapsId);
			byte[] serializedBloomFilter = Base64.decodeBase64(base64EncodedBloomFilter);
			KR2RMLBloomFilter bf = new KR2RMLBloomFilter();
			bf.readFields(new ObjectInputStream(new ByteArrayInputStream(serializedBloomFilter)));
			triplesMapsIdToBloomFilter.put(triplesMapsId, bf);
		}
	}
	
	public KR2RMLBloomFilter getBloomFilter(String triplesMapId)
	{
		return triplesMapsIdToBloomFilter.get(triplesMapId);
	}
	public void addUriToBloomFilter(String subjTriplesMapId, String subjUri) {
		KR2RMLBloomFilter bf = null;
		if(!triplesMapsIdToBloomFilter.containsKey(subjTriplesMapId))
		{
			triplesMapsIdToBloomFilter.putIfAbsent(subjTriplesMapId, new KR2RMLBloomFilter(1000000, 8,Hash.JENKINS_HASH));
		}
		bf = triplesMapsIdToBloomFilter.get(subjTriplesMapId);
		
		Key k = new Key(subjUri.getBytes());
		bf.add(k);
		return;
	}
	
	public JSONObject toJSON()
	{
		JSONObject filters = new JSONObject();
		StringBuffer triplesMapsIds = new StringBuffer(); 
		for(Entry<String, KR2RMLBloomFilter> entry : triplesMapsIdToBloomFilter.entrySet())
		{
			KR2RMLBloomFilter bf = entry.getValue();
			ByteArrayOutputStream baos = new ByteArrayOutputStream(bf.getVectorSize() + 1000);
			
			try {
				ObjectOutputStream dout = new ObjectOutputStream(baos);
				bf.write(dout);
				dout.flush();
				filters.put(entry.getKey(), Base64.encodeBase64String(baos.toByteArray()));
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(triplesMapsIds.length() != 0)
			{
				triplesMapsIds.append(",");
			}
			triplesMapsIds.append(entry.getKey());
			
		}
		filters.put("triplesMapsIds", triplesMapsIds.toString());
		return filters;
	}
}
