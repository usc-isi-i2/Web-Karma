package edu.isi.karma.cleaning.Correctness;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import edu.isi.karma.cleaning.GrammarTreeNode;
import edu.isi.karma.cleaning.Partition;
import edu.isi.karma.cleaning.ProgSynthesis;
import edu.isi.karma.cleaning.ProgramRule;
import edu.isi.karma.cleaning.Template;
import edu.isi.karma.cleaning.Traces;

public class ViewFunc implements VerificationFunc {
	private HashMap<String, String> data = new HashMap<String, String>();
	public ViewFunc(ArrayList<TransRecord> records, ProgSynthesis ps, ProgramRule pr)
	{
		Vector<Partition> pars = ps.myprog.partitions;
		HashMap<String, ArrayList<TransRecord>> resHashMap = cluster(records);
		for(Partition p: pars)
		{
			handlePartition(resHashMap.get(p.label), p.trace, pr.getStringRule(p.label));
		}
	}
	public HashMap<String, ArrayList<TransRecord>> cluster(ArrayList<TransRecord> reds)
	{
		HashMap<String, ArrayList<TransRecord>> res = new HashMap<String, ArrayList<TransRecord>>();
		for(TransRecord r: reds)
		{
			if(res.containsKey(r.label))
			{
				res.get(r.label).add(r);
			}
			else {
				ArrayList<TransRecord> line = new ArrayList<TransRecord>();
				line.add(r);
				res.put(r.label, line);
			}
		}
		return res;
	}
	public void handlePartition(ArrayList<TransRecord> records, Traces trace, String prog) {
		
		if (identifyIncorrRecord(records, prog)) {
			return;
		} else {
			identifyRecord(records, trace);
		}
	}

	// detect the records which prog failed on
	public boolean identifyIncorrRecord(ArrayList<TransRecord> records,
			String prog) {
		ProgramRule pr = new ProgramRule(prog);
		boolean res = true;
		for (TransRecord r : records) {
			String orgString = r.org;
			String tar = pr.transform(orgString);
			if (tar.compareTo(r.tar) != 0) {
				res = false;
				data.put(r.org, "1");
			}
		}
		return res;
	}

	public void identifyRecord(ArrayList<TransRecord> records, Traces trace) {
		ArrayList<Template> all = new ArrayList<Template>();
		Collection<Template> tlines = trace.traceline.values();
		all.addAll(tlines);
		ArrayList<Template> llines = new ArrayList<Template>();
		for (Integer k : trace.loopline.keySet()) {
			llines.addAll(trace.loopline.get(k).values());
		}
		all.addAll(llines);
		ArrayList<String> rdata = new ArrayList<String>();
		for (TransRecord tr : records) {
			rdata.add(tr.org);
		}
		int cnt = 0;
		HashMap<String, ArrayList<ArrayList<String>>> views = new HashMap<String, ArrayList<ArrayList<String>>>();
		for (Template t : all) {
			Vector<GrammarTreeNode> bd = t.body;
			ArrayList<ArrayList<String>> equviViews = new ArrayList<ArrayList<String>>();
			for (GrammarTreeNode gt : bd) {
				String rule = "";
				HashSet<String> vs = new HashSet<String>();
				ArrayList<String> lviews = new ArrayList<String>();
				do {
					rule = gt.toProgram();
					ProgramRule pr = new ProgramRule(rule);
					String ares = "";
					for (String s : rdata) {
						ares += pr.transform(s);
					}
					if (!vs.contains(ares)) {
						lviews.add(rule);
					}
				} while (rule.compareTo("null") != 0);
				equviViews.add(lviews);
			}
			cnt++;
			views.put(cnt+"", equviViews);
		}
		
		
	}

	public String verify(TransRecord record) {
		if (data.containsKey(record.org)) {
			return data.get(record.org);
		}
		return "0";
	}

}
