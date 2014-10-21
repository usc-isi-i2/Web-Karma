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
$(document).on("click", "#importFromServiceButton", function() {
	console.log("Import From Service");
	ImportFromServiceDialog.getInstance().show();
});

var ImportFromServiceDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#importFromServiceDialog");

		function init() {
			dialog.on('show.bs.modal', function(e) {
				$("#serviceErrorRow").hide();
				getServicePreferences();
			});

			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});
		}

		function saveDialog(e) {
			console.log("Save clicked");
			var url = $.trim($("#serviceUrl").val());
			var worksheetName = $.trim($("#serviceWorksheetName").val());

			if (!url || !worksheetName) {
				$("#serviceErrorRow").show();
				console.log("Service Errow: missing values");
				return false;
			}

			dialog.modal('hide');

			var info = generateInfoObject("", "", "ImportServiceCommand");
			info["serviceUrl"] = url;
			info["worksheetName"] = worksheetName;
			info["includeInputAttributes"] = $('#includeInputAttributesService').is(':checked');
			info["encoding"] = $("#serviceEncoding").val();

			showWaitingSignOnScreen();
			var returned = sendRequest(info);
		}

		function getServicePreferences() {
			var info = generateInfoObject("", "", "FetchPreferencesCommand");
			info["preferenceCommand"] = "ImportServiceCommand";
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);
					$.each(json["elements"], function(index, element) {
						if (element["updateType"] == "ImportServiceCommandPreferences") {

							if (element["PreferenceValues"]) {
								$("input#serviceUrl").val(element["PreferenceValues"]["ServiceUrl"]);
								$("input#serviceWorksheetName").val(element["PreferenceValues"]["WorksheetName"]);
							}
						}
					});

				},
				error: function(xhr, textStatus) {
					alert("Error occured with fetching new rows! " + textStatus);
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