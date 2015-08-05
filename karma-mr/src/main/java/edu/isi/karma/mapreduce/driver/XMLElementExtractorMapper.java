package edu.isi.karma.mapreduce.driver;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLElementExtractorMapper extends
		Mapper<Text, BytesWritable, Text, Text> {

	private static Logger LOG = LoggerFactory.getLogger(XMLElementExtractorMapper.class);
	private Text reusableKey = new Text();
	private Text reusableValue = new Text();
	private String tag;
	private String startTag;
	private String endTag;
	@Override
	public void setup(Context context) {
		Configuration config = context.getConfiguration();
		tag = config.get("karma.extraction.xml.tag");
		startTag = "<" + tag;
		endTag ="</" + tag+ ">";
	}
	@Override
	public void map(Text key, BytesWritable value, Context context) throws IOException, InterruptedException {
		String xmlString = new String(value.getBytes(),"UTF-8");
		String ouputKey = key.toString();
		reusableKey.set(ouputKey);
		int start =0;
		int end = 0;
		int position = 0;
		while(-1 != (position = xmlString.indexOf(startTag, start)))
		{
			end = xmlString.indexOf(endTag, position);
			String patent = xmlString.substring(position, end + 18 );
			
			reusableValue.set(patent);
			context.write(reusableKey, reusableValue);	
			start = end + 1;
		}
		
		
	}
}
