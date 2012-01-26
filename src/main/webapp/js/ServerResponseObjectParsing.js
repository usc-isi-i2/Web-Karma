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
					var mainDiv = $("<div>")
									.attr("id", worksheet["worksheetId"])
									.addClass("Worksheet");
					mainDiv.data("isCollapsed", worksheet["isCollapsed"]);
					
					// Div for adding title of that worksheet
					var titleDiv = $("<div>")
									.addClass("WorksheetTitleDiv ui-corner-top");
					titleDiv.append($("<div>")
								.text(worksheet["title"])
								.addClass("tableTitleTextDiv")
								)
							.append($("<div>")
								.addClass("WorksheetOptionsButtonDiv")
								.attr("id", "optionsButton" + worksheet["worksheetId"])
								.data("worksheetId", worksheet["worksheetId"])
								.click(openWorksheetOptions)
								.button({
									icons: {
										primary: 'ui-icon-triangle-1-s'
								    },
									text: false
									}).mouseleave(function(){
										$("#WorksheetOptionsDiv").hide();
									})
								)
							.append($("<div>").addClass("rightOptionsToolbar")
								.append($("<div>").addClass("toggleMapView")
									.append($("<img>")
										.attr("src","../images/google-earth-32.png"))
									.qtip({
										   content: {
										      text: 'View on map'
										   },
										   position: {
										      my: 'top center',  // Position my top left...
										      at: 'bottom center', // at the bottom right of...
										   },
										   style: {
										      classes: 'ui-tooltip-light ui-tooltip-shadow'
										   }
										})
										.data("worksheetId", worksheet["worksheetId"])
										.data("state", "table")
										.click(showMapViewForWorksheet)
									)
									.append($("<div>")
										.addClass("showHideWorkSheet")
										.attr("id", "hideShow"+worksheet["worksheetId"])
										.click(function() {
											$("#" + worksheet["worksheetId"] + "TableDiv").toggle(400);
											$("#topLevelpagerOptions" + worksheet["worksheetId"]).toggle(400);
											// Change the corners
											titleDiv.toggleClass("ui-corner-top");
											titleDiv.toggleClass("ui-corner-all");
											
											// Change the icon
											if($(this).data("state") == "open") {
												$(this).data("state", "close");
												$(this).button({
												    icons: {
												        primary: 'ui-icon-plusthick'
												    },
												    text: false
												});
											} else if ($(this).data("state") == "close") {
												$(this).data("state", "open");
												$(this).button({
												    icons: {
												        primary: 'ui-icon-minusthick'
												    },
												    text: false
												});
											}
										}).button({
										    icons: {
										        primary: 'ui-icon-minusthick'
										    },
										    text: false
										}).data("state", "open")
									)
								);
					mainDiv.append(titleDiv);
					
					// Add the table (if it does not exists)
					var tableDiv = $("<div>").attr("id", worksheet["worksheetId"] + "TableDiv").addClass("TableDiv");
					var table = $("<table>").attr("id", worksheet["worksheetId"]).addClass("WorksheetTable");
					tableDiv.append(table);
					mainDiv.append(tableDiv);
					
					// Add the row options
					var pagerOptionsDiv = 
						$("<div>").addClass("topLevelpagerOptions pager ui-corner-bottom")
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
									.bind("click", handlePagerResize))
								.append($("<a>").addClass("pagerResizeLink")
									//.attr("id", "pagerResizeLink20"+ worksheet["worksheetId"])
									.data("rowCount",20)
									.data("vWorksheetId", worksheet["worksheetId"])
									.attr("href", "JavaScript:void(0);")
									.text("20 ")
									.bind("click", handlePagerResize))
								.append($("<a>").addClass("pagerResizeLink")
									//.attr("id", "pagerResizeLink50"+ worksheet["worksheetId"])
									.data("rowCount",50)
									.data("vWorksheetId", worksheet["worksheetId"])
									.attr("href", "JavaScript:void(0);")
									.text("50 ")
									.bind("click", handlePagerResize))
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
										.bind("click", handlePrevNextLink))
								.append($("<span>").attr("id","previousNextText" + worksheet["worksheetId"])
									.addClass("previousNextText"))
								.append($("<a>").attr("id", "nextLink" + worksheet["worksheetId"]) 
									.attr("href", "JavaScript:void(0);#")
									.data("direction", "showNext")
									.data("vWorksheetId", worksheet["worksheetId"])
									.addClass("inactiveLink")
									.text("  Next")
									.bind("click", handlePrevNextLink))
							);
					mainDiv.append(pagerOptionsDiv);
					
					$("#tablesWorkspace").append(mainDiv).append("<br>");
				} else {
					
				}
			});
		}
		
		else if(element["updateType"] == "WorksheetHierarchicalHeadersUpdate") {
			var table = $("table#" + element["worksheetId"]);
			
			var thead = $("thead", table);
			
			if(thead.length == 0) {
				thead = $("<thead>").addClass("tableHeader");
				table.append(thead);			
			}
			$("tr.ColumnHeaders", thead).remove();
			
			$.each(element["rows"], function(index, row) {
				var trTag = $("<tr>").addClass("ColumnHeaders");
				$.each(row["cells"], function(index2, cell){
					var tdTag = $("<td>");
					
					// Add the background information
					tdTag.addClass("fill" + cell["fillId"]);
						
					// Add the left border
					tdTag.addClass("leftBorder" + cell["leftBorder"].replace(":", ""));
					
					// Add the right border
					tdTag.addClass("rightBorder" + cell["rightBorder"].replace(":", ""));
					
					// Add the top border
					tdTag.addClass("topBorder" + cell["topBorder"].replace(":", ""));	
					
					if(cell["cellType"] == "border") {
						tdTag.addClass("bordertdTags")
						
					}
					else if (cell["cellType"] == "heading") {
						tdTag.addClass("columnHeadingCell")
						// Add the colspan
						tdTag.attr("colspan", cell["colSpan"]);
						
						// Store the node ID
						tdTag.attr("id", cell["contentCell"]["id"]);
						
						//Add the name
						tdTag.append($("<div>").addClass("ColumnHeadingNameDiv")
							.text(cell["contentCell"]["label"])
							//.mouseenter(config)
							//.mouseleave(configOut)
						);
						
						// tdTag.text(cell["columnNameFull"])
							// .mouseenter(config)
							// .mouseleave(configOut);
					} else if(cell["cellType"] == "headingPadding") {
						// Add the colspan
						tdTag.attr("colspan", cell["colSpan"]);
					}
					tdTag.data("jsonElement", cell).hover(showConsoleInfo);
					
					trTag.append(tdTag);
				});
				thead.append(trTag);
			});
		}
		
		else if(element["updateType"] == "AlignmentHeadersUpdate") {
			var table = $("table#" + element["worksheetId"]);
			table.data("alignmentId", element["alignmentId"]);
			
			var thead = $("thead", table);
			$("tr", thead).remove();
			var columnHeaders = $("tr", thead).clone(true);
			$("tr", thead).remove();
			
			$.each(element["rows"], function(index, row) {
				var trTag = $("<tr>").addClass("AlignmentRow");
				$.each(row["cells"], function(index2, cell){
					var tdTag = $("<td>");
					
					// Add the background information
					tdTag.addClass("fill" + cell["fillId"]);
						
					// Add the left border
					tdTag.addClass("leftBorder" + cell["leftBorder"].replace(":", ""));
					
					// Add the right border
					tdTag.addClass("rightBorder" + cell["rightBorder"].replace(":", ""));
					
					// Add the top border
					tdTag.addClass("topBorder" + cell["topBorder"].replace(":", ""));	
					
					if(cell["cellType"] == "border") {
						tdTag.addClass("bordertdTags")
						
					}
					else if (cell["cellType"] == "heading") {
						tdTag.addClass("columnHeadingCell")
						// Add the colspan
						tdTag.attr("colspan", cell["colSpan"]);
						
						// Store the node ID
						//tdTag.attr("id", cell["hNodeId"]);
						
						// Add the label
						var labelDiv = $("<div>").addClass("AlignmentHeadingNameDiv")
							.text(cell["contentCell"]["label"]);
						
						// Add the pencil
						if(cell["contentCell"]["parentLinkId"] != null) {
							// Special case for the key attribute which has the link and node named BlankNode
							if(cell["contentCell"]["parentLinkLabel"] == "BlankNode") {
								tdTag.append($("<span>").text("key").addClass("KeyAtrributeLabel"));
							} else {
								var pencilDiv = $("<div>").addClass("AlignmentLinkConfigDiv")
									.append($("<img>").attr("src","../images/configure-icon.png"))
									.append($("<span>").text(cell["contentCell"]["parentLinkLabel"]))
									.click(showAlternativeParents);
								
								tdTag.append(pencilDiv);
							
								// Special case for data properties
								if(cell["contentCell"]["parentLinkLabel"] != cell["contentCell"]["label"])
									tdTag.append(labelDiv);
								}
						} else {
							labelDiv.prepend($("<img>").attr("src","../images/configure-icon.png")).click(showAlternativeParents);
							tdTag.append(labelDiv);
						}
						
						
						// tdTag.text(cell["columnNameFull"])
							// .mouseenter(config)
							// .mouseleave(configOut);
					} else if(cell["cellType"] == "headingPadding") {
						// Add the colspan
						tdTag.attr("colspan", cell["colSpan"]);
					}
					tdTag.data("jsonElement", cell).hover(showConsoleInfo);
					
					trTag.append(tdTag);
				});
				thead.append(trTag);
			});
			thead.append(columnHeaders);
		}
		
		else if(element["updateType"] == "WorksheetHierarchicalDataUpdate") {
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
			
			$.each(element["rows"], function(index, row) {
				var trTag = $("<tr>");
				trTag.addClass(row["rowType"]);
				$.each(row["rowCells"], function(index2, cell){
					var tdTag = $("<td>");
					
					// Split the attr attribute of the row cell
					var attr = cell["attr"];
					var attrVals = attr.split(":");
					var cssClass = attrVals[2];
					tdTag.addClass("data"+cssClass);
					
					// Populate the td with value if the cell is of content type
					if(attrVals[0] == "c") {
						if(cell["value"] == null)
							console.log("Value not found in a content cell!");
						if(cell["value"] != null){
							var valueToShow = cell["value"];
								
							tdTag.append($("<span>").addClass("cellValue")
										.text(valueToShow))
										//.mouseenter(showTableCellMenu)
										//.mouseleave(hideTableCellMenu))
									.attr('id', cell["nodeId"]);
						}
							
					}
					
					tdTag.addClass(attrVals[0]);
					
					// Add the left border
					if(attrVals[3] != "_") {
						if(attrVals[3] == "o") {
							tdTag.addClass("leftBorderouter" + cssClass);
						} else if (attrVals[3] == "i") {
							tdTag.addClass("leftBorderinner" + cssClass);
						} else {
							console.log("Unknown border type detected!");
						}
					}
					
					// Add the right border
					if(attrVals[4] != "_") {
						if(attrVals[4] == "o") {
							tdTag.addClass("rightBorderouter" + cssClass);
						} else if (attrVals[4] == "i") {
							tdTag.addClass("rightBorderinner" + cssClass);
						} else {
							console.log("Unknown border type detected!");
						}
					}
					
					// Add the top border
					if(attrVals[5] != "_") {
						if(attrVals[5] == "o") {
							tdTag.addClass("topBorderouter" + cssClass);
						} else if (attrVals[5] == "i") {
							tdTag.addClass("topBorderinner" + cssClass);
						} else {
							console.log("Unknown border type detected!");
						}
					}
					
					// Add the bottom border
					if(attrVals[6] != "_") {
						if(attrVals[6] == "o") {
							tdTag.addClass("bottomBorderouter" + cssClass);
						} else if (attrVals[6] == "i") {
							tdTag.addClass("bottomBorderinner" + cssClass);
						} else {
							console.log("Unknown border type detected!");
						}
					}
					
					tdTag.data("jsonElement", cell).hover(showConsoleInfo);
					
					trTag.append(tdTag);
				});
				table.append(trTag);
			});
			
			// Delete the old rows
			$("tr.deleteMe", tbody).remove();
			
			// Bottom anchor for scrolling page
			if($("div#" + element["worksheetId"] + "bottomAnchor").length == 0)
				$("div#" + element["worksheetId"]).append($("<div>").attr("id", element["worksheetId"] + "bottomAnchor"));
		}
		
		/* Update the worksheet data */
				// if(element["updateType"] == "WorksheetDataUpdate") {
			// var table = $("table#" + element["worksheetId"]);
			// // Check if the table has tbody for data rows. if not, then create one.
			// var tbody = $("tbody", table);
			// if(tbody.length == 0) {
				// tbody = $("<tbody>");
				// table.append(tbody);			
			// }
			// // Mark the rows that need to be deleted later
			// if($("tr", tbody).length != 0) {
				// $("tr", tbody).addClass("deleteMe");
			// }
