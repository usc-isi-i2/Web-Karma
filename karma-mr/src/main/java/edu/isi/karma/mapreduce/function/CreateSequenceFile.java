package edu.isi.karma.mapreduce.function;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
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
	static String filePath = null;
	public static void main(String[] args) throws IOException {
		if (args.length < 1)
			return;
		filePath = args[0];
		if(args.length > 1)
		{
			useKey = Boolean.parseBoolean(args[1]);
		}
		if(args.length > 2)
		{
			outputFileName = Boolean.parseBoolean(args[2]);
		}
		FileSystem hdfs = FileSystem.get(new Configuration());
		RemoteIterator<LocatedFileStatus> itr = hdfs.listFiles(new Path(args[0]), true);
		while (itr.hasNext()) {
			LocatedFileStatus status = itr.next();
			String fileName = status.getPath().getName();
			if (fileName.substring(fileName.lastIndexOf(".") + 1).contains("json")) {
				createSequenceFileFromJSON(hdfs.open(status.getPath()), fileName);
			}
		}
		System.out.println("dir name: " + args[0]);
	}

	public static void createSequenceFileFromJSON(InputStream stream, String fileName) throws IOException {
		JSONTokener tokener = new JSONTokener(new InputStreamReader(stream, "UTF-8"));
		String outputFileName = filePath + File.separator +fileName.substring(0, fileName.lastIndexOf(".")) + ".seq";
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
		addValuesToSequenceFile(tokener, writer, fileName);
		writer.close();
	}
	
	public static void addValuesToSequenceFile(JSONTokener tokener, SequenceFile.Writer writer, String fileName) throws JSONException, IOException {
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
							writer.append(new Text(fileName), new Text(obj.toString()));
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
