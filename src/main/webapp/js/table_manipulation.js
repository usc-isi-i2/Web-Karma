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
function handleTableCellEditButton(event) {
	var tableCellDiv = $("div#tableCellEditDiv");
	var tdTagId = $("div#tableCellToolBarMenu").data("parentCellId");
	var tdTag = $("td#"+tdTagId);
	$("#editCellTextArea", tableCellDiv).remove();
	
	if(tdTag.hasClass("hasTruncatedValue")) {
		tableCellDiv.append($("<textarea>")
						.attr("id", "editCellTextArea")
						.text(tdTag.data("fullValue")));
	} else {
		tableCellDiv.append($("<textarea>")
						.attr("id", "editCellTextArea")
						.text($("#"+tdTagId + " span.cellValue").text()));
	}
	
	var positionArray = [event.clientX-150		// distance from left
					, event.clientY-10];	// distance from top
	
	tableCellDiv.dialog({ title: 'Edit Cell Value',
			buttons: { "Cancel": function() { $(this).dialog("close"); }, "Submit":submitEdit }, width: 300, height: 150, position: positionArray});
	tableCellDiv.data("tdTagId", tdTagId);
}

function submitEdit(value, settings) {
	$("#tableCellEditDiv").dialog("close");
	var edits = new Object();
	// console.log($("#tableCellEditDiv").data("tdTagId"));
	var tdTag = $("#" + $("#tableCellEditDiv").data("tdTagId"));
	
	edits["value"] = $("#editCellTextArea").val();
	edits["command"] = "EditCellCommand";
	edits["nodeId"] = $(tdTag).attr("id");
	edits["vWorksheetId"] = $(tdTag).parents(".Worksheet").attr("id");
	
	edits["workspaceId"] = $.workspaceGlobalInformation.id;

	var returned = $.ajax({
	   	url: "RequestController", 
	   	type: "POST",
	   	data : edits,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	    		var json = $.parseJSON(xhr.responseText);
	    		parse(json);
		   	}
		});
 }


function openAddNewColumnDialog(event) {
    var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
    var hNodeId = columnHeadingMenu.data("parentCellId");
    $("#addNewColumnNameInput").val("");
    $("#addNewColumnDefaultValueInput").val("");
    $("div#addNewColumnDialogDiv span.error").hide();

    var addNewColumnDiv = $("div#addNewColumnDialogDiv");
    var positionArray = [event.clientX-100		// distance from left
        , event.clientY-10];	// distance from top
    addNewColumnDiv.dialog({ title: 'Add new column',
        buttons: { "Cancel": function() { $(this).dialog("close"); }, "Submit":submitAddNewColumn },
        width: 300,
        height: 150,
        position: positionArray
    });
}

function submitAddNewColumn() {
    var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
    var selectedHNodeId = columnHeadingMenu.data("parentCellId");
    var worksheetId = $("td#" + selectedHNodeId).parents("table.WorksheetTable").attr("id");
    var newColumnValue = $.trim($("#addNewColumnNameInput").val());
    var defaultValue = $.trim($("#addNewColumnDefaultValueInput").val());

    var validationResult = true;
    if (!newColumnValue)
        validationResult = false;
    // Check if the column name already exists
    var columnNameDivs = $("#" + worksheetId + " div.ColumnHeadingNameDiv");
    $.each(columnNameDivs, function(index, element) {
        if ($.trim($(element).text()) == newColumnValue) {
            validationResult = false;
        }
    });
    if (!validationResult) {
        $("div#addNewColumnDialogDiv span.error").show();
        $("#addNewColumnNameInput").focus();
        return false;
    }

    $("div#addNewColumnDialogDiv").dialog("close");

    var info = new Object();
    info["vWorksheetId"] = $("td#" + selectedHNodeId).parents("table.WorksheetTable").attr("id");
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["hNodeId"] = selectedHNodeId
    info["hTableId"] = ""
    info["newColumnName"] = "new_column"
    info["command"] = "AddColumnCommand";

    var newInfo = [];	// Used for commands that take JSONArray as input
    newInfo.push(getParamObject("hNodeId", selectedHNodeId,"hNodeId"));
    newInfo.push(getParamObject("hTableId", "","other"));
    newInfo.push(getParamObject("vWorksheetId", $("td#" + selectedHNodeId).parents("table.WorksheetTable").attr("id"),"vWorksheetId"));
    newInfo.push(getParamObject("newColumnName", newColumnValue,"other"));
    newInfo.push(getParamObject("defaultValue", defaultValue,"other"));
    info["newInfo"] = JSON.stringify(newInfo);

    //console.log(info["vWorksheetId"]);
    showLoading(info["vWorksheetId"]);

    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                //alert(xhr.responseText);
                var json = $.parseJSON(xhr.responseText);
                parse(json);
                hideLoading(info["vWorksheetId"]);
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while removing semantic types!" + textStatus);
                hideLoading(info["vWorksheetId"]);
            }
    });
}

