package edu.isi.karma.cleaning.correctness;

import java.util.ArrayList;
import java.util.List;

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
		//inspectorNames.add(OutlierInspector.class.getName());
		//inspectorNames.add(MultiviewInspector.class.getName());
		inspectorNames.add(OutlierInspector.class.getName()+"|1.2");
		weights.add(0.5666898451496911);
		inspectorNames.add(ClasscenterInspector.class.getName()+"|3.0");
		weights.add(0.408915677);
		inspectorNames.add(OutlierInspector.class.getName()+"|3.0");
		weights.add(0.185721764);
		inspectorNames.add(ClasscenterInspector.class.getName()+"|2.0");
		weights.add(0.113038286);
		inspectorNames.add(MultiviewInspector.class.getName());
		weights.add(0.087623594);
		inspectorNames.add(ClasscenterInspector.class.getName()+"|2.0");
		weights.add(0.061326947);
		inspectorNames.add(OutlierInspector.class.getName()+"|1.0");
		weights.add(0.064084607);
		inspectorNames.add(MembershipAmbiguityInspector.class.getName()+"|0.07");
		weights.add(0.030297008);
		inspectorNames.add(ClasscenterInspector.class.getName()+"|1.6");
		weights.add(0.029405159);
		inspectorNames.add(MembershipAmbiguityInspector.class.getName()+"|0.05");
		weights.add( 0.005144666429806189);		
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
		for (int i = 0; i < inspectors.size(); i++) {
			ret += inspectors.get(i).getActionLabel(record) * weights.get(i);
			//line += "|"+inspectors.get(i).getActionLabel(record) +", "+weights.get(i);
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
