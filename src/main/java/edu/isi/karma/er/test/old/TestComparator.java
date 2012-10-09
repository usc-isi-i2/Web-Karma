package edu.isi.karma.er.test.old;

import org.json.JSONArray;

import edu.isi.karma.er.helper.ConfigUtil;

public class TestComparator {

	public static void main(String[] args) {
		
		ConfigUtil util = new ConfigUtil();
		JSONArray arr = util.loadConfig();
		
		int a1 = -1, a2 = 1928;
		int b1 = 1928, b2 = 2013;
		int c1 = 1975, c2 = 1974;
		int d1 = 1887, d2 = 1887;
		
		
	}
}
