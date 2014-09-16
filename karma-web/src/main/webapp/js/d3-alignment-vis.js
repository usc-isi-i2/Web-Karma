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
	
	var alignJson = json.alignObject;
	$.each(alignJson.anchors, function(index, node) {
		var hNodeId = node["hNodeId"];
		var columnNode = $("td#" + hNodeId);
		var leftX = $(columnNode).offset().left - tableLeftOffset;
		node.posX = leftX;
	});
	
	var layout = new D3ModelLayout(layoutElement);
	layout.generateLayoutForJson(json.alignObject);
	$(mainWorksheetDiv).data("alignmentId", json["alignmentId"]);
	$(mainWorksheetDiv).data("svgVis", {"svgVis":true});
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
