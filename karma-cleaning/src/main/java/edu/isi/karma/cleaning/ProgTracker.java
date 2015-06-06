package edu.isi.karma.cleaning;

import java.util.Map;
import java.util.Vector;

public class ProgTracker {
	public static void printPartition(Vector<Partition> pars) {
		System.out.println("-----------");
		System.out.println("CURRENT_PARS" + pars.toString());
		System.out.println("-----------");
	}

	public static void printConstraints(Vector<Vector<String[]>> constr) {
		String res = "";
		for (Vector<String[]> line : constr) {
			String s = "CONST: ";
			for (String[] p : line) {
				s += p[0] + ", " + p[1] + " |";
			}
			s += "\n";
			res += s;
		}
		System.out.println("" + res);
	}

	public static void printPartitions(Partition a, Partition b) {
		System.out.println("TOMERGE:" + a + "\n" + b);
	}

	@SuppressWarnings("rawtypes")
	public static void printUnlabeledData(Map dicttmp) {
		for (Object xkey : dicttmp.keySet()) {
			System.out.println(String.format("Entry: %s,%f", xkey,dicttmp.get(xkey)));
		}
	}
}
