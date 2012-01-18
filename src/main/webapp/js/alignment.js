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
							.val(label["Type"]);
				
			if(label["Domain"] != null)
				radioButton.data("domain", label["Domain"]);
				
			var selectedFlag = false;
			if(fullType == label["Type"]) {
				if(domain == "") {
					radioButton.attr('checked',true);
					selectedFlag = true;
				} else {
					if(label["Domain"] != null) {
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
			else {
				var typeItalicSpan = $("<span>").addClass("italic").text(label["DisplayLabel"]);
				console.log(label["DisplayLabel"]);
				typeLabel.text(" of " + label['DisplayDomainLabel']);
				typeLabel.prepend($(typeItalicSpan));
			}
				
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
		if($(this).data("domain") != null)
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
	optionsDiv.dialog({width: 400, height: 650, position: positionArray
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
		if(optionsDiv.data("domain") != null)
			info["domain"] = optionsDiv.data("domain");
		else
			info["domain"] = "";
		
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
							var trTag = $("<tr>").addClass("AlternativeLink");
							
							var radioButton = $("<input>")
								.attr("type", "radio")
								.attr("id", edge["edgeId"])
								.attr("name", "AlternativeLinksGroup")
								.attr("value", edge["edgeId"])
								.val(edge["edgeLabel"])
								.data("isDuplicate", false);
								
							var typeItalicSpan = $("<span>").addClass("italic").text(edge["edgeLabel"]);	
							var linkLabel = $("<label>").attr("for",edge["edgeId"]).text(edge["edgeSource"] + " ").append(typeItalicSpan);
							var linkLabelTd = $("<td>").append(linkLabel); 
							
							trTag.append($("<td>").append(radioButton))
								.append(linkLabelTd);
								
							if(edge["selected"]) {
								radioButton.attr("checked", true);
								// Add the Duplicate button
								var dupButton = $("<button>").addClass("duplicateClass").text("Duplicate").click(duplicateLink);
								$(dupButton).button();
								linkLabelTd.append(dupButton);
							}
								
							table.append(trTag);
						});
						// Show the dialog box
						optionsDiv.dialog({width: 300, height: 300, position: positionArray
							, buttons: { "Cancel": function() { $(this).dialog("close"); }, "Submit":submitAlignmentLinkChange }});
							
						$("input:radio[@name='AlternativeLinksGroup']").change(function(){
							if($(this).data("isDuplicate"))
								optionsDiv.data("currentSelection", $(this).data("edgeId"));
							else
								optionsDiv.data("currentSelection", $(this).attr("id"));
								
							// Remove the button from the previously selected radio button and add it to the current one
							var buttonClone = $("button", optionsDiv).clone(true);
							$("button", optionsDiv).remove();
							$("td:eq(1)",$(this).parents("tr")).append(buttonClone);
								
							optionsDiv.data("alignmentId", info["alignmentId"]);
							optionsDiv.data("worksheetId", info["worksheetId"]);
							optionsDiv.data("isDuplicate", $(this).data("isDuplicate"));
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

function duplicateLink() {
	var optionsPanel = $("div#OntologyAlternativeLinksPanel");
	var currentRow = $(this).parents("tr.AlternativeLink", optionsPanel);
	
	// Create a clone row
	var dupRow = $(currentRow).clone(true);
	
	// Hide the duplicate button from the duplicate row
	$("button", dupRow).hide();
	
	// Change the id etc for the dup row
	var numRand = Math.floor(Math.random()*101);
	$("input", dupRow).attr("id", numRand);
	$("label", dupRow).attr("for", numRand);
	$("label", dupRow).attr("value", numRand);
	$("input", dupRow).data("edgeId", $("input", currentRow).attr("id"));

	currentRow.after(dupRow);
	$("input", dupRow).attr("checked", false);
	$("input", dupRow).data("isDuplicate", true);
	$("input", currentRow).attr("checked", true);
}

function submitAlignmentLinkChange() {
	var optionsDiv = $("div#OntologyAlternativeLinksPanel");
	
	var info = new Object();
	if(optionsDiv.data("isDuplicate"))
		info["command"] = "DuplicateDomainOfLinkCommand";
	else
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


