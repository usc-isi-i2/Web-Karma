package edu.isi.karma.mapreduce.driver;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class XMLElementExtractorMapper extends
		Mapper<Text, BytesWritable, Text, Text> {

	private Text reusableKey = new Text();
	private Text reusableValue = new Text();
	private String tag;
	private String startTag;
	private String endTag;
	private String prologue = "";
	private String epilogue = "";
	
	@Override
	public void setup(Context context) {
		Configuration config = context.getConfiguration();
		tag = config.get("karma.extraction.xml.tag");
		prologue = config.get("karma.extraction.xml.prologue","");
		epilogue = config.get("karma.extraction.xml.epilogue","");
		startTag = "<" + tag;
		endTag ="</" + tag+ ">";
	}
	@Override
	public void map(Text key, BytesWritable value, Context context) throws IOException, InterruptedException {
		
		String ouputKey = key.toString();
		reusableKey.set(ouputKey);
		int start =0;
		int end;
		int position;
		while(-1 != (position = makeSureTagIsValid(value.getBytes(), startTag, start)))
		{
			end = rawIndexOf(value.getBytes(), endTag, position);
			String patent = new String(value.getBytes(), position,  end + endTag.length() - position);
			
			reusableValue.set(prologue + patent + epilogue);
			context.write(reusableKey, reusableValue);	
			start = end + 1;
		}
		
		
	}
	
	public int makeSureTagIsValid(byte[] bytes, String startTag, int start)
	{
		int position  = rawIndexOf(bytes, startTag, start); 
		while(position != -1)
		{
			char nextChar = (char)bytes[position + startTag.length()];
			if(nextChar != ' ' && nextChar != '>'){
				position = rawIndexOf(bytes, startTag, position +1 ); 
			}
			else
			{
				break;
			}
		
		}
		return position;
	}
	public int rawIndexOf(byte[] input, String startTag, int start)
	{
		int temp = start;
		byte[] startTagBytes = startTag.getBytes();
		int i = 0;
		int log = -1;
		while(temp < input.length)
		{
			if(input[temp] == startTagBytes[i])
			{
				i++;
			}
			else if (i > 0)
			{
				i= 0;
				temp = log;
				log = -1;
			}
			if(i == 1)
			{
				log = temp;
			}
			else if (i == startTag.length())
			{
				break;
			}
			temp++;
		}
		if(temp >= input.length)
			return -1;
		return log;
	}
}
