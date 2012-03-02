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
	$("button#showModel").click(function(){
		optionsDiv.hide();
		
		// console.log("Showing model for table with ID: " +optionsDiv.data("worksheetId"));
		var info = new Object();
		info["vWorksheetId"] = optionsDiv.data("worksheetId");
		info["workspaceId"] = $.workspaceGlobalInformation.id;
		info["command"] = "ShowModelCommand";
			
		showLoading(info["vWorksheetId"]);
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
		    		hideLoading(info["vWorksheetId"]);
			   	},
			error :
				function (xhr, textStatus) {
		   			alert("Error occured while generating semantic types!" + textStatus);
		   			hideLoading(info["vWorksheetId"]);
			   	}		   
		});
	});
	
	$("button#hideModel").click(function(){
		optionsDiv.hide();
		var table = $("table#" + optionsDiv.data("worksheetId"));
		$("tr.AlignmentRow", table).remove();
		$("div.semanticTypeDiv", table).remove();
	});
	
	$("button#resetModel").click(function(){
		optionsDiv.hide();
		
		var info = new Object();
		info["vWorksheetId"] = optionsDiv.data("worksheetId");
		info["workspaceId"] = $.workspaceGlobalInformation.id;
		info["command"] = "ResetModelCommand";
			
		showLoading(info["vWorksheetId"]);
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
		    		hideLoading(info["vWorksheetId"]);
			   	},
			error :
				function (xhr, textStatus) {
		   			alert("Error occured while removing semantic types!" + textStatus);
		   			hideLoading(info["vWorksheetId"]);
			   	}		   
		});
	});

	$("button#publishRDF").click(function(){
		optionsDiv.hide();
		showHideRdfInfo();
		getPreferences();
		var rdfDialogBox = $("div#PublishRDFDialogBox");
		// Show the dialog box
		rdfDialogBox.dialog({width: 300
			, buttons: { "Cancel": function() { $(this).dialog("close"); }, "Submit": publishRDFFunction }});

	});

	$("button#splitByComma").click(function(){
		optionsDiv.hide();
		
		// console.log("Splitting by comma for table: " + optionsDiv.data("worksheetId"));
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
	});
}

function openWorksheetOptions(event) {
	$("div#WorksheetOptionsDiv")
			.css({'position':'fixed', 'left':(event.clientX - 75) + 'px', 'top':(event.clientY+4)+'px'})
			.data("worksheetId", $(this).parents("div.Worksheet").attr("id"))
			.show();
}

function splitColumnByComma() {
	$("div#SplitByCommaColumnListPanel").dialog("close");
	var selectedHNodeId = $("select#splitByCommaColumnList option:selected").val();
	
	var info = new Object();
	info["vWorksheetId"] = $("div#WorksheetOptionsDiv").data("worksheetId");
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["hNodeId"] = selectedHNodeId;
	info["command"] = "SplitByCommaCommand";
			
	showLoading(info["vWorksheetId"]);
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
	    		hideLoading(info["vWorksheetId"]);
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured while splitting a column by comma! " + textStatus);
	   			hideLoading(info["vWorksheetId"]);
		   	}		   
	});
}

function showSemanticTypeInfo() {
	// var crfData = $(this).data("crfInfo");
	// var table = $("div#ColumnCRFModelInfoBox table");
	// $("tr", table).remove();
// 	
	// $.each(crfData["Labels"], function(index, label) {
		// var trTag = $("<tr>");
		// trTag.append($("<td>").text(label["Type"]))
			// .append($("<td>").text(label["Probability"]));
	// });
}

function hideSemanticTypeInfo() {
	
}

function styleAndAssignHandlersToTableCellMenu() {
	var optionsDiv = $("div#tableCellMenuButtonDiv");
	var tableCellMenu = $("div#tableCellToolBarMenu");
	
	// Stylize the buttons
	$("button", tableCellMenu).button();
	
	// Assign handlers
	optionsDiv.click(openTableCellOptions)
		.button({
			icons: {
				primary: 'ui-icon-triangle-1-s'
		    },
			text: false
			})
		.mouseenter(function(){
			$(this).show();
		})	
		.mouseleave(function(){
			tableCellMenu.hide();
	})
	
	$("button#editCellButton").click(function(event){
		handleTableCellEditButton(event);
	});
	
	$("button#expandValueButton" ).click(function(event){
		handleEableCellExpandButton(event);
	});
	
	// Hide the option button when mouse leaves the menu
	tableCellMenu.mouseenter(function(){
		$(this).show();
	})
	.mouseleave(function() {
		optionsDiv.hide();
		$(this).hide();
	});
}

