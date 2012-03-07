package edu.isi.karma.cleaning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;
import java.util.Vector;


//generate randomly mixed data for training
public class NegativeDataGen {
	public static void randremove(Vector<TNode> x)
	{
		Random r = new Random();
		int cnt = (int)(x.size()*0.7)+r.nextInt((int)(x.size()*0.3));
		int i = 0;
		while(i<cnt)
		{
			int pos =r.nextInt((int)(x.size()-1));
			x.remove(pos);
			i++;
		}
	}

	// fetch all the files under a dir
	//concatenate all string into one
	//token the string and randomly remove random number < size tokens
	public static void main(String[] args)
	{
		File dir = new File("/Users/bowu/Research/dataclean/data/RuleData/rawdata");
		
		File[] flist = dir.listFiles();
		try
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter("/Users/bowu/Research/dataclean/data/negadata0.txt"));
			BufferedReader[] brlist = new BufferedReader[flist.length];
			for(int i = 0 ; i<flist.length;i++)
			{
				BufferedReader br = new BufferedReader(new FileReader(flist[i]));
				brlist[i] = br;
				//br.close();
			}
			while(true)
			{
				String line = "";
				for(int i = 0 ; i<flist.length;i++)
				{
					line += brlist[i].readLine();
					//br.close();
				}
				Ruler r = new Ruler();
				r.setNewInput(line);
				NegativeDataGen.randremove(r.vec);
				String res = r.toString();
				if(res == null)
					break;
				bw.write(res+"\n");
				bw.flush();
				System.out.println(""+res);
				if(line.compareTo("")==0)
					break;
			}
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
		}
	}
}
