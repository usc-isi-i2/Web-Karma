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
				getGraphURI();
				fetchGraphsFromTripleStore($('#txtData_URL').html());
			});

			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				validateAndPublishRDF(e);
			});

		}

		function fetchGraphsFromTripleStore(url) {

			var info = generateInfoObject("", "", "FetchGraphsFromTripleStoreCommand");
			info["tripleStoreUrl"] = url;
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);
					graphs = [];
					if (json["elements"] && json["elements"][0]['graphs']) {
						graphs = json["elements"][0]['graphs'];
					}
					var modelGraphList = $("#modelGraphList");
					modelGraphList.html('<option value="create_new_context">Create New Context </option>');
					for (var x in graphs) {
						modelGraphList.append('<option value="' + graphs[x] + '">' + graphs[x] + '</option>');
					}
					if (graphs.length > 0) {
						modelGraphList.val(graphs[0]);
						$('#labelFor_rdfSPAQRLGraph').hide();
						$('#rdfSPAQRLGraph').hide();
					} else {
						modelGraphList.val("create_new_context");
						$('#rdfSPAQRLGraph').val(getUniqueGraphUri());
						$('#labelFor_rdfSPAQRLGraph').show();
						$('#rdfSPAQRLGraph').show();
					}

					modelGraphList.unbind('change');
					modelGraphList.change(function(event) {
						if ($('#modelGraphList').val() == "create_new_context") {
							$('#rdfSPAQRLGraph').val(getUniqueGraphUri());
							$('#labelFor_rdfSPAQRLGraph').show();
							$('#rdfSPAQRLGraph').show();
						} else {
							$('#labelFor_rdfSPAQRLGraph').hide();
							$('#rdfSPAQRLGraph').hide();
						}
						//$('#rdfSPAQRLGraph').val($('#modelGraphList').val());
					});
				},
				error: function(xhr, textStatus) {
					alert("Error occurred with fetching graphs! " + textStatus);
				}
			});
		}

		function validateAndPublishRDF() {
			var expression = /(^|\s)((https?:\/\/)?[\w-]+(\.[\w-]+)+\.?(:\d+)?(\/\S*)?)/gi;
			// /[-a-zA-Z0-9@:%_\+.~#?&//=]{2,256}\.[a-z]{2,4}\b(\/[-a-zA-Z0-9@:%_\+.~#?&//=]*)?/gi;
			var regex = new RegExp(expression);
			var graphUri = "";
			var needsValidation = false;
			if ($('#modelGraphList').val() == "create_new_context") {
				graphUri = $("input#rdfSPAQRLGraph").val();
				needsValidation = true;
			} else {
				graphUri = $('#modelGraphList').val();
			}
			// validate the sparql endpoint
			if (!testSparqlEndPoint($('#txtData_URL').html(), worksheetId)) {
				alert("Invalid sparql end point. Could not establish connection.");
				return;
			}

			// validate the graph uri
			if (needsValidation) {
				if (graphUri.length < 3) {
					alert("Context field is empty");
					return;
				}
				if (!graphUri.match(regex)) {
					alert("Invalid Url format for context");
					return;
				}
				var newUri = getUniqueGraphUri(graphUri);
				if (graphUri != newUri) {
					showError("The context you provided already exists. Please either enter a different context name, " +
						"or select the context from the 'Use existing context' dropdown");
					//    				var rdfDialogBox = $("div#confirmPublishRDFDialogBox");
					//    				rdfDialogBox.find('span').html('The context you provided already exists. <br /> You can either publish to the same \
					//    						context or use the one that is suggested below. <br /> ' + newUri);
					//    				
					//    				rdfDialogBox.dialog({ title:'Confirmation',  width: 700 , buttons: { 
					//    						"Use Old": function() { publishRDFFunction(graphUri) },
					//    						"Use New": function() { publishRDFFunction(newUri) },
					//    						"Cancel": function() { $(this).dialog("close"); }
					//    					}});
				} else {
					publishRDFFunction(graphUri);
				}
			} else {
				publishRDFFunction(graphUri);
			}
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
			console.log(info["rdfPrefix"]);
			console.log(info["rdfNamespace"]);
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

		function getGraphURI() {
			// get the graph uri for the worksheet
			var info = generateInfoObject(worksheetId, "", "FetchExistingWorksheetPropertiesCommand");

			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);
					var props = json["elements"][0]["properties"];

					// Set graph name
					if (props["graphName"] != null) {
						$("#rdfSPAQRLGraph").val(props["graphName"]);
					} else {
						$("#rdfSPAQRLGraph").val("");
					}
				}
			});
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

		function getUniqueGraphUri(graphUriTobeValidated) {
			var info = generateInfoObject(worksheetId, "", "GetUniqueGraphUrlCommand");
			info["tripleStoreUrl"] = $('#txtData_URL').html();
			if (graphUriTobeValidated && graphUriTobeValidated != null) {
				info["graphUri"] = graphUriTobeValidated;
			}
			$('#rdfSPAQRLGraph').attr('rel', '');
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				async: false,
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);
					$('#rdfSPAQRLGraph').attr('rel', json.elements[0].graphUri);
				},
				error: function(xhr, textStatus) {
					alert("Error occurred with fetching graphs! " + textStatus);
				}
			});
			return String($('#rdfSPAQRLGraph').attr('rel'));
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