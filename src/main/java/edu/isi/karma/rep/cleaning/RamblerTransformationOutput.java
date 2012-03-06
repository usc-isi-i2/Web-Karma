package edu.isi.karma.rep.cleaning;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVReader;
import edu.isi.karma.cleaning.*;

public class RamblerTransformationOutput implements TransformationOutput {

	private RamblerTransformationInputs input;
	private HashMap<String,Transformation> transformations; 
	public RamblerTransformationOutput(RamblerTransformationInputs input)
	{
		this.input = input;
		transformations = new HashMap<String,Transformation>();
		this.learnTransformation();
	}
	private void learnTransformation()
	{
		Collection<TransformationExample> exs =  input.getExamples();
		Vector<String[]> exps = new Vector<String[]>();
		Iterator<TransformationExample> iter = exs.iterator();
		while(iter.hasNext())
		{
			TransformationExample t = iter.next();
			String[] tmp = {t.getBefore(),t.getAfter()};
			exps.add(tmp);
		}
		Vector<String> trans = RuleUtil.genRule(exps);
		for(int i = 0; i<trans.size(); i++)
		{
			String[] rules = trans.get(i).split("#");
			//System.out.println(""+s1);
			Vector<String> xr = new Vector<String>();
			for(int t = 0; t< rules.length; t++)
			{
				if(rules[t].length()!=0)
					xr.add(rules[t]);
			}
			RamblerTransformation r = new RamblerTransformation(xr);
			transformations.put(r.signature, r);
		}
	}
	@Override
	public HashMap<String,Transformation> getTransformations() {
		// TODO Auto-generated method stub
		return transformations;
	}

	@Override
	public ValueCollection getTransformedValues(String TransformatinId) {
		// TODO Auto-generated method stub
		Transformation t = transformations.get(TransformatinId);
		ValueCollection v = input.getInputValues();
		Collection<String> keys = v.getNodeIDs();
		Iterator<String> iter = keys.iterator();
		while(iter.hasNext())
		{
			String k = iter.next();
			String val = v.getValue(k);
			val = t.transform(val);
			v.setValue(k, val);
		}
		return v;
	}

	@Override
	public Collection<String> getRecommandedNextExample() {
		// TODO Auto-generated method stub
		return null;
	}
	//for testing the interfaces
	public static void main(String[] args)
	{
		String dirpath = "/Users/bowu/Research/dataclean/data/RuleData/rawdata/pairs/test";
		File nf = new File(dirpath);
		File[] allfiles = nf.listFiles();
		//statistics
		Vector<String> names = new Vector<String>();
		Vector<Integer> exampleCnt = new Vector<Integer>();
		Vector<Double> timeleng = new Vector<Double>();
		Vector<Integer> cRuleNum = new Vector<Integer>();
		for(File f:allfiles)
		{
			Vector<String[]> examples = new Vector<String[]>();
			Vector<String[]> entries = new Vector<String[]>();	
			try
			{
				if(f.getName().indexOf(".csv")==(f.getName().length()-4))
				{
					
					CSVReader cr = new CSVReader(new FileReader(f),'\t');
					String[] pair;
					while ((pair=cr.readNext())!=null)
					{
						pair[0] = "%"+pair[0]+"@";
						entries.add(pair);
					}
					examples.add(entries.get(0));
					boolean isend = false;
					int corrNum = 0;
					double timespan = 0.0;
					
					while(corrNum==0)
					{
						long st = System.currentTimeMillis();
						Vector<String> pls = RuleUtil.genRule(examples);
						System.out.println("Consistent Rules :"+pls.size());
						for(int k = 0; k<examples.size();k++)
						{
							System.out.println(examples.get(k)[0]+"    "+examples.get(k)[1]);
						}
						String[] wexam = null;
						for(int i = 0; i<pls.size(); i++)
						{			
							String[] rules = pls.get(i).split("#");
							//System.out.println(""+s1);
							Vector<String> xr = new Vector<String>();
							for(int t = 0; t< rules.length; t++)
							{
								if(rules[t].length()!=0)
									xr.add(rules[t]);
							}
							isend = true;
							for(int j = 0; j<entries.size(); j++)
							{
								String s = RuleUtil.applyRule(xr, entries.get(j)[0]);
								if(s== null)
								{
									isend = false;
									wexam = entries.get(j);
									break;
								}
								if(s.compareTo(entries.get(j)[1])!=0)
								{
									isend = false;
									wexam = entries.get(j);
									break;
								}
							}
							if(isend)
								corrNum++;
						}
						/*if(wexam == null)
						{
							examples.add(entries.get())
						}*/
						long ed = System.currentTimeMillis();
						timespan = (ed -st)*1.0/60000;
						if(wexam!=null&&corrNum<=0)
						{
							examples.add(wexam);
						}
					}
					names.add(f.getName());
					exampleCnt.add(examples.size());
					timeleng.add(timespan);
					cRuleNum.add(corrNum);
				}
				
			}
			catch(Exception ex)
			{
				System.out.println(""+ex.toString());
			}
			
		}
		Vector<TransformationExample> vt = new Vector<TransformationExample>();
		
		//RamblerTransformationInputs ri = new RamblerTransformationInputs(examples, inputValues) 
	}

}
