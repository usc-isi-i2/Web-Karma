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
package edu.isi.karma.rep.cleaning;


public class RamblerTransformationExample implements TransformationExample {

	private String before = "";
	private String after = "";
	private String nodeID = "";
	public RamblerTransformationExample(String before,String after,String nodeID)
	{
		this.before = before;
		this.after = after;
		this.nodeID = nodeID;
	}
	public String getNodeId() {
		// TODO Auto-generated method stub
		return this.nodeID;
	}

	public String getBefore() {
		// TODO Auto-generated method stub
		return this.before;
	}

	public String getAfter() {
		// TODO Auto-generated method stub
		return this.after;
	}
	public void setBefore(String before)
	{
		this.before = before;
	}
	public void setAfter(String after)
	{
		this.after = after;
	}
	public void setNodeId(String NodeId)
	{
		this.nodeID = NodeId;
	}

}
