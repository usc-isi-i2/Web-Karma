package edu.isi.karma.mapreduce.inputformat;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;

public class XMLInputFormat extends TextInputFormat {
public static final String START_TAG = "<add";
public static final String END_TAG = "</add>";

@Override
public RecordReader<LongWritable,Text> getRecordReader(InputSplit inputSplit,
                                                       JobConf jobConf,
                                                       Reporter reporter) throws IOException {
  return new XMLRecordReader((FileSplit) inputSplit, jobConf);
}

/**
 * XMLRecordReader class to read through a given xml document to output xml
 * blocks as records as specified by the start tag and end tag
 * 
 */
public static class XMLRecordReader implements
    RecordReader<LongWritable,Text> {
  private final byte[] startTag;
  private final byte[] startTagUpper;
  private final byte[] endTag;
  private final byte[] endTagUpper;
  private final long start;
  private final long end;
  private final FSDataInputStream fsin;
  private final DataOutputBuffer buffer = new DataOutputBuffer();
  private String filename;
  
  public XMLRecordReader(FileSplit split, JobConf jobConf) throws IOException {
    startTag = START_TAG.getBytes("utf-8");
    startTagUpper = START_TAG.toUpperCase().getBytes("utf-8");
    endTag = END_TAG.getBytes("utf-8");
    endTagUpper = END_TAG.toUpperCase().getBytes("utf-8");
    
    // open the file and seek to the start of the split
    start = split.getStart();
    end = start + split.getLength();
    Path file = split.getPath();
    FileSystem fs = file.getFileSystem(jobConf);
    fsin = fs.open(split.getPath());
    filename = split.getPath().getName();
    fsin.seek(start);
  }
  
  @Override
  public boolean next(LongWritable key, Text value) throws IOException {
    if (fsin.getPos() < end) {
      if (readUntilMatch(startTag, startTagUpper, false)) {
        try {
          buffer.write(startTag);
          if (readUntilMatch(endTag, endTagUpper, true)) {
            key.set(fsin.getPos());
	    //  key.set(filename);
            buffer.write(("<location>" + filename + "</location>").getBytes("utf-8"));
            value.set(buffer.getData(), 0, buffer.getLength());
            return true;
          }
        } finally {
          buffer.reset();
        }
      }
    }
    return false;
  }
  
  @Override
  public LongWritable createKey() {
    return new LongWritable();
  }
  
  @Override
  public Text createValue() {
    return new Text();
  }
  
  @Override
  public long getPos() throws IOException {
    return fsin.getPos();
  }
  
  @Override
  public void close() throws IOException {
    fsin.close();
  }
  
  @Override
  public float getProgress() throws IOException {
    return (fsin.getPos() - start) / (float) (end - start);
  }
  
  private boolean readUntilMatch(byte[] match, byte[] uppermatch, boolean withinBlock) throws IOException {
    int i = 0;
    while (true) {
      int b = fsin.read();
      // end of file:
      if (b == -1) return false;
      // save to buffer:
      if (withinBlock && b!= 13 && b != 10) buffer.write(b);
      
      // check if we're matching:
      if (b == match[i] || b == uppermatch[i]) {
        i++;
        if (i >= match.length) return true;
      } else i = 0;
      // see if we've passed the stop point:
      if (!withinBlock && i == 0 && fsin.getPos() >= end) return false;
    }
  }
}
}
