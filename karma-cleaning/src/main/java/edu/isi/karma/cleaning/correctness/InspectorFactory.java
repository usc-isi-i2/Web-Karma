package edu.isi.karma.cleaning.correctness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.isi.karma.cleaning.DataPreProcessor;
import edu.isi.karma.cleaning.DataRecord;
import edu.isi.karma.cleaning.Messager;
import edu.isi.karma.cleaning.ProgramRule;

public class InspectorFactory {
	public HashMap<String, ArrayList<DataRecord>> examplegroups = new HashMap<String, ArrayList<DataRecord>>();
	public double[] weights;
	public ArrayList<DataRecord> all;
	public DataPreProcessor dpp;
	public ProgramRule program;
	public InspectorFactory(DataPreProcessor dpp, Messager msger, ArrayList<DataRecord> records, ArrayList<String> exampleIDs, ProgramRule program) {
		weights = msger.getWeights();
		all = records;
		this.dpp = dpp;
		this.program = program;
		initExampleGroups(records, exampleIDs);
		
	}
	public static List<String> getInspectorNames(){
		ArrayList<String> names = new ArrayList<String>();
		names.add(ClasscenterInspector.class.getName()+"|1.0");
		names.add(ClasscenterInspector.class.getName()+"|1.2");
		names.add(ClasscenterInspector.class.getName()+"|1.4");
		names.add(ClasscenterInspector.class.getName()+"|1.6");
		names.add(ClasscenterInspector.class.getName()+"|1.8");
		names.add(ClasscenterInspector.class.getName()+"|2.0");
		names.add(ClasscenterInspector.class.getName()+"|3.0");
		names.add(OutlierInspector.class.getName()+"|1.0");
		names.add(OutlierInspector.class.getName()+"|1.2");
		names.add(OutlierInspector.class.getName()+"|1.4");
		names.add(OutlierInspector.class.getName()+"|1.6");
		names.add(OutlierInspector.class.getName()+"|1.8");
		names.add(OutlierInspector.class.getName()+"|2.0");
		names.add(OutlierInspector.class.getName()+"|3.0");
		names.add(MembershipAmbiguityInspector.class.getName()+"|0.05");
		names.add(MembershipAmbiguityInspector.class.getName()+"|0.07");
		names.add(MembershipAmbiguityInspector.class.getName()+"|0.09");
		names.add(MembershipAmbiguityInspector.class.getName()+"|0.1");
		names.add(MembershipAmbiguityInspector.class.getName()+"|0.12");
		names.add(MultiviewInspector.class.getName());
		return names;
		
	}
	public List<Inspector> getAllInspector(){
		List<String> names = getInspectorNames();
		List<Inspector> inspectors = new ArrayList<Inspector>();
		for(String name: names){
			inspectors.add(this.getInspector(name));
		}
		return inspectors;
	}
	public void initExampleGroups(ArrayList<DataRecord> records, ArrayList<String> exampleIDs){
		for(DataRecord record: records){
			if(exampleIDs.contains(record.id)){
				String clabel = record.classLabel;
				if(examplegroups.containsKey(clabel)){
					examplegroups.get(clabel).add(record);
				}
				else{
					ArrayList<DataRecord> sets = new ArrayList<DataRecord>();
					sets.add(record);
					examplegroups.put(clabel, sets);
				}
			}			
		}
	}
	public Inspector getInspector(String name) {
		Inspector ret = null;
		double scale = 1.8;
		double ratio = 0.05;
		if (name.indexOf(ClasscenterInspector.class.getName()) != -1) {
			if(name.indexOf("|")!= -1)
				scale = Double.parseDouble(name.split("\\|")[1]);
			ret = new ClasscenterInspector(dpp, examplegroups,all, weights, scale);
			
		} else if (name.indexOf(OutlierInspector.class.getName()) != -1) {
			if(name.indexOf("|")!= -1)
				scale = Double.parseDouble(name.split("\\|")[1]);
			ret = new OutlierInspector(dpp, all, weights, scale);

		} else if (name.indexOf(MembershipAmbiguityInspector.class.getName()) != -1) {
			if(name.indexOf("|")!= -1)
				ratio = Double.parseDouble(name.split("\\|")[1]);
			ret = new MembershipAmbiguityInspector(dpp, examplegroups,ratio, weights);

		} else if (name.compareTo(MultiviewInspector.class.getName()) == 0) {
			ret = new MultiviewInspector(program);
		} else {

		}
		return ret;
	}
}
