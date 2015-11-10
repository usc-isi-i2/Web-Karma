package edu.isi.karma.cleaning;

import java.util.ArrayList;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.Token;

import edu.isi.karma.cleaning.grammartree.TNode;

public class PreprocessTokenizerWrapper {
	public ArrayList<TNode> tokenize(String Org){
		ArrayList<TNode> ret = new ArrayList<TNode>();
		CharStream cs = new ANTLRStringStream(Org);
		PreprocessingTokenizer tk = new PreprocessingTokenizer(cs);
		Token t;
		t = tk.nextToken();
		while (t.getType() != -1) {
			int mytype = -1;
			String txt = "";
			if (t.getType() == PreprocessingTokenizer.LWRD) {
				mytype = TNode.LWRDTYP;
				txt = t.getText();
			} else if (t.getType() == PreprocessingTokenizer.UWRD) {
				mytype = TNode.UWRDTYP;
				txt = t.getText();
			} else if (t.getType() == PreprocessingTokenizer.BLANK) {
				mytype = TNode.BNKTYP;
				txt = t.getText();
			} else if (t.getType() == PreprocessingTokenizer.NUMBER) {
				mytype = TNode.NUMTYP;
				txt = t.getText();
			} else if (t.getType() == PreprocessingTokenizer.SYBS) {
				// mytype = TNode.SYBSTYP;
				mytype = (int) t.getText().charAt(0);
				txt = t.getText();
			} else if (t.getType() == PreprocessingTokenizer.START) {
				mytype = TNode.STARTTYP;
				txt = "";
			} else if (t.getType() == PreprocessingTokenizer.END) {
				mytype = TNode.ENDTYP;
				txt = "";
			}
			else{
				
			}
			TNode tx = new TNode(mytype, txt);
			ret.add(tx);
			t = tk.nextToken();
		}
		return ret;
	}
	public static void main(String[] args){
		PreprocessTokenizerWrapper tkner = new PreprocessTokenizerWrapper();
		ArrayList<TNode> ret = tkner.tokenize("Hello");
		System.out.println(""+ret);
	}
}
