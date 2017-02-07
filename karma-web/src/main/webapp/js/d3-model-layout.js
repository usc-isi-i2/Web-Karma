D3ModelLayout = function(p_htmlElement, p_cssClass, p_width, p_worksheetId) {
	var htmlElement = p_htmlElement;
	var cssClass = p_cssClass;
	var htmlWidth = p_width;
	var worksheetId = p_worksheetId;

	//var padding = 35;
	var rightPanelWidth = parseInt($("." + cssClass).css("width"));
	var leftPanelWidth = window.innerWidth - rightPanelWidth;
	console.log("rightPanelWidth: " + rightPanelWidth + "  leftPanelWidth: " + leftPanelWidth);
	var windowWidth = rightPanelWidth;
	var maxXOfferset = 0;
	var width=0;           
	var height=0;
	var textHeight = 15;

	var linkClickListener = null;
	var nodeClickListener = null;
	var anchorClickListener = null;
	var anchorMouseListener = null;
	var linkApproveClickListener = null;
	var nodeDragDropListener = null;

	var test = [];
	//var tableData = [];                            //store table data
	//var columnPos = [];                            //position for each column
	var anchorName = [];                           //store anchor name, include nested
	var anchorData = [];                           //store anchor nodes
	var nodesData = [];                            //store all nodes includes anchors
	var linksData = [];                            //links data
	var noCycleLinksData = [];                     //cycles are removed
	var cycles = [];                               //all cycles, each cycle contians all nodes in that cycle.
	var textData = [];                             //text nodes
	var textLinksData = [];                        //text links
	var layerMap = [];                             //store nodes'id in sequence of layers
	var nodesChildren = [];                        //store node's id and its children pair
	var SCCindex = 0;                              //strong connected component node's index
	var SCCNodes = [];                             //SCC nodes set
	var SCCtmpNodes = [];                          //the nodes stack of SCC
	var layerLabel = [];                           //layers are divided into sections based on its layer
	var cycleSet = [];                             //each element in the array is an arraylist, consist of edges of cycle. ex: {1->2, 2->3, 3->1}
	var myMap = function(){
		var data = [];
		this.entry = data;
		this.set = function(key, value){
			data[key] = value;
		}
		this.get = function(key){
			return data[key];
		}
		this.has = function(key){
			return (key in data);
		}
		this.delete = function(key){
			data.splice(data.indexOf(key), 1);
		}
		this.clear = function(){
			data = [];
		}
	}
	var ArrayList = function(){
		var data = [];
		this.contains = function(arg){			
			for (var i = 0; i < data.length; i++){
				if (data[i] == arg){
					return true;
				}
			};
			return false;
		}
		this.add = function(arg){
			data.push(arg);
		}
		this.removeByValue = function(arg){
			var index = data.indexOf(arg);
			if (index > -1){
				data.splice(index, 1);
				return true;
			}
			return false;
		}
		this.removeByIndex = function(arg){
			if (arg >= 0 && arg < data.length){
				data.splice(arg, 1);
				return true;
			}
			return false;
		}
		this.get = function(arg){
			if (arg >= 0 && arg < data.length){
				return data[arg];
			}
			return undefined;
		}
		this.print = function(){
			var output = "[";
			data.forEach(function(d, i){
				output += d;
				if (i < data.length - 1){
					output += ',';
				}
			});
			output += "]";
			console.log("ArrayList: " + output);
		}
	}
	var map = new myMap();
	var idMap = [];                                //map from label to id
	var edgeIdMap = [];                            //map of edge's id
	var nodePosMap = new myMap();                  //map of node's position


	var print = false;                             //whether or not print 
	var nodeRadius = 6;
	var unitLinkLength = 70;                       //difference between layers
	var outsideUnitLinkLength = 50;                //length for outside links
	var maxLayer = 0;                              //max layer number, base 0
	var reshuffleFrequency = 8;                    //pixel changes to excute scroll bar event
	var xOffset = 0;                               //x position offset
	var firstTime = true;                          //first time to load the force-layout
	var maxLabelLength = 0;    
	var tableHeight = 0;
	var cellWidth = $(".wk-header-cell").css("width");
	var cScale = d3.scale.category20();
	var transitionDisapearTime = 100;						//time for d3 components transition of deleting

	//create svg
	var svg = d3.select(htmlElement)                         
	    .append("svg")
	    .attr("id", p_htmlElement)
	    //.on("mousemove", mousemove);


	//svg to draw nodes and links
	var forceSVG = svg.append("g");

	//place to show mouse coordinate
	var pos = svg.append("text")
		.attr("fill", "black")
		.attr("font-size", 10);

	//coefficient of force move nodes to top
	var upperForceScale = d3.scale.linear()
		//.domain([0, height]) 
		.range([1, 0]);        

	var nodes = forceSVG.selectAll(".node");       //all nodes    
	var links = forceSVG.selectAll(".link");       //all links
	var labels = forceSVG.selectAll(".label");     //all labels
	var labelLinks = forceSVG.selectAll(".labelLinks"); //all label links.
	var linkArrow = forceSVG.selectAll(".linkArrow");   //little triangle of links
	var labelFrame = forceSVG.selectAll(".labelFrame"); //the frame of each label

	//force layout for nodes
	var force = d3.layout.force()
		.gravity(0)
		.linkStrength(function(d){
			if (d.type == "edgeLink"){
				if (d.target.outside.isOutside){
					return 1;
				}
				return 0;
			}
			if(d.source.isTemporary || d.target.isTemporary)
				return 1;
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
		.charge(function(d) {
			if(d.isTemporary)
				return -8000;
			return -100;
		})
		.linkDistance(function(d){
			if (d.target.noLayer){
				return outsideUnitLinkLength * 4;
			}
			return outsideUnitLinkLength;
		})
		.on("tick", tick);

	// force layout for labels
	var labelForce = d3.layout.force()
		//.size([Math.max(width, columns * barWidth), height])
		.gravity(0)
		.friction(0.8)
		.charge(function(d){
			return 0;
		})
		.linkDistance(0)
		.linkStrength(0.8);
		//node can be dragged to the position you want

	var drag = force.drag()
		.on("dragstart", function(d) {
			if(d.type == "anchor") {
				d3.select(this).classed("fixed", d.fixed = false);
			} else if(!d.outside.isOutside || d.noLayer){
	  			d3.select(this).classed("fixed", d.fixed = true);
			}
// 			console.log(JSON.stringify(d));
			if(d.isTemporary && d.original.nodeDomain == "BlankNode") {
				d3.select(this).moveToBack();
				nodes.each(function(d){
					d.beforeDragFixed = d.fixed;
					d3.select(this).classed("fixed", d.fixed = true);
				});
				labelFrame.each(function(d) {
					d.beforeDragFixed = d.fixed;
					d3.select(this).classed("fixed", d.fixed = true);

				});
				// force.charge(function(d) {
				// 	if(d.original.nodeDomain == "BlankNode")
				// 		return 5000;
				// 	if(d.isTemporary)
				// 		return -5000;
				// 	return -100;
				// });
				// labelForce.charge(-1);
				// force.start();
				// labelForce.start();
			}
	  	})
	  	.on("dragend", function(d) {
	    	if(d.isTemporary) {
	    		console.log("DragEnd of TemporaryLink")
	    		originalNode = d.original;	
	    		var targetEvent = d3.event.sourceEvent;
	    		d3.select(targetEvent.target).call(function(d) {
	    			if(d.length > 0) {
	    				if(d[0].length > 0 && d[0][0].__data__) {
	    					data = d[0][0].__data__;
	    					if(data.node) {
	    						if(nodeDragDropListener != null) {
	    							nodeDragDropListener(originalNode, data.node.original, targetEvent);
	    						}
	    					}
	    				} 
	    			}
	    		});

	    		if(d.original.nodeDomain == "BlankNode") {
		    		nodes.each(function(d){
						d3.select(this).classed("fixed", d.fixed = d.beforeDragFixed);
					});
					labelFrame.each(function(d) {
						d3.select(this).classed("fixed", d.fixed = d.beforeDragFixed);
					});
				}
	    // 			force.charge(chargeFunc).linkStrength(linkStrengthFunc);
					// labelForce.charge(labelChargeFunc).linkStrength(labelLinkStrengthFunc);
					// force.start();
					// labelForce.start();
	    // 		}
	    	}

	  		if (d.type != "anchor" && (!d.outside.isOutside || d.noLayer)){
	  			d.position.x = d.x;
	  			d.position.y = d.y;
	  		}
		});
	//draw nodes and links




	function transit(){
		links = links.data(linksData, function(d){
			return d.edgeId;
		});
		links.enter()
			.append("path")
			.attr("stroke", "#555")
			.attr("stroke-width", 1)
			//.attr("opacity", 0.5)
			.attr("id", function(d, i){
				return d.source.id + "link" + d.target.id;
			})
			.attr("class", function(d) {
				var provClass = ""
				if(d.isProvenance)
					provClass = " provenanceLink"
				return "link " + d.linkType + " " + d.linkStatus + provClass;
			})
			.attr("fill", "none");
		links.exit()
			.transition()
			.duration(transitionDisapearTime)
			.attr("opacity", 0)
			.remove();


		linkArrow = linkArrow.data(linksData, function(d){
			return d.edgeId;
		});
		linkArrow.enter()
			.append("polygon")
			.attr("fill", "#555")
		linkArrow.exit()
			.transition()
			.duration(transitionDisapearTime)
			.attr("opacity", 0)
			.remove();
			
		labels = labels.data(textData, function(d){
			return d.nodeId;
		});
		labels.enter()
			.append("g")
			.classed("label", true)
			.attr("id", function(d, i){
				if (d.type == "nodeLabel"){
					return "nodeLabelG" + d.node.id;
				} else if (d.type == "linkLabel"){
					return "linkLabelG" + d.node.src + "-" + d.node.tgt;
				} else if (d.type == "edgeLinkLabel"){
					return "edgeLinkLabelG" + nodesData[d.node.tgt].id;
				}
				return "labelPartG" + i;
			});
		labels.exit()
			.transition()
			.duration(transitionDisapearTime)
			.attr("opacity", 0)
			.remove();

		
		var test = labels.append("circle")
			.attr("r", 0)

		labelFrame = labels
			.append("g")
			.attr("class", function(d){
				if (d.type == "nodeLabel"){
					return "nodeLabel";
				} else if (d.type == "linkLabel"){
					return "linkLabel";
				} else if (d.type == "edgeLinkLabel"){
					return "edgeLinkLabel";
				}
				return "unusedLable";
			})
			.attr("id", function(d, i){
				if (d.type == "nodeLabel"){
					return "nodeLabel" + d.node.id;
				} else if (d.type == "linkLabel"){
					return "linkLabel" + d.node.src + "-" + d.node.tgt;
				} else if (d.type == "edgeLinkLabel"){
					return "edgeLinkLabel" + nodesData[d.node.tgt].id;
				}
				return "labelPart" + i;
			})
			.attr("opacity", 0);
			
		labelText = labelFrame
			.filter(function(d, i){
				return i % 2 == 1;
			})
			.append("text")
			.text(function(d, i){
				if (d.type == "nodeLabel" && d.node.type == "anchor"){
					return d.content;
				}
				if (d.content.length > 20){
					d.alt = d.content.slice(0, 7) + "..." + d.content.slice(d.content.length - 10, d.content.length);
					return d.alt;
				}
				return d.content;
			})
			.attr("fill", function(d){
				if (d.type == "nodeLabel"){
					return "white";
				}
				if (d.type == "linkLabel" || d.type == "edgeLinkLabel"){
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
			.attr("font-family", "Arial")
			.attr("font-size", 12)
			.attr("opacity", 0.8)
			.attr("x", function(d){
				d.width = this.getBBox().width //+ textHeight / 3 * 2;
				if (d.type == "nodeLabel"){
					d.node.labelWidth = d.width;
				}
				return -(d.width / 2);
			})
			.attr("y", -3);

		labelBoard = labelFrame
			.filter(function(d, i){
				return i % 2 == 1;
			})
			.append("path")	
			.attr("stroke-width", 0)
			.attr("stroke", function(d){
				return "red";
				if (d.type == "nodeLabel"){
					return "#ddd";
				} else if (d.type == "linkLabel"){
					return "#555";
				}
			})		
			.attr("id", function(d){
				if (d.type == "nodeLabel"){
					return "nodeLabelBoard" + d.node.id;
				} else if (d.type == "linkLabel"){
					return "linkLabelBoard" + d.node.src + "-" + d.node.tgt;
				} else if (d.type == "edgeLinkLabel"){
					return "edgeLinkLabelBoard" + nodesData[d.node.tgt].id;
				}
			})	
			.attr("fill", function(d){
				if (d.type == "nodeLabel"){
					if (d.node.isForcedByUser) return "rgb(42,98,126)";
					if (d.node.isTemporary) return "#888";
					return "#555";
				}
				if (d.type == "linkLabel" || d.type == "edgeLinkLabel"){
					return "#ddd";
				}
				return "none";
			})
			.attr("d", function(d){
				if (d.type != "nodeLabel" && d.type != "linkLabel" && d.type != "edgeLinkLabel"){
					d.width = 0;
					return "";
				}
				var textWidth = d.width;
				var dx = -textWidth / 2;
				var dy = 0;							
				return "M " + dx + " " + dy + " L " + (dx + textWidth) + " " + dy + " Q " + (dx + textWidth + textHeight / 3) + " " + (dy - textHeight / 2) + " " + (dx + textWidth) + " " + (dy - textHeight) + " L " + dx + " " + (dy - textHeight) + " Q " + (dx - textHeight / 3) + " " + (dy - textHeight / 2) + " " + dx + " " + dy;
			});
			
		labelBoard.moveToBack();

		labelApproveBoard = labels
			.filter(function(d, i){
				//console.log(JSON.stringify(d));
				return d.type == "linkLabel" && d.node.original.linkStatus == "TemporaryLink";
			})
			.append("rect")
			.attr("fill", "transparent")
			.classed("clickBoard", true)
			.attr("r", nodeRadius+1)
			.attr("opacity", 1)
			.attr("stroke-width", 2)
			.attr("stroke", "red")
			.attr("width", function(d){
				return 8;
			})
			.attr("height", function(d){
				return 8;
			})
			.attr("x", function(d){
				var w = Math.ceil(this.parentNode.childNodes[1].getBBox().width);
				return -4;
			})
			.attr("y", function(d){
				var h = Math.ceil(this.parentNode.childNodes[1].getBBox().height);
				return -h+15;
			})
			.on("click", function(d){
				if(linkApproveClickListener != null)
						linkApproveClickListener(d.node.original, d3.event);
			});

		labelClickBoard = labels
			.filter(function(d, i){
				return (i % 2 == 1) && d.node.type != "anchor";
			})
			.append("rect")
			.classed("clickBoard", true)
			.attr("id", function(d){
				if (d.type == "nodeLabel"){
					return "nodeLabelClickBoard" + d.node.id;
				} else if (d.type == "linkLabel"){
					return "linkLabelClickBoard" + d.node.src + " " + d.node.tgt;
				} else if (d.type == "edgeLinkLabel"){
					return "edgeLinkClickBoard" + nodesData[d.node.tgt].id;
				}
			})
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
				if (d.type == "linkLabel" || d.type == "edgeLinkLabel"){
					if(linkClickListener != null)
						linkClickListener(d.node.original, d3.event);
				} else {
					if(nodeClickListener != null)
						nodeClickListener(d.node.original, d3.event);
				}
				//console.log(d.type);
			})
			.on("mouseover", function(d){				
				var frameId = "";
				showNodeHelp(worksheetId, d.node.original);

				//console.log(d.content);
				if (d.type == "nodeLabel"){
					frameId = "#nodeLabelG" + d.node.id;
				} else if (d.type == "linkLabel"){
					frameId = "#linkLabelG" + d.node.src + "-" + d.node.tgt;
				} else if (d.type == "edgeLinkLabel"){
					frameId = "#edgeLinkLabelG" + nodesData[d.node.tgt].id;
				}
				if (d.alt != undefined){
					d3.select(frameId)
						.select("text")
						.text(function(d){
							return d.content;
						})
						.attr("x", function(d){
							d.width = this.getBBox().width
							return -(d.width / 2);
						})
					d3.select(frameId)
						.select("path")	
						.transition()
						.duration(500)					
						.attr("d", function(d){							
							var textWidth = d.width;
							var dx = -textWidth / 2;
							var dy = 0;
							return "M " + dx + " " + dy + " L " + (dx + textWidth) + " " + dy + " Q " + (dx + textWidth + textHeight / 3) + " " + (dy - textHeight / 2) + " " + (dx + textWidth) + " " + (dy - textHeight) + " L " + dx + " " + (dy - textHeight) + " Q " + (dx - textHeight / 3) + " " + (dy - textHeight / 2) + " " + dx + " " + dy;
						})
				}
				d3.select(frameId)
					.select("path")
					.attr("stroke-width", 2);
				d3.select(frameId)
					.moveToFront();
			})
			.on("mouseout", function(d, i){
				hideHelp();

				var frameId = "";
				if (d.type == "nodeLabel"){
					frameId = "#nodeLabelG" + d.node.id;
				} else if (d.type == "linkLabel"){
					frameId = "#linkLabelG" + d.node.src + "-" + d.node.tgt;
				} else if (d.type == "edgeLinkLabel"){
					frameId = "#edgeLinkLabelG" + nodesData[d.node.tgt].id;
				}
				if (d.alt != undefined){
					d3.select(frameId)
						.select("text")
						.text(function(d){
							return d.alt;
						})
						.attr("x", function(d){
							d.width = this.getBBox().width
							return -(d.width / 2);
						})
					d3.select(frameId)
						.select("path")	
						.transition()
						.duration(200)					
						.attr("d", function(d){							
							var textWidth = d.width;
							var dx = -textWidth / 2;
							var dy = 0;
							return "M " + dx + " " + dy + " L " + (dx + textWidth) + " " + dy + " Q " + (dx + textWidth + textHeight / 3) + " " + (dy - textHeight / 2) + " " + (dx + textWidth) + " " + (dy - textHeight) + " L " + dx + " " + (dy - textHeight) + " Q " + (dx - textHeight / 3) + " " + (dy - textHeight / 2) + " " + dx + " " + dy;
						})
				}
				d3.select(frameId)
					.select("path")
					.attr("stroke-width", 0);
				d3.select(frameId)
					.moveToBack();

			});


			
		labelLinks = labelLinks.data(textLinksData, function(d){
			return d.edgeId;
		});
		labelLinks.enter()
			.append("line")
			.classed("labelLinks", true)
			.attr("stroke-width", 0);
		labelLinks.exit()
			.transition()
			.duration(transitionDisapearTime)
			.attr("opacity", 0)
			.remove();


		
			

		nodesData.forEach(function(d){				
			if (d.noLayer){					
				d.position.x = -1;
				d.position.y = -1;
			} else {
				d.position.x = d.xpos;
				d.position.y = height - nodeRadius - d.layer * unitLinkLength;
			}	
		})	

		nodes = nodes.data(nodesData, function(d){
			return d.nodeId;
		});

		nodes.enter()
			.append("circle")
			.classed("node", true)
			.attr("r", nodeRadius)
			.attr("opacity", 0.7)
			.attr("fill", "red")
			.attr("id", function(d, i){
				return "node" + d.id;
			})
			.call(drag)
			.on("click", function(d){
				if(anchorClickListener != null && d.original.nodeType == "ColumnNode")
					anchorClickListener(d.original, d3.event);
				var offset = Math.max(xOffset - leftPanelWidth,0);
				if (d.outside.isOutside && !d.noLayer){					
					if (d.position.x < offset){		
						var destination = Math.max(0, d.position.x - windowWidth / 2);
						var xPosition = offset;
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
						var destination = Math.min(width - windowWidth, d.position.x - windowWidth / 2);
						var xPosition = offset;
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
				if(anchorMouseListener != null && !d.isTemporary && !d.outside.isOutside)
					anchorMouseListener(d.original, d3.event);
				d3.select(this)
					.transition()
					.duration(500)
					.attr("opacity", 1)
					.attr("r", nodeRadius * 1.5);

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
				d3.select(this)
					.transition()
					.duration(500)
					.attr("opacity", 0.7)
					.attr("r", nodeRadius)

				if (d.noLayer){

				} else if (d.outside.isOutside){
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
			.duration(transitionDisapearTime)
			.attr("opacity", 500)
			.remove();
	}

	//tick function for force-layout
	function tick(e){
		labelForce.start();
		var k = 0.03 * e.alpha;
		var kY = 0.05;


		nodes
			.attr("cx", function(d) {
				if (d.outside.isOutside){		
					var tmpXOffset = Math.max(Math.min(xOffset, maxXOfferset) - leftPanelWidth, 0);		
					return d.x = Math.max(tmpXOffset + nodeRadius + d.labelWidth / 2, Math.min(tmpXOffset + windowWidth - nodeRadius - d.labelWidth / 2, d.x));
				} 
				if (d.isTemporary){		
					var tmpXOffset = Math.max(Math.min(xOffset, maxXOfferset) - leftPanelWidth, 0);		
					return d.x = Math.max(tmpXOffset + nodeRadius + d.labelWidth / 2, Math.min(tmpXOffset + windowWidth - nodeRadius - d.labelWidth / 2, d.x));
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
	        		return d.y = Math.max(nodeRadius + 12, Math.min(height - nodeRadius, d.y));
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
	    		if (d.type == "linkCircle"){
		    		var a = nodesData[d.node.src];
		    		var b = nodesData[d.node.tgt];
		    		d.y = (a.y + b.y) / 2;
		    		d.x = (a.x + b.x) / 2 + (nodesData[d.node.tgt].x - nodesData[d.node.src].x) / 6;
		    		/*var i, j;			
					for (i = 0; i < 1; i += 0.1){
						if (getXofLabel(d, i, d.y)){
							break;
						}
					}
					i -= 0.1;
					for (j = 0; j < 0.1; j += 0.01){
						if (getXofLabel(d, i + j, d.y)){
							break;
						}
					}   	*/

		    			    		
	    		} else if (d.type == "edgeLinkCircle"){
	    			var a = d.node.src;
	    			var b = nodesData[d.node.tgt];
	    			d.x = (a.x + b.x) / 2 + (b.x - a.x) / 6;
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

	    //if (!firstTime){
	    	links.call(updateLink);
	    	labels.call(updateLabel);
	    	labelLinks.call(updateLabelLink);  	
		//}
	}

	//updata link for tick function
	var updateLink = function(){
	    this.attr("d", function(d){
	    	var a = d.source;
			var b = d.target;

			
			//if (d.type == "edgeLink"){
				if (b.outside.isOutside && d.target.noLayer == undefined){
					return "M" + b.x + " " + b.y + " L " + a.x + " " + a.y;
				}
			///} else {
				//if ((a.outside.isOutside && b.outside.isOutside) || (b.outside.isOutside && b.type == "anchor")){
				//	return "M" + b.x + " " + b.y + " L " + a.x + " " + a.y;
				//}
			//}
			
			var ax = a.x - (a.x - b.x) / 3;
			var by = b.y - (b.y - a.y) / 3;
			var p = "M " + b.x + " " + b.y + " C " + b.x + " " + by + " " + ax + " " + a.y + " " + a.x + " " + a.y;

			//for no Layer nodes
			if (b.noLayer){
				d.up = true;
				d.arrow = {};
				d.arrow.x = b.x;
				d.arrow.y = b.y;
				getArrowAngle(d, ax, by, 0.05);
				return p;
			}

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
			if (d.target.outside.isOutside && d.target.noLayer == undefined){
				return "";
			}
			//if (d.arrow == undefined){
				//console.log(d.id);
				//return "";
			//}
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
			if (!d.target.outside.isOutside || d.target.noLayer){
				if (d.arrow.x == NaN){
					console.log("x NaN: " + d);
					d.arrow.x = xOffset + 10;
				}
				if (d.arrow.y == NaN){
					console.log("y NaN: " + d);
					d.arrow.y = 10;
				}
				return "rotate(" + d.angle + " " + d.arrow.x + " " + d.arrow.y + ")";
			}
		})
		//alert();
	}

	//calculate the X of inside labels
	function getXofLabel(d, t, y){
		var a = nodesData[d.node.src];
		var b = nodesData[d.node.tgt];
		var ax = a.x - (a.x - b.x) / 3;
		var by = b.y - (b.y - a.y) / 3;
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
	    d.x = Px;
	    return Py < y;
	}

	//calculate coordinate on the bezier curve
	function getXofBezier(d, ax, by, t, y){
		var a = d.source;
		var b = d.target;
		var x = b.x;
		var tmpWidth = d.width / 2;
		/*var tmpWidth = 100;
		var obj = d3.select("nodeLabel" + b.id)
			.attr("id", function(d){
				tmpWidth = d.width / 2;
			})*/
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
						var layer = d.node.layer * 2;
						if (!layerLabel[layer]){
							layerLabel[layer] = [];
						}
						layerLabel[layer].push(d);
					}
				} else if (d.type == "edgeLinkLabel"){
					if(d.show) {
						if (!layerLabel[0]){
							layerLabel[0] = [];
						}
						layerLabel[0].push(d);
					}
				} else if (d.type == "linkLabel"){
					//if (!nodesData[d.node.src].outside.isOutside && !nodesData[d.node.tgt].outside.isOutside){
					if (d.show){
						if (nodesData[d.node.tgt].noLayer == undefined){
							var layer = (nodesData[d.node.src].layer + nodesData[d.node.tgt].layer);
							if (!layerLabel[layer]){
								layerLabel[layer] = [];
							}
							layerLabel[layer].push(d);
						} else {
							layerLabel[0].push(d);
						}
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
	    	}/**/
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
			if (d.x == NaN){
				console.log("x NaN: " + d);
				d.x = xOffset + 10;
			}
			if (d.y == NaN){
				console.log("y NaN: " + d);
				d.y = 10;
			}
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
	          		l = Math.sqrt(x * x),
	          		r = d.width / 2 + textHeight / 3 * 2 + quad.point.width / 2;
	      		if (l < r) {
	        		l = (l != 0) ? (l - r) / l * 0.5 : (l - r) * 0.5;
	        		x *= l;
	        		d.x = Math.max(d.width, d.x - x);
	        		quad.point.x = Math.min(xOffset + windowWidth - d.width, quad.point.x + x);
	      		}
	    	}
	    	return x1 > nx2 || x2 < nx1;
	  	};
	}

	//collision detection for outside nodes
	function collideOutside(d){
	  	var r = d.width / 2 + textHeight,
	      	nx1 = d.x - r,
	      	nx2 = d.x + r,
	      	ny1 = d.y - textHeight,
	      	ny2 = d.y;
	  	return function(quad, x1, y1, x2, y2) {
	    	if (quad.point && (quad.point !== d)) {
	      		var x = d.x - quad.point.x,
	          		y = (d.y - quad.point.y),
	          		lx = Math.abs(x),
	          		ly = Math.abs(y),
	          		rx = d.width / 2 + quad.point.width / 2,
	          		ry = textHeight;

	          	if (ly >= ry || lx >= rx){
	          		return true;
	          	}      		
	        	ly = (ly != 0) ? (ly - ry) / ly * 0.5 : (ly - ry) * 0.5;
	        	y *= ly;
	        	d.y = Math.max(d.y - y, textHeight);
	        	quad.point.y = Math.min(quad.point.y + y, height);
	    	}
	    	return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
	  	};
	}
	//reset data
	function resetData(){
		nodePosMap.clear();
		nodesData.forEach(function(d){
			nodePosMap.set(d.nodeId, {x : d.x, y : d.y});
		});

		//padding = 35;
		windowWidth = parseInt($("." + cssClass).css("width"));
		leftPanelWidth = window.innerWidth - windowWidth;
		maxXOfferset = 0;
		width=0;           
		height=0;

		test = [];
	 	//tableData = [];                            //store table data
		//columnPos = [];                            //position for each column
		anchorData = [];                           //store anchor nodes
		nodesData = [];                            //store all nodes includes anchors
		linksData = [];                            //links data
		noCycleLinksData = [];                     //cycles are removed
		cycles = [];                               //all cycles, each cycle contians all nodes in that cycle.
		textData = [];                             //text nodes
		textLinksData = [];                        //text links
		layerMap = [];                             //store nodes'id in sequence of layers
		nodesChildren = [];                        //store node's id and its children pair
		SCCindex = 0;                              //strong connected component node's index
		SCCNodes = [];                             //SCC nodes set
		SCCtmpNodes = [];                          //the nodes stack of SCC
		layerLabel = [];                           //layers are divided into sections based on its layer
		
		map = new myMap();
		idMap = [];                                //map from label to id
		edgeIdMap = [];                            //map of edge's id	

		maxLayer = 0;                              //max layer number, base 0
		reshuffleFrequency = 8;                    //pixel changes to excute scroll bar event
		xOffset = 0;                               //x position offset
		firstTime = true;                          //first time to load the force-layout
		maxLabelLength = 0;    	
	}

	//initialize data
	function initializeData(tmpL, tmpN){
		var anchorNameIndex = 0;
		tmpN.forEach(function(d, i){
			var node = {};
			node.label = d.id;
			node.rdfsLabel = d.rdfsLabel;
			node.rdfsComment = d.rdfsComment;
			node.id = i;
			node.nodeId = d.nodeId;
			node.degree = 0;
			node.showLabel = false;
			node.original = d;
			node.isForcedByUser = d.isForcedByUser;
			node.isTemporary = d.isTemporary;
			if (d.column || d.column == 0){
				//columnPos.push(d.xPos);
				node.nodeId = d.hNodeId;
				node.column = d.column;
				node.type = "anchor";
				node.layer = 0;
				node.xpos = d.xPos;
				anchorData.push(node);
				layerMap[i] = 1;
			} else {
				node.type = "node";
			}
			node.position = {};
			node.outside = {};
			//node.outside.position = {};
			node.outside.isOutside = false;
			if (nodePosMap.has(node.nodeId)){
				node.x = nodePosMap.get(node.nodeId).x;
				node.y = nodePosMap.get(node.nodeId).y;
			}
			nodesData.push(node);
			idMap[d.id] = i;

		
			textData.push({
				node : node,
				nodeId : "circle" + d.nodeId
			});
			textData.push({
				node : node,
				nodeId : d.nodeId,
				content : anchorNameIndex < anchorName.length ? anchorName[anchorNameIndex++] : d.label,
				type : "nodeLabel"
			});

			textLinksData.push({
				source : textData.length - 2,
				target : textData.length - 1,
				edgeId : d.nodeId
			});
			
		});

		tmpL.forEach(function(d, i){
			var edge = {};
			edge.source = idMap[d.source];
			edge.target = idMap[d.target];
			edge.id = i;
			edge.edgeId = d.id;
			edge.linkType = d.linkType;
			edge.linkStatus = d.linkStatus;
			edge.rdfsLabel = d.rdfsLabel;
			edge.rdfsComment = d.rdfsComment;
			edge.isProvenance = false;
			if(d.isProvenance)
				edge.isProvenance = d.isProvenance;
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
				type : "linkCircle",
				nodeId : "circle" + d.id
			});
			textData.push({
				node : node,
				type : "linkLabel",
				content : d.label,
				nodeId : d.id
			});

			textLinksData.push({
				source : textData.length - 2,
				target : textData.length - 1,
				edgeId : d.id
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

		//mark anchors that do not connect to any nodes
		nodesData.forEach(function(d){
			if (d.type == 'anchor' && d.degree == 0){
				d.unAssigned = true;
			}
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
		//model point of cycles to edge, ex: [1,2,3] to {1->2, 2->3, 3->1}
		//edge set stored in the data structure of arraylist, and all these edge sets store into an array, defined above -- cycleSet
		cycles.forEach(function(d){
			var tmpArrayList = new ArrayList();
			for (var i = 0; i < d.length; i++){
				//e is the edge, the pattern is (source Id) + " " + (target Id)
				var e = "";
				if (i == d.length - 1){
					e = d[i] + " " + d[0];
				} else {
					e = d[i] + " " + d[i + 1];
				}
				//console.log(e);
				tmpArrayList.add(e);
			}
			//tmpArrayList.print();
			cycleSet.push(tmpArrayList);
		});
	}

	//set layer and position for each node
	function setLayer(tmpLinkData, tmpE){
		//layer is set from bottem to top, one layer per loop. The anchors are layer 0.
		//change evalutes how many node's layer has set in one loop, if no node's layer is set, change is 0. It means all node's layer is set properly
		var change = anchorData.length;
		var baseLayer = new ArrayList();
		anchorData.forEach(function(d){
			baseLayer.add(d.id);
		});

		//for (var j = 0; j < 5; j++){
		var prevChange = -1;
		var numChances = 0;
		while (change > 0){
			//tmpLayerMap is a set of nodes that change their layer in this loop, they will be considered as the base layer for next iteration.
			//the base layer of first iteration is anchors, their layer is 0.
			//console.log("baseLayer: ");
			//baseLayer.print();
			var nextLayer = new ArrayList();

			if(prevChange == change) {
				numChances++;
			} else {
				numChances = 0;
			}
			prevChange = change;
			change = 0;

			if(numChances > 10) {
				console.log("In a loop to many time, just breaking out");
				maxLayer -= 9;
				break;
			}
			//parse all edges
			tmpLinkData.forEach(function(d){
				var src = d.source;
				var tgt = d.target;
				//flag to check if one link need to be evaluated. It is used to avoid infinity loop of cycle.
				//when target id is in the base layer, the source will be set will layer = target's layer + 1, if this edge dose not comprise a cycle, the third edge of above example.
				if (baseLayer.contains(tgt)){
					var e = src + " " + tgt;
					var flag = true;
					cycleSet.forEach(function(cycleInstance){
						if (cycleInstance.contains(e)){
							//when the edge set has only one edge, this edge will comprise a cycle based on the tree we build befor, the tree consists of all edge we evaluated before
							if (cycleInstance.size > 1){
								//remove the edge from edge set.
								cycleInstance.removeByValue(e);											
							} else {
								flag = false;
							}						
						}
					});
					//set the source node's layer, and record the source node id for next iteration
					if (flag){
						nodesData[src].layer = nodesData[tgt].layer + 1;
						if (!nextLayer.contains(src)){
							nextLayer.add(src);
						}
						change++;
						//console.log("id: " + src + "  layer: " + nodesData[src].layer);		
					}		
				}
			});
			baseLayer = nextLayer;
			//maxLayer is the max layer value we set.
			maxLayer++;
			//console.log("change: " + change);			
		}
		maxLayer--;	
		/*
		nodesData.forEach(function(d, i){
			console.log(i + " " + d.layer);
		})
		*/

		//store the node id in the sequence of layer
		//xPos is the x position for nodes in the unit of column's width
		layerMap = d3.range(maxLayer + 1)
			.map(function(d){
				return [];
			});
		nodesData.forEach(function(d){
			if (d.layer != undefined){
				if (!d.unAssigned){
					if(d.layer > maxLayer)
						d.layer = maxLayer;
					layerMap[d.layer].push(d.id);
				}				
			}		
		});

		//set xpos of nodes, check whether a node is a outside nodes;
		var offset = Math.max(xOffset - leftPanelWidth,0);
		layerMap.forEach(function(d, i){
			if (i > 0){
				d.forEach(function(e){
					var tmp = [];
					nodesChildren[e].forEach(function(f){
						if (!nodesData[f].outside.isOutside){
							tmp.push(nodesData[f].xpos);
						}
					});	
					if (tmp.length == 0){
						nodesData[e].outside.isOutside = true;
						nodesData[e].xpos = -1;
					} else {
						nodesData[e].xpos = (d3.min(tmp) + d3.max(tmp)) / 2;
					}
				}); 
			} else {
				d.forEach(function(e){
					var tmpX = nodesData[e].xpos;	
					if (tmpX - nodeRadius > offset && tmpX + nodeRadius < offset + windowWidth){
						nodesData[e].outside.isOutside = false;
					} else {
						nodesData[e].outside.isOutside = true;
					}
				})
			}
		});


		//for node that has no layer, set it as outside node	
		nodesData.forEach(function(d){
			if (d.layer == undefined){
				d.outside.isOutside = true;
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
			edge.linkType = d.linkType;
			edge.type = "edgeLink";
			edge.node = {};
			edge.node.original = d;
			linksData.push(edge);

			var node = {};
			node.src = edge.source;
			node.tgt = edge.target;
			node.original = d;
			textData.push({
				node : node,
				type : "edgeLinkCircle",
				content : d.label
			});
			textData.push({
				node : node,
				type : "edgeLinkLabel",
				content : d.label,
				nodeId : d.id
			});

			textLinksData.push({
				source : textData.length - 2,
				target : textData.length - 1,
				edgeId : d.id
			});
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

	//print the Extented svg
	function printExtentedSVG(resolution, callback){
		var recordWidth = windowWidth;
		windowWidth = width + 100;
		print = true;
		firstTime = true;
		setNodePosition();

		var objSVG = document.getElementById(htmlElement);
		setTimeout(function(){
			saveSvgAsPng(objSVG, htmlElement + ".png", resolution);
			windowWidth = recordWidth;
			print = false;
			firstTime = true;
			setNodePosition();
			if(callback)
				callback();
		}, 1500);
	}

	//print screen shot
	function printSVG(resolution, callback){
		print = true;
		firstTime = true;
		setNodePosition();
		var objSVG = document.getElementById(htmlElement);
		setTimeout(function(){
			saveSvgAsPng(objSVG, htmlElement + ".png", resolution);
			print = false;
			firstTime = true;
			setNodePosition();
			if(callback)
				callback();
		}, 1500);
	}

	//extract table header
	function extractTable(data, prefix) {		
		data.forEach(function(column, i) {			
			if(column.hasNestedTable) {
				extractTable(column.columns, column.columnName + ":");
			} else {
				anchorName.push(prefix + column.columnName);
			}
		});
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

	//set the outside nodes
	function setNodePosition(){
		//console.log(print);
		var change = 0;
		var offset = Math.max(xOffset - leftPanelWidth,0);

		//set xpos of nodes, and check if node is an outside node
		layerMap.forEach(function(d, i){
			if (i > 0){
				d.forEach(function(e){
					var tmp = [];
					nodesChildren[e].forEach(function(f){
						if (!nodesData[f].outside.isOutside){
							tmp.push(nodesData[f].xpos);
						}
					});	
					if (tmp.length == 0){
						if (!nodesData[e].outside.isOutside){
							change++;
						}
						nodesData[e].outside.isOutside = true;
						nodesData[e].xpos = -1;
					} else {
						if (nodesData[e].outside.isOutside){
							change++;
						}
						nodesData[e].xpos = (d3.min(tmp) + d3.max(tmp)) / 2;
						if (!d.fixed){
							nodesData[e].position.x = nodesData[e].xpos;
						}
						nodesData[e].outside.isOutside = false;
					}
				}); 
			} else {
				d.forEach(function(e){
					var tmpX = nodesData[e].xpos;	
					if (tmpX - nodeRadius > offset && tmpX + nodeRadius < offset + windowWidth){
						if (nodesData[e].outside.isOutside){
							change++;
						}
						nodesData[e].outside.isOutside = false;
					} else {
						if (!nodesData[e].outside.isOutside){
							change++;
						}
						nodesData[e].outside.isOutside = true;
					}
				})
			}
		});


		
		//Set the color, opacity of nodes based on the status of isOutside
		nodes.each(function(d){
			/*if (d.noLayer){
				d.outside.isOutside = true;
				d3.select(this)
					.attr("opacity", 0.8)
					.attr("r", nodeRadius)
					.attr("fill", "red");
			}		*/				
			if (d.fixed && !(d.x - nodeRadius > offset && d.x + nodeRadius < offset + windowWidth)){
				d.fixed = false				
				d3.select(this).classed("fixed", false);
	  			d.position.x = d.xpos;
				d.position.y = height - nodeRadius - d.layer * unitLinkLength;
			}
			if (d.outside.isOutside && !d.noLayer){
				if (d.unAssigned){

				}				
				d3.select(this)
					.transition()
					.duration(500)
					.attr("opacity", 0.5)
					.attr("r", nodeRadius)
					.attr("fill", function(d){
						return cScale(d.index + 1);
					});	
				
			} else {
				d3.select(this)
					.transition()
					.duration(500)
					.attr("opacity", 0.7)
					.attr("r", nodeRadius)
					.attr("fill", "red");				
			}
			//console.log(d.id + " " + d.outside.isOutside + " " + d.position.x);
		});
		//console.log(change)

		//when some node changes its status, the correspoding links, labels and the x position of inside nodes should also change.
		if (change > 0 || firstTime){
			firstTime = false;
			d3.select(htmlElement).selectAll(".nodeLabel")
				.attr("opacity", function(d){
					if (d.node.noLayer){
						d.node.showLabel = true;
						return 1;
					}
					if (d.type == "nodeLabel" && d.node.outside){
						if ((!d.node.outside.isOutside && (print || d.node.type != "anchor")) || (d.node.outside.isOutside && d.node.degree >= 3)){
							d.node.showLabel = true;
							return 1;
						}
					}	
					d.node.showLabel = false;
					return 0;			
				});
			d3.select(htmlElement).selectAll(".linkLabel")
				.attr("opacity", function(d){
					if (d.type == "linkLabel"){
						// if (nodesData[d.node.tgt].noLayer){
						// 	d.show = true;
						// 	return 1;
						// }
						// if (nodesData[d.node.tgt].type == 'anchor' && !nodesData[d.node.tgt].outside.isOutside){
						// 	d.show = true;
						// 	return 1;
						// }
						if ((d.node.src < nodesData.length && nodesData[d.node.src].noLayer == undefined && nodesData[d.node.src].outside.isOutside) 
							|| (d.node.tgt < nodesData.length && nodesData[d.node.tgt].noLayer == undefined && nodesData[d.node.tgt].outside.isOutside)){
							d.show = false;
							return 0;
						}
						d.show = true;
						return 1;
					}
				});
			d3.select(htmlElement).selectAll(".edgeLinkLabel")
				.attr("opacity", function(d){
					//console.log(d.index);
					if (!d.node.src.show || 
						(nodesData[d.node.tgt].noLayer == undefined && nodesData[d.node.tgt].outside.isOutside)) {
						d.show = false;
						return 0;
					}
					d.show = true;
					return 1;					
				});
			/*d3.select(htmlElement).selectAll(".clickBoard")
				.attr("fill", function(d){
					if (d.content == "edgeLinks"){
						if (nodesData[d.node.tgt].outside.isOutside || !d.node.src.show){
							return "node";
						}
						return "transparent";
					} else if (d.type == "linkLabel"){
						if (nodesData[d.node.src].outside.isOutside || nodesData[d.node.tgt].outside.isOutside){
							return "none";
						}
						return "transparent";
					} 
					return d.node.showLabel ? "transparent" : "none";
				});*/

			links.classed("outsideLink", function(d){
				/*if (d.type == "edgeLink"){
					return d.target.outside.isOutside;
				}*/
				if (d.target.noLayer){
					return false;
				}
				if (d.source.outside && d.target.outside){
					if (d.target.type == 'anchor' && !d.target.outside.isOutside){
						return false;
					}
					return d.source.outside.isOutside || d.target.outside.isOutside;
				}
				return false;
			});

			
			force.start();
		}
	}

	var generateLayout = function(json) {  
		//read file and execute program
		//d3.json(jsonFile, function(d){
		var processData = function(d) {
			resetData();
			var tmpNodeData = d.anchors.concat(d.nodes);
			var tmpLinkData = d.links;
			var tmpEdgeLink = d.edgeLinks;

			xOffset = window.pageXOffset;
			width = d.width;// + padding;
			maxXOfferset = Math.max(0, leftPanelWidth + width - window.innerWidth);
			windowWidth = Math.ceil(Math.min(windowWidth + Math.min(xOffset, leftPanelWidth), width));

			//extractTable(d.tableLayout, "");
			//console.log(anchorName);
			initializeData(tmpLinkData, tmpNodeData);
			removeCycle();
			
			var tmpL = linksData.slice(0);
			setLayer(tmpL, tmpEdgeLink);

			
			height = (maxLayer + 0.5) * unitLinkLength * 1.5;
			if (width > window.innerWidth){
				height += (maxLayer + 1.5) * outsideUnitLinkLength;
			}

			console.log("width: " + width + "  window height: " + height + " max offset: " + maxXOfferset);

			svg.attr("width", width + 100);
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

			// setTimeout(function(){
			// 	setNodePosition();
			// },0);
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
	
	this.setAnchorClickListener = function(listener) {
		anchorClickListener = listener;
	}

	this.setAnchorMouseListener = function(listener) {
		anchorMouseListener = listener;
	}
	
	this.setLinkApproveClickListener = function(listener) {
		linkApproveClickListener = listener;
	}

	this.setNodeDragDropListener = function(listener) {
		nodeDragDropListener = listener;
	}

	this.onscroll = function(event){
		//console.log(window.pageXOffset);
		if (Math.abs(window.pageXOffset - xOffset) > reshuffleFrequency){
			xOffset = window.pageXOffset;
			windowWidth = Math.min(rightPanelWidth + Math.min(xOffset, leftPanelWidth), width);
			setNodePosition();
		}
	}

	this.onresize = function(event) {
		rightPanelWidth = parseInt($("." + cssClass).css("width"));
		leftPanelWidth = window.innerWidth - rightPanelWidth;
		windowWidth = rightPanelWidth;
		maxXOfferset = 0;
		xOffset = window.pageXOffset;
		maxXOfferset = Math.max(0, leftPanelWidth + width - window.innerWidth);
		windowWidth = Math.ceil(Math.min(windowWidth + Math.min(xOffset, leftPanelWidth), width));

	    console.log("on resize: rightPanelWidth: " + rightPanelWidth + "  leftPanelWidth: " + leftPanelWidth + " maxXOfferset: " + maxXOfferset + " windowWidth: " + windowWidth + " xOffset: " + xOffset);
		setNodePosition();
	};

	//The savePath format: "file/image/", include last 'image'. 
	this.printExtented = function(resolution, callback){
		printExtentedSVG(resolution, callback);
	}
	this.print = function(resolution, callback){
		printSVG(resolution, callback);
	}
};
