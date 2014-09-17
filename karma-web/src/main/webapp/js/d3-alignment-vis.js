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
	var mainWorksheetDiv = $("div#" + worksheetId);
	var wsVisible = mainWorksheetDiv.data("worksheetVisible");
	if (!wsVisible) {
		return;
	}
	var optionsDiv = $("div#WorksheetOptionsDiv", mainWorksheetDiv);
	var viewStraightLineModel = optionsDiv.data("viewStraightLineModel");

	console.log("displayAlignmentTree_ForceKarmaLayout:viewStraightLineModel:" + viewStraightLineModel);

	var w = 0;
	var mainWorksheetDiv = $("div#" + worksheetId);
	var layoutElement = "div#svgDiv_" + worksheetId;
	
	if ($(mainWorksheetDiv).data("svgVis") != null) {
		//w = $("div#svgDiv_"+worksheetId).width();
		$(layoutElement).remove();
	}

	$("<div>").attr("id", "svgDiv_" + worksheetId).addClass("svg-model").insertBefore('div#' + worksheetId + " > div.table-container");
	if (w == 0)
		w = $("div#" + worksheetId).width();

	
	var tableLeftOffset = mainWorksheetDiv.offset().left;
	
	var maxX = 0;
	var alignJson = json.alignObject;
	var nodesMap = [];
	$.each(alignJson.anchors, function(index, node) {
		var hNodeId = node["hNodeId"];
		var columnNode = $("td#" + hNodeId);
		var leftX = $(columnNode).offset().left - tableLeftOffset;
		node.xPos = leftX + ($(columnNode).width()/2);
		var nodeMax = leftX + $(columnNode).width();
		if(nodeMax > maxX)
			maxX = nodeMax;
		nodesMap[node["id"]] = node;
	});
	alignJson.width = maxX;
	
	$.each(alignJson.nodes, function(index, node) {
		nodesMap[node["id"]] = node;
	});
	
	$.each(alignJson.links, function(index, link) {
		if(link.label == "classLink")
			link.label = "uri";
	});
	
	var layout = new D3ModelLayout(layoutElement);
	layout.generateLayoutForJson(alignJson);
	
	var alignmentId = json["alignmentId"];
	$(mainWorksheetDiv).data("alignmentId", alignmentId);
	$(mainWorksheetDiv).data("svgVis", {"svgVis":true});
	
	layout.setNodeClickListener(function(node, event) {
		console.log("This function is called when the node is clicked");
		var d = node.node.original;
		if (d["nodeType"] == "InternalNode" || d["nodeType"] == "LiteralNode") {
			var nodeCategory = "";
			if (d.isForcedByUser)
				nodeCategory = "forcedAdded";
			ClassDropdownMenu.getInstance().show(worksheetId, d.nodeId, d.label, d.nodeId, d.nodeDomain, nodeCategory,
					alignmentId, event);
		}
	});
	
	layout.setLinkClickListener(function(link, event) {
		console.log("This function is called when the link is clicked");
		var d = link.node.original;
		var sourceObj = nodesMap[d.source];
		var targetObj = nodesMap[d.target];
		PropertyDropdownMenu.getInstance().show(
				worksheetId,
				alignmentId,
				d.id,
				d.linkUri,
				d.sourceNodeId,
				sourceObj.nodeType,
				sourceObj.label,
				sourceObj.nodeDomain,
				d.sourceNodeId,
				d.targetNodeId,
				targetObj.nodeType,
				targetObj.label,
				targetObj.nodeDomain,
				d.targetNodeId,
				event);
	});
}

var refreshAlignmentTree = function(worksheetId) {
	var mainWorksheetDiv = $("div#" + worksheetId);
	var svg = $(mainWorksheetDiv).data("svgVis");
	if (svg) {
		var alignmentId = $(mainWorksheetDiv).data("alignmentId");
		console.log("RefreshSVGAlignmentCommand: " + worksheetId, alignmentId);
		var info = generateInfoObject(worksheetId, "", "RefreshSVGAlignmentCommand");
		info["alignmentId"] = alignmentId;
		showLoading(worksheetId);
		sendRequest(info, worksheetId);
	}
};
