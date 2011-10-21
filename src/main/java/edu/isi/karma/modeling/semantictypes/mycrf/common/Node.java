package edu.isi.karma.modeling.semantictypes.mycrf.common ;

import java.util.ArrayList;

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
	