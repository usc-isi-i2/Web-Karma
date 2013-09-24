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
    var vworksheetId = json["worksheetId"];
    var mainWorksheetDiv = $("div#"+vworksheetId);
    var tableLeftOffset = mainWorksheetDiv.offset().left;
    
    var w = 0;
    var levelHeight = 50;
    if($(mainWorksheetDiv).data("svgVis") != null) {
        w = $("div#svgDiv_"+vworksheetId).width();
        $("div#svgDiv_"+vworksheetId).remove();
    }
    
    $("<div>").attr("id","svgDiv_"+vworksheetId).addClass("svg-model").insertBefore('div#'+vworksheetId + " > div.table-container");
    
    var h = 0;
    // if(json["maxTreeHeight"] == 0)
        // h = levelHeight * (json["maxTreeHeight"] + 0.2);
    // else
        h = levelHeight * (json["maxTreeHeight"] + 0.4);
    if(w == 0)
        w = $("div#"+vworksheetId + "TableDiv").width();
    
    var svg = d3.select("div#svgDiv_"+vworksheetId).append("svg:svg")
        .attr("width", w)
        .attr("height", h);
        
    $(svg).data("alignmentId", json["alignmentId"]);
    $(svg).data("worksheetId", json["worksheetId"]);
    
    $(mainWorksheetDiv).data("svgVis", svg);
    $(mainWorksheetDiv).data("forceLayoutObject", force);
    
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

        // Add 18 to account for the padding in cells
        var width = extremeRightX - extremeLeftX + 18;
        node["width"] = width;
        node["y"] = h - ((node["height"] * levelHeight));
        if(node["nodeType"] == "ColumnNode" || node["nodeType"] == "Unassigned")
            node["y"] -= 5;
        if(node["nodeType"] == "FakeRoot")
            node["y"] += 15;
        node["x"] = extremeLeftX + width/2;
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
    
    // var dataPropertyOfColumnLinks = [];
    // $.each(json.links, function(i, item) {
    	// if (item.linkType == "DataPropertyOfColumnLink") {
    		// dataPropertyOfColumnLinks.push(item);
    	// }
    // });
//     
    // if (dataPropertyOfColumnLinks.length != 0) {
    	// var dataPropertyOfColumnSVGLinks = svg.selectAll("line.link")
        // .data(dataPropertyOfColumnLinks)
        // .enter().append("svg:line")
        // .attr("class", function(d) { return "link " + d.linkType; })
        // .attr("x1", function(d) { return d.source.x; })
        // .attr("y1", function(d) { return d.source.y; })
        // .attr("x2", function(d) { return d.target.x; })
        // .attr("y2", function(d) { return d.target.y; })
        // .attr("id", function(d) { return "line"+d.source.index+"_"+d.target.index; })
        // .attr("marker-end", function(d) {
            // if(d.target.nodeType == "ColumnNode") 
                // return "url(#marker-DataProperty)";
            // else
                // return "url(#marker-Class)";
        // });
    // }
        
    svg.selectAll("text")
        .data(json.links)
        .enter().append("text")
        .text(function(d) {
            return d.label;
        })
        .attr("class", function(d) {
            if(d.id != "FakeRootLink")
                return "LinkLabel "+vworksheetId + " " + d.linkStatus;
            else
                return "LinkLabel FakeRootLink "+vworksheetId;
        })
        .attr("x", function(d) {
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
                showLinksForInternalNode(d, svg, d3.event);
            }
        });
        
    node.insert("rect", "text")
        .attr("ry", 6)
        .attr("rx", 6)
        .attr("class", function(d){
            if(d.nodeType != "ColumnNode" && d.nodeType != "Unassigned" && d.nodeType != "FakeRoot")
                return vworksheetId;
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
                showLinksForInternalNode(d, svg, d3.event);
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
                menu.data("worksheetId", vworksheetId);
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
    d3.selectAll("text.LinkLabel." + vworksheetId)
        .sort(comparator)
        .each(function(d1,i1) {
            // console.log("^^^^^^^^^^^^^^^^^^^^^^^" + d1.label)
            var x1 = this.getBBox().x;
            var y1 = this.getBBox().y;
            var width1 = this.getBBox().width;
            var height1 = this.getBBox().height;
            
            var cur1 = $(this);
            d3.selectAll("rect." + vworksheetId).each(function(d2,i2){
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
    d3.selectAll("text.LinkLabel." + vworksheetId)
        .sort(comparator)
        .each(function(d1,i1) {
            var x1 = this.getBBox().x;
            var y1 = this.getBBox().y;
            var width1 = this.getBBox().width;
            var height1 = this.getBBox().height;
            var cur1 = $(this);
            // console.log("^^^^^^^^^^^^");
            d3.selectAll("text.LinkLabel." + vworksheetId)
               .sort(comparator)
               .each(function(d2,i2) {
                   var x2 = this.getBBox().x;
                   var y2 = this.getBBox().y;
                   var width2 = this.getBBox().width;
                   var height2 = this.getBBox().height;
                   if(d1.id != d2.id) {
                       if(y1 == y2) {
                           if(((x1 + width1) > x2) && (x2+width2>x1+width1)){
                               // console.log("Collision detected!");
                               // console.log(d1);
                               // console.log(d2);
                               // console.log("Existing: " + $(cur1).attr("y"));
                               // console.log("Flag: " + flag);
                               if(flag%2 == 0)
                                   $(cur1).attr("y", Number($(cur1).attr("y"))-8);
                               else
                                   $(cur1).attr("y", Number($(cur1).attr("y"))+5);
                               flag++;
                           }
                       }
                       if(x1+width1 < x2)
                            return false;
                   }
            });
        });
    
//    $("text.LinkLabel").qtip({content: {text: "Edit Relationship"}});
    // $("g.ColumnNode, g.Unassigned").qtip({content: {text: "Change Semantic Type"}});
//    $("g.InternalNode").qtip({content: {text: "Add Parent Relationship"}});
    
    link.attr("x1", function(d) {
        if (d.linkType == "horizontalDataPropertyLink") {
        	return d.source.x;
        }
        
        if(d.source.y > d.target.y)
            return d.source.x;
        else
            return d.target.x;
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
        if(d.source.y > d.target.y)
            return d.source.x;
        else
            return d.target.x; 
    })
    .attr("y2", function(d) { return d.target.y; });

    node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
    
    $(window).resize(function() {
         waitForFinalEvent(function(){
            displayAlignmentTree_ForceKarmaLayout(json);
         }, 500, vworksheetId);
    });
}

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
    newInfo.push(getParamObject("worksheetId", optionsDiv.data("worksheetId"), "other"));

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

function showChooseLinkDialog() {
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















