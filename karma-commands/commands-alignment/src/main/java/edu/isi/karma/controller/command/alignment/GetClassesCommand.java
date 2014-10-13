package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.view.VWorkspace;

public class GetClassesCommand extends WorksheetCommand {

	final private INTERNAL_NODES_RANGE range;

	public enum INTERNAL_NODES_RANGE {
		allClasses, classesInModel, classesWithProperty, allClassesRaw
	}

	private enum JsonKeys {
		updateType, nodeLabel, nodeId, nodes, nodeUri
	}

	private String propertyURI;

	private static Logger logger = LoggerFactory.getLogger(GetClassesCommand.class.getSimpleName());

	protected GetClassesCommand(String id, String worksheetId, INTERNAL_NODES_RANGE range, String propertyURI) {
		super(id, worksheetId);
		this.range = range;
		this.propertyURI = propertyURI;
	}

	@Override
	public String getCommandName() {
		return GetClassesCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Get Classes";
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

		Set<Node> nodeSet = null;
		if (range == INTERNAL_NODES_RANGE.classesInModel) {
			nodeSet = getClassesInModel(workspace);
		} else if (range == INTERNAL_NODES_RANGE.allClasses) {
			nodeSet = getAllClasses(workspace);
		} else if(range == INTERNAL_NODES_RANGE.classesWithProperty) {
			nodeSet = getClassesWithProperty(workspace, propertyURI);
		}
		else if (range == INTERNAL_NODES_RANGE.allClassesRaw) {
			final OntologyManager ontMgr = workspace.getOntologyManager();
			final HashMap<String, Label> allClasses = ontMgr.getClasses();
			UpdateContainer upd = new UpdateContainer(new AbstractUpdate() {
				@Override
				public void generateJson(String prefix, PrintWriter pw,
						VWorkspace vWorkspace) {
					JSONArray nodesArray = new JSONArray();
					JSONObject obj = new JSONObject();
					for (Entry<String, Label> entry : allClasses.entrySet()) {
						JSONObject nodeObj = new JSONObject();
						Label label = entry.getValue();
						nodeObj.put(JsonKeys.nodeLabel.name(), label.getDisplayName());
						nodeObj.put(JsonKeys.nodeId.name(), label.getUri());
						nodeObj.put(JsonKeys.nodeUri.name(), label.getUri());
						nodesArray.put(nodeObj);
					}
					obj.put(JsonKeys.nodes.name(), nodesArray);
					pw.println(obj.toString());
				}
			});
			return upd;
		}

		if (nodeSet == null) {
			nodeSet = new HashSet<Node>();
		}
		final Set<Node> finalNodeSet = nodeSet;

		UpdateContainer upd = new UpdateContainer(new AbstractUpdate() {
			@Override
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				JSONObject obj = new JSONObject();
				JSONArray nodesArray = new JSONArray();

				try {
					obj.put(JsonKeys.updateType.name(), "InternalNodesList");
					for (Node node:finalNodeSet) {
						if (!(node instanceof InternalNode)) {
							continue;
						}

						JSONObject nodeObj = new JSONObject();
						String nodeLabelStr = node.getDisplayId();

						Label nodeLabel = node.getLabel();
						if (nodeLabel.getUri() !=null && nodeLabel.getNs() != null 
								&& nodeLabel.getUri().equalsIgnoreCase(nodeLabel.getNs())) {
							nodeLabelStr = node.getId();
						} else if(nodeLabel.getPrefix() == null && nodeLabel.getUri() != null) {
							nodeLabelStr = nodeLabel.getUri() + "/" + nodeLabelStr;
						}
						nodeObj.put(JsonKeys.nodeLabel.name(), nodeLabelStr);
						nodeObj.put(JsonKeys.nodeId.name(), node.getId());
						nodeObj.put(JsonKeys.nodeUri.name(), nodeLabel.getUri());
						nodesArray.put(nodeObj);
					}

					obj.put(JsonKeys.nodes.name(), nodesArray);
					pw.println(obj.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		return upd;
	}

	private Set<Node> getAllClasses(Workspace workspace) {
		final OntologyManager ontMgr = workspace.getOntologyManager();
		HashMap<String, Label> allClasses = ontMgr.getClasses();

		logger.info("Got " + allClasses.size() + " classes from OntologyManager");

		Set<Label> nodeLabels = new HashSet<>();
		nodeLabels.addAll(allClasses.values());
		return getNodesUsingAlignment(workspace, nodeLabels);
	}

	private Set<Node> getClassesInModel(Workspace workspace) {
		final Alignment alignment = AlignmentManager.Instance().getAlignment(
				workspace.getId(), worksheetId);
		return alignment.getSteinerTree().vertexSet();
	}

	private Set<Node> getClassesWithProperty(Workspace workspace, String propertyURI) {
		final OntologyManager ontMgr = workspace.getOntologyManager();
		final HashSet<String> domains = ontMgr.getDomainsOfProperty(
				propertyURI, true);
		if (domains == null || domains.size() == 0) {
			return null;
		}

		Set<Label> nodeLabels = new HashSet<>();
		for(String domain : domains) {
			Label domainURI = ontMgr.getUriLabel(domain);
			if(domainURI == null)
				continue;
			nodeLabels.add(domainURI);
		}


		return getNodesUsingAlignment(workspace, nodeLabels);
	}

	private Set<Node> getNodesUsingAlignment(Workspace workspace, Set<Label> nodeLabels) {
		Set<Node> nodeSet = new HashSet<>();
		final OntologyManager ontMgr = workspace.getOntologyManager();
		final Alignment alignment = AlignmentManager.Instance().getAlignmentOrCreateIt(
				workspace.getId(), worksheetId, ontMgr);

		final Set<String> steinerTreeNodeIds = new HashSet<String>();

		if (alignment != null && !alignment.isEmpty()) {
			for (Node node: alignment.getSteinerTree().vertexSet()) {
				if (node.getType() == NodeType.InternalNode) {
					steinerTreeNodeIds.add(node.getId());
				}
			}
		}
		for (Label nodeLabel : nodeLabels) {
			String nodeUri = nodeLabel.getUri();

			int graphLastIndex = -1;
			if (alignment != null) {
				graphLastIndex = alignment.getLastIndexOfNodeUri(nodeUri);
			}
			String nodeId;
			// If the node exists in graph but not in tree then use the graph node id
			if (graphLastIndex != -1) {
				if (!steinerTreeNodeIds.contains(nodeUri + graphLastIndex)) {
					nodeId = nodeUri + "1 (add)";
				} else {
					nodeId = nodeUri + (graphLastIndex+1) + " (add)";
				}
			} else {
				nodeId = nodeUri + "1 (add)";
			}


			InternalNode node = new InternalNode(nodeId, nodeLabel);
			nodeSet.add(node);


			// Populate the graph nodes also
			if (alignment != null) {
				Set<Node> graphNodes = alignment.getNodesByUri(nodeUri);
				if (graphNodes != null && graphNodes.size() != 0) {
					for (Node graphNode: graphNodes) {
						if (steinerTreeNodeIds.contains(graphNode.getId())) {
							nodeSet.add(graphNode);
						}
					}
				}
			}

		}

		return nodeSet;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
