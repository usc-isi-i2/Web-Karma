var UnconnectedNodesLayout = function () {
	this.nodes = new Array();
	this.nodeLinks = new Object();
	this.nodesAtLevel = new Object();
};

UnconnectedNodesLayout.prototype.addNode = function(node, links) {
	this.nodes.push(node);
	this.nodeLinks[node.id] = links;
};

UnconnectedNodesLayout.prototype.computeNodePositions = function(h, levelHeight, width) {
	var maxLevel = -1;
	
	//Compute Nodes at each level
	for(var i=0; i<this.nodes.length; i++) {
		var node = this.nodes[i];
		var level = node["height"];
		if (this.nodesAtLevel[level] == null) {
			this.nodesAtLevel[level] = new Array();
		}
		this.nodesAtLevel[level].push(node);
		if(level > maxLevel)
			maxLevel = level;
	}
	
	for(var i=0; i<=maxLevel; i++)
		this._layoutNodesAtLevel(i, h, levelHeight, width);
	
	for(var i=1; i<=maxLevel; i++)
		this._computeNodeWidthOnLinks(i);
};

UnconnectedNodesLayout.prototype._layoutNodesAtLevel = function(level, h, levelHeight, width) {
	var nodes = this.nodesAtLevel[level];
	if(nodes) {
		var left = 10;
		
		for(var i=0; i<nodes.length; i++) {
			var node = nodes[i];
			node["width"] = width;
			node["x"] = left + width/2;
			node["y"] = h - ((node["height"] * levelHeight));
			left += width + 50;
		}
	}
};

UnconnectedNodesLayout.prototype._computeNodeWidthOnLinks = function(level) {
	var nodes = this.nodesAtLevel[level];
	if(nodes) {
		for(var i=0; i<nodes.length; i++) {
			var node = nodes[i];
			var links = this.nodeLinks[node.id];
			if(links) {
				var width = node["width"];
				var extremeLeftX = node["x"] - width/2;
				var extremeRightX = node["x"] + width/2;
				
				for(var j=0; j<links.length; j++) {
					var linkId = links[j];
					var nodeConnect = this.nodes[linkId];
					if(nodeConnect["height"] > level)
						continue;
					
					var ncWidth = nodeConnect["width"];
		            var x = nodeConnect["x"];
		            //var y = nodeConnect["y"];
		            var leftX = x - ncWidth/2;
		            var rightX = x + ncWidth/2;
		            if(leftX < extremeLeftX)
		                extremeLeftX = leftX;
		            if(rightX > extremeRightX)
		                extremeRightX = rightX;
				}
				
				width = extremeRightX - extremeLeftX;
				node["x"] = extremeLeftX + width/2;
			    node["width"] = width;
			}
		}
	}
};
