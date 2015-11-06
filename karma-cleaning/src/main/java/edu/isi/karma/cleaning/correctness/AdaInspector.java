package edu.isi.karma.cleaning.correctness;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import edu.isi.karma.cleaning.DataPreProcessor;
import edu.isi.karma.cleaning.DataRecord;
import edu.isi.karma.cleaning.Messager;
import edu.isi.karma.cleaning.ProgramRule;

public class AdaInspector implements Inspector {
	private ArrayList<Inspector> inspectors = new ArrayList<Inspector>();
	private List<String> inspectorNames = new ArrayList<String>();
	private List<Double> weights = new ArrayList<Double>();
	InspectorFactory factory;

	public AdaInspector() {

	}

	public void initeInspector(DataPreProcessor dpp, Messager msger, ArrayList<DataRecord> records, ArrayList<String> exampleIDs, ProgramRule program) {
		factory = new InspectorFactory(dpp, msger, records, exampleIDs, program);
		inspectors.clear();
		for (int i = 0; i < inspectorNames.size(); i++) {
			inspectors.add(factory.getInspector(inspectorNames.get(i)));
		}
	}
	public void initeParameter(){
		String value = "{\"edu.isi.karma.cleaning.correctness.OutlierInspector|1.0|2\":0.20450001920049324,\"edu.isi.karma.cleaning.correctness.OutlierInspector|1.6|2\":0.024386868116059113,\"edu.isi.karma.cleaning.correctness.MembershipAmbiguityInspector|0.07\":0.0025917344906929455,\"edu.isi.karma.cleaning.correctness.ClasscenterInspector|2.6|2\":0.007308103433307873,\"edu.isi.karma.cleaning.correctness.MultiviewInspector\":0.21458067957712698,\"edu.isi.karma.cleaning.correctness.ClasscenterInspector|2.8|1\":0.12985188307902093,\"edu.isi.karma.cleaning.correctness.ClasscenterInspector|2.8|2\":0.20038555374000705}";		
		//String value = "{\"edu.isi.karma.cleaning.correctness.ClasscenterInspector|0.0|3\":0.08225784904838043}";
		JSONObject parameters = new JSONObject(value);
		Iterator<String> keys = parameters.keys();
		while(keys.hasNext()){
			String name = keys.next();
			double weight = parameters.getDouble(name);
			inspectorNames.add(name);
			weights.add(weight);
		}
	}
	public void initeParameterWithTraining() {
		CreatingTrainingData cdata = new CreatingTrainingData();
		ArrayList<Instance> all = cdata.runDir();
		List<String> clfs = InspectorFactory.getInspectorNames();
		AdaInspectorTrainer adaTrainer = new AdaInspectorTrainer(all.toArray(new Instance[all.size()]), clfs);
		adaTrainer.adaboost(clfs.size());
		inspectorNames = adaTrainer.classifierList;
		weights = adaTrainer.alphaList;

	}

	@Override
	public double getActionLabel(DataRecord record) {
		double ret = 0.0;
		//String line = "";
		for (int i = 0; i < inspectors.size(); i++) {
			ret += inspectors.get(i).getActionLabel(record) * weights.get(i);
			//line += "|"+inspectors.get(i).getActionLabel(record) +", "+weights.get(i);
		}
		if (ret > 0) {
			return 1;
		} else {
			return -1;
		}
	}
	public double tuneResult(double ret, double tuneflag){
		double bigNegtive = -1.0 * 1e-10; 
		if(tuneflag < 0){
			if(ret >= 0){
				ret = bigNegtive; 
			}
		}
		return ret;
	}
	public double getActionScore(DataRecord record){
		double ret = 0.0;
		ArrayList<Double> alllabels = new ArrayList<Double>();
		for (int i = 0; i < inspectors.size(); i++) {
			alllabels.add(inspectors.get(i).getActionLabel(record) * weights.get(i));
		}
		for(double d: alllabels){
			ret += d;
		}
		return ret;
	}
	@Override
	public String getName() {
		return "AdaInspector";
	}
	public static void main(String[] args){
		AdaInspector aInspector = new AdaInspector();
		aInspector.initeParameterWithTraining();
	}

}
