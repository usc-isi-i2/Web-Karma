/*******************************************************************************
 * Copyright 2012 University of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This code was developed by the Information Integration Group as part
 * of the Karma project at the Information Sciences Institute of the
 * University of Southern California.  For more information, publications,
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

function parse(data) {

    $.workspaceGlobalInformation = {
        "id" : data["workspaceId"]
    }

    var isError = false;
    var error = [];
    var infos = [];
    var trivialErrors = [];
    
    // Check for errors
    $.each(data["elements"], function(i, element) {
    	
        if(element["updateType"] == "KarmaError") {
        	if(error[element["Error"]]) {
        		//ignore;
        	} else {
	            $.sticky("<span class='karmaError'>" + element["Error"] + "</span>");
	            isError = true;
	            error[element["Error"]] = true;
        	}
        }
    });

    if(isError)
    	return false;
    
    /* Always add the charts from cleaning service in end, so pushing that CleaningServiceUpdate in the end of updates array (if present) */
    // Identify the index
    var cleaningUpdateIndex = -1;
    $.each(data["elements"], function(i, element) {
        if(element["updateType"] == "WorksheetCleaningUpdate") {
            cleaningUpdateIndex = i;
        }
    });
    // Move it to the end
    if (cleaningUpdateIndex != -1) {
        var cleaningUpdate = data["elements"].splice(cleaningUpdateIndex, 1)[0];
        data["elements"].push(cleaningUpdate);
    }

    // Loop through each update from the server and take required action for the GUI
    $.each(data["elements"], function(i, element) {
        if(element["updateType"] == "WorksheetListUpdate") {

            $.each(element["worksheets"], function(j, worksheet) {
                // If worksheet doesn't exist yet
                if($("div#" + worksheet["worksheetId"]).length == 0) {
                    var mainDiv = $("<div>").attr("id", worksheet["worksheetId"]).addClass("Worksheet");
                    mainDiv.data("isCollapsed", worksheet["isCollapsed"]);

                    // Div for adding title of that worksheet
                    var titleDiv = $("<div>").addClass("WorksheetTitleDiv ui-corner-top").mouseleave(function() {
                        // Hiding the option buttons for the heading and the data cell
                        $("div#tableCellMenuButtonDiv").hide();
                        $("div#columnHeadingMenuButtonDiv").hide();
                    });

                    titleDiv
                        .append($("<div>")
                            .text(worksheet["title"])
                            .addClass("tableTitleTextDiv"))
                        .append($("<div>")
                            .addClass("WorksheetOptionsButtonDiv")
                            .attr("id", "optionsButton" + worksheet["worksheetId"])
                            .data("worksheetId", worksheet["worksheetId"])
                            .click(openWorksheetOptions).button({
                                icons : {
                                    primary : 'ui-icon-triangle-1-s'
                                },
                                text : false
                            })
                            .mouseleave(function() {
                                $("#WorksheetOptionsDiv").hide();
                            }))
                        .append($("<div>")
                            .addClass("rightOptionsToolbar")
                            .append($("<div>")
                                .addClass("toggleMapView")
                                .append($("<img>")
                                    .attr("src", "images/google-earth-32.png"))
                                .qtip({
                                    content : {
                                        text : 'View on map'
                                    },
                                    position : {
                                        my : 'top center', // Position my top left...
                                        at : 'bottom center', // at the bottom right of...
                                    },
                                    style : {
                                        classes : 'ui-tooltip-light ui-tooltip-shadow'
                                    }
                                })
                                .data("worksheetId", worksheet["worksheetId"])
                                .data("state", "table")
                                .click(showMapViewForWorksheet))
                            .append($("<div>")
                                .addClass("showHideWorkSheet")
                                .attr("id", "hideShow" + worksheet["worksheetId"])
                                .click(function() {
                                    $("div.svg-model", mainDiv).toggle(400);
                                    $("div.table-container", mainDiv).toggle(400);
                                    $("div.table-data-container", mainDiv).toggle(400);

                                    // Change the corners
                                    titleDiv.toggleClass("ui-corner-top");
                                    titleDiv.toggleClass("ui-corner-all");

                                    // Change the icon
                                    if($(this).data("state") == "open") {
                                        $(this).data("state", "close");
                                        $(this).button({
                                            icons : {
                                                primary : 'ui-icon-plusthick'
                                            },
                                            text : false
                                        });
                                    } else if($(this).data("state") == "close") {
                                        $(this).data("state", "open");
                                        $(this).button({
                                            icons : {
                                                primary : 'ui-icon-minusthick'
                                            },
                                            text : false
                                        });
                                    }
                                }).button({
                                    icons : {
                                        primary : 'ui-icon-minusthick'
                                    },
                                    text : false
                                }).data("state", "open")
                            )
                        );
                    mainDiv.append(titleDiv);

                    // Add the table (if it does not exists)
                    var tableDiv = $("<div>").attr("id", worksheet["worksheetId"] + "TableDiv").addClass("TableDiv").mouseleave(function() {
                        $("div#tableCellMenuButtonDiv").hide();
                        $("div#columnHeadingMenuButtonDiv").hide();
                    });

                    $("#tablesWorkspace").append(mainDiv).append("<br>");
                } else {

                }
            });
        }
        else if(element["updateType"] == "WorksheetHeadersUpdate") {
            var worksheetPanel = $("div.Worksheet#" + element["worksheetId"]);

            var tableContainer = $("div.table-container", worksheetPanel);
            if (tableContainer.length == 0) {
                tableContainer = $("<div>").addClass("table-container");
                worksheetPanel.append(tableContainer);
            }

            var tableHeaderContainer = $("div.table-header-container", worksheetPanel);
            if (tableHeaderContainer.length == 0) {
                tableHeaderContainer = $("<div>").addClass("table-header-container");
                tableContainer.append(tableHeaderContainer);
            }

            var headersTable = $("table.wk-table", tableHeaderContainer);
            if (headersTable.length == 0) {
                headersTable = $("<table>").addClass("wk-table htable-odd");
                tableHeaderContainer.append(headersTable);
            } else {
                $("tr", headersTable).addClass("deleteMe");
            }

            var colWidths = addColumnHeadersRecurse(element["columns"], headersTable, true);
            var stylesheet = document.styleSheets[0];

            // Remove the previous rows if any
            $("tr.deleteMe", headersTable).remove();

            $.each(colWidths, function(index2, colWidth){
                var selector = "." + colWidth.columnClass;
                var rule = "{width : " +colWidth.width+ "px }";
                if (stylesheet.insertRule) {
                    stylesheet.insertRule(selector + rule, stylesheet.cssRules.length);
                } else if (stylesheet.addRule) {
                    stylesheet.addRule(selector, rule, -1);
                }
            });
        }
        else if(element["updateType"] == "WorksheetDataUpdate") {
            var worksheetPanel = $("div.Worksheet#" + element["worksheetId"]);

            var tableDataContainer = $(worksheetPanel).children("div.table-data-container");
            if (tableDataContainer.length == 0) {
                tableDataContainer = $("<div>").addClass("table-data-container").attr("id", element["tableId"]);
                worksheetPanel.append(tableDataContainer);
            }

            var dataTable = $(tableDataContainer).children("table.wk-table");
            if (dataTable.length == 0) {
                dataTable = $("<table>").addClass("wk-table");
                tableDataContainer.append(dataTable);
            }

            // Check if the table has tbody for data rows. if not, then create one.
            var tBody = $(dataTable).children("tbody");
            if(tBody.length != 0) {
                // Mark the rows that need to be deleted later
                if($(tBody).children("tr").length != 0) {
                    $(tBody).children("tr").addClass("deleteMe");
                }
            }

            addWorksheetDataRecurse(element["rows"], dataTable, true);

            // Delete the old rows
            $(tBody).children("tr.deleteMe").remove();

            var additionalRowsAvail = element["additionalRowsCount"];
            var moreRowsDiv = $("<div>").addClass("load-more");
            if (additionalRowsAvail == 0) {
                // Do nothing
            } else {
                // Remove any existing rows available links
                $("div.load-more", tableDataContainer).remove();
                var moreRowsLink = $("<a>").attr("href","#!").text(additionalRowsAvail + " additional records, load more...")
                    .click(loadAdditionalRowsHandler);
                moreRowsDiv.append(moreRowsLink);
                tableDataContainer.append(moreRowsDiv);
            }
        }
        else if(element["updateType"] == "HistoryAddCommandUpdate") {
            var commandDiv = $("<div>").addClass("CommandDiv undo-state " + element.command.commandType).attr("id", element.command.commandId).css({
                "position" : "relative"
            }).append($("<div>").text(element.command.title + ": " + element.command.description)).append($("<div>").addClass("iconDiv").append($("<img>").attr("src", "images/edit_undo.png")).bind('click', clickUndoButton).qtip({
                    content : {
                        text : 'Undo'
                    },
                    style : {
                        classes : 'ui-tooltip-light ui-tooltip-shadow'
                    }
                })).hover(
                    // hover in function
                    commandDivHoverIn,
                    // hover out function
                    commandDivHoverOut);
            if(element.command["commandType"] == "notUndoable")
                $("div.iconDiv", commandDiv).remove();
            var commandHistoryDiv = $("div#commandHistory");
            // Remove the commands on redo stack
            $(".redo-state").remove();

            commandHistoryDiv.append(commandDiv);
        }
        else if(element["updateType"] == "HistoryUpdate") {
            $("div#commandHistory div.CommandDiv").remove();
            $.each(element["commands"], function(index, command) {
                var commandDiv = $("<div>").addClass("CommandDiv " + command.commandType).attr("id", command.commandId).css({
                    "position" : "relative"
                }).append($("<div>").text(command.title + ": " + command.description)).append($("<div>").addClass("iconDiv").bind('click', clickUndoButton)).hover(
                        // hover in function
                        commandDivHoverIn,
                        // hover out function
                        commandDivHoverOut);
                if(command["commandType"] == "notUndoable")
                    $("div.iconDiv", commandDiv).remove();

                if(command.historyType == "redo") {
                    $(commandDiv).addClass("redo-state");
                    $("div.iconDiv", commandDiv).append($("<img>").attr("src", "images/edit_redo.png")).qtip({
                        content : {
                            text : 'Redo'
                        },
                        style : {
                            classes : 'ui-tooltip-light ui-tooltip-shadow'
                        }
                    });
                } else {
                    $(commandDiv).addClass("undo-state");
                    $("div.iconDiv", commandDiv).append($("<img>").attr("src", "images/edit_undo.png")).qtip({
                        content : {
                            text : 'Undo'
                        },
                        style : {
                            classes : 'ui-tooltip-light ui-tooltip-shadow'
                        }
                    });
                    ;
                }
                $("div#commandHistory").append(commandDiv);
            });
        }
        else if(element["updateType"] == "NodeChangedUpdate") {
            var cellDiv = $("div#" + element.nodeId);
            $(cellDiv).text(element.displayValue);

            $.removeData($(cellDiv), 'expandedValue');

            cellDiv.data("expandedValue", element["expandedValue"]);


            if(element.newStatus == "E") {
                cellDiv.addClass("editedValue");
            } else {
                cellDiv.removeClass("editedValue");
            }

            // Remove any tags
//			$("span.tag", tdTag).remove();
        }
        else if(element["updateType"] == "NewImportDatabaseTableCommandUpdate") {
            $("#DatabaseImportDiv").data("commandId", element["commandId"]);
        }
        else if(element["updateType"] == "SemanticTypesUpdate") {
            var wk = $("div#" + element["worksheetId"]);

            $.each(element["Types"], function(index, type) {
                var tdTag = $("td#" + type["HNodeId"], wk);
                tdTag.data("typesJsonObject", type);
            });
        }
        else if(element["updateType"] == "ImportOntologyCommand") {
            if(!element["Import"])
                $.sticky("Ontology import failed!");
            else
                $.sticky("Ontology successfully imported!");
        }
        else if(element["updateType"] == "TagsUpdate") {
            // Remove all the existing tags
            $("span.tag").remove();
            $.each(element["Tags"], function(index, tag) {
                $.each(tag["Nodes"], function(index2, node) {
                    var cellDiv = $("div#" + node);
                    if(cellDiv.length != 0) {
                        var tagSpanBox = $("<span>").css({
                            backgroundColor : tag["Color"]
                        }).addClass("tag").qtip({
                                content : {
                                    text : tag["Label"]
                                },
                                style : {
                                    classes : 'ui-tooltip-light ui-tooltip-shadow'
                                }
                            });
                        cellDiv.append(tagSpanBox);
                        $(tagSpanBox).show();
                    }
                });
            });
        }
        else if(element["updateType"] == "PublishCSVUpdate") {
            $("a.CSVDownloadLink", titleDiv).remove();
            var titleDiv = $("div#" + element["worksheetId"] + " div.WorksheetTitleDiv");
            // Remove existing link if any
            hideLoading(element["worksheetId"]);
            var downloadLink = $("<a>").attr("href", element["fileUrl"])
                .text("CSV")
                .addClass("CSVDownloadLink  DownloadLink")
                .attr("target", "_blank");
            $("div.tableTitleTextDiv", titleDiv).after(downloadLink);
            $.sticky("CSV file published");
        }
        else if(element["updateType"] == "PublishMDBUpdate") {
            $("a.MDBDownloadLink", titleDiv).remove();
            var titleDiv = $("div#" + element["worksheetId"] + " div.WorksheetTitleDiv");
            // Remove existing link if any
            hideLoading(element["worksheetId"]);
            var downloadLink = $("<a>")
                .attr("href", element["fileUrl"])
                .text("ACCESS MDB")
                .addClass("MDBDownloadLink  DownloadLink")
                .attr("target", "_blank");
            $("div.tableTitleTextDiv", titleDiv).after(downloadLink);
            $.sticky("MDB file published");
        }
        else if(element["updateType"] == "PublishR2RMLUpdate") {
            // Remove existing link if any
            $("a.R2RMLDownloadLink", titleDiv).remove();

            var titleDiv = $("div#" + element["worksheetId"] + " div.WorksheetTitleDiv");
            hideLoading(element["worksheetId"]);
            var downloadLink = $("<a>").attr("href", element["fileUrl"]).text("R2RML Model").addClass("R2RMLDownloadLink  DownloadLink").attr("target", "_blank");
            $("div.tableTitleTextDiv", titleDiv).after(downloadLink);
            $.sticky("R2RML Model published");
        }
        else if(element["updateType"] == "PublishSpatialDataUpdate") {
            $("a.SpatialDataDownloadLink", titleDiv).remove();
            var titleDiv = $("div#" + element["worksheetId"] + " div.WorksheetTitleDiv");
            // Remove existing link if any
            hideLoading(element["worksheetId"]);
            var downloadLink = $("<a>").attr("href", element["fileUrl"]).text("SPATIAL DATA").addClass("SpatialDataDownloadLink  DownloadLink").attr("target", "_blank");
            $("div.tableTitleTextDiv", titleDiv).after(downloadLink);
            $.sticky("Spatial data published");
        }
        else if(element["updateType"] == "PublishRDFUpdate") {
            var titleDiv = $("div#" + element["worksheetId"] + " div.WorksheetTitleDiv");
            // Remove existing link if any
            $("a.RdfDownloadLink", titleDiv).remove();

            var downloadLink = $("<a>").attr("href", element["fileUrl"]).text("RDF").addClass("RdfDownloadLink  DownloadLink").attr("target", "_blank");
            $("div.tableTitleTextDiv", titleDiv).after(downloadLink);

            var errorArr = $.parseJSON(element["errorReport"]);
            if (errorArr && errorArr.length !=0) {
                var errorWindow = $("#rdfGenerationErrorWindow");
                errorWindow.empty();

                $.each(errorArr, function(index, errorMessage){
                    errorWindow.append("<b>Error # " + (index+1) + "</b><br>");
                    errorWindow.append("<b>Description:</b> " + errorMessage.title + "<br>");
                    errorWindow.append("<b>Reason:</b> " + errorMessage.description + "<br>");
                    errorWindow.append("<hr>")
                });

                errorWindow.dialog({title: "RDF Generation Error Report", width: 900});
            }
        }
        else if(element["updateType"] == "PublishWorksheetHistoryUpdate") {
            var titleDiv = $("div#" + element["worksheetId"] + " div.WorksheetTitleDiv");
            // Remove existing link if any
            $("a.HistoryDownloadLink", titleDiv).remove();

            var downloadLink = $("<a>").attr("href", element["fileUrl"]).text("History").addClass("HistoryDownloadLink DownloadLink").attr("target", "_blank");
            $("div.tableTitleTextDiv", titleDiv).after(downloadLink);
        }
        else if(element["updateType"] == "PublishDatabaseUpdate") {
            if(element["numRowsNotInserted"] == 0) {
                $.sticky("Data saved successfully!");
            } else {
                $.sticky(element["numRowsNotInserted"] + " rows not saved in database! Check log file for details.");
            }
        }
        else if(element["updateType"] == "CleaningResultUpdate") {
            if(element["result"] != null) {
                //var pdata = getVaritions(element["result"]);
                if(element["result"][0] == null || element["result"][0]["top"].length==0) {
                    alert("Cannot find any transformations! ");
                    //populateInfoPanel();
                    //return;
                }
                var topCol = element["result"][0];
                //var sndCol = element["result"][1];
                //preprocessData(topCol, topCol["top"]);
                preprocessData(topCol,topCol["top"])
                $("div#columnHeadingDropDownMenu").data("topkeys",topCol["top"]);
                $("div#columnHeadingDropDownMenu").data("results", element["result"]);
                populateInfoPanel();
                populateResult(topCol);
                //var pdata = getVaritions(element["result"]);
                //populateVariations(topCol["top"], sndCol["data"]);

            }
        }
        else if(element["updateType"] == "InfoUpdate") {
            $.sticky(element["Info"]);
        }
        else if(element["updateType"] == "AlignmentSVGVisualizationUpdate") {
            // In d3-alignment-vis.js
            displayAlignmentTree_ForceKarmaLayout(element);

        }
        else if(element["updateType"] == "KarmaInfo") {
        	if(infos[element["Info"]]) {
        		//ignore;
        	} else {
	            $.sticky(element["Info"]);
	            
	            infos[element["Info"]] = true;
        	}
        } else if(element["updateType"] == "KarmaTrivialError") {
        	trivialErrors.push(element["TrivialError"]);
        }
        else if(element["updateType"] == "FetchDataMiningModelsUpdate") {

            var modelListRadioBtnGrp = $("#modelListRadioBtnGrp");
            modelListRadioBtnGrp.html('');
            var rows = element["models"]
            for(var x in rows) {
                modelListRadioBtnGrp.append('<input type="radio" name="group1" id="model_'+x+'" value="'+rows[x].url+'" /> <label for="model_'+x+'">'+rows[x].name+' ('+rows[x].url+') </label> <br />');
            }
            var modelListDiv = $('div#modelListDiv');
            modelListDiv.dialog({ title: 'Select a service',
                buttons: { "Cancel": function() { $(this).dialog("close"); }, "Select": submitSelectedModelNameToBeLoaded }, width: 300, height: 150 });

        }
        else if(element["updateType"] == "InvokeDataMiningServiceUpdate") {

            $('#invokeDMServiceSpan').html(elements['data']);
            var modelListDiv = $('div#invokeDMServiceDiv');
            modelListDiv.dialog({ title: 'Results from data mining service',
                buttons: { "Cancel": function() { $(this).dialog("close"); }  }, width: 300, height: 150 });
        }
        else if(element["updateType"] == "CleaningServiceOutput") {
            //console.log(element);
            //console.log(element["hNodeId"]);
            //console.log(element["chartData"]);

            drawChart(element);
            //drawBigChart() ;
        }
        else if(element["updateType"] == "WorksheetCleaningUpdate") {
            var worksheetChartData = element["worksheetChartData"];
            $.each(worksheetChartData, function(index, columnData){
                //console.log(columnData)
                drawChart(columnData);
            });
        }
        else if(element["updateType"] == "AdditionalRowsUpdate") {
            var dataTable = $("div#" + element.tableId + " > table.wk-table");
            addWorksheetDataRecurse(element.rows, dataTable, true);

            // Update the number of additional records left
            var additionalRecordsLeft = element.additionalRowsCount;
            var loadMoreLink = $("div#" + element.tableId +  " > div.load-more > a");

            if (additionalRecordsLeft == 0) {
                loadMoreLink.remove();
                $("div#" + element.tableId +  " > div.load-more").remove();
            } else {
                loadMoreLink.text(additionalRecordsLeft + " additional records, load more...");
            }
        }
    });
    
    if(trivialErrors.length > 0) {
       var errorWindow = $("#rdfGenerationErrorWindow");
       errorWindow.empty();

       var errExists = [];
        $.each(trivialErrors, function(index, errorMessage) {
        	if(errExists[errorMessage]) {
        		//do nothing
        	} else {
        		errorWindow.append("<b>Error # " + (index+1) + "</b><br>");
        		errorWindow.append("<b>Description:</b> " + errorMessage + "<br>");
        		errorWindow.append("<hr>");
        		errExists[errorMessage] = true;
        	}
        });

        errorWindow.dialog({title: "Error Report", width: 900});
        
    }
}

