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

function styleAndAssignHandlersToModelingVizElements() {
    var dropDownMenu = $("div#modelingClassDropDownMenu");
    $("button", dropDownMenu).button();

    $("#addNodeIncomingLink").click(function() {
    	console.log("Add Incoming Link");
    	showIncomingOutgoingDialog("incoming");
//    	showLinksForInternalNode(d, svg, d3.event);
    });
    
    $("#incomingOutgoingClassKeyword").keyup(function(event) {
        if(event.keyCode == 13){
            $("#incomingOutgoingClassSearch").click();
        }
    });
    $("#incomingOutgoingPropertyKeyword").keyup(function(event) {
        if(event.keyCode == 13){
            $("#incomingOutgoingPropertySearch").click();
        }
    });
    
    
    $("#incomingOutgoingClassSearch").button().click(function(){
        $("div#incomingOutgoingLinksClassDiv1").jstree("search", $("#incomingOutgoingClassKeyword").val());
        $("div#incomingOutgoingLinksClassDiv2").jstree("search", $("#incomingOutgoingClassKeyword").val());
    });
    $("#incomingOutgoingPropertySearch").button().click(function(){
        $("div#incomingOutgoingLinksPropertyDiv1").jstree("search", $("#incomingOutgoingPropertyKeyword").val());
        $("div#incomingOutgoingLinksPropertyDiv2").jstree("search", $("#incomingOutgoingPropertyKeyword").val());
    });
    
    
    $("#addNodeOutgoingLink").click(function() {
    	console.log("Add Outgoing Link");
    	showIncomingOutgoingDialog("outgoing");
    });
    
    $("#invokeRubenReconciliationService").click(function() {
        console.log("I am clicked!!")
        dropDownMenu.hide();
        var info = new Object();
        info["workspaceId"] = $.workspaceGlobalInformation.id;
        info["command"] = "InvokeRubenReconciliationService";

        var newInfo = [];
        newInfo.push(getParamObject("alignmentNodeId", dropDownMenu.data("nodeId"), "other"));
        newInfo.push(getParamObject("worksheetId", dropDownMenu.data("worksheetId"), "other"));

        info["newInfo"] = JSON.stringify(newInfo);

        showLoading(dropDownMenu.data("worksheetId"));
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
                    hideLoading(dropDownMenu.data("worksheetId"));
                },
            error :
                function (xhr, textStatus) {
                    alert("Error occured while exporting CSV!" + textStatus);
                    hideLoading(dropDownMenu.data("worksheetId"));
                }
        });
    });

    // Adding mouse handlers to the div
    dropDownMenu.mouseenter(function() {
        if ($(this).data("timer") != null)
            clearTimeout($(this).data("timer"));
        $(this).show();
    });
    dropDownMenu.mouseleave(function() {
        var timer = setTimeout(function() {
            $("#modelingClassDropDownMenu").hide();
        }, 700);
        $(this).data("timer", timer);
    });

    // Filter for the links
    $("#linksTableFilter").keyup( function (event) {
        // fire the above change event after every letter

        //if esc is pressed or nothing is entered
        if (event.keyCode == 27 || $(this).val() == '') {
            //if esc is pressed we want to clear the value of search box
            $(this).val('');

            //we want each row to be visible because if nothing
            //is entered then all rows are matched.
            $('tr').removeClass('visible').show().addClass('visible');
        }

        //if there is text, lets filter
        else {
            filter('#linksList tr', $(this).val(), "edgeLabel");
        }
    });

    // Filter for the nodes
    $("#nodesTableFilter").keyup( function (event) {
        // fire the above change event after every letter

        //if esc is pressed or nothing is entered
        if (event.keyCode == 27 || $(this).val() == '') {
            //if esc is pressed we want to clear the value of search box
            $(this).val('');

            //we want each row to be visible because if nothing
            //is entered then all rows are matched.
            $('tr').removeClass('visible').show().addClass('visible');
        }

        //if there is text, lets filter
        else {
            filter('#nodesList tr', $(this).val(), "nodeLabel");
        }
    });

    // Filter for the nodes
    $("#alternateLinksTableFilter").keyup( function (event) {
        // fire the above change event after every letter

        //if esc is pressed or nothing is entered
        if (event.keyCode == 27 || $(this).val() == '') {
            //if esc is pressed we want to clear the value of search box
            $(this).val('');

            //we want each row to be visible because if nothing
            //is entered then all rows are matched.
            $('tr').removeClass('visible').show().addClass('visible');
        }

        //if there is text, lets filter
        else {
            filter('#alternativeLinksList tr', $(this).val(), "edgeLabel");
        }
    });
}

