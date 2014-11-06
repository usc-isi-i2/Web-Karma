package edu.isi.karma.mapreduce.inputformat;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapreduce.AvroRecordReaderBase;
import org.apache.hadoop.io.NullWritable;

public class AvroBatchRecordReader<T> extends AvroRecordReaderBase<Iterable<AvroKey<T>>, NullWritable, T>{

	private static final int batchSize = 1000;
	protected AvroBatchRecordReader(Schema readerSchema) {
		super(readerSchema);
	}
	List<AvroKey<T>> data = new LinkedList<AvroKey<T>>();

	@Override
	public Iterable<AvroKey<T>> getCurrentKey() throws IOException, InterruptedException {
		return data;
	}

	@Override
	public NullWritable getCurrentValue() throws IOException,
	InterruptedException {
		return NullWritable.get();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		data.clear();
		int i = 0;
		while (super.nextKeyValue()) {	
			T tmp = getCurrentRecord();
			if (tmp instanceof GenericData.Record) {
				data.add(new AvroKey<T>((T) new GenericData.Record((Record) tmp, true)));
			}
			i++;
			if (i == batchSize) {
				break;
			}
		}
		return (data.size() != 0);
	}

}
