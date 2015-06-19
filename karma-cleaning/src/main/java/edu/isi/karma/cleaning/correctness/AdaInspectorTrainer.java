package edu.isi.karma.cleaning.correctness;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONObject;

public class AdaInspectorTrainer {
	Instance[] instances;
	List<String> classifierList = null;	//各个弱分类器
	List<Double> alphaList = null;			//每个弱分类器的权重
	List<String> remainClassifiers;;
	public static double questionablePreference = 5;
	
	public AdaInspectorTrainer(Instance[] instances, List<String> classifiers) {		
		this.instances = instances;
		remainClassifiers = classifiers;
	}
	
	public void adaboost(int T) {
		int len = this.instances.length;
		double totalweight = 0;
		for(int i = 0; i < len; i ++) {
			if(instances[i].label == -1){
				instances[i].weight = questionablePreference ;
				totalweight += questionablePreference;
			}
			else{
				instances[i].weight = 1.0;
				totalweight += 1;
			}
		}
		for(int i = 0; i < len; i++){
			instances[i].weight = instances[i].weight * 1.0 / totalweight;
		}
		classifierList = new ArrayList<String>();
		alphaList = new ArrayList<Double>();
		for(int t = 0; t < T; t++) {
			String cf = getMinErrorRateClassifier(this.instances);
			double errorRate = calculateOneClassifierErrorRate(cf, instances);
			remainClassifiers.remove(cf); // discard this inspector
			if(errorRate > 0.5){
				continue;
			}
			//计算弱分类器的权重
			classifierList.add(cf);
			double alpha = 0.5 * Math.log((1 - errorRate) / errorRate);
			alphaList.add(alpha);
			//更新样例的权重
			double z = 0;
			double unbalancedweight = 1.0;
			for(int i = 0; i < this.instances.length; i++) {
				if(instances[i].label == -1)
					unbalancedweight = questionablePreference;
				instances[i].weight = unbalancedweight*instances[i].weight * Math.exp(-alpha * instances[i].label * instances[i].labeles.get(cf));
				z += instances[i].weight;
			}
			for(int i = 0; i < instances.length; i++) {
				instances[i].weight /= z;
			}
		}
	}
	

	/**
	 * 得到错误率最低的分类器
	 * @param W
	 * @return
	 */
	public void printInstanceWeight(Instance[] data){
		ArrayList<String> line = new ArrayList<String>();
		for(Instance i:data){
			line.add(i.weight+"");
		}
		System.out.println(""+line.toString());
	}
	private String getMinErrorRateClassifier(Instance[] instances) {
		
		double errorRate = Double.MAX_VALUE;
		String minErrorRateClassifier = "";
		for(String clf:remainClassifiers) {
			double rate = calculateOneClassifierErrorRate(clf, instances);
			if(errorRate > rate){
				errorRate  = rate;
				minErrorRateClassifier = clf;
			}
		}
		return minErrorRateClassifier;
	}
	public double calculateOneClassifierErrorRate(String clfname, Instance[] instances){
		double error = 0;
		double overflow = 1e-8;
		for(Instance i: instances){
			error += i.weight*i.labeles.get(clfname)*i.label;
		}
		double ret = 0.5 - 0.5*error;
		if(ret < 0 && Math.abs(ret) < overflow)
		{
			return overflow;
		}
		return ret;
	}
	public double getResult(List<String> clfs, List<Double> weights, List<Instance> data){
		double ret = 0.0;
		int total = data.size(); 
		int wrong = 0;
		for(Instance i: data){
			wrong += (getLabel(i, clfs, weights) == i.label ? 1 : 0);
		}
		ret = wrong * 1.0 / total;
		return ret;
		
	}
	public double getLabel(Instance ina, List<String> clfs, List<Double> weights ){
		double ret = 0;
		for(int i = 0; i < clfs.size(); i++){
			ret += ina.labeles.get(clfs.get(i))*weights.get(i);
		}
		if(ret > 0){
			return 1;
		}
		else{
			return -1;
		}
	}
	public static void main(String[] args) {
		CreatingTrainingData cdata = new CreatingTrainingData();
		ArrayList<Instance> all = cdata.runDir();
		System.out.println(cdata.printTrainingData(all));
		List<String> clfs = InspectorFactory.getInspectorNames();
		AdaInspectorTrainer adaTrainer = new AdaInspectorTrainer(all.toArray(new Instance[all.size()]), clfs);
		adaTrainer.adaboost(clfs.size());
		JSONObject parameters = new JSONObject();
		for(int i = 0; i < adaTrainer.alphaList.size(); i++){
			parameters.put(adaTrainer.classifierList.get(i), adaTrainer.alphaList.get(i));
		}
		System.out.println(""+StringEscapeUtils.escapeJava(parameters.toString()));
		System.out.println(adaTrainer.getResult(adaTrainer.classifierList, adaTrainer.alphaList, all));
	}
}
