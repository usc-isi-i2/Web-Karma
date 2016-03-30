package edu.isi.karma.kr2rml.writer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.util.bloom.Key;
import org.apache.hadoop.util.hash.Hash;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.modeling.Uris;

public class KR2RMLBloomFilterManager {

	private static final Logger LOG = LoggerFactory.getLogger(KR2RMLBloomFilterManager.class);
	private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
	protected ConcurrentHashMap<String, KR2RMLBloomFilter> idToBloomFilter;
	protected R2RMLMappingIdentifier mappingIdentifier;
	public KR2RMLBloomFilterManager(R2RMLMappingIdentifier mappingIdentifier)
	{
		idToBloomFilter = new ConcurrentHashMap<>();
		this.mappingIdentifier = mappingIdentifier;
	}
	public KR2RMLBloomFilterManager(JSONObject serializedManager) throws IOException
	{
		idToBloomFilter = new ConcurrentHashMap<>();
		String idsConcatenated = serializedManager.getString("ids");
		String[] ids = idsConcatenated.split(",");
		for(String id : ids)
		{
			String base64EncodedBloomFilter = serializedManager.getString(id);
			KR2RMLBloomFilter bf = new KR2RMLBloomFilter();
			bf.populateFromCompressedAndBase64EncodedString(base64EncodedBloomFilter);
			idToBloomFilter.put(id, bf);
		}
		this.mappingIdentifier = new R2RMLMappingIdentifier(serializedManager.getJSONObject("mappingIdentifier"));
	}
	
	public KR2RMLBloomFilter getBloomFilter(String id)
	{
		return idToBloomFilter.get(id);
	}
	public void addUriToBloomFilter(String id, String uri) {
		KR2RMLBloomFilter bf = null;
		if(!idToBloomFilter.containsKey(id))
		{
			idToBloomFilter.putIfAbsent(id, new KR2RMLBloomFilter(KR2RMLBloomFilter.defaultVectorSize, KR2RMLBloomFilter.defaultnbHash, Hash.JENKINS_HASH));
		}
		bf = idToBloomFilter.get(id);
		
		Key k = new Key(uri.getBytes(UTF8_CHARSET));
		bf.add(k);
		return;
	}
	
	public JSONObject toJSON()
	{
		JSONObject filters = new JSONObject();
		StringBuffer ids = new StringBuffer(); 
		for(Entry<String, KR2RMLBloomFilter> entry : idToBloomFilter.entrySet())
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
				LOG.error("Unable to append bloom filter for id: " +key);
				continue;
			}
			if(ids.length() != 0)
			{
				ids.append(",");
			}
			ids.append(entry.getKey());
			
			
		}
		filters.put("ids", ids.toString());
		filters.put("mappingIdentifier", mappingIdentifier.toJSON());
		return filters;
	}
	public String toRDF()
	{
		StringBuilder builder = new StringBuilder();
		for(Entry<String, KR2RMLBloomFilter> entry : idToBloomFilter.entrySet())
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
				LOG.error("Unable to append bloom filter for id: " + key);
				continue;
			}
			tripleBuilder.append("\" . \n");
			builder.append(tripleBuilder);
		}
		return builder.toString();
	}
	
}
