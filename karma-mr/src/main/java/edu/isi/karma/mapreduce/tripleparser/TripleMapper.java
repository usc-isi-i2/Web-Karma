package edu.isi.karma.mapreduce.tripleparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class TripleMapper extends Mapper<Text, Text, Text, NullWritable> {

//	MultipleOutputs<Text,Text> namedOutput;
//	private static String typeUri;

	@Override
	public void map(Text key, Text value, Context context)
			throws IOException, InterruptedException {

//		if (typeUri == null || typeUri.trim().isEmpty()) 
//			return;

		String triple = key.toString() + value.toString();

		if (triple.endsWith("@en .")) {
			triple = triple.substring(0, triple.length() - 5) + " .";
		}

		List<String> list = new ArrayList<>();
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(triple);
		while (m.find())
			list.add(m.group(1));
		if (list.size() != 4 && list.size() != 5) 
			return;

		// skip other languages
//		if ((list.size() == 5)) 
//			if (!list.get(3).startsWith("@en")) 
//				return;

		String subject = list.get(0);
		String predicate = list.get(1);
		String object = list.get(2);

		if (subject.trim().length() == 0 || 
				predicate.trim().length() == 0 || 
				object.trim().length() == 0)
			return;

		if (!subject.startsWith("<") || !subject.endsWith(">")) {
			return;
		}
		if (!predicate.startsWith("<") || !predicate.endsWith(">")) {
			return;
		}

		subject = subject.substring(1, subject.length() - 1);
		subject = subject.replace("\t", "");

		predicate = predicate.substring(1, predicate.length() - 1);
		predicate = predicate.replace("\t", "");

		if (object.startsWith("<")  && object.endsWith(">"))  // object is uri
			object = object.substring(1, object.length() - 1);
		else {
			object = object.replace("\t", "");
			object = object.replaceAll("\\\\", "");
			object = "Literal:" + object;
		}

//		String guid = new RandomGUID().toString();
		
//		if (!object.startsWith("<") || !object.endsWith(">")) { // object is literal
//			object = "<" + guid + ">///" + object;
//		} else {
//			object = object.substring(1, object.length() - 1);
//		}

		String keyStr = subject + "|||" + predicate + "|||" + object; 
		context.write(new Text(keyStr), NullWritable.get());
	}

//	@Override
//	protected void setup(Context context) throws IOException, InterruptedException {
//		super.setup(context);
////		namedOutput = new MultipleOutputs<Text,Text>(context);
//		Configuration conf = context.getConfiguration();
////		typeUri = conf.get("typeUri");
//	}

}
