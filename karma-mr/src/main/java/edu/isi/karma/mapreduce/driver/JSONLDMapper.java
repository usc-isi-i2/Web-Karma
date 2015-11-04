package edu.isi.karma.mapreduce.driver;

import edu.isi.karma.jsonld.helper.JSONLDConverter;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by chengyey on 11/2/15.
 */
public class JSONLDMapper extends Mapper<Text, Text, Text, Text> {
    private Text reusableText = new Text();
    private static Logger LOG = LoggerFactory.getLogger(JSONLDMapper.class);
    @Override
    public void map(final Text key, Text value, final Context context) throws IOException,
            InterruptedException {
        try {
            reusableText.set(new JSONLDConverter().convertJSONLD(value.toString()));
            context.write(key, reusableText);
        } catch (Exception e) {
            LOG.error("Can't convert JSON-LD", e);
            LOG.error("Text: {}", value.toString());
        }

    }
}
