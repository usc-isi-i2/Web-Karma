package edu.isi.karma.er.helper;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.util.bloom.BloomFilter;
import org.apache.hadoop.util.hash.Hash;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.writer.KR2RMLBloomFilter;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.util.HTTPUtil;
import edu.isi.karma.webserver.KarmaException;

public class BloomFilterTripleStoreUtil extends TripleStoreUtil {

	private static Logger logger = LoggerFactory
			.getLogger(BloomFilterTripleStoreUtil.class);
	public boolean processBloomFilters(String modelContext,
			String modelRepoUrl,
			Map<String, String> bloomfilterMapping, 
			JSONObject obj) {
		boolean result = true;
		try{
		result &= updateTripleStore(obj, bloomfilterMapping, modelRepoUrl, modelContext);
		Map<String, String> verification = new HashMap<>();
		Set<String> triplemaps = new HashSet<>(Arrays.asList(obj.getString("ids").split(",")));
		boolean verify = verify(verification, triplemaps,modelRepoUrl ,modelContext,obj);
		if (!verify) {
			result &= updateTripleStore(obj, verification, modelRepoUrl, modelContext);
		}
		
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
			result=false;
		}
		return result;
	}

	public boolean verify(Map<String, String> verification, Set<String> triplemaps, String modelRepoUrl, String modelContext, JSONObject  obj) throws KarmaException, IOException
	{
		BloomFilterTripleStoreUtil bloomFilterUtilObj = new BloomFilterTripleStoreUtil();
		verification.putAll(bloomFilterUtilObj.getBloomFiltersForMaps(modelRepoUrl, modelContext, triplemaps));
		boolean verify = true;
		for (Entry<String, String> entry : verification.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			KR2RMLBloomFilter bf2 = new KR2RMLBloomFilter(KR2RMLBloomFilter.defaultVectorSize, KR2RMLBloomFilter.defaultnbHash, Hash.JENKINS_HASH);
			KR2RMLBloomFilter bf = new KR2RMLBloomFilter(KR2RMLBloomFilter.defaultVectorSize, KR2RMLBloomFilter.defaultnbHash, Hash.JENKINS_HASH);
			bf2.populateFromCompressedAndBase64EncodedString(value);
			bf.populateFromCompressedAndBase64EncodedString(obj.getString(key));
			bf2.and(bf);
			bf2.xor(bf);
			try {
				Field f1 = BloomFilter.class.getDeclaredField("bits");
				f1.setAccessible(true);
				BitSet bits = (BitSet) f1.get(bf2);
				if (bits.cardinality() != 0) {
					verify = false;
					break;
				}
			} catch (Exception e) {
				
			}
		}
		return verify;
	}

