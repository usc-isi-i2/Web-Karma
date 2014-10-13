function WorksheetOptions(wsId, wsTitle) {

	var worksheetId = wsId;
	var worksheetTitle = wsTitle;
	var worksheetOptionsDiv;

	var options = [
		{
			name: "View model using straight lines",
			func: viewStraightLineModel,
			showCheckbox: true,
			defaultChecked: true,
			initFunc: initStrightLineModel
		},
	    {
			name: "Organize Columns",
			func: organizeColumns
		}, 
		{
			name: "divider"
		},

		{
			name: "Suggest Model",
			func: undefined,
			addLevel: true,
			levels: [{
				name: "Using Current Ontology",
				func: suggestModel
			}, {
				name: "Generate New Ontology",
				func: suggestAutoModel
			}, ]
		},

		{
			name: "Set Properties",
			func: setProperties
		},

		{
			name: "Apply R2RML Model",
			func: undefined,
			addLevel: true,
			levels: [{
				name: "From File",
				func: applyR2RMLModel,
				useFileUpload: true,
				uploadDiv: "applyWorksheetHistory"
			}, {
				name: "From Repository",
				func: applyModel
			}]
		}, {
			name: "Add Node",
			func: addNode
		}, {
			name:"Add Liternal Node", 
			func:addLiteralNode
		}, {
			name: "divider"
		},

		{
			name: "Publish",
			func: undefined,
			addLevel: true,
			levels: [{
				name: "RDF",
				func: publishRDF
			}, {
				name: "Model",
				func: publishModel
			}, {
				name: "Service Model",
				func: publishServiceModel
			}, {
				name: "Report",
				func: publishReport
			}, {
				name: "JSON",
				func: saveAsJson
			}, ]
		}, {
			name: "Export",
			func: undefined,
			addLevel: true,
			levels: [{
				name: "To CSV",
				func: exportToCSV
			}, {
				name: "To Database",
				func: exportToDatabase
			}, {
				name: "To MDB",
				func: exportToMDB
			}, {
				name: "To SpatialData",
				func: exportToSpatial
			}, ]
		}, {
			name: "divider"
		},

		{
			name: "Populate Source",
			func: populateSource
		}, {
			name: "Invoke Service",
			func: invokeService
		}, {
			name: "divider"
		},

		{
			name: "Fold",
			func: Fold
		}, {
			name: "GroupBy",
			func: GroupBy
		}, {
			name: "Glue Columns",
			func: Glue
		}, {
			name: "Delete",
			func: deleteWorksheet
		}, {
			name: "divider"
		}, {
			name: "Selection",
			func: undefined,
			addLevel: true,
			levels: [{
				name: "Add Rows",
				func: addRows
			}, {
				name: "Intersect Rows",
				func: intersectRows
			}, {
				name: "Subtract Rows",
				func: subtractRows
			}, {
				name: "Invert",
				func: invertRows
			}, {
				name: "Clear",
				func: undefined,
				addLevel: true,
				levels: [{
					name: "In All Nested Tables",
					func: clearAll
				}, {
					name: "In This Column",
					func: clearThis
				}]
			}]
		}
	];

	function viewStraightLineModel(event) {
		if(forceLayoutEnabled)
			return false;
		var isChecked = getCheckboxState(event);
		console.log("viewStraightLineModel: " + isChecked);
		worksheetOptionsDiv.data("viewStraightLineModel", isChecked);
		hideDropdown();
		refreshAlignmentTree(worksheetId);
		return false;
	}
	
	function initStrightLineModel() {
		if (worksheetOptionsDiv)
			worksheetOptionsDiv.data("viewStraightLineModel", true);
		else
			window.setTimeout(initStrightLineModel, 100);
	}
		
	function hideDropdown() {
		$('.dropdown.open .dropdown-toggle').dropdown('toggle');
	}

	function addRows() {
		console.log("addRows");
		hideDropdown();
		$("#pyTransformSelectionDialog").data("operation", "Union");
		PyTransformSelectionDialog.getInstance(wsId, "").show();
	}

	function intersectRows() {
		hideDropdown();
		$("#pyTransformSelectionDialog").data("operation", "Intersect");
		PyTransformSelectionDialog.getInstance(wsId, "").show();
	}

	function subtractRows() {
		hideDropdown();
		$("#pyTransformSelectionDialog").data("operation", "Subtract");
		PyTransformSelectionDialog.getInstance(wsId, "").show();
	}

	function invertRows() {
		hideDropdown();
		var headers = getColumnHeadingsForColumn(wsId, "", "GroupBy");
		var info = generateInfoObject(wsId, headers[0]['HNodeId'], "OperateSelectionCommand");
		var newInfo = info['newInfo'];
		newInfo.push(getParamObject("pythonCode", "", "other"));
		newInfo.push(getParamObject("operation", "Invert", "other"));
		info["newInfo"] = JSON.stringify(newInfo);
		showLoading(worksheetId);
		sendRequest(info, worksheetId);
	}

	function clearAll() {
		hideDropdown();
		var headers = getColumnHeadingsForColumn(wsId, "", "GroupBy");
		var info = generateInfoObject(wsId, headers[0]['HNodeId'], "ClearSelectionCommand");
		var newInfo = info['newInfo'];
		newInfo.push(getParamObject("type", "All", "other"));
		info["newInfo"] = JSON.stringify(newInfo);
		showLoading(worksheetId);
		sendRequest(info, worksheetId);
	}

	function clearThis() {
		hideDropdown();
		var headers = getColumnHeadingsForColumn(wsId, "", "GroupBy");
		var info = generateInfoObject(wsId, headers[0]['HNodeId'], "ClearSelectionCommand");
		var newInfo = info['newInfo'];
		newInfo.push(getParamObject("type", "Column", "other"));
		info["newInfo"] = JSON.stringify(newInfo);
		showLoading(worksheetId);
		sendRequest(info, worksheetId);
	}

	function getCheckboxState(event) {
		var target = event.target;
		var checkbox;
		if (target.localName == "input") {
			checkbox = target;
		} else {
			checkbox = $("input[type='checkbox']", target)[0];
			$(checkbox).prop('checked', !checkbox.checked);
		}
		return checkbox.checked;
	}

	function organizeColumns() {
		hideDropdown();
		OrganizeColumnsDialog.getInstance().show(worksheetId);
		return false;
	}

	function publishReport() {
		hideDropdown();
		var info = generateInfoObject(worksheetId, "", "PublishReportCommand");

		showLoading(info["worksheetId"]);
		var returned = sendRequest(info, worksheetId);
		return false;
	}

	function deleteWorksheet() {
		if (confirm("Are you sure you wish to delete the worksheet? \nYou cannot undo this operation")) {
			hideDropdown();
			var info = generateInfoObject(worksheetId, "", "DeleteWorksheetCommand");

			showLoading(info["worksheetId"]);
			var returned = sendRequest(info, worksheetId);
		}
		return false;
	}


	function suggestModel() {
		console.log("Suggest Model: " + worksheetTitle);
		hideDropdown();
		var info = generateInfoObject(worksheetId, "", "SuggestModelCommand");

		var newInfo = info['newInfo'];
		info["newInfo"] = JSON.stringify(newInfo);

		showLoading(info["worksheetId"]);
		var returned = sendRequest(info, worksheetId);
		return false;
	}

	function setProperties() {
		console.log("Set Properties: " + worksheetTitle);
		hideDropdown();
		SetPropertiesDialog.getInstance().show(worksheetId);
		return false;
	}

	function suggestAutoModel() {
		console.log("Suggest Auto Model: " + worksheetTitle);
		hideDropdown();
		var info = generateInfoObject(worksheetId, "", "SuggestAutoModelCommand");

		var newInfo = info['newInfo'];
		info["newInfo"] = JSON.stringify(newInfo);

		showLoading(info["worksheetId"]);
		var returned = sendRequest(info, worksheetId);
		return false;
	}

	function Fold() {
		console.log("Fold: " + worksheetTitle);
		hideDropdown();
		FoldDialog.getInstance().show(worksheetId);
	}

	function GroupBy() {
		console.log("GroupBy: " + worksheetTitle);
		hideDropdown();
		GroupByDialog2.getInstance().show(worksheetId);
	}

	function Glue() {
		console.log("Glue: " + worksheetTitle);
		hideDropdown();
		GlueDialog2.getInstance().show(worksheetId);
	}

	function resetModel() {
		console.log("Reset Model: " + worksheetTitle);
		hideDropdown();
		var info = generateInfoObject(worksheetId, "", "ResetModelCommand");

		showLoading(info["worksheetId"]);
		var returned = sendRequest(info, worksheetId);
		return false;
	}

	function applyR2RMLModel() {
		console.log("Apply R2RMl Model: " + worksheetTitle);

		$("#applyWorksheetHistory_" + worksheetId).fileupload({
			add: function(e, data) {
				var override = false;
				var modelExist = false;
				var info = generateInfoObject(worksheetId, "", "CheckModelExistenceCommand");
				info["command"] = "CheckModelExistenceCommand";
				var returned = $.ajax({
					url: "RequestController",
					type: "POST",
					data: info,
					dataType: "json",
					async: false,
					complete: function(xhr, textStatus) {
						var json = $.parseJSON(xhr.responseText);
						json = json.elements[0];
						console.log(json);
						modelExist = json['modelExist'];

					},
					error: function(xhr, textStatus) {

					}
				});
				if (modelExist) {
					console.log("here" + modelExist);
					if (confirm('Clearing the current model?')) {
						override = true;
					} else {
						override = false;
					}
				}
				$("#applyWorksheetHistory_" + worksheetId).fileupload({
					url: "RequestController?workspaceId=" + $.workspaceGlobalInformation.id +
						"&command=ApplyHistoryFromR2RMLModelCommand&worksheetId=" + worksheetId + "&override=" + override
				});
				hideDropdown();
				showLoading(worksheetId);
				data.submit();
			},
			done: function(e, data) {
				$("div.fileupload-progress").hide();
				console.log(data);
				parse(data.result);
				hideLoading(worksheetId);
			},
			fail: function(e, data) {
				$.sticky("History file upload failed!");
				hideLoading(worksheetId);
			},
			dropZone: null
		});
		$('#applyWorksheetHistory_' + worksheetId).fileupload('option', 'redirect', window.location.href.replace(/\/[^\/]*$/, '/cors/result.html?%s'));
		return false;
	}

	function publishRDF() {
		console.log("Publish RDF: " + worksheetTitle);
		hideDropdown();

		PublishRDFDialog.getInstance().show(worksheetId);

		return false;
	}

	function publishModel(event) {
		console.log("Publish Model: " + worksheetTitle);
		hideDropdown();
		var info = generateInfoObject(worksheetId, "", "GenerateR2RMLModelCommand");
		info['tripleStoreUrl'] = $('#txtModel_URL').text();
		showLoading(info["worksheetId"]);
		var returned = sendRequest(info, worksheetId);
		return false;
	}

	function applyModel(event) {
		console.log("Apply Model: " + worksheetTitle);
		hideDropdown();
		applyModelDialog.getInstance(worksheetId).show();
		return false;
	}

	function publishServiceModel() {
		console.log("Publish Service Model: " + worksheetTitle);
		hideDropdown();
		var info = generateInfoObject(worksheetId, "", "PublishModelCommand");

		showLoading(info["worksheetId"]);
		var returned = sendRequest(info, worksheetId);
		return false;
	}

	function populateSource() {
		console.log("Populate Source: " + worksheetTitle);
		hideDropdown();
		var info = generateInfoObject(worksheetId, "", "PopulateCommand");

		var newInfo = info['newInfo']; // Used for commands that take JSONArray as input
		info["newInfo"] = JSON.stringify(newInfo);

		showLoading(info["worksheetId"]);
		var returned = sendRequest(info, worksheetId);
		return false;
	}

	function invokeService() {
		console.log("Invoke Service " + worksheetTitle);
		hideDropdown();
		FetchModelDialog.getInstance().show(worksheetId);
		return false;
	}

	function exportToCSV() {
		console.log("Export to CSV: " + worksheetTitle);
		hideDropdown();

		var info = generateInfoObject(worksheetId, "", "PublishCSVCommand");
		info["command"] = "PublishCSVCommand";

		showLoading(info["worksheetId"]);
		var returned = sendRequest(info, worksheetId);

		return false;
	}

	function exportToDatabase() {
		hideDropdown();
		PublishDatabaseDialog.getInstance().show(worksheetId);
		return false;
	}

	function exportToMDB() {
		console.log("Export To MDB: " + worksheetTitle);
		hideDropdown();

		var info = generateInfoObject(worksheetId, "", "PublishMDBCommand");

		showLoading(info["worksheetId"]);
		var returned = sendRequest(info, worksheetId);
		return false;
	}

	function exportToSpatial() {
		console.log("Export to Spatial: " + worksheetTitle);
		hideDropdown();

		var info = generateInfoObject(worksheetId, "", "PublishSpatialDataCommand");

		showLoading(info["worksheetId"]);
		var returned = sendRequest(info, worksheetId);
		return false;
	}

	function saveAsJson() {
		console.log("Save as json");
		hideDropdown();
		PublishJSONDialog.getInstance().show(worksheetId);
		return false;
	}

	function addNode() {
		console.log("Add Node");
		hideDropdown();
		AddNodeDialog.getInstance().show(worksheetId);
		return false;
	}
	
	function addLiteralNode() {
		console.log("Add Literal Node");
		hideDropdown();
		AddLiteralNodeDialog.getInstance().show(worksheetId);
		return false;
	}
	
	this.generateJS = function() {
		var div =
			$("<div>")
			.attr("id", "WorksheetOptionsDiv")
			.data("worksheetId", worksheetId)
			.addClass("worksheetDropdown")
			.addClass("dropdown")
			.append($("<a>")
				.attr("href", "#")
				.addClass("dropdown-toggle")
				.addClass("WorksheetTitle")
				//.addClass("btn").addClass("dropdown-toggle").addClass("sr-only")
				.attr("id", "optionsButton" + worksheetId)
				.data("worksheetId", worksheetId)
				.attr("data-toggle", "dropdown")
				//.attr("type", "button")
				.text(worksheetTitle)
				.append($("<span>").addClass("caret"))
			);


		var ul = $("<ul>").addClass("dropdown-menu");
		//console.log("There are " + options.length + " menu items");
		for (var i = 0; i < options.length; i++) {
			var option = options[i];
			var li = $("<li>");
			//console.log("Got option" +  option);
			var title = option.name;
			if (title == "divider")
				li.addClass("divider");
			else {
				var func = option.func;
				var a = $("<a>")
					.attr("href", "#");
				if (option.showCheckbox) {
					var checkbox = $("<input>").attr("type", "checkbox");
					if (option.defaultChecked)
						checkbox.attr("checked", "checked");
					var label = $("<span>").append(checkbox).append("&nbsp;").append(title);
					a.append(label);
					a.click(func);
				} else if (option.addLevel) {
					addLevels(li, a, option, worksheetId);
				} else {
					a.text(title);
					a.click(func);
				}

				li.append(a);
			}
			if (option.initFunc)
				option.initFunc();
			ul.append(li);
		};
		div.append(ul);
		worksheetOptionsDiv = div;
		return div;
	};
}