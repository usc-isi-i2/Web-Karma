package edu.isi.karma.cleaning.internalfunlibrary;

import java.util.Vector;

import edu.isi.karma.cleaning.TNode;
import edu.isi.karma.cleaning.UtilTools;

public class ExactEqual implements TransformFunction {

	@Override
	public boolean convertable(Vector<TNode> sour, Vector<TNode> dest) {
		if (UtilTools.print(sour).compareTo(UtilTools.print(dest)) == 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String convert(Vector<TNode> sour) {
		return UtilTools.print(sour);
	}

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return InternalTransformationLibrary.Functions.Exact.getValue();
	}

}
