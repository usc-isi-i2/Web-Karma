var PropertyFunctions = (function() {

	var instance = null;


	function PrivateConstructor() {
		var menuId = "propertyFunctions";
		var parentId = "propertyDialog";

		var worksheetId;
		var alignmentId;
		var propertyId;
		var propertyUri;
		var sourceNodeId, sourceLabel, sourceDomain, sourceId, sourceNodeType, sourceIsUri;
		var targetNodeId, targetLabel, targetDomain, targetId, targetNodeType, targetIsUri;
		var hideFunction;

		var options = [
			//Title, function to call, needs file upload     
			// ,
			["Delete", deleteLink],
			// ["divider", null],
			["Change From", changeFrom],
			["Change To", changeTo]
		];

		function init() {
			generateJS();
		}

		function hide() {
			if(hideFunction)
				hideFunction();
		}

		function deleteLink(e) {
			console.log("deleteLink");
			if (confirm("Are you sure you wish to delete the link?")) {
				var info;
				hide();
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
			e.preventDefault();
		}

		function changeFrom(e) {
			console.log("Change From");
			hide();
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
			e.preventDefault();
		}

		function changeTo(e) {
			console.log("Change To");
			if(targetNodeType == "ColumnNode") {
				alert("Cannot change the link. You could delete it from the Delete menu option");
			} else {
				hide();
				var dialog = IncomingOutgoingLinksDialog.getInstance();
				dialog.setSelectedToClass(targetId);
				dialog.setSelectedProperty(propertyUri);
				dialog.show(worksheetId,
					sourceNodeId, alignmentId,
					sourceLabel, sourceId, sourceDomain, sourceNodeType, sourceIsUri,
					"changeOutgoing", sourceNodeId, targetNodeId, propertyUri);
			}
			e.preventDefault();
		}

		function generateJS() {
			var btnList = $("<div>").addClass("btn-group-vertical").css("display", "block");
			for (var i = 0; i < options.length; i++) {
				var option = options[i];

				var btn = $("<button>").addClass("btn").addClass("btn-default")
						.text(option[0])
						.click(option[1]);

				btnList.append(btn);

				if(i % 2 == 0)
					btn.addClass("list-even");
				else
					btn.addClass("list-odd");
				
			}

			var div = $("<div>")
				.attr("id", menuId)
				.append(btnList);

			var container = $("#" + parentId + "Functions");
			container.append(div);
		}

		function enableAllItems() {
			var btns = $("button", "#" + menuId);
			for(var i=0; i<btns.length; i++) {
				var btn = $(btns[i]);
				btn.removeClass("disabled");
				btn.prop("disabled", false);
			}
		}
		
		function disableItem(value) {
			var btns = $("button", "#" + menuId);
			for(var i=0; i<btns.length; i++) {
				var btn = $(btns[i]);
				if(btn.text() == value) {
					btn.addClass("disabled");
					btn.prop("disabled", true);
					break;
				}
			}
		}


		function show(p_worksheetId, p_alignmentId, p_propertyId, p_propertyUri,
			p_sourceNodeId, p_sourceNodeType, p_sourceLabel, p_sourceDomain, p_sourceId, p_sourceIsUri,
			p_targetNodeId, p_targetNodeType, p_targetLabel, p_targetDomain, p_targetId, p_targetIsUri,
			hideFunc, event) {
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
			hideFunction = hideFunc;

			enableAllItems();
			if (p_sourceNodeType == "ColumnNode" || p_sourceNodeType == "LiteralNode") {
				disableItem ("Change From");
			}

			if (p_targetNodeType == "ColumnNode" || p_targetNodeType == "LiteralNode") {
				disableItem("Change To");
			}
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