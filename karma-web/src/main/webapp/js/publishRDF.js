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




var PublishRDFDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#publishRDFDialog");
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
				getRDFPreferences();
				window.rdfSPAQRLEndPoint = $('#txtData_URL').html();
				helper.getGraphURIForWorksheet('rdf');
				helper.fetchGraphsFromTripleStore($('#txtData_URL').html(), 'rdf', dialog);
			});

			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				graphURI = helper.validate($('#txtData_URL').html(), 'rdf', dialog);
				if (! graphURI) {
					return
				}
				publishRDFFunction(graphURI);
			});

		}

		function publishRDFFunction(graphUri) {
			hide();

			var info = generateInfoObject(worksheetId, "", "PublishRDFCommand");
			info["addInverseProperties"] = $("input#addInverseProperties").is(":checked");
			info["saveToStore"] = $("input#saveToStore").is(":checked");
			info["hostName"] = $("input#hostName").val();
			info["dbName"] = $("input#dbName").val();
			info["userName"] = $("input#userName").val();
			info["password"] = $("input#password").val();
			info["modelName"] = $("input#modelName").val();
			info["tripleStoreUrl"] = $('#txtData_URL').html();
			info["graphUri"] = graphUri;
			var replace = false;
			if ($('input:radio[name=group1]:checked', dialog).val() == "replace")
				replace = true;
			info["replaceContext"] = replace;
			info["generateBloomFilters"] = $("input#generateBloomFilters").is(":checked");
			if ($("input#saveToRDFStore").is(":checked")) {
				publishRDFToStore(info);
			} else {
				publishRDFToFile(info);
			}
		}

		function publishRDFToFile(info) {

			showLoadingRDF(info["worksheetId"], "Saving to file...");
			returnFunc(info);
		}

		function publishRDFToStore(info) {

			showLoadingRDF(info["worksheetId"], "Saving to RDF store...");
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
			info["preferenceCommand"] = "PublishRDFCommand";
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