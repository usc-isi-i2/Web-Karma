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

function assignHandlersToServiceInvocationObjects() {
    $("button#invokeServiceButton").click(function(){
        var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
        var selectedHNodeId = columnHeadingMenu.data("parentCellId");
        var tdTag = $("td#"+selectedHNodeId);
        var worksheetId = tdTag.parents("div.Worksheet").attr("id");
        
        var info = new Object();
        info["workspaceId"] = $.workspaceGlobalInformation.id;
        info["worksheetId"] = worksheetId;
        info["hNodeId"] = selectedHNodeId;
        info["command"] = "InvokeServiceCommand";
        
        showWaitingSignOnScreen();
        var returned = $.ajax({
            url: "RequestController", 
            type: "POST",
            data : info,
            dataType : "json",
            complete : 
                function (xhr, textStatus) {
                    var json = $.parseJSON(xhr.responseText);
                    parse(json);
                    hideWaitingSignOnScreen();
                },
            error :
                function (xhr, textStatus) {
                    $.sticky("Error invoking services!");
                }          
        });
    });
}