function addColumnHeadersRecurse(columns, headersTable, isOdd) {
    var row = $("<tr>");
    if (isOdd) {
        row.addClass("wk-row-odd");
    } else {
        row.addClass("wk-row-even");
    }

    var columnWidths = [];
    $.each (columns, function (index, column) {

        var td = $("<td>").addClass("wk-cell").attr("id", column.hNodeId);
        var headerDiv = $("<div>").addClass(column["columnClass"]);

        var colWidthNumber = 0;
        if (column["pythonTransformation"])
        {
        	td.data("pythonTransformation", column["pythonTransformation"]);
        }
        if (column["previousCommandId"])
        {
        	td.data("previousCommandId", column["previousCommandId"]);
        }
        if (column["columnDerivedFrom"])
        {
        	td.data("columnDerivedFrom", column["columnDerivedFrom"]);
        }
        
        if (column["hasNestedTable"]) {
            var pElem = $("<div>").addClass("wk-header wk-subtable-header").text(column["columnName"])
                .mouseenter(showColumnOptionButton).mouseleave(hideColumnOptionButton);

            var nestedTableContainer = $("<div>").addClass("table-container");
            var nestedTableHeaderContainer = $("<div>").addClass("table-header-container");
            var nestedTable = $("<table>").addClass("wk-table");
            if (isOdd) {
                nestedTable.addClass("htable-even");
            } else {
                nestedTable.addClass("htable-odd");
            }
            var nestedColumnWidths = addColumnHeadersRecurse(column["columns"], nestedTable, !isOdd);

            var colAdded = 0;
            $.each(nestedColumnWidths, function(index2, colWidth){
                if (!colWidth.widthAddedToNestedTable) {
                    colWidthNumber += colWidth.width;
                    colWidth.widthAddedToNestedTable = true;
                    colAdded++;
                }
                columnWidths.push(colWidth);
            });
            // Add padding for nested cells
            colWidthNumber += (colAdded * 2 * 9);
            // Add border width
            colWidthNumber += (colAdded + 1);

            headerDiv.append(pElem).append(nestedTableContainer.append(nestedTableHeaderContainer.append(nestedTable)));
        } else {
            headerDiv.addClass("wk-header").text(column["columnName"]).mouseenter(showColumnOptionButton).mouseleave(hideColumnOptionButton);
            // Pedro: limit cells to 30 chars wide. This should be smarter: if the table is not too wide, then allow more character.
            // If we impose the limit, we should set the CSS to wrap rather than use ... ellipsis.
            // We will need a smarter data structure so we can do two passes, first to compute the desired lenghts based on number of characters
            // and then revisit to assign widths based on total demand for space.
            var effectiveCharacterLength = Math.min(column.characterLength, 30);
            //colWidthNumber = Math.floor(column.characterLength * 12 * 0.75);
            colWidthNumber = Math.floor(effectiveCharacterLength * 12 * 0.75);
        }

        var colWidth = {};
        colWidth.columnClass = column["columnClass"];
        colWidth.width = colWidthNumber;
        colWidth.widthAddedToNestedTable = false;
        columnWidths.push(colWidth);

        row.append(td.append(headerDiv));
    });
    headersTable.append(row);
    return columnWidths;
}

