package edu.isi.karma.cleaning;

import java.util.HashMap;

public class ProgramRule {
	public HashMap<String, InterpreterType> rules = new HashMap<String, InterpreterType>();
	public HashMap<String, String> strRules = new HashMap<String, String>();
	public PartitionClassifierType pClassifier;
	public static Interpretor itInterpretor;
	public String signString = "";
	public static final String IDENTITY = "substr(value,'START','END')";
	public boolean nullRule = false;

	public ProgramRule(Program prog) {
		this.pClassifier = prog.classifier;
		initInterpretor();
	}

	public ProgramRule(String rule) {
		initInterpretor();
		InterpreterType worker = itInterpretor.create(rule);
		rules.put("attr_0", worker);

	}

	public String transform(String value) {
		InterpreterType worker= this.getRuleForValue(value);
		String s2 = worker.execute(value);
		return s2;
	}

	public void initInterpretor() {
		if (itInterpretor == null)
			itInterpretor = new Interpretor();
	}

	public InterpreterType getRuleForValue(String value) {
		String c = getClassForValue(value);

		return getWorkerForClass(c);
	}

	public String getClassForValue(String value) {
		String labelString = "attr_0";
		if (value.length() == 0)
			return labelString;
		if (pClassifier != null) {
			labelString = pClassifier.getLabel(value);
		}
		return labelString;

	}

	public InterpreterType getWorkerForClass(String category) {
		if (category != "") {
			InterpreterType rule = this.rules.get(category);
			return rule;
		} else {
			return rules.values().iterator().next();
		}
	}

	public void updateClassworker(String category, String newRule) {
		this.rules.remove(category);
		this.strRules.remove(category);
		addRule(category, newRule);

	}

	public void addRule(String partition, String rule) {
		InterpreterType worker = itInterpretor.create(rule);
		this.signString += rule;
		rules.put(partition, worker);
		strRules.put(partition, rule);
	}

	public String getStringRule(String par) {
		return this.strRules.get(par);
	}

	public String toString() {
		String res = "";
		for (String key : strRules.keySet()) {
			res += String.format("%s:%s\n", key, strRules.get(key));
		}
		return res;
	}

	public static void main(String[] args) {

	}
}
