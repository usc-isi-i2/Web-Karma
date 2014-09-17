D3ModelLayout = function(htmlElement) {
	padding = 35;
	width=0;           
	height=0;

	linkClickListener = null;
	nodeClickListener = null;
	
	test = [];
	anchorData = [];                           //store anchor nodes
	nodesData = [];                            //store all nodes includes anchors
	linksData = [];                            //links data
	noCycleLinksData = [];                     //cycles are removed
	cycles = [];                               //all cycles, each cycle contians all nodes in that cycle.
	textData = [];                             //text nodes
	textLinksData = [];                        //text links
	idMap = [];                                //map from label to id
	edgeIdMap = [];                            //map of edge's id
	layerMap = [];                             //store nodes'id in sequence of layers
	nodesChildren = [];                        //store node's id and its children pair
	SCCindex = 0;                              //strong connected component node's index
	SCCNodes = [];                             //SCC nodes set
	SCCtmpNodes = [];                          //the nodes stack of SCC
	layerLabel = [];                           //layers are divided into sections based on its layer
	map = new Map();


	nodeRadius = 4;
	unitLinkLength = 70;                       //difference between layers
	outsideUnitLinkLength = 30;                //length for outside links
	maxLayer = 0;                              //max layer number, base 0
	reshuffleFrequency = 10;                   //pixel changes to excute scroll bar event
	xOffset = 0;                               //x position offset
	outsideNodesNum = 0;                       //outside nodes number
	firstTime = true;                          //first time to load the force-layout
	maxLabelLength = 0;    
	cScale = d3.scale.category20();

	//create svg
	svg = d3.select(htmlElement)                         
	    .append("svg");
	    //.on("mousemove", mousemove);

	//svg to draw nodes and links
	forceSVG = svg.append("g");

	//place to show mouse coordinate
	pos = svg.append("text")
		.attr("fill", "black")
		.attr("font-size", 10);

	//coefficient of force move nodes to top
	upperForceScale = d3.scale.linear()
		//.domain([0, height]) 
		.range([1, 0]);        

	nodes = forceSVG.selectAll(".node");       //all nodes    
	links = forceSVG.selectAll(".link");       //all links
	labels = forceSVG.selectAll(".label");     //all labels
	labelLinks = forceSVG.selectAll(".labelLinks"); //all label links.
	linkArrow = forceSVG.selectAll(".linkArrow");   //little triangle of links
	labelFrame = forceSVG.selectAll(".labelFrame"); //the frame of each label

	//force layout for nodes
	force = d3.layout.force()
		//.size([Math.max(width, columns * barWidth), height])
		.gravity(0)
		.linkStrength(function(d){
			if (d.type == "edgeLink"){
				if (d.target.outside.isOutside){
					return 1;
				}
				return 0;
			}
			if (d.source.outside.isOutside && d.target.outside.isOutside){
				return 1;
			} else if (d.source.outside.isOutside || d.target.outside.isOutside){
				if (!d.target.outside.isOutside && d.target.type == "anchor"){
					return 0.1;
				}
				return 0.8;
			}
			return 0;
		})
		.friction(0.8)
		//.theta(0.1)
		.charge(-100)
		.linkDistance(outsideUnitLinkLength)
		.on("tick", tick);
		// force layout for labels

	labelForce = d3.layout.force()
		//.size([Math.max(width, columns * barWidth), height])
		.gravity(0)
		.friction(0.8)
		.charge(function(d){
			return 0;
		})
		.linkDistance(0)
		.linkStrength(3);
		//node can be dragged to the position you want

	drag = force.drag()
		.on("dragstart", function(d) {
			if (!d.outside.isOutside){
	  			d3.select(this).classed("fixed", d.fixed = true);
			}
	  	})
	    .on("dragend", function(d) {
	  		if (!d.outside.isOutside){
	  			d.position.x = d.x;
	  			d.position.y = d.y;
	  		}
		});
	//draw nodes and links




	function transit(){
		links = links.data(linksData)
		links.enter()
			.append("path")
			.classed("link", true)
			.attr("stroke", "#555")
			.attr("stroke-width", 0)
			//.attr("opacity", 0.5)
			.attr("id", function(d, i){
				return d.source.id + "link" + d.target.id;
			})
			.attr("fill", "none");
		links.transition()
			.delay(250)
			.duration(500)
			.attr("stroke-width", 1);
		links.exit()
			.transition()
			.duration(500)
			.attr("opacity", 0)
			.remove();


		linkArrow = linkArrow.data(linksData);
		linkArrow.enter()
			.append("polygon")
			.attr("fill", "#555")
		linkArrow.exit()
			.transition()
			.duration(500)
			.attr("opacity", 500)
			.remove();


		labels = labels.data(textData);
		labels.enter()
			.append("g")
			.classed("label", true);
		labels.exit()
			.transition()
			.duration(500)
			.attr("opacity", 0)
			.remove();

		var test = labels.append("circle")
			.attr("r", 0)
			//.attr("fill", "black");
		
		labelFrame = labels.append("g")
			.attr("class", function(d){
				if (d.type == "nodeLabel"){
					return "nodeLabel";
				} else if (d.type == "linkLabel"){
					return "linkLabel";
				}
				return "unusedLable";
			})
			.attr("id", function(d, i){
				if (d.type == "nodeLabel"){
					return "nodeLabel" + d.node.id;
				} else if (d.type == "linkLabel"){
					return "linkLabel" + nodesData[d.node.src].id + " " + nodesData[d.node.tgt].id;
				}
				return "labelPart" + i;
			})
			.attr("opacity", 0);
		labelText = labelFrame.append("text")
			.text(function(d, i){
				return i % 2 == 0 ? "" : d.content;
			})
			.attr("fill", function(d){
				if (d.type == "nodeLabel"){
					return "white";
				}
				if (d.type == "linkLabel"){
					return "black";
				}
				return "none";
			})
			.attr("font-weight", function(d){
				if (d.type == "nodeLabel"){
					return "bold";
				}
				return "normal";
			})
			.classed("labelText", true)
			.attr("x", function(d){
				return -(this.getBBox().width / 2);
			})
			.attr("y", -3);
		labelBoard = labelFrame.append("path")
			//.attr("stroke-width", 1)
			//.attr("stroke", "black")
			.attr("fill", function(d){
				if (d.type == "nodeLabel"){
					return "#555";
				}
				if (d.type == "linkLabel"){
					return "#ddd";
				}
				return "none";
			})
			.attr("d", function(d){
				if (d.type != "nodeLabel" && d.type != "linkLabel"){
					d.width = 0;
					return "";
				}
				var textWidth = Math.ceil(this.parentNode.firstChild.getBBox().width);
				var textHeight = Math.ceil(this.parentNode.firstChild.getBBox().height);
				//console.log(textWidth + " " + textHeight);
				var dx = -textWidth / 2;
				var dy = 0;
				d.width = textWidth + textHeight / 3 * 2;
				d.height = textHeight;
				if (d.type == "linkLabel" || (d.node.type != "anchor")){
					maxLabelLength = Math.max(maxLabelLength, d.width);
				}
				return "M " + dx + " " + dy + " L " + (dx + textWidth) + " " + dy + " Q " + (dx + textWidth + textHeight / 3) + " " + (dy - textHeight / 2) + " " + (dx + textWidth) + " " + (dy - textHeight) + " L " + dx + " " + (dy - textHeight) + " Q " + (dx - textHeight / 3) + " " + (dy - textHeight / 2) + " " + dx + " " + dy;
			})
		labelBoard.moveToBack();
		labelClickBoard = labels.filter(function(d, i){
			return (i % 2 == 1) && d.node.type != "anchor";
		})
		.append("rect")
		.classed("clickBoard", true)
		.attr("fill", "transparent")
		.attr("width", function(d){
			var w = Math.ceil(this.parentNode.childNodes[1].getBBox().width);
			return w;
		})
		.attr("height", function(d){
			var h = Math.ceil(this.parentNode.childNodes[1].getBBox().height);
			return h;
		})
		.attr("x", function(d){
			var w = Math.ceil(this.parentNode.childNodes[1].getBBox().width);
			return -w / 2;
		})
		.attr("y", function(d){
			var h = Math.ceil(this.parentNode.childNodes[1].getBBox().height);
			return -h;
		})
		.on("click", function(d){
			if (d.type == "linkLabel"){
				if(linkClickListener != null)
					linkClickListener(d);
			} else {
				if(nodeClickListener != null)
					nodeClickListener(d);
			}
			//console.log(d.type);
		})


			
		labelLinks = labelLinks.data(textLinksData);
		labelLinks.enter()
			.append("line")
			.classed("labelLinks", true)
			.attr("stroke-width", 0);
		labelLinks.exit()
			.transition()
			.duration(500)
			.attr("opacity", 0)
			.remove();



		nodes = nodes.data(nodesData);
		nodes.enter()
			.append("circle")
			.classed("node", true)
			.attr("r", 1)
			.attr("opacity", 0.7)
			.attr("fill", function(d, i){
				d.position = {};
				d.position.x = d.xpos;
				d.position.y = height - nodeRadius - d.layer * unitLinkLength;
				d.outside = {};
				d.outside.position = {};
				d.outside.isOutside = false;
				if (d.noLayer){
					//d.outside.isOutside = true;
					d.position.x = -1;
					d.position.y = -1;
				}
				return "red";
			})
			.attr("id", function(d, i){
				return "node" + d.id;
			})
			.call(drag)
			.on("click", function(d){
				if (d.outside.isOutside && !d.noLayer){
					if (d.position.x < xOffset){		
						var destination = Math.max(0, d.position.x - width / 2);
						var xPosition = window.pageXOffset;
						var differ = Math.max(30, (xPosition - destination) / 200);
						var interval = setInterval(function(){
							xPosition -= differ;
							if (xPosition > destination){
								$(window).scrollLeft(xPosition);
							} else {
								clearInterval(interval);
							}
						}, 10);
					} else {
						var destination = Math.min(width - window.innerWidth, d.position.x - width / 2);
						var xPosition = window.pageXOffset;
						var differ = Math.max(30, (destination - xPosition) / 200);
						var interval = setInterval(function(){
							xPosition += differ;
							if (xPosition < destination){
								$(window).scrollLeft(xPosition);
							} else {
								clearInterval(interval);
							}
						}, 10);
					}				
				}
			})
			.on("dblclick", function(d) {
	  			d3.select(this).classed("fixed", d.fixed = false);
	  			d.position.x = d.xpos;
				d.position.y = height - nodeRadius - d.layer * unitLinkLength;
			})
			.on("mouseover", function(d){
				d3.select("#nodeLabel" + d.id)
					.attr("opacity", 1);			
				d.showLabel = true;
				if (d.parent){
					d3.select("#nodeLabel" + d.parent)
						.attr("opacity", 1);
					nodesData[d.parent].showLabel = true;
				}
				if (nodesChildren[d.id] == undefined){
					return;
				}
				nodesChildren[d.id].forEach(function(e){
					if (nodesData[e].degree >= 2){
						d3.select("#nodeLabel" + e)
							.attr("opacity", 1);
						nodesData[e].showLabel = true;
					}
				});
				labelForce.start();
			})
			.on("mouseout", function(d){
				if (d.outside.isOutside){
					if (d.degree < 3){
						d3.select("#nodeLabel" + d.id)
							.attr("opacity", 0);
						d.showLabel = false;
					}
					if (d.parent && nodesData[d.parent].degree < 3){
						d3.select("#nodeLabel" + d.parent)
							.attr("opacity", 0);
						d.showLabel = true;
					}
					if (nodesChildren[d.id] == undefined){
						return;
					}
					nodesChildren[d.id].forEach(function(e){
						if (nodesData[e].degree < 3){
							d3.select("#nodeLabel" + e)
								.attr("opacity", 0);
							nodesData[e].showLabel = false;
						}
					});
				} else if (d.type == "anchor"){
					d3.select("#nodeLabel" + d.id)
						.attr("opacity", 0);
					d.showLabel = false;
				}
			});
		nodes.transition()
			.duration(500)
			.attr("r", nodeRadius);
		nodes.exit()
			.transition()
			.duration(500)
			.attr("opacity", 500)
			.remove();
	}

	//tick function for force-layout
	function tick(e){
		labelForce.start();
		var k = 0.03 * e.alpha;
		var kY = 0.05;


		nodes.attr("cx", function(d) {
				if (d.outside.isOutside){				
					return d.x = Math.max(xOffset + nodeRadius, Math.min(xOffset + width - nodeRadius, d.x));
				} 
				var differX = d.position.x - d.x;
				if (d.type == "anchor"){
					d.x += differX * kY;
					if (Math.abs(d.position.x - d.x) < 20){
	        			d.x = d.position.x;
	        		}
				} else {
	    			d.x += differX * kY;
				}
	    		return d.x;
	    	})
	        .attr("cy", function(d) { 
	        	if (d.outside.isOutside){
	        		d.y += -d.y * k;
	        		return d.y = Math.max(nodeRadius, Math.min(height - nodeRadius, d.y));
	        	}
	        	var differY = d.position.y - d.y;
	        	if (d.type == "anchor"){
	        		d.y += differY * kY;
	        		if (Math.abs(d.position.y - d.y) < 20 || d.y >= height - nodeRadius){
	        			d.y = d.position.y;
	        		}
	        	} else {
	        		d.y += differY * kY;
	        	}
	        	return d.y = Math.max(nodeRadius, Math.min(height - nodeRadius, d.y));
	        });
	    


	    labels.each(function(d, i){
	    	if (i % 2 == 0){
	    		if (d.type){
	    			var a = nodesData[d.node.src];
	    			var b = nodesData[d.node.tgt];
	    			d.x = (a.x + b.x) / 2 + (nodesData[d.node.tgt].x - nodesData[d.node.src].x) / 6;
	    			d.y = (a.y + b.y) / 2;
	    		} else {
	    			d.x = d.node.x;
	    			d.y = d.node.y;
	    		} 
	    	} else {
				var dx = textData[i - 1].x;
				var dy = textData[i - 1].y;
				d.y = dy;
				d.x = dx;
	    	}
	    })

	    if (!firstTime){
	    	links.call(updateLink);
	    	labels.call(updateLabel);
	    	labelLinks.call(updateLabelLink);  	
		}
	}

	//updata link for tick function
	var updateLink = function(){
	    this.attr("d", function(d){
	    	var a = d.source;
			var b = d.target;

			
			if (d.type == "edgeLink"){
				if (b.outside.isOutside){
					return "M" + b.x + " " + b.y + " L " + a.x + " " + a.y;
				}
			} else {
				if ((a.outside.isOutside && b.outside.isOutside) || (b.outside.isOutside && b.type == "anchor") || b.noLayer){
					return "M" + b.x + " " + b.y + " L " + a.x + " " + a.y;
				}
			}
			
			var ax = a.x - (a.x - b.x) / 3;
			var by = b.y - (b.y - a.y) / 3;
			var p = "M " + b.x + " " + b.y + " C " + b.x + " " + by + " " + ax + " " + a.y + " " + a.x + " " + a.y;

			//calculate the position for upward arrow
			if (b.layer > a.layer){
				d.up = true;
				d.arrow = {};
				d.arrow.x = b.x;
				d.arrow.y = b.y;
				getArrowAngle(d, ax, by, 0.05);
			}
			else {
			//calculate the position for downward arrow
				var y = (d.target.type == "anchor") ? b.y : b.y - 12;
				d.arrow = {};
				d.arrow.y = y;
				var i, j;			
				for (i = 0; i < 1; i += 0.1){
					if (getXofBezier(d, ax, by, i, y)){
						break;
					}
				}
				i -= 0.1;
				for (j = 0; j < 0.1; j += 0.01){
					if (getXofBezier(d, ax, by, i + j, y)){
						break;
					}
				}   		
				d.arrow.t = i + j;
				getArrowAngle(d, ax, by, i + j + 0.05);
				if (b.y < a.y){
					d.angle -= 180;
				}
			}
			return p;
		});	

		linkArrow.attr("points", function(d){
			if (d.target.outside.isOutside || d.target.noLayer){
				return "";
			}
			if (d.up){
				var ax = d.arrow.x;
				var ay = d.arrow.y;
				var bx = ax - 6;
				var by = ay + 6 * 1.7;
				var cx = ax + 6;
				var cy = by;
			} else {
				var ax = d.arrow.x;
				var ay = d.arrow.y;
				var bx = ax - 6;
				var by = ay - 6 * 1.7;
				var cx = ax + 6;
				var cy = by;
			}
			return ax + "," + ay + " " + bx + "," + by + " " + cx + "," + cy;
		})
		.attr("transform", function(d){
			if (!d.target.outside.isOutside && !d.target.noLayer){
				return "rotate(" + d.angle + " " + d.arrow.x + " " + d.arrow.y + ")";
			}
		})
	}

	//calculate coordinate on the bezier curve
	function getXofBezier(d, ax, by, t, y){
		var a = d.source;
		var b = d.target;
		var x = b.x;
		var tmpWidth = 100;
		var obj = d3.select("nodeLabel" + b.id)
			.attr("id", function(d){
				tmpWidth = d.width / 2;
			})
		var Ax = ((1 - t) * b.x) + (t * b.x);
		var Ay = ((1 - t) * b.y) + (t * by);
		var Bx = ((1 - t) * b.x) + (t * ax);
		var By = ((1 - t) * by) + (t * a.y);
		var Cx = ((1 - t) * ax) + (t * a.x);
		var Cy = ((1 - t) * a.y) + (t * a.y);
		var Dx = ((1 - t) * Ax ) + (t * Bx);
	    var Dy = ((1 - t) * Ay ) + (t * By);
	    var Ex = ((1 - t) * Bx ) + (t * Cx);
	    var Ey = ((1 - t) * By ) + (t * Cy);
	    var Px = ((1 - t) * Dx ) + (t * Ex);
	    var Py = ((1 - t) * Dy ) + (t * Ey);
	    d.arrow.y = Py;
	    d.arrow.x = Px;
	    if (Py < y || Math.abs(Px - x) > tmpWidth){
	    	return true;
	    }    
	    return false;
	}

	//compute the rotate slope of arrow
	function getArrowAngle(d, ax, by, t){
		var a = d.source;
		var b = d.target;
		var Ax = ((1 - t) * b.x) + (t * b.x);
		var Ay = ((1 - t) * b.y) + (t * by);
		var Bx = ((1 - t) * b.x) + (t * ax);
		var By = ((1 - t) * by) + (t * a.y);
		var Cx = ((1 - t) * ax) + (t * a.x);
		var Cy = ((1 - t) * a.y) + (t * a.y);
		var Dx = ((1 - t) * Ax ) + (t * Bx);
	    var Dy = ((1 - t) * Ay ) + (t * By);
	    var Ex = ((1 - t) * Bx ) + (t * Cx);
	    var Ey = ((1 - t) * By ) + (t * Cy);
	    var Px = ((1 - t) * Dx ) + (t * Ex);
	    var Py = ((1 - t) * Dy ) + (t * Ey);
	    d.slope = (d.arrow.y - Py != 0) ? (d.arrow.x - Px) / (d.arrow.y - Py) : 1;
		d.angle = -Math.atan(d.slope) / Math.PI * 180;
	}

	//updata links of labels for tick function
	var updateLabelLink = function(){
		this.attr("x1", function(d) { return d.source.x; })
	        .attr("y1", function(d) { return d.source.y; })
	        .attr("x2", function(d) { return d.target.x; })
	        .attr("y2", function(d) { return d.target.y; });
	}

	//update label of nodes for tick function
	var updateLabel = function() {
		layerLabel = [];
		textData.forEach(function(d, i){
			if (i % 2 == 1){
				if (d.type == "nodeLabel"){
					if (d.node.outside.isOutside && d.node.showLabel){
						if (!layerLabel[0]){
							layerLabel[0] = [];
						}
						layerLabel[0].push(d);
					}
					if (!d.node.outside.isOutside && d.node.layer > 0){
						if (!layerLabel[d.node.layer]){
							layerLabel[d.node.layer] = [];
						}
						layerLabel[d.node.layer].push(d);
					}
				} else {
					if (!nodesData[d.node.src].outside.isOutside && !nodesData[d.node.tgt].outside.isOutside){
						var l = (nodesData[d.node.src].layer + nodesData[d.node.tgt].layer) / 2;
						if (!layerLabel[l]){
							layerLabel[l] = [];
						}
						layerLabel[l].push(d);
					}
				}
			}
		});

		layerLabel.forEach(function(e, i){
			if (e.length > 1 && i > 0){
				var q = d3.geom.quadtree(e),
					i = 0,
	      			n = e.length;
	    		while (++i < n) q.visit(collide(e[i]));
	    	}
	    	if (i == 0 && e.length > 1){
	    		var q = d3.geom.quadtree(e),
					i = 0,
	      			n = e.length;
	    		while (++i < n) q.visit(collideOutside(e[i]));
	    	}
		});
	      	
		this.attr("transform", function(d) {
			//dx = Math.max(xOffset + 20, Math.min(xOffset + width, d.x)); 
			//d.y = Math.max(nodeRadius, Math.min(height - nodeRadius, d.y)); 
			return "translate(" + d.x + "," + d.y + ")";
		});
	}

	//collision detection
	function collide(d) {
	    var r = d.width / 2,
	      	nx1 = d.x - r,
	      	nx2 = d.x + r;
	  	return function(quad, x1, y1, x2, y2) {
	    	if (quad.point && (quad.point !== d)) {
	      		var x = d.x - quad.point.x,
	          		y = 0,
	          		l = Math.sqrt(x * x),
	          		r = d.width / 2 + quad.point.width / 2;
	      		if (l < r) {
	        		l = (l - r) / l * .5;
	        		d.x -= x *= l;
	        		quad.point.x += x;
	      		}
	    	}
	    	return x1 > nx2 || x2 < nx1;
	  	};
	}

	//collision detection for outside nodes
	function collideOutside(d){
	  	var r = d.width / 2,
	      	nx1 = d.x - r,
	      	nx2 = d.x + r,
	      	ny1 = d.y - d.height,
	      	ny2 = d.y;
	  	return function(quad, x1, y1, x2, y2) {
	    	if (quad.point && (quad.point !== d)) {
	      		var x = d.x - quad.point.x,
	          		y = (d.y - quad.point.y),
	          		lx = Math.abs(x),
	          		ly = Math.abs(y),
	          		rx = d.width / 2 + quad.point.width / 2,
	          		ry = d.height;

	          	if (ly >= ry || lx >= rx){
	          		return true;
	          	}      		
	        	ly = (ly - ry) / ly * .5;
	        	d.y -= y *= ly;
	        	quad.point.y += y;
	      		
	    	}
	    	return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
	  	};
	}

	//initialize data
	function initializeData(tmpL, tmpN){
		tmpN.forEach(function(d, i){
			var node = {};
			node.label = d.id;
			node.id = i;
			node.degree = 0;
			node.showLabel = false;
			node.original = d;
			if (d.column || d.column == 0){
				node.column = d.column;
				node.type = "anchor";
				node.layer = 0;
				node.xpos = d.xPos;
				anchorData.push(node);
				layerMap[i] = 1;
			} else {
				node.type = "node";
			}
			nodesData.push(node);
			idMap[d.id] = i;

		
			textData.push({node : node});
			textData.push({
				node : node,
				content : d.label,
				type : "nodeLabel"
			});

			textLinksData.push({
				source : textData.length - 2,
				target : textData.length - 1
			});
			
		});

		tmpL.forEach(function(d, i){
			var edge = {};
			edge.source = idMap[d.source];
			edge.target = idMap[d.target];
			edge.id = i;
			if (d.id){
				edgeIdMap[d.id] = i;
			}
			linksData.push(edge);

			var node = {};
			node.src = edge.source;
			node.tgt = edge.target;
			node.original = d;
			textData.push({
				node : node,
				type : "linkCircle"
			});
			textData.push({
				node : node,
				type : "linkLabel",
				content : d.label
			});

			textLinksData.push({
				source : textData.length - 2,
				target : textData.length - 1
			});
		});

		//nodesChildren contains the node's id - node's children pair.
		anchorData.forEach(function(d){
			nodesChildren[d.id] = [];
		});
		linksData.forEach(function(d){
			if (!nodesChildren[d.source]){
				nodesChildren[d.source] = [];
			}
			nodesChildren[d.source].push(d.target);
			nodesData[d.target].parent = d.source;
			nodesData[d.source].degree++;
			nodesData[d.target].degree++;
		});		
	}

	//detect the strong connect component in the graph
	function detectCycle(){
		SCCNodes = d3.range(nodesData.length).map(function(d, i){
			return {id : nodesData[i].id, index : -1};
		})
		map.clear();
		for (var i = 0; i < SCCNodes.length; i++){
			if (SCCNodes[i].index == -1 && SCCNodes[i].id in nodesChildren){
				strongConnect(SCCNodes[i]);
			}
		}
	}

	function strongConnect(d){	
		d.index = SCCindex;
		d.lowlink = SCCindex++;
		SCCtmpNodes.push(d.id);
		map.set(d.id, 1);
		nodesChildren[d.id].forEach(function(e){
			if (nodesChildren[SCCNodes[e].id] != undefined){
				if (SCCNodes[e].index == -1){
					strongConnect(SCCNodes[e]);
					d.lowlink = Math.min(d.lowlink, SCCNodes[e].lowlink);
				} else if (map.has(SCCNodes[e].id)){
					d.lowlink = Math.min(d.lowlink, SCCNodes[e].index);
					var cycle = SCCtmpNodes.slice(SCCtmpNodes.indexOf(SCCNodes[e].id));
					cycles.push(cycle);
				}
			}
		});
		//if (d.lowlink == d.index){
			//var cycle = [];
			var last;
			do {
				last = SCCtmpNodes[SCCtmpNodes.length - 1];
				SCCtmpNodes.pop();
				map.delete(last);
				//cycle.push(last);			
			} while (last != d.id);
			//if (cycle.length > 1){
				//cycles.push(cycle);
			//}
		//}
	}

	//generate cycles set
	function removeCycle(){
		detectCycle();
		map.clear();
		cycles.forEach(function(d){
			var tmp = [];
			for (var i = 0; i < d.length; i++){
				var e = "";
				if (i == d.length - 1){
					e = d[i] + " " + d[0];
				} else {
					e = d[i] + " " + d[i + 1];
				}
				map.set(e, tmp);
				tmp.push(e);
			}
		});
		/*map.forEach(function(v, k, m){
			console.log(v + "------" + k);
		})*/
	}

	//set layer and position for each node
	function setLayer(tmpLinkData, tmpE){
		//layer is set from bottem to top, one layer per loop. The anchors are layer 0.
		var change = anchorData.length;
		//for (var i = 0; i < 10; i++){
		while (change > 0){
			var tmpLayerMap = [];
			change = 0;
			tmpLinkData.forEach(function(d){
			//for (var i = 0; i < tmpLinkData.length; i++){
				//d = tmpLinkData[i];
				var src = d.source;
				var tgt = d.target;
				var flag = true;
				if (tgt in layerMap){
					var e = src + " " + tgt;
					if (map.has(e)){
						var tmp = map.get(e);
						if (tmp.length > 1){
							tmp.splice(tmp.indexOf(e), 1);
							//console.log(tmp);
						} else {
							if (tmp[0] == e){
								var index = nodesChildren[src].indexOf(tgt);
								if (index > -1){
									nodesChildren[src].splice(index, 1);
								}
								flag = false;
								//console.log("left" + e);
							}
						}
					}
					if (flag){
						nodesData[src].layer = nodesData[tgt].layer + 1;
						tmpLayerMap[src] = 1;
						change++;
					}
				}
			});
			layerMap = tmpLayerMap.slice(0);
			maxLayer++;
		}
		maxLayer--;	


		//store the node id in the sequence of layer
		//xPos is the x position for nodes in the unit of column's width
		layerMap = d3.range(maxLayer + 1).map(function(d){
			return [];
		});
		nodesData.forEach(function(d){
			if (d.layer != undefined){
				layerMap[d.layer].push(d.id);
			}		
		});
		layerMap.forEach(function(d, i){
			if (i > 0){
				d.forEach(function(e){
					var tmp = [];
					nodesChildren[e].forEach(function(f){
						tmp.push(nodesData[f].xpos);
					});				
					nodesData[e].xpos = (d3.min(tmp) + d3.max(tmp)) / 2;
				}); 
			}
		});


		//for node that has no layer, set it as outside node	
		nodesData.forEach(function(d){
			if (d.layer == undefined){
				d.noLayer = true;
				d.layer = -1;
				d.xpos = -1;
			}
		});


		//set the edge link
		tmpE.forEach(function(d, i){
			var srcIndex = nodesData.length * 2 + edgeIdMap[d.source] * 2 + 1;
			textData[srcIndex].layer = (nodesData[textData[srcIndex].node.src].layer + nodesData[textData[srcIndex].node.tgt].layer) / 2;

			var edge = {};
			edge.source = textData[srcIndex];
			edge.target = idMap[d.target];
			edge.id = linksData.length;
			edge.type = "edgeLink";
			linksData.push(edge);
		});
	}

	//when move over show the coordinate
	function mousemove(){
		var ary = d3.mouse(this);
		pos.attr("x", ary[0] + 2)
			.attr("y", ary[1] + 2)
			//.attr("x", 100)
			//.attr("y", 100)
			.text(Math.round(ary[0]) + ", " + Math.round(ary[1]));
	}

	//move element to the back of its parent's children
	d3.selection.prototype.moveToBack = function() { 
	    return this.each(function() { 
	        var firstChild = this.parentNode.firstChild; 
	        if (firstChild) { 
	            this.parentNode.insertBefore(this, firstChild); 
	        } 
	    });    //move component to the down of svg
	};

	//move element to the top of its parent's children
	d3.selection.prototype.moveToFront = function() {
	  return this.each(function(){
	    this.parentNode.appendChild(this);
	  });   //move component to the up of svg
	};

	window.onscroll = function(event){
		//console.log(window.pageXOffset);
		if (Math.abs(window.pageXOffset - xOffset) > reshuffleFrequency){
			xOffset = window.pageXOffset;
			setNodePosition();
		}
	}

	window.onresize = function(event) {
	    width = window.innerWidth - padding;
	    //height=window.innerHeight - padding;
	    //console.log(width + " " + height);
	};

	//set the outside nodes
	function setNodePosition(){
		var num = 0;
		var change = 0;
		//check if node is outside or not, when the status of node changes, the transition happens
		nodes.each(function(d){
			if (d.noLayer){
				d.outside.isOutside = true;
				d3.select(this)
					.transition()
					.duration(500)
					.attr("opacity", 0.8)
					.attr("r", nodeRadius)
					.attr("fill", "black");	
				return;				
			}
			if (d.position.x - nodeRadius < xOffset || d.position.x + nodeRadius > xOffset + window.innerWidth){
				d3.select(this).classed("fixed", d.fixed = false);
	  			d.position.x = d.xpos;
				d.position.y = height - nodeRadius - d.layer * unitLinkLength;
			}
			if (d.position.x - nodeRadius < xOffset || d.position.x + nodeRadius > xOffset + window.innerWidth){
				if (!d.outside.isOutside){
					d.outside.isOutside = true;				
					d3.select(this)
						.transition()
						.duration(500)
						.attr("opacity", 0.5)
						.attr("r", nodeRadius)
						.attr("fill", function(d){
							return cScale(d.index + 1);
						});					
					change++;
					num++;
				}
			} else {
				if (d.outside.isOutside){
					d.outside.isOutside = false;
					d3.select(this)
						.transition()
						.duration(500)
						.attr("opacity", 0.7)
						.attr("r", nodeRadius)
						.attr("fill", "red");

					change++;
					num--;
				}
			}
		});

		//when some node changes its status, the correspoding links, labels and the x position of inside nodes should also change.
		if (change > 0 || firstTime){
			firstTime = false;
			outsideNodesNum = num;	
			d3.selectAll(".nodeLabel")
				.attr("opacity", function(d){
					if (d.type == "nodeLabel" && d.node.outside){
						if ((!d.node.outside.isOutside && d.node.type != "anchor") || (d.node.outside.isOutside && d.node.degree >= 3)){
							d.node.showLabel = true;
							return 1;
						}
					}	
					d.node.showLabel = false;
					return 0;			
				});
			d3.selectAll(".linkLabel")
				.attr("opacity", function(d){
					if (d.type == "linkLabel"){
						if (nodesData[d.node.src].outside.isOutside || nodesData[d.node.tgt].outside.isOutside){
							return 0;
						}
						return 1;
					}
				})
			d3.selectAll(".clickBoard")
				.attr("fill", function(d){
					if (d.type == "linkLabel"){
						if (nodesData[d.node.src].outside.isOutside || nodesData[d.node.tgt].outside.isOutside){
							return "none";
						}
						return "transparent";
					} 
					return d.node.showLabel ? "transparent" : "none";
				});

			links.classed("outsideLink", function(d){
				if (d.type == "edgeLink"){
					return d.target.outside.isOutside;
				}
				if (d.source.outside && d.target.outside){
					return d.source.outside.isOutside || d.target.outside.isOutside;
				}
				return false;
			});

			layerMap.forEach(function(d, i){
				if (i > 0){
					d.forEach(function(e){
						if (!nodesData[e].outside.isOutside && !nodesData[e].fixed){
							var tmp = [];
							nodesChildren[e].forEach(function(f){
								if (!nodesData[f].outside.isOutside){
									tmp.push(nodesData[f].xpos);
								}
							});				
							nodesData[e].xpos = (d3.min(tmp) + d3.max(tmp)) / 2;
							nodesData[e].position.x = nodesData[e].xpos;
						}
					});
				}
			});	
			force.start();
		}
	}

	var generateLayout = function(json) {  
		//read file and execute program
		//d3.json(jsonFile, function(d){
		var processData = function(d) {
			var tmpNodeData = d.anchors.concat(d.nodes);
			var tmpLinkData = d.links;
			var tmpEdgeLink = d.edgeLinks;
			initializeData(tmpLinkData, tmpNodeData);
			removeCycle();
			var tmpL = linksData.slice(0);
			setLayer(tmpL, tmpEdgeLink);


			width = d.width + padding;
			height = (maxLayer + 1) * (unitLinkLength + outsideUnitLinkLength);
			svg.attr("width", width);
			svg.attr("height", height);
			force.size([width, height]);
			labelForce.size([width, height]);

			transit();

			force.nodes(nodesData)
				.links(linksData)
				.start();

			labelForce.nodes(textData)
				.links(textLinksData)
				.start();

			setNodePosition();
		};
		processData(json);
	}




	/**
	===========================================================================
					PUBLIC METHODS
	===========================================================================
	**/

	this.generateLayoutForJson = function(inputJson) {
		generateLayout(inputJson);
	}

	this.generateLayoutForJsonFile = function(jsonFile) {
		d3.json(jsonFile, function(d){
			generateLayout(d);
		});
	};

	this.setNodeClickListener = function(listener) {
		nodeClickListener = listener;
	}

	this.setLinkClickListener = function(listener) {
		linkClickListener = listener
	}
};
