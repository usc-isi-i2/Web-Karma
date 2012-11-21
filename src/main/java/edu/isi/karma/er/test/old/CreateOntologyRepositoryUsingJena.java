package edu.isi.karma.er.test.old;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.util.FileManager;

import edu.isi.karma.er.helper.Constants;
import edu.isi.karma.er.helper.ScoreBoardFileUtil;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.NYTimes;
import edu.isi.karma.er.helper.entity.Paginator;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.helper.entity.ScoreBoard;
import edu.isi.karma.er.helper.ontology.MatchOntologyUtil;
import edu.isi.karma.er.helper.ontology.MatchResultOntology;
import edu.isi.karma.er.web.service.ResultService;

public class CreateOntologyRepositoryUsingJena {

	private static double THRESHOLD = 0.95;
	/**  
	 * @param args
	 */
	public static void main(String[] args) {

		loadRepositoryFromCSV();
		
		// listRevisionsForGivenSrcDst();
		
		//updateGroundTruthForSaam();
		
		// batchLoadingDataFromCSV();
		
		// getLatestMatchResults();
		
		//getVerifiedGroundTruth();
		
		// outputModelToN3();
		// loadN3ToModel();
		
		//mapToNytimes();
	}
	
	public static void getVerifiedGroundTruth() {
		MatchOntologyUtil util = new MatchOntologyUtil();
		util.setRepositoryName("match_result");
		List<MatchResultOntology> list = util.listLatestMatchResultObjectsHumanFirst("src_asc");
		for (MatchResultOntology onto: list) {
			if ("M".equals(onto.getMatched())) {
				System.out.println(onto.getMatched() + "," + onto.getSrcUri() + "," + onto.getDstUri() + "," + onto.getCreator() + "," + onto.getComment());
			}
		}
		System.out.println("size:" + list.size());
	}

	public static void mapToNytimes() {
		NYTimes nyt = new NYTimes();
		Map<String, NYTimes> map = nyt.listAllToMap();
		MatchOntologyUtil util = new MatchOntologyUtil();
		List<MatchResultOntology> list = util.listLatestMatchResultObjects("", "");
		for (MatchResultOntology onto : list) {
			nyt = map.get(onto.getDstUri());
			if (nyt != null) {
				System.out.println(onto.getSrcUri() + "\t" + onto.getDstUri() + "\t" + nyt.getSubject());
			}
		}
	}