function assignHandlersToRenameButton(event) {
    var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
    var hNodeId = columnHeadingMenu.data("parentCellId");

    $("#renameColumnNameInput").val($("td#" + hNodeId + " div.ColumnHeadingNameDiv").text());
    var renameColumnDiv = $("div#renameColumnDiv");
    var positionArray = [event.clientX-100		// distance from left
        , event.clientY-10];	// distance from top
    renameColumnDiv.dialog({ title: 'Rename column',
        buttons: { "Cancel": function() { $(this).dialog("close"); }, "Submit":submitRenameColumn }, width: 180, height: 180, position: positionArray});
}

function submitRenameColumn(value, settings) {
    var newColumnValue = $.trim($("#renameColumnNameInput").val());
    if (!newColumnValue) {
        $("div#renameColumnDiv span.error").show();
        return false;
    }

    $("div#renameColumnDiv").dialog("close");

    var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
    var hNodeId = columnHeadingMenu.data("parentCellId");
    var vWorksheetId = $("td#" + hNodeId).parents("table.WorksheetTable").attr("id");

    var info = new Object();
    var newInfo = [];   // for input parameters
    newInfo.push(getParamObject("vWorksheetId", vWorksheetId ,"vWorksheetId"));
    newInfo.push(getParamObject("hNodeId", hNodeId,"hNodeId"));
    newInfo.push(getParamObject("newColumnName", newColumnValue, "other"));
    newInfo.push(getParamObject("getAlignmentUpdate", ($("#svgDiv_" + vWorksheetId).length >0), "other"));
    info["newInfo"] = JSON.stringify(newInfo);
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "RenameColumnCommand";

    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                parse(json);
            },
        error :
            function (xhr, textStatus) {
                $.sticky("Error occured while renaming column!");
            }
    });
}




function handlePublishModelToStoreButton(event) {
	var tableCellDiv = $("div#r2rmlModelNameDiv");
	var positionArray = [event.clientX-150		// distance from left
					, event.clientY-10];	// distance from top
	$('#txtR2RMLModelName').val('');
	
	var r2rmlDialogBox = $("div#PublishR2RMLModelDialogBox");
	 $('#txtR2RML_URL').val('http://'+window.location.host + '/openrdf-sesame/repositories/karma_models');
	 $('#browseRepo').attr('href', 'http://'+window.location.host + '/openrdf-workbench/repositories/karma_models/summary');
	// Show the dialog box
	r2rmlDialogBox.dialog({
		width: 400, 
		title : "Publish Model",
		buttons: { "Cancel": function() { $(this).dialog("close"); }, "Submit": submitModelName },
		resize: function(event, ui) { 
		}
		
	});
	/*
	tableCellDiv.dialog({ title: 'R2RML Model Name',
			buttons: { "Cancel": function() { $(this).dialog("close"); }, "Submit":submitModelName }, width: 300, height: 150, position: positionArray});
		*/
}

function submitModelName(value, settings) {
	$("div#PublishR2RMLModelDialogBox").dialog("close");

	var info = new Object();
	info["vWorksheetId"] = $("div#WorksheetOptionsDiv").data("worksheetId");
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "GenerateR2RMLModelCommand";
	info['tripleStoreUrl'] = $('#txtR2RML_URL').val();
        
    showLoading(info["vWorksheetId"]);
    var returned = $.ajax({
        url: "RequestController", 
        type: "POST",
        data : info,
        dataType : "json",
        complete : 
            function (xhr, textStatus) {
                //alert(xhr.responseText);
                var json = $.parseJSON(xhr.responseText);
                parse(json);
                hideLoading(info["vWorksheetId"]);
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while exporting CSV!" + textStatus);
                hideLoading(info["vWorksheetId"]);
            }          
    });
 }

 
function submitSelectedModelNameToBeLoaded() {
	$('div#modelListDiv').dialog("close");
	var optionsDiv = $("div#WorksheetOptionsDiv");
	var value = $("#modelListRadioBtnGrp").find("input:checked");
	
	var info = new Object();
	info["vWorksheetId"] = optionsDiv.data("worksheetId");
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "InvokeDataMiningServiceCommand";
	info['modelName'] = value.val();
    
	
    var returned = $.ajax({
		url: "RequestController", 
		type: "POST",
		data : info,
		dataType : "json",
		complete : 
			function (xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			parse(json);
		},
		error :
			function (xhr, textStatus) {
			alert("Error occured while applying model from Triple Store!" + textStatus);
		}		   
	});
 }