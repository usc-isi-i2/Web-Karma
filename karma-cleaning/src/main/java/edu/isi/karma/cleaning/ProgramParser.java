package edu.isi.karma.cleaning;

import java.util.ArrayList;

import edu.isi.karma.cleaning.ParseTreeNode.nodetype;

public class ProgramParser {
	public ParseTreeNode root = null;

	public ProgramParser() {

	}

	public ProgramParser(String program) {
		this.root = this.parse(program);

	}

	// parse the sub program with a sequence of segment
	public ArrayList<ParseTreeNode> parseNodeSeq(String nodeseq) {
		ArrayList<ParseTreeNode> res = new ArrayList<ParseTreeNode>();
		// find segments
		String[] tokens = nodeseq.split("\\+(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
		for (String tok : tokens) {
			// segment expression
			ParseTreeNode node = new ParseTreeNode(nodetype.segment, tok);
			res.add(node);
		}
		return res;
	}
	//parse the position program for each segment
	public void parseSegment(ParseTreeNode seg)
	{
		String tok = seg.value;
		// find the startposition
		int sposS = tok.indexOf("substr(value,", 0)+13;
		if (sposS <= 12) {
			return;
		}
		int sposE = 0;
		if(tok.charAt(sposS)<'9' && tok.charAt(sposS) > '0')
		{
			sposE = tok.indexOf(",",sposS)-1;
		}
		else
		{
			sposE = tok.indexOf("),",sposS+11);
		}
		
		String sposExpre = tok.substring(sposS, sposE + 1);
		// find the endPosition
		int eposS = sposE+2;
		int eposE = tok.length()-1;
		String eposExpre = tok.substring(eposS, eposE);
		ParseTreeNode sPosNode = new ParseTreeNode(nodetype.position,
				sposExpre);
		ParseTreeNode ePosNode = new ParseTreeNode(nodetype.position,
				eposExpre);
		seg.addChildren(sPosNode);
		seg.addChildren(ePosNode);
	}
	public ParseTreeNode parse(String prog) {
		ParseTreeNode root = new ParseTreeNode(nodetype.root, prog);
		// find segments
		String[] tokens = prog.split("\\+(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
		for (String tok : tokens) {
			// loop expression, possibily a sequence of loop segments
			if (tok.indexOf("loop(value,r") != -1) {
				String exp = tok.substring(13, tok.length() - 2);
				//create loop node
				ParseTreeNode pTreeNode = new ParseTreeNode(nodetype.loop, tok);
				root.addChildren(pTreeNode);
				ArrayList<ParseTreeNode> children = this.parseNodeSeq(exp);
				for(ParseTreeNode n:children)
				{
					pTreeNode.addChildren(n);
					this.parseSegment(n);
				}

			} else {
				// segment expression
				ParseTreeNode node = new ParseTreeNode(nodetype.segment, tok);
				root.addChildren(node);
				// find the startposition
				this.parseSegment(node);
			}
		}
		return root;
	}

}
