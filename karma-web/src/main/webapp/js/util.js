function isDialogInitialized(dialog) {
	if (dialog.hasClass("ui-dialog-content"))
		return true;
	return false;
}

function refreshRows(wsId) {
		var info = generateInfoObject(wsId, "", "RefreshSuperSelectionCommand");
		var newInfo = info['newInfo'];
		info["newInfo"] = JSON.stringify(newInfo);
		showLoading(wsId);
		sendRequest(info, wsId);
	}

function sendRequest(info, worksheetId, callback) {
	$.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			parse(json);
			if (worksheetId == undefined)
				hideWaitingSignOnScreen();
			else
				hideLoading(worksheetId);
			if(callback)
				callback(json);
		},
		error: function(xhr, textStatus) {
			alert("Error occured with " + info['command'] + textStatus);
			if (worksheetId == undefined)
				hideWaitingSignOnScreen();
			else
				hideLoading(worksheetId);
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

function getColumnHeadingsForColumn(worksheetId, columnId, commandName) {
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

function generateInfoObject(worksheetId, columnId, commandName) {
	var info = {};
	info["hNodeId"] = columnId;
	info["workspaceId"] = $.workspaceGlobalInformation.id;
	info["worksheetId"] = worksheetId;
	info["command"] = commandName;
	info['selectionName'] = "DEFAULT_TEST";
	var newInfo = [];
	if (columnId != "" && columnId != undefined)
		newInfo.push(getParamObject("hNodeId", columnId, "hNodeId"));
	if (worksheetId != "" && worksheetId != undefined)
		newInfo.push(getParamObject("worksheetId", worksheetId, "worksheetId"));
	newInfo.push(getParamObject("selectionName", "DEFAULT_TEST", "other"));
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
	var info = generateInfoObject(worksheetId, "", "TestSPARQLEndPointCommand");
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
	var info = generateInfoObject(worksheetId, "", "GetClassesCommand");
	info["nodesRange"] = "allClasses";

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
	var info = generateInfoObject(worksheetId, "", "GetClassesCommand");
	info["nodesRange"] = "allClassesRaw";

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

	var info = generateInfoObject(worksheetId, "", "GetClassesCommand");
	info["nodesRange"] = "classesWithProperty";
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
	var info = generateInfoObject(worksheetId, "", "GetClassesCommand");
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
	var rdfsLabel = clazz.nodeRDFSLabel;

	var node = {
		"label": label,
		"id": id,
		"uri": uri,
		"rdfsLabel": rdfsLabel
	};
	result.push(node);
	//	if(clazz.children) {
	//		$.each(clazz.children, function(index, clazzChild){
	//        	parseClassJSON(clazzChild, result, allLabels);
	//        });
	//	}
}

function getSuggestedSemanticTypes(worksheetId, columnId, classUri) {
	var info = generateInfoObject(worksheetId, columnId, "GetSemanticSuggestionsCommand");
	var newInfo = info['newInfo']; // Used for commands that take JSONArray as input and are saved in the history
	if(classUri) {
		info["classUri"] = classUri;
		newInfo.push(getParamObject("classUri", info["classUri"], "other"));
	}
	info["newInfo"] = JSON.stringify(newInfo);
	showLoading(info["worksheetId"]);
	var result;
	var returned = $.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		async: false,
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			hideLoading(info["worksheetId"]);
			result = json.elements[0];
		},
		error: function(xhr, textStatus) {
			alert("Error occured with fetching new rows! " + textStatus);
			hideLoading(info["worksheetId"]);
		}
	});
	return result;
}

function getSuggestedLinks(worksheetId, columnId) {
	var info = generateInfoObject(worksheetId, columnId, "GetLinkSuggestionsCommand");
	var newInfo = info['newInfo']; // Used for commands that take JSONArray as input and are saved in the history
	
	info["newInfo"] = JSON.stringify(newInfo);
	showLoading(info["worksheetId"]);
	var result;
	var returned = $.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		async: false,
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			hideLoading(info["worksheetId"]);
			result = json.elements[0];
		},
		error: function(xhr, textStatus) {
			alert("Error occured with fetching new rows! " + textStatus);
			hideLoading(info["worksheetId"]);
		}
	});
	return result;
}

