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
	
	public static Map<String, Pattern> importPatterns(String inputDir, Integer length) {
		
		Map<String, Pattern> patterns = new HashMap<String, Pattern>();
		
//		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n").
//				withIgnoreEmptyLines(true).
//				withDelimiter(' ').
//				withIgnoreSurroundingSpaces(true).
//				withHeader();
		CSVFormat csvFileFormat = CSVFormat.RFC4180.withHeader();
		CSVParser csvParserTotalFrequency;
		CSVParser csvParser;
		
		File patternDir = new File(inputDir);
		if (patternDir.exists()) {
			File[] patternFiles = patternDir.listFiles();
			for (File file : patternFiles) {
				try {
					if (file.isDirectory()) continue;
					csvParser = CSVParser.parse(file, Charsets.UTF_8, csvFileFormat);
					Map<String, Integer> headerMap = csvParser.getHeaderMap();
					
					csvParserTotalFrequency = CSVParser.parse(file, Charsets.UTF_8, csvFileFormat);
					int totalFrequency = getTotalFrequency(csvParserTotalFrequency, headerMap.get(COUNT_COLUMN));
					
					for (CSVRecord record : csvParser) {
						Pattern p = getPattern(record, headerMap, totalFrequency);
						if (p != null) {
							if (length != null) {
								if (p.getLength() == length.intValue())
									patterns.put(p.getId(), p);
							} else {
								patterns.put(p.getId(), p);
							}
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
	
	private static int getTotalFrequency(CSVParser csvParser, Integer countColumnIndex) {
		int total = 0;
		if (csvParser == null || countColumnIndex == null)
			return 0;
		
		for (CSVRecord record : csvParser) {
			int frequency = Integer.parseInt(record.get(countColumnIndex));
			total += frequency;
		}		
		return total;
	}
	
	private static Pattern getPattern(CSVRecord record, Map<String, Integer> headerMap, int totalFrequency) {
		
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
		
		int length = numOfObjectProperties + numOfDataProperties;

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
		double w;
		String key;
		for (String header : headerMap.keySet()) {
			String s = record.get(header);
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
				w = getWeight(length, frequency, totalFrequency);
				graph.setEdgeWeight(link, w);
//				if (size == 3 && (target.getId().contains("E21_") || target.getId().contains("E39_")) && source.getId().contains("E12_"))
//					System.out.println("pattern size: " + size + " , link: " + link.getId() + ", count: " + frequency + ", w:" + w);
			} else if (header.startsWith(DATA_PROPERTY_PREFIX)) {
				key = header.substring(DATA_PROPERTY_PREFIX.length());
				if (key.length() != 2) continue; // the object property header should be in form of prefix (dp) + [1..9] + [a..z] --> the key is "xy"
				source = classNodes.get(String.valueOf(key.charAt(0)));
				target = dataNodes.get(key);
				LabeledLink link = new DataPropertyLink(
						LinkIdFactory.getLinkId(s, source.getId(), target.getId()), 
						new Label(s));
				graph.addEdge(source, target, link);
				w = getWeight(length, frequency, totalFrequency);
				graph.setEdgeWeight(link, w);
			}
		}

//		Pattern p = new Pattern(id, length, frequency, types, graph, nodeIdFactory);
		Pattern p = new Pattern(length, frequency, graph, nodeIdFactory);
		
		return p;
	}
	
	private static double getWeight(int patternLength, int frequency, int totalFrequency) {
		double w = 0.0;
		double initialWeight;
		double subtract = 0.0;
		
		initialWeight = (ModelingParams.PATTERN_LINK_WEIGHT / (patternLength));
		initialWeight = initialWeight - ((double)patternLength - 1.0) / 10.0;
		if (totalFrequency > 0) {
			subtract = (double)frequency / (double)totalFrequency;
			if (subtract > initialWeight) subtract = 0.0000001; //initialWeight - Double.MIN_VALUE;
		}
		
		w = initialWeight - subtract;
//		w = ModelingParams.PATTERN_LINK_WEIGHT;
		return w;
	}
	
	public static Pattern getPattern(String line) {
		return null;
	}
}
