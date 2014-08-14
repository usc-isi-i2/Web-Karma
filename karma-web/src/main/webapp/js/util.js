function isDialogInitialized(dialog) {
	if (dialog.hasClass("ui-dialog-content"))
		return true;
	return false;
}

function sendRequest(info, worksheetId) {
	$.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			parse(json);
			hideLoading(worksheetId);
		},
		error: function(xhr, textStatus) {
			alert("Error occured with " + info['command'] + );
			hideLoading(worksheetId);
		}
	});
}

function sendRequest(info) {
	$.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			parse(json);
			hideWaitingSignOnScreen();
		},
		error: function(xhr, textStatus) {
			alert("Error occured with " + info['command'] + );
			hideWaitingSignOnScreen();
		}
	});
}

function getColumnHeadings(worksheetId) {
	var columnNames = [];

	var columnNameDivs = $("#" + worksheetId + " div.wk-header a.ColumnTitle");
	$.each(columnNameDivs, function(index, element) {
		columnNames.push($.trim($(element).text()));
	});

	return columnNames;
}

function getColumnHeadings(worksheetId, columnId, commandName) {
	var info = generateInfoObject(worksheetId, columnId, "GetHeadersCommand");
	info["commandName"] = commandName;
	var returnJSON = [];
	$.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		async: false,
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			returnJSON = json['elements'][0];
		}
	});
	return returnJSON;
}

function generateInfoObject(worksheetId, commandName) {
	var info = {};
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["worksheetId"] = worksheetId;
	info["command"] = commandName;
	var newInfo = [];
	newInfo.push(getParamObject("worksheetId", worksheetId, "worksheetId"));
	info["newInfo"] = newInfo;
	return info;
}

function generateInfoObject(worksheetId, columnId, commandName) {
	var info = {};
	info["hNodeId"] = columnId;
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["worksheetId"] = worksheetId;
	info["command"] = commandName;
	var newInfo = [];
	newInfo.push(getParamObject("hNodeId", columnId, "hNodeId"));
	newInfo.push(getParamObject("worksheetId", worksheetId, "worksheetId"));
	info["newInfo"] = newInfo;
	return info;
}

function addLevels(li, a, option, worksheetId) {
	li.addClass("dropdown-submenu");
	a.text(option['name']);
	var subul = $("<ul>").addClass("dropdown-menu");
	var suboptions = option.levels;
	for (var j = 0; j < suboptions.length; j++) {
		var suboption = suboptions[j];
		var needFile = suboption.useFileUpload;
		var li2 = $("<li>");
		var a2 = $("<a>");
		if (needFile) {
			a2.addClass("fileinput-button");
			var form = $("<form>")
				.attr("id", suboption.uploadDiv + "_" + worksheetId)
				.attr("action", "ImportFileCommand")
				.attr("method", "POST")
				.attr("enctype", "multipart/form-data")
				.text(suboption['name']);
			var input = $("<input>")
				.attr("type", "file")
				.attr("name", "files[]");
			form.append(input);
			a2.append(form);
			window.setTimeout(suboption.func, 1000);
		} else if (suboption.addLevel) {
			addLevels(li2, a2, suboption);
		} else {
			a2.text(suboption['name']);
			a2.click(suboption.func);
		}
		a2.css("cursor", "pointer");
		li2.append(a2);
		subul.append(li2);
	}
	li.append(subul);
}

function showLoading(worksheetId) {
	// Remove any existing coverDiv
	$("div#WaitingDiv_" + worksheetId).remove();

	// Create a new cover
	var coverDiv = $("<div>").attr("id", "WaitingDiv_" + worksheetId).addClass('waitingDiv')
		.append($("<div>").html('<b>Please wait</b>')
			.append($('<img>').attr("src", "images/ajax-loader.gif"))
		);

	var spaceToCoverDiv = $("div#" + worksheetId);
	spaceToCoverDiv.append(coverDiv.css({
		"position": "absolute",
		"height": spaceToCoverDiv.height(),
		"width": spaceToCoverDiv.width(),
		"top": spaceToCoverDiv.position().top,
		"left": spaceToCoverDiv.position().left
	}).show());
}

function hideLoading(worksheetId) {
	$("div#WaitingDiv_" + worksheetId).hide();
}