function getAllDataAndObjectProperties(worksheetId) {
	var info = generateInfoObject(worksheetId, "", "GetPropertiesCommand");
	info["propertiesRange"] = "allDataAndObjectProperties";

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

function getAllDataProperties(worksheetId) {
	var info = generateInfoObject(worksheetId, "", "GetPropertiesCommand");
	info["propertiesRange"] = "allDataProperties";

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
	var info = generateInfoObject(worksheetId, "", "GetPropertiesCommand");
	info["propertiesRange"] = "allObjectProperties";

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
	var info = generateInfoObject(worksheetId, "", "GetPropertiesCommand");
	info["propertiesRange"] = "existingProperties";

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

	var info = generateInfoObject(worksheetId, "", "GetPropertiesCommand");
	info["propertiesRange"] = "dataPropertiesForClass";
	info["classURI"] = classUri;

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
	var info = generateInfoObject(worksheetId, "", "GetPropertiesCommand");
	info["propertiesRange"] = "propertiesWithDomainRange";
	info["domainURI"] = domainUri;
	info["rangeURI"] = rangeUri;

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

function getRecommendedProperties(worksheetId, linkId) {
	var info = generateInfoObject(worksheetId, "", "GetPropertiesCommand");
	info["propertiesRange"] = "recommendedProperties";
	info["linkId"] = linkId;

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
	//sortClassPropertyNodes(result);
	return result;
}

function parsePropertyJSON(prop, result) {
	var node = {
		"label": prop.label,
		"rdfsLabel": prop.rdfsLabel,
		"id": prop.id,
		"uri": prop.uri,
		"type": prop.type
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
	var info = generateInfoObject(worksheetId, "", "GetCurrentLinksOfInternalNodeCommand");
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

function changeLinks(worksheetId, alignmentId, oldEdges, newEdges, callback) {
	var info = generateInfoObject(worksheetId, "", "ChangeInternalNodeLinksCommand");
	// Prepare the input for command
	var newInfo = info['newInfo'];
	newInfo.push(getParamObject("alignmentId", alignmentId, "other"));

	// Put the old edge information
	var initialEdges = [];
	$.each(oldEdges, function(index, edge) {
		var oldEdgeObj = {};
		oldEdgeObj["edgeSourceId"] = edge.source.id;
		oldEdgeObj["edgeTargetId"] = edge.target.id;
		oldEdgeObj["edgeId"] = edge.uri;
		oldEdgeObj["isProvenance"] = edge.isProvenance;
		initialEdges.push(oldEdgeObj);
	});
	newInfo.push(getParamObject("initialEdges", initialEdges, "other"));
	info["initialEdges"] = initialEdges;

	// Put the new edge information
	var newEdgesArr = [];
	$.each(newEdges, function(index, edge) {
		var newEdgeObj = {};
		newEdgeObj["edgeSourceId"] = edge.source.id;
		newEdgeObj["edgeSourceUri"] = edge.source.uri;
		newEdgeObj["edgeTargetId"] = edge.target.id;
		newEdgeObj["edgeTargetUri"] = edge.target.uri;
		newEdgeObj["edgeId"] = edge.uri;
		newEdgeObj["isProvenance"] = edge.isProvenance;
		newEdgesArr.push(newEdgeObj);
	});
	newInfo.push(getParamObject("newEdges", newEdgesArr, "other"));
	info["newEdges"] = newEdgesArr;

	info["newInfo"] = JSON.stringify(newInfo);
	showLoading(worksheetId);
	return sendRequest(info, worksheetId, callback);
}

function setSpecializedEdgeSemanticType(worksheetId, columnId, edge, rdfLiteralType) {
	var info = generateInfoObject(worksheetId, columnId, "");
	info["command"] = "SetMetaPropertyCommand";

	var newInfo = info['newInfo'];

	info["metaPropertyName"] = "isSpecializationForEdge";
	info["metaPropertyUri"] = edge.uri;
	info["metaPropertyId"] = edge.id;
	newInfo.push(getParamObject("metaPropertyName", info["metaPropertyName"], "other"));
	newInfo.push(getParamObject("metaPropertyUri", info["metaPropertyUri"], "other"));
	newInfo.push(getParamObject("metaPropertyId", info["metaPropertyId"], "linkWithHNodeId"));

	info["trainAndShowUpdates"] = true
	info["rdfLiteralType"] = ''
	newInfo.push(getParamObject("trainAndShowUpdates", true, "other"));
	if(rdfLiteralType)
		newInfo.push(getParamObject("rdfLiteralType", rdfLiteralType, "other"));
	else
		newInfo.push(getParamObject("rdfLiteralType", '', "other"));

	info["newInfo"] = JSON.stringify(newInfo);
	showLoading(info["worksheetId"]);
	var returned = sendRequest(info, worksheetId);
}

function setSubClassSemanticType(worksheetId, columnId, clazz, rdfLiteralType, language) {
	var info = generateInfoObject(worksheetId, columnId, "");
	info["command"] = "SetMetaPropertyCommand";

	var newInfo = info['newInfo'];

	info["metaPropertyName"] = "isSubclassOfClass";
	info["metaPropertyUri"] = clazz.uri;
	info["metaPropertyId"] = clazz.id;
	newInfo.push(getParamObject("metaPropertyName", info["metaPropertyName"], "other"));
	newInfo.push(getParamObject("metaPropertyUri", info["metaPropertyUri"], "other"));
	newInfo.push(getParamObject("metaPropertyId", info["metaPropertyId"], "linkWithHNodeId"));

	info["trainAndShowUpdates"] = true
	info["rdfLiteralType"] = ''
	info['language'] = '';
	newInfo.push(getParamObject("trainAndShowUpdates", true, "other"));
	newInfo.push(getParamObject("rdfLiteralType", rdfLiteralType, "other"));
	newInfo.push(getParamObject("language", language, "other"));

	info["newInfo"] = JSON.stringify(newInfo);
	showLoading(info["worksheetId"]);
	var returned = sendRequest(info, worksheetId);
}

function removeSemanticLink(worksheetId, alignmentId, columnId, columnType, link) {
	if(columnType == "ColumnNode") {
		info = generateInfoObject(worksheetId, columnId, "UnassignSemanticTypeCommand");
		var newInfo = info['newInfo'];

		var semTypesArray = new Array();
		var isPrimary = true;
		
		//1. Add all existing semantic types
		var tdTag = $("td#" + columnId);
		var typeJsonObject = $(tdTag).data("typesJsonObject");
		if (typeJsonObject) {
			existingTypes = typeJsonObject["SemanticTypesArray"];
		} else {
			existingTypes = [];
		}
		var language;
		var rdfLiteralType;
		$.each(existingTypes, function(index, type) {
			if(type.FullType != link.uri || type.DomainId != link.source.id) {
				var newType = new Object();
				newType["FullType"] = type.FullType;
				newType["DomainUri"] = type.DomainUri;
				newType["DomainId"] = type.DomainId;
				newType["DomainLabel"] = type.DisplayDomainLabel;
				newType["isPrimary"] = type.isPrimary;
				if(type.isPrimary)
					isPrimary = false;
				newType["isProvenance"] = type.isProvenance;
				language = type.language;
				rdfLiteralType = type.rdfLiteralType;
				semTypesArray.push(newType);
			}
		});

		if(semTypesArray.length > 0) {
			info["command"] = "SetSemanticTypeCommand";
			info["SemanticTypesArray"] = JSON.stringify(semTypesArray);
			newInfo.push(getParamObject("SemanticTypesArray", semTypesArray, "other"));
			newInfo.push(getParamObject("trainAndShowUpdates", true, "other"));
			newInfo.push(getParamObject("rdfLiteralType", rdfLiteralType, "other"));
			newInfo.push(getParamObject("language", language, "other"));
		} else {
			newInfo.push(getParamObject("alignmentId", alignmentId, "other"));
		}
		info["newInfo"] = JSON.stringify(newInfo);
	} else {
		info = generateInfoObject(worksheetId, "", "ChangeInternalNodeLinksCommand");

		// Prepare the input for command
		var newInfo = info['newInfo'];

		// Put the old edge information
		var initialEdges = [];
		var oldEdgeObj = {};
		oldEdgeObj["edgeSourceId"] = link.source.id;
		oldEdgeObj["edgeTargetId"] = link.target.id;
		oldEdgeObj["edgeId"] = link.uri;
		initialEdges.push(oldEdgeObj);
		newInfo.push(getParamObject("initialEdges", initialEdges, "other"));
		newInfo.push(getParamObject("alignmentId", alignmentId, "other"));
		var newEdges = [];
		newInfo.push(getParamObject("newEdges", newEdges, "other"));
		info["newInfo"] = JSON.stringify(newInfo);
		info["newEdges"] = newEdges;
	}

	showLoading(worksheetId);
	var returned = sendRequest(info, worksheetId);
}


function setSemanticType(worksheetId, columnId, type, rdfLiteralType, language) {
	var info = generateInfoObject(worksheetId, columnId, "");
	var newInfo = info['newInfo']; 
	if(rdfLiteralType) {
	} else {
		rdfLiteralType = '';
	}
	if(language) {
	} else {
		language = '';
	}
	info["rdfLiteralType"] = rdfLiteralType;
	info["language"] = language;
	if(type.uri == "http://isi.edu/integration/karma/dev#classLink") {
		info["command"] = "SetMetaPropertyCommand";
		info["metaPropertyName"] = "isUriOfClass";
		info["metaPropertyUri"] = type.source.uri
		info["metaPropertyId"] = type.source.id;
		newInfo.push(getParamObject("metaPropertyName", info["metaPropertyName"], "other"));
		newInfo.push(getParamObject("metaPropertyUri", info["metaPropertyUri"], "other"));
		newInfo.push(getParamObject("metaPropertyId", info["metaPropertyId"], "other"));
	} else {
		info["command"] = "SetSemanticTypeCommand";
		var semTypesArray = new Array();
		var isPrimary = true;
		
		//1. Add all existing semantic types
		var tdTag = $("td#" + columnId);
		var typeJsonObject = $(tdTag).data("typesJsonObject");
		if (typeJsonObject) {
			existingTypes = typeJsonObject["SemanticTypesArray"];
		} else {
			existingTypes = [];
		}
		var isValid = true;
		var errorMsg = "";
		$.each(existingTypes, function(index, etype) {
			var newType = new Object();
			if(etype.FullType == type.uri && etype.DomainId == type.source.id)
				return;

			newType["FullType"] = etype.FullType;
			newType["DomainUri"] = etype.DomainUri;
			newType["DomainId"] = etype.DomainId;
			newType["DomainLabel"] = etype.DomainLabel;
			newType["isPrimary"] = etype.isPrimary;
			if(etype.isPrimary)
				isPrimary = false;
			newType["isProvenance"] = etype.isProvenance;
			if(etype.FullType == "http://isi.edu/integration/karma/dev#classLink") {
				isValid = false;
				errorMsg = "Please delete URI type. Only 1 Semantic Type can be defined for columns that are URIs"
				return;
			} else if(etype.FullType == "http://isi.edu/integration/karma/dev#dataPropertyOfColumnLink") {
				isValid = false;
				errorMsg = "Please delete edge specializing link before adding the new type. \nOnly 1 Semantic type can be defined for columns that specify specialization for edge"
			} else if(etype.FullType == "http://isi.edu/integration/karma/dev#columnSubClassOfLink") {
				isValid = false;
				errorMsg = "Please delete columnSubClassOfLink before adding the new type. \nOnly 1 Semantic Type can be defined for columns that specify subclass for Nodes"
			}
			semTypesArray.push(newType);
		});

		if(!isValid) {
			alert(errorMsg);
			return;
		}
		//2. Add this new type that we have
		var newType = new Object();
		newType["FullType"] = type.uri;
		newType["DomainUri"] = type.source.uri;
		newType["DomainId"] = type.source.id;
		newType["DomainLabel"] = type.source.label;
		newType["isPrimary"] = isPrimary;
		newType["isProvenance"] = type.isProvenance;
		semTypesArray.push(newType);

		info["SemanticTypesArray"] = JSON.stringify(semTypesArray);
		newInfo.push(getParamObject("SemanticTypesArray", semTypesArray, "other"));
	}

	newInfo.push(getParamObject("trainAndShowUpdates", true, "other"));
	newInfo.push(getParamObject("rdfLiteralType", rdfLiteralType, "other"));
	newInfo.push(getParamObject("language", language, "other"));
	info["newInfo"] = JSON.stringify(newInfo);
	showLoading(info["worksheetId"]);
	var returned = sendRequest(info, worksheetId);
}

function changeKarmaHome(homeDir) {
	var info = generateInfoObject("", "", "SetKarmaHomeCommand");
	info["directory"] = homeDir;
	var result = true;
	$.cookie("karmaHome", homeDir);
	$.ajax({
		url: "RequestController",
		type: "POST",
		data: info,
		dataType: "json",
		async: false,
		complete: function(xhr, textStatus) {
			var json = $.parseJSON(xhr.responseText);
			parse(json);
			$.each(data["elements"], function(i, element) {
				if (element["updateType"] == "KarmaError") {
					result = false;
					return;
				}
			});
			
		},
		error: function(xhr, textStatus) {
			alert("Error occured while setting Karma Home Directory!");
		}
	});
	return result;
}

function refreshWorksheet(worksheetId, updates) {
	console.log("Refresh Worksheet:" + refreshWorksheet)
	var info = generateInfoObject(worksheetId, "", "RefreshWorksheetCommand");
	info["updates"] = JSON.stringify(updates);
	showLoading(info["worksheetId"]);
	sendRequest(info, worksheetId);
}

function refreshHistory(worksheetId, callback) {
	console.log("Refresh History:" + refreshWorksheet)
	var info = generateInfoObject(worksheetId, "", "RefreshHistoryCommand");
	// Prepare the input for command
	var newInfo = info['newInfo'];
	newInfo.push(getParamObject("worksheetId", worksheetId, "worksheetId"));
	info["newInfo"] = JSON.stringify(newInfo);
	showLoading(worksheetId);
	return sendRequest(info, worksheetId, callback);
}

function getLabelWithoutPrefix(label) {
	idx = label.indexOf(":");
	if(idx != -1)
		return label.substring(idx+1);
	return label;
}

function isValidUrl(url) {
	var re = /^(ht|f)tps?:\/\/(([a-z0-9-\.]+\.[a-z]{2,4})|(localhost))\/?([^\s<>\#%"\,\{\}\\|\\\^\[\]`]+)?$/;
    return re.test(url);
}

function showNodeHelp(worksheetId, node) {
	if(node.nodeId)
		uri = node.nodeId;
	else
		uri = node.linkUri;
	var html = "<div class='rdfsUriHelp'><span class='heading'>URI&nbsp;</span>" + uri + "</div>";
	if(node.rdfsLabel)
		html += "<div class='rdfsLabelHelp'><span class='heading'>rdfs:label&nbsp;</span>" + node.rdfsLabel + "</div>";
	if(node.rdfsComment)
		html += "<div class='rdfsCommentHelp'><span class='heading'>rdfs:comment&nbsp;</span>" + node.rdfsComment + "</div>";
	showHelp(worksheetId, html);
}

function showHelp(worksheetId, html) {
	var helpDiv = $("#helpDiv");
	helpDiv.html(html);
	var worksheetTop = $("#svgDiv_" + worksheetId);
	var top = worksheetTop.offset().top;
	var left = worksheetTop.offset().left;

	var windowBoxLeft = $(window).scrollLeft();
	var windowBoxRight = windowBoxLeft + $(window).width();
	var windowBoxTop = $(window).scrollTop();
	var windowBoxBottom = windowBoxTop + $(window).height();

	if(left < windowBoxLeft || left > windowBoxRight)
		left = windowBoxLeft + 2;
	if(top < windowBoxTop || top > windowBoxBottom)
		top = windowBoxTop + 2;

	helpDiv.css({ "top": top+2, "left": left+2 });
	helpDiv.show();
}

function hideHelp() {
	$("#helpDiv").hide();
}

//Make All Modal Dialogs Resizeable
$(".modal-dialog").resizable({
	handles: "e, w"
});
$(".modal-dialog").draggable({
	handle: ".modal-header"
});