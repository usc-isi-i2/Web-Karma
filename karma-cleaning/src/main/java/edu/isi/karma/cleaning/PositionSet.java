package edu.isi.karma.cleaning;

import java.util.ArrayList;
import java.util.Collection;

public class PositionSet implements GrammarTreeNode {

	ArrayList<Position> spaces = new ArrayList<Position>();
	int size = 1;

	public PositionSet(Collection<Position> poss) {
		this.spaces.addAll(poss);
		if (spaces != null && spaces.size() > 0)
			for (Position p : spaces) {
				if (p != null) {
					size = p.rules.size();
					break;
				}
			}
	}

	@Override
	public String toProgram() {
		for (Position p : spaces) {
			if (p != null) {
				String rule1 = p.VerifySpace(0);
				if (rule1.indexOf("null") == -1) {
					return rule1;
				}
			}
		}
		return "null";
	}

	@Override
	public GrammarTreeNode mergewith(GrammarTreeNode a) {
		Position position = (Position) a;
		ArrayList<Position> x = new ArrayList<Position>();
		for (int i = 0; i < this.spaces.size(); i++) {
			Position xp = spaces.get(i).mergewith(position);
			x.add(xp);
		}
		PositionSet ps = new PositionSet(x);
		return ps;
	}

	@Override
	public String getNodeType() {
		// TODO Auto-generated method stub
		return this.getClass().getName();
	}

	@Override
	public double getScore() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getrepString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createTotalOrderVector() {
		// TODO Auto-generated method stub

	}

	@Override
	public void emptyState() {
		// TODO Auto-generated method stub

	}

	@Override
	public long size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getProgram() {
		// TODO Auto-generated method stub
		return null;
	}

}
