var LineLayout = function() {
	this.groups = new Object();
	this.nodes = new Object();
	this.internalNodes = new Object();
	this.links = new Object();
	this.groupInfo = new Object();
	this.reverseMap = new Object();
	this.counter = 0;
}

LineLayout.prototype.addInternalNode = function(id, level, left, top, width, height) {
	this.internalNodes[id] = {
		"id": id,
		"type": "internal",
		"level": level,
		"left": left,
		"top": top,
		"width": width,
		"height": height
	};
	var topName = id + "_top";
	var BottomName = id + "_bottom";
	if (this.groups[topName] == null) {
		this.groups[topName] = new Array();
		this.groupInfo[topName] = {
			"y": top,
			"width": width,
			"left": left
		};
	}
	if (this.groups[BottomName] == null) {
		this.groups[BottomName] = new Array();
		this.groupInfo[BottomName] = {
			"y": top - height,
			"width": width,
			"left": left
		};
	}
	return this;
}

LineLayout.prototype.addColumnNode = function(id, x, y) {
	this.nodes[id] = {
		"id": id,
		"level": 0,
		"nodeId": id,
		"x": x,
		"y": y,
		"type": "column"
	};
	return this;
}

LineLayout.prototype.addAnchorNode = function(internalNode) {
	var id = internalNode.id + "_" + (this.counter++);
	this.nodes[id] = {
		"id": id,
		"type": "anchor",
		"nodeId": internalNode.id
	};
	return this.nodes[id];
}

LineLayout.prototype.getGroupName = function(anchorNode, otherNode) {
	var anchorLevel = anchorNode.level;
	var otherLevel = otherNode.level;
	var result = anchorNode.id;
	if (anchorLevel > 0) {
		result += anchorLevel < otherLevel ? "_top" : "_bottom";
	}
	return result;
}

LineLayout.prototype.addLink = function(id, from, to) {
	this.links[id] = {
		"id": id,
		"from": from,
		"to": to
	};
	var fromNode = this.findNode(from);
	var toNode = this.findNode(to);
	var fromAnchor = fromNode;
	var toAnchor = toNode;
	var fromGroupName = this.getGroupName(fromNode, toNode);
	var toGroupName = this.getGroupName(toNode, fromNode);
	if ("internal" == fromNode.type) {
		fromAnchor = this.addAnchorNode(fromNode, fromGroupName);
		fromAnchor.linkToPropId = id;
		this.groups[fromGroupName].push(fromAnchor.id);
		fromAnchor.groupId = fromGroupName;
	}
	if ("internal" == toNode.type) {
		toAnchor = this.addAnchorNode(toNode, toGroupName);
		this.groups[toGroupName].push(toAnchor.id);
		toAnchor.groupId = toGroupName;
	}
	fromAnchor.linkTo = toAnchor.id;
	toAnchor.linkTo = fromAnchor.id;

	this.links[id].fromAnchor = fromAnchor;
	this.links[id].toAnchor = toAnchor;
	this.links[id].fromNodeType = fromNode.type;
	this.links[id].toNodeType = toNode.type;
	this.addReverseMap(id, from, to);
	return this;
}

LineLayout.prototype.addReverseMap = function(id, n1, n2) {
	this.reverseMap[n1 + "->-" + n2] = id;
	this.reverseMap[n2 + "->-" + n1] = id;
}

LineLayout.prototype.findLink = function(n1, n2) {
	return this.reverseMap[n1 + "->-" + n2];
}


LineLayout.prototype.findNode = function(id) {
	var n = this.internalNodes[id];
	return (n == null) ? this.nodes[id] : n;
}

LineLayout.prototype.assignAnchorCoordinates = function() {

	var graph = this;
	for (g in this.groups) {
		var separation = this.groupInfo[g].width / (this.groups[g].length + 1);
		var y = this.groupInfo[g].y;
		this.groups[g].reduce(function(previous, anchor) {
			var anchorNode = graph.nodes[anchor];
			anchorNode.y = y;
			anchorNode.x = previous + separation;
			return graph.nodes[anchor].x;
		}, this.groupInfo[g].left);
	}
	return this;
}


/**
 * @param {String} n name of node.
 * @return {Number} the cost of a node, defined as the cosine of the angle of the link to it.
 */
LineLayout.prototype.nodeCost = function(n) {
	var n1 = this.nodes[n];
	var n2 = this.nodes[n1.linkTo];
	var x = Math.abs(n1.x - n2.x);
	var y = Math.abs(n1.y - n2.y);
	//	return x*x/(x*x + y*y); 

	var cos = x / Math.sqrt(x * x + y * y);
	var cost = Math.exp(cos * 100);
	return cost;
};


