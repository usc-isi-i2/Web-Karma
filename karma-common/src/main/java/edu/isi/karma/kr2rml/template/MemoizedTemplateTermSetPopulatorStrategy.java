package edu.isi.karma.kr2rml.template;

import java.util.Collection;
import java.util.LinkedList;

import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;

public class MemoizedTemplateTermSetPopulatorStrategy implements
		TemplateTermSetPopulatorStrategy {

	private Collection<Node> nodes = null;
	private Row topRow = null;
	private HNodePath path;
	public MemoizedTemplateTermSetPopulatorStrategy(HNodePath path)
	{
		this.path = path;
	}
	@Override
	public Collection<Node> getNodes(Row topRow, Row currentRow) 
	{
		try
		{
			synchronized(this)
			{
				if(nodes == null || topRow != this.topRow)
				{
					nodes = new LinkedList<Node>();
					topRow.collectNodes(path, nodes);
					this.topRow = topRow;
				}
			}
			return nodes;
		}
		catch (Exception e)
		{
			throw e;
		}
	}

}
