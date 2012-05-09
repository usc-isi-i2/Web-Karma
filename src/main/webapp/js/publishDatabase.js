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

function publishDatabaseFunction() {
		$("div#PublishDatabaseDialogBox").dialog("close");

		var info = new Object();
		info["vWorksheetId"] = $("div#WorksheetOptionsDiv").data("worksheetId");
		info["workspaceId"] = $.workspaceGlobalInformation.id;
		info["command"] = "PublishDatabaseCommand";
		info["dbType"] = $("select#dbType").val();
		info["hostName"] = $("input#hostName1").val();
		info["dbName"] = $("input#dbName1").val();
		info["userName"] = $("input#userName1").val();
		info["password"] = $("input#password1").val();
		info["tableName"] = $("input#tableName").val();
		info["port"] = $("input#port").val();
		info["overwriteTable"] = $("input#overwriteTable").is(":checked");
		info["insertTable"] = $("input#insertTable").is(":checked");

		showLoadingDatabase(info["vWorksheetId"],"Saving to database...");
		returnFunc(info);
}

function returnFunc(info) {
		var returned = $.ajax({
		   	url: "/RequestController", 
		   	type: "POST",
		   	data : info,
		   	dataType : "json",
		   	complete : 
		   		function (xhr, textStatus) {
		    		var json = $.parseJSON(xhr.responseText);
		    		parse(json);
		    		hideLoading(info["vWorksheetId"]);
			   	},
			error :
				function (xhr, textStatus) {
		   			alert("Error occured while saving to database!" + textStatus);
		   			hideLoading(info["vWorksheetId"]);
			   	}		   
		});
}

function showLoadingDatabase(worksheetId, message) {
    var coverDiv = $("<div>").attr("id","WaitingDiv_"+worksheetId).addClass('waitingDiv')
        .append($("<div>").html('<b>'+message+'</b>')
            .append($('<img>').attr("src","images/ajax-loader.gif"))
    );
     
    var spaceToCoverDiv = $("div#"+worksheetId);
    spaceToCoverDiv.append(coverDiv.css({"position":"absolute", "height":spaceToCoverDiv.height(), 
        "width": spaceToCoverDiv.width(), "top":spaceToCoverDiv.position().top, "left":spaceToCoverDiv.position().left}).show());
}

function getDatabasePreferences() {
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "FetchPreferencesCommand";
	info["preferenceCommand"] = "PublishDatabaseCommand";
	var returned = $.ajax({
	   	url: "/RequestController", 
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	   			var json = $.parseJSON(xhr.responseText);
	    		$.each(json["elements"], function(index, element) {
	    			if(element["updateType"] == "PublishDatabaseCommandPreferences") {
	    				
	    				if(element["PreferenceValues"]) {
	    					$("select#dbType").val(element["PreferenceValues"]["dbType"]);
	    					$("input#hostName1").val(element["PreferenceValues"]["hostName"]);
	    					$("input#dbName1").val(element["PreferenceValues"]["dbName"]);
	    					$("input#userName1").val(element["PreferenceValues"]["userName"]);
	    					$("input#tableName").val(element["PreferenceValues"]["tableName"]);
	    					$("input#port").val(element["PreferenceValues"]["port"]);
	    					$("input#overwriteTable").val(element["PreferenceValues"]["overwriteTable"]);
	    					$("input#insertTable").val(element["PreferenceValues"]["insertTable"]);
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
