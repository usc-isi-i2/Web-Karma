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

function styleAndAssignHandlerToResetButton() {
	$("#resetButton").button().click(function(event) {
		$("#resetDialogDiv input").attr("checked", false);
		$("div#resetDialogDiv").dialog({
			width: 100,
			position: [event.clientX-50, event.clientY+20],
			title: "Reset Options",
			buttons: { "Cancel": function() { $(this).dialog("close"); }, "Submit": submitResetOptions }
		});
	});
}

function submitResetOptions() {
	$("div#resetDialogDiv").dialog("close");
		
	if (!($("#forgetSemanticTypes").is(':checked')) && !($("#forgetModels").is(':checked'))) {
		$.sticky("Please select an option!");
		return false;
	}
	
	var info = new Object();
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "ResetKarmaCommand";
	info["forgetSemanticTypes"] = $("#forgetSemanticTypes").is(':checked');
	info["forgetModels"] = $("#forgetModels").is(':checked');
	
	showWaitingSignOnScreen();
	var returned = $.ajax({
    	url: "RequestController", 
    	type: "POST",
    	data : info,
    	dataType : "json",
    	complete : function (xhr, textStatus) {
            hideWaitingSignOnScreen();
            var json = $.parseJSON(xhr.responseText);
            parse(json);
        },
    	error : function (xhr, textStatus) {
    		hideWaitingSignOnScreen();
        }
	});
	
	
}
