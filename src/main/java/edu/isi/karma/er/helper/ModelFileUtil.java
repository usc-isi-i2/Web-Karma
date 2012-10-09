package edu.isi.karma.er.helper;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

/**
 * A utility for file processing, including import and export.
 * @author isi
 *
 */
public class ModelFileUtil {

	/**
	 * Loading a rdf model from a Turtle file.
	 * @param file -- the absolute path name of a Turtle file.
	 * @return a rdf model, null when loading error.
	 */
	public Model loadModelFromFile(String file) {
		
		return loadModelFromFile(file, "Turtle");
	}

	/**
	 * Loading a rdf model from a text file, file types can be RDF/XML, N3, N-Triple... etc which is designated by the parameter rdfSyntax.
	 * @param file -- the absolute path name of text file.
	 * @param rdfSyntax -- the file type
	 * @return a rdf model, null when loading error.
	 */
	public Model loadModelFromFile(String file, String rdfSyntax) {
		
		return FileManager.get().loadModel(file, rdfSyntax);
	}

}