/** 
 * @param {String} g name of group.
 * @return {Number} the cost of a group of, defined as the sum of the costs of the nodes.
 */
LineLayout.prototype.groupCost = function(g) {
	var graph = this;
	return this.groups[g].reduce(function(previous, x) {
		return previous + graph.nodeCost(x);
	}, 0);
};

/** 
 * @param {String} g name of group.
 * @return {String} the name of the node with the max cost in the group.
 */
LineLayout.prototype.groupMaxCostNode = function(g) {
	var graph = this;
	return this.groups[g].reduce(function(previous, x) {
		if (previous == null) {
			return x;
		} else {
			var cost = graph.nodeCost(previous);
			return (cost < graph.nodeCost(x)) ? x : previous;
		}
	}, null);
};



/** 
 * Suppose n1 points to l1, and n2 points to l2. We may reduce the cost by
 * swapping l1 and l2. This function will swap them if the cost savings is
 * more than the given savings threshold. The idea is that we are making the
 * lines more vertical by reducing their cosines.
 * @param {String} n1 name of a node.
 * @param {String} n2 name of a node, assumed to be in the same group as n1.
 * @param {Number} savings the amount of savings we need to achieve to accept the swap.
 * @return {String} the savings achieved by swapping n1 and n2.
 */
LineLayout.prototype.swapIfBetter = function(n1, n2, savings) {
	var node1 = this.nodes[n1];
	var node2 = this.nodes[n2];
	var l1 = node1.linkTo;
	var l2 = node2.linkTo;
	var l1PropId = node1.linkToPropId;
	var l2PropId = node2.linkToPropId;

	var costn1 = this.nodeCost(n1);
	var costn2 = this.nodeCost(n2);
	var currentCost = costn1 + costn2;

	node1.linkTo = l2;
	node1.linkToPropId = l2PropId;
	this.nodes[l2].linkTo = n1;
	node2.linkTo = l1;
	node2.linkToPropId = l1PropId;
	this.nodes[l1].linkTo = n2;
	var newCost = this.nodeCost(n1) + this.nodeCost(n2);

	var newSavings = currentCost - newCost;
	if (newSavings > savings) {
		console.log("Swap nodes: " + node1.id + "(" + costn1 + ") and " + node2.id + "(" + costn2 + ")");
		return newSavings;
	} else {
		node1.linkTo = l1;
		node1.linkToPropId = l1PropId;
		this.nodes[l1].linkTo = n1;
		node2.linkTo = l2;
		node2.linkToPropId = l2PropId;
		this.nodes[l2].linkTo = n2;
		return savings;
	}
};

/** 
 * Find the node with highest cost and do the swap that maximally reduces the cost of the group.
 * @param {String} g name of group.
 * @return {Number} the savings achieved by the best swap in g, 0 if no swap reduced cost.
 */
LineLayout.prototype.doSwap = function(g) {
	var n0 = this.groupMaxCostNode(g, graph);
	var graph = this;
	return this.groups[g].reduce(function(previous, x) {
		if (x == n0) {
			return previous;
		} else {
			return graph.swapIfBetter(n0, x, previous);
		}
	}, 0);
}

/** 
 * Do the best swap in each group and return the accumulated savings.
 * @return {String} the savings achieved.
 */
LineLayout.prototype.tryOneSwapInEachGroup = function() {
	var graph = this;
	return Object.keys(this.groups).reduce(function(previous, g) {
		//console.log("key:"+g);
		return previous + graph.doSwap(g);
	}, 0);
};

/** 
 * Iterate tryOneSwapInEachGroup until no savings can be achieved.
 * @param {Graph} graph is the whole graph.
 */
LineLayout.prototype.optimizeGroups = function() {
	do {
		savings = this.tryOneSwapInEachGroup();
		console.log("tried swap, savings: " + savings);
	} while (savings > 0);

	var me = this;
	$.each(this.nodes, function(index, node) {
		if (node.type == "anchor") {
			var linkid = node.linkToPropId;
			if (linkid) {
				var link = me.links[linkid];
				link.fromAnchor = node;
				link.toAnchor = me.nodes[node.linkTo];
			}
		}
	});

	console.log(this);
	return this;
};


/**
 * Get the X1 position for the link
 * @param: id of the link used in the addLink call
 */
LineLayout.prototype.getLinkX1 = function(id) {
	var link = this.links[id];
	var fromAnchor = link.fromAnchor;
	return fromAnchor.x;
};

/**
 * Get theY1 position for the link
 * @param: id of the link used in the addLink call
 */
LineLayout.prototype.getLinkY1 = function(id) {
	var link = this.links[id];
	var fromAnchor = link.fromAnchor;
	return fromAnchor.y;
};

/**
 * Get the X2 position for the link
 * @param: id of the link used in the addLink call
 */
