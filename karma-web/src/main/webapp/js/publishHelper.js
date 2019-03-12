var PublishHelper = (function() {
	var instance = null;

	function PrivateConstructor() {
		var worksheetId; 
		
		function init() {
			
		}
		function fetchGraphsFromTripleStore(url, rdfOrModel, dialog) {

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
					var graphList = $("#"+rdfOrModel+"GraphList");
					graphList.html('<option value="create_new_context">Create New Context </option>');
					for (var x in graphs) {
						graphList.append('<option value="' + graphs[x] + '">' + graphs[x] + '</option>');
					}
					if (graphs.length > 0) {
						graphList.val(graphs[0]);
						$('#labelFor_'+rdfOrModel+'SPAQRLGraph').hide();
						$('#'+rdfOrModel+'SPAQRLGraph').hide();
					} else {
						graphList.val("create_new_context");
						$('#'+rdfOrModel+'SPAQRLGraph').val(getUniqueGraphUri(null, url, rdfOrModel));
						$('#labelFor_'+rdfOrModel+'SPAQRLGraph').show();
						$('#'+rdfOrModel+'SPAQRLGraph').show();
					}

					graphList.unbind('change');
					graphList.change(function(event) {
						hideError(dialog);
						if ($('#'+rdfOrModel+'GraphList').val() == "create_new_context") {
							$('#'+rdfOrModel+'SPAQRLGraph').val(getUniqueGraphUri(null, url, rdfOrModel));
							$('#labelFor_'+rdfOrModel+'SPAQRLGraph').show();
							$('#'+rdfOrModel+'SPAQRLGraph').show();
						} else {
							$('#labelFor_'+rdfOrModel+'SPAQRLGraph').hide();
							$('#'+rdfOrModel+'SPAQRLGraph').hide();
						}
						//$('#rdfSPAQRLGraph').val($('#modelGraphList').val());
					});
				},
				error: function(xhr, textStatus) {
					alert("Error occurred with fetching graphs! " + textStatus);
				}
			});
		}

		function hideError(dialog) {
			$("div.error", dialog).hide();
		}
		
		function showError(dialog, err) {
			$("div.error", dialog).show();
			if (err) {
				$("div.error", dialog).text(err);
			}
		}
		
		function validate(url, rdfOrModel, dialog) {
			var expression = /(^|\s)((https?:\/\/)?[\w-]+(\.[\w-]+)+\.?(:\d+)?(\/\S*)?)/gi;
			// /[-a-zA-Z0-9@:%_\+.~#?&//=]{2,256}\.[a-z]{2,4}\b(\/[-a-zA-Z0-9@:%_\+.~#?&//=]*)?/gi;
			var regex = new RegExp(expression);
			var graphUri = "";
			var needsValidation = false;
			if ($('#'+rdfOrModel+'GraphList').val() == "create_new_context") {
				graphUri = $("input#"+rdfOrModel+"SPAQRLGraph").val();
				needsValidation = true;
			} else {
				graphUri = $('#'+rdfOrModel+'GraphList').val();
			}
			// validate the sparql endpoint
			if (!testSparqlEndPoint(url, worksheetId)) {
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
				var newUri = getUniqueGraphUri(graphUri, url, rdfOrModel);
				if (graphUri != newUri) {
					showError(dialog, "The context you provided already exists. Please either enter a different context name, " +
						"or select the context from the 'Use existing context' dropdown");
					return;
				}
			}
			return graphUri
		}

		function getGraphURIForWorksheet(rdfOrModel) {
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
						$("#"+rdfOrModel+"SPAQRLGraph").val(props["graphName"]);
					} else {
						$("#"+rdfOrModel+"SPAQRLGraph").val("");
					}
				}
			});
			return $("#"+rdfOrModel+"SPAQRLGraph").val();
		}

		function getUniqueGraphUri(graphUriTobeValidated, tripleStoreURL, rdfOrModel) {
			var info = generateInfoObject(worksheetId, "", "GetUniqueGraphUrlCommand");
			info["tripleStoreUrl"] = tripleStoreURL;
			if (graphUriTobeValidated && graphUriTobeValidated != null) {
				info["graphUri"] = graphUriTobeValidated;
			}
			$('#'+rdfOrModel+'SPAQRLGraph').attr('rel', '');
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				async: false,
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);
					$('#'+rdfOrModel+'SPAQRLGraph').attr('rel', json.elements[0].graphUri);
				},
				error: function(xhr, textStatus) {
					alert("Error occurred with fetching graphs! " + textStatus);
				}
			});
			return String($('#'+rdfOrModel+'SPAQRLGraph').attr('rel'));
		}

		function show(wsId) {
			worksheetId = wsId;
		};

		return { //Return back the public methods
			fetchGraphsFromTripleStore: fetchGraphsFromTripleStore,
			init: init,
			show: show,
			validate: validate,
			getGraphURIForWorksheet: getGraphURIForWorksheet
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