package edu.isi.karma.mapreduce.function;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CreateSequenceFile {

	public static void main(String[] args) throws IOException {
		if (args.length < 1)
			return;
		File directory = new File(args[0]);
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
		String outputFileName = f.getName().substring(0, f.getName().lastIndexOf(".")) + ".seq";
		Path outputPath = new Path(outputFileName);
		SequenceFile.Writer writer = SequenceFile.createWriter(new Configuration(),Writer.keyClass(Text.class),
				Writer.valueClass(Text.class), Writer.file(outputPath),Writer.compression(CompressionType.NONE));
		char c = tokener.nextClean();
		if (c == '[') {
			while (true) {
				Object o = tokener.nextValue();
				if (o instanceof JSONObject) {
					JSONObject obj = (JSONObject) o;
					writer.append(new Text(obj.getString("@id")), new Text(obj.toString()));
				}
				char tmp = tokener.nextClean();
				if (tmp == ']')
					break;
			}
		}
		writer.close();
	}

}
