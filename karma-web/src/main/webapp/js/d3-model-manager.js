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
		var modelJsons;
		var modelNodesMap;
		var modelLinksMap;
		var savedJsons;

		function init() {
			models = [];
			modelJsons = [];
			modelNodesMap = [];
			modelLinksMap = [];
			savedJsons = [];

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
		
		function getModelManager(worksheetId, layoutElement, layoutClass, width, force) {
			if(models[worksheetId]) {
				//This is temporary. Comment this out when the D3ModelLayout can handle updates.
				if(force) {
					models[worksheetId] = new D3ModelLayout(layoutElement, layoutClass, width, worksheetId);
					modelJsons[worksheetId] = {};
				}
			} else { 
				models[worksheetId] = new D3ModelLayout(layoutElement, layoutClass, width, worksheetId);
				modelJsons[worksheetId] = {};
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
			var layout;
			try {
				layout = getModelManager(worksheetId, layoutElement, "col-sm-10", w);
				console.log(JSON.stringify(alignJson));
				modelJsons[worksheetId] = alignJson;
				wsModelNodesMap = [];
				wsModelLinksMap = [];
				maxId = -1;
				$.each(alignJson.anchors, function(index, node) {
					wsModelNodesMap[node.nodeId] = node;
					if(node.id > maxId) maxId = node.id;
				});
				$.each(alignJson.nodes, function(index, node) {
					wsModelNodesMap[node.nodeId] = node;
					if(node.id > maxId) maxId = node.id;
				});
				wsModelNodesMap["MAX_ID"] = maxId;
				$.each(alignJson.links, function(index, link) {
					wsModelLinksMap[link.id] = link;
				});
				$.each(alignJson.edgeLinks, function(index, link) {
					wsModelLinksMap[link.id] = link;
				});
				modelNodesMap[worksheetId] = wsModelNodesMap;
				modelLinksMap[worksheetId] = wsModelLinksMap;

				layout.generateLayoutForJson(alignJson);
			} catch(err) {
				console.log("Got exception in D3ModelLayout:" + err.message);
				console.log(err.stack);
				//Try again generating a new D3Layout
				$(layoutElement).empty();
				layout = getModelManager(worksheetId, layoutElement, "col-sm-10", w, true);
				console.log(JSON.stringify(alignJson));
				layout.generateLayoutForJson(alignJson);
			}
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
					ClassDropdownMenu.getInstance().show(worksheetId, id, d.label, d.nodeDomain, d.nodeDomain, nodeCategory, 
						alignmentId, d.nodeType, d.isUri, event);

					ClassSuggestDropdown.getInstance().show(worksheetId, id, d.label, d.nodeDomain, d.nodeDomain, nodeCategory, 
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
				if(d.linkStatus != "TemporaryLink") {
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
				}
				PropertySuggestDropdown.getInstance().show(
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
			
			layout.setAnchorMouseListener(function(d, event) {
				console.log("This function is called when the anchor has mouse over");
				console.log(d.nodeType);
				console.log(d.nodeId);
				console.log(d.id);
				
				AnchorDropdownMenu.getInstance().show(worksheetId, d.nodeId, d.label, d.nodeDomain, d.nodeDomain, "", 
						alignmentId, d.nodeType, d.isUri, event);
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
		
		function getNodes(worksheetId) {
			var result = [];
			$.each(modelJsons[worksheetId].nodes, function(index, node) {
				result.push({
					"id": node.nodeId,
					"uri": node.nodeDomain,
					"label": node.label
				})
			});
			return result;
		}

		function getLinks(worksheetId) {
			worksheetNodes = modelNodesMap[worksheetId];
			var result = [];
			$.each(modelJsons[worksheetId].links, function(index, link) {
				var sourceNodeOrg = worksheetNodes[link.sourceNodeId];
				var targetNodeOrg = worksheetNodes[link.targetNodeId]
				result.push({
					"id": link.id,
					"uri": link.linkUri,
					"source": link.sourceNodeId,
					"target": link.targetNodeId,
					"sourceNode": {"id":sourceNodeOrg.nodeId, "uri":sourceNodeOrg.nodeDomain, "label":sourceNodeOrg.label},
					"targetNode": {"id":targetNodeOrg.nodeId, "uri":targetNodeOrg.nodeDomain, "label":targetNodeOrg.label},
					"label":link.label,
					"type":link.linkType
				})
			});
			$.each(modelJsons[worksheetId].edgeLinks, function(index, link) {
				var sourceNodeOrg = worksheetNodes[link.sourceNodeId];
				var targetNodeOrg = worksheetNodes[link.targetNodeId]
				result.push({
					"id": link.id,
					"uri": link.linkUri,
					"source": link.sourceNodeId,
					"target": link.targetNodeId,
					"sourceNode": {"id":sourceNodeOrg.nodeId, "uri":sourceNodeOrg.nodeDomain, "label":sourceNodeOrg.label},
					"targetNode": {"id":targetNodeOrg.nodeId, "uri":targetNodeOrg.nodeDomain, "label":targetNodeOrg.label},
					"label":link.label,
					"type":link.linkType
				})
			});
			return result;
		}

		function addToModel(worksheetId, nodes, links, edgeLinks) {
			worksheetJson = modelJsons[worksheetId];
			worksheetNodes = modelNodesMap[worksheetId];
			worksheetLinks = modelLinksMap[worksheetId];
			wsMaxId = worksheetNodes["MAX_ID"];

			$.each(nodes, function(index, node) {
				var wsNode = worksheetNodes[node.id];
				if(wsNode) {
					//node found, do nothing
				} else {
					wsNode = {
						"id": wsMaxId+1,
						"isForcedByUser": false,
						"isTemporary": true,
						"nodeId": node.id,
						"nodeDomain": node.uri,
						"isUri": false,
						"label": node.label,
						"nodeType": "InternalNode"
					}
					worksheetNodes[node.id] = wsNode;
					worksheetJson.nodes.push(wsNode);
					wsMaxId += 1;
				}
			});

			$.each(links, function(index, link) {
				var wsLink = worksheetLinks[link.id];
				if(wsLink) {
					//Ignore, link is already there
				} else {
					wsLink = {
						"id": link.id,
						"linkUri": link.uri,
						"source": worksheetNodes[link.source].id,
					    "sourceNodeId": link.source,
					    "target": worksheetNodes[link.target].id,
					    "linkType": link.type,
					    "linkStatus": "TemporaryLink",
					    "label": link.label,
					    "targetNodeId": link.target

					}
					worksheetJson.links.push(wsLink);
					worksheetLinks[link.id] = wsLink;
				}
			});

			displayModel({"worksheetId": worksheetId, "alignObject": worksheetJson});
		}
		
		function saveModel(worksheetId) {
			worksheetJson = modelJsons[worksheetId];
			savedJsons[worksheetId] = $.extend(true, {}, worksheetJson);
		}

		function restoreSavedModel(worksheetId) {
			worksheetJson = savedJsons[worksheetId];
			displayModel({"worksheetId": worksheetId, "alignObject": worksheetJson});
		}

		return { //Return back the public methods
			init: init,
			getModelManager: getModelManager,
			displayModel: displayModel,
			refreshModel: refreshModel,
			printModel: printModel,
			addToModel: addToModel,
			getNodes: getNodes,
			getLinks: getLinks,
			saveModel: saveModel,
			restoreSavedModel: restoreSavedModel
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

