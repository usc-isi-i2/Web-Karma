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

function styleAndAssignHandlersToPyTransformElements() {
    var editor = ace.edit("transformCodeEditor");
    editor.setTheme("ace/theme/dreamweaver");
    editor.getSession().setMode("ace/mode/python");
    editor.getSession().setUseWrapMode(true);
    editor.getSession().setValue("return getValue(\"state\")");

    // $("#transformCodeEditor").resizable();
    $("#previewPyTransformButton").button().click(submitPythonPreview);
    $("#pyTransformViewErrorButton").button().click(function(event){
        var positionArray = [event.clientX, event.clientY];
        $("#pyTransformErrorWindow").dialog({width: 400, title: "Python transformation errors", position: positionArray}).show();
    });

}

function openPyTransformDialogBox() {
    $("table#pythonPreviewResultsTable").hide();
    $("span#pyTransformColumnNameError").hide();
    $("#pyTransformViewErrorButton").button('disable');
    var dialogBox = $("div#pyTransformDialog");
    dialogBox.dialog({width: 540, height: 460, title:"Python Transform", resizable:true
        , buttons: {
            "Cancel": function() { $(this).dialog("close"); },
            "Submit": submitPythonTransform}
    });
}

function submitPythonPreview() {
    var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
    var hNodeId = columnHeadingMenu.data("parentCellId");
    // prepare the JSON Object to be sent to the server
    var info = {};
    info["hNodeId"] = hNodeId;
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["worksheetId"] = $("td#" + hNodeId).parents("div.Worksheet").attr("id");;
    info["transformationCode"] = ace.edit("transformCodeEditor").getValue();
    info["errorDefaultValue"] = $("#pythonTransformErrorDefaultValue").val();
    info["command"] = "PreviewPythonTransformationResultsCommand";

    // Send the request
    $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                var previewTable = $("table#pythonPreviewResultsTable");
                $("tr",previewTable).remove();
                $.each(json["elements"], function(index, element) {
                    if(element["updateType"] == "PythonPreviewResultsUpdate") {
                        var result = element["result"];
                        $.each(result, function(index2, resVal){
                            previewTable.append($("<tr>").append($("<td>").text(resVal)));
                        });
                        $("div.pythonError", errorWindow).remove();
                        var errors = element["errors"];
                        if (errors.length > 0) {
                            $("#pyTransformViewErrorButton").button('enable');
                            var errorWindow = $("#pyTransformErrorWindow");
                            $.each(errors, function(index3, error){
                                var errorHtml = $("<div>").addClass("pythonError")
                                    .append($("<span>").addClass("pythonErrorRowNumber").text("Row: " + error.row)).append($("<br>"))
                                    .append($("<span>").addClass("pythonErrorText").text("Error: " + error["error"])).append($("<br>")).append($("<br>"));
                                errorWindow.append(errorHtml);
                            })
                        } else {
                            $("#pyTransformViewErrorButton").button('disable');
                        }
                    } else if(element["updateType"] == "KarmaError") {
                        $.sticky(element["Error"]);
                    }
                });
                previewTable.show();
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured with fetching new rows! " + textStatus);
            }
    });
}

function submitPythonTransform() {
    var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
    var hNodeId = columnHeadingMenu.data("parentCellId");
    var worksheetId = $("td#" + hNodeId).parents("div.Worksheet").attr("id");
    var columnName = $("#pythonTransformNewColumnName").val();
    // Validate new column name
    var validationResult = true;
    if (!columnName)
        validationResult = false;
    // Check if the column name already exists
    var columnNameDivs = $("#" + worksheetId + " div.ColumnHeadingNameDiv");
    $.each(columnNameDivs, function(index, element) {
        if ($.trim($(element).text()) == columnName) {
            validationResult = false;
        }
    });
    if (!validationResult) {
        $("span#pyTransformColumnNameError").show();
        $("#pythonTransformNewColumnName").focus();
        return false;
    }

    $("div#pyTransformDialog").dialog("close");
    // prepare the JSON Object to be sent to the server
    var info = {};
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "SubmitPythonTransformationCommand";

    var newInfo = [];
    newInfo.push(getParamObject("newColumnName",columnName, "other"));
    newInfo.push(getParamObject("transformationCode", ace.edit("transformCodeEditor").getValue(), "other"));
    newInfo.push(getParamObject("worksheetId", worksheetId, "worksheetId"));
    newInfo.push(getParamObject("hNodeId", hNodeId, "hNodeId"));
    newInfo.push(getParamObject("hTableId", "", "other"));
    newInfo.push(getParamObject("errorDefaultValue", $("#pythonTransformErrorDefaultValue").val(), "other"));
    info["newInfo"] = JSON.stringify(newInfo);

    showLoading(worksheetId)
    // Send the request
    $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                parse(json);
                hideLoading(worksheetId);
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured with fetching new rows! " + textStatus);
                hideLoading(worksheetId);
            }
    });
}