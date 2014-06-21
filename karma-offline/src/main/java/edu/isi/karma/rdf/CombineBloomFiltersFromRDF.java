package edu.isi.karma.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

import org.apache.hadoop.util.hash.Hash;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.uwyn.jhighlight.tools.FileUtils;

import edu.isi.karma.kr2rml.KR2RMLBloomFilter;


public class CombineBloomFiltersFromRDF {


	public static void main(String[] args) throws IOException {
		if (args.length == 0)
			return;
		File file = new File(args[0]);
		String predicateURI = null;
		Map<String, KR2RMLBloomFilter> bfs = new HashMap<String, KR2RMLBloomFilter>();
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
							predicateURI = predicate;
							KR2RMLBloomFilter bf = null;
							if (bfs.get(subject) == null) {
								bf = new KR2RMLBloomFilter(1000000, 8,Hash.JENKINS_HASH);
							}
							KR2RMLBloomFilter bf2 = new KR2RMLBloomFilter(1000000, 8,Hash.JENKINS_HASH);
							bf2.populateFromCompressedAndBase64EncodedString(object);
							bf.or(bf2);
							bfs.put(subject, bf);
						}
					}
				}

			}
		}
		File output = new File(args[1]);
		PrintWriter pw = new PrintWriter(output);
		for (String key : bfs.keySet()) {
			pw.print("<" + key + "> ");
			pw.print("<" + predicateURI + "> ");
			pw.println("\"" + bfs.get(key).compressAndBase64Encode() + "\" . ");
		}
		pw.close();
	}

}