function displayAlignmentTree_ForceKarmaLayout(json) {
    var worksheetId = json["worksheetId"];
    var mainWorksheetDiv = $("div#"+worksheetId);
    var tableLeftOffset = mainWorksheetDiv.offset().left;
    
    var w = 0;
    var levelHeight = 50;
    if($(mainWorksheetDiv).data("svgVis") != null) {
        w = $("div#svgDiv_"+worksheetId).width();
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
        $.each(hNodeList, function(index2, hNode){
            var hNodeTD = $("td#"+hNode);

            if(hNodeTD != null) {
                var leftX = $(hNodeTD).offset().left - tableLeftOffset;
                var rightX = $(hNodeTD).offset().left - tableLeftOffset + $(hNodeTD).width();
                if(leftX < extremeLeftX)
                    extremeLeftX = leftX;
                if(rightX > extremeRightX)
                    extremeRightX = rightX;
            }
        });
        
        if(hNodeList.length != 0) {
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
    })
    .attr("y1", function(d) {
    	if (d.linkType == "DataPropertyOfColumnLink" || d.linkType == "ObjectPropertySpecializationLink") {
    		return d.source.y + 18;
    	}
    	return d.source.y; 
    })
    .attr("x2", function(d) {
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
        
        if(!(x2 >= minX2 && x2 <=maxX2)) {
        	//Arrow is not wihin the box now
        	console.log("x2 of Arrow not in limits: " + x2 + ", Source:" + d.source.x + "," + d.source.width 
        			+ " Target:" + d.target.x + "," + d.target.y);
        	x2 = d.target.x;
        }
        return x2;
    })
    .attr("y2", function(d) { return d.target.y; });
    
    //Hanlde drawing of the link labels
    svg.selectAll("text")
        .data(json.links)
        .enter().append("text")
        .text(function(d) {
            return d.label;
        })
        .attr("class", function(d) {
            if(d.id != "FakeRootLink")
                return "LinkLabel "+worksheetId + " " + d.linkStatus;
            else
                return "LinkLabel FakeRootLink "+worksheetId;
        })
        .attr("x", function(d) {
        	if(d.calculateOverlap) {
        		return d.overlapx;
        	}
        	
            if(d.source.y > d.target.y)
                return d.source.x;
            else
                return d.target.x;
        })
        .attr("y", function(d) {
            if(d.target.nodeType == "ColumnNode")
                return ((d.source.y + d.target.y)/2 + 12);
            if(d.source.nodeType == "FakeRoot")
                return ((d.source.y + d.target.y)/2 - 4);
            return ((d.source.y + d.target.y)/2 + 5);
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
            showAlternativeLinksDialog(d, svg, d3.event);
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
                d["targetNodeId"] = d["id"];
                d.worksheetId = worksheetId;
                d.alignmentId = $(svg).data("alignmentId");
                showClassPopupMenu(d, this, d3.event);
            }

        
        });
        
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
        }).on("click", function(d){
            if(d["nodeType"] == "ColumnNode" || d.nodeType == "Unassigned")
                changeSemanticType_d3(d, svg, d3.event);
            else if(d["nodeType"] == "InternalNode") {
                d["targetNodeId"] = d["id"];
                d.worksheetId = worksheetId;
                d.alignmentId = $(svg).data("alignmentId");
                showClassPopupMenu(d, this, d3.event);
            }

        
        });

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
        }).on("click", function(d){
            if(d["nodeType"] == "InternalNode") {
                var menu = $("div#modelingClassDropDownMenu");
                menu.data("nodeId", d.id);
                menu.data("worksheetId", worksheetId);
                menu.css({"position":"absolute",
                    "top":$(this).offset().top + 5,
                    "left": $(this).offset().left + $(this).width()/2 - $(menu).width()/2}).show();
            }
        }).on("mouseout", function(d){
            var timer = setTimeout(function() {
                $("#modelingClassDropDownMenu").hide();
            }, 700);
            $("#modelingClassDropDownMenu").data("timer", timer);
        });

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
    
//    $("text.LinkLabel").qtip({content: {text: "Edit Relationship"}});
    // $("g.ColumnNode, g.Unassigned").qtip({content: {text: "Change Semantic Type"}});
//    $("g.InternalNode").qtip({content: {text: "Add Parent Relationship"}});
    
    
    

    node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
    
    $(window).resize(function() {
         waitForFinalEvent(function(){
            displayAlignmentTree_ForceKarmaLayout(json);
         }, 500, worksheetId);
    });
}

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

function changeSemanticType_d3(d, vis, event) {
	var optionsDiv = $("#ChangeSemanticTypesDialogBox");
    
    var tdTag = $("td#"+d["hNodeId"]); 
    var typeJsonObject = $(tdTag).data("typesJsonObject");
    optionsDiv.data("currentNodeId",typeJsonObject["HNodeId"]);
    optionsDiv.data("worksheetId", tdTag.parents("div.Worksheet").attr("id"))
    $("table#currentSemanticTypesTable tr.semTypeRow",optionsDiv).remove();
    $("table#currentSemanticTypesTable tr.editRow",optionsDiv).remove();
    $("input#chooseClassKey").attr("checked", false);
    $("div#SemanticTypeErrorWindow").hide();
    $(optionsDiv).removeData("selectedPrimaryRow");
    // Deselect all the advanced options check boxes
    $("div#semanticTypingAdvacedOptionsDiv").hide().data("state","closed");
    $("div#semanticTypingAdvacedOptionsDiv input:checkbox").prop('checked', false);
    $("div#semanticTypingAdvacedOptionsDiv input:text").val("");
    $("div#rdfTypeSelectDiv input").val("")
    
    // Store a copy of the existing types.
    // This is tha JSON array which is changed when the user adds/changes through GUI and is submitted to the server.
    var existingTypes = typeJsonObject["SemanticTypesArray"];
    var existingTypesCopy = jQuery.extend(true, [], existingTypes);
    optionsDiv.data("existingTypes", existingTypesCopy);

    var CRFInfo = typeJsonObject["FullCRFModel"];
    
    // Populate the table with existing types and CRF suggested types
    $.each(existingTypes, function(index, type){
        // Take care of the special meta properties that are set through the advanced options
    	if (type["isMetaProperty"]) {
    		if (type["DisplayLabel"] == "km-dev:classLink") {
    			$("#isUriOfClass").prop('checked', true);
    			$("#isUriOfClassTextBox").val(type["DisplayDomainLabel"]);
    		} else if (type["DisplayLabel"] == "km-dev:columnSubClassOfLink") {
    			$("#isSubclassOfClass").prop('checked', true);
    			$("#isSubclassOfClassTextBox").val(type["DisplayDomainLabel"]);
    		} else if (type["DisplayLabel"] == "km-dev:dataPropertyOfColumnLink") {
    			$("#isSpecializationForEdge").prop('checked', true);
    			$("#isSpecializationForEdgeTextBox").val(type["DisplayDomainLabel"]);
    		}
    		$("div#semanticTypingAdvacedOptionsDiv").show().data("state", "open");
    	} else {
    		addSemTypeObjectToCurrentTable(type, true, false);
    	}
    });
    if(CRFInfo != null) {
        $.each(CRFInfo["Labels"], function(index, type){
            addSemTypeObjectToCurrentTable(type, false, true);
        });
    }
    
    // Get the whole list of classes and properties from the server for autocompletion
    var info = new Object();
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "GetPropertiesAndClassesList";
    info["worksheetId"] = optionsDiv.data("worksheetId");

    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                optionsDiv.data("classAndPropertyListJson", json);
                if (json) {
                    json["elements"][0]["classList"].sort(function(a,b) {
                        return a["label"].toUpperCase().localeCompare(b["label"].toUpperCase());
                    });

                    json["elements"][0]["propertyList"].sort(function(a,b) {
                        return a.toUpperCase().localeCompare(b.toUpperCase());
                    });
                }

                // Special case when no training has been done to CRF model
                // Shows an empty semantic type
                if((!CRFInfo && existingTypes.length == 0) ||
                    ((existingTypes && existingTypes.length == 0) && (CRFInfo && CRFInfo.length == 0)) ||
                    ((existingTypes && existingTypes.length == 0) && (CRFInfo && CRFInfo["Labels"].length == 0))) {
                    addEmptySemanticType();
                    $("table#currentSemanticTypesTable input").prop("checked", true);
                    $("table#currentSemanticTypesTable tr.semTypeRow").addClass("selected");
                    optionsDiv.data("selectedPrimaryRow",$("table#currentSemanticTypesTable tr.semTypeRow"));
                    $("table#currentSemanticTypesTable tr td button").click();
                }
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while fetching classes and properties list! " + textStatus);
            }
    });
    
    // Get the column name to show in dalog box
    var columnName = $("div.wk-header", tdTag).text();
    
    // Show the dialog box
    var positionArray = [event.clientX+20, event.clientY+10];
    optionsDiv.dialog({width: 450, position: positionArray, title:columnName
        , buttons: { 
            "Cancel": function() { $(this).dialog("close"); }, 
            "Submit":submitSemanticTypeChange }
    });
}

