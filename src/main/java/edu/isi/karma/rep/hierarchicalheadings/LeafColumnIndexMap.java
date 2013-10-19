package edu.isi.karma.rep.hierarchicalheadings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LeafColumnIndexMap {
	private HashMap<String, Integer> hNodeIdToIndexMap = new HashMap<String, Integer>();

	public LeafColumnIndexMap(HHTree tree) {
		List<HHTNode> list = new ArrayList<HHTNode>();
		for (HHTNode root : tree.getRootNodes()) {
			list.add(root);
		}
		
		while(!list.isEmpty()) {
			HHTNode node = list.remove(0);
			if(node.isLeaf()) {
				hNodeIdToIndexMap.put(node.gettNode().getId(), node.getStartCol());
			} else {
				list.addAll(node.getChildren());
			}
		}
		
//		System.out.println("Index Map:");
//		for(String key : hNodeIdToIndexMap.keySet()) {
//			System.out.println("HNode ID: " + key + " Index: " + hNodeIdToIndexMap.get(key));
//		}
	}
	
	public int getColumnIndex (String hNodeId) {
		return hNodeIdToIndexMap.get(hNodeId);
	}
}
