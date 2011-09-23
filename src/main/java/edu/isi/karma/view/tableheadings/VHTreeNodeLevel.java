/**
 * 
 */
package edu.isi.karma.view.tableheadings;

import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.cells;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

;

/**
 * @author szekely
 * 
 */
public class VHTreeNodeLevel {

	private final List<VHTreeNode> elements = new LinkedList<VHTreeNode>();
	private final int depth;

	VHTreeNodeLevel(int depth) {
		super();
		this.depth = depth;
	}

	VHTreeNodeLevel(VHTreeNode root) {
		super();
		this.elements.add(root);
		this.depth = 0;
	}

	int getDepth() {
		return depth;
	}

	boolean isFinalLevel() {
		for (VHTreeNode n : elements) {
			if (n.hasChildren()) {
				return false;
			}
		}
		return true;
	}

	VHTreeNodeLevel getNextLevel() {
		VHTreeNodeLevel nextLevel = new VHTreeNodeLevel(depth + 1);
		for (VHTreeNode n : elements) {
			if (n.hasChildren()) {
				nextLevel.elements.addAll(n.getChildren());
			} else {
				nextLevel.elements.add(n);
			}
		}
		return nextLevel;
	}

	void generateJson(JSONWriter jw, VWorksheet vWorksheet,
			VWorkspace vWorkspace) throws JSONException {
		jw.object()//
				.key(cells.name()).array();
		for (VHTreeNode n : elements) {
			n.generateJson(jw, depth, vWorksheet, vWorkspace);
		}
		jw.endArray();
		jw.endObject();
	}
}