function handleEableCellExpandButton(event) {
	var tdTagId = $("#tableCellToolBarMenu").data("parentCellId");
	// Get the full expanded value
	var value = ""
	var tdTag = $("td#"+tdTagId);
	if(tdTag.hasClass("hasTruncatedValue")) {
		value = tdTag.data("fullValue");
	} else {
		value = $("span.cellValue", tdTag).text();
	}
	
	// Get the RDF text (if it exists)
	var info = new Object();
	info["nodeId"] = tdTagId;
	info["vWorksheetId"] = tdTag.parents("div.Worksheet").attr("id");
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "PublishRDFCellCommand";
	
	var returned = $.ajax({
	   	url: "/RequestController", 
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	    		var json = $.parseJSON(xhr.responseText);
	    		if(json["elements"][0]["updateType"] == "PublishCellRDFUpdate") {
	    			var rdfText = json["elements"][0]["cellRdf"];
	    			$("div#rdfValue", expandDiv).html(rdfText);
	    		} else if (json["elements"][0]["updateType"] == "KarmaError") {
	    			var rdfText = "<i>" + json["elements"][0]["Error"] + "<i>";
	    			$("div#rdfValue", expandDiv).html(rdfText);
	    		}
		   	},
		error :
			function (xhr, textStatus) {
	   			$.sticky("Error occured while getting RDF triples for the cell! " + textStatus);
		   	}		   
	});
	
	
	var expandDiv = $("div#ExpandCellValueDialog");
	// Add the cell value
	$("div#cellExpandedValue", expandDiv).text(value);
	
	var positionArray = [event.clientX-150		// distance from left
					, event.clientY-10];	// distance from top
	expandDiv.show().dialog({height: 300, width: 500, show:'blind', position: positionArray});
}

function openTableCellOptions() {
	var tableCellMenu = $("div#tableCellToolBarMenu");
	tableCellMenu.data("parentCellId", $(this).data("parentCellId"));
	tableCellMenu.css({"position":"absolute",
		"top":$(this).offset().top + 15, 
		"left": $(this).offset().left + $(this).width()/2 - $(tableCellMenu).width()/2}).show();
}

function showTableCellMenuButton() {
	// Get the parent table
	var tdTag = $(this);
	var optionsDiv = $("div#tableCellMenuButtonDiv");
	optionsDiv.data("parentCellId", tdTag.attr("id"));
	
	// Show it at the right place
	var top = $(tdTag).offset().top + $(tdTag).height()-18;
	var left = $(tdTag).offset().left + $(tdTag).width()-25;
	optionsDiv.css({"position":"absolute",
		"top":top, 
		"left": left}).show();
}

function hideTableCellMenuButton() {
	$("div#tableCellMenuButtonDiv").hide();
}

function styleAndAssignHandlersToColumnHeadingMenu() {
	var optionsDiv = $("div#columnHeadingMenuButtonDiv");
	var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
	
	$("button", columnHeadingMenu).button();
	
	optionsDiv.click(openColumnHeadingOptions)
		.button({
			icons: {
				primary: 'ui-icon-triangle-1-s'
		    },
			text: false
			})
		.mouseenter(function(){
			$(this).show();
		})	
		.mouseleave(function(){
			columnHeadingMenu.hide();
	})
	// Hide the option button when mouse leaves the menu
	columnHeadingMenu.mouseleave(function() {
		optionsDiv.hide();
		columnHeadingMenu.hide();
	}).mouseenter(function(){
		$(this).show();
	});
	
	// Assign handler to column clean button (in cleaning.js)
    assignHandlersToCleaningPanelObjects();
}

function openColumnHeadingOptions() {
	var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
	columnHeadingMenu.data("parentCellId", $(this).data("parentCellId"));
	columnHeadingMenu.css({"position":"absolute",
		"top":$(this).offset().top + 15, 
		"left": $(this).offset().left + $(this).width()/2 - $(columnHeadingMenu).width()/2}).show();
}

function showColumnOptionButton(event) {
	var tdTag = $(this);
	var optionsDiv = $("div#columnHeadingMenuButtonDiv");
	optionsDiv.data("parentCellId", tdTag.attr("id"));
    
    // Show it at the right place
	var top = $(tdTag).offset().top + $(tdTag).height()-18;
	var left = $(tdTag).offset().left + $(tdTag).width()-25;
	optionsDiv.css({"position":"absolute",
		"top":top, 
		"left": left}).show();
}

function hideColumnOptionButton() {    
	$("div#columnHeadingMenuButtonDiv").hide();    
}

function showLoading(worksheetId) {
    var coverDiv = $("<div>").attr("id","WaitingDiv_"+worksheetId).addClass('waitingDiv')
        .append($("<div>").html('<b>Please wait</b>')
            .append($('<img>').attr("src","images/ajax-loader.gif"))
    );
     
    var spaceToCoverDiv = $("div#"+worksheetId);
    spaceToCoverDiv.append(coverDiv.css({"position":"absolute", "height":spaceToCoverDiv.height(), 
        "width": spaceToCoverDiv.width(), "top":spaceToCoverDiv.position().top, "left":spaceToCoverDiv.position().left}).show());
}

function hideLoading(worksheetId) {
	$("div#WaitingDiv_"+worksheetId).hide();
}