function showAlternativeLinksDialog(d, vis, event) {
    var optionsDiv = $("#alternativeLinkDialog");
    optionsDiv.data("sourceNodeId", d["sourceNodeId"]);
    optionsDiv.data("targetNodeId", d["targetNodeId"]);
    optionsDiv.data("currentEdgeId", d["id"]);
    optionsDiv.data("currentEdgeUri", d["linkUri"]);
    optionsDiv.data("alignmentId", $(vis).data("alignmentId"));
    optionsDiv.data("worksheetId",$(vis).data("worksheetId"));

    $("#alternateLinksTableFilter").val("");

    $("#showCompatibleLinks").trigger("click");

    var positionArray = [event.clientX+20       // distance from left
        , event.clientY+10];    // distance from top

    // Show the dialog box
    optionsDiv.dialog({width: 250, height: 400, position: positionArray, title: "Choose Link"
        , buttons: { "Cancel": function() {
            $(this).dialog("close");
        }, "Submit":submitDirectLinkChange }});
}

function submitDirectLinkChange() {
    var optionsDiv = $("#alternativeLinkDialog");

    var table = $("#alternativeLinksList");
    // Flag error if no value has been selected
    if ($("td.selected", table).length == 0) {
        $("span.error", optionsDiv).show();
        return false;
    }
    optionsDiv.dialog("close");

    var info = new Object();
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "ChangeInternalNodeLinksCommand";

    // Prepare the input for command
    var newInfo = [];
    newInfo.push(getParamObject("alignmentId", optionsDiv.data("alignmentId"), "other"));
    newInfo.push(getParamObject("worksheetId", optionsDiv.data("worksheetId"), "worksheetId"));

    // Put the new edge information
    var newEdges = [];
    var newEdgeObj = {};
    newEdgeObj["edgeSourceId"] = optionsDiv.data("sourceNodeId");
    newEdgeObj["edgeTargetId"] = optionsDiv.data("targetNodeId");
    newEdgeObj["edgeId"] = $("td.selected", table).data("edgeId");
    newEdges.push(newEdgeObj);
    newInfo.push(getParamObject("newEdges", newEdges, "other"));

    // Put the old edge information
    var initialEdges = [];
    var oldEdgeObj = {};
    oldEdgeObj["edgeSourceId"] = optionsDiv.data("sourceNodeId");
    oldEdgeObj["edgeTargetId"] = optionsDiv.data("targetNodeId");
    oldEdgeObj["edgeId"] = optionsDiv.data("currentEdgeUri");
    initialEdges.push(oldEdgeObj);
    newInfo.push(getParamObject("initialEdges", initialEdges, "other"));

    info["newInfo"] = JSON.stringify(newInfo);

    showLoading(optionsDiv.data("worksheetId"));
    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                parse(json);
                hideLoading(optionsDiv.data("worksheetId"));
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while getting nodes list!");
                hideLoading(optionsDiv.data("worksheetId"));
            }
    });
}

function populateAlternativeLinks() {
    var optionsDiv = $("#alternativeLinkDialog");

    var info = new Object();
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["sourceNodeId"] = optionsDiv.data("sourceNodeId");
    info["targetNodeId"] = optionsDiv.data("targetNodeId");
    info["command"] = "GetAlternativeLinksCommand";
    info["alignmentId"] = optionsDiv.data("alignmentId");
    info["worksheetId"] = optionsDiv.data("worksheetId");

    if ($(this).attr("id") == "showCompatibleLinks") {
        info["linksRange"] = "compatibleLinks";
    } else {
        info["linksRange"] = "allObjectProperties";
    }

    var table =  $("#alternativeLinksList");
    $("tr", table).remove();
    var currentSelectedLinkId = optionsDiv.data("currentEdgeUri");

//    console.log(info);

    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                $.each(json["elements"], function(index, element) {
                    if(element["updateType"] == "LinksList") {
                        // Sort the list
                        element["edges"].sort(function(a,b) {
                            return a["edgeLabel"].toUpperCase().localeCompare(b["edgeLabel"].toUpperCase());
                        });

                        $.each(element["edges"], function(index2, node) {
                            var trTag = $("<tr>");
                            var edgeTd = $("<td>").append($("<span>").text(node["edgeLabel"]))
                                .data("edgeId", node["edgeId"])
                                .click(function(){
                                    $("td", table).removeClass("selected");
                                    $(this).addClass("selected");
                                });

                            if (edgeTd.data("edgeId") == currentSelectedLinkId) {
                                edgeTd.addClass("selected");
                            }

                            trTag.append(edgeTd).data("edgeLabel", node["edgeLabel"])
                            table.append(trTag);
                        });
                    }
                });
            },
        error :
            function (xhr, textStatus) {
                alert("Error occurred while getting links list!");
            }
    });
}

