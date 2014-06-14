package edu.isi.karma.kr2rml;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.util.bloom.Key;
import org.apache.hadoop.util.hash.Hash;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.modeling.Uris;

public class TriplesMapBloomFilterManager {

	private static final Logger LOG = LoggerFactory.getLogger(TriplesMapBloomFilterManager.class);
	protected ConcurrentHashMap<String, KR2RMLBloomFilter> triplesMapsIdToBloomFilter;
	protected R2RMLMappingIdentifier mappingIdentifier;
	public TriplesMapBloomFilterManager(R2RMLMappingIdentifier mappingIdentifier)
	{
		triplesMapsIdToBloomFilter = new ConcurrentHashMap<String, KR2RMLBloomFilter>();
		this.mappingIdentifier = mappingIdentifier;
	}
	public TriplesMapBloomFilterManager(JSONObject serializedManager) throws IOException
	{
		triplesMapsIdToBloomFilter = new ConcurrentHashMap<String, KR2RMLBloomFilter>();
		String triplesMapsIdsConcatenated = serializedManager.getString("triplesMapsIds");
		String[] triplesMapsIds = triplesMapsIdsConcatenated.split(",");
		for(String triplesMapsId : triplesMapsIds)
		{
			String base64EncodedBloomFilter = serializedManager.getString(triplesMapsId);
			KR2RMLBloomFilter bf = new KR2RMLBloomFilter();
			bf.populateFromCompressedAndBase64EncodedString(base64EncodedBloomFilter);
			triplesMapsIdToBloomFilter.put(triplesMapsId, bf);
		}
		this.mappingIdentifier = new R2RMLMappingIdentifier(serializedManager.getJSONObject("mappingIdentifier"));
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
			String key = entry.getKey();
			KR2RMLBloomFilter bf = entry.getValue();
			
			try
			{
				String base64EncodedCompressedSerializedBloomFilter = bf.compressAndBase64Encode();
				filters.put(key, base64EncodedCompressedSerializedBloomFilter);
			}
			catch (IOException e)
			{
				LOG.error("Unable to append bloom filter for triples map: " +key);
				continue;
			}
			if(triplesMapsIds.length() != 0)
			{
				triplesMapsIds.append(",");
			}
			triplesMapsIds.append(entry.getKey());
			
			
		}
		filters.put("triplesMapsIds", triplesMapsIds.toString());
		filters.put("mappingIdentifier", mappingIdentifier.toJSON());
		return filters;
	}
	public String toRDF()
	{
		StringBuilder builder = new StringBuilder();
		for(Entry<String, KR2RMLBloomFilter> entry : triplesMapsIdToBloomFilter.entrySet())
		{
			KR2RMLBloomFilter bf = entry.getValue();
			String key = entry.getKey();
			StringBuilder tripleBuilder = new StringBuilder();
			tripleBuilder.append("<");
			tripleBuilder.append(key);
			tripleBuilder.append("> <");
			tripleBuilder.append(Uris.KM_HAS_BLOOMFILTER);
			tripleBuilder.append("> \"");
			try
			{
			String base64EncodedCompressedSerializedBloomFilter = bf.compressAndBase64Encode();
				tripleBuilder.append(base64EncodedCompressedSerializedBloomFilter);
			}
			catch (IOException e)
			{
				LOG.error("Unable to append bloom filter for triples map: " + key);
				continue;
			}
			tripleBuilder.append("\" . \n");
			builder.append(tripleBuilder);
		}
		return builder.toString();
	}
	
}
