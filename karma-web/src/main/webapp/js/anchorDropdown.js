var AnchorDropdownMenu = (function() {

	var instance = null;


	function PrivateConstructor() {
		var menuId = "anchorDropdownMenu";
		var worksheetId, columnId;
		var columnUri, columnLabel, columnDomain, columnCategory, columnType, alignmentId;
		var options = [
			["Suggest", suggestSemanticTypes]
		];

		function init() {
			generateJS();
		}

		function hide() {
			$("#" + menuId).hide();
			$(document).off('click', hide);
		}

		function suggestSemanticTypes() {
			var semSuggestions = getSuggestedSemanticTypes(worksheetId, columnId);
			var items = [];
			var uriFound = false;
			var nodes = [];
			var links = [];
			var seenNodes = [];
			var seenLinks = [];

			if(semSuggestions != null && semSuggestions["Labels"]) {
				var wsNodes = D3ModelManager.getInstance().getNodes(worksheetId);
				$.each(wsNodes, function(index, node) {
					seenNodes.push(node.id);
				});
				var wsLinks = D3ModelManager.getInstance().getLinks(worksheetId);
				$.each(wsLinks, function(index, link) {
					var linkTypeId = link.source.uri;
					seenLinks.push(linkTypeId);
				});

				$.each(semSuggestions["Labels"], function(index, type) {
					if(type["DisplayLabel"] == "km-dev:columnSubClassOfLink" ||
							type["DisplayLabel"] == "km-dev:dataPropertyOfColumnLink" ||
							type["DisplayLabel"] == "km-dev:objectPropertySpecialization") {
						return;
					}
					console.log("Show type: " + JSON.stringify(type));
					if($.inArray(type["DomainId"], seenNodes) == -1) {
						var linkTypeId = type["DomainUri"];
						if($.inArray(linkTypeId, seenLinks) == -1) {
							nodeLabel = type["DisplayDomainLabel"];
							if(nodeLabel.endsWith("(add)")) {
								nodeLabel = nodeLabel.substring(0, nodeLabel.length-6);
							}
							nodeLabel = getLabelWithoutPrefix(nodeLabel);
							nodes.push({"id":type["DomainId"], "uri":type["DomainUri"], "label":nodeLabel});
							seenNodes.push(type["DomainId"]);
						
							var linkId = type["DomainId"] + "--" + type["FullType"] + "--" + columnId;
							var linkLabel = type["DisplayLabel"];
							idx = linkLabel.indexOf(":");
							if(idx != -1)
								linkLabel = linkLabel.substring(idx+1);
							links.push({"id":linkId, "source":type["DomainId"], "target":columnId, 
									"uri":type["FullType"], "label":linkLabel, "type":"DataPropertyLink"});
							seenLinks.push(linkTypeId);
						}
					}
				});
			}

			//Add the blank node and link
			var blankNode = {"id":"BlankNode1", "uri":"BlankNode", "label":" "};
			nodes.push(blankNode);
			var defaultProperty = PropertyDialog.getInstance().getDefaultProperty();
			var linkId = blankNode.id + "--" + defaultProperty.label + "--" + columnId;
			links.push({"id": linkId, 
						"source": blankNode.id, 
						"target": columnId, 
						"uri": defaultProperty.uri, 
						"label": getLabelWithoutPrefix(defaultProperty.label), 
						"type": "DataPropertyLink"});

			//Register the link approve listener	
			D3ModelManager.getInstance().setLinkApproveListener(worksheetId, function(link, event) {
				console.log("This function is called when a link is approved");
				if(link.source.uri == "BlankNode") {
					//event.stopPropogation();
					alert("Please select the Class Node");
					return;
				}
				if(link.target.type == "ColumnNode") {
					//Set the semantic type
					var type = {
						"uri": link.uri,
						"label": link.label,
						"source": link.source
					}
					setSemanticType(worksheetId, link.target.id, type);
				} else {
					var newEdges = [];
					var edge = {
						"uri": link.uri,
						"label": link.label,
						"source": link.source,
						"target": link.target
					}
					newEdges.push(edge);
					changeLinks(worksheetId, alignmentId, [], newEdges);
				}
			});

			//Register the blank node listener
			D3ModelManager.getInstance().setNodeDragDropListener(worksheetId, function(source, target, event) {
				if(source.uri == "BlankNode") {
					if(columnType == "ColumnNode") {
						if(target.source) {
							//Target is a link
							setSpecializedEdgeSemanticType(worksheetId, columnId, target);
						} else {
							//Set the semantic type
							var type = {
								"uri": defaultProperty.uri,
								"label": defaultProperty.label,
								"source": target
							}
							setSemanticType(worksheetId, columnId, type);
						}
					} else {
						if(target.source) //Ignore if target is an link in other cases
							return;

						var newEdges = [];
						var edge = {
							"uri": defaultProperty.uri,
							"label": defaultProperty.label,
							"source": target,
							"target": {"id":columnId, "uri":columnUri, "label":columnLabel, "type":columnType}
						}
						newEdges.push(edge);
						changeLinks(worksheetId, alignmentId, [], newEdges);
					}
				}
			});

			
			D3ModelManager.getInstance().addToModel(worksheetId, nodes, links, [], true);
			// $(document).off('click', restoreD3Model);
			// window.setTimeout(function() {
			// 	$(document).on('click', restoreD3Model);
			// }, 10);
			window.setTimeout(function() {
				$(document).on('keydown', function(e) {
					if (e.keyCode == 27) {
				        restoreD3Model();
				    }
				});
			}, 10)
		}

		function restoreD3Model(event) {
			// console.log("CLICK ON:" + event.target);
			// console.log("CLICK ON:" + $(event.target).attr("id"));

			if(event && ($(event.target).attr("id") == "div#svgDiv_" + worksheetId ||
				$(event.target.farthestViewportElement).attr("id") == "div#svgDiv_" + worksheetId)) {
				return;
			} else {
				D3ModelManager.getInstance().restoreSavedModel(worksheetId);
				$(document).off('click', restoreD3Model);
			}
		}

		function generateJS() {
			var btnArr = [];
			for (var i = 0; i < options.length; i++) {
				var option = options[i];
				if (option[0] == "divider") {
					continue;
				}
				var btn = $("<button>")
								.addClass("btn").addClass("btn-default")
								.text(option[0])
								.click(option[1]);
				btnArr.push(btn);
			}
			var div = $("<div>")
						.attr("id", menuId)
						.addClass("btn-group")
						.attr("role", "group")
						.append(btnArr);

			var container = $("body div.container");
			container.append(div);
		}

		
		function show(p_worksheetId, p_columnId, p_columnLabel, p_columnUri, p_columnDomain, p_columnCategory, 
				p_alignmentId, p_nodeType, p_isUri,
				event) {
			worksheetId = p_worksheetId;
			columnLabel = p_columnLabel;
			columnId = p_columnId;
			columnUri = p_columnUri;
			columnDomain = p_columnDomain;
			columnCategory = p_columnCategory;
			alignmentId = p_alignmentId;
			columnType = p_nodeType;

			//console.log("Click for opening Menu");
			$("#" + menuId).css({
				display: "block",
				position: "absolute",
				left: event.pageX - 30,
				top: event.pageY
			});

			window.setTimeout(function() {
				$(document).on('click', hide);
			}, 10);
		};


		return { //Return back the public methods
			show: show,
			init: init
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