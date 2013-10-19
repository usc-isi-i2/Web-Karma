/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.controller.command.cleaning;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.json.JSONArray;
import org.json.JSONObject;

import au.com.bytecode.opencsv.CSVReader;
import edu.isi.karma.cleaning.ConfigParameters;
import edu.isi.karma.cleaning.DataCollection;
import edu.isi.karma.cleaning.ExampleSelection;
import edu.isi.karma.cleaning.Ruler;
import edu.isi.karma.cleaning.TNode;
import edu.isi.karma.cleaning.UtilTools;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.CleaningResultUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.cleaning.RamblerTransformationExample;
import edu.isi.karma.rep.cleaning.RamblerTransformationInputs;
import edu.isi.karma.rep.cleaning.RamblerTransformationOutput;
import edu.isi.karma.rep.cleaning.RamblerValueCollection;
import edu.isi.karma.rep.cleaning.TransformationExample;
import edu.isi.karma.rep.cleaning.ValueCollection;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class GenerateCleaningRulesCommand extends WorksheetCommand {
	final String hNodeId;
	private Vector<TransformationExample> examples;
	private HashSet<String> nodeIds = new HashSet<String>();
	RamblerTransformationInputs inputs;
	public String compResultString = "";

	public GenerateCleaningRulesCommand(String id, String worksheetId,
			String hNodeId, String examples, String cellIDs) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.nodeIds = parseNodeIds(cellIDs);
		ConfigParameters cfg = new ConfigParameters();
		cfg.initeParameters();
		DataCollection.config = cfg.getString();
		this.examples = parseExample(examples);
		////log info
		try
		{
			FileAppender appender = new FileAppender(new SimpleLayout(),"./log/cleanning.log");
			logger.addAppender(appender);
		}
		catch (Exception e) {
		}

	}

	private HashSet<String> parseNodeIds(String Ids) {
		HashSet<String> tSet = new HashSet<String>();
		try {
			JSONArray jsa = new JSONArray(Ids);
			for (int i = 0; i < jsa.length(); i++) {
				tSet.add(jsa.getString(i));
			}

		} catch (Exception e) {
			System.out.println("" + e.toString());
		}
		return tSet;
	}

	public static Vector<TransformationExample> parseExample(String example) {
		Vector<TransformationExample> x = new Vector<TransformationExample>();
		try {
			JSONArray jsa = new JSONArray(example);
			for (int i = 0; i < jsa.length(); i++) {
				String[] ary = new String[3];
				JSONObject jo = (JSONObject) jsa.get(i);
				String nodeid = (String) jo.get("nodeId");
				String before = (String) jo.getString("before");
				String after = (String) jo.getString("after");
				ary[0] = nodeid;
				ary[1] = "<_START>" + before + "<_END>";
				ary[2] = after;
				TransformationExample re = new RamblerTransformationExample(
						ary[1], ary[2], ary[0]);
				x.add(re);
			}
		} catch (Exception ex) {
			System.out.println("" + ex.toString());
		}
		return x;
	}

	private String getBestExample(HashMap<String, String[]> xHashMap,
			HashMap<String, Vector<String[]>> expFeData) {
		String ID = "";
		ExampleSelection es = new ExampleSelection();
		es.inite(xHashMap, expFeData);
		return es.Choose();
	}

