var PropertyDialog = (function() {

	var instance = null;


	function PrivateConstructor() {
		var dialog = $("#propertyDialog");
		var menuId = "propertyFunctions";
		var rightDiv;

		var worksheetId;
		var alignmentId;
		var propertyId, propertyUri, propertyLabel;
		var sourceNodeId, sourceLabel, sourceDomain, sourceId, sourceNodeType, sourceIsUri;
		var targetNodeId, targetLabel, targetDomain, targetId, targetNodeType, targetIsUri;
		var propertyTabs;

		var options = [
			//Title, function to call, needs file upload     
			// ,
			["Change Link", changeLinkUI],
			["Advance Options", advanceOptionsUI],
			["Delete", deleteLink],
			// ["divider", null],
			["Change From", changeFromUI],
			["Change To", changeToUI]
		];

		function init() {
			propertyTabs = PropertyTabs.getInstance();
			reloadCache();
			generateJS();
			rightDiv = $("#propertyDialogRight");
		}

		function reloadCache() {
			propertyTabs.reloadCache();
		}

		function getDefaultProperty() {
			return propertyTabs.getDefaultProperty();
		}

		function hide() {
			propertyTabs.hide();
			dialog.modal('hide');
		}

		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError(err) {
			if (err) {
				$("div.error", dialog).text(err);
			}
			$("div.error", dialog).show();
		}

		function changeLinkUI() {
			initRightDiv("Change Link");
			propertyTabs.show(worksheetId, alignmentId, propertyId, propertyUri, 
													sourceNodeId, sourceNodeType, sourceLabel, sourceDomain, sourceId, sourceIsUri,
													targetNodeId, targetNodeType, targetLabel, targetDomain, targetId, targetIsUri,
													rightDiv, selectPropertyFromMenu,
													event);
		}

		function initRightDiv(mode) {
			showFunctionsMenu();
			disableItem(mode);
			setTitle(mode);
			var firstChild = rightDiv.children();
			firstChild.hide();
			$("body").append(firstChild);
			rightDiv.empty();
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

		function advanceOptionsUI(e) {
			initRightDiv("Advance Options");
			var tdTag = $("td#" + targetNodeId);
			var typeJsonObject = $(tdTag).data("typesJsonObject");
			if (typeJsonObject) {
				existingTypes = typeJsonObject["SemanticTypesArray"];
			} else {
				existingTypes = [];
			}
			var isSubClass = false;
			var rdfLiteralType = "";
			$.each(existingTypes, function(index, type) {
				if (type["DisplayLabel"] == "km-dev:columnSubClassOfLink") {
					isSubClass = true;
					rdfLiteralType = type["rdfLiteralType"];
				} else if(type["rdfLiteralType"]){
					rdfLiteralType = type["rdfLiteralType"];
				}
			});

			PropertyAdvanceOptionsDialog.getInstance().show(rdfLiteralType, isSubClass, rightDiv, saveAdvanceOptions);
		}

		function saveAdvanceOptions(literalType, isSubclassOfClass) {
			if(isSubclassOfClass) {
				setSubClassSemanticType(worksheetId, targetId, {"uri": sourceDomain, "id": sourceId, "label":sourceLabel}, literalType);
			} else {
				var type = {};
				type.label = propertyLabel;
				type.uri = propertyUri;
				type.source = {"uri": sourceDomain, "label": sourceLabel, "id": sourceId};
				type.target = {"uri": targetDomain, "label": targetLabel, "id": targetId};
				setSemanticType(worksheetId, targetId, type, literalType);
			}
			hide();
		}

		function changeFromUI(e) {
			initRightDiv("Change From");

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

		function changeToUI(e) {
			initRightDiv("Change To");

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

		function changeLink(label, uri) {
			oldEdges = [];
			var oldEdgeObj = {};
			oldEdgeObj["source"] = {"id": sourceId, "uri":sourceDomain, "label": sourceLabel};
			oldEdgeObj["target"] = {"id": targetId, "uri":targetDomain, "label": targetLabel};
			oldEdgeObj["uri"] = propertyUri;
			oldEdges.push(oldEdgeObj);

			// Put the new edge information
			var newEdges = [];
			var newEdgeObj = {};
			newEdgeObj["source"] = {"id": sourceId, "uri":sourceDomain, "label": sourceLabel};
			newEdgeObj["target"] = {"id": targetId, "uri":targetDomain, "label": targetLabel};
			newEdgeObj["uri"] = uri
			newEdges.push(newEdgeObj);

			changeLinks(worksheetId, alignmentId, oldEdges, newEdges);
			hide();
		}

		function changeSemanticType(label, uri) {
			var type = {
				"label": label,
				"uri": uri,
				"source": {"id": sourceId, "uri":sourceDomain, "label": sourceLabel}
			}
			setSemanticType(worksheetId, targetId, type);
			hide();
		}

		function selectPropertyFromMenu(label, uri) {
			label = target.text();
			

			console.log("Selected property:" + label);
			if(label == 'More...') {
				populateAllProperties();
				e.stopPropagation();
				return;
			} else if(sourceDomain == "BlankNode") {
				D3ModelManager.getInstance().changeTemporaryLink(worksheetId, propertyId, uri, label);
				e.stopPropagation();
			} else {
				if(targetNodeType == "ColumnNode") {
					changeSemanticType(label, uri);
				} else {
					changeLink(label, uri);	
				}
			}
			hide();
		}


		function setTitle(title) {
			$("#propertyDialog_title", dialog).html(title + ": " + propertyLabel);
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

			var container = $("#propertyDialogFunctions");
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

		function disableAllItems() {
			var btns = $("button", "#" + menuId);
			for(var i=0; i<btns.length; i++) {
				var btn = $(btns[i]);
				btn.addClass("disabled");
				btn.prop("disabled", true);
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

		function enableItem(value) {
			var btns = $("button", "#" + menuId);
			for(var i=0; i<btns.length; i++) {
				var btn = $(btns[i]);
				if(btn.text() == value) {
					btn.removeClass("disabled");
					btn.prop("disabled", false);
					break;
				}
			}
		}

		function showFunctionsMenu() {
			$("#propertyDialogFunctions", dialog).show();
			if(sourceNodeType == "Link") {
				disableAllItems();
				enableItem("Delete");
			} else {
				enableAllItems();
				if (sourceNodeType == "ColumnNode" || sourceNodeType == "LiteralNode") {
					disableItem ("Change From");
				}

				if (targetNodeType == "ColumnNode" || targetNodeType == "LiteralNode") {
					disableItem("Change To");
				}

				if(targetNodeType  != "ColumnNode")
					disableItem("Advance Options");
			}
			rightDiv.removeClass("col-sm-12").addClass("col-sm-10");
		}

		function hideFunctionsMenu() {
			$("#propertyDialogFunctions", dialog).hide();
			rightDiv.removeClass("col-sm-10").addClass("col-sm-12");
		}

		function show(p_worksheetId, p_alignmentId, p_propertyId, p_propertyUri, p_propertyLabel, p_propertyStatus,
			p_sourceNodeId, p_sourceNodeType, p_sourceLabel, p_sourceDomain, p_sourceId, p_sourceIsUri,
			p_targetNodeId, p_targetNodeType, p_targetLabel, p_targetDomain, p_targetId, p_targetIsUri,
			event) {
			worksheetId = p_worksheetId;
			alignmentId = p_alignmentId;
			propertyId = p_propertyId;
			linkStatus = p_propertyStatus;
			propertyUri = p_propertyUri;
			propertyLabel = p_propertyLabel;

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

			$("input", dialog).val('');

			$("#propertyDialog_title", dialog).html("Link: " + propertyLabel);
			if(sourceNodeType == "Link") {
				showFunctionsMenu();
				rightDiv.hide();
			} else {
				rightDiv.show();
				changeLinkUI();
				if(linkStatus == "TemporaryLink") {
					hideFunctionsMenu();
				}
			}

			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
		};


		return { //Return back the public methods
			show: show,
			init: init,
			setTitle: setTitle,
			getDefaultProperty: getDefaultProperty,
			reloadCache: reloadCache
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


var PropertyAdvanceOptionsDialog = (function() {

	var instance = null;

	function PrivateConstructor() {
		var callback;
		var dialog = $("#propertyAdvanceOptions");

		function init() {
			$("#btnSaveAdvanceOptions").on("click", function(e) {
				var literalType = $("#propertyLiteralType", dialog).val();
				var isSubclassOfClass = $("#propertyIsSubclass", dialog).is(':checked');
				callback(literalType, isSubclassOfClass);	
			});	
			$("#propertyLiteralType").typeahead( 
				{source:LITERAL_TYPE_ARRAY, minLength:0, items:"all"});
		}

		function show(rdfLiteralType, isSubClass, div, p_callback) {
			callback = p_callback;

			$("#propertyLiteralType", dialog).val(rdfLiteralType);
			$("div#propertyAdvanceOptions input:checkbox").prop('checked', isSubClass);
			
			div.append(dialog);
			dialog.show();
		}

		function hide() {
			dialog.hide();
		}

		return { //Return back the public methods
			show: show,
			init: init,
			hide: hide
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

