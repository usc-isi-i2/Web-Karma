package edu.isi.karma.common;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Class JenaWritable
 *
 * @since 01/24/2014
 */
public class JenaWritable extends Writer
{
	private Model model;

	public JenaWritable(Model model)
	{
		this.model = model;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException
	{
//		boolean endsInPeriod = cbuf[off+len-1] == '.';

		String s1 = new String(cbuf, off, len);

		StringReader stringReader = new StringReader(s1);
		model.read(stringReader, null, "N-TRIPLE");
/*
		if(s1.trim().isEmpty()){
			return;
		}

		s1 = s1.replaceAll("<", "");
		s1 = s1.replaceAll(">", "");
		String[] s = s1.split("\\s++");
		model.add(new StatementImpl(new ResourceImpl(s[0]), new PropertyImpl(s[1]), new ResourceImpl(s[2])));*/
	}

	@Override
	public void flush() throws IOException
	{

	}

	@Override
	public void close() throws IOException
	{

	}
}
