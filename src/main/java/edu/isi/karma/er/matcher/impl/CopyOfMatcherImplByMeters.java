package edu.isi.karma.er.matcher.impl;

import java.util.LinkedList;
import java.util.List;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edu.isi.karma.er.helper.entity.InputStruct;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.matcher.MatcherBuilding;

public class CopyOfMatcherImplByMeters implements MatcherBuilding {

	public CopyOfMatcherImplByMeters() {

	}

	public MultiScore matchBuilding(Connection connection, MultiScore result, String p, String q,
			InputStruct v, InputStruct w) {

		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.createStatement();

		} catch (Exception ex) {
			ex.getStackTrace();
		}

		String watp = null;
		String watq = null;
		List<Score> lists=new LinkedList<Score>();
		int threshold = 50;

		double x1 = 0.0;
		double y1 = 0.0;
		double x2 = 0.0;
		double y2 = 0.0;

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

			if (p.equals("POINT")) {// point VS point； SELECT ST_Distance
				try {
					rs = stmt
							.executeQuery("SELECT ST_Distance(ST_GeographyFromText(\'SRID=4326;POINT("
									+ x1
									+ " "
									+ y1
									+ " )\'), "
									+ "ST_GeographyFromText(\'SRID=4326; POINT("
									+ x2 + " " + y2 + " )\'));");

					while (rs.next()) {

						try {
							Score ss=new Score();
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
							.executeQuery("SELECT ST_Distance(ST_GeographyFromText(\'SRID=4326;POINT("
									+ x2
									+ " "
									+ y2
									+ " )\'), "
									+ "ST_GeographyFromText(\'SRID=4326; "
									+ watp + "\'));");

					while (rs.next()) {

						try {
							Score ss=new Score();
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
			// x2= mean();
			// y2=mean();
			System.out.println("PREPARING :");

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

			if (p.equals("POINT")) {// point VS polygon；
				try {

					rs = stmt
							.executeQuery("SELECT ST_Distance(ST_GeographyFromText(\'SRID=4326;POINT("
									+ x1
									+ " "
									+ y1
									+ " )\'), "
									+ "ST_GeographyFromText(\'SRID=4326; "
									+ watq + "\'));");

					while (rs.next()) {

						try {
							Score ss=new Score();
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
			} else if (p.equals("POLYGON")) {// polygon VS polygon；

				try {
					rs = stmt
							.executeQuery("SELECT ST_Distance(ST_GeographyFromText(\'SRID=4326; "
									+ watp
									+ " \'), "
									+ "ST_GeographyFromText(\'SRID=4326; "
									+ watq + "\'));");

					while (rs.next()) {

						try {
							Score ss=new Score();
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
		double dis=result.getScoreList().get(0).getFreq();
		

		if (dis > threshold) {
			result.getScoreList().get(0).setSimilarity(0);
			result.setFinalScore(0);
		} else {
			double res=1-(dis/threshold);
			result.getScoreList().get(0).setSimilarity(res);
			result.setFinalScore(res);
		}

		return result;

	}

}
