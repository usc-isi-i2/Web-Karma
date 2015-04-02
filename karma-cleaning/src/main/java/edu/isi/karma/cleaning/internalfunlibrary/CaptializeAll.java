package edu.isi.karma.cleaning.internalfunlibrary;

import java.util.Vector;

import edu.isi.karma.cleaning.TNode;
import edu.isi.karma.cleaning.UtilTools;

public class CaptializeAll implements TransformFunction {

	@Override
	public boolean convertable(Vector<TNode> sour, Vector<TNode> dest) {
		String target = UtilTools.print(dest);
		// if exact match succeed, return false;
		if(UtilTools.print(sour).compareTo(target) == 0)
			return false;
		String tran = this.convert(sour);
		if(target.compareTo(tran) == 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public String convert(Vector<TNode> sour) {
		String ret = "";
		try {
			for (TNode t : sour) {
				ret += Character.toUpperCase(t.text.charAt(0))
						+ t.text.substring(1).toLowerCase();
			}
			if(ret.compareTo(UtilTools.print(sour)) == 0)
			{
				return null;
			}
			return ret;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public int getId() {
		return InternalTransformationLibrary.Functions.Cap.getValue();
	}

}
