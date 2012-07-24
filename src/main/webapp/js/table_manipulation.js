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