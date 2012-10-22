package edu.isi.karma.er.test.old;

import java.util.List;
import java.util.Vector;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;

import edu.isi.karma.er.compare.impl.StringQGramComparatorImpl;
import edu.isi.karma.er.helper.Constants;
import edu.isi.karma.er.helper.entity.PersonProperty;
import edu.isi.karma.er.helper.entity.SaamPerson;

public class LoadModelIntoObject {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

		long t = System.currentTimeMillis();
		
		Model srcModel = TDBFactory.createDataset(Constants.PATH_REPOSITORY + "saam_a/").getDefaultModel();
		Model dstModel = TDBFactory.createDataset(Constants.PATH_REPOSITORY + "dbpedia_a/").getDefaultModel();
		String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";	// property to retrieve all the subjects from models.
		Property RDF = ResourceFactory.createProperty(RDF_TYPE);
		Property p = ResourceFactory.createProperty("http://americanart.si.edu/saam/fullName");
		
		
		ResIterator iter1 = srcModel.listSubjectsWithProperty(RDF);
		ResIterator iter2 = dstModel.listSubjectsWithProperty(RDF);
		
		System.out.println("load model in " + (System.currentTimeMillis() - t));
		t = System.currentTimeMillis();
		
		List<SaamPerson> list1 = loadOntologies(iter1);
		List<SaamPerson> list2 = loadOntologies(iter2);
		
		System.out.println("size:" + list1.size() + " | " + list2.size() + " in " + (System.currentTimeMillis() - t));
		t = System.currentTimeMillis();
		
		int count = 0, i = 0;
		StringQGramComparatorImpl comp = new StringQGramComparatorImpl();
		
		for (SaamPerson v : list1) {
			i ++;
			if (i % 100 == 0) {
				System.out.println(i + " rows process in " + (System.currentTimeMillis() - t));
				t = System.currentTimeMillis();
			}
			for (SaamPerson w : list2) {
				for (String vp : v.getFullName().getValue()) {
					for (String wp : w.getFullName().getValue()) {
						if (comp.getSimilarity(vp, wp) > 0.95) {
							count ++;
							break;
						}
					}
				}
			}
		}
		
		
		System.out.println("count:" + count);
	}

	private static List<SaamPerson> loadOntologies(ResIterator iter) {
		List<SaamPerson> list = new Vector<SaamPerson>();
		while (iter.hasNext()) {
			Resource res = iter.next();
			StmtIterator siter = res.listProperties();
			SaamPerson per = new SaamPerson();
			per.setSubject(res.getURI());
			
			while (siter.hasNext()) {
				Statement st = siter.next();
				String pred = st.getPredicate().getURI();
				RDFNode node = st.getObject();
				PersonProperty p = new PersonProperty();
				p.setPredicate(pred);
				
				if (node != null) {
					if (pred.indexOf("fullName") > -1) {
						p.setValue(node.asLiteral().getString());
						per.setFullName(p);
					} else if (pred.indexOf("birthYear") > -1) {
						p.setValue(node.asLiteral().getString());
						per.setBirthYear(p);
					} else if (pred.indexOf("deathYear") > -1) {
						p.setValue(node.asLiteral().getString());
						per.setDeathYear(p);
					}
				}
			}
			list.add(per);
		}
		return list;
	}

}
