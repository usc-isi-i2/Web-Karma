package edu.isi.karma.er.matcher;


import edu.isi.karma.er.helper.entity.SaamPerson;
import edu.isi.karma.er.helper.entity.Score;

public interface Matcher {

	public Score match(String p, SaamPerson v, SaamPerson w);
}
