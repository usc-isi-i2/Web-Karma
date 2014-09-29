Window.cleaningStore = {};

function showChartButtonHandler() {
	var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
	var hNodeId = columnHeadingMenu.data("parentCellId");


	var worksheetId = $("td#" + hNodeId).parents("div.Worksheet").attr("id");
	showChartForColumn(worksheetId, hNodeId);
}

function showChartForColumn(worksheetId, hNodeId) {

	var info = generateInfoObject(worksheetId, hNodeId, "InvokeCleaningServiceCommand");
	var newInfo = info['newInfo']; // for input parameters
	info["newInfo"] = JSON.stringify(newInfo);
	var returned = sendRequest(info, worksheetId);
}

function drawChart(element) {

	var divId = "#" + element["hNodeId"];
	Window.cleaningStore[element["hNodeId"]] = element;
	var margin = {
			top: 0,
			right: 0,
			bottom: 0,
			left: 0
		},
		//	 w = 100 - margin.left - margin.right,
		w = $(divId).width(),
		h = 30 - margin.top - margin.bottom;

	// Check w for maximum width
	w = (w > 150) ? 150 : w;

	var barPadding = 5;
	var yaxispadding = 10;
	var xPadding = 10;
	var yPadding = 2;

	
	if (element["chartData"]) {
	} else {
		return;
	}
	//console.log("histogram:-" + element["chartData"].histogram);
	if (element["chartData"].histogram == null)
		return;
	var dataArray = eval(element["chartData"].histogram);
	//console.log("dataArray:-" + dataArray); 
	var xLabel = element["chartData"].xLabel;
	var yLabel = element["chartData"].yLabel;
	/*var tooltip = "Data Type Detected: " + element["chartData"].Category  + "\nTotal Data: " + element["chartData"].Total_ID_Count + 
	"\nTotal Valid Data: " + element["chartData"].Valid_ID_Count
	+ "\nTotal Invalid Data: "	+ element["chartData"].Invalid_ID_Count;*/
	var tooltip = "Data Type Detected: " + element["chartData"].xLabel + "\nTotal Data: " + element["chartData"].Total_ID_Count +
		"\nTotal Valid Data: " + element["chartData"].Valid_ID_Count + "\nTotal Invalid Data: " + element["chartData"].Invalid_ID_Count;
	var containsInvalid = false;
	if (dataArray.length > 0)
		containsInvalid = (dataArray[dataArray.length - 1].Value.toUpperCase() == "INVALID".toUpperCase());
	if (containsInvalid == false && dataArray.length >= 2)
		containsInvalid = (dataArray[dataArray.length - 2].Value.toUpperCase() == "INVALID".toUpperCase());
	var containsRemaining = false;
	if (dataArray.length > 0)
		containsRemaining = (dataArray[dataArray.length - 1].Value.toUpperCase() == "Remaining".toUpperCase());
	if (containsRemaining == false && dataArray.length > 1)
		containsRemaining = (dataArray[dataArray.length - 2].Value.toUpperCase() == "Remaining".toUpperCase());
	if (containsRemaining == false && dataArray.length > 2)
		containsRemaining = (dataArray[dataArray.length - 3].Value.toUpperCase() == "Remaining".toUpperCase());

	var containsMissing = false;
	if (dataArray.length > 1)
		containsMissing = (dataArray[dataArray.length - 2].Value.toUpperCase() == "MISSING".toUpperCase()) || (dataArray[dataArray.length - 1].Value.toUpperCase() == "MISSING".toUpperCase());
	if (containsMissing == false && dataArray.length > 0)
		containsMissing = (dataArray[dataArray.length - 1].Value.toUpperCase() == "MISSING".toUpperCase());

	var counters = [];

	for (i = 0; i < dataArray.length; i++) {
		num = parseInt(dataArray[i].Frequency, 10);
		counters.push(num);
	}

	// Scale : X axis
	var xScale = d3.scale.ordinal()
		.rangeRoundBands([0, w], .1);

	// Scale : Y axis
	var yScale = d3.scale.linear()
		.domain([0, d3.max(counters, function(d) {
			return d;
		})])
		.range([yPadding, h - yPadding]);

	var yScaleinverted = d3.scale.linear()
		.domain([0, d3.max(counters, function(d) {
			return d;
		})])
		.range([h, 0]);

	var formatPercent = d3.format("d");

	// Define X axis
	var xAxis = d3.svg.axis()
		.scale(xScale)
		.orient("bottom");

	var br1 = d3.select(divId)
		.select("br")
		.remove();
	var svg1 = d3.select(divId)
		.select("#smallChart")
		.remove();

	var br = d3.select(divId)
		.append("br");

	var svg = d3.select(divId)
		.append("svg")
		.attr("width", w + margin.left + margin.right)
		.attr("height", h + margin.top + margin.bottom)
		.attr("class", "smallChart")
		.append("g")
		.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
	// Bars
	svg.selectAll("rect")
		.data(counters)
		.enter()
		.append("rect")
		.attr("class", function(d, i) {
			if ((i == counters.length - 1 && !containsInvalid && containsRemaining) || (i == counters.length - 2 && containsInvalid && containsRemaining) || (i == counters.length - 2 && containsMissing && containsRemaining) || (i == counters.length - 3 && containsInvalid && containsMissing && containsRemaining)) {
				containsRemaining = false;
				return "cleaningRectRemaining";
			}
			if (i == counters.length - 1 && containsInvalid)
				return "cleaningRectInvalid";
			if ((i == counters.length - 2 && containsMissing && containsInvalid) || (i == counters.length - 1 && containsMissing))
				return "cleaningRectMissing";
			return "cleaningRectDefault";
		})
		.attr("x", function(d, i) {
			return i * ((w - xPadding) / counters.length) + xPadding;
		})
		.attr("y", function(d, i) {
			return h - yScale(d);
		})
		.attr("width", (w - xPadding) / counters.length - barPadding)
		.attr("height", function(d, i) {
			return yScale(d);
		});
	svg.append("title")
		.text(tooltip);
}

