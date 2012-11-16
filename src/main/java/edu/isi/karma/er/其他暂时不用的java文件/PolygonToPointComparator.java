package edu.isi.karma.er.compare;

import java.sql.Connection;

import edu.isi.karma.er.helper.entity.MultiScore;

public interface PolygonToPointComparator {
	
	public MultiScore getSimilarity(Connection connection, MultiScore result, String polygon, double x2, double y2);
}
