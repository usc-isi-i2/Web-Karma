package edu.isi.karma.er.test;

import java.io.*;
import java.util.List;

import edu.isi.karma.er.aggregator.impl.AggregatorImplByMeters;
import edu.isi.karma.er.aggregator.impl.AggregatorImplByUnits;
import edu.isi.karma.er.helper.ExtractCoordinates;
import edu.isi.karma.er.helper.WriteCSVImpl;
import edu.isi.karma.er.helper.entity.InputStruct;
import edu.isi.karma.er.helper.entity.MultiScore;

import edu.isi.karma.er.helper.entity.ResultRecord;
import edu.isi.karma.er.helper.ConnectPostgis;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

import java.sql.Connection;
import java.lang.String;

public class BuildingMatching {

	public static int struct_number = 200;

	private static void setupDirectoryModel(String DBPEDIA_DIRECTORY,
			String DBPEDIA_FILE) {

		Dataset dataset = TDBFactory.createDataset(DBPEDIA_DIRECTORY);
		Model model = dataset.getDefaultModel();
		FileManager.get().readModel(model, DBPEDIA_FILE);
		dataset.close();
	}


	public static void main(String[] args) {

		String fileAddress1 = "/Users/yzhang/Downloads/building_openstreetmap.n3";
		String fileAddress2 = "/Users/yzhang/Downloads/building_wikimapia.n3";
		String directory1 = "/Users/yzhang/Downloads/Repository/openstreetmap";
		String directory2 = "/Users/yzhang/Downloads/Repository/wikimapia";
		/*
		 * run this statement only once,unless you revised the n3 file;
		 */
		// setupDirectoryModel(directory1, fileAddress1);
		// setupDirectoryModel(directory2, fileAddress2);

		ResultRecord[] matchedPair = new ResultRecord[struct_number];
		for (int j = 0; j < struct_number; j++) {
			matchedPair[j] = new ResultRecord();
		}

		Connection connection = null;
		/*
		 * Connecting to database;
		 */
		ConnectPostgis conpgis = new ConnectPostgis();
		connection = (Connection) conpgis.ConnectingPostgis();

		WriteCSVImpl wcsv = new WriteCSVImpl();
		wcsv.WriteCSVImpl();

		InputStruct resource1[] = new InputStruct[struct_number];
		for (int i = 0; i < struct_number; i++) {
			resource1[i] = new InputStruct();

		}
		InputStruct resource2[] = new InputStruct[struct_number];
		for (int i = 0; i < struct_number; i++) {
			resource2[i] = new InputStruct();
		}

		resource1 = ExtractCoordinates.realExtractCoordinates(directory1, resource1);
		resource2 = ExtractCoordinates.realExtractCoordinates(directory2, resource2);

		/*
		 * Matching by Projected Units;
		 */

		 //AggregatorImplByUnits aggImpl=new AggregatorImplByUnits(connection);

		/*
		 * Matching by Projected Meters;
		 */

		AggregatorImplByMeters aggImpl = new AggregatorImplByMeters(connection);

		MultiScore received = new MultiScore();

		for (int i = 0; i < struct_number; i++) {

			if (((InputStruct) resource1[i]).getX() != 0
					|| ((InputStruct) resource1[i]).getY() != 0
					|| ((InputStruct) resource1[i]).getPolygon() != null) {

				matchedPair[i].setRes((InputStruct) resource1[i]);

				for (int j = 0; j < struct_number; j++) {

					if (((InputStruct) resource2[j]).getX() != 0
							|| ((InputStruct) resource2[j]).getY() != 0
							|| ((InputStruct) resource2[j]).getPolygon() != null) {

						received = aggImpl.match(
								(InputStruct) matchedPair[i].getRes(),
								(InputStruct) resource2[j]);
						received.setSrcSubj((InputStruct) resource1[i]);
						received.setDstSubj((InputStruct) resource2[j]);
						double sim = received.getScoreList().get(0)
								.getSimilarity();
						received.setFinalScore(sim);
						matchedPair[i].addMultiScore(received);

					} else {
						break;
					}

				}

				for (int j = 0; j < matchedPair[i].getLimit(); j++) {
					wcsv.WriteOneLine(((InputStruct) matchedPair[i].getRes())
							.getResource().toString()
							+ ","
							+ ((InputStruct) matchedPair[i].getRes())
									.getNameforBuilding()
							+ ","
							+ ((InputStruct) matchedPair[i].getRes()).getX()
							+ ","
							+ ((InputStruct) matchedPair[i].getRes()).getY()
							+ ","
							+ ((InputStruct) matchedPair[i].getRes())
									.getPolygon()
							+ ","
							+ ((InputStruct) ((matchedPair[i].getRankList()
									.get(j)).getDstSubj())).getResource()
									.toString()
							+ ","
							+ ((InputStruct) ((matchedPair[i].getRankList()
									.get(j)).getDstSubj()))
									.getNameforBuilding()
							+ ","
							+ ((InputStruct) ((matchedPair[i].getRankList()
									.get(j)).getDstSubj())).getX()
							+ ","
							+ ((InputStruct) ((matchedPair[i].getRankList()
									.get(j)).getDstSubj())).getY()
							+ ","
							+ ((InputStruct) ((matchedPair[i].getRankList()
									.get(j)).getDstSubj())).getPolygon()
							+ ","
							+ ((matchedPair[i].getRankList().get(j)))
									.getScoreList().get(0).getFreq()
							+ ","
							+ ((matchedPair[i].getRankList().get(j)))
									.getScoreList().get(0).getSimilarity());
				}

			} else {
				break;
			}

		}
		wcsv.Close();
		try {
			connection.close();
		} catch (Exception ex) {
			ex.getMessage();
		}

	}

}