LineLayout.prototype.getLinkX2 = function(id) {
	var link = this.links[id];
	var toAnchor = link.toAnchor;
	return toAnchor.x;
};

/**
 * Get the Y2 position for the link
 * @param: id of the link used in the addLink call
 */
LineLayout.prototype.getLinkY2 = function(id) {
	var link = this.links[id];
	var toAnchor = link.toAnchor;
	return toAnchor.y;
};

LineLayout.prototype.getLinkSlope = function(id) {
	var link = this.links[id];
	var x1 = link.fromAnchor.x,
		y1 = link.fromAnchor.y,
		x2 = link.toAnchor.x,
		y2 = link.toAnchor.y;

	if (x1 == x2)
		return 10000;

	var s = (y1 - y2) / (x1 - x2);

	return s;
};

LineLayout.prototype.getLinkLabelPosition = function(id) {
	var link = this.links[id];

	//y = mx + b
	var m = this.getLinkSlope(id);
	//b = y - mx
	var b = link.toAnchor.y - (m * link.toAnchor.x);

	//Now calculate the new point on the line
	var y = link.toAnchor.y;
	if (link.toNodeType == "column") {
		y = y + 12;
	} else {
		if (link.fromAnchor.y > y)
			y = y + 20;
		else
			y = y - 20;
	}
	var x = (y - b) / m;
	return [x, y];
};

/**********************************************************
 *
 * How to use this
 *
 * The idea is to create two groups per bubble in the model:
 * group1: contains one node for each link from a bubble in a level above
 * group2: contains one node for each link from a bubble in a level below
 * In addition, we make a node for each semantic type, but don't put those
 * in any group.
 *
 * Each node will have exactly one link to another node. Here is how we
 * construct it. Iterate over all the links (outgoing + incoming) of a bubble.
 * 1) Construct a group for the target of the link (if no such group exists yet)
 * 2) Add one node to each group, and a link between them.
 * Do this for every bubble in the model. Need ot be careful to not add nodes twice.
 *
 * Assign the coordinates as follows. The Y coordinates are easy as they
 * are a function of the levels, like in the current implementation.
 * Assign the X's by taking the width of the bubble, dividing it by (N+1),
 * where N is the number of nodes in a group. Then equally space the nodes
 * accordingly.
 *
 * After building the graph, call optimizeGroups, which will reasign the links
 * to minimize crossings.
 *
 **********************************************************/




/**********************************************************
 *
 * Test
 *
 **********************************************************/


// var graph1 = {
// 	groups : {
// 		g1: ["n1", "n2"],
// 		g2: ["n3", "n4", "n5"],
// 		g3: ["n6", "n7"],
// 		g4: ["n8", "n9"]
// 	},
// 	nodes: {
// 		n1: { x: 30, y: 29, linkTo: "n14" },
// 		n2: { x: 60, y: 29, linkTo: "n7" },
// 		n3: { x: 60, y: 19, linkTo: "n6" },
// 		n4: { x: 80, y: 19, linkTo: "n13" },
// 		n5: { x: 90, y: 19, linkTo: "n12" },
// 		n6: { x: 20, y: 11, linkTo: "n3" },
// 		n7: { x: 40, y: 11, linkTo: "n2" },
// 		n8: { x: 20, y: 9, linkTo: "n11" },
// 		n9: { x: 40, y: 9, linkTo: "n10" },
// 		n10: { x: 10, y: 0, linkTo: "n9" },
// 		n11: { x: 50, y: 0, linkTo: "n8" },
// 		n12: { x: 70, y: 0, linkTo: "n5" },
// 		n13: { x: 90, y: 0, linkTo: "n4" },
// 		n14: { x: 80, y: 21, linkTo: "n1" }
// 	}
// }

//optimizeGroups(graph1);

//var ll1 = new LineLayout();
//ll1
//	.addInternalNode("i1", 3, 10, 31, 40, 3)
//	.addInternalNode("i2", 2, 60, 21, 30, 3)
//	.addInternalNode("i3", 1, 60, 11, 20, 3)
//	.addColumnNode("c1", 10, 0)
//	.addColumnNode("c2", 50, 0)
//	.addColumnNode("c3", 70, 0)
//	.addColumnNode("c4", 90, 0)
//
//	.addLink("l1", "i1", "i3")
//	.addLink("l2", "i2", "i1")
//	.addLink("l3", "i2", "i3")
//	.addLink("l4", "i3", "c1")
//	.addLink("l5", "i3", "c2")
//	.addLink("l6", "i2", "c3")
//	.addLink("l7", "i2", "c4")
//
//	.assignAnchorCoordinates()
//
//	.optimizeGroups();
//	;
//
//console.log(ll1);