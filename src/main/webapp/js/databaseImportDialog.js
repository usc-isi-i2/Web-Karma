function openDatabaseTableImportDialog() {
	$("#DatabaseImportTableListAndPreview tr").hide();
	$("#DatabaseTablePreview table tr").remove();
	$("#dbPreviewTableName").text("");
	$("#DatabasePassword").val("");
	sendDBTableImportCommandCreateRequest();
	$("#DatabaseImportDiv").dialog({ modal: true , title: 'Import Database Table', width: 900,
		height: 650, buttons: { "Cancel": function() { $(this).dialog("close"); }, 
			"OK":function() { $(this).dialog("close"); }}});
}

function styleAndAssignHandlerstoDatabaseImportObjects(){
	$("button#importDatabaseTableButton").button();
	$("button#DatabaseImportFieldsButton").button();
	
	$("#importDatabaseTableButton").click(openDatabaseTableImportDialog);
	
	// Click handler for button that loads the databases or tables
	$("#DatabaseImportFieldsButton").click(function (){
		// Check if any field has been left empty
		if($.trim($("#DatabaseHostName").val()) == "" || $.trim($("#DatabasePortNumber").val()) == ""
			|| $.trim($("#DatabaseUsername").val()) == "" || $.trim($("#DatabasePassword").val()) == ""
			|| $.trim($("#DatabaseName").val()) == "") {
			$("#DatabaseImportErrorWindowText").text("No field can be left empty!");
			$("#DatabaseImportErrorWindow").show();
		} else if (!isNumeric($.trim($("#DatabasePortNumber").val()))) {
			$("#DatabaseImportErrorWindowText").text("Port number should be positive whole number!");
			$("#DatabaseImportErrorWindow").show();
		} else {
			// Send the AJAX request
			$("#DatabaseImportErrorWindow").hide();
			sendGenerateTableListRequest();
		}
	});
	
	// Show an extra field when user chooses Oracle
	$("#databaseTypeSelector").change(function(){
		if($("#databaseTypeSelector option:selected").text() == "Oracle") {
			$("#DBNameCell span").text("SID/Service Name");
		} else {
			$("#DBNameCell span").text("Database Name");
		}
	});
	
	// Filter for the table names
	$("#databaseTableFilterTable").keyup( function (event) {
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
	      filter('#DatabaseTablesList tr', $(this).val());  
	    }
  	});
}

//filter results
function filter(selector, query) {
	  query	= $.trim(query); //trim white space
	  query = query.replace(/ /gi, '|'); //add OR for regex query
	
	  $(selector).each(function() {
	    ($(this).data("tableName").search(new RegExp(query, "i")) < 0) 
	    	? $(this).hide().removeClass('visible') : $(this).show().addClass('visible');
	  });
}

function isNumeric(input){
    return /^ *[0-9]+ *$/.test(input);
}

function populateTableList(json) {
	$("#DatabaseImportTableListAndPreview tr").show();
	var table = $("#DatabaseTablesList table");
	$("tr", table).remove();
	table.width(0);
	
	// Clear the existing table preview if any
	$("#DatabaseTablePreview table tr").remove();
	$("#dbPreviewTableName").text("");
	
	table.data("commandId", json["elements"][0]["commandId"])
	$.each(json["elements"][0]["TableList"][0] , function(index, value) {
		var tr = $("<tr>").data("tableName", value);
		var tableNametd = $("<td>");
		tableNametd.text(value).append("&nbsp; &nbsp; &nbsp;");
		
		var importButton = $("<button>").addClass("fg-button ui-state-default ui-corner-all databaseTableImportButton").text("Import");
		var previewButton = $("<button>").addClass("fg-button ui-state-default ui-corner-all databaseTableImportButton").text("Preview");
		tableNametd.append(importButton).append("&nbsp;&nbsp;");
		tableNametd.append(previewButton);
		
		importButton.hide();
		previewButton.hide();
		importButton.click(sendImportTableRequest);
		previewButton.click(sendPreviewTableRequest);
		
		table.append(tr.append(tableNametd));
		
		tableNametd.hover(function() {
			importButton.show();
			previewButton.show();
			$(this).addClass("highlightDatabaseTableList");
		},
		function() {
			importButton.hide();
			previewButton.hide();
			$(this).removeClass("highlightDatabaseTableList");
		});
	});
	
	table.width(table.width() + 150);
	$("#databaseTableFilterTable").width(table.width()-6);
	$("#DatabaseTablesList").width(table.width() + 18);
}

