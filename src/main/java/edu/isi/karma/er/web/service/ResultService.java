package edu.isi.karma.er.web.service;

import java.util.List;

import edu.isi.karma.er.helper.ScoreBoardFileUtil;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.Ontology;
import edu.isi.karma.er.helper.entity.Paginator;
import edu.isi.karma.er.helper.entity.ScoreBoard;
import edu.isi.karma.er.helper.ontology.MatchOntologyUtil;
import edu.isi.karma.er.helper.ontology.MatchResultOntology;
   

public class ResultService {
	private String filename = "";
	private MatchOntologyUtil util = null;
	
	public ResultService() {
		util = new MatchOntologyUtil();
	}
 
	public List<MatchResultOntology> getResultList(Paginator pager, String sortBy) {
		
		List<MatchResultOntology> resultList = util.listPagedLatestMatchResultObjects(pager, sortBy);
		return resultList;
	}

	public List<MatchResultOntology> getResultList(String sortBy) {
		
		List<MatchResultOntology> resultList = util.listLatestMatchResultObjects(sortBy);
		return resultList;
	}
	
	public String getFilename() {
		return this.filename;
	}

	public MatchResultOntology updateRecord(String srcUri, String dstUri, String matched,
			String comment) {
		MatchResultOntology onto = util.getLatestOneMatchResultObject(srcUri, dstUri);
		onto.setComment(comment);
		onto.setCreator("Human");
		onto.setMatched(matched);
		return util.createMatchOntology(onto);
		
	}
	
	public void clearOntology() {
		MatchOntologyUtil ontoUtil = new MatchOntologyUtil();
		ontoUtil.clear();
	}
	
	public List<MatchResultOntology> listHistory(String srcUri, String dstUri) {
		List<MatchResultOntology> list = util.listMatchResultObjectWithGiven(srcUri, dstUri);
		return list;
	}

	
	public void initOntology() {
		double THRESHOLD = 0.9;
		ScoreBoardFileUtil util = new ScoreBoardFileUtil();
		String filename = util.getLastestResult();
		List<ScoreBoard> resultList = util.loadScoreResultFile(filename);
		
		String creator, comment;
		double finalScore;
		
		MatchOntologyUtil ontoUtil = new MatchOntologyUtil();
		MatchResultOntology onto = new MatchResultOntology();
		
		for (int j = 0; j < resultList.size() ; j++) {
			ScoreBoard sb = resultList.get(j);
			if (sb.getFound() > 0) {
				MultiScore ms = sb.getRankList().get(0);
				Ontology ont = new Ontology();
				ont.setSubject(sb.getSubject());
				ms.setSrcSubj(ont);
				ont = new Ontology();
				ont.setSubject(sb.getKarmaUri());
				ms.setDstSubj(ont);
				sb.getRankList().set(0, ms);
				
				creator = "Karma";
				finalScore = sb.getFound();
				if (finalScore >= THRESHOLD) {
					comment = "Exact match (" + finalScore + ")";
					onto.setMatched("M");
				} else {
					comment = "Not match (" + finalScore + ")";
					onto.setMatched("N");
				}
								
				onto.setSrcUri(sb.getSubject());
				onto.setDstUri(sb.getKarmaUri());
				onto.setCreator(creator);
				onto.setComment(comment);
				onto.setMemberList(ms.getScoreList());
				onto.setFinalScore(finalScore);
				
				ontoUtil.createMatchOntology(onto);
			}
		}
		
	}
	
	public void updateOntology() {
		String srcUri = "http://americanart.si.edu/saam/Person_894";
		String dstUri = "http://dbpedia.org/resource/Alvan_Clark";
		MatchResultOntology onto = util.getLatestOneMatchResultObject(srcUri, dstUri);
		onto.setComment("not sure");
		onto.setCreator("Human");
		onto.setMatched("U");
		util.createMatchOntology(onto);
	}

	
	
}
