package edu.isi.karma.er.helper;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;

import edu.isi.karma.er.helper.entity.InputStruct;

public class ExtractCoordinates {
	public ExtractCoordinates() {

	}

	public static InputStruct[] realExtractCoordinates(String directory_path,
			InputStruct[] struct_building) {
		Model model = TDBFactory.createDataset(directory_path)
				.getDefaultModel();
		String building_name = null;
		String point = null;
		StmtIterator iter = model.listStatements();
		int i = 0;
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement(); // get next statement
			Resource subject = stmt.getSubject(); // get the subject
			Property predicate = stmt.getPredicate(); // get the predicate
			RDFNode object = stmt.getObject(); // get the object
			if (subject.toString().indexOf("Building_") != -1
					&& predicate.toString().indexOf("buildingName") != -1) {
				building_name = object.toString();
				building_name = building_name.replaceAll(",", "&");
				((InputStruct) struct_building[i]).setResource(subject);
				((InputStruct) struct_building[i])
						.setNameforBuilding(building_name);
				StmtIterator iter_building = subject.listProperties();
				while (iter_building.hasNext()) {
					Statement statement_building = iter_building
							.nextStatement();
					Property pro_building = (Property) statement_building
							.getPredicate();
					RDFNode object_building = statement_building.getObject();
					if (pro_building.toString().indexOf("hasPoint") != -1) {
						point = object_building.toString();
						Resource subject_point = object_building.asResource();
						StmtIterator iter_point = subject_point
								.listProperties();
						while (iter_point.hasNext()
								&& subject_point.toString().indexOf(point) != -1) {
							Statement statement_point = iter_point
									.nextStatement();
							Property pro_onepoint = (Property) statement_point
									.getPredicate();
							RDFNode object_onepoint = statement_point
									.getObject();
							if (pro_onepoint.toString().indexOf(
									"hasGeocoordinates") != -1) {
								String PointGeocoordinates = object_onepoint
										.toString();
								Resource subject_PointGeocoordinates = object_onepoint
										.asResource();
								StmtIterator iter_PointGeocoordinates = subject_PointGeocoordinates
										.listProperties();
								while (iter_PointGeocoordinates.hasNext()
										&& subject_PointGeocoordinates
												.toString().indexOf(
														PointGeocoordinates) != -1) {
									Statement statement_PointGeocoordinates = iter_PointGeocoordinates
											.nextStatement();
									Property pro_onePointGeocoordinates = (Property) statement_PointGeocoordinates
											.getPredicate();
									RDFNode object_onePointGeocoordinates = statement_PointGeocoordinates
											.getObject();
									if (pro_onePointGeocoordinates.toString()
											.indexOf("yInDecimalLatitude") != -1) {
										String YGeocoordinates = object_onePointGeocoordinates
												.toString();
										((InputStruct) struct_building[i])
												.setY(Double
														.parseDouble(YGeocoordinates));
									} else if (pro_onePointGeocoordinates
											.toString().indexOf(
													"xInDecimalLongitude") != -1) {
										String XGeocoordinates = object_onePointGeocoordinates
												.toString();
										((InputStruct) struct_building[i])
												.setX(Double
														.parseDouble(XGeocoordinates));
									}
								}
							}
						}
					} else if (pro_building.toString().indexOf("hasPolygon") != -1) {
						String polygon = object_building.toString();
						Resource subject_polygon = object_building.asResource();
						StmtIterator iter_polygon = subject_polygon
								.listProperties();
						while (iter_polygon.hasNext()
								&& subject_polygon.toString().indexOf(polygon) != -1) {
							Statement statement_polygon = iter_polygon
									.nextStatement();
							Property pro_onepolygon = (Property) statement_polygon
									.getPredicate();
							RDFNode object_onepolygon = statement_polygon
									.getObject();
							if (pro_onepolygon.toString().indexOf(
									"hasGeocoordinates") != -1) {
								String PolygonGeocoordinates = object_onepolygon
										.toString();
								Resource subject_PolygonGeocoordinates = object_onepolygon
										.asResource();
								StmtIterator iter_PolygonGeocoordinates = subject_PolygonGeocoordinates
										.listProperties();
								while (iter_PolygonGeocoordinates.hasNext()
										&& subject_PolygonGeocoordinates
												.toString().indexOf(
														PolygonGeocoordinates) != -1) {
									Statement statement_PolygonGeocoordinates = iter_PolygonGeocoordinates
											.nextStatement();
									Property pro_onePolygonGeocoordinates = (Property) statement_PolygonGeocoordinates
											.getPredicate();
									RDFNode object_onePolygonGeocoordinates = statement_PolygonGeocoordinates
											.getObject();
									if (pro_onePolygonGeocoordinates.toString()
											.indexOf("wellKnownBinary") != -1) {

										String polygonGeocoordinates = object_onePolygonGeocoordinates
												.toString();
										((InputStruct) struct_building[i])
												.setPolygon(polygonGeocoordinates);
									}
								}
							}
						}
					}
				}
				i++;
			}
		}
		return struct_building;
	}
}
