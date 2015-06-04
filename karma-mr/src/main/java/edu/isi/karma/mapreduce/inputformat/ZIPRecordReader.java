package edu.isi.karma.mapreduce.inputformat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

@InterfaceAudience.Public
@InterfaceStability.Stable
public class ZIPRecordReader
extends RecordReader<Writable, Writable> {

	private Text key = new Text();
	private BytesWritable value = new BytesWritable();
	private long start;
	private long bytesReadEstimate = 0;
	private long end;
	private ZipInputStream is; 
	private ZipEntry entry;
	public ZIPRecordReader()
			throws IOException {
		//TODO
	}

	public void initialize(InputSplit rawSplit, TaskAttemptContext context)
			throws IOException, InterruptedException {
		 FileSplit split = (FileSplit) rawSplit;
	    Configuration job = context.getConfiguration();
	    
	    start = split.getStart();
	    end = start + split.getLength();
	    final Path file = split.getPath();
	    
	    // open the file and seek to the start of the split
	    final FileSystem fs = file.getFileSystem(job);
	    is = new ZipInputStream(fs.open(file));
	}

	@Override
	public Text getCurrentKey() 
			throws IOException, InterruptedException {
		if(entry != null)
		{
			key.set(entry.getName());
		}
		else
		{
			key.set("");
		}
		return key;
	}

	@Override
	public Writable getCurrentValue() 
			throws IOException, InterruptedException {
		if(entry != null)
		{
		      ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        while(true){
		            int b = is.read();
		            if(b == -1) break;
		            baos.write(b);
		        }
		        byte[] filebytes = baos.toByteArray();
			
			value = new BytesWritable(filebytes);
			bytesReadEstimate += filebytes.length;
			
		}
		return value;
	}

	public synchronized boolean nextKeyValue() 
			throws IOException, InterruptedException {
		while((entry = is.getNextEntry())!=null && entry.isDirectory());
		return entry != null;
	}

	public float getProgress() throws IOException,  InterruptedException {
		return bytesReadEstimate/end;
	}

	public synchronized void close() throws IOException {
		is.close();
	}
}
