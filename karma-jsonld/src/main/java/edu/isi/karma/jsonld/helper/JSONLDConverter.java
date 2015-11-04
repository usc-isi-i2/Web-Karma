package edu.isi.karma.jsonld.helper;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

import java.io.IOException;
import java.util.*;

/**
 * Created by chengyey on 11/2/15.
 */
public class JSONLDConverter {
    public String convertJSONLD(String json) throws IOException, JsonLdError {
        JsonLdOptions jsonLdOptions = new JsonLdOptions();
        jsonLdOptions.format = "application/nquads";
        Object jsonObject = JsonUtils.fromString(json);
        return JsonLdProcessor.toRDF(jsonObject, jsonLdOptions).toString();
    }

    public String deduplicateTriples(String t1, String t2) {
        List<String> list = Arrays.asList(t1, t2);
        Iterator<String> iterator = list.iterator();
        return deduplicateTriples(iterator);
    }

    public String deduplicateTriples(Iterator<String> iterator) {
        Set<String> allTriples = new HashSet<>();
        while(iterator.hasNext())
        {
            String value = iterator.next();
            String[] triples = value.split("(\r\n|\n)");
            for(String triple : triples)
            {
                allTriples.add(triple);
            }

        }
        StringBuilder sb = new StringBuilder();
        for(String triple : allTriples)
        {
            sb.append(triple);
            sb.append(System.lineSeparator());
        }
        return sb.toString();

    }
}
