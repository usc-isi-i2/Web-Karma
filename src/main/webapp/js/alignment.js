function attachOntologyOptionsRadioButtonHandlers() {
    var optionsDiv = $("#ChangeSemanticTypesDialogBox");
    
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
	
	// Adding the handlers to the radio buttons
    $("input[name='semanticTypeGroup']:radio").live("change", (function(){
        optionsDiv.data("FullType", $(this).val());
        optionsDiv.data("Domain", $(this).data("Domain"));
        optionsDiv.data("DisplayLabel", $(this).data("DisplayLabel"));
        optionsDiv.data("DisplayDomainLabel", $(this).data("DisplayDomainLabel"));
        
        // optionsDiv.data("DisplayLabel",$("label",$($("td", $(this).parents("tr"))[1])).html());
        optionsDiv.data("source", "RadioButtonList");
        $("div#firstColumnTree").jstree("deselect_all");
        $("div#secondColumnTree").jstree("deselect_all");
    }));
	
	$("input[name='currentSemanticTypeGroup']:radio").live("change", (function(){
	    var semTypesArray = optionsDiv.data("SemanticTypesArray");
	    var type = $(this).parents("tr").data("FullType");
	    var domain = $(this).parents("tr").data("Domain");
        $.each(semTypesArray, function(index, semType) {
            if(semType["FullType"] == type && semType["Domain"] == domain) {
                semType["isPrimary"] = true;
            } else
                semType["isPrimary"] = false;
        });
        //console.log(JSON.stringify(semTypesArray));
    }));
}

