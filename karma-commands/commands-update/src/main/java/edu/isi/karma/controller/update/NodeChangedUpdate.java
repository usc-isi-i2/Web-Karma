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
/**
 * 
 */
package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import edu.isi.karma.rep.CellValue;
import edu.isi.karma.rep.Node;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.ViewPreferences.ViewPreference;

/**
 * Provides information about a value update to a single node.
 * 
 * @author szekely
 * 
 */
public class NodeChangedUpdate extends AbstractUpdate {

	public enum JsonKeys {
		worksheet, nodeId, newStatus, displayValue, expandedValue
	}
	
	private final String worksheetId;

	private final String nodeId;

	private final CellValue newValue;
	
	private final Node.NodeStatus newStatus;

	public NodeChangedUpdate(String worksheetId, String nodeId,
			CellValue newValue, Node.NodeStatus newStatus) {
		super();
		this.worksheetId = worksheetId;
		this.nodeId = nodeId;
		this.newValue = newValue;
		this.newStatus = newStatus;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
		pw.println(prefix + "{");
		String newPref = prefix + "  ";
		pw.println(newPref + JSONUtil.json(GenericJsonKeys.updateType, getUpdateType()));
		pw.println(newPref + JSONUtil.json(JsonKeys.worksheet, worksheetId));
		pw.println(newPref + JSONUtil.json(JsonKeys.nodeId, nodeId));
		pw.println(newPref + JSONUtil.json(JsonKeys.newStatus, newStatus.getCodedStatus()));
		pw.println(newPref + JSONUtil.json(JsonKeys.expandedValue, newValue.asString()));
		
		String displayValueString = newValue.asString();
		int maxValueLength = vWorkspace.getPreferences().getIntViewPreferenceValue(
				ViewPreference.maxCharactersInCell);
		if(displayValueString.length() > maxValueLength) {
			displayValueString = JSONUtil.truncateCellValue(
					displayValueString,maxValueLength);
		}
		
		pw.println(newPref + JSONUtil.jsonLast(JsonKeys.displayValue, displayValueString));
		pw.println(prefix + "}");
	}
	
	public boolean equals(Object o) {
		if (o instanceof NodeChangedUpdate) {
			NodeChangedUpdate t = (NodeChangedUpdate)o;
			return t.worksheetId.equals(worksheetId) && t.nodeId.equals(nodeId);
		}
		return false;
	}
}
