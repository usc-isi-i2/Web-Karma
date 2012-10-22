package edu.isi.karma.er.test.old;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.Vector;

import edu.isi.karma.er.helper.Constants;
import edu.isi.karma.er.helper.entity.ScoreBoard;

public class DrawDiagramFromScoreBoard {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filename = "result2012-10-19-15-29.csv";
		Vector<ScoreBoard> list = loadScoreBoardFile(filename);
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

	private static Vector<ScoreBoard> loadScoreBoardFile(String filename) {
		File file = new File(Constants.PATH_SCORE_BOARD_FILE + filename);
		if (!file.exists())
			throw new IllegalArgumentException("file " + file.getAbsolutePath() + " not exists.");
		
		RandomAccessFile raf = null;
		Vector<ScoreBoard> list = new Vector<ScoreBoard>();
		
		try {
			raf = new RandomAccessFile(file, "r");
			String line;
			raf.readLine();
			double found = -1;
			
			while ((line = raf.readLine()) != null) {
				String[] arr = line.split(",");
				
				if (arr.length >= 5) {
					ScoreBoard s = new ScoreBoard();
					s.setDbpediaUri(arr[2]);
					s.setKarmaUri(arr[3]);
					try {
						found = Double.parseDouble(arr[4]);
					} catch (NumberFormatException nfe) {
						found = -1;
					}
					s.setFound(found);
					list.add(s);
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
		
		return list;
	}
	
	
}