function showWaitingSignOnScreen() {
	var coverDiv = $("<div>").attr("id", "WaitingDiv").addClass('waitingDiv')
		.append($("<div>").html('<b>Please wait</b>')
			.append($('<img>').attr("src", "images/ajax-loader.gif"))
		);

	var spaceToCoverDiv = $('body');
	spaceToCoverDiv.append(coverDiv.css({
		"position": "fixed",
		"height": $(document).height(),
		"width": $(document).width(),
		"zIndex": 100,
		"top": spaceToCoverDiv.position().top,
		"left": spaceToCoverDiv.position().left
	}).show());
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
	$.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		async: false,
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			if (json['elements'] && json['elements'][0]['connectionStatus'] && json['elements'][0]['connectionStatus'] == 1) {
				window.conncetionStat = true;
			}
		},
		error: function(xhr, textStatus) {
			alert("Error occured while testing connection to sparql endpoint!" + textStatus);
		}
	});
	return window.conncetionStat;
}

function getParamObject(name, value, type) {
	var param = new Object();
	param["name"] = name;
	param["value"] = value;
	param["type"] = type;

	return param;
}

function getAllClasses(worksheetId) {
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "GetClassesCommand";
	info["nodesRange"] = "allClasses";
	info["worksheetId"] = worksheetId;

	var result = [];
	$.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		async: false,
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			var data = json.elements[0].nodes;
			$.each(data, function(index, clazz) {
				parseClassJSON(clazz, result);
			});
		},
		error: function(xhr, textStatus) {
			alert("Error occured while fetching classes: " + textStatus);
		}
	});
	sortClassPropertyNodes(result);
	var lastLabel = "";
	var uniques = [];
	$.each(result, function(index, item) {
		if (item.label != lastLabel)
			uniques.push(item);
		lastLabel = item.label;
	});
	return uniques;
}

function getAllClassesRaw(worksheetId) {
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "GetClassesCommand";
	info["nodesRange"] = "allClassesRaw";
	info["worksheetId"] = worksheetId;

	var result = [];
	$.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		async: false,
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			var data = json.elements[0].nodes;
			$.each(data, function(index, clazz) {
				parseClassJSON(clazz, result);
			});
		},
		error: function(xhr, textStatus) {
			alert("Error occured while fetching classes: " + textStatus);
		}
	});
	sortClassPropertyNodes(result);
	var lastLabel = "";
	var uniques = [];
	$.each(result, function(index, item) {
		if (item.label != lastLabel)
			uniques.push(item);
		lastLabel = item.label;
	});
	return uniques;
}

function getAllClassesForProperty(worksheetId, propertyUri) {
	if (propertyUri == null || propertyUri == "")
		return [];

	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "GetClassesCommand";
	info["nodesRange"] = "classesWithProperty";
	info["worksheetId"] = worksheetId;
	info["propertyURI"] = propertyUri;

	var result = [];
	$.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		async: false,
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			var data = json.elements[0].nodes;
			$.each(data, function(index, clazz) {
				parseClassJSON(clazz, result);
			});
		},
		error: function(xhr, textStatus) {
			alert("Error occured while fetching classes for property " + property.label + ": " + textStatus);
		}
	});
	sortClassPropertyNodes(result);
	var lastLabel = "";
	var uniques = [];
	$.each(result, function(index, item) {
		if (item.label != lastLabel)
			uniques.push(item);
		lastLabel = item.label;
	});
	return uniques;
}

function getClassesInModel(worksheetId) {
	var info = new Object();
	info["command"] = "GetClassesCommand";
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["worksheetId"] = worksheetId;
	info["nodesRange"] = "classesInModel";
	var result = [];
	$.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		async: false,
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			var data = json.elements[0].nodes;
			$.each(data, function(index, clazz) {
				parseClassJSON(clazz, result);
			});
		},
		error: function(xhr, textStatus) {
			alert("Error occured while getting nodes list!");
		}
	});
	sortClassPropertyNodes(result);
	return result;
}


function parseClassJSON(clazz, result, allLabels) {
	var uri = clazz.nodeUri;
	var id = clazz.nodeId;
	var label = clazz.nodeLabel;

	var node = {
		"label": label,
		"id": id,
		"uri": uri
	};
	result.push(node);
	//	if(clazz.children) {
	//		$.each(clazz.children, function(index, clazzChild){
	//        	parseClassJSON(clazzChild, result, allLabels);
	//        });
	//	}
}

function getAllDataProperties(worksheetId) {
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "GetPropertiesCommand";
	info["propertiesRange"] = "allDataProperties";
	info["worksheetId"] = worksheetId;

	var result = [];
	$.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		async: false,
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			var data = json.elements[0].properties;
			$.each(data, function(index, prop) {
				parsePropertyJSON(prop, result);
			});
		},
		error: function(xhr, textStatus) {
			alert("Error occured while fetching properties: " + textStatus);
		}
	});
	sortClassPropertyNodes(result);
	return result;
}

