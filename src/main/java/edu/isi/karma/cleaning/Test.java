package edu.isi.karma.cleaning;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVReader;

public class Test {
	public static void test1()
	{
		Vector<String[]> examples = new Vector<String[]>();
		String[] xStrings = {"<_START>1337 36th place<_END>","1337 36th place, Los Angeles, CA"};
		String[] yStrings = {};
		examples.add(xStrings);
		//examples.add(yStrings);
		ProgSynthesis psProgSynthesis = new ProgSynthesis();
		psProgSynthesis.inite(examples);
		Vector<ProgramRule> pls = new Vector<ProgramRule>();
		Collection<ProgramRule> ps = psProgSynthesis.run_main();
		ProgramRule pr = ps.iterator().next();
		String val = "1337 36th place";
		InterpreterType rule = pr.getRuleForValue(val);
		System.out.println(rule.execute_debug(val));
		
	}
	public static void test4(String dirpath) {
		HashMap<String, Vector<String>> records = new HashMap<String, Vector<String>>();
		File nf = new File(dirpath);
		File[] allfiles = nf.listFiles();
		// statistics
		DataCollection dCollection = new DataCollection();
		// list all the csv file under the dir
		for (File f : allfiles) {
			Vector<String[]> examples = new Vector<String[]>();
			Vector<String[]> entries = new Vector<String[]>();
			try {
				
				if (f.getName().indexOf(".csv") == (f.getName().length() - 4)) {
					HashMap<String, String[]> xHashMap = new HashMap<String, String[]>();
					CSVReader cr = new CSVReader(new FileReader(f), ',', '"',
							'\0');
					String[] pair;
					int index = 0;
					while ((pair = cr.readNext()) != null) {
						if (pair == null || pair.length <= 1)
							break;
						entries.add(pair);
						xHashMap.put(index + "", pair);
						index++;
					}
					if (entries.size() <= 1)
						continue;
					ExampleSelection expsel = new ExampleSelection();
					expsel.inite(xHashMap);
					int target = Integer.parseInt(expsel.Choose());
					String[] mt = {
							"<_START>" + entries.get(target)[0] + "<_END>",
							entries.get(target)[1] };
					examples.add(mt);
					while (true) // repeat as no correct answer appears.
					{
						Vector<String> resultString = new Vector<String>();
						xHashMap.clear();
						ProgSynthesis psProgSynthesis = new ProgSynthesis();
						psProgSynthesis.inite(examples);
						Vector<ProgramRule> pls = new Vector<ProgramRule>();
						Collection<ProgramRule> ps = psProgSynthesis.run_main();
						if (ps != null)
							pls.addAll(ps);
						String[] wexam = null;
						if (pls.size() == 0)
							break;
						long t1 = System.currentTimeMillis();
						
						for (int i = 0; i < pls.size(); i++) {
							ProgramRule script = pls.get(i);
							// System.out.println(script);
							String res = "";
							for (int j = 0; j < entries.size(); j++) {
								InterpreterType worker = script
										.getRuleForValue(entries.get(j)[0]);
								String tmps = worker.execute_debug(entries.get(j)[0]);
								HashMap<String, String> dict = new HashMap<String, String>();
								UtilTools.StringColorCode(entries.get(j)[0], tmps, dict);
								String s = dict.get("Tar");
								res += s+"\n";
								if (ConfigParameters.debug == 1)
									System.out.println("result:   " + dict.get("Tardis"));
								if (s == null || s.length() == 0) {
									String[] ts = {"<_START>" + entries.get(j)[0] + "<_END>",s};
									xHashMap.put(j + "", ts);
									wexam = ts;
								}
								if (s.compareTo(entries.get(j)[1]) != 0) {
									String[] ts = {"<_START>" + entries.get(j)[0] + "<_END>",s};
									xHashMap.put(j + "", ts);
									wexam = ts;
								}
							}
							if (wexam == null)
								break;
							resultString.add(res);
						}
						records.put(f.getName()+examples.size(), resultString);
						long t2 = System.currentTimeMillis();
						FileStat fileStat = new FileStat(f.getName(),
								psProgSynthesis.learnspan,
								psProgSynthesis.genspan, (t2 - t1),
								examples.size(), examples,
								psProgSynthesis.ruleNo, pls.get(0).toString());
						dCollection.addEntry(fileStat);
						if (wexam != null) {
							expsel.inite(xHashMap);
							int e = Integer.parseInt(expsel.Choose());
							String[] wexp = {
									"<_START>" + entries.get(e)[0] + "<_END>",
									entries.get(e)[1] };
							examples.add(wexp);
						} else {
							break;
						}
						
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		dCollection.print();
		dCollection.print1();
		//hashResultPrint(records);
	}
	public static void hashResultPrint(HashMap<String, Vector<String>> res)
	{
		String s = "";
		for(String key:res.keySet())
		{
			s += "=============="+key+"=============\n";
			for(String value: res.get(key))
			{
				s += value+"\n";
			}
		}
		System.out.println(""+s);
	}
	public static void test0_sub(Vector<String[]> all, Vector<String[]> cand,
			Vector<String[]> examples, int cnt) {
		if (cand.size() <= 0) {
			if (cnt > Test.MaximalNumber) {
				MaximalNumber = cnt;
				Test.larexamples = examples;
			}
			if (cnt < Test.MinimalNumber) {
				MinimalNumber = cnt;
				Test.smalexamples = examples;
			}
			//System.out.println("returned");
			return;
		}
		for (int p = 0; p < cand.size(); p++) {
			Vector<String[]> tmp = new Vector<String[]>();
			tmp.addAll(examples);
			String[] x = { "<_START>" + cand.get(p)[0] + "<_END>",
					cand.get(p)[1] };
			tmp.add(x);
			Vector<String[]> tmpxStrings = new Vector<String[]>();

			ProgSynthesis psProgSynthesis = new ProgSynthesis();
			psProgSynthesis.inite(tmp);
			Vector<ProgramRule> pls = new Vector<ProgramRule>();
			Collection<ProgramRule> ps = psProgSynthesis.run_main();
			if (ps != null)
				pls.addAll(ps);
			String[] wexam = null;
			if (pls.size() == 0)
				break;
			for (int i = 0; i < pls.size(); i++) {
				ProgramRule script = pls.get(i);
				for (int j = 0; j < all.size(); j++) {
					InterpreterType worker = script
							.getRuleForValue(all.get(j)[0]);
					String s = worker.execute(all.get(j)[0]);
					//System.out.println("result:   " + s);
					if (s == null || s.length() == 0) {
						wexam = all.get(j);
						String[] ep = { "<_START>" + wexam[0] + "<_END>",
								wexam[1] };
						tmpxStrings.add(ep);
						continue;
					}
					if (s.compareTo(all.get(j)[1]) != 0) {
						wexam = all.get(j);
						String[] ep = { "<_START>" + wexam[0] + "<_END>",
								wexam[1] };
						tmpxStrings.add(ep);
						continue;
					}
				}

			}
			test0_sub(all, tmpxStrings, tmp, cnt + 1);
		}
	}

	public static int MaximalNumber = -1;
	public static int MinimalNumber = 100;
	public static Vector<String[]> larexamples = new Vector<String[]>();
	public static Vector<String[]> smalexamples = new Vector<String[]>();

	public static void test0(String dirpath) {
		File nf = new File(dirpath);
		File[] allfiles = nf.listFiles();
		// statistics
		DataCollection dCollection = new DataCollection();
		// list all the csv file under the dir
		for (File f : allfiles) {
			Vector<String[]> examples = new Vector<String[]>();
			Vector<String[]> entries = new Vector<String[]>();
			try {
				if (f.getName().indexOf(".csv") == (f.getName().length() - 4)) {
					CSVReader cr = new CSVReader(new FileReader(f), ',', '"',
							'\0');
					String[] pair;
					while ((pair = cr.readNext()) != null) {
						if (pair == null || pair.length <= 1)
							break;
						entries.add(pair);
					}
					if (entries.size() <= 1)
						continue;
					int cnt = 0;
					Vector<String[]> candStrings = new Vector<String[]>();
					candStrings.addAll(entries);
					test0_sub(entries, candStrings, examples, cnt);
					System.out.println("File " + f.getName() + "\n");
					System.out.println("Max: " + Test.MaximalNumber);
					System.out.println("Min: " + Test.MinimalNumber);
					String str = "Larget number of Examples:\n";
					for (int x = 0; x < Test.larexamples.size(); x++) {
						str += String.format("exp: %s, %s\n",
								larexamples.get(x)[0], larexamples.get(x)[1]);
					}
					System.out.println("Largest: " + str);
					String str1 = "Smallest number of Examples:\n";
					for (int x = 0; x < Test.smalexamples.size(); x++) {
						str1 += String.format("exp: %s, %s\n",
								smalexamples.get(x)[0], smalexamples.get(x)[1]);
					}
					System.out.println("Smallest: " + str1);
					//clear
					Test.MaximalNumber = -1;
					Test.larexamples = new Vector<String[]>();
					Test.MinimalNumber = 200;
					Test.smalexamples = new Vector<String[]>();
				}
				//
			} catch (Exception e) {
				System.out.println("" + e.toString());
			}
		}
	}

	public static void main(String[] args) {
		// load parameters
		ConfigParameters cfg = new ConfigParameters();
		cfg.initeParameters();
		DataCollection.config = cfg.getString();
		//Test.test0("/Users/bowu/Research/testdata/TestSingleFile");
		//Test.test4("/Users/bowu/Research/testdata/TestSingleFile");
		Test.test1();
	}
}
