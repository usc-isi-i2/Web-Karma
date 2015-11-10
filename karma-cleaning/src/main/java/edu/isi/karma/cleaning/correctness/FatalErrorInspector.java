package edu.isi.karma.cleaning.correctness;

import edu.isi.karma.cleaning.DataRecord;

public class FatalErrorInspector implements Inspector {

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public double getActionLabel(DataRecord record) {
		if(record.transformed.indexOf("_FATAL_ERROR_")!= -1){
			String[] tmp = record.transformed.split("((?<=_\\d_FATAL_ERROR_)|(?=_\\d_FATAL_ERROR_))");
			double ret = 0.0;
			for (String tmpstring : tmp) {
				int errnum = 0;
				if (tmpstring.indexOf("_FATAL_ERROR_") == -1) {
					continue;
				}
				errnum = Integer.valueOf(tmpstring.substring(1, 2));
				ret -= errnum;
			}
			return ret;
		}
		else{
			return 1;
		}

	}

}
