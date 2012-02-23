function assignHandlersToCleaningPanelObjects() {
    var cleaningPanel = $("div#ColumnCleaningPanel"); 
    
    $("button", cleaningPanel).button();
    $("button#cleanColumnButton").click(handleCleanColumnButton);
    $("button#generateCleaningRules",cleaningPanel).click(handleGenerateCleaningRulesButton);
}


function handleCleanColumnButton() {
    var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
    columnHeadingMenu.hide();
    
    // Get the values from the column to be cleaned
    var selectedHNodeId = columnHeadingMenu.data("parentCellId");
    var tdTag = $("td#"+selectedHNodeId);
    var table = tdTag.parents("table.WorksheetTable");
    var lastRow = $("thead tr:last", table);
    var index = $("td", lastRow).index(tdTag);
    var values=[];
    $('tbody>tr>td:nth-child('+(index+1)+')', table).each( function() {
       values.push({"nodeId":$(this).attr("id"), "nodeValue": $(this).text()});       
    });
    
    // Create and store a array that stores the user provided examples
    var examples = [];
    columnHeadingMenu.data("cleaningExamples", examples);
    
    // Populate the table of cleaning preview table
    var cleaningTable = $("table#cleaningExamplesTable");
    $("tr", cleaningTable).remove();
    cleaningTable.append(
        $("<tr>").append($("<td>").text("Original values").addClass("cleaningTableHeading noBorder"))
                .append($("<td>").addClass("examplesDivider noBorder"))
                .append($("<td>").text("Provide examples").addClass("cleaningTableHeading noBorder"))
    );
    $.each(values, function(index, val) {
        var tr = $("<tr>")
            .append($("<td>").text(val["nodeValue"]))
            .append($("<td>").addClass("noBorder"))
            .append($("<td>").append($("<div>")
                .data("nodeId", val["nodeId"])
                .addClass("cleanExampleDiv").text(val["nodeValue"])
                .editable(function(value, settings) { 
                     examples.push(
                         {
                             "nodeId":$(this).data("nodeId"),
                             "before": val["nodeValue"],
                             "after":value
                         });
                     return(value);
                }, { 
                    type    : 'textarea',
                    submit  : 'OK',
                    cancel  : 'Cancel',
                    width: 140,
                })
            )
        );
        cleaningTable.append(tr);
    });
    
    $("div#ColumnCleaningPanel").dialog({title: 'Clean', width: 500,
        height: 500, buttons: { "Cancel": function() { $(this).dialog("close"); }, 
            "OK":function() { $(this).dialog("close"); }}});
}

function handleGenerateCleaningRulesButton() {
    var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
    var selectedHNodeId = columnHeadingMenu.data("parentCellId");
    var tdTag = $("td#"+selectedHNodeId);
    var vWorksheetId = tdTag.parents("div.Worksheet").attr("id");
    
    var examples = columnHeadingMenu.data("cleaningExamples");
    
    var info = new Object();
    info["vWorksheetId"] = vWorksheetId;
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["hNodeId"] = selectedHNodeId;
    info["command"] = "GenerateCleaningRulesCommand";
    info["examples"] = JSON.stringify(examples);
            
    var returned = $.ajax({
        url: "/RequestController", 
        type: "POST",
        data : info,
        dataType : "json",
        complete : 
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                //parse(json);
            },
        error :
            function (xhr, textStatus) {
                $.sticky("Error generating new cleaning rules!");
            }          
    });
}
