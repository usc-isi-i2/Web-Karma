package edu.isi.karma.er.compare;

import java.sql.Connection;

import edu.isi.karma.er.helper.entity.MultiScore;

public interface PointToPointComparator{

	public MultiScore getSimilarity(Connection connection, MultiScore result, double x1, double y1, double x2, double y2);
}
