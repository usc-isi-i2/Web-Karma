$(document).on("click", "#modelManagerButton", function() {
	console.log("Manage Model");
	modelManagerDialog.getInstance().show();
});

$('#txtModel_URL').text('http://' + window.location.host + '/openrdf-sesame/repositories/karma_models');
$('#txtData_URL').text('http://' + window.location.host + '/openrdf-sesame/repositories/karma_data');
$('#txtData_URL').editable({
	type: 'text',
	pk: 1,
	success: function(response, newValue) {
		console.log("Set new value:" + newValue);
		$('#txtData_URL').text(newValue);
	},
	title: 'Enter Data Endpoint'
});

$('#txtModel_URL').editable({
	type: 'text',
	pk: 1,
	success: function(response, newValue) {
		console.log("Set new value:" + newValue);
		$('#txtModel_URL').text(newValue);
	},
	title: 'Enter Model Endpoint'
});

var saveModelDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#saveModelDialog");

		function init() {
			//Initialize what happens when we show the dialog

			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});
			console.log(window.location);
			$('#txtGraph_URL_Save', dialog).val(window.location.protocol + "//" + window.location.host + window.location.pathname + "R2RMLMapping/local");
			$('#txtModel_URL_Save', dialog).on('keyup', function(e) {
				//$('#txtGraph_URL_Save').val($('#txtModel_URL_Save').val());
				$('input[name="buttonCollection_Save"][value="URL"]').prop('checked', true);
			});

			$('#txtGraph_URL_Save', dialog).on('keyup', function(e) {
				$('input[name="buttonCollection_Save"][value="Collection"]').prop('checked', true);
			});

			$('#txtModel_URL_Save', dialog).bind('input paste', function(e) {
				console.log("here");
				console.log($('#txtModel_URL_Save').val());
				$('input[name="buttonCollection_Save"][value="URL"]').prop('checked', true);
				// $('#txtGraph_URL_Save').val($('#txtModel_URL_Save').val());
			});

			$('#txtModel_URL_Save', dialog).on('change', function(e) {
				// $('#txtGraph_URL_Save').val($('#txtModel_URL_Save').val());
			});


		}

		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError() {
			$("div.error", dialog).show();
		}

		function saveDialog(e) {

			var checkboxes = dialog.find(":checked");
			if ($('#txtGraph_URL_Save').val() === '' && checkboxes[0]['value'] === 'Collection') {
				alert("Please enter the Collection");
				return;
			}
			if ($('#txtModel_URL_Save').val() === '' && checkboxes[0]['value'] === 'URL') {
				alert("Please enter the model URL");
				return;
			}
			if ($('#txtModel_URL_Save').val() === '' && $('#txtGraph_URL_Save').val() === '') {
				alert("Please enter the model URL or Collection");
				return;
			}

			hide();

			var info = generateInfoObject("", "", "SaveR2RMLModelCommand");
			info['tripleStoreUrl'] = $('#txtModel_URL').html();
			info['modelUrl'] = $('#txtModel_URL_Save').val();
			info['graphContext'] = $('#txtGraph_URL_Save').val();
			info['collection'] = checkboxes[0]['value'];
			console.log(info['collection']);
			showWaitingSignOnScreen();
			var returned = sendRequest(info);
		};

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

var clearModelDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#clearModelDialog");

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
			hide();

			var info = generateInfoObject("", "", "ClearTripleStoreCommand");
			info['tripleStoreUrl'] = $('#txtModel_URL').html();
			info['graphContext'] = $('#txtGraph_URL_Clear').val();
			showWaitingSignOnScreen();
			var returned = sendRequest(info);
		};

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

var modelManagerDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#modelManagerDialog");
		var availableModels;
		var filteredModels;
		var table;

		function init() {
			//Initialize what happens when we show the dialog
			refresh();
			var dialogContent = $("#modelManagerDialogColumns", dialog);
			table = $("<table>")
				.addClass("table table-striped table-condensed");
			var tr = getHeaderRow();
			table.append(tr);
			dialogContent.append(table);

			$('#btnAddModel', dialog).on('click', function(e) {
				e.preventDefault();
				hide();
				saveModelDialog.getInstance().show();
			});

			//            $('#btnClearModel', dialog).on('click', function (e) {
			//                e.preventDefault();
			//                hide();
			//                clearModelDialog.getInstance().show();
			//            });

			$('#btnRemoveModel', dialog)
				.on('click', deleteModel);

			$('#btnRefreshModel', dialog)
				.on('click', refreshModel);
		}

		function onClickSelectAllCheckbox() {
			var checked = $("#modelManagerSelectAllCheckbox").prop("checked");

			$(".modelManagerCheckbox").each(function() {
				$(this).prop("checked", checked);
			});
		}

		function getHeaderRow() {
			var tr = $("<tr>");
			var th = $("<th>"); //.addClass("CheckboxProperty");
			var checkbox = $("<input>")
				.attr("type", "checkbox")
				.attr("id", "modelManagerSelectAllCheckbox")
				.prop('checked', false)
				.change(onClickSelectAllCheckbox);
			th.append(checkbox);
			tr.append(th);

			var th = $("<th>"); //.addClass("FileNameProperty");
			var label = $("<label>").text("Name"); //.addClass("FileNameProperty");
			th.append(label);
			var label = $("<input>").text("")
				.addClass("form-control")
				.addClass("modelSearchControl")
				.attr("id", "txtFilterFileName")
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
				.attr("id", "txtFilterPublishTime")
				.attr("type", "text")
				.on('keyup', applyFilter);
			th.append(label);
			tr.append(th);

			var th = $("<th>"); //.addClass("URLProperty");
			var label = $("<label>").text("URL"); //.addClass("URLProperty");
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
				.attr("id", "txtFilterURL")
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
			var info = generateInfoObject("", "", "FetchR2RMLModelsListCommand");
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
					availableModels = json;
					filteredModels = availableModels;
				},
				error: function(xhr, textStatus) {
					alert("Error occured while Fetching Models!" + textStatus);
					hideLoading(info["worksheetId"]);
				}
			});
		}

		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError() {
			$("div.error", dialog).show();
		}

		function applyFilter(e) {
			console.log("applyFilter");
			var tmp = [];
			var filterFilename = $('#txtFilterFileName').val().toLowerCase();
			var filterTime = $('#txtFilterPublishTime').val().toLowerCase();
			var filterURL = $('#txtFilterURL').val().toLowerCase();
			for (var i = 0; i < availableModels.length; i++) {
				var name = availableModels[i]['name'].toLowerCase();
				var time = new Date(availableModels[i].publishTime * 1).toString();
				time = time.substring(0, time.indexOf("GMT") - 1).toLowerCase();
				var url = availableModels[i].url.toLowerCase();
				var flag = true;
				if (name.indexOf(filterFilename) == -1) {
					flag = false;
				} else if (time.indexOf(filterTime) == -1) {
					flag = false;
				} else if (url.indexOf(filterURL) == -1) {
					flag = false;
				}
				if (flag) {
					tmp.push(availableModels[i]);
				}
			}
			filteredModels = tmp;

			showFilteredModels();
		};

		function hide() {
			dialog.modal('hide');
		}

		function deleteModel(e) {
			e.preventDefault();
			var checkboxes = dialog.find(".modelManagerCheckbox:checked");
			if (checkboxes.length == 0) {
				alert("Please select the models to delete");
				return false;
			}

			for (var i = 0; i < checkboxes.length; i++) {
				var checkbox = checkboxes[i];
				var info = generateInfoObject("", "", "DeleteModelFromTripleStoreCommand");
				info['tripleStoreUrl'] = $('#txtModel_URL').html();
				info['graphContext'] = checkbox['value'];
				info['mappingURI'] = checkbox['src'];
				console.log(info['graphContext']);
				console.log(info['mappingURI']);
				var returned = $.ajax({
					url: "RequestController",
					type: "POST",
					data: info,
					dataType: "json",
					async: false,
					complete: function(xhr, textStatus) {
						var json = $.parseJSON(xhr.responseText);
					},
					error: function(xhr, textStatus) {
						alert("Error occured while clearing model!" + textStatus);
					}
				});
			}
			refresh();
			instance.show();
		}

		function refreshModel(e) {
			e.preventDefault();
			var checkboxes = dialog.find(".modelManagerCheckbox:checked");
			if (checkboxes.length == 0) {
				alert("Please select the models to refresh");
				return false;
			}

			for (var i = 0; i < checkboxes.length; i++) {
				var checkbox = checkboxes[i];
				var info = generateInfoObject("", "", "RefreshModelFromTripleStoreCommand");
				info['tripleStoreUrl'] = $('#txtModel_URL').html();
				info['graphContext'] = checkbox['value'];
				info['mappingURI'] = checkbox['src'];
				var returned = $.ajax({
					url: "RequestController",
					type: "POST",
					data: info,
					dataType: "json",
					async: false,
					complete: function(xhr, textStatus) {
						//alert(xhr.responseText);
						var json = $.parseJSON(xhr.responseText);
						//parse(json);
					},
					error: function(xhr, textStatus) {
						alert("Error occured while clearing model!" + textStatus);
					}
				});
			}
			refresh();
			instance.show();
		}

		function disableButton(e) {
			//            var checkboxes = dialog.find(":checked");
			//            if (checkboxes.length == 0) {
			//                $('#btnDeleteModel', dialog)
			//                    .attr("disabled", "disabled");
			//
			//                $('#btnRefreshModel', dialog)
			//                    .attr("disabled", "disabled");
			//            }
			//            else {
			//                $('#btnDeleteModel', dialog)
			//                    .removeAttr("disabled");
			//
			//                $('#btnRefreshModel', dialog)
			//                    .removeAttr("disabled");
			//            }
		}

		function showFilteredModels() {
			table.find("tr:gt(0)").remove();

			console.log(filteredModels.length);
			for (var i = 0; i < filteredModels.length; i++) {
				var name = filteredModels[i]['name'];
				var time = new Date(filteredModels[i].publishTime * 1).toString();
				time = time.substring(0, time.indexOf("GMT") - 1);
				var url = filteredModels[i].url;
				var context = filteredModels[i].context;
				var tr = $("<tr>");
				var td = $("<td>");
				//.addClass("CheckboxProperty");
				var checkbox = $("<input>")
					.attr("type", "checkbox")
					.attr("id", "modelManagerCheckbox")
					.addClass("modelManagerCheckbox")
					.attr("value", context)
					.attr("src", url)
					.change(disableButton);
				td.append(checkbox);
				tr.append(td);
				var td = $("<td>");
				// .addClass("FileNameProperty");
				var label = $("<span>").text(name);
				//.addClass("FileNameProperty");
				td.append(label);
				tr.append(td);
				var td = $("<td>")
					//.css("overflow", "scroll");
					//.addClass("PublishTimeProperty");
				var label = $("<span>").text(time);
				// .addClass("PublishTimeProperty");
				td.append(label);
				tr.append(td);
				var td = $("<td>"); //.addClass("URLProperty");
				var label = $("<span>").text(url);
				//.addClass("URLProperty");
				td.append(label);
				tr.append(td);
				table.append(tr);
			}
		}

		function show() {
			showFilteredModels();
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
			hideSearchControls();
		};


		return { //Return back the public methods
			show: show,
			init: init,
			refresh: refresh
		};
	};

	function getInstance() {
		if (!instance) {
			instance = new PrivateConstructor();
			instance.init();
		}
		console.log(instance);
		instance.refresh();
		return instance;
	}

	return {
		getInstance: getInstance
	};

})();

