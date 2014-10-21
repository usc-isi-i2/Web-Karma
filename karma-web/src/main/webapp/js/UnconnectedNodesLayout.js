var UnconnectedNodesLayout = function() {
	this.nodes = new Array();
	this.nodeLinks = new Object();
	this.nodesAtLevel = new Object();
	this.maxLevel = -1;
};

UnconnectedNodesLayout.prototype.addNode = function(node, links) {
	this.nodes.push(node);
	this.nodeLinks[node.id] = links;
};

UnconnectedNodesLayout.prototype.computeNodePositions = function(h, levelHeight, width, maxRight) {
	this.maxLevel = -1;
	this.h = h;
	this.levelHeight = levelHeight;

	//Compute Nodes at each level
	for (var i = 0; i < this.nodes.length; i++) {
		var node = this.nodes[i];
		var level = node["height"];
		node["width"] = width;
		node["x"] = 10 + width / 2;
		this._computeNodeY(node);

		if (this.nodesAtLevel[level] == null) {
			this.nodesAtLevel[level] = new Array();
		}
		this.nodesAtLevel[level].push(node);
		if (level > this.maxLevel)
			this.maxLevel = level;
	}

	this._layoutNodesAtLevel1(width);
	for (var i = 2; i <= this.maxLevel; i++)
		this._layoutNodesAtLevel(i, width);


	for (var i = 1; i <= this.maxLevel; i++)
		this._computeNodeWidthOnLinks(i);

	var allIn = false;
	while (!allIn) {
		allIn = true;
		//bump levels if nodes are out of the maxLeft
		for (var i = 1; i <= this.maxLevel; i++) {
			var inAtLevel = this._checkMaxLeftAtLevel(i, maxRight);
			if (!inAtLevel) {
				allIn = false;
			}
		}
		if (!allIn) {
			this._removeEmptyLevels(this.maxLevel);
		}
		this._computeMaxLevel();
	}

	console.log("Final floating node positions:");
	for (var i = 0; i < this.nodes.length; i++) {
		var node = this.nodes[i];
		console.log(node.id + " x:" + node["x"] + " left:" + (node["x"] - node["width"] / 2));
	}
};

UnconnectedNodesLayout.prototype.getMaxLevel = function() {
	return this.maxLevel;
};

UnconnectedNodesLayout.prototype.setNewH = function(h) {
	this.h = h;
	for (var i = 0; i < this.nodes.length; i++) {
		var node = this.nodes[i];
		this._computeNodeY(node);
	}
};

UnconnectedNodesLayout.prototype._layoutNodesAtLevel1 = function(width) {
	var nodes = this.nodesAtLevel[1];
	if (nodes) {
		var left = 10;

		for (var i = 0; i < nodes.length; i++) {
			var node = nodes[i];
			node["x"] = left + width / 2;
			left += width + 50;
		}
	}
};

UnconnectedNodesLayout.prototype._getNodeById = function(id) {
	for (var i = 0; i < this.nodes.length; i++) {
		var node = this.nodes[i];
		if (node.id == id)
			return node;
	}
	return null;
};

UnconnectedNodesLayout.prototype._layoutNodesAtLevel = function(level, width) {
	var nodes = this.nodesAtLevel[level];
	if (nodes) {
		for (var i = 0; i < nodes.length; i++) {
			var node = nodes[i];

			var links = this.nodeLinks[node.id];
			var left = 999999;
			if (links) {
				for (var j = 0; j < links.length; j++) {
					var linkId = links[j];
					var nodeConnect = this._getNodeById(linkId);
					if (nodeConnect["height"] > level)
						continue;
					if (nodeConnect["x"] < left)
						left = nodeConnect["x"] - width / 2;
				}
			}

			if (left == 999999) {
				var maxXAtPrevLevel = this._getMaxXAtLevel(level - 1);
				if (maxXAtPrevLevel != -1)
					left = this._getMaxXAtLevel(level - 1) + width + 50;
				else
					left = 10;
				for (var j = 0; j < i; j++) {
					var doneNode = nodes[j];
					var doneLeft = doneNode["x"] - width / 2;
					if (left <= doneLeft) {
						left = doneLeft + width + 50;
					}
				}
			}
			if (left < 10) left = 10;
			node["x"] = left + width / 2;
		}
	}
};

