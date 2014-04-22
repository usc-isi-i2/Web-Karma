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

//type
public class TNode {
	public int color = -1;
	public static final int MOVCOLOR = 0;
	public static final int DELCOLOR = 1;
	public static final int INSCOLOR = 2;
	public static final int ANYTYP = 0;
	public static final int NUMTYP = 1;
	public static final int SYBSTYP = 2;
	public static final int BNKTYP = 3;
	public static final int UWRDTYP = 4;
	public static final int STARTTYP = 5;
	public static final int ENDTYP = 6;
	public static final int LWRDTYP = 7;
	public static final int WORD = 8;
	public static final String ANYTOK = "ANYTOK";
	public static final int ANYNUM = Integer.MAX_VALUE;
	public int type;
	public String text;

	public TNode(int type, String text) {
		this.type = type;
		this.text = text;
	}

	public void setColor(int c) {
		this.color = c;
	}

	public int getColor() {
		return color;
	}

	public String toString() {
		return "\"" + text + "\"";
	}

	public String getType() {
		if (type == TNode.ANYTYP) {
			return "ANYTYP";
		} else if (type == TNode.WORD) {
			return "Word";
		} else if (type == TNode.NUMTYP) {
			return "Number";
		} else if (type == TNode.SYBSTYP) {
			return "Symbol";
		} else if (type == TNode.BNKTYP) {
			return "Blank";
		} else if (type == TNode.STARTTYP) {
			return "START";
		} else if (type == TNode.ENDTYP) {
			return "END";
		} else if (type == TNode.LWRDTYP) {
			return "LWD";
		} else if (type == TNode.UWRDTYP) {
			return "UWD";
		} else {
			return "" + (char) this.type;
		}
	}

	public boolean sameNode(TNode t) {
		if (sameText(t) && sameType(t)) {
			return true;
		}
		return false;
	}

	public boolean sameText(TNode t) {
		if (this.text.compareTo(TNode.ANYTOK) == 0
				|| t.text.compareTo(TNode.ANYTOK) == 0) {
			return true;
		}
		if (this.text.compareTo(t.text) == 0) {
			return true;
		}
		return false;
	}

	public boolean sameType(TNode t) {
		if (this.type == TNode.ANYTYP || t.type == TNode.ANYTYP) {
			return true;
		}
		if (this.type == t.type)
			return true;
		return false;
	}

	public int mergableType(TNode t) {

		boolean res = this.sameType(t);
		if (res)
			return this.type;
		if ((this.type == TNode.UWRDTYP || this.type == TNode.LWRDTYP || this.type == TNode.WORD)
				&& (t.type == TNode.LWRDTYP || t.type == TNode.UWRDTYP || t.type == TNode.WORD)) {
			return TNode.WORD;
		}
		return -1;
	}
}
