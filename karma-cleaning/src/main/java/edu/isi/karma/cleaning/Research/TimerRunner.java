package edu.isi.karma.cleaning.Research;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVReader;
import edu.isi.karma.cleaning.DataPreProcessor;
import edu.isi.karma.cleaning.ExampleSelection;
import edu.isi.karma.cleaning.InterpreterType;
import edu.isi.karma.cleaning.Messager;
import edu.isi.karma.cleaning.ProgSynthesis;
import edu.isi.karma.cleaning.ProgramRule;
import edu.isi.karma.cleaning.UtilTools;

public class TimerRunner implements Runnable {
	File f;
	DataCollection dCollection;

	public TimerRunner(File f, DataCollection dCollection) {
		this.f = f;
		this.dCollection = dCollection;
	}

	public void run() {
		try {
			HashMap<String, Vector<String>> records = new HashMap<String, Vector<String>>();
			Vector<String[]> examples = new Vector<String[]>();
			Vector<String[]> addExamples = new Vector<String[]>();
			Vector<String[]> entries = new Vector<String[]>();
			// TODO Auto-generated method stub
			if (f.getName().indexOf(".csv") != -1
					&& f.getName().indexOf(".csv") == (f.getName().length() - 4)) {
				// MyLogger.logsth("========"+f.getName()+"============\n");
				HashMap<String, String[]> xHashMap = new HashMap<String, String[]>();
				@SuppressWarnings("resource")
				CSVReader cr = new CSVReader(new FileReader(f), ',', '"', '\0');
				String[] pair;
				int index = 0;
				Vector<String> vtmp = new Vector<String>();
				while ((pair = cr.readNext()) != null) {
					if (pair == null || pair.length <= 1)
						break;
					entries.add(pair);
					vtmp.add(pair[0]);
					String[] line = { pair[0], pair[1], "", "", "wrong" }; // org,
																			// tar,
																			// tarcode,
																			// label
					xHashMap.put(index + "", line);
					index++;
				}
				DataPreProcessor dpp = new DataPreProcessor(vtmp);
				dpp.run();
				Messager msger = new Messager();
				Vector<Vector<String[]>> constraints = new Vector<Vector<String[]>>();
				if (entries.size() <= 1)
					return;
				ExampleSelection expsel = new ExampleSelection();
				ExampleSelection.firsttime = true;
				expsel.inite(xHashMap, null);
				int target = Integer.parseInt(expsel.Choose());
				String[] mt = { "<_START>" + entries.get(target)[0] + "<_END>",
						entries.get(target)[1] };
				examples.add(mt);
				ExampleSelection.firsttime = false;
				// accuracy record code
				ArrayList<double[]> accArrayList = new ArrayList<double[]>();
				while (true) // repeat as no incorrect answer appears.
				{
					if (examples.size() == 8) {
						System.out.println("Hello World");
					}
					long checknumber = 1;
					long iterAfterNoFatalError = 0;
					long isvisible = 0;
					HashMap<String, Vector<String[]>> expFeData = new HashMap<String, Vector<String[]>>();
					Vector<String> resultString = new Vector<String>();
					xHashMap = new HashMap<String, String[]>();
					ProgSynthesis psProgSynthesis = new ProgSynthesis();
					HashMap<String, String> unlabeledData = new HashMap<String, String>();
					for (int i = 0; i < vtmp.size(); i++) {
						unlabeledData.put("" + i, vtmp.get(i));
					}
					psProgSynthesis.inite(examples, dpp, msger);
					Vector<ProgramRule> pls = new Vector<ProgramRule>();
					Collection<ProgramRule> ps = psProgSynthesis.run_main();
					// collect history contraints
					msger.updateCM_Constr(psProgSynthesis.partiCluster
							.getConstraints());
					msger.updateWeights(psProgSynthesis.partiCluster.weights);
					// constraints.addAll();
					if (ps != null) {
						pls.addAll(ps);
					} else {
						System.out.println("Cannot find any rule");
					}
					String[] wexam = null;
					if (pls.size() == 0)
						break;
					long t1 = System.currentTimeMillis();
					int ErrorCnt = 0;
					int clf_acc_error_cnt = 0;
					int clf_acc_total_cnt = 0;
					HashMap<String, HashMap<String, String>> uData = new HashMap<String, HashMap<String, String>>();
					for (int i = 0; i < pls.size(); i++) {
						ProgramRule script = pls.get(i);
						// System.out.println(script);
						String res = "";
						for (int j = 0; j < entries.size(); j++) {
							InterpreterType worker = script
									.getRuleForValue(entries.get(j)[0]);
							String classlabel = script.getClassForValue(entries
									.get(j)[0]);
							String tmps = worker
									.execute_debug(entries.get(j)[0]);
							HashMap<String, String> dict = new HashMap<String, String>();
							dict.put("class", classlabel);
							UtilTools.StringColorCode(entries.get(j)[0], tmps,
									dict);
							String s = dict.get("Tar");
							res += s + "\n";
							if (ConfigParameters.debug == 1) {
								String indicator = "wrong";
								if (s.compareTo(entries.get(j)[1]) == 0)
									indicator = "correct";
								if (!uData.containsKey(j + ""))
									uData.put(j + "", dict);
							}
							// classifier accuracy

							boolean existres = false;
							for (InterpreterType w : script.rules.values()) {
								String xtmp = w.execute(entries.get(j)[0]);
								if (xtmp != null
										&& xtmp.length() > 0
										&& xtmp.compareTo(entries.get(j)[1]) == 0) {
									existres = true;
									break;
								}
							}
							if (existres) {
								clf_acc_total_cnt++;
								if (s == null || s.length() == 0
										|| s.compareTo(entries.get(j)[1]) != 0) {
									clf_acc_error_cnt++;
								}
							}
							//
							if (s == null || s.length() == 0) {
								String[] ts = {
										"<_START>" + entries.get(j)[0]
												+ "<_END>", "", tmps,
										classlabel, "wrong" };
								xHashMap.put(j + "", ts);
								wexam = ts;
								checknumber++;
							}
							boolean isfind = false;
							for (String[] exppair : examples) {
								if (exppair[0].compareTo("<_START>"
										+ dict.get("Org") + "<_END>") == 0) {
									String[] exp = { dict.get("Org"), tmps };
									if (!expFeData.containsKey(classlabel)) {
										Vector<String[]> vstr = new Vector<String[]>();
										vstr.add(exp);
										expFeData.put(classlabel, vstr);
									} else {
										expFeData.get(classlabel).add(exp);
									}
									isfind = true;
								}
							}
							// update positive traing data with user
							// specification
							for (String[] tmpx : addExamples) {
								if (tmpx[0].compareTo(dict.get("Org")) == 0
										&& tmpx[1].compareTo(dict.get("Tar")) == 0) {
									String[] exp = { dict.get("Org"), tmps };
									if (!expFeData.containsKey(classlabel)) {
										Vector<String[]> vstr = new Vector<String[]>();
										vstr.add(exp);
										expFeData.put(classlabel, vstr);
									} else {
										expFeData.get(classlabel).add(exp);
									}
									isfind = true;
								}
							}
							if (!isfind) {
								String[] ts = {
										"<_START>" + entries.get(j)[0]
												+ "<_END>", s, tmps,
										classlabel, "right" };
								if (s.compareTo(entries.get(j)[1]) != 0) {
									ErrorCnt++;
									wexam = ts;
									ts[4] = "wrong";
								}
								xHashMap.put(j + "", ts);
							}
						}

						if (wexam == null
								|| (entries.size() - ErrorCnt) * 1.0
										/ entries.size() == 1.0) {
							wexam = null;
							break;
						}
						resultString.add(res);
					}
					double resultacc = (entries.size() - ErrorCnt) * 1.0
							/ entries.size();
					double clf_acc = 1 - clf_acc_error_cnt * 1.0
							/ clf_acc_total_cnt;
					double[] accarray = { resultacc, clf_acc };
					// use uData to refiner the result
					System.out.println("" + psProgSynthesis.myprog.toString());
					System.out.println(pls.get(0).toString());
					// cRefiner.clusterUdata();
					records.put(f.getName() + examples.size(), resultString);
					long t2 = System.currentTimeMillis();

					if (wexam != null) {
						String[] wexp = new String[2];
						while (true) {
							expsel = new ExampleSelection();
							expsel.inite(xHashMap, expFeData);
							int e = Integer.parseInt(expsel.Choose());
							// /
							System.out.println("Recommand Example: "
									+ Arrays.toString(xHashMap.get("" + e)));
							// /
							if (xHashMap.get("" + e)[4].compareTo("right") != 0) {
								wexp[0] = "<_START>" + entries.get(e)[0]
										+ "<_END>";
								wexp[1] = entries.get(e)[1];
								break;
							} else {
								// update positive training data
								addExamples.add(entries.get(e));
								// update the rest dataset
								xHashMap.remove("" + e);
							}
							checknumber++;
						}

						examples.add(wexp);
						FileStat fileStat = new FileStat(f.getName() + "",
								psProgSynthesis.learnspan,
								psProgSynthesis.genspan, (t2 - t1),
								examples.size(), examples,
								psProgSynthesis.partiCluster.failedCnt,
								checknumber, iterAfterNoFatalError, isvisible,
								pls.get(0).toString(), resultacc, clf_acc);
						dCollection.addEntry(fileStat);
					} else {
						FileStat fileStat = new FileStat(f.getName() + "",
								psProgSynthesis.learnspan,
								psProgSynthesis.genspan, (t2 - t1),
								examples.size(), examples,
								psProgSynthesis.partiCluster.failedCnt,
								checknumber, iterAfterNoFatalError, isvisible,
								pls.get(0).toString(), resultacc, clf_acc);
						dCollection.addEntry(fileStat);
						dCollection.addSucceededFile(f.getName());
						break;
					}
				}
			}
			return;
		} catch (Exception ex) {
			System.out.println("" + ex.toString());
			return;
		}
	}

}
