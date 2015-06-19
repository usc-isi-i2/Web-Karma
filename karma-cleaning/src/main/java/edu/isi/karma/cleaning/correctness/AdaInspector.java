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
		String value = "{\"edu.isi.karma.cleaning.correctness.ClasscenterInspector|1.0|2\":0.027693142998358185,\"edu.isi.karma.cleaning.correctness.ClasscenterInspector|2.8|3\":0.006409251098819078,\"edu.isi.karma.cleaning.correctness.OutlierInspector|1.4|2\":0.06780036942851692,\"edu.isi.karma.cleaning.correctness.MultiviewInspector\":0.09992930976206217,\"edu.isi.karma.cleaning.correctness.ClasscenterInspector|2.6|3\":0.09469385739867839,\"edu.isi.karma.cleaning.correctness.MembershipAmbiguityInspector|0.05\":0.020846077294063826}";
		
		JSONObject parameters = new JSONObject(value);
		Iterator<String> keys = parameters.keys();
		while(keys.hasNext()){
			String name = keys.next();
			double weight = parameters.getDouble(name);
			inspectorNames.add(name);
			weights.add(weight);
		}
		//inspectorNames.add(OutlierInspector.class.getName());
		/*inspectorNames.add(MultiviewInspector.class.getName());
		weights.add(0.087861029);
		inspectorNames.add(OutlierInspector.class.getName()+"|2.0");
		weights.add(0.012302152);
		inspectorNames.add(OutlierInspector.class.getName()+"|1.6");
		weights.add(0.035516576);		
		inspectorNames.add(OutlierInspector.class.getName()+"|1.4");
		weights.add(0.247238603);
		inspectorNames.add(ClasscenterInspector.class.getName()+"|3.0");
		weights.add(0.2608630532750992);
		inspectorNames.add(ClasscenterInspector.class.getName()+"|2.0");
		weights.add(0.095855231);
		inspectorNames.add(ClasscenterInspector.class.getName()+"|1.8");
		weights.add(0.055448516);
		inspectorNames.add(ClasscenterInspector.class.getName()+"|1.6");
		weights.add(0.016814621);
		inspectorNames.add(ClasscenterInspector.class.getCanonicalName()+"1.4");
		weights.add(0.00632224);
		inspectorNames.add(MembershipAmbiguityInspector.class.getName()+"|0.05");
		weights.add(0.089861187);
		inspectorNames.add(ClasscenterInspector.class.getName()+"|2.0");
		weights.add(0.137122140755571);*/
		
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
	public double getActionScore(DataRecord record){
		double ret = 0.0;
		//String line = "";
		ArrayList<Double> alllabels = new ArrayList<Double>();
		for (int i = 0; i < inspectors.size(); i++) {
			alllabels.add(inspectors.get(i).getActionLabel(record) * weights.get(i));
			//line += "|"+inspectors.get(i).getActionLabel(record) +", "+weights.get(i);
		}
		for(double d: alllabels){
			ret += d;
		}
		return ret;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "AdaInspector";
	}
	public static void main(String[] args){
		AdaInspector aInspector = new AdaInspector();
		aInspector.initeParameterWithTraining();
	}

}
