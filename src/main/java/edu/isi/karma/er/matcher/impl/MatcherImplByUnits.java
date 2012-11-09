package edu.isi.karma.er.matcher.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import edu.isi.karma.er.helper.entity.InputStruct;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.matcher.MatcherBuilding;

public class MatcherImplByUnits implements MatcherBuilding {

	public MatcherImplByUnits() {
	}

	public MultiScore matchBuilding(Connection connection, MultiScore result,
			String p, String q, InputStruct v, InputStruct w) {

		String watp = null;
		String watq = null;
		List<Score> lists = new LinkedList<Score>();
		double x1 = 0.0;
		double y1 = 0.0;
		double x2 = 0.0;
		double y2 = 0.0;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.createStatement();
		} catch (Exception ex) {
			ex.getStackTrace();
		}

		if (p.equals("POINT")) {
			x1 = v.getX();
			y1 = v.getY();
		} else if (p.equals("POLYGON")) {

			try {
				rs = stmt.executeQuery("SELECT ST_AsText(\'" + v.getPolygon()
						+ "\')");
				while (rs.next()) {

					try {
						watp = rs.getString(1);
					} catch (SQLException e) {
						e.printStackTrace();
					}

				}
			} catch (SQLException ee) {
				ee.getStackTrace();
			}

		}

		if (q.equals("POINT")) {
			x2 = w.getX();
			y2 = w.getY();

			if (p.equals("POINT")) {// POINT VS POINT；
				try {
					rs = stmt
							.executeQuery(" SELECT ST_Distance( ST_GeomFromText(\'POINT("
									+ x1
									+ " "
									+ y1
									+ " )\',4326), "
									+ "ST_GeomFromText(\'POINT("
									+ x2
									+ " "
									+ y2 + " )\', 4326));");

					while (rs.next()) {

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
			} else if (p.equals("POLYGON")) {// POLYGON VS POINT；

				try {
					rs = stmt
							.executeQuery(" SELECT ST_Distance( ST_GeomFromText(\'POINT("
									+ x2
									+ " "
									+ y2
									+ " )\',4326), "
									+ "ST_GeomFromText(\'"
									+ watp
									+ "\', 4326));");

					while (rs.next()) {
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

			}

		} else if (q.equals("POLYGON")) {
			try {
				rs = stmt.executeQuery("SELECT ST_AsText(\'" + w.getPolygon()
						+ "\')");
				while (rs.next()) {

					try {
						watq = rs.getString(1);
					} catch (SQLException e) {
						e.printStackTrace();
					}

				}

			} catch (SQLException ee) {
				ee.getStackTrace();
			}
			if (p.equals("POINT")) {// POINT VS POLYGON ；
				try {
					rs = stmt
							.executeQuery(" SELECT ST_Distance( ST_GeomFromText(\'POINT("
									+ x1
									+ " "
									+ y1
									+ " )\',4326), "
									+ "ST_GeomFromText(\'"
									+ watq
									+ "\', 4326));");

					while (rs.next()) {

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
			} else if (p.equals("POLYGON")) {// POLYGON VS POLYGON ；

				try {
					rs = stmt
							.executeQuery(" SELECT ST_Distance( ST_GeomFromText(\'"
									+ watp
									+ "\',4326), "
									+ "ST_GeomFromText(\'"
									+ watq
									+ "\', 4326));");

					while (rs.next()) {

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

			}

		}
		double sim = 1 - result.getScoreList().get(0).getFreq();
		result.getScoreList().get(0).setSimilarity(sim);
		return result;
	}

}
