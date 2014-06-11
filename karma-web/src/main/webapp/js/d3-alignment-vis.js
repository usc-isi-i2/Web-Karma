/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

//Called for every AlignmentSVGVisualizationUpdate
function displayAlignmentTree_ForceKarmaLayout(json) {
    var worksheetId = json["worksheetId"];
    var mainWorksheetDiv = $("div#"+worksheetId);
    var tableLeftOffset = mainWorksheetDiv.offset().left;
    var optionsDiv = $("div#WorksheetOptionsDiv", mainWorksheetDiv);
    var viewStraightLineModel = optionsDiv.data("viewStraightLineModel");
   
    console.log("displayAlignmentTree_ForceKarmaLayout:viewStraightLineModel:" + viewStraightLineModel);
    
    var w = 0;
    var levelHeight = 50;
    if($(mainWorksheetDiv).data("svgVis") != null) {
        //w = $("div#svgDiv_"+worksheetId).width();
        $("div#svgDiv_"+worksheetId).remove();
    }
    
    $("<div>").attr("id","svgDiv_"+worksheetId).addClass("svg-model").insertBefore('div#'+worksheetId + " > div.table-container");
    
    var h = 0;
    // if(json["maxTreeHeight"] == 0)
        // h = levelHeight * (json["maxTreeHeight"] + 0.2);
    // else
        h = levelHeight * (json["maxTreeHeight"] + 0.4);
    if(w == 0)
        w = $("div#"+worksheetId + "TableDiv").width();
    
    var svg = d3.select("div#svgDiv_"+worksheetId).append("svg:svg")
        .attr("width", w)
        .attr("height", h);
        
    $(svg).data("alignmentId", json["alignmentId"]);
    $(svg).data("worksheetId", json["worksheetId"]);
    
    $(mainWorksheetDiv).data("svgVis", svg);
    $(mainWorksheetDiv).data("forceLayoutObject", force);
    
    //console.log("There are " + json["links"].length + " links, " + json["nodes"].length + " nodes");
    $.each(json["nodes"], function(index, node){
        node["fixed"] = true;
        var hNodeList = node["hNodesCovered"];
       
        var extremeLeftX = Number.MAX_VALUE;
        var extremeRightX = Number.MIN_VALUE;
        var inside = false;
        $.each(hNodeList, function(index2, hNode){
            var hNodeTD = $("td#"+hNode);

            if(hNodeTD != null && $(hNodeTD).offset() != undefined) {
                inside = true;
                var leftX = $(hNodeTD).offset().left - tableLeftOffset;
                var rightX = $(hNodeTD).offset().left - tableLeftOffset + $(hNodeTD).width();
                if(leftX < extremeLeftX)
                    extremeLeftX = leftX;
                if(rightX > extremeRightX)
                    extremeRightX = rightX;
            }
        });
        
        if(hNodeList.length != 0 && inside) {
	        // Add 18 to account for the padding in cells
	        var width = extremeRightX - extremeLeftX + 18;
	        node["width"] = width;
	        node["y"] = h - ((node["height"] * levelHeight));
	        if(node["nodeType"] == "ColumnNode" || node["nodeType"] == "Unassigned")
	            node["y"] -= 5;
	        if(node["nodeType"] == "FakeRoot")
	            node["y"] += 15;
	        node["x"] = extremeLeftX + width/2;
        }
    });
    
    
  //Take into account where hNodesCovered is empty as this node does
    //not anchor to anything in the table
    
    $.each(json["nodes"], function(index, node){
        node["fixed"] = true;
        var hNodeList = node["hNodesCovered"];
        
       ///console.log("Hnode:" + node["id"] + "->" + hNodeList.length);
        if(hNodeList.length == 0) {
        	//This node does not anchor to the table.
        	//So, to calculate width, we need to go and get information
        	//from the links
        	var linkList = [];
        	$.each(json["links"], function(index2, link) {
        		var source = link["source"];
        		if(typeof source == "object")
        			source = source["index"];
        		var target = link["target"];
        		if(typeof target == "object")
        			target = target["index"];
        		
        		if(source == index) {
        			linkList.push(target);
        		} else if(target == index) {
        			linkList.push(source);
        		}
        	});
       
	        var extremeLeftX = Number.MAX_VALUE;
	        var extremeRightX = Number.MIN_VALUE;
	        $.each(linkList, function(index2, hNodeIdx){
	            var nodeConnect = json["nodes"][hNodeIdx];
	            var width = nodeConnect["width"];
	            var x = nodeConnect["x"];
	            //var y = nodeConnect["y"];
	            var leftX = x - (width/2);
	            var rightX = x + (width/2);
	            if(leftX < extremeLeftX)
	                extremeLeftX = leftX;
	            if(rightX > extremeRightX)
	                extremeRightX = rightX;
	        });
	        //console.log("HNode:" + node["id"] + " has " + linkList.length + " links");
	        //console.log("left, right: " + extremeLeftX + ":" + extremeRightX);
	        
	        var width = extremeRightX - extremeLeftX + 18;
	        node["width"] = width;
	        node["y"] = h - ((node["height"] * levelHeight));
	        if(node["nodeType"] == "ColumnNode" || node["nodeType"] == "Unassigned")
	            node["y"] -= 5;
	        if(node["nodeType"] == "FakeRoot")
	            node["y"] += 15;
	        node["x"] = extremeLeftX + width/2;
	        
	        //console.log("x:" + node["x"] + ", y:" + node["y"] + ", width:" + node["width"]);
        }
    });
    
    var lineLayout;
    if(viewStraightLineModel) {
    	lineLayout = null;
    } else {
	    lineLayout = new LineLayout();
	    $.each(json["nodes"], function(index, node){
	    	if(node.nodeType == "ColumnNode") {
	    		//console.log("Add Column Node: " + node.id + " " + node.x + "," + (h - node.y));
	    		lineLayout.addColumnNode(node.id, node.x, h - node.y);
	    	} else if(node.nodeType == "InternalNode") {
	    		var level = node.height;
	    		var width = node.width;
	    		var height = 20; //node.y;
	    		var left = node.x - (width/2); //node.x is the center point of the node
	    		var top = level * (height + 30);
	    		
	    		//console.log("Add Internal Node: " + node.id + " " + level + " " + left + "," + top + "," + width + "," + height);
	    		lineLayout.addInternalNode(node.id, level, left, top, width, height);
	    	}
	    });
	    
	    $.each(json["links"], function(index, link){
	    	lineLayout.addLink(link.id, link.sourceNodeId, link.targetNodeId);
	    });
	    
	    lineLayout.assignAnchorCoordinates();
	    lineLayout.optimizeGroups();
    }
    
    var force = self.force = d3.layout.force()
        .nodes(json.nodes)
        .links(json.links)
        .size([w, h])
        .start();
    
    svg.append("svg:defs").selectAll("marker")
        .data(["marker-Class", "marker-DataProperty"])
      .enter().append("svg:marker")
        .attr("id", String)
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", function (d) {
            if(d == "marker-Class")
                return 30;
            else
                return 12;
        })
        .attr("refY", 0)
        .attr("markerWidth", 6)
        .attr("markerHeight", 6)
        .attr("orient", "auto")
      .append("svg:path")
        .attr("d", "M0,-5L10,0L0,5");
    
    //Hanlde drawing of the links
    var link = svg.selectAll("line.link")
        .data(json.links)
        .enter().append("svg:line")
        .attr("class", function(d) { return "link " + d.linkType; })
        .attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return d.target.y; })
        .attr("id", function(d) { return "line"+d.source.index+"_"+d.target.index; })
        .attr("marker-end", function(d) {
            if(d.target.nodeType == "ColumnNode") 
                return "url(#marker-DataProperty)";
            else
                return "url(#marker-Class)";
        });
    
    
  //Now, let us try to get straight line links
    link.attr("x1", function(d) {
    	if(viewStraightLineModel) {
    		if (d.linkType == "horizontalDataPropertyLink") {
    		 	return d.source.x;
    		 }
    		 var x1;
    		 if(d.source.y > d.target.y)
    		     x1 = d.source.x;
    		 else
    		     x1 = d.target.x;
    		 
    		 var tx1 = d.target.x - d.target.width/2;
    		 var tx2 = d.target.x + d.target.width/2;
    		 var sx1 = d.source.x - d.source.width/2;
    		 var sx2 = d.source.x + d.source.width/2;
    		 
    		 d.calculateOverlap = 0;
    		 
    		 if(!(x1 >= sx1 && x1 <=sx2)) {
    		 	d.calculateOverlap = 1;
    		 	x1 = getOverlappingCenter(sx1, sx2, tx1, tx2);
    		 	d.overlapx = x1;
    		 }
    		 
    		 var x2;
    		 if(d.source.y > d.target.y)
    		     x2 = d.source.x;
    		 else
    		     x2 = d.target.x;
    		 
    		 if(!(x2 >= tx1 && x2 <=tx2)) {
    		 	d.calculateOverlap = 1;
    		 	x1 = getOverlappingCenter(sx1, sx2, tx1, tx2);
    		 	d.overlapx = x1;
    		 }
    		 
    		 return x1;
    	}
    	return lineLayout.getLinkX1(d.id);
    })
    .attr("y1", function(d) {
    	if (d.linkType == "DataPropertyOfColumnLink" || d.linkType == "ObjectPropertySpecializationLink") {
    		return d.source.y + 18;
    	}
    	if(viewStraightLineModel)
    		return d.source.y; 
    	return d.source.y + 10; //Height is 20 
    })
    .attr("x2", function(d) {
    	if(viewStraightLineModel) {
    		if (d.linkType == "horizontalDataPropertyLink") {
    			return d.target.x;
    		}
    		if(d.calculateOverlap) {
    			return d.overlapx;
    		}
    		var x2;
    		if(d.source.y > d.target.y)
    		     x2 = d.source.x;
    		 else
   			     x2 = d.target.x; 
    			 
   			 var minX2 = d.target.x - d.target.width/2;
   			 var maxX2 = d.target.x + d.target.width/2;
    			 
   			 if(!(x2 >= minX2 && x2 <=maxX2)) {    			 	//Arrow is not wihin the box now
    		 	console.log("x2 of Arrow not in limits: " + x2 + ", Source:" + d.source.x + "," + d.source.width 
    			 			+ " Target:" + d.target.x + "," + d.target.y);
    		 	x2 = d.target.x;
   			 }
   			 return x2;
    	} else {
    		return lineLayout.getLinkX2(d.id);
    	}
    })
    .attr("y2", function(d) { 
    	if(viewStraightLineModel) {
    		return d.target.y; 
    	}
    	
    	if(d.target.nodeType == "InternalNode") {
    		var slope = Math.abs(lineLayout.getLinkSlope(d.id));
        	//console.log(d.source.id + "->" + d.target.id + ": slope=" + slope);
        	if(slope <= 0.2) return d.target.y - 10;
        	if(slope <= 1.0) return d.target.y - 5;
    	}
    	
    	return d.target.y; 
    });
    
    //Hanlde drawing of the link labels
    svg.selectAll("text")
        .data(json.links)
        .enter().append("text")
        .text(function(d) {
        	if(d.label == "classLink")
        		return "uri";
            return d.label;
        })
        .attr("class", function(d) {
            if(d.id != "FakeRootLink")
                return "LinkLabel "+worksheetId + " " + d.linkStatus;
            else
                return "LinkLabel FakeRootLink "+worksheetId;
        })
        .attr("x", function(d) {
        	if(viewStraightLineModel) {
        		if(d.calculateOverlap) {
        			return d.overlapx;
        		}
        		if(d.source.y > d.target.y)
        			return d.source.x;
        		else
        			return d.target.x;
        	}
        	return lineLayout.getLinkLabelPosition(d.id)[0];
        })
        .attr("y", function(d) {
        	if(viewStraightLineModel)
        		return d.target.y - 20;
        	return h - lineLayout.getLinkLabelPosition(d.id)[1];
        })
        .attr("transform", function(d) {
            var X = 0; var Y = 0;
            if(d.source.y > d.target.y)
                X = d.source.x;
            else
                X = d.target.x;
            Y = (d.source.y + d.target.y)/2;
            return "translate(" + (this.getComputedTextLength()/2 * -1) + ")";
            // return "translate(" + (this.getComputedTextLength()/2 * -1) + ") rotate(-8 " +X+","+Y+ ")";  
        }).on("click", function(d){
            //showAlternativeLinksDialog(d, svg, d3.event);
        	
        	PropertyDropdownMenu.getInstance().show(
        			$(svg).data("worksheetId"),
        			$(svg).data("alignmentId"),
        			d["id"],
        			d["linkUri"],
        			d["sourceNodeId"],
        			d.source.nodeType,
        			d.source.label,
        			d.source.nodeDomain,
        			d.source.id,
        			d["targetNodeId"],
        			d.target.nodeType,
        			d.target.label,
        			d.target.nodeDomain,
        			d.target.id,
        			d3.event);
        }).on("mouseover", function(d){
            d3.selectAll("g.InternalNode").each(function(d2,i) {
                if(d2 == d.source) {
                    var newRect = $(this).clone();
                    newRect.attr("class","InternalNode highlightOverlay");
                    $("div#svgDiv_" + json["worksheetId"] + " svg").append(newRect);
                    return false;
                }
            });
        })
        .on("mouseout", function(d){
            // d3.selectAll("g.Class").classed("highlight", false);
            $("g.highlightOverlay").remove();
        });
    
    
    //Handle drawing of nodes
    var node = svg.selectAll("g.node")
        .data(json.nodes);
        
    node.enter().append("svg:g")
        .attr("class", function(d) {
            return d["nodeType"];
        });
     
    node.append("text")
        .attr("dy", ".32em")
        .text(function(d) {
            $(this).data("text",d.label);
            if(d.nodeType == "ColumnNode" || d.nodeType == "Unassigned" || d.nodeType == "FakeRoot")
                return "";
            else 
                return d.label; })
        .attr("width", function(d) {
            var newText = $(this).text();
            if(this.getComputedTextLength() > d["width"]) {
                if(d.nodeType == "ColumnNode" || d.nodeType == "Unassigned" || d.nodeType == "FakeRoot")
                    return 0;
                $(this).qtip({content: {text: $(this).data("text")}});
                // Trim the string to make it fit inside the rectangle
                while(this.getComputedTextLength() > d["width"]) {
                    if(newText.length > 6) {
                        newText = newText.substring(0,newText.length/2-2) + "..." + newText.substring(newText.length/2+2,newText.length); 
                        $(this).text(newText);
                    }
                    else
                        break;
                }   
            }
            else
                return this.getComputedTextLength();
        })
        .attr("x", function(d){ return this.getComputedTextLength()/2 * -1;})
        .on("click", function(d){
            if(d["nodeType"] == "InternalNode") {
        		ClassDropdownMenu.getInstance().show(worksheetId, d.id, d.label, d["id"], d.nodeDomain,
        				$(svg).data("alignmentId"), d3.event);
            }
        })
        ;
        
    node.insert("rect", "text")
        .attr("ry", 6)
        .attr("rx", 6)
        .attr("class", function(d){
            if(d.nodeType != "ColumnNode" && d.nodeType != "Unassigned" && d.nodeType != "FakeRoot")
                return worksheetId;
        })
        .attr("y", function(d){
            if(d.nodeType == "ColumnNode" || d.nodeType == "Unassigned" || d.nodeType == "FakeRoot") {
                return -2;
            } else if (d.nodeType == "DataPropertyOfColumnHolder") 
            	return 0;
            else
                return -10;
        })
        .attr("height", function(d){
            if(d.nodeType == "ColumnNode" || d.nodeType == "Unassigned" || d.nodeType == "FakeRoot")
                return 6;
            else if (d.nodeType == "DataPropertyOfColumnHolder") 
            	return 0;
            else
                return 20;  
        })
        .attr("width", function(d) {
            if(d.nodeType == "ColumnNode" || d.nodeType == "Unassigned" || d.nodeType == "FakeRoot")
                return 6;
           else if (d.nodeType == "DataPropertyOfColumnHolder") 
            	return 0;
            else
                return d["width"];
        }).attr("x", function(d){
            if(d.nodeType == "ColumnNode" || d.nodeType == "Unassigned" || d.nodeType == "FakeRoot") {
                return -3;
            } else if (d.nodeType == "DataPropertyOfColumnHolder") 
            	return 0;
            else
                return d.width/2 * -1;
        })
        .on("click", function(d){
           if(d["nodeType"] == "InternalNode") {
        	   ClassDropdownMenu.getInstance().show(worksheetId, d.id, d.label, d["id"], d.nodeDomain,
        			   $(svg).data("alignmentId"), d3.event);
            }
        })
        ;

    node.insert("path")
        .attr("d", function(d) {
            if(d.nodeType == "ColumnNode" || d.nodeType == "Unassigned" || d.nodeType == "FakeRoot" || d.nodeType == "DataPropertyOfColumnHolder") {
                return "M0 0Z";
            } else {
                var w = d.width/2;
                return "M" + (w-12) + " -2 L" + (w-2) + " -2 L" + (w-7) + " 3 Z";
            }
        })
        .attr("y", function(d){
            if(d.nodeType == "ColumnNode" || d.nodeType == "Unassigned" || d.nodeType == "FakeRoot" || d.nodeType == "DataPropertyOfColumnHolder") {
                return -2;
            }
            else {
                return -10;
            }
        }).attr("x", function(d){
            if(d.nodeType == "ColumnNode" || d.nodeType == "Unassigned" || d.nodeType == "FakeRoot" || d.nodeType == "DataPropertyOfColumnHolder") {
                return 0;
            } else {
                return d["width"]-5;
            }
        })
        .on("click", function(d){
        	if(d["nodeType"] == "InternalNode") {
        		ClassDropdownMenu.getInstance().show(worksheetId, d.id, d.label, d["id"], d.nodeDomain,
        				$(svg).data("alignmentId"), d3.event);
            }
        })
        ;

    /*** Check for collisions between labels and rectangles ***/
    d3.selectAll("text.LinkLabel." + worksheetId)
        .sort(comparator)
        .each(function(d1,i1) {
            // console.log("^^^^^^^^^^^^^^^^^^^^^^^" + d1.label)
            var x1 = this.getBBox().x;
            var y1 = this.getBBox().y;
            var width1 = this.getBBox().width;
            var height1 = this.getBBox().height;
            
            var cur1 = $(this);
            d3.selectAll("rect." + worksheetId).each(function(d2,i2){
                var x2 = d2.px + this.getBBox().x;
                var y2 = d2.py + this.getBBox().y;
                var width2 = this.getBBox().width;
                var height2 = this.getBBox().height;
                // console.log("D2 width: " + d2["width"]);
                
                // Check if they overlap on y axis
                if((y2<y1 && y1<y2+height2 ) || (y1<y2 && y2<y1+height1 && y1+height1<y2+height2)) {
                    // console.log("Collision detected on Y axis");
                    // console.log("Rect- X2: " + x2 + " Y2: " + y2 + " width: " + width2 + " height " + height2);
                    // console.log("Text- X1: " + x1 + " Y1: " + y1 + " width " + width1 + " height " + height1);
                    // Check overlap on X axis
                    if(x1>x2 && x2+width2>x1) {
                        // console.log("Rect- X2: " + x2 + " Y2: " + y2 + " width: " + width2 + " height " + height2);
                        // console.log("Text- X1: " + x1 + " Y1: " + y1 + " width " + width1 + " height " + height1);
                        // console.log("Collision detected!")
                        // console.log(d1);
                        // console.log(d2);
                        // console.log("Number to add: " + (y2-y1-16));
                        $(cur1).attr("y", Number($(cur1).attr("y"))+(y2-y1-16));
                    }
                }
                
            });
        });
        
    /*** Check for collisions between labels ***/
    var flag = 0;
    d3.selectAll("text.LinkLabel." + worksheetId)
        .sort(comparator)
        .each(function(d1,i1) {
            var x1 = this.getBBox().x;
            var y1 = this.getBBox().y;
            var width1 = this.getBBox().width;
            var height1 = this.getBBox().height;
            var cur1 = $(this);
            // console.log("^^^^^^^^^^^^");
            d3.selectAll("text.LinkLabel." + worksheetId)
               .sort(comparator)
               .each(function(d2,i2) {
                   var x2 = this.getBBox().x;
                   var y2 = this.getBBox().y;
                   var width2 = this.getBBox().width;
                   var height2 = this.getBBox().height;
                   if(d1.id != d2.id) {
                       if(y1 == y2) {
                           if(((x1 + width1) > x2) && (x2+width2>x1+width1)){
                                //console.log("Collision detected!");
                               // console.log(d1);
                               // console.log(d2);
                               // console.log("Existing: " + $(cur1).attr("y"));
                               // console.log("Flag: " + flag);
                               if(flag%2 == 0)
                                   $(cur1).attr("y", Number($(cur1).attr("y"))-12);
                               else
                                   $(cur1).attr("y", Number($(cur1).attr("y"))+5);
                               flag++;
                           }
                       } else if(y2 >= y1 && y2 <= y1+height1) {
                    	   //console.log("Collision2 detected!");
                    	   if(flag%2 == 0)
                               $(cur1).attr("y", Number($(cur1).attr("y"))-6);
                           else
                               $(cur1).attr("y", Number($(cur1).attr("y"))+3);
                           flag++;
                       }
                       if(x1+width1 < x2)
                            return false;
                   }
            });
        });
    
    
    node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
    
    $(window).resize(function() {
         waitForFinalEvent(function(){
            displayAlignmentTree_ForceKarmaLayout(json);
         }, 500, worksheetId);
    });
    
    window.setTimeout(function() {
    	 if($(mainWorksheetDiv).data("svgVis") != null) {
    	     var w = $("div#svgDiv_"+worksheetId).width();
    	     svg.attr("width", w);
    	   }
    	 
    }, 100);
    
}

