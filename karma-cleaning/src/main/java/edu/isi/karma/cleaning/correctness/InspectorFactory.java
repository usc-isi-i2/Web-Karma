package edu.isi.karma.cleaning.correctness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hp.hpl.jena.sparql.function.library.namespace;

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
		ArrayList<Integer> targetContents = new ArrayList<Integer>();
		targetContents.add(InspectorUtil.input);
		targetContents.add(InspectorUtil.input_output);
		targetContents.add(InspectorUtil.output);
		//class center 
		String classCenterPrefix = ClasscenterInspector.class.getName();
		for(double scale = 1.0; scale <= 3.0; scale += 0.2){
			for(int tar: targetContents){
				String ins = String.format("%s|%.1f|%d", classCenterPrefix,scale,tar);
				names.add(ins);
			}
		}
		
		String outlierPrefix = OutlierInspector.class.getName();
		for(double scale = 1.0; scale <= 3.0; scale += 0.2){
			for(int tar: targetContents){
				String ins = String.format("%s|%.1f|%d", outlierPrefix,scale,tar);
				names.add(ins);
			}
		}
		/*names.add(ClasscenterInspector.class.getName()+"|1.0");
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
		names.add(OutlierInspector.class.getName()+"|3.0");*/
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
		//double scale = 1.8;
		//double ratio = 0.05;
		if (name.indexOf(ClasscenterInspector.class.getName()) != -1) {
			String[] parameters = name.split("\\|");
			double scale =  Double.parseDouble(parameters[1]);
			int targetContent = Integer.parseInt(parameters[2]); 
			ret = new ClasscenterInspector(dpp, examplegroups,all, weights, scale, targetContent);
			
		} else if (name.indexOf(OutlierInspector.class.getName()) != -1) {
			String[] parameters = name.split("\\|");
			double scale =  Double.parseDouble(parameters[1]);
			int targetContent = Integer.parseInt(parameters[2]); 
			ret = new OutlierInspector(dpp, all, weights, scale, targetContent);

		} else if (name.indexOf(MembershipAmbiguityInspector.class.getName()) != -1) {
			double ratio = 0.05;
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
