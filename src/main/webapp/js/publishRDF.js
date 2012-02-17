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
		if( $("input#saveToRDFStore").is(":checked")) {
			publishRDFToStore();
		}
		else {
			publishRDFToFile();		
		}
}

function publishRDFToFile() {
		var info = new Object();
		info["vWorksheetId"] = $("div#WorksheetOptionsDiv").data("worksheetId");
		info["workspaceId"] = $.workspaceGlobalInformation.id;
		info["command"] = "PublishRDFCommand";
		info["addInverseProperties"] = $("input#addInverseProperties").is(":checked");
		info["rdfPrefix"] = $("input#rdfPrefix").val();
		info["saveToStore"] = "false";

		returnFunc(info);
}

function publishRDFToStore() {
		$("div#PublishRDFDialogBox").dialog("close");

		var info = new Object();
		info["vWorksheetId"] = $("div#WorksheetOptionsDiv").data("worksheetId");
		info["workspaceId"] = $.workspaceGlobalInformation.id;
		info["command"] = "PublishRDFCommand";
		info["addInverseProperties"] = $("input#addInverseProperties").is(":checked");
		info["rdfPrefix"] = $("input#rdfPrefix").val();
		info["saveToStore"] = "true";
		info["hostName"] = $("input#hostName").val();
		info["dbName"] = $("input#dbName").val();
		info["userName"] = $("input#userName").val();
		info["password"] = $("input#password").val();
		info["modelName"] = $("input#modelName").val();
		
		returnFunc(info);		
}

function returnFunc(info) {
		showLoading(info["vWorksheetId"]);
		var returned = $.ajax({
		   	url: "/RequestController", 
		   	type: "POST",
		   	data : info,
		   	dataType : "json",
		   	complete : 
		   		function (xhr, textStatus) {
		   			//alert("RDF Generated!");
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