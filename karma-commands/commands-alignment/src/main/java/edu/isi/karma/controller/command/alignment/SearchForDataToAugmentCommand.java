package edu.isi.karma.controller.command.alignment;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.util.bloom.Key;
import org.apache.hadoop.util.hash.Hash;
import org.json.JSONArray;
import org.json.JSONObject;

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
import edu.isi.karma.rep.HashValueManager;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;

public class SearchForDataToAugmentCommand extends Command{
	private String tripleStoreUrl;
	private String context;
	private String nodeUri;
	private String worksheetId;
	private String columnUri;
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
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Search For Data To Augment";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommandType getCommandType() {
		// TODO Auto-generated method stub
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		// TODO Auto-generated method stub
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		RepFactory factory = workspace.getFactory();
		TripleStoreUtil util = new TripleStoreUtil();
		HashMap<String, List<String>> result = null;
		nodeUri = nodeUri.trim();
		nodeUri = "<" + nodeUri + ">";
		try {
			result = util.getPredicatesForTriplesMapsWithSameClass(tripleStoreUrl, context, nodeUri);
		} catch (KarmaException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		final JSONArray array = new JSONArray();
		List<String> triplesMaps = result.get("triplesMaps");
		List<String> predicate = result.get("predicate");
		List<String> otherClass = result.get("otherClass");
		Iterator<String> triplesMapsItr = triplesMaps.iterator();
		Iterator<String> predicateItr = predicate.iterator();
		Iterator<String> otherClassItr = otherClass.iterator();
		String hNodeId = FetchHNodeIdFromAlignmentCommand.gethNodeId(AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId), columnUri);
		HNode hnode = factory.getHNode(hNodeId);
		List<Table> dataTables = new ArrayList<Table>();
		CloneTableUtils.getDatatable(worksheet.getDataTable(), factory.getHTable(hnode.getHTableId()), dataTables);
		KR2RMLBloomFilter uris = new KR2RMLBloomFilter(1000000, 8,Hash.JENKINS_HASH);
		Set<String> uriSet = new HashSet<String>();
		for(Table t : dataTables) {
			for(Row r : t.getRows(0, t.getNumRows())) {
				Node n = r.getNode(hNodeId);
				if(n != null && n.getValue() != null && !n.getValue().isEmptyValue() && n.getValue().asString() != null && !n.getValue().asString().trim().isEmpty() ) {
					String value = n.getValue().asString().trim();
					value = "<" + value + ">"; //String builder
					uriSet.add(value);
					uris.add(new Key(value.getBytes()));
				}
			}
		}
		while(triplesMapsItr.hasNext() && predicateItr.hasNext() && otherClassItr.hasNext())
		{
			JSONObject obj = new JSONObject();
			String tripleMap = triplesMapsItr.next();
			List<String> triplemaps = new ArrayList<String>(Arrays.asList(tripleMap.split(",")));
			
			List<String> values = null;
			try {
				values = util.getBloomFiltersForTriplesMaps(tripleStoreUrl, context, triplemaps);
				KR2RMLBloomFilter intersectionBF = new KR2RMLBloomFilter(1000000,8,Hash.JENKINS_HASH);
				for (String value : values) {
					byte[] serializedBloomFilter = Base64.decodeBase64(value);
					KR2RMLBloomFilter bf = new KR2RMLBloomFilter();
					bf.readFields(new ObjectInputStream(new ByteArrayInputStream(serializedBloomFilter)));
					intersectionBF.or(bf);
				}
				intersectionBF.and(uris);
				double probability = intersectionBF.estimateNumberOfHashedValues()/(uriSet.size()*1.0);
				obj.put("predicate", predicateItr.next());
				obj.put("otherClass", otherClassItr.next());
				obj.put("probability", String.format("%.4f", probability));
				array.put(obj);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		// TODO Auto-generated method stub
		return null;
	}

}
