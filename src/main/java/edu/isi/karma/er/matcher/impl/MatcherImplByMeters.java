package edu.isi.karma.er.matcher.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edu.isi.karma.er.compare.impl.PointToPointComparatorImpl;
import edu.isi.karma.er.compare.impl.PointToPolygonComparatorImpl;
import edu.isi.karma.er.compare.impl.PolygonToPolygonComparatorImpl;
import edu.isi.karma.er.helper.entity.InputStruct;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.matcher.MatcherBuilding;

public class MatcherImplByMeters implements MatcherBuilding {
	public MultiScore matchBuilding(Connection connection, MultiScore result,
			String p, String q, InputStruct v, InputStruct w) {
		String watp = null;
		String watq = null;
		double x1 = 0.0;
		double y1 = 0.0;
		double x2 = 0.0;
		double y2 = 0.0;
		Statement stmt = null;
		ResultSet rs = null;
		PointToPointComparatorImpl point2point = new PointToPointComparatorImpl();
		PolygonToPolygonComparatorImpl polygon2polygon = new PolygonToPolygonComparatorImpl();
		PointToPolygonComparatorImpl point2polygon = new PointToPolygonComparatorImpl();
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
				point2point.getSimilarity(connection, result, x1, y1, x2, y2);
			} else if (p.equals("POLYGON")) {// POLYGON VS POINT；
				point2polygon.getSimilarity(connection, result, x2, y2, watp);
			}

		} else if (q.equals("POLYGON")) {
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

			if (p.equals("POINT")) {// POINT VS POLYGON ；
				point2polygon.getSimilarity(connection, result, x1, y1, watq);
			} else if (p.equals("POLYGON")) {// // POLYGON VS POLYGON ；
				polygon2polygon.getSimilarity(connection, result, watp, watq);
			}
		}
		return result;

	}

}
