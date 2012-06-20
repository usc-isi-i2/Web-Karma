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

function assignHandlersToCleaningPanelObjects() {
	var cleaningPanel = $("div#ColumnCleaningPanel");
	$("button#cleanColumnButton").click(handleCleanColumnButton);
	$("button#generateCleaningRules", cleaningPanel).click(handleGenerateCleaningRulesButton);
}

function handleCleanColumnButton() {
	var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
	columnHeadingMenu.hide();

	// Get the values from the column to be cleaned
	var selectedHNodeId = columnHeadingMenu.data("parentCellId");
	var tdTag = $("td#" + selectedHNodeId);
	var table = tdTag.parents("table.WorksheetTable");
	var lastRow = $("thead tr:last", table);
	var index = $("td", lastRow).index(tdTag);
	var values = [];
	$('tbody>tr>td:nth-child(' + (index + 1) + ')', table).each(function() {
		if($(this).attr("id"))
			values.push({
				"nodeId" : $(this).attr("id"),
				"nodeValue" : $(this).text()
			});
	});

	// Create and store a array that stores the user provided examples
	var examples = [];
	columnHeadingMenu.data("cleaningExamples", examples);

	// Populate the table of cleaning preview table
	var cleaningTable = $("table#cleaningExamplesTable");
	$("tr.nonHeading", cleaningTable).remove();

	var res = new Object();
	$.each(values, function(index, val) {
		var tr = $("<tr>").attr("id", val["nodeId"] + "_cl_row").addClass("nonHeading").append($("<td>").text(val["nodeValue"]).attr('id', val['nodeId'] + "_origVal"))//add text and id to the td
		.append($("<td>").addClass("noBorder"));
		//add td to seperate org and result
		res[val["nodeId"]] = val["nodeValue"];
		cleaningTable.append(tr);
	});
	var initialResultsValues = new Array();
	var pac = new Object();
	pac["data"] = res;
	initialResultsValues.push(pac);
	$("div#columnHeadingDropDownMenu").data("results", initialResultsValues);
	populateResult(initialResultsValues[0]);

	//
	$("div#ColumnCleaningPanel").dialog({
		title : 'Transform',
		width : 900,
		height : 500,
		buttons : {
			"Cancel" : function() {
				$(this).dialog("close");
			},
			//"Generate Rules" : handleGenerateCleaningRulesButton,
			"Submit" : function() {
				submit();
				$(this).dialog("close");
			}
		}
	});
}

function populateResult(rdata) {
	var examples = $("div#columnHeadingDropDownMenu").data("cleaningExamples", examples);
	var cleaningTable = $("table#cleaningExamplesTable");
	var transformedResult = new Object();
	$("div#columnHeadingDropDownMenu").data("transformedResult", transformedResult);
	// Remove the old results
	$("td.ruleResultsValue_rest", cleaningTable).remove();
	//$("td.ruleResultsValue_begin", cleaningTable).remove();
	var data = rdata["data"];
	$.each(data, function(nodeId, xval) {
		var trTag = $("tr#" + nodeId + "_cl_row");
		if(trTag != null) {
			transformedResult[nodeId] = xval;
			if(xval == $("div#" + nodeId + "_c").text()) {
				return true;
			}
			$("td.ruleResultsValue_begin", trTag).remove();
			trTag.append($("<td>").addClass("ruleResultsValue_begin").attr("id", nodeId + "_transformed").append($("<table>").append($("<tr>").append($("<td>").addClass("noinnerBorder").append($("<div>").data("nodeId", nodeId).data("originalVal", $("td#" + nodeId + "_origVal", cleaningTable).text())// set the original value for the example
			.data("cellValue", xval).addClass("cleanExampleDiv").text(xval)//set the result here
			.attr("id", nodeId + "_c").editable(function(value, settings) {
				var editDiv = $(this);
				// Add the revert button
				var revertButton = $("<div>").addClass("undoEditButton").button({
					icons : {
						primary : 'ui-icon-arrowreturnthick-1-w'
					},
					text : false
				}).click(function() {
					editDiv.text(editDiv.data("cellValue"));
					$(this).parent().remove();

					// Remove the user provided example from the examples JSON object
					var delInd = -1;
					$.each(examples, function(index2, example) {
						if(example["nodeId"] == editDiv.data("nodeId"))
							delInd = index2;
					});
					if(delInd != -1)
					{
						examples.splice(delInd, 1);
						updateResult();
					}
				}).qtip({
					content : {
						text : 'Undo'
					},
					style : {
						classes : 'ui-tooltip-light ui-tooltip-shadow'
					}
				});
				// Remove existing button
				$("td.noBorder", $(this).parent().parent()).remove();
				$(this).parent().parent().append($("<td>").addClass("noBorder").append(revertButton));

				examples.push({
					"nodeId" : $(this).data("nodeId"),
					"before" : $(this).data("originalVal"),
					"after" : value
				});
				//call the update result function
				updateResult();
				return (value);
			}, {
				type : 'textarea',
				submit : 'OK',
				cancel : 'Cancel',
				width : 140,
				onblur : 'ignore',
			}))))))
		}
	});
}

