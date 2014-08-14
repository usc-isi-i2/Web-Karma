/*******************************************************************************
 * Copyright 2012 University of Southern California
 *
 ******************************************************************************/

function showMapViewForWorksheet() {
	var state = $(this).data("state");
	var worksheetId = $(this).data("worksheetId");
	var worksheetPanel = $("div#" + worksheetId);

	// Change from table to map if current state is table
	if (state == "table") {
		var info = generateInfoObject(worksheetId, "", "PublishKMLLayerCommand");
		showLoading(worksheetId);
		var returned = $.ajax({
			url: "RequestController",
			type: "POST",
			data: info,
			dataType: "json",
			complete: function(xhr, textStatus) {
				// console.log(xhr.responseText);
				var json = $.parseJSON(xhr.responseText);
				if (json["elements"][0]["updateType"] == "KarmaError") {
					hideLoading(worksheetId);
					$.sticky(json["elements"][0]["Error"]);

					//google.setOnLoadCallback(displayKMLOnGoogleEarth(worksheetId, "", worksheetPanel));
				} else if (json["elements"][0]["updateType"] == "PublishKMLUpdate") {
					// google.setOnLoadCallback(displayKMLOnGoogleEarth(worksheetId, fileName, worksheetPanel));
					// displayKMLandToggleIcon(worksheetId, fileName, worksheetPanel);
					hideLoading(worksheetId);

					if (json["elements"][0]["transferSuccessful"]) {
						var fileName = json["elements"][0]["fileName"];
						$("div#tablesWorkspace").data("currentKMLFile", fileName);
						var mapPanel = $("<div>").addClass("mapViewPanel").width(800).height(650).attr("id", "map_canvas_" + worksheetId);
						var tableDiv = $("div.table-data-container", worksheetPanel);
						mapPanel.insertAfter(tableDiv);

						google.earth.createInstance('map_canvas_' + worksheetId, initCB, failureCB);
						$("div#svgDiv_" + worksheetId, worksheetPanel).hide();
						$("div.table-container", worksheetPanel).hide();

						tableDiv.hide();
						// Toggle the icon and state of the div
						var iconDiv = $("div.toggleMapView", worksheetPanel);
						$("img", iconDiv).attr("src", "images/table.png")
						$(iconDiv).data("state", "map");

						// Change the tooltip
						$(iconDiv).qtip({
							content: {
								text: 'View as table'
							},
							style: {
								classes: 'ui-tooltip-light ui-tooltip-shadow'
							}
						});
					}

					// Show the KML download link
					var titleDiv = $("div#" + worksheetId + " div.WorksheetTitleDiv");
					// Remove existing link if any
					$("a.KMLDownloadLink", titleDiv).remove();
					var downloadLink = $("<a>").attr("href", json["elements"][0]["localFileName"]).text("SPATIAL DATA").addClass("KMLDownloadLink DownloadLink").attr("target", "_blank");
					$("div#WorksheetOptionsDiv", titleDiv).after(downloadLink);
				}
			},
			error: function(xhr, textStatus) {
				alert("Error occured creating KML Layer for the source");
			}
		});
	} else if (state == "map") { // Change from map to table
		// Remove the map panel
		$("div.mapViewPanel", worksheetPanel).remove();
		$("div#svgDiv_" + worksheetId, worksheetPanel).show();
		$("div.table-container", worksheetPanel).show();
		$("div.table-data-container", worksheetPanel).show();
		$("div#" + worksheetId + "TableDiv", worksheetPanel).show();
		$(this).data("state", "table").qtip({
			content: {
				text: 'View as map'
			}
		});
		$("div.toggleMapView img", worksheetPanel).attr("src", "images/google-earth-32.png")
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
	var mapPanel = $("<div>").addClass("mapViewPanel").width(800).height(650).attr("id", "map_canvas_" + worksheetId);
	var tableDiv = $("div#" + worksheetId + "TableDiv", worksheetPanel);
	mapPanel.insertAfter(tableDiv);

	var myOptions = {
		zoom: 8,
		center: new google.maps.LatLng(31.408422, -102.316512),
		mapTypeId: google.maps.MapTypeId.ROADMAP
	};
	var map = new google.maps.Map(document.getElementById('map_canvas_' + worksheetId), myOptions);
	var filePath = fileName + "?rand=" + (new Date()).valueOf();
	var layer = new google.maps.KmlLayer(filePath);
	// console.log(filePath)
	layer.setMap(map);

	// Create the DIV to hold the control and call the DownloadKMLControl() constructor
	// passing in this DIV.
	var kmlDownloadDiv = document.createElement('DIV');
	var kmlDownloadControl = new DownloadKMLControl(kmlDownloadDiv, map, fileName + "?rand=" + (new Date()).valueOf());

	kmlDownloadDiv.index = 1;
	map.controls[google.maps.ControlPosition.TOP_RIGHT].push(kmlDownloadDiv);

	tableDiv.hide();

	// Toggle the icon and state of the div
	var iconDiv = $("div.toggleMapView", worksheetPanel);
	$("img", iconDiv).attr("src", "images/table.png")
	$(iconDiv).data("state", "map");

	// Change the tooltip
	$(iconDiv).qtip({
		content: {
			text: 'View as table'
		},
		style: {
			classes: 'ui-tooltip-light ui-tooltip-shadow'
		}
	});
}

function displayKMLOnGoogleEarth(worksheetId, fileName, worksheetPanel) {
	// Create the canvas for map
	var mapPanel = $("<div>").addClass("mapViewPanel").width(800).height(650).attr("id", "map_canvas_" + worksheetId);
	var tableDiv = $("div#" + worksheetId + "TableDiv", worksheetPanel);
	mapPanel.insertAfter(tableDiv);

	google.earth.createInstance("map_canvas_" + worksheetId, initCB, failureCB);

	tableDiv.hide();

	// Toggle the icon and state of the div
	var iconDiv = $("div.toggleMapView", worksheetPanel);
	$("img", iconDiv).attr("src", "images/table.png")
	$(iconDiv).data("state", "map");

	// Change the tooltip
	$(iconDiv).qtip({
		content: {
			text: 'View as table'
		},
		style: {
			classes: 'ui-tooltip-light ui-tooltip-shadow'
		}
	});
}

function initCB(instance) {
	ge = instance;
	ge.getWindow().setVisibility(true);

	// save these lines for using networkLink in GE plugin
	//var link = ge.createLink('');
	//var href = $("div#tablesWorkspace").data("currentKMLFile");
	//link.setHref(href);

	//var networkLink = ge.createNetworkLink('');
	//networkLink.set(link, true, true); // Sets the link, refreshVisibility, and flyToView

	var networkLink = ge.parseKml($("div#tablesWorkspace").data("currentKMLFile"));
	//networkLink.set(link, true, true); // Sets the link, refreshVisibility, and flyToView

	ge.getFeatures().appendChild(networkLink);

	if (networkLink.getAbstractView()) {
		ge.getView().setAbstractView(networkLink.getAbstractView());
	}
}

function failureCB(errorCode) {
	alert("Error loading Google Earth: " + errorCode);
}