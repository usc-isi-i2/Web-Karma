function changeSemanticType(event) {
	var optionsDiv = $("#ChangeSemanticTypesDialogBox");
	
	console.log($(this).data("hNodeId"));
	$("#ChangeSemanticTypesDialogBox").data("currentNodeId",$(this).data("hNodeId"));
	$("table#CRFSuggestedLabelsTable tr",optionsDiv).remove();
	$("div#ontologyOptionsTable", optionsDiv).hide();
	
	var positionArray = [event.clientX-150		// distance from left
					, event.clientY+10];	// distance from top
	
	// Populate with possible labels that CRF Model suggested
	var labelsTable = $("table#CRFSuggestedLabelsTable");
	var labelsElem = $(this).data("crfInfo");
	if(labelsElem != null){
		$.each(labelsElem["Labels"], function(index, label) {
			// Turning the probability into percentage
			var prob = label["Probability"];
			var percentage = Math.floor(prob*100);
			
			var trTag = $("<tr>");
			
			var radioButton = $("<input>")
							.attr("type", "radio")
							.attr("id", label["Type"])
							.attr("name", "semanticTypeGroup")
							.attr("value", label["Type"])
							.val(label["Type"]);
			if(index == labelsElem["Labels"].length-1)
				radioButton.attr('checked',true);
			
			trTag.append($("<td>").append(radioButton))
				.append($("<td>").append($("<label>").text(label["DisplayLabel"]).attr("for",label["Type"])))
				.append($("<td>").text("Probability: " + percentage+"%"));
			labelsTable.prepend(trTag);
		});
	}
	
	
	// Adding the choose from ontology radio button
	var radioButton = $("<input>")
						.attr("type", "radio")
						.attr("id", "chooseFromOnotologyRadioButton")
						.attr("name", "semanticTypeGroup")
						.attr("value", "ChooseFromOntology")
						.val("ChooseFromOntology");
	labelsTable.append($("<tr>")
		.append($("<td>")
			.append(radioButton))
		.append($("<td>")
			.append($("<label>").text("Choose From Ontology").attr("for","chooseFromOnotologyRadioButton")))
	);
	
	// Adding the handlers to the radio buttons
	$("input:radio[@name='semanticTypeGroup']").change(function(){
		if($(this).val() == "ChooseFromOntology"){
			$("div#ontologyOptionsTable").show();
			$("#chooseClass").attr('checked', false);
			$("#chooseDataProperty").attr('checked', false);
		} else if ($(this).val()=="Choose Class" || $(this).val() == "Choose Data Property"){
			// Dont know why this is also reached in some cases!
		}
		else{
			$("#ChangeSemanticTypesDialogBox").data("currentSelection", $(this).val());
			$("div#ontologyOptionsTable").hide();
		}
	});
	
	// Show the dialog box
	optionsDiv.dialog({width: 300, height: 600, position: positionArray
		, buttons: { "Cancel": function() { $(this).dialog("close"); }, "Submit":submitSemanticTypeChange }});
}

function attachOntologyOptionsRadioButtonHandlers() {
	$("input:radio[@name='ontologyOptionGroup']").change(function(){
		// Send a request to get the JSON for displaying the list of classes
		var info = new Object();
		info["workspaceId"] = $.workspaceGlobalInformation.id;
		
		
		if($(this).val()=="Choose Class") {
			info["command"] = "GetOntologyClassHierarchyCommand";
		} else if($(this).val()=="Choose Data Property")
			info["command"] = "GetDataPropertyListCommand";
		else {}
			
		var returned = $.ajax({
		   	url: "/RequestController", 
		   	type: "POST",
		   	data : info,
		   	dataType : "json",
		   	complete : 
		   		function (xhr, textStatus) {
		   			//alert(xhr.responseText);
		    		var json = $.parseJSON(xhr.responseText);
		    		var dataArray = json["elements"][0]["data"];
		    		
		    		var listDiv = $("div#ontologyOptionsList");
		    		$(listDiv).jstree({ 
						"json_data" : {
							"data" : dataArray
						},
						"themes" : {
							"theme" : "apple",
							"url": "css/jstree-themes/apple/style.css",
							"dots" : true,
							"icons" : false
						},
						
						"plugins" : [ "themes", "json_data", "ui" ,"sort", "search"]
					}).bind("select_node.jstree", function (e, data) { 
						$("#ChangeSemanticTypesDialogBox").data("currentSelection",data.rslt.obj.data("URI"));
						//alert(data.rslt.obj.data("URI"));
					});
			   	},
			error :
				function (xhr, textStatus) {
		   			alert("Error occured while fetching ontology data!" + textStatus);
			   	}		   
		});
	});
	
	// Add handler for the search button
	$("#searchOntologyList").click(function(){
		$("div#ontologyOptionsList").jstree("search", $("#searchQueryOntologyList").val());
	});
}


