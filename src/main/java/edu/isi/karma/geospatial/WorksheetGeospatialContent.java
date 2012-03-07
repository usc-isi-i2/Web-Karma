/*******************************************************************************
 * Copyright 2012 University of Southern California
 *  
 ******************************************************************************/
package edu.isi.karma.geospatial;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.metadata.TagsContainer;
import edu.isi.karma.rep.metadata.TagsContainer.TagName;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class WorksheetGeospatialContent {
	private Worksheet worksheet;

	private List<Point> points = new ArrayList<Point>();
	private List<LineString> lines = new ArrayList<LineString>();

	private static String WGS84_LAT_PROPERTY = ServletContextParameterMap
			.getParameterValue(ContextParameter.WGS84_LAT_PROPERTY);
	private static String WGS84_LNG_PROPERTY = ServletContextParameterMap
			.getParameterValue(ContextParameter.WGS84_LNG_PROPERTY);
	private static String POINT_POS_PROPERTY = ServletContextParameterMap
			.getParameterValue(ContextParameter.POINT_POS_PROPERTY);
	private static String POS_LIST_PROPERTY = ServletContextParameterMap
			.getParameterValue(ContextParameter.POS_LIST_PROPERTY);

	private static String POINT_CLASS = ServletContextParameterMap
			.getParameterValue(ContextParameter.POINT_CLASS);
	private static String LINE_CLASS = ServletContextParameterMap
			.getParameterValue(ContextParameter.LINE_CLASS);

	private static final Logger logger = LoggerFactory
			.getLogger(WorksheetGeospatialContent.class);

	private enum CoordinateCase {
		POINT_LAT_LNG, POINT_POS, LINE_POS_LIST, POLYGON_POS_LIST, NOT_PRESENT
	}

	public WorksheetGeospatialContent(Worksheet worksheet,
			TagsContainer tagsContainer) {
		this.worksheet = worksheet;
		populateGeospatialData(tagsContainer);
	}

	private void populateGeospatialData(TagsContainer tagsContainer) {
		List<String> coordinateHNodeIds = new ArrayList<String>();
		CoordinateCase currentCase = CoordinateCase.NOT_PRESENT;
		boolean latFound = false;
		boolean lngFound = false;

		if (worksheet.getSemanticTypes().getListOfTypes().size() == 0) {
			SemanticTypeUtil.populateSemanticTypesUsingCRF(worksheet,
					tagsContainer.getTag(TagName.Outlier));
		}

		for (SemanticType type : worksheet.getSemanticTypes().getListOfTypes()) {
			// Latitude of a Point case. E.g. For a column with only latitude
			if (type.getType().equals(WGS84_LAT_PROPERTY)
					&& type.getDomain().equals(POINT_CLASS)) {
				// Longitude id is always before the latitude id
				if (lngFound) {
					currentCase = CoordinateCase.POINT_LAT_LNG;
					coordinateHNodeIds.add(type.getHNodeId());
					populatePoints(coordinateHNodeIds, currentCase, getRows(),
							getColumnMap());
					latFound = lngFound = false;
					coordinateHNodeIds.clear();
				} else {
					coordinateHNodeIds.add(null); // Setting space for long id
					coordinateHNodeIds.add(type.getHNodeId());
					latFound = true;
				}
			}
			// Long of a Point case. E.g. for a column with only longitude
			else if (type.getType().equals(WGS84_LNG_PROPERTY)
					&& type.getDomain().equals(POINT_CLASS)) {
				// Longitude id is always before the latitude id
				if (latFound) {
					coordinateHNodeIds.set(0, type.getHNodeId());
					currentCase = CoordinateCase.POINT_LAT_LNG;
					populatePoints(coordinateHNodeIds, currentCase, getRows(),
							getColumnMap());
					latFound = lngFound = false;
					coordinateHNodeIds.clear();
				} else {
					coordinateHNodeIds.add(type.getHNodeId());
					lngFound = true;
				}
			}
			// Position of a Point case. E.g. for a column containing lat and
			// long data such as "12.34, 234.2"
			else if (type.getType().equals(POINT_POS_PROPERTY)
					&& type.getDomain().equals(POINT_CLASS)) {
				coordinateHNodeIds.add(0, type.getHNodeId());
				currentCase = CoordinateCase.POINT_POS;
				populatePoints(coordinateHNodeIds, currentCase, getRows(),
						getColumnMap());
			}
			// PosList of a Line case. E.g. for a column containing list of
			// coordinates for a line string
			else if (type.getType().equals(POS_LIST_PROPERTY)
					&& type.getDomain().equals(LINE_CLASS)) {
				coordinateHNodeIds.add(0, type.getHNodeId());
				currentCase = CoordinateCase.LINE_POS_LIST;
				populateLines(coordinateHNodeIds, getRows(), getColumnMap());
			}
		}
	}

	private void populateLines(List<String> coordinateHNodeIds,
			ArrayList<Row> rows, Map<String, String> columnNameMap) {
		for (Row row : rows) {
			try {
				String posList = row.getNode(coordinateHNodeIds.get(0))
						.getValue().asString();
				String[] coords = posList.split(" ");
				List<Coordinate> coordsList = new ArrayList<Coordinate>();
				for (String coordStr : coords) {
					Coordinate coord = new Coordinate(coordStr);
					coordsList.add(coord);
				}

				if (coordsList.size() == 0)
					continue;

				LineString line = new LineString(coordsList);
				Collection<Node> nodes = row.getNodes();
				for (Node node : nodes) {
					if (!(coordinateHNodeIds.contains(node.getHNodeId()))
							&& !(node.hasNestedTable())) {
						line.addColumnToDescription(columnNameMap.get(node
								.getHNodeId()), node.getValue().asString());
					}
				}
				lines.add(line);
			} catch (Exception e) {
				logger.error("Error creating line! Skipping it.", e);
				continue;
			}
		}
	}

	private void populatePoints(List<String> coordinateHNodeIds,
			CoordinateCase currentCase, ArrayList<Row> rows,
			Map<String, String> columnNameMap) {
		// Extract the latitude, longitude and the other description data
		String lng = "";
		String lat = "";
		for (Row row : rows) {
			try {
				switch (currentCase) {
				case POINT_LAT_LNG: {
					try {
						lng = row.getNode(coordinateHNodeIds.get(0)).getValue()
								.asString();
						lat = row.getNode(coordinateHNodeIds.get(1)).getValue()
								.asString();
					} catch (Exception e) {
						logger.error("Error creating point!", e);
						continue;
					}
					break;
				}
				case POINT_POS: {
					try {
						String coordinate = row
								.getNode(coordinateHNodeIds.get(0)).getValue()
								.asString();
						String[] coordinateSplit = coordinate.split(",");

						lng = coordinateSplit[0];
						lat = coordinateSplit[1];

					} catch (Exception e) {
						logger.error("Error creating point! Skipping it.", e);
						continue;
					}
					break;
				}
				}

				if (lng == null || lng.trim().equals("") || lat == null
						|| lat.trim().equals(""))
					continue;

				double lngF = Double.parseDouble(lng.trim());
				double latF = Double.parseDouble(lat.trim());
				Point point = new Point(lngF, latF);

				// Get the data from the other columns for description
				Collection<Node> nodes = row.getNodes();
				for (Node node : nodes) {
					if (!(coordinateHNodeIds.contains(node.getHNodeId()))
							&& !(node.hasNestedTable())) {
						point.addColumnToDescription(columnNameMap.get(node
								.getHNodeId()), node.getValue().asString());
					}
				}

				points.add(point);
			} catch (Exception e) {
				logger.error("Error creating point! Skipping it.", e);
				continue;
			}
		}
	}

	private ArrayList<Row> getRows() {
		int numRows = worksheet.getDataTable().getNumRows();
		return worksheet.getDataTable().getRows(0, numRows);
	}

	private Map<String, String> getColumnMap() {
		// Prepare a map of the column names that we use for descriptions
		List<HNode> sortedLeafHNodes = new ArrayList<HNode>();
		worksheet.getHeaders().getSortedLeafHNodes(sortedLeafHNodes);
		Map<String, String> columnNameMap = new HashMap<String, String>();
		for (HNode hNode : sortedLeafHNodes) {
			columnNameMap.put(hNode.getId(), hNode.getColumnName());
		}
		return columnNameMap;
	}

	public File publishKML() throws FileNotFoundException {
		File outputFile = new File("./publish/KML/" + worksheet.getTitle()
				+ ".kml");
		final Kml kml = KmlFactory.createKml();
		final Folder folder = kml.createAndSetFolder()
				.withName(worksheet.getTitle()).withOpen(true);

		for (Point point : points) {
			folder.createAndAddPlacemark()
					.withDescription(point.getHTMLDescription())
					.withVisibility(true)
					.createAndSetPoint()
					.withAltitudeMode(AltitudeMode.CLAMP_TO_GROUND)
					.addToCoordinates(
							point.getLongitude() + "," + point.getLatitude());

		}

		for (LineString line : lines) {
			folder.createAndAddPlacemark()
					.withDescription(line.getHTMLDescription())
					.withVisibility(true).createAndSetLineString()
					.withAltitudeMode(AltitudeMode.CLAMP_TO_GROUND)
					.setCoordinates(line.getCoordinatesList());
		}

		kml.marshal(outputFile);
		logger.info("KML file published. Location:"
				+ outputFile.getAbsolutePath());
		return outputFile;
	}

	public boolean hasNoGeospatialData() {
		if (points.size() == 0 && lines.size() == 0)
			return true;
		return false;
	}
}