function changeSemanticType(event) {
	var optionsDiv = $("#ChangeSemanticTypesDialogBox");
	
	var typeJsonObject = $(this).data("typesJsonObject");
	optionsDiv.data("currentNodeId",typeJsonObject["HNodeId"]);
	$("table#CRFSuggestedLabelsTable tr",optionsDiv).remove();
	$("table#currentSemanticTypesTable tr",optionsDiv).remove();
	$("#firstColumnKeyword").val("");
	$("#secondColumnKeyword").val("");
	$("div#secondColumnOntologyBox").hide();
	$("input#chooseClassKey").attr("checked", false);
	//$("div#ontologyOptionsTable", optionsDiv).hide();
	
    var labelsTable = $("table#CRFSuggestedLabelsTable");
	var existingTypes = typeJsonObject["SemanticTypesArray"];
	var CRFInfo = typeJsonObject["FullCRFModel"];
	var fullType = "";
	var domain = "";
	
	if(existingTypes.length == 0) {
        $("span#NoSemanticTypeText", optionsDiv).show();
    } else {
        var primIndex = getPrimarySemTypeObject(existingTypes);
        var primTypeObject = existingTypes[primIndex];
        
        // Populate with possible labels that CRF Model suggested
        fullType = primTypeObject["FullType"];
        domain = primTypeObject["Domain"];
        var origin = primTypeObject["Origin"];
        
        // Populate the table to show the current semantic types
        $.each(existingTypes, function(index, type) {
            if(index == 0)
                addSemTypeObjectToCurrentTable(type, true);
            else
                addSemTypeObjectToCurrentTable(type, false);
        });
        
        // Check/uncheck the "Mark as key for the class." option depending on the existing status
        if(primTypeObject["isPartOfKey"] == true)
            $("input#chooseClassKey").attr("checked", true);
    }
	
	// Use an array to store all the selected semantic types
	optionsDiv.data("SemanticTypesArray", existingTypes);
	
	/* Show the suggested semantic types with radio button choices */
	if(CRFInfo != null) {
		$("span", labelsTable).remove();
		$.each(CRFInfo["Labels"], function(index, label) {
			// Turning the probability into percentage
			var prob = label["Probability"];
			var percentage = Math.floor(prob*100);
			var trTag = $("<tr>");
			var radioButton = $("<input>")
							.attr("type", "radio")
							.attr("id", label["FullType"] + "|" + label["Domain"])
							.attr("name", "semanticTypeGroup")
							.attr("value", label["FullType"])
							.val(label["FullType"]);
		  
		    radioButton.data("FullType", label["FullType"]);
            radioButton.data("Domain", label["Domain"]);
            radioButton.data("DisplayLabel", label["DisplayLabel"]);
            radioButton.data("DisplayDomainLabel", label["DisplayDomainLabel"]);
				
			var selectedFlag = false;
			if(fullType == label["FullType"]) {
				if(domain == "") {
					radioButton.attr('checked',true);
					optionsDiv.data("FullType", fullType);
					optionsDiv.data("source", "RadioButtonList");
					selectedFlag = true;
					optionsDiv.data("DisplayLabel", label["DisplayLabel"]);
				} else {
					if(label["Domain"] != null) {
						if(domain == label["Domain"]){
							radioButton.attr('checked',true);
							optionsDiv.data("source", "RadioButtonList");
							optionsDiv.data("Domain", domain);
							optionsDiv.data("FullType", fullType);
							selectedFlag = true;
							optionsDiv.data("DisplayLabel", "<span class='italic'>" + label["DisplayLabel"] + "</span> of " + label["DisplayDomainLabel"]);
						}
					}
				}
			}
				
			var typeLabel = $("<label>").attr("for",label["FullType"] + "|" + label["Domain"]);
			
			// Check if the domain needs to be displayed
			if($.trim(label["DisplayDomainLabel"]) == "")
				typeLabel.text(label["DisplayLabel"]);
			else {
				var typeItalicSpan = $("<span>").addClass("italic").text(label["DisplayLabel"]);
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
	
	// Populate the class tree
	$("#toggleOntologyHierarchy").val("class");
	$("td#firstColumnLabel").text("Class");
	$("td#secondColumnLabel").text("Data Property").hide();
	// Send a request to get the JSON for displaying the list of classes
	optionsDiv.data("secondColumnSelection","");
	optionsDiv.data("firstColumnSelection","");
	populatefirstColumnOntologyBox();
	
	// Show the dialog box
	var positionArray = [event.clientX+20, event.clientY+10];
	optionsDiv.dialog({width: 500, height: 700, position: positionArray
		, buttons: { 
    		"Cancel": function() { $(this).dialog("close"); }, 
    		"Add": function(){
                $("div#firstColumnTree").jstree("deselect_all");
                $("div#secondColumnTree").jstree("deselect_all");
                addTypeToArrayAndShowInTable();
    		}, 
            "Submit":submitSemanticTypeChange }
        });
}

function addSemTypeObjectToCurrentTable(semTypeObject, isFirst) {
    var table = $("#currentSemanticTypesTable");
    // Add the row that says Primary
    if(isFirst) {
        $("span#NoSemanticTypeText").hide();
        table.append($("<tr>")
            .append($("<td>").text('Primary'))
            .append($("<td>"))
        );
    }
    var displayLabel = "";
    if(semTypeObject["Domain"].length == 0 || semTypeObject["Domain"] == "") 
        displayLabel = semTypeObject["DisplayLabel"];
    else
        displayLabel = "<span class='italic'>" + semTypeObject["DisplayLabel"] + "</span> of " + semTypeObject["DisplayDomainLabel"];
        
    var trTag = $("<tr>").addClass("semTypeRow")
        .data("FullType", semTypeObject["FullType"])
        .data("Domain", semTypeObject["Domain"])
        .append($("<td>").append($("<input>")
            .attr("type", "radio")
            .attr("id", semTypeObject["FullType"] + "|" + semTypeObject["Domain"] + "_cur")
            .attr("name", "currentSemanticTypeGroup")
            .attr("value", semTypeObject["DisplayLabel"])
            .val(semTypeObject["DisplayLabel"])))
        .append($("<td>")
            .append($("<label>")
                .attr("for",semTypeObject["FullType"] + "|" + semTypeObject["Domain"]+ "_cur").html(displayLabel)))
        .append($("<td>").append($("<span>").addClass("ui-icon ui-icon-closethick")).click(deleteSemanticTypeFromArrayAndTable));
    
    if(isFirst)
        $("input", trTag).attr('checked', true);
    table.append(trTag);
}

function addTypeToArrayAndShowInTable() {
    var optionsDiv = $("#ChangeSemanticTypesDialogBox");
    if($("#toggleOntologyHierarchy").val() == "dataProperty" 
        && optionsDiv.data("secondColumnSelection") == "") {
        alert("Please specify the domain for the data property!");
        return;
    }
    
    var info = new Object();
    var hNodeId = optionsDiv.data("currentNodeId");

    if(optionsDiv.data("source") == "RadioButtonList") {
        info["FullType"] = optionsDiv.data("FullType");
        info["Domain"] = optionsDiv.data("Domain");
        info["DisplayLabel"] = optionsDiv.data("DisplayLabel");
        info["DisplayDomainLabel"] = optionsDiv.data("DisplayDomainLabel");

        if(info["Domain"] == "")
            info["ResourceType"] = "Class";
        else
            info["ResourceType"] = "DataProperty";
            
    } else if (optionsDiv.data("source") == "OntologyHierarchy") {
        if($("#toggleOntologyHierarchy").val() == "class") {
            if(optionsDiv.data("secondColumnSelection") == "") {
                info["ResourceType"] = "Class";
                info["FullType"] = optionsDiv.data("firstColumnSelection");
                info["Domain"] = ""
                info["DisplayLabel"] = optionsDiv.data("firstColumnDisplayLabel");
                info["DisplayDomainLabel"] = "";
                // optionsDiv.data("DisplayLabel", optionsDiv.data("firstColumnDisplayLabel"));
            } else {
                info["ResourceType"] = "DataProperty";
                info["Domain"] = optionsDiv.data("firstColumnSelection");
                info["FullType"] = optionsDiv.data("secondColumnSelection");
                info["DisplayLabel"] = optionsDiv.data("secondColumnDisplayLabel");
                info["DisplayDomainLabel"] = optionsDiv.data("firstColumnDisplayLabel");
                // optionsDiv.data("DisplayLabel", 
                    // "<span class='italic'>" + optionsDiv.data("secondColumnDisplayLabel") + "</span> of " + optionsDiv.data("firstColumnDisplayLabel"));
            }
        } else {
            info["ResourceType"] = "DataProperty";
            info["FullType"] = optionsDiv.data("firstColumnSelection");
            info["Domain"] = optionsDiv.data("secondColumnSelection");
            info["DisplayLabel"] = optionsDiv.data("firstColumnDisplayLabel");
            info["DisplayDomainLabel"] = optionsDiv.data("secondColumnDisplayLabel");
            // optionsDiv.data("DisplayLabel", 
                // "<span class='italic'>" + optionsDiv.data("firstColumnDisplayLabel") + "</span> of " + optionsDiv.data("secondColumnDisplayLabel"));
        }
    }
    
    // Sanity check
    if(info["FullType"] == "" && info["Domain"] == "")
        return false;
    
    /* Insert it into the array */
    var semTypesArray = optionsDiv.data("SemanticTypesArray");
    // Check if same type already exists
    var exists = false;
    $.each(semTypesArray, function(index, semType) {
        if(semType["FullType"] == info["FullType"]) {
            if(semType["ResourceType"] == info["ResourceType"] && info["ResourceType"] == "Class") {
                //console.log("Type already exists!");
                exists = true;
                return false;
            } else if (semType["Domain"] == info["Domain"]) {
                //console.log("Type already exists!");
                exists = true;
                return false;                
            }
        }
    });
    
    if(exists)
        return false;
    semTypesArray.push(info);
    
    /* Show it in the table */
    var table = $("#currentSemanticTypesTable");
    if(semTypesArray.length == 1) {
        info["isPrimary"] = true;
        addSemTypeObjectToCurrentTable(info, true);
    } else {
        info["isPrimary"] = false;
        addSemTypeObjectToCurrentTable(info, false);
    }
    
    // Clear all values
    optionsDiv.data("secondColumnSelection","");
    optionsDiv.data("firstColumnSelection","");
}

function deleteSemanticTypeFromArrayAndTable() {
    /* Get the associated type and domain */
    var trTag = $(this).parents("tr.semTypeRow");
    var type = $(trTag).data("FullType");
    var domain = $(trTag).data("Domain");
    // Remove from table
    trTag.remove();
    
    // Delete from the array
    semTypesArray = $("#ChangeSemanticTypesDialogBox").data("SemanticTypesArray");
    
    var selectedIndex = -1;
    $.each(semTypesArray, function(index, semType){
        if(semType["FullType"] == type && (semType["Domain"] == domain)) {
                selectedIndex = index;
                return false;
        }
    });
    if(selectedIndex != -1)
        semTypesArray.splice(selectedIndex, 1);
    if(semTypesArray.length == 0) {
        $("table#currentSemanticTypesTable tr").remove();
        $("span#NoSemanticTypeText").show();
    }
    
    // Check if it was marked as primary
    if($("input", trTag).is(":checked") && (semTypesArray.length != 0)){
        // Mark the first one as primary and trigger the change event
        var table = $("table#currentSemanticTypesTable");
        $("input", table).first().attr("checked", true);
        
        $("input[name='currentSemanticTypeGroup'][id='"+$("input", table).first().attr('id')+"']:radio").trigger('change');
    }
}

function populatefirstColumnOntologyBox(){
	var optionsDiv = $("#ChangeSemanticTypesDialogBox")
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
						optionsDiv.data("source","OntologyHierarchy")
						      .data("firstColumnSelection",data.rslt.obj.data("URI"))
						      .data("firstColumnDisplayLabel",data.rslt.obj.context.lastChild.wholeText);
						
						$("input[name='semanticTypeGroup']:radio").attr("checked", false);
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
						$("#ChangeSemanticTypesDialogBox").data("secondColumnSelection",data.rslt.obj.data("URI"))
						  .data("secondColumnDisplayLabel",data.rslt.obj.context.lastChild.wholeText);
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
	
	var info = new Object();
	var hNodeId = optionsDiv.data("currentNodeId");
	info["command"] = "SetSemanticTypeCommand";
	info["vWorksheetId"] = $("td.columnHeadingCell#" + hNodeId).parents("table.WorksheetTable").attr("id");
	info["hNodeId"] = hNodeId;
	info["isKey"] = $("input#chooseClassKey").is(":checked");
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["SemanticTypesArray"] = JSON.stringify(optionsDiv.data("SemanticTypesArray"));
	
	if(optionsDiv.data("SemanticTypesArray").length == 0)
	    info["command"] = "UnassignSemanticTypeCommand";
    else
        info["command"] = "SetSemanticTypeCommand";
	
	showLoading(info["vWorksheetId"]);
	var returned = $.ajax({
	   	url: "/RequestController", 
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	    		var json = $.parseJSON(xhr.responseText);
	    		parse(json);
	    		hideLoading(info["vWorksheetId"]);
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured with fetching new rows! " + textStatus);
	   			hideLoading(info["vWorksheetId"]);
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
						
						// Sort the edges by class names
						if(element["Edges"] && element["Edges"].length != 0) {
						    element["Edges"].sort(function(a,b){
						        var aName = a.edgeSource.toLowerCase();
                                var bName = b.edgeSource.toLowerCase();
                                
                                if(aName == bName) {
                                    var aEdge = a.edgeLabel.toLowerCase();
                                    var bEdge = b.edgeLabel.toLowerCase();
                                    return ((aEdge < bEdge) ? -1 : ((aEdge > bEdge) ? 1 : 0));
                                } else
                                    return ((aName < bName) ? -1 : ((aName > bName) ? 1 : 0));   
						    });
						}
						
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
	
	showLoading(info["vWorksheetId"]);
	var returned = $.ajax({
	   	url: "/RequestController", 
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	   			var json = $.parseJSON(xhr.responseText);
	    		parse(json);
	    		hideLoading(info["vWorksheetId"]);
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured with fetching new rows! " + textStatus);
	   			hideLoading(info["vWorksheetId"]);
		   	}
	});
	
	optionsDiv.dialog("close");
}

function getPrimarySemTypeObject(semanticTypesArray) {
    var indexType;
    $.each(semanticTypesArray, function(index, type){
        if(type["isPrimary"]){
            indexType = index;
            return false;
        }
    });
    return indexType;
}

