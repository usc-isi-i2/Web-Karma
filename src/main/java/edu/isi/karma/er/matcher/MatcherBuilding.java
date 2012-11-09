package edu.isi.karma.er.matcher;

import java.sql.Connection;

import edu.isi.karma.er.helper.entity.InputStruct;
import edu.isi.karma.er.helper.entity.MultiScore;

public interface MatcherBuilding {

	public MultiScore matchBuilding(Connection connection, MultiScore result,
			String p, String q, InputStruct v, InputStruct w);

}
