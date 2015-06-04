package edu.isi.karma.cleaning.correctness;

import edu.isi.karma.cleaning.DataRecord;

public interface Inspector {
	public String getName();
	public double getActionLabel(DataRecord record);// -1 should be checked, 1 should not be checked
}
