package edu.isi.karma.view.tableheadings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.hierarchicalheadings.TForest;
import edu.isi.karma.rep.hierarchicalheadings.TNode;

public class VTHeaderForest implements TForest {
	private List<TNode> roots = new ArrayList<TNode>();
	private Map<String, VTHNode> nodeMap = new HashMap<String, VTHNode>();

	@Override
	public List<TNode> getRoots() {
		return roots;
	}

	public void constructFromHNodePaths(List<HNodePath> paths) {
		for (HNodePath path : paths) {

			// Add the root
			HNode root = path.getFirst();
			String rootId = root.getId();
			VTHNode rootNode;
			if (nodeMap.containsKey(rootId)) {
				rootNode = nodeMap.get(rootId);

			} else {
				rootNode = new VTHNode(rootId, root.getColumnName());
				nodeMap.put(rootId, rootNode);
			}
			if (!roots.contains(rootNode))
				roots.add(rootNode);

			// Add the children
			HNodePath rest = path.getRest();
			VTHNode parentNode = rootNode;
			while (!rest.isEmpty()) {
				HNode child = rest.getFirst();
				String childId = child.getId();
				
				VTHNode childNode;
				if (nodeMap.containsKey(childId)) {
					childNode = nodeMap.get(childId);
				} else {
					childNode = new VTHNode(childId, child.getColumnName());
					nodeMap.put(childId, childNode);
				}
				parentNode.addChild(childNode);
				rest = rest.getRest();
				parentNode = childNode;
			}
		}
	}

}
