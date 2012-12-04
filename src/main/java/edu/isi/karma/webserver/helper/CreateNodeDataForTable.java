package edu.isi.karma.webserver.helper;

import java.io.File;
import java.sql.Connection;
//import java.sql.ResultSet;
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

public class CreateNodeDataForTable {

	private Connection connection = null;
	private String osmFile_path;
	private Statement stmt = null;

	public CreateNodeDataForTable(Connection connection, String osmFile_path) {
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
		while (i.hasNext()) {
			list.add(clazz.cast(i.next()));
		}
		return list;
	}

	/*
	 * Create the table for nodes
	 */
	public void createNodeDataforTable() {

		try {
			stmt.executeQuery("drop TABLE postgis.public.nodestable");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}
		try {
			stmt.executeQuery("CREATE TABLE postgis.public.nodestable (ord integer PRIMARY KEY, id integer, lat double precision, lon double precision, uid integer, visible boolean, version integer, changeset integer, users character varying, timestamp timestamp without time zone)");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}

		try {
			stmt.executeQuery("drop TABLE postgis.public.nodes_tag");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}
		try {
			stmt.executeQuery("CREATE TABLE postgis.public.nodes_tag (number integer PRIMARY KEY, node_id integer, tag_order integer, k character varying, v character varying)");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}

		int ord = 1;
		int columofnodes_tag = 1;
		SAXReader saxReadering = new SAXReader();
		try {
			Document document = saxReadering.read(new File(this.osmFile_path));
			String node_id = null;
			List<Element> list = castList(Element.class,
					document.selectNodes("//osm/node"));
			for (Element ele : list) {
				try {

					stmt.executeQuery("insert into postgis.public.nodestable (ord) values ("
							+ ord + ")");
				} catch (SQLException ee) {
					ee.getStackTrace();
				}

				List<Attribute> ite = castIterator(Attribute.class,
						ele.attributeIterator());
				for (Attribute attribute : ite) {
					String name = attribute.getName();
					String value = attribute.getText();
					if (name.equals("user")) {
						name = "users";
					} else if (name.equals("id")) {
						node_id = value;
					}
					try {
						stmt.executeQuery("update postgis.public.nodestable set "
								+ name + "=\'" + value + "\' where ord=" + ord);

					} catch (SQLException ee) {
						ee.getStackTrace();
					}

				}

				int colm_tag = 1;
				List<Element> nods = castList(Element.class,
						ele.elements("tag"));
				for (Element elms : nods) {
					try {
						stmt.executeQuery("insert into postgis.public.nodes_tag (number) values ("
								+ columofnodes_tag + ")");
					} catch (SQLException ee) {
						ee.getStackTrace();
					}

					try {
						stmt.executeQuery("update postgis.public.nodes_tag set node_id=\'"
								+ node_id
								+ "\', tag_order=\'"
								+ colm_tag
								+ "\' where number=\'"
								+ columofnodes_tag
								+ "\'");
					} catch (SQLException ee) {
						ee.getStackTrace();
					}

					List<Attribute> itLists = castIterator(Attribute.class,
							elms.attributeIterator());
					for (Iterator<Attribute> iters = itLists.iterator(); iters
							.hasNext();) {
						Attribute attribute = (Attribute) iters.next();
						String name = attribute.getName();
						String value = attribute.getText();
						try {
							stmt.executeQuery("update postgis.public.nodes_tag set "
									+ name
									+ "=\'"
									+ value
									+ "\' where number=\'"
									+ columofnodes_tag
									+ "\'");
						} catch (SQLException ee) {
							ee.getStackTrace();
						}

					}
					columofnodes_tag = columofnodes_tag + 1;
					colm_tag = colm_tag + 1;
				}

				ord = ord + 1;
			}
		} catch (DocumentException e) {
			e.getStackTrace();
		}

	}

}
