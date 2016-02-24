package edu.isi.karma.research.modeling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.alignment.GraphBuilder;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.GraphVizLabelType;
import edu.isi.karma.modeling.alignment.GraphVizUtil;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.alignment.NodeIdFactory;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.ObjectPropertyType;
import edu.isi.karma.util.RandomGUID;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;

public class GraphBuilder_Popularity {

	private OntologyManager ontologyManager;
	private GraphBuilder graphBuilder;
	private NodeIdFactory nodeIdFactory;
	private static Logger logger = LoggerFactory.getLogger(GraphBuilder_Popularity.class);

	private class Triple {

		public Triple(String subjectUri, String predicateUri, String objectUri, int linkFrequency) {
			this.subjectUri = subjectUri;
			this.predicateUri = predicateUri;
			this.objectUri = objectUri;
			this.linkFrequency = linkFrequency;
		}

		public Triple(String subjectUri, String predicateUri, int linkFrequency) {
			this.subjectUri = subjectUri;
			this.predicateUri = predicateUri;
			this.objectUri = null;
			this.linkFrequency = linkFrequency;
		}

		private String subjectUri;
		private String predicateUri;
		private String objectUri;
		private int linkFrequency;

		public String getSubjectUri() {
			return subjectUri;
		}

		public String getPredicateUri() {
			return predicateUri;
		}

		public String getObjectUri() {
			return objectUri;
		}

		public int getLinkFrequency() {
			return linkFrequency;
		}

	}

	public GraphBuilder_Popularity(OntologyManager ontologyManager, 
			String objectPropertiesFile,
			String dataPropertiesFile) {

		this.ontologyManager = ontologyManager;
		this.nodeIdFactory = new NodeIdFactory();
		this.graphBuilder = new GraphBuilder(this.ontologyManager, false);

		List<Triple> objectPropertiesTriples = null;
		List<Triple> dataPropertiesTriples = null;
		try {
			objectPropertiesTriples = readObjectProperties(objectPropertiesFile);
			dataPropertiesTriples = readDataProperties(dataPropertiesFile);
		} catch (IOException e) {
			logger.error("error in reading the statistics files.");
			e.printStackTrace();
		}

		Set<InternalNode> addedNodes = new HashSet<InternalNode>();
		addObjectPrpertiesTriplesToGraph(objectPropertiesTriples, addedNodes);
		addDataPrpertiesTriplesToGraph(dataPropertiesTriples, addedNodes);

		try {
			GraphVizUtil.exportJGraphToGraphviz(this.graphBuilder.getGraph(), 
					"LOD Graph", 
					false, 
					GraphVizLabelType.LocalId,
					GraphVizLabelType.LocalUri,
					true, 
					true, 
					Params.GRAPHS_DIR + 
					"lod.graph.small.dot");
		} catch (Exception e) {
			logger.error("error in exporting the alignment graph to graphviz!");
		}

		this.graphBuilder.addClosureAndUpdateLinks(addedNodes, null);

		try {
			GraphVizUtil.exportJGraphToGraphviz(this.graphBuilder.getGraph(), 
					"LOD Graph", 
					false, 
					GraphVizLabelType.LocalId,
					GraphVizLabelType.LocalUri,
					true, 
					true, 
					Params.GRAPHS_DIR + "lod.graph.dot");
			GraphUtil.exportJson(this.graphBuilder.getGraph(), Params.GRAPHS_DIR + "lod" + Params.GRAPH_JSON_FILE_EXT, true, true);
		} catch (Exception e) {
			logger.error("error in exporting the alignment graph to graphviz!");
		}

		logger.info("finished.");

	}

	public GraphBuilder getGraphBuilder() {
		return this.graphBuilder;
	}

	private List<Triple> readObjectProperties(String objectPropertiesFile) throws IOException {

		InputStream f;
		BufferedReader br;
		String line;
		String[] parts;
		List<Triple> triples = new ArrayList<Triple>();

		f = new FileInputStream(objectPropertiesFile);
		br = new BufferedReader(new InputStreamReader(f, Charset.forName("UTF-8")));
		while ((line = br.readLine()) != null) {
			parts = line.trim().split(",");
			if (parts == null || parts.length != 4) continue;
			Triple t = new Triple(parts[0].trim(), parts[1].trim(), parts[2].trim(), Integer.parseInt(parts[3].trim()));
			triples.add(t);
		}

		// Done with the file
		br.close();
		br = null;
		f = null;

		return triples;

	}

