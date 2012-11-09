package edu.isi.karma.cleaning;

import java.util.HashMap;


public class ProgramRule {
	public HashMap<String, InterpreterType> rules = new HashMap<String, InterpreterType>();
	public PartitionClassifierType pClassifier;
	public static Interpretor itInterpretor;
	public String signString = "";
	public ProgramRule(Program prog)
	{
		this.pClassifier = prog.classifier;
		initInterpretor();
	}
	public void initInterpretor()
	{
		if(itInterpretor == null)
			itInterpretor = new Interpretor();
	}
	public InterpreterType getRuleForValue(String value)
	{
		if(pClassifier != null)
		{
			String labelString = pClassifier.getLabel(value);
			InterpreterType rule = this.rules.get(labelString);
			return rule;
		}
		else {
			return rules.values().iterator().next();
		}
	}
	public void addRule(String partition, String rule)
	{
		InterpreterType worker = itInterpretor.create(rule);
		this.signString += rule;
		rules.put(partition, worker);
	}
	public String toString()
	{
		return rules.values().toString();
	}
	public static void main(String[] args)
	{
		
	}
}
