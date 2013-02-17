package edu.isi.karma.er.test;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

import edu.isi.karma.er.helper.ConfigUtil;
import edu.isi.karma.er.helper.Constants;
import edu.isi.karma.er.helper.ScoreBoardFileUtil;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.NYTimes;
import edu.isi.karma.er.helper.entity.ResultRecord;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.helper.entity.ScoreBoard;
import edu.isi.karma.er.helper.ontology.MatchOntologyUtil;
import edu.isi.karma.er.linkage.LinkageFinder;
//import com.hp.hpl.jena.util.FileManager;

public class TestPairMatchMain {
	static Logger log = Logger.getRootLogger();								// log the output into both file and terminal, see log4j.properties
	static double threshold = 0.85;
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	


	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		log.info("Program execution start from: " + sdf.format(new java.util.Date()));
		//Model model = TDBFactory.createDataset(Constants.PATH_REPOSITORY + "saam_a/").getDefaultModel();
		//Model model2 = TDBFactory.createDataset(Constants.PATH_REPOSITORY + "dbpedia_a/").getDefaultModel();
		Model model2 = FileManager.get().loadModel(Constants.PATH_N3_FILE + "dbpedia_dbpprop_start_with_A.n3");
		Model model = FileManager.get().loadModel(Constants.PATH_N3_FILE + "saam_name_birth_death_A.n3");
		//Model model = FileManager.get().loadModel(Constants.PATH_N3_FILE + "getty_sample.n3");
		
		log.info("finish loading."); 
		
		ConfigUtil util = new ConfigUtil();
		util.loadConstants();
		JSONArray confArr = util.loadConfig();
		
		LinkageFinder finder = new LinkageFinder(confArr);
		finder.setThreshold(threshold);
		List<ResultRecord> resultList = finder.findLinkage(model, model2);						// match and return linkage result in list.
		
		outputResultWithTemplate(resultList, confArr);
		
		//outputResult2Ontology(resultList, confArr);
		
		log.info("total time elapsed:" + (System.currentTimeMillis() - startTime) / 1000 + "s");
		log.info("Program finished at:" + sdf.format(new java.util.Date()));
	}

	public static void outputResult2Ontology(List<ResultRecord> resultList, JSONArray confArr) {
		NYTimes nyt = new NYTimes();
		Map<String, NYTimes> map = nyt.listAllToMap();
		
		MatchOntologyUtil util = new MatchOntologyUtil();
		util.setRepositoryName("match_result");
		util.createMatchOntology(resultList, map);
		
		util.outputLatestMatchResult();
		
	}

	public static void outputResultWithoutTemplate(List<ResultRecord> resultList, JSONArray confArr) {
		int count = 0;
		DecimalFormat df = new DecimalFormat("0.0000000000");
		
		ScoreBoardFileUtil util = new ScoreBoardFileUtil();
		//Map<String, ScoreBoard> map = util.loadScoreBoard();
		
		for (int i = 0; i < resultList.size(); i++) {	// output results.
			
			ResultRecord rec = resultList.get(i);
			String mark = "";
			//if (rec.getCurrentMaxScore() >= threshold) {
				// prepare to write output csv file 
				//ScoreBoard sb = map.get(rec.getRes().getSubject());
				//System.out.println(rec.getRes().getURI());
				//String res = null;
				//if (rec.getRankList().size() > 0)
				//	res = rec.getRankList().get(0).getDstSubj().getSubject();
				//sb.setRankList(rec.getRankList());
				//sb.setKarmaUri(res);
				//sb.setFound(rec.getCurrentMaxScore()); 			// count means valid results with similarity greater than threshold 0.9
				//map.put(sb.getSubject(), sb);
				
				// prepare to write log in log4j
				if (rec.getCurrentMaxScore() >= threshold) {
					count ++;													// count means valid results with similarity greater than threshold 0.9
					mark = "(****** " + count + " *****)";		
				}
			//}
			
			log.info("==============================================================================");
			log.info(rec.getRes().getSubject() + "\t\t\t\t\t " + mark);
		
			List<MultiScore> sList = rec.getRankList();
			for (int j = 0; j < sList.size(); j++) {
				MultiScore ms = sList.get(j);
				log.info("\t [" + df.format(ms.getFinalScore()) + "] " + ms.getDstSubj().getSubject());
				
				for (Score s : ms.getScoreList()) {
					String srcObj = (s.getSrcObj() == null ? "" : s.getSrcObj());
					String dstObj = (s.getDstObj() == null ? "" : s.getDstObj());
					log.info("\t\t\t[ " 
							+ df.format(s.getSimilarity()) 
							+ "]\t[ " + srcObj 
							+ " | " + dstObj 
							+ " ]");
				}
				
			}
			log.info("==============================================================================\n");
			
		}
		log.info("************************************************************************************");
		log.info("Total results found:" + count + " of " + resultList.size());
		log.info("************************************************************************************");
		
		util.write2Log(resultList, confArr);		// write to output csv file.
			
	}
	
	public static void outputResultWithTemplate(List<ResultRecord> resultList, JSONArray confArr) {
		int count = 0;
		DecimalFormat df = new DecimalFormat("0.0000000000");
		
		ScoreBoardFileUtil util = new ScoreBoardFileUtil();
		util.setThreshold(threshold);
		Map<String, ScoreBoard> map = util.loadScoreBoard();
		
		for (int i = 0; i < resultList.size(); i++) {	// output results.
			
			ResultRecord rec = resultList.get(i);
			String mark = "";
			//if (rec.getCurrentMaxScore() >= threshold) {
				// prepare to write output csv file 
				ScoreBoard sb = map.get(rec.getRes().getSubject());
				//System.out.println(rec.getRes().getURI());
				String res = null;
				if (rec.getRankList().size() > 0)
					res = rec.getRankList().get(0).getDstSubj().getSubject();
				sb.setRankList(rec.getRankList());
				sb.setKarmaUri(res);
				sb.setFound(rec.getCurrentMaxScore()); 			// count means valid results with similarity greater than threshold 0.9
				map.put(sb.getSubject(), sb);
				
				// prepare to write log in log4j
				if (rec.getCurrentMaxScore() >= threshold) {
					count ++;													// count means valid results with similarity greater than threshold 0.9
					mark = "(****** " + count + " *****)";		
				}
			//}
			
			log.info("==============================================================================");
			log.info(rec.getRes().getSubject() + "\t\t\t\t\t " + mark);
		
			List<MultiScore> sList = rec.getRankList();
			for (int j = 0; j < sList.size(); j++) {
				MultiScore ms = sList.get(j);
				log.info("\t [" + df.format(ms.getFinalScore()) + "] " + ms.getDstSubj().getSubject());
				
				for (Score s : ms.getScoreList()) {
					String srcObj = (s.getSrcObj() == null ? "" : s.getSrcObj());
					//String pred = (s.getSrcObj() == null ? "": s.getSrcObj());
					String dstObj = (s.getDstObj() == null ? "" : s.getDstObj());
					log.info("\t\t\t[ " 
							+ df.format(s.getSimilarity()) 
							+ "]\t[ " + srcObj 
							+ " | " + dstObj 
							+ " ]");
				}
				
			}
			log.info("==============================================================================\n");
			
		}
		log.info("************************************************************************************");
		log.info("Total results found:" + count + " of " + resultList.size());
		log.info("************************************************************************************");
		
		util.write2Log(map, confArr);		// write to output csv file.
		
		
		
		
	}

	
	
}

