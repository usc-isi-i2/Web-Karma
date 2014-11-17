package edu.isi.karma.mapreduce.inputformat;

import java.io.IOException;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;

@InterfaceAudience.Public
@InterfaceStability.Stable
public class SequenceFileAsJSONInputBatchFormat
  extends SequenceFileInputFormat<Text, Text> {

  public SequenceFileAsJSONInputBatchFormat() {
    super();
  }

  public RecordReader<Text, Text> createRecordReader(InputSplit split,
      TaskAttemptContext context) throws IOException {
    context.setStatus(split.toString());
    return new SequenceFileAsJSONRecordBatchReader();
  }
}