var ApplyR2RMLModelFromUrlDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#applyR2RMLModelFromUrlDialog");
		var worksheetId;
		
		function init(p_worksheetId) {
			//Initialize what happens when we show the dialog
			worksheetId = p_worksheetId;
			
			dialog.on('show.bs.modal', function(e) {
				var info = generateInfoObject(worksheetId, "", "GetR2RMLModelURLsCommand");
				info["command"] = "GetR2RMLModelURLsCommand";
				var returned = $.ajax({
					url: "RequestController",
					type: "POST",
					data: info,
					dataType: "json",
					async: false,
					complete: function(xhr, textStatus) {
						var json = $.parseJSON(xhr.responseText);
						var models = json["elements"][0].models;
						console.log(json);
						console.log(models);
						$('.typeahead').typeahead('destroy');
						$("input#txtModelURL", dialog).typeahead('destroy');
						$("input#txtModelURL", dialog).typeahead({
							source: models,
							minLength: 0
						});
					},
					error: function(xhr, textStatus) {

					}
				});
			});
			
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				if(validateInput()) {
					dialog.modal('hide');
					window.setTimeout(function() {
						saveDialog(e);
					}, 10);
				}
			});
		}

		function showError() {
			$('.error', dialog).show();
		}
		
		function hideError() {
			$('.error', dialog).hide();
		}
		
		function validateInput() {
			var url = $("#txtModelURL", dialog).val();
			if(!isValidUrl(url)) {
				showError();
				return false;
			}
			hideError();
			return true;
		}

		function saveDialog(e) {
			console.log("Save clicked");
			
			var url = $("#txtModelURL", dialog).val();
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
			
			var info2 = generateInfoObject(worksheetId, "", "ApplyHistoryFromR2RMLModelCommand");
			info2["newInfo"].push(getParamObject("override", override, "other"));
			info2["newInfo"].push(getParamObject("url", url, "other"));
			info2["override"] = override;
			info2["url"] = url;
			info2["command"] = "ApplyHistoryFromR2RMLModelCommand";
			showLoading(worksheetId);
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info2,
				dataType: "json",
				async: false,
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);
					parse(json);
					hideLoading(worksheetId);

				},
				error: function(xhr, textStatus) {
					hideLoading(worksheetId);
					alert("Error applying model:" + textStatus);
				}
			});

		};

		

		function show() {
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

	function getInstance(worksheetId) {
		if (!instance) {
			instance = new PrivateConstructor();
			instance.init(worksheetId);
		}
		return instance;
	}

	return {
		getInstance: getInstance
	};

})();
