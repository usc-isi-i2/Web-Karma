package edu.isi.karma.rep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import edu.isi.karma.controller.command.selection.SuperSelectionManager;

public class HashValueManager {
	private static Map<String, HashMap<String, String>> hashTable = new HashMap<String, HashMap<String, String>>();

	public HashValueManager() {
		// TODO Auto-generated constructor stub
	}
	
	private static void computeHashValue(Row row, List<String> HNodeIds) {
		for (String HNodeid : HNodeIds) {
			Node n = row.getNode(HNodeid);
			if (n.hasNestedTable()) {
				Table nestedTable = n.getNestedTable();
				for (Row nestedRow : nestedTable.getRows(0, nestedTable.getNumRows(), SuperSelectionManager.DEFAULT_SELECTION)) {
					List<String> ids = new ArrayList<String>();
					for (Node node : nestedRow.getNodes()) {
						ids.add(node.getHNodeId());
					}
					computeHashValue(nestedRow, ids);
				}
				//System.out.println("CellValue:" + v.asString());
			}
			else {
				//System.out.println("CellValue:" + n.getValue().asString());
				HashMap<String, String> tmp = hashTable.get(row.getId());
				if (tmp == null) 
					tmp = new HashMap<String, String>();
				String value = n.getValue().asString();
				value = DigestUtils.shaHex(value);
				tmp.put(n.getId(), value);
				hashTable.put(row.getId(), tmp);
			}
		}
		
	}
	
	public static String getHashValue(Row row, List<String> HNodeIds) {
		computeHashValue(row, HNodeIds);
		return getHashValueRecurse(row, HNodeIds);
	}
	
	private static String getHashValueRecurse(Row row, List<String> HNodeIds) {
		List<String> hashString = new ArrayList<String>();
		for (String HNodeid : HNodeIds) {
			Node n = row.getNode(HNodeid);
			if (n.hasNestedTable()) {
				Table nestedTable = n.getNestedTable();
				for (Row nestedRow : nestedTable.getRows(0, nestedTable.getNumRows(), SuperSelectionManager.DEFAULT_SELECTION)) {
					List<String> ids = new ArrayList<String>();
					for (Node node : nestedRow.getNodes()) {
						ids.add(node.getHNodeId());
					}
					hashString.add(getHashValue(nestedRow, ids));				
				}
			}
			else {
				HashMap<String, String> tmp = hashTable.get(row.getId());
				
				hashString.add(tmp.get(n.getId()));
			}
		}
		Collections.sort(hashString);
		String hash = "";
		for (String t : hashString) {
			hash += t; 
		}
		//System.out.println(row.getId() + ": " + hash);
		return hash;
	}
	
	public static void purgeHashTable() {
		hashTable.clear();
	}
	
	public static String getHashValue(Worksheet worksheet, String NodeId, RepFactory factory) {
		Node node = factory.getNode(NodeId);
		if (node != null) {
			return DigestUtils.shaHex(node.getValue().asString());
		}
		return null;
	}

}
