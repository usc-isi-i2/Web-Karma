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

function displayAlignmentTree_ForceKarmaLayout(json) {
    var vworksheetId = json["worksheetId"];
    var showHideDiv = $("div#showHideSpace_"+vworksheetId);
    var tableLeftOffset = $("table#"+vworksheetId).offset().left;
    
    var w = 0;
    var levelHeight = 50;
    if($(showHideDiv).data("svgVis") != null) {
        w = $("div#svgDiv_"+vworksheetId).width();
        $("div#svgDiv_"+vworksheetId).remove();
    }
    
    $("<div>").attr("id","svgDiv_"+vworksheetId).insertBefore('table#'+vworksheetId);
    
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
    
    $(showHideDiv).data("svgVis", svg);
    $(showHideDiv).data("forceLayoutObject", force);
    
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
        
        var width = extremeRightX - extremeLeftX;
        node["width"] = width;
        node["y"] = h - ((node["height"] * levelHeight));
        if(node["nodeType"] == "DataProperty" || node["nodeType"] == "Unassigned")
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
        .attr("class", "link")
        .attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return d.target.y; })
        .attr("id", function(d) { return "line"+d.source.index+"_"+d.target.index; })
        .attr("marker-end", function(d) {
            if(d.target.nodeType == "DataProperty") 
                return "url(#marker-DataProperty)";
            else
                return "url(#marker-Class)";
        });
        
    svg.selectAll("text")
        .data(json.links)
        .enter().append("text")
        .text(function(d) {
            return d.label;
        })
        .attr("class", function(d) {
            if(d.id != "FakeRootLink")
                return "LinkLabel "+vworksheetId;
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
            if(d.target.nodeType == "DataProperty")
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
            showAlternativeParents_d3(d, svg, d3.event);
        }).on("mouseover", function(d){
            d3.selectAll("g.Class").each(function(d2,i) {
                if(d2 == d.source) {
                    var newRect = $(this).clone();
                    newRect.attr("class","Class highlightOverlay");
                    $("svg").append(newRect);
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
            if(d.nodeType == "DataProperty" || d.nodeType == "Unassigned" || d.nodeType == "FakeRoot")
                return "";
            else 
                return d.label; })
        .attr("width", function(d) {
            var newText = $(this).text();
            if(this.getComputedTextLength() > d["width"]) {
                if(d.nodeType == "DataProperty" || d.nodeType == "Unassigned" || d.nodeType == "FakeRoot")
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
            if(d["nodeType"] == "Class") {
                d["targetNodeId"] = d["id"];
                showAlternativeParents_d3(d, svg, d3.event);
            }
        });
        
    node.insert("rect", "text")
        .attr("ry", 6)
        .attr("rx", 6)
        .attr("class", function(d){
            if(d.nodeType != "DataProperty" && d.nodeType != "Unassigned" && d.nodeType != "FakeRoot")
                return vworksheetId;
        })
        .attr("y", function(d){
            if(d.nodeType == "DataProperty" || d.nodeType == "Unassigned" || d.nodeType == "FakeRoot") {
                return -2;
            } else
                return -10;
        })
        .attr("height", function(d){
            if(d.nodeType == "DataProperty" || d.nodeType == "Unassigned" || d.nodeType == "FakeRoot")
                return 6;
            else
                return 20;  
        })
        .attr("width", function(d) {
            if(d.nodeType == "DataProperty" || d.nodeType == "Unassigned" || d.nodeType == "FakeRoot")
                return 6;
            else
                return d["width"];
        }).attr("x", function(d){
            if(d.nodeType == "DataProperty" || d.nodeType == "Unassigned" || d.nodeType == "FakeRoot") {
                return -3;
            } else
                return d.width/2 * -1;
        }).on("click", function(d){
            if(d["nodeType"] == "DataProperty" || d.nodeType == "Unassigned")
                changeSemanticType_d3(d, svg, d3.event);
            else if(d["nodeType"] == "Class") {
                d["targetNodeId"] = d["id"];
                showAlternativeParents_d3(d, svg, d3.event);
            }
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
                
                // Check if they overlap on y axis
                if((y2<y1 && y1<y2+height2 ) || (y1<y2 && y2<y1+height1 && y1+height1<y2+height2)) {
                    // console.log("Collision detected on Y axis");
                    // Check overlap on X axis
                    if(x2<x1 && x2+width2>x1) {
                        // console.log("Rect: " + x2 + " " + y2 + " " + width2 + " " + height2);
                        // console.log("Text: " + x1 + " " + y1 + " " + width1 + " " + height1);
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
    
    $("text.LinkLabel").qtip({content: {text: "Edit Relationship"}});
    $("g.DataProperty, g.Unassigned").qtip({content: {text: "Change Semantic Type"}});
    $("g.Class").qtip({content: {text: "Add Parent Relationship"}});
    
    link.attr("x1", function(d) {
        if(d.source.y > d.target.y)
            return d.source.x;
        else
            return d.target.x;
    })
    .attr("y1", function(d) { return d.source.y; })
    .attr("x2", function(d) {
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
    $("table#currentSemanticTypesTable tr.semTypeRow",optionsDiv).remove();
    $("table#currentSemanticTypesTable tr.editRow",optionsDiv).remove();
    $("input#chooseClassKey").attr("checked", false);
    $("div#SemanticTypeErrorWindow").hide();
    $(optionsDiv).removeData("selectedPrimaryRow");
    
    // Store a copy of the existing types.
    // This is tha JSON array which is changed when the user adds/changes through GUI and is submitted to the server.
    var existingTypes = typeJsonObject["SemanticTypesArray"];
    var existingTypesCopy = jQuery.extend(true, [], existingTypes);
    optionsDiv.data("existingTypes", existingTypesCopy);

    var CRFInfo = typeJsonObject["FullCRFModel"];
    
    // Populate the table with existing types and CRF suggested types
    $.each(existingTypes, function(index, type){
        addSemTypeObjectToCurrentTable(type, true, false);
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
    info["vWorksheetId"] = optionsDiv.data("worksheetId");
    var returned = $.ajax({
        url: "/RequestController", 
        type: "POST",
        data : info,
        dataType : "json",
        complete : 
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                optionsDiv.data("classAndPropertyListJson", json);
                
                // Special case when no training has been done to CRF model
                // Shows an empty semantic type
                if((!CRFInfo && existingTypes.length == 0) || 
                    ((existingTypes && existingTypes.length == 0) && (CRFInfo && CRFInfo.length == 0))) {
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
    var td = $(this).parents("td");
    var columnName = $("div.ColumnHeadingNameDiv", td).text();
    
    // Show the dialog box
    var positionArray = [event.clientX+20, event.clientY+10];
    optionsDiv.dialog({width: 350, position: positionArray, title:columnName
        , buttons: { 
            "Cancel": function() { $(this).dialog("close"); }, 
            "Submit":submitSemanticTypeChange }
    });
}


function showAlternativeParents_d3(d, vis, event) {
    var info = new Object();
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["nodeId"] = d["targetNodeId"];
    info["command"] = "GetAlternativeLinksCommand";
    info["alignmentId"] = $(vis).data("alignmentId");
    info["worksheetId"] = $(vis).data("worksheetId");
        
    var returned = $.ajax({
        url: "/RequestController", 
        type: "POST",
        data : info,
        dataType : "json",
        complete : 
            function (xhr, textStatus) {
                // alert(xhr.responseText);
                var json = $.parseJSON(xhr.responseText);
                $.each(json["elements"], function(index, element) {
                    if(element["updateType"] == "GetAlternativeLinks") {
                        var optionsDiv = $("div#OntologyAlternativeLinksPanel");
                        var table = $("table", optionsDiv);
                        $("tr", table).remove();
                        var positionArray = [event.clientX+20       // distance from left
                                    , event.clientY+10];    // distance from top
                        
                        // Sort the edges by class names
                        if(element["Edges"] && element["Edges"].length != 0) {
                            element["Edges"].sort(function(a,b){
                                var aName = a.edgeSource.toLowerCase();
                                var bName = b.edgeSource.toLowerCase();
                                
                                if(aName == bName) {
                                    var aEdge = a.edgeLabel.toLowerCase();
                                    var bEdge = b.edgeLabel.toLowerCase();
                                    return ((aEdge < bEdge) ? -1 : ((aEdge > bEdge) ? 1 : 0));
                                } else
                                    return ((aName < bName) ? -1 : ((aName > bName) ? 1 : 0));   
                            });
                        }
                        
                        $.each(element["Edges"], function(index2, edge) {
                            var trTag = $("<tr>").addClass("AlternativeLink");
                            
                            var radioButton = $("<input>")
                                .attr("type", "radio")
                                .attr("id", edge["edgeId"])
                                .attr("name", "AlternativeLinksGroup")
                                .attr("value", edge["edgeId"])
                                .val(edge["edgeLabel"])
                                .data("isDuplicate", false);
                                
                            var typeItalicSpan = $("<span>").addClass("italic").text(edge["edgeLabel"]);    
                            var linkLabel = $("<label>").attr("for",edge["edgeId"]).text(edge["edgeSource"] + " ").append(typeItalicSpan);
                            var linkLabelTd = $("<td>").append(linkLabel); 
                            
                            trTag.append($("<td>").append(radioButton))
                                .append(linkLabelTd);
                                
                            if(edge["selected"]) {
                                radioButton.attr("checked", true);
                                // Add the Duplicate button
                                var dupButton = $("<button>").addClass("duplicateClass").text("Duplicate").click(duplicateLink);
                                $(dupButton).button();
                                linkLabelTd.append(dupButton);
                            }
                                
                            table.append(trTag);
                        });
                        // Show the dialog box
                        optionsDiv.dialog({width: 300, height: 300, position: positionArray
                            , buttons: { "Cancel": function() { $(this).dialog("close"); }, "Submit":submitAlignmentLinkChange }});
                            
                        $("input:radio[@name='AlternativeLinksGroup']").change(function(){
                            if($(this).data("isDuplicate"))
                                optionsDiv.data("currentSelection", $(this).data("edgeId"));
                            else
                                optionsDiv.data("currentSelection", $(this).attr("id"));
                                
                            // Remove the button from the previously selected radio button and add it to the current one
                            var buttonClone = $("button", optionsDiv).clone(true);
                            $("button", optionsDiv).remove();
                            $("td:eq(1)",$(this).parents("tr")).append(buttonClone);
                                
                            optionsDiv.data("alignmentId", info["alignmentId"]);
                            optionsDiv.data("worksheetId", info["worksheetId"]);
                            optionsDiv.data("isDuplicate", $(this).data("isDuplicate"));
                        });
                    }
                });
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while getting alternative links!" + textStatus);
            }          
    });
}





















