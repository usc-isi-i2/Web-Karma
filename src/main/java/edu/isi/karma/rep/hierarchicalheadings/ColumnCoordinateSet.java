package edu.isi.karma.rep.hierarchicalheadings;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.rep.hierarchicalheadings.Coordinate.Position;

public class ColumnCoordinateSet {
	final private TreeSet<Coordinate> coordinates = new TreeSet<Coordinate>();
	
	private static Logger logger = LoggerFactory.getLogger(ColumnCoordinateSet.class);

	public ColumnCoordinateSet(HHTree hHtree, ColspanMap cspanmap) {
		HashMap<HHTNode, Span> spanMap = cspanmap.getSpanMap();

		// Add the integer coordinates
		List<HHTNode> roots = hHtree.getRootNodes();
		for(HHTNode root:roots) {
			Span span = spanMap.get(root);
			for(int i = span.getStartIndex(); i <= span.getEndIndex(); i++) {
				coordinates.add(new Coordinate(i));
			}
		}
		
		// Add the column separators coordinates
		for(HHTNode node: spanMap.keySet()) {
			Span span = spanMap.get(node);
			if(!node.isLeaf()){
				coordinates.add(new Coordinate(span.getStartIndex(), Position.left, node.getDepth()));
				coordinates.add(new Coordinate(span.getEndIndex(), Position.right, node.getDepth()));
			}
		}
		
//		for(Coordinate cr:coordinates){
//			System.out.println(cr);
//		}	
	}
	
	public int getNumberOfCoordinatesBetweenTwoIndexes(int startIndex, int endIndex, int depth) {
		// If any column separators exist around the indexes
		Coordinate cs1 = new Coordinate(startIndex, Position.left, depth);
		Coordinate cs2 = new Coordinate(endIndex, Position.right, depth);
		
		if(coordinates.contains(cs1) && coordinates.contains(cs2)) {
			return coordinates.subSet(cs1, false, cs2, false).size();
		}
		
		// If the column separators do not exist
		Coordinate c1 = new Coordinate(startIndex);
		Coordinate c2 = new Coordinate(endIndex);
		if(!coordinates.contains(c1) || !coordinates.contains(c2)) {
			logger.error("Coordinates not found for the input indexes! Start Index: " 
					+ startIndex + " EndIndex: " + endIndex);
			return 0;
		}
		return coordinates.subSet(c1, true, c2, true).size();
	}
	
	public int getCoordinatesCountForIndex (int index) {
		int count = 0;
		for(Coordinate c : coordinates) {
			if(c.getIndex() > index)
				break;
			if (c.getIndex() == index)
				count++;
		}
		return count;
	}
}