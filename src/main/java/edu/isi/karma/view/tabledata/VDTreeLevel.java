/**
 * 
 */
package edu.isi.karma.view.tabledata;

import java.util.LinkedList;
import java.util.List;

/**
 * Each level generates a row of the HTML table.
 * 
 * @author szekely
 *
 */
public class VDTreeLevel {

	private final List<VDTreeNode> elements = new LinkedList<VDTreeNode>();
	
	private final int depth;

	public VDTreeLevel(int depth) {
		super();
		this.depth = depth;
	}
	
	
}
