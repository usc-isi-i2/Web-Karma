package edu.isi.karma.mapreduce.driver;

import java.util.Iterator;

import org.apache.hadoop.io.Text;

public class TextToStringIterator implements Iterator<String>{

	private Iterator<Text> iteratorToWrap;
	public TextToStringIterator(Iterator<Text> iteratorToWrap)
	{
		this.iteratorToWrap = iteratorToWrap;
	}
	@Override
	public boolean hasNext() {
		return iteratorToWrap.hasNext();
	}

	@Override
	public String next() {
		return iteratorToWrap.next().toString();
	}

	@Override
	public void remove() {
		iteratorToWrap.remove();
	}

}
