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
		var savedNodesMap;
		var savedLinksMap;

		var linkApproveListeners;
		var nodeDragDropListeners;

		function init() {
			models = [];
			modelJsons = [];
			modelNodesMap = [];
			modelLinksMap = [];
			savedJsons = [];
			savedNodesMap = [];
			savedLinksMap = [];

			linkApproveListeners = [];
			nodeDragDropListeners = [];

			window.onscroll = function(event){
				for (var worksheetId in models) {
					if(models[worksheetId])
						models[worksheetId].onscroll(event);
				} 
			};
			
			window.onresize = function(event) {
				for (var worksheetId in models) {
					if(models[worksheetId])
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

		function getNodeRep(node) {
			return {"id": node.nodeId, 
					"uri": node.nodeDomain, 
					"label": node.label, 
					"rdfsLabel": node.rdfsLabel,
					"rdfsComment": node.rdfsComment,
					"type": node.nodeType};
		}

		function displayModel(json) {
			var worksheetId = json["worksheetId"];
			displayModelInternal(json);
			
			worksheetJson = modelJsons[worksheetId];
			worksheetNodes = modelNodesMap[worksheetId];
			worksheetLinks = modelLinksMap[worksheetId];

			savedJsons[worksheetId] = $.extend(true, {}, worksheetJson);
			savedLinksMap[worksheetId] = $.extend(true, {}, worksheetLinks);
			savedNodesMap[worksheetId] = $.extend(true, {}, worksheetNodes);
		}

		function deleteModel(worksheetId) {
			var mainWorksheetDiv = $("div#" + worksheetId);
			var layoutElement = "div#svgDiv_" + worksheetId;
			$(layoutElement).remove();
			models[worksheetId] = null;
		}

		function displayModelInternal(json) {
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
					else if(d.isTemporary)
						nodeCategory = "temporary";
					var id;
					if(d.nodeId)
						id = d.nodeId;
					else
						id = d.id;
					ClassDialog.getInstance().show(worksheetId, id, 
						d.label, d.rdfsLabel, d.rdfsComment,
						d.nodeDomain, d.nodeDomain, nodeCategory, 
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
				if(sourceObj == null) {
					sourceObj = {"nodeType": "Link", "label":d.source, "nodeDomain":d.source};
				}
				PropertyDialog.getInstance().show(
						worksheetId,
						alignmentId,
						d.id,
						d.linkUri,
						d.label,
						d.rdfsLabel,
						d.rdfsComment,
						d.isProvenance,
						d.linkStatus,
						d.sourceNodeId,
						sourceObj.nodeType,
						sourceObj.label,
						sourceObj.rdfsLabel,
						sourceObj.rdfsComment,
						sourceObj.nodeDomain,
						d.sourceNodeId,
						d.source.isUri,
						d.targetNodeId,
						targetObj.nodeType,
						targetObj.label,
						targetObj.rdfsLabel,
						targetObj.rdfsComment,
						targetObj.nodeDomain,
						d.targetNodeId,
						d.target.isUri,
						event);
			});
			
			layout.setAnchorMouseListener(function(d, event) {
				AnchorDropdownMenu.getInstance().show(worksheetId, d.nodeId, 
						d.label, d.rdfsLabel, d.rdfComment, 
						d.nodeDomain, d.nodeDomain, "", 
						alignmentId, d.nodeType, d.isUri, event);
			});

			if(linkApproveListeners[worksheetId]) {
				layout.setLinkApproveClickListener(function(d, event) {
					worksheetNodes = modelNodesMap[worksheetId];
					var func = linkApproveListeners[worksheetId];
					func(getLinkRep(d), event);
				});
					
			}

			if(nodeDragDropListeners[worksheetId]) {
				layout.setNodeDragDropListener(function(source, target, event) {
					var sourceNodeOrg = worksheetNodes[source.nodeId];
					var func = nodeDragDropListeners[worksheetId];
					if(target.nodeId) {
						var targetNodeOrg = worksheetNodes[target.nodeId];
						var targetRep = getNodeRep(targetNodeOrg);
					} else {
						var linkOrg = worksheetLinks[target.id];
						var targetRep = getLinkRep(linkOrg);
					}
					func(getNodeRep(sourceNodeOrg), targetRep, event);
				});
			}
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
			if(savedJsons[worksheetId]) {
				worksheetJson = savedJsons[worksheetId];
			} else {
				worksheetJson = modelJsons[worksheetId];
			}
			$.each(worksheetJson.nodes, function(index, node) {
				result.push(getNodeRep(node));
			});
			return result;
		}

		function getLinks(worksheetId) {
			if(savedJsons[worksheetId]) {
				worksheetJson = savedJsons[worksheetId];
				worksheetNodes = savedNodesMap[worksheetId];
			} else {
				worksheetJson = modelJsons[worksheetId];
				worksheetNodes = modelNodesMap[worksheetId];
			}

			var result = [];
			$.each(worksheetJson.links, function(index, link) {
				result.push(getLinkRep(link));
			});
			$.each(worksheetJson.edgeLinks, function(index, link) {
				result.push(getLinkRep(link));
			});
			return result;
		}

		function getLinkRep(link) {
			var sourceNodeOrg = worksheetNodes[link.sourceNodeId];
			var targetNodeOrg = worksheetNodes[link.targetNodeId]
			return {
				"id": link.id,
				"uri": link.linkUri,
				"label":link.label,
				"rdfsLabel": link.rdfsLabel,
				"rdfsComment": link.rdfsComment,
				"type":link.linkType,
				"source": getNodeRep(sourceNodeOrg),
				"target": getNodeRep(targetNodeOrg)
			};
		}

		function getCurrentLinksToNode(worksheetId, nodeId) {
			
			worksheetJson = modelJsons[worksheetId];
			worksheetNodes = modelNodesMap[worksheetId];

			var result = [];
			$.each(worksheetJson.links, function(index, link) {
				if(link.sourceNodeId == nodeId || link.targetNodeId == nodeId) {
					result.push(getLinkRep(link));
				}
			});
			$.each(worksheetJson.edgeLinks, function(index, link) {
				if(link.sourceNodeId == nodeId || link.targetNodeId == nodeId) {
					result.push(getLinkRep(link));
				}
			});
			return result;
		}

		function changeTemporaryLink(worksheetId, linkId, newPropertyUri, newPropertyLabel, newPropertyRdfsLabel, newPropertyRdfsComment) {
			worksheetJson = modelJsons[worksheetId];

			$.each(worksheetJson.links, function(index, link) {
				if(link.id == linkId) {
					link.linkUri = newPropertyUri;
					link.label = newPropertyLabel;
					link.rdfsLabel = newPropertyRdfsLabel;
					link.rdfsComment = newPropertyRdfsComment;
					link.id = link.sourceNodeId + "--" + newPropertyUri + "--" +
							link.targetNodeId
				}
			});

			displayModelInternal({"worksheetId": worksheetId, "alignObject": worksheetJson});
		}

		function addToModel(worksheetId, nodes, links, edgeLinks) {
			if(savedJsons[worksheetId]) {
				worksheetJson = $.extend(true, {}, savedJsons[worksheetId]);
				worksheetNodes = $.extend(true, {}, savedNodesMap[worksheetId]);
				worksheetLinks = $.extend(true, {}, savedLinksMap[worksheetId]);
			} else {
				worksheetJson = modelJsons[worksheetId];
				worksheetNodes = modelNodesMap[worksheetId];
				worksheetLinks = modelLinksMap[worksheetId];
			}
			
			wsMaxId = worksheetNodes["MAX_ID"];
			
			//1. Save the worksheetJson
			savedJsons[worksheetId] = $.extend(true, {}, worksheetJson);
			savedLinksMap[worksheetId] = $.extend(true, {}, worksheetLinks);
			savedNodesMap[worksheetId] = $.extend(true, {}, worksheetNodes);

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
						"rdfsLabel": node.rdfsLabel,
						"rdfsComment": node.rdfsComment,
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
					    "rdfsLabel": link.rdfsLabel,
					    "rdfsComment": link.rdfsComment,
					    "targetNodeId": link.target

					}
					worksheetJson.links.push(wsLink);
					worksheetLinks[link.id] = wsLink;
				}
			});

			displayModelInternal({"worksheetId": worksheetId, "alignObject": worksheetJson});
		}
		
		function restoreSavedModel(worksheetId) {
			if(savedJsons[worksheetId]) {
				worksheetJson = savedJsons[worksheetId];
				displayModelInternal({"worksheetId": worksheetId, "alignObject": worksheetJson});
				delete savedJsons[worksheetId];
			}
		}

		function setLinkApproveListener(worksheetId, listener) {
			linkApproveListeners[worksheetId] = listener;
		}

		function setNodeDragDropListener(worksheetId, listener) {
			nodeDragDropListeners[worksheetId] = listener;
		}

		return { //Return back the public methods
			init: init,
			getModelManager: getModelManager,
			displayModel: displayModel,
			refreshModel: refreshModel,
			deleteModel: deleteModel,
			printModel: printModel,
			addToModel: addToModel,
			getNodes: getNodes,
			getLinks: getLinks,
			getCurrentLinksToNode: getCurrentLinksToNode,
			changeTemporaryLink: changeTemporaryLink,
			restoreSavedModel: restoreSavedModel,
			setLinkApproveListener: setLinkApproveListener,
			setNodeDragDropListener: setNodeDragDropListener
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

