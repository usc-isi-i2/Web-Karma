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

var PublishDatabaseDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#publishDatabaseDialog");
		var worksheetId;

		function init() {
			//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				hideError();
				getDatabasePreferences();
			});

			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});


		}

		function getDatabasePreferences() {
			var info = generateInfoObject("", "", "FetchPreferencesCommand");
			info["preferenceCommand"] = "PublishDatabaseCommand";
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);
					$.each(json["elements"], function(index, element) {
						if (element["updateType"] == "PublishDatabaseCommandPreferences") {

							if (element["PreferenceValues"]) {
								$("select#dbType1").val(element["PreferenceValues"]["dbType"]);
								$("input#hostName1").val(element["PreferenceValues"]["hostName"]);
								$("input#dbName1").val(element["PreferenceValues"]["dbName"]);
								$("input#userName1").val(element["PreferenceValues"]["userName"]);
								$("input#tableName1").val(element["PreferenceValues"]["tableName"]);
								$("input#port1").val(element["PreferenceValues"]["port"]);
								//$("input#overwriteTable").val(element["PreferenceValues"]["overwriteTable"]);
								//$("input#insertTable").val(element["PreferenceValues"]["insertTable"]);
							}
						}
					});

				},
				error: function(xhr, textStatus) {
					alert("Error occured with fetching new rows! " + textStatus);
				}
			});
		}

		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError(err) {
			$("div.error", dialog).show();
			if (err) {
				$("div.error", dialog).text(err);
			}
		}

		function validateInputs() {
			if ($.trim($("input#hostName1").val()) == "") {
				showErrory("Host name is empty!");
				return false;
			}
			if ($.trim($("input#port1").val()) == "") {
				showError("Port is empty!");
				return false;
			}
			if ($.trim($("input#dbName1").val()) == "") {
				showError("DB name is empty!");
				return false;
			}
			if ($.trim($("input#tableName1").val()) == "") {
				showError("Table name is empty!");
				return false;
			}
			if ($.trim($("input#userName1").val()) == "") {
				showError("User name is empty!");
				return false;
			}
			return true;
		}

		function saveDialog(e) {
			if (validateInputs()) {
				hide();
				var info = generateInfoObject(worksheetId, "", "PublishDatabaseCommand");
				info["dbType"] = $("select#dbType1").val();
				info["hostName"] = $("input#hostName1").val();
				info["dbName"] = $("input#dbName1").val();
				info["userName"] = $("input#userName1").val();
				info["password"] = $("input#password1").val();
				info["tableName"] = $("input#tableName1").val();
				info["port"] = $("input#port1").val();

				var overwriteInstruction = $("input:radio[name='overwriteInstruction']:checked").val();
				console.log("Got overwriteInstruction:" + overwriteInstruction);
				info["overwriteTable"] = (overwriteInstruction == "overwrite") ? true : false;
				info["insertTable"] = (overwriteInstruction == "insert") ? true : false;

				showLoadingDatabase(info["worksheetId"], "Saving to database...");

				var returned = sendRequest(info, worksheetId);
			}
		};

		function showLoadingDatabase(worksheetId, message) {
			var coverDiv = $("<div>").attr("id", "WaitingDiv_" + worksheetId).addClass('waitingDiv')
				.append($("<div>").html('<b>' + message + '</b>')
					.append($('<img>').attr("src", "images/ajax-loader.gif"))
				);

			var spaceToCoverDiv = $("div#" + worksheetId);
			spaceToCoverDiv.append(coverDiv.css({
				"position": "absolute",
				"height": spaceToCoverDiv.height(),
				"width": spaceToCoverDiv.width(),
				"top": spaceToCoverDiv.position().top,
				"left": spaceToCoverDiv.position().left
			}).show());
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