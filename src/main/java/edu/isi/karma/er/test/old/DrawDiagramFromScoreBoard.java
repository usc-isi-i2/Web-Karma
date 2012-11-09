package edu.isi.karma.er.test.old;

import java.text.DecimalFormat;
import java.util.Vector;

import edu.isi.karma.er.helper.ScoreBoardFileUtil;
import edu.isi.karma.er.helper.entity.ScoreBoard;

public class DrawDiagramFromScoreBoard {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filename = "result2012-10-19-15-29.csv";
		ScoreBoardFileUtil util = new ScoreBoardFileUtil();
		Vector<ScoreBoard> list = util.loadScoreBoardFile(filename);
		sortByScoreDesc(list);
		
		drawGraphData(list);
	}

	private static void drawGraphData(Vector<ScoreBoard> list) {
		System.out.println("size:" + list.size());
		DecimalFormat df = new DecimalFormat("#.0000");
		double found = 0, count  = 0, total = 175, lastResult = -1;
		for (ScoreBoard s : list) {
			if (s.getFound() > 0) {
				count ++;
				if (s.getKarmaUri().equals(s.getDbpediaUri())) {
					found ++;
				}
				if (lastResult - s.getFound() > 1e-8)
					System.out.println(s.getFound() + "\t" + df.format(found / count)  + "\t" + df.format(found / total) + "\t" + df.format(2*found/(count + total)));
				lastResult = s.getFound();
			}
		}
		
	}

	private static void sortByScoreDesc(Vector<ScoreBoard> list) {
		int i,j, len = list.size();
		ScoreBoard s1, s2;
		
		for (i = 0; i < len - 1; i++) {
			for (j = len - 1; j > i; j--) {
				s1 = list.get(j);
				s2 = list.get(j-1);
				if (s1.getFound() > s2.getFound()) {
					list.set(j, s2);
					list.set(j-1, s1);
				}
			}
		}
		
	}

	
	
}
