package edu.isi.karma.er.helper;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

import org.apache.log4j.Logger;
import org.openjena.atlas.logging.Log;

import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.helper.entity.ScoreBoard;

public class ScoreBoardFileUtil {
	
	private String scoreBoardFile = Constants.PATH_SCORE_BOARD_FILE + "score_board.csv";
	
	private Logger log = Logger.getRootLogger();
	/**
	 * Load data of score template from scoreboard file.
	 * @return map of pair which saam uri as the key and scoreboard object as the value.
	 */
	public Map<String, ScoreBoard> loadScoreBoard() {
		Map<String, ScoreBoard> sMap = new HashMap<String, ScoreBoard>();
		File file = new File(scoreBoardFile);
		
		if (file == null || !file.exists()) {
			throw new IllegalArgumentException("the file input " + file.getAbsolutePath() + " does not exist actually.");
		}
		
		RandomAccessFile raf = null;
		
		
		try {
			String str = null;
			raf = new java.io.RandomAccessFile(file, "rw");
			
			
			while ((str = raf.readLine()) != null) {			// read a text line each time from ratio file
				String[] lines = split(str);
				
				if (lines.length >= 4) {						// 2 elements separated by ':' for a text line.
					ScoreBoard sb = new ScoreBoard();
					sb.setSubject(lines[0]);
					sb.setSaamUri(lines[1]);
					sb.setWikiUri(lines[2]);
					sb.setDbpediaUri(lines[3]);
					sMap.put(lines[0], sb);
				}
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return sMap;
	}
	
	public void write2Log(Map<String, ScoreBoard> map) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
		write2Log(map, Constants.PATH_SCORE_BOARD_FILE + "result" + sdf.format(new Date()) + ".csv");
	}
	
	public void write2Log(Map<String, ScoreBoard> map, String logFile) {
		File file = new File(logFile);
		if (file.exists()) {
			file.delete();
		}
		
		RandomAccessFile raf = null;
		int count = 0, i = 0, perfCount = 0, thresholdCount = 0; 
		DecimalFormat df = new DecimalFormat("0.0000");
		try {
			file.createNewFile();
			raf = new RandomAccessFile(file, "rw");
			raf.writeBytes("SAAM Person,URI in SAAM,URI in DBPedia,URI found by karma,matched, ,No.1,No.2,No.3,No.4,No.5 \r\n");
			for (String str: map.keySet()) {
				
				ScoreBoard s = map.get(str);
				if (s.getDbpediaUri().trim().length() > 0)
					i ++;
				if (s.getKarmaUri() != null && s.getKarmaUri().length() > 0) {
					count++;
					StringBuffer sb = new StringBuffer();
					List<MultiScore> rankList = s.getRankList();
					for (int k = 0; k < rankList.size(); k++) {
						MultiScore ms = rankList.get(k);
						sb.append(", [").append(df.format(ms.getFinalScore())).append("]");
						for (Score sc : ms.getScoreList()) {
							sb.append("\t(").append(df.format(sc.getSimilarity())).append("== ")
								.append(sc.getSrcObj() == null ? "----" : sc.getSrcObj().getObject().toString())
								.append(" | ")
								.append(sc.getDstObj() == null ? "----" : sc.getDstObj().getObject().toString()).append(") ");
						}
						
					}
					if (s.getDbpediaUri().equals(s.getKarmaUri())) {
						raf.writeBytes(s.getSubject() + "," + s.getSaamUri() + "," + s.getDbpediaUri() + "," + s.getKarmaUri() + "," + df.format(s.getFound()) + ", same" + sb.toString() + "\r\n");
							if (Math.abs(s.getFound() -1) < 1e-5) {
							perfCount ++;
						}
						thresholdCount ++;
					} else {
						raf.writeBytes(s.getSubject() + "," + s.getSaamUri() + "," + s.getDbpediaUri() + "," + s.getKarmaUri() + "," + df.format(s.getFound()) + ", not same" + sb.toString() + "\r\n");
						
					}
				} else {
					raf.writeBytes(s.getSubject() + "," + s.getSaamUri() + "," + s.getDbpediaUri() + ",,\r\n");
				}
			}
			raf.writeBytes("(similarity >= 0.9) precision: " + thresholdCount + " of " + count + " (" + df.format(thresholdCount*1.0/count) + ")\r\n");
			raf.writeBytes("(similarity >= 0.9) recall: " + thresholdCount + " of " + i + " (" + df.format(thresholdCount*1.0/i) + ")\r\n");
			raf.writeBytes("(similarity >= 0.9) F1 score:" + df.format(2.0*thresholdCount/(i+count)) + "\r\n");
			log.info("(similarity >= 0.9) precision: " + thresholdCount + " of " + count + " (" + df.format(thresholdCount*1.0/count) + ")");
			log.info("(similarity >= 0.9) recall: " + thresholdCount + " of " + i + " (" + df.format(thresholdCount*1.0/i) + ")");
			log.info("(similarity >= 0.9) F1 score:" + df.format(2.0*thresholdCount/(i+count)) + "\r\n");
			
			raf.writeBytes("(similarity = 1) precision: " + perfCount + " of " + count + " (" + df.format(perfCount*1.0/count) + ")\r\n");
			raf.writeBytes("(similarity = 1) recall: " + perfCount + " of " + i + " (" + df.format(perfCount*1.0/i) + ")\r\n");
			
		} catch (IOException e) {
			
			e.printStackTrace();
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	private String[] split(String str) {
		ArrayList<String> list = new ArrayList<String>();
		int index = str.indexOf(',');
		while (index > -1) {
			list.add(str.substring(0, index));
			str = str.substring(index+1);
			index = str.indexOf(',');
		}
		list.add(str);
		String[] arr = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			arr[i] = list.get(i);
		}
		
		return arr;
	}
}
