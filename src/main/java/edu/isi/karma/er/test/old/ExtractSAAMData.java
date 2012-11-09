package edu.isi.karma.er.test.old;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;

import edu.isi.karma.er.helper.Constants;

public class ExtractSAAMData {

	private static String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	private static Property RDF = ResourceFactory.createProperty(RDF_TYPE);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		Dataset dataset = TDBFactory.createDataset(Constants.PATH_REPOSITORY + "saam_a/");
		Model model = dataset.getDefaultModel();
		Logger logger = Logger.getLogger(ExtractSAAMData.class);
		int count = 0;
		
		//String predicate = "http://americanart.si.edu/saam/fullName";
		ResIterator iter = model.listSubjectsWithProperty(RDF);
		while (iter.hasNext()) {
			Resource subj = iter.next();
			count ++;
			StmtIterator siter = subj.listProperties();
			while (siter.hasNext()) {
				Statement stmt = siter.next();
				logger.info("\t" + subj.getURI() + "\t" + stmt.getPredicate().getURI() + "\t" + stmt.getObject());
			}
		}
		
		System.out.println("time elapsed totally:" + (System.currentTimeMillis() - startTime));
		System.out.println("total number:" + count);

	}
	
	public void print(String str) {
		
	}

}
