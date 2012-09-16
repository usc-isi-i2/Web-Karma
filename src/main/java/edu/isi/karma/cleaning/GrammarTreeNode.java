package edu.isi.karma.cleaning;

public interface GrammarTreeNode {
	public String toProgram();
	public GrammarTreeNode mergewith(GrammarTreeNode a );
	public String getNodeType();
	public double getScore();
}
