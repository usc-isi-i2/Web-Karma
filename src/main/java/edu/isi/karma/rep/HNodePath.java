/**
 * 
 */
package edu.isi.karma.rep;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author szekely
 * 
 */
public class HNodePath {

	private List<HNode> hNodes = new LinkedList<HNode>();

	public HNodePath() {
	}

	public HNodePath(HNode hNode) {
		hNodes.add(hNode);
	}

	public HNodePath(List<HNode> hNodes) {
		this.hNodes = hNodes;
	}

	public HNodePath(HNodePath path) {
		for (HNode hn : path.hNodes) {
			this.hNodes.add(hn);
		}
	}

	/** 
	 * Adds an HNode to this path.
	 * @param n
	 * @author mariam
	 */
	public void addHNode(HNode n){
		hNodes.add(n);
	}
	
	/** 
	 * Adds an HNodePath to this path.
	 * @param np
	 * @author mariam
	 */
	public void addHNodePath(HNodePath np){
		hNodes.addAll(np.hNodes);
	}

	public boolean isEmpty() {
		return hNodes.isEmpty();
	}
	
	public HNode getFirst() {
		return hNodes.get(0);
	}

	public HNode getLeaf() {
		return hNodes.get(hNodes.size() - 1);
	}

	public HNodePath getRest() {
		return new HNodePath(hNodes.subList(1, hNodes.size()));
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		Iterator<HNode> it = hNodes.iterator();
		while (it.hasNext()) {
			b.append(it.next().getId());
			if (it.hasNext()){
				b.append("/");
			}
		}
		return b.toString();
	}

	//mariam
	/**
	 * Returns the HNodePath as a String containing the column name of the child node at the end.
	 * @return
	 * 		the HNodePath as a String containing the column name of the child node at the end.
	 * Example:
	 * HN1/HN2/ColumnName
	 */
	public String toColumnNames() {
		String path = toString() + "/";
		//get the last node
		HNode n = hNodes.get(hNodes.size()-1);
		path += n.getColumnName();
		return path;
	}
	
	/*
	public String toColumnNames(RepFactory f) {
		StringBuffer b = new StringBuffer();
		Iterator<HNode> it = hNodes.iterator();
		while (it.hasNext()) {
			HNode n = it.next();
			HTable t = n.getHTable(f);
			b.append(t.getTableName()+ "." + n.getColumnName());
			if (it.hasNext()){
				b.append("/");
			}
		}
		return b.toString();
	}
	 */
	public static HNodePath concatenate(HNodePath prefix, HNodePath suffix) {
		HNodePath result = new HNodePath(prefix);
		for (HNode hn : suffix.hNodes){
			result.hNodes.add(hn);
		}
		return result;
	}
}
