function attachOntologyOptionsRadioButtonHandlers() {
	$("select#toggleOntologyHierarchy").change(function(){
		if($("select#toggleOntologyHierarchy").val() == "class"){
			$("td#firstColumnLabel").text("Class");
			$("td#secondColumnLabel").text("Data Property").hide();
		} else {
			$("td#firstColumnLabel").text("Data Property");
			$("td#secondColumnLabel").text("Domain (Class)").hide();
		}
		
		populatefirstColumnOntologyBox();
		$("div#secondColumnOntologyBox").hide();
	});

	$("div#secondColumnOntologyBox").hide();
	
	// Add handler for the search button
	$("#firstColumnKeyword").keyup(function(event) {
		if(event.keyCode == 13){
    		$("#submitFirstColumnSearch").click();
  		}
	});
	$("#secondColumnKeyword").keyup(function(event) {
		if(event.keyCode == 13){
    		$("#submitSecondColumnSearch").click();
  		}
	});
	$("#submitFirstColumnSearch").click(function(){
		$("div#firstColumnTree").jstree("search", $("#firstColumnKeyword").val());
	});
	$("#submitSecondColumnSearch").click(function(){
		$("div#secondColumnTree").jstree("search", $("#secondColumnKeyword").val());
	});
	
	// Assign empty domain to the Unassigned radio button
	$("input#UnassignTypeButton").data("Domain", "");
}

function changeSemanticType(event) {
	var optionsDiv = $("#ChangeSemanticTypesDialogBox");
	
	optionsDiv.data("currentNodeId",$(this).data("hNodeId"));
	$("table#CRFSuggestedLabelsTable tr",optionsDiv).remove();
	$("#firstColumnKeyword").val("");
	$("#secondColumnKeyword").val("");
	$("div#secondColumnOntologyBox").hide();
	//$("div#ontologyOptionsTable", optionsDiv).hide();
	
	var positionArray = [event.clientX+20		// distance from left
					, event.clientY+10];	// distance from top
	
	// Populate with possible labels that CRF Model suggested
	var labelsTable = $("table#CRFSuggestedLabelsTable");
	var labelsElem = $(this).data("crfInfo");
	var fullType = $(this).data("fullType");
	var domain = $(this).data("domain");
	var origin = $(this).data("origin");
	
	if(labelsElem != null){
		$("span", labelsTable).remove();
		$.each(labelsElem["Labels"], function(index, label) {
			// Turning the probability into percentage
			var prob = label["Probability"];
			var percentage = Math.floor(prob*100);
			var trTag = $("<tr>");
			var radioButton = $("<input>")
							.attr("type", "radio")
							.attr("id", label["Type"] + "|" + label["Domain"])
							.attr("name", "semanticTypeGroup")
							.attr("value", label["Type"])
							.val(label["Type"])
							.data("domain", label["Domain"]);
				
			var selectedFlag = false;
			if(fullType == label["Type"]) {
				if(domain == "") {
					radioButton.attr('checked',true);
					selectedFlag = true;
				} else {
					if(label["Domain"].length != 0) {
						if(domain == label["Domain"]){
							radioButton.attr('checked',true);
							selectedFlag = true;
						}
					}
				}
			}
				
				
			var typeLabel = $("<label>").attr("for",label["Type"] + "|" + label["Domain"]);
			
			// Check if the domain needs to be displayed
			if($.trim(label["DisplayDomainLabel"]) == "")
				typeLabel.text(label["DisplayLabel"]);
			else
				typeLabel.text(label["DisplayLabel"] + " of " + label['DisplayDomainLabel']);
				
			// Check if the label was assigned by the user
			var score = "";
			if(selectedFlag && origin == "User")
				score = "Probability: " + percentage+"% (User Assigned)"
			else
				score = "Probability: " + percentage+"%";
				
			trTag.append($("<td>").append(radioButton))
				.append($("<td>").append(typeLabel))
				.append($("<td>").text(score));
			labelsTable.prepend(trTag);
		});
	} else {
		labelsTable.html("<span class='smallSizedFont'><i>&nbsp;&nbsp;none</i></span>");
	}
	
	if(fullType == "Unassigned") {
		$("input#UnassignTypeButton").attr("checked", true);
	}
	
	// Adding the handlers to the radio buttons
	$("input:radio[@name='semanticTypeGroup']").change(function(){
		optionsDiv.data("type", $(this).val());
		optionsDiv.data("domain", $(this).data("domain"));
		optionsDiv.data("source", "RadioButtonList");
		$("div#firstColumnTree").jstree("deselect_all");
		$("div#secondColumnTree").jstree("deselect_all");
	});
	
	// Populate the class tree
	$("#toggleOntologyHierarchy").val("class");
	$("td#firstColumnLabel").text("Class");
	$("td#secondColumnLabel").text("Data Property").hide();
	// Send a request to get the JSON for displaying the list of classes
	optionsDiv.data("secondColumnSelection","");
	optionsDiv.data("firstColumnSelection","");
	populatefirstColumnOntologyBox();
	
	
	// Show the dialog box
	optionsDiv.dialog({width: 400, height: 620, position: positionArray
		, buttons: { "Cancel": function() { $(this).dialog("close"); }, "Submit":submitSemanticTypeChange }});
}

