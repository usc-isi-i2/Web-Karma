package edu.isi.karma.er.test.old;

import java.util.List;

import edu.isi.karma.er.helper.ScoreBoardFileUtil;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.helper.entity.ScoreBoard;

public class TestScoreBoardFileMain {
   
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ScoreBoardFileUtil util = new ScoreBoardFileUtil();
		/*
		Map<String, ScoreBoard> map = util.loadScoreBoard();
		
		int i = 0;   
		for (String str : map.keySet()) {
			ScoreBoard s = map.get(str);
			if (s.getDbpediaUri() != null) {
				System.out.println((++i) + "\t" + str + "\t" + s.getDbpediaUri());
			}
		}
		*/
		
		List<ScoreBoard> list = util.loadScoreBoardFile("result2012-10-23-09-42.csv");
		  
		for (ScoreBoard sb : list) {
			if (sb.getRankList() != null && sb.getRankList().size() > 0) {
				MultiScore ms = sb.getRankList().get(0);
				
				Score bs = ms.getScoreList().get(0);
				Score ds = ms.getScoreList().get(1);
				Score fs = ms.getScoreList().get(2);
				
				System.out.println(sb.getSubject() + "\t" + sb.getDbpediaUri() + "\t" + bs.getSrcObj() + "|" + bs.getDstObj()
						+ "\t" + ds.getSrcObj() + "|" + ds.getDstObj() + "\t" + fs.getSrcObj() + "|" + fs.getDstObj()
						+ "\t" + sb.getFound() + sb.getRankList().size() + "\t" + (sb.getFound() > 0 && sb.getKarmaUri().equals(sb.getDbpediaUri()) ? "same" : "not same"));
			} else {
				System.out.println(sb.getSubject() + "\t" + sb.getDbpediaUri() + "\t\t\t\t" + (sb.getFound() > 0 && sb.getKarmaUri().equals(sb.getDbpediaUri()) ? "same" : "not same"));
			}
		}

	}

}