function getAllObjectProperties(worksheetId) {
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "GetPropertiesCommand";
	info["propertiesRange"] = "allObjectProperties";
	info["worksheetId"] = worksheetId;

	var result = [];
	$.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		async: false,
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			var data = json.elements[0].properties;
			$.each(data, function(index, prop) {
				parsePropertyJSON(prop, result);
			});
		},
		error: function(xhr, textStatus) {
			alert("Error occured while fetching properties: " + textStatus);
		}
	});
	sortClassPropertyNodes(result);
	return result;
}

function getAllExistingProperties(worksheetId) {
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "GetPropertiesCommand";
	info["propertiesRange"] = "existingProperties";
	info["worksheetId"] = worksheetId;

	var result = [];
	$.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		async: false,
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			var data = json.elements[0].properties;
			$.each(data, function(index, prop) {
				parsePropertyJSON(prop, result);
			});
		},
		error: function(xhr, textStatus) {
			alert("Error occured while fetching properties: " + textStatus);
		}
	});
	sortClassPropertyNodes(result);
	return result;
}

function getAllPropertiesForClass(worksheetId, classUri) {
	if (classUri == null || classUri == "" || classUri == "fakeDomainURI")
		return [];

	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "GetPropertiesCommand";
	info["propertiesRange"] = "dataPropertiesForClass";
	info["classURI"] = classUri;
	info["worksheetId"] = worksheetId;

	var result = [];
	$.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		async: false,
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			var data = json.elements[0].properties;
			$.each(data, function(index, prop) {
				parsePropertyJSON(prop, result);
			});
		},
		error: function(xhr, textStatus) {
			alert("Error occured while fetching properties: " + textStatus);
		}
	});
	sortClassPropertyNodes(result);
	return result;
}

function getAllPropertiesForDomainRange(worksheetId, domainUri, rangeUri) {
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "GetPropertiesCommand";
	info["propertiesRange"] = "propertiesWithDomainRange";
	info["domainURI"] = domainUri;
	info["rangeURI"] = rangeUri;
	info["worksheetId"] = worksheetId;

	var result = [];
	$.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		async: false,
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			var data = json.elements[0].properties;
			$.each(data, function(index, prop) {
				parsePropertyJSON(prop, result);
			});
		},
		error: function(xhr, textStatus) {
			alert("Error occured while fetching properties: " + textStatus);
		}
	});
	sortClassPropertyNodes(result);
	return result;
}

function parsePropertyJSON(prop, result) {
	var node = {
		"label": prop.label,
		"id": prop.id,
		"uri": prop.uri
	};
	result.push(node);
	//	if(prop.children) {
	//		$.each(prop.children, function(index, propChild){
	//			parsePropertyJSON(propChild, result);
	//        });
	//	}
}

function sortClassPropertyNodes(nodes) {
	nodes.sort(function(a, b) {
		var label1 = a.label;
		if (label1.indexOf(":") == -1)
			label1 = a.uri + "/" + label1;
		var label2 = b.label;
		if (label2.indexOf(":") == -1)
			label2 = b.uri + "/" + label2;

		return label1.toUpperCase().localeCompare(label2.toUpperCase());
	});
}

function getAllLinksForNode(worksheetId, alignmentId, nodeId) {
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "GetCurrentLinksOfInternalNodeCommand";
	info["worksheetId"] = worksheetId;
	info["alignmentId"] = alignmentId;
	info["nodeId"] = nodeId;
	var result = [];
	$.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		async: false,
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			$.each(json["elements"], function(index, element) {
				if (element["updateType"] == "GetCurrentLinks") {
					$.each(element["edges"], function(index2, node) {
						var source = {
							"id": node["edgeSourceId"],
							"label": node["edgeSource"],
							"uri": node["edgeSourceUri"]
						};
						var target = {
							"id": node["edgeTargetId"],
							"label": node["edgeTarget"],
							"uri": node["edgeTargetUri"]
						};
						var prop = {
							"id": node["edgeId"],
							"label": node["edgeLabel"]
						};
						var link = {
							"type": node["direction"],
							"source": source,
							"target": target,
							"property": prop
						};

						result.push(link);
					});
				}
			});
		},
		error: function(xhr, textStatus) {
			alert("Error occured while getting nodes list!");
		}
	});
	return result;
}

function changeKarmaHome(homeDir) {
	var info = new Object();
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["command"] = "SetKarmaHomeCommand";
	info["directory"] = homeDir;
	var result = [];
	$.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		async: false,
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			parse(json);
		},
		error: function(xhr, textStatus) {
			alert("Error occured while setting Karma Home Directory!");
		}
	});
	return result;
}

//Make All Modal Dialogs Resizeable
$(".modal-dialog").resizable({
	handles: "e, w"
});
$(".modal-dialog").draggable({
	handle: ".modal-header"
});