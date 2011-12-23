package edu.isi.karma.geospatial;


public class Point {
	private final double latitude;
	private final double longitude;

//	private Map<String, String> popupData = new HashMap<String, String>();

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

	@Override
	public String toString() {
		return "Point [latitude=" + latitude + ", longitude=" + longitude + "]";
	}
}
