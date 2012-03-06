package edu.isi.karma.cleaning.features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import edu.isi.karma.cleaning.TNode;
class Varfeature implements Feature {

	String name = "";
	double score = 0.0;
	public Varfeature(double a, double b,String fname)
	{
		score = a-b;
		name = fname+"_var";
	}
	
	@Override
	public String getName() {
		
		return this.name;
	}
	
	@Override
	public double getScore() {
		
		return score;
	}

} 
public class VarianceFeatureSet implements FeatureSet {
	
	public VarianceFeatureSet()
	{
	}
	public Collection<Feature> computeFeatures(Collection<String> oldexamples,Collection<String> newexamples) {
		
		Vector<Feature> fs = new Vector<Feature>();
		RegularityFeatureSet rf1 = new RegularityFeatureSet();
		Collection<Feature> x = rf1.computeFeatures(oldexamples,newexamples);
		RegularityFeatureSet rf2 = new RegularityFeatureSet();
		Collection<Feature> y = rf2.computeFeatures(oldexamples,newexamples);
		Iterator<Feature> i1 = x.iterator();
		Iterator<Feature> i2 = y.iterator();
		while(i1.hasNext()&&i2.hasNext())
		{
			Feature f1 = i1.next();
			Feature f2 = i2.next();
			if(f1.getName().compareTo(f2.getName())==0)
			{
				Varfeature vf = new Varfeature(f1.getScore(),f2.getScore(),f1.getName());
				fs.add(vf);
			}
		}
		return fs;
	}

	@Override
	public Collection<String> getFeatureNames() {
		
		return null;
	}
}