function showLinksForInternalNode(d, vis, event) {
    // Hide the error window if it was open before
    $("div#currentLinksErrorWindowBox").hide();

    var info = new Object();
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["nodeId"] = d["targetNodeId"];
    info["command"] = "GetCurrentLinksOfInternalNodeCommand";
    info["alignmentId"] = $(vis).data("alignmentId");
    info["worksheetId"] = $(vis).data("worksheetId");

    var optionsDiv = $("div#currentLinksInternalNodeDialog");
    optionsDiv.data("alignmentId", info["alignmentId"]);
    optionsDiv.data("worksheetId", info["worksheetId"]);
    optionsDiv.data("internalNodeId", d["targetNodeId"]);
    optionsDiv.data("internalNodeLabel", d["label"]);
    optionsDiv.data("worksheetId", info["worksheetId"]);

    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
//                alert(xhr.responseText);
                var json = $.parseJSON(xhr.responseText);
                $.each(json["elements"], function(index, element) {
                    if(element["updateType"] == "GetCurrentLinks") {


                        optionsDiv.data("initialEdges", element["edges"]);

                        var inLinksTable = $("table#currentIncomingLinksTable", optionsDiv);
                        var outLinksTable = $("table#currentOutgoingLinksTable", optionsDiv);
                        $("tr", inLinksTable).remove();
                        $("tr", outLinksTable).remove();

                        var positionArray = [event.clientX+20       // distance from left
                            , event.clientY+10];    // distance from top

                        $.each(element["edges"], function(index2, edge) {
                            var trTag = $("<tr>").addClass("InternalNodeLink");

                            var srcTd = $("<td>").addClass("sourceNode").append(
                                            $("<span>").text(edge["edgeSource"])
                                                .addClass("node-or-edge-label").click(showChooseNodeDialog))
                                        .data("nodeId", edge["edgeSourceId"]);

                            var targetTd = $("<td>").addClass("targetNode").append(
                                                $("<span>").text(edge["edgeTarget"])
                                                    .addClass("node-or-edge-label").click(showChooseNodeDialog))
                                            .data("nodeId", edge["edgeTargetId"]);

                            var edgeLabelTd = $("<td>").addClass("edgeLabel").data("edgeId", edge["edgeId"]).append(
                                                    $("<span>").text(edge["edgeLabel"])
                                                        .addClass("node-or-edge-label").click(showChooseLinkDialog));

                            var delButton = $("<td>").append($("<button>").button({
                                                icons: {
                                                    primary: "ui-icon-close"
                                                },
                                                text: false
                                            }).addClass("deleteLink").click(
                                                function(){
                                                    $(this).parents("tr.InternalNodeLink").remove();
                                            }));

                            trTag.data("linkDirection", edge["direction"]);

                            if (edge["direction"] == "incoming") {
                                trTag.append($("<td>").append($("<span>").text("from")))
                                    .append(srcTd)
                                    .append($("<td>").text("via"))
                                    .append(edgeLabelTd)
                                    .append(targetTd).append(delButton);
                                targetTd.hide();
                                inLinksTable.append(trTag);
                            } else if (edge["direction"] == "outgoing"){
                                trTag.append($("<td>").text("to"))
                                    .append(srcTd).append(targetTd)
                                    .append($("<td>").text("via"))
                                    .append(edgeLabelTd).append(delButton);
                                srcTd.hide();
                                outLinksTable.append(trTag);
                            }
                        });

                        if ($("tr", inLinksTable).length == 0) {
                            $(inLinksTable).append($("<tr>").addClass("emptyRow").append($("<td>").text("none")));
                        }

                        if ($("tr", outLinksTable).length == 0) {
                            $(outLinksTable).append($("<tr>").addClass("emptyRow").append($("<td>").text("none")));
                        }
                        // Show the dialog box
                        optionsDiv.dialog({width: 350, height: 380, position: positionArray, title: d["label"]
                            , buttons: { "Cancel": function() { $(this).dialog("close"); }, "Submit":submitInternalNodesLinksChange }});
                    }
                });
            },
        error :
            function (xhr, textStatus) {
                alert("Error occurred while getting alternative links!" + textStatus);
            }
    });
}

function submitInternalNodesLinksChange() {
    var optionsDiv = $("div#currentLinksInternalNodeDialog");
    var table = $("table", optionsDiv);

    var info = new Object();
    info["command"] = "ChangeInternalNodeLinksCommand";
    info["workspaceId"] = $.workspaceGlobalInformation.id;

    var newInfo = [];
    newInfo.push(getParamObject("initialEdges", optionsDiv.data("initialEdges"), "other"));
    newInfo.push(getParamObject("alignmentId", optionsDiv.data("alignmentId"), "other"));
    newInfo.push(getParamObject("worksheetId", optionsDiv.data("worksheetId"), "worksheetId"));

    // Get the new edges information
    var newEdges = [];
    var invalidValueExists = false;
    var invalidRow;
    $.each($("tr", table), function (index, row) {
        if ($(row).hasClass("emptyRow"))
            return true;

        var edgeObj = {};
        edgeObj["edgeSourceId"] = $("td.sourceNode", row).data("nodeId");
        edgeObj["edgeTargetId"] = $("td.targetNode", row).data("nodeId");
        edgeObj["edgeId"] = $("td.edgeLabel", $(row)).data("edgeId");

        // Check for the empty nodes and links
        if (edgeObj["edgeSourceId"] == "emptyNodeId" || edgeObj["edgeTargetId"] == "emptyNodeId"
            || edgeObj["edgeId"] == "emptyEdgeId") {
            invalidValueExists = true;
            invalidRow = row;
            return false;
        }
        newEdges.push(edgeObj);
    });

    // Show error and return if row with invalid value exists
    if (invalidValueExists) {
        console.log("Invalid value exists!");
        $(invalidRow).addClass("fixMe");
        $("span#currentLinksWindowText").text("Please provide valid value!");
        $("div#currentLinksErrorWindowBox").show();
        return false;
    }

    newInfo.push(getParamObject("newEdges", newEdges, "other"));

    info["newEdges"] = newEdges;
    info["newInfo"] = JSON.stringify(newInfo);

    showLoading(optionsDiv.data("worksheetId"));
    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                parse(json);
                hideLoading(optionsDiv.data("worksheetId"));
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while getting nodes list!");
                hideLoading(optionsDiv.data("worksheetId"));
            }
    });
    optionsDiv.dialog("close");
}