// input: data shows resultual varations for each nodeID
function populateVariations(data) {
	var examples = $("div#columnHeadingDropDownMenu").data("cleaningExamples", examples);
	var cleaningTable = $("table#cleaningExamplesTable");

	// Remove the old results
	$("td.ruleResultsValue_rest", cleaningTable).remove();

	$.each(data, function(nodeId, xval) {
		var values = Object.keys(xval);
		$.each(values, function(index, val) {
			var trTag = $("tr#" + nodeId + "_cl_row");
			var tdTag = $("td#" + nodeId + "_variations");
			if(tdTag == null || tdTag.length == 0) {
				var tdTag = $("<td>").addClass("ruleResultsValue_rest").attr("id", nodeId + "_variations");
			}
			if(tdTag != null) {
				trTag.append(tdTag);
				tdTag.append($("<input>").data("nodeId", nodeId).data("before", $("td#" + nodeId + "_origVal", cleaningTable).text()).attr("type", "button").addClass("suggestion").prop('value', val).click(function() {
					examples.push({
						"nodeId" : $(this).data("nodeId"),
						"before" : $(this).data("before"),
						"after" : $(this).attr("value")
					});
					$("div#" + nodeId + "_c").text($(this).attr("value"));
					var revertButton = $("<div>").addClass("undoEditButton").button({
						icons : {
							primary : 'ui-icon-arrowreturnthick-1-w'
						},
						text : false
					}).click(function() {
						//editDiv.text(editDiv.data("cellValue"));
						$(this).remove();
						// Remove the user provided example from the examples JSON object
						var delInd = -1;
						$.each(examples, function(index2, example) {
							if(example["nodeId"] == $("div#" + nodeId + "_c").data("nodeId"))
								delInd = index2;
						});
						if(delInd != -1)
						{
							examples.splice(delInd, 1);
							updateResult();
						}
					}).qtip({
						content : {
							text : 'Undo'
						},
						style : {
							classes : 'ui-tooltip-light ui-tooltip-shadow'
						}
					});
					// Remove existing button
					$("td.noBorder", $("div#" + nodeId + "_c").parent().parent()).remove();
					$("div#" + nodeId + "_c").parent().parent().append($("<td>").addClass("noBorder").append(revertButton));
					updateResult();
					return;
				}));
			}
		});
	});
}

function handleGenerateCleaningRulesButton() {
	var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
	var selectedHNodeId = columnHeadingMenu.data("parentCellId");
	var tdTag = $("td#" + selectedHNodeId);
	var vWorksheetId = tdTag.parents("div.Worksheet").attr("id");

	var examples = columnHeadingMenu.data("cleaningExamples");

	var info = new Object();
	info["vWorksheetId"] = vWorksheetId;
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["hNodeId"] = selectedHNodeId;
	info["command"] = "GenerateCleaningRulesCommand";
	info["examples"] = JSON.stringify(examples);

	var returned = $.ajax({
		url : "/RequestController",
		type : "POST",
		data : info,
		dataType : "json",
		complete : function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			hideCleanningWaitingSignOnScreen();
			parse(json);
			var tdata = $("div#columnHeadingDropDownMenu").data("results");

		},
		error : function(xhr, textStatus) {
			hideCleanningWaitingSignOnScreen();
			$.sticky("Error generating new cleaning rules!");
		}
	});
}