function addWorksheetDataRecurse(rows, dataTable, isOdd) {
    // Loop through the rows
    $.each (rows, function (index, row) {
        var rowTr = $("<tr>");
        if (isOdd) {
            rowTr.addClass("wk-row-odd");
        } else {
            rowTr.addClass("wk-row-even");
        }

        // Loop through the values in a given row
        $.each(row, function(index2, cell){
            var td = $("<td>").addClass("wk-cell");
            var dataDiv = $("<div>");

            if (cell["hasNestedTable"]) {
                var nestedTableDataContainer = $("<div>").addClass("table-data-container").attr("id", cell["tableId"]);
                var nestedTable = $("<table>").addClass("wk-table");

                addWorksheetDataRecurse(cell["nestedRows"], nestedTable, !isOdd);

                var additionalRowsAvail = cell["additionalRowsCount"];

                var moreRowsDiv = $("<div>").addClass("load-more");
                if (additionalRowsAvail != 0) {
                    var moreRowsLink = $("<a>").attr("href","#!").text(additionalRowsAvail +
                        " additional records, load more...").click(loadAdditionalRowsHandler);
                    moreRowsDiv.append(moreRowsLink);
                    nestedTableDataContainer.append(nestedTable).append(moreRowsDiv);
                } else {
                    nestedTableDataContainer.append(nestedTable);
                }

                dataDiv.append(nestedTableDataContainer);
            } else {
                dataDiv.addClass("wk-value").addClass(cell["columnClass"]);
                dataDiv.text(cell["displayValue"])
                    .attr('id', cell["nodeId"])
                    .data("expandedValue", cell["expandedValue"])
                    .mouseenter(showTableCellMenuButton)
                    .mouseleave(hideTableCellMenuButton);
            }

            rowTr.append(td.append(dataDiv));
        });

        dataTable.append(rowTr);
    });
    return;
}
