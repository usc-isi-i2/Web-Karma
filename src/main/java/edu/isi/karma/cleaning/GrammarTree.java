/*******************************************************************************
 * Copyright 2012 University of Southern California
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
package edu.isi.karma.cleaning;

import java.util.ArrayList;

public class GrammarTree {
	public GTNode root;
	private int size;
	public GrammarTree(GTNode e)
	{
		root = e;
		//traversal the tree 
		
	}
	public GTNode randomChoose()
	{
		return null;
	}
	public void setSize(int a)
	{
		size = a;
	}
	public int getNontermSize()
	{
		return size;
	}

}

class GTNode{
	private String nonterm;
	private int choice; // the No of the production used for this non-terminal
	public ArrayList<GTNode> children;
	public GTNode(String a,int b)
	{
		nonterm = a;
		choice = b;
		children = new ArrayList<GTNode>();
	}
	public void addChildren(GTNode t)
	{
		children.add(t);
	}
	public void replaceChildren(int i,GTNode t)
	{
		children.remove(i);
		children.add(i, t);
	}
	public void setName(String a)
	{
		nonterm = a;
	}
	public void setNum(int b)
	{
		choice = b;
	}
	
	public String getName()
	{
		return nonterm;
	}
	public int getNum()
	{
		return choice;
	}
	
}