function showChooseNodeDialog(event) {
    var optionsDiv = $("div#chooseNodeDialog");
    optionsDiv.data("currentEditedCell", $(this).parent());

    $(this).parents("tr.fixMe").removeClass("fixMe");
    $("div#currentLinksErrorWindowBox").hide();
    $(this).parent().addClass("currentEditedCell");
    $("#nodesTableFilter").val("");

    $("#chooseExistingNodes").trigger("click");

    var positionArray = [event.clientX+20       // distance from left
        , event.clientY+10];    // distance from top

    // Show the dialog box
    optionsDiv.dialog({width: 250, height: 400, position: positionArray, title: "Choose Node"
        , buttons: { "Cancel": function() {
            $(this).dialog("close");
        }, "Submit":submitInternalNodeChange }});
}

function showChooseLinkDialog(event) {
    var optionsDiv = $("div#chooseLinkDialog");
    optionsDiv.data("currentEditedCell", $(this).parent());
    $(this).parents("tr.fixMe").removeClass("fixMe");
    $("div#currentLinksErrorWindowBox").hide();
    $(this).parent().addClass("currentEditedCell");
    $("#linksTableFilter").val("");

    $("#chooseAllLinks").trigger("click");

    var positionArray = [event.clientX+20       // distance from left
        , event.clientY+10];    // distance from top

    // Show the dialog box
    optionsDiv.dialog({width: 280, height: 400, position: positionArray, title: "Choose Link"
        , buttons: { "Cancel": function() {
            $(this).dialog("close");
        }, "Submit":submitLinkChange }});
}

function submitLinkChange() {
    var optionsDiv = $("div#chooseLinkDialog");

    var table = $("#linksList");
    // Flag error if no value has been selected
    if ($("td.selected", table).length == 0) {
        $("span.error", optionsDiv).show();
        return false;
    }
    var selectedLinkId = $("td.selected", table).data("edgeId");
    var selectedLinkLabel = $("td.selected span", table).text();

    optionsDiv.dialog("close");

    // Remove the selection highlighting
    $("#currentIncomingLinksTable td.currentEditedCell").removeClass("currentEditedCell");
    $("#currentOutgoingLinksTable td.currentEditedCell").removeClass("currentEditedCell");

    var cellChanged = $(optionsDiv.data("currentEditedCell"));
    $("span", cellChanged).text(selectedLinkLabel);
    $(cellChanged).data("edgeId", selectedLinkId).addClass("valueChangedCell");
}

function attachHandlersToChangeObjPropertyObjects() {
    $("#chooseExistingNodes, #chooseDomain, #chooseAllNodes").click(populateNodesListFromServer);
    $("#chooseExistingLinks, #choosePropertyWithDomainAndRange, #chooseAllLinks").click(populateLinksListFromServer);

    $("#showCompatibleLinks, #showAllAlternativeLinks").click(populateAlternativeLinks);

    $("#addIncomingInternalNodeLink, #addOutgoingInternalNodeLink").button().click(function() {
        var table;
        var srcTd;
        var targetTd;

        var optionsDiv = $("div#currentLinksInternalNodeDialog");

        if ($(this).attr("id") == "addIncomingInternalNodeLink") {
            table = $("table#currentIncomingLinksTable");
            targetTd = $("<td>").addClass("targetNode").data("nodeId", optionsDiv.data("internalNodeId"))
                .append($("<span>").text(optionsDiv.data("internalNodeLabel"))
                                .click(showChooseNodeDialog)
                                .addClass("node-or-edge-label"));
            targetTd.hide();
        } else {
            table = $("table#currentOutgoingLinksTable");
            srcTd = $("<td>").addClass("sourceNode").data("nodeId", optionsDiv.data("internalNodeId"))
                .append($("<span>").text(optionsDiv.data("internalNodeLabel"))
                            .click(showChooseNodeDialog)
                            .addClass("node-or-edge-label"));
            srcTd.hide();
        }

        var trTag = $("<tr>").addClass("InternalNodeLink");
        if (srcTd == null) {
            srcTd = $("<td>").addClass("sourceNode").data("nodeId", "emptyNodeId")
                        .append($("<span>").text("class").addClass("node-or-edge-label")
                            .click(showChooseNodeDialog));
        }

        if (targetTd == null) {
            targetTd = $("<td>").addClass("targetNode").data("nodeId", "emptyNodeId")
                .append($("<span>").text("class").addClass("node-or-edge-label")
                    .click(showChooseNodeDialog));
        }

        var edgeLabelTd = $("<td>").addClass("edgeLabel").data("edgeId", "emptyEdgeId")
                    .append($("<span>").text("property").addClass("node-or-edge-label")
                    .click(showChooseLinkDialog));

        var delButton = $("<td>").append($("<button>").button({
                icons: {
                    primary: "ui-icon-close"
                },
                text: false
            }).addClass("deleteLink").click(function(){
                $(this).parents("tr.InternalNodeLink").remove();
            }));

        if ($(this).attr("id") == "addIncomingInternalNodeLink") {
            trTag.append($("<td>").text("from"))
                .append(srcTd).append($("<td>").text("via"))
                .append(edgeLabelTd).append(targetTd).append(delButton);
        } else {
            trTag.append($("<td>").text("to"))
                .append(srcTd).append(targetTd).append($("<td>").text("via"))
                .append(edgeLabelTd).append(delButton);
        }

        // Remove the "none" row if present
        $("tr.emptyRow", table).remove();

        table.append(trTag);

    });

    $("div#chooseLinkDialog, div#chooseNodeDialog").bind('dialogclose', function(event) {
        $("#currentIncomingLinksTable td.currentEditedCell").removeClass("currentEditedCell");
        $("#currentOutgoingLinksTable td.currentEditedCell").removeClass("currentEditedCell");
    });
}

