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
	$.sticky("Under construction!");
	// if($(this).hasClass("pagerSizeSelected"))
		// return;
// 		
	// // $(this).siblings().removeClass("pagerSizeSelected");	
	// // $(this).addClass("pagerSizeSelected");	
// 	
	// // Prepare the data to be sent to the server	
	// var info = new Object();
// 	
	// var worksheetId = $(this).data("vWorksheetId");
	// info["newPageSize"] = $(this).data("rowCount");
	// info["tableId"] = $(this).parents("div.pager").data("tableId");
	// info["vWorksheetId"] = worksheetId;
	// info["workspaceId"] = $.workspaceGlobalInformation.id;
	// info["command"] = "TablePagerResizeCommand";
// 		
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
	   			// alert("Error occured with fetching new rows! " + textStatus);
// 	   			
		   	// }		   
	// });
	// return false;
	// $(this).preventDefault();
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
						.text($("#"+tdTagId + " span.cellValue").text()));
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
		
		// console.log("Generating semantic types for table with ID: " + $("#WorksheetOptionsDiv").data("worksheetId"));
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
		
		// console.log("Showing model for table with ID: " +optionsDiv.data("worksheetId"));
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
	
	$("button#publishRDF").click(function(){
		optionsDiv.hide();
		
		// console.log("Generate RDF for table with ID: " +optionsDiv.data("worksheetId"));
		var info = new Object();
		info["vWorksheetId"] = optionsDiv.data("worksheetId");
		info["workspaceId"] = $.workspaceGlobalInformation.id;
		info["command"] = "PublishRDFCommand";
			
		var returned = $.ajax({
		   	url: "/RequestController", 
		   	type: "POST",
		   	data : info,
		   	dataType : "json",
		   	complete : 
		   		function (xhr, textStatus) {
		   			//alert("RDF Generated!");
		    		var json = $.parseJSON(xhr.responseText);
		    		parse(json);
			   	},
			error :
				function (xhr, textStatus) {
		   			alert("Error occured while generating RDF!" + textStatus);
			   	}		   
		});
	});

	$("#alignToOntologyButton").click(function(){
		optionsDiv.hide();
		
		// console.log("Aligning the table with ID: " + optionsDiv.data("worksheetId"));
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

function showNestedTablePager() {
	// Get the parent table
	var tableId = $(this).parents("table").attr("id");
	var nestedTablePager = $("#nestedTablePager" + tableId);
	
	var pagerElem = $(this).data("pagerElem");
	changePagerOptions(pagerElem, nestedTablePager);
	
	nestedTablePager.css({"position":"absolute",
    					"top":$(this).offset().top + 10, 
    					"left": $(this).offset().left + $(this).width()/2 - nestedTablePager.width()/2}).show();
}

function changePagerOptions(pagerJSONElement, pagerDOMElement) {
	// Store the table Id information
	$(pagerDOMElement).data("tableId", pagerJSONElement["tableId"]);
	
	// Change the ___ of ___ rows information
	var totalRows = pagerJSONElement["numRecordsShown"] + pagerJSONElement["numRecordsBefore"] 
								+ pagerJSONElement["numRecordsAfter"];  
	var currentRowInfo = "" + (pagerJSONElement["numRecordsBefore"] +1) + " - " + 
				(pagerJSONElement["numRecordsShown"] + pagerJSONElement["numRecordsBefore"]);
	$("span.previousNextText", pagerDOMElement).text(currentRowInfo + " of " + totalRows);
	
	// Make the Previous link active/inactive as required
	if(pagerJSONElement["numRecordsBefore"] != 0) {
		var previousLink = $("a", pagerDOMElement)[3]; 
		if($(previousLink).hasClass("inactiveLink"))
			$(previousLink).removeClass("inactiveLink").addClass("activeLink");
	} else {
		if($(previousLink).hasClass("activeLink"))
			$(previousLink).removeClass("activeLink").addClass("inactiveLink");
	}
	
	// Make the Next link active/inactive as required
	if(pagerJSONElement["numRecordsAfter"] != 0){
		var nextLink = $("a", pagerDOMElement)[4];
		if($(nextLink).hasClass("inactiveLink"))
			$(nextLink).removeClass("inactiveLink").addClass("activeLink");
	} else {
		if($(nextLink).hasClass("activeLink"))
			$(nextLink).removeClass("activeLink").addClass("inactiveLink");
	}
	
	// Select the correct pager resize links
	$.each($("a.pagerResizeLink", pagerDOMElement), function(index, link) {
		if($(link).data("rowCount") == pagerJSONElement["desiredNumRecordsShown"]){
			$(link).addClass("pagerSizeSelected");
		} else {
			$(link).removeClass("pagerSizeSelected");
		}
	});
	
	return true;
}

function hideNestedTablePager() {
	// Get the parent table
	var table = $(this).parents("table");
	var nestedTablePager = $("#nestedTablePager" + table.attr("id"));
	nestedTablePager.hide();
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
	
	$("button#viewValueButton" ).click(function(){
		var tdTagId = $("#tableCellToolBarMenu").data("parentCellId");
		var value = $("td#" + tdTagId).data("fullValue");
		if(value) {
			$("#CellValueDialog").text(value).dialog({height: 300, width: 400, show:'blind'
				});
		}
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

function openTableCellOptions() {
	var tableCellMenu = $("div#tableCellToolBarMenu");
	tableCellMenu.data("parentCellId", $(this).data("parentCellId"));
	tableCellMenu.css({"position":"absolute",
		"top":$(this).offset().top + 15, 
		"left": $(this).offset().left + $(this).width()/2 - $(tableCellMenu).width()/2}).show();
    					
	// if($(this).parents("td").hasClass("expandValueCell")){
		// $("#viewValueButton").show();
		// $("#tableCellMenutriangle").css({"margin-left" : "32px"});
		// $("div#tableCellToolBarMenu").css({"width": "105px"});
	// } else {
		// $("#viewValueButton").hide();
		// $("#tableCellMenutriangle").css({"margin-left" : "10px"});
		// $("div#tableCellToolBarMenu").css({"width": "48px"});
	// }
	// $("div#tableCellToolBarMenu").css({"position":"absolute",
    					// "top":$(this).offset().top + 10, 
    					// "left": $(this).offset().left + $(this).width()/2 - $("div#tableCellToolBarMenu").width()/2}).show();
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
	
	// Assign handler to column rename button
	$("button#renameColumnButton").click(function() {
		
	});
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
};

function hideColumnOptionButton() {    
	$("div#columnHeadingMenuButtonDiv").hide();    
};


