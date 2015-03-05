package edu.isi.karma.research.modeling;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.Charsets;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.alignment.NodeIdFactory;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.ObjectPropertyType;
import edu.isi.karma.util.RandomGUID;

public class PatternReader {

	static Logger logger = LoggerFactory.getLogger(PatternReader.class);
	private static String CLASS_PREFIX = "c";
	private static String DATA_PROPERTY_PREFIX = "dp";
	private static String OBJECT_PROPERTY_PREFIX = "op";
	private static String COUNT_COLUMN = "count";
	
	public static Map<String, Set<String>> createPatternIndex(Collection<Pattern> patterns) {
		
		Map<String, Set<String>> invertedIndex = new HashMap<String, Set<String>>();
		if (patterns != null) {
			for (Pattern p : patterns) {
				if (p.getTypes() == null) continue;
				for (String t : p.getTypes()) {
					Set<String> patternIds = invertedIndex.get(t);
					if (patternIds == null) {
						patternIds = new HashSet<String>();
						invertedIndex.put(t, patternIds);
					}
					patternIds.add(p.getId());
				}
			}
		}
		return invertedIndex;
	}
	
	public static Map<String, Pattern> importPatterns(String patternDirectoryPath) {
		
		Map<String, Pattern> patterns = new HashMap<String, Pattern>();
		
		File patternDir = new File(patternDirectoryPath);
		if (patternDir.exists()) {
			File[] patternFiles = patternDir.listFiles();
			for (File file : patternFiles) {
				try {
					CSVParser csvParser = CSVParser.parse(file, Charsets.UTF_8, CSVFormat.RFC4180.withHeader());
					Map<String, Integer> headerMap = csvParser.getHeaderMap();
					for (CSVRecord record : csvParser) {
						Pattern p = getPattern(record, headerMap);
						if (p != null) {
							patterns.put(p.getId(), p);
//							System.out.println(p.getPrintStr());
						} else {
							logger.error("Error in reading the pattern: " + record.toString());
						}
					}
				} catch (IOException e) {
					logger.error("Error in reading the patterns from the file " + file.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}

		return patterns;
	}
	
	private static Pattern getPattern(CSVRecord record, Map<String, Integer> headerMap) {
		
		if (headerMap == null) return null;
		
		int numOfDataProperties = 0;
		int numOfObjectProperties = 0;
		
		for (String header : headerMap.keySet()) {
			if (header.startsWith(OBJECT_PROPERTY_PREFIX)) numOfObjectProperties ++;
			if (header.startsWith(DATA_PROPERTY_PREFIX)) numOfDataProperties ++;
		}
		
		if (numOfDataProperties == 0 && numOfObjectProperties == 0) { // no relationship exists in the pattern
			logger.error("No relationship exists in the pattern");
			return null;
		}
		
		int size = numOfObjectProperties + numOfDataProperties + 1;
		String id = "p" + size + "-" + new RandomGUID().toString();

		Integer countIndex = headerMap.get(COUNT_COLUMN);
		if (countIndex == null) {
			logger.error("Cannot find frequency of the pattern");
			return null;
		}
		int frequency = Integer.parseInt(record.get(countIndex));

		HashMap<String, Node> classNodes = new HashMap<String, Node>();
		HashMap<String, Node> dataNodes = new HashMap<String, Node>();
		
		NodeIdFactory nodeIdFactory = new NodeIdFactory();
		
		List<String> types = new ArrayList<String>();
		for (String header : headerMap.keySet()) {
			String s = record.get(header);
			String key;
			if (header.startsWith(CLASS_PREFIX) && !header.equalsIgnoreCase(COUNT_COLUMN)) {
				types.add(s);
				key = header.substring(CLASS_PREFIX.length());
				classNodes.put(key, new InternalNode(nodeIdFactory.getNodeId(s), new Label(s)));
			} else if (header.startsWith(DATA_PROPERTY_PREFIX)) {
				key = header.substring(DATA_PROPERTY_PREFIX.length());
				dataNodes.put(key, new ColumnNode(new RandomGUID().toString(), "NA", "NA", null) );
			}
		}

		DirectedWeightedMultigraph<Node, LabeledLink> graph
			= new DirectedWeightedMultigraph<Node, LabeledLink>(LabeledLink.class);

		// adding nodes
		for (Node n : classNodes.values()) {
			graph.addVertex(n);
		}
		for (Node n : dataNodes.values()) {
			graph.addVertex(n);
		}

		// adding links
		for (String header : headerMap.keySet()) {
			String s = record.get(header);
			String key;
			Node source, target;
			if (header.startsWith(OBJECT_PROPERTY_PREFIX)) {
				key = header.substring(OBJECT_PROPERTY_PREFIX.length());
				if (key.length() != 2) continue; // the object property header should be in form of prefix (op) + [1..9] + [1..9] --> the key is "xy"
				source = classNodes.get(String.valueOf(key.charAt(0)));
				target = classNodes.get(String.valueOf(key.charAt(1)));
				LabeledLink link = new ObjectPropertyLink(
						LinkIdFactory.getLinkId(s, source.getId(), target.getId()), 
						new Label(s), ObjectPropertyType.None);
				graph.addEdge(source, target, link);
//				graph.setEdgeWeight(link, frequency);
				graph.setEdgeWeight(link, ModelingParams.PROPERTY_DIRECT_WEIGHT - (double)size / ModelingParams.PROPERTY_DIRECT_WEIGHT);
			} else if (header.startsWith(DATA_PROPERTY_PREFIX)) {
				key = header.substring(DATA_PROPERTY_PREFIX.length());
				if (key.length() != 2) continue; // the object property header should be in form of prefix (dp) + [1..9] + [a..z] --> the key is "xy"
				source = classNodes.get(String.valueOf(key.charAt(0)));
				target = dataNodes.get(key);
				LabeledLink link = new DataPropertyLink(
						LinkIdFactory.getLinkId(s, source.getId(), target.getId()), 
						new Label(s));
				graph.addEdge(source, target, link);
//				graph.setEdgeWeight(link, frequency);
				graph.setEdgeWeight(link, ModelingParams.PROPERTY_DIRECT_WEIGHT - (double)size / ModelingParams.PROPERTY_DIRECT_WEIGHT);
			}
		}

		Pattern p = new Pattern(id, size, frequency, types, graph);
		
		return p;
	}
	
	public static Pattern getPattern(String line) {
		return null;
	}
}
