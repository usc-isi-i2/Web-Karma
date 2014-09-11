package edu.isi.karma.mapreduce.function;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

public class CreateJSONFromSequenceFile {
	public static void main(String[] args) throws IOException {
		if (args.length < 1)
			return;
		File directory = new File(args[0]);
		if (directory.isDirectory()) {
			for (File f : directory.listFiles()) {
				if (f.getName().contains("seq")) {
					createJSONFromSequenceFileFrom(f);
				}
			}
		}
	}

	public static void createJSONFromSequenceFileFrom(File f) throws IOException {
		String filePath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(File.separator));
		String outputFileName = filePath + File.separator + f.getName().substring(0, f.getName().lastIndexOf(".")) + ".json";
		Path inputPath = new Path(f.getAbsolutePath());
		Configuration conf = new Configuration();
		conf.setIfUnset("fs.default.name", "file:///");
		FileWriter fw = new FileWriter(new File(outputFileName));
		
		SequenceFile.Reader reader = new SequenceFile.Reader(conf, SequenceFile.Reader.file(inputPath));
		//SequenceFile.Reader reader = new SequenceFile.Reader(FileSystem.getLocal(conf),inputPath,conf);
		Text key = new Text();
		Text val = new Text();
		fw.write("[\n");
		boolean writtenyet = false;
		while(reader.next(key, val))
		{
			if(!writtenyet)
			{
				writtenyet=true;
			}
			else
			{
				fw.write(",\n");
			}
			fw.write(val.toString());
		}
		fw.write("\n]\n");
		fw.close();
		reader.close();
	}
	


}
