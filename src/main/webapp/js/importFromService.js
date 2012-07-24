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
	$("#ServiceImportSourceList tr").hide();
	var table = $("#ServiceSourceList table");
	$("tr", table).remove();
	getServicePreferences();
	$("#ServiceImportDiv").dialog({ modal: true , title: 'Import from Service', width: 450,
		height: 650, buttons: { "Cancel": function() { $(this).dialog("close"); }, 
			"OK":function() { $(this).dialog("close"); }}});
}

function styleAndAssignHandlerstoServiceImportObjects(){
	$("button#importFromServiceButton").button();
	$("button#ServiceImportFieldsButton").button();
	
	$("#importFromServiceButton").click(openServiceImportDialog);
	
	// Click handler for button that gets source names
	$("#ServiceImportFieldsButton").click(function (){
		// Check if any field has been left empty
		if($.trim($("#ServiceUrl").val()) == "") {
			$("#ServiceImportErrorWindowText").text("Service URL is empty!!");
			$("#ServiceImportErrorWindow").show();
		} else {
			// Send the AJAX request
			$("#ServiceImportErrorWindow").hide();
			sendGenerateSourceListRequest();
		}
	});
	
	// Filter for the source names
	$("#serviceTableFilterTable").keyup( function (event) {
    	// fire the above change event after every letter
    	
    	//if esc is pressed or nothing is entered  
	    if (event.keyCode == 27 || $(this).val() == '') {
	      //if esc is pressed we want to clear the value of search box  
	      $(this).val('');  
	  
	      //we want each row to be visible because if nothing  
	      //is entered then all rows are matched.  
	      $('tr').removeClass('visible').show().addClass('visible');  
	    }  
	  
	    //if there is text, lets filter
	    else {  
	      filter('#ServiceSourceList tr', $(this).val(), "sourceName");  // present in databasImportDialog.js
	    }
  	});
}

function populateSourceList(element) {
	$("#ServiceImportSourceList tr").show();
	var table = $("#ServiceSourceList table");
	$("tr", table).remove();
	table.width(0);
	
	$.each(element["SourceList"] , function(index, value) {
		var tr = $("<tr>").data("sourceName", value);
		var tableNametd = $("<td>");
		tableNametd.text(value).append("&nbsp; &nbsp; &nbsp;");
		
		var importButton = $("<button>").addClass("fg-button ui-state-default ui-corner-all databaseTableImportButton").text("Import");
		tableNametd.append(importButton);
		
		importButton.hide();
		importButton.click(sendImportSourceRequest);
		
		table.append(tr.append(tableNametd));
		
		tableNametd.hover(function() {
			importButton.show();
			$(this).addClass("highlightDatabaseTableList");
		},
		function() {
			importButton.hide();
			$(this).removeClass("highlightDatabaseTableList");
		});
	});
	
	table.width(table.width() + 150);
	$("#serviceTableFilterTable").width(table.width()-6);
	$("#ServiceSourceList").width(table.width() + 18);
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
	    					$("input#ServiceUrl").val(element["PreferenceValues"]["ServiceUrl"]);
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


function sendGenerateSourceListRequest() {
	// Prepare the data to be sent to the server	
	var info = new Object();
	info["ServiceUrl"] = $.trim($("#ServiceUrl").val());
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "ImportServiceCommand";
	info["commandType"] = "GetSources";
		
	var returned = $.ajax({
	   	url: "RequestController", 
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	    		var json = $.parseJSON(xhr.responseText);
	    		$.each(json["elements"], function(i, element){
		    		if( element["updateType"] == "KarmaError")
		    			showServiceImportError(json);
	    			else if (element["updateType"] == "GetServiceSourceList")
	    				populateSourceList(element);
	    			else {} 
	    	    })
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured while getting table list! " + textStatus );
		   	}		   
	});
}


function sendImportSourceRequest() {
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "ImportServiceCommand";
	info["ServiceUrl"] = $.trim($("#ServiceUrl").val());
	info["sourceName"] = $(this).parents("tr").data("sourceName");
	info["commandType"] = "ImportSource";
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
	   			var flag = 0;
	   			$.each(json["elements"], function(index, element) {
	   				if( element["updateType"] == "KarmaError") {
	    				showServiceImportError(json);
	    				flag = -1;
	    			}
				});
				
				if(flag != -1) {
					parse(json);
					hideWaitingSignOnScreen();
    				alert("Source imported in workspace!");
				}
    			
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured with fetching new rows! " + textStatus);
	   			hideWaitingSignOnScreen();
		   	}		   
	});
}

function showServiceImportError(json) {
	var table = $("#ServiceSourceList table");
	$("tr", table).remove();
	$("#ServiceImportSourceList tr").hide();
	
	$.each(json["elements"], function(index, element) {
		if( element["updateType"] == "KarmaError") {
			$("#ServiceImportErrorWindowText").text(element["Error"]);
			$("#ServiceImportErrorWindow").show();
		}
			
	});
	
}









