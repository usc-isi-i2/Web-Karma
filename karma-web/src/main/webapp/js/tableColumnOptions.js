function TableColumnOptions(wsId, wsColumnId, wsColumnTitle, isLeafNode, isOutofStatus) {

	var worksheetId = wsId;
	var columnTitle = wsColumnTitle;
	var columnId = wsColumnId;

	var options = [{
			name: "Set Semantic Type",
			func: setSemanticType,
			leafOnly: true,
			leafExcluded: false
		}, {
			name: "divider",
			leafOnly: true,
			leafExcluded: false
		}, {
			name: "Add Column",
			func: addColumn,
			leafOnly: false,
			leafExcluded: false
		}, {
			name: "Rename",
			func: renameColumn,
			leafOnly: true,
			leafExcluded: false
		}, {
			name: "Split Values",
			func: splitValue,
			leafOnly: true,
			leafExcluded: false
		}, {
			name: "Add Row",
			func: addRow,
			leafOnly: false,
			leafExcluded: false
		}, {
			name: "divider",
			leafOnly: false,
			leafExcluded: false
		},

		{
			name: "Extract Entities",
			func: extractEntities,
			leafOnly: true,
			leafExcluded: false
		}, {
			name: "PyTransform",
			func: pyTransform,
			leafOnly: true,
			leafExcluded: false
		}, 
		{
			name: "Transform",
			func: transform,
			leafOnly: true,
			leafExcluded: false
		},
		//{name:"Generate Cluster Values", func:clusterValues, leafOnly:true, leafExcluded: false},
		//{name:"Merge Cluster Values", func:mergeValues, leafOnly:true, leafExcluded: false},
		{
			name: "divider",
			leafOnly: true,
			leafExcluded: false
		},

		{
			name: "Invoke Service",
			func: invokeService,
			leafOnly: true,
			leafExcluded: false
		},
		//{name:"Show Chart", func:showChart, leafOnly:true, leafExcluded: false},
		{
			name: "divider",
			leafOnly: true,
			leafExcluded: false
		},

		{
			name: "Group By",
			func: GroupBy,
			leafOnly: false,
			leafExcluded: true
		}, {
			name: "Unfold",
			func: Unfold,
			leafOnly: true,
			leafExcluded: false
		}, {
			name: "Fold",
			func: Fold,
			leafOnly: false,
			leafExcluded: true
		}, {
			name: "Glue Columns",
			func: Glue,
			leafOnly: false,
			leafExcluded: true
		}, {
			name: "Selection",
			func: undefined,
			addLevel: true,
			leafOnly: false,
			leafExcluded: true,
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

	function hideDropdown() {
		$('.dropdown.open .dropdown-toggle').dropdown('toggle');
	}

	function addRows() {
		console.log("addRows");
		hideDropdown();
		$("#pyTransformSelectionDialog").data("operation", "Union");
		PyTransformSelectionDialog.getInstance(wsId, wsColumnId).show();
	}

	function intersectRows() {
		hideDropdown();
		$("#pyTransformSelectionDialog").data("operation", "Intersect");
		PyTransformSelectionDialog.getInstance(wsId, wsColumnId).show();
	}

	function subtractRows() {
		hideDropdown();
		$("#pyTransformSelectionDialog").data("operation", "Subtract");
		PyTransformSelectionDialog.getInstance(wsId, wsColumnId).show();
	}

	function invertRows() {
		hideDropdown();
		var headers = getColumnHeadingsForColumn(wsId, wsColumnId, "GroupBy");
		var info = generateInfoObject(wsId, headers[0]['HNodeId'], "OperateSelectionCommand");
		var newInfo = info['newInfo'];
		newInfo.push(getParamObject("pythonCode", "", "other"));
		newInfo.push(getParamObject("operation", "Invert", "other"));
		info["newInfo"] = JSON.stringify(newInfo);
		showLoading(worksheetId);
		sendRequest(info, worksheetId);
	}

	function refreshRows() {
		var headers = getColumnHeadingsForColumn(wsId, wsColumnId, "GroupBy");
		var info = generateInfoObject(wsId, headers[0]['HNodeId'], "RefreshSelectionCommand");
		var newInfo = info['newInfo'];
		info["newInfo"] = JSON.stringify(newInfo);
		showLoading(worksheetId);
		sendRequest(info, worksheetId);
	}

	function clearAll() {
		hideDropdown();
		var headers = getColumnHeadingsForColumn(wsId, wsColumnId, "GroupBy");
		var info = generateInfoObject(wsId, headers[0]['HNodeId'], "ClearSelectionCommand");
		var newInfo = info['newInfo'];
		newInfo.push(getParamObject("type", "All", "other"));
		info["newInfo"] = JSON.stringify(newInfo);
		showLoading(worksheetId);
		sendRequest(info, worksheetId);
	}

	function clearThis() {
		hideDropdown();
		var headers = getColumnHeadingsForColumn(wsId, wsColumnId, "GroupBy");
		var info = generateInfoObject(wsId, headers[0]['HNodeId'], "ClearSelectionCommand");
		var newInfo = info['newInfo'];
		newInfo.push(getParamObject("type", "Column", "other"));
		info["newInfo"] = JSON.stringify(newInfo);
		showLoading(worksheetId);
		sendRequest(info, worksheetId);
	}

	function setSemanticType() {
		hideDropdown();
		SetSemanticTypeDialog.getInstance().show(worksheetId, columnId, columnTitle);
		return false;
	}

	function clusterValues() {
		hideDropdown();
		//alert("reached here yo");
		//ClusterValuesDialog.getInstance().show(worksheetId, columnId);
		ClusterValues(worksheetId, columnId);
		return false;
	}

	function mergeValues() {
		hideDropdown();
		//alert("reached here merging");
		//mergeValuesProcess.getInstance().show(worksheetId, columnId);
		MergeValues(worksheetId, columnId);
		return false;
	}



	function addRow() {
		var info = generateInfoObject(worksheetId, columnId, "AddRowCommand");

		var newInfo = info['newInfo']; // Used for commands that take JSONArray as input
		newInfo.push(getParamObject("hTableId", "", "other"));

		info["newInfo"] = JSON.stringify(newInfo);

		// console.log(info["worksheetId"]);
		showLoading(info["worksheetId"]);

		var returned = sendRequest(info, worksheetId);
	}

	function addColumn() {
		hideDropdown();
		AddColumnDialog.getInstance().show(worksheetId, columnId);
		return false;
	}


	function extractEntities() {
		hideDropdown();
		ExtractEntitiesDialog.getInstance().show(worksheetId, columnId);
		return false;
	}

	function pyTransform() {
		hideDropdown();
		PyTransformDialog.getInstance().show(worksheetId, columnId, columnTitle);
		return false;
	}

	function invokeService() {
		var info = generateInfoObject(worksheetId, columnId, "InvokeServiceCommand");

		showLoading(worksheetId);
		var returned = sendRequest(info, worksheetId);
	}

	function renameColumn() {
		hideDropdown();
		RenameColumnDialog.getInstance().show(worksheetId, columnId);
		return false;
	}

	function splitColumn() {
		hideDropdown();
		SplitColumnDialog.getInstance().show(worksheetId, columnId);
		return false;
	}

	function splitValue() {
		hideDropdown();
		SplitValueDialog.getInstance().show(worksheetId, columnId);
		return false;
	}

	function transform() {
		hideDropdown();
		TransformColumnDialog.getInstance().show(worksheetId, columnId);
		return false;
	}

	function showChart() {
		showChartForColumn(worksheetId, columnId);
	}

	function GroupBy() {
		//console.log("Group By: " + worksheetTitle);
		hideDropdown();
		GroupByDialog.getInstance().show(worksheetId, columnId);
	}

	function Unfold() {
		//console.log("Group By: " + worksheetTitle);
		hideDropdown();
		UnfoldDialog.getInstance().show(worksheetId, columnId);
	}

	function Fold() {
		//console.log("Group By: " + worksheetTitle);
		hideDropdown();
		FoldDialog2.getInstance().show(worksheetId, columnId);
	}

	function Glue() {
		//console.log("Group By: " + worksheetTitle);
		hideDropdown();
		GlueDialog.getInstance().show(worksheetId, columnId);
	}

	this.generateJS = function() {
		var dropdownId = "columnOptionsButton" + worksheetId + "_" + columnId;
		var span = $("<span>")
			.attr("display", "inline-block")
			.addClass("tableDropdown")
			.addClass("dropdown")
			.append($("<a>")
				.attr("href", "#")
				.addClass("dropdown-toggle")
				.addClass("ColumnTitle")
				.attr("id", dropdownId)
				.attr("title", columnTitle)
				.data("worksheetId", worksheetId)
				.attr("data-toggle", "dropdown")
				.append($("<div>")
					.addClass("truncate")
					.text(columnTitle)
					.append($("<span>").addClass("caret"))
				)
			);

		var div =
			$("<div>")
			.attr("id", "TableOptionsDiv")
			.data("worksheetId", worksheetId)
			.append(span);
		if (isOutofStatus) {
			var a = $("<a>").attr("href", "#");
			a.click(refreshRows);
			a.append($("<span>").addClass("glyphicon glyphicon-refresh"));
			div.append(a);
		}
		var ul = $("<ul>").addClass("dropdown-menu");
		ul.attr("role", "menu")
			.attr("aria-labelledby", dropdownId);
		// console.log("There are " + options.length + " menu items");
		for (var i = 0; i < options.length; i++) {
			var option = options[i];

			if (option.leafOnly == true && isLeafNode == false) {
				continue;
			}

			if (option.leafExcluded == true && isLeafNode == true) {
				continue;
			}

			var needFile = option.useFileUpload;

			var li = $("<li>");
			//console.log("Got option" +  option);
			var title = option.name;
			if (title == "divider")
				li.addClass("divider");
			else {
				var func = option.func;
				var a = $("<a>")
					.attr("href", "#");
				if (needFile) {
					//<form id="fileupload" action="ImportFileCommand" method="POST" enctype="multipart/form-data">From File<input type="file" name="files[]" multiple></form>
					a.addClass("fileinput-button");
					var form = $("<form>")
						.attr("id", option.uploadDiv + "_" + worksheetId)
						.attr("action", "ImportFileCommand")
						.attr("method", "POST")
						.attr("enctype", "multipart/form-data")
						.text(title);
					var input = $("<input>")
						.attr("type", "file")
						.attr("name", "files[]");
					form.append(input);
					a.append(form);
					window.setTimeout(func, 1000);
				} else {
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

				}
				li.append(a);
			}
			if (option.initFunc)
				option.initFunc();
			ul.append(li);
		};
		span.append(ul);
		return div;
	};
};

var AddColumnDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#addColumnDialog");
		var worksheetId, columnId;

		function init() {
			// Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				hideError();
				$("input", dialog).val("");
				$("#columnName", dialog).focus();
			});

			// Initialize handler for Save button
			// var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});
		}

		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError() {
			$("div.error", dialog).show();
		}

		function saveDialog(e) {
			console.log("Save clicked");

			var newColumnValue = $.trim($("#columnName", dialog).val());
			var defaultValue = $.trim($("#defaultValue", dialog).val());

			var validationResult = true;
			if (!newColumnValue)
				validationResult = false;
			// Check if the column name already exists
			var columnNames = getColumnHeadings(worksheetId);
			$.each(columnNames, function(index, columnName) {
				if (columnName == newColumnValue) {
					validationResult = false;
				}
			});
			if (!validationResult) {
				showError();
				$("#columnName", dialog).focus();
				return false;
			}

			dialog.modal('hide');

			var info = generateInfoObject(worksheetId, columnId, "AddColumnCommand");
			info["hTableId"] = "";
			info["newColumnName"] = "new_column";

			var newInfo = info['newInfo']; // Used for commands that take JSONArray as
			// input
			newInfo.push(getParamObject("hTableId", "", "other"));
			newInfo.push(getParamObject("newColumnName", newColumnValue, "other"));
			newInfo.push(getParamObject("defaultValue", defaultValue, "other"));
			info["newInfo"] = JSON.stringify(newInfo);

			// console.log(info["worksheetId"]);
			showLoading(info["worksheetId"]);

			var returned = sendRequest(info, worksheetId);
		};

		function show(wsId, colId) {
			worksheetId = wsId;
			columnId = colId;
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
		};


		return { // Return back the public methods
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





var RenameColumnDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#renameColumnDialog");
		var worksheetId, columnId;

		function init() {
			// Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				hideError();
				$("input", dialog).val("");
				$("#columnName", dialog).focus();
			});

			// Initialize handler for Save button
			// var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});
		}

		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError() {
			$("div.error", dialog).show();
		}

		function saveDialog(e) {
			console.log("Save clicked");

			var newColumnValue = $.trim($("#columnName", dialog).val());

			var validationResult = true;
			if (!newColumnValue)
				validationResult = false;
			// Check if the column name already exists
			var columnNames = getColumnHeadings(worksheetId);
			$.each(columnNames, function(index, columnName) {
				if (columnName == newColumnValue) {
					validationResult = false;
				}
			});
			if (!validationResult) {
				showError();
				$("#columnName", dialog).focus();
				return false;
			}

			dialog.modal('hide');

			var info = generateInfoObject(worksheetId, columnId, "RenameColumnCommand");
			var newInfo = info['newInfo']; // for input parameters
			newInfo.push(getParamObject("newColumnName", newColumnValue, "other"));
			newInfo.push(getParamObject("getAlignmentUpdate", ($("#svgDiv_" + worksheetId).length > 0), "other"));
			info["newInfo"] = JSON.stringify(newInfo);
			showLoading(worksheetId);
			var returned = sendRequest(info, worksheetId);
		};

		function show(wsId, colId) {
			worksheetId = wsId;
			columnId = colId;
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
		};


		return { // Return back the public methods
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


var SplitValueDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#splitValuesDialog");
		var worksheetId, columnId;
		var worksheetHeaders;
		var updatableColumns;
		
		function init() {
			// Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				hideError();
				$("#valueSplitNewColName", dialog).val("");
				$("#valueSplitDelimiter", dialog).focus();
				$("#splitValuesUpdateColumns").empty();
				worksheetHeaders = getColumnHeadingsForColumn(worksheetId, columnId, "SplitValues");
				updatableColumns = [];
				nonupdatableColumns = [];
				var columnName = getColumnName();
				$.each(worksheetHeaders, function(index, element) {
					if (element['ColumnName'] == columnName) {
						if(element["appliedCommands"]) {
							$.each(element["appliedCommands"], function(index, appliedCommand) {
								if(appliedCommand["CommandName"] == "SplitValuesCommand") {
									var columns = appliedCommand["Columns"];
									$.each(columns, function(index, column) {
										updatableColumns.push(column["ColumnName"]);
										var option = $('<option>').html(column["ColumnName"]).val(column["HNodeId"]);
										$("#splitValuesUpdateColumns").append(option);
									});
								}
							});
						}
					}
				});
				
				var $radios = $('input:radio[name=splitValuesType]');
			    $radios.filter('[value=new]').prop('checked', true);
			    
			    
				if(updatableColumns.length > 0) {
					$("#splitValuesUpdateColumns").attr('disabled',false);
					jQuery("#splitValuesTypeEdit").attr('disabled',false);
					$.each(updatableColumns, function(index, element) {
						console.log("Can update:" + element);
					});
				} else {
					$("#splitValuesUpdateColumns").attr('disabled',true);
					jQuery("#splitValuesTypeEdit").attr('disabled',true);
				}
			});

			// Initialize handler for Save button
			// var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});
		}

		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError(txt) {
			$("div.error", dialog).text(txt);
			$("div.error", dialog).show();
		}

		function getColumnName() {
			var id = "columnOptionsButton" + worksheetId + "_" + columnId;
			var oldColName = $("#" + id).attr("title");
			return oldColName;
		}
		
		function saveDialog(e) {
			console.log("Save clicked");

			var delimiter = $.trim($("#valueSplitDelimiter", dialog).val());
			
			var splitValuesType = $('input:radio[name=splitValuesType]:checked').val();
			var newColName;
			var newHNodeId;
			if(splitValuesType == "new") {
				newColName = $.trim($("#valueSplitNewColName", dialog).val());
				newHNodeId = "";
			} else {
				newHNodeId = $("#splitValuesUpdateColumns").val();
				newColName = $("#splitValuesUpdateColumns").html();
			}
			
			 
			var id = "columnOptionsButton" + worksheetId + "_" + columnId;
			var oldColName = $("#" + id).attr("title");
			var validationResult = true;
			
			if (!delimiter) {
				validationResult = false;
			} else if (delimiter != "space" && delimiter != "tab" && delimiter.length != 1) {
				validationResult = false;
			}
			if (!validationResult) {
				showError("Length of the delimter should be 1");
				$("#valueSplitDelimiter", dialog).focus();
				return false;
			}
			if(newColName.length == 0) {
				showError("Please enter the column name");
				$("#valueSplitNewColName", dialog).focus();
			}
			validationResult = true;
			if (newColName.toLowerCase() != oldColName.toLowerCase()) {
				$.each(worksheetHeaders, function(index, element) {
					if (element['ColumnName'].toLowerCase() == newColName.toLowerCase()) {
						var isUpdatable = false;
						$.each(updatableColumns, function(idx, cn) {
							if(cn.toLowerCase() == newColName.toLowerCase()) {
								isUpdatable = true;
							}
						});
						if(!isUpdatable)
							validationResult = false;
					}
				});
			} else {
				validationResult = false;
			}
			
			if (!validationResult) {
				showError(newColName + " already exists");
				$("#valueSplitNewColName", dialog).focus();
				return false;
			}

			dialog.modal('hide');
			var info = generateInfoObject(worksheetId, columnId, "SplitValuesCommand");
			info["delimiter"] = delimiter;
			info["newColName"] = newColName;
			info["newHNodeId"] = newHNodeId;

			var newInfo = info['newInfo'];
			newInfo.push(getParamObject("delimiter", delimiter, "other"));
			newInfo.push(getParamObject("newColName", newColName, "other"));
			newInfo.push(getParamObject("newHNodeId", newHNodeId, "other"));
			info["newInfo"] = JSON.stringify(newInfo);

			showLoading(info["worksheetId"]);
			var returned = sendRequest(info, worksheetId);
		};

		function show(wsId, colId) {
			worksheetId = wsId;
			columnId = colId;
			var id = "columnOptionsButton" + wsId + "_" + colId;
			var title = $("#" + id).attr("title");
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
		};


		return { // Return back the public methods
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




var PyTransformDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#pyTransformDialog");
		var worksheetId, columnId, columnName;
		var editor;

		function init() {
			editor = ace.edit("transformCodeEditor");
			editor.setTheme("ace/theme/dreamweaver");
			editor.getSession().setMode("ace/mode/python");
			editor.getSession().setUseWrapMode(true);
			editor.getSession().setValue("return getValue(\"state\")");

			dialog.on("resize", function(event, ui) {
				editor.resize();
			});

			// Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				hideError();
				var hNode = $("td#" + columnId);

				if (hNode.data("pythonTransformation"))
					editor.getSession().setValue(hNode.data("pythonTransformation"));
				else
					editor.getSession().setValue("return getValue(\"" + columnName + "\")");

				$("#pythonTransformEditColumnName").html(columnName);
				$("#pythonTransformNewColumnName").val("");
				// $("#pythonTransformNewColumnName").attr('disabled','disabled');

				$("#btnError", dialog).button('disable');
				$("input").removeAttr('disabled');
				$("#pythonPreviewResultsTable").hide();
			});

			// Initialize handler for Save button
			// var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});

			$('#btnErrors', dialog).on('click', function(event) {
				$("#pyTransformErrorWindow").show();
			});

			$('#btnPreview', dialog).on('click', function(e) {
				previewTransform();
			});
		}

		function hideError() {
			$("div.error", dialog).hide();
			$("#pyTransformErrorWindow").hide();
		}

		function showError(message) {
			if (message) {
				$("div.error", dialog).text(message);
			}
			$("div.error", dialog).show();
		}

		function saveDialog(e) {
			console.log("Save clicked");
			var hNode = $("td#" + columnId);

			var transformType = $('input:radio[name=pyTransformType]:checked').val();
			console.log("Got transform type: " + transformType);
			if (transformType == "edit") {
				if (hNode.data("columnDerivedFrom"))
					submitEditPythonTransform();
				else {
					// alert("We need to handle this extension of python
					// transform");
					submitAddPythonTransform(true);
				}
			} else {
				submitAddPythonTransform(false);
			}
		};

		function previewTransform() {
			var info = generateInfoObject(worksheetId, columnId, "PreviewPythonTransformationResultsCommand");
			info["transformationCode"] = editor.getValue();
			info["errorDefaultValue"] = $("#pythonTransformErrorDefaultValue").val();
			$("#pyTransformErrorWindow").hide();
			// Send the request
			$.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);
					var previewTable = $("table#pythonPreviewResultsTable");
					$("tr", previewTable).remove();
					$.each(json["elements"], function(index, element) {
						if (element["updateType"] == "PythonPreviewResultsUpdate") {
							var result = element["result"];
							$.each(result, function(index2, resVal) {
								previewTable.append($("<tr>").append($("<td>").text(resVal.value)));
							});
							var errorWindow = $("#pyTransformErrorWindow", dialog);
							$("div.pythonError", errorWindow).remove();
							var errors = element["errors"];
							if (errors.length > 0) {
								$("#pyTransformViewErrorButton").button('enable');
								$.each(errors, function(index3, error) {
									var errorHtml = $("<div>").addClass("pythonError");
									if (error.row != -1)
										errorHtml.append($("<span>").addClass("pythonErrorRowNumber").text("Row: " + error.row)).append($("<br>"));
									errorHtml.append($("<span>").addClass("pythonErrorText").text("Error: " + error["error"])).append($("<br>")).append($("<br>"));
									errorWindow.append(errorHtml);
								});
							} else {
								$("#pyTransformViewErrorButton").button('disable');
							}
						} else if (element["updateType"] == "KarmaError") {
							showError(element["Error"]);
						}
					});
					previewTable.show();
				},
				error: function(xhr, textStatus) {
					alert("Error occured with fetching new rows! " + textStatus);
				}
			});
		}

		function submitEditPythonTransform() {
			var hNode = $("td#" + columnId);
			var columnName = $("#pythonTransformEditColumnName").val();

			hide();

			var prevTransCode = hNode.data("pythonTransformation");
			var newTransCode = editor.getValue();

			if (prevTransCode.trim() == newTransCode.trim()) {
				console.log("Code has not changed, we do not need to perform an edit");
				return;
			}


			// prepare the JSON Object to be sent to the server
			var info = generateInfoObject(worksheetId, hNode.data("columnDerivedFrom"), "SubmitEditPythonTransformationCommand");
			var newInfo = info['newInfo'];
			newInfo.push(getParamObject("newColumnName", columnName, "other"));
			newInfo.push(getParamObject("transformationCode", newTransCode, "other"));
			newInfo.push(getParamObject("previousCommandId", hNode.data("previousCommandId"), "other"));
			newInfo.push(getParamObject("errorDefaultValue", $("#pythonTransformErrorDefaultValue").val(), "other"));
			newInfo.push(getParamObject("targetHNodeId", columnId, "hNodeId"));
			info["newInfo"] = JSON.stringify(newInfo);

			showLoading(worksheetId);
			sendRequest(info, worksheetId);
		}

		function submitAddPythonTransform(useExistingColumnName) {
			var hNodeId = columnId;

			var columnName = (useExistingColumnName == true) ? $("#pythonTransformEditColumnName").html() : $("#pythonTransformNewColumnName").val();
			// Validate new column name
			var validationResult = true;
			if (!columnName)
				validationResult = false;
			// Check if the column name already exists
			if (!useExistingColumnName) {
				var columnNames = getColumnHeadings(worksheetId);
				$.each(columnNames, function(index, element) {
					if ($.trim(element) == columnName) {
						validationResult = false;
					}
				});
			}
			if (!validationResult) {
				showError('Please provide a new unique column name!');
				$("#pythonTransformNewColumnName").focus();
				return false;
			}

			hide();

			// prepare the JSON Object to be sent to the server
			var info = generateInfoObject(worksheetId, hNodeId, "SubmitPythonTransformationCommand");
			var newInfo = info['newInfo'];
			newInfo.push(getParamObject("newColumnName", columnName, "other"));
			newInfo.push(getParamObject("transformationCode", editor.getValue(), "other"));
			newInfo.push(getParamObject("errorDefaultValue", $("#pythonTransformErrorDefaultValue").val(), "other"));
			// newInfo.push(getParamObject("useExistingColumnName",
			// useExistingColumnName, "useExistingColumnName"));
			info["newInfo"] = JSON.stringify(newInfo);

			showLoading(worksheetId)
			sendRequest(info, worksheetId);
		}

		function hide() {
			dialog.modal('hide');
		}

		function show(wsId, colId, colName) {
			worksheetId = wsId;
			columnId = colId;
			columnName = colName;
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
		};


		return { // Return back the public methods
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

var ExtractEntitiesDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#extractEntitiesDialog");
		var entitySelDialog = $("#extractionCapabilitiesDialog");
		// hidden by default
		entitySelDialog.modal('hide');

		var worksheetId, columnId;

		function init() {
			// Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				console.log("dialog displayed");
				hideError();
				$('#extractionService_URL').val("http://karmanlp.isi.edu:8080/ExtractionService/StanfordCoreNLP");
			});

			// Initialize handler for Save button
			// var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
				console.log("dialog hidden after save");
			});
		}

		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError() {
			$("div.error", dialog).show();
		}

		function saveDialog(e) {
			console.log("Save clicked");
			var info = generateInfoObject(worksheetId, columnId, "ExtractEntitiesCommand");
			info["hTableId"] = "";
			info["extractionURL"] = $('#extractionService_URL').val();

			dialog.modal('hide');
			var newInfo = info['newInfo'];
			newInfo.push(getParamObject("extractionURL", info["extractionURL"], "other"));
			
			info["newInfo"] = JSON.stringify(newInfo);
			
			// console.log(info["worksheetId"]);
			showLoading(info["worksheetId"]);

			var userSelResp = $.ajax({
				url: $('#extractionService_URL').val() + "/getCapabilities",
				type: "GET",
				dataType: "json",
				contentType: "text/plain",
				crossDomain: true,
				complete: function(xhr, textStatus) {
					console.log(xhr.responseText);
					var jsonresp = $.parseJSON(xhr.responseText);

					var dialogContent = $("#userSelection", entitySelDialog);
					dialogContent.empty();

					$.each(jsonresp, function(index, data) {
						var row = $("<div>").addClass("checkbox");
						var label = $("<label>").text(data.capability);
						var input = $("<input>")
							.attr("type", "checkbox")
							.attr("id", "selectentities")
							.attr("value", data.capability);
						label.append(input);
						row.append(label);
						dialogContent.append(row);
					});

					//Initialize handler for Save button
					//var me = this;
					$('#btnSave', entitySelDialog).on('click', function(e) {
						e.preventDefault();
						saveUserSelDialog(e, info);
					});

					//display user selection dialog
					entitySelDialog.modal('show');
					console.log("User selection dialog displayed");

					hideLoading(info["worksheetId"]);
				},
				error: function(xhr, textStatus) {
					console.log("error");
					alert("Error occured while getting capabilities from the specified service:" + textStatus);
					hideLoading(info["worksheetId"]);
				}
			});

		};

		function saveUserSelDialog(e, info) {
			console.log("Save clicked");
			var userSelection = "";

			var checkboxes = entitySelDialog.find(":checked");
			var checked = [];
			for (var i = 0; i < checkboxes.length - 1; i++) {
				var checkbox = checkboxes[i];
				userSelection = userSelection + checkbox.value + ",";
			}

			if (checkboxes.length > 0) {
				userSelection = userSelection + checkboxes[checkboxes.length - 1].value;
			}

			entitySelDialog.modal('hide');

			// console.log(info["worksheetId"]);
			showLoading(info["worksheetId"]);

			console.log("User selection: " + userSelection);
			info["entitiesToBeExt"] = userSelection;
			var newInfo = JSON.parse(info['newInfo']);
			newInfo.push(getParamObject("entitiesToBeExt", info["entitiesToBeExt"], "other"));
			
			info["newInfo"] = JSON.stringify(newInfo);
			
			var returned = sendRequest(info, worksheetId);

		};

		function show(wsId, colId) {
			worksheetId = wsId;
			columnId = colId;
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
		};


		return { // Return back the public methods
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


var GroupByDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#groupByDialog");
		var worksheetId, columnId;

		function init() {

			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});
		}

		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError() {
			$("div.error", dialog).show();
		}

		function saveDialog(e) {
			console.log("Save clicked");

			var checkboxes = dialog.find(":checked");
			var checked = [];
			for (var i = 0; i < checkboxes.length; i++) {
				var checkbox = checkboxes[i];
				checked.push(getParamObject("checked", checkbox['value'], "hNodeId"));
			}
			if (checked.length == 0) {
				hide();
				return;
			}
			//console.log(checked);
			var info = generateInfoObject(worksheetId, checkboxes[0]['value'], "GroupByCommand");

			var newInfo = info['newInfo'];
			newInfo.push(getParamObject("values", JSON.stringify(checked), "hNodeIdList"));
			info["newInfo"] = JSON.stringify(newInfo);

			showLoading(info["worksheetId"]);
			var returned = sendRequest(info, worksheetId);

			hide();
		};

		function hide() {
			dialog.modal('hide');
		}

		function show(wsId, cId) {
			worksheetId = wsId;
			columnId = cId;
			dialog.on('show.bs.modal', function(e) {
				hideError();
				var dialogContent = $("#groupByDialogColumns", dialog);
				dialogContent.empty();
				var headers = getColumnHeadingsForColumn(wsId, cId, "GroupBy");
				console.log(headers);
				if (!headers) {
					hide();
					return;
				}
				//console.log(headers);
				for (var i = 0; i < headers.length; i++) {

					var columnName = headers[i].ColumnName;
					var id = headers[i].HNodeId;
					//console.log(columnName);
					//console.log(id);
					var row = $("<div>").addClass("checkbox");
					var label = $("<label>").text(columnName);
					var input = $("<input>")
						.attr("type", "checkbox")
						.attr("id", "selectcolumns")
						.attr("value", id);
					label.append(input);
					row.append(label);
					dialogContent.append(row);
				}
			});
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
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

var UnfoldDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#unfoldDialog");
		var worksheetId, columnId;

		function init() {

			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});
		}

		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError() {
			$("div.error", dialog).show();
		}

		function saveDialog(e) {
			console.log("Save clicked");

			var checkboxes = dialog.find(":checked");
			if (checkboxes.length == 0) {
				hide();
				return;
			}
			var checked = checkboxes[0];

			//console.log(checked);
			var info = generateInfoObject(worksheetId, "", "UnfoldCommand");
			var newInfo = info['newInfo'];
			newInfo.push(getParamObject("keyhNodeId", columnId, "hNodeId"));
			newInfo.push(getParamObject("valuehNodeId", checked['value'], "hNodeId"));
			info["newInfo"] = JSON.stringify(newInfo);
			showLoading(info["worksheetId"]);
			var returned = sendRequest(info, worksheetId);

			hide();
		};

		function hide() {
			dialog.modal('hide');
		}

		function show(wsId, cId) {
			worksheetId = wsId;
			columnId = cId;
			var headers = getColumnHeadingsForColumn(wsId, cId, "Unfold");
			if (headers.length == 0)
				return;
			dialog.on('show.bs.modal', function(e) {
				hideError();
				var dialogContent = $("#unfoldDialogColumns", dialog);
				dialogContent.empty();


				for (var i = 0; i < headers.length; i++) {

					var columnName = headers[i].ColumnName;
					var id = headers[i].HNodeId;
					//console.log(columnName);
					//console.log(id);
					var row = $("<div>").addClass("radio");
					var label = $("<label>").text(columnName);
					var input = $("<input>")
						.attr("type", "radio")
						.attr("id", "selectcolumns")
						.attr("value", id)
						.attr("name", "unfoldColumn");
					label.append(input);
					row.append(label);
					dialogContent.append(row);
				}
			});
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
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

var FoldDialog2 = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#foldDialog2");
		var worksheetId, columnId;

		function init() {
			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});
		}

		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError() {
			$("div.error", dialog).show();
		}

		function saveDialog(e) {
			console.log("Save clicked");

			var checkboxes = dialog.find(":checked");
			var checked = [];
			for (var i = 0; i < checkboxes.length; i++) {
				var checkbox = checkboxes[i];
				checked.push(getParamObject("checked", checkbox['value'], "hNodeId"));
			}
			if (checked.length == 0) {
				hide();
				return;
			}
			//console.log(checked);
			var info = generateInfoObject(worksheetId, checkboxes[0]['value'], "FoldCommand");

			var newInfo = info['newInfo'];
			newInfo.push(getParamObject("values", JSON.stringify(checked), "hNodeIdList"));
			info["newInfo"] = JSON.stringify(newInfo);

			showLoading(info["worksheetId"]);
			var returned = sendRequest(info, worksheetId);

			hide();
		};

		function hide() {
			dialog.modal('hide');
		}

		function show(wsId, cId) {
			worksheetId = wsId;
			columnId = cId;
			dialog.on('show.bs.modal', function(e) {
				hideError();
				var dialogContent = $("#foldDialogColumns", dialog);
				dialogContent.empty();
				var headers = getColumnHeadingsForColumn(wsId, cId, "Fold");
				if (!headers) {
					hide();
					return;
				}
				//console.log(headers);
				for (var i = 0; i < headers.length; i++) {

					var columnName = headers[i].ColumnName;
					var id = headers[i].HNodeId;
					//console.log(columnName);
					//console.log(id);
					var row = $("<div>").addClass("checkbox");
					var label = $("<label>").text(columnName);
					var input = $("<input>")
						.attr("type", "checkbox")
						.attr("id", "selectcolumns")
						.attr("value", id)
					label.append(input);
					row.append(label);
					dialogContent.append(row);
				}
			});
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
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


var GlueDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#glueDialog");
		var worksheetId, columnId;

		function init() {

			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});
		}

		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError() {
			$("div.error", dialog).show();
		}

		function saveDialog(e) {
			console.log("Save clicked");

			var checkboxes = dialog.find(":checked");
			var checked = [];
			for (var i = 0; i < checkboxes.length; i++) {
				var checkbox = checkboxes[i];
				checked.push(getParamObject("checked", checkbox['value'], "hNodeId"));
			}
			if (checked.length == 0) {
				hide();
				return;
			}
			//console.log(checked);
			var info = generateInfoObject(worksheetId, checkboxes[0]['value'], "GlueCommand");

			var newInfo = info['newInfo'];
			newInfo.push(getParamObject("values", JSON.stringify(checked), "hNodeIdList"));
			info["newInfo"] = JSON.stringify(newInfo);

			showLoading(info["worksheetId"]);
			var returned = sendRequest(info, worksheetId);

			hide();
		};

		function hide() {
			dialog.modal('hide');
		}

		function show(wsId, cId) {
			worksheetId = wsId;
			columnId = cId;
			dialog.on('show.bs.modal', function(e) {
				hideError();
				var dialogContent = $("#glueDialogColumns", dialog);
				dialogContent.empty();
				var headers = getColumnHeadingsForColumn(wsId, cId, "Glue");
				console.log(headers);
				if (!headers) {
					hide();
					return;
				}
				//console.log(headers);
				for (var i = 0; i < headers.length; i++) {

					var columnName = headers[i].ColumnName;
					var id = headers[i].HNodeId;
					//console.log(columnName);
					//console.log(id);
					var row = $("<div>").addClass("checkbox");
					var label = $("<label>").text(columnName);
					var input = $("<input>")
						.attr("type", "checkbox")
						.attr("id", "selectcolumns")
						.attr("value", id);
					label.append(input);
					row.append(label);
					dialogContent.append(row);
				}
			});
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
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

var PyTransformSelectionDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#pyTransformSelectionDialog");
		var worksheetId, columnId;
		var editor;
		var headers;

		function init(wsId, colId) {
			worksheetId = wsId;
			columnId = colId;
			headers = getColumnHeadingsForColumn(worksheetId, columnId, "GroupBy");
			console.log(headers);
			console.log(headers);
			$('#btnSaveSelection', dialog).unbind('click');
			$('#btnErrorsSelection', dialog).unbind('click');
			$('#btnPreviewSelection', dialog).unbind('click');
			dialog.unbind("show.bs.modal");
			dialog.unbind("resize");
			if (editor == undefined) {
				editor = ace.edit("transformCodeEditorSelection");
				editor.setTheme("ace/theme/dreamweaver");
				editor.getSession().setMode("ace/mode/python");
				editor.getSession().setUseWrapMode(true);
			}
			editor.getSession().setValue("return getValue(\"" + headers[0]['ColumnName'] + "\")");
			dialog.on("resize", function(event, ui) {
				editor.resize();
			});
			// Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				$("#onErrorSelection").attr("checked", true);
				hideError();
				$("#pythonPreviewResultsTableSelection").hide();
				$("#btnErrorsSelection").button('disable');
			});

			// Initialize handler for Save button
			// var me = this;
			$('#btnSaveSelection', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});

			$('#btnErrorsSelection', dialog).on('click', function(event) {
				$("#pyTransformErrorWindowSelection").show();
			});

			$('#btnPreviewSelection', dialog).on('click', function(e) {
				previewTransform();
			});
		}

		function hideError() {
			$("div.error", dialog).hide();
			$("#pyTransformErrorWindowSelection").hide();
		}

		function showError(message) {
			if (message) {
				$("div.error", dialog).text(message);
			}
			$("div.error", dialog).show();
		}

		function saveDialog(e) {
			console.log("Save clicked");
			var info = generateInfoObject(worksheetId, headers[0]['HNodeId'], "OperateSelectionCommand");
			var newInfo = info['newInfo'];
			var error = $("#onErrorSelection").prop("checked");
			newInfo.push(getParamObject("pythonCode", editor.getValue(), "other"));
			newInfo.push(getParamObject("operation", dialog.data("operation"), "other"));
			newInfo.push(getParamObject("onError", error ? "true" : "false", "other"));
			info["newInfo"] = JSON.stringify(newInfo);
			showLoading(worksheetId);
			sendRequest(info, worksheetId);
			hide();
		};

		function previewTransform() {
			var info = generateInfoObject(worksheetId, headers[0]['HNodeId'], "PreviewPythonTransformationResultsCommand");
			info["transformationCode"] = editor.getValue();
			info["errorDefaultValue"] = $("#pythonTransformErrorDefaultValueSelection").val();
			$("#pyTransformErrorWindowSelection").hide();
			// Send the request
			$.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);
					var previewTable = $("table#pythonPreviewResultsTableSelection");
					$("tr", previewTable).remove();
					$.each(json["elements"], function(index, element) {
						if (element["updateType"] == "PythonPreviewResultsUpdate") {
							var result = element["result"];
							$.each(result, function(index2, resVal) {
								previewTable.append($("<tr>").append($("<td>").text(resVal.value)));
							});
							var errorWindow = $("#pyTransformErrorWindowSelection", dialog);
							$("div.pythonError", errorWindow).remove();
							var errors = element["errors"];
							if (errors.length > 0) {
								$("#btnErrorsSelection").button('enable');
								$.each(errors, function(index3, error) {
									var errorHtml = $("<div>").addClass("pythonError");
									if (error.row != -1)
										errorHtml.append($("<span>").addClass("pythonErrorRowNumber").text("Row: " + error.row)).append($("<br>"));
									errorHtml.append($("<span>").addClass("pythonErrorText").text("Error: " + error["error"])).append($("<br>")).append($("<br>"));
									errorWindow.append(errorHtml);
								});
							} else {
								$("#btnErrorsSelection").button('disable');
							}
						} else if (element["updateType"] == "KarmaError") {
							showError(element["Error"]);
						}
					});
					previewTable.show();
				},
				error: function(xhr, textStatus) {
					alert("Error occured with fetching new rows! " + textStatus);
				}
			});
		}


		function hide() {
			dialog.modal('hide');
		}

		function show() {
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
		};


		return { // Return back the public methods
			show: show,
			init: init
		};
	};

	function getInstance(wsId, colId) {
		if (!instance) {
			instance = new PrivateConstructor();
		}
		instance.init(wsId, colId);
		return instance;
	}

	return {
		getInstance: getInstance
	};


})();


function ClusterValues(worksheetId, columnId) {

	var info = generateInfoObject(worksheetId, columnId, "GenerateClusterValuesCommand");

	var newInfo = info['newInfo'];

	info["newInfo"] = JSON.stringify(newInfo);
	//alert("sending"+info);
	showLoading(worksheetId);
	sendRequest(info, worksheetId);
}

function MergeValues(worksheetId, columnId) {

	var info = generateInfoObject(worksheetId, columnId, "MergeClusterValuesCommand");

	var newInfo = info['newInfo'];

	info["newInfo"] = JSON.stringify(newInfo);
	showLoading(worksheetId);
	//alert("sending"+info);

	sendRequest(info, worksheetId);
}