function drawBigChart(pid) {
	var divId = "#drawBigChartId";
	var margin = {
			top: 10,
			right: 20,
			bottom: 35,
			left: 20
		},
		w = 500 - margin.left - margin.right,
		h = 300 - margin.top - margin.bottom;

	var barPadding = 5;
	var yaxispadding = 20;
	var xPadding = 20;
	var yPadding = 2;
	element = Window.cleaningStore[pid];
	if (element["chartData"].histogram == null)
		return;
	var dataArray = eval(element["chartData"].histogram);
	var xLabel = element["chartData"].xLabel;
	var yLabel = element["chartData"].yLabel;
	var containsInvalid = false;
	if (dataArray.length > 0)
		containsInvalid = (dataArray[dataArray.length - 1].Value.toUpperCase() == "INVALID".toUpperCase());
	if (containsInvalid == false && dataArray.length >= 2)
		containsInvalid = (dataArray[dataArray.length - 2].Value.toUpperCase() == "INVALID".toUpperCase());
	var containsRemaining = false;
	if (dataArray.length > 0)
		containsRemaining = (dataArray[dataArray.length - 1].Value.toUpperCase() == "Remaining".toUpperCase());
	if (containsRemaining == false && dataArray.length > 1)
		containsRemaining = (dataArray[dataArray.length - 2].Value.toUpperCase() == "Remaining".toUpperCase());
	if (containsRemaining == false && dataArray.length > 2)
		containsRemaining = (dataArray[dataArray.length - 3].Value.toUpperCase() == "Remaining".toUpperCase());
	var containsMissing = false;
	if (dataArray.length >= 2)
		containsMissing = (dataArray[dataArray.length - 2].Value.toUpperCase() == "MISSING".toUpperCase()) || (dataArray[dataArray.length - 1].Value.toUpperCase() == "MISSING".toUpperCase());
	if (containsMissing == false && dataArray.length > 0)
		containsMissing = (dataArray[dataArray.length - 1].Value.toUpperCase() == "MISSING".toUpperCase());

	var counters = [];

	for (i = 0; i < dataArray.length; i++) {
		num = parseInt(dataArray[i].Frequency, 10);
		counters.push(num);
	}

	// Scale : X axis
	var xScale = d3.scale.ordinal()
		.rangeRoundBands([0, w], .1);

	// Scale : Y axis
	var yScale = d3.scale.linear()
		.domain([0, d3.max(counters, function(d) {
			return d;
		})])
		.range([yPadding, h - yPadding]);

	var yScaleinverted = d3.scale.linear()
		.domain([0, d3.max(counters, function(d) {
			return d;
		})])
		.range([h, 0]);

	var formatPercent = d3.format("d");

	// Define Y axis
	var yAxis = d3.svg.axis()
		.scale(yScaleinverted)
		.orient("left");

	// Define X axis
	var xAxis = d3.svg.axis()
		.scale(xScale)
		.orient("bottom");

	//Create SVG element
	var svg1 = d3.select(divId)
		.select("svg")
		.remove();

	var svg = d3.select(divId)
		.append("svg")
		.attr("width", w + margin.left + margin.right)
		.attr("height", h + margin.top + margin.bottom)
		.attr("id", "bigChart")
		.append("g")
		.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
	// Bars
	svg.selectAll("rect")
		.data(counters)
		.enter()
		.append("rect")
		.attr("class", function(d, i) {
			if ((i == counters.length - 1 && !containsInvalid && containsRemaining) || (i == counters.length - 2 && containsInvalid && containsRemaining) || (i == counters.length - 2 && containsMissing && containsRemaining) || (i == counters.length - 3 && containsInvalid && containsMissing && containsRemaining)) {
				containsRemaining = false;
				return "cleaningRectRemaining";
			}

			if (i == counters.length - 1 && containsInvalid)
				return "cleaningRectInvalid";
			if ((i == counters.length - 2 && containsMissing && containsInvalid) || (i == counters.length - 1 && containsMissing))
				return "cleaningRectMissing";
			return "cleaningRectDefault";
		})
		.attr("x", function(d, i) {
			return i * ((w - xPadding) / counters.length) + xPadding;
		})
		.attr("y", function(d, i) {
			return h - yScale(d);
		})
		.attr("width", (w - xPadding) / counters.length - barPadding)
		.attr("height", function(d, i) {
			return yScale(d);
		});

	//Text
	svg.selectAll("text")
		.data(dataArray)
		.enter()
		.append("text")
		.text(function(d) {
			// TO CHECK FOR String
			if (xLabel == "Date") {
				if (d.Value.length > 21) {

					d.Value = d.Value.substring(0, 20) + "..";
				}
			} else {
				if (d.Value.length > 9) {

					d.Value = d.Value.substring(0, 8) + "..";
				}
			}
			return d.Value;
		})
		.attr("text-anchor", "middle")
		.attr("font-family", "sans-serif")
		.attr("font-size", "8px")
		.attr("class", "xaxisText")
		.attr("fill", "black")
		.attr("transform", function(d, i) {
			return "translate(" + (i * ((w - xPadding) / counters.length) + ((w - xPadding) / counters.length - barPadding) / 2 + xPadding - 2) + "," + (h + margin.top + 8) + ") rotate(-25)";
		});

	//Create Y axis
	svg.append("g")
		.attr("class", "axis")
		.attr("transform", "translate(" + (yaxispadding) + ",0)")
		.call(yAxis)
		.append("text")
		.attr("transform", "rotate(-90)")
		.attr("y", 6)
		.attr("dy", ".71em")
		.attr("x", 7)
		.text(yLabel);

	//Create X axis
	svg.append("g")
		.attr("class", "axis")
		.attr("transform", "translate(" + xPadding + "," + (h) + ")")
		.call(xAxis);
	/*.append("text")
	      .attr("transform", "rotate(0)")
	      .attr("x", w/2)
	      .attr("dx", ".71em")
	      .attr("y", 32)
	      .text(xLabel);
	*/

	var colID = element.hNodeId;
	var colName = $("#" + colID).children().eq(0).text();
	var chartTitle = "";
	if (xLabel == "String" || xLabel == "Boolean" || xLabel == "DayOfWeek") {
		chartTitle = "Count of '" + colName + "' (Detected as " + xLabel + ")";
	} else {

		chartTitle = "Frequency in the range of '" + colName + "' (Detected as " + xLabel + ")";
		if (element["chartData"].histogram_Colwidth != null)
			chartTitle += " Width of each column: " + element["chartData"].histogram_Colwidth
	}

	//$("#bigChartTitle").text(chartTitle);
	return chartTitle;
}