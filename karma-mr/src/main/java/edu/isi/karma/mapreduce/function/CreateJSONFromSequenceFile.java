package edu.isi.karma.mapreduce.function;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

public class CreateJSONFromSequenceFile {
	static String filePath = null;
	public static void main(String[] args) throws IOException {
		if (args.length < 1)
			return;
		filePath = args[0];
		FileSystem hdfs = FileSystem.get(new Configuration());
		RemoteIterator<LocatedFileStatus> itr = hdfs.listFiles(new Path(args[0]), true);
		while (itr.hasNext()) {
			LocatedFileStatus status = itr.next();
			String fileName = status.getPath().getName();
			if (status.getLen() > 0) {
				String outputFileName = filePath + File.separator + fileName + ".json";
				createJSONFromSequenceFileFrom(status.getPath(), hdfs.create(new Path(outputFileName)));
			}
		}
	}

	public static void createJSONFromSequenceFileFrom(Path input, FSDataOutputStream fsDataOutputStream) throws IOException {
		Path inputPath = input;
		Configuration conf = new Configuration();
		PrintWriter fw = new PrintWriter(fsDataOutputStream);
		SequenceFile.Reader reader = new SequenceFile.Reader(conf, SequenceFile.Reader.file(inputPath));
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
