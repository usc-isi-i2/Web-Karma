function isDialogInitialized(dialog) {
	if(dialog.hasClass("ui-dialog-content"))
		return true;
	return false;
}

function getColumnHeadings(worksheetId) {
	var columnNames = [];
	
	var columnNameDivs = $("#" + worksheetId + " div.wk-header a.ColumnTitle");
    $.each(columnNameDivs, function(index, element) {
        columnNames.push($.trim($(element).text()));
    });
    
    return columnNames;
}

function showLoading(worksheetId) {
    // Remove any existing coverDiv
    $("div#WaitingDiv_" + worksheetId).remove();

    // Create a new cover
    var coverDiv = $("<div>").attr("id","WaitingDiv_"+worksheetId).addClass('waitingDiv')
        .append($("<div>").html('<b>Please wait</b>')
            .append($('<img>').attr("src","images/ajax-loader.gif"))
        );

    var spaceToCoverDiv = $("div#"+worksheetId);
    spaceToCoverDiv.append(coverDiv.css({"position":"absolute", "height":spaceToCoverDiv.height(),
        "width": spaceToCoverDiv.width(), "top":spaceToCoverDiv.position().top, "left":spaceToCoverDiv.position().left}).show());
}

function hideLoading(worksheetId) {
    $("div#WaitingDiv_"+worksheetId).hide();
}

function showWaitingSignOnScreen() {
    var coverDiv = $("<div>").attr("id","WaitingDiv").addClass('waitingDiv')
        .append($("<div>").html('<b>Please wait</b>')
            .append($('<img>').attr("src","images/ajax-loader.gif"))
        );

    var spaceToCoverDiv = $('body');
    spaceToCoverDiv.append(coverDiv.css({"position":"fixed", "height":$(document).height(),
        "width": $(document).width(), "zIndex":100,"top":spaceToCoverDiv.position().top, "left":spaceToCoverDiv.position().left}).show());
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
	var returned = $.ajax({
	   	url: "RequestController",
	   	type: "POST",
	   	data : info,
	   	dataType : "json",
	   	async : false,
	   	complete :
	   		function (xhr, textStatus) {
	    		var json = $.parseJSON(xhr.responseText);
	    		if(json['elements'] && json['elements'][0]['connectionStatus'] && json['elements'][0]['connectionStatus'] == 1) {
	    			window.conncetionStat = true;
	    		}
		   	},
		error :
			function (xhr, textStatus) {
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
    info["command"] = "GetOntologyClassHierarchyCommand";
    info["worksheetId"] = worksheetId;
    
    var result = [];
    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        async : false,
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                var data = json.elements[0].data;
                $.each(data, function(index, clazz){
                	parseClassJSON(clazz, result);
                });
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while fetching classes: " + textStatus);
            }
    });
    sortClassPropertyNodes(result);
    var lastLabel = "";
    var uniques = [];
    $.each(result, function(index, item){
    	if(item.label != lastLabel)
    		uniques.push(item);
    	lastLabel = item.label;
    });
	return uniques;
}

function getAllClassesForProperty(worksheetId, propertyUri) {
	if(propertyUri == null || propertyUri == "")
		return [];
	
	var info = new Object();
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "GetDomainsForDataPropertyCommand";
    info["worksheetId"] = worksheetId;
    info["URI"] = propertyUri;
    
    var result = [];
    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        async : false,
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                var data = json.elements[0].data;
                $.each(data, function(index, clazz){
                	parseClassJSON(clazz, result);
                });
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while fetching classes for property " + property.label + ": " + textStatus);
            }
    });
    sortClassPropertyNodes(result);
    var lastLabel = "";
    var uniques = [];
    $.each(result, function(index, item){
    	if(item.label != lastLabel)
    		uniques.push(item);
    	lastLabel = item.label;
    });
	return uniques;
}

function getClassesInModel(alignmentId) {
	var info = new Object();
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "GetInternalNodesListOfAlignmentCommand";
    info["alignmentId"] = alignmentId;
    info["nodesRange"] = "existingTreeNodes"; //allGraphNodes";
    var result = [];
    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        async : false,
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                $.each(json["elements"], function(index, element) {
                    if(element["updateType"] == "InternalNodesList") {
                    	$.each(element["nodes"], function(index2, node) {
                        	var label = node["nodeLabel"];
                        	if(label.indexOf(":") == -1)
                        		label = node["nodeUri"] + "/" + label;
                        	var nodeData = {"label":label, "id":node["nodeId"], "uri":node["nodeUri"]};
                        	result.push(nodeData);
                        });
                    }
                });
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while getting nodes list!");
            }
    });
    sortClassPropertyNodes(result);
    return result;
}


function parseClassJSON(clazz, result, allLabels) {
	var uri = clazz.metadata.URIorId;
	var index = clazz.metadata.newIndex;
	
	if(clazz.metadata.isExistingSteinerTreeNode) {
		if(index) {
			uri = uri.substr(0, uri.lastIndexOf(index));
		}
		index = false;
	}
	var label = clazz.data;
	var id = clazz.metadata.URIorId;
	
	if(index) {
		label = label + index + " (add)";
		id = id + index;
	}
	var node = {"label":label, "id":id, "uri":uri};
	result.push(node);
	if(clazz.children) {
		$.each(clazz.children, function(index, clazzChild){
        	parseClassJSON(clazzChild, result, allLabels);
        });
	}
}

