package edu.isi.karma.cleaning.internalfunlibrary;

import java.util.Vector;

import edu.isi.karma.cleaning.TNode;
import edu.isi.karma.cleaning.UtilTools;

public class UpperCaseAll implements TransformFunction {

	@Override
	public boolean convertable(Vector<TNode> sour, Vector<TNode> dest) {
		String ts = UtilTools.print(dest);
		if(UtilTools.print(sour).compareTo(ts) == 0)
			return false;
		String ss = this.convert(sour);
		if(ss.compareTo(ts) == 0)
		{
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public String convert(Vector<TNode> sour) {
		String ret = "";
		for(TNode t: sour){
			ret += t.text.toUpperCase();
		}
		if(ret.compareTo(UtilTools.print(sour)) == 0)
		{
			return null;
		}
		return ret;
	
	}

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return InternalTransformationLibrary.Functions.Uppercase.getValue();
	}


}
