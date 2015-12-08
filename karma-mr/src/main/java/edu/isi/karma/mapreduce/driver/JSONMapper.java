package edu.isi.karma.mapreduce.driver;

import edu.isi.karma.rdf.JSONImpl;
import org.apache.hadoop.io.Text;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JSONMapper extends BaseRDFMapper {
	private Text reusableOutputValue = new Text("");
	private Text reusableOutputKey = new Text("");
	private JSONImpl json = new JSONImpl();
	
	private static Logger LOG = LoggerFactory.getLogger(JSONMapper.class);
	@Override
	public void setup(Context context) throws IOException {
		this.process = json;
		super.setup(context);
	}
	@Override
	protected void writeRDFToContext(Context context, String results)
			throws IOException, InterruptedException {
	
			JSONArray generatedObjects = new JSONArray(results);
			for(int i = 0; i < generatedObjects.length(); i++)
			{
				try{
					if (generatedObjects.getJSONObject(i).has(json.getAtId())) {
						reusableOutputKey.set(generatedObjects.getJSONObject(i).getString(json.getAtId()));
					}
					else {
						reusableOutputKey.set(generatedObjects.getJSONObject(i).toString());
					}
					reusableOutputValue.set(generatedObjects.getJSONObject(i).toString());
					context.write(reusableOutputKey, new Text(reusableOutputValue));
				}catch(ArrayIndexOutOfBoundsException ae){
					LOG.error("************ARRAYEXCEPTION*********:" + ae.getMessage() + "SOURCE: " + generatedObjects.getJSONObject(i).toString());
					//TODO figure the below line out, fails when a thread has multiple posts
					//throw new ArrayIndexOutOfBoundsException("************ARRAYEXCEPTION*********:" + ae.getMessage() + "SOURCE: " + generatedObjects.getJSONObject(i).toString());
				}
			}

	}
}
