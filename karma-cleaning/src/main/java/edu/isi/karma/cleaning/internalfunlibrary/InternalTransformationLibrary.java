package edu.isi.karma.cleaning.internalfunlibrary;

import java.util.Collection;
import java.util.HashMap;

public class InternalTransformationLibrary {
	public static enum Functions {
		NonExist(-1), Cap(1), Exact(2), Uppercase(3), Lowercase(4), Firstletter(5);
		private final int id;

		Functions(int id) {
			this.id = id;
		}

		public int getValue() {
			return this.id;
		}
	}

	public HashMap<Integer, TransformFunction> funcs = new HashMap<Integer, TransformFunction>();
	public InternalTransformationLibrary() {
		// add all the functions
		ExactEqual equal = new ExactEqual();
		funcs.put(equal.getId(), equal);
		CaptializeAll cap = new CaptializeAll();
		funcs.put(cap.getId(), cap);
		FirstLettersOfWords fword = new FirstLettersOfWords();
		funcs.put(fword.getId(), fword);
		LowerCaseAll lca = new LowerCaseAll();
		funcs.put(lca.getId(), lca);
		UpperCaseAll uca = new UpperCaseAll();
		funcs.put(uca.getId(), uca);
	}
	public static String getName(int id){
		for(Functions val:Functions.values()){
			if(id == val.getValue()){
				return val.name();
			}
		}
		return "";
	}
	public Collection<Integer> getAllIDs() {
		return funcs.keySet();
	}

	public Collection<TransformFunction> getAllFuncs() {
		return funcs.values();
	}

	public TransformFunction getFunc(Integer Id) {
		if (funcs.containsKey(Id)) {
			return funcs.get(Id);
		} else {
			return null;
		}
	}

}