//////////////////////////
//find the variations for each cell
function getVaritions(data) {
	var x = {};
	var top = data[0]["data"];
	var subdata = data.slice(1);
	$.each(subdata, function(index, pacdata) {
		ruleResult = pacdata["data"];
		for(var nodeId in ruleResult) {
			if(ruleResult[nodeId] == "") {
				continue;
			}
			if( nodeId in x) {
				var dic = x[nodeId];
				var value = ruleResult[nodeId];
				if(!( value in Object.keys(dic)) && (value != top[nodeId])) {
					dic[value] = "" + index;
				}
			} else {
				var value = ruleResult[nodeId];
				var y = {};
				if((value != top[nodeId])) {
					y[value] = "" + index;
					x[nodeId] = y;
				}
			}
		}
	});
	//attach data to dom node
	return x;
}

//submit the transformed result
function submit() {
	var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
	var selectedHNodeId = columnHeadingMenu.data("parentCellId");
	var tdTag = $("td#" + selectedHNodeId);
	var vWorksheetId = tdTag.parents("div.Worksheet").attr("id");
	var transformedRes = $("div#columnHeadingDropDownMenu").data("transformedResult");
	var info = new Object();
	info["vWorksheetId"] = vWorksheetId;
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["hNodeId"] = selectedHNodeId;
	info["command"] = "AddNewColumnCommand";
	info["result"] = JSON.stringify(transformedRes);

	var returned = $.ajax({
		url : "/RequestController",
		type : "POST",
		data : info,
		dataType : "json",
		complete : function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			parse(json);
			hideCleanningWaitingSignOnScreen();
		},
		error : function(xhr, textStatus) {
			$.sticky("Error in submitting");
		}
	});
}

//add the choosen value to be a new example
function addExample(nodeID) {

}

//update column when examples are added
function updateResult() {
	var data = $("div#columnHeadingDropDownMenu").data("results");
	var newdata = [];
	var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
	var examples = columnHeadingMenu.data("cleaningExamples");
	if(examples.length == 0)
	{
		return;
	}
	$.each(data, function(index, pacdata) {
		var column = pacdata["data"];
		islegal = true;
		$.each(examples, function(ind, exp) {
			if(!(column[exp["nodeId"]] === exp["after"])) {
				islegal = false;
				return;
			}
		});
		if(islegal)// add the result to the new data collection
		{
			newdata.push(pacdata);
		}
	});
	//generate rules and apply them to test data
	if(newdata.length == 0) {
		showCleanningWaitingSignOnScreen();
		handleGenerateCleaningRulesButton();
	} else// use the trimmed data
	{
		populateResult(newdata[0]);
		var pdata = getVaritions(newdata);
		populateVariations(pdata);
	}
}

//wait
function showCleanningWaitingSignOnScreen() {
	var coverDiv = $("<div>").attr("id", "WaitingDiv").addClass('waitingDiv').append($("<div>").html('<b>Please wait</b>').append($('<img>').attr("src", "images/ajax-loader.gif")));

	var spaceToCoverDiv = $("div#ColumnCleaningPanel");
	spaceToCoverDiv.append(coverDiv.css({
		"position" : "fixed",
		"height" : $(document).height(),
		"width" : $(document).width(),
		"zIndex" : 100,
		"top" : spaceToCoverDiv.position().top,
		"left" : spaceToCoverDiv.position().left
	}).show());
}

function hideCleanningWaitingSignOnScreen() {
	$("div#WaitingDiv").hide();
}
