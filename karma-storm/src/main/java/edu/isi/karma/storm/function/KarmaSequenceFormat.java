package edu.isi.karma.storm.function;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.storm.hdfs.bolt.format.SequenceFormat;

import backtype.storm.tuple.Tuple;

public class KarmaSequenceFormat implements SequenceFormat {

    private transient Text key;
    private transient Text value;

    private String keyField;
    private String valueField;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public KarmaSequenceFormat(String keyField, String valueField)
	{
		this.keyField = keyField;
		this.valueField = valueField;
	}
	
	@SuppressWarnings("rawtypes")
	@Override 
	public Class keyClass() {
		return Text.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class valueClass() {
		return Text.class;
	}
	
    @Override
    public Writable key(Tuple tuple) {
        if(this.key == null){
            this.key  = new Text();
        }
        this.key.set(tuple.getStringByField(this.keyField));
        return this.key;
    }

    @Override
    public Writable value(Tuple tuple) {
        if(this.value == null){
            this.value = new Text();
        }
        this.value.set(tuple.getStringByField(this.valueField));
        return this.value;
    }
}
