package edu.isi.karma.geospatial;

import java.util.HashMap;
import java.util.Map;

public class Point {
	private final double latitude;
	private final double longitude;

	private Map<String, String> popupData = new HashMap<String, String>();

	public Point(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void addColumnToDescription(String columnName, String data) {
		popupData.put(columnName, data);
	}

	public String getHTMLDescription() {
		StringBuilder str = new StringBuilder();
		for (String name : popupData.keySet()) {
			str.append("<b>" + name + "</b>: " + popupData.get(name)
					+ " <br />");
		}
		return str.toString();
	}

	@Override
	public String toString() {
		return "Point [latitude=" + latitude + ", longitude=" + longitude + "]";
	}
}
