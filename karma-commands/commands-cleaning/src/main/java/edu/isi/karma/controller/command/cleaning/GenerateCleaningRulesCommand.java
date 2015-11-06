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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.isi.karma.cleaning.DataPreProcessor;
import edu.isi.karma.cleaning.DataRecord;
import edu.isi.karma.cleaning.Messager;
import edu.isi.karma.cleaning.UtilTools;
import edu.isi.karma.cleaning.correctness.AdaInspector;
import edu.isi.karma.cleaning.correctness.FatalErrorInspector;
import edu.isi.karma.cleaning.correctness.Inspector;
import edu.isi.karma.cleaning.correctness.InspectorFactory;
import edu.isi.karma.cleaning.correctness.MultiviewInspector;
import edu.isi.karma.cleaning.research.ConfigParameters;
import edu.isi.karma.cleaning.research.DataCollection;
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
import edu.isi.karma.rep.cleaning.RamblerTransformation;
import edu.isi.karma.rep.cleaning.RamblerTransformationExample;
import edu.isi.karma.rep.cleaning.RamblerTransformationInputs;
import edu.isi.karma.rep.cleaning.RamblerTransformationOutput;
import edu.isi.karma.rep.cleaning.RamblerValueCollection;
import edu.isi.karma.rep.cleaning.Transformation;
import edu.isi.karma.rep.cleaning.TransformationExample;
import edu.isi.karma.rep.cleaning.ValueCollection;

public class GenerateCleaningRulesCommand extends WorksheetSelectionCommand {
	final String hNodeId;
	private int sample_cnt = 300;
	private int sample_size = 200;
	private int maximal_recommand_size = 150;
	private Vector<TransformationExample> examples;
	private HashSet<String> nodeIds = new HashSet<String>();
	RamblerTransformationInputs inputs;
	public String compResultString = "";
	private static Logger logger = LoggerFactory.getLogger(GenerateCleaningRulesCommand.class);