UnconnectedNodesLayout.prototype._getMaxXAtLevel = function(level) {
	var nodes = this.nodesAtLevel[level];
	var maxLeft = -1;
	if (nodes) {
		for (var i = 0; i < nodes.length; i++) {
			var node = nodes[i];
			var width = node["width"];
			var x = node["x"] - width / 2;
			if (x > maxLeft)
				maxLeft = x;
		}
	}
	return maxLeft;
};

UnconnectedNodesLayout.prototype._computeNodeWidthOnLinks = function(level) {
	var nodes = this.nodesAtLevel[level];
	if (nodes) {
		for (var i = 0; i < nodes.length; i++) {
			var node = nodes[i];
			var links = this.nodeLinks[node.id];
			if (links) {
				var width = node["width"];
				var extremeLeftX = node["x"] - width / 2;
				var extremeRightX = node["x"] + width / 2;

				for (var j = 0; j < links.length; j++) {
					var linkId = links[j];
					var nodeConnect = this._getNodeById(linkId);
					if (nodeConnect["height"] > level)
						continue;

					var ncWidth = nodeConnect["width"];
					var x = nodeConnect["x"];
					//var y = nodeConnect["y"];
					var leftX = x - ncWidth / 2;
					var rightX = x + ncWidth / 2;
					if (leftX < extremeLeftX)
						extremeLeftX = leftX;
					if (rightX > extremeRightX)
						extremeRightX = rightX;
				}

				width = extremeRightX - extremeLeftX;
				if (extremeLeftX < 10) extremeLeftX = 10;
				node["x"] = extremeLeftX + width / 2;
				node["width"] = width;
			}
		}
	}
};

UnconnectedNodesLayout.prototype._checkMaxLeftAtLevel = function(level, maxRight) {
	var nodes = this.nodesAtLevel[level];
	var allIn = true;
	if (nodes) {
		for (var i = 0; i < nodes.length; i++) {
			var node = nodes[i];
			var x = node["x"];
			var width = node["width"];
			var rightX = x + width / 2;
			var leftX = x - width / 2;
			if (rightX > maxRight) {
				//need to bump this
				allIn = false;
				this._setNodeLevel(node, this.maxLevel + node["height"]);
				var left = leftX - maxRight;
				if (left < 10) left = 10;
				node["x"] = left + width / 2;
			}
		}
	}
	return allIn;
};

UnconnectedNodesLayout.prototype._setNodeLevel = function(node, level) {
	node["height"] = level;
	if (this.nodesAtLevel[level] == null) {
		this.nodesAtLevel[level] = new Array();
	}
	this.nodesAtLevel[level].push(node);
	this._computeNodeY(node);
};

UnconnectedNodesLayout.prototype._removeEmptyLevels = function(startLevel) {
	this._computeMaxLevel();
	var max = this.maxLevel;
	for (var i = startLevel; i < max; i++) {
		var nodes = this.nodesAtLevel[i];
		if (nodes) {

		} else {
			//move all at i+1 here
			var next = i + 1;
			var done = false;
			while (!done && next <= max) {
				var upper = this.nodesAtLevel[next];
				if (upper) {
					done = true;
					this.nodesAtLevel[next] = undefined;
					for (var j = 0; j < upper.length; j++) {
						var node = upper[j];
						this._setNodeLevel(node, i);
					}
				}
				next++;
			}
		}
	}
};

UnconnectedNodesLayout.prototype._computeNodeY = function(node) {
	node["y"] = this.h - ((node["height"] * this.levelHeight));
};

UnconnectedNodesLayout.prototype._computeMaxLevel = function() {
	this.maxLevel = -1;
	for (var i = 0; i < this.nodes.length; i++) {
		var node = this.nodes[i];
		var level = node["height"];

		if (level > this.maxLevel)
			this.maxLevel = level;
	}
};