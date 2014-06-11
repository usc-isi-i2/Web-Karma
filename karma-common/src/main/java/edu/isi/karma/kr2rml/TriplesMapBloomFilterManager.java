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

import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Uris;

public class TriplesMapBloomFilterManager {

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
			byte[] serializedBloomFilter = Base64.decodeBase64(base64EncodedBloomFilter);
			KR2RMLBloomFilter bf = new KR2RMLBloomFilter();
			bf.readFields(new ObjectInputStream(new ByteArrayInputStream(serializedBloomFilter)));
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
			builder.append("<");
			builder.append(key);
			builder.append("> <");
			builder.append(Uris.KM_HAS_BLOOMFILTER);
			builder.append("> \"");
			ByteArrayOutputStream baos = new ByteArrayOutputStream(bf.getVectorSize() + 1000);
			ObjectOutputStream dout;
			try {
				dout = new ObjectOutputStream(baos);
				bf.write(dout);
				dout.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}		
			builder.append(Base64.encodeBase64String(baos.toByteArray()));
			builder.append("\" . \n");
		}
		return builder.toString();
	}
	
}