function sendDBTableImportCommandCreateRequest() {
	var table = $("#DatabaseTablesList table");
	$("tr", table).remove();
	
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "ImportDatabaseTableCommand";
	info["interactionType"] = "getPreferencesValues";
	
	var returned = $.ajax({
	   	url: "/RequestController", 
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	   			var json = $.parseJSON(xhr.responseText);
	    		$.each(json["elements"], function(index, element) {
	    			if(element["updateType"] == "ImportDatabaseTableCommandPreferences") {
	    				var commandId = element["commandId"];
	    				$("#DatabaseImportDiv").data("commandId", commandId);
	    				
	    				if(element["PreferenceValues"]) {
	    					$("#databaseTypeSelector").val(element["PreferenceValues"]["dbType"]);
	    					$("#DatabaseHostName").val(element["PreferenceValues"]["hostname"]);
	    					$("#DatabasePortNumber").val(element["PreferenceValues"]["portnumber"]);
	    					$("#DatabaseUsername").val(element["PreferenceValues"]["username"]);
	    					$("#DatabaseName").val(element["PreferenceValues"]["dBorSIDName"]);
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

function sendGenerateTableListRequest() {
	// Prepare the data to be sent to the server	
	var info = new Object();
	info["dBType"] = $("#databaseTypeSelector option:selected").text();
	info["hostname"] = $.trim($("#DatabaseHostName").val());
	info["portNumber"] = $.trim($("#DatabasePortNumber").val());
	info["username"] = $.trim($("#DatabaseUsername").val());
	info["password"] = $.trim($("#DatabasePassword").val());
	info["dBorSIDName"] = $.trim($("#DatabaseName").val());
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["commandId"] = $("#DatabaseImportDiv").data("commandId");
	info["command"] = "ImportDatabaseTableCommand";
	info["interactionType"] = "generateTableList";
		
	var returned = $.ajax({
	   	url: "/RequestController", 
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	    		var json = $.parseJSON(xhr.responseText);
	    		if( json["elements"][0]["updateType"] == "KarmaError")
	    			showDatabaseImportError(json);
	    		else if (json["elements"][0]["updateType"] == "GetDatabaseTableList")
	    			populateTableList(json);
	    		else {} 
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured while getting table list! " + textStatus );
		   	}		   
	});
}


function sendImportTableRequest() {
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "ImportDatabaseTableCommand";
	info["commandId"] = $("#DatabaseImportDiv").data("commandId");
	info["tableName"] = $(this).parents("tr").data("tableName");
	info["interactionType"] = "importTable";
	info["execute"] = true;
		
	var returned = $.ajax({
	   	url: "/RequestController", 
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
	    				showDatabaseImportError(json);
	    				flag = -1;
	    			}
				});
				
				if(flag != -1) {
					parse(json);
    				alert("Table imported in the workspace!");
				}
    			
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured with fetching new rows! " + textStatus);
		   	}		   
	});
}

function sendPreviewTableRequest() {
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "ImportDatabaseTableCommand";
	info["commandId"] = $(this).parents("table").data("commandId");
	info["tableName"] = $(this).parents("tr").data("tableName");
	info["interactionType"] = "previewTable";
	// options["execute"] = true;
		
	var returned = $.ajax({
	   	url: "/RequestController", 
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	   			//alert(xhr.responseText);
	    		var json = $.parseJSON(xhr.responseText);
	    		handleDBTablePreviewResponse(json);
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured with fetching new rows! " + textStatus);
		   	}		   
	});
}

function handleDBTablePreviewResponse (json) {
	$("#DatabaseTablePreview table tr").remove();
	$("#dbPreviewTableName").text("");
	if(json["elements"][0]["updateType"] == "ImportDatabaseTablePreview") {
		var table = $("#DatabaseTablePreview table");
		
		// Add the headers
		var headerTR = $("<tr>");
		$.each(json["elements"][0]["headers"], function(index, header) {
			var th = $("<th>").text(header);
			headerTR.append(th);
		});
		table.append(headerTR);
		
		// Add the data
		$.each(json["elements"][0]["rows"], function(index, row) {
			var tr = $("<tr>");
			$.each(row, function(index2, cell){
				var td = $("<td>");
				if(cell != null)
					td.text(cell);
				else
					td.text("");
				tr.append(td);
			});
			table.append(tr);
		});
		$("#dbPreviewTableName").text("Preview of " + 
			json["elements"][0]["tableName"] + " (Only Top 10 rows shown)");
	}
} 

function showDatabaseImportError(json) {
	var table = $("#DatabaseTablesList table");
	$("tr", table).remove();
	$("#DatabaseImportTableListAndPreview tr").hide();
	
	$.each(json["elements"], function(index, element) {
		if( element["updateType"] == "KarmaError") {
			$("#DatabaseImportErrorWindowText").text(element["Error"]);
			$("#DatabaseImportErrorWindow").show();
		}
			
	});
	
}









