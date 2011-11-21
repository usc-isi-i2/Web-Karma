package edu.isi.karma.modeling.semantictypes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Worksheet;

public class SemanticTypeUtil {

	private static Logger logger = LoggerFactory
			.getLogger(SemanticTypeUtil.class);

	// TODO Make this parameter configurable through web.xml
	private final static int TRAINING_EXAMPLE_MAX_COUNT = 100;

	public static ArrayList<String> getTrainingExamples(Worksheet worksheet,
			HNodePath path) {
		Collection<Node> nodes = new ArrayList<Node>();
		worksheet.getDataTable().collectNodes(path, nodes);

		ArrayList<String> nodeValues = new ArrayList<String>();
		for (Node n : nodes) {
			String nodeValue = n.getValue().asString().trim();
			if (!nodeValue.equals(""))
				nodeValues.add(nodeValue);
		}

		// Get rid of duplicate strings by creating a set over the list
		HashSet<String> valueSet = new HashSet<String>(nodeValues);
		nodeValues.clear();
		nodeValues.addAll(valueSet);

		// Shuffling the values so that we get randomly chosen values to train
		Collections.shuffle(nodeValues);

		if (nodeValues.size() > TRAINING_EXAMPLE_MAX_COUNT) {
			ArrayList<String> subset = new ArrayList<String>();
			// SubList method of ArrayList causes ClassCast exception
			for (int i = 0; i < TRAINING_EXAMPLE_MAX_COUNT; i++)
				subset.add(nodeValues.get(i));
			return subset;
		}

//		System.out.println("Examples: " + nodeValues);
		return nodeValues;
	}

	public static void prepareCRFModelHandler() throws IOException {
		File file = new File("CRF_Model.txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		boolean result = CRFModelHandler.readModelFromFile("CRF_Model.txt");

		if (!result)
			logger.error("Error occured while reading CRF Model!");
	}

	public static String removeNamespace(String uri) {
		if(uri.contains("#"))
			uri = uri.split("#")[1];
		else if(uri.contains("/"))
			uri = uri.substring(uri.lastIndexOf("/")+1);
		return uri;
	}

}
