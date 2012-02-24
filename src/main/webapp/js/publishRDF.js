function showHideRdfInfo() {
		if( $("input#saveToRDFStore").is(":checked")) {
			$("div#rdfStoreInfo").show();
		}
		else {
			$("div#rdfStoreInfo").hide();
		}
}

function publishRDFFunction() {
		$("div#PublishRDFDialogBox").dialog("close");

		var info = new Object();
		info["vWorksheetId"] = $("div#WorksheetOptionsDiv").data("worksheetId");
		info["workspaceId"] = $.workspaceGlobalInformation.id;
		info["command"] = "PublishRDFCommand";
		info["addInverseProperties"] = $("input#addInverseProperties").is(":checked");
		info["rdfPrefix"] = $("input#rdfPrefix").val();
		info["saveToStore"] = $("input#saveToStore").is(":checked");;
		info["hostName"] = $("input#hostName").val();
		info["dbName"] = $("input#dbName").val();
		info["userName"] = $("input#userName").val();
		info["password"] = $("input#password").val();
		info["modelName"] = $("input#modelName").val();

		if( $("input#saveToRDFStore").is(":checked")) {
			publishRDFToStore(info);
		}
		else {
			publishRDFToFile(info);		
		}
}

function publishRDFToFile(info) {

		showLoadingRDF(info["vWorksheetId"],"Saving to file...");
		returnFunc(info);
}

function publishRDFToStore(info) {
		
		showLoadingRDF(info["vWorksheetId"],"Saving to RDF store...");
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
		   			alert("Error occured while generating RDF!" + textStatus);
		   			hideLoading(info["vWorksheetId"]);
			   	}		   
		});
}

function showLoadingRDF(worksheetId, message) {
    var coverDiv = $("<div>").attr("id","WaitingDiv_"+worksheetId).addClass('waitingDiv')
        .append($("<div>").html('<b>'+message+'</b>')
            .append($('<img>').attr("src","images/ajax-loader.gif"))
    );
     
    var spaceToCoverDiv = $("div#"+worksheetId);
    spaceToCoverDiv.append(coverDiv.css({"position":"absolute", "height":spaceToCoverDiv.height(), 
        "width": spaceToCoverDiv.width(), "top":spaceToCoverDiv.position().top, "left":spaceToCoverDiv.position().left}).show());
}

function getPreferences() {
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "FetchPreferencesCommand";
	info["preferenceCommand"] = "PublishRDFCommand";
	var returned = $.ajax({
	   	url: "/RequestController", 
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	   			var json = $.parseJSON(xhr.responseText);
	    		$.each(json["elements"], function(index, element) {
	    			if(element["updateType"] == "PublishRDFCommandPreferences") {
	    				
	    				if(element["PreferenceValues"]) {
	    					$("input#hostName").val(element["PreferenceValues"]["hostName"]);
	    					$("input#dbName").val(element["PreferenceValues"]["dbName"]);
	    					$("input#userName").val(element["PreferenceValues"]["userName"]);
	    					$("input#modelName").val(element["PreferenceValues"]["modelName"]);
	    					$("input#rdfPrefix").val(element["PreferenceValues"]["rdfPrefix"]);
	    					$("input#saveToRDFStore").val(element["PreferenceValues"]["saveToStore"]);
	    					$("input#saveToRDFStore").val(element["PreferenceValues"]["saveToStore"]);
	    					$("input#addInverseProperties").val(element["PreferenceValues"]["addInverseProperties"]);
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
