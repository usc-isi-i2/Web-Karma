package edu.isi.karma.er.compare.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import edu.isi.karma.er.compare.PointToPointComparator;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.Score;

public class PointToPointComparatorImpl implements PointToPointComparator {

	public MultiScore getSimilarity(Connection connection, MultiScore result,
			double x1, double y1, double x2, double y2) {
		List<Score> lists = new LinkedList<Score>();
		Statement stmt = null;
		ResultSet rs = null;
		double dis = 0;
		int threshold = 50;

		try {
			stmt = connection.createStatement();

		} catch (Exception ex) {
			ex.getStackTrace();
		}

		try {
			rs = stmt
					.executeQuery("SELECT ST_Distance(ST_GeographyFromText(\'SRID=4326;POINT("
							+ x1
							+ " "
							+ y1
							+ " )\'), "
							+ "ST_GeographyFromText(\'SRID=4326; POINT("
							+ x2
							+ " " + y2 + " )\'));");

			if (rs.next()) {
				try {
					Score ss = new Score();
					ss.setFreq(rs.getDouble(1));
					lists.add(0, ss);
					result.setScoreList(lists);

				} catch (SQLException e) {
					e.printStackTrace();
				}

			}

		} catch (SQLException ee) {
			ee.getStackTrace();
		}

		dis = result.getScoreList().get(0).getFreq();

		if (dis > threshold) {
			result.getScoreList().get(0).setSimilarity(0);
			result.setFinalScore(0);
		} else {
			double res = 1 - (dis / threshold);
			result.getScoreList().get(0).setSimilarity(res);
			result.setFinalScore(res);
		}

		return result;

	}

}
