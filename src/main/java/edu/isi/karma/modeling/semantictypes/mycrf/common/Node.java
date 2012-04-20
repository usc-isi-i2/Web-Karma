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
package edu.isi.karma.modeling.semantictypes.mycrf.common ;

import java.util.ArrayList;

/**
 * This class defined the nodes used in graphs.
 * It stores the features of the item that the node represents, a field or a token.
 * 
 * @author amangoel
 *
 */
public class Node {
	
	public int type ;
	public int fieldPos ;
	public int tokenPos ;

	public String string ;
	public ArrayList<String> features ;
	public int labelIndex ;
	
	protected Node parentNode ;
	protected ArrayList<Node> childrenNodeList ;
	

	public Node(int type, int topLevelPosition, int bottomLevelPosition) {
		this.type = type ;
		this.fieldPos = topLevelPosition ;
		this.tokenPos = bottomLevelPosition ;
		string = null ;
		labelIndex = -1 ;
		features = new ArrayList<String>() ;
		parentNode = null ;
		childrenNodeList = new ArrayList<Node>() ;
	}
	
	public void setParentNode(Node parentNode) {
		this.parentNode = parentNode ;
	}
	
	public Node getParentNode()	{
		return parentNode ;
	}
	
	public void addChildNode(Node childNode) {
		childrenNodeList.add(childNode) ;
	}
	
	public ArrayList<Node> getChildrenNodesList() {
		return childrenNodeList ;
	}
	
	public int numberOfChildren() {
		return childrenNodeList.size() ;
	}	

	public String toString() {
		String levelName = type == Constants.FIELD_TYPE ? "TopLevel" : "BottomLevel" ;
		return "NODE(" + levelName + ", " + fieldPos + ", " +  tokenPos + ")" ; 
	}
	
}
	
