package edu.isi.karma.webserver.helper;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class CreateNodeDataForTable {

	private Connection connection = null;
	private String osmFile_path;
	private Statement stmt = null;
	private ResultSet rs = null;

	public CreateNodeDataForTable(Connection connection, String osmFile_path) {
		this.connection = connection;
		this.osmFile_path = osmFile_path;
		try {
			this.stmt = this.connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/*
	 * Create the table for nodes
	 */
	public void createNodeDataforTable() {

		try {
			rs = stmt.executeQuery("drop TABLE postgis.public.nodestable");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}
		try {
			rs = stmt
					.executeQuery("CREATE TABLE postgis.public.nodestable (ord integer PRIMARY KEY, id integer, lat double precision, lon double precision, uid integer, visible boolean, version integer, changeset integer, users character varying, timestamp timestamp without time zone)");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}


		try {
			rs = stmt.executeQuery("drop TABLE postgis.public.nodes_tag");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}
		try {
			rs = stmt
					.executeQuery("CREATE TABLE postgis.public.nodes_tag (number integer PRIMARY KEY, node_id integer, tag_order integer, k character varying, v character varying)");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}

	    
		int ord = 1;
		int columofnodes_tag = 1;
		SAXReader saxReadering = new SAXReader();
		try {
			Document document = saxReadering.read(new File(this.osmFile_path));
			List list = document.selectNodes("//osm/node");
			Iterator iter = list.iterator();
			String node_id = null;
			while (iter.hasNext()) {
				Element ele = (Element) iter.next();
				String ss = ele.getName();
				try {
					rs = stmt
							.executeQuery("insert into postgis.public.nodestable (ord) values ("
									+ ord + ")");
				} catch (SQLException ee) {
					ee.getStackTrace();
				}

				for (Iterator ite = ele.attributeIterator(); ite.hasNext();) {
					Attribute attribute = (Attribute) ite.next();
					String name = attribute.getName();
					String value = attribute.getText();
					if (name.equals("user")) {
						name = "users";
					} else if (name.equals("id")) {
						node_id = value;
					}
					try {
						rs = stmt
								.executeQuery("update postgis.public.nodestable set "
										+ name
										+ "=\'"
										+ value
										+ "\' where ord=" + ord);
					} catch (SQLException ee) {
						ee.getStackTrace();
					}

				}


				List nods = ele.elements("tag");
				int colm_tag = 1;
				for (Iterator its = nods.iterator(); its.hasNext();) {

					try {
						rs = stmt
								.executeQuery("insert into postgis.public.nodes_tag (number) values ("
										+ columofnodes_tag + ")");
					} catch (SQLException ee) {
						ee.getStackTrace();
					}

					try {
						rs = stmt
								.executeQuery("update postgis.public.nodes_tag set node_id=\'"
										+ node_id
										+ "\'where number=\'"
										+ columofnodes_tag + "\'");
					} catch (SQLException ee) {
						ee.getStackTrace();
					}

					try {
						rs = stmt
								.executeQuery("update postgis.public.nodes_tag set tag_order=\'"
										+ colm_tag
										+ "\'where number=\'"
										+ columofnodes_tag + "\'");
					} catch (SQLException ee) {
						ee.getStackTrace();
					}

					Element elms = (Element) its.next();
					for (Iterator iters = elms.attributeIterator(); iters.hasNext();) {
						Attribute attribute = (Attribute) iters.next();
						String name = attribute.getName();
						String value = attribute.getText();

						try {
							rs = stmt
									.executeQuery("update postgis.public.nodes_tag set "
											+ name
											+ "=\'"
											+ value
											+ "\' where number=\'"
											+ columofnodes_tag + "\'");
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
