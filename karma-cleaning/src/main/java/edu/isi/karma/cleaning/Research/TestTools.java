package edu.isi.karma.cleaning.Research;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class TestTools {
	public static void print(ArrayList<double[]> res) {
		String p = "";
		for (double[] line : res) {
			p += Arrays.toString(line);
			p += "\n";
		}
		System.out.println("" + p);
	}

	public void handledata() throws IOException {
		File nf = new File("./log/data");
		File[] allfiles = nf.listFiles();
		ArrayList<Vector<String[]>> res = new ArrayList<Vector<String[]>>();
		for (File f : allfiles) {
			Vector<String[]> entries = new Vector<String[]>();
			try {
				if (f.getName().indexOf(".csv") != -1
						&& f.getName().indexOf(".csv") == (f.getName().length() - 4)) {
					CSVReader cr = new CSVReader(new FileReader(f), ',', '"',
							'\0');
					String[] pair;
					while ((pair = cr.readNext()) != null) {
						if (pair == null || pair.length <= 1)
							break;
						entries.add(pair);
					}
					res.add(entries);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		String x = "./log/data/00_";
		for (int i = 0; i < res.size(); i++) {
			Vector<String[]> temp = new Vector<String[]>();
			for (int j = 0; j <= i; j++) {
				temp.addAll(res.get(j));
			}
			CSVWriter cWriter;
			cWriter = new CSVWriter(new FileWriter(x+i+".csv"));

			for (String[] line : temp) {
				cWriter.writeNext(line);
			}
			cWriter.close();
		}
	}
	public static void main(String[] args)
	{
		TestTools ttls = new TestTools();
		try {
			ttls.handledata();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
