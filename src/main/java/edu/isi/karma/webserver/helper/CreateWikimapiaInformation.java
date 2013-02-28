package edu.isi.karma.webserver.helper;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;


import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CreateWikimapiaInformation {
	private Connection connection = null;
	private String osmFile_path;
	private Statement stmt = null;

	// private ResultSet rs = null;

	public CreateWikimapiaInformation(Connection connection, String osmFile_path) {
		this.connection = connection;
		this.osmFile_path = osmFile_path;
		try {
			this.stmt = this.connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

//	private <T> List<T> castList(Class<T> clazz, Collection<?> c) {
//		List<T> list = new Vector<T>(c.size());
//		for (Object object : c) {
//			list.add(clazz.cast(object));
//		}
//		return list;
//	}
//
//	private <T> List<T> castIterator(Class<T> clazz, Iterator<?> i) {
//		List<T> list = new Vector<T>();
//		while (i.hasNext()) {
//			list.add(clazz.cast(i.next()));
//		}
//		return list;
//	}

	public String createWikiMapiaTable() {
		// 为wikimapia创建表，上传到karma，地点为El Segundo；
		// （1）首先创建一个Node表；如果之前已有，需要先删除前面的；
		try {// 删除已有的；
		// rs=stmt.executeQuery("drop TABLE dbb.public.nodestable");
			this.stmt
					.executeQuery("drop TABLE postgis.public.WikiMapia_table_El");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}
		try {// 创建新的；
		// rs=stmt.executeQuery("CREATE TABLE dbb.public.nodestable (ord integer PRIMARY KEY, id integer, lat double precision, lon double precision, uid integer, visible boolean, version integer, changeset integer, users character varying, timestamp timestamp without time zone)");
		// create table place (id serial, lat numeric, lon
		// numeric,polygon poly， place_id integer, name varchar)
		// rs=stmt.executeQuery("CREATE TABLE postgis.public.WikiMapia_table (ord integer PRIMARY KEY, place_id integer, place_name character varying, lat double precision, lon double precision, polygon geography(POLYGON))");

			stmt.executeQuery("CREATE TABLE postgis.public.WikiMapia_table_El (ord integer PRIMARY KEY, place_id integer, place_name character varying, lat double precision, lon double precision, polygon geography(POLYGON, 4326),coordinate_system character varying,srid integer)");
			// rs=stmt.executeQuery("CREATE TABLE postgis.public.WikiMapia_table (ord integer PRIMARY KEY, place_id integer, place_name text, lat double precision, lon double precision)");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}

		// (2)读入xml文件（openstreetmap中的usc区域数据），存入每个节点的属性值，node的子元素tag及其属性值创建在另外的table中；
		int ord = 1;

		String coordinate_system = "WGS84";
		int srid = 4326;
		// String highway=" ";
		SAXReader saxReadering = new SAXReader();
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();
		try {

			Document document = saxReadering.read(new File(this.osmFile_path));
			// Document document=saxReadering.read(new
			// File("/Users/yzhang/Downloads/USC_wikimapia.xml"));
			 List list = document.selectNodes("//folder/place");
			Iterator iter = list.iterator();
			System.out.println("begin");
			// String backid=null;

			while (iter.hasNext()) { // 循环查询每一个place节点；

				String place_id = null;
				String name_str = null;
				System.out.println("First hahaaaaaaa");
				Element ele = (Element) iter.next();
				String ss = ele.getName();
				System.out.println(ss);
				try {// 向表中插入序号——ord列 ；
				// rs=stmt.executeQuery("insert into dbb.public.nodestable (ord) values ("+ord+")");
					stmt.executeQuery("insert into postgis.public.WikiMapia_table_El (ord) values ("
							+ ord + ")");
				} catch (SQLException ee) {
					ee.getStackTrace();
				}
				// 填入place节点属性的值 ；
				for (Iterator ite = ele.attributeIterator(); ite.hasNext();) {
					Attribute attribute = (Attribute) ite.next();
					String name = attribute.getName();
					String value = attribute.getText();
					System.out
							.append("Attribute: " + name + "=" + value + "**");
					if (name.equals("id")) {
						place_id = value;
					}
					try {
						// rs=stmt.executeQuery("update dbb.public.nodestable set "+name+"=\'"+value+"\' where ord="+ord);
						stmt.executeQuery("update postgis.public.WikiMapia_table_El set place_id=\'"
								+ place_id + "\' where ord=" + ord);
					} catch (SQLException ee) {
						ee.getStackTrace();
					}

				}

				// 88888888888-----tag
				// 向nodes_tag表填充数据；
				// ******node元素所包含的所有tag节点元素，提取其tag节点的属性值；
				List nodes = ele.elements("name");

				for (Iterator its = nodes.iterator(); its.hasNext();) { // 在一个place元素的所有name之内；
					Element elm_name = (Element) its.next();
					name_str = elm_name.getText();
					System.out.println("place_id=" + place_id);
					System.out.println("name=" + name_str);
					try {
						// rs=stmt.executeQuery("update dbb.public.nodestable set "+name+"=\'"+value+"\' where ord="+ord);
						stmt.executeQuery("update postgis.public.WikiMapia_table_El set place_name=\'"
								+ name_str.toString() + "\' where ord=" + ord);
					} catch (SQLException ee) {
						ee.getStackTrace();
					}

				}

				List nodes_loc = ele.elements("location");
				String longitude = null;
				String latitude = null;
				// double latitude=0.0;
				for (Iterator its = nodes_loc.iterator(); its.hasNext();) { // 在一个place元素的所有location之内；
					Element elm_loc = (Element) its.next();
					List nodes_lon = elm_loc.elements("lon");
					for (Iterator it = nodes_lon.iterator(); it.hasNext();) {
						Element elm_lon = (Element) it.next();
						longitude = elm_lon.getText();
						System.out.println("place_id=" + place_id);
						System.out.println("lon=" + longitude);
						try {
							// rs=stmt.executeQuery("update dbb.public.nodestable set "+name+"=\'"+value+"\' where ord="+ord);
							stmt.executeQuery("update postgis.public.WikiMapia_table_El set lon=\'"
									+ longitude + "\' where ord=" + ord);
						} catch (SQLException ee) {
							ee.getStackTrace();
						}
					}

					List nodes_lat = elm_loc.elements("lat");
					for (Iterator it = nodes_lat.iterator(); it.hasNext();) {
						Element elm_lat = (Element) it.next();
						latitude = elm_lat.getText();
						System.out.println("place_id=" + place_id);
						System.out.println("lat=" + latitude);
						try {
							// rs=stmt.executeQuery("update dbb.public.nodestable set "+name+"=\'"+value+"\' where ord="+ord);
							stmt.executeQuery("update postgis.public.WikiMapia_table_El set lat=\'"
									+ latitude + "\' where ord=" + ord);
						} catch (SQLException ee) {
							ee.getStackTrace();
						}
					}
				}

				List nodes_polygon = ele.elements("polygon");
				String lon_lat = " ";

				// double latitude=0.0;
				for (Iterator its = nodes_polygon.iterator(); its.hasNext();) { // 在一个place元素的所有polygon之内；
					Element elm_polygon = (Element) its.next();

					List nodes_point = elm_polygon.elements("point");
					int point = 1;
					String x = " ";
					String y = " ";
					String firstx = " ";
					String firsty = " ";

					for (Iterator it = nodes_point.iterator(); it.hasNext();) {
						Element elm_point = (Element) it.next();
						// 每一个point得到一对x,y

						// 填入point节点属性的值 ；
						for (Iterator ite = elm_point.attributeIterator(); ite
								.hasNext();) {
							Attribute attribute = (Attribute) ite.next();
							String name = attribute.getName();
							String value = attribute.getText();
							System.out.append("Attribute: " + name + "="
									+ value + "**");
							if (name.equals("x")) {
								x = value;
							} else if (name.equals("y")) {
								y = value;
							}

						}
						if (point == 1) {
							lon_lat = lon_lat + x + " " + y;
							firstx = x;
							firsty = y;
							point = point + 1;
						} else {
							lon_lat = lon_lat + "," + x + " " + y;

							point = point + 1;
						}

					}

					lon_lat = lon_lat + "," + firstx + " " + firsty;

				}

				System.out.println("AAAA:");
				System.out.println("lontal:::" + lon_lat);

				try {// 将上面得到的每个way的所有nd子元素的坐标加入到数据中。
				// System.out.println("update postgis.public.ways_geo set line=ST_GeomFromText(\'LINESTRING("+node_latlon+")\');");
				// rs=stmt.executeQuery("update postgis.public.ways_geo set line=ST_GeomFromText(\'LINESTRING("+node_latlon+")\') where number="+ord+"");
					stmt.executeQuery("update postgis.public.WikiMapia_table_El set polygon=ST_GeographyFromText(\'SRID=4326; POLYGON(("
							+ lon_lat
							+ "))\'),coordinate_system=\'"
							+ coordinate_system
							+ "\' ,srid=\'"
							+ srid
							+ "\' where ord=\'" + ord + "\'");
					// rs=stmt.executeQuery("update postgis.public.WikiMapia_table set polygon=ST_GeomFromText(\'POLYGON(("+lon_lat+"))\') where ord="+ord);
					// System.out.println("update postgis.public.ways_geo set line=ST_GeomFromText(\'LINESTRING("+node_latlon+")\');");
				} catch (SQLException ee) {
					ee.getStackTrace();
				}

				try {
					obj.put("Street_Name", name_str);
					obj.put("place_id", place_id);
					obj.put("polygon", "POLYGON((" + lon_lat + "))");
					obj.put("coordinate_system", coordinate_system);
					obj.put("Srid", srid);
					arr.put(obj);
					obj = new JSONObject();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				ord = ord + 1;
			}
		} catch (DocumentException e) {
			e.getStackTrace();
		}
		String jsonOutput = arr.toString();
		return jsonOutput;
	}

}