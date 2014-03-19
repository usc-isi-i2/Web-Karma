package edu.isi.karma.kr2rml.template;

import java.util.Collection;
import java.util.LinkedList;

import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;

public class DynamicTemplateTermSetPopulatorStrategy implements
		TemplateTermSetPopulatorStrategy {

	private HNodePath relativePath;
	public DynamicTemplateTermSetPopulatorStrategy(HNodePath targetPath, HNodePath parentPath)
	{
		relativePath = new HNodePath();
		relativePath = HNodePath.findPathBetweenLeavesWithCommonHead(parentPath, targetPath);
		
	}
	@Override
	public Collection<Node> getNodes(Row topRow, Row currentRow) {
		Collection<Node> nodes = new LinkedList<Node>();
		currentRow.collectNodes(relativePath, nodes);
		return nodes;
	}

}
