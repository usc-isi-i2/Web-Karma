package edu.isi.karma.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.uwyn.jhighlight.tools.FileUtils;



public class CombineBloomFiltersFromRDF {


	public static void main(String[] args) throws IOException {
		if (args.length == 0)
			return;
		File file = new File(args[0]);
		String predicateURI = null;
		Map<String, BloomFilterWorker> bfs = new HashMap<String, BloomFilterWorker>();
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
							BloomFilterWorker worker = bfs.get(subject);
							if (worker == null) {
								worker = new BloomFilterWorker();
								Thread t = new Thread(worker);
								t.start();
							}
							worker.addBloomfilters(object);
							bfs.put(subject, worker);
						}
					}
				}
				
			}
			
			File output = new File(args[1]);
			PrintWriter pw = new PrintWriter(output);
			for (String key : bfs.keySet()) {
				bfs.get(key).setDone();
				while(!bfs.get(key).isFinished());
				pw.print("<" + key + "> ");
				pw.print("<" + predicateURI + "> ");
				pw.println("\"" + bfs.get(key).getKR2RMLBloomFilter().compressAndBase64Encode() + "\" . ");
			}
			pw.close();
		}
		
	}

}