	public static void outputModelToN3() {
		MatchOntologyUtil util = new MatchOntologyUtil();
		util.setRepositoryName("match_result");
		File file = new File(Constants.PATH_N3_FILE + "match_result_export.n3");
		if (file.exists()) {
			file.delete();
		}
		FileOutputStream fos = null;
		try {
			file.createNewFile();
			fos = new FileOutputStream(file);
			util.getModel().write(fos, "N3");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void loadN3ToModel() {
		MatchOntologyUtil util = new MatchOntologyUtil();
		util.setRepositoryName("match_result");
		Model model = util.getModel();
		model.begin();
		model.removeAll();
		
		FileManager.get().readModel(model, Constants.PATH_N3_FILE + "match_result_export.n3");
		model.commit();
		TDB.sync(model);
	}

	public static void getLatestMatchResults() {
		/*
		MatchOntologyUtil util = new MatchOntologyUtil();
		util.setRepositoryName("match_result"); */
		Paginator pager = new Paginator();
		pager.setCurPage(1);
		pager.setPageSize(50);
		
		ResultService ser = new ResultService("match_result");
		System.out.println(ResultService.getRepositoryList().get(0));
		List<MatchResultOntology> list = ser.getResultList(pager, "");
		for (MatchResultOntology onto : list) {
			NYTimes nyt = onto.getNytimes();
			System.out.println(onto.getCreator() + "\t" + onto.getComment() + "\t" + onto.getUpdated() + "\t" + (nyt == null ? "no nytimes" : nyt.getSubject()));
		}
	}

	public static void getLatestOneMatchResult() {
		String srcUri = "http://americanart.si.edu/saam/Person_4809";
		String dstUri = "http://dbpedia.org/resource/Alessandro_Tiarini";
		MatchOntologyUtil util = new MatchOntologyUtil();
		util.setRepositoryName("match_result");
		
		MatchResultOntology onto = util.getLatestOneMatchResultObject(srcUri, dstUri, "Karma");
		System.out.println(onto.getResId() + "\t" + onto.getComment() + "\t" + onto.getUpdated());
		
		onto = util.getLatestOneMatchResultObject(srcUri, dstUri, "Human");
		System.out.println(onto.getResId() + "\t" + onto.getComment() + "\t" + onto.getUpdated());
		
	}

	public static void listRevisionsForGivenSrcDst() {
		MatchOntologyUtil util = new MatchOntologyUtil();
		util.setRepositoryName("match_result");
		String srcUri = "http://americanart.si.edu/saam/Person_1882";
		String dstUri = "http://dbpedia.org/resource/Arshile_Gorky";
		
		
		List<MatchResultOntology> list = util.listMatchResultObjectWithGiven(srcUri, dstUri);
		for (MatchResultOntology onto : list) {
			NYTimes nyt = onto.getNytimes();
			System.out.println(onto.getCreator() + "\t" + onto.getComment() + "\t" + onto.getUpdated() + "\t" + (nyt == null ? "no nytimes" : nyt.getSubject()));
			
		}
		
	}

	
	public static void loadRepositoryFromCSV() {
		ScoreBoardFileUtil util = new ScoreBoardFileUtil();
		String filename = util.getLastestResult();
		List<ScoreBoard> resultList = util.loadScoreResultFile(filename);
		
		NYTimes nyt = new NYTimes();
		Map<String, NYTimes> map = nyt.listAllToMap();
		
		String srcURI, dstURI, creator, comment ;
		double finalScore;
		MatchResultOntology onto = null;
		MatchOntologyUtil ontoUtil = new MatchOntologyUtil();
		ontoUtil.setRepositoryName("match_result");
		
		for (int j = 0; j < resultList.size() ; j++) {
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
				if (map != null) {
					onto.setNytimes(map.get(dstURI));
				}
				
				ontoUtil.createMatchOntology(onto);
			}
		}
		
		System.out.println("finished.");

	}

	

	
	public static void createRepository() {
		String srcURI, dstURI, creator, comment ;
		double finalScore;
		
		MatchOntologyUtil util = new MatchOntologyUtil();
		
		NYTimes nyt = new NYTimes();
		Map<String, NYTimes> map = nyt.listAllToMap();
		
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
		if (map != null) {
			onto.setNytimes(map.get(dstURI));
		}
		util.createMatchOntology(onto);
		
		util.outputAllMatchResult();
		
		util.outputLatestMatchResult();
	}
	
	public static List<MatchResultOntology> loadBuildingOntologyFromCSV() {
		List<MatchResultOntology> mlist = new Vector<MatchResultOntology>();
		String filename = "BuildingMatchResult.csv";
		File file = new File(Constants.PATH_SCORE_BOARD_FILE + filename);
		if (!file.exists())
			throw new IllegalArgumentException("file " + file.getAbsolutePath() + " not exists.");
		
		RandomAccessFile raf = null;
		
		try {
			raf = new RandomAccessFile(file, "r");
			String line;
			raf.readLine();
			List<Score> sList = null;
			Score s = null;
			
			while ((line = raf.readLine()) != null) {
				String[] arr = split(line, ",");
				
				if (line.indexOf("http") > -1) {
					if (arr.length >= 12) {
						MatchResultOntology onto = new MatchResultOntology();
						onto.setSrcUri(arr[0]);
						onto.setFinalScore(Double.parseDouble(arr[11]));
						onto.setDstUri(arr[5]);
						onto.setCreator("Karma");
						if (onto.getFinalScore() > 0.9) {
							onto.setMatched("M");
							onto.setComment("Exact Match (" + onto.getFinalScore() + ")");
						} else {
							onto.setMatched("N");
							onto.setComment("Not Match (" + onto.getFinalScore() + ")");
						}
						
						sList = new Vector<Score>();
						s = new Score();
						s.setPredicate("NAME");
						s.setSrcObj(arr[1]);
						s.setDstObj(arr[6]);
						sList.add(s);
						
						s = new Score();
						s.setPredicate("X Pos.");
						s.setSrcObj(arr[2]);
						s.setDstObj(arr[7]);
						sList.add(s);
						
						s = new Score();
						s.setPredicate("Y Pos.");
						s.setSrcObj(arr[3]);
						s.setDstObj(arr[8]);
						sList.add(s);
						
						s = new Score();
						s.setPredicate("Polygon");
						s.setSrcObj(arr[4]);
						s.setDstObj(arr[9]);
						sList.add(s);
						
						onto.setMemberList(sList);
						mlist.add(onto);
					}
				} 
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		return mlist;
	}
	
	private static String[] split(String str, String delimiter) {
		ArrayList<String> list = new ArrayList<String>();
		int index = str.indexOf(delimiter);
		while (index > -1) {
			list.add(str.substring(0, index));
			str = str.substring(index+delimiter.length());
			index = str.indexOf(delimiter);
		}
		list.add(str);
		String[] arr = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			arr[i] = list.get(i);
		}
		
		return arr;
	}
	
	public static void updateGroundTruthForSaam() {
		ScoreBoardFileUtil sbutil = new ScoreBoardFileUtil();
		List<ScoreBoard> list = sbutil.loadScoreBoardFile("score_board.csv");
		
		MatchOntologyUtil util = new MatchOntologyUtil();
		util.setRepositoryName("match_result");
		int count = 0;
		
		for (ScoreBoard sb : list) {
			if (sb.getSubject().length() > 0 && sb.getDbpediaUri().length() > 0) {
				MatchResultOntology onto = util.getLatestOneMatchResultObject(sb.getSubject(), sb.getDbpediaUri());
				if (onto != null) {
					onto.setComment("Verified by Human");
					onto.setCreator("Human");
					onto.setMatched("M");
					util.createMatchOntology(onto);
					
					count ++;
				}
			}
		}
		System.out.println(count + " updated.");
	}
	
	public static void batchLoadingDataFromCSV() {
		String filename, repositoryName;
		char[] letters = {'B', 'C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
		ScoreBoardFileUtil sbutil = new ScoreBoardFileUtil();
		String srcURI, dstURI, creator, comment, matched ;
		double finalScore;
		MatchResultOntology onto = null;
		
		NYTimes nyt = new NYTimes();
		Map<String, NYTimes> map = nyt.listAllToMap();
		
		for (char ch : letters) {
			long startTime = System.currentTimeMillis();
			System.out.println("part " + ch);
			filename = "result_20121208_" + ch + ".csv";
			repositoryName = "SAAM_links_part" + ch;
			List<ScoreBoard> resultList = sbutil.loadScoreResultFile(filename);
			MatchOntologyUtil util = new MatchOntologyUtil();
			util.setThreshold(THRESHOLD);
			util.setRepositoryName(repositoryName);
			
			for (int j = 0; j < resultList.size(); j++) {
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
						matched = "M";
					} else {
						comment = "Not match ( " + finalScore + ")";
						matched = "N";
					}
					
					List<Score> slist = ms.getScoreList();
					
					onto.setFinalScore(finalScore);
					onto.setMatched(matched);
					onto.setSrcUri(srcURI);
					onto.setDstUri(dstURI);
					onto.setCreator(creator);
					onto.setComment(comment);
					onto.setMemberList(slist);
					if (map != null) {
						onto.setNytimes(map.get(dstURI));
					}
					util.createMatchOntology(onto);
				}
			}
			
			System.out.println(" finished in " + (System.currentTimeMillis() - startTime));
			
		}
	}
}
