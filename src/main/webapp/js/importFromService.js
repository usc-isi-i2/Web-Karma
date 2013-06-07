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

function openServiceImportDialog() {
    $("#serviceErrorRow").hide();
    getServicePreferences();
	$("#ServiceImportDiv").dialog({ modal: true , title: 'Import from Service', width: 500,
		height: 200, buttons: { "Cancel": function() { $(this).dialog("close"); },
			"Import": importService}});
}

function importService() {
    var url = $.trim($("#serviceUrl").val());
    var worksheetName = $.trim($("#serviceWorksheetName").val());

    if(!url || !worksheetName) {
        $("#serviceErrorRow").show();
        return false;
    }
    $("#ServiceImportDiv").dialog("close");

    var info = new Object();
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "ImportServiceCommand";
    info["serviceUrl"] = url;
    info["worksheetName"] = worksheetName;
    info["includeInputAttributes"] = $('#includeInputAttributesService').is(':checked');

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
                hideWaitingSignOnScreen();
            },
        error :
            function (xhr, textStatus) {
                alert("Error creating worksheet from web service! " + textStatus);
                hideWaitingSignOnScreen();
            }
    });

}

function styleAndAssignHandlerstoServiceImportObjects(){
	$("button#importFromServiceButton").button();

	$("#importFromServiceButton").click(openServiceImportDialog);
}

function getServicePreferences() {
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "FetchPreferencesCommand";
	info["preferenceCommand"] = "ImportServiceCommand";
	var returned = $.ajax({
	   	url: "RequestController", 
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	   			var json = $.parseJSON(xhr.responseText);
	    		$.each(json["elements"], function(index, element) {
	    			if(element["updateType"] == "ImportServiceCommandPreferences") {
	    				
	    				if(element["PreferenceValues"]) {
	    					$("input#serviceUrl").val(element["PreferenceValues"]["ServiceUrl"]);
                            $("input#serviceWorksheetName").val(element["PreferenceValues"]["WorksheetName"]);
	    				}
	    			}
	    		});
	    		
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured with fetching new rows! " + textStatus);
		   	}		   
	});
}




