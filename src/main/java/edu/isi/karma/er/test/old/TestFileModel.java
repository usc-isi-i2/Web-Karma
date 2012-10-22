package edu.isi.karma.er.test.old;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

import edu.isi.karma.er.helper.Constants;

public class TestFileModel {
	
	private final static String DBPEDIA_DIRECTORY = Constants.PATH_REPOSITORY + "dbpedia/";
	private final static String SAAM_DIRECTORY = Constants.PATH_REPOSITORY + "saam_a/";
	private final static String ALL_DIRECTORY = Constants.PATH_REPOSITORY + "all/";
	

	private final static String SAAM_FILE = Constants.PATH_N3_FILE + "saam_fullname_birth_death_asso_city_state_country.n3";
	private final static String DBPEDIA_FILE = Constants.PATH_N3_FILE + "dbpedia_fullname_birth_death_dbpprop.n3";
	private final static String ALL_FILE = Constants.PATH_N3_FILE + "all_fullname_birth_death_asso_city_state_country.n3";
	private final static String SAAM_FILE_PART = Constants.PATH_N3_FILE + "saam_fullname_start_with_A.n3";
	private final static String DBPEDIA_FILE_PART = Constants.PATH_N3_FILE + "dbpedia_dbpprop_start_with_A.n3";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		long startTime = System.currentTimeMillis();
		setupDirectoryModel();
		
		System.out.println("setup finished in " + (System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
		
//		System.out.println("load model from file");
//		Model model = loadModelFromFile();
//		System.out.println("\nload model from TDB");
//		Model model = loadModelFromTDB();
//		
//		System.out.println("loading model time:" + (System.currentTimeMillis() - startTime));
		
/*		
		startTime = System.currentTimeMillis();
		String q = "select * where {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o}";
		Query query = QueryFactory.create(q);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		System.out.println("\nresult size:" + results.getRowNumber());
		System.out.println("select subjects time:" + (System.currentTimeMillis() - startTime));
*/		
		
//		startTime = System.currentTimeMillis();
//		StmtIterator iter = model.listStatements(null, null, (RDFNode)null);
//		System.out.println("\nstatements size:" + iter.toList().size());
//		System.out.println("list statements time:" + (System.currentTimeMillis() - startTime));
//		
//		System.out.println("task finished.");
		
	}

	/**
	 * Load model from a TDB directory.
	 * @return
	 */
	private static Model loadModelFromTDB() {
		Dataset dataset = TDBFactory.createDataset(DBPEDIA_DIRECTORY);
		//dataset.begin(ReadWrite.READ);
		//System.out.println(dataset.listNames().next());
		Model model = dataset.getDefaultModel();
		//dataset.end();
		//dataset.close();
		return model;
	}

	/**
	 * Just load model from a single file.
	 * @return
	 */
	private static Model loadModelFromFile() {
		return FileManager.get().loadModel(SAAM_FILE);
	}
	
	/**
	 * setup model directory for jena TDB
	 */
	private static void setupDirectoryModel() {
		
		Dataset dataset = TDBFactory.createDataset(DBPEDIA_DIRECTORY);
		
		Model model = dataset.getDefaultModel();
		
		FileManager.get().readModel(model, DBPEDIA_FILE);
		
		dataset.close();
		
	}

}