	public boolean updateTripleStore(JSONObject obj, Map<String, String> bloomfilterMapping, String modelRepoUrl, String modelContext) throws KarmaException, IOException {
		Set<String> triplemaps = new HashSet<>(Arrays.asList(obj.getString("ids").split(",")));
		bloomfilterMapping.putAll(getBloomFiltersForMaps(modelRepoUrl, modelContext, triplemaps));
		Map<String, KR2RMLBloomFilter> bfs = new HashMap<>();
		for (String tripleUri : triplemaps) {
			String serializedBloomFilter = obj.getString(tripleUri);
			KR2RMLBloomFilter bf = new KR2RMLBloomFilter();
			bf.populateFromCompressedAndBase64EncodedString(serializedBloomFilter);
			bfs.put(tripleUri, bf);
		}
		return updateTripleStoreWithBloomFilters(bfs, bloomfilterMapping, modelRepoUrl, modelContext);
	}
	public Map<String, String> getBloomFiltersForMaps(String tripleStoreURL, String context, Collection<String> maps) throws KarmaException
	{
		tripleStoreURL = normalizeTripleStoreURL(tripleStoreURL);
		testTripleStoreConnection(tripleStoreURL);

		Map<String, String> bloomfilters = new HashMap<>();
		try {

			StringBuilder query = new StringBuilder();
			query.append("PREFIX km-dev:<http://isi.edu/integration/karma/dev#>\n");
			query.append("PREFIX rr:<http://www.w3.org/ns/r2rml#>\n");
			query.append("SELECT ?bf ?s \n");			
			injectContext(context, query);
			query.append("WHERE \n{\n");
			Iterator<String> iterator = maps.iterator();
			while(iterator.hasNext()) {
				query.append("{");
				query.append("\n ?s <");
				query.append(Uris.KM_HAS_BLOOMFILTER);
				query.append("> ?bf . ");
				query.append("\n<");
				query.append(iterator.next());
				query.append("> <");
				query.append(Uris.KM_HAS_BLOOMFILTER);
				if (iterator.hasNext())
					query.append("> ?bf . \n} UNION \n");
				else
					query.append("> ?bf . \n} \n");
			}
			query.append("}\n");

			String queryString = query.toString();
			logger.debug("query: " + queryString);


			Map<String, String> formparams = new HashMap<>();
			formparams.put("query", queryString);
			formparams.put("queryLn", "SPARQL");

			String responseString = HTTPUtil.executeHTTPPostRequest(
					tripleStoreURL, null, "application/sparql-results+json",
					formparams);
			if (responseString != null) {
				JSONObject models = new JSONObject(responseString);
				JSONArray values = models.getJSONObject("results")
						.getJSONArray("bindings");
				int count = 0;
				while (count < values.length()) {
					JSONObject o = values.getJSONObject(count++);
					bloomfilters.put(o.getJSONObject("s").getString("value"), o.getJSONObject("bf").getString("value"));
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return bloomfilters;
	}

	public void deleteBloomFiltersForMaps(String tripleStoreURL, String context, Collection<String> maps) throws KarmaException
	{
		testTripleStoreConnection(tripleStoreURL);
		tripleStoreURL = normalizeTripleStoreURL(tripleStoreURL) + "/statements";		

		try {

			StringBuilder query = new StringBuilder();
			query.append("PREFIX km-dev:<http://isi.edu/integration/karma/dev#>\n");
			query.append("PREFIX rr:<http://www.w3.org/ns/r2rml#>\n");
			if (null != context && !context.trim().isEmpty())
			{
				query.append("WITH ");
				formatURI(context, query);
				query.append("\n");
			}
			query.append("DELETE {?s km-dev:hasBloomFilter ?bf} \n");			
			query.append("WHERE \n{\n");
			Iterator<String> iterator = maps.iterator();
			while(iterator.hasNext()) {
				query.append("{");
				query.append("\n ?s <");
				query.append(Uris.KM_HAS_BLOOMFILTER);
				query.append("> ?bf . ");
				query.append("\n<");
				query.append(iterator.next());
				query.append("> <");
				query.append(Uris.KM_HAS_BLOOMFILTER);
				if (iterator.hasNext())
					query.append("> ?bf . \n} UNION \n");
				else
					query.append("> ?bf . \n} \n");
			}
			query.append("}\n");

			String queryString = query.toString();
			logger.debug("query: " + queryString);


			Map<String, String> formparams = new HashMap<>();
			formparams.put("update", queryString);

			String responseString = HTTPUtil.executeHTTPPostRequest(
					tripleStoreURL, null, mime_types.get(RDF_Types.N3.name()),
					formparams);
			System.out.println(responseString);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public boolean updateTripleStoreWithBloomFilters(Map<String, KR2RMLBloomFilter> bfs, Map<String, String> bloomfilterMapping, String modelurl, String context) throws KarmaException, IOException {
		Set<String> triplemaps = bfs.keySet();
		for (Entry<String, KR2RMLBloomFilter> stringKR2RMLBloomFilterEntry : bfs.entrySet()) {
			KR2RMLBloomFilter bf = stringKR2RMLBloomFilterEntry.getValue();
			String oldserializedBloomFilter = bloomfilterMapping.get(stringKR2RMLBloomFilterEntry.getKey());
			if (oldserializedBloomFilter != null) {
				KR2RMLBloomFilter bf2 = new KR2RMLBloomFilter();
				bf2.populateFromCompressedAndBase64EncodedString(oldserializedBloomFilter);
				bf.or(bf2);
			}
			bfs.put(stringKR2RMLBloomFilterEntry.getKey(), bf);
		}
		deleteBloomFiltersForMaps(modelurl, null, triplemaps);
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		for (Entry<String, KR2RMLBloomFilter> entry : bfs.entrySet()) {
			pw.print("<" + entry.getKey() + "> ");
			pw.print("<" + Uris.KM_HAS_BLOOMFILTER + "> ");
			pw.println("\"" + entry.getValue().compressAndBase64Encode() + "\" . ");
		}
		pw.close();
		return saveToStoreFromString(sw.toString(), modelurl, context, new Boolean(false), null);
	}
}
