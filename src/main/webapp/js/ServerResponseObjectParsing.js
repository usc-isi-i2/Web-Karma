/**
 * @author Shubham Gupta
 */

function parse(data) {

	$.workspaceGlobalInformation = {
		"id" : data["workspaceId"]
	}
	
	$.each(data["elements"], function(i, element){
		/* Update the worksheet list */
		if(element["updateType"] == "WorksheetListUpdate") {
			
			$.each(element["worksheets"], function(j, worksheet){
				// If worksheet doesn't exist yet
				if($("div#" + worksheet["worksheetId"]).length == 0){
					var mainDiv = $("<div>").attr("id", worksheet["worksheetId"]).addClass("Worksheet");
					mainDiv.data("isCollapsed", worksheet["isCollapsed"]);
					
					// Div for adding title of that worksheet
					var titleDiv = $("<div>").addClass("WorksheetTitleDiv ui-corner-top");
					titleDiv.append($("<div>")
									.text(worksheet["title"])
									.addClass("tableTitleTextDiv")
									.append($("<div>")
											.addClass("showHideWorkSheet")
											.attr("id", "hideShow"+worksheet["worksheetId"])
											.css({"float": "right"})
											.append(
												$("<img>").addClass("minimizeWorksheetImg")
														.attr("src", "../images/blue-box-minimize.png")
														.data("state", "open")
											)
											.click(function() {
												$("#" + worksheet["worksheetId"] + "TableDiv").toggle(400);
												$("#topLevelpagerOptions" + worksheet["worksheetId"]).toggle(400);
												// Change the corners
												titleDiv.toggleClass("ui-corner-top");
												titleDiv.toggleClass("ui-corner-all");
												
												// Change the icon
												var img = $(this).find("img");
												if(img.data("state") == "open") {
													img.attr("src", "../images/orange-maximize.png")
													img.data("state", "close")
												}
												else {
													img.attr("src", "../images/blue-box-minimize.png")
													img.data("state", "open")
												}
											})
											)
									);
					mainDiv.append(titleDiv);
					
					// Add the table (if it does not exists)
					var tableDiv = $("<div>").attr("id", worksheet["worksheetId"] + "TableDiv").addClass("TableDiv");
					var table = $("<table>").attr("id", worksheet["worksheetId"]);
					tableDiv.append(table);
					mainDiv.append(tableDiv);
					
					// Add the row options
					var pagerOptionsDiv = $("<div>").addClass("topLevelpagerOptions pager ui-corner-bottom")
										.attr("id","topLevelpagerOptions" + worksheet["worksheetId"])
										.append($("<div>").addClass("rowCountDiv")
												.append($("<span>")
														.text("Show: "))
												.append($("<a>").addClass("pagerResizeLink")
														//.attr("id", "pagerResizeLink10"+ worksheet["worksheetId"])
														.addClass("pagerSizeSelected")
														.data("rowCount",10)
														.data("vWorksheetId", worksheet["worksheetId"])
														.attr("href", "JavaScript:void(0);")
														.text("10 ")
														.bind("click", handlePagerResize)
														)
												.append($("<a>").addClass("pagerResizeLink")
														//.attr("id", "pagerResizeLink20"+ worksheet["worksheetId"])
														.data("rowCount",20)
														.data("vWorksheetId", worksheet["worksheetId"])
														.attr("href", "JavaScript:void(0);")
														.text("20 ")
														.bind("click", handlePagerResize)
														)
												.append($("<a>").addClass("pagerResizeLink")
														//.attr("id", "pagerResizeLink50"+ worksheet["worksheetId"])
														.data("rowCount",50)
														.data("vWorksheetId", worksheet["worksheetId"])
														.attr("href", "JavaScript:void(0);")
														.text("50 ")
														.bind("click", handlePagerResize)
														)
												.append($("<span>")
														.text(" records"))																
												)
										.append($("<div>").addClass("prevNextRowsDiv")
												.append($("<a>").attr("id", "previousLink" + worksheet["worksheetId"]) 
														.attr("href", "JavaScript:void(0);")
														.data("direction", "showPrevious")
														.data("vWorksheetId", worksheet["worksheetId"])
														.addClass("inactiveLink")
														.text("Previous ")
														.bind("click", handlePrevNextLink)	
														)
												.append($("<span>").attr("id","previousNextText" + worksheet["worksheetId"])
															.addClass("previousNextText")
												)
												.append($("<a>").attr("id", "nextLink" + worksheet["worksheetId"]) 
														.attr("href", "JavaScript:void(0);#")
														.data("direction", "showNext")
														.data("vWorksheetId", worksheet["worksheetId"])
														.addClass("inactiveLink")
														.text("  Next")
														.bind("click", handlePrevNextLink)	
												)
										);
					mainDiv.append(pagerOptionsDiv);
					
					$("#tablesWorkspace").append(mainDiv).append("<br>");
				} else {
					
				}
			});
		}
		
		/* Update the worksheet column headers */
		if(element["updateType"] == "WorksheetHeadersUpdate") {
			
			var table = $("table#" + element["worksheetId"]);
			// Check if the table has column header row. if not, then create one.
			var theadRow = $("thead tr", table);
			if(theadRow.length == 0) {
				var thead = $("<thead>").addClass("tableHeader");
				theadRow = $("<tr>").addClass("tableHeaderRow");
				thead.append(theadRow);
				table.append(thead);			
			}
			
			// Loop for the headers
			$.each(element["columns"], function(j, column){ 
				// Check if the column header with same Id exists
				if($("#" + column["path"], table).length == 0) {
					theadRow.append(
						$("<td>").append(
							$("<div>").addClass("tableHeaderDiv")
									.append($("<span>").text(column["columnNameFull"])
									)
							)
							.mouseenter(config)
							.mouseleave(configOut)
					);
				}
				
			});
		}
		
		/* Update the worksheet data */
		if(element["updateType"] == "WorksheetDataUpdate") {
			var table = $("table#" + element["worksheetId"]);
			// Check if the table has tbody for data rows. if not, then create one.
			var tbody = $("tbody", table);
			if(tbody.length == 0) {
				tbody = $("<tbody>");
				table.append(tbody);			
			}
			// Mark the rows that need to be deleted later
			if($("tr", tbody).length != 0) {
				$("tr", tbody).addClass("deleteMe");
			}
			
			// Add the rows
			$.each(element["rows"], function(j, row) {
				var rowTag = $("<tr>");
				// Adding each cell
				$.each(row["cells"], function(k, cell) {
					if (!cell["isDummy"]) {
						var tdTag = $("<td>").addClass("noLineBelow")
									.addClass(cell["tableCssTag"])
									.addClass("editable")
									//.text(cell["value"])
									.append($("<div>").addClass("cellValue")
										//.text(cell["value"])
										.mouseenter(showTableCellMenu)
										.mouseleave(hideTableCellMenu)
									)
									.attr('id', cell["nodeId"])
									.attr('path', cell["path"])
									.data('jsonElement', cell)
									.hover(showConsoleInfo)
									;			
						// Mark the edited cells			
						if(cell["status"] == "E")
							$(tdTag).children("div.cellValue").addClass("editedValue")
							
						if(cell["value"].length > 20) {
							var valueToShow = cell["value"].substring(0,20);
							$(tdTag).children("div.cellValue").text(valueToShow + "...");
							$(tdTag).data("fullValue", cell["value"]);
							$(tdTag).addClass("expandValueCell");
						} else {
							$(tdTag).children("div.cellValue").text(cell["value"]);
						}
							
						// Check if the cell has pager associated with it
						if(cell["pager"]) {
							$(tdTag).append(
								$("<div>").addClass("nestedTableLastRow")
										.append($("<img>").attr("src","../images/pagerBar.png"))
										.mouseenter(showNestedTablePager)
										.mouseleave(hideNestedTablePager)
										.data("pagerElem", cell["pager"])
							)
						
							// Check if the nested table pager has been created already for the existing worksheet.
							// We maintain one nested table pager for each worksheet
							if($("#nestedTablePager" + element["worksheetId"]).length == 0){
								// Create a nested table pager by cloning the pager object present for the whole table
								var nestedTablePager = $("div#topLevelpagerOptions" + element["worksheetId"]).clone(true, true)
														.addClass("ui-corner-all").removeClass("topLevelpagerOptions");
								nestedTablePager.addClass("nestedTablePager pager")
														.attr("id", "nestedTablePager" + element["worksheetId"])
														.mouseenter(function() {
															$(this).show();
														})
														.mouseleave(function(){
															$(this).hide();
														});
								$($("a", nestedTablePager)[3]).data("vWorksheetId", element["worksheetId"]);
								$($("a", nestedTablePager)[4]).data("vWorksheetId", element["worksheetId"]);												
								// Change the row count values to 5, 10, 20
								$($("a.pagerResizeLink", nestedTablePager)[0]).text("5 ")
											.data("rowCount", 5).data("vWorksheetId", element["worksheetId"]);
								$($("a.pagerResizeLink", nestedTablePager)[1]).text("10 ")
											.data("rowCount", 10).data("vWorksheetId", element["worksheetId"]);
								$($("a.pagerResizeLink", nestedTablePager)[2]).text("20 ")
											.data("rowCount", 20).data("vWorksheetId", element["worksheetId"]);
														
								//table.append(nestedTablePager);
								$("body").append(nestedTablePager);
								nestedTablePager.hide();
							} else {
								changePagerOptions(cell["pager"], $("#nestedTablePager" + element["worksheetId"]));
							}
						}
						
						rowTag.append(tdTag);
					} else if (cell["isDummy"]) {
						rowTag.append(
							$("<td>").addClass("noLineAboveAndBelow")
									.addClass(cell["tableCssTag"])
						);
					} else {
					};
				});
				tbody.append(rowTag);
			});
			
			// Delete the old rows
			$("tr.deleteMe", tbody).remove();
			
			/* Update the pager information */
			changePagerOptions(element["pager"], $("div#topLevelpagerOptions" + element["worksheetId"]));
		}
		
		/* Update the commands list */
		if(element["updateType"] == "HistoryAddCommandUpdate") {
			var commandDiv = $("<div>")
							.addClass("CommandDiv undo-state " + element.command.commandType)
							.attr("id", element.command.commandId)
							.css({"position":"relative"})
							.append($("<div>")
								.text(element.command.title + ": " + element.command.description)
								)
							.append($("<div>")
									.addClass("iconDiv")
									.append($("<img>")
											.attr("src", "../images/edit_undo.png")
									)
									.bind('click', clickUndoButton)
								)
							.hover(
								// hover in function
								commandDivHoverIn,
							    // hover out function
								commandDivHoverOut);
			if(element.command["commandType"] == "notUndoable")
				$("div.iconDiv",commandDiv).remove();
			var commandHistoryDiv = $("div#commandHistory");
			// Remove the commands on redo stack
			$(".redo-state").remove();
			
			commandHistoryDiv.append(commandDiv);
		}
		
		if(element["updateType"] == "HistoryUpdate") {
			$("div#commandHistory div.CommandDiv").remove();
			$.each(element["commands"], function(index, command){
				var commandDiv = $("<div>")
							.addClass("CommandDiv " + command.commandType)
							.attr("id", command.commandId)
							.css({"position":"relative"})
							.append($("<div>")
								.text(command.title + ": " + command.description)
								)
							.append($("<div>")
									.addClass("iconDiv")
									.bind('click', clickUndoButton)
								)
							.hover(
								// hover in function
								commandDivHoverIn,
							    // hover out function
								commandDivHoverOut);
				if(command["commandType"] == "notUndoable")
					$("div.iconDiv",commandDiv).remove();
								
				if(command.historyType == "redo") {
					$(commandDiv).addClass("redo-state");
					$("div.iconDiv", commandDiv).append($("<img>")
											.attr("src", "../images/edit_redo.png"));
				} else {
					$(commandDiv).addClass("undo-state");
					$("div.iconDiv", commandDiv).append($("<img>")
											.attr("src", "../images/edit_undo.png"));
				}				
				$("div#commandHistory").append(commandDiv);
			});
		}
		
		/* Update the cell value */
		if(element["updateType"] == "NodeChangedUpdate") {
			var tdTag = $("td#" + element.nodeId); 
			if(element.newValue.length > 20) {
				var valueToShow = element.newValue.substring(0,20);
				$(tdTag).children("div.cellValue").text(valueToShow + "...");
				$(tdTag).data("fullValue", element.newValue);
				$(tdTag).addClass("expandValueCell");
			} else {
				if($(tdTag).hasClass("expandValueCell")){
					$(tdTag).removeClass("expandValueCell");
					$.removeData($(tdTag), "fullValue");
				}
				$(tdTag).children("div.cellValue").text(element.newValue);
			}
			
			//tdTag.children("div.cellValue").text(element.newValue);
			if(element.newStatus == "E"){
				tdTag.children("div.cellValue").addClass("editedValue");
			}
			else {
				tdTag.children("div.cellValue").removeClass("editedValue");
			}
		}
		
		if(element["updateType"] == "NewImportDatabaseTableCommandUpdate") {
			$("#DatabaseImportDiv").data("commandId", element["commandId"]);
		}
	});
}

