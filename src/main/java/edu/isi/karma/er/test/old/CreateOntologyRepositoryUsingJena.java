package edu.isi.karma.er.test.old;

import java.util.List;
import java.util.Vector;

import edu.isi.karma.er.helper.ScoreBoardFileUtil;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.Paginator;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.helper.entity.ScoreBoard;
import edu.isi.karma.er.helper.ontology.MatchOntologyUtil;
import edu.isi.karma.er.helper.ontology.MatchResultOntology;
import edu.isi.karma.er.web.service.ResultService;

public class CreateOntologyRepositoryUsingJena {

	private static double THRESHOLD = 0.9;
	/**  
	 * @param args
	 */
	public static void main(String[] args) {
		
		ResultService ser = new ResultService();
		//ser.initOntology();
		ser.listHistory("", "");
		//ser.clearOntology();
		MatchOntologyUtil util = new MatchOntologyUtil();
		//util.outputLatestMatchResult();
		//util.outputAllMatchResult();
	
		Paginator pager = new Paginator();
		pager.setPageSize(3);
		pager.setCurPage(1);
		List<MatchResultOntology> list = util.listPagedLatestMatchResultObjects(pager, "");
		
		for (MatchResultOntology onto : list) {
			System.out.println(onto.getSrcUri());
		}
		
		System.out.println(pager.getTotalPage() + "\t" + pager.getTotalNumber());
		//loadRepositoryFromCSV();
		//model.write(System.out, "N3");
/*		
		MatchOntologyUtil util = new MatchOntologyUtil();
		//util.outputLatestMatchResult(model);
		
		util.outputLatestMatchResult();
		
		
//		String srcUri = "http://americanart.si.edu/saam/Person_894";
//		String dstUri = "http://dbpedia.org/resource/Alvan_Clark";
//		ScoreBoard sb = util.getLatestOneMatchResultObject(srcUri, dstUri);
//		sb.setComment("not sure");
//		sb.setCreator("Human");
//		sb.setMatched("U");
//		util.createMatchOntology(sb);
*/		
/*		
		List<MatchResultOntology> sbList = util.listLatestMatchResultObjects("");
		for (MatchResultOntology sb1 : sbList) {
			System.out.println(sb1.getSrcUri() + "\t" + sb1.getDstUri() + "\t" + sb1.getComment() + "\t" + sb1.getMatched() + "\t" + sb1.getUpdated());
		}
		System.out.println("list size:" + sbList.size());
/*		
		
		//util.outputLatestOneMatchResultObject(model, srcUri, dstUri);
		//System.out.println(sb.getSubject() + "\t" + sb.getKarmaUri() + "\t" + sb.getUpdated());
		util.outputLatestMatchResult();
*/
		
		
	}

	
	public static void loadRepositoryFromCSV() {
		ScoreBoardFileUtil util = new ScoreBoardFileUtil();
		String filename = util.getLastestResult();
		List<ScoreBoard> resultList = util.loadScoreResultFile(filename);
		
		String srcURI, dstURI, creator, comment ;
		double finalScore;
		MatchResultOntology onto = null;
		MatchOntologyUtil ontoUtil = new MatchOntologyUtil();
		
		for (int j = 0; j < resultList.size() && j < 10; j++) {
			ScoreBoard sb = resultList.get(j);
			srcURI = sb.getSaamUri(); //"http://smithsonianart.si.edu/saam/Person_2482";
			dstURI = sb.getKarmaUri();
			if (sb.getFound() > 0) {
				onto = new MatchResultOntology();
				MultiScore ms = sb.getRankList().get(0);
				
				creator = "Karma";
				finalScore = sb.getFound();
				if (finalScore >= THRESHOLD) {
					comment = "Exact match (" + finalScore + ")";
				} else {
					comment = "Not match ( " + finalScore + ")";
				}
				
				List<Score> slist = ms.getScoreList();
				
				onto.setFinalScore(finalScore);
				onto.setMatched("");
				onto.setSrcUri(srcURI);
				onto.setDstUri(dstURI);
				onto.setCreator(creator);
				onto.setComment(comment);
				onto.setMemberList(slist);
				
				ontoUtil.createMatchOntology(onto);
			}
		}

	}

	

	
	public static void createRepository() {
		String srcURI, dstURI, creator, comment ;
		double finalScore;
		
		MatchOntologyUtil util = new MatchOntologyUtil();
		
		
		String[] srcAttr1 = {"http://americanart.si.edu/saam/deathYear", "http://americanart.si.edu/saam/birthYear", "http://americanart.si.edu/saam/fullName"};
		String[] srcVal1 = {"1887", "1804", "Alvan Clark"};
		String[] dstVal1 = {"1887", "1804", "Alvan Clark"};

		srcURI = "http://smithsonianart.si.edu/saam/Person_2482";
		dstURI = "http://dbpedia.org/Andy_Smith";
		comment = "";
		finalScore = 0.999986;
		creator = "Karma";
		
		MatchResultOntology onto = new MatchResultOntology();
		onto.setSrcUri(srcURI);
		onto.setDstUri(dstURI);
		onto.setComment(comment);
		onto.setCreator(creator);
		onto.setFinalScore(finalScore);
		List<Score> list = new Vector<Score>();
		for (int i= 0; i < srcAttr1.length; i++) {
			Score s = new Score();
			s.setPredicate(srcAttr1[i]);
			s.setSrcObj(srcVal1[i]);
			s.setDstObj(dstVal1[i]);
			list.add(s);
		}
		onto.setMemberList(list);
		
		util.createMatchOntology(onto);
		/*
		
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
		
		util.createMatchOntology(model, finalScore, "", createTime, srcURI, dstURI, creator, comment, srcAttr, srcVal, dstAttr, dstVal);
		
		
		
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
		
		util.createMatchOntology(model, finalScore, "", createTime, srcURI, dstURI, creator, comment, srcAttr, srcVal, dstAttr, dstVal);
		
		
		
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
		
		util.createMatchOntology(model, finalScore, "", createTime, srcURI, dstURI, creator, comment, srcAttr, srcVal, dstAttr, dstVal);
		
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
		
		util.createMatchOntology(model, finalScore, "", createTime, srcURI, dstURI, creator, comment, srcAttr, srcVal, dstAttr, dstVal);
		
		
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
		
		util.createMatchOntology(model, finalScore, "", createTime, srcURI, dstURI, creator, comment, srcAttr, srcVal, dstAttr, dstVal);
		*/
		util.outputAllMatchResult();
		
		util.outputLatestMatchResult();
	}
	
}