function submitInternalNodeChange() {
    var optionsDiv = $("div#chooseNodeDialog");

    var table = $("#nodesList");
    // Flag error if no value has been selected
    if ($("td.selected", table).length == 0) {
        $("span.error", optionsDiv).show();
        return false;
    }
    var selectedNodeId = $("td.selected", table).data("nodeId");
    var selectedNodeLabel = $("td.selected span", table).text();

    optionsDiv.dialog("close");

    // Remove the selection highlighting
    $("#currentLinksTable td.currentEditedCell").removeClass("currentEditedCell");

    var cellChanged = $(optionsDiv.data("currentEditedCell"));
    $("span", cellChanged).text(selectedNodeLabel);
    $(cellChanged).data("nodeId", selectedNodeId).addClass("valueChangedCell");
}

function populateNodesListFromServer(event) {
    var info = new Object();
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "GetInternalNodesListOfAlignmentCommand";
    info["alignmentId"] = $("div#currentLinksInternalNodeDialog").data("alignmentId");

    if ($(this).attr("id") == "chooseExistingNodes") {
        info["nodesRange"] = "existingTreeNodes";
    } else if ($(this).attr("id") == "chooseDomain") {
        info["nodesRange"] = "domainNodesOfProperty";
        info["property"] = "";
    } else {
        info["nodesRange"] = "allGraphNodes";
    }
    var currentSelectedNodeId = $($("div#chooseNodeDialog").data("currentEditedCell")).data("nodeId");

    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                $.each(json["elements"], function(index, element) {
                    if(element["updateType"] == "InternalNodesList") {
                        var table = $("#nodesList");
                        $("tr", table).remove();
                        $("div#chooseNodeDialog span.error").hide();

                        element["nodes"].sort(function(a,b) {
                            return a["nodeLabel"].toUpperCase().localeCompare(b["nodeLabel"].toUpperCase());
                        });

                        $.each(element["nodes"], function(index2, node) {
                            var trTag = $("<tr>");
                            var nodeTd = $("<td>").append($("<span>").text(node["nodeLabel"]))
                                                .data("nodeId", node["nodeId"])
                                    .click(function(){
                                        $("td", table).removeClass("selected");
                                        $(this).addClass("selected");
                                    });
                            if (nodeTd.data("nodeId") == currentSelectedNodeId) {
                                nodeTd.addClass("selected");
                            }

                            trTag.append(nodeTd).data("nodeLabel", node["nodeLabel"]);
                            table.append(trTag);
                        });
                    }
                });
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while getting nodes list!");
            }
    });
}

function populateLinksListFromServer() {
    // Remove existing links in the table
    var table = $("#linksList");
    $("tr", table).remove();
    $("div#chooseLinkDialog span.error").hide();

    // Prepare the request
    var info = new Object();
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "GetLinksOfAlignmentCommand";
    info["alignmentId"] = $("div#currentLinksInternalNodeDialog").data("alignmentId");

    if ($(this).attr("id") == "chooseExistingLinks") {
        info["linksRange"] = "existingLinks";
    } else if ($(this).attr("id") == "choosePropertyWithDomainAndRange") {
        info["linksRange"] = "linksWithDomainAndRange";
        info["domain"] = "";
        info["range"] = "";
    } else {
        info["linksRange"] = "allObjectProperties";
    }

    var currentSelectedLinkId = $($("div#chooseLinkDialog").data("currentEditedCell")).data("edgeId");

    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                $.each(json["elements"], function(index, element) {
                    if(element["updateType"] == "LinksList") {
                        // Sort the list
                        element["edges"].sort(function(a,b) {
                            return a["edgeLabel"].toUpperCase().localeCompare(b["edgeLabel"].toUpperCase());
                        });

                        $.each(element["edges"], function(index2, node) {
                            var trTag = $("<tr>");
                            var edgeTd = $("<td>").append($("<span>").text(node["edgeLabel"]))
                                .data("edgeId", node["edgeId"])
                                .addClass("visible")
                                .click(function(){
                                    $("td", table).removeClass("selected");
                                    $(this).addClass("selected");
                                });

                            if (edgeTd.data("edgeId") == currentSelectedLinkId) {
                                edgeTd.addClass("selected");
                            }

                            trTag.append(edgeTd).data("edgeLabel", node["edgeLabel"]);
                            table.append(trTag);
                        });
                    }
                });
            },
        error :
            function (xhr, textStatus) {
                alert("Error occurred while getting links list!");
            }
    });
}

function showClassPopupMenu(d, classObj, event) {
	var menu = $("div#modelingClassDropDownMenu");
    menu.data("nodeId", d.id);
    menu.data("nodeDomain", d.nodeDomain);
    menu.data("nodeLabel", d.label);
    menu.data("worksheetId", d.worksheetId);
    menu.data("alignmentId", d.alignmentId);         
    menu.css({"position":"absolute",
        "top":$(classObj).offset().top + 5,
        "left": event.clientX}).show(); // + $(this).width()/2 - $(menu).width()/2}).show();
}