// 			
			// // Add the rows
			// $.each(element["rows"], function(j, row) {
				// var rowTag = $("<tr>");
				// // Adding each cell
				// $.each(row["cells"], function(k, cell) {
					// if (!cell["isDummy"]) {
						// var tdTag = $("<td>").addClass("noLineBelow")
									// .addClass(cell["tableCssTag"])
									// .addClass("editable")
									// //.text(cell["value"])
									// .append($("<span>").addClass("cellValue")
										// //.text(cell["value"])
										// .mouseenter(showTableCellMenu)
										// .mouseleave(hideTableCellMenu)
									// )
									// .attr('id', cell["nodeId"])
									// .attr('path', cell["path"])
									// .data('jsonElement', cell)
									// .hover(showConsoleInfo)
									// ;			
						// // Mark the edited cells			
						// if(cell["status"] == "E")
							// $(tdTag).children("span.cellValue").addClass("editedValue")
// 							
						// if(cell["value"].length > 20) {
							// var valueToShow = cell["value"].substring(0,20);
							// $(tdTag).children("span.cellValue").text(valueToShow + "...");
							// $(tdTag).data("fullValue", cell["value"]);
							// $(tdTag).addClass("expandValueCell");
						// } else {
							// $(tdTag).children("span.cellValue").text(cell["value"]);
						// }
