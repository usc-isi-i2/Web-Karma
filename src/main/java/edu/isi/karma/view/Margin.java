/**
 * 
 */
package edu.isi.karma.view;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author szekely
 * 
 */
public class Margin {

	private final String hTableId;

	private final int depth;

	private static Margin rootMargin = new Margin("root", 0);
	private static Margin leafMargin = new Margin("leaf", -1);

	public boolean isRootMargin(Margin margin){
		return hTableId.equals("root");
	}
	
	public static Margin getRootMargin() {
		return rootMargin;
	}

	public static Margin getleafMargin() {
		return leafMargin;
	}

	public Margin(String hTableId, int depth) {
		super();
		this.hTableId = hTableId;
		this.depth = depth;
	}

	public String getHTableId() {
		return hTableId;
	}

	public int getDepth() {
		return depth;
	}

	public String toString() {
		return "m(" + depth + ":" + hTableId + ")";
	}
	
	public static String toString(Collection<Margin> marginList) {
		StringBuffer b = new StringBuffer();
		Iterator<Margin> it = marginList.iterator();
		while (it.hasNext()) {
			b.append(it.next().toString());
			if (it.hasNext()) {
				b.append("/");
			}
		}
		return b.toString();
	}
}