	public GenerateCleaningRulesCommand(String id, String model, String worksheetId, String hNodeId, String examples, String cellIDs, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.hNodeId = hNodeId;
		//this.nodeIds = parseNodeIds(cellIDs);
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
				TransformationExample re = new RamblerTransformationExample(ary[1], ary[2], ary[0]);
				x.add(re);
			}
		} catch (Exception ex) {
			logger.error("" + ex.toString());
		}
		return x;
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
		// Get the HNode
		HashMap<String, String> rows = new HashMap<String, String>();
		HNodePath selectedPath = null;
		List<HNodePath> columnPaths = wk.getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(hNodeId)) {
				selectedPath = path;
			}
		}

		HashSet<String> existed = new HashSet<String>();
		addExampleIDsintoExistedSet(existed);
		ArrayList<Node> nodes = new ArrayList<Node>();
		wk.getDataTable().collectNodes(selectedPath, nodes, selection);
		int cnt = 0;
		Random rchooser = new Random();
		while(cnt < sample_cnt && rows.size() < sample_size && rows.size() < nodes.size()) {
			cnt ++;
			int index = 	rchooser.nextInt(nodes.size()-1);
			Node node = nodes.get(index);
			String id = node.getId();
			if(existed.contains(id)){
				continue;
			}
			else{
				existed.add(id);
			}
			String originalVal = node.getValue().asString();
			rows.put(id, originalVal);
			this.compResultString += originalVal + "\n";
		}
		RamblerValueCollection vc = new RamblerValueCollection(rows);
		HashMap<String, Vector<String[]>> expFeData = new HashMap<String, Vector<String[]>>();
		Messager mg = null;
		if (wk.getMsg() != null) {
			mg = (Messager) wk.getMsg();
		} else {
			mg = new Messager();
			wk.setMsg(mg);
		}
		DataPreProcessor dp = null;
		if (wk.getDpp() != null) {
			dp = (DataPreProcessor) wk.getDpp();
		} else {
			dp = new DataPreProcessor(rows.values());
			dp.run();
			wk.setDpp(dp);
		}
		inputs = new RamblerTransformationInputs(examples, vc, dp, mg);
		// generate the program
		boolean results = false;
		int iterNum = 0;
		RamblerTransformationOutput rtf = null;
		while (iterNum < 1 && !results) // try to find a program within iterNum
		{
			rtf = new RamblerTransformationOutput(inputs);
			if (rtf.getTransformations().keySet().size() > 0) {
				results = true;
			}
			iterNum++;
		}
		Iterator<String> iter = rtf.getTransformations().keySet().iterator();
		// id:{"org": ,"tar": , "orgdis": ,"tardis": }
		HashMap<String, HashMap<String, String>> resdata = new HashMap<String, HashMap<String, String>>();
		String tpid = iter.next();
		ValueCollection rvco = rtf.getTransformedValues_debug(tpid);
		// constructing displaying data
		HashMap<String, String[]> xyzHashMap = new HashMap<String, String[]>();
		ArrayList<DataRecord> records = new ArrayList<DataRecord>();
		ArrayList<DataRecord> nonFatalError = new ArrayList<DataRecord>();
		ArrayList<DataRecord> fatalError = new ArrayList<DataRecord>();
		boolean existFatalError = false;
		for (String key : rvco.getNodeIDs()) {
			HashMap<String, String> dict = new HashMap<String, String>();
			// add to the example selection
			boolean isExp = false;
			String org = vc.getValue(key);
			String classLabel = rvco.getClass(key);
			String pretar = rvco.getValue(key);
			String dummyValue = pretar;
			if (pretar.indexOf("_FATAL_ERROR_") != -1) {
				dummyValue = org;
				existFatalError = true;
			}
			try {
				UtilTools.StringColorCode(org, dummyValue, dict);
			} catch (Exception ex) {
				logger.info(String.format("ColorCoding Exception%s, %s", org, dummyValue));
			}
			for (TransformationExample exp : examples) {
				if (exp.getNodeId().compareTo(key) == 0) {
					if (!expFeData.containsKey(classLabel)) {
						Vector<String[]> vstr = new Vector<String[]>();
						String[] texp = { dict.get("Org"), pretar };
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
				String[] pair = { dict.get("Org"), dict.get("Tar"), pretar, classLabel };
				xyzHashMap.put(key, pair);
				if (existFatalError && pretar.indexOf("_FATAL_ERROR_")!= -1) {
					DataRecord record = new DataRecord(key, pair[0], pretar, classLabel);
					fatalError.add(record);
				}
			}
			resdata.put(key, dict);
			if (!existFatalError) {
				DataRecord record = new DataRecord(key, org, dict.get("Tar"), classLabel);
				nonFatalError.add(record);
			}
		}
		if(existFatalError){
			records = fatalError;
		}
		else{
			records = nonFatalError;
		}
		List<String> keys = new ArrayList<String>();
		if (!existFatalError) {
			addExamplesIntoRecords(records, rtf.getTransformations().get(tpid));
			ArrayList<String> sortedIds = getRecommendedIDs(dp, mg, records, rtf, tpid);
			keys = sortedIds;
		} else {
			ArrayList<String> sortedIds = getRecommendedIDswithFatalError(records);
			keys = sortedIds;
		}
		HashSet<String> mintest = getMinimalTestSet(dp, mg, records, rtf, tpid, keys);
		mg.addMultiInterpreationRecords(mintest);
		double coverage = getTestCoverage(mintest, mg.allMultipleInterpretation);
		String vars = "";
		if (rtf.nullRule) {
			keys.clear();
		}
		//UserStudyUtil.logOneiteration(wk.getUserMonitor(), worksheetId, System.currentTimeMillis(), examples, keys, resdata);
		//logDiagnosticInfo(rtf, resdata, keys);
		return new UpdateContainer(new CleaningResultUpdate(hNodeId, resdata, coverage, keys, Lists.newArrayList(mintest)));
	}
	private void addExamplesIntoRecords(ArrayList<DataRecord> records, Transformation tf){
		for(TransformationExample exp: examples){
			DataRecord rec =  new DataRecord(exp.getNodeId(), exp.getBefore(), exp.getAfter(), tf.getClassLabel(exp.getBefore()));
			records.add(rec);
		}
	}
	private void logDiagnosticInfo(RamblerTransformationOutput rtf, HashMap<String, HashMap<String, String>> resdata, List<String> keys) {
		try {
			String expstr = "";
			String recmd = "";
			if (!resdata.isEmpty() && !rtf.nullRule && keys.size() != 0) {
				recmd = resdata.get(keys.iterator().next()).get("Org");
			} else {
				recmd = "";
			}
			for (TransformationExample x : examples) {
				expstr += String.format("%s|%s", x.getBefore(), x.getAfter());
			}
			expstr += "|";
			String msg = String.format("Gen rule end, Time,%d, Worksheet,%s,Examples:%s,Recmd:%s", System.currentTimeMillis(), worksheetId, expstr, recmd);
			logger.info(msg);
		} catch (Exception ex) {
			logger.info(ex.toString());
		}
	}

	private ArrayList<String> getRecommendedIDswithFatalError(ArrayList<DataRecord> records) {
		ArrayList<String> ret = new ArrayList<String>();
		PriorityQueue<DataRecord> sortedList = new PriorityQueue<DataRecord>();
		FatalErrorInspector inspector = new FatalErrorInspector();
		HashSet<String> existed = new HashSet<String>();
		HashSet<String> exampleInputs = getExampleInputs();
		for (DataRecord r : records) {
			if(existed.contains(r.origin) || exampleInputs.contains(r.origin)){
				continue;
			}
			existed.add(r.origin);
			double value = inspector.getActionLabel(r);
			r.value = value;
			sortedList.add(r);
		}
		while (!sortedList.isEmpty()) {
			DataRecord r = sortedList.poll();
			ret.add(r.id);
		}
		return ret;
	}
	private void addExampleIDsintoExistedSet(HashSet<String> existed){
		ArrayList<String> exampleIDs = getExampleIDs();
		for(String id: exampleIDs){
			if(!existed.contains(id))
				existed.add(id);
		}
	}
	private HashSet<String> getExampleInputs(){
		HashSet<String> ret = new HashSet<String>();
		String v = "";
		for(TransformationExample exp: examples){
			if(!ret.contains(exp.getBefore()))
				v = exp.getBefore();
				v = v.replace("<_START>", "");
				v = v.replace("<_END>", "");
				ret.add(v);
		}
		return ret;
		
	}
	private double getTestCoverage(HashSet<String> toTest, HashSet<String> all){

		if(all.size() == 0){
			return 100;
		}
		return (1- toTest.size() * 1.0 / all.size()) * 100;
	}
	private HashSet<String> getMinimalTestSet(DataPreProcessor dp, Messager mg, ArrayList<DataRecord> records, RamblerTransformationOutput rtf, String tpid,List<String> sortedIds){
		HashSet<String> tmp = new HashSet<String>();
		ArrayList<String> exampleIDs = getExampleIDs();
		RamblerTransformation rtransformation = (RamblerTransformation) rtf.getTransformations().get(tpid);
		InspectorFactory factory = new InspectorFactory(dp, mg, records, exampleIDs, rtransformation.prog);
		Inspector mvInspector = factory.getInspector(MultiviewInspector.class.getName());
		HashSet<String> existed = new HashSet<String>();
		for (DataRecord r : records) {
			double value = mvInspector.getActionLabel(r);
			if(value < 0){
				if(!tmp.contains(r.id) && !existed.contains(r.origin));
					tmp.add(r.id);
			}
		}
		HashSet<String> ret = new HashSet<String>();
		for(String key: sortedIds){
			if(tmp.contains(key)){
				ret.add(key);
			}
		}
		return ret;
	}
	private ArrayList<String> getRecommendedIDs(DataPreProcessor dp, Messager mg, ArrayList<DataRecord> records, RamblerTransformationOutput rtf, String tpid) {
		AdaInspector inspector = new AdaInspector();
		inspector.initeParameter();
		RamblerTransformation rtransformation = (RamblerTransformation) rtf.getTransformations().get(tpid);
		ArrayList<String> exampleIDs = getExampleIDs();
		HashSet<String> exampleInputs = getExampleInputs();
		HashSet<String> existed = new HashSet<String>();
		inspector.initeInspector(dp, mg, records, exampleIDs, rtransformation.prog);
		ArrayList<String> ret = new ArrayList<String>();
		PriorityQueue<DataRecord> sortQueue = new PriorityQueue<DataRecord>();
		for (DataRecord r : records) {
			double value = inspector.getActionScore(r);
			r.value = value;
			if (value < 0 &&!exampleInputs.contains(r.origin)&& !exampleIDs.contains(r.id) && ! existed.contains(r.origin) && sortQueue.size() < maximal_recommand_size) {
				sortQueue.add(r);
				existed.add(r.origin);
			}
		}
		while(!sortQueue.isEmpty()){
			ret.add(sortQueue.poll().id);
		}
		return ret;
	}

	private ArrayList<String> getExampleIDs() {
		ArrayList<String> ret = new ArrayList<String>();
		for (TransformationExample e : examples) {
			ret.add(e.getNodeId());
		}
		return ret;
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

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
