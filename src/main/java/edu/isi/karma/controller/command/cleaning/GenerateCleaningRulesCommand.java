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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.cleaning.RuleUtil;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.CleaningResultUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.cleaning.*;
import edu.isi.karma.view.VWorkspace;

public class GenerateCleaningRulesCommand extends WorksheetCommand {
	final String hNodeId;
	private Vector<TransformationExample> examples;
	RamblerTransformationInputs inputs;

	public GenerateCleaningRulesCommand(String id, String worksheetId, String hNodeId, String examples) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.examples = this.parseExample(examples);
		
	}
	public Vector<TransformationExample> parseExample(String example)
	{
		Vector<TransformationExample> x = new Vector<TransformationExample>();
		try
		{
			JSONArray jsa = new JSONArray(example);
			for(int i=0;i<jsa.length();i++)
			{
				String[] ary = new String[3];
				JSONObject jo = (JSONObject) jsa.get(i);
				String nodeid = (String)jo.get("nodeId");
				String before = (String)jo.getString("before");
				String after = (String)jo.getString("after");
				ary[0] = nodeid;
				ary[1] = "%"+before+"@";
				ary[2] = after;
				TransformationExample re = new RamblerTransformationExample(ary[1], ary[2], ary[0]);
				x.add(re);
			}
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
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
		return CommandType.undoable;
	}
	
	private Vector<ValueCollection> getTopK(RamblerTransformationOutput rtf,int k)
	{
		Iterator<String> iter = rtf.getTransformations().keySet().iterator();
		Vector<ValueCollection> vvc = new Vector<ValueCollection>();
		int index = 0;
		while(iter.hasNext() && index<k)
		{
			ValueCollection rvco = rtf.getTransformedValues(iter.next());
			vvc.add(rvco);
			System.out.println(rvco.getJson());
			index ++;
		}
		return vvc;
	}
	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Worksheet wk = vWorkspace.getRepFactory().getWorksheet(worksheetId);
		// Get the HNode
		HNodePath selectedPath = null;
		List<HNodePath> columnPaths = wk.getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(hNodeId)) {
				selectedPath = path;
			}
		}
		Collection<Node> nodes = new ArrayList<Node>();
		wk.getDataTable().collectNodes(selectedPath, nodes);
		HashMap<String, String> rows = new HashMap<String,String>();
		//obtain original rows
		for (Node node : nodes) {
			String id = node.getId();
			String originalVal = "%"+node.getValue().asString()+"@";
			//System.out.println(id+","+originalVal);
			rows.put(id, originalVal);
		}
		RamblerValueCollection vc = new RamblerValueCollection(rows);
		inputs = new RamblerTransformationInputs(examples, vc);
		//generate the program
		RamblerTransformationOutput rtf = new RamblerTransformationOutput(inputs);
		Vector<ValueCollection> vvc = getTopK(rtf, 3);
		CleaningResultUpdate cru = new CleaningResultUpdate(this.id,this.worksheetId,this.hNodeId);
		for(ValueCollection v:vvc)
		{
			cru.addValueCollection(v);
		}
		return new UpdateContainer(cru);
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
