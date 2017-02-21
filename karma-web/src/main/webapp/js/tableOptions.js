var SetPropertiesDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#setPropertiesDialog");
		var worksheetId;

		function init() {
			//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				hideError();
				$("#servicePostOptions").hide();
				fetchExistingWorksheetOptions();
			});

			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});



			$('#serviceRequestMethod').on("change", function(e) {
				console.log("serviceRequestMethod change: " + $(this).val());
				if ($(this).val() == "POST") {
					$("#servicePostOptions").show();
				} else {
					$("#servicePostOptions").hide();
				}
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

			// Prepare the input data
			var worksheetProps = new Object();
			worksheetProps["graphLabel"] = "";

			// Set service options if the window is visible
			if ($('#worksheetServiceOptions').is(':visible')) {
				worksheetProps["hasServiceProperties"] = true;
				worksheetProps["serviceUrl"] = $("#serviceUrlInput").val();
				worksheetProps["serviceRequestMethod"] = $("#serviceRequestMethod option:selected").text();
				if ($("#serviceRequestMethod option:selected").text() == "POST") {
					worksheetProps["serviceDataPostMethod"] = $("input:radio[name=serviceDataPostMethod]:checked").val();
				}

			} else {
				worksheetProps["hasServiceProperties"] = false;
			}

			var info = generateInfoObject(worksheetId, "", "SetWorksheetPropertiesCommand");

			var newInfo = info['newInfo']; // for input parameters
			newInfo.push(getParamObject("properties", worksheetProps, "other"));
			info["newInfo"] = JSON.stringify(newInfo);
			// Store the data to be shown later when the dialog is opened again
			$("div#" + info["worksheetId"]).data("worksheetProperties", worksheetProps);
			showLoading(worksheetId);
			var returned = sendRequest(info, worksheetId);
			hide();
		};

		function fetchExistingWorksheetOptions() {

			var info = generateInfoObject(worksheetId, "", "FetchExistingWorksheetPropertiesCommand");
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);
					var props = json["elements"][0]["properties"];

					// Set service options if present
					if (props["hasServiceProperties"]) {
						// Select the service option checkbox
						$("#serviceOptions").trigger("click");

						// Set the service URL
						if (props["serviceUrl"] != null) {
							$("#serviceUrlInput").val(props["serviceUrl"]);
						} else {
							$("#serviceUrlInput").val("");
						}

						// Set the request method
						var index = (props["serviceRequestMethod"] === "GET") ? 0 : 1;
						$('#serviceRequestMethod option').eq(index).prop('selected', true);

						// Set the POST request invocation method
						if (props["serviceRequestMethod"] === "POST") {
							$("#servicePostOptions").show();
							$(":radio[value=" + props["serviceDataPostMethod"] + "]").prop('checked', true);
						}

					} else {
						$("#serviceUrlInput").val("");
						$('#serviceRequestMethod option').eq(0).prop('selected', true);
					}
				},
				error: function(xhr, textStatus) {
					$.sticky("Error occurred while fetching worksheet properties!");
				}
			});
		}

		function hide() {
			dialog.modal('hide');
		}

		function show(wsId) {
			worksheetId = wsId;
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


var applyModelDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#applyModelDialog");
		var worksheetId;
		var availableModels;
		var filteredModels;
		var table;

		function init() {
			var dialogContent = $("#applyModelDialogColumns", dialog);
			table = $("<table>")
				.addClass("table table-striped table-condensed");
			var tr = getHeaderRow();
			table.append(tr);
			dialogContent.append(table);
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});
		}

		function getHeaderRow() {
			var tr = $("<tr>");
			var th = $("<th>"); //.addClass("CheckboxProperty");
			tr.append(th);

			var th = $("<th>"); //.addClass("FileNameProperty");
			var label = $("<label>").text("Name"); //.addClass("FileNameProperty");
			th.append(label);
			var label = $("<input>").text("")
				.addClass("form-control")
				.addClass("modelSearchControl")
				.attr("id", "txtFilterFileName_Apply")
				.attr("type", "text")
				.on('keyup', applyFilter);
			th.append(label);
			tr.append(th);

			var th = $("<th>"); //.addClass("PublishTimeProperty");
			var label = $("<label>").text("Publish Time"); //.addClass("PublishTimeProperty");
			th.append(label);
			var label = $("<input>").text("")
				.addClass("form-control")
				.addClass("modelSearchControl")
				.attr("id", "txtFilterPublishTime_Apply")
				.attr("type", "text")
				.on('keyup', applyFilter);
			th.append(label);
			tr.append(th);

			var th = $("<th>"); //.addClass("PublishTimeProperty");
			var label = $("<label>").text("URL"); //.addClass("PublishTimeProperty");
			th.append(label);
			var label = $("<input>").text("")
				.addClass("form-control")
				.addClass("modelSearchControl")
				.attr("id", "txtFilterURL_Apply")
				.attr("type", "text")
				.on('keyup', applyFilter);
			th.append(label);
			tr.append(th);

			var th = $("<th>"); //.addClass("URLProperty");
			var label = $("<label>").text("# Matched Columns"); //.addClass("URLProperty");
			th.append(label);
			var searchBtn = $("<i>").addClass("glyphicon")
				.addClass("glyphicon-search")
				.css("float", "right")
				.css("cursor", "pointer")
				.on("click", toggleSearchControls);
			th.append(searchBtn);
			var label = $("<input>").text("")
				.addClass("form-control")
				.addClass("modelSearchControl")
				.attr("id", "txtFilterMatchedColumns_Apply")
				.attr("type", "text")
				.on('keyup', applyFilter);
			th.append(label);
			tr.append(th);
			return tr;
		}

		function toggleSearchControls() {
			$(".modelSearchControl").each(function() {
				if ($(this).is(":visible")) {
					$(this).hide();
				} else {
					$(this).show();
				}
			});
		}

		function hideSearchControls() {
			$(".modelSearchControl").each(function() {
				$(this).hide();
			});
		}

		function refresh() {
			console.log("refresh");
			var info = generateInfoObject(worksheetId, "", "FetchR2RMLModelsListCommand");
			info['tripleStoreUrl'] = $('#txtModel_URL').html();
			info['graphContext'] = "";
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				async: false,
				complete: function(xhr, textStatus) {
					//alert(xhr.responseText);
					var json = $.parseJSON(xhr.responseText);
					json = json.elements[0];
					console.log(json);
					//parse(json);
					availableModels = json;
					filteredModels = availableModels;
				},
				error: function(xhr, textStatus) {
					alert("Error occured while Fetching Models!" + textStatus);
				}
			});
		}

		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError() {
			$("div.error", dialog).show();
		}

		function saveDialog(e) {
			hide();

			console.log("here");
			var checkboxes = dialog.find(":checked");
			if (checkboxes.length == 0) {
				hide();
				return;
			}
			var override = false;
			var modelExist = false;
			var info = generateInfoObject(worksheetId, "", "CheckModelExistenceCommand");
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

					//hideLoading(info["worksheetId"]);
				},
				error: function(xhr, textStatus) {

					//hideLoading(info["worksheetId"]);
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
			var checked = checkboxes[0];
			var info = generateInfoObject(worksheetId, "", "ApplyModelFromURLCommand");
			info['modelRepository'] = $('#txtModel_URL').html();
			info['modelContext'] = checked['value'];
			info['modelUrl'] = checked['src'];
			info['override'] = override;
			console.log(info["worksheetId"]);
			showLoading(info["worksheetId"]);
			var returned = sendRequest(info, worksheetId);
		};

		function hide() {
			dialog.modal('hide');
		}

		function applyFilter(e) {
			console.log("applyFilter");
			var tmp = [];
			var filterFilename = $('#txtFilterFileName_Apply').val().toLowerCase();
			var filterTime = $('#txtFilterPublishTime_Apply').val().toLowerCase();
			var filterURL = $('#txtFilterURL_Apply').val().toLowerCase();
			var filterInputs = $('#txtFilterMatchedColumns_Apply').val();
			for (var i = 0; i < availableModels.length; i++) {
				var name = availableModels[i]['name'].toLowerCase();
				var time = new Date(availableModels[i].publishTime * 1).toString();
				time = time.substring(0, time.indexOf("GMT") - 1).toLowerCase();
				var url = availableModels[i].url.toLowerCase();
				var inputColumns = availableModels[i].inputColumns;
				var flag = true;
				if (name.indexOf(filterFilename) == -1) {
					flag = false;
				} else if (time.indexOf(filterTime) == -1) {
					flag = false;
				} else if (url.indexOf(filterURL) == -1) {
					flag = false;
				} else if (filterInputs > inputColumns) {
					flag = false;
				}
				if (flag) {
					tmp.push(availableModels[i]);
				}
			}
			filteredModels = tmp;
			showFilteredModels();
		};

		function showFilteredModels() {
			table.find("tr:gt(0)").remove();
			for (var i = 0; i < filteredModels.length; i++) {
				var name = filteredModels[i]['name'];
				var time = new Date(filteredModels[i].publishTime * 1).toString();
				time = time.substring(0, time.indexOf("GMT") - 1);
				var url = filteredModels[i].url;
				var context = filteredModels[i].context;
				var inputColumns = filteredModels[i].inputColumns;
				var tr = $("<tr>");
				var td = $("<td>");
				var checkbox = $("<input>")
					.attr("type", "radio")
					.attr("id", "modelManagerCheckbox")
					.attr("name", "modelManagerCheckbox")
					.attr("value", context)
					.attr("src", url);
				td.append(checkbox);
				tr.append(td);
				var td = $("<td>");
				var label = $("<span>").text(name);
				td.append(label);
				tr.append(td);
				var td = $("<td>");
				var label = $("<span>").text(time);
				td.append(label);
				tr.append(td);
				var td = $("<td>");
				var label = $("<span>").text(url);
				td.append(label);
				tr.append(td);
				var td = $("<td>");
				var label = $("<span>").text(inputColumns);
				td.append(label);
				tr.append(td);
				table.append(tr);
			}
		}

		function show() {
			dialog.on('show.bs.modal', function(e) {
				hideError();
				showFilteredModels();
				hideSearchControls();
			});
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
		};

		function initVars(wsId) {
			worksheetId = wsId;
		}


		return { //Return back the public methods
			show: show,
			init: init,
			refresh: refresh,
			initVars: initVars
		};
	};

	function getInstance(wsId) {
		if (!instance) {
			instance = new PrivateConstructor();
			instance.init();
		}
		instance.initVars(wsId);
		instance.refresh();
		return instance;
	}

	return {
		getInstance: getInstance
	};

})();

var FetchModelDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#fetchModelDialog");
		var worksheetId;

		function init() {
			//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				hideError();
				$('#txtR2RML_URL_fetch').val('http://' + window.location.host + '/openrdf-sesame/repositories/karma_models');
			});

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
			hide();

			var info = generateInfoObject(worksheetId, "", "FetchR2RMLModelsCommand");
			info['tripleStoreUrl'] = $('#txtR2RML_URL_fetch').val();
			showLoading(worksheetId);
			var returned = sendRequest(info, worksheetId);
		};

		function hide() {
			dialog.modal('hide');
		}

		function show(wsId) {
			worksheetId = wsId;
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





var ExportCSVModelDialog = (function() {
	var instance = null;

	function setOpeartingMode(mode) {
		opeartingMode = mode;
	};

	function PrivateConstructor(mode) {
		var dialog = $("#exportCSVDialog");
		var worksheetId;
		var alignmentNodeId;
		var columnId;
		var operatingMode = mode;

		function init() {
			//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				hideError();
				$("body").css("cursor", "auto");


			});

			//			//Initialize handler for Save button
			//			$('#btnSave', dialog).on('click', function (e) {
			//				e.preventDefault();
			//				getColumnList();
			//			});

			//Initialize change handler for sparql end point text box for url
			$('input#csvSPAQRLEndPoint').on('focusout', function(event) {
				fetchGraphsFromTripleStore($("#csvSPAQRLEndPoint").val(), $("#csvModelGraphList"));
			});

			//Initialize change handler for sparql end point text box for url
			$('input#csvDataEndPoint').on('focusout', function(event) {
				fetchGraphsFromTripleStore($("#csvDataEndPoint").val(), $("#csvDataGraphList"));
			});
		}

		// this method will fetch all the context (graphs) from the given endpoint
		// it takes in the url and the input element object
		function fetchGraphsFromTripleStore(url, modelGraphList) {

			var info = generateInfoObject("", "", "FetchGraphsFromTripleStoreCommand");
			info["tripleStoreUrl"] = url;
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					console.log("got graphs ..");
					var json = $.parseJSON(xhr.responseText);
					graphs = [];
					if (json["elements"] && json["elements"][0]['graphs']) {
						graphs = json["elements"][0]['graphs'];
					}
					//var modelGraphList = $("#csvModelGraphList");
					modelGraphList.html('<option value="000">Current Worksheet</option>');
					for (var x in graphs) {
						var str = graphs[x];
						modelGraphList.append('<option value="' + graphs[x] + '">' + str + '</option>');
					}
				},
				error: function(xhr, textStatus) {
					alert("Error occurred with fetching graphs! " + textStatus);
				}
			});
		}


		function performInvokeMLService() {

			var graphUri = $('#csvDataGraphList').val().trim();

			var list = {};
			$('#csv_columns').find('li').each(function(index) {
				list[index] = {
					'name': $(this).attr('rel'),
					'url': $(this).attr('name')
				};
			});

			var info = generateInfoObject(worksheetId, "", "ExportCSVCommand");
			info["rootNodeId"] = $('#csv_columns').attr('rel');
			info["tripleStoreUrl"] = $("#csvDataEndPoint").val();
			info["graphUrl"] = graphUri;
			info["columnList"] = JSON.stringify(list);

			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);

					var fileName = json["elements"][0]['fileUrl'];

					var dmURL = $('#dataMiningUrl').val().trim();
					if (dmURL.length < 2) {
						dmURL = 'http://localhost:8088/train';
					}
					var info = generateInfoObject(worksheetId, "", "InvokeDataMiningServiceCommand");
					info["dataMiningURL"] = dmURL;
					info["csvFileName"] = fileName;
					hide();
					sendRequest(info, worksheetId);
				},
				error: function(xhr, textStatus) {
					alert("Error occurred while exporting CSV! " + textStatus);
				}
			});
		}

		function performExportCSV() {

			var graphUri = $('#csvDataGraphList').val().trim();
			//graphUri = (graphUri == '000') ? '' : graphUri;

			var list = {};
			$('#csv_columns').find('li').each(function(index) {
				list[index] = {
					'name': $(this).attr('rel'),
					'url': $(this).attr('name')
				};
			});

			var info = generateInfoObject(worksheetId, "", "ExportCSVCommand");
			info["rootNodeId"] = $('#csv_columns').attr('rel');
			info["tripleStoreUrl"] = $("#csvDataEndPoint").val();
			info["graphUrl"] = graphUri;
			info["columnList"] = JSON.stringify(list);
			hide();
			showLoading(worksheetId);
			var returned = sendRequest(info, worksheetId);
		}

		function initCSVDataDialog() {
			$("input#csvDataEndPoint").val('http://' + window.location.host + '/openrdf-sesame/repositories/karma_data');
			var url2 = 'http://' + window.location.host + '/openrdf-sesame/repositories/karma_data';
			fetchGraphsFromTripleStore(url2, $('#csvDataGraphList'));
			$('#csvDataDialogContent').show();
			console.log("initCSVDataDialog..");

		}

		// this method will fetch the columns that are reachable from this node
		function getColumnList(showCallback, dialog) {
			//    		var graphUri = $('#csvModelGraphList').val().trim();
			//    		graphUri = (graphUri == '000') ? '' : graphUri; 
			console.log("getting columns ..");
			var info = generateInfoObject(worksheetId, "", "FetchColumnCommand");
			//    		info["alignmentNodeId"] = alignmentNodeId;
			//    		info["tripleStoreUrl"] = $("#csvSPAQRLEndPoint").val();
			//    		info["graphUrl"] =  graphUri ;
			info["nodeId"] = columnId;
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					var data = $.parseJSON(xhr.responseText);
					$('#csvDialogContent').hide();

					var ele = $('#csvDialogColumnList');
					var content = '<ol id="csv_columns" rel="' + data['elements'][0]['rootId'] + '">';
					var list = data['elements'][0]['columns'];
					for (var x in list) {
						//    		   				var index = 0;
						//    		   				var str = list[x];
						//    		   				if(str.lastIndexOf('#') > 0 ) {
						//    		   					index = str.lastIndexOf('#') + 1;
						//    		   				} else if(str.lastIndexOf('/') > 0 ) {
						//    		   					index = str.lastIndexOf('/') + 1;
						//    		   				}
						//    		   				str = str.substr(index, (str.length - index));
						//    		   				
						content += '<li style="padding=4px;" name="' + list[x]['url'] + '" rel="' + list[x]['name'] + '">' + list[x]['name'] + ' &nbsp; <a class="icon-remove pull-right">X</a>' + '</li>';
						console.log("done fetching columns..");
					}
					ele.html(content + '</ol>');
					$("#csv_columns").delegate('a.icon-remove', 'click', function(event) {
						$(this).parent().remove();
					});

					//$('#btnExportCSV').show();
					$("#csv_columns").sortable();

					initCSVDataDialog();
					showCallback(dialog);
				}
			});
		}

		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError() {
			$("div.error", dialog).show();
		}

		function hide() {
			dialog.modal('hide');
		}

		function show(wsId, algnId, colId, mode) {
			worksheetId = wsId;
			alignmentNodeId = algnId;
			columnId = colId;
			operatingMode = mode;

			// here we need to get the list of columns before showing the dialog
			$("body").css("cursor", "progress");

			console.log("showing modal..");

			// var url2 = 'http://'+window.location.host + '/openrdf-sesame/repositories/karma_data';
			// $("input#csvDataEndPoint").val(url2);               
			// fetchGraphsFromTripleStore(url2, $('#csvDataGraphList') );

			//$("input#csvSPAQRLEndPoint").val('http://'+window.location.host + '/openrdf-sesame/repositories/karma_models');
			//window.csvSPAQRLEndPoint = 'http://'+window.location.host + '/openrdf-sesame/repositories/karma_models';
			//fetchGraphsFromTripleStore(window.csvSPAQRLEndPoint, $("#csvModelGraphList"));

			//Initialize handler for ExportCSV button
			$('#btnExportCSV', dialog).unbind('click');

			if (operatingMode === "invokeMLService") {
				$('#exportCSV_ModelTitle').html('Invoke Table Service');
				$('#btnExportCSV', dialog).html('Invoke');
				$('div.formDivDMUrl', dialog).show();

				//Initialize handler for ExportCSV button
				$('#btnExportCSV', dialog).on('click', function(e) {
					performInvokeMLService();
				});

			} else {
				$('#exportCSV_ModelTitle').html('Export CSV');
				$('#btnExportCSV', dialog).html('Export');
				$('div.formDivDMUrl', dialog).hide();

				//Initialize handler for ExportCSV button
				$('#btnExportCSV', dialog).on('click', function(e) {
					performExportCSV();
				});
			}

			$('#csvDialogColumnList').html('');
			$('#csvDataDialogContent').hide();

			var showDialog = function(dialog) {
				console.log("callback....executing......");
				dialog.modal({
					keyboard: true,
					show: true,
					backdrop: 'static'
				});
			};

			getColumnList(showDialog, dialog);
			//$('#csvDialogContent').show();

			//$('#csvDialogColumnList').hide();
			//$('#btnExportCSV').hide();





		};


		return { //Return back the public methods
			show: show,
			init: init
		};
	};

	function getInstance(mode) {
		if (!instance) {
			instance = new PrivateConstructor(mode);
			instance.init();
		}
		return instance;
	}

	return {
		getInstance: getInstance
	};

})();


var FoldDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#foldDialog");
		var worksheetId;

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

		function show(wsId) {
			worksheetId = wsId;
			dialog.on('show.bs.modal', function(e) {
				hideError();
				var dialogContent = $("#foldDialogColumns", dialog);
				dialogContent.empty();
				var headers = getColumnHeadingsForColumn(wsId, "", "Fold");
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

var GroupByDialog2 = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#groupByDialog2");
		var worksheetId;

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

		function show(wsId) {
			worksheetId = wsId;
			dialog.on('show.bs.modal', function(e) {
				hideError();
				var dialogContent = $("#groupByDialogColumns", dialog);
				dialogContent.empty();
				var headers = getColumnHeadingsForColumn(wsId, "", "GroupBy");
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

var GlueDialog2 = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#glueDialog2");
		var worksheetId;

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

			var checkboxes = $("#glueDialog2Columns").find(":checked");
			var checked = [];
			for (var i = 0; i < checkboxes.length; i++) {
				var checkbox = checkboxes[i];
				checked.push(getParamObject("checked", checkbox['value'], "hNodeId"));
			}
			if (checked.length == 0) {
				hide();
				return;
			}
			var selected = $("#glueDialog2ImplWays").find(":selected");
			console.log(selected.val());
			//console.log(checked);
			var info = generateInfoObject(worksheetId, checkboxes[0]['value'], "GlueCommand");

			var newInfo = info['newInfo'];
			newInfo.push(getParamObject("values", JSON.stringify(checked), "hNodeIdList"));
			newInfo.push(getParamObject("ImplMethod", selected.val(), "other"));
			info["newInfo"] = JSON.stringify(newInfo);

			showLoading(info["worksheetId"]);
			var returned = sendRequest(info, worksheetId);

			hide();
		};

		function hide() {
			dialog.modal('hide');
		}

		function show(wsId) {
			worksheetId = wsId;
			dialog.on('show.bs.modal', function(e) {
				hideError();
				var dialogContent = $("#glueDialog2Columns", dialog);
				dialogContent.empty();
				var headers = getColumnHeadingsForColumn(wsId, "", "Glue");
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
				$('#glueDialog2ImplWays option[value="Longest"]').attr("selected", true);
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

var OrganizeColumnsDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#organizeColumnsDialog");
		var _worksheetId;


		function init() {
			//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				$(".error").hide();

				var wsColumnsJson = getAllWorksheetHeaders();
				console.log(window.JSON.stringify(wsColumnsJson));

				var columns = $('#organizeColumns_body', dialog);
				var nestableDiv = $("#nestable", columns);
				nestableDiv.empty();
				createColumnList(wsColumnsJson, nestableDiv, true);
				nestableDiv.nestable({
					group: 1
				})
				//                .on("change", function(e) {
				//                	var list   = e.length ? e : $(e.target);
				//                    if (window.JSON) {
				//                        console.log(window.JSON.stringify(list.nestable('serialize')));
				//                    } else {
				//                       alert('JSON browser support required for this functionality.');
				//                    }
				//                })
				;
			});

			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
				dialog.modal('hide');
			});
		}

		function getAllWorksheetHeaders() {
			//console.log(checked);
			var info = generateInfoObject(_worksheetId, "", "GetAllWorksheetHeadersCommand");

			showLoading(info["worksheetId"]);
			var headers = [];
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				async: false,
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);
					var updates = json.elements;
					for (var i = 0; i < updates.length; i++) {
						var update = updates[i];
						if (update.updateType == "AllWorksheetHeadersUpdate") {
							headers = update.columns;
							break;
						}
					}
					hideLoading(info["worksheetId"]);
				},
				error: function(xhr, textStatus) {
					alert("Error occured while getting worksheet headers!" + textStatus);
					hideLoading(info["worksheetId"]);
				}
			});
			return headers;
		}

		function createColumnList(json, outer, parentVisible) {
			var list = $("<ol>").addClass("dd-list");
			outer.append(list);
			$.each(json, function(i, element) {
				if(!element.visible)
					element.hideable = true;

				//make all elements hideable
				element.hideable = true;

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
			var columns = $('#organizeColumns_body', dialog);
			var nestableDiv = $("#nestable", columns);
			var columnsJson = nestableDiv.nestable('serialize');

			var info = generateInfoObject(_worksheetId, "", "OrganizeColumnsCommand");

			var newInfo = info['newInfo'];
			newInfo.push(getParamObject("orderedColumns", JSON.stringify(columnsJson), "orderedColumns"));
			info["newInfo"] = JSON.stringify(newInfo);
			console.log(info);
			showLoading(info["worksheetId"]);
			var returned = sendRequest(info, _worksheetId);

		};

		function show(worksheetId) {
			_worksheetId = worksheetId;
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



var PublishJSONDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#publishJSONDialog");
		var worksheetId;

		function init() {
			//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				hideError();
			});

			//Initialize handler for Save button
			//var me = this;
			$('#btnYes', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e, true);
			});
			$('#btnNo', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e, false);
			});

		}

		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError() {
			$("div.error", dialog).show();
		}

		function saveDialog(e, importAsWorksheet) {
			hide();

			var info = generateInfoObject(worksheetId, "", "PublishJSONCommand");
			info["importAsWorksheet"] = importAsWorksheet;

			showLoading(info["worksheetId"]);
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					//alert(xhr.responseText);
					var json = $.parseJSON(xhr.responseText);
					parse(json);
					hideLoading(info["worksheetId"]);
					var lastWorksheetLoaded = $("div.Worksheet").last();
					if (lastWorksheetLoaded) {
						var lastWorksheetId = lastWorksheetLoaded.attr("id");
						ShowExistingModelDialog.getInstance().showIfNeeded(lastWorksheetId);
					}
				},
				error: function(xhr, textStatus) {
					alert("Error occured while exporting spatial data!" + textStatus);
					hideLoading(info["worksheetId"]);
				}
			});
		};

		function hide() {
			dialog.modal('hide');
		}

		function show(wsId) {
			worksheetId = wsId;
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