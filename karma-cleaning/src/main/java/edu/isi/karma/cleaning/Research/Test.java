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
import edu.isi.karma.cleaning.EmailNotification;
import edu.isi.karma.cleaning.ExampleCluster;
import edu.isi.karma.cleaning.ExampleCluster.method;
import edu.isi.karma.cleaning.ExampleSelection;
import edu.isi.karma.cleaning.GradientDecendOptimizer;
import edu.isi.karma.cleaning.InterpreterType;
import edu.isi.karma.cleaning.Messager;
import edu.isi.karma.cleaning.ProgSynthesis;
import edu.isi.karma.cleaning.ProgramRule;
import edu.isi.karma.cleaning.UtilTools;
import edu.isi.karma.cleaning.features.RecordClassifier;

public class Test {

	// check whether it longest or shortest
	public static boolean visible(HashMap<String, String[]> xHashMap, String Id) {
		String[] pair = xHashMap.get(Id);
		HashMap<String, String> tmp = new HashMap<String, String>();
		try {
			UtilTools.StringColorCode(pair[0], pair[1], tmp);
		} catch (Exception e) {
			tmp.put("Org", pair[0]);
			tmp.put("Tar", "ERROR");
			tmp.put("Orgdis", pair[0]);
			tmp.put("Tardis", "ERROR");
		}
		String tar = tmp.get("Tar");
		int length = tar.length();
		boolean sequalBefore = false;
		boolean lequalBefore = false;
		boolean shortest = true;
		boolean longest = true;
		for (String[] elem : xHashMap.values()) {
			HashMap<String, String> t = new HashMap<String, String>();
			try {
				UtilTools.StringColorCode(elem[0], elem[1], t);
			} catch (Exception ex) {
				tmp.put("Org", elem[0]);
				tmp.put("Tar", "ERROR");
				tmp.put("Orgdis", elem[0]);
				tmp.put("Tardis", "ERROR");
			}
			String tres = tmp.get("Tar");
			int newl = tres.length();
			if (newl >= length) {
				if (lequalBefore)
					longest = false;
				lequalBefore = true;
			}
			if (newl <= length) {
				if (sequalBefore)
					shortest = false;
				sequalBefore = true;
			}
		}
		return (shortest || longest);
	}

	public void parameterSelection(String dirpath) {
		ArrayList<String> selectParams = new ArrayList<String>();
		ArrayList<String> runningInfo = new ArrayList<String>();
		File nf = new File(dirpath);
		File[] allfiles = nf.listFiles();
		double[] r = { 0.2, 0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8 };
		// statistics
		// list all the csv file under the dir
		for (File f : allfiles) {
			if (f.getName().indexOf(".csv") != -1
					&& f.getName().indexOf(".csv") == (f.getName().length() - 4)) {
				double minS = Double.MAX_VALUE;
				double bestRatio = 0.0;
				for (int i = 0; i < r.length; i++) {
					GradientDecendOptimizer.ratio = r[i];
					double s = transform(f);
					if (s <= minS) {
						minS = s;
						bestRatio = r[i];

					}
					String runinfo = String.format("%s: ratio: %f, value:%f\n",
							f.getName(), r[i], s);
					runningInfo.add(runinfo);
				}
				String parms = String.format("%s: ratio: %f, BestVal:%f",
						f.getName(), bestRatio, minS);
				selectParams.add(parms);
			}
		}
		System.out.println("" + runningInfo.toString());
		System.out.println(selectParams.toString());
	}