var refreshAlignmentTree = function(worksheetId) {
	var mainWorksheetDiv = $("div#"+worksheetId);
	var svg =  $(mainWorksheetDiv).data("svgVis");
	if(svg) {
		
		var alignmentId = $(svg).data("alignmentId");
		console.log("RefreshSVGAlignmentCommand: " + worksheetId, alignmentId);
		var info = new Object();
	    info["worksheetId"] = worksheetId;
	    info["alignmentId"] = alignmentId;
	    info["workspaceId"] = $.workspaceGlobalInformation.id;
	    info["command"] = "RefreshSVGAlignmentCommand";

	    showLoading(info["worksheetId"]);
	    var returned = $.ajax({
	        url: "RequestController",
	        type: "POST",
	        data : info,
	        dataType : "json",
	        complete :
	            function (xhr, textStatus) {
	                //alert(xhr.responseText);
	                var json = $.parseJSON(xhr.responseText);
	                parse(json);
	                hideLoading(info["worksheetId"]);
	            },
	        error :
	            function (xhr, textStatus) {
	                alert("Error occured while refreshing model!" + textStatus);
	                hideLoading(info["worksheetId"]);
	            }
	    });
	}
};

var getOverlappingCenter = function(line1x1, line1x2, line2x1, line2x2) {
	var start = line1x1;
	if(line2x1 > start) 
		start = line2x1;
	
	var end = line1x2;
	if(line2x2 < end)
		end = line2x2;
	
	var width = end - start;
	return start + width/2;
};

var comparator = function(a,b){
    var x1 = 0;
    if(a.source.y > a.target.y)
        x1 = a.source.x;
    else
        x1 = a.target.x;
        
    var x2 = 0;
    if(b.source.y > b.target.y)
        x2 = b.source.x;
    else
        x2 = b.target.x;
    return x1-x2;
}
// Thanks to http://stackoverflow.com/questions/2854407/javascript-jquery-window-resize-how-to-fire-after-the-resize-is-completed
var waitForFinalEvent = (function () {
    var timers = {};
    return function (callback, ms, uniqueId) {
        if (!uniqueId) {
            uniqueId = "Don't call this twice without a uniqueId";
        }
        if (timers[uniqueId]) {
            clearTimeout (timers[uniqueId]);
        }
        timers[uniqueId] = setTimeout(callback, ms);
    };
})();