function getAllDataProperties(worksheetId) {
	var info = new Object();
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "GetDataPropertyHierarchyCommand";
    info["worksheetId"] = worksheetId;
    
    var result = [];
    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        async : false,
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                var data = json.elements[0].data;
                $.each(data, function(index, prop){
                	parsePropertyJSON(prop, result);
                });
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while fetching properties: " + textStatus);
            }
    });
    sortClassPropertyNodes(result);
    var lastLabel = "";
    var uniques = [];
    $.each(result, function(index, item){
    	if(item.label != lastLabel)
    		uniques.push(item);
    	lastLabel = item.label;
    });
	return uniques;
}

function getAllObjectProperties(alignmentId) {
	var info = new Object();
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "GetLinksOfAlignmentCommand";
    info["alignmentId"] = alignmentId;
    
    info["linksRange"] = "allObjectProperties";
    
    var result = [];
    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        async: false,
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                $.each(json["elements"], function(index, element) {
        	        if(element["updateType"] == "DataPropertyListUpdate" || element["updateType"] == "DataPropertiesForClassUpdate") {
        	            $.each(element["data"], function(index2, node) {
        	            	result.push({"label":node.data, "id":node.metadata.URIorId, "uri":node.metadata.URIorId});
        	            });
        	        } else if(element["updateType"] == "LinksList") {
        	        	 $.each(element["edges"], function(index2, node) {
        	        		 result.push({"label":node["edgeLabel"], "id":node["edgeId"], "uri":node["edgeId"]});
        	             });
        	        }
        	    });
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while getting property list!");
            }
    });
    sortClassPropertyNodes(result);
    return result;
}

function getAllPropertiesForClass(worksheetId, classUri) {
	if(classUri == null || classUri == "")
		return [];
	
	//http://localhost:8080/RequestController?URI=http%3A%2F%2Flod.isi.edu%2Fontology%2Fsyllabus%2FLecture1&command=GetDataPropertiesForClassCommand&workspaceId=WSP23
	var info = new Object();
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "GetDataPropertiesForClassCommand";
    info["worksheetId"] = worksheetId;
    info["URI"] = classUri;
    
    var result = [];
    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        async : false,
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                var data = json.elements[0].data;
                $.each(data, function(index, prop){
                	parsePropertyJSON(prop, result);
                });
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while fetching properties for " + thisClass.label + ": " + textStatus);
            }
    });
    sortClassPropertyNodes(result);
    var lastLabel = "";
    var uniques = [];
    $.each(result, function(index, item){
    	if(item.label != lastLabel)
    		uniques.push(item);
    	lastLabel = item.label;
    });
	return uniques;
}

function getAllPropertiesForDomainRange(alignmentId, domain, range) {
	var info = new Object();
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "GetLinksOfAlignmentCommand";
    info["alignmentId"] = alignmentId;
    
    info["linksRange"] = "linksWithDomainAndRange";
    info["domain"] = domain;
    info["range"] = range;
    
    var result = [];
    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        async: false,
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                $.each(json["elements"], function(index, element) {
        	        if(element["updateType"] == "DataPropertyListUpdate" || element["updateType"] == "DataPropertiesForClassUpdate") {
        	            $.each(element["data"], function(index2, node) {
        	            	result.push({"label":node.data, "id":node.metadata.URIorId, "uri":node.metadata.URIorId});
        	            });
        	        } else if(element["updateType"] == "LinksList") {
        	        	 $.each(element["edges"], function(index2, node) {
        	        		 result.push({"label":node["edgeLabel"], "id":node["edgeId"], "uri":node["edgeId"]});
        	             });
        	        }
        	    });
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while getting property list!");
            }
    });
    sortClassPropertyNodes(result);
    return result;
}

function parsePropertyJSON(prop, result) {
	var label = prop.data;
	var id = prop.metadata.URIorId;
	var uri = prop.metadata.URIorId;
	var node = {"label":label, "id":id, "uri":uri};
	result.push(node);
	if(prop.children) {
		$.each(prop.children, function(index, propChild){
			parsePropertyJSON(propChild, result);
        });
	}
}

function sortClassPropertyNodes(nodes) {
	nodes.sort(function(a,b) {
		var label1 = a.label;
    	if(label1.indexOf(":") == -1)
    		label1 = a.uri + "/" + label1;
    	var label2 = b.label;
    	if(label2.indexOf(":") == -1)
    		label2 = b.uri + "/" + label2;
    	
        return label1.toUpperCase().localeCompare(label2.toUpperCase());
    });
}

//Make All Modal Dialogs Resizeable
$(".modal-dialog").resizable({ handles: "e, w" });
$(".modal-dialog").draggable({
    handle: ".modal-header"
}); 
