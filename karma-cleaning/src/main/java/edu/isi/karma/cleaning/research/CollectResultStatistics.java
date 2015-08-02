package edu.isi.karma.cleaning.research;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVReader;
import edu.isi.karma.cleaning.DataRecord;
import edu.isi.karma.cleaning.ProgramRule;
import edu.isi.karma.cleaning.correctness.AdaInspector;
import edu.isi.karma.cleaning.correctness.AdaInspectorTrainer;
class ErrorCnt{
	int firstErrorIndex = -1;
	int runtimeerror = 0;
	int totalerror = 0;
	int totalrecord = 0;
	int recommand = 0;
	int correctrecommand = 0;
	double reductionRate = -1.0;
	double precsion = 0.0;
}

public class CollectResultStatistics {
	// collect the incorrect but successfully transformed results.
	public int all_record_cnt = 0;
	public int correct_identified_record_cnt = 0;
	public int all_iter_cnt = 0;
	public int correct_all_iter_cnt = 0;
	public int all_scenario_cnt = 0;		
	public int correct_all_scenario_cnt = 0;
	public int questionable_iteration = 0;
	public int questionable_correct_iteration = 0;
	public int sum_first_incorrect_indexes = 0;
	public AdaInspector inspector = new AdaInspector();
	public String collectIncorrects(String fpath) throws IOException {
		// read a file
		File f = new File(fpath);
		String ret = "";
		CSVReader cr = new CSVReader(new FileReader(f), ',', '"', '\0');
		String[] pair;
		ArrayList<DataRecord> allrec = new ArrayList<DataRecord>();
		Vector<String[]> allrec_v2 = new Vector<String[]>();
		while ((pair = cr.readNext()) != null) {
			if (pair == null || pair.length <= 1)
				break;
			DataRecord tmp = new DataRecord();
			tmp.id = pair[0] + "";
			tmp.origin = pair[0];
			tmp.target = pair[1];
			allrec.add(tmp);
			allrec_v2.add(pair);
		}
		cr.close();
		assert (!allrec.isEmpty());
		Tools tool = new Tools();
		tool.init(allrec_v2);
		PriorityQueue<DataRecord> wrong = new PriorityQueue<DataRecord>();
		wrong.addAll(allrec);
		Vector<String[]> examples = new Vector<String[]>();
		ArrayList<String> exampleIDs = new ArrayList<String>();
		while (!wrong.isEmpty()) {
			DataRecord expRec = wrong.poll();
			String[] exp = this.generateExample(expRec);
			System.out.println("" + Arrays.toString(exp));
			examples.add(exp);
			exampleIDs.add(expRec.origin);
			tool.learnProgramRule(examples);
			//System.out.println("error cnt: " + wrong.size());
			assignClassLabel(allrec, exampleIDs, tool.getProgramRule());
			int runtimeErrorcnt = getFailedCnt(allrec);
			ErrorCnt ecnt = new ErrorCnt();
			ecnt.runtimeerror = runtimeErrorcnt;
			ecnt.totalrecord = allrec.size();
			all_iter_cnt++;
			wrong.clear();
			if(runtimeErrorcnt == 0){
				ArrayList<DataRecord> recmd = new ArrayList<DataRecord>();
				inspector.initeInspector(tool.dpp, tool.msger, allrec, exampleIDs, tool.progRule);
				for(DataRecord rec: allrec){
					//System.out.println(String.format("%s, %s", rec.origin, rec.transformed));
					double value = inspector.getActionScore(rec);
					rec.value = value;
					if(value <= 0){
						recmd.add(rec);
					}
					if(inspector.getActionLabel(rec) == (rec.target.compareTo(rec.transformed)==0 ? 1.0: -1.0)){
						correct_identified_record_cnt ++;
					}
					if(rec.transformed.compareTo(rec.target)!= 0 && !exampleIDs.contains(rec.origin)){
						wrong.add(rec);
					}
					all_record_cnt ++;
				}
				
				ArrayList<DataRecord> crecmd = getCorrectRecommand(recmd, wrong, ecnt);
				System.out.println(""+recmd.size());
				ecnt.recommand = recmd.size();
				ecnt.correctrecommand = crecmd.size();
				ecnt.precsion = ecnt.correctrecommand * 1.0 / ecnt.recommand;
				ecnt.reductionRate = ecnt.recommand *1.0/ecnt.totalrecord;
				if(ecnt.correctrecommand > 0 || wrong.size() == 0){
						correct_all_iter_cnt ++;
				}
				questionable_iteration ++;
				this.sum_first_incorrect_indexes += ecnt.firstErrorIndex;
			}
			else{
				for(DataRecord rec: allrec){
					rec.value = 0;
					if(rec.transformed.compareTo(rec.target)!= 0 && !exampleIDs.contains(rec.origin)){
						wrong.add(rec);
					}
				}
				ecnt.firstErrorIndex = 0;
				correct_all_iter_cnt ++;
				this.sum_first_incorrect_indexes += 0;
			}
			ecnt.totalerror = wrong.size();
			System.out.println(""+tool.progRule.toString());
			ret += printResult(ecnt)+"\n";
		}
		return ret;
	}
	public String[] generateExample(DataRecord rec) {
		String[] xStrings = { "<_START>" + rec.origin + "<_END>", rec.target };
		return xStrings;
	}

