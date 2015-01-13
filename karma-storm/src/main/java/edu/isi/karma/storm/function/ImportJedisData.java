package edu.isi.karma.storm.function;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.json.JSONObject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class ImportJedisData {

	public static void main(String[] args) {
		Group options = createCommandLineOptions();
        Parser parser = new Parser();
        parser.setGroup(options);
        HelpFormatter hf = new HelpFormatter();
        parser.setHelpFormatter(hf);
        parser.setHelpTrigger("--help");
        CommandLine cl = parser.parseAndHelp(args);
        if (cl == null || cl.getOptions().size() == 0 || cl.hasOption("--help") || !cl.hasOption("--filepath")) {
            hf.setGroup(options);
            hf.print();
            return;
        }
        String auth =  (String) cl.getValue("--auth");
        String filePath =  (String) cl.getValue("--filepath");
        String atId =  (String) cl.getValue("--atid");
		Jedis jedis = new Jedis("karma-dig-service.cloudapp.net",55299);
		jedis.auth(auth);
		Configuration conf = new Configuration();
		try {
			SequenceFile.Reader reader = new SequenceFile.Reader(conf, SequenceFile.Reader.file(new Path(filePath)));
			Text key = new Text();
			Text val = new Text();
			int i = 0;
			Transaction t = jedis.multi();
			while(reader.next(key, val))
			{
				JSONObject obj = new JSONObject(val.toString());
				t.set(obj.getString(atId), obj.toString());
				i++;
				if (i % 10000 == 0) {
					t.exec();
					t = jedis.multi();
				}
			}
			t.exec();
			reader.close();		
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		jedis.close();
	}

	private static Group createCommandLineOptions() {
		DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
		ArgumentBuilder abuilder = new ArgumentBuilder();
		GroupBuilder gbuilder = new GroupBuilder();

		Group options =
				gbuilder
				.withName("options")
				.withOption(buildOption("filepath", "location of the input file directory", "filepath", obuilder, abuilder))
				.withOption(buildOption("auth", "auth string", "auth", obuilder, abuilder))
				.withOption(buildOption("atid", "context at id", "atid", obuilder, abuilder))
				.withOption(obuilder
						.withLongName("help")
						.withDescription("print this message")
						.create())
						.create();

		return options;
	}

	public static Option buildOption(String shortName, String description, String argumentName,
			DefaultOptionBuilder obuilder, ArgumentBuilder abuilder) {
		return obuilder
				.withLongName(shortName)
				.withDescription(description)
				.withArgument(
						abuilder
						.withName(argumentName)
						.withMinimum(1)
						.withMaximum(1)
						.create())
						.create();
	}

}
