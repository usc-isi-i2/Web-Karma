/**
 * @author Shubham Gupta
 */

function submitEdit(value, settings) {
	$("#tableCellEditDiv").dialog("close");
	var edits = new Object();
	
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
	   			//alert(xhr.responseText);
	    		var json = $.parseJSON(xhr.responseText);
	    		parse(json);
		   	}
		});
 }