function showIncomingOutgoingDialog(linkType) {
	var linkTitle;
	var menuDiv = $("#modelingClassDropDownMenu");
	
	console.log("Link type::" + linkType)
	if(linkType == "incoming") {
		linkTitle = "Add Incoming Link for " + $(menuDiv).data("nodeLabel");
		$("#incomingOutgoingLinksDirection").html("from");
	} else if(linkType == "outgoing") {
		linkTitle = "Add Outgoing Link for " + $(menuDiv).data("nodeLabel");
		$("#incomingOutgoingLinksDirection").html("to");
	} else if(linkType == "fromClass"){
		linkTitle = "Change from Class";
		$("#incomingOutgoingLinksDirection").html("from");
	} else if(linkType == "toClass") {
		linkTitle = "Change to Class";
		$("#incomingOutgoingLinksDirection").html("to");
	} else {
		linkTitle = linkType;
	}
	
	var optionsDiv = $("div#incomingOutgoingLinksDialog");
	
    optionsDiv.data("workspaceId", $.workspaceGlobalInformation.id);
    optionsDiv.data("nodeId",  $(menuDiv).data("nodeId"));
    optionsDiv.data("nodeDomain",  $(menuDiv).data("nodeDomain"));
    optionsDiv.data("alignmentId",  $(menuDiv).data("alignmentId"));
    optionsDiv.data("worksheetId", $(menuDiv).data("worksheetId"));
    optionsDiv.data("linkType", linkType);
    
    //Add the nodes in model followed by other nodes in incomingOutgoingLinksClassTable
    populateClassListFromServer(optionsDiv);
	
	//Add the compatible properties followed by all other properties in incomingOutgoingLinksPropertyTable
    populatePropertyListFromServer(optionsDiv);
    
	optionsDiv.dialog({width: 550, 
						height: 480, 
						position: [200, 200], 
						title: linkTitle,
						buttons: { 
								"Cancel": function() { $(this).dialog("close"); }, 
								"Submit":submitIncomingOutgoingLinksDialog }});
}


function submitIncomingOutgoingLinksDialog() {
	var dialog = $("div#incomingOutgoingLinksDialog");
	var startNode = dialog.data("nodeId");
	var linkType = dialog.data("linkType");
	
	var classDiv = $("#incomingOutgoingLinksClassData");
	var propertyDiv = $("#incomingOutgoingLinksPropertyData");
	
	 var info = new Object();
	 info["workspaceId"] = dialog.data("workspaceId");
	 info["command"] = "ChangeInternalNodeLinksCommand";

	 // Prepare the input for command
	 var newInfo = [];
	 
	// Put the old edge information
	var initialEdges = [];
	newInfo.push(getParamObject("initialEdges", initialEdges, "other"));
	    
	newInfo.push(getParamObject("alignmentId", dialog.data("alignmentId"), "other"));
	newInfo.push(getParamObject("worksheetId", dialog.data("worksheetId"), "worksheetId"));
	 
	 // Put the new edge information
	 var newEdges = [];
	 var newEdgeObj = {};
	 
	 var source, target;
	 var property = propertyDiv.data("id");
	    
	if(linkType == "incoming") {
		target = startNode;
		source = classDiv.data("id");
	} else if(linkType == "outgoing") {
		source = startNode;
		target = classDiv.data("id");
	} else {
		alert("Invalid linkType: " + linkType);
		return;
	}
	
	newEdgeObj["edgeSourceId"] = source;
    newEdgeObj["edgeTargetId"] = target;
    newEdgeObj["edgeId"] = property;
    newEdges.push(newEdgeObj);
    
	newInfo.push(getParamObject("newEdges", newEdges, "other"));
	info["newInfo"] = JSON.stringify(newInfo);
	info["newEdges"] = newEdges;
	
	showLoading(dialog.data("worksheetId"));
    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                parse(json);
                hideLoading(dialog.data("worksheetId"));
                dialog.dialog("close");
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while getting nodes list!");
                hideLoading(dialog.data("worksheetId"));
                dialog.dialog("close");
            }
    });
}

function populateClassListFromServer(dialog) {
	var info = new Object();
    info["workspaceId"] = dialog.data("workspaceId");
    info["command"] = "GetInternalNodesListOfAlignmentCommand";
    info["alignmentId"] = dialog.data("alignmentId");
    info["nodesRange"] = "existingTreeNodes"; //allGraphNodes";
    
    $("#incomingOutgoingLinksClassData").data("label", "");
    
    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                var nodeModel = parseInternalNodeList(json, true);
                
                $("div#incomingOutgoingLinksErrorWindowBox").hide();
                displayIncomingOutgoingClasses(nodeModel, $("#incomingOutgoingLinksClassDiv1"),
                		$("#incomingOutgoingLinksClassDiv2"), $("#incomingOutgoingLinksClassData"), dialog);
                
                
                //Now get all other nodes
                info["nodesRange"] = "allGraphNodes";
                $.ajax({
                	url: "RequestController",
                	type: "POST",
                	data: info,
                	dataType: "json",
                	complete: 
                		function(xhr, textStatus) {
                			var json = $.parseJSON(xhr.responseText);
                			var nodesInner = parseInternalNodeList(json, true);
                			var nodeAll = [];
                			$.each(nodesInner, function(index, node) {
                				//console.log(node + ":" + nodeModel[0] + ": " + $.inArray(node, nodeModel));
                				if($.inArray(node, nodeModel) == -1) {
	                                nodeAll.push(node);
                				}
                            });
                			displayIncomingOutgoingClasses(nodeAll, $("#incomingOutgoingLinksClassDiv2"), 
                					$("#incomingOutgoingLinksClassDiv1"), $("#incomingOutgoingLinksClassData"), dialog);
                		},
                	error:
                		function(xhr, textStatus) {
                			alert("Error occured while getting nodes list!");
                		}
                });
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while getting nodes list!");
            }
    });
}

