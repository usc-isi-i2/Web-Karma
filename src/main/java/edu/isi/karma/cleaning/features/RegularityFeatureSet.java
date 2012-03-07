package edu.isi.karma.cleaning.features;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.Token;

import edu.isi.karma.cleaning.*;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;


public class RegularityFeatureSet implements FeatureSet {

	public ArrayList<Vector<TNode>> tokenseqs;
	public ArrayList<Vector<TNode>> otokenseqs;
	public Vector<String> fnames;
	public RegularityFeatureSet()
	{
		tokenseqs = new ArrayList<Vector<TNode>>();
		otokenseqs= new ArrayList<Vector<TNode>>();
		fnames = new Vector<String>();
	}
	public Vector<TNode> tokenizer(String Org)
	{
		CharStream cs =  new ANTLRStringStream(Org);
		Tokenizer tk = new Tokenizer(cs);
		Token t;
		t = tk.nextToken();
		Vector<TNode> x = new Vector<TNode>();
		while(t.getType()!=-1)
		{
			int mytype = -1;
			if(t.getType()==12)
			{
				mytype = TNode.WRDTYP;
			}
			else if(t.getType() == 4)
			{
				mytype = TNode.BNKTYP;
			}
			else if(t.getType() == 8)
			{
				mytype = TNode.NUMTYP;
			}
			else if(t.getType() == 9)
			{
				mytype = TNode.SYBSTYP;
			}
			TNode tx = new TNode(mytype,t.getText());
			x.add(tx);
			//System.out.println("cnt: "+t.getText()+" type:"+t.getType());
			t = tk.nextToken();
		}
		return x;
	}
	@Override
	public Collection<Feature> computeFeatures(Collection<String> examples,Collection<String> oexamples) {
		Vector<Feature> r = new Vector<Feature>();
		
		for(String s:examples)
		{
			Vector<TNode> x = this.tokenizer(s);
			this.tokenseqs.add(x);
		}
		for(String s:oexamples)
		{
			Vector<TNode> x = this.tokenizer(s);
			this.otokenseqs.add(x);
		}
		//counting feature
		String[] symbol = {"#",";",",","!","~","@","$","%","^","&","*","(",")","_","-","{","}","[","]","\"","'",":","?","<",">","."};
		Vector<CntFeature> cntfs = new Vector<CntFeature>(symbol.length);
		for(int i=0; i<symbol.length;i++)
		{
			TNode t = new TNode(TNode.SYBSTYP,symbol[i]);
			Vector<TNode> li = new Vector<TNode>();
			li.add(t);
			cntfs.add(i,new CntFeature(this.otokenseqs,this.tokenseqs,li));
			cntfs.get(i).setName("entr_cnt_"+symbol[i]);
		}
		//count the blank, symbol  wrd and number token
		TNode t = new TNode(TNode.BNKTYP,TNode.ANYTOK);
		Vector<TNode> li = new Vector<TNode>();
		li.add(t);
		CntFeature cf = new CntFeature(this.otokenseqs,this.tokenseqs,li);
		cf.setName("entr_cnt_bnk");
		TNode t1 = new TNode(TNode.SYBSTYP,TNode.ANYTOK);
		Vector<TNode> li1 = new Vector<TNode>();
		li1.add(t1);
		CntFeature cf1 = new CntFeature(this.otokenseqs,this.tokenseqs,li1);
		cf1.setName("entr_cnt_syb");
		TNode t2 = new TNode(TNode.WRDTYP,TNode.ANYTOK);
		Vector<TNode> li2 = new Vector<TNode>();
		li2.add(t2);
		CntFeature cf2 = new CntFeature(this.otokenseqs,this.tokenseqs,li2);
		cf2.setName("entr_cnt_wrd");
		TNode t3 = new TNode(TNode.NUMTYP,TNode.ANYTOK);
		Vector<TNode> li3 = new Vector<TNode>();
		li3.add(t3);
		CntFeature cf3 = new CntFeature(this.otokenseqs,this.tokenseqs,li3);
		cf3.setName("entr_cnt_num");
		cntfs.add(cf); 
		cntfs.add(cf1);
		cntfs.add(cf2);
		cntfs.add(cf3);
		r.addAll(cntfs);
		//create the feature objects
		/*Feature0 f0 = new Feature0(this.tokenseqs);
		Feature1 f1 = new Feature1(this.tokenseqs);
		Feature2 f2 = new Feature2(this.tokenseqs);
		Feature3 f3 = new Feature3(this.tokenseqs);
		Feature4 f4 = new Feature4(this.tokenseqs);
		//Feature5 f5 = new Feature5(this.tokenseqs);
		//Feature6 f6 = new Feature6(this.tokenseqs);
		Feature7 f7 = new Feature7(this.tokenseqs);
		Feature8 f8 = new Feature8(this.tokenseqs);
		Feature9 f9 = new Feature9(this.tokenseqs);
		Feature10 f10 = new Feature10(this.tokenseqs);
		Feature11 f11 = new Feature11(this.tokenseqs);
		Feature12 f12 = new Feature12(this.tokenseqs);
		Feature13 f13 = new Feature13(this.tokenseqs);
		Feature14 f14 = new Feature14(this.tokenseqs);
		Feature15 f15 = new Feature15(this.tokenseqs);
		Feature16 f16 = new Feature16(this.tokenseqs);
		
		r.add(f0);
		r.add(f1);
		r.add(f2);
		r.add(f3);
		r.add(f4);
		//r.add(f5);
		//r.add(f6);
		r.add(f7);
		r.add(f8);
		r.add(f9);
		r.add(f10);
		r.add(f11);
		r.add(f12);
		r.add(f13);
		r.add(f14);
		r.add(f15);
		r.add(f16);*/
		for(int i= 0; i<r.size();i++)
		{
			fnames.add(r.get(i).getName());
		}
		return r;
	}
	public static void buildEntropy(double a,int[] buk)
	{
		int buks[] = buk;
		if(a>=0.0 && a<0.1)
		{
			buks[0] += 1;
		}
		else if(a>=0.1 && a<0.2)
		{
			buks[1] += 1;
		}
		else if(a>=0.2 && a<0.3)
		{
			buks[2] += 1;
		}
		else if(a>=0.3 && a<0.4)
		{
			buks[3] += 1;
		}
		else if(a>=0.4 && a<0.5)
		{
			buks[4] += 1;
		}
		else if(a>=0.5 && a<0.6)
		{
			buks[5] += 1;
		}
		else if(a>=0.6 && a<0.7)
		{
			buks[6] += 1;
		}
		else if(a>=0.7 && a<0.8)
		{
			buks[7] += 1;
		}
		else if(a>=0.8 && a<0.9)
		{
			buks[8] += 1;
		}
		else if(a>=0.9 && a<=1.0)
		{
			buks[9] += 1;
		}
	}
	public static double calShannonEntropy(int[] a)
	{
		int cnt = 0;
		for(int c:a)
		{
			cnt += c;
		}
		if(cnt==0)
			return Math.log(10);//
		double entropy = 0.0;
		for(int i=0;i<a.length;i++)
		{
			double freq = a[i]*1.0/cnt;
			if(freq==0)
				continue;
			entropy -= freq*Math.log(freq);
		}
		return entropy;
	}
	@Override
	public Collection<String> getFeatureNames() {
		
		return fnames;
		
	}
	//test the class
	public static void main(String[] args)
	{
		File dir = new File("/Users/bowu/Research/dataclean/data/RuleData");
		File[] flist = dir.listFiles();
		try
		{
			//BufferedWriter bw = new BufferedWriter(new FileWriter("/Users/bowu/Research/dataclean/data/negadata.out"));
			ResultViewer rv = new ResultViewer();
			boolean isfirstRun = true;
			for(int i = 0 ; i<flist.length;i++)
			{
				Vector<String> row = new Vector<String>();
				Vector<String> oexamples = new Vector<String>();
				Vector<String> examples = new Vector<String>();
				System.out.println(flist[i].getName());
				if(!flist[i].getName().contains(".csv"))
					continue;
				CSVReader re = new CSVReader(new FileReader(flist[i]), '\t');
				String[] line = null;
				re.readNext();//discard the first line
				while((line=re.readNext() )!= null)
				{
					oexamples.add(line[0]);
					examples.add(line[1]);
				}
				RegularityFeatureSet rf = new RegularityFeatureSet();
				Collection<Feature> cf = rf.computeFeatures(oexamples,examples);
				Feature[] x = (Feature[])cf.toArray();
				if(isfirstRun)
				{
					row.add("Featurename");
					for(int l=0;l<x.length;l++)
					{
						row.add(x[i].getName());
					}
					isfirstRun = false;
				}
				if(!isfirstRun)
				{
					row.add(flist[i].getName());
					for(int k=0;k<cf.size();k++)
					{
						row.add(String.valueOf(x[k].getScore()));
					}
				}
			}
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
		}
	}
}
class CntFeature implements Feature{
	String name = "";
	double score = 0.0;
	Vector<TNode> pa;
	public void setName(String name)
	{
		this.name = name;
	}
	public CntFeature(ArrayList<Vector<TNode>> v,ArrayList<Vector<TNode>> n,Vector<TNode> t)
	{
		pa = t;
		score= calFeatures(v,n);
	}
	// x is the old y is the new example
	public double calFeatures(ArrayList<Vector<TNode>> x,ArrayList<Vector<TNode>> y)
	{
		HashMap<Integer,Integer> tmp = new HashMap<Integer,Integer>();
		for(int i = 0; i<x.size();i++)
		{
			int cnt = 0;
			Vector<TNode> z = x.get(i);
			Vector<TNode> z1 = y.get(i);
			int bpos = 0;
			int p = 0;
			int bpos1 = 0;
			int p1 = 0;
			int cnt1 = 0;
			while (p!=-1)
			{
				p = Ruler.Search(z, pa, bpos);
				if(p==-1)
					break;
				bpos = p+1;
				cnt++;
			}
			while (p1!=-1)
			{
				p1 = Ruler.Search(z1, pa, bpos1);
				if(p1==-1)
					break;
				bpos1 = p1+1;
				cnt1++;
			}
			//use the minus value to compute homogenenity 
			cnt = cnt - cnt1;
			if(tmp.containsKey(cnt))
			{
				tmp.put(cnt, tmp.get(cnt)+1);
			}
			else
			{
				tmp.put(cnt, 1);
			}
		}
		Integer a[] = new Integer[tmp.keySet().size()];
		tmp.values().toArray(a);
		int b[] = new int[a.length];
		for(int i = 0; i<a.length;i++)
		{
			b[i] = a[i].intValue();
		}
		return RegularityFeatureSet.calShannonEntropy(b)*1.0/Math.log(x.size());
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
//the avg of frequency of symbols divided by standard variance
class Feature0 implements Feature {
	final static String name = "entr_freq_bnk";
	double score = 0.0;
	public Feature0(ArrayList<Vector<TNode>> x)
	{
		score = calFeatures(x);
		//System.out.println(this.name+" "+score);
	}
	public double calFeatures(ArrayList<Vector<TNode>> x)
	{
		Vector<Double> freqs = new Vector<Double>();
		double total = 0.0;
		for(Vector<TNode> t:x)
		{
			int size = t.size();
			if(size==0)
			{
				continue;
			}
			int scnt = 0;
			for(TNode e:t)
			{
				if(e.type==TNode.BNKTYP)
				{
					scnt++;
				}
			}
			freqs.add(scnt*1.0/size);
			total += scnt*1.0/size;
		}
		//calculate average
		double avg = total*1.0/x.size();
		//calculate standard variance
		double totvar = 0.0;
		for(Double d:freqs)
		{
			totvar += (d-avg)*(d-avg);
		}
		if(totvar==0) // ??????
		{
			totvar = 0.00001;
		}
		return 1-avg*1.0/Math.log(10);
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
// the avg of frequency of symbols divided by standard variance
class Feature1 implements Feature {
	final static String name = "entr_freq_syb";
	double score = 0.0;
	public Feature1(ArrayList<Vector<TNode>> x)
	{
		score = calFeatures(x);
		//System.out.println(this.name+" "+score);
	}
	public double calFeatures(ArrayList<Vector<TNode>> x)
	{
		Vector<Double> freqs = new Vector<Double>();
		double total = 0.0;
		int a[] = new int[10];
		for(Vector<TNode> t:x)
		{
			int size = t.size();
			if(size==0)
			{
				continue;
			}
			int scnt = 0;
			for(TNode e:t)
			{
				if(e.type==TNode.SYBSTYP)
				{
					scnt++;
				}
			}
			freqs.add(scnt*1.0/size);
			RegularityFeatureSet.buildEntropy(scnt*1.0/size,a);
			total += scnt*1.0/size;
		}
		return 1-RegularityFeatureSet.calShannonEntropy(a)*1.0/Math.log(10);
		
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
//the freqency of number token
class Feature2 implements Feature {

	final static String name = "entr_freq_num";
	double score = 0.0;
	public Feature2(ArrayList<Vector<TNode>> x)
	{
		score = calFeatures(x);
		//System.out.println(this.name+" "+score);
	}
	public double calFeatures(ArrayList<Vector<TNode>> x)
	{
		Vector<Double> freqs = new Vector<Double>();
		double total = 0.0;
		int a[] = new int[10];
		for(Vector<TNode> t:x)
		{
			int size = t.size();
			if(size==0)
			{
				continue;
			}
			int scnt = 0;
			for(TNode e:t)
			{
				if(e.type==TNode.NUMTYP)
				{
					scnt++;
				}
			}
			freqs.add(scnt*1.0/size);
			RegularityFeatureSet.buildEntropy(scnt*1.0/size, a);
			total += scnt*1.0/size;
		}
		
		return 1-RegularityFeatureSet.calShannonEntropy(a)*1.0/Math.log(10);
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
// the freqency of wrd  token
class Feature3 implements Feature {

	final static String name = "entr_freq_wrd";
	double score = 0.0;
	public Feature3(ArrayList<Vector<TNode>> x)
	{
		score = calFeatures(x);
		//System.out.println(this.name+" "+score);
	}
	public double calFeatures(ArrayList<Vector<TNode>> x)
	{
		Vector<Double> freqs = new Vector<Double>();
		double total = 0.0;
		int[] a = new int[10];
		for(Vector<TNode> t:x)
		{
			int size = t.size();
			if(size==0)
			{
				continue;
			}
			int scnt = 0;
			for(TNode e:t)
			{
				if(e.type==TNode.WRDTYP)
				{
					scnt++;
				}
			}
			freqs.add(scnt*1.0/size);
			RegularityFeatureSet.buildEntropy(scnt*1.0/size, a);
			total += scnt*1.0/size;
		}
		return 1-RegularityFeatureSet.calShannonEntropy(a)*1.0/Math.log(10);
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
// the percentage of the first token is num
class Feature4 implements Feature {

	final static String name = "prcent_first_num";
	double score = 0.0;
	public Feature4(ArrayList<Vector<TNode>> x)
	{
		score = calFeatures(x);
		System.out.println(this.name+" "+score);
	}
	public double calFeatures(ArrayList<Vector<TNode>> x)
	{
		int cnt = 0;
		for(Vector<TNode> t:x)
		{
			if(t.size()==0)
				continue;
			if(t.get(0).type == TNode.NUMTYP)
			{
				cnt ++;
			}
		}
		return cnt*1.0/x.size();
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
// the deleted content leave 
// 
class Feature5 implements Feature {

	@Override
	public String getName() {
		
		return null;
	}

	@Override
	public double getScore() {
		
		return 0;
	}

}
// the deleted position
class Feature6 implements Feature {

	@Override
	public String getName() {
		
		return null;
	}

	@Override
	public double getScore() {
		
		return 0;
	}

}
//test bigram features
//the (num wrd) frequency
class Feature7 implements Feature {

	final static String name = "entr_freq_num_wrd";
	double score = 0.0;
	public Feature7(ArrayList<Vector<TNode>> x)
	{
		score = calFeatures(x);
		//System.out.println(this.name+" "+score);
	}
	public double calFeatures(ArrayList<Vector<TNode>> x)
	{
		Vector<Double> freqs = new Vector<Double>();
		double total = 0.0;
		int a[] = new int[10];
		for(Vector<TNode> t:x)
		{
			int size = t.size();
			if(size<=1)
			{
				continue;
			}
			int scnt = 0;
			for(int i=0; i<t.size()-1;i++)
			{
				if(t.get(i).type==TNode.NUMTYP && t.get(i+1).type== TNode.WRDTYP)
				{
					scnt++;
				}
			}
			freqs.add(scnt*1.0/(size-1));
			RegularityFeatureSet.buildEntropy(scnt*1.0/size, a);
		}
		return 1-RegularityFeatureSet.calShannonEntropy(a)*1.0/Math.log(10);
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
//test bigram features
//the (num syb) frequency
class Feature8 implements Feature {

	final static String name = "entr_freq_num_syb";
	double score = 0.0;
	public Feature8(ArrayList<Vector<TNode>> x)
	{
		score = calFeatures(x);
		//System.out.println(this.name+" "+score);
	}
	public double calFeatures(ArrayList<Vector<TNode>> x)
	{
		int a[] = new int[10];
		Vector<Double> freqs = new Vector<Double>();
		double total = 0.0;
		for(Vector<TNode> t:x)
		{
			int size = t.size();
			if(size<=1)
			{
				continue;
			}
			int scnt = 0;
			for(int i=0; i<t.size()-1;i++)
			{
				if(t.get(i).type==TNode.NUMTYP && t.get(i+1).type== TNode.SYBSTYP)
				{
					scnt++;
				}
			}
			freqs.add(scnt*1.0/(size-1));
			RegularityFeatureSet.buildEntropy(scnt*1.0/size, a);
		}
		return 1-RegularityFeatureSet.calShannonEntropy(a)*1.0/Math.log(10);
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
//test bigram features
//the (num bnk) frequency
class Feature9 implements Feature {

	final static String name = "entr_freq_num_bnk";
	double score = 0.0;
	public Feature9(ArrayList<Vector<TNode>> x)
	{
		score = calFeatures(x);
		//System.out.println(this.name+" "+score);
	}
	public double calFeatures(ArrayList<Vector<TNode>> x)
	{
		Vector<Double> freqs = new Vector<Double>();
		double total = 0.0;
		int a[] = new int[10];
		for(Vector<TNode> t:x)
		{
			int size = t.size();
			if(size<=1)
			{
				continue;
			}
			int scnt = 0;
			for(int i=0; i<t.size()-1;i++)
			{
				if(t.get(i).type==TNode.NUMTYP && t.get(i+1).type== TNode.BNKTYP)
				{
					scnt++;
				}
			}
			freqs.add(scnt*1.0/(size-1));
			RegularityFeatureSet.buildEntropy(scnt*1.0/size, a);
		}
		return 1-RegularityFeatureSet.calShannonEntropy(a)*1.0/Math.log(10);
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
//test bigram features
//the (wrd num) frequency
class Feature10 implements Feature {

	final static String name = "entr_freq_wrd_num";
	double score = 0.0;
	public Feature10(ArrayList<Vector<TNode>> x)
	{
		score = calFeatures(x);
		//System.out.println(this.name+" "+score);
	}
	public double calFeatures(ArrayList<Vector<TNode>> x)
	{
		Vector<Double> freqs = new Vector<Double>();
		double total = 0.0;
		int a[]  = new int[10];
		for(Vector<TNode> t:x)
		{
			int size = t.size();
			if(size<=1)
			{
				continue;
			}
			int scnt = 0;
			for(int i=0; i<t.size()-1;i++)
			{
				if(t.get(i).type==TNode.WRDTYP && t.get(i+1).type== TNode.NUMTYP)
				{
					scnt++;
				}
			}
			freqs.add(scnt*1.0/(size-1));
			RegularityFeatureSet.buildEntropy(scnt*1.0/size, a);
		}
		return 1-RegularityFeatureSet.calShannonEntropy(a)*1.0/Math.log(10);
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
//test bigram features
//the (bnk wrd) frequency
class Feature11 implements Feature {

	final static String name = "entr_freq_bnk_wrd";
	double score = 0.0;
	public Feature11(ArrayList<Vector<TNode>> x)
	{
		score = calFeatures(x);
		//System.out.println(this.name+" "+score);
	}
	public double calFeatures(ArrayList<Vector<TNode>> x)
	{
		Vector<Double> freqs = new Vector<Double>();
		double total = 0.0;
		int a[] =new int[10];
		for(Vector<TNode> t:x)
		{
			int size = t.size();
			if(size<=1)
			{
				continue;
			}
			int scnt = 0;
			for(int i=0; i<t.size()-1;i++)
			{
				if(t.get(i).type==TNode.BNKTYP && t.get(i+1).type== TNode.WRDTYP)
				{
					scnt++;
				}
			}
			freqs.add(scnt*1.0/(size-1));
			RegularityFeatureSet.buildEntropy(scnt*1.0/size, a);
		}
		return 1-RegularityFeatureSet.calShannonEntropy(a)*1.0/Math.log(10);
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
//test bigram features
//the (wrd syb) frequency
class Feature12 implements Feature {

	final static String name = "entr_freq_wrd_syb";
	double score = 0.0;
	public Feature12(ArrayList<Vector<TNode>> x)
	{
		score = calFeatures(x);
		//System.out.println(this.name+" "+score);
	}
	public double calFeatures(ArrayList<Vector<TNode>> x)
	{
		Vector<Double> freqs = new Vector<Double>();
		int a[] = new int[10];
		for(Vector<TNode> t:x)
		{
			int size = t.size();
			if(size<=1)
			{
				continue;
			}
			int scnt = 0;
			for(int i=0; i<t.size()-1;i++)
			{
				if(t.get(i).type==TNode.WRDTYP && t.get(i+1).type== TNode.SYBSTYP)
				{
					scnt++;
				}
			}
			freqs.add(scnt*1.0/(size-1));
			RegularityFeatureSet.buildEntropy(scnt*1.0/size, a);
		}
		return 1-RegularityFeatureSet.calShannonEntropy(a)*1.0/Math.log(10);
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
//test bigram features
//the (wrd bnk) frequency
class Feature13 implements Feature {

	final static String name = "entr_freq_wrd_bnk";
	double score = 0.0;
	public Feature13(ArrayList<Vector<TNode>> x)
	{
		score = calFeatures(x);
		//System.out.println(this.name+" "+score);
	}
	public double calFeatures(ArrayList<Vector<TNode>> x)
	{
		Vector<Double> freqs = new Vector<Double>();
		double total = 0.0;
		int a[] = new int[10];
		for(Vector<TNode> t:x)
		{
			int size = t.size();
			if(size<=1)
			{
				continue;
			}
			int scnt = 0;
			for(int i=0; i<t.size()-1;i++)
			{
				if(t.get(i).type==TNode.WRDTYP && t.get(i+1).type== TNode.BNKTYP)
				{
					scnt++;
				}
			}
			freqs.add(scnt*1.0/(size-1));
			RegularityFeatureSet.buildEntropy(scnt*1.0/size, a);
		}
		return 1-RegularityFeatureSet.calShannonEntropy(a)*1.0/Math.log(10);
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
//test bigram features
//the (syb wrd) frequency
class Feature14 implements Feature {

	final static String name = "entr_freq_syb_wrd";
	double score = 0.0;
	public Feature14(ArrayList<Vector<TNode>> x)
	{
		score = calFeatures(x);
		//System.out.println(this.name+" "+score);
	}
	public double calFeatures(ArrayList<Vector<TNode>> x)
	{
		int a[] = new int[10];
		Vector<Double> freqs = new Vector<Double>();
		double total = 0.0;
		for(Vector<TNode> t:x)
		{
			int size = t.size();
			if(size<=1)
			{
				continue;
			}
			int scnt = 0;
			for(int i=0; i<t.size()-1;i++)
			{
				if(t.get(i).type==TNode.SYBSTYP && t.get(i+1).type== TNode.WRDTYP)
				{
					scnt++;
				}
			}
			freqs.add(scnt*1.0/(size-1));
			RegularityFeatureSet.buildEntropy(scnt*1.0/size, a);
		}
		return 1-RegularityFeatureSet.calShannonEntropy(a)*1.0/Math.log(10);
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
//test bigram features
//the (syb syb) frequency
class Feature15 implements Feature {

	final static String name = "entr_freq_syb_syb";
	double score = 0.0;
	public Feature15(ArrayList<Vector<TNode>> x)
	{
		score = calFeatures(x);
		//System.out.println(this.name+" "+score);
	}
	public double calFeatures(ArrayList<Vector<TNode>> x)
	{
		Vector<Double> freqs = new Vector<Double>();
		double total = 0.0;
		int a[] = new int[10];
		for(Vector<TNode> t:x)
		{
			int size = t.size();
			if(size<=1)
			{
				continue;
			}
			int scnt = 0;
			for(int i=0; i<t.size()-1;i++)
			{
				if(t.get(i).type==TNode.SYBSTYP && t.get(i+1).type== TNode.SYBSTYP)
				{
					scnt++;
				}
			}
			freqs.add(scnt*1.0/(size-1));
			RegularityFeatureSet.buildEntropy(scnt*1.0/size, a);
		}
		return 1-RegularityFeatureSet.calShannonEntropy(a)*1.0/Math.log(10);
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
//test bigram features
//the (syb num) frequency
class Feature16 implements Feature {

	final static String name = "entr_freq_syb_num";
	double score = 0.0;
	public Feature16(ArrayList<Vector<TNode>> x)
	{
		score = calFeatures(x);
		//System.out.println(this.name+" "+score);
	}
	public double calFeatures(ArrayList<Vector<TNode>> x)
	{
		Vector<Double> freqs = new Vector<Double>();
		double total = 0.0;
		int a[] = new int[10];
		for(Vector<TNode> t:x)
		{
			int size = t.size();
			if(size<=1)
			{
				continue;
			}
			int scnt = 0;
			for(int i=0; i<t.size()-1;i++)
			{
				if(t.get(i).type==TNode.SYBSTYP && t.get(i+1).type== TNode.NUMTYP)
				{
					scnt++;
				}
			}
			freqs.add(scnt*1.0/(size-1));
			RegularityFeatureSet.buildEntropy(scnt*1.0/size, a);
		}
		return 1-RegularityFeatureSet.calShannonEntropy(a)*1.0/Math.log(10);
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