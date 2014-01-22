function isDialogInitialized(dialog) {
	if(dialog.hasClass("ui-dialog-content"))
		return true;
	return false;
}

function getColumnHeadings(worksheetId) {
	var columnNames = [];
	
	var columnNameDivs = $("#" + worksheetId + " div.wk-header a.ColumnTitle");
    $.each(columnNameDivs, function(index, element) {
        columnNames.push($.trim($(element).text()));
    });
    
    return columnNames;
}

function showLoading(worksheetId) {
    // Remove any existing coverDiv
    $("div#WaitingDiv_" + worksheetId).remove();

    // Create a new cover
    var coverDiv = $("<div>").attr("id","WaitingDiv_"+worksheetId).addClass('waitingDiv')
        .append($("<div>").html('<b>Please wait</b>')
            .append($('<img>').attr("src","images/ajax-loader.gif"))
        );

    var spaceToCoverDiv = $("div#"+worksheetId);
    spaceToCoverDiv.append(coverDiv.css({"position":"absolute", "height":spaceToCoverDiv.height(),
        "width": spaceToCoverDiv.width(), "top":spaceToCoverDiv.position().top, "left":spaceToCoverDiv.position().left}).show());
}

function hideLoading(worksheetId) {
    $("div#WaitingDiv_"+worksheetId).hide();
}

function showWaitingSignOnScreen() {
    var coverDiv = $("<div>").attr("id","WaitingDiv").addClass('waitingDiv')
        .append($("<div>").html('<b>Please wait</b>')
            .append($('<img>').attr("src","images/ajax-loader.gif"))
        );

    var spaceToCoverDiv = $('body');
    spaceToCoverDiv.append(coverDiv.css({"position":"fixed", "height":$(document).height(),
        "width": $(document).width(), "zIndex":100,"top":spaceToCoverDiv.position().top, "left":spaceToCoverDiv.position().left}).show());
}

function hideWaitingSignOnScreen() {
    $("div#WaitingDiv").hide();
}

function testSparqlEndPoint(url, worksheetId) {
	var info = new Object();
	info["worksheetId"] = worksheetId;
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "TestSPARQLEndPointCommand";
	info["tripleStoreUrl"] = url;
	window.conncetionStat = false;
	var returned = $.ajax({
	   	url: "RequestController",
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	async : false,
	   	complete :
	   		function (xhr, textStatus) {
	    		var json = $.parseJSON(xhr.responseText);
	    		if(json['elements'] && json['elements'][0]['connectionStatus'] && json['elements'][0]['connectionStatus'] == 1) {
	    			window.conncetionStat = true;
	    		}
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured while testing connection to sparql endpoint!" + textStatus);
		   	}
	});
	return window.conncetionStat;
}