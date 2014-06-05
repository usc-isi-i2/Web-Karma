package edu.isi.karma.kr2rml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.util.bloom.BloomFilter;
import org.apache.hadoop.util.bloom.Key;
import org.apache.hadoop.util.hash.Hash;
import org.json.JSONObject;

public class TriplesMapBloomFilterManager {

	ConcurrentHashMap<String, BloomFilter> triplesMapsIdToBloomFilter;
	
	public TriplesMapBloomFilterManager()
	{
		triplesMapsIdToBloomFilter = new ConcurrentHashMap<String, BloomFilter>();
	}
	public TriplesMapBloomFilterManager(JSONObject serializedManager) throws IOException
	{
		triplesMapsIdToBloomFilter = new ConcurrentHashMap<String, BloomFilter>();
		String triplesMapsIdsConcatenated = serializedManager.getString("triplesMapsIds");
		String[] triplesMapsIds = triplesMapsIdsConcatenated.split(",");
		for(String triplesMapsId : triplesMapsIds)
		{
			String base64EncodedBloomFilter = serializedManager.getString(triplesMapsId);
			byte[] serializedBloomFilter = Base64.decodeBase64(base64EncodedBloomFilter);
			BloomFilter bf = new BloomFilter();
			bf.readFields(new ObjectInputStream(new ByteArrayInputStream(serializedBloomFilter)));
			triplesMapsIdToBloomFilter.put(triplesMapsId, bf);
		}
	}
	
	public BloomFilter getBloomFilter(String triplesMapId)
	{
		return triplesMapsIdToBloomFilter.get(triplesMapId);
	}
	public void addUriToBloomFilter(String subjTriplesMapId, String subjUri) {
		BloomFilter bf = null;
		if(!triplesMapsIdToBloomFilter.containsKey(subjTriplesMapId))
		{
			triplesMapsIdToBloomFilter.putIfAbsent(subjTriplesMapId, new BloomFilter(1000000, 8,Hash.JENKINS_HASH));
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
		for(Entry<String, BloomFilter> entry : triplesMapsIdToBloomFilter.entrySet())
		{
			BloomFilter bf = entry.getValue();
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