function parseInternalNodeList(json, sortNodes) {
	var nodes = [];
	$.each(json["elements"], function(index, element) {
        if(element["updateType"] == "InternalNodesList") {
            if(sortNodes) {
	        	element["nodes"].sort(function(a,b) {
	                return a["nodeLabel"].toUpperCase().localeCompare(b["nodeLabel"].toUpperCase());
	            });
            }
            
            $.each(element["nodes"], function(index2, node) {
            	var nodeData = {data:node["nodeLabel"], metadata:{"uri": node["nodeUri"], "id" : node["nodeId"]}};
            	nodes.push(nodeData);
                
            });
        }
    });
	return nodes;
}


function displayIncomingOutgoingClasses(dataArray, treeDiv, otherTreeDiv, dataDiv, dialog) {
	if(dataArray.length == 0) {
        $(treeDiv).html("<i>none</i>");
    } else {
        $(treeDiv).jstree({
            "json_data" : {
                "data" : dataArray
            },
            "themes" : {
                "theme" : "proton",
                "url": "uiLibs/jquery/css/jstree-themes/proton/style.css",
                "dots" : true,
                "icons" : false
            },
            "search" : {
                "show_only_matches": true
            },
            "plugins" : [ "themes", "json_data", "ui", "search"]
        })
        	.bind("select_node.jstree", function (e, data) {
                dataDiv.data("label",data.rslt.obj.context.lastChild.wholeText);
                dataDiv.data("uri",data.rslt.obj.data("uri"));
                dataDiv.data("id", data.rslt.obj.data("id"))
                var a = $.jstree._focused().get_selected();
                $(otherTreeDiv).jstree("deselect_all");
                $(treeDiv).jstree("open_node", a);
                
                refreshPropertyListFromServer(data.rslt.obj.data("uri"), dialog);
            });

    }
}


function populatePropertyListFromServer(dialog) {
	var info = new Object();
    info["workspaceId"] = dialog.data("workspaceId");
    info["command"] = "GetDataPropertyHierarchyCommand";
    
    $("#incomingOutgoingLinksPropertyData").data("label", "");
    $("#incomingOutgoingLinksPropertyDiv1").html("<i>&nbsp;</i>");
    
    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                var nodeModel = parseDataPropertyList(json, true);
                
                $("div#incomingOutgoingLinksErrorWindowBox").hide();
                displayIncomingOutgoingProperty(nodeModel, $("#incomingOutgoingLinksPropertyDiv2"),
                		$("#incomingOutgoingLinksPropertyDiv1"), $("#incomingOutgoingLinksPropertyData"));
                
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while getting property list!");
            }
    });
}

function parseDataPropertyList(json, sortNodes) {
	var nodes = [];
	$.each(json["elements"], function(index, element) {
        if(element["updateType"] == "DataPropertyListUpdate" || element["updateType"] == "DataPropertiesForClassUpdate") {
            if(sortNodes) {
	        	element["data"].sort(function(a,b) {
	                return a["data"].toUpperCase().localeCompare(b["data"].toUpperCase());
	            });
            }
            
            $.each(element["data"], function(index2, node) {
            	nodes.push(node["data"]);
                
            });
        } else if(element["updateType"] == "LinksList") {
        	 if(sortNodes) {
 	        	element["edges"].sort(function(a,b) {
 	                return a["edgeLabel"].toUpperCase().localeCompare(b["edgeLabel"].toUpperCase());
 	            });
             }
             
             $.each(element["edges"], function(index2, node) {
            	 var nodeData = {data:node["edgeLabel"], metadata:{"id": node["edgeId"]}};
             	 nodes.push(nodeData);
                 
             });
        }
    });
	return nodes;
}

function refreshPropertyListFromServer(selectedClass, dialog) {
	//alert("Get compatibe properties for: " + selectedClass);
	var info = new Object();
    info["workspaceId"] = dialog.data("workspaceId");
    info["command"] = "GetLinksOfAlignmentCommand";
    info["alignmentId"] = dialog.data("alignmentId");
    var linkType = dialog.data("linkType");
    var startNodeClass = dialog.data("nodeDomain");
    
    info["linksRange"] = "linksWithDomainAndRange";
    if(linkType == "incoming") {
    	info["domain"] = selectedClass;
    	info["range"] = startNodeClass;
    } else if(linkType == "outgoing") {
    	info["domain"] = startNodeClass;
    	info["range"] = selectedClass;
    }
   // info["URI"] = selectedClass;
    
    //$("#incomingOutgoingLinksPropertyData").data("label", "");
    $("#incomingOutgoingLinksPropertyDiv1").html("<i>&nbsp;</i>");
    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                var nodeModel = parseDataPropertyList(json, true);
                
                $("div#incomingOutgoingLinksErrorWindowBox").hide();
                displayIncomingOutgoingProperty(nodeModel, $("#incomingOutgoingLinksPropertyDiv1"),
                		$("#incomingOutgoingLinksPropertyDiv2"), $("#incomingOutgoingLinksPropertyData"));
                
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while getting property list!");
            }
    });
}

function displayIncomingOutgoingProperty(dataArray, treeDiv, otherTreeDiv, dataDiv) {
	if(dataArray.length == 0) {
        $(treeDiv).html("<i>none</i>");
    } else {
        $(treeDiv).jstree({
            "json_data" : {
                "data" : dataArray
            },
            "themes" : {
                "theme" : "apple",
                "url": "uiLibs/jquery/css/jstree-themes/apple/style.css",
                "dots" : true,
                "icons" : false
            },
            "search" : {
                "show_only_matches": true
            },
            "plugins" : [ "themes", "json_data", "ui", "search"]
        })
        	.bind("select_node.jstree", function (e, data) {
                dataDiv.data("label",data.rslt.obj.context.lastChild.wholeText);
                dataDiv.data("id",data.rslt.obj.data("id"));
                var a = $.jstree._focused().get_selected();
                $(otherTreeDiv).jstree("deselect_all");
                $(treeDiv).jstree("open_node", a);
            });

    }
}

