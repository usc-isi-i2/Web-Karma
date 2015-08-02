package edu.isi.karma.cleaning.features;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.isi.karma.cleaning.Ruler;
import edu.isi.karma.cleaning.grammartree.TNode;

public class RecordTokenFirstOccPosition implements Feature{
	public String name = "";
	public String value = "";
	public String tar = "";
	public double score = 0.0;
	public final double cannot_find = 0.01;

	public RecordTokenFirstOccPosition(String name, String value, String tar) {
		this.name = "attr_pos_" + name;
		this.value = value;
		this.tar = tar;
		score = this.computerScore();
	}
	public String reverseEscape(String tar){
		if(tar.length() >= 2){
			if(tar.substring(0, 1).compareTo("\\")==0){
				return tar.substring(1);
			}
		}
		return tar;
	}
	public double computerScore() {
		Ruler r = new Ruler();
		r.setNewInput(value);
		String nonescaped = reverseEscape(tar);
		Vector<TNode> nodes = r.vec;		
		for(int i = 0; i < nodes.size(); i++){
			if(nodes.get(i).text.equals(nonescaped)){
				return i;
			}
		}
		return cannot_find;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public double getScore(String value) {
		this.value = value;
		return this.computerScore();
	}
}
