package edu.isi.karma.cleaning;

import java.io.BufferedReader;
import java.io.FileReader;

import weka.classifiers.functions.SimpleLogistic;
import weka.core.Instance;
import weka.core.Instances;
import edu.isi.karma.util.Prnt;

public class RegularityClassifer {
	/*public static void Add2FeatureFile(Collection<String> posEg,String ftrs_file,boolean ispos) throws Exception 
	{
		ftrs_file = "./cleaning_dataset/ftrs_file1.txt" ;
		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(ftrs_file,true)) ;//append mode
		RegularityFeatureSet regfs = new RegularityFeatureSet() ;
		ArrayList<Feature> ftrs = new ArrayList<Feature>(regfs.computeFeatures(posEg)) ; 			
		for(int i=0;i<ftrs.size();i++) 
		{
				Feature ftr = ftrs.get(i) ;
				fileWriter.write("" + ftr.getScore() + ",") ;
		}
		if(ispos)
		{
			fileWriter.write("1\n") ;
		}
		else
		{
			fileWriter.write("-1\n") ;
		}
		fileWriter.close() ;
	}*/
	public RegularityClassifer()
	{
		
	}
	public SimpleLogistic train(String fpath) throws Exception
	{
		BufferedReader fileReader = new BufferedReader(new FileReader(fpath)) ;
		//BufferedReader fileReader = new BufferedReader(new FileReader("/Users/amangoel/research/data/iris2.txt")) ;
		
		Instances instances = new Instances(fileReader) ;
		instances.setClassIndex(instances.numAttributes() -1) ;
		
		Prnt.prn(instances.numAttributes()) ;
		
		SimpleLogistic logreg = new SimpleLogistic(10, true, true) ;
		//SimpleLogistic logreg = new SimpleLogistic() ;
		logreg.buildClassifier(instances) ;
		return logreg;
	}
	public void Classify(String fpath,SimpleLogistic cf) throws Exception
	{
		BufferedReader fileReader = new BufferedReader(new FileReader(fpath)) ;
		//BufferedReader fileReader = new BufferedReader(new FileReader("/Users/amangoel/research/data/iris2.txt")) ;
		
		Instances instances = new Instances(fileReader) ;
		instances.setClassIndex(instances.numAttributes() -1) ;
		Prnt.prn(instances.numAttributes()) ;
		for(int i=0;i<instances.size();i++) {			
			Instance instance = instances.get(i) ;		
			double[] dist = cf.distributionForInstance(instance) ;
			//System.out.println(cf.classifyInstance(instance));
			for(double d : dist) {
				System.out.print(d + "  ") ;
			}
			System.out.println() ;
		}
		fileReader.close() ;
		Prnt.prn(cf) ;
	}
	public static void main(String[] args)
	{
		try
		{
			RegularityClassifer rc = new RegularityClassifer();
			SimpleLogistic c = rc.train("./cleaning_dataset/ftrs_file.txt");
			rc.Classify("./cleaning_dataset/ftrs_file1.txt", c);
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
		}
	}

}
