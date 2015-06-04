package edu.isi.karma.cleaning.correctness;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import edu.isi.karma.cleaning.DataRecord;
import edu.isi.karma.cleaning.InterpreterType;
import edu.isi.karma.cleaning.grammartree.*;
import edu.isi.karma.cleaning.ProgramRule;
import edu.isi.karma.cleaning.UtilTools;

public class MultiviewInspector implements Inspector {
	public Program program;
	public ProgramRule progRule;
	public HashMap<String, ArrayList<String>> partitionRules = new HashMap<String, ArrayList<String>>();

	public MultiviewInspector(ProgramRule programRule) {
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

	public HashSet<String> genPosResultofProgram(DataRecord record, ProgramRule prog) {
		HashSet<String> ret = new HashSet<String>();
		String origin = record.origin;
		InterpreterType worker = prog.getWorkerForClass(prog.getClassForValue(origin));
		String transformed = worker.execute_debug(origin);
		ret = UtilTools.getPositions(transformed);
		return ret;

	}
	public boolean isSame(ArrayList<String> rules, String raw, HashSet<String> refer) {
		boolean same = true;
		for (String rule : rules) {
			ProgramRule pr = new ProgramRule(rule);
			String out = pr.transform(raw);
			if (!refer.contains(out)) {
				return false;
			}
		}
		return same;
	}

	public ArrayList<DataRecord> checkRecordCollection(ArrayList<DataRecord> records) {
		ArrayList<DataRecord> ret = new ArrayList<DataRecord>();
		for (DataRecord record : records) {
			
		}
		return ret;
	}

	public void programHashSetChecker1(Collection<String> c1, Collection<String> c2) {
		System.out.println("" + c1);
		System.out.println("" + c2);
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

	@Override
	public double getActionLabel(DataRecord record) {
		HashSet<String> refer = genPosResultofProgram(record, progRule);
		String label = (record.classLabel == DataRecord.unassigned ? this.getLabel(record.origin) : record.classLabel);
		if (partitionRules.containsKey(label)) {
			if (!isSame(partitionRules.get(label), record.origin, refer)) {
				return -1;
			}
		}
		return 1;
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

}