/*	private static Vector<String> getTopK(Set<String> res, int k, String cmpres) {
		String dirpathString = ServletContextParameterMap
				.getParameterValue(ContextParameter.USER_DIRECTORY_PATH);
		if (dirpathString.compareTo("") == 0) {
			dirpathString = "./src/main/webapp/";
		}

		String trainPath = dirpathString + "grammar/features.arff";
		//
		String[] x = (String[]) res.toArray(new String[res.size()]);
		System.out.println("" + x);
		// Vector<Double> scores = UtilTools.getScores(x, trainPath);
		Vector<Double> scores = UtilTools.getScores2(x, cmpres);
		System.out.println("Scores: " + scores);
		Vector<Integer> ins = UtilTools.topKindexs(scores, k);
		System.out.println("Indexs: " + ins);
		Vector<String> y = new Vector<String>();
		for (int i = 0; i < k && i < ins.size(); i++) {
			y.add(x[ins.get(i)]);
		}
		return y;
	}*/

	@Override
	public String getCommandName() {
		return GenerateCleaningRulesCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Generate Cleaning Rules";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	public void StringColorCode(String org, String res,
			HashMap<String, String> dict) {
		int segmentCnt = 0;
		Vector<int[]> allUpdates = new Vector<int[]>();
		String pat = "((?<=\\{_L\\})|(?=\\{_L\\}))";
		String pat1 = "((?<=\\{_S\\})|(?=\\{_S\\}))";
		String orgdis = "";
		String tardis = "";
		String tar = "";
		String[] st = res.split(pat);
		boolean inloop = false;
		for (String token : st) {
			if (token.compareTo("{_L}") == 0 && !inloop) {
				inloop = true;
				continue;
			}
			if (token.compareTo("{_L}") == 0 && inloop) {
				inloop = false;
				continue;
			}
			String[] st1 = token.split(pat1);
			for (String str : st1) {
				if (str.compareTo("{_S}") == 0 || str.compareTo("{_S}") == 0) {
					continue;
				}
				if (str.indexOf("{_C}") != -1) {
					String[] pos = str.split("\\{_C\\}");
					int[] poses = { Integer.valueOf(pos[0]),
							Integer.valueOf(pos[1]),segmentCnt};
					boolean findPos = false;
					for (int i = 0; i < allUpdates.size(); i++) {
						int[] cur = allUpdates.get(i);
						if (poses[0] <= cur[0]) {
							findPos = true;
							allUpdates.add(i, poses);
							break; // avoid infinite adding
						}
					}
					if(!findPos)
					{
						allUpdates.add(poses);
					}
					String tarseg = org.substring(Integer.valueOf(pos[0]),
							Integer.valueOf(pos[1]));

					if (inloop) {

						tardis += String.format(
								"<span class=\"a%d\">%s</span>", segmentCnt,
								tarseg);
						// orgdis +=
						// String.format("<span class=\"a%d\">%s</span>",
						// segmentCnt,tarseg);
						tar += tarseg;
					} else {
						tardis += String.format(
								"<span class=\"a%d\">%s</span>", segmentCnt,
								tarseg);
						// orgdis +=
						// String.format("<span class=\"a%d\">%s</span>",
						// segmentCnt,tarseg);
						segmentCnt++;
						tar += tarseg;
					}

				} else {
					tardis += String.format("<span class=\"ins\">%s</span>",
							str);
					tar += str;
				}
			}
		}
		int pre = 0;
		for(int[] update:allUpdates)
		{
			if(update[0] >= pre)
			{
				orgdis += org.substring(pre,update[0]);
				orgdis += String.format(
						"<span class=\"a%d\">%s</span>", update[2],
						org.substring(update[0],update[1]));
				pre = update[1];
			}
		}
		if(org.length() > pre)
			orgdis += org.substring(pre);
		dict.put("Org", org);
		dict.put("Tar", tar);
		dict.put("Orgdis", orgdis);
		dict.put("Tardis", tardis);
	}
	private static Logger logger = Logger.getLogger(GenerateCleaningRulesCommand.class);
	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Worksheet wk = vWorkspace.getRepFactory().getWorksheet(worksheetId);
		String Msg = String.format("Gen rule start,Time:%d, Worksheet:%s",System.currentTimeMillis(),worksheetId);
		logger.info(Msg);
		// Get the HNode
		HashMap<String, String> rows = new HashMap<String, String>();
		HashMap<String, Integer> amb = new HashMap<String, Integer>();
		boolean firstCol = true;
		HNodePath selectedPath = null;
		List<HNodePath> columnPaths = wk.getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(hNodeId)) {
				selectedPath = path;
			}
		}
		Collection<Node> nodes = new ArrayList<Node>();
		wk.getDataTable().collectNodes(selectedPath, nodes);
		for (Node node : nodes) {
			String id = node.getId();
			if (!this.nodeIds.contains(id))
				continue;
			String originalVal = node.getValue().asString();
			rows.put(id, originalVal);
			this.compResultString += originalVal + "\n";
			calAmbScore(id, originalVal, amb);
		}
		RamblerValueCollection vc = new RamblerValueCollection(rows);
		HashMap<String, Vector<String[]>> expFeData = new HashMap<String, Vector<String[]>>();
		inputs = new RamblerTransformationInputs(examples, vc);
		// generate the program
		boolean results = false;
		int iterNum = 0;
		RamblerTransformationOutput rtf = null;

		long time1 = System.currentTimeMillis();
		while (iterNum < 1 && !results) // try to find an program within iterNum
		{
			rtf = new RamblerTransformationOutput(inputs);
			if (rtf.getTransformations().keySet().size() > 0) {
				results = true;
			}
			iterNum++;
		}
		long time2 = System.currentTimeMillis();
		Iterator<String> iter = rtf.getTransformations().keySet().iterator();
		long time6 = 0, time7 = 0;
		// id:{org: tar: orgdis: tardis: }
		HashMap<String, HashMap<String, String>> resdata = new HashMap<String, HashMap<String, String>>();
		HashSet<String> keys = new HashSet<String>();
		while (iter.hasNext()) {
			long _time5 = System.currentTimeMillis();
			String tpid = iter.next();
			ValueCollection rvco = rtf.getTransformedValues_debug(tpid);
			if (rvco == null)
				continue;
			long _time6 = System.currentTimeMillis();
			// constructing displaying data
			HashMap<String, String[]> xyzHashMap = new HashMap<String, String[]>();
			for (String key : rvco.getNodeIDs()) {
				HashMap<String, String> dict = new HashMap<String, String>();
				// add to the example selection
				boolean isExp = false;
				String org = vc.getValue(key);
				String classLabel = rvco.getClass(key);
				String pretar = rvco.getValue(key);
				String dummyValue = pretar;
				if(pretar.indexOf("_FATAL_ERROR_")!= -1)
				{
					dummyValue = org;
				}
				this.StringColorCode(org, dummyValue, dict);
				for (TransformationExample exp : examples) {
					if (exp.getNodeId().compareTo(key) == 0) {
						if (!expFeData.containsKey(classLabel)) {
							Vector<String[]> vstr = new Vector<String[]>();
							String[] texp = {dict.get("Org"), pretar};
							vstr.add(texp);
							expFeData.put(classLabel, vstr);
						} else {
							String[] texp = { dict.get("Org"), pretar };
							expFeData.get(classLabel).add(texp);
						}
						isExp = true;
					}
				}

				if (!isExp) {
					String[] pair = { dict.get("Org"), dict.get("Tar"), pretar,
							classLabel };
					xyzHashMap.put(key, pair);
				}
				resdata.put(key, dict);
			}
			if(!rtf.nullRule)
				keys.add(getBestExample(xyzHashMap, expFeData));
			long _time7 = System.currentTimeMillis();
			time6 += _time6 - _time5;
			time7 = _time7 - _time6;
		}
		// find the best row
		String vars = "";
		String expstr = "";
		String recmd = "";
		for(TransformationExample x:examples)
		{
			expstr += String.format("%s|%s", x.getBefore(),x.getAfter());
		}
		expstr += "|";
		if(rtf.nullRule)
		{
			keys.clear();
			//keys.add("-2"); // "-2 indicates null rule"
		}
		if(!resdata.isEmpty() && !rtf.nullRule)
		{
			recmd = resdata.get(keys.iterator().next()).get("Org");
		}
		else
		{
			recmd = ""; 
		}
		Msg = String.format("Gen rule end, Time:%d, Worksheet:%s,Examples:%s,Recmd:%s",System.currentTimeMillis(),worksheetId,expstr,recmd);
		logger.info(Msg);
		return new UpdateContainer(new CleaningResultUpdate(hNodeId, resdata,
				vars, keys));
	}

	public String getVarJSON(HashMap<String, HashSet<String>> values) {
		JSONObject jsobj = new JSONObject();
		try {
			for (String key : values.keySet()) {
				JSONArray jsonArray = new JSONArray();
				HashSet<String> vs = values.get(key);
				for (String v : vs)
					jsonArray.put(v);
				jsobj.put(key, jsonArray);
			}
		} catch (Exception e) {
			System.out.println("value generation error");
		}
		return jsobj.toString();
	}

	public void calAmbScore(String id, String org, HashMap<String, Integer> amb) {
		Ruler ruler = new Ruler();
		ruler.setNewInput(org);
		Vector<TNode> tNodes = ruler.vec;
		int tcnt = 1;
		for (int i = 0; i < tNodes.size(); i++) {
			if (tNodes.get(i).text.compareTo(" ") == 0)
				continue;
			for (int j = 0; j > i && j < tNodes.size(); j++) {
				if (tNodes.get(j).sameNode(tNodes.get(i))) {
					tcnt++;
				}
			}
		}
		amb.put(id, tcnt);
	}

	public void updateCandiScore(ValueCollection rvco,
			HashMap<String, HashMap<String, Integer>> values) {
		Iterator<String> ids = rvco.getNodeIDs().iterator();
		while (ids.hasNext()) {
			String id = ids.next();
			String value = rvco.getValue(id);
			HashMap<String, Integer> dict;
			if (values.containsKey(id)) {
				dict = values.get(id);
			} else {
				dict = new HashMap<String, Integer>();
				values.put(id, dict);
			}
			if (dict.containsKey(value)) {
				dict.put(value, dict.get(value) + 1);
			} else {
				dict.put(value, 1);
			}
		}
		return;
	}

	public HashMap<String, Double> getScore(HashMap<String, Integer> dicts,
			HashMap<String, HashMap<String, Integer>> values, boolean sw) {

		int topKsize = 1;
		if (sw)
			topKsize = Integer.MAX_VALUE;
		HashMap<String, Double> topK = new HashMap<String, Double>();
		Iterator<String> iditer = dicts.keySet().iterator();
		while (iditer.hasNext()) {
			String id = iditer.next();
			int amb = dicts.get(id);
			HashMap<String, Integer> hm = values.get(id);
			int div = 0;
			int squrecnt = 0;
			Iterator<String> iters = hm.keySet().iterator();
			while (iters.hasNext()) {
				String value = iters.next();
				squrecnt += Math.pow(hm.get(value), 2);
			}
			div = hm.keySet().size();
			// double entro = squrecnt*1.0/div;
			// double score = amb*1.0/entro;
			double score = div;
			if (topK.keySet().size() < topKsize && div > 1) {
				topK.put(id, score);
			} else {
				String[] keys = topK.keySet().toArray(
						new String[topK.keySet().size()]);
				for (String key : keys) {
					if (topK.get(key) < score) {
						topK.remove(key);
						topK.put(id, score);
					}
				}
			}
		}
		return topK;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
		String dirpath = "/Users/bowu/Research/testdata/TestSingleFile";
		File nf = new File(dirpath);
		File[] allfiles = nf.listFiles();
		for (File f : allfiles) {
			try {
				if (f.getName().indexOf(".csv") == (f.getName().length() - 4)) {

					CSVReader cr = new CSVReader(new FileReader(f), '\t');
					String[] pair;
					int isadded = 0;
					HashMap<String, String> tx = new HashMap<String, String>();
					int i = 0;
					Vector<TransformationExample> vrt = new Vector<TransformationExample>();
					while ((pair = cr.readNext()) != null) {

						pair[0] = "<_START>" + pair[0] + "<_END>";
						tx.put(i + "", pair[0]);
						if (isadded < 2) {
							RamblerTransformationExample tmp = new RamblerTransformationExample(
									pair[0], pair[1], i + "");
							vrt.add(tmp);
							isadded++;
						}
						i++;
					}
					RamblerValueCollection vc = new RamblerValueCollection(tx);
					RamblerTransformationInputs inputs = new RamblerTransformationInputs(
							vrt, vc);
					// generate the program
					RamblerTransformationOutput rtf = new RamblerTransformationOutput(
							inputs);
					HashMap<String, Vector<String>> js2tps = new HashMap<String, Vector<String>>();
					Iterator<String> iter = rtf.getTransformations().keySet()
							.iterator();
					Vector<ValueCollection> vvc = new Vector<ValueCollection>();
					while (iter.hasNext()) {
						String tpid = iter.next();
						ValueCollection rvco = rtf.getTransformedValues(tpid);
						vvc.add(rvco);
						String reps = rvco.getJson().toString();
						if (js2tps.containsKey(reps)) {
							js2tps.get(reps).add(tpid); // update the variance
														// dic
						} else {
							Vector<String> tps = new Vector<String>();
							tps.add(tpid);
							js2tps.put(reps, tps);
						}
					}
					// //////
					if (js2tps.keySet().size() == 0) {
						System.out.println("No Rules have been found");
						return;
					}
					for (String s : js2tps.keySet()) {
						System.out.println("" + s);
					}
				}
			} catch (Exception ex) {
				System.out.println("" + ex.toString());
			}
		}
	}
}
