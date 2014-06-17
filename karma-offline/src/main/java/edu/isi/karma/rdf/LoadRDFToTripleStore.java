package edu.isi.karma.rdf;

import java.io.File;

import com.uwyn.jhighlight.tools.FileUtils;

import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.webserver.KarmaException;

public class LoadRDFToTripleStore {
	
	public static void main(String args[]) throws KarmaException {
		TripleStoreUtil util = new TripleStoreUtil();
		if (args.length < 3)
			return;
		File file = new File(args[0]);
		String context = args[1];
		String tripleStoreUrl = args[2];
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				System.out.println(FileUtils.getExtension(f.getName()));
				if (FileUtils.getExtension(f.getName()) != null && FileUtils.getExtension(f.getName()).compareTo("ttl") == 0)
					util.saveToStore(f.getAbsolutePath(), tripleStoreUrl, context, false, null);
			}
		}
		else {
			if (FileUtils.getExtension(file.getName()) != null && FileUtils.getExtension(file.getName()).compareTo("ttl") == 0)
				util.saveToStore(file.getAbsolutePath(), tripleStoreUrl, context, false, null);
		}
	}
}
