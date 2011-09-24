/**
 * 
 */
package edu.isi.karma.view.tabledata;

import java.util.LinkedList;
import java.util.List;

import edu.isi.karma.rep.Node;

/**
 * @author szekely
 *
 */
public class VDTreeNode {

	private final Node node;
	
	private final List<VDRow> nestedTableRows = new LinkedList<VDRow>();

	public VDTreeNode(Node node) {
		super();
		this.node = node;
	}
	
}
