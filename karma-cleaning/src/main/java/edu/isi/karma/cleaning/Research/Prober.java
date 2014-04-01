package edu.isi.karma.cleaning.Research;

import java.util.Vector;

import edu.isi.karma.cleaning.Partition;
import edu.isi.karma.cleaning.UtilTools;

public class Prober {
	public static String PartitionDisplay1(Vector<Partition> vp) {
		String res = "";
		for (Partition p : vp) {
			res += "Partition: " + p.label + "\n";
			for (int i = 0; i < p.tarNodes.size(); i++) {
				String line = UtilTools.print(p.orgNodes.get(i)) + "|"
						+ UtilTools.print(p.tarNodes.get(i));
				res += line + "\n";
			}
		}
		return res;
	}

}