// 							
						// // Check if the cell has pager associated with it
						// if(cell["pager"]) {
							// $(tdTag).append(
								// $("<div>").addClass("nestedTableLastRow")
										// .append($("<img>").attr("src","../images/pagerBar.png"))
										// .mouseenter(showNestedTablePager)
										// .mouseleave(hideNestedTablePager)
										// .data("pagerElem", cell["pager"])
							// )
// 						
							// // Check if the nested table pager has been created already for the existing worksheet.
							// // We maintain one nested table pager for each worksheet
							// if($("#nestedTablePager" + element["worksheetId"]).length == 0){
								// // Create a nested table pager by cloning the pager object present for the whole table
								// var nestedTablePager = $("div#topLevelpagerOptions" + element["worksheetId"]).clone(true, true)
														// .addClass("ui-corner-all").removeClass("topLevelpagerOptions");
								// nestedTablePager.addClass("nestedTablePager pager")
														// .attr("id", "nestedTablePager" + element["worksheetId"])
														// .mouseenter(function() {
															// $(this).show();
														// })
														// .mouseleave(function(){
															// $(this).hide();
														// });
								// $($("a", nestedTablePager)[3]).data("vWorksheetId", element["worksheetId"]);
								// $($("a", nestedTablePager)[4]).data("vWorksheetId", element["worksheetId"]);												
								// // Change the row count values to 5, 10, 20
								// $($("a.pagerResizeLink", nestedTablePager)[0]).text("5 ")
											// .data("rowCount", 5).data("vWorksheetId", element["worksheetId"]);
								// $($("a.pagerResizeLink", nestedTablePager)[1]).text("10 ")
											// .data("rowCount", 10).data("vWorksheetId", element["worksheetId"]);
								// $($("a.pagerResizeLink", nestedTablePager)[2]).text("20 ")
											// .data("rowCount", 20).data("vWorksheetId", element["worksheetId"]);
