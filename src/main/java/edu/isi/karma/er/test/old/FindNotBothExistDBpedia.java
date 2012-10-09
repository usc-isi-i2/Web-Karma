package edu.isi.karma.er.test.old;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.FileManager;

import edu.isi.karma.er.helper.Constants;

public class FindNotBothExistDBpedia {
	
	private final static String OLD_DBPEDIA_FILE_PART = Constants.PATH_N3_FILE + "dbpedia_fullname_start_with_A.n3";
	private final static String NEW_DBPEDIA_FILE_PART = Constants.PATH_N3_FILE + "dbpedia_dbpprop_start_with_A.n3";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Model oldm = FileManager.get().loadModel(OLD_DBPEDIA_FILE_PART);
		Model newm = FileManager.get().loadModel(NEW_DBPEDIA_FILE_PART);
		
		String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";	// property to retrieve all the subjects from models.
		Property RDF = ResourceFactory.createProperty(RDF_TYPE);
		
		List<Resource> oldList = oldm.listSubjectsWithProperty(RDF).toList();
		List<Resource> newList = newm.listSubjectsWithProperty(RDF).toList();
		
		System.out.println("subject size:" + oldList.size() + " | " + newList.size());
		
		int newSize, oldSize, count = 0;
		newSize = newList.size();
		for (int i = 0; i < newSize; i++) {
			Resource newS = newList.get(i);
			int j = 0;
			oldSize = oldList.size();
			for (j = 0; j < oldSize; j++) {
				Resource oldS = oldList.get(i);
				if (newS.getURI().equals(oldS.getURI())) {
					break;
				}
			}
			
			if (j >= oldSize) {
				count ++;
				System.out.println("[" + count + "] " + newS.getURI());
			}
		}
	}

}
