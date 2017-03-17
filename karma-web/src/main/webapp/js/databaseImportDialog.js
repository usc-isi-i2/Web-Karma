/*******************************************************************************
 * Copyright 2012 University of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This code was developed by the Information Integration Group as part
 * of the Karma project at the Information Sciences Institute of the
 * University of Southern California.  For more information, publications,
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

$(document).on("click", "#importDatabaseTableButton", function() {
	console.log("Import From Database");
	DatabaseImportDialog.getInstance().show();
});

var DatabaseImportDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#databaseImportDialog");

		function init() {
			dialog.on('show.bs.modal', function(e) {
				$("#listAndPreview", dialog).hide();
				$("#dbPreviewTableName", dialog).text("");
				$("#DatabasePassword", dialog).val("");
				$("#databaseImportError", dialog).html("");
				getSavedPreferences();
			});

			$("#databaseTypeSelector", dialog).change(function() {
				if ($("#databaseTypeSelector option:selected", dialog).text() == "Oracle") {
					$("#lblDatabase", dialog).text("SID/Service Name");
				} else {
					$("#lblDatabase", dialog).text("Database");
				}
			});

			// Filter for the table names
			$("#databaseTableFilterTable", dialog).keyup(function(event) {
				// fire the above change event after every letter

				//if esc is pressed or nothing is entered  
				if (event.keyCode == 27 || $(this).val() == '') {
					//if esc is pressed we want to clear the value of search box  
					$(this).val('');

					//we want each row to be visible because if nothing  
					//is entered then all rows are matched.  
					$('tr').removeClass('visible').show().addClass('visible');
				}

				//if there is text, lets filter
				else {
					filter('#databaseImportDialog #tablesDiv table tr', $(this).val(), "tableName");
				}
			});

			$('#btnConnect', dialog).on('click', function(e) {
				e.preventDefault();
				hideErrorMsg();
				$("#listAndPreview", dialog).hide();

				var err = validate();
				if (err != null) {
					showErrorMsg(err);
					return false;
				}
				generateTableList();
			});
		}

		function validate() {
			if ($.trim($("#DatabaseHostName", dialog).val()) == "" || $.trim($("#DatabasePortNumber", dialog).val()) == "" || $.trim($("#DatabaseUsername", dialog).val()) == "" || $.trim($("#DatabasePassword", dialog).val()) == "" || $.trim($("#DatabaseName", dialog).val()) == "") {
				return "No field can be left empty!";
			} else if (!isNumeric($.trim($("#DatabasePortNumber", dialog).val()))) {
				return "Port number should be positive whole number!";
			}
			return null;
		}

		function hideErrorMsg() {
			$("#databaseImportError", dialog).html("");
			$("#databaseImportError", dialog).hide();
		}

		function showErrorMsg(msg) {
			$("#databaseImportError", dialog).html(msg);
			$("#databaseImportError", dialog).show();
		}

		function getSavedPreferences() {
			var table = $("#DatabaseTablesList table", dialog);
			$("tr", table).remove();

			var info = generateInfoObject("", "", "ImportDatabaseTableCommand");
			info["interactionType"] = "getPreferencesValues";
			info["isPreview"] = true;
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);
					$.each(json["elements"], function(index, element) {
						if (element["updateType"] == "ImportDatabaseTableCommandPreferences") {
							var commandId = element["commandId"];
							dialog.data("commandId", commandId);

							if (element["PreferenceValues"]) {
								$("#databaseTypeSelector", dialog).val(element["PreferenceValues"]["dbType"]);
								$("#DatabaseHostName", dialog).val(element["PreferenceValues"]["hostname"]);
								$("#DatabasePortNumber", dialog).val(element["PreferenceValues"]["portnumber"]);
								$("#DatabaseUsername", dialog).val(element["PreferenceValues"]["username"]);
								$("#DatabaseName", dialog).val(element["PreferenceValues"]["dBorSIDName"]);
							}
						}
					});
				},
				error: function(xhr, textStatus) {
					alert("Error occured with fetching new rows! " + textStatus);
				}
			});
		}

		function generateTableList() {
			// Prepare the data to be sent to the server	
			var info = generateInfoObject("", "", "ImportDatabaseTableCommand");
			info["dBType"] = $("#databaseTypeSelector option:selected", dialog).text();
			info["hostname"] = $.trim($("#DatabaseHostName", dialog).val());
			info["portNumber"] = $.trim($("#DatabasePortNumber", dialog).val());
			info["username"] = $.trim($("#DatabaseUsername", dialog).val());
			info["password"] = $.trim($("#DatabasePassword", dialog).val());
			info["dBorSIDName"] = $.trim($("#DatabaseName", dialog).val());
			info["commandId"] = dialog.data("commandId");
			info["interactionType"] = "generateTableList";
			info["isUserInteraction"] = true;
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);
					if (json["elements"][0]["updateType"] == "KarmaError")
						$.each(json["elements"], function(index, element) {
							if (element["updateType"] == "KarmaError") {
								showErrorMsg(element["Error"]);
							}

						});
					else if (json["elements"][0]["updateType"] == "GetDatabaseTableList") {
						$("#listAndPreview", dialog).show();
						var table = $("#tablesDiv table", dialog);
						$("tr", table).remove();

						// Clear the existing table preview if any
						$("#previewDiv table tr", dialog).remove();
						$("#dbPreviewTableName", dialog).text("");

						table.data("commandId", json["elements"][0]["commandId"]);
						$.each(json["elements"][0]["TableList"][0], function(index, value) {
							var tr = $("<tr>").data("tableName", value);
							var tableNametd2 = $("<td>");
							tableNametd2.text(value).append("&nbsp; &nbsp; &nbsp;");
							tr.append(tableNametd2);

							var tableNametd = $("<td>");
							var importButton = $("<button>").text("Import");
							var previewButton = $("<button>").text("Preview");
							tableNametd.append(importButton).append("&nbsp;&nbsp;");
							tableNametd.append(previewButton);

							importButton.hide();
							previewButton.hide();
							importButton.click(sendImportTableRequest);
							previewButton.click(sendPreviewTableRequest);

							tableNametd.addClass("text-right");
							table.append(tr.append(tableNametd));

							tr.hover(function() {
									importButton.show();
									previewButton.show();
								},
								function() {
									importButton.hide();
									previewButton.hide();
								});
						});


					} else {

					}
				},
				error: function(xhr, textStatus) {
					alert("Error occured while getting table list! " + textStatus);
				}
			});
		}

		//filter results
		function filter(selector, query, dataAttribute) {
			query = $.trim(query); //trim white space
			query = query.replace(/ /gi, '|'); //add OR for regex query


			$(selector).each(function() {
				($(this).data(dataAttribute).search(new RegExp(query, "i")) < 0) ? $(this).hide().removeClass('visible') : $(this).show().addClass('visible');
			});
		}

		function isNumeric(input) {
			return /^ *[0-9]+ *$/.test(input);
		}

		function sendImportTableRequest() {
			var info = generateInfoObject("", "", "ImportDatabaseTableCommand");
			info["commandId"] = dialog.data("commandId");
			info["tableName"] = $(this).parents("tr").data("tableName");
			info["interactionType"] = "importTable";
			info["isUserInteraction"] = true;
			info["execute"] = true;

			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					//alert(xhr.responseText);
					var json = $.parseJSON(xhr.responseText);
					var flag = 0;
					$.each(json["elements"], function(index, element) {
						if (element["updateType"] == "KarmaError") {
							showDatabaseImportError(json);
							flag = -1;
						}
					});

					if (flag != -1) {
						parse(json);
						alert("Table imported in the workspace!");
						var lastWorksheetLoaded = $("div.Worksheet").last();
						if (lastWorksheetLoaded) {
							var lastWorksheetId = lastWorksheetLoaded.attr("id");
							ShowExistingModelDialog.getInstance().showIfNeeded(lastWorksheetId);
						}
					}

				},
				error: function(xhr, textStatus) {
					alert("Error occured with fetching new rows! " + textStatus);
				}
			});
		}

		function sendPreviewTableRequest() {
			var info = generateInfoObject("", "", "ImportDatabaseTableCommand");
			info["commandId"] = $(this).parents("table").data("commandId");
			info["tableName"] = $(this).parents("tr").data("tableName");
			info["interactionType"] = "previewTable";
			info["isUserInteraction"] = true;
			// options["execute"] = true;

			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					//alert(xhr.responseText);
					var json = $.parseJSON(xhr.responseText);
					handleDBTablePreviewResponse(json);
				},
				error: function(xhr, textStatus) {
					alert("Error occured with fetching new rows! " + textStatus);
				}
			});
		}

		function handleDBTablePreviewResponse(json) {
			$("#previewDiv table tr", dialog).remove();
			$("#dbPreviewTableName", dialog).text("");
			if (json["elements"][0]["updateType"] == "ImportDatabaseTablePreview") {
				var table = $("#previewDiv table", dialog);

				// Add the headers
				var headerTR = $("<tr>");
				$.each(json["elements"][0]["headers"], function(index, header) {
					var th = $("<th>").text(header);
					headerTR.append(th);
				});
				table.append(headerTR);

				// Add the data
				$.each(json["elements"][0]["rows"], function(index, row) {
					var tr = $("<tr>");
					$.each(row, function(index2, cell) {
						var td = $("<td>");
						if (cell != null)
							td.text(cell);
						else
							td.text("");
						tr.append(td);
					});
					table.append(tr);
				});
				$("#dbPreviewTableName", dialog).text("Preview of " +
					json["elements"][0]["tableName"] + " (Only Top 10 rows shown)");
			}
		}

		function showDatabaseImportError(json) {
			$("#listAndPreview", dialog).hide();

			$.each(json["elements"], function(index, element) {
				if (element["updateType"] == "KarmaError") {
					showErrorMsg(element["Error"]);
				}

			});

		}

		function show(data) {
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





$(document).on("click", "#importSQLButton", function() {
	console.log("Import Using SQL");
	SQLImportDialog.getInstance().show();
});

var SQLImportDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#sqlImportDialog");

		function init() {
			dialog.on('show.bs.modal', function(e) {
				$("#DatabasePassword", dialog).val("");
				$("#databaseImportError", dialog).html("");
				getSavedPreferences();
			});

			$("#databaseTypeSelector", dialog).change(function() {
				if ($("#databaseTypeSelector option:selected", dialog).text() == "Oracle") {
					$("#lblDatabase", dialog).text("SID/Service Name");
				} else {
					$("#lblDatabase", dialog).text("Database");
				}
			});


			$('#btnImport', dialog).on('click', function(e) {
				e.preventDefault();
				hideErrorMsg();
				var err = validate();
				if (err != null) {
					showErrorMsg(err);
					return false;
				}
				sendImportRequest();
			});
		}

		function validate() {
			if ($.trim($("#DatabaseHostName", dialog).val()) == "" || $.trim($("#DatabasePortNumber", dialog).val()) == "" || $.trim($("#DatabaseUsername", dialog).val()) == "" || $.trim($("#DatabasePassword", dialog).val()) == "" || $.trim($("#DatabaseName", dialog).val()) == "") {
				return "No field can be left empty!";
			} else if (!isNumeric($.trim($("#DatabasePortNumber", dialog).val()))) {
				return "Port number should be positive whole number!";
			}
			return null;
		}

		function hideErrorMsg() {
			$("#databaseImportError", dialog).html("");
			$("#databaseImportError", dialog).hide();
		}

		function showErrorMsg(msg) {
			$("#databaseImportError", dialog).html(msg);
			$("#databaseImportError", dialog).show();
		}

		function getSavedPreferences() {
			var info = generateInfoObject("", "", "ImportSQLCommand");
			info["interactionType"] = "getPreferencesValues";
			info["isPreview"] = true;
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);
					$.each(json["elements"], function(index, element) {
						if (element["updateType"] == "ImportSQLCommandPreferences") {
							var commandId = element["commandId"];
							dialog.data("commandId", commandId);

							if (element["PreferenceValues"]) {
								$("#databaseTypeSelector", dialog).val(element["PreferenceValues"]["dbType"]);
								$("#DatabaseHostName", dialog).val(element["PreferenceValues"]["hostname"]);
								$("#DatabasePortNumber", dialog).val(element["PreferenceValues"]["portnumber"]);
								$("#DatabaseUsername", dialog).val(element["PreferenceValues"]["username"]);
								$("#DatabaseName", dialog).val(element["PreferenceValues"]["dBorSIDName"]);
								$("#DatabaseQuery", dialog).val(element["PreferenceValues"]["query"]);
							}
						}
					});
				},
				error: function(xhr, textStatus) {
					alert("Error occured with fetching new rows! " + textStatus);
				}
			});
		}

		//filter results
		function filter(selector, query, dataAttribute) {
			query = $.trim(query); //trim white space
			query = query.replace(/ /gi, '|'); //add OR for regex query


			$(selector).each(function() {
				($(this).data(dataAttribute).search(new RegExp(query, "i")) < 0) ? $(this).hide().removeClass('visible') : $(this).show().addClass('visible');
			});
		}

		function isNumeric(input) {
			return /^ *[0-9]+ *$/.test(input);
		}

		function sendImportRequest() {
			var info = generateInfoObject("", "", "ImportSQLCommand");
			info["interactionType"] = "importSQL";
			info["commandId"] = dialog.data("commandId");
			info["execute"] = true;
			info["isUserInteraction"] = true;
			info["dBType"] = $("#databaseTypeSelector option:selected", dialog).text();
			info["hostname"] = $.trim($("#DatabaseHostName", dialog).val());
			info["portNumber"] = $.trim($("#DatabasePortNumber", dialog).val());
			info["username"] = $.trim($("#DatabaseUsername", dialog).val());
			info["password"] = $.trim($("#DatabasePassword", dialog).val());
			info["dBorSIDName"] = $.trim($("#DatabaseName", dialog).val());
			info["query"] = $.trim($("#DatabaseQuery", dialog).val());

			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					//alert(xhr.responseText);
					var json = $.parseJSON(xhr.responseText);
					var flag = 0;
					$.each(json["elements"], function(index, element) {
						if (element["updateType"] == "KarmaError") {
							showDatabaseImportError(json);
							getSavedPreferences(); //Get this again to get the latest command Id
							flag = -1;
						}
					});

					if (flag != -1) {
						parse(json);
						hide();
						//alert("Data using the SQL query imported in the workspace!");
						var lastWorksheetLoaded = $("div.Worksheet").last();
						if (lastWorksheetLoaded) {
							var lastWorksheetId = lastWorksheetLoaded.attr("id");
							ShowExistingModelDialog.getInstance().showIfNeeded(lastWorksheetId);
						}
					}

				},
				error: function(xhr, textStatus) {
					alert("Error occured with fetching new rows! " + textStatus);
				}
			});
		}

		function showDatabaseImportError(json) {
			$.each(json["elements"], function(index, element) {
				if (element["updateType"] == "KarmaError") {
					showErrorMsg(element["Error"]);
				}

			});

		}

		function hide() {
			dialog.modal('hide');
		}

		function show(data) {
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