	public double transform(File f) {
		double measure = Double.MAX_VALUE;
		try {
			Vector<String[]> examples = new Vector<String[]>();
			Vector<String[]> addExamples = new Vector<String[]>();
			Vector<String[]> entries = new Vector<String[]>();
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
					return Double.MAX_VALUE;
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
					if (examples.size() == 4) {
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
							if (Test.isExample(entries.get(j)[0], examples)) {
								s = entries.get(j)[1];
							}
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
								if (expsel.isDetectingQuestionableRecord) {
									iterAfterNoFatalError++;
									// check whether this record is has the
									// longest or shortest result
									Boolean v = visible(xHashMap, "" + e);
									if (v) {
										isvisible += 1;
									}
								}
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

					} else {
						measure = examples.size();
						break;
					}
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return measure;
	}
	public static String test_seq(ArrayList<String[]> exps) {
		String timeres = "";
		HashMap<String, String[]> xHashMap = new HashMap<String, String[]>();
		int index = 0;
		Vector<String> vtmp = new Vector<String>();
		Vector<String[]> examples = new Vector<String[]>();
		for (String[] pair : exps) {
			if (pair == null || pair.length <= 1)
				break;
			String[] line = { pair[0], pair[1], "", "", "wrong" };
			vtmp.add(pair[0]);
			xHashMap.put(index + "", line);
			index++;
		}
		DataPreProcessor dpp = new DataPreProcessor(vtmp);
		dpp.run();
		Messager msger = new Messager();
		Vector<Vector<String[]>> constraints = new Vector<Vector<String[]>>();
		ArrayList<double[]> accArrayList = new ArrayList<double[]>();
		int i = 0;
		while (i<exps.size()) // repeat as no incorrect answer appears.
		{
			String[] tmt = { "<_START>" + exps.get(i)[0] + "<_END>", exps.get(i)[1] };
			examples.add(tmt);
			xHashMap = new HashMap<String, String[]>();
			ProgSynthesis psProgSynthesis = new ProgSynthesis();
			psProgSynthesis.inite(examples, dpp, msger);
			Vector<ProgramRule> pls = new Vector<ProgramRule>();
			long t1 = System.currentTimeMillis();
			Collection<ProgramRule> ps = psProgSynthesis.run_main();
			long span = System.currentTimeMillis()-t1;
			timeres += span+",";
			// collect history contraints
			msger.updateCM_Constr(psProgSynthesis.partiCluster.getConstraints());
			msger.updateWeights(psProgSynthesis.partiCluster.weights);
			i++;
			// constraints.addAll();
			if (ps != null) {
				pls.addAll(ps);
				System.out.println("program: "+ps);
			} else {
				System.out.println("Cannot find any rule");
			}
			if (pls.size() == 0)
				break;
		}
		return timeres;
	}
	public static String test_adaptive_seq(ArrayList<String[]> exps) {
		String timeres = "";
		HashMap<String, String[]> xHashMap = new HashMap<String, String[]>();
		int index = 0;
		Vector<String> vtmp = new Vector<String>();
		Vector<String[]> examples = new Vector<String[]>();
		for (String[] pair : exps) {
			if (pair == null || pair.length <= 1)
				break;
			String[] line = { pair[0], pair[1], "", "", "wrong" };
			vtmp.add(pair[0]);
			xHashMap.put(index + "", line);
			index++;
		}
		DataPreProcessor dpp = new DataPreProcessor(vtmp);
		dpp.run();
		Messager msger = new Messager();
		Vector<Vector<String[]>> constraints = new Vector<Vector<String[]>>();
		ArrayList<double[]> accArrayList = new ArrayList<double[]>();
		int i = 0;
		while (i<exps.size()) // repeat as no incorrect answer appears.
		{
			String[] tmt = { "<_START>" + exps.get(i)[0] + "<_END>", exps.get(i)[1] };
			examples.add(tmt);
			xHashMap = new HashMap<String, String[]>();
			ProgSynthesis psProgSynthesis = new ProgSynthesis();
			psProgSynthesis.inite(examples, dpp, msger);
			Vector<ProgramRule> pls = new Vector<ProgramRule>();
			long t1 = System.currentTimeMillis();
			Collection<ProgramRule> ps = psProgSynthesis.adaptive_main();
			long span = System.currentTimeMillis()-t1;
			timeres += span+",";
			// collect history contraints
			msger.updateCM_Constr(psProgSynthesis.partiCluster.getConstraints());
			msger.updateWeights(psProgSynthesis.partiCluster.weights);
			i++;
			// constraints.addAll();
			if (ps != null) {
				pls.addAll(ps);
				System.out.println("program: "+ps);
			} else {
				System.out.println("Cannot find any rule");
			}
			if (pls.size() == 0)
				break;
		}
		return timeres;
	}

	public static void test3(String dirpath) {
		HashMap<String, Vector<String>> records = new HashMap<String, Vector<String>>();
		File nf = new File(dirpath);
		File[] allfiles = nf.listFiles();
		// statistics
		DataCollection dCollection = new DataCollection();
		// list all the csv file under the dir
		for (File f : allfiles) {
			Vector<String[]> examples = new Vector<String[]>();
			Vector<String[]> addExamples = new Vector<String[]>();
			Vector<String[]> entries = new Vector<String[]>();
			try {
				if (f.getName().indexOf(".csv") != -1
						&& f.getName().indexOf(".csv") == (f.getName().length() - 4)) {
					// MyLogger.logsth("========"+f.getName()+"============\n");
					HashMap<String, String[]> xHashMap = new HashMap<String, String[]>();
					@SuppressWarnings("resource")
					CSVReader cr = new CSVReader(new FileReader(f), ',', '"',
							'\0');
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
						continue;
					ExampleSelection expsel = new ExampleSelection();
					ExampleSelection.firsttime = true;
					expsel.inite(xHashMap, null);
					int target = Integer.parseInt(expsel.Choose());
					String[] mt = {
							"<_START>" + entries.get(target)[0] + "<_END>",
							entries.get(target)[1] };
					examples.add(mt);
					ExampleSelection.firsttime = false;
					// accuracy record code
					ArrayList<double[]> accArrayList = new ArrayList<double[]>();

					while (true) // repeat as no incorrect answer appears.
					{
						long checknumber = 1;
						long iterAfterNoFatalError = 0;
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
						Collection<ProgramRule> ps = psProgSynthesis
								.adaptive_main();
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
								String classlabel = script
										.getClassForValue(entries.get(j)[0]);
								String tmps = worker.execute_debug(entries
										.get(j)[0]);
								HashMap<String, String> dict = new HashMap<String, String>();
								dict.put("class", classlabel);
								UtilTools.StringColorCode(entries.get(j)[0],
										tmps, dict);
								String s = dict.get("Tar");
								if (Test.isExample(entries.get(j)[0], examples)) {
									s = entries.get(j)[1];
								}
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
									if (s == null
											|| s.length() == 0
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
											&& tmpx[1].compareTo(dict
													.get("Tar")) == 0) {
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
						System.out.println(""
								+ psProgSynthesis.myprog.toString());
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
								if (xHashMap.get("" + e)[4].compareTo("right") != 0) {
									System.out
									.println("Recommand Example: "
											+ Arrays.toString(xHashMap
													.get("" + e)));
									wexp[0] = "<_START>" + entries.get(e)[0]
											+ "<_END>";
									wexp[1] = entries.get(e)[1];
									if (expsel.isDetectingQuestionableRecord) {
										iterAfterNoFatalError++;
										// check whether this record is has the
										// longest or shortest result
										Boolean v = visible(xHashMap, "" + e);
									}
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
									checknumber, iterAfterNoFatalError,
									psProgSynthesis.myprog.partitions.size(),
									pls.get(0).toString(), resultacc, clf_acc);
							dCollection.addEntry(fileStat);
						} else {
							FileStat fileStat = new FileStat(f.getName() + "",
									psProgSynthesis.learnspan,
									psProgSynthesis.genspan, (t2 - t1),
									examples.size(), examples,
									psProgSynthesis.partiCluster.failedCnt,
									checknumber, iterAfterNoFatalError,
									psProgSynthesis.myprog.partitions.size(),
									pls.get(0).toString(), resultacc, clf_acc);
							dCollection.addEntry(fileStat);
							dCollection.addSucceededFile(f.getName());
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
		Prober.displayProgram();
		//
	}

	public static void test4(String dirpath) {
		HashMap<String, Vector<String>> records = new HashMap<String, Vector<String>>();
		File nf = new File(dirpath);
		File[] allfiles = nf.listFiles();
		// statistics
		DataCollection dCollection = new DataCollection();
		RecordClassifier rcf;
		// list all the csv file under the dir
		for (File f : allfiles) {
			Vector<String[]> examples = new Vector<String[]>();
			Vector<String[]> addExamples = new Vector<String[]>();
			Vector<String[]> entries = new Vector<String[]>();
			try {
				if (f.getName().indexOf(".csv") != -1
						&& f.getName().indexOf(".csv") == (f.getName().length() - 4)) {
					// MyLogger.logsth("========"+f.getName()+"============\n");
					HashMap<String, String[]> xHashMap = new HashMap<String, String[]>();
					@SuppressWarnings("resource")
					CSVReader cr = new CSVReader(new FileReader(f), ',', '"',
							'\0');
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
						continue;
					ExampleSelection expsel = new ExampleSelection();
					ExampleSelection.firsttime = true;
					expsel.inite(xHashMap, null);
					int target = Integer.parseInt(expsel.Choose());
					String[] mt = {
							"<_START>" + entries.get(target)[0] + "<_END>",
							entries.get(target)[1] };
					examples.add(mt);
					ExampleSelection.firsttime = false;
					// accuracy record code
					ArrayList<double[]> accArrayList = new ArrayList<double[]>();
					long stime = System.currentTimeMillis();
					boolean overtime = true;
					while (true) // repeat as no incorrect answer appears.
					{
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
						
						rcf = (RecordClassifier) psProgSynthesis.classifier;
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
								String classlabel = script
										.getClassForValue(entries.get(j)[0]);
								String tmps = worker.execute_debug(entries
										.get(j)[0]);
								
								HashMap<String, String> dict = new HashMap<String, String>();
								dict.put("class", classlabel);
								UtilTools.StringColorCode(entries.get(j)[0],
										tmps, dict);
								String s = worker.execute(entries.get(j)[0]);
								dict.put("Tar", s);
								if (Test.isExample(entries.get(j)[0], examples)) {
									s = entries.get(j)[1];
								}
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
									if (s == null
											|| s.length() == 0
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
											&& tmpx[1].compareTo(dict
													.get("Tar")) == 0) {
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
						System.out.println(""
								+ psProgSynthesis.myprog.toString());
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
								System.out
										.println("Recommand Example: "
												+ Arrays.toString(xHashMap
														.get("" + e)));
								// /
								if (xHashMap.get("" + e)[4].compareTo("right") != 0) {
									wexp[0] = "<_START>" + entries.get(e)[0]
											+ "<_END>";
									wexp[1] = entries.get(e)[1];
									if (expsel.isDetectingQuestionableRecord) {
										iterAfterNoFatalError++;
										// check whether this record is has the
										// longest or shortest result
										Boolean v = visible(xHashMap, "" + e);
										if (v) {
											isvisible += 1;
										}
									}
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
									checknumber, iterAfterNoFatalError,
									psProgSynthesis.myprog.partitions.size(),
									pls.get(0).toString(), resultacc, clf_acc);
							dCollection.addEntry(fileStat);
						} else {
							FileStat fileStat = new FileStat(f.getName() + "",
									psProgSynthesis.learnspan,
									psProgSynthesis.genspan, (t2 - t1),
									examples.size(), examples,
									psProgSynthesis.partiCluster.failedCnt,
									checknumber, iterAfterNoFatalError,
									psProgSynthesis.myprog.partitions.size(),
									pls.get(0).toString(), resultacc, clf_acc);
							dCollection.addEntry(fileStat);
							dCollection.addSucceededFile(f.getName());
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
		Prober.displayProgram();
	}

	public static boolean isExample(String var, Vector<String[]> examples) {
		boolean is = false;
		String s = "<_START>" + var + "<_END>";
		for (String[] x : examples) {
			if (s.compareTo(x[0]) == 0) {
				is = true;
				break;
			}
		}
		return is;
	}

	public static void hashResultPrint(HashMap<String, Vector<String>> res) {
		String s = "";
		for (String key : res.keySet()) {
			s += "==============" + key + "=============\n";
			for (String value : res.get(key)) {
				s += value + "\n";
			}
		}
		System.out.println("" + s);
	}

	public static void runSeriseExper() {
		EmailNotification notification = new EmailNotification();
		ExampleCluster.option = method.CP;
		Test.test4("/Users/bowu/Research/testdata/TestSingleFile");
		notification.notify(true,"CP");
		
		ExampleCluster.option = method.DPIC;
		Test.test4("/Users/bowu/Research/testdata/TestSingleFile");
		notification.notify(true,"DPIC");


		ExampleCluster.option = method.DP;
		Test.test4("/Users/bowu/Research/testdata/TestSingleFile");
		notification.notify(true,"DP");

		ExampleCluster.option = method.SPIC;
		Test.test4("/Users/bowu/Research/testdata/TestSingleFile");
		notification.notify(true,"SPIC");

		ExampleCluster.option = method.SP;
		Test.test4("/Users/bowu/Research/testdata/TestSingleFile");
		notification.notify(true,"SP");

		ExampleCluster.option = method.CPIC;
		Test.test4("/Users/bowu/Research/testdata/TestSingleFile");
		notification.notify(true,"CPIC");

		

		
	}

	public static int MaximalNumber = -1;
	public static int MinimalNumber = 100;
	public static Vector<String[]> larexamples = new Vector<String[]>();
	public static Vector<String[]> smalexamples = new Vector<String[]>();

	public static void main(String[] args) {
		// load parameters
		ConfigParameters cfg = new ConfigParameters();
		cfg.initeParameters();
		DataCollection.config = cfg.getString();
		Test.test4("/Users/bowu/Research/testdata/TestSingleFile");
		//Test.test3("/Users/bowu/Research/testdata/TestSingleFile");
		 //Test.runSeriseExper();
		// Test test = new Test();
		// test.parameterSelection("/Users/bowu/Research/testdata/TestSingleFile");
	}
}