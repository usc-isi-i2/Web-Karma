package edu.isi.karma.er.compare;

import java.sql.Connection;

import edu.isi.karma.er.helper.entity.MultiScore;

public interface PointToPolygonComparator {
	
	public MultiScore getSimilarity(Connection connection, MultiScore result, double x1, double y1, String polygon);
}
