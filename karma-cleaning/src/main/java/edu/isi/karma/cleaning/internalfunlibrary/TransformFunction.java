package edu.isi.karma.cleaning.internalfunlibrary;

import java.util.Vector;

import edu.isi.karma.cleaning.TNode;

public interface TransformFunction {
	public boolean convertable(Vector<TNode> sour, Vector<TNode> dest);
	public String convert(Vector<TNode> sour);
	public int getId();
}
