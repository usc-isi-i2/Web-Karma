package edu.isi.karma.cleaning;

public interface GrammarTreeNode {
	public String toProgram();

	public GrammarTreeNode mergewith(GrammarTreeNode a);

	public String getNodeType();

	public double getScore();

	public String getrepString(); // return a string represent the type info of
									// the grammar tree node

	public void createTotalOrderVector(); // use the predefined partial order to
											// creat a total order vector

	public void emptyState();

	public long size();
}
