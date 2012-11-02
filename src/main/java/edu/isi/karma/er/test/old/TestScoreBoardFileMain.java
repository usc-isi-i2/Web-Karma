package edu.isi.karma.er.test.old;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import edu.isi.karma.er.helper.Constants;
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
		
		String filename = getLatestResult();
		List<ScoreBoard> list = util.loadScoreBoardFile(filename);
		
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
	
	private static String getLatestResult() {
		String path = Constants.PATH_SCORE_BOARD_FILE;
		File file = new File(path);
		if (!file.isDirectory()) {
			throw new IllegalArgumentException(path + " is not a directory");
		}
		FilenameFilter filter = new FilenameFilter() {

			
			public boolean accept(File dir, String name) {
				if (name.toLowerCase().endsWith(".csv")) 
					return true;
				return false;
			}};
		File[] files = file.listFiles(filter);
		long latest = files[0].lastModified();
		File latestFile = files[0];
		for (File f : files) {
			if (latest < f.lastModified()) {
				latest = f.lastModified();
				latestFile = f;
			}
		}
		return latestFile.getName();
	}

}
