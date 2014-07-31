package edu.isi.karma.rdf;

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

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;
import org.apache.hadoop.util.bloom.Key;
import org.apache.hadoop.util.hash.Hash;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.kr2rml.KR2RMLBloomFilter;
import edu.isi.karma.webserver.KarmaException;

public class AddSameAsToBloomFilters {
	static String filepath;
    static String triplestoreURL;
    static String predicate;
	public static void main(String[] args) throws KarmaException, IOException {
		Group options = createCommandLineOptions();
        Parser parser = new Parser();
        parser.setGroup(options);
        HelpFormatter hf = new HelpFormatter();
        parser.setHelpFormatter(hf);
        parser.setHelpTrigger("--help");
        CommandLine cl = parser.parseAndHelp(args);
        if (cl == null || cl.getOptions().size() == 0 || cl.hasOption("--help")) {
            hf.setGroup(options);
            hf.print();
            return;
        }
        filepath = (String) cl.getValue("--filepath");
        triplestoreURL = (String) cl.getValue("--triplestoreurl");
        predicate = (String) cl.getValue("--predicate");
        TripleStoreUtil utilObj = new TripleStoreUtil();
        Set<String> predicates = new HashSet<String>();
        predicates.add(predicate);
        List<String> predicateObjectMaps = new ArrayList<String>();
        for (String t : utilObj.getPredicatesForParentTriplesMapsWithSameClass(triplestoreURL, null, predicates).get("refObjectMaps")) {
        	predicateObjectMaps.addAll(Arrays.asList(t.split(",")));
        }
        for (String t : utilObj.getPredicatesForTriplesMapsWithSameClass(triplestoreURL, null, predicates).get("predicateObjectMaps")) {
        	predicateObjectMaps.addAll(Arrays.asList(t.split(",")));
        }
        Map<String, String> serializedmapping = utilObj.getBloomFiltersForMaps(triplestoreURL, null, predicateObjectMaps);
        Map<String, KR2RMLBloomFilter> mapping = new HashMap<String, KR2RMLBloomFilter>();
        
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
	
	private static Group createCommandLineOptions() {
		DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
		ArgumentBuilder abuilder = new ArgumentBuilder();
		GroupBuilder gbuilder = new GroupBuilder();

		Group options =
				gbuilder
				.withName("options")
				.withOption(buildOption("filepath", "location of the input file directory", "filepath", obuilder, abuilder))
				.withOption(buildOption("triplestoreurl", "location of the triplestore", "triplestoreurl", obuilder, abuilder))
				.withOption(buildOption("predicate", "the uri or the predicate", "predicate", obuilder, abuilder))
				.withOption(obuilder
						.withLongName("help")
						.withDescription("print this message")
						.create())
						.create();

		return options;
	}

	public static Option buildOption(String shortName, String description, String argumentName,
			DefaultOptionBuilder obuilder, ArgumentBuilder abuilder) {
		return obuilder
				.withLongName(shortName)
				.withDescription(description)
				.withArgument(
						abuilder
						.withName(argumentName)
						.withMinimum(1)
						.withMaximum(1)
						.create())
						.create();
	}


}
