package edu.isi.karma.rep.cleaning;

public interface Transformation {
	public abstract String transform(String value);
	public abstract String getId();
}
