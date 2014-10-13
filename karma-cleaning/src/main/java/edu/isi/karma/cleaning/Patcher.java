package edu.isi.karma.cleaning;
import java.util.Vector;


public class Patcher {
	public Vector<GrammarTreeNode> patchSpace = new Vector<GrammarTreeNode>();
	public Vector<GrammarTreeNode> groundTruth = new Vector<GrammarTreeNode>();
	public Vector<ParseTreeNode> programNodes = new Vector<ParseTreeNode>();
	public Vector<ParseTreeNode> replaceNodes = new Vector<ParseTreeNode>();
}