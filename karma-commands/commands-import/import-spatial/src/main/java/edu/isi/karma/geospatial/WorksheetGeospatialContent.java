/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
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

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Style;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class WorksheetGeospatialContent {
	private Worksheet worksheet;
	private SuperSelection selection;
	private List<edu.isi.karma.geospatial.Point> points = new ArrayList<>();
	private List<edu.isi.karma.geospatial.LineString> lines = new ArrayList<>();
	private List<Polygon> polygons = new ArrayList<>();
	private List<FeatureTable> polygonTable = new ArrayList<>();
	
	private static String SRID_PROPERTY = ContextParametersRegistry.getInstance().getDefault()
			.getParameterValue(ContextParameter.SRID_PROPERTY);
	private static String WGS84_LAT_PROPERTY = ContextParametersRegistry.getInstance().getDefault()
			.getParameterValue(ContextParameter.WGS84_LAT_PROPERTY);
	private static String WGS84_LNG_PROPERTY = ContextParametersRegistry.getInstance().getDefault()
			.getParameterValue(ContextParameter.WGS84_LNG_PROPERTY);
	private static String POINT_POS_PROPERTY = ContextParametersRegistry.getInstance().getDefault()
			.getParameterValue(ContextParameter.POINT_POS_PROPERTY);
	private static String POS_LIST_PROPERTY = ContextParametersRegistry.getInstance().getDefault()
			.getParameterValue(ContextParameter.POS_LIST_PROPERTY);

	private static String SRID_CLASS = ContextParametersRegistry.getInstance().getDefault()
			.getParameterValue(ContextParameter.SRID_CLASS);
	private static String POINT_CLASS = ContextParametersRegistry.getInstance().getDefault()
			.getParameterValue(ContextParameter.POINT_CLASS);
	private static String LINE_CLASS = ContextParametersRegistry.getInstance().getDefault()
			.getParameterValue(ContextParameter.LINE_CLASS);
	private static String POLYGON_CLASS = ContextParametersRegistry.getInstance().getDefault()
			.getParameterValue(ContextParameter.POLYGON_CLASS);

	private static final Logger logger = LoggerFactory
			.getLogger(WorksheetGeospatialContent.class);

	private enum CoordinateCase {
		POINT_LAT_LNG, POINT_POS, LINE_POS_LIST, POLYGON_POS_LIST, NOT_PRESENT
	}
	
	private static int randomCounter = 0;

	public WorksheetGeospatialContent(Worksheet worksheet, SuperSelection sel) {
		this.worksheet = worksheet;
		this.selection = sel;
		//WorksheetToFeatureCollections wtfc = new WorksheetToFeatureCollections(worksheet);
		populateGeospatialData();
	}

	private void populateGeospatialData() {
		
		List<String> coordinateHNodeIds = new ArrayList<>();
		CoordinateCase currentCase = CoordinateCase.NOT_PRESENT;
		boolean latFound = false;
		boolean lngFound = false;
		for (SemanticType type : worksheet.getSemanticTypes().getListOfTypes()) {
			if (type.getType().getUri().equals(SRID_PROPERTY)
					&& type.getDomain().getUri().equals(SRID_CLASS)){
				
			}
		}
		for (SemanticType type : worksheet.getSemanticTypes().getListOfTypes()) {
			// Latitude of a Point case. E.g. For a column with only latitude
			if (type.getType().getUri().equals(WGS84_LAT_PROPERTY)
					&& type.getDomain().getUri().equals(POINT_CLASS)) {
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
			else if (type.getType().getUri().equals(WGS84_LNG_PROPERTY)
					&& type.getDomain().getUri().equals(POINT_CLASS)) {
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
			else if (type.getType().getUri().equals(POINT_POS_PROPERTY)
					&& type.getDomain().getUri().equals(POINT_CLASS)) {
				coordinateHNodeIds.add(0, type.getHNodeId());
				currentCase = CoordinateCase.POINT_POS;
				populatePoints(coordinateHNodeIds, currentCase, getRows(),
						getColumnMap());
			}
			// PosList of a Line case. E.g. for a column containing list of
			// coordinates for a line string
			else if (type.getType().getUri().equals(POS_LIST_PROPERTY)
					&& type.getDomain().getUri().equals(LINE_CLASS)) {
				coordinateHNodeIds.add(0, type.getHNodeId());
				currentCase = CoordinateCase.LINE_POS_LIST;
				populateLines(coordinateHNodeIds, getRows(), getColumnMap());
			}
			// PosList of a Line case. E.g. for a column containing list of
			// coordinates for a line string
			else if (type.getType().getUri().equals(POS_LIST_PROPERTY)
					&& type.getDomain().getUri().equals(POLYGON_CLASS)) {
				coordinateHNodeIds.add(0, type.getHNodeId());
				currentCase = CoordinateCase.POLYGON_POS_LIST;
				populatePolygons(coordinateHNodeIds, getRows(), getColumnMap());
			}
		}
	}
	private void populatePolygons(List<String> coordinateHNodeIds,
			ArrayList<Row> rows, Map<String, String> columnNameMap) {
		for (Row row : rows) {
			try {
				String posList = row.getNode(coordinateHNodeIds.get(0))
						.getValue().asString();
		        WKTReader reader = new WKTReader();
		        Polygon JTSPolygon = (Polygon)reader.read(posList);
		        if(JTSPolygon == null) continue;
		        polygons.add(JTSPolygon);
				FeatureTable featureTable = new FeatureTable();
				Collection<Node> nodes = row.getNodes();
				for (Node node : nodes) {
					if (!(coordinateHNodeIds.contains(node.getHNodeId()))
							&& !(node.hasNestedTable())) {
						featureTable.addColumnToDescription(columnNameMap.get(node
								.getHNodeId()), node.getValue().asString());
					}
				}
				polygonTable.add(featureTable);
			} catch (Exception e) {
				logger.error("Error creating line! Skipping it.", e);
				continue;
			}
		}
	}
	private void populateLines(List<String> coordinateHNodeIds,
			ArrayList<Row> rows, Map<String, String> columnNameMap) {
		for (Row row : rows) {
			try {
				String posList = row.getNode(coordinateHNodeIds.get(0))
						.getValue().asString();
				
		        WKTReader reader = new WKTReader();
		        
		        LineString JTSLine = (LineString)reader.read(posList);
		        if(JTSLine == null) continue;
		        int lineLength = JTSLine.getNumPoints();
				List<Coordinate> coordsList = new ArrayList<>();
				for (int i=0;i<lineLength;i++) {
					
					Coordinate coord = new Coordinate(JTSLine.getPointN(i).getX(),JTSLine.getPointN(i).getY());
					coordsList.add(coord);
				}

				if (coordsList.isEmpty())
					continue;

				edu.isi.karma.geospatial.LineString line = new edu.isi.karma.geospatial.LineString(coordsList);
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
					//	String[] coordinateSplit = coordinate.split(",");
					//	lng = coordinateSplit[0];
					//	lat = coordinateSplit[1];
						WKTReader reader = new WKTReader();
						Point JTSPoint = (Point)reader.read(coordinate);
						if(JTSPoint == null) continue;
						lng = Double.toString(JTSPoint.getX());
						lat = Double.toString(JTSPoint.getY());
					} catch (Exception e) {
						logger.error("Error creating point! Skipping it.", e);
						continue;
					}
					break;
				}
				case LINE_POS_LIST:
					break;
				case NOT_PRESENT:
					break;
				case POLYGON_POS_LIST:
					break;
				default:
					break;
				}

				if (lng == null || lng.trim().equals("") || lat == null
						|| lat.trim().equals(""))
					continue;

				double lngF = Double.parseDouble(lng.trim());
				double latF = Double.parseDouble(lat.trim());
				edu.isi.karma.geospatial.Point point = new edu.isi.karma.geospatial.Point(lngF, latF);

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
		return worksheet.getDataTable().getRows(0, numRows, selection);
	}

	private Map<String, String> getColumnMap() {
		// Prepare a map of the column names that we use for descriptions
		List<HNode> sortedLeafHNodes = new ArrayList<>();
		worksheet.getHeaders().getSortedLeafHNodes(sortedLeafHNodes);
		Map<String, String> columnNameMap = new HashMap<>();
		for (HNode hNode : sortedLeafHNodes) {
			columnNameMap.put(hNode.getId(), hNode.getColumnName());
		}
		return columnNameMap;
	}

	public File publishKML() throws FileNotFoundException {
		File outputFile = new File(ContextParametersRegistry.getInstance().getDefault().getParameterValue(ContextParameter.KML_PUBLISH_DIR) + worksheet.getTitle() + ".kml");
		final Kml kml = KmlFactory.createKml();
		final Folder folder = kml.createAndSetFolder()
				.withName(worksheet.getTitle()).withOpen(true);

		Style style = folder.createAndAddStyle().withId("karma");
		
		if(randomCounter++%2 == 0)
			style.createAndSetIconStyle().withScale(1.399999976158142).withIcon(new Icon().withHref("http://maps.google.com/mapfiles/ms/icons/blue-pushpin.png"));
		else
			style.createAndSetIconStyle().withScale(1.399999976158142).withIcon(new Icon().withHref("http://maps.google.com/mapfiles/ms/icons/red-pushpin.png"));

		for (edu.isi.karma.geospatial.Point point : points) {
			folder.createAndAddPlacemark()
					.withDescription(point.getHTMLDescription())
					.withVisibility(true)
					.withStyleUrl("karma")
					.createAndSetPoint()
					.withAltitudeMode(AltitudeMode.CLAMP_TO_GROUND)
					.addToCoordinates(
							point.getLongitude() + "," + point.getLatitude());

		}

		for (edu.isi.karma.geospatial.LineString line : lines) {
			folder.createAndAddPlacemark()
					.withDescription(line.getHTMLDescription())
					.withVisibility(true).createAndSetLineString()
					.withAltitudeMode(AltitudeMode.CLAMP_TO_GROUND)
					.setCoordinates(line.getCoordinatesList());
		}
		int n=0;
		for (Polygon polygon: polygons) {
			FeatureTable featureTable = polygonTable.get(n);
			
			Placemark placemark = folder.createAndAddPlacemark()
			.withDescription(featureTable.getHTMLDescription())
			.withVisibility(true);
			
			final de.micromata.opengis.kml.v_2_2_0.Polygon kmlPolygon = new de.micromata.opengis.kml.v_2_2_0.Polygon();
			placemark.setGeometry(kmlPolygon);

			kmlPolygon.setExtrude(true);
			kmlPolygon.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);

			final Boundary outerboundary = new Boundary();
			kmlPolygon.setOuterBoundaryIs(outerboundary);

			final LinearRing outerlinearring = new LinearRing();
			outerboundary.setLinearRing(outerlinearring);

			List<Coordinate> outercoord = new ArrayList<>();
			outerlinearring.setCoordinates(outercoord);
			for (int i=0;i<polygon.getExteriorRing().getNumPoints();i++) {
				outercoord.add(new Coordinate(polygon.getExteriorRing().getPointN(i).getX(),polygon.getExteriorRing().getPointN(i).getY()));
			}
			
			int numOfInnerBoundaries = polygon.getNumInteriorRing();
			for(int i=0;i<numOfInnerBoundaries;i++)
			{
				final Boundary innerboundary = new Boundary();
				kmlPolygon.getInnerBoundaryIs().add(innerboundary);
	
				final LinearRing innerlinearring = new LinearRing();
				innerboundary.setLinearRing(innerlinearring);
	
				List<Coordinate> innercoord = new ArrayList<>();
				innerlinearring.setCoordinates(innercoord);
				int numOfPoints = polygon.getInteriorRingN(i).getNumPoints();
				for(int j=0;j<numOfPoints;j++)
					innercoord.add(new Coordinate(polygon.getInteriorRingN(i).getPointN(j).getX(),polygon.getInteriorRingN(i).getPointN(j).getY()));
				
			}
			
			
		}
		kml.marshal(outputFile);
		logger.info("KML file published. Location:"
				+ outputFile.getAbsolutePath());
		return outputFile;
	}

	public boolean hasNoGeospatialData() {
		if (points.isEmpty() && lines.isEmpty() && polygons.isEmpty())
			return true;
		return false;
	}
}
