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
package edu.isi.karma.view.tableheadings;

import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.rows;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.worksheetId;

import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.tabledata.VDVerticalSeparator;
import edu.isi.karma.view.tabledata.VDVerticalSeparators;

/**
 * @author szekely
 * 
 */
public class VTableHeadings {

	@SuppressWarnings("unused")
	private List<HNodePath> headingHNodePaths;

	private String hTableId;

	private VHTreeNode rootVHNode;

	public VTableHeadings(List<HNodePath> headingHNodePaths, String hTableId) {
		super();
		this.headingHNodePaths = headingHNodePaths;
		this.hTableId = hTableId;
		this.rootVHNode = new VHTreeNode(hTableId);
		this.rootVHNode.addColumns(headingHNodePaths);
		this.rootVHNode.computeDerivedInformation();
	}

	public VHTreeNode getRootVHNode() {
		return rootVHNode;
	}

	public void populateVDVerticalSeparators(
			VDVerticalSeparators vdVerticalSeparators) {
		VDVerticalSeparator vs = new VDVerticalSeparator();
		vs.add(0, hTableId);
		vdVerticalSeparators.put(rootVHNode.getHNodeId(), vs);
		rootVHNode.populateVDVerticalSeparators(vdVerticalSeparators);
	}

	public void generateJson(JSONWriter jw, VWorksheet vWorksheet,
			VWorkspace vWorkspace) {
		try {
			jw.object()
					.key(AbstractUpdate.GenericJsonKeys.updateType.name())
					.value(WorksheetHierarchicalHeadersUpdate.class
							.getSimpleName())
					//
					.key(worksheetId.name())
					.value(vWorksheet.getId())//
					.key(WorksheetHierarchicalHeadersUpdate.JsonKeys.hTableId
							.name()).value(hTableId)//
					.key(rows.name());
			generateJsonRows(jw, vWorksheet, vWorkspace);
			jw.endObject();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void generateJsonRows(JSONWriter jw, VWorksheet vWorksheet,
			VWorkspace vWorkspace) throws JSONException {
		jw.array();
		VHTreeNodeLevel level = new VHTreeNodeLevel(rootVHNode).getNextLevel();
		while (!level.isFinalLevel()) {
			level.generateJson(jw, vWorksheet, vWorkspace);
			level = level.getNextLevel();
		}
		level.generateJson(jw, vWorksheet, vWorkspace);
		jw.endArray();
	}

	public JSONWriter prettyPrintJson(JSONWriter jw) throws JSONException {
		jw.object()//
				.key("hTableId").value(hTableId)//
				.key("root")//
		;
		rootVHNode.prettyPrintJson(jw, true, true);
		jw.endObject();
		return jw;
	}
}
