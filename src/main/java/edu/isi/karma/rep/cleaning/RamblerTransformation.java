package edu.isi.karma.rep.cleaning;

import java.util.Vector;

import edu.isi.karma.cleaning.RuleUtil;


public class RamblerTransformation implements Transformation {

	private Vector<String> rules = new Vector<String>();
	public String signature = "";
	public RamblerTransformation(Vector<String> rules)
	{
		this.setTransformationRules(rules);
	}
	public void setTransformationRules(Vector<String> rules)
	{
		this.rules = rules;
		for(int i = 0; i< rules.size(); i++)
		{
			signature += rules.get(i);
		}
	}
	@Override
	public String transform(String value) {
		if(this.rules.size() == 0)
		{
			return value; // if no rule exists, return the original string
		}
		String s = RuleUtil.applyRule(this.rules, value);
		return s;
	}
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return this.signature;
	}
	public int hashCode()
	{
		return this.signature.hashCode();
	}
	public boolean equals(Object other) 
	{
		RamblerTransformation e = (RamblerTransformation)other;
		if(e.signature.compareTo(this.signature)==0)
		{
			return true;
		}
		return false;
	}

}
