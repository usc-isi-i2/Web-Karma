package edu.isi.karma.er.helper;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ConfigUtil {

	public JSONArray loadConfig() {
		File file = new File("config/er_configuration.json");
		if (!file.exists()) {
			throw new IllegalArgumentException("file name " + file.getAbsolutePath() + " does not exist.");
		}
		return loadConfig(file);
	}
	
	/** 
	 * Load configurations from the specified configuration file.
	 * @param file, absolute path of configuration json file.
	 * @return configurations in a JSON array. A example of json file looks like following:
	 * 		{{ "property": "birthYear",
	 * 		   "comparator": {
	 * 				"class": "edu.isi.karma.er.compare.impl.NumberComparatorImpl",
	 * 				"alpha-file": "birthYear.txt"
	 *         		......
	 *         }
	 *       },
	 *       {
	 *         "property": "...",
	 *         "comparator": {
	 *         		...
	 *         }
	 *       }
	 */
	public JSONArray loadConfig(File file) {
		RandomAccessFile raf = null;
		StringBuffer sb = new StringBuffer();
		JSONArray arr = null;
		
		try {
			String str = null;
			raf = new java.io.RandomAccessFile(file, "rw");
			while ((str = raf.readLine()) != null) {
				sb.append("\n").append(str);
			}
			arr = new JSONArray(sb.toString());
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return arr;
	}
	
	/**
	 * Retrive a property config from configs loaded previously.
	 * @param propertyName, the full name of given property. like 'http://smithsoniean.org/saam'
	 * @return a json object related to the config of given property
	 */
	public JSONObject loadProperty(String propertyName) {
		JSONObject obj = null, result = null;
		
		try {
			JSONArray arr = loadConfig();
			
			for (int i = 0; i < arr.length(); i++) {
				obj = (JSONObject)arr.get(i);
				if (propertyName.equals(obj.get("property"))) {
					result = obj;
					break;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return result;
		
	}
	
	public void loadConstants() {
		File file = new File("config/er_constants.json");
		if (!file.exists()) {
			throw new IllegalArgumentException("file name " + file.getAbsolutePath() + " does not exist.");
		}
		
		RandomAccessFile raf = null;
		StringBuffer sb = new StringBuffer();
		JSONObject json = null;
		try {
			String str = null;
			raf = new java.io.RandomAccessFile(file, "rw");
			while ((str = raf.readLine()) != null) {
				sb.append("\n").append(str);
			}
			json = new JSONObject(sb.toString());
			Constants.PATH_BASE = json.optString("path_base");
			Constants.PATH_REPOSITORY = json.optString("path_repository");
			Constants.PATH_N3_FILE = json.optString("path_n3_file");
			Constants.PATH_RATIO_FILE = json.optString("path_ratio_file");
			Constants.PATH_SCORE_BOARD_FILE = json.optString("path_score_board_file");
			Logger log = Logger.getRootLogger();
			log.info("Constants PATH_BASE is successfully set to " + Constants.PATH_BASE);
			log.info("Constants PATH_REPOSITORY is successfully set to " + Constants.PATH_REPOSITORY);
			log.info("Constants PATH_N3_FILE is successfully set to " + Constants.PATH_N3_FILE);
			log.info("Constants PATH_RATIO_FILE is successfully set to " + Constants.PATH_RATIO_FILE);
			log.info("Constants PATH_SCORE_BOARD_FILE is successfully set to " + Constants.PATH_SCORE_BOARD_FILE);
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
