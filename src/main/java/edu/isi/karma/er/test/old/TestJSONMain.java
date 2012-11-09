package edu.isi.karma.er.test.old;

import java.io.File;
import java.io.RandomAccessFile;

import org.json.JSONArray;
import org.json.JSONObject;

public class TestJSONMain {

	public static void main(String[] args) {
		File file = new File("config/configuration.json");
		System.out.println(file.getAbsolutePath());
		
		RandomAccessFile raf = null;
		StringBuffer sb = new StringBuffer();
		
		try {
			String str = null;
			raf = new java.io.RandomAccessFile(file, "rw");
			while ((str = raf.readLine()) != null) {
				sb.append("\n").append(str);
			}
			JSONArray arr = new JSONArray(sb.toString());
			for (int i = 0; i < arr.length(); i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				System.out.println(obj.get("property") + ":" + ((JSONObject)obj.get("comparator")).get("class"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	
	}
}
