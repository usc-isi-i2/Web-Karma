package edu.isi.karma.mapreduce.driver;

import edu.isi.karma.rdf.N3Impl;
import org.apache.hadoop.io.Text;

import java.io.IOException;

public class N3Mapper extends BaseRDFMapper {
	private Text reusableOutputValue = new Text("");
	private Text reusableOutputKey = new Text("");
	private N3Impl n3 = new N3Impl();
	@Override
	public void setup(Context context) throws IOException {
		this.process = n3;
		super.setup(context);
	}
	protected void writeRDFToContext(Context context, String results)
			throws IOException, InterruptedException {
		String[] lines = results.split("(\r\n|\n)");
		for(String line : lines)
		{
			if((line = line.trim()).isEmpty())
			{
				continue;
			}
			int splitBetweenSubjectAndPredicate = line.indexOf(' ');
			reusableOutputKey.set(line.substring(0, splitBetweenSubjectAndPredicate));
			reusableOutputValue.set(line);
			context.write(reusableOutputKey,reusableOutputValue);
		}
	}
}
