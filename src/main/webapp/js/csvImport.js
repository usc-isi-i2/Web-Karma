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

function showCSVImportOptions(responseJSON, dialogVisible) {
  var csvPreviewTable = $("#CSVPreviewTable");
  var csvImportDiv = $("#CSVImportDiv");
  // TODO Reset the CSV import options
  $("tr", csvPreviewTable).remove();
  //Pedro csvPreviewTable.append($("<tr>").append($("<td>").addClass("rowIndexCell").text("File Row Number")));
	
  var headers = null;
  if (responseJSON) {
    headers = responseJSON["elements"][0]["headers"];
	var encoding = responseJSON["elements"][0]["encoding"];
	$("#encoding").val(encoding);
	var maxNumLines = responseJSON["elements"][0]["maxNumLines"];
	$("#maxNumLines").val(maxNumLines);
  }
  //Change the source name
  $("#CSVSourceName", csvImportDiv).text(responseJSON["elements"][0]["fileName"]);
	
  // Populate the headers
  if(headers != null)  {
    var trTag = $("<tr>");
    $.each(headers, function(index, val) {
      if(index == 0){
        trTag.append($("<td>").addClass("rowIndexCell").text(val));
      } else {
        trTag.append($("<th>").text(val));
      }
    });
    csvPreviewTable.append(trTag);
  } else {
    // Put empty column names
    var trTag = $("<tr>");
    $.each(responseJSON["elements"][0]["rows"][0], function(index, val) {
      if(index == 0){
        trTag.append($("<td>").addClass("rowIndexCell").text("-"));
      } else {
        trTag.append($("<th>").text("Column_" + index).addClass("ItalicColumnNames"));
      }
			
    });
    csvPreviewTable.append(trTag);
  }
	
  // Populate the data
  var rows = responseJSON["elements"][0]["rows"];
  $.each(rows, function(index, row) {
    var trTag = $("<tr>");
    $.each(row, function(index2, val) {
      var displayVal = val;
      if(displayVal.length > 20) {
        displayVal = displayVal.substring(0,20) + "...";
      }
      if(index2 == 0) {
        trTag.append($("<td>").addClass("rowIndexCell").text(displayVal));
      } else {
        trTag.append($("<td>").text(displayVal));
      }
    });
    csvPreviewTable.append(trTag);
  });
	
  // Attach the command ID
  csvImportDiv.data("commandId", responseJSON["elements"][0]["commandId"]);
	
  if(dialogVisible) {
	  //It is already visible, dont try to reload it
	 //else it prevents the cascaded events from happening
	 //like onclick on import button after onchange of textfield happens
  } else {
	  // Open the dialog
	  csvImportDiv.dialog({
	    modal: true , 
	    width: 820, 
	    title: 'Import CSV File Options',
	    buttons: {
	      "Cancel": function() {
	        $(this).dialog("close");
	      }, 
	      "Import":CSVImportOptionsChanged
	    }
	  });
  }
}

function CSVImportOptionsChanged(flag) {
  var csvImportDiv = $("#CSVImportDiv");
  var options = new Object();
  options["command"] = "ImportCSVFileCommand";
  options["commandId"] = csvImportDiv.data("commandId");
  options["delimiter"] = $("#delimiterSelector").val();
  options["CSVHeaderLineIndex"] = $("#CSVHeaderLineIndex").val();
  options["startRowIndex"] = $("#startRowIndex").val();
  options["textQualifier"] = $("#textQualifier").val();
  options["encoding"] = $("#encoding").val();
  options["maxNumLines"] = $("#maxNumLines").val();
  options["workspaceId"] = $.workspaceGlobalInformation.id;
  options["interactionType"] = "generatePreview";
	
  // Import the CSV if Import button invoked this function
  if(typeof(flag) == "object") {
    options["execute"] = true;
    options["interactionType"] = "importTable";
    $("#CSVImportDiv").dialog("close");
    showWaitingSignOnScreen();
  }
	
    
  var returned = $.ajax({
    url: "RequestController", 
    type: "POST",
    data : options,
    dataType : "json",
    complete : 
    function (xhr, textStatus) {
      if(!options["execute"]) {
        showCSVImportOptions($.parseJSON(xhr.responseText), true);
      } else {
        var json = $.parseJSON(xhr.responseText);
        parse(json);
        hideWaitingSignOnScreen();
        showDialogToLoadModel();
      }		
    }
  });	
}

function resetCSVDialogOptions() {
  var csvImportDiv = $("div#CSVImportDiv");
  $("#delimiterSelector :nth-child(1)", csvImportDiv).attr('selected', 'selected');
  $("#CSVHeaderLineIndex", csvImportDiv).val("1");
  $("#startRowIndex", csvImportDiv).val("2");
  $("#textQualifier", csvImportDiv).val("\"");
  $("#encoding", csvImportDiv).val("\"");
  $("#encoding", csvImportDiv).val("1000");
}