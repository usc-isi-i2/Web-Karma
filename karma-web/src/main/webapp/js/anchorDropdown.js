var AnchorDropdownMenu = (function() {

	var instance = null;


	function PrivateConstructor() {
		var menuId = "anchorDropdownMenu";
		var worksheetId, columnId;
		var columnUri, columnLabel, columnRdfsLabel, columnRdfsComment, columnDomain, columnCategory, columnType, alignmentId;
		var options = [
			["Suggest", suggest]
		];

		function init() {
			generateJS();
		}

		function hide() {
			$("#" + menuId).hide();
			$(document).off('click', hide);
		}

		function suggest() {
			if(columnType == "ColumnNode")
				suggestSemanticTypes();
			else
				suggestLinks();
		}

		function suggestSemanticTypes() {
			var semSuggestions = getSuggestedSemanticTypes(worksheetId, columnId);
			var links = [];

			if(semSuggestions != null && semSuggestions["Labels"]) {
				
				$.each(semSuggestions["Labels"], function(index, type) {
					if(type["DisplayLabel"] == "km-dev:columnSubClassOfLink" ||
							type["DisplayLabel"] == "km-dev:dataPropertyOfColumnLink" ||
							type["DisplayLabel"] == "km-dev:objectPropertySpecialization") {
						return;
					}

					links.push(
						{"uri": type["FullType"], 
						"label": type["DisplayLabel"], "rdfsLabel": type["DisplayRDFSLabel"],
						"source": {"uri": type["DomainUri"], "id": type["DomainId"], "label":type["DisplayDomainLabel"], "rdfsLabel":type["DomainRDFSLabel"]},
						"target": {"uri": columnDomain, "id": columnId, "label": columnLabel, "rdfsLabel": columnRdfsLabel}
						});
				});
			}

			addSuggestionedLinks(links);
		}

		function addSuggestionedLinks(suggestedLinks) {
			var items = [];
			var uriFound = false;
			var nodes = [];
			var links = [];
			var seenNodes = [];
			var seenLinks = [];

			if(suggestedLinks.length > 0) {
				var wsNodes = D3ModelManager.getInstance().getNodes(worksheetId);
				$.each(wsNodes, function(index, node) {
					seenNodes.push(node.id);
				});
				var wsLinks = D3ModelManager.getInstance().getLinks(worksheetId);
				$.each(wsLinks, function(index, link) {
					var linkTypeId;
					if(columnType == "ColumnNode") {
					 	linkTypeId = link.source.uri + "--" + link.target.id;
					 } else {
					 	linkTypeId = link.source.id + "--" + link.target.id;
					 }
					seenLinks.push(linkTypeId);
				});

				$.each(suggestedLinks, function(index, link) {
					console.log("Show type: " + JSON.stringify(link));
					var linkTypeId;
					if(columnType == "ColumnNode") {
					 	linkTypeId = link.source.uri + "--" + link.target.id;
					 } else {
					 	linkTypeId = link.source.id + "--" + link.target.id;
					 }

					if($.inArray(linkTypeId, seenLinks) == -1) {
						//Handle the source
						if(link.source.label.endsWith("(add)")) {
							link.source.label = link.source.label.substring(0, link.source.label.length-6);
						}
						link.source.label = getLabelWithoutPrefix(link.source.label);
						if($.inArray(link.source.id, seenNodes) == -1) {
							nodes.push({"id":link.source.id, "uri":link.source.uri, "label":link.source.label});
							seenNodes.push(link.source.id);
						}

						//Hanlde the target
						if(link.target.label.endsWith("(add)")) {
							link.target.label = link.target.label.substring(0, link.target.label.length-6);
						}
						link.target.label = getLabelWithoutPrefix(link.target.label);
						if($.inArray(link.target.id, seenNodes) == -1) {
							nodes.push({"id":link.target.id, "uri":link.target.uri, "label":link.target.label});
							seenNodes.push(link.target.id);
						}

						var linkId = link.source.id + "--" + link.uri + "--" + link.target.id;
						var linkLabel = link.label;
						idx = linkLabel.indexOf(":");
						if(idx != -1)
							linkLabel = linkLabel.substring(idx+1);
						links.push({"id":linkId, "source":link.source.id, "target":link.target.id, 
									"uri":link.uri, 
									"label":linkLabel, 
									"rdfsLabel": link.rdfsLabel,
									"type":"DataPropertyLink"});
						seenLinks.push(linkTypeId);
					}
				});
			}

			var blankNodeLinks = [];
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
						"rdfsLabel": defaultProperty.rdfsLabel,
						"type": "DataPropertyLink"});

			if(columnType != "ColumnNode") {
				var blankNode2 = {"id":"BlankNode2", "uri":"BlankNode", "label":" "};
				nodes.push(blankNode2);
				var defaultProperty = PropertyDialog.getInstance().getDefaultProperty();
				var linkId = columnId + "--" +  defaultProperty.label + "--" + blankNode2.id;
				links.push({"id": linkId, 
							"source": columnId, 
							"target": blankNode2.id, 
							"uri": defaultProperty.uri, 
							"label": getLabelWithoutPrefix(defaultProperty.label), 
							"rdfsLabel": defaultProperty.rdfsLabel,
							"type": "ObjectPropertyLink"});
			}

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
						"rdfsLabel": link.rdfsLabel,
						"source": link.source
					}
					setSemanticType(worksheetId, link.target.id, type);
				} else {
					var newEdges = [];
					var edge = {
						"uri": link.uri,
						"label": link.label,
						"rdfsLabel": link.rdfsLabel,
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
					var thisLink = D3ModelManager.getInstance().getCurrentLinksToNode(worksheetId, source.id)[0];
					
					if(columnType == "ColumnNode") {
						if(target.source) {
							//Target is a link
							setSpecializedEdgeSemanticType(worksheetId, columnId, target);
						} else {
							//Set the semantic type
							var type = {
								"uri": thisLink.uri,
								"label": thisLink.label,
								"rdfsLabel": thisLink.rdfsLabel,
								"source": target
							}
							setSemanticType(worksheetId, columnId, type);
						}
					} else {
						if(target.source) //Ignore if target is an link in other cases
							return;

						if(thisLink.source.uri == "BlankNode") {
							var newEdges = [];
							var edge = {
								"uri": thisLink.uri,
								"label": thisLink.label,
								"rdfsLabel": thisLink.rdfsLabel,
								"source": target,
								"target": {"id":columnId, "uri":columnUri, "label":columnLabel, "rdfsLabel": columnRdfsLabel, "type":columnType}
							}
							newEdges.push(edge);
							changeLinks(worksheetId, alignmentId, [], newEdges);
						} else {
							var newEdges = [];
							var edge = {
								"uri": thisLink.uri,
								"label": thisLink.label,
								"rdfsLabel": thisLink.rdfsLabel,
								"source": {"id":columnId, "uri":columnUri, "label":columnLabel, "rdfsLabel": columnRdfsLabel, "type":columnType},
								"target": target
							}
							newEdges.push(edge);
							changeLinks(worksheetId, alignmentId, [], newEdges);
						}
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

		function suggestLinks() {
			var suggestions = getSuggestedLinks(worksheetId, columnId);
			addSuggestionedLinks(suggestions.links);
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

		
		function show(p_worksheetId, p_columnId, p_columnLabel, p_columnRdfsLabel, p_columnRdfsComment,
				p_columnUri, p_columnDomain, p_columnCategory, 
				p_alignmentId, p_nodeType, p_isUri,
				event) {
			worksheetId = p_worksheetId;
			columnLabel = p_columnLabel;
			columnRdfsLabel = p_columnRdfsLabel;
			columnRdfsComment = p_columnRdfsComment;
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
				left: event.pageX - 40,
				top: event.pageY + 7
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