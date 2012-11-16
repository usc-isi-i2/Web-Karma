package edu.isi.karma.er.test;

import edu.isi.karma.er.helper.ExtractCoordinates;
import edu.isi.karma.er.helper.CalculateResultScore;
import edu.isi.karma.er.helper.entity.InputStruct;
import edu.isi.karma.er.helper.entity.ResultRecord;
import edu.isi.karma.er.helper.ConnectPostgis;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;
import java.sql.Connection;
import java.lang.String;

public class BuildingMatching {

	public static int struct_number = 200;

	public static void setupDirectoryModel(String DBPEDIA_DIRECTORY,
			String DBPEDIA_FILE) {

		Dataset dataset = TDBFactory.createDataset(DBPEDIA_DIRECTORY);
		Model model = dataset.getDefaultModel();
		FileManager.get().readModel(model, DBPEDIA_FILE);
		dataset.close();
	}

	public static void main(String[] args) {

		String directory1 = "/Users/yzhang/Downloads/Repository/openstreetmap";
		String directory2 = "/Users/yzhang/Downloads/Repository/wikimapia";
		String csvFileAddress = "/Users/yzhang/Downloads/BuildingMatchedResult.csv";
		Connection connection = null;
		/*
		 * run this statement only once,unless you revised the n3 file;
		 */
		// setupDirectoryModel(directory1, fileAddress1);
		// setupDirectoryModel(directory2, fileAddress2);

		ResultRecord[] matchedPair = new ResultRecord[struct_number];
		for (int j = 0; j < struct_number; j++) {
			matchedPair[j] = new ResultRecord();
		}


		/*
		 * Connecting to database;
		 */
		ConnectPostgis conpgis = new ConnectPostgis();
		connection = (Connection) conpgis.ConnectingPostgis();

		InputStruct resource1[] = new InputStruct[struct_number];
		for (int i = 0; i < struct_number; i++) {
			resource1[i] = new InputStruct();

		}
		InputStruct resource2[] = new InputStruct[struct_number];
		for (int i = 0; i < struct_number; i++) {
			resource2[i] = new InputStruct();
		}

		resource1 = ExtractCoordinates.realExtractCoordinates(directory1,
				resource1);
		resource2 = ExtractCoordinates.realExtractCoordinates(directory2,
				resource2);

		/*
		 * Matching by Projected Units;
		 */

		// AggregatorImplByUnits aggImpl=new AggregatorImplByUnits(connection);

		/*
		 * Matching by Projected Meters;
		 */

		CalculateResultScore ors=new CalculateResultScore(connection,resource1,resource2,struct_number, matchedPair,csvFileAddress );		
        ors.calculateResults();

		try {
			connection.close();
		} catch (Exception ex) {
			ex.getMessage();
		}
	}
}
