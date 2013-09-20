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

function handlePrevNextLink() {
	if($(this).hasClass("inactiveLink"))
		return;
	// Prepare the data to be sent to the server	
	var info = new Object();
	var worksheetId = $(this).data("worksheetId");
	info["tableId"] = $(this).parents("div.pager").data("tableId");
	info["direction"] = $(this).data("direction");
	info["worksheetId"] = worksheetId;
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "TablePagerCommand";
		
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
	// var worksheetId = $(this).data("worksheetId");
	// info["newPageSize"] = $(this).data("rowCount");
	// info["tableId"] = $(this).parents("div.pager").data("tableId");
	// info["worksheetId"] = worksheetId;
	// info["workspaceId"] = $.workspaceGlobalInformation.id;
	// info["command"] = "TablePagerResizeCommand";
// 		
	// var returned = $.ajax({
	   	// url: "RequestController", 
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