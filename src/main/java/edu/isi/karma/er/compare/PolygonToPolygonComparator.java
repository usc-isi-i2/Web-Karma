package edu.isi.karma.er.compare;

import java.sql.Connection;

import edu.isi.karma.er.helper.entity.MultiScore;

public interface PolygonToPolygonComparator {
	public MultiScore getSimilarity(Connection connection, MultiScore result, String polygonA, String polygonB);
	
}
