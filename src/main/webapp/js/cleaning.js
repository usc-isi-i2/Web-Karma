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
	$("button#cleanColumnButton").click(90);
	$("button#generateCleaningRules", cleaningPanel).click(handleGenerateCleaningRulesButton);
}

function handleColumnsTransformation() {
	//choose columns
	var columnselPanel = $("div#ColumnSelection");
	var IDname = {
		"HN6" : "col1",
		"HN7" : "col2"
	};
	//show multiple columns cleaning panel
	var table = $("table#allcolumns");
	$("tr", table).remove();
	for(var key in IDname) {
		var tr = $("<tr>");
		var inputbox = $("<input>").attr("type", "checkbox").attr("name", IDname[key]).attr("checked", false).attr("colid", key);
		var td = $("<td>").text(IDname[key]);
		tr.append(inputbox);
		tr.append(td);
		table.append(tr);
	}
	columnselPanel.dialog({
		title : 'ColumnSelection',
		width : 400,
		height : 400,
		buttons : {
			"Cancel" : function() {
				$(this).dialog("close");
			},
			"Submit" : function() {
				var HNodeIDs = [];
				$.each($("input", columnselPanel), function(index, inp) {
					if(inp.checked) {
						HNodeIDs.push(inp.getAttribute("colid"));
					}
				});
				columnselPanel.data("selectcols", HNodeIDs);
				$("div#columnHeadingDropDownMenu").data("parentCellId",HNodeIDs)
				$(this).dialog("close");
				//open the transformation panel
				handleMultiColumnsDisplay();
			}
		}
	});
}

function gatherData(selectedHNodeIds) {
	var values = {};
	var indexs = [];
	var table = undefined;
	$.each(selectedHNodeIds, function(index, selectedHNodeId) {
		var tdTag = $("td#" + selectedHNodeId);
		table = tdTag.parents("table.WorksheetTable");
		var rows = $("thead tr", table);
		var index = -1;
		$.each(rows, function(ind, row) {
			var cells = $('td', row);
			if(cells.index(tdTag) == -1) {
				return;
			}
			index = 0;
			for(var k = 0; k < cells.length; k++) {
				cell = cells.get(k);
				if($(tdTag).text() != $(cell).text()) {
					if(cell.attributes["colspan"] != undefined) {
						index = index + parseInt($(cell).attr("colspan"));
					} else {
						index = index + 1;
					}
				} else {
					break;
				}
			}
			return index;
		});
		if(index != -1) {
			indexs.push(index);
		}
	});
	$.each(indexs, function(index, pos) {
		var cnt = 0;
		$('tbody>tr>td:nth-child(' + (index + 1) + ')', table).each(function() {
			
			if(cnt in values) {
				var elem = values[cnt];
				elem[$(this).attr("id")] = $(this).text();
			} 
			else {
				var elem = {};
				elem[$(this).attr("id")] = $(this).text();
				values[cnt] = elem;
			}
			cnt = cnt +1;
		});
	});

	return values;
}

function handleMultiColumnsDisplay() {
	// Get the values from the column to be cleaned
	var selectedHNodeIds = $("div#ColumnSelection").data("selectcols");
	var values = gatherData(selectedHNodeIds)
	// Create and store a array that stores the user provided examples
	var examples = [];
	var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
	columnHeadingMenu.data("cleaningExamples", examples);

	// Populate the table of cleaning preview table
	var cleaningTable = $("table#cleaningExamplesTable");
	$("tr.nonHeading", cleaningTable).remove();

	var res = new Object();
	for(var key in values) {
		var tr = $("<tr>").attr('id',key+"_cl_row").addClass("nonHeading");
		var constr = "";
		for(var key2 in values[key]) {
			tr.append($("<td>").text(values[key][key2]).attr('id', key2 + "_origVal")).append($("<td>").addClass("noBorder"));
			constr = constr + values[key][key2];
		}
		//add td to seperate org and result
		tr.data("originalVal",constr);
		res[key] = constr;
		cleaningTable.append(tr);
	}
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
				//submit();
				$(this).dialog("close");
			}
		}
	});
}

