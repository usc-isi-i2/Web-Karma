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

var D3ModelManager = (function() {
	var instance = null;

	function PrivateConstructor() {
		var models;
		
		function init() {
			models = [];
			
			window.onscroll = function(event){
				for (var worksheetId in models) {
					models[worksheetId].onscroll(event);
				} 
			};
			
			window.onresize = function(event) {
				for (var worksheetId in models) {
					models[worksheetId].onresize(event);
				} 
			};
		}
		
		function getModelManager(worksheetId, layoutElement, layoutClass, width) {
			if(models[worksheetId]) {
				//This is temporary. Comment this out when the D3ModelLayout can handle updates.
				//models[worksheetId] = new D3ModelLayout(layoutElement, layoutClass, width, worksheetId);
			} else { 
				models[worksheetId] = new D3ModelLayout(layoutElement, layoutClass, width, worksheetId);
			}
			return models[worksheetId];	
		};

		function displayModel(json) {
			var worksheetId = json["worksheetId"];
			var mainWorksheetDiv = $("div#" + worksheetId);
			var wsVisible = mainWorksheetDiv.data("worksheetVisible");
			if (!wsVisible) {
				return;
			}
			
			var w = 0;
			var mainWorksheetDiv = $("div#" + worksheetId);
			var layoutElement = "div#svgDiv_" + worksheetId;
			
			if ($(mainWorksheetDiv).data("svgVis") != null) {
				//$(layoutElement).remove();
			} else {
				$("<div>").attr("id", "svgDiv_" + worksheetId).addClass("svg-model").insertBefore('div#' + worksheetId + " > div.table-container");
			}
			
			if (w == 0)
				w = $("div#" + worksheetId).width();

			
			var tableLeftOffset = mainWorksheetDiv.offset().left;
			
			var alignJson;
			var nodesMap;
			if(json.alignObject) {
				var maxX = 0;
				alignJson = json.alignObject;
				nodesMap = [];
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
				alignJson.width = maxX + 100;
				
				$.each(alignJson.nodes, function(index, node) {
					nodesMap[node["id"]] = node;
				});
				
				$.each(alignJson.links, function(index, link) {
					if(link.label == "classLink")
						link.label = "uri";
				});
			} else {
				alignJson = json;
			}
			var layout = getModelManager(worksheetId, layoutElement, "col-sm-10", w);
			console.log(JSON.stringify(alignJson));
			layout.generateLayoutForJson(alignJson);
			
			var alignmentId = json["alignmentId"];
			$(mainWorksheetDiv).data("alignmentId", alignmentId);
			$(mainWorksheetDiv).data("svgVis", {"svgVis":true});
			
			layout.setNodeClickListener(function(d, event) {
				console.log("This function is called when the node is clicked");
				if (d["nodeType"] == "InternalNode" || d["nodeType"] == "LiteralNode") {
					var nodeCategory = "";
					if (d.isForcedByUser)
						nodeCategory = "forcedAdded";
					var id;
					if(d.nodeId)
						id = d.nodeId;
					else
						id = d.id;
					ClassDropdownMenu.getInstance().show(worksheetId, id, d.label, id, d.nodeDomain, nodeCategory,
							alignmentId, d.nodeType, d.isUri, event);
				}

			});
			
			layout.setLinkClickListener(function(d, event) {
				console.log("This function is called when the link is clicked");
				var sourceObj, targetObj;
				if(nodesMap) {
					sourceObj = nodesMap[d.source];
					targetObj = nodesMap[d.target];
				} else {
					sourceObj = d.source;
					targetObj = d.target;
				}
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
						d.source.isUri,
						d.targetNodeId,
						targetObj.nodeType,
						targetObj.label,
						targetObj.nodeDomain,
						d.targetNodeId,
						d.target.isUri,
						event);
			});
		};
		
		function refreshModel(worksheetId) {
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
		
		function printModel(worksheetId) {
			var layout = getModelManager(worksheetId);
			if(layout.printExtented) {
				showLoading(worksheetId);
				layout.printExtented(3, function() {
					hideLoading(worksheetId);
				});
			} else {
				alert("The Model Manager does not support printing");
			}
		}
		
		return { //Return back the public methods
			init: init,
			getModelManager: getModelManager,
			displayModel: displayModel,
			refreshModel: refreshModel,
			printModel: printModel
		};
	};

	function getInstance() {
		if (!instance) {
			instance = new PrivateConstructor();
			instance.init();
		}
		return instance;
	}
	
	return {
		getInstance: getInstance
	};

})();