	private List<Triple> readDataProperties(String dataPropertiesFile) throws IOException {

		InputStream f;
		BufferedReader br;
		String line;
		String[] parts;
		List<Triple> triples = new ArrayList<Triple>();

		f = new FileInputStream(dataPropertiesFile);
		br = new BufferedReader(new InputStreamReader(f, Charset.forName("UTF-8")));
		while ((line = br.readLine()) != null) {
			parts = line.trim().split(",");
			if (parts == null || parts.length != 3) continue;
			Triple t = new Triple(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2].trim()));
			triples.add(t);
		}

		// Done with the file
		br.close();
		br = null;
		f = null;

		return triples;

	}

	private void addObjectPrpertiesTriplesToGraph(List<Triple> objectPropertiesTriples, Set<InternalNode> addedNodes) {

		String subjectUri = "";
		String predicateUri = "";
		String objectUri = "";
		int linkFrequency;
		String id;
		Set<Node> nodes;
		Node node, n1, n2;
		LabeledLink link;

		int countOfObjectProperties = 0;

		if (objectPropertiesTriples != null) {
			for (Triple t : objectPropertiesTriples) {
				countOfObjectProperties += t.getLinkFrequency();
			}
		}

		if (objectPropertiesTriples != null) {
			for (Triple t : objectPropertiesTriples) {
				subjectUri = t.getSubjectUri();
				predicateUri = t.getPredicateUri();
				objectUri = t.getObjectUri();
				linkFrequency = t.getLinkFrequency();

				nodes = this.graphBuilder.getUriToNodesMap().get(subjectUri);
				if (nodes == null || nodes.isEmpty()) {
					id = this.nodeIdFactory.getNodeId(subjectUri);
					node = new InternalNode(id, new Label(subjectUri));
					if (this.graphBuilder.addNode(node)) {
						addedNodes.add((InternalNode)node);
						n1 = node;
					} else {
						continue;
					}
				} else {
					n1 = nodes.iterator().next();
				}

				nodes = this.graphBuilder.getUriToNodesMap().get(objectUri);
				if (nodes == null || nodes.isEmpty()) {
					id = this.nodeIdFactory.getNodeId(objectUri);
					node = new InternalNode(id, new Label(objectUri));
					if (this.graphBuilder.addNode(node)) {
						addedNodes.add((InternalNode)node);
						n2 = node;
					} else {
						continue;
					}
				} else {
					n2 = nodes.iterator().next();
				}

				id = LinkIdFactory.getLinkId(predicateUri, n1.getId(), n2.getId());
				link = new ObjectPropertyLink(id, new Label(predicateUri), ObjectPropertyType.None);
				if (this.graphBuilder.addLink(n1, n2, link)) {
					this.graphBuilder.changeLinkWeight(link, 1 - ((double)linkFrequency / (double)countOfObjectProperties));
//					this.graphBuilder.changeLinkWeight(link, countOfObjectProperties - linkFrequency);
//					this.graphBuilder.changeLinkWeight(link, linkFrequency);
				}

			}
		}
	}

	private void addDataPrpertiesTriplesToGraph(List<Triple> dataPropertiesTriples, Set<InternalNode> addedNodes) {

		String subjectUri = "";
		String predicateUri = "";
		int linkFrequency;
		String id;
		Set<Node> nodes;
		Node node, n1, n2;
		LabeledLink link;

		int countOfDataProperties = 0;

		if (dataPropertiesTriples != null) {
			for (Triple t : dataPropertiesTriples) {
				countOfDataProperties += t.getLinkFrequency();
			}
		}

		if (dataPropertiesTriples != null) {
			for (Triple t : dataPropertiesTriples) {
				subjectUri = t.getSubjectUri();
				predicateUri = t.getPredicateUri();
				linkFrequency = t.getLinkFrequency();

				nodes = this.graphBuilder.getUriToNodesMap().get(subjectUri);
				if (nodes == null || nodes.isEmpty()) {
					id = this.nodeIdFactory.getNodeId(subjectUri);
					node = new InternalNode(id, new Label(subjectUri));
					if (this.graphBuilder.addNode(node)) {
						addedNodes.add((InternalNode)node);
						n1 = node;
					} else {
						continue;
					}
				} else {
					n1 = nodes.iterator().next();
				}

				id = new RandomGUID().toString();
				node = new ColumnNode(id, id, "", null);
				if (this.graphBuilder.addNode(node)) {
					n2 = node;
				} else {
					continue;
				}

				id = LinkIdFactory.getLinkId(predicateUri, n1.getId(), n2.getId());	
				link = new DataPropertyLink(id, new Label(predicateUri));
				if (this.graphBuilder.addLink(n1, n2, link)) {
					this.graphBuilder.changeLinkWeight(link, 1 - ((double)linkFrequency / (double)countOfDataProperties));
//					this.graphBuilder.changeLinkWeight(link, countOfDataProperties - linkFrequency);
//					this.graphBuilder.changeLinkWeight(link, linkFrequency);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {

		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getDefault();
		OntologyManager ontologyManager = new OntologyManager(contextParameters.getId());
		File ff = new File(Params.ONTOLOGY_DIR);
		File[] files = ff.listFiles();
		if (files == null) {
			logger.error("no ontology to import at " + ff.getAbsolutePath());
			return;
		}

		for (File f : files) {
			if (f.getName().endsWith(".owl") || 
					f.getName().endsWith(".rdf") || 
					f.getName().endsWith(".rdfs") || 
					f.getName().endsWith(".n3") || 
					f.getName().endsWith(".ttl") || 
					f.getName().endsWith(".xml")) {
				System.out.println("Loading ontology file: " + f.getAbsolutePath());
				logger.info("Loading ontology file: " + f.getAbsolutePath());
				ontologyManager.doImport(f, "UTF-8");
			}
		}
		ontologyManager.updateCache(); 
		
		new GraphBuilder_Popularity(ontologyManager, 
				Params.LOD_OBJECT_PROPERIES_FILE, 
				Params.LOD_DATA_PROPERIES_FILE);
		

	}
	
}
