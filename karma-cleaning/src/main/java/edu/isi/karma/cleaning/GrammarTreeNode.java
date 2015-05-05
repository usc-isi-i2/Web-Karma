package edu.isi.karma.cleaning;

import java.util.ArrayList;

public interface GrammarTreeNode {
	public String toProgram();
	public GrammarTreeNode mergewith(GrammarTreeNode a);
	public String getNodeType();
	public double getScore();
	public String getrepString(); // return a string represent the type info of		// the grammar tree node
	public void createTotalOrderVector(); // use the predefined partial order to
	public void emptyState();
	public long size();
	public String getProgram(); // return the program generated from this node;
	public ArrayList<String> genAtomicPrograms(); // return a set of consistent basic program 
}
