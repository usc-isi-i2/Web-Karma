function assignHandlersToCleaningPanelObjects() {
    var cleaningPanel = $("div#ColumnCleaningPanel"); 
    
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
        if($(this).attr("id"))    
            values.push({"nodeId":$(this).attr("id"), "nodeValue": $(this).text()});       
    });
    
    // Create and store a array that stores the user provided examples
    var examples = [];
    columnHeadingMenu.data("cleaningExamples", examples);
    
    // Populate the table of cleaning preview table
    var cleaningTable = $("table#cleaningExamplesTable");
    $("tr.nonHeading", cleaningTable).remove();
    
    $.each(values, function(index, val) {
        var tr = $("<tr>")
            .addClass("nonHeading")
            .append($("<td>").text(val["nodeValue"]))
            .append($("<td>").addClass("noBorder"))
            .append($("<td>")
                .append($("<table>").addClass("cleaningExampleDivTable").append($("<tr>")
                    .append($("<td>")
                        .append($("<div>")
                            .data("nodeId", val["nodeId"])
                            .addClass("cleanExampleDiv")
                            .text(val["nodeValue"])
                            .attr("id",val["nodeId"]+"_c1")
                            .editable(function(value, settings) {
                                var editDiv = $(this);
                                var revertButton = $("<div>").addClass("undoEditButton").button({
                                    icons: {
                                        primary: 'ui-icon-arrowreturnthick-1-w'
                                    },
                                    text: false
                                }).click(function(){
                                    editDiv.text(val["nodeValue"]);
                                    $(this).parent().remove();
                                    
                                    // Remove the user provided example from the examples JSON object
                                    var delInd = -1;
                                    $.each(examples, function(index2, example){
                                        if(example["nodeId"] == editDiv.data("nodeId"))
                                            delInd = index2; 
                                    });
                                    if(delInd != -1)
                                        examples.splice(delInd, 1);
                                }).qtip({
                                   content: {
                                      text: 'Undo'
                                   },
                                   style: {
                                      classes: 'ui-tooltip-light ui-tooltip-shadow'
                                   }
                                });
                                
                                // Remove existing button
                                $("td.noBorder", $(this).parent().parent()).remove();
                                $(this).parent().parent().append($("<td>").addClass("noBorder").append(revertButton));
                                
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
                                onblur: 'ignore',
                            })
                        )
                    )
                )
            )
        );
        $("tr#buttonsAndRuleInfoRow").before(tr);
    });
    
    $("div#ColumnCleaningPanel").dialog({title: 'Clean', width: 500,
        height: 500, buttons: { "Cancel": function() { $(this).dialog("close"); },  
            "Generate Rules": handleGenerateCleaningRulesButton,
            "Submit":function() { $(this).dialog("close"); }}});
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
