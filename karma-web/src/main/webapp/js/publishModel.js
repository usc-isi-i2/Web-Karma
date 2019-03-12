/*******************************************************************************
 * Copyright 2018 University of Southern California
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
 
var PublishModelDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#publishModelDialog");
		var worksheetId;
		var helper = PublishHelper.getInstance()

		function init() {
			//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				hideError();
				if ($("input#saveToRDFStore").is(":checked")) {
					$("div#rdfStoreInfo").show();
				} else {
					$("div#rdfStoreInfo").hide();
				}
				window.rdfSPAQRLEndPoint = $('#txtModel_URL').html();
				helper.getGraphURIForWorksheet('model');
				helper.fetchGraphsFromTripleStore($('#txtModel_URL').html(), 'model', dialog);
			});

			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				graphURI = helper.validate($('#txtModel_URL').html(), 'model', dialog);
				if (! graphURI) {
					return
				}
				publishModelFunction(graphURI)
			});

		}

		function publishToGithub(worksheetId, repo) {
			var auth = Settings.getInstance().getGithubAuth();
			if(repo != "disabled" && !repo.endsWith("disabled)")) {
				if(auth) {
					showLoading(worksheetId);
					var githubInfo = generateInfoObject(worksheetId, "", "PublishGithubCommand");
			        githubInfo["worksheetId"] = worksheetId;
			        githubInfo["auth"] = auth;
			        githubInfo["repo"] = repo;
			        var returned = sendRequest(githubInfo, worksheetId);
			    }
		    }
		}
		
        function publishModelFunction(graphUri) {
			hide();
			var info = generateInfoObject(worksheetId, "", "GenerateR2RMLModelCommand");
			info['tripleStoreUrl'] = $('#txtModel_URL').text();
			info['graphUri'] = graphUri
			info['modelUri'] = $("input#modelURI").val()
			showLoading(info["worksheetId"]);
			var repoUrl = $("#txtGithubUrl_" + worksheetId).text(); 
			var returned = sendRequest(info, worksheetId,
				function(data) {
					var newWorksheetId = worksheetId;
					$.each(data["elements"], function(i, element) {
						if(element) {
							if (element["updateType"] == "PublishR2RMLUpdate") {
								newWorksheetId = element["worksheetId"];
							}
						}
					});

				var info = generateInfoObject(newWorksheetId, "", "PublishReportCommand");
				showLoading(newWorksheetId);
				var returned = sendRequest(info, newWorksheetId, function(json) {
					publishToGithub(newWorksheetId, repoUrl);
				});
			});
		}
	
		function publishModelToFile(info) {

			showLoadingRDF(info["worksheetId"], "Saving to file...");
			returnFunc(info);
		}

		function returnFunc(info) {
			sendRequest(info, info['worksheetId']);
		}

		function showLoadingRDF(worksheetId, message) {
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
		
        function getRDFPreferences() {
			var info = generateInfoObject("", "", "FetchPreferencesCommand");
			info["preferenceCommand"] = "GenerateR2RMLModelCommandPreferences";
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				async: false,
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);
					$.each(json["elements"], function(index, element) {
						if (element["updateType"] == "PublishRDFCommandPreferences") {
							if (element["PreferenceValues"]) {
								$("input#hostName").val(element["PreferenceValues"]["hostName"]);
								$("input#dbName").val(element["PreferenceValues"]["dbName"]);
								$("input#userName").val(element["PreferenceValues"]["userName"]);
								$("input#modelName").val(element["PreferenceValues"]["modelName"]);
								$("input#saveToRDFStore").val(element["PreferenceValues"]["saveToStore"]);
								$("input#addInverseProperties").val(element["PreferenceValues"]["addInverseProperties"]);
							}
						}
					});

				},
				error: function(xhr, textStatus) {
					alert("Error occurred with fetching new rows! " + textStatus);
				}
			});
		}

		function hideError() {
			$("div.error", dialog).hide();
		}

		function hide() {
			dialog.modal('hide');
		}

		function show(wsId) {
			worksheetId = wsId;
			helper.show(worksheetId)
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