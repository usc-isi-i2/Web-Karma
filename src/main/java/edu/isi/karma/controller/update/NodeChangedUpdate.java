/**
 * 
 */
package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import edu.isi.karma.rep.CellValue;
import edu.isi.karma.rep.Node;
import edu.isi.karma.util.Util;
import edu.isi.karma.view.VWorkspace;

/**
 * Provides information about a value update to a single node.
 * 
 * @author szekely
 * 
 */
public class NodeChangedUpdate extends AbstractUpdate {

	public enum JsonKeys {
		worksheet, nodeId, newValue, newStatus
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
		pw.println(newPref + Util.json(GenericJsonKeys.updateType, getUpdateType()));
		pw.println(newPref + Util.json(JsonKeys.worksheet, worksheetId));
		pw.println(newPref + Util.json(JsonKeys.nodeId, nodeId));
		pw.println(newPref + Util.json(JsonKeys.newStatus, newStatus.getCodedStatus()));
		pw.println(newPref + Util.jsonLast(JsonKeys.newValue, newValue.asString()));
		pw.println(prefix + "}");
	}
}
