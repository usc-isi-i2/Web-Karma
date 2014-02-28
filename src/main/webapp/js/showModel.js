function styleAndAssignHandlersToApplyModelDialog() {
    var optionsDiv = $("#showExistingModelDialog");
    var table = $("#modelsList");

    $("#chooseMatchingModels").change(function() {
        $("div.error", optionsDiv).hide();
        $("div.noItems", optionsDiv).hide();
        var sourceName = optionsDiv.data("existingWorksheetName");
        var count = 0;
        $.each($("tr", table), function(index, row) {
            var rowSourceName = $("td", row).first().data("sourceName");
            if (sourceName !== rowSourceName) {
                $(row).hide();
            } else {
                count++;
            }
        });
        if (count === 0) {
            $("div.noItems", optionsDiv).show();
        }
    });

    $("#chooseAllModels").change(function() {
        $("div.error", optionsDiv).hide();
        $("div.noItems", optionsDiv).hide();
        $("tr", table).show();

        if($("tr", table).length == 0) {
            $("div.noItems", optionsDiv).show();
        }
    });
}

function showDialogToLoadModel() {
    var lastWorksheetLoaded = $("div.Worksheet").last();
    var optionsDiv = $("#showExistingModelDialog");
    $("div.error", optionsDiv).hide();
    $("div.noItems", optionsDiv).hide();

    var info = new Object();
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "FetchExistingModelsForWorksheetCommand";
    info["worksheetId"] = lastWorksheetLoaded.attr("id");
    info["garbage"] = "garbage";
    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);

                $.each(json["elements"], function(index, element) {
                    if (element["updateType"] == "ExistingModelsList") {
                        optionsDiv.data("existingWorksheetName", element["worksheetName"]);
                        var modelsList = element["existingModelNames"];

                        // Remove existing models in the table
                        var table = $("#modelsList");
                        $("tr", table).remove();

                        if (!modelsList || modelsList.length === 0) {
                            // Create new model by default if no model exists in the triple store
                            createNewModelForWorksheet();
                        } else {
                            // Show a dialog box to ask user for applying an existing model
                            $.each(modelsList, function(index, model) {
                                var trTag = $("<tr>");
                                var edgeTd = $("<td>").append($("<span>").text(model["modelName"]))
                                    .data("sourceName", model["sourceName"])
                                    .click(function(){
                                        $("td", table).removeClass("selected");
                                        $(this).addClass("selected");
                                    });

                                trTag.append(edgeTd);
                                table.append(trTag);
                            });

                            // Show the dialog box
                            optionsDiv.dialog({width: 250, height: 300,
                                closeOnEscape: false,
                                open: function(event, ui) {
                                    // Hide the close button
                                    $(this).parent().children().children('.ui-dialog-titlebar-close').hide();
                                },
                                title: "Apply Existing Model",
                                modal: true,
                                buttons: {
//                                "Cancel": function() { $(this).dialog("close");},
                                    "Create New Model": createNewModelForWorksheet,
                                    "Apply Selected Model":submitModelForWorksheet }});
                        }
                    } else if (element["updateType"] == "KarmaError") {
                        $.sticky(element["Error"]);
                    }
                });
            },
        error :
            function (xhr, textStatus) {
                $.sticky("Error occurred while setting properties!");
            }
    });
}

function submitModelForWorksheet() {
    var optionsDiv = $("#showExistingModelDialog");

    var info = {};
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "ApplyModelFromTripleStoreCommand";
    info["worksheetId"] = $("div.Worksheet").last().attr("id");

    var table = $("#modelsList");
    if ($("td.selected", table).length == 0) {
        $("div.error", optionsDiv).show();
        return false;
    }

    info["sourceName"] = $("td.selected", table).data("sourceName");
    info["modelName"] = $("td.selected span", table).text();

    if(isDialogInitialized(optionsDiv))
    	optionsDiv.dialog("close");
    showLoading(info["worksheetId"]);
    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                parse(json);
                hideLoading(info["worksheetId"]);
            },
        error :
            function (xhr, textStatus) {
                $.sticky("Error occurred applying model!");
                hideLoading(info["worksheetId"]);
            }
    });
}

function createNewModelForWorksheet() {
    var optionsDiv = $("#showExistingModelDialog");
    if(isDialogInitialized(optionsDiv))
    	optionsDiv.dialog("close");
    
    var info = {};
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "CreateNewModelCommand";
    info["worksheetId"] = $("div.Worksheet").last().attr("id");

    showLoading(info["worksheetId"]);
    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                parse(json);
                hideLoading(info["worksheetId"]);
            },
        error :
            function (xhr, textStatus) {
                $.sticky("Error occurred applying model!");
                hideLoading(info["worksheetId"]);
            }
    });
}