package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.CloneTableUtils;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.kr2rml.KR2RMLBloomFilter;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;

public class SearchForDataToAugmentCommand extends Command{
	private static final Logger LOG = LoggerFactory.getLogger(SearchForDataToAugmentCommand.class);
	private String tripleStoreUrl;
	private String context;
	private String nodeUri;
	private String worksheetId;
	private String columnUri;
	private final Integer limit = 100;
	public SearchForDataToAugmentCommand(String id, String url, String context, String nodeUri, String worksheetId, String columnUri) {
		super(id);
		this.tripleStoreUrl = url;
		this.context = context;
		this.nodeUri = nodeUri;
		this.worksheetId = worksheetId;
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
		RepFactory factory = workspace.getFactory();
		TripleStoreUtil util = new TripleStoreUtil();
		HashMap<String, List<String>> result = null;
		nodeUri = nodeUri.trim();
		StringBuilder builder = new StringBuilder();
		nodeUri = builder.append("<").append(nodeUri).append(">").toString();
		try {
			result = util.getPredicatesForTriplesMapsWithSameClass(tripleStoreUrl, context, nodeUri);
		} catch (KarmaException e) {
			LOG.error("Unable to find predicates for triples maps with same class as: " + nodeUri, e);
		}
		final JSONArray array = new JSONArray();
		List<String> concatenatedPredicateObjectMapsList = result.get("predicateObjectMaps");
		List<String> predicates = result.get("predicates");
		List<String> otherClasses = result.get("otherClasses");
		Iterator<String> concatenatedPredicateObjectMapsListItr = concatenatedPredicateObjectMapsList.iterator();
		Iterator<String> predicatesItr = predicates.iterator();
		Iterator<String> otherClassesItr = otherClasses.iterator();
		String hNodeId = FetchHNodeIdFromAlignmentCommand.gethNodeId(AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId), columnUri);
		if (hNodeId == null) {
			return new UpdateContainer(new AbstractUpdate() {

				@Override
				public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
					pw.print(array.toString());
				}
			});
		}
		HNode hnode = factory.getHNode(hNodeId);
		List<Table> dataTables = new ArrayList<Table>();
		CloneTableUtils.getDatatable(worksheet.getDataTable(), factory.getHTable(hnode.getHTableId()), dataTables);
		KR2RMLBloomFilter uris = new KR2RMLBloomFilter(KR2RMLBloomFilter.defaultVectorSize, KR2RMLBloomFilter.defaultnbHash, Hash.JENKINS_HASH);
		Set<String> uriSet = new HashSet<String>();
		for(Table t : dataTables) {
			for(Row r : t.getRows(0, t.getNumRows())) {
				Node n = r.getNode(hNodeId);
				if(n != null && n.getValue() != null && !n.getValue().isEmptyValue() && n.getValue().asString() != null && !n.getValue().asString().trim().isEmpty() ) {
					String value = n.getValue().asString().trim();
					builder = new StringBuilder();
					value = builder.append("<").append(value).append(">").toString(); //String builder
					uriSet.add(value);
					uris.add(new Key(value.getBytes()));
				}
			}
		}
		Set<String> maps = new HashSet<String>();
		Map<String, String> bloomfilterMapping = new HashMap<String, String>();
		try{
			for (String concatenatedPredicateObjectMaps : concatenatedPredicateObjectMapsList) {
				List<String> predicateObjectMaps = new ArrayList<String>(Arrays.asList(concatenatedPredicateObjectMaps.split(",")));
				maps.addAll(predicateObjectMaps);
				if (maps.size() > limit) {
					bloomfilterMapping.putAll(util.getBloomFiltersForMaps(tripleStoreUrl, context, maps));
					maps = new HashSet<String>();
				}
			}
			if (maps.size() > 0)
				bloomfilterMapping.putAll(util.getBloomFiltersForMaps(tripleStoreUrl, context, maps));
		} catch (KarmaException e1) {
			e1.printStackTrace();
		}
		while(concatenatedPredicateObjectMapsListItr.hasNext() && predicatesItr.hasNext() && otherClassesItr.hasNext())
		{
			JSONObject obj = new JSONObject();
			String concatenatedPredicateObjectMaps = concatenatedPredicateObjectMapsListItr.next();
			List<String> predicateObjectMaps = new ArrayList<String>(Arrays.asList(concatenatedPredicateObjectMaps.split(",")));
			String predicate =  predicatesItr.next();
			try {
				KR2RMLBloomFilter intersectionBF = new KR2RMLBloomFilter(KR2RMLBloomFilter.defaultVectorSize, KR2RMLBloomFilter.defaultnbHash, Hash.JENKINS_HASH);
				for (String triplemap : predicateObjectMaps) {
					String serializedBloomFilter = bloomfilterMapping.get(triplemap);
					KR2RMLBloomFilter bf = new KR2RMLBloomFilter();
					bf.populateFromCompressedAndBase64EncodedString(serializedBloomFilter);
					intersectionBF.or(bf);
				}
				System.out.println(predicate + " " + intersectionBF.estimateNumberOfHashedValues());
				intersectionBF.and(uris);
				double probability = intersectionBF.estimateNumberOfHashedValues()/(uriSet.size()*1.0);				
				obj.put("predicate", predicate);
				obj.put("otherClass", otherClassesItr.next());
				obj.put("probability", String.format("%.4f", probability));
				obj.put("incoming", "false");
				array.put(obj);
			} catch (Exception e) {
				LOG.error("Unable to process bloom filter: " + e.getMessage());
			}
		}
		return new UpdateContainer(new AbstractUpdate() {

			@Override
			public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
				pw.print(array.toString());
			}
		});
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
