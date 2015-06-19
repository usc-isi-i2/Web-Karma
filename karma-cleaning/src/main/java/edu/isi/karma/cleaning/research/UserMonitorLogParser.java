package edu.isi.karma.cleaning.research;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class UserMonitorLogParser {
	public static void main(String[] args) {
		String dirpath = "/Users/bowu/projects/Web-Karma/karma-web/log";
		File dir = new File(dirpath);
		for (File f : dir.listFiles()) {
			if (f.getName().indexOf(".txt") != (f.getName().length() - 4)) {
				continue;
			}
			try {
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String line = "";
				String jsonstr = "";
				while ((line = reader.readLine()) != null) {
					jsonstr += line;
				}
				JSONObject data = new JSONObject(jsonstr);
				ArrayList<Long> stamps = getTimeStamps(data);
				System.out.println("[durations]: "+UserMonitorLogParser.getDurations(stamps));
				ArrayList<int[]> acc = UserMonitorLogParser.getExampleIndex(data);
				System.out.println("[exampleIndex]: "+UserMonitorLogParser.printExampleIndexes(acc));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	public static String printExampleIndexes(ArrayList<int[]> acc){
		String ret = "";
		for(int[] t: acc){
			ret += String.format("%d/%d, ", t[0], t[1] );
		}
		return ret;
	}
	public static ArrayList<String> getExampleIDs(JSONObject obj){
		JSONArray x = obj.getJSONArray("examples");
		ArrayList<String> ret = new ArrayList<String>();
		for(int i = 0; i < x.length(); i++){
			ret.add(x.getJSONObject(i).getString("ID"));
		}
		return ret;
	}
	public static ArrayList<String> getrecommends(JSONObject obj){
		JSONArray x = obj.getJSONArray("recommend");
		ArrayList<String> ret = new ArrayList<String>();
		for(int i = 0; i < x.length(); i++){
			ret.add(x.getString(i));
		}
		return ret;
	}
	public static ArrayList<int[]> getExampleIndex(JSONObject obj){
		ArrayList<int[]> ret = new ArrayList<int[]>();
		try {
			for (Object key : obj.keySet()) {
				String skey = (String) key;
				if (skey.equals("start") || skey.equals("end")) {
					continue;
				}
				JSONArray array = obj.getJSONArray((String) key);
				for (int i = 0; i < array.length()-1; i++) {
					JSONObject e = array.getJSONObject(i);
					JSONObject ne = array.getJSONObject(i+1);
					ArrayList<String> cur = UserMonitorLogParser.getExampleIDs(e);
					ArrayList<String> rmds = UserMonitorLogParser.getrecommends(e);
					ArrayList<String> nxt = UserMonitorLogParser.getExampleIDs(ne);
					nxt.removeAll(cur);
					String exp = nxt.get(0);
					int index = -1;
					for(int j = 0; j < rmds.size(); j++){
						if(rmds.get(j).equals(exp)){
							index = j;
							break;
						}
					}
					int[] tmp = {index+1, rmds.size()};
					ret.add(tmp);										
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}
	public static ArrayList<Long> getDurations(ArrayList<Long> stamps) {
		ArrayList<Long> durations = new ArrayList<Long>();
		for (int i = 0; i < stamps.size() - 1; i++) {
			durations.add(stamps.get(i+1) - stamps.get(i));
		}
		return durations;
	}

	public static ArrayList<Long> getTimeStamps(JSONObject obj) {
		ArrayList<Long> ret = new ArrayList<Long>();
		ret.add(obj.getLong("start"));
		try {
			for (Object key : obj.keySet()) {
				String skey = (String) key;
				if (skey.equals("start") || skey.equals("end")) {
					continue;
				}
				JSONArray array = obj.getJSONArray((String) key);
				for (int i = 0; i < array.length(); i++) {
					JSONObject e = array.getJSONObject(i);
					ret.add(e.getLong("time"));
				}
			}
			ret.add(obj.getLong("end"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}
}
