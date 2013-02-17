package edu.isi.karma.er.test.old;

import java.util.Map;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

import edu.isi.karma.er.helper.Constants;
import edu.isi.karma.er.helper.entity.NYTimes;
import edu.isi.karma.er.helper.entity.NameSpace;

public class TestNYTimesOntology {
	
	private final static String NYTIMES_DIRECTORY = Constants.PATH_REPOSITORY + "nytimes/";
	private final static String NYTIMES_FILE = Constants.PATH_N3_FILE + "people_nytimes.rdf";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//outputOntology();
		
		listOntology();
		
		//loadOntology();
		
		//updateMatchResult();
	}

	private static void listOntology() {
		NYTimes nyt = new NYTimes();
		Map<String, NYTimes> map = nyt.listAllToMap();
		int count = 0;
		
		for (String key : map.keySet()) {
			count ++;
			NYTimes ent = map.get(key);
			if (ent.getTopicPage() == null) {
				System.out.println(count + ":" + ent.getSubject());
			}
			//System.out.println(count + "\t" + key + "\t" + ent.getSubject() + "\t" + ent.getName() + "\t" + ent.getTopicPage());// + ent.getDbpediaUri() + "\t" + ent.getFreebaseUri());
		}
		
	}
	
	

	public static void outputOntology() {
		Model model = TDBFactory.createDataset(NYTIMES_DIRECTORY).getDefaultModel();
		model.write(System.out, "N3");
	}

	public static void loadOntology() {
		
		Dataset dataset = TDBFactory.createDataset(NYTIMES_DIRECTORY);
		
		Model model = dataset.getDefaultModel();
		model.begin();
		FileManager.get().readModel(model, NYTIMES_FILE, "RDF/XML");
		model.commit();
		
		TDB.sync(model);
		TDB.sync(dataset);
		model.close();
		dataset.close();
	}
	
	public static void updateMatchResult() {
		NYTimes nyt = new NYTimes();
		Map<String, NYTimes> map = nyt.listAllToMap();
		
		Model model = TDBFactory.createDataset(Constants.PATH_REPOSITORY + "match_result").getDefaultModel();
		model.begin();
		
		Resource subj, act, dst, nEnt;
		String dbUri;
		ResIterator iter = model.listSubjectsWithProperty(NameSpace.MATCH_HAS_SCORE, (RDFNode)null);
		while(iter.hasNext()) {
			subj = iter.next();
			subj.addProperty(NameSpace.RDF_TYPE, NameSpace.PROV_GENERATION);
			
			act = subj.getProperty(NameSpace.PROV_WAS_GENERATED_BY).getObject().asResource();
			dst = act.getProperty(NameSpace.MATCH_HAS_MATCH_TARGET).getObject().asResource();
			dbUri = dst.getProperty(NameSpace.PROV_WAS_QUOTED_FROM).getObject().asResource().getURI();
			nyt = map.get(dbUri);
			if (nyt != null) {
				nEnt = model.createResource(nyt.getSubject());
				nEnt.addProperty(NameSpace.SKOS_PREF_LABEL, nyt.getName());
				nEnt.addProperty(NameSpace.NYTIMES_TOPIC_PAGE, ResourceFactory.createResource(nyt.getTopicPage()));
				nEnt.addProperty(NameSpace.NYTIMES_SEARCH_API_QUERY, nyt.getSearchLink());
				nEnt.addProperty(NameSpace.OWL_SAME_AS, ResourceFactory.createResource(nyt.getFreebaseUri()));
				dst.addProperty(NameSpace.OWL_SAME_AS, nEnt);
			}
		}
		
		model.commit();
		TDB.sync(model);
		model.close();
		
	}
	
	

}
