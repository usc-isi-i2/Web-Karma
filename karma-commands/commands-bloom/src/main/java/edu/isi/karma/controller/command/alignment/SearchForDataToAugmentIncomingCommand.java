package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.util.bloom.Key;
import org.apache.hadoop.util.hash.Hash;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.BloomFilterTripleStoreUtil;
import edu.isi.karma.er.helper.CloneTableUtils;
import edu.isi.karma.kr2rml.writer.KR2RMLBloomFilter;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;

public class SearchForDataToAugmentIncomingCommand extends WorksheetSelectionCommand{
	private static final Logger LOG = LoggerFactory.getLogger(SearchForDataToAugmentIncomingCommand.class);
	private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
	private String tripleStoreUrl;
	private String context;
	private String nodeUri;
	private String columnUri;
	private final Integer limit = 100;
	public SearchForDataToAugmentIncomingCommand(String id, String model, String url, String context, String nodeUri, String worksheetId, String columnUri, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.tripleStoreUrl = url;
		this.context = context;
		this.nodeUri = nodeUri;
		this.columnUri = columnUri;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Search For Data To Augment";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {

		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		SuperSelection selection = getSuperSelection(worksheet);
		RepFactory factory = workspace.getFactory();
		BloomFilterTripleStoreUtil util = new BloomFilterTripleStoreUtil();
		HashMap<String, List<String>> result = null;
		nodeUri = nodeUri.trim();
		Map<String, Label> parents = workspace.getOntologyManager().getSuperClasses(nodeUri, true);
		Set<String> classes = new HashSet<>(parents.keySet());
		classes.add(nodeUri);
		StringBuilder builder = new StringBuilder();
		nodeUri = builder.append("<").append(nodeUri).append(">").toString();
		try {
			result = util.getPredicatesForParentTriplesMapsWithSameClass(tripleStoreUrl, context, classes);
		} catch (KarmaException e) {
			LOG.error("Unable to find predicates for triples maps with same class as: " + nodeUri, e);
		}
		final JSONArray array = new JSONArray();
		List<JSONObject> objects = new ArrayList<>();
		List<String> concatenatedPredicateObjectMapsList = result.get("refObjectMaps");
		List<String> predicates = result.get("predicates");
		List<String> otherClasses = result.get("otherClasses");
		Iterator<String> concatenatedPredicateObjectMapsListItr = concatenatedPredicateObjectMapsList.iterator();
		Iterator<String> predicatesItr = predicates.iterator();
		Iterator<String> otherClassesItr = otherClasses.iterator();
		String hNodeId = FetchHNodeIdFromAlignmentCommand.gethNodeId(AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId), columnUri);
		HNode hnode = factory.getHNode(hNodeId);
		List<Table> dataTables = new ArrayList<>();
		CloneTableUtils.getDatatable(worksheet.getDataTable(), factory.getHTable(hnode.getHTableId()), dataTables, selection);
		KR2RMLBloomFilter uris = new KR2RMLBloomFilter(KR2RMLBloomFilter.defaultVectorSize, KR2RMLBloomFilter.defaultnbHash, Hash.JENKINS_HASH);
		Set<String> uriSet = new HashSet<>();
		for(Table t : dataTables) {
			for(Row r : t.getRows(0, t.getNumRows(), selection)) {
				Node n = r.getNode(hNodeId);
				if(n != null && n.getValue() != null && !n.getValue().isEmptyValue() && n.getValue().asString() != null && !n.getValue().asString().trim().isEmpty() ) {
					String value = n.getValue().asString().trim().replace(" ", "");;
					builder = new StringBuilder();
					String baseURI = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(Property.baseURI);
					try {
						URI uri = new URI(value);
						if (!uri.isAbsolute() && baseURI != null) {
							value = baseURI + value;
						}
					} catch (URISyntaxException e) {
					}
					value = builder.append("<").append(value).append(">").toString(); //String builder
					uriSet.add(value);
					uris.add(new Key(value.getBytes(UTF8_CHARSET)));
				}
			}
		}
		Set<String> maps = new HashSet<>();
		Map<String, String> bloomfilterMapping = new HashMap<>();
		try{
			for (String concatenatedPredicateObjectMaps : concatenatedPredicateObjectMapsList) {
				List<String> predicateObjectMaps = new ArrayList<>(Arrays.asList(concatenatedPredicateObjectMaps.split(",")));
				maps.addAll(predicateObjectMaps);
				if (maps.size() > limit) {
					bloomfilterMapping.putAll(util.getBloomFiltersForMaps(tripleStoreUrl, null, maps));
					maps = new HashSet<>();
				}
			}
			if (!maps.isEmpty())
				bloomfilterMapping.putAll(util.getBloomFiltersForMaps(tripleStoreUrl, null, maps));
		} catch (KarmaException e1) {
			e1.printStackTrace();
		}
		while(concatenatedPredicateObjectMapsListItr.hasNext() && predicatesItr.hasNext() && otherClassesItr.hasNext())
		{
			String concatenatedPredicateObjectMaps = concatenatedPredicateObjectMapsListItr.next();
			List<String> predicateObjectMaps = new ArrayList<>(Arrays.asList(concatenatedPredicateObjectMaps.split(",")));
			String predicate =  predicatesItr.next();
			String otherClass = otherClassesItr.next();
			try {
				KR2RMLBloomFilter intersectionBF = new KR2RMLBloomFilter(KR2RMLBloomFilter.defaultVectorSize, KR2RMLBloomFilter.defaultnbHash, Hash.JENKINS_HASH);
				for (String triplemap : predicateObjectMaps) {
					String serializedBloomFilter = bloomfilterMapping.get(triplemap);
					if (serializedBloomFilter != null) {
						KR2RMLBloomFilter bf = new KR2RMLBloomFilter();
						bf.populateFromCompressedAndBase64EncodedString(serializedBloomFilter);
						intersectionBF.or(bf);
					}
				}
				intersectionBF.and(uris);
				int estimate = intersectionBF.estimateNumberOfHashedValues();
				JSONObject obj = new JSONObject();
				obj.put("predicate", predicate);
				obj.put("otherClass", otherClass);
				obj.put("estimate", estimate);
				obj.put("incoming", "true");
				//array.put(obj);
				objects.add(obj);
			} catch (Exception e) {
				LOG.error("Unable to process bloom filter: " + e.getMessage());
			}
		}
		Collections.sort(objects, new Comparator<JSONObject>() {

			@Override
			public int compare(JSONObject a, JSONObject b) {
				return b.getInt("estimate") - a.getInt("estimate");
			}
		});
		for (JSONObject obj : objects) {
			array.put(obj);
		}
		return new UpdateContainer(new AbstractUpdate() {

			@Override
			public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
				System.out.println(array.toString());
				pw.print(array.toString());
			}
		});
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
