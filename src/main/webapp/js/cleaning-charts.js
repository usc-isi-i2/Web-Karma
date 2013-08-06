function showChartButtonHandler() {
    var columnHeadingMenu = $("div#columnHeadingDropDownMenu");
    var hNodeId = columnHeadingMenu.data("parentCellId");

    var vWorksheetId = $("td#" + hNodeId).parents("table.WorksheetTable").attr("id");

    var info = new Object();
    var newInfo = [];   // for input parameters
    newInfo.push(getParamObject("vWorksheetId", vWorksheetId ,"vWorksheetId"));
    newInfo.push(getParamObject("hNodeId", hNodeId,"hNodeId"));

    info["newInfo"] = JSON.stringify(newInfo);
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "InvokeCleaningServiceCommand";

    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
        		console.log(xhr.responseText);
                var json = $.parseJSON(xhr.responseText);
                console.log(json);
                parse(json);
            },
        error :
            function (xhr, textStatus) {
                $.sticky("Error occured while renaming column!");
            }
    });
}


function drawChart(element)  {
	var divId = "#" + element["hNodeId"];
	//console.log(divId);
	var margin = {top: 0, right: 0, bottom: 0, left: 0},
	 w = 100 - margin.left - margin.right,
	h = 30 - margin.top - margin.bottom;
	
	var barPadding = 5;
	var yaxispadding = 30;
	var xPadding = 10;
	var yPadding = 2;
			
	var dataArray = eval(element["chartData"].histogram);
	//console.log(dataArray);
	var xLabel = element["chartData"].xLabel;
	var yLabel = element["chartData"].yLabel;
	var containsInvalid = (dataArray[dataArray.length-1][0].toUpperCase() == "INVALID".toUpperCase());
	var containsRemaining = (dataArray[dataArray.length-1][0].toUpperCase() == "Remaining".toUpperCase());
	var containsMissing = (dataArray[dataArray.length-2][0].toUpperCase() == "MISSING".toUpperCase()) 
							||(dataArray[dataArray.length-1][0].toUpperCase() == "MISSING".toUpperCase());
	
	var counters = [];
	
	for (i=0; i<dataArray.length; i++)
	{
		count = dataArray[i][1].split(":");
		num = parseInt(count[0], 10);
		counters.push(num);
	}
	
	// Scale : X axis
	var xScale = d3.scale.ordinal()
					.rangeRoundBands([0, w], .1);
	
	// Scale : Y axis
	var yScale = d3.scale.linear()
	          			.domain([0, d3.max(counters, function(d) { return d; })])
	          			.range([ yPadding, h-yPadding ]);
	
	var yScaleinverted = d3.scale.linear()
							    .domain([0, d3.max(counters, function(d) { return d; })])
	   						.range([ h, 0]);
	
	var formatPercent = d3.format("d");
	
	// Define Y axis
	/*var yAxis = d3.svg.axis()
				  //.scale(yScaleinverted)
				  .orient("left");*/
	
	// Define X axis
	var xAxis = d3.svg.axis()
			    .scale(xScale)
			    .orient("bottom");
	
	//Create SVG element
	//$("div" + divId).append("<br>");
	
	var br = d3.select(divId) 
			   .append("br");
	var svg = d3.select(divId)
				.append("svg")
				.attr("width", w  + margin.left + margin.right)
				.attr("height", h + margin.top + margin.bottom)
				.attr("style", "background: #00174D")
				.attr("id", "smallChart")
			.append("g")
				.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
	// Bars
	svg.selectAll("rect")
	.data(counters)
	.enter()
	.append("rect")
	.attr("class", function(d, i) {
		if (i == counters.length -1 && (containsInvalid || containsRemaining))
			return "cleaningRectInvalid";
		if ((i == counters.length -2  && containsMissing && (containsInvalid || containsRemaining)) || ( i == counters.length -1 && containsMissing))
			return "cleaningRectMissing";
		return "cleaningRectDefault";
		})
	.attr("x", function(d, i) {
				return i * ((w-xPadding) / counters.length) + xPadding;
			})
	.attr("y", function(d, i) {
						return h - yScale(d);
					})
	.attr("width", (w-xPadding) / counters.length - barPadding)
	.attr("height",	function(d, i) {
				return yScale(d);
			})
	/*.attr("fill", function(d, i) {
		if (i == counters.length -1 && containsInvalid)
			return "orangered";
		if ((i == counters.length -2  && containsMissing && containsInvalid) || ( i == counters.length -1 && containsMissing))
			return "Gray";
		return "slateblue";
	})*/
	;
				
	//Text
	/*svg.selectAll("text")
	.data(dataArray)
	.enter()
	.append("text")
	.text(function(d) {
				return d[0];
			})
	.attr("text-anchor", "middle")
	.attr("x", function(d, i) {
				return i * ((w-xPadding) / counters.length)
						+ ((w-xPadding) / counters.length - barPadding) / 2 + xPadding;
			})
	.attr("y", function(d) {
			return h + margin.top +1 ;
	})
	.attr("font-family", "sans-serif")
	.attr("font-size", "8px")
	.attr("class", "xaxisText")
	.attr("fill", "black");*/
		
	/*//Create Y axis
	svg.append("g")
	    .attr("class", "axis")
	    .attr("transform", "translate(" + (yaxispadding) + ",0)")
	    //.call(yAxis)
    .append("text")
        .attr("transform", "rotate(-90)")
        .attr("y", 6)
        .attr("dy", ".71em")
        .attr("x", 10)
        .style("text-anchor", "end")
        .style("font-size","13")
        //.text(yLabel);
        ;*/

	//Create X axis
	/*svg.append("g")
		.attr("class", "axis")
		.attr("transform", "translate(" + xPadding +"," + (h) + ")")
		.call(xAxis)
	.append("text")
	      .attr("transform", "rotate(0)")
	      .attr("x", w/2)
	      .attr("dx", ".71em")
	      .attr("y", 23)
	      .style("text-anchor", "end")
	      .style("font-size","10px")
	      .text(xLabel);*/
}