function showConsoleInfo() {
	// if (console && console.log) {
		// console.clear();
		// var elem = $(this).data("jsonElement");
		// $.each(elem, function(key, value){
			// if(key == "pager"){
				// console.log("Pager Information:")
				// $.each(value, function(key2,value2){
					// console.log(key2 +" : " + value2)
				// })
				// console.log("Pager Information Finished.")
			// }
			// else
				// console.log(key + " : " + value);
		// })
	// }
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

function showTableCellMenu() {
	// Get the parent table
	$("div#tableCellToolBarMenu").data("parentCellId", $(this).parents("td").attr("id"));
	if($(this).parents("td").hasClass("expandValueCell")){
		$("#viewValueButton").show();
		$("#tableCellMenutriangle").css({"margin-left" : "32px"});
		$("div#tableCellToolBarMenu").css({"width": "105px"});
	} else {
		$("#viewValueButton").hide();
		$("#tableCellMenutriangle").css({"margin-left" : "10px"});
		$("div#tableCellToolBarMenu").css({"width": "48px"});
	}
	$("div#tableCellToolBarMenu").css({"position":"absolute",
    					"top":$(this).offset().top + 10, 
    					"left": $(this).offset().left + $(this).width()/2 - $("div#tableCellToolBarMenu").width()/2}).show();
}

function hideTableCellMenu() {
	$("div#tableCellToolBarMenu").hide();
}

function config(event) {
	$("#toolBarMenu").data("parent", $(this));
    $("#toolBarMenu").css({"position":"absolute","width":"165px",
    					"top":$(this).offset().top + $(this).height(), 
    					//"left":event.clientX-150	,
    					"left": $(this).offset().left + $(this).width()/2 - $("#toolBarMenu").width()/2}).show();
    					//"top": event.clientY-10}).show();    
};

function configOut() {    
	$("#toolBarMenu").hide();    
};
