package edu.isi.karma.rdf.bloom;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.util.bloom.Key;
import org.apache.hadoop.util.hash.Hash;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.isi.karma.er.helper.BloomFilterTripleStoreUtil;
import edu.isi.karma.kr2rml.writer.KR2RMLBloomFilter;
import edu.isi.karma.rdf.CommandLineArgumentParser;
import edu.isi.karma.webserver.KarmaException;

public class AddSameAsToBloomFilters {
	static String filepath;
    static String triplestoreURL;
    static String predicate;
	public static void main(String[] args) throws KarmaException, IOException, ParseException {
		Options options = createCommandLineOptions();
		CommandLine cl = CommandLineArgumentParser.parse(args, options, AddSameAsToBloomFilters.class.getSimpleName());
		if(cl == null)
		{
			return;
		}
        filepath = (String) cl.getOptionValue("filepath");
        triplestoreURL = (String) cl.getOptionValue("triplestoreurl");
        predicate = (String) cl.getOptionValue("predicate");
        BloomFilterTripleStoreUtil utilObj = new BloomFilterTripleStoreUtil();
        Set<String> predicates = new HashSet<>();
        predicates.add(predicate);
        List<String> predicateObjectMaps = new ArrayList<>();
        for (String t : utilObj.getPredicatesForParentTriplesMapsWithSameClass(triplestoreURL, null, predicates).get("refObjectMaps")) {
        	predicateObjectMaps.addAll(Arrays.asList(t.split(",")));
        }
        for (String t : utilObj.getPredicatesForTriplesMapsWithSameClass(triplestoreURL, null, predicates).get("predicateObjectMaps")) {
        	predicateObjectMaps.addAll(Arrays.asList(t.split(",")));
        }
        Map<String, String> serializedmapping = utilObj.getBloomFiltersForMaps(triplestoreURL, null, predicateObjectMaps);
        Map<String, KR2RMLBloomFilter> mapping = new HashMap<>();
        
        for (Entry<String, String> entry : serializedmapping.entrySet()) {
        	String key = entry.getKey();
        	String value = entry.getValue();
        	KR2RMLBloomFilter bf = new KR2RMLBloomFilter(KR2RMLBloomFilter.defaultVectorSize, KR2RMLBloomFilter.defaultnbHash, Hash.JENKINS_HASH);
        	bf.populateFromCompressedAndBase64EncodedString(value);
        	mapping.put(key, bf);
        }
        
        Model model = ModelFactory.createDefaultModel();
        InputStream s = new FileInputStream(new File(filepath));
		model.read(s, null, "TURTLE");
		StmtIterator iterator = model.listStatements();
		while(iterator.hasNext()) {
			iterator.next();
			Statement st = iterator.next();
			String subject = "<" + st.getSubject().toString() + ">";
			String object = "<" + st.getObject().toString() + ">";
			for (Entry<String, KR2RMLBloomFilter> entry : mapping.entrySet()) {
				KR2RMLBloomFilter bf = entry.getValue();
				if (bf.membershipTest(new Key(subject.getBytes("UTF-8"))))
					bf.add(new Key(object.getBytes("UTF-8")));
				if (bf.membershipTest(new Key(object.getBytes("UTF-8"))))
					bf.add(new Key(subject.getBytes("UTF-8")));
			}
		}
		
		utilObj.updateTripleStoreWithBloomFilters(mapping, serializedmapping, triplestoreURL, null);
		
	}
	
	private static Options createCommandLineOptions() {

		Options options = new Options();
		options.addOption( new Option("filepath", "filepath", true, "location of the input file directory"));
		options.addOption( new Option("triplestoreurl", "triplestoreurl", true, "location of the triple store"));
		options.addOption( new Option("predicate", "predicate",true, "the uri or the predicate"));
		options.addOption( new Option("help", "help", true, "print this message"));
			
		return options;
	}


}
