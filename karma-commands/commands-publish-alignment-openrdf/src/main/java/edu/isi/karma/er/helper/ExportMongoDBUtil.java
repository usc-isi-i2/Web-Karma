package edu.isi.karma.er.helper;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class ExportMongoDBUtil {
	private static final Logger logger = LoggerFactory.getLogger(ExportMongoDBUtil.class);

	public void publishMongoDB(JSONArray JSONArray) throws UnknownHostException {
		MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
		DB db = mongoClient.getDB("test");
		
		DBCollection coll = db.getCollection("testCollection");
		
		// removes the existing documents
		coll.drop();

		List<DBObject> list = new ArrayList<DBObject>();
		for (int i=0; i<JSONArray.length(); i++) {
			logger.info("Inserting: " + JSONArray.get(i).toString().replaceAll("\\.", ""));

			DBObject object = (DBObject) JSON.parse(JSONArray.get(i).toString().replaceAll("\\.", ""));
			list.add(object);
		}
		coll.insert(list);
		
		logger.info("Inserted into MongoDB");
	}
}
