$('#fileupload').fileupload({
	url: "RequestController?command=ImportFileCommand",
	add: function(e, data) {
		console.log("add");
		FileFormatSelectionDialog.getInstance().show(data);
	},
	done: function(e, data) {
		console.log("done");
		parse(data.result);
	},
	fail: function(e, data) {
		$.sticky("File upload failed!");
	},
	dropZone: $(document)
});
//Enable iframe cross-domain access via redirect option:
$('#fileupload').fileupload('option', 'redirect', window.location.href.replace(/\/[^\/]*$/, '/cors/result.html?%s'));


var FileFormatSelectionDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#fileFormatSelectionDialog");

		function init() {
			//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				$("#fileFormatError").hide();
				$("input:radio[name=FileFormatSelection]").attr("checked", false);
				$("input:checkbox[name='FilterCheck']", dialog).attr('checked', false);
				var fileName = $("#fileFormatSelectionDialog").data("fileName");
				console.log("show.bs.modal::Select filename:" + fileName);
				if (fileName.match(".csv$") || fileName.match(".tsv$") ||
					fileName.match(".txt$") || fileName.match(".log$")) {
					$(":radio[name=FileFormatSelection][value=CSVFile]").prop("checked", true);
				} else if (fileName.match(".xml$")) {
					$(":radio[name=FileFormatSelection][value=XMLFile]").prop("checked", true);
				} else if (fileName.match(".xls$") || fileName.match(".xlsx$")) {
					$(":radio[name=FileFormatSelection][value=ExcelFile]").prop("checked", true);
				} else if (fileName.match(".owl$") || fileName.match(".rdf$") || fileName.match(".n3$") || fileName.match(".ttl$")) {
					$(":radio[name=FileFormatSelection][value=Ontology]").prop("checked", true);
				} else if (fileName.match(".json$")) {
					$(":radio[name=FileFormatSelection][value=JSONFile]").prop("checked", true);
				} else if (fileName.match(".jl$")) {
					$(":radio[name=FileFormatSelection][value=JSONLinesFile]").prop("checked", true);
				} else if (fileName.match(".avro$")) {
					$(":radio[name=FileFormatSelection][value=AvroFile]").prop("checked", true);
				}

				var worksheets = $('.Worksheet');
				if (worksheets.size() > 0) {
					disableRevision(false);
					worksheets.each(function() {
						var item = $('<option />');
						item.val($(this).attr('id'));
						item.text($(this).find('.WorksheetTitle').text());
						$('#revisedWorksheetSelector').append(item);
					});
				} else {
					disableRevision(true);
				}
			});

			//Initialize handler for Save button
			//var me = this;
			$('#btnSaveFormat', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});
		}


		function disableRevision(disabled) {
			$('#revisedWorksheetSelector').prop('disabled', disabled);
			$("input:checkbox[name='RevisionCheck']").prop('disabled', disabled);
		};

		function saveDialog(e) {
			console.log("Save clicked");
			var data = dialog.data("formData");
			var selectedFormat = $("input:radio[name='FileFormatSelection']:checked").val();
			console.log("Selected format:" + selectedFormat);
			if (selectedFormat == null || selectedFormat == "") {
				$("#fileFormatError").show();
				return false;
			}
			
			dialog.modal('hide');
			
			var urlString = "RequestController?workspaceId=" + $.workspaceGlobalInformation.id;

			//MVS: add the id of the revised worksheet in the request
			if ($("input:checkbox[name='RevisionCheck']").prop('checked')) {
				urlString += "&revisedWorksheet=" + $('#revisedWorksheetSelector').val();
			}
			var RequireFilter = $("input:checkbox[name='FilterCheck']", dialog).prop('checked');
			RequireFilter = RequireFilter &&
				(selectedFormat === "JSONFile" || selectedFormat === "XMLFile" || selectedFormat === "JSONLinesFile");
			urlString += "&filter=" + RequireFilter;
			urlString += "&isPreview=true";
			urlString += "&command=";
			console.log(urlString);
			$("#fileupload").fileupload({
				url: urlString + "Import" + selectedFormat + "Command",
				done: function(e, data) {
					console.log("done");
					console.log(selectedFormat);
					if (RequireFilter)
						SelectColumnsDialog.getInstance(data.result, selectedFormat).show();
					else {
						$("#fileOptionsDialog").data("isFilterSelected", $("input:checkbox[name='FilterCheck']", dialog).prop('checked'));
						FileOptionsDialog.getInstance().show(data.result, selectedFormat, "", false);
					}
				}
			});

			data.submit();
		};

		function show(data) {
			var fileName = data.files[0].name;
			dialog.data("fileName", fileName);
			dialog.data("formData", data);
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


var FileOptionsDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#fileOptionsDialog");
		var columnsJson;
		var savePreset;
		var optionSettings = {
			"JSONFile": ["colEncoding", "colMaxNumLines"],
			"JSONLinesFile": ["colEncoding", "colMaxNumLines"],
			"CSVFile": ["colDelimiterSelector", "colTextQualifier", "colHeaderStartIndex", "colStartRowIndex", "colEncoding", "colMaxNumLines"],
			"XMLFile": ["colEncoding", "colMaxNumLines"],
			"ExcelFile": ["colEncoding", "colMaxNumLines"],
			"Ontology": ["colEncoding", "colMaxNumLines"],
			"AvroFile": ["colEncoding", "colMaxNumLines"]
		};

		function init() {
			//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {

			});

			//Initialize handler for Save button
			$('#btnSaveOptions', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
				dialog.modal('hide');
			});


			$('.ImportOption').change(function() {
				reloadOptions(false);
			});
		}

		function saveDialog(e) {
			console.log("Save clicked");
			reloadOptions(true);
		}

		function reset() {
			$("#delimiterSelector :first-child", dialog).attr('selected', 'selected');
			$("#delimiterSelector :first-child", dialog).prop('selected', 'selected');
			$("#headerStartIndex", dialog).val("1");
			$("#startRowIndex", dialog).val("2");
			$("#textQualifier", dialog).val("\"");
			$("#encoding", dialog).val("\"");
			$("#maxNumLines", dialog).val("10000");
		};

		function showOptions(responseJSON) {
			console.log("ShowOptions: " + responseJSON);
			dialog.data("formData", responseJSON);
			var format = dialog.data("format");
			console.log("Got format: " + format);

			//Show only options that are relevant to the format
			$(".fileOptions").hide();
			var optionSetting = optionSettings[format];
			$.each(optionSetting, function(index, val) {
				$("#" + val).show();
			});
			if (format == "JSONFile" || format == "XMLFile" || format == "AvroFile" || format == "JSONLinesFile") {
				$('#lblMaxNumLines').text("Objects to import");
				$(".help-block", $("#colMaxNumLines")).text("Enter 0 to import all objects");
			} else {
				$('#lblMaxNumLines').text("Rows to import");
				$(".help-block", $("#colMaxNumLines")).text("Enter 0 to import all rows");
			}

			var headers = null;
			var previewTable = $("#previewTable", dialog);
			$("thead", previewTable).remove();
			$("tr", previewTable).remove();

			if (responseJSON) {
				headers = responseJSON["elements"][0]["headers"];
				var encoding = responseJSON["elements"][0]["encoding"];
				$("#encoding", dialog).val(encoding);
				var maxNumLines = responseJSON["elements"][0]["maxNumLines"];
				$("#maxNumLines", dialog).val(maxNumLines);
				if (maxNumLines == -1)
					$("#colMaxNumLines").hide();
				else
					$("#colMaxNumLines").show();

				var rows = responseJSON["elements"][0]["rows"];
				if (rows) {
					generatePreview(headers, rows);
					$("#previewTableDiv").show();
				} else {
					$("#previewTableDiv").hide();
				}
				dialog.data("commandId", responseJSON["elements"][0]["commandId"]);
			}
		}

		// Pedro added the if(index>0) to not show the index of the table.
		// It is weird to have to do this, I guess the better solution is that
		// the data that came from the server would not include the index.
		// No big deal.
		function generatePreview(headers, rows) {
			var previewTable = $("#previewTable", dialog);
			//previewTable.append($("<thead>").append("<tr>").append($("<th colspan='4'>").text("File Row Number")));
			if (headers != null) {
				var trTag = $("<tr>");
				$.each(headers, function(index, val) {
					if (index > 0) {
						trTag.append($("<th>").text(val));
					}
				});
				previewTable.append(trTag);
			} else {
				// Put empty column names
				var trTag = $("<tr>");
				$.each(rows[0], function(index, val) {
					if (index > 0) {
						trTag.append($("<th>").text("Column_" + index));
					}
				});
				previewTable.append(trTag);
			}

			$.each(rows, function(index, row) {
				var trTag = $("<tr>");
				$.each(row, function(index2, val) {
					if (index2 > 0) {
						var displayVal = val;
						if (displayVal.length > 20) {
							displayVal = displayVal.substring(0, 20) + "...";
						}
						trTag.append($("<td>").text(displayVal));
					}
				});
				previewTable.append(trTag);
			});
		}

		function reloadOptions(execute) {
			var format = dialog.data("format");
			var optionSetting = optionSettings[format];
			console.log(format);
			var options = generateInfoObject("", "", "Import" + format + "Command");
			
			options["commandId"] = dialog.data("commandId");
			if ($.inArray("colDelimiterSelector", optionSetting) != -1)
				options["delimiter"] = $("#delimiterSelector").val();
			if ($.inArray("colHeaderStartIndex", optionSetting) != -1)
				options["CSVHeaderLineIndex"] = $("#headerStartIndex").val();
			if ($.inArray("colStartRowIndex", optionSetting) != -1)
				options["startRowIndex"] = $("#startRowIndex").val();
			if ($.inArray("colTextQualifier", optionSetting) != -1)
				options["textQualifier"] = $("#textQualifier").val();
			if ($.inArray("colEncoding", optionSetting) != -1)
				options["encoding"] = $("#encoding").val();
			if ($.inArray("colMaxNumLines", optionSetting) != -1)
				options["maxNumLines"] = $("#maxNumLines").val();
			
			options["interactionType"] = "generatePreview";
			options["isUserInteraction"] = true;
			var RequireFilter = dialog.data("isFilterSelected");
			if (execute && format == "CSVFile" && RequireFilter) {
				var dialog2 = $("#selectColumnsDialog");
				options["interactionType"] = "generateFilter";
				options["filter"] = true;
				dialog2.data("commandId", options["commandId"]);
				dialog2.data("delimiter", options["delimiter"]);
				dialog2.data("CSVHeaderLineIndex", options["CSVHeaderLineIndex"]);
				dialog2.data("startRowIndex", options["startRowIndex"]);
				dialog2.data("textQualifier", options["textQualifier"]);
				dialog2.data("encoding", options["encoding"]);
				dialog2.data("maxNumLines", options["maxNumLines"]);
			} else if (execute) {
				options["execute"] = true;
				options["interactionType"] = "importTable";
				options["savePreset"] = savePreset;
				console.log(columnsJson);
				options["columnsJson"] = JSON.stringify(columnsJson);
				if (columnsJson === "")
					options["columnsJson"] = "";
				showWaitingSignOnScreen();
			}

			$.ajax({
				url: "RequestController",
				type: "POST",
				data: options,
				dataType: "json",
				complete: function(xhr, textStatus) {
					if (!execute) {
						var json = $.parseJSON(xhr.responseText);
						console.log("Got json:" + json);
						showOptions(json);
					} else if (execute && format == "CSVFile" && RequireFilter) {
						var json = $.parseJSON(xhr.responseText);
						console.log(json);
						SelectColumnsDialog.getInstance(json, format).show();
					} else {
						var json = $.parseJSON(xhr.responseText);
						parse(json);
						hideWaitingSignOnScreen();
						if (format !== "Ontology") {
							var lastWorksheetLoaded = $("div.Worksheet").last();
							if (lastWorksheetLoaded) {
								var lastWorksheetId = lastWorksheetLoaded.attr("id");
								ShowExistingModelDialog.getInstance().showIfNeeded(lastWorksheetId);
							}
						} else {
							//format is ontology, reload caches
							PropertyDialog.getInstance().reloadCache();
							ClassDialog.getInstance().reloadCache();
						}
						dialog.modal('hide');
					}
				}
			});
		}


		function show(data, format, colJson, preset) {
			reset();
			columnsJson = colJson;
			savePreset = preset;
			var fileName = data["elements"][0]["fileName"];
			$("#filename", dialog).html(fileName);
			dialog.data("format", format);
			console.log(dialog.data("isFilterSelected"));
			if (format == "CSVFile" && dialog.data("isFilterSelected"))
				$("#btnSaveOptions").text("Next");
			showOptions(data);
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
		}

		return {
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

var SelectColumnsDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#selectColumnsDialog");
		var wsJson, selectedFormat;

		function init(json, selFormat) {
			//Initialize what happens when we show the dialog
			wsJson = json;
			selectedFormat = selFormat;
			var wsColumnsJson;
			dialog.on('show.bs.modal', function(e) {
				$(".error").hide();
				$.each(json['elements'], function(i, element) {
					console.log(element);
					if (element["updateType"] === "PreviewHeaderUpdate") {
						wsColumnsJson = element['columns'];
					}
				});
				console.log(wsColumnsJson);
				var columns = $('#selectColumns_body', dialog);
				var nestableDiv = $("#nestable", columns);
				nestableDiv.empty();
				createColumnList(wsColumnsJson, nestableDiv, true);
				nestableDiv.nestable({
					group: 1
				});
			});
			if (selFormat == "CSVFile") {
				$('#btnSaveFilter', dialog).text("Import");
			}
			$('#btnSaveFilter', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
				dialog.modal('hide');
			});

			$('#presetupload').fileupload({
				url: "/",
				add: function(e, data) {
					console.log("add");
					console.log(data);
					loadPreset(data.files);
				}
			});
		}


		function createColumnList(json, outer, parentVisible) {
			var list = $("<ol>").addClass("dd-list");
			outer.append(list);
			$.each(json, function(i, element) {
				var li = $("<li>").addClass("dd-item")
					.attr("data-name", element.name)
					.attr("data-id", element.id)
					.attr("data-visible", element.visible)
					.attr("data-hideable", element.hideable)
					.attr("data-toggle", "tooltip")
					.attr("data-placement", "auto bottom");

				var eye = $("<span>").addClass("glyphicon").css("margin-right", "5px");
				if (element.visible) {
					eye.addClass("glyphicon-eye-open")
				} else {
					eye.addClass("glyphicon-eye-close");
				}
				var eyeOuter = $("<span>");
				eyeOuter.append(eye);
				var div = $("<div>").addClass("dd-handle").append(eyeOuter).append(element.name);
				if (!parentVisible) {
					div.addClass("dd-handle-hide-all");
					li.addClass("dd-item-hidden-all");
				} else if (!element.visible) {
					div.addClass("dd-handle-hide");
					li.attr("title", element.name)
					li.addClass("dd-item-hidden");
				}
				if (!element.hideable) {
					eye.css("color", "#DDDDDD");
					eye.addClass("glyphicon-noclick");
				}

				list.append(li);
				li.append(div);

				if (element.children) {
					if (element.children.length > 0)
						createColumnList(element.children, li, element.visible && parentVisible);
				}
			});

			$(".dd-item-hidden").tooltip(); //activate the bootstrap tooltip
		}

		function saveDialog(e) {
			console.log("Save clicked");
			var columns = $('#selectColumns_body', dialog);
			var nestableDiv = $("#nestable", columns);
			var columnsJson = nestableDiv.nestable('serialize');
			var savePreset = $("input:checkbox[name='SavePresetCheck']", dialog).prop('checked');
			console.log(columnsJson);
			if (selectedFormat == "CSVFile") {
				var options = generateInfoObject("", "", "Import" + selectedFormat + "Command");
				options["commandId"] = dialog.data("commandId");
				options["delimiter"] = dialog.data("delimiter");
				options["CSVHeaderLineIndex"] = dialog.data("CSVHeaderLineIndex");
				options["startRowIndex"] = dialog.data("startRowIndex");
				options["textQualifier"] = dialog.data("textQualifier");
				options["encoding"] = dialog.data("encoding");
				options["maxNumLines"] = dialog.data("maxNumLines");
				options["isUserInteraction"] = true;
				options["execute"] = true;
				options["interactionType"] = "importTable";
				options["savePreset"] = savePreset;
				console.log(columnsJson);
				options["columnsJson"] = JSON.stringify(columnsJson);
				if (columnsJson === "")
					options["columnsJson"] = "";
				showWaitingSignOnScreen();
				$.ajax({
					url: "RequestController",
					type: "POST",
					data: options,
					dataType: "json",
					complete: function(xhr, textStatus) {
						var json = $.parseJSON(xhr.responseText);
						parse(json);
						hideWaitingSignOnScreen();
						if (selectedFormat !== "Ontology") {
							var lastWorksheetLoaded = $("div.Worksheet").last();
							if (lastWorksheetLoaded) {
								var lastWorksheetId = lastWorksheetLoaded.attr("id");
								ShowExistingModelDialog.getInstance().showIfNeeded(lastWorksheetId);
							}
						}
						dialog.modal('hide');
					}
				});
			} else
				FileOptionsDialog.getInstance().show(wsJson, selectedFormat, columnsJson, savePreset);
		};

		function loadPreset(filelist) {
			console.log("load preset");
			for (var i = 0; i < filelist.length; i++) {
				var file = filelist[i];
				if (file.size < 1024 * 1024) {
					var reader = new FileReader();
					reader.onload = function(e) {
						var json;
						try {
							json = $.parseJSON(e.target.result);
							console.log(json);
						} catch (err) {

						}
						if (json != undefined) {
							var wsColumnsJson;
							$.each(wsJson['elements'], function(i, element) {
								if (element["updateType"] === "PreviewHeaderUpdate") {
									wsColumnsJson = element['columns'];
								}
							});
							var copyOfColumnsJson = $.parseJSON(JSON.stringify(wsColumnsJson));
							if (compareJSON(copyOfColumnsJson, json)) {
								console.log("is identical");
								console.log(copyOfColumnsJson);
								var columns = $('#selectColumns_body', dialog);
								var nestableDiv = $("#nestable", columns);
								nestableDiv.empty();
								createColumnList(copyOfColumnsJson, nestableDiv, true);
								nestableDiv.nestable({
									group: 1
								});
							}
						}
					};
					reader.readAsText(file);
				}
			}
		};

		function show() {
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
		};

		function compareJSON(org_json, preset) {
			if (org_json == undefined) {
				return false;
			}
			if (!$.isArray(preset)) {
				return false;
			}
			if (org_json.length < preset.length) {
				return false;
			}
			for (var i = 0; i < preset.length; i++) {
				var obj_preset = preset[i];
				var index = getCorrespondingIndex(obj_preset, org_json);
				if (index == -1) {
					return false;
				}
				var obj_org = org_json[index];
				obj_org['visible'] = obj_preset['visible'];
				if (obj_preset['children'] != undefined && obj_org['children'] == undefined) {
					return false;
				}
				if (obj_preset['children'] != undefined) {
					if (!compareJSON(obj_org['children'], obj_preset['children'])) {
						return false;
					}
				}
			}
			return true;
		};

		function getCorrespondingIndex(obj, preset) {
			for (var i = 0; i < preset.length; i++) {
				if (obj['name'] == preset[i]['name'])
					return i;
			}
			return -1;
		};

		return { //Return back the public methods
			show: show,
			init: init
		};
	};

	function getInstance(json, selectedFormat) {
		if (!instance) {
			instance = new PrivateConstructor();
		}
		instance.init(json, selectedFormat);
		return instance;
	}

	return {
		getInstance: getInstance
	};

})();