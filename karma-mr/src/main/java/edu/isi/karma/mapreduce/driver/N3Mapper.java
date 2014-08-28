package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.hadoop.io.Text;

import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.N3KR2RMLRDFWriter;

public class N3Mapper extends BaseRDFMapper {
	protected KR2RMLRDFWriter configureRDFWriter(StringWriter sw) {
		PrintWriter pw = new PrintWriter(sw);
		URIFormatter uriFormatter = new URIFormatter();
		N3KR2RMLRDFWriter outWriter = new N3KR2RMLRDFWriter(uriFormatter, pw);
		outWriter.setBaseURI(baseURI);
		return outWriter;
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
			context.write(new Text(line.substring(0, splitBetweenSubjectAndPredicate)), new Text(line));
		}
	}
}
