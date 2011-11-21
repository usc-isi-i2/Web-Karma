package edu.isi.karma.rep.hierarchicalheadings;

import java.io.PrintWriter;
import java.util.List;

import org.json.JSONObject;

public interface TNode {
	List<TNode> getChildren();
	String getId();
	void generateContentJson(PrintWriter pw);
	String getColorKey();
	JSONObject generateJsonObject();
}
