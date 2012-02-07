/**
 * @author Shubham Gupta
 */

function handleTableCellEditButton(event) {
	var tableCellDiv = $("div#tableCellEditDiv");
	var tdTagId = $("div#tableCellToolBarMenu").data("parentCellId");
	$("#editCellTextArea", tableCellDiv).remove();
	
	if($("#"+tdTagId).hasClass("expandValueCell")) {
		tableCellDiv.append($("<textarea>")
						.attr("id", "editCellTextArea")
						.text($("#"+tdTagId).data("fullValue")));
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
	   	url: "/RequestController", 
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