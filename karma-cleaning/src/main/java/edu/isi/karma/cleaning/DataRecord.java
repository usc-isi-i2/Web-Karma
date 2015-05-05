package edu.isi.karma.cleaning;

public class DataRecord {
	public static final String nonexist = "none";
	public static final String unassigned = "none";
	public String id = nonexist;
	public String origin = "";
	public String transformed = "";
	public String target = "";
	public String classLabel = unassigned;
	public String toString(){
		String ret = String.format("%s, %s, %s", id, origin, transformed);
		return ret;
	}
}
