package edu.isi.karma.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.BitSet;
import java.util.HashMap;
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
import org.apache.hadoop.util.bloom.BloomFilter;
import org.apache.hadoop.util.hash.Hash;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.uwyn.jhighlight.tools.FileUtils;

import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.kr2rml.writer.KR2RMLBloomFilter;
import edu.isi.karma.webserver.KarmaException;



public class CombineBloomFiltersFromRDF {

	static String filepath;
    static String triplestoreURL;
    static String context;
    static String predicateURI = "http://isi.edu/integration/karma/dev#hasBloomFilter";
	public static void main(String[] args) throws IOException, KarmaException {
		Group options = createCommandLineOptions();
        Parser parser = new Parser();
        parser.setGroup(options);
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
        context = (String) cl.getValue("--context");
        if (filepath == null || triplestoreURL == null || context == null)
        	return;
		File file = new File(filepath);
		Map<String, BloomFilterWorker> workers = new HashMap<String, BloomFilterWorker>();
		Map<String, KR2RMLBloomFilter> bfs = new HashMap<String, KR2RMLBloomFilter>();
		long start = System.currentTimeMillis();
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				if (FileUtils.getExtension(f.getName()) != null) {
					Model model = ModelFactory.createDefaultModel();
					InputStream s = new FileInputStream(f);
					model.read(s, null, "TURTLE");
					StmtIterator iterator = model.listStatements();
					while(iterator.hasNext()) {
						Statement st = iterator.next();
						String subject = st.getSubject().toString();
						String object = st.getObject().toString();
						String predicate = st.getPredicate().toString();
						if (predicate.contains("hasBloomFilter")) {
							//predicateURI = predicate;
							BloomFilterWorker worker = workers.get(subject);
							if (worker == null) {
								worker = new BloomFilterWorker();
								Thread t = new Thread(worker);
								t.start();
							}
							worker.addBloomfilters(object);
							workers.put(subject, worker);
						}
					}
				}

			}
			for (Entry<String, BloomFilterWorker> entry : workers.entrySet()) {
				entry.getValue().setDone();
			}
			for (Entry<String, BloomFilterWorker> entry : workers.entrySet()) {
				while(!entry.getValue().isFinished());
				bfs.put(entry.getKey(), entry.getValue().getKR2RMLBloomFilter());
			}
			TripleStoreUtil utilObj = new TripleStoreUtil();
			Set<String> triplemaps = bfs.keySet();
			Map<String, String> bloomfilterMapping = new HashMap<String, String>();
			bloomfilterMapping.putAll(utilObj.getBloomFiltersForMaps(triplestoreURL, context, triplemaps));
			utilObj.updateTripleStoreWithBloomFilters(bfs, bloomfilterMapping, triplestoreURL, context);
			System.out.println("process time: " + (System.currentTimeMillis() - start));
			Map<String, String> verification = new HashMap<String, String>();
			verification.putAll(utilObj.getBloomFiltersForMaps(triplestoreURL, context, triplemaps));
			boolean verify = true;
			for (Entry<String, String> entry : verification.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				KR2RMLBloomFilter bf2 = new KR2RMLBloomFilter(KR2RMLBloomFilter.defaultVectorSize, KR2RMLBloomFilter.defaultnbHash, Hash.JENKINS_HASH);
				KR2RMLBloomFilter bf = bfs.get(key);
				bf2.populateFromCompressedAndBase64EncodedString(value);
				bf2.and(bf);
				bf2.xor(bf);
				try {
					Field f = BloomFilter.class.getDeclaredField("bits");
					f.setAccessible(true);
					BitSet bits = (BitSet) f.get(bf2);
					if (bits.cardinality() != 0) {
						verify = false;
						break;
					}
				} catch (Exception e) {

				}
			}
			if (!verify) {
				utilObj.updateTripleStoreWithBloomFilters(bfs, verification, triplestoreURL, context);
			}
		}

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
				.withOption(buildOption("context", "the context uri", "context", obuilder, abuilder))
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
