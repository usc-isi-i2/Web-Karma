package edu.isi.karma.cleaning;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVReader;
import edu.isi.karma.rep.cleaning.RamblerTransformationExample;
import edu.isi.karma.rep.cleaning.RamblerTransformationInputs;
import edu.isi.karma.rep.cleaning.RamblerTransformationOutput;
import edu.isi.karma.rep.cleaning.RamblerValueCollection;
import edu.isi.karma.rep.cleaning.TransformationExample;
import edu.isi.karma.rep.cleaning.ValueCollection;

public class CommandLineInterface {
	public void Run(String exp, String data, String res)
	{
		Vector<TransformationExample> examples = new Vector<TransformationExample>();
		try {
			CSVReader cr1 = new CSVReader(new FileReader(exp), ',','"','\0');
			String[] line;
			while((line = cr1.readNext())!=null)
			{
				if(line.length >= 3)
				{
					RamblerTransformationExample re = new RamblerTransformationExample(
						line[1], line[2], line[0]);
					examples.add(re);
				}
			}
			cr1.close();
			HashMap<String, String> datadict = new HashMap<String, String>();
			CSVReader cr2 = new CSVReader(new FileReader(data), ',','"','\0');
			while((line = cr2.readNext())!=null)
			{
				if(line.length >= 2)
				{
					datadict.put(line[0], line[1]);
				}
			}
			System.out.println(""+datadict.toString());
			RamblerValueCollection rv = new RamblerValueCollection(datadict);
			cr2.close();
			System.out.println(""+examples.toString());
			RamblerTransformationInputs ri = new RamblerTransformationInputs(examples, rv);
			RamblerTransformationOutput ro = new RamblerTransformationOutput(ri);
			BufferedWriter cr3 = new BufferedWriter(new FileWriter(res));
			System.out.println("Transformation keyset: "+ro.getTransformations().keySet());
			for (String ruleid : ro.getTransformations().keySet()) {
				System.out.println(""+ruleid);
				ValueCollection vCollection = ro.getTransformedValues(ruleid);
				String r = vCollection.getJson().toString();
				System.out.println("The result: "+r);
				cr3.write(r);
			}
			cr3.flush();
			cr3.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args)
	{
		//arg1 csv file of the examples (id, org, tar)
		String path1 = args[0];
		//arg2 csv file of the records to transform (id, org)
		String path2 = args[1];
		//arg3 json file of the transformed result
		String path3 = args[2];
		CommandLineInterface cmdInterface = new CommandLineInterface();
		System.out.println(""+String.format("%s,%s, %s", path1, path2, path3));
		cmdInterface.Run(path1, path2, path3);
		System.out.println("Done...");
		System.exit(0);
	}

}
