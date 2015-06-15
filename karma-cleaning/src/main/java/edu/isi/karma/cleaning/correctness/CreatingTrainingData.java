package edu.isi.karma.cleaning.correctness;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVReader;
import edu.isi.karma.cleaning.DataRecord;
import edu.isi.karma.cleaning.ProgramRule;
import edu.isi.karma.cleaning.research.Tools;

class ErrorCnt {
	int runtimeerror = 0;
	int totalerror = 0;
	int totalrecord = 0;
	int recommand = 0;
	int correctrecommand = 0;
	double reductionRate = -1.0;
	double precsion = 0.0;
}

public class CreatingTrainingData {
	public int all_iter_cnt = 0;
	public int correct_all_iter_cnt = 0;
	public int all_scenario_cnt = 0;
	public int correct_all_scenario_cnt = 0;
	public int instanceCnt = 0;
	public List<Instance> createDataforOneFile(String fpath) throws IOException {
		// read a file
		File f = new File(fpath);
		List<Instance> ret = new ArrayList<Instance>();
		CSVReader cr = new CSVReader(new FileReader(f), ',', '"', '\0');
		String[] pair;
		ArrayList<DataRecord> allrec = new ArrayList<DataRecord>();
		Vector<String[]> allrec_v2 = new Vector<String[]>();
		int seqno = 0;
		while ((pair = cr.readNext()) != null) {
			if (pair == null || pair.length <= 1)
				break;
			DataRecord tmp = new DataRecord();
			tmp.id = seqno + "";
			tmp.origin = pair[0];
			tmp.target = pair[1];
			allrec.add(tmp);
			allrec_v2.add(pair);
			seqno++;
		}
		assert (!allrec.isEmpty());
		Tools tool = new Tools();
		tool.init(allrec_v2);
		Vector<DataRecord> wrong = new Vector<DataRecord>();
		wrong.add(allrec.get(0));
		Vector<String[]> examples = new Vector<String[]>();
		ArrayList<String> exampleIDs = new ArrayList<String>();
		while (!wrong.isEmpty()) {
			String[] exp = this.generateExample(wrong.get(0));
			System.out.println("" + Arrays.toString(exp));
			examples.add(exp);
			exampleIDs.add(wrong.get(0).id);
			tool.learnProgramRule(examples);
			System.out.println("error cnt: " + wrong.size());
			wrong = assignClassLabel(allrec, exampleIDs, tool.getProgramRule());
			int runtimeErrorcnt = getFailedCnt(wrong);
			ErrorCnt ecnt = new ErrorCnt();
			ecnt.runtimeerror = runtimeErrorcnt;
			ecnt.totalerror = wrong.size();
			ecnt.totalrecord = allrec.size();
			all_iter_cnt++;
			if (runtimeErrorcnt == 0 && wrong.size() > 0) {
				InspectorFactory factory = new InspectorFactory(tool.dpp, tool.msger, allrec, exampleIDs, tool.getProgramRule());
				List<Inspector> inspectors = factory.getAllInspector();
				for(DataRecord rec: allrec){
					Instance x = new Instance();
					x.label = (double)(rec.target.compareTo(rec.transformed) == 0 ? 1 : -1);
					for(Inspector p: inspectors){
						x.labeles.put(p.getName(), p.getActionLabel(rec));
					}
					x.ID = instanceCnt+"";
					instanceCnt ++;
					ret.add(x);
				}
				
			} else {
			}
		}
		return ret;
	}
	
	public String[] generateExample(DataRecord rec) {
		String[] xStrings = { "<_START>" + rec.origin + "<_END>", rec.target };
		return xStrings;
	}

	public Vector<DataRecord> assignClassLabel(ArrayList<DataRecord> allrec, ArrayList<String> expIds, ProgramRule prog) {
		Vector<DataRecord> ret = new Vector<DataRecord>();
		if (prog.pClassifier != null) {
			for (DataRecord record : allrec) {
				record.classLabel = prog.pClassifier.getLabel(record.origin);
			}
		}
		for(DataRecord rec: allrec){
			rec.transformed = prog.transform(rec.origin);
			if(rec.transformed.compareTo(rec.target)!= 0 && !expIds.contains(rec.id)){
				ret.add(rec);
			}
		}
		return ret;

	}

	public int getFailedCnt(Vector<DataRecord> wrong) {
		int cnt = 0;
		for (DataRecord s : wrong) {
			if (s.transformed.contains("_FATAL_ERROR_")) {
				cnt++;
			}
		}
		return cnt;
	}

	public String printTrainingData(List<Instance> values) {
		Collection<String> clfs = values.get(0).labeles.keySet();
		ArrayList<String> names = new ArrayList<String>();
		names.addAll(clfs);
		String ret = "";
		ret += names.toString() + ", label\n";
		for (Instance x : values) {
			String line = "";
			for (String n : names) {
				line += x.labeles.get(n) + ",";
			}
			line = line.substring(0, line.length() - 1);
			line += ", "+x.label;
			line += "\n";
			ret += line;
		}
		return ret;
	}

	public ArrayList<Instance> runDir() {
		String dirpath = "/Users/bowu/Research/testdata/TestSingleFile/";
		File nf = new File(dirpath);
		File[] allfiles = nf.listFiles();
		ArrayList<Instance> alldata = new ArrayList<Instance>();
		try {
			for (File f : allfiles) {
				if (f.getName().indexOf(".csv") != (f.getName().length() - 4)) {
					continue;
				}
				List<Instance> instances = this.createDataforOneFile(f.getAbsolutePath());
				alldata.addAll(instances);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return alldata;
	}
	public static void main(String[] args){
		CreatingTrainingData cdata = new CreatingTrainingData();
		System.out.println(cdata.printTrainingData(cdata.runDir()));
	}
}
