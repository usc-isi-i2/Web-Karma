package edu.isi.karma.mapreduce.function;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.json.JSONArray;
import org.json.JSONObject;


public class CreateSequenceFilesByType extends CreateSequenceFile {

	public static void main(String[] args) throws IOException {
		CreateSequenceFilesByType csf = new CreateSequenceFilesByType();
		csf.setup(args);
		csf.execute();
	}
	
	protected class JSONFileProcessorByType extends JSONFileProcessor{

		public JSONFileProcessorByType(InputStream stream, String fileName) {
			super(stream, fileName);
		}
		
		public SequenceFile.Writer getWriter(JSONObject obj) throws IOException
		{
			String type = "";
			Object rawtype = obj.get("@type");
			if(rawtype instanceof String)
			{
				type = (String) rawtype;
				
			}
			else if(rawtype instanceof JSONArray)
			{
				JSONArray types = (JSONArray)rawtype;
				type = types.getString(0);
			}
			if(!writers.containsKey(type))
			{
				String typeSpecificOutputFileName = outputPath + File.separator +type.substring(Math.max(0,type.lastIndexOf('/'))) + ".seq";
				Path outputPath = new Path(typeSpecificOutputFileName);
				synchronized(writers)
				{
					writers.put(type, createSequenceFile(outputPath));
				}
			}
			return writers.get(type);
		}
	}
	
	protected JSONFileProcessor getNewJSONProcessor(FileSystem hdfs,
			LocatedFileStatus status, String fileName) throws IOException {
		return new JSONFileProcessorByType(hdfs.open(status.getPath()), fileName);
	}
}
