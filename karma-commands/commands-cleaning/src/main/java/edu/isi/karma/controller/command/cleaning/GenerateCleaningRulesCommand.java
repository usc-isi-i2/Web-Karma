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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.cleaning.DataPreProcessor;
import edu.isi.karma.cleaning.ExampleSelection;
import edu.isi.karma.cleaning.Messager;
import edu.isi.karma.cleaning.Ruler;
import edu.isi.karma.cleaning.TNode;
import edu.isi.karma.cleaning.UtilTools;
import edu.isi.karma.cleaning.Research.ConfigParameters;
import edu.isi.karma.cleaning.Research.DataCollection;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.CleaningResultUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.cleaning.RamblerTransformationExample;
import edu.isi.karma.rep.cleaning.RamblerTransformationInputs;
import edu.isi.karma.rep.cleaning.RamblerTransformationOutput;
import edu.isi.karma.rep.cleaning.RamblerValueCollection;
import edu.isi.karma.rep.cleaning.TransformationExample;
import edu.isi.karma.rep.cleaning.ValueCollection;
public class GenerateCleaningRulesCommand extends WorksheetSelectionCommand {
	final String hNodeId;
	private Vector<TransformationExample> examples;
	private HashSet<String> nodeIds = new HashSet<String>();
	RamblerTransformationInputs inputs;
	public String compResultString = "";
	private static Logger logger = LoggerFactory.getLogger(GenerateCleaningRulesCommand.class);

	public GenerateCleaningRulesCommand(String id, String worksheetId,
			String hNodeId, String examples, String cellIDs, String selectionId) {
		super(id, worksheetId, selectionId);
		this.hNodeId = hNodeId;
		this.nodeIds = parseNodeIds(cellIDs);
		ConfigParameters cfg = new ConfigParameters();
		cfg.initeParameters();
		DataCollection.config = cfg.getString();
		this.examples = parseExample(examples);
	}

	private HashSet<String> parseNodeIds(String Ids) {
		HashSet<String> tSet = new HashSet<String>();
		try {
			JSONArray jsa = new JSONArray(Ids);
			for (int i = 0; i < jsa.length(); i++) {
				tSet.add(jsa.getString(i));
			}

		} catch (Exception e) {
			logger.error("" + e.toString());
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
			logger.error("" + ex.toString());
		}
		return x;
	}

	private String getBestExample(HashMap<String, String[]> xHashMap,
			HashMap<String, Vector<String[]>> expFeData) {
		ExampleSelection es = new ExampleSelection();
		es.inite(xHashMap, expFeData);
		return es.Choose();
	}
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

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet wk = workspace.getWorksheet(worksheetId);
		SuperSelection selection = getSuperSelection(wk);
		String msg = String.format("Gen rule start,Time,%d, Worksheet,%s",System.currentTimeMillis(),worksheetId);
		logger.info(msg);
		// Get the HNode
		HashMap<String, String> rows = new HashMap<String, String>();
		HashMap<String, Integer> amb = new HashMap<String, Integer>();
		HNodePath selectedPath = null;
		List<HNodePath> columnPaths = wk.getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(hNodeId)) {
				selectedPath = path;
			}
		}
		
		Collection<Node> nodes = new ArrayList<Node>();
		wk.getDataTable().collectNodes(selectedPath, nodes, selection);
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
		Messager mg = null;
		if(wk.getMsg()!= null)
		{
			mg = (Messager) wk.getMsg();
		}
		else
		{
			mg = new Messager();
			wk.setMsg(mg);
		}
		DataPreProcessor dp = null;
		if(wk.getDpp()!= null)
		{
			dp = (DataPreProcessor) wk.getDpp();
		}
		else
		{
			dp = new DataPreProcessor(rows.values());
			dp.run();
			wk.setDpp(dp);
		}
		inputs = new RamblerTransformationInputs(examples, vc,dp,mg);
		// generate the program
		boolean results = false;
		int iterNum = 0;
		RamblerTransformationOutput rtf = null;
		// initialize the vocabulary
		Iterator<String> iterx = inputs.getInputValues().getValues().iterator();
		Vector<String> v = new Vector<String>();
		int vb_cnt = 0;
		while(iterx.hasNext() && vb_cnt < 30)
		{
			String eString = iterx.next();
			v.add(eString);
			vb_cnt ++;
		}
		while (iterNum < 1 && !results) // try to find an program within iterNum
		{
			rtf = new RamblerTransformationOutput(inputs);
			if (rtf.getTransformations().keySet().size() > 0) {
				results = true;
			}
			iterNum++;
		}
		Iterator<String> iter = rtf.getTransformations().keySet().iterator();
		// id:{org: tar: orgdis: tardis: }
		HashMap<String, HashMap<String, String>> resdata = new HashMap<String, HashMap<String, String>>();
		HashSet<String> keys = new HashSet<String>();
		while (iter.hasNext()) {
			String tpid = iter.next();
			ValueCollection rvco = rtf.getTransformedValues_debug(tpid);
			if (rvco == null)
				continue;
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
					//dummyValue = "#ERROR";
				}
				try
				{
					UtilTools.StringColorCode(org, dummyValue, dict);
				}
				catch(Exception ex)
				{
					logger.info(String.format("ColorCoding Exception%s, %s", org,dummyValue));
					//set dict 
					dict.put("Org", org);
					dict.put("Tar", "ERROR");
					dict.put("Orgdis", org);
					dict.put("Tardis", "ERROR");
				}
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
		msg = String.format("Gen rule end, Time,%d, Worksheet,%s,Examples:%s,Recmd:%s",System.currentTimeMillis(),worksheetId,expstr,recmd);
		logger.info(msg);
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
			logger.error("value generation error");
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
			HashMap<String, Integer> hm = values.get(id);
			int div = 0;
			div = hm.keySet().size();
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
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

	/* save for future use
	 * public static void main(String[] args) {
	 *
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
						logger.debug("No Rules have been found");
						return;
					}
					for (String s : js2tps.keySet()) {
						logger.debug("" + s);
					}
				}
			} catch (Exception ex) {
				logger.error("" + ex.toString());
			}
		}
	}*/
}
