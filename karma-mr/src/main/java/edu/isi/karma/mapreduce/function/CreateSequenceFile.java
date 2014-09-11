package edu.isi.karma.mapreduce.function;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CreateSequenceFile {

	static boolean useKey = true;
	static boolean outputFileName = false;
	public static void main(String[] args) throws IOException {
		if (args.length < 1)
			return;
		File directory = new File(args[0]);
		if(args.length > 1)
		{
			useKey = Boolean.parseBoolean(args[1]);
		}
		if(args.length > 2)
		{
			outputFileName = Boolean.parseBoolean(args[2]);
		}
		if (directory.isDirectory()) {
			for (File f : directory.listFiles()) {
				if (f.getName().contains("json")) {
					createSequenceFileFromJSON(f);
				}
			}
		}
	}

	public static void createSequenceFileFromJSON(File f) throws IOException {
		JSONTokener tokener = new JSONTokener(new InputStreamReader(new FileInputStream(f), "UTF-8"));
		String filePath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(File.separator));
		String outputFileName = filePath + File.separator + f.getName().substring(0, f.getName().lastIndexOf(".")) + ".seq";
		Path outputPath = new Path(outputFileName);
		SequenceFile.Writer writer = null;
		if(useKey)
		{
			writer = SequenceFile.createWriter(new Configuration(),Writer.keyClass(Text.class),
			Writer.valueClass(Text.class), Writer.file(outputPath),Writer.compression(CompressionType.NONE));
		}
		else
		{
			writer = 	SequenceFile.createWriter(new Configuration(),Writer.keyClass(BytesWritable.class),
					Writer.valueClass(Text.class), Writer.file(outputPath),Writer.compression(CompressionType.NONE));
		}
		addValuesToSequenceFile(tokener, writer, f);
		writer.close();
	}
	
	public static void addValuesToSequenceFile(JSONTokener tokener, SequenceFile.Writer writer, File f) throws JSONException, IOException {
		char c = tokener.nextClean();
		if (c == '[') {
			while (true) {
				Object o = tokener.nextValue();
				if (o instanceof JSONObject) {
					JSONObject obj = (JSONObject) o;
					if(useKey)
					{
						if(outputFileName)
						{
							writer.append(new Text(f.getName()), new Text(obj.toString()));
						}
						else
						{
							writer.append(new Text(obj.getString("@id")), new Text(obj.toString()));
						}
					}
					else
					{
						writer.append(new BytesWritable(), new Text(obj.toString()));
					}
						
				}
				char tmp = tokener.nextClean();
				if (tmp == ']')
					break;
			}
		}
	}

}
