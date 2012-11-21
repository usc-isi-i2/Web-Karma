package edu.isi.karma.er.web.service;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import edu.isi.karma.er.helper.ScoreBoardFileUtil;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.NYTimes;
import edu.isi.karma.er.helper.entity.Ontology;
import edu.isi.karma.er.helper.entity.Paginator;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.helper.entity.ScoreBoard;
import edu.isi.karma.er.helper.ontology.MatchOntologyUtil;
import edu.isi.karma.er.helper.ontology.MatchResultOntology;
   

public class ResultService {
	private MatchOntologyUtil util = null;
	
	public ResultService(String repositoryName) {
		util = new MatchOntologyUtil();
		util.setRepositoryName(repositoryName);
	}
 
	public static List<String> getRepositoryList() {
		List<String> repoList = MatchOntologyUtil.listRepositories();
		
		return repoList;
	}
	
	public List<MatchResultOntology> getResultList(Paginator pager, String sortBy) {
		
		List<MatchResultOntology> resultList = util.listPagedLatestMatchResultObjects(pager, sortBy);
		return resultList;
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
		util.clear();
	}
	
	public List<MatchResultOntology> listHistory(String srcUri, String dstUri) {
		List<MatchResultOntology> list = util.listMatchResultObjectWithGiven(srcUri, dstUri);
		return list;
	}

	
	public void initOntology() {
		double THRESHOLD = 0.9;
		ScoreBoardFileUtil sbutil = new ScoreBoardFileUtil();
		String filename = sbutil.getLastestResult();
		List<ScoreBoard> resultList = sbutil.loadScoreResultFile(filename);
		
		String creator, comment;
		double finalScore;
		
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
				
				util.createMatchOntology(onto);
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

	public List<String> getPredicateList(List<MatchResultOntology> resultList) {
		List<String> predList = new Vector<String>();
		if (resultList != null && resultList.size() > 0) {
			List<Score> scList = resultList.get(0).getMemberList();
			for (int i = 0; i < scList.size(); i++) {
				Score s = scList.get(i);
				String pred = s.getPredicate().substring(s.getPredicate().lastIndexOf('/') + 1);
				predList.add(pred);
			}
		}
		return predList;
	}

	public int createOrUpdateRespository(String resultFile) {
		int count = 0;
		
		ScoreBoardFileUtil sbutil = new ScoreBoardFileUtil();
		List<ScoreBoard> resultList = sbutil.loadScoreResultFile(resultFile);
		
		NYTimes nyt = new NYTimes();
		Map<String, NYTimes> map = nyt.listAllToMap();
		MatchResultOntology onto = null;
		
		for (int j = 0; j < resultList.size() ; j++) {
			ScoreBoard sb = resultList.get(j);
			onto = util.createMatchOntology(sb, map);
			if (onto != null)
				count ++;
			
		}

		return count;
		
	}

	public void addRepositoryToList(String repoName) {
		util.addRepositoryName(repoName);
		
	}

	
	
}