function submitSemanticTypeChange() {
	var info = new Object();
	var hNodeId = $("#ChangeSemanticTypesDialogBox").data("currentNodeId");
	info["command"] = "SetSemanticTypeCommand";
	info["vWorksheetId"] = $("td.columnHeadingCell#" + hNodeId).parents("table.WorksheetTable").attr("id");
	info["hNodeId"] = hNodeId;
	console.log("Node ID: " + info["hNodeId"]);
	info["newType"] = $("#ChangeSemanticTypesDialogBox").data("currentSelection");
	console.log("Type: " + info["newType"]);
	
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	
	var returned = $.ajax({
	   	url: "/RequestController", 
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	   			console.log(xhr.responseText);
	    		var json = $.parseJSON(xhr.responseText);
	    		parse(json);
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured with fetching new rows! " + textStatus);
		   	}
	});
	
	$("#ChangeSemanticTypesDialogBox").dialog("close");
}


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
	// TODO Reset the CSV import options
	$("#CSVPreviewTable tr").remove();
	$("#CSVPreviewTable").append($("<tr>").append($("<td>").addClass("rowIndexCell").text("File Row Number")));
	
	var responseJSON = $.parseJSON(response);
	var headers = responseJSON["elements"][0]["headers"];
	
	//Change the source name
	$("#CSVSourceName").text(responseJSON["elements"][0]["fileName"]);
	
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
		$("#CSVPreviewTable").append(trTag);
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
		$("#CSVPreviewTable").append(trTag);
	}
	
	// Populate the data
	var rows = responseJSON["elements"][0]["rows"];
	$.each(rows, function(index, row) {
		var trTag = $("<tr>");
		$.each(row, function(index2, val) {
			if(index2 == 0) {
				trTag.append($("<td>").addClass("rowIndexCell").text(val));
			} else {
				trTag.append($("<td>").text(val));
			}
		});
		$("#CSVPreviewTable").append(trTag);
	});
	
	// Attach the command ID
	$("#CSVImportDiv").data("commandId", responseJSON["elements"][0]["commandId"]);
	
	// Open the dialog
	$("#CSVImportDiv").dialog({ modal: true , width: 820, title: 'Import CSV File Options',
		buttons: { "Cancel": function() { $(this).dialog("close"); }, "Import":CSVImportOptionsChanged}});
}

function CSVImportOptionsChanged(flag) {
	
	var options = new Object();
	options["command"] = "ImportCSVFileCommand";
	options["commandId"] = $("#CSVImportDiv").data("commandId");
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
	    			parse($.parseJSON(xhr.responseText));
	    		}		
		   	}
		});	
}

function resetCSVDialogOptions() {
	$("#delimiterSelector :nth-child(1)").attr('selected', 'selected');
	$("#CSVHeaderLineIndex").val("1");
	$("#startRowIndex").val("2");
	$("#textQualifier").val("\"");
}

function handleTableCellEditButton(event) {
	var tdTagId = $("#tableCellToolBarMenu").data("parentCellId");
	$("#tableCellEditDiv #editCellTextArea").remove();
	
	if($("#"+tdTagId).hasClass("expandValueCell")) {
		$("#tableCellEditDiv").append($("<textarea>")
						.attr("id", "editCellTextArea")
						.text($("#"+tdTagId).data("fullValue")));
	} else {
		$("#tableCellEditDiv").append($("<textarea>")
						.attr("id", "editCellTextArea")
						.text($("#"+tdTagId + " div.cellValue").text()));
	}
	
	var positionArray = [event.clientX-150		// distance from left
					, event.clientY-10];	// distance from top
	
	$("#tableCellEditDiv").dialog({ title: 'Edit Cell Value',
			buttons: { "Cancel": function() { $(this).dialog("close"); }, "Submit":submitEdit }, width: 300, height: 150, position: positionArray});
	console.log(tdTagId);
	$("#tableCellEditDiv").data("tdTagId", tdTagId);
}

function openWorksheetOptions(event) {
	$("#WorksheetOptionsDiv").css({'position':'fixed', 
			'left':(event.clientX - 75) + 'px', 'top':(event.clientY+4)+'px'});
	$("#WorksheetOptionsDiv").show();
	
	$("#WorksheetOptionsDiv").data("worksheetId", $(this).parents("div.Worksheet").attr("id"));
}

function styleAndAssignHandlersToWorksheetOptionButtons() {
	// Styling the elements
	$("#WorksheetOptionsDiv").hide().addClass("ui-corner-all");
	$("#WorksheetOptionsDiv button").button();
	
	// Adding mouse handlers to the div
	$("#WorksheetOptionsDiv").mouseenter(function() {
		$(this).show();
	});
	$("#WorksheetOptionsDiv").mouseleave(function() {
		$(this).hide();
	});
	
	// Adding handlers to the buttons
	$("#generateSemanticTypesButton").click(function(){
		$("#WorksheetOptionsDiv").hide();
		
		console.log("Generating semantic types for table with ID: " + $("#WorksheetOptionsDiv").data("worksheetId"));
		var info = new Object();
		info["vWorksheetId"] = $("#WorksheetOptionsDiv").data("worksheetId");
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
	
	$("#alignToOntologyButton").click(function(){
		$("#WorksheetOptionsDiv").hide();
		
		console.log("Aligning the table with ID: " + $("#WorksheetOptionsDiv").data("worksheetId"));
		var info = new Object();
		info["vWorksheetId"] = $("#WorksheetOptionsDiv").data("worksheetId");
		info["workspaceId"] = $.workspaceGlobalInformation.id;
		info["command"] = "AlignToOntologyCommand";
			
		var returned = $.ajax({
		   	url: "/RequestController", 
		   	type: "POST",
		   	data : info,
		   	dataType : "json",
		   	complete : 
		   		function (xhr, textStatus) {
		   			alert(xhr.responseText);
		    		// var json = $.parseJSON(xhr.responseText);
		    		// parse(json);
			   	},
			error :
				function (xhr, textStatus) {
		   			//alert("Error occured while generating semantic types!" + textStatus);
			   	}		   
		});
	});
}







