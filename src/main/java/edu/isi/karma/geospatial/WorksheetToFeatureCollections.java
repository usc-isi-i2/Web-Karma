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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Style;

import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.util.RandomGUID;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorksheetToFeatureCollections {
	private Worksheet worksheet;
	private OntologyManager om;

	List<SimpleFeature> pointFeatureList = new ArrayList<SimpleFeature>();
	List<SimpleFeature> lineFeatureList = new ArrayList<SimpleFeature>();
	List<SimpleFeature> polygonFeatureList = new ArrayList<SimpleFeature>();
	List<AttributeDescriptor> featureSchema = new ArrayList<AttributeDescriptor>();
	List<String> geomHNodeIdList = new ArrayList<String>();

	private String SRIDHNodeId = "";
	private String pointFeatureHNodeId = "";
	private String pointFeatureLatHNodeId = "";
	private String pointFeatureLonHNodeId = "";
	private String lineFeatureHNodeId = "";
	private String polygonFeatureHNodeId = "";
	private String zippedSpatialDataFolderAndName = "";

	private static String SRID_PROPERTY = ServletContextParameterMap
			.getParameterValue(ContextParameter.SRID_PROPERTY);
	private static String WGS84_LAT_PROPERTY = ServletContextParameterMap
			.getParameterValue(ContextParameter.WGS84_LAT_PROPERTY);
	private static String WGS84_LNG_PROPERTY = ServletContextParameterMap
			.getParameterValue(ContextParameter.WGS84_LNG_PROPERTY);
	private static String POINT_POS_PROPERTY = ServletContextParameterMap
			.getParameterValue(ContextParameter.POINT_POS_PROPERTY);
	private static String POS_LIST_PROPERTY = ServletContextParameterMap
			.getParameterValue(ContextParameter.POS_LIST_PROPERTY);
	private static String SRID_CLASS = ServletContextParameterMap
			.getParameterValue(ContextParameter.SRID_CLASS);
	private static String POINT_CLASS = ServletContextParameterMap
			.getParameterValue(ContextParameter.POINT_CLASS);
	private static String LINE_CLASS = ServletContextParameterMap
			.getParameterValue(ContextParameter.LINE_CLASS);
	private static String POLYGON_CLASS = ServletContextParameterMap
			.getParameterValue(ContextParameter.POLYGON_CLASS);

	private static final Logger logger = LoggerFactory
			.getLogger(WorksheetGeospatialContent.class);

	public WorksheetToFeatureCollections(Worksheet worksheet, OntologyManager om) {
		this.worksheet = worksheet;
		this.om = om;
		prepareFeatureSchema();
	}

	//
	// private boolean isSubclassOf(String subClassUri, String superClassUri,
	// boolean recursive){
	// boolean isSub=false;
	// isSub=this.om.isSubClass(subClassUri, superClassUri, recursive);
	// return isSub;
	// }

	private boolean isSubPropertyOf(String subPropertyUri,
			String superPropertyUri, boolean recursive) {
		boolean isSub = false;
		isSub = this.om.isSubProperty(subPropertyUri, superPropertyUri,
				recursive);
		return isSub;
	}

	private void prepareFeatureSchema() {
		List<String> spatialHNodeIds = new ArrayList<String>();

		for (SemanticType type : worksheet.getSemanticTypes().getListOfTypes()) {
			if (type.getType().getUriString().equals(SRID_PROPERTY)
					&& type.getDomain().getUriString().equals(SRID_CLASS)) {
				SRIDHNodeId = type.getHNodeId();
				spatialHNodeIds.add(0, SRIDHNodeId);
			} else if (isSubPropertyOf(
					type.getType().getUriString().toString(),
					"http://www.opengis.net/gml/hasID", true)) {
				System.out.println("POS_LIST_PROPERTY:"
						+ type.getType().getUriString().toString());
				SRIDHNodeId = type.getHNodeId();
				spatialHNodeIds.add(0, SRIDHNodeId);
			} else if (type.getType().getUriString().equals(POINT_POS_PROPERTY)
					&& type.getDomain().getUriString().equals(POINT_CLASS)
					&& pointFeatureHNodeId == "") {
				spatialHNodeIds.add(0, type.getHNodeId());
				pointFeatureHNodeId = type.getHNodeId();
			} else if (type.getType().getUriString().equals(WGS84_LAT_PROPERTY)
					&& type.getDomain().getUriString().equals(POINT_CLASS)
					&& pointFeatureLatHNodeId == "") {
				spatialHNodeIds.add(0, type.getHNodeId());
				pointFeatureLatHNodeId = type.getHNodeId();
			} else if (type.getType().getUriString().equals(WGS84_LNG_PROPERTY)
					&& type.getDomain().getUriString().equals(POINT_CLASS)
					&& pointFeatureLonHNodeId == "") {
				spatialHNodeIds.add(0, type.getHNodeId());
				pointFeatureLonHNodeId = type.getHNodeId();
			} else if (type.getType().getUriString().equals(POS_LIST_PROPERTY)
					&& type.getDomain().getUriString().equals(LINE_CLASS)
					&& lineFeatureHNodeId == "") {
				spatialHNodeIds.add(0, type.getHNodeId());
				lineFeatureHNodeId = type.getHNodeId();
			} else if (isSubPropertyOf(
					type.getType().getUriString().toString(),
					"http://www.opengis.net/gml/posList", true)) {
				System.out.println("POS_LIST_PROPERTY:"
						+ type.getType().getUriString().toString());
				spatialHNodeIds.add(0, type.getHNodeId());
				lineFeatureHNodeId = type.getHNodeId();

			} else if (type.getType().getUriString().equals(POS_LIST_PROPERTY)
					&& type.getDomain().getUriString().equals(POLYGON_CLASS)
					&& polygonFeatureHNodeId == "") {
				spatialHNodeIds.add(0, type.getHNodeId());
				polygonFeatureHNodeId = type.getHNodeId();
			}
		}

		if (spatialHNodeIds.size() > 1) {
			List<HNode> sortedLeafHNodes = new ArrayList<HNode>();
			worksheet.getHeaders().getSortedLeafHNodes(sortedLeafHNodes);
			for (HNode hNode : sortedLeafHNodes) {
				if (!spatialHNodeIds.contains(hNode.getId())) {
					AttributeTypeBuilder build = new AttributeTypeBuilder();
					build.setNillable(true);
					build.setBinding(String.class); // might need to change to
													// specific bindings
					AttributeDescriptor descriptor = build
							.buildDescriptor(hNode.getColumnName());
					featureSchema.add(descriptor);
					geomHNodeIdList.add(hNode.getId());
				}
			}
		}

		if (spatialHNodeIds.size() > 1) { // has spatial data
			if (pointFeatureHNodeId != "")
				populateSimpleFeatures(spatialHNodeIds, pointFeatureHNodeId,
						"", getRows(), getColumnMap(), pointFeatureList,
						Point.class);
			if (pointFeatureLatHNodeId != "" && pointFeatureLonHNodeId != "")
				populateSimpleFeatures(spatialHNodeIds, pointFeatureLonHNodeId,
						pointFeatureLatHNodeId, getRows(), getColumnMap(),
						pointFeatureList, Point.class);
			if (lineFeatureHNodeId != "")
				populateSimpleFeatures(spatialHNodeIds, lineFeatureHNodeId, "",
						getRows(), getColumnMap(), lineFeatureList,
						LineString.class);
			if (polygonFeatureHNodeId != "")
				populateSimpleFeatures(spatialHNodeIds, polygonFeatureHNodeId,
						"", getRows(), getColumnMap(), polygonFeatureList,
						Polygon.class);
		}
	}

	private void populateSimpleFeatures(List<String> spatialHNodeIds,
			String geometryHNodeId, String geometry2HNodeId,
			ArrayList<Row> rows, Map<String, String> columnNameMap,
			List<SimpleFeature> features, Class binding) {

		for (Row row : rows) {
			try {
				Geometry JTSGeometry = null;
				String posList = "";
				if (geometry2HNodeId == "") {
					posList = row.getNode(geometryHNodeId).getValue()
							.asString();
					// Future work on WKB columns:
					// WKBReader wkbreader = new WKBReader();
					// byte[] wkbPosList = WKBReader.hexToBytes(posList);
					// JTSGeometry = wkbreader.read(wkbPosList);
				} else {
					String lon = row.getNode(geometryHNodeId).getValue()
							.asString();
					String lat = row.getNode(geometry2HNodeId).getValue()
							.asString();
					if (lon.trim().length() == 0 || lat.trim().length() == 0)
						continue;
					posList = "POINT(" + lon + " " + lat + ")";
				}

				if (posList.length() == 0)
					continue;

				posList = posList.toUpperCase();
				WKTReader reader = new WKTReader();
				JTSGeometry = reader.read(posList);

				if (JTSGeometry == null)
					continue;

				String srid = row.getNode(SRIDHNodeId).getValue().asString();
				if (!srid.contains(":"))
					srid = "EPSG:" + srid;
				CoordinateReferenceSystem sourceCRS = null;

				try {
					sourceCRS = CRS.decode(srid, true);
				} catch (NoSuchAuthorityCodeException e) {
					logger.error("No such authority code!", e);
				} catch (FactoryException e) {
					logger.error("Error parsing SRID!", e);
				}
				SimpleFeatureTypeBuilder simpleFeatureTypeBuilder = new SimpleFeatureTypeBuilder();
				simpleFeatureTypeBuilder.setName("SimpleFeature");
				simpleFeatureTypeBuilder.setCRS(sourceCRS);
				simpleFeatureTypeBuilder.add("GEOM", binding);
				simpleFeatureTypeBuilder.addAll(featureSchema);
				SimpleFeatureType simpleFeatureTypeWithCRS = simpleFeatureTypeBuilder
						.buildFeatureType();
				SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(
						simpleFeatureTypeWithCRS);
				featureBuilder.add(JTSGeometry);

				for (String hNodeId : geomHNodeIdList) {
					Node node = row.getNode(hNodeId);
					if (node.hasNestedTable())
						featureBuilder.add("Nested table");
					else {
						String colValue = node.getValue().asString();
						featureBuilder.add(colValue);
					}
				}
				SimpleFeature feature = featureBuilder.buildFeature(null);
				features.add(feature);
			} catch (Exception e) {
				logger.error("Error creating geometry! Skipping it.", e);
				continue;
			}
		}
	}

	private ArrayList<Row> getRows() {
		int numRows = worksheet.getDataTable().getNumRows();
		return worksheet.getDataTable().getRows(0, numRows);
	}

	private Map<String, String> getColumnMap() {
		List<HNode> sortedLeafHNodes = new ArrayList<HNode>();
		worksheet.getHeaders().getSortedLeafHNodes(sortedLeafHNodes);
		Map<String, String> columnNameMap = new HashMap<String, String>();
		for (HNode hNode : sortedLeafHNodes) {
			columnNameMap.put(hNode.getId(), hNode.getColumnName());
		}
		return columnNameMap;
	}

	public String getZippedSpatialDataPath() {
		return zippedSpatialDataFolderAndName;
	}

	public File SaveSpatialData() throws Exception {
		String spatialDataFolder = new RandomGUID().toString();
		String fileName = worksheet.getTitle();
		fileName = fileName.substring(0, fileName.length() - 4);
		zippedSpatialDataFolderAndName = spatialDataFolder + "/" + fileName
				+ ".zip";
		spatialDataFolder = ServletContextParameterMap
				.getParameterValue(ContextParameter.USER_DIRECTORY_PATH)
				+ "publish/SpatialData/" + spatialDataFolder + "/";
		File dir = new File(spatialDataFolder);
		dir.mkdir();

		File kml = publishKML(spatialDataFolder, fileName);
		if (pointFeatureHNodeId != ""
				|| (pointFeatureLatHNodeId != "" && pointFeatureLonHNodeId != ""))
			saveShapefile(pointFeatureList, spatialDataFolder, fileName
					+ "_point");
		if (lineFeatureHNodeId != "")
			saveShapefile(lineFeatureList, spatialDataFolder, fileName
					+ "_line");
		if (polygonFeatureHNodeId != "")
			saveShapefile(polygonFeatureList, spatialDataFolder, fileName
					+ "_polygon");

		saveSpatialDataToZip(spatialDataFolder, fileName);
		return kml;
	}

	public File saveShapefile(List<SimpleFeature> features,
			String spatialDataFolder, String fileName)
			throws FileNotFoundException, Exception {
		File outputFile = new File(spatialDataFolder + fileName + ".shp");
		try {
			/*
			 * Get an output file name and create the new shapefile
			 */
			ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

			Map<String, Serializable> params = new HashMap<String, Serializable>();
			params.put("url", outputFile.toURI().toURL());
			params.put("create spatial index", Boolean.TRUE);

			ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory
					.createNewDataStore(params);
			newDataStore.setStringCharset(Charset.forName("UTF-8"));
			newDataStore.createSchema(features.get(0).getFeatureType());

			/*
			 * You can comment out this line if you are using the
			 * createFeatureType method (at end of class file) rather than
			 * DataUtilities.createType
			 */
			CoordinateReferenceSystem crs = features.get(0).getFeatureType()
					.getCoordinateReferenceSystem();
			if (crs == null)
				newDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);
			/*
			 * Write the features to the shapefile
			 */
			Transaction transaction = new DefaultTransaction("create");

			String typeName = newDataStore.getTypeNames()[0];
			SimpleFeatureSource featureSource = newDataStore
					.getFeatureSource(typeName);

			if (featureSource instanceof SimpleFeatureStore) {
				SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

				/*
				 * SimpleFeatureStore has a method to add features from a
				 * SimpleFeatureCollection object, so we use the
				 * ListFeatureCollection class to wrap our list of features.
				 */
				SimpleFeatureCollection collection = new ListFeatureCollection(
						features.get(0).getFeatureType(), features);
				featureStore.setTransaction(transaction);
				try {
					featureStore.addFeatures(collection);
					transaction.commit();

				} catch (Exception problem) {
					problem.printStackTrace();
					transaction.rollback();

				} finally {
					transaction.close();
				}
				// success!
			} else {
				logger.error(typeName + " does not support read/write access");
			}
		} catch (Exception e) {
			logger.error("Shapefile file published failed! Do you have multiple SRID in a single worksheet?");
		}
		// kml.marshal(outputFile);
		logger.info("Shapefile file published. Location:"
				+ outputFile.getAbsolutePath());
		return outputFile;
	}

	public File publishKML(String spatialDataFolder, String fileName)
			throws IOException {
		File outputFile = new File(spatialDataFolder + fileName + ".kml");

		final Kml kml = KmlFactory.createKml();
		final Folder folder = kml.createAndSetFolder()
				.withName(worksheet.getTitle()).withOpen(true);

		Style style = folder.createAndAddStyle().withId("karma");
		style.createAndSetIconStyle()
				.withScale(1.399999976158142)
				.withIcon(
						new Icon()
								.withHref("http://maps.google.com/mapfiles/ms/icons/blue-pushpin.png"));
		style.createAndSetLineStyle().withColor("501400FF").withWidth(2);
		style.createAndSetPolyStyle().withColor("5014F000");

		for (SimpleFeature pointFeature : pointFeatureList) {

			CoordinateReferenceSystem sourceCRS = pointFeature.getType()
					.getCoordinateReferenceSystem();
			CoordinateReferenceSystem targetCRS = null;
			try {
				targetCRS = CRS.decode("EPSG:4326", true);
			} catch (NoSuchAuthorityCodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Point p = (Point) SpatialReferenceSystemTransformationUtil
					.Transform((Geometry) pointFeature.getAttribute("GEOM"),
							sourceCRS, targetCRS);
			/*
			 * keep this for a while MathTransform transform=null; try {
			 * transform = CRS.findMathTransform(sourceCRS, targetCRS,true); }
			 * catch (FactoryException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); } Point p=null; try { Point temp =
			 * (Point)pointFeature.getAttribute("GEOM"); p =
			 * (Point)JTS.transform( temp, transform); } catch
			 * (MismatchedDimensionException e) { e.printStackTrace(); } catch
			 * (TransformException e) { e.printStackTrace(); }
			 */
			String htmlDescription = getHTMLDescription(pointFeature);
			folder.createAndAddPlacemark().withDescription(htmlDescription)
					.withVisibility(true).withStyleUrl("#karma")
					.createAndSetPoint()
					.withAltitudeMode(AltitudeMode.CLAMP_TO_GROUND)
					.addToCoordinates(p.getX() + "," + p.getY());
		}
		for (SimpleFeature lineFeature : lineFeatureList) {
			String htmlDescription = getHTMLDescription(lineFeature);

			CoordinateReferenceSystem sourceCRS = lineFeature.getType()
					.getCoordinateReferenceSystem();
			CoordinateReferenceSystem targetCRS = null;
			try {
				targetCRS = CRS.decode("EPSG:4326", true);
			} catch (NoSuchAuthorityCodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LineString line = (LineString) SpatialReferenceSystemTransformationUtil
					.Transform((Geometry) lineFeature.getAttribute("GEOM"),
							sourceCRS, targetCRS);

			List<de.micromata.opengis.kml.v_2_2_0.Coordinate> coordsList = new ArrayList<de.micromata.opengis.kml.v_2_2_0.Coordinate>();
			for (int i = 0; i < line.getNumPoints(); i++) {

				de.micromata.opengis.kml.v_2_2_0.Coordinate coord = new de.micromata.opengis.kml.v_2_2_0.Coordinate(
						line.getPointN(i).getX(), line.getPointN(i).getY());
				coordsList.add(coord);
			}
			folder.createAndAddPlacemark().withDescription(htmlDescription)
					.withVisibility(true).withStyleUrl("karma")
					.createAndSetLineString()
					.withAltitudeMode(AltitudeMode.CLAMP_TO_GROUND)
					.setCoordinates(coordsList);
		}
		for (SimpleFeature polygonFeature : polygonFeatureList) {
			String htmlDescription = getHTMLDescription(polygonFeature);

			CoordinateReferenceSystem sourceCRS = polygonFeature.getType()
					.getCoordinateReferenceSystem();
			CoordinateReferenceSystem targetCRS = null;
			try {
				targetCRS = CRS.decode("EPSG:4326", true);
			} catch (NoSuchAuthorityCodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Polygon polygon = (Polygon) SpatialReferenceSystemTransformationUtil
					.Transform((Geometry) polygonFeature.getAttribute("GEOM"),
							sourceCRS, targetCRS);

			Placemark placemark = folder.createAndAddPlacemark()
					.withDescription(htmlDescription).withVisibility(true)
					.withStyleUrl("karma");

			final de.micromata.opengis.kml.v_2_2_0.Polygon kmlPolygon = new de.micromata.opengis.kml.v_2_2_0.Polygon();
			placemark.setGeometry(kmlPolygon);

			kmlPolygon.setExtrude(true);
			kmlPolygon.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);

			final Boundary outerboundary = new Boundary();
			kmlPolygon.setOuterBoundaryIs(outerboundary);

			final LinearRing outerlinearring = new LinearRing();
			outerboundary.setLinearRing(outerlinearring);

			List<de.micromata.opengis.kml.v_2_2_0.Coordinate> outercoord = new ArrayList<de.micromata.opengis.kml.v_2_2_0.Coordinate>();
			outerlinearring.setCoordinates(outercoord);
			for (int i = 0; i < polygon.getExteriorRing().getNumPoints(); i++) {
				outercoord.add(new de.micromata.opengis.kml.v_2_2_0.Coordinate(
						polygon.getExteriorRing().getPointN(i).getX(), polygon
								.getExteriorRing().getPointN(i).getY()));
			}
			int numOfInnerBoundaries = polygon.getNumInteriorRing();
			for (int i = 0; i < numOfInnerBoundaries; i++) {
				final Boundary innerboundary = new Boundary();
				kmlPolygon.getInnerBoundaryIs().add(innerboundary);

				final LinearRing innerlinearring = new LinearRing();
				innerboundary.setLinearRing(innerlinearring);

				List<de.micromata.opengis.kml.v_2_2_0.Coordinate> innercoord = new ArrayList<de.micromata.opengis.kml.v_2_2_0.Coordinate>();
				innerlinearring.setCoordinates(innercoord);
				int numOfPoints = polygon.getInteriorRingN(i).getNumPoints();
				for (int j = 0; j < numOfPoints; j++)
					innercoord
							.add(new de.micromata.opengis.kml.v_2_2_0.Coordinate(
									polygon.getInteriorRingN(i).getPointN(j)
											.getX(), polygon
											.getInteriorRingN(i).getPointN(j)
											.getY()));
			}
		}
		final StringWriter out = new StringWriter();
		kml.marshal(out);
		String test = out.toString();
		Writer outUTF8 = null;
		try {
			outUTF8 = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile), "UTF8"));
			outUTF8.append(test);
			outUTF8.flush();
			outUTF8.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		logger.info("KML file published. Location:"
				+ outputFile.getAbsolutePath());
		return outputFile;
	}

	private String getHTMLDescription(SimpleFeature simpleFeature) {

		StringBuilder str = new StringBuilder();
		for (Property property : simpleFeature.getProperties()) {
			str.append("<b>" + property.getName() + "</b>: "
					+ property.getValue().toString() + " <br />");
		}
		return str.toString();
	}

	private void saveSpatialDataToZip(String shapefileFolder, String fileName) {
		try {
			String zipFile = shapefileFolder + fileName + ".zip";
			String sourceDirectory = shapefileFolder;

			// create byte buffer
			byte[] buffer = new byte[1024];

			// create object of FileOutputStream
			FileOutputStream fout = new FileOutputStream(zipFile);

			// create object of ZipOutputStream from FileOutputStream
			ZipOutputStream zout = new ZipOutputStream(fout);

			// create File object from directory name
			File dir = new File(sourceDirectory);

			// check to see if this directory exists
			if (!dir.isDirectory())
				System.out.println(sourceDirectory + " is not a directory");
			else {
				File[] files = dir.listFiles();

				for (int i = 0; i < files.length; i++) {
					System.out.println("Adding " + files[i].getName());
					if (files[i].getName().contains(".zip"))
						continue;
					// create object of FileInputStream for source file
					FileInputStream fin = new FileInputStream(files[i]);

					/*
					 * To begin writing ZipEntry in the zip file, use
					 * 
					 * void putNextEntry(ZipEntry entry) method of
					 * ZipOutputStream class.
					 * 
					 * This method begins writing a new Zip entry to the zip
					 * file and positions the stream to the start of the entry
					 * data.
					 */

					zout.putNextEntry(new ZipEntry(files[i].getName()));

					/*
					 * After creating entry in the zip file, actually write the
					 * file.
					 */
					int length;

					while ((length = fin.read(buffer)) > 0) {
						zout.write(buffer, 0, length);
					}

					/*
					 * After writing the file to ZipOutputStream, use
					 * 
					 * void closeEntry() method of ZipOutputStream class to
					 * close the current entry and position the stream to write
					 * the next entry.
					 */

					zout.closeEntry();
					// close the InputStream
					fin.close();
				}
			}
			// close the ZipOutputStream
			zout.close();
		} catch (IOException ioe) {
			logger.error("IOException :" + ioe);
		}
	}

	public boolean hasNoGeospatialData() {
		if (pointFeatureList.size() == 0 && lineFeatureList.size() == 0
				&& polygonFeatureList.size() == 0)
			return true;
		return false;
	}

}
