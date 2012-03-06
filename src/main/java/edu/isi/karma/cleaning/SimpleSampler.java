package edu.isi.karma.cleaning;

import java.util.HashSet;
import java.util.Vector;

public class SimpleSampler {
	private RuleGenerator rg;
	public SimpleSampler()
	{
		rg = new RuleGenerator();
	}
	// store all the framework rule
	public void putFrameworkRule()
	{
		//
		HashSet<String> s = rg.printRules("rule",0);
		
	}
	// store the what
	public void storeruleFragment(Vector<TNode> example, int sPos, int ePos)
	{
		HashSet<String> s=rg.printRules("tokenspec",(ePos-sPos));
	}
	// refine the rules
	public String refineRule()
	{
		return "";
	}
	public String generate()
	{
		return "";
	}
	public static void main(String[] args)
	{
		
	}

}