	public PriorityQueue<DataRecord> assignClassLabel(ArrayList<DataRecord> allrec,ArrayList<String> expIds, ProgramRule prog) {
		PriorityQueue<DataRecord> ret = new PriorityQueue<DataRecord>();
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

	public int getFailedCnt(ArrayList<DataRecord> wrong) {
		int cnt = 0;
		for (DataRecord s : wrong) {
			//Prober.CheckSpecificRecord(s);
			if (s.transformed.contains("_FATAL_ERROR_")) {
				cnt++;
			}
		}
		return cnt;
	}
	public ArrayList<DataRecord> getCorrectRecommand(ArrayList<DataRecord> recmd,PriorityQueue<DataRecord> wrong, ErrorCnt repo){
		HashSet<String> rawInputs = new HashSet<String>();
		for(DataRecord rec: wrong){
			if(!rawInputs.contains(rec.origin)){
				rawInputs.add(rec.origin);
			}
		}
		ArrayList<DataRecord> ret = new ArrayList<DataRecord>();
		for(int i = 0; i < recmd.size(); i++){
			DataRecord rec = recmd.get(i);
			if(rawInputs.contains(rec.origin)){
				if(repo.firstErrorIndex == -1){
					repo.firstErrorIndex = i;
				}
				ret.add(rec);
			}
		}
		return ret;
	}
	public String printResult(ErrorCnt ecnt) {
		String s = "";
		s += String.format("rt, %d, e,%d,t,%d, r,%d, cr, %d, first, %d, red, %f, pre, %f", ecnt.runtimeerror, ecnt.totalerror, ecnt.totalrecord, ecnt.recommand,ecnt.correctrecommand, ecnt.firstErrorIndex, ecnt.reductionRate, ecnt.precsion);
		/*for (String[] e : wrong) {
			s += String.format("%s, %s, %s ||", e[0], e[1], e[2]);
		}*/
		return s;
	}

	public double[] parameterSelection(double parameter){
		String dirpath = "/Users/bowu/Research/testdata/TestSingleFile/";
		File nf = new File(dirpath);
		File[] allfiles = nf.listFiles();
		AdaInspectorTrainer.questionablePreference = parameter;
		double[] ret = {0,0,0, 0};
		inspector = new AdaInspector();
		inspector.initeParameter();
		String line = "";
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					"/Users/bowu/Research/Feedback/result"+parameter+".txt")));
			for (File f : allfiles) {
				if (f.getName().indexOf(".csv")== -1 || ( f.getName().indexOf(".csv") != (f.getName().length() - 4))) {
					continue;
				}
				bw.write(f.getName()+"\n");
				line = collectIncorrects(f.getAbsolutePath())+"\n";
				System.out.println(""+line);
				bw.write(line);
			}
			
			System.out.println(String.format("%d, %d, %f", correct_identified_record_cnt, all_record_cnt, correct_identified_record_cnt*1.0/all_record_cnt));
			System.out.println(String.format("%d, %d", correct_all_iter_cnt, all_iter_cnt) +", Percentage of correct iteration: "+ (correct_all_iter_cnt * 1.0 / all_iter_cnt));
			ret[0] = correct_all_iter_cnt * 1.0 / all_iter_cnt;
			ret[1] = questionable_correct_iteration * 1.0 / questionable_iteration;
			ret[2] = correct_identified_record_cnt*1.0/all_record_cnt;
			ret[3] = sum_first_incorrect_indexes*1.0 / all_iter_cnt;
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	public static void main(String[] args) {
		String ret = "";
		for(double p = 3; p <= 3; p += 1){
			CollectResultStatistics collect = new CollectResultStatistics();
			double[] one = collect.parameterSelection(p);
			ret += String.format("%f, a, %f, q, %f, r, %f, avg_first %f", p, one[0], one[1], one[2], one[3])+"\n";
		}
		System.out.println(""+ret);
		//EmailNotification alert = new EmailNotification();
		//alert.notify(true, ret);
	}

}
