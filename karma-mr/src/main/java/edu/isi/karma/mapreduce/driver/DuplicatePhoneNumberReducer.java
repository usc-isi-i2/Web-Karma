package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DuplicatePhoneNumberReducer extends Reducer<Text,Text,Text,Text> {
	private static Logger LOG = LoggerFactory.getLogger(DuplicatePhoneNumberReducer.class);

	@Override
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
		for (Text value : values) {
			JSONObject obj = new JSONObject(value.toString());
			try {
				JSONArray phoneArray = obj.getJSONObject("hasFeatureCollection").getJSONArray("phonenumber_feature");
				JSONArray tmp = new JSONArray();
				Map<String, Integer> phoneNumbers = new HashMap<String, Integer>();
				Map<String, Integer> phoneNumbersFeature = new HashMap<String, Integer>();
				for (int i = 0; i < phoneArray.length(); i++) {
					JSONObject phoneObj = phoneArray.getJSONObject(i);
					String cleanedNumber = phoneObj.getString("featureValue").replace("+1-","");
					String rawPhoneNumber = phoneObj.getString("featureValue");
					if (phoneObj.has("featureObject")) {
						Integer pos = phoneNumbersFeature.get(cleanedNumber);
						if (pos == null) {
							tmp.put(phoneObj);
							phoneNumbersFeature.put(cleanedNumber, tmp.length() - 1);
						}
						else if (rawPhoneNumber.startsWith("+1-")) {
							tmp.put(pos, phoneObj);
						}
					}
					else {
						Integer pos = phoneNumbers.get(cleanedNumber);
						if (pos == null) {						
							tmp.put(phoneObj);
							phoneNumbers.put(cleanedNumber, tmp.length() - 1);
						}
						else if (rawPhoneNumber.startsWith("+1-")) {
							tmp.put(pos, phoneObj);
						}
					}
				}
				obj.getJSONObject("hasFeatureCollection").put("phonenumber_feature", tmp);
			}catch (Exception e) {
			}
			finally {
				context.write(key, new Text(obj.toString()));
			}

		}
	}
}
