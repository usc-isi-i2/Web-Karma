package edu.isi.karma.cleaning;
import java.util.Vector;


public class Patcher {
	public Vector<GrammarTreeNode> patchSpace = new Vector<>();
	public Vector<GrammarTreeNode> groundTruth = new Vector<>();
	public Vector<ParseTreeNode> programNodes = new Vector<>();
	public Vector<ParseTreeNode> replaceNodes = new Vector<>();
}