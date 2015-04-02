/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.cleaning;

import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.Vector;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.Token;

public class Ruler {
	String Org = "";
	String Trgt = "";
	StringTokenizer st = null;
	String[] seperator = { " ", "," };
	public Vector<TNode> vec;
	int curPos = 0;
	Vector<Object[]> operators = new Vector<Object[]>();
	Vector<Integer> positions;
	Vector<TNode> whats;
	Vector<Integer> consPos;

	public Ruler() {
		positions = new Vector<Integer>();
		consPos = new Vector<Integer>();
		whats = new Vector<TNode>();
	}

	public Ruler(String x) {
		positions = new Vector<Integer>();
		consPos = new Vector<Integer>();
		whats = new Vector<TNode>();
		this.initConstantPosition();
		vec = new Vector<TNode>();
		Org = x;
		tokenize();
	}

	public void initConstantPosition() {
		String target = ",";
		for (int i = 0; i < vec.size(); i++) {
			if (vec.get(i).text.compareTo(target) == 0) {
				this.consPos.add(i);
			}
		}

	}

	public void setNewInput(String x) {
		this.Org = x;
		this.Trgt = "";
		this.vec = new Vector<TNode>();
		this.curPos = 0;
		this.tokenize();
		this.initConstantPosition();
	}

	public void setNewInput(Vector<TNode> x) {
		this.Org = x.toString();
		this.Trgt = "";
		this.vec = new Vector<TNode>();
		this.curPos = 0;
		this.vec = x;
		this.initConstantPosition();
	}

	// in current data,search the position of the tvec
	public static int Search(Vector<TNode> xvec, Vector<TNode> tvec, int bpos) {
		boolean isFind = false;
		int p1 = -1;
		for (int t = bpos; t < xvec.size() - tvec.size() + 1; t++) {
			p1 = t;
			for (int x = 0; x < tvec.size(); x++) {
				int p2 = x;
				if (xvec.get(p1).sameNode(tvec.get(p2))) {
					p1++;
				} else {
					isFind = false;
					break;
				}
				isFind = true;
			}
			if (isFind) {
				return t;
			}
		}
		return -1;
	}

	// evalPos()
	public int evalPos(String input, Vector<TNode> t, String option) {
		boolean incld = false;
		if (input.contains("first")) {
			if (!input.contains("incld")) {
				incld = false;
			} else {
				incld = true;
			}
			// int pos1 = this.Search(this.vec,t,0);
			if (option.compareTo("from_beginning") == 0) {
				int pos = Ruler.Search(vec, t, 0);
				if (pos == -1)
					return -1;
				if (incld) {
					return pos;
				} else {
					if (pos < vec.size())
						if (pos > 0) {
							return pos - 1;
						} else {
							return 0;
						}

					else
						return vec.size() - 1;
				}

			} else {
				@SuppressWarnings("unchecked")
				Vector<TNode> tmpvec = (Vector<TNode>) this.vec.clone();
				Collections.reverse(tmpvec);
				int pos = Ruler.Search(tmpvec, t, 0);
				if (pos == -1)
					return -1;
				if (incld) {
					if (this.vec.size() - pos - 1 >= 0
							&& this.vec.size() - pos - 1 <= vec.size())
						return this.vec.size() - pos - 1;
					else
						return 0;
				} else {
					if (this.vec.size() - pos >= 0
							&& this.vec.size() - pos <= vec.size())
						return this.vec.size() - pos;
					else
						return 0;
				}
			}
		}
		/* LSA to do */
		else {
			if (option.compareTo("from_beginning") == 0) {
				return Integer.parseInt(input) - 1;
			} else {
				return this.vec.size() - Integer.parseInt(input);
			}
		}
	}

	public void addOperators(Object[][] opers) {
		for (int j = 0; j < opers.length; j++) {
			operators.add(opers[j]);
		}
	}

	// seperate by , and " "
	//
	public void tokenize() {
		CharStream cs = new ANTLRStringStream(Org);
		Tokenizer tk = new Tokenizer(cs);
		Token t;
		t = tk.nextToken();
		while (t.getType() != -1) {
			int mytype = -1;
			String txt = "";
			if (t.getType() == Tokenizer.LWRD) {
				mytype = TNode.LWRDTYP;
				txt = t.getText();
			} else if (t.getType() == Tokenizer.UWRD) {
				mytype = TNode.UWRDTYP;
				txt = t.getText();
			} else if (t.getType() == Tokenizer.BLANK) {
				mytype = TNode.BNKTYP;
				txt = t.getText();
			} else if (t.getType() == Tokenizer.NUMBER) {
				mytype = TNode.NUMTYP;
				txt = t.getText();
			} else if (t.getType() == Tokenizer.SYBS) {
				// mytype = TNode.SYBSTYP;
				mytype = (int) t.getText().charAt(0);
				txt = t.getText();
			} else if (t.getType() == Tokenizer.START) {
				mytype = TNode.STARTTYP;
				txt = "";
			} else if (t.getType() == Tokenizer.END) {
				mytype = TNode.ENDTYP;
				txt = "";
			}
			else if(t.getType() == Tokenizer.WRD){
				mytype = TNode.WORD;
				txt = t.getText();
			}
			TNode tx = new TNode(mytype, txt);
			vec.add(tx);
			t = tk.nextToken();
		}
	}

