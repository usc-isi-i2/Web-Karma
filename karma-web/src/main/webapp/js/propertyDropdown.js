var PropertyDropdownMenu = (function() {

	var instance = null;


	function PrivateConstructor() {
		var menuId = "propertyDropdownMenu";
		var worksheetId;
		var alignmentId;
		var propertyId;
		var propertyUri;
		var sourceNodeId, sourceLabel, sourceDomain, sourceId, sourceNodeType, sourceIsUri;
		var targetNodeId, targetLabel, targetDomain, targetId, targetNodeType, targetIsUri;

		var options = [
			//Title, function to call, needs file upload     
			// ,
			["Delete", deleteLink],
			["divider", null],
			["Change From", changeFrom],
			["Change To", changeTo],
			["Change Link", changeLink]
		];

		function init() {
			generateJS();
		}

		function hide() {
			$("#" + menuId).hide();
			$(document).off('click', hide);
		}

		function changeLink() {
			console.log("changeLink");
			if(targetNodeType == "ColumnNode") {
    			SetSemanticTypeDialog.getInstance().show(worksheetId, targetId, targetLabel);
    		} else {
				var dialog = IncomingOutgoingLinksDialog.getInstance();
				dialog.setSelectedFromClass(sourceId);
				dialog.setSelectedToClass(targetId);
				dialog.setSelectedProperty(propertyUri);
				dialog.show(worksheetId,
					targetNodeId, alignmentId,
					targetLabel, targetId, targetDomain, targetNodeType, targetIsUri,
					"changeLink", sourceNodeId, targetNodeId, propertyUri);
    		}
		};

		function deleteLink() {
			console.log("deleteLink");
			if (confirm("Are you sure you wish to delete the link?")) {
				var info;
				if(targetNodeType == "ColumnNode") {
					info = generateInfoObject(worksheetId, targetNodeId, "UnassignSemanticTypeCommand");
					info["newInfo"] = JSON.stringify(info['newInfo']);
				} else {
					info = generateInfoObject(worksheetId, "", "ChangeInternalNodeLinksCommand");
	
					// Prepare the input for command
					var newInfo = info['newInfo'];
	
					// Put the old edge information
					var initialEdges = [];
					var oldEdgeObj = {};
					oldEdgeObj["edgeSourceId"] = sourceNodeId;
					oldEdgeObj["edgeTargetId"] = targetNodeId;
					oldEdgeObj["edgeId"] = propertyUri;
					initialEdges.push(oldEdgeObj);
					newInfo.push(getParamObject("initialEdges", initialEdges, "other"));
					newInfo.push(getParamObject("alignmentId", alignmentId, "other"));
					var newEdges = [];
					newInfo.push(getParamObject("newEdges", newEdges, "other"));
					info["newInfo"] = JSON.stringify(newInfo);
					info["newEdges"] = newEdges;
				}

				showLoading(worksheetId);
				var returned = sendRequest(info, worksheetId);
			}
		}

		function changeFrom() {
			console.log("Change From");
			if(targetNodeType == "ColumnNode") {
    			SetSemanticTypeDialog.getInstance().show(worksheetId, targetId, targetLabel);
    		} else {
				var dialog = IncomingOutgoingLinksDialog.getInstance();
				dialog.setSelectedFromClass(sourceId);
				dialog.setSelectedProperty(propertyUri);
				dialog.show(worksheetId,
					targetNodeId, alignmentId,
					targetLabel, targetId, targetDomain, targetNodeType, targetIsUri,
					"changeIncoming", sourceNodeId, targetNodeId, propertyUri);
			}

		}

		function changeTo() {
			console.log("Change To");
			if(targetNodeType == "ColumnNode") {
				alert("Cannot change the link. You could delete it from the Delete menu option");
			} else {
				var dialog = IncomingOutgoingLinksDialog.getInstance();
				dialog.setSelectedToClass(targetId);
				dialog.setSelectedProperty(propertyUri);
				dialog.show(worksheetId,
					sourceNodeId, alignmentId,
					sourceLabel, sourceId, sourceDomain, sourceNodeType, sourceIsUri,
					"changeOutgoing", sourceNodeId, targetNodeId, propertyUri);
			}
		}

		function generateJS() {
			var ul = $("<ul>");
			ul.attr("role", "menu")
				.addClass("dropdown-menu")
				.css("display", "block")
				.css("position", "static")
				.css("margin-bottom", "5px");
			for (var i = 0; i < options.length; i++) {
				var option = options[i];
				var li = $("<li>");
				if (option[0] == "divider") {
					li.addClass("divider");
				} else {
					var a = $("<a>")
						.attr("href", "#")
						.attr("tabindex", "-1")
						.text(option[0])
						.click(option[1]);
					li.append(a);
				}
				ul.append(li);
			}

			var div = $("<div>")
				.attr("id", menuId)
				.addClass("dropdown")
				.addClass("clearfix")
				.addClass("contextMenu")
				.append(ul);

			var container = $("body div.container");
			container.append(div);
		}

		function disableItem(itemIdx) {
			var div = $("#" + menuId);
			var li = $("li:eq(" + itemIdx + ")", div);
			li.addClass("disabled");
		}


		function show(p_worksheetId, p_alignmentId, p_propertyId, p_propertyUri,
			p_sourceNodeId, p_sourceNodeType, p_sourceLabel, p_sourceDomain, p_sourceId, p_sourceIsUri,
			p_targetNodeId, p_targetNodeType, p_targetLabel, p_targetDomain, p_targetId, p_targetIsUri,
			event) {
			worksheetId = p_worksheetId;
			alignmentId = p_alignmentId;
			propertyId = p_propertyId;
			propertyUri = p_propertyUri;
			sourceNodeId = p_sourceNodeId;
			sourceLabel = p_sourceLabel;
			sourceDomain = p_sourceDomain;
			sourceId = p_sourceId;
			sourceIsUri = p_sourceIsUri;
			targetNodeId = p_targetNodeId;
			targetLabel = p_targetLabel;
			targetDomain = p_targetDomain;
			targetId = p_targetId;
			targetIsUri = p_targetIsUri;
			
			sourceNodeType = p_sourceNodeType;
			targetNodeType = p_targetNodeType;

			if (p_sourceNodeType == "ColumnNode" || p_sourceNodeType == "LiteralNode") {
				for (var i = 0; i < options.length; i++) {
					if (options[i][0] == "Change From") {
						disableItem(i);
						break;
					}
				}
			}

			if (p_targetNodeType == "ColumnNode" || p_targetNodeType == "LiteralNode") {
				for (var i = 0; i < options.length; i++) {
					if (options[i][0] == "Change To") {
						disableItem(i);
						break;
					}
				}
			}
			
			
			//console.log("Click for opening Menu");
			$("#" + menuId).css({
				display: "block",
				position: "absolute",
				left: event.pageX,
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