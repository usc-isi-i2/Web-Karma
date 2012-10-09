package edu.isi.karma.er.test.old;

import java.util.Map;

import edu.isi.karma.er.helper.ScoreBoardFileUtil;
import edu.isi.karma.er.helper.entity.ScoreBoard;

public class TestScoreBoardFileMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ScoreBoardFileUtil util = new ScoreBoardFileUtil();
		Map<String, ScoreBoard> map = util.loadScoreBoard();
		
		int i = 0;
		for (String str : map.keySet()) {
			ScoreBoard s = map.get(str);
			if (s.getDbpediaUri() != null) {
				System.out.println((++i) + "\t" + str + "\t" + s.getDbpediaUri());
			}
		}

	}

}
