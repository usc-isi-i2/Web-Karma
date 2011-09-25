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

	public void generateJson(JSONWriter jw, VWorksheet vWorksheet,
			VWorkspace vWorkspace) {
		try {
			jw.object()
					.key(AbstractUpdate.GenericJsonKeys.updateType.name())
					.value(WorksheetHierarchicalHeadersUpdate.class
							.getSimpleName())
					//
					.key(worksheetId.name())
					.value(vWorksheet.getWorksheet().getId())//
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
		rootVHNode.prettyPrintJson(jw);
		jw.endObject();
		return jw;
	}
}
