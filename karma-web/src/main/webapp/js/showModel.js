var ShowExistingModelDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#showExistingModelDialog");
		var lastWorksheetId;
		var existingWorksheetName;

		function init() {
			//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {

			});

			//Initialize handler for Save button
			//var me = this;
			$('#btnCreateNew', dialog).on('click', function(e) {
				e.preventDefault();
				createNewModelForWorksheet();

			});

			$("#btnApplySelected", dialog).on('click', function(e) {
				e.preventDefault();
				submitModelForWorksheet();
				dialog.modal('hide');
			});
		}

		function createNewModelForWorksheet() {
			var info = generateInfoObject(lastWorksheetId, "", "CreateNewModelCommand");

			showLoading(info["worksheetId"]);
			sendRequest(info, info["worksheetId"]);
			dialog.modal('hide');
		}

		function submitModelForWorksheet() {
			var info = generateInfoObject(lastWorksheetId, "", "ApplyModelFromTripleStoreCommand");

			var table = $("#modelsList", dialog);
			if ($("td.selected", table).length == 0) {
				$("div.error", optionsDiv).show();
				return false;
			}

			info["sourceName"] = $("td.selected", table).data("sourceName");
			info["modelName"] = $("td.selected span", table).text();

			showLoading(info["worksheetId"]);
			var returned = sendRequest(info, info['worksheetId']);
		}

		function saveDialog(e) {
			console.log("Save clicked");

		};

		function showIfNeeded(lastWorksheetLoaded) {
			lastWorksheetId = lastWorksheetLoaded;

			$("span.error", dialog).hide();
			$("div.noItems", dialog).hide();


			var info = generateInfoObject(lastWorksheetId, "", "FetchExistingModelsForWorksheetCommand");
			info["garbage"] = "garbage";
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);

					$.each(json["elements"], function(index, element) {
						if (element["updateType"] == "ExistingModelsList") {
							existingWorksheetName = element["worksheetName"];
							var modelsList = element["existingModelNames"];

							// Remove existing models in the table
							var table = $("#modelsList", dialog);
							$("tr", table).remove();

							if (!modelsList || modelsList.length === 0) {
								// Create new model by default if no model exists in the triple store
								createNewModelForWorksheet();
							} else {
								dialog.modal({
									keyboard: true,
									show: true,
									backdrop: 'static'
								});
								// Show a dialog box to ask user for applying an existing model
								$.each(modelsList, function(index, model) {
									var trTag = $("<tr>");
									var edgeTd = $("<td>").append($("<span>").text(model["modelName"]))
										.data("sourceName", model["sourceName"])
										.click(function() {
											$("td", table).removeClass("selected");
											$(this).addClass("selected");
										});

									trTag.append(edgeTd);
									table.append(trTag);
								});
							}
						} else if (element["updateType"] == "KarmaError") {
							$.sticky(element["Error"]);
						}
					});
				},
				error: function(xhr, textStatus) {
					$.sticky("Error occurred while setting properties!");
				}
			});

		};


		return { //Return back the public methods
			showIfNeeded: showIfNeeded,
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