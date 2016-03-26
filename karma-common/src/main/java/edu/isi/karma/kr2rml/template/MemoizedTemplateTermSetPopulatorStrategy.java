/*******************************************************************************
 * Copyright 2014 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.kr2rml.template;

import java.util.Collection;
import java.util.LinkedList;

import edu.isi.karma.controller.command.selection.SuperSelection;
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
	public Collection<Node> getNodes(Row topRow, Row currentRow, SuperSelection sel) 
	{
		try
		{
			synchronized(this)
			{
				if(nodes == null || topRow != this.topRow)
				{
					nodes = new LinkedList<>();
					topRow.collectNodes(path, nodes, sel);
					this.topRow = topRow;
				}
				return nodes;
			}
			
		}
		catch (Exception e)
		{
			throw e;
		}
	}

}
