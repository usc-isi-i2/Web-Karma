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

import java.util.HashSet;
import java.util.Vector;
class Tuple
{
	//(beforetoks, aftertoks)
	private Vector<Vector<TNode>> tuple = new Vector<Vector<TNode>>();
	public Tuple(Vector<TNode> bef,Vector<TNode> aft)
	{
		tuple.add(bef);
		tuple.add(aft);
	}
	public Vector<TNode> getBefore()
	{
		return tuple.get(0);
	}
	public Vector<TNode> getafter()
	{
		return tuple.get(1);
	}
}
public class Description 
{
	public Vector<Vector<Vector<HashSet<String>>>> desc = new Vector<Vector<Vector<HashSet<String>>>>();
	//((tup11,tup21),(tup12,tup22)....)
	public Vector<Vector<Vector<Tuple>>> sequences = new Vector<Vector<Vector<Tuple>>>();
	public Description()
	{
	}
	public Description(Vector<Vector<Vector<HashSet<String>>>> desc)
	{
		this.desc = desc;
	}
	public void delComponent(int index)
	{
		desc.remove(index);
		sequences.remove(index);
	}
	public Vector<Vector<Vector<HashSet<String>>>> getDesc()
	{
		return desc;
	}
	public Vector<Vector<Vector<Tuple>>> getSeqs()
	{
		return this.sequences;
	}
	
	public void addSeqs(Vector<Vector<Tuple>> seq)
	{
		sequences.add(seq);
	}
	public void addDesc(Vector<Vector<HashSet<String>>> seq)
	{
		desc.add(seq);
	}
	public void newDesc()
	{
		this.desc = new Vector<Vector<Vector<HashSet<String>>>>();
	}
	public void newSeqs()
	{
		this.sequences = new Vector<Vector<Vector<Tuple>>>();
	}
}
