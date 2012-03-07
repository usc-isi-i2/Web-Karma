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
	@Override
	public String getNodeId() {
		// TODO Auto-generated method stub
		return this.nodeID;
	}

	@Override
	public String getBefore() {
		// TODO Auto-generated method stub
		return this.before;
	}

	@Override
	public String getAfter() {
		// TODO Auto-generated method stub
		return this.after;
	}

}
