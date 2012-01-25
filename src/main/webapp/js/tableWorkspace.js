function handlePrevNextLink() {
	if($(this).hasClass("inactiveLink"))
		return;
	// Prepare the data to be sent to the server	
	var info = new Object();
	var worksheetId = $(this).data("vWorksheetId");
	info["tableId"] = $(this).parents("div.pager").data("tableId");
	info["direction"] = $(this).data("direction");
	info["vWorksheetId"] = worksheetId;
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "TablePagerCommand";
		
	var returned = $.ajax({
	   	url: "/RequestController", 
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	   			//alert(xhr.responseText);
	    		var json = $.parseJSON(xhr.responseText);
	    		parse(json);
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured with fetching new rows! " + textStatus);
		   	}		   
	});
	return false;
	$(this).preventDefault();
}

function handlePagerResize() {
	if($(this).hasClass("pagerSizeSelected"))
		return;
		
	// $(this).siblings().removeClass("pagerSizeSelected");	
	// $(this).addClass("pagerSizeSelected");	
	
	// Prepare the data to be sent to the server	
	var info = new Object();
	
	var worksheetId = $(this).data("vWorksheetId");
	info["newPageSize"] = $(this).data("rowCount");
	info["tableId"] = $(this).parents("div.pager").data("tableId");
	info["vWorksheetId"] = worksheetId;
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "TablePagerResizeCommand";
		
	var returned = $.ajax({
	   	url: "/RequestController", 
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	   			//alert(xhr.responseText);
	    		var json = $.parseJSON(xhr.responseText);
	    		parse(json);
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured with fetching new rows! " + textStatus);
	   			
		   	}		   
	});
	return false;
	$(this).preventDefault();
}

