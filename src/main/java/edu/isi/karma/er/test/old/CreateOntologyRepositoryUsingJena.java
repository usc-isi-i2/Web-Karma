package edu.isi.karma.er.test.old;

import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;

import edu.isi.karma.er.helper.Constants;
import edu.isi.karma.er.helper.ScoreBoardFileUtil;
import edu.isi.karma.er.helper.entity.ScoreBoard;
import edu.isi.karma.er.helper.ontology.MatchOntologyUtil;

public class CreateOntologyRepositoryUsingJena {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Model model = ModelFactory.createDefaultModel();
		Model model = TDBFactory.createDataset(Constants.PATH_REPOSITORY + "match_result/").getDefaultModel();
		//model = createRepository(model);
		//model = loadRepositoryFromCSV(model);
		// model.write(System.out, "N3");
		queryRepository(model);
		model.close();
		
	}

	private static Model loadRepositoryFromCSV(Model model) {
		ScoreBoardFileUtil util = new ScoreBoardFileUtil();
		String filename = util.getLastestResult();
		List<ScoreBoard> resultList = util.loadScoreBoardFile(filename);
		String subject, srcId, srcUrl, dstId, dstUrl, comment, matched, creator, createTime;
		
		MatchOntologyUtil ontoUtil = new MatchOntologyUtil();
		
		for (ScoreBoard sb : resultList) {
			srcId = sb.getSubject();//"Person_2482";
			srcUrl = sb.getSaamUri(); //"http://smithsonianart.si.edu/saam/Person_2482";
			subject = srcUrl;
			dstId = sb.getKarmaUri();
			dstUrl = sb.getKarmaUri();
			if (sb.getFound() > 0.9) {
				matched = "T";
			} else {
				matched = "F";
			}
			comment = "";
			createTime = String.valueOf(System.currentTimeMillis());
			creator = "Karma";
			ontoUtil.createMatchOntology(model, subject, srcId, srcUrl, dstId, dstUrl, matched, comment, createTime, creator);
		}
		
		return model;
	}

	private static void queryRepository(Model model) {
		String sparql = "PREFIX :<http://www.semanticweb.org/ontologies/2012/9/OntologyMatchGroundTruth.owl#>" + "\n" +
				" select ?saamUrl ?otherUrl (max(?time) as ?latest) " + "\n" +
				"where {" + "\n" +
				"?s :saamArtist ?s1." + "\n" +
				"?s1 :url ?saamUrl." + "\n" +
				"?s :otherArtist ?s2." +  "\n" +
				"?s2 :url ?otherUrl." + "\n" +
				"?s :createTime ?time." + "\n" + 
//				"?s :matched ?matched " + "\n" +
//				"filter contains(?matched, \"F\")." + "\n" +
				"} group by ?saamUrl ?otherUrl";
		
		QueryExecution exec = QueryExecutionFactory.create(sparql, model);
		ResultSet rs = null;
		RDFNode node = null;
		String s, p, o;
		int count = 0;
		try {
			rs = exec.execSelect();
			
			while(rs.hasNext()) {
				count++;
				QuerySolution solu = rs.next();
				node = solu.get("saamUrl");
				s = node.toString();
				node = solu.get("otherUrl");
				o = node.toString();
				node = solu.get("latest");
				p = node.toString();
				System.out.println(s + "\t" + o + "\t" + p);
			}
			System.out.println(count + " records find matched.");
		} catch (Exception e ) {
			e.printStackTrace();
		} finally {
			exec.close();
		}
	}

	private static Model createRepository(Model model) {
		String subject, srcId, srcUrl, dstId, dstUrl, comment, matched , createTime;
		Resource res = null;
		model = ModelFactory.createDefaultModel();
		MatchOntologyUtil util = new MatchOntologyUtil();
		
		srcId = "Person_2482";
		srcUrl = "http://smithsonianart.si.edu/saam/Person_2482";
		subject = srcUrl;
		dstId = "http://dbpedia.org/Andy_Smith";
		dstUrl = "http://dbpedia.org/Andy_Smith";
		matched = "T";
		comment = "They are real matched";
		createTime = String.valueOf(System.currentTimeMillis());
		util.createMatchOntology(model, subject, srcId, srcUrl, dstId, dstUrl, matched, comment, createTime, "Karma");
		
		srcId = "Person_1251";
		srcUrl = "http://smithsonianart.si.edu/saam/Person_1251";
		subject = srcUrl;
		dstId = "http://dbpedia.org/Andy_Smith";
		dstUrl = "http://dbpedia.org/Andy_Smith";
		matched = "T";
		comment = "They are real matched";
		createTime = String.valueOf(System.currentTimeMillis());
		util.createMatchOntology(model, subject, srcId, srcUrl, dstId, dstUrl, matched, comment, createTime, "karma");
		
		return model;
	}

}