function populatefirstColumnOntologyBox(){
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	
	if($("#toggleOntologyHierarchy").val() == "class")
		info["command"] = "GetOntologyClassHierarchyCommand";
	else 
		info["command"] = "GetDataPropertyHierarchyCommand";
		
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
	    		
	    		var listDiv = $("div#firstColumnTree");
	    		
	    		if(dataArray.length == 0) {
	    			$(listDiv).html("<i>none</i>")
	    		} else {
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
						$("#ChangeSemanticTypesDialogBox").data("source","OntologyHierarchy");
						$("#ChangeSemanticTypesDialogBox").data("firstColumnSelection",data.rslt.obj.data("URI"));
						$("input:radio[@name='semanticTypeGroup']").attr("checked", false);
						$("#UnassignTypeButton").attr('checked',false);
						populateSecondColumnOntologyBox();
					});
	    		} 
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured while fetching ontology data!" + textStatus);
		   	}		   
	});
}

function populateSecondColumnOntologyBox() {
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["URI"] = $("#ChangeSemanticTypesDialogBox").data("firstColumnSelection");
	
	if($("#toggleOntologyHierarchy").val() == "class")
		info["command"] = "GetDataPropertiesForClassCommand";
	else 
		info["command"] = "GetDomainsForDataPropertyCommand";
		
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
	    		var listDiv = $("div#secondColumnTree");
	    		
	    		if(dataArray.length == 0) {
	    			$(listDiv).html("<i>none</i>")
	    		} else {
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
						$("#ChangeSemanticTypesDialogBox").data("secondColumnSelection",data.rslt.obj.data("URI"));
					});
	    		}
	    		
				$("div#secondColumnOntologyBox").show();
				$("td#secondColumnLabel").show();
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured while fetching ontology data!" + textStatus);
		   	}		   
	});
}