	public static void main(String[] args) {
	}

	// move a position complied with condition
	// move to n -1
	// move up to tok -2
	public void move(int n, TNode tok, int opt) {
		if (opt == -1) {
			this.curPos = n;
		} else if (opt == -2) {
			Iterator<TNode> iter = vec.iterator();
			while (iter.hasNext()) {
				if (iter.next().sameText(tok)) {
					this.curPos++;
				}
			}
		}
	}

	public String print() {
		String res = "";
		for (int i = 0; i < vec.size(); i++) {
			String type = "";
			if (vec.get(i).type == TNode.LWRDTYP)
				type = "LWRD";
			else if (vec.get(i).type == TNode.UWRDTYP)
				type = "UWRD";
			else if (vec.get(i).type == TNode.SYBSTYP)
				type = "SYB";
			else if (vec.get(i).type == TNode.NUMTYP)
				type = "NUM";
			else if (vec.get(i).type == TNode.BNKTYP)
				type = "BNK";
			res += vec.get(i).text + "<" + type + ">";
		}
		return res;
	}

	public String toString() {
		String res = "";
		for (int i = 0; i < vec.size(); i++) {
			res += vec.get(i).text;
		}
		return res;
	}

	public void doOperation(String oper, String num, Vector<TNode> x, int spos,
			int epos) {
		int quan = 0;
		if (num == null || num.compareTo("anynumber") == 0) {
			quan = Integer.MAX_VALUE;
		} else {
			quan = Integer.parseInt(num);
		}
		if (oper.compareTo("del") == 0) {
			if (spos < 0) {
				return;// not applicable
			}
			if (epos < 0) {
				return; // not applicable
			}
			this.det(quan, x, spos, epos);
		}
		if (oper.compareTo("mov") == 0) {
			if (spos > epos) {
				this.vec = null;
				return;
			}
			this.mov(x, Integer.parseInt(num), spos, epos);
		}
		if (oper.compareTo("ins") == 0) {
			this.ins(x, spos);
		}
	}

	public void collectPoss(int x) {
		// the consPos show be sorted from small to high
		for (int i = 0; i < this.consPos.size(); i++) {
			if (x < consPos.get(i)) {
				this.positions.add(i);
			}
		}
		this.positions.add(consPos.size());
	}

	// toks is the token sequence that needed to be inserted into original token
	// sequence
	// dpos is the position start of the insertion
	public void ins(Vector<TNode> toks, int dpos) {
		if (dpos < vec.size()) {
			vec.addAll(dpos, toks);
		} else {
			vec.addAll(toks);
		}
	}

	// dpos is the destination position
	// toks specify the tokens need to be moved
	// spos is the start position of the segment
	// epos is the end position of the segment
	public void mov(Vector<TNode> toks, int dpos, int spos, int epos) {
		int pos = 0;
		int size = 0;
		if (toks != null) {
			pos = Ruler.Search(this.vec, toks, spos);
			if (pos + toks.size() > epos + 1 || pos == -1) {
				return;
			}
			size = toks.size();
		} else {
			pos = spos;
			if (epos == vec.size()) {
				size = vec.size() - spos;
			} else {
				size = epos - spos + 1;
			}
		}
		// update the end position and do the del
		ListIterator<TNode> l = this.vec.listIterator(pos);
		// ListIterator<TNode> dl = this.vec.listIterator(dpos);
		int c = 0;
		Vector<TNode> x = new Vector<TNode>();
		for (c = 0; c < size; c++) {
			// this.collectPoss(pos);
			TNode tn = l.next();
			tn.setColor(TNode.MOVCOLOR); // set color for moving.
			x.add(tn);
			// this.whats.add(tn);
			l.remove();
		}
		if (dpos <= spos) {
			if (dpos == vec.size()) {
				this.vec.addAll(x);
				return;
			}
			this.vec.addAll(dpos, x);

		}
		if (dpos >= epos) {
			dpos = dpos - size;
			if (dpos == vec.size()) {
				this.vec.addAll(x);
				return;
			}
			this.vec.addAll(dpos, x);
		}
	}

	public void det(int n, Vector<TNode> toks, int start, int end) {
		int cnt = 0;
		int pos = 0;
		int deleng = 0;
		while (cnt < n) {
			if (toks == null) // don't specify all particular token sequence
			{
				pos = start;
				n = 0;
				deleng = end - start + 1;
			} else {
				pos = Ruler.Search(this.vec, toks, start);
				deleng = toks.size();
			}
			if (pos + deleng > end + 1 || pos == -1) {
				break;
			}
			// update the end position and do the del
			ListIterator<TNode> l = this.vec.listIterator(pos);
			int c = 0;
			for (c = 0; c < deleng; c++) {
				// this.collectPoss(pos);
				TNode tn = l.next();
				this.whats.add(tn);
				l.remove();
				pos++;

			}
			end = end - deleng;
			cnt++;
		}
	}
}
