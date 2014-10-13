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

var TransformColumnDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#transformColumnDialog");
		var worksheetId, columnId;
		var cleaningExamples, results, topkeys, transformedResult, nodeIds;

		function init() {
			//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				hideError();
				userExamples = [];
				loadInitialData();
			});

			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});

			$("button#generateCleaningRules", dialog).on('click', function(e) {
				e.preventDefault();
				handleGenerateCleaningRulesButton();
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
			hide();

			var selectedHNodeId = columnId;
			//var transformedRes = transformedResult;
			var info = generateInfoObject(worksheetId, selectedHNodeId, "SubmitCleaningCommand");
			info["examples"] = JSON.stringify(cleaningExamples);

			var newInfo = info['newInfo'];
			newInfo.push(getParamObject("examples", cleaningExamples, "other"));
			info["newInfo"] = JSON.stringify(newInfo);
			showLoading(worksheetId);
			var returned = sendRequest(info, worksheetId);
		};

		function loadInitialData() {
			cleaningExamples = [];
			results = [];
			topkeys = [];

			var values = fetchCleanningRawData();

			// Populate the table of cleaning preview table
			var cleaningTable = $("table#cleaningExamplesTable");
			$("tr.nonHeading", cleaningTable).remove();
			$("tr.suggestion", cleaningTable).remove();

			nodeIds = [];
			var data = values[0]["data"]
			for (var nodeId in data) {
				if (data.hasOwnProperty(nodeId)) {
					var tr = $("<tr>").attr("id", nodeId + "_cl_row").addClass("nonHeading").append($("<td>").text(data[nodeId]["Org"]).attr('id', nodeId + "_origVal")) //add text and id to the td
						.append($("<td>").addClass("noBorder"));
					//add td to seperate org and result
					tr.data("originalVal", (data[nodeId]["Org"]));
					cleaningTable.append(tr);
					nodeIds.push(nodeId);
				}
			}
			//cleaning
			var tab1 = $("table#recmd");
			$("tr", tab1).remove();
			var tab2 = $("table#examples");
			$("tr", tab2).remove();

			populateResult(values[0]);
		}

		function fetchCleanningRawData() {
			var info = generateInfoObject(worksheetId, columnId, "FetchTransformingDataCommand");
			var json = {};
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				async: false,
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					json = $.parseJSON(xhr.responseText);
					json = json["elements"][0]["result"];
					hideCleanningWaitingSignOnScreen();
				},
				error: function(xhr, textStatus) {
					hideCleanningWaitingSignOnScreen();
					$.sticky("Error in Fetching Raw Data!");
				}
			});
			return json;
		}

		function populateResult(rdata) {
			try {
				var examples = cleaningExamples;
				var cleaningTable = $("table#cleaningExamplesTable");
				transformedResult = new Object();

				// Remove the old results
				$("td.ruleResultsValue_rest", cleaningTable).remove();
				$("tr.suggestion", cleaningTable).remove();
				//$("td.ruleResultsValue_begin", cleaningTable).remove();

				var data = rdata["data"];
				$.each(data, function(nodeId, xval) {
					var trTag = $("tr#" + nodeId + "_cl_row");
					if (trTag != null) {
						transformedResult[nodeId] = xval;
						if (xval == $("div#" + nodeId).text()) {
							$("div#" + nodeId).attr("class", "cleanExampleDiv");
							//return true;
						}
						$("td.ruleResultsValue_begin", trTag).remove();
						$("#" + nodeId + "_origVal", trTag).html(xval["Orgdis"]).data("CellValue", xval["Org"]);
						var cleaningRes = xval["Tardis"];
						if (cleaningRes == "<span class=\"ins\"></span>")
							cleaningRes = "";
						trTag.append(
							$("<td>").addClass("ruleResultsValue_begin").attr("id", nodeId + "_transformed")
							.append($("<table>")
								.append($("<tr>")
									.append($("<td>").addClass("noinnerBorder")
										.append($("<div>").data("nodeId", nodeId) // set the original value for the example
											.data("cellValue", xval["Tar"])
											.addClass("cleanExampleDiv")
											.html(cleaningRes) //set the result here
											.attr("id", nodeId)))
									.append($("<td>").addClass("noBorder")))));

						console.log($("div#" + nodeId).text() + ":" + xval["Tardis"]);
						$("div#" + nodeId, trTag).editable({
							type: 'text',
							success: function(response, value) {
								console.log("Set new value:" + value);
								var tmpnodeId = nodeId;
								$("div", $(this).parent().prev()).html(xval["Tar"]);
								if (nodeId.indexOf("suggestion") >= 0) {
									tmpnodeId = nodeId.substring(0, nodeId.indexOf("_suggestion"));
									$("div#" + tmpnodeId).text(value);
								}
								var editDiv = $("div#" + nodeId);
								examples.push({
									"nodeId": tmpnodeId,
									"before": $("tr#" + tmpnodeId + "_cl_row").data("originalVal"),
									"after": value
								});
								$("div#" + nodeId).text(value);
								xval["Tardis"] = value;
								updateResult();
							},
							showbuttons: 'bottom',
							mode: 'popup',
							inputclass: 'worksheetInputEdit'
						});
					}
				});

			} catch (err) {
				console.log(err.message)
			}
		}

		function updateResult() {
			var data = results;
			var newdata = [];
			var examples = cleaningExamples;
			console.log(examples)
			showCleanningWaitingSignOnScreen();
			handleGenerateCleaningRulesButton();
			/*else//use the trimmed data
			 {
			 populateResult(newdata[0]);
			 var pdata = getVaritions(newdata);
			 populateVariations(newdata[0]["top"],pdata);
			 }*/
		}

		function handleGenerateCleaningRulesButton() {
			var info = generateInfoObject(worksheetId, columnId, "GenerateCleaningRulesCommand");
			info["examples"] = JSON.stringify(cleaningExamples);
			info["cellIDs"] = JSON.stringify(nodeIds);

			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);
					hideCleanningWaitingSignOnScreen();
					parse(json);
				},
				error: function(xhr, textStatus) {
					hideCleanningWaitingSignOnScreen();
					$.sticky("Error generating new cleaning rules!");
				}
			});
		}

		function showCleanningWaitingSignOnScreen() {
			var coverDiv = $("<div>").attr("id", "WaitingDiv").addClass('waitingDiv').append($("<div>").html('<b>Please wait</b>').append($('<img>').attr("src", "images/ajax-loader.gif")));

			var spaceToCoverDiv = dialog;
			spaceToCoverDiv.append(coverDiv.css({
				"position": "fixed",
				"height": $(document).height(),
				"width": $(document).width(),
				"zIndex": 100,
				"top": spaceToCoverDiv.position().top,
				"left": spaceToCoverDiv.position().left
			}).show());
		}

		function hideCleanningWaitingSignOnScreen() {
			$("div#WaitingDiv").hide();
		}

		function populateInfoPanel() {
			var nodeId = topKeys[0];
			//tab1.append(trTag.clone(true,true));
			var tab2 = $("table#examples");
			$("tr", tab2).remove();
			var examples = cleaningExamples;
			$.each(examples, function(index, value) {
				var nodeID = value["nodeId"];
				var trTag1 = $("tr#" + nodeID + "_suggestion_cl_row", tab2);

				if (trTag1.length == 0) {
					trTag1 = $("<tr>").attr("id", nodeID + "_suggestion_cl_row").append($("<td>").addClass('info').text($("tr#" + nodeID + "_cl_row").data("originalVal")));
				}

				var closeButton = $("<button>");
				closeButton.attr("id", nodeID);
				closeButton.addClass("ui-icon").addClass("ui-icon-close").addClass("closeButton");
				closeButton.button({
					icons: {
						//primary : "ui-icon-close"
					},
					text: false
				});
				closeButton.click(function(event) {
					trTag1.hide();
					var delInd = -1;
					var curId = $(this).attr("id");
					// update examples and rerun the program
					$.each(examples, function(index2, example) {
						if (example["nodeId"] == curId)
							delInd = index2;
					});
					if (delInd != -1) {
						examples.splice(delInd, 1);
						updateResult();
					}

				});
				var tdButton = $("<td>").attr("class", "infobutton").append(closeButton);

				trTag1.append($("<td>").addClass('noBorder'))
				trTag1.append($("<td>").addClass("info").append($("<table>").append($("<tr>").append($("<td>").attr("class", "contentNoBorder").append($("<div>").data("nodeId", nodeID).data("cellValue", value["after"]).addClass("cleanExampleDiv").html(value["after"]))).append(tdButton))));

				//$(">td",trTag1).addClass("info");
				tab2.append(trTag1);
			});
			// recommanded examples
			if (nodeId == undefined || nodeId == "-2") {
				return;
			}
			var datadict = results[0]["data"];
			var tab1 = $("table#recmd");
			var trTag = $("tr#" + nodeId + "_suggestion_cl_row", tab1);
			$("tr", tab1).remove();
			// empty an array in JS
			if (trTag.length == 0) {
				trTag = $("<tr>").attr("id", nodeId + "_suggestion_cl_row").append($("<td>").addClass('info').html(datadict[nodeId]["Orgdis"])).append($("<td>").addClass("noBorder"));
			} else {
				trTag = trTag[0];
			}
			tab1.append(trTag);
		}

		function preprocessData(data, nodeIds) {
			data = data["data"];
			$.each(nodeIds, function(index, value) {
				var x = data[value];
				data[value + "_suggestion"] = x;
			});
		}

		function handleCleaningResultUpdate(cleaningResults) {
			console.log("handleCleaningResultUpdate: " + cleaningResults);
			var topCol = cleaningResults[0];
			results = cleaningResults;
			topKeys = topCol["top"];

			//var sndCol = element["result"][1];
			preprocessData(topCol, topCol["top"]);
			populateInfoPanel();
			populateResult(topCol);
			//var pdata = getVaritions(element["result"]);
			//populateVariations(topCol["top"], sndCol["data"]);
		}

		function hide() {
			dialog.modal('hide');
		}

		function show(wsId, colId) {
			worksheetId = wsId;
			columnId = colId;
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
		};

		return { //Return back the public methods
			show: show,
			init: init,
			handleCleaningResultUpdate: handleCleaningResultUpdate
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