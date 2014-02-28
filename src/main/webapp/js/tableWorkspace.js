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
        handleTableCellExpandButton(event);
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

function handleTableCellExpandButton(event) {
    var tdTagId = $("#tableCellToolBarMenu").data("parentCellId");
    // Get the full expanded value
    var cellValueTag = $("div#"+tdTagId);
    var value = cellValueTag.data("expandedValue");

    // Get the RDF text (if it exists)
    var info = new Object();
    info["nodeId"] = tdTagId;
    info["worksheetId"] = cellValueTag.parents("div.Worksheet").attr("id");
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "PublishRDFCellCommand";

    var returned = $.ajax({
        url: "RequestController",
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
    var cellDiv = $(this);

    var optionsDiv = $("div#tableCellMenuButtonDiv");
    optionsDiv.data("parentCellId", cellDiv.attr("id"));

    // Show it at the right place
    var top = $(cellDiv).offset().top + $(cellDiv).height()-20;
    var left = $(cellDiv).offset().left + $(cellDiv).width()-20;
    optionsDiv.css({"position":"absolute",
        "top":top,
        "left": left}).show();
}

function hideTableCellMenuButton() {
    $("div#tableCellMenuButtonDiv").hide();
}


function styleAndAssignHandlersToMergeButton() {
    $("button#mergeButton").button().click(function(){
        var info = new Object();
        info["workspaceId"] = $.workspaceGlobalInformation.id;
        info["command"] = "ImportUnionResultCommand";

        showWaitingSignOnScreen();
        var returned = $.ajax({
            url: "RequestController",
            type: "POST",
            data : info,
            dataType : "json",
            complete :
                function (xhr, textStatus) {
                    //alert(xhr.responseText);
                    var json = $.parseJSON(xhr.responseText);
                    parse(json);
                    hideCleanningWaitingSignOnScreen();
                },
            error :
                function (xhr, textStatus) {
                    $.sticky("Error occured while doing merge!");
                    hideCleanningWaitingSignOnScreen();
                }
        });
    });
}


