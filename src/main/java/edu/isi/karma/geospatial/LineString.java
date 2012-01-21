package edu.isi.karma.geospatial;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;

public class LineString {
	private final List<Coordinate> coordinatesList;
	private Map<String, String> popupData = new HashMap<String, String>();

	public LineString(List<Coordinate> posList) {
		super();
		this.coordinatesList = posList;
	}

	public List<Coordinate> getCoordinatesList() {
		return coordinatesList;
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
}
