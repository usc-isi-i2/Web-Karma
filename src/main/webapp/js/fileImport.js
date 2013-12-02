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

function showFileImportOptions(responseJSON, command) {
  var previewTable = $("#FilePreviewTable");
  var importDiv = $("#FileImportDiv");

  $("tr", previewTable).remove();
  previewTable.append($("<tr>").append($("<td>").addClass("rowIndexCell").text("File Row Number")));
	
  var headers = null;
  if (responseJSON) {
    headers = responseJSON["elements"][0]["headers"];
	var encoding = responseJSON["elements"][0]["encoding"];
	$("#fileEncoding").val(encoding);
	var maxNumLines = responseJSON["elements"][0]["maxNumLines"];
	$("#fileMaxNumLines").val(maxNumLines);
	if(maxNumLines == -1)
		$("#colMaxNumLines").hide();
	else
		$("#colMaxNumLines").show();
  }
  //Change the source name
  $("#FileSourceName", importDiv).text(responseJSON["elements"][0]["fileName"]);
  var rows = responseJSON["elements"][0]["rows"];
  
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
    previewTable.append(trTag);
  } else {
	  if(rows) {
		  // Put empty column names
	    var trTag = $("<tr>");
	    $.each(rows[0], function(index, val) {
	      if(index == 0){
	        trTag.append($("<td>").addClass("rowIndexCell").text("-"));
	      } else {
	        trTag.append($("<th>").text("Column_" + index).addClass("ItalicColumnNames"));
	      }
				
	    });
	    previewTable.append(trTag);
	  }
  }
	
  // Populate the data
  if(rows) {
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
	    previewTable.append(trTag);
	  });
	  
	  $("#filePreviewTableDiv").show();
  } else {
	  $("#filePreviewTableDiv").hide();
  }
  // Attach the command ID
  importDiv.data("commandId", responseJSON["elements"][0]["commandId"]);
  importDiv.data("command", command);
  
  //console.log("fileImport: Got command: '" + command + "'");
  if(command == "JSONFile" || command == "XMLFile") {
	  $("#fileMaxName").html("objects");
  } else {
	  $("#fileMaxName").html("lines");
  }
  
  // Open the dialog
  importDiv.dialog({
    modal: true , 
    width: 620, 
    title: 'Import File Options',
    buttons: {
      "Cancel": function() {
        $(this).dialog("close");
      }, 
      "Import":FileImportOptionsChanged
    }
  });
}

function FileImportOptionsChanged(flag) {
  var importDiv = $("#FileImportDiv");
  var options = new Object();
  var command = importDiv.data("command"); 
 // console.log("FileImportOptionsChanged: " + command);
  options["command"] = "Import" + command + "Command";
  options["commandId"] = importDiv.data("commandId");
 
  options["encoding"] = $("#fileEncoding").val();
  options["maxNumLines"] = $("#fileMaxNumLines").val();
  options["workspaceId"] = $.workspaceGlobalInformation.id;
  options["interactionType"] = "generatePreview";
	
  // Import the file if Import button invoked this function
  if(typeof(flag) == "object") {
	  console.log("Got flag to imprt table now");
    options["execute"] = true;
    options["interactionType"] = "importTable";
    $("#FileImportDiv").dialog("close");
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
        showFileImportOptions($.parseJSON(xhr.responseText), command);
      } else {
        var json = $.parseJSON(xhr.responseText);
        parse(json);
        hideWaitingSignOnScreen();
        if(command !== "Ontology") 
        	showDialogToLoadModel();
      }		
    }
  });	
}

function resetFileDialogOptions() {
  var importDiv = $("div#FileImportDiv");
  $("#encoding", importDiv).val("\"");
  $("#maxNumLines", importDiv).val("1000");
}