function handlePrevNextLink() {
	if($(this).hasClass("inactiveLink"))
		return;
	// Prepare the data to be sent to the server	
	var info = new Object();
	var worksheetId = $(this).data("vWorksheetId");
	info["tableId"] = $(this).parents("div.pager").data("tableId");
	info["direction"] = $(this).data("direction");
	info["vWorksheetId"] = worksheetId;
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "TablePagerCommand";
		
	var returned = $.ajax({
	   	url: "/RequestController", 
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	   			//alert(xhr.responseText);
	    		var json = $.parseJSON(xhr.responseText);
	    		parse(json);
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured with fetching new rows! " + textStatus);
		   	}		   
	});
	return false;
	$(this).preventDefault();
}

function handlePagerResize() {
	$.sticky("Under construction!");
	// if($(this).hasClass("pagerSizeSelected"))
		// return;
// 		
	// // $(this).siblings().removeClass("pagerSizeSelected");	
	// // $(this).addClass("pagerSizeSelected");	
// 	
	// // Prepare the data to be sent to the server	
	// var info = new Object();
// 	
	// var worksheetId = $(this).data("vWorksheetId");
	// info["newPageSize"] = $(this).data("rowCount");
	// info["tableId"] = $(this).parents("div.pager").data("tableId");
	// info["vWorksheetId"] = worksheetId;
	// info["workspaceId"] = $.workspaceGlobalInformation.id;
	// info["command"] = "TablePagerResizeCommand";
// 		
	// var returned = $.ajax({
	   	// url: "/RequestController", 
	   	// type: "POST",
	   	// data : info,
	   	// dataType : "json",
	   	// complete : 
	   		// function (xhr, textStatus) {
	   			// //alert(xhr.responseText);
	    		// var json = $.parseJSON(xhr.responseText);
	    		// parse(json);
		   	// },
		// error :
			// function (xhr, textStatus) {
	   			// alert("Error occured with fetching new rows! " + textStatus);
// 	   			
		   	// }		   
	// });
	// return false;
	// $(this).preventDefault();
}

function changePagerOptions(pagerJSONElement, pagerDOMElement) {
	// Store the table Id information
	$(pagerDOMElement).data("tableId", pagerJSONElement["tableId"]);
	
	// Change the ___ of ___ rows information
	var totalRows = pagerJSONElement["numRecordsShown"] + pagerJSONElement["numRecordsBefore"] 
								+ pagerJSONElement["numRecordsAfter"];  
	var currentRowInfo = "" + (pagerJSONElement["numRecordsBefore"] +1) + " - " + 
				(pagerJSONElement["numRecordsShown"] + pagerJSONElement["numRecordsBefore"]);
	$("span.previousNextText", pagerDOMElement).text(currentRowInfo + " of " + totalRows);
	
	// Make the Previous link active/inactive as required
	if(pagerJSONElement["numRecordsBefore"] != 0) {
		var previousLink = $("a", pagerDOMElement)[3]; 
		if($(previousLink).hasClass("inactiveLink"))
			$(previousLink).removeClass("inactiveLink").addClass("activeLink");
	} else {
		if($(previousLink).hasClass("activeLink"))
			$(previousLink).removeClass("activeLink").addClass("inactiveLink");
	}
	
	// Make the Next link active/inactive as required
	if(pagerJSONElement["numRecordsAfter"] != 0){
		var nextLink = $("a", pagerDOMElement)[4];
		if($(nextLink).hasClass("inactiveLink"))
			$(nextLink).removeClass("inactiveLink").addClass("activeLink");
	} else {
		if($(nextLink).hasClass("activeLink"))
			$(nextLink).removeClass("activeLink").addClass("inactiveLink");
	}
	
	// Select the correct pager resize links
	$.each($("a.pagerResizeLink", pagerDOMElement), function(index, link) {
		if($(link).data("rowCount") == pagerJSONElement["desiredNumRecordsShown"]){
			$(link).addClass("pagerSizeSelected");
		} else {
			$(link).removeClass("pagerSizeSelected");
		}
	});
	
	return true;
}

function showNestedTablePager() {
	// Get the parent table
	var tableId = $(this).parents("table").attr("id");
	var nestedTablePager = $("#nestedTablePager" + tableId);
	
	var pagerElem = $(this).data("pagerElem");
	changePagerOptions(pagerElem, nestedTablePager);
	
	nestedTablePager.css({"position":"absolute",
    					"top":$(this).offset().top + 10, 
    					"left": $(this).offset().left + $(this).width()/2 - nestedTablePager.width()/2}).show();
}

function hideNestedTablePager() {
	// Get the parent table
	var table = $(this).parents("table");
	var nestedTablePager = $("#nestedTablePager" + table.attr("id"));
	nestedTablePager.hide();
}