function showCSVImportOptions(response) {
	var csvPreviewTable = $("#CSVPreviewTable");
	var csvImportDiv = $("#CSVImportDiv");
	// TODO Reset the CSV import options
	$("tr", csvPreviewTable).remove();
	csvPreviewTable.append($("<tr>").append($("<td>").addClass("rowIndexCell").text("File Row Number")));
	
	var responseJSON = $.parseJSON(response);
	var headers = responseJSON["elements"][0]["headers"];
	
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
	
	// Open the dialog
	csvImportDiv.dialog({ modal: true , width: 820, title: 'Import CSV File Options',
		buttons: { "Cancel": function() { $(this).dialog("close"); }, "Import":CSVImportOptionsChanged}});
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
	options["workspaceId"] = $.workspaceGlobalInformation.id;
	options["interactionType"] = "generatePreview";
	
	// Import the CSV if Import button invoked this function
	if(typeof(flag) == "object") {
		options["execute"] = true;
		options["interactionType"] = "importTable";
	}
		

	var returned = $.ajax({
	   	url: "/RequestController", 
	   	type: "POST",
	   	data : options,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	   			if(!options["execute"])
	    			showCSVImportOptions(xhr.responseText);
	    		else{
	    			$("#CSVImportDiv").dialog("close");
	    			var json = $.parseJSON(xhr.responseText);
	    			
	    			if(json["elements"][0]["updateType"] == "CSVImportError") {
		   				alert(json["elements"][0]["Error"]);
		   			} else
	    				parse(json);
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
}

function handleTableCellEditButton(event) {
	var tableCellDiv = $("#tableCellEditDiv");
	var tdTagId = $("#tableCellToolBarMenu").data("parentCellId");
	$("#editCellTextArea", tableCellDiv).remove();
	
	if($("#"+tdTagId).hasClass("expandValueCell")) {
		tableCellDiv.append($("<textarea>")
						.attr("id", "editCellTextArea")
						.text($("#"+tdTagId).data("fullValue")));
	} else {
		tableCellDiv.append($("<textarea>")
						.attr("id", "editCellTextArea")
						.text($("#"+tdTagId + " div.cellValue").text()));
	}
	
	var positionArray = [event.clientX-150		// distance from left
					, event.clientY-10];	// distance from top
	
	tableCellDiv.dialog({ title: 'Edit Cell Value',
			buttons: { "Cancel": function() { $(this).dialog("close"); }, "Submit":submitEdit }, width: 300, height: 150, position: positionArray});
	tableCellDiv.data("tdTagId", tdTagId);
}



function openWorksheetOptions(event) {
	$("div#WorksheetOptionsDiv")
			.css({'position':'fixed', 'left':(event.clientX - 75) + 'px', 'top':(event.clientY+4)+'px'})
			.data("worksheetId", $(this).parents("div.Worksheet").attr("id"))
			.show();
}

function styleAndAssignHandlersToWorksheetOptionButtons() {
	var optionsDiv = $("div#WorksheetOptionsDiv");
	// Styling the elements
	optionsDiv.addClass("ui-corner-all");
	$("button", optionsDiv).button();
	
	// Adding mouse handlers to the div
	optionsDiv.mouseenter(function() {
		$(this).show();
	});
	optionsDiv.mouseleave(function() {
		$(this).hide();
	});
	
	// Adding handlers to the buttons
	$("#generateSemanticTypesButton").click(function(){
		optionsDiv.hide();
		
		console.log("Generating semantic types for table with ID: " + $("#WorksheetOptionsDiv").data("worksheetId"));
		var info = new Object();
		info["vWorksheetId"] = optionsDiv.data("worksheetId");
		info["workspaceId"] = $.workspaceGlobalInformation.id;
		info["command"] = "GenerateSemanticTypesCommand";
			
		var returned = $.ajax({
		   	url: "/RequestController", 
		   	type: "POST",
		   	data : info,
		   	dataType : "json",
		   	complete : 
		   		function (xhr, textStatus) {
		   			//alert(xhr.responseText);
		    		var json = $.parseJSON(xhr.responseText);
		    		parse(json);
			   	},
			error :
				function (xhr, textStatus) {
		   			alert("Error occured while generating semantic types!" + textStatus);
			   	}		   
		});
	});
	
	$("button#showModel").click(function(){
		optionsDiv.hide();
		
		console.log("Showing model for table with ID: " +optionsDiv.data("worksheetId"));
		var info = new Object();
		info["vWorksheetId"] = optionsDiv.data("worksheetId");
		info["workspaceId"] = $.workspaceGlobalInformation.id;
		info["command"] = "ShowModelCommand";
			
		var returned = $.ajax({
		   	url: "/RequestController", 
		   	type: "POST",
		   	data : info,
		   	dataType : "json",
		   	complete : 
		   		function (xhr, textStatus) {
		   			//alert(xhr.responseText);
		    		var json = $.parseJSON(xhr.responseText);
		    		parse(json);
			   	},
			error :
				function (xhr, textStatus) {
		   			alert("Error occured while generating semantic types!" + textStatus);
			   	}		   
		});
	});
	
	$("button#hideModel").click(function(){
		optionsDiv.hide();
		var table = $("table#" + optionsDiv.data("worksheetId"));
		$("tr.AlignmentRow", table).remove();
		$("div.semanticTypeDiv", table).remove();
	});
	
	$("#alignToOntologyButton").click(function(){
		optionsDiv.hide();
		
		console.log("Aligning the table with ID: " + optionsDiv.data("worksheetId"));
		var info = new Object();
		info["vWorksheetId"] = optionsDiv.data("worksheetId");
		info["workspaceId"] = $.workspaceGlobalInformation.id;
		info["command"] = "AlignToOntologyCommand";
			
		var returned = $.ajax({
		   	url: "/RequestController", 
		   	type: "POST",
		   	data : info,
		   	dataType : "json",
		   	complete : 
		   		function (xhr, textStatus) {
		   			//alert(xhr.responseText);
		    		var json = $.parseJSON(xhr.responseText);
		    		parse(json);
			   	},
			error :
				function (xhr, textStatus) {
		   			//alert("Error occured while generating semantic types!" + textStatus);
			   	}		   
		});
	});
	
	$("button#splitByComma").click(function(){
		optionsDiv.hide();
		
		console.log("Splitting by comma for table: " + optionsDiv.data("worksheetId"));
		var table = $("table#" + optionsDiv.data("worksheetId"));
		var cols = $('td.columnHeadingCell[colspan="1"]', table);
		
		var columnListDiv = $("div#SplitByCommaColumnListPanel");
		var columnList = $("select#splitByCommaColumnList", columnListDiv);
		
		// Remove any existing option from the list
		$("option", columnList).remove();
		
		$.each(cols, function(index, col){
			if($("div.ColumnHeadingNameDiv",col).length != 0)
				columnList.append($("<option>").val($(col).attr("id")).text($("div.ColumnHeadingNameDiv",col).text()));
		});
		
		// Show the dialog box
		columnListDiv.dialog({width: 300, height: 150
			, buttons: { "Cancel": function() { $(this).dialog("close"); }, "Submit": splitColumnByComma }});
		// var info = new Object();
		// info["vWorksheetId"] = $("#WorksheetOptionsDiv").data("worksheetId");
		// info["workspaceId"] = $.workspaceGlobalInformation.id;
		// info["command"] = "ShowModelCommand";
			
		// var returned = $.ajax({
		   	// url: "/RequestController", 
		   	// type: "POST",
		   	// data : info,
		   	// dataType : "json",
		   	// complete : 
		   		// function (xhr, textStatus) {
		   			// //alert(xhr.responseText);
		    		// var json = $.parseJSON(xhr.responseText);
		    		// parse(json);
			   	// },
			// error :
				// function (xhr, textStatus) {
		   			// alert("Error occured while generating semantic types!" + textStatus);
			   	// }		   
		// });
	});
}

function splitColumnByComma() {
	$("div#SplitByCommaColumnListPanel").dialog("close");
	var selectedHNodeId = $("select#splitByCommaColumnList option:selected").val();
	
	var info = new Object();
	info["vWorksheetId"] = $("div#WorksheetOptionsDiv").data("worksheetId");
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["hNodeId"] = selectedHNodeId;
	info["command"] = "SplitByCommaCommand";
			
	var returned = $.ajax({
	   	url: "/RequestController", 
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	   			// alert(xhr.responseText);
	    		var json = $.parseJSON(xhr.responseText);
	    		parse(json);
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured while splitting a column by comma! " + textStatus);
		   	}		   
	});
}