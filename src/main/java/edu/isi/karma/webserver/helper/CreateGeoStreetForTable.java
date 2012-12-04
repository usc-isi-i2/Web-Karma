package edu.isi.karma.webserver.helper;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CreateGeoStreetForTable {

	private Connection connection = null;
	private String osmFile_path;
	private Statement stmt = null;
	private ResultSet rs = null;

	public CreateGeoStreetForTable(Connection connection, String osmFile_path) {
		this.connection = connection;
		this.osmFile_path = osmFile_path;
		try {
			this.stmt = this.connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private <T> List<T> castList(Class<T> clazz, Collection<?> c) {
		List<T> list = new Vector<T>(c.size());
		for (Object object : c) {
			System.out.println(object.getClass());
			list.add(clazz.cast(object));
		}
		return list;
	}
	
	private <T> List<T> castIterator(Class<T> clazz, Iterator<?> i) {
		List<T> list = new Vector<T>();
		while(i.hasNext()) {
			list.add(clazz.cast(i.next()));
		}
		return list;
	}

	public String createGeoStreet() {
		CreateNodeDataForTable cnd = new CreateNodeDataForTable(
				this.connection, this.osmFile_path);
		cnd.createNodeDataforTable();// create a nodetalbe 

		try {
			rs = stmt.executeQuery("drop TABLE postgis.public.streets_geo");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}
		try {
			rs = stmt
					.executeQuery("CREATE TABLE postgis.public.streets_geo (Street_number integer PRIMARY KEY, way_id integer, way_type character varying, Street_name character varying, Street_name_Alias character varying, line geography(LINESTRING, 4326), lineAsText character varying, SRID integer)");

		} catch (SQLException ee) {
			ee.getStackTrace();
		}

		SAXReader saxReadering = new SAXReader();
		Document document = null;
		JSONObject obj=new JSONObject();
		JSONArray arr=new JSONArray();
		try {
			document = saxReadering.read(new File(osmFile_path));
		} catch (DocumentException e) {
			e.getStackTrace();
		}
		
		int ord = 1;
		int srid = 4326;
		List<Element> list = castList(Element.class, document.selectNodes("//osm/way"));
	    for (Element ele : list) {
			String Street_name = "Street_name";
			String Street_name_Alias = " ";
			String way_type = "way_type";
			String way_id = "way_id";
			String node_latlon = "";
			List<Attribute> ite = castIterator(Attribute.class, ele.attributeIterator());
			for (Attribute attribute : ite) {
				String name = attribute.getName();
				String value = attribute.getText();
				if (name.equals("id")) {
					way_id = value;
				}
			}

			int colm_nd = 1;
			float lats = 0;
			float lons = 0;
			List<Element> nods = castList(Element.class, ele.elements("nd"));
			for (Element elms:nods) {
				List<Attribute> iters = castIterator(Attribute.class, elms.attributeIterator());
				for (Attribute attribute : iters) {
					String name = attribute.getName();
					String value = attribute.getText();
					if (name.equals("ref")) {// ref值为way成员节点的node_id，所以需要提取每个id值，并查询nodestable表，获得相应的lat，lon；
						try {
							rs = stmt
									.executeQuery("select lat,lon from postgis.public.nodestable where id=\'"
											+ value + "\';");

							while (rs.next()) {
								lats = rs.getFloat("lat");
								lons = rs.getFloat("lon");
								if (colm_nd == 1) {
									node_latlon = node_latlon + lons + " "
											+ lats;
								} else {
									node_latlon = node_latlon + "," + lons
											+ " " + lats;
								}
							}

						} catch (SQLException ee) {
							ee.getStackTrace();
						}

					}
				}
				colm_nd = colm_nd + 1;
			}

			List<Element> nodes = castList(Element.class, ele.elements("tag"));

			for (Element elm_tag : nodes) {	
				List<Attribute> itLists = castIterator(Attribute.class, elm_tag.attributeIterator());
				for (Iterator<Attribute> iters = itLists.iterator(); iters
						.hasNext();) {
					Attribute attributes = (Attribute) iters.next();
					String value = attributes.getText();
					if (value.equals("highway")) {
						Attribute attribute_highway = (Attribute) iters.next();
						way_type = attribute_highway.getText();

					} else if (value.equals("name")) {
						Attribute attribute_name = (Attribute) iters.next();
						Street_name = attribute_name.getText();
						System.out.println("Street Name :" + Street_name);

					} else if (value.equals("name_1")) {
						Attribute attribute_alias = (Attribute) iters.next();
						Street_name_Alias = attribute_alias.getText();
					}
				}
			}
			if (!Street_name.equals("Street_name")) {
				if (way_type.equals("secondary") || way_type.equals("motorway")
						|| way_type.equals("pedestrian")
						|| way_type.equals("residential")
						|| way_type.equals("footway")) {
					try {
						rs = stmt
								.executeQuery("insert into postgis.public.streets_geo(Street_number) values ("
										+ ord + ")");

					} catch (SQLException ee) {
						ee.getStackTrace();
					}

					try {
						rs = stmt
								.executeQuery("update postgis.public.streets_geo set way_id=\'"
										+ way_id
										+ "\', way_type=\'"
										+ way_type
										+ "\',Street_name=\'"
										+ Street_name
										+ "\',Street_name_Alias=\'"
										+ Street_name_Alias
										+ "\',line=ST_GeomFromText(\'SRID=4326; LINESTRING("
										+ node_latlon
										+ ")\'),lineAsText=\' LINESTRING("
										+ node_latlon
										+ ") \',srid=\'"+
										srid+"\' where Street_number=" + ord);

					} catch (SQLException ee) {
						ee.getStackTrace();
					}
					
					try {
						obj.put("Street_Name", Street_name);
						obj.put("Way_Id", way_id);
						obj.put("Way_Type", way_type);						
						obj.put("Street_Name", Street_name);
						obj.put("Polyline", "LINESTRING("+node_latlon+")");
						obj.put("Srid", srid);
						arr.put(obj);
						obj=new JSONObject();
					} catch (JSONException e) {
						e.printStackTrace();
					}
					ord = ord + 1;
				}
			}

		}// while //osm/way;

		try {
			rs = stmt
					.executeQuery("	Copy (Select * From postgis.public.streets_geo) To '/tmp/streets_geo.csv' CSV HEADER;");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}
		String jsonOutput= arr.toString();
		return jsonOutput;
	}

}
