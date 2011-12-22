function showMapViewForWorksheet() {
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["vWorksheetId"] = $(this).data("worksheetId");
	info["command"] = "PublishKMLLayerCommand";

	var returned = $.ajax({
	   	url: "/RequestController", 
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	   			console.log(xhr.responseText);
	   			showKML(info["vWorksheetId"]); 
		   	},
		error :
			function (xhr, textStatus) {
	   			alert("Error occured creating KML Layer for the source");
		   	}		   
	});
}

function showKML(worksheetId) {
	var worksheetPanel = $("div#"+worksheetId);
	console.log(worksheetId); 
	var mapPanel = $("<div>").addClass("mapViewPanel").width(600).height(600).attr("id","map_canvas" + worksheetId);
	console.log($("div.WorksheetTitleDiv", worksheetPanel).length);
	mapPanel.insertAfter($("div.WorksheetTitleDiv", worksheetPanel));
	
	var myOptions = {
		zoom: 8,
      	center: new google.maps.LatLng(31.408422, -102.316512),
      	mapTypeId: google.maps.MapTypeId.ROADMAP
	};
  	var map = new google.maps.Map(document.getElementById('map_canvas' + worksheetId), myOptions);
    
    var gml = new EGeoXml("gml",map,"./KML/test.kml",{});
	gml.parse();
}
