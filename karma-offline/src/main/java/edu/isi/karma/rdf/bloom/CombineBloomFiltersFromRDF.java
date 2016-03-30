package edu.isi.karma.rdf.bloom;

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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.hadoop.util.bloom.BloomFilter;
import org.apache.hadoop.util.hash.Hash;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.uwyn.jhighlight.tools.FileUtils;

import edu.isi.karma.er.helper.BloomFilterTripleStoreUtil;
import edu.isi.karma.kr2rml.writer.KR2RMLBloomFilter;
import edu.isi.karma.rdf.CommandLineArgumentParser;
import edu.isi.karma.webserver.KarmaException;



public class CombineBloomFiltersFromRDF {

	static String filepath;
    static String triplestoreURL;
    static String context;
    static String predicateURI = "http://isi.edu/integration/karma/dev#hasBloomFilter";
	public static void main(String[] args) throws IOException, KarmaException {
		Options options = createCommandLineOptions();
		CommandLine cl = CommandLineArgumentParser.parse(args, options, CombineBloomFiltersFromRDF.class.getSimpleName());
		if(cl == null)
		{
			return;
		}
        filepath = (String) cl.getOptionValue("filepath");
        triplestoreURL = (String) cl.getOptionValue("triplestoreurl");
        context = (String) cl.getOptionValue("context");
        if (filepath == null || triplestoreURL == null || context == null)
        	return;
		File file = new File(filepath);
		Map<String, BloomFilterWorker> workers = new HashMap<>();
		Map<String, KR2RMLBloomFilter> bfs = new HashMap<>();
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
			BloomFilterTripleStoreUtil utilObj = new BloomFilterTripleStoreUtil();
			Set<String> triplemaps = bfs.keySet();
			Map<String, String> bloomfilterMapping = new HashMap<>();
			bloomfilterMapping.putAll(utilObj.getBloomFiltersForMaps(triplestoreURL, context, triplemaps));
			utilObj.updateTripleStoreWithBloomFilters(bfs, bloomfilterMapping, triplestoreURL, context);
			System.out.println("process time: " + (System.currentTimeMillis() - start));
			Map<String, String> verification = new HashMap<>();
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

	private static Options createCommandLineOptions() {

		Options options = new Options();
		options.addOption(new Option("filepath", "filepath", false, "location of the input file directory"));
		options.addOption(new Option("triplestoreurl", "triplestoreurl", true, "location of the triplestore"));
		options.addOption(new Option("context", "context", true, "the context uri"));
		options.addOption(new Option("help", "help", false, "print this message"));

		return options;
	}

}
