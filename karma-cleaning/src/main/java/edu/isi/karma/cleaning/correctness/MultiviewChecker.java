package edu.isi.karma.cleaning.correctness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import edu.isi.karma.cleaning.DataPreProcessor;
import edu.isi.karma.cleaning.DataRecord;
import edu.isi.karma.cleaning.InterpreterType;
import edu.isi.karma.cleaning.Messager;
import edu.isi.karma.cleaning.grammartree.*;
import edu.isi.karma.cleaning.ProgSynthesis;
import edu.isi.karma.cleaning.ProgramRule;
import edu.isi.karma.cleaning.UtilTools;

public class MultiviewChecker {
	public Program program;
	public ProgramRule progRule;
	public HashMap<String, ArrayList<String>> partitionRules = new HashMap<String, ArrayList<String>>();


	public MultiviewChecker(ProgramRule programRule) {
		progRule = programRule; 
		initPartitionRules(programRule.program);
	}

	public void initPartitionRules(Program program) {
		this.program = program;
		for (Partition par : program.partitions) {
			String parlabel = par.label;
			ArrayList<String> rules = par.genAtomicPrograms();
			rules = removeDuplict(rules);
			partitionRules.put(parlabel, rules);
		}
	}

	public ArrayList<String> removeDuplict(ArrayList<String> rules) {
		ArrayList<String> ret = new ArrayList<String>();
		HashSet<String> duplictChecker = new HashSet<String>();
		for (String rule : rules) {
			if (!duplictChecker.contains(rule)) {
				ret.add(rule);
				duplictChecker.add(rule);
			}
		}
		return ret;
	}
	
	public HashSet<String> genPosResultofProgram(DataRecord record, ProgramRule prog){
		HashSet<String> ret = new HashSet<String>();
		String origin = record.origin ;
		InterpreterType worker = prog.getWorkerForClass(prog.getClassForValue(origin));
		String transformed = worker.execute_debug(origin);
		ret = UtilTools.getPositions(transformed);
		return ret;
		
	}
	public boolean isSame(ArrayList<String> rules, String raw, HashSet<String> refer){
		boolean same = true;
		for (String rule : rules) {
			ProgramRule pr = new ProgramRule(rule);
			String out = pr.transform(raw);
			if(!refer.contains(out)){
				return false;
			}
		}
		return same;
	}
	public ArrayList<DataRecord> checkRecordCollection(
			ArrayList<DataRecord> records) {
		ArrayList<DataRecord> ret = new ArrayList<DataRecord>();
		for (DataRecord record : records) {
			HashSet<String> refer = genPosResultofProgram(record, progRule);
			String label = (record.classLabel == DataRecord.unassigned ? this
					.getLabel(record.origin) : record.classLabel);
			if (partitionRules.containsKey(label)) {
				if(!isSame(
						partitionRules.get(label), record.origin, refer)){				
					ret.add(record);
				}
			}
		}
		return ret;
	}

	public void programHashSetChecker1(Collection<String> c1, Collection<String> c2){
		System.out.println(""+c1);
		System.out.println(""+c2);
	}
	public String getLabel(String value) {
		if (value.isEmpty())
			return ProgramRule.defaultclasslabel;
		if (program.classifier == null) {
			return ProgramRule.defaultclasslabel;
		} else {
			return program.classifier.getLabel(value);
		}
	}

	public static void main(String[] args) {
		String[][] examples = {
				{ "avage 2099#24 (November, 1994)", "November, 1994" },
				{ "Punisher 2099 AD #28 (May, 1995)", "May, 1995" },
				{ "TAKEN 2007#22 (November, 1995)", "November, 1995" },
				{ "Spider-Man I#375 (March, 1993);Venom Sinner Takes All#2 (September, 1995)", "March, 1993" } };
		String[] oline = { "<_START>" + examples[0][0] + "<_END>",
				examples[0][1] };
		String[] oline1 = { "<_START>" + examples[1][0] + "<_END>",
				examples[1][1] };
		Vector<String[]> trainData = new Vector<String[]>();
		trainData.add(oline);
		trainData.add(oline1);
		ProgSynthesis progSyn = new ProgSynthesis();
		Vector<String> allraw = new Vector<String>();
		for (String[] exp : examples) {
			allraw.add(exp[0]);
		}

		ArrayList<DataRecord> records = new ArrayList<DataRecord>();
		for (String[] exp : examples) {
			DataRecord drec = new DataRecord();
			drec.id = Arrays.toString(exp);
			drec.origin = exp[0];
			drec.target = exp[1];
			records.add(drec);
		}
		DataPreProcessor dpp = new DataPreProcessor(allraw);
		dpp.run();
		Messager msger = new Messager();
		progSyn.inite(trainData, dpp, msger);
		ProgramRule prog = progSyn.run_main().iterator().next();
		MultiviewChecker mchecker = new MultiviewChecker(prog);
		
		ArrayList<DataRecord> myret = mchecker.checkRecordCollection(records);
		System.out.println("" + myret.toString());
	}
}
