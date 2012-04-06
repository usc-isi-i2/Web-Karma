package edu.isi.karma.rep.hierarchicalheadings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Coordinate implements Comparable<Coordinate> {

	final private Position position;
	final private int depth;
	final private int index;
	final private Type type;

	public enum Position {
		left, right, top, bottom, center
	}

	private enum Type {
		columnSeparator, content
	}

	public Coordinate(int index) {
		this.index = index;
		this.position = Position.center;
		this.depth = -1;
		type = Type.content;
	}

	public Coordinate(int index, Position position, int depth) {
		this.index = index;
		this.position = position;
		this.depth = depth;
		type = Type.columnSeparator;
	}

	public Position getPosition() {
		return position;
	}

	public int getDepth() {
		return depth;
	}

	public int getIndex() {
		return index;
	}

	public Type getType() {
		return type;
	}

	@Override
	public int compareTo(Coordinate o) {
		if (o.type == Type.content) {
			if (this.type == Type.content) {
				return this.index - o.index;
			} else {
				// If they have the same index
				if (this.index - o.index == 0) {
					// left and top is always less than right, center, and
					// bottom
					if (this.position == Position.left
							|| this.position == Position.top)
						return -1;
					else
						return 1;

				} else {
					return this.index - o.index;
				}
			}
		} else {
			if (this.type == Type.content) {
				if (this.index - o.index == 0) {
					// left and top is always less than right, center, and
					// bottom
					if (o.position == Position.left
							|| o.position == Position.top)
						return 1;
					else
						return -1;

				} else {
					return this.index - o.index;
				}
			} else {
				// If they have the same index
				if (this.index - o.index == 0) {
					// if the position is same, use the depth
					if (this.position == o.position) {
						if (this.position == Position.left
								|| this.position == Position.top)
							return this.depth - o.depth;
						else if (this.position == Position.right
								|| this.position == Position.bottom) {
							return o.depth - this.depth;
						}
					}
					// top is always less then bottom and center
					if (this.position == Position.left
							|| this.position == Position.top)
						return -1;
					else
						return 1;

				} else {
					return this.index - o.index;
				}
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj.getClass() != getClass()) {
			return false;
		}
		
		Coordinate cr = (Coordinate) obj;
		if(cr.type == this.type && cr.depth == this.depth 
				&& cr.index == this.index && cr.position == this.position)
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		String str = this.type + "&" + this.depth + "&" + this.index + "&" + this.position;
		return str.hashCode();
	}

	@Override
	public String toString() {
		return "Coordinate [position=" + position + ", depth=" + depth
				+ ", index=" + index + ", type=" + type + "]";
	}

	public static void main(String[] args) {
		List<Coordinate> c = new ArrayList<Coordinate>();
		c.add(new Coordinate(1, Position.left, 2));
		c.add(new Coordinate(1, Position.left, 3));
		c.add(new Coordinate(1, Position.right, 3));
		c.add(new Coordinate(1, Position.right, 2));

		c.add(new Coordinate(1));
		c.add(new Coordinate(0));
		c.add(new Coordinate(2));
		c.add(new Coordinate(3));

		c.add(new Coordinate(1, Position.right, 1));
		c.add(new Coordinate(1, Position.left, 1));

		for(Coordinate cr:c)
			System.out.println(cr);
		System.out.println("SORTING ***************");
		Collections.sort(c);
		for(Coordinate cr:c)
			System.out.println(cr);
	}
}