function submitSemanticTypeChange() {
	var optionsDiv = $("#ChangeSemanticTypesDialogBox");
	if($("#toggleOntologyHierarchy").val() == "dataProperty" 
		&& optionsDiv.data("secondColumnSelection") == "") {
		alert("Please specify the domain for the data property!");
		return;
	}
	
	var info = new Object();
	var hNodeId = optionsDiv.data("currentNodeId");
	info["command"] = "SetSemanticTypeCommand";
	info["vWorksheetId"] = $("td.columnHeadingCell#" + hNodeId).parents("table.WorksheetTable").attr("id");
	info["hNodeId"] = hNodeId;
	
	if(optionsDiv.data("source") == "RadioButtonList") {
		info["type"] = optionsDiv.data("type");
		info["domain"] = optionsDiv.data("domain");
		
		// Check if the user selected the unassigned  option
		if(info["type"] == "UnassignType") {
			info["command"] = "UnassignSemanticTypeCommand";
		}
		
		if(info["domain"] == "")
			info["resourceType"] = "Class";
		else
			info["resourceType"] = "DataProperty";
			
	} else if (optionsDiv.data("source") == "OntologyHierarchy") {
		if($("#toggleOntologyHierarchy").val() == "class") {
			if(optionsDiv.data("secondColumnSelection") == "") {
				info["resourceType"] = "Class";
				info["type"] = optionsDiv.data("firstColumnSelection");
			} else {
				info["resourceType"] = "DataProperty";
				info["domain"] = optionsDiv.data("firstColumnSelection");
				info["type"] = optionsDiv.data("secondColumnSelection");
			}
		} else {
			info["resourceType"] = "DataProperty";
			info["type"] = optionsDiv.data("firstColumnSelection");
			info["domain"] = optionsDiv.data("secondColumnSelection");
		}
	}
	
	
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	
	var returned = $.ajax({
	   	url: "/RequestController", 
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
	   			alert("Error occured with fetching new rows! " + textStatus);
		   	}
	});
	
	optionsDiv.dialog("close");
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

function showAlternativeParents(event) {
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["nodeId"] = $(this).parents("td.columnHeadingCell").data("jsonElement")["contentCell"]["id"];
	info["command"] = "GetAlternativeLinksCommand";
	info["alignmentId"] = $(this).parents("table.WorksheetTable").data("alignmentId");
	info["worksheetId"] = $(this).parents("table.WorksheetTable").attr("id");
		
	var returned = $.ajax({
	   	url: "/RequestController", 
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	   			// alert(xhr.responseText);
	    		var json = $.parseJSON(xhr.responseText);
	    		$.each(json["elements"], function(index, element) {
	    			if(element["updateType"] == "GetAlternativeLinks") {
	    				var optionsDiv = $("div#OntologyAlternativeLinksPanel");
	    				var table = $("table", optionsDiv);
	    				$("tr", table).remove();
	    				var positionArray = [event.clientX+20		// distance from left
									, event.clientY+10];	// distance from top
						
						$.each(element["Edges"], function(index2, edge) {
							var trTag = $("<tr>");
							
							var radioButton = $("<input>")
								.attr("type", "radio")
								.attr("id", edge["edgeId"])
								.attr("name", "AlternativeLinksGroup")
								.attr("value", edge["edgeId"])
								.val(edge["edgeLabel"]);
								
							var linkLabel = $("<label>").attr("for",edge["edgeId"]).text(edge["edgeSource"] + " " + edge["edgeLabel"]);
							
							trTag.append($("<td>").append(radioButton))
								.append($("<td>").append(linkLabel));
								
							table.append(trTag);
						});
						// Show the dialog box
						optionsDiv.dialog({width: 300, height: 300, position: positionArray
							, buttons: { "Cancel": function() { $(this).dialog("close"); }, "Submit":submitAlignmentLinkChange }});
							
						$("input:radio[@name='AlternativeLinksGroup']").change(function(){
							optionsDiv.data("currentSelection", $(this).attr("id"));
							optionsDiv.data("alignmentId", info["alignmentId"]);
							optionsDiv.data("worksheetId", info["worksheetId"]);
						});
	    			}
	    		});
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured while getting alternative links!" + textStatus);
		   	}		   
	});
}

function submitAlignmentLinkChange() {
	var optionsDiv = $("div#OntologyAlternativeLinksPanel");
	
	var info = new Object();
	info["command"] = "AddUserLinkToAlignmentCommand";
	info["vWorksheetId"] = optionsDiv.data("worksheetId");
	info["alignmentId"] = optionsDiv.data("alignmentId");
	info["edgeId"] = optionsDiv.data("currentSelection");
	
	
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	
	var returned = $.ajax({
	   	url: "/RequestController", 
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
	   			alert("Error occured with fetching new rows! " + textStatus);
		   	}
	});
	
	optionsDiv.dialog("close");
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
}







