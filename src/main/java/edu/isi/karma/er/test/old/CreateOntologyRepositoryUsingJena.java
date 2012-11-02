package edu.isi.karma.er.test.old;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.tdb.TDBFactory;

import edu.isi.karma.er.helper.Constants;
import edu.isi.karma.er.helper.ScoreBoardFileUtil;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.helper.entity.ScoreBoard;
import edu.isi.karma.er.helper.ontology.MatchOntologyUtil;

public class CreateOntologyRepositoryUsingJena {

	private static double THRESHOLD = 0.9;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Model model = ModelFactory.createDefaultModel();
		Model model = TDBFactory.createDataset(Constants.PATH_REPOSITORY + "match_result/").getDefaultModel();
		//model.remove(model);
		//model = createRepository(model);
		//model = loadRepositoryFromCSV(model);
		//model.write(System.out, "N3");
		
		MatchOntologyUtil util = new MatchOntologyUtil();
		util.outputAllMatchResult(model);
		
		util.outputLatestMatchResult(model);
		
		System.out.println("model size:" + model.size());
		model.close();
		
	}

	
	public static Model loadRepositoryFromCSV(Model model) {
		ScoreBoardFileUtil util = new ScoreBoardFileUtil();
		String filename = util.getLastestResult();
		List<ScoreBoard> resultList = util.loadScoreBoardFile(filename);
		
		String srcURI, dstURI, creator, comment ;
		String[] srcAttr, srcVal, dstAttr, dstVal;
		long createTime = System.currentTimeMillis();
		double finalScore;
		
		MatchOntologyUtil ontoUtil = new MatchOntologyUtil();
		
		for (ScoreBoard sb : resultList) {
			srcURI = sb.getSaamUri(); //"http://smithsonianart.si.edu/saam/Person_2482";
			dstURI = sb.getKarmaUri();
			if (sb.getFound() > 0) {
				MultiScore ms = sb.getRankList().get(0);
				
				createTime = System.currentTimeMillis();
				creator = "Karma";
				finalScore = sb.getFound();
				if (finalScore >= THRESHOLD) {
					comment = "Exact match, cause similarity is greater than " + THRESHOLD;
				} else {
					comment = "Not match, cause similarity is less than " + THRESHOLD;
				}
				
				List<Score> slist = ms.getScoreList();
				int len = slist.size();
				srcAttr = new String[len];
				srcVal = new String[len];
				dstAttr = new String[len];
				dstVal = new String[len];
				for (int i = 0; i < len; i++) {
					Score s = slist.get(i);
					srcAttr[i] = s.getPredicate();
					dstAttr[i] = s.getPredicate();
					srcVal[i] = s.getSrcObj();
					dstVal[i] = s.getDstObj();
				}
				
				ontoUtil.createMatchOntology(model, finalScore, createTime, srcURI, dstURI, creator, comment, srcAttr, srcVal, dstAttr, dstVal);
			}
		}
		
		return model;
	}

	

	
	public static Model createRepository(Model model) {
		String srcURI, dstURI, creator, comment ;
		String[] srcAttr, srcVal, dstAttr, dstVal;
		long createTime;
		double finalScore;
		
		model = ModelFactory.createDefaultModel();
		MatchOntologyUtil util = new MatchOntologyUtil();
		
		
		String[] srcAttr1 = {"http://americanart.si.edu/saam/deathYear", "http://americanart.si.edu/saam/birthYear", "http://americanart.si.edu/saam/fullName"};
		String[] srcVal1 = {"1887", "1804", "Alvan Clark"};
		String[] dstAttr1 = {"http://americanart.si.edu/saam/deathYear", "http://americanart.si.edu/saam/birthYear", "http://americanart.si.edu/saam/fullName"};
		String[] dstVal1 = {"1887", "1804", "Alvan Clark"};
		srcAttr = srcAttr1; 
		srcVal = srcVal1;
		dstAttr = dstAttr1;
		dstVal = dstVal1;
		srcURI = "http://smithsonianart.si.edu/saam/Person_2482";
		dstURI = "http://dbpedia.org/Andy_Smith";
		createTime = System.currentTimeMillis();
		comment = "They are real matched";
		finalScore = 0.999986;
		creator = "Karma";
		
		util.createMatchOntology(model, finalScore, createTime, srcURI, dstURI, creator, comment, srcAttr, srcVal, dstAttr, dstVal);
		
		
		String[] srcAttr2 = {"http://americanart.si.edu/saam/deathYear", "http://americanart.si.edu/saam/birthYear", "http://americanart.si.edu/saam/fullName"};
		String[] srcVal2 = {"1887", "1804", "Alvan Clark"};
		String[] dstAttr2 = {"http://americanart.si.edu/saam/deathYear", "http://americanart.si.edu/saam/birthYear", "http://americanart.si.edu/saam/fullName"};
		String[] dstVal2 = {"1887", "1804", "Alvan Clark"};
		srcAttr = srcAttr2; 
		srcVal = srcVal2;
		dstAttr = dstAttr2;
		dstVal = dstVal2;
		srcURI = "http://smithsonianart.si.edu/saam/Person_2482";
		dstURI = "http://dbpedia.org/Andy_Smith";
		createTime = System.currentTimeMillis();
		comment = "They are real matched";
		finalScore = 0.999986;
		creator = "Human";
		
		util.createMatchOntology(model, finalScore, createTime, srcURI, dstURI, creator, comment, srcAttr, srcVal, dstAttr, dstVal);
		
		
		
		String[] srcAttr3 = {"http://americanart.si.edu/saam/deathYear", "http://americanart.si.edu/saam/birthYear", "http://americanart.si.edu/saam/fullName"};
		String[] srcVal3 = {"1887", "1804", "Alvan Clark"};
		String[] dstAttr3 = {"http://americanart.si.edu/saam/deathYear", "http://americanart.si.edu/saam/birthYear", "http://americanart.si.edu/saam/fullName"};
		String[] dstVal3 = {"1887", "1804", "Alvan Clark"};
		srcAttr = srcAttr3; 
		srcVal = srcVal3;
		dstAttr = dstAttr3;
		dstVal = dstVal3;
		srcURI = "http://smithsonianart.si.edu/saam/Person_2482";
		dstURI = "http://dbpedia.org/Andy_Smith";
		createTime = System.currentTimeMillis();
		comment = "They are real matched";
		finalScore = 0.999986;
		creator = "Human";
		
		util.createMatchOntology(model, finalScore, createTime, srcURI, dstURI, creator, comment, srcAttr, srcVal, dstAttr, dstVal);
		
		
		
		String[] srcAttr4 = {"http://americanart.si.edu/saam/deathYear", "http://americanart.si.edu/saam/birthYear", "http://americanart.si.edu/saam/fullName"};
		String[] srcVal4 = {"1984", "1900", "Alice Neel"};
		String[] dstAttr4 = {"http://americanart.si.edu/saam/deathYear", "http://americanart.si.edu/saam/birthYear", "http://americanart.si.edu/saam/fullName"};
		String[] dstVal4 = {"1984", "1900", "Alice Neel"};
		srcAttr = srcAttr4; 
		srcVal = srcVal4;
		dstAttr = dstAttr4;
		dstVal = dstVal4;
		srcURI = "http://americanart.si.edu/saam/Person_3504";
		dstURI = "http://dbpedia.org/resource/Alice_Neel";
		createTime = System.currentTimeMillis();
		comment = "They are real matched";
		finalScore = 0.9999901;
		creator = "Karma";
		
		util.createMatchOntology(model, finalScore, createTime, srcURI, dstURI, creator, comment, srcAttr, srcVal, dstAttr, dstVal);
		
		String[] srcAttr5 = {"http://americanart.si.edu/saam/deathYear", "http://americanart.si.edu/saam/birthYear", "http://americanart.si.edu/saam/fullName"};
		String[] srcVal5 = {"1984", "1900", "Alice Neel"};
		String[] dstAttr5 = {"http://americanart.si.edu/saam/deathYear", "http://americanart.si.edu/saam/birthYear", "http://americanart.si.edu/saam/fullName"};
		String[] dstVal5 = {"1984", "1900", "Alice Neel"};
		srcAttr = srcAttr5; 
		srcVal = srcVal5;
		dstAttr = dstAttr5;
		dstVal = dstVal5;
		srcURI = "http://americanart.si.edu/saam/Person_3504";
		dstURI = "http://dbpedia.org/resource/Alice_Neel";
		createTime = System.currentTimeMillis();
		comment = "They are real matched";
		finalScore = 0.9999901;
		creator = "Human";
		
		util.createMatchOntology(model, finalScore, createTime, srcURI, dstURI, creator, comment, srcAttr, srcVal, dstAttr, dstVal);
		
		
		String[] srcAttr6 = {"http://americanart.si.edu/saam/deathYear", "http://americanart.si.edu/saam/birthYear", "http://americanart.si.edu/saam/fullName"};
		String[] srcVal6 = {"", "1958", "Anne Smith"};
		String[] dstAttr6 = {"http://americanart.si.edu/saam/deathYear", "http://americanart.si.edu/saam/birthYear", "http://americanart.si.edu/saam/fullName"};
		String[] dstVal6 = {"", "1959", "Anne Smith"};
		srcAttr = srcAttr6; 
		srcVal = srcVal6;
		dstAttr = dstAttr6;
		dstVal = dstVal6;
		srcURI = "http://americanart.si.edu/saam/Person_18472";
		dstURI = "http://dbpedia.org/resource/Anne_Smith";
		createTime = System.currentTimeMillis();
		comment = "They are not matched";
		finalScore = 0.96447678;
		creator = "Karma";
		
		util.createMatchOntology(model, finalScore, createTime, srcURI, dstURI, creator, comment, srcAttr, srcVal, dstAttr, dstVal);
		
		util.outputAllMatchResult(model);
		
		util.outputLatestMatchResult(model);
		return model;
	}
	
}