function handleCleanColumnButton() {
	var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
	columnHeadingMenu.hide();

	// Get the values from the column to be cleaned
	var selectedHNodeId = columnHeadingMenu.data("parentCellId");
	var tdTag = $("td#" + selectedHNodeId);
	var worksheetId = tdTag.parents("div.Worksheet").attr("id");
	var table = tdTag.parents("table.WorksheetTable");
	var rows = $("thead tr", table);
	var index = -1;
	$.each(rows, function(ind, row) {
		var cells = $('td', row);
		if(cells.index(tdTag) == -1) {
			return;
		}
		index = 0;
		for(var k = 0; k < cells.length; k++) {
			cell = cells.get(k);
			if($(tdTag).text() != $(cell).text()) {
				if(cell.attributes["colspan"] != undefined) {
					index = index + parseInt($(cell).attr("colspan"));
				} else {
					index = index + 1;
				}
			} else {
				break;
			}
		}
		return index;
	});
	var values = {};
	values = FetchCleanningRawData(selectedHNodeId,worksheetId);

	// Create and store a array that stores the user provided examples
	var examples = [];
	columnHeadingMenu.data("cleaningExamples", examples);

	// Populate the table of cleaning preview table
	var cleaningTable = $("table#cleaningExamplesTable");
	$("tr.nonHeading", cleaningTable).remove();
	$("tr.suggestion", cleaningTable).remove();
	var nodeIds = [];
	for( var nodeId in values) 
	{
		if(values.hasOwnProperty(nodeId))
		{
			var tr = $("<tr>").attr("id",nodeId+"_cl_row").addClass("nonHeading").append($("<td>").text(values[nodeId]).attr('id', nodeId + "_origVal"))//add text and id to the td
			.append($("<td>").addClass("noBorder"));
			//add td to seperate org and result	
			tr.data("originalVal",values[nodeId]);
			cleaningTable.append(tr);
			nodeIds.push(nodeId);
		}
	}
	var initialResultsValues = new Array();
	var pac = new Object();
	pac["data"] = values;
	initialResultsValues.push(pac);
	$("div#columnHeadingDropDownMenu").data("results", initialResultsValues);
	$("div#columnHeadingDropDownMenu").data("nodeIds", nodeIds);
	populateResult(initialResultsValues[0]);

	$("div#ColumnCleaningPanel").dialog({
		title : 'Transform',
		width : 1100,
		height : 600,
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

function movetop(keys) 
{
	if(keys == undefined) {
		return;
	}
	if(keys.length == 0) {
		return;
	}
	var cleaningTable = $("table#cleaningExamplesTable");
	for(var i = 0; i < keys.length; i++) {
		var trtag = $("tr#" + keys[i] + "_cl_row");
		//trtag.data("nodeId", nodeId).data("originalVal", $("td#" + nodeId + "_origVal", cleaningTable).text())
		//$("tr#" + keys[i] + "_cl_row").detach();
		$("tr", cleaningTable).eq(0).after(trtag);
	}
}
function preprocessData(data,nodeIds)
{
	data = data["data"];
	$.each(nodeIds, function(index, value) {
		var x = data[value];
		data[value+"_suggestion"] = x;
	});
}
     
function populateResult(rdata,nodeIds) {
	var examples = $("div#columnHeadingDropDownMenu").data("cleaningExamples", examples);
	var cleaningTable = $("table#cleaningExamplesTable");
	var transformedResult = new Object();
	$("div#columnHeadingDropDownMenu").data("transformedResult", transformedResult);
	// Remove the old results
	$("td.ruleResultsValue_rest", cleaningTable).remove();
	$("tr.suggestion", cleaningTable).remove();
	//$("td.ruleResultsValue_begin", cleaningTable).remove();

	var data = rdata["data"];
	$.each(data, function(nodeId, xval) {
		var trTag = $("tr#" + nodeId + "_cl_row");
		if(trTag.length == 0 && nodeId.indexOf("suggestion")>=0)
		{
			var orgnodeId = nodeId.substring(0,nodeId.indexOf("_suggestion"));
			trTag = $("<tr>").attr("id", nodeId+"_cl_row").addClass("suggestion").append($("<td>").text($("tr#"+orgnodeId+"_cl_row").data("originalVal"))).append($("<td>").addClass("noBorder"));
			$("tr", cleaningTable).eq(0).after(trTag);
		}
		if(trTag != null) 
		{
			transformedResult[nodeId] = xval;
			if(xval == $("div#" + nodeId).text()) {
				$("div#" + nodeId).attr("class","cleanExampleDiv");
				return true;
			}
			$("td.ruleResultsValue_begin", trTag).remove();
			trTag.append($("<td>").addClass("ruleResultsValue_begin").attr("id", nodeId + "_transformed").append($("<table>").append($("<tr>").append($("<td>").addClass("noinnerBorder").append($("<div>").data("nodeId", nodeId)// set the original value for the example
			.data("cellValue", xval).addClass("cleanExampleDiv").text(xval)//set the result here
			.attr("id", nodeId).editable(function(value, settings) {
				if(nodeId.indexOf("suggestion")>=0)
				{
					nodeId = nodeId.substring(0,nodeId.indexOf("_suggestion"));
				}
				var editDiv = $("div#"+ nodeId);
				// Add the revert button
				var revertButton = $("<div>").addClass("undoEditButton").button({
					icons : {
						primary : 'ui-icon-arrowreturnthick-1-w'
					},
					text : false
				}).click(function() {
					var orgvalue = editDiv.data("cellValue");		
					$(this).parent().remove();
					// Remove the user provided example from the examples JSON object
					var delInd = -1;
					$.each(examples, function(index2, example) {
						if(example["nodeId"] == editDiv.data("nodeId"))
							delInd = index2;
					});
					if(delInd != -1) {
						examples.splice(delInd, 1);
						updateResult();
					}
					editDiv.text(orgvalue);
				}).qtip({
					content : {
						text : 'Undo'
					},
					style : {
						classes : 'ui-tooltip-light ui-tooltip-shadow'
					}
				});
				// Remove existing button
				$("td.noBorder", editDiv.parent().parent()).remove();
				examples.push({
					"nodeId" : nodeId,
					"before" : $("tr#"+nodeId+"_cl_row").data("originalVal"),
					"after" : value
				});
				$("div#" + nodeId).text(value);
				updateResult();
				var trs = $("td#"+nodeId+"_transformed tr");
				$(trs[trs.length-1]).append($("<td>").addClass("noBorder").append(revertButton));
				
				//call the update result function
				return (value);
			}, {
				type : 'textarea',
				submit : 'OK',
				cancel : 'Cancel',
				width : 350,
				onblur : 'ignore',
			}))))))
		}
	});

}

// input: data shows resultual varations for each nodeID
function populateVariations(data,data1) {
	var examples = $("div#columnHeadingDropDownMenu").data("cleaningExamples", examples);
	var cleaningTable = $("table#cleaningExamplesTable");
	var tmpTr = $("tr#suggestedExample");
	tmpTr.remove();
	// Remove the old results
	$("td.ruleResultsValue_rest", cleaningTable).remove();
	//movetop(rdata["top"]);
	$.each(data, function(index, nodeId) {
		$("div#" + nodeId).attr("class","ambExampleDiv");
		}
	);
	$.each(data, function(index, nodeId) {
		var trTag = $("tr#" + nodeId + "_suggestion_cl_row");
		trTag.attr("class","suggestion")
		//$("tr", cleaningTable).eq(0).after(trTag);
		var values = data1[nodeId];
		$.each(values, function(index, val) {
			
			var tdTag = $("td#" + nodeId + "_suggestion_variations");
			if(tdTag == null || tdTag.length == 0) 
			{
				tdTag = $("<td>").addClass("ruleResultsValue_rest").attr("id", nodeId + "_suggestion_variations");
			}
			if(tdTag != null) 
			{
				trTag.append(tdTag);
				tdTag.append($("<input>").data("nodeId", nodeId).data("before", $("td#" + nodeId + "_origVal", cleaningTable).text()).attr("type", "button").addClass("suggestion").prop('value', val).click(function() {
					examples.push({
						"nodeId" : $(this).data("nodeId"),
						"before" : $(this).data("before"),
						"after" : $(this).attr("value")
					});
					$("div#" + nodeId).text($(this).attr("value"));
					$("div#" + nodeId+"_suggestion").text($(this).attr("value"));
					var revertButton = $("<div>").addClass("undoEditButton").button({
						icons : {
							primary : 'ui-icon-arrowreturnthick-1-w'
						},
						text : false
					}).click(function() {
						//editDiv.text(editDiv.data("cellValue"));
						$(this).remove();
						var orgvalue = $("div#"+nodeId).data("cellValue");
						// Remove the user provided example from the examples JSON object
						var delInd = -1;
						$.each(examples, function(index2, example) {
							if(example["nodeId"] == $("div#" + nodeId).data("nodeId"))
								delInd = index2;
						});
						if(delInd != -1) {
							examples.splice(delInd, 1);
							updateResult();
						}
						 $("div#"+nodeId).text(orgvalue);
					}).qtip({
						content : {
							text : 'Undo'
						},
						style : {
							classes : 'ui-tooltip-light ui-tooltip-shadow'
						}
					});
					// Remove existing button
					$("td.noBorder", $("div#" + nodeId).parent().parent()).remove();
					//$("div#" + nodeId).parent().parent().append($("<td>").addClass("noBorder").append(revertButton));
					var trs = $("td#"+nodeId+"_transformed tr");
					$(trs[trs.length-1]).append($("<td>").addClass("noBorder").append(revertButton));
					updateResult();
					return;
				}));	
			}
		});
	});
}

function FetchCleanningRawData(hnodeId,worksheetId) {
	var info = new Object();
	info["vWorksheetId"] = worksheetId;
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["hNodeId"] = hnodeId;
	info["command"] = "FetchTransformingDataCommand";
	var json = {};
	var returned = $.ajax({
		url : "RequestController",
		type : "POST",
		async: false,
		data : info,
		dataType : "json",
		complete : function(xhr, textStatus) {
			json = $.parseJSON(xhr.responseText);
			json = json["elements"][0]["result"];
			hideCleanningWaitingSignOnScreen();
		},
		error : function(xhr, textStatus) {
			hideCleanningWaitingSignOnScreen();
			$.sticky("Error in Fetching Raw Data!");
		}
	});
	return json;
}
function handleGenerateCleaningRulesButton() {
	var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
	var selectedHNodeId = columnHeadingMenu.data("parentCellId");
	if(jQuery.type(selectedHNodeId) === "array")
	{
		selectedHNodeId = selectedHNodeId.join("#");
	}
	
	var tdTag = $("td#" + selectedHNodeId);
	var vWorksheetId = tdTag.parents("div.Worksheet").attr("id");
	var examples = columnHeadingMenu.data("cleaningExamples");
	var info = new Object();
	info["vWorksheetId"] = vWorksheetId;
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["hNodeId"] = selectedHNodeId;
	info["command"] = "GenerateCleaningRulesCommand";
	info["examples"] = JSON.stringify(examples);
	info["cellIDs"] =JSON.stringify($("div#columnHeadingDropDownMenu").data("nodeIds"));

	var returned = $.ajax({
		url : "RequestController",
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


	//submit the transformed result apt
	function submit() {
		var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
		var selectedHNodeId = columnHeadingMenu.data("parentCellId");
		var tdTag = $("td#" + selectedHNodeId);
		var vWorksheetId = tdTag.parents("div.Worksheet").attr("id");
		var transformedRes = $("div#columnHeadingDropDownMenu").data("transformedResult");
		var info = new Object();
		info["vWorksheetID"] = vWorksheetId;
		info["hNodeID"] = selectedHNodeId;
		info["command"] = "SubmitCleanningCommand";
		info["workspaceId"] = $.workspaceGlobalInformation.id;
		info["examples"] = JSON.stringify(columnHeadingMenu.data("cleaningExamples"));
		var returned = $.ajax({
			url : "RequestController",
			type : "POST",
			data : info,
			dataType : "json",
			complete : function(xhr, textStatus) {
				var json = $.parseJSON(xhr.responseText);
				hideCleanningWaitingSignOnScreen();
				parse(json);
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
	console.log(examples)
	if(examples.length == 0) {
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
	} 
	/*else//use the trimmed data
	{
		populateResult(newdata[0]);
		var pdata = getVaritions(newdata);
		populateVariations(newdata[0]["top"],pdata);
	}*/
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
