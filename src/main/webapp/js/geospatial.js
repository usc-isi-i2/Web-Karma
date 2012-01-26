function showMapViewForWorksheet() {
	var state = $(this).data("state");
	var worksheetId = $(this).data("worksheetId");
	var worksheetPanel = $("div#"+worksheetId);
	
	// Change from table to map if current state is table
	if(state == "table") {
		var info = new Object();
		info["workspaceId"] = $.workspaceGlobalInformation.id;
		info["vWorksheetId"] = worksheetId;
		info["command"] = "PublishKMLLayerCommand";
	
		var returned = $.ajax({
		   	url: "/RequestController", 
		   	type: "POST",
		   	data : info,
		   	dataType : "json",
		   	complete : 
		   		function (xhr, textStatus) {
		   			console.log(xhr.responseText);
		   			var json = $.parseJSON(xhr.responseText);
		   			if(json["elements"][0]["updateType"] == "PublishKMLError") {
		   				alert(json["elements"][0]["Error"]);
		   			} else if(json["elements"][0]["updateType"] == "PublishKMLUpdate") {
		   				var fileName = json["elements"][0]["fileName"];
		   				displayKMLandToggleIcon(worksheetId, fileName, worksheetPanel);
		   			}
			   	},
			error :
				function (xhr, textStatus) {
		   			alert("Error occured creating KML Layer for the source");
			   	}
		});
	} else if (state == "map") {	// Change from map to table
		// Remove the map panel
		$("div.mapViewPanel", worksheetPanel).remove();
		$("div#"+worksheetId + "TableDiv", worksheetPanel).show();
		$(this).data("state", "table").qtip({
		   content: {
		      text: 'View as map'
		   }
		});
		$("div.toggleMapView img", worksheetPanel).attr("src","../images/google-earth-32.png")
	}
}

function DownloadKMLControl(controlDiv, map, fileName) {

  	// Set CSS styles for the DIV containing the control
  	// Setting padding to 5 px will offset the control
  	// from the edge of the map.
  	controlDiv.style.padding = '5px';

  	// Set CSS for the control border.
  	var controlUI = document.createElement('DIV');
  	controlUI.style.backgroundColor = 'white';
  	controlUI.style.borderStyle = 'solid';
  	controlUI.style.borderWidth = '1px';
  	controlUI.style.cursor = 'pointer';
  	controlUI.style.textAlign = 'center';
  	controlUI.title = 'Click to download the KML';
  	controlDiv.appendChild(controlUI);

	// Set CSS for the control interior.
	var controlText = document.createElement('DIV');
	controlText.style.fontFamily = 'Arial,sans-serif';
	controlText.style.fontSize = '12px';
	controlText.style.paddingLeft = '4px';
	controlText.style.paddingRight = '4px';
	controlText.style.paddingTop = '1.5px';
	controlText.style.paddingBottom = '1.5px';
	controlText.innerHTML = 'Download KML';
	controlUI.appendChild(controlText);
	
	// Setup the click event listeners: simply set the map to Chicago.
	google.maps.event.addDomListener(controlUI, 'click', function() {
		window.open(fileName);
	});
}

function displayKMLandToggleIcon(worksheetId, fileName, worksheetPanel) {
	var mapPanel = $("<div>").addClass("mapViewPanel").width(800).height(650).attr("id","map_canvas_" + worksheetId);
	mapPanel.insertAfter($("div.WorksheetTitleDiv", worksheetPanel));
	
	var myOptions = {
		zoom: 8,
      	center: new google.maps.LatLng(31.408422, -102.316512),
      	mapTypeId: google.maps.MapTypeId.ROADMAP
	};
  	var map = new google.maps.Map(document.getElementById('map_canvas_' + worksheetId), myOptions);
  	var layer = new google.maps.KmlLayer(fileName);
	layer.setMap(map);
	
	// Create the DIV to hold the control and call the DownloadKMLControl() constructor
	// passing in this DIV.
  	var kmlDownloadDiv = document.createElement('DIV');
  	var kmlDownloadControl = new DownloadKMLControl(kmlDownloadDiv, map, fileName);

  	kmlDownloadDiv.index = 1;
  	map.controls[google.maps.ControlPosition.TOP_RIGHT].push(kmlDownloadDiv);
	
	$("div#"+worksheetId + "TableDiv").hide();
	
	// Toggle the icon and state of the div
	var iconDiv = $("div.toggleMapView", worksheetPanel);	
	$("img", iconDiv).attr("src","../images/table.png")
	$(iconDiv).data("state", "map");
	
	// Change the tooltip
	$(iconDiv).qtip({
	   content: {
	      text: 'View as table'
	   }, style: {
	      classes: 'ui-tooltip-light ui-tooltip-shadow'
	   }
	});
}