// 														
								// //table.append(nestedTablePager);
								// $("body").append(nestedTablePager);
								// nestedTablePager.hide();
							// } else {
								// changePagerOptions(cell["pager"], $("#nestedTablePager" + element["worksheetId"]));
							// }
						// }
// 						
						// rowTag.append(tdTag);
					// } else if (cell["isDummy"]) {
						// rowTag.append(
							// $("<td>").addClass("noLineAboveAndBelow")
									// .addClass(cell["tableCssTag"])
						// );
					// } else {
					// };
				// });
				// tbody.append(rowTag);
			// });
// 			
			// // Delete the old rows
			// $("tr.deleteMe", tbody).remove();
// 			
			// /* Update the pager information */
			// changePagerOptions(element["pager"], $("div#topLevelpagerOptions" + element["worksheetId"]));
		// }
// 		
		/* Update the commands list */
		else if(element["updateType"] == "HistoryAddCommandUpdate") {
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
									.qtip({
									   content: {
									      text: 'Undo'
									   },
									   style: {
									      classes: 'ui-tooltip-light ui-tooltip-shadow'
									   }
									})
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
		
		else if(element["updateType"] == "HistoryUpdate") {
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
					$("div.iconDiv", commandDiv)
						.append($("<img>")
						.attr("src", "../images/edit_redo.png"))
						.qtip({
						   content: {
						      text: 'Redo'
						   },
						   style: {
						      classes: 'ui-tooltip-light ui-tooltip-shadow'
						   }
						});
				} else {
					$(commandDiv).addClass("undo-state");
					$("div.iconDiv", commandDiv)
						.append($("<img>")
						.attr("src", "../images/edit_undo.png"))
						.qtip({
						   content: {
						      text: 'Undo'
						   },
						   style: {
						      classes: 'ui-tooltip-light ui-tooltip-shadow'
						   }
						});;
				}				
				$("div#commandHistory").append(commandDiv);
			});
		}
		
		/* Update the cell value */
		else if(element["updateType"] == "NodeChangedUpdate") {
			var tdTag = $("td#" + element.nodeId); 
			if(element.newValue.length > 20) {
				var valueToShow = element.newValue.substring(0,20);
				$(tdTag).children("span.cellValue").text(valueToShow + "...");
				$(tdTag).data("fullValue", element.newValue);
				$(tdTag).addClass("expandValueCell");
			} else {
				if($(tdTag).hasClass("expandValueCell")){
					$(tdTag).removeClass("expandValueCell");
					$.removeData($(tdTag), "fullValue");
				}
				$(tdTag).children("span.cellValue").text(element.newValue);
			}
			
			if(element.newStatus == "E"){
				tdTag.children("span.cellValue").addClass("editedValue");
			}
			else {
				tdTag.children("span.cellValue").removeClass("editedValue");
			}
		}
		
		else if(element["updateType"] == "NewImportDatabaseTableCommandUpdate") {
			$("#DatabaseImportDiv").data("commandId", element["commandId"]);
		}
		
		else if(element["updateType"] == "SemanticTypesUpdate") {
			var table = $("table#" + element["worksheetId"]);
			$.each(element["Types"], function(index, type) {
				var tdTag = $("td.columnHeadingCell#" + type["HNodeId"], table);
				// Remove any existing semantic type div
				$("br", tdTag).remove();
				$("div.semanticTypeDiv", tdTag).remove();
				
				var semDiv = $("<div>").addClass("semanticTypeDiv " + 
						type["ConfidenceLevel"]+"ConfidenceLevel");
				
				if(type["FullType"] == ""){
					semDiv.text("Unassigned").addClass("LowConfidenceLevel")
						.data("hNodeId", type["HNodeId"])
						.data("fullType", "Unassigned");
					if(type["FullCRFModel"] != null)
						semDiv.data("crfInfo",type["FullCRFModel"]);		
				} else if (type["ConfidenceLevel"] == "Low") {
					semDiv.text("Unassigned").addClass("LowConfidenceLevel")
						.data("hNodeId", type["HNodeId"])
						.data("fullType", "Unassigned")
						.data("crfInfo",type["FullCRFModel"]);
				} else {
					if(type["Domain"] != null && type["Domain"] != ""){
						var typeItalicSpan = $("<span>").addClass("italic").text(type["DisplayLabel"]);
						// semDiv.text(type["DisplayDomainLabel"] + ":" + type["DisplayLabel"]);
						semDiv.text(type["DisplayDomainLabel"] + ":").append(typeItalicSpan);
					}
					else
						semDiv.text(type["DisplayLabel"]);
					semDiv.data("crfInfo",type["FullCRFModel"])
						.data("hNodeId", type["HNodeId"])
						.data("fullType", type["FullType"])
						.data("domain", type["Domain"])
						.data("origin", type["Origin"]);	
				}
					
				//semDiv.hover(showSemanticTypeInfo, hideSemanticTypeInfo);
				semDiv.click(changeSemanticType);
				tdTag.append(semDiv);
			});
		}
		
		else if(element["updateType"] == "ImportOntologyCommand") {
			if(!element["Import"])
				alert("Ontology import failed!");
		} 
		
		else if(element["updateType"] == "TagsUpdate") {
			$.each(element["Tags"], function(index, tag) {
				$.each(tag["Nodes"], function(index2, node){
					var tdTag = $("td#"+node);
					if(tdTag.length != 0) {
						var tagSpanBox = $("<span>")
							.css({backgroundColor:tag["Color"]})
							.addClass("tag")
							.qtip({
							   content: {
							      text: tag["Label"]
							   },
							   style: {
							      classes: 'ui-tooltip-light ui-tooltip-shadow'
							   }
							});
						tdTag.append(tagSpanBox);
						$(tagSpanBox).show();
					}
				});
			});
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

function showConsoleInfo() {
	if (console && console.log) {
		console.clear();
		var elem = $(this).data("jsonElement");
		$.each(elem, function(key, value){
			if(key == "pager"){
				console.log("Pager Information:")
				$.each(value, function(key2,value2){
					console.log(key2 +" : " + value2)
				})
				console.log("Pager Information Finished.")
			}
			else
				console.log(key + " : " + value);
		})
	}
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
