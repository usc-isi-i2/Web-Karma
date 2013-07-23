function showChartButtonHandler() {
    var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
    var hNodeId = columnHeadingMenu.data("parentCellId");

    var vWorksheetId = $("td#" + hNodeId).parents("table.WorksheetTable").attr("id");

    var info = new Object();
    var newInfo = [];   // for input parameters
    newInfo.push(getParamObject("vWorksheetId", vWorksheetId ,"vWorksheetId"));
    newInfo.push(getParamObject("hNodeId", hNodeId,"hNodeId"));

    info["newInfo"] = JSON.stringify(newInfo);
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "InvokeCleaningServiceCommand";

    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                parse(json);
            },
        error :
            function (xhr, textStatus) {
                $.sticky("Error occured while renaming column!");
            }
    });
}