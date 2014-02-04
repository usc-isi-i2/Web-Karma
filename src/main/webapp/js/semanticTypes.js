var SetSemanticTypeDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
    	var dialog = $("#setSemanticTypeDialog");
    	var worksheetId;
    	var columnId;
    	var columnTitle;
    	var existingTypes, selectedPrimaryRow, classAndPropertyListJson;
    	var classPropertyUIDiv;
    	
    	function init() {
    		//Initialize what happens when we show the dialog
    		dialog.on('show.bs.modal', function (e) {
				hideError();
				
				$("#semanticType_columnName", dialog).text(columnTitle);
				
			    $("table#semanticTypesTable tr.semTypeRow",dialog).remove();
			    $("table#semanticTypesTable tr.editRow",dialog).remove();
			    $("input#chooseClassKey", dialog).attr("checked", false);
			    $("#literalTypeSelect").val("");
			    
			    dialog.removeData("selectedPrimaryRow");
			    // Deselect all the advanced options check boxes
			    $("div#semanticTypesAdvacedOptionsDiv").hide();
			    $("div#semanticTypesAdvacedOptionsDiv input:checkbox").prop('checked', false);
			    $("div#semanticTypesAdvacedOptionsDiv input:text").val("");
			    
			    // Store a copy of the existing types.
			    // This is tha JSON array which is changed when the user adds/changes through GUI and is submitted to the server.
			    var tdTag = $("td#"+ columnId); 
			    var typeJsonObject = $(tdTag).data("typesJsonObject");
			    existingTypes = typeJsonObject["SemanticTypesArray"];
			    
			    var CRFInfo = typeJsonObject["FullCRFModel"];
			    
			    // Populate the table with existing types and CRF suggested types
			    $.each(existingTypes, function(index, type){
			        // Take care of the special meta properties that are set through the advanced options
			    	if (type["isMetaProperty"]) {
			    		if (type["DisplayLabel"] == "km-dev:classLink") {
			    			$("#isUriOfClass").prop('checked', true);
			    			$("#isUriOfClassTextBox").val(type["DisplayDomainLabel"]);
			    		} else if (type["DisplayLabel"] == "km-dev:columnSubClassOfLink") {
			    			$("#isSubclassOfClass").prop('checked', true);
			    			$("#isSubclassOfClassTextBox").val(type["DisplayDomainLabel"]);
			    		} else if (type["DisplayLabel"] == "km-dev:dataPropertyOfColumnLink") {
			    			$("#isSpecializationForEdge").prop('checked', true);
			    			$("#isSpecializationForEdgeTextBox").val(type["DisplayDomainLabel"]);
			    		}
			    		$("div#semanticTypingAdvacedOptionsDiv").show();
			    	} else {
			    		addSemTypeObjectToCurrentTable(type, true, false);
			    	}
			    });
			    if(CRFInfo != null) {
			        $.each(CRFInfo["Labels"], function(index, type){
			            addSemTypeObjectToCurrentTable(type, false, true);
			        });
			    }
			    
			    // Get the whole list of classes and properties from the server for autocompletion
			    var info = new Object();
			    info["workspaceId"] = $.workspaceGlobalInformation.id;
			    info["command"] = "GetPropertiesAndClassesList";
			    info["worksheetId"] = worksheetId;

			    var returned = $.ajax({
			        url: "RequestController",
			        type: "POST",
			        data : info,
			        dataType : "json",
			        complete :
			            function (xhr, textStatus) {
			                var json = $.parseJSON(xhr.responseText);
			                classAndPropertyListJson = json;
			                if (json) {
			                    json["elements"][0]["classList"].sort(function(a,b) {
			                        return a["label"].toUpperCase().localeCompare(b["label"].toUpperCase());
			                    });

			                    json["elements"][0]["propertyList"].sort(function(a,b) {
			                        return a.toUpperCase().localeCompare(b.toUpperCase());
			                    });
			                }

			                // Special case when no training has been done to CRF model
			                // Shows an empty semantic type
			                if((!CRFInfo && existingTypes.length == 0) ||
			                    ((existingTypes && existingTypes.length == 0) && (CRFInfo && CRFInfo.length == 0)) ||
			                    ((existingTypes && existingTypes.length == 0) && (CRFInfo && CRFInfo["Labels"].length == 0))) {
			                    addEmptySemanticType();
			                    $("table#semanticTypesTable input").prop("checked", true);
			                    $("table#semanticTypesTable tr.semTypeRow").addClass("selected");
			                    selectedPrimaryRow = $("table#semanticTypesTable tr.semTypeRow");
			                    $("table#semanticTypesTable tr td button").click();
			                }
			            },
			        error :
			            function (xhr, textStatus) {
			                alert("Error occured while fetching classes and properties list! " + textStatus);
			            }
			    });
			});
			
			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function (e) {
				e.preventDefault();
				saveDialog(e);
			});
			
			
			$("#semanticTypesAdvancedOptions", dialog).on('click', function(e) {
				e.preventDefault();
				$("#semanticTypesAdvacedOptionsDiv").toggle();
				
				var classArray = getClassLabels();
				var propertyArray = getPropertyLabels();
				
				$('.typeahead').typeahead('destroy');
				$("input#isUriOfClassTextBox", dialog).typeahead({ source:classArray});
				$("input#isSubclassOfClassTextBox", dialog).typeahead({ source:classArray});
				$("input#isSpecializationForEdgeTextBox", dialog).typeahead({ source:propertyArray});
			});
			
			$("div#semanticTypesAdvacedOptionsDiv input:checkbox").on('click', function(e) {
				console.log("semanticTypesAdvancedOptions checbox change handler");
				 var semTypesTable = $("table#semanticTypesTable");
			    $.each($("tr.selected.semTypeRow",semTypesTable), function(index, row){
			        $(this).removeClass('selected');
			        $("input[name='currentSemanticTypeCheckBoxGroup']:checkbox", $(this)).prop('checked', false);
			        $("input[name='isPrimaryGroup']:radio", $(this)).prop('checked',false);
			    });

			    $("div#semanticTypesAdvacedOptionsDiv input:checkbox").not($(this)).prop('checked', false);
			});
			
			$("#addType", dialog).on("click", function(e) {
				e.preventDefault();
				addEmptySemanticType();
			});
			
			$("#literalTypeSelect").typeahead( 
				{source:[
					"xsd:string","xsd:boolean","xsd:decimal","xsd:integer","xsd:double","xsd:float","xsd:time",
					"xsd:dateTime","xsd:dateTimeStamp","xsd:gYear","xsd:gMonth","xsd:gDa","xsd:gYearMonth",
					"xsd:gMonthDay","xsd:duration","xsd:yearMonthDuration","xsd:dayTimeDuration","xsd:",
					"xsd:shor","xsd:int","xsd:long","xsd:unsignedByte","xsd:unsignedShort","xsd:unsignedInt",
					"xsd:unsignedLong","xsd:positiveInteger","xsd:nonNegativeInteger","xsd:negativeInteger",
					"xsd:nonPositiveInteger","xsd:hexBinary","xsd:base64Binar","xsd:anyURI",
					"xsd:language","xsd:normalizedString","xsd:token","xsd:NMTOKEN","xsd:Namexsd:NCName"
			         ],
			      minLength:0,
			      items:"all"});
			
    	}
    	
		function hideError() {
			$("div.error", dialog).hide();
		}
		
		function showError(err) {
			if(err) {
				$("div.error", dialog).text(err);
			}
			$("div.error", dialog).show();
		}
        
		function validate() {
			if($("#isUriOfClass").prop("checked")) {
				var foundObj = doesClassExist($("input#isUriOfClassTextBox", dialog).val());
	        	if(!foundObj.found) {
	        		showError("Class for 'contains URI for node' does not exist");
	        		return false;
	        	}
			}
			
			if($("#isSubclassOfClass").prop("checked")) {
	        	var foundObj = doesClassExist($("input#isSubclassOfClassTextBox", dialog).val());
	        	if(!foundObj.found) {
	        		showError("Class for 'specifies class for node' does not exist");
	        		return false;
	        	}
			}
			
			if($("#isSpecializationForEdge").prop("checked")) {
				var foundObj = doesClassExist($("input#isSpecializationForEdgeTextBox", dialog).val());
	        	if(!foundObj.found) {
	        	   showError("Property for 'specifies specialization for edge' does not exist");
	        	   return false;
	        	} 
			}
			
        	return true;
		}
		
		function getCurrentSelectedTypes() {
		    var existingTypes = new Array();
		    var table = $("#semanticTypesTable");

		    var notValid = false;
		    // Loop through each selected row in the table
		    $.each($("tr.selected.semTypeRow",table), function(index, row){
		        var fullType = $(row).data("FullType");
		        var domain = $(row).data("Domain");

		        // Check if the user selected a fake semantic type object
		        if(domain == "fakeDomainURI" || fullType == "fakePropertyURI") {
		            $(row).addClass("fixMe");
		           showError("Semantic type not valid!");
		            notValid = true;
		            return false;
		        }
		        // Check if the type already exists (like the user had same type in a previous row)
		        var exists = false;
		        $.each(existingTypes, function(index2, type){
		            if(type["Domain"] == domain && fullType == type["FullType"]) {
		                exists = true;
		                return false;
		            }
		        });
		        if(exists)
		            return false;

		        // Create a new object and push it into the array
		        var newType = new Object();
		        newType["FullType"] = fullType;
		        newType["Domain"] = domain;

		        // Check if it was chosen primary
		        newType["isPrimary"] = $("input[name='isPrimaryGroup']:radio", $(row)).is(":checked");
		        existingTypes.push(newType);
		    });
		    if(notValid)
		        return null;

		    return existingTypes;
		}
		
		
        function saveDialog(e) {
        	hideError();
        	
        	if(!validate()) {
        		return false;
        	}
        	
        	var info = new Object();
            var newInfo = [];	// Used for commands that take JSONArray as input and are saved in the history
            var hNodeId = columnId;
            info["worksheetId"] = worksheetId;
            info["hNodeId"] = hNodeId;
            info["isKey"] = $("input#chooseClassKey").is(":checked");
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["rdfLiteralType"] = $("#literalTypeSelect").val()

            // Check if any meta property (advanced options) was selected
            if($("#isUriOfClass").prop("checked") || $("#isSubclassOfClass").prop("checked") || $("#isSubclassOfClass").prop("checked")) {
            	info["command"] = "SetMetaPropertyCommand";
            	var propValue;
            	
            	if($("#isUriOfClass").prop("checked")) {
					propValue = ($("input#isUriOfClassTextBox", dialog).val());
					info["metaPropertyName"] = "isUriOfClass";
				} else if($("#isSubclassOfClass").prop("checked")) {
		        	propValue = ($("input#isSubclassOfClassTextBox", dialog).val());
		        	info["metaPropertyName"] = "isSubclassOfClass";
				} else {
					propValue = $("input#isSpecializationForEdgeTextBox", dialog).val();
					info["metaPropertyName"] = "isSpecializationForEdge";
				}
           
                if (propValue == null || $.trim(propValue) == "") {
                    showError("Please provide a value!");
                    return false;
                }
                
                newInfo.push(getParamObject("metaPropertyName", info["metaPropertyName"], "other"));
                
                var valueFound = false;
                // Get the proper id
                if (info["metaPropertyName"] == "isUriOfClass" || info["metaPropertyName"] == "isSubclassOfClass") {
                    var classMap = classAndPropertyListJson["elements"][0]["classMap"];
                    $.each(classMap, function(index, clazz){
                        for(var key in clazz) {
                            if(clazz.hasOwnProperty(key)) {
                                if(key.toLowerCase() == propValue.toLowerCase()) {
                                    info["metaPropertyValue"] = clazz[key];
                                    newInfo.push(getParamObject("metaPropertyValue", clazz[key], "other"));
                                    valueFound = true;
                                }
                            }
                        }
                    });
                } else {
                    var existingLinksMap = classAndPropertyListJson["elements"][0]["existingDataPropertyInstances"];
                    $.each(existingLinksMap, function(index, prop) {
                        if (prop["label"] == propValue) {
                            info["metaPropertyValue"] = prop["id"];
                            newInfo.push(getParamObject("metaPropertyValue", prop["id"], "other"));
                            valueFound = true;
                        }
                    });
                }
                if(!valueFound) {
                	showError("Class/Property does not exist");
                	return false;
                }
                
            } else {                // Get the JSON Array that captures all the currently selected semantic types
                var semTypesArray = getCurrentSelectedTypes();
                if(semTypesArray == null || semTypesArray === false)
                    return false;
                info["SemanticTypesArray"] = JSON.stringify(semTypesArray);
                if(semTypesArray.length == 0)
                    info["command"] = "UnassignSemanticTypeCommand";
                else
                    info["command"] = "SetSemanticTypeCommand";
            }

            info["SemanticTypesArray"] = JSON.stringify(semTypesArray);
            newInfo.push(getParamObject("hNodeId", hNodeId,"hNodeId"));
            newInfo.push(getParamObject("SemanticTypesArray", semTypesArray, "other"));
            newInfo.push(getParamObject("worksheetId", info["worksheetId"], "worksheetId"));
            newInfo.push(getParamObject("isKey", $("input#chooseClassKey").is(":checked"), "other"));
            newInfo.push(getParamObject("trainAndShowUpdates", true, "other"));
            newInfo.push(getParamObject("rdfLiteralType", $("#literalTypeSelect").val(), "other"));
            info["newInfo"] = JSON.stringify(newInfo);


            showLoading(info["worksheetId"]);
            var returned = $.ajax({
                url: "RequestController",
                type: "POST",
                data : info,
                dataType : "json",
                complete :
                    function (xhr, textStatus) {
                        var json = $.parseJSON(xhr.responseText);
                        parse(json);
                        classAndPropertyListJson = [];
                        hideLoading(info["worksheetId"]);
                    },
                error :
                    function (xhr, textStatus) {
                        alert("Error occured with fetching new rows! " + textStatus);
                        hideLoading(info["worksheetId"]);
                    }
            });

            hide();
        	return true;
        };
        
        function addEmptySemanticType() {
            // Create a fake sem type object to how in the table
            var fakeSemType = new Object();
            fakeSemType["FullType"] = "fakePropertyURI";
            fakeSemType["Domain"] = "fakeDomainURI";
            fakeSemType["DisplayLabel"] = "property";
            fakeSemType["DisplayDomainLabel"] = "Class";
            // Add it to the table
            addSemTypeObjectToCurrentTable(fakeSemType, false, false);
        }
        
        function addSemTypeObjectToCurrentTable(semTypeObject, isSelected, isCrfModelSuggested) {
            var table = $("#semanticTypesTable");

            // Check if it is eligible to be added to the table
            var isValid = true;
            $.each($("tr", table), function(index, row){
                if($(row).data("FullType") == semTypeObject["FullType"] && $(row).data("Domain") == semTypeObject["Domain"]) {
                    // We allow multiple fake semantic type objects to be added
                    if(!(semTypeObject["FullType"] == "fakePropertyURI" && semTypeObject["Domain"] == "fakeDomainURI"))
                        isValid = false;
                }
            });
            if(!isValid)
                return false;

            // Add it to the table
            var displayLabel = "";
            if(semTypeObject["Domain"].length == 0 || semTypeObject["Domain"] == "")
                displayLabel = semTypeObject["DisplayLabel"];
            else
                displayLabel = "<span class='italic'>" + semTypeObject["DisplayLabel"] + "</span> of " + semTypeObject["DisplayDomainLabel"];

            var trTag = $("<tr>").addClass("semTypeRow")
                .data("FullType", semTypeObject["FullType"])
                .data("Domain", semTypeObject["Domain"])
                .data("DisplayDomainLabel", semTypeObject["DisplayDomainLabel"])
                .data("DisplayLabel", semTypeObject["DisplayLabel"])
                .append($("<td>").append($("<input>")
                    .attr("type", "checkbox")
                    .attr("name", "currentSemanticTypeCheckBoxGroup")
                    .attr("value", semTypeObject["DisplayLabel"])
                    .val(semTypeObject["DisplayLabel"])
                    .prop("checked", isSelected)
                    .change(semanticTypesTableCheckBoxHandler)))
                .append($("<td>")
                    .append($("<label>").html(displayLabel).addClass('displayLabel')))
                .append($("<td>").append($("<input>")
                    .attr("type", "radio")
                    .attr("name", "isPrimaryGroup")
                    .attr("value", semTypeObject["DisplayLabel"])
                    .val(semTypeObject["DisplayLabel"])))
                .append($("<td>")
                		.append($("<button>").attr("type", "button").addClass("btn").addClass("editButton").addClass("btn-default").text("Edit").click(showSemanticTypeEditOptions))
                		.append($("<button>").attr("type", "button").addClass("btn").addClass("hideButton").css("display","none").addClass("btn-default").text("Hide").click(hideSemanticTypeEditOptions)))
                ;

            if(isCrfModelSuggested)
            // trTag.append($("<td>").addClass("CRFSuggestedText").text("  (CRF Suggested)"));
                trTag.append($("<td>").addClass("CRFSuggestedText"));
            else
                trTag.append($("<td>"));

            if(isSelected)
                trTag.addClass("selected");

            if(semTypeObject["isPrimary"]) {
                $("input[name='isPrimaryGroup']:radio", trTag).prop('checked', true);
                selectedPrimaryRow = trTag;
                $("#literalTypeSelect").val(semTypeObject["rdfLiteralType"]);
            }


            // Check if it was marked as key for a class
            if(semTypeObject["isPartOfKey"]) {
                $("input#chooseClassKey").attr("checked", true);
            }

            if(semTypeObject["Domain"].length == 0 || semTypeObject["Domain"] == "")
                trTag.data("ResourceType", "Class");
            else
                trTag.data("ResourceType", "DataProperty");

            table.append(trTag);
        }
        
        function semanticTypesTableCheckBoxHandler() {
           // var existingTypesArray = existingTypes;
            var parentTr = $(this).parents("tr");
            var table = $("table#semanticTypesTable");

            // Deselect any meta property checkbox
            $("div#semanticTypesAdvacedOptionsDiv input:checkbox").prop('checked', false);

            // If it was checked
            if($(this).is(':checked')) {
                parentTr.addClass("selected");

                if($("tr.selected", table).length == 1)
                    $("input[name='isPrimaryGroup']:radio", parentTr).prop('checked',true);
            }
            // If it was unchecked
            else {
                parentTr.removeClass("selected");
                // If the row was marked as primary, make some other selected row as primary
                if($("input[name='isPrimaryGroup']:radio", parentTr).is(':checked')) {
                    if($("tr.selected",table).length == 0)
                        $("input[name='isPrimaryGroup']:radio", parentTr).prop('checked',false);
                    else {
                        $.each($("tr.selected",table), function(index, row){
                            if(index == 0) {
                                $("input[name='isPrimaryGroup']:radio", row).prop('checked',true);
                                return false;
                            }
                        });
                    }
                }
            }
        }
        
        function getClassesForProperty(property) {
        	//http://localhost:8080/RequestController?URI=http%3A%2F%2Fisi.edu%2Fintegration%2Fkarma%2Fdev%23classLink&"
        	//command=GetDomainsForDataPropertyCommand&workspaceId=WSP23
        	if(property.uri == null || property.uri == "")
        		return;
        	
        	var info = new Object();
		    info["workspaceId"] = $.workspaceGlobalInformation.id;
		    info["command"] = "GetDomainsForDataPropertyCommand";
		    info["worksheetId"] = worksheetId;
		    info["URI"] = property.uri;
		    
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
        	return result;
        }
        
        function parseClassJSON(clazz, result) {
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
        	
        	
        	result.push(ClassPropertyUI.getNodeObject(label, id, uri));
        	if(clazz.children) {
        		$.each(clazz.children, function(index, clazzChild){
                	parseClassJSON(clazzChild, result);
                });
        	}
        }
        
        function parsePropertyJSON(prop, result) {
        	var label = prop.data;
        	var id = prop.metadata.URIorId;
        	var uri = prop.metadata.URIorId;
        	result.push(ClassPropertyUI.getNodeObject(label, id, uri));
        	if(prop.children) {
        		$.each(prop.children, function(index, propChild){
        			parsePropertyJSON(propChild, result);
                });
        	}
        }
        
        function getPropertiesForClass(thisClass) {
        	if(thisClass.uri == null || thisClass.uri == "")
        		return;
        	
        	//http://localhost:8080/RequestController?URI=http%3A%2F%2Flod.isi.edu%2Fontology%2Fsyllabus%2FLecture1&command=GetDataPropertiesForClassCommand&workspaceId=WSP23
        	var info = new Object();
		    info["workspaceId"] = $.workspaceGlobalInformation.id;
		    info["command"] = "GetDataPropertiesForClassCommand";
		    info["worksheetId"] = worksheetId;
		    info["URI"] = thisClass.uri;
		    
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
        	return result;
        }
        
        function getClasses() {
        	//http://localhost:8080/RequestController?command=GetOntologyClassHierarchyCommand&worksheetId=WS1&workspaceId=WSP23
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
        	return result;
        }
        
        function getClassLabels() {
        	var classes = classAndPropertyListJson["elements"][0]["classList"];
        	var classLabels = [];
        	
        	$.each(classes, function(index, clazz){
        		classLabels.push(clazz.label);
            });
        	
        	return classLabels;
        }
        
        function getPropertyLabels() {
        	return classAndPropertyListJson["elements"][0]["propertyList"];
        }
        
        function getProperties() {
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
		                	var label = prop.data;
		                	var id = prop.metadata.URIorId;
		                	var uri = prop.metadata.URIorId;
		                	result.push(ClassPropertyUI.getNodeObject(label, id, uri));
		                });
		            },
		        error :
		            function (xhr, textStatus) {
		                alert("Error occured while fetching properties: " + textStatus);
		            }
		    });
        	return result;
        }
        
        function hideSemanticTypeEditOptions() {
        	var table = $("#semanticTypesTable");
        	var parentTrTag = $(this).parents("tr");
        	 $("tr", table).removeClass('currentEditRow');
             $("td.CRFSuggestedText", parentTrTag).text("");
             $("tr.editRow", table).remove();
             
             $(".editButton", parentTrTag).show();
             $(".hideButton", parentTrTag).hide();
        }
        function showSemanticTypeEditOptions() {
            var table = $("#semanticTypesTable");
            var parentTrTag = $(this).parents("tr");
            $("tr", table).removeClass('currentEditRow');
            $("td.CRFSuggestedText", parentTrTag).text("");

            $(".editButton", parentTrTag).hide();
            $(".hideButton", parentTrTag).show();
            
            
            $(parentTrTag).addClass("currentEditRow");

            // Automatically select the row
            if(!$("input[name='currentSemanticTypeCheckBoxGroup']:checkbox", parentTrTag).is(':checked')) {
                $("input[name='currentSemanticTypeCheckBoxGroup']:checkbox", parentTrTag).prop('checked', true);
                $(parentTrTag).addClass("selected");

                if($("tr.selected", table).length == 1)
                    $("input[name='isPrimaryGroup']:radio", parentTrTag).prop('checked',true);
            }


            if(classAndPropertyListJson == null){
                alert("Class and property list not yet loaded from the server!");
                return false;
            }

            var classArray = classAndPropertyListJson["elements"][0]["classList"];
            var propertyArray = classAndPropertyListJson["elements"][0]["propertyList"];

            // Remove any existing edit window open for other semantic type
            $("tr.editRow", table).remove();

            var classPropertyUI = new ClassPropertyUI("semanticTypeEdit", 
            		getClassesForProperty, getPropertiesForClass, 
            		getClasses, getProperties,
            		false,
            		75);
            
            if($(parentTrTag).data("ResourceType") == "Class") {
                var classLabel = $(parentTrTag).data("DisplayLabel");
                var classUri = $(parentTrTag).data("FullType");
                classPropertyUI.setDefaultClass(classLabel, classUri, classUri);
                //defaultProperty = "";
            } else {
               defaultClass = $(parentTrTag).data("DisplayDomainLabel");
               var classUri = $(parentTrTag).data("Domain");
               classPropertyUI.setDefaultClass(defaultClass, classUri, classUri);
               var defaultProperty = $(parentTrTag).data("DisplayLabel");
               var defaultPropertyUri = $(parentTrTag).data("FullType");
               classPropertyUI.setDefaultProperty(defaultProperty, defaultPropertyUri, defaultPropertyUri);
            }
            
            
            classPropertyUIDiv = classPropertyUI.generateJS();
            var editTr = $("<tr>").addClass("editRow")
            	.append($("<td>")
            			.attr("colspan", "4")
            			.append(classPropertyUIDiv)
            	);
            
            classPropertyUI.onClassSelect(validateClassInputValue);
            classPropertyUI.onPropertySelect(validatePropertyInputValue);
            
            editTr.insertAfter(parentTrTag);
            editTr.addClass("currentEditRow");
            editTr.data("editRowObject", parentTrTag);
        }
        
        function doesPropertyExist(inputVal) {
        	var propertyMap = classAndPropertyListJson["elements"][0]["propertyMap"];
            
            var found = false;
            var uri = "";
            var properCasedKey = "";
            $.each(propertyMap, function(index, prop){
                for(var key in prop) {
                    if(prop.hasOwnProperty(key)) {
                        if(key.toLowerCase() == inputVal.toLowerCase()) {
                            found = true;
                            uri = prop[key];
                            properCasedKey = key;
                        }
                    }
                }
            });
            return {"found": found, "uri": uri, "properCasedKey": properCasedKey};
        }
        
        function validatePropertyInputValue(propertyData) {
        	var inputVal = propertyData.label;
            
            hideError();

            $("table#semanticTypesTable tr").removeClass("fixMe");

            var foundObj = doesPropertyExist(inputVal);
            var found = foundObj.found;
            var uri = foundObj.uri;
            var properCasedKey = foundObj.properCasedKey;
            

            if(!found && $.trim(inputVal) != "") {
                showError("Input data property not valid!");
                return false;
            }

            var rowToChange = $(classPropertyUIDiv).parents("tr.editRow").data("editRowObject");
            var displayLabel = "";

            if($(rowToChange).data("ResourceType") == "Class") {
                if($.trim(inputVal) == "")
                    return false;
                // existing fullType (which was a class) becomes the domain of the chosen data property. So changing from class sem type to data prop sem type
                var domain = $(rowToChange).data("FullType");
                var displayDomainLabel = $(rowToChange).data("DisplayLabel");
                $(rowToChange).data("FullType",uri).data("DisplayLabel",properCasedKey)
                    .data("Domain", domain).data("DisplayDomainLabel",displayDomainLabel)
                    .data("ResourceType","DataProperty");

                displayLabel = "<span class='italic'>" + $(rowToChange).data("DisplayLabel") + "</span> of " + $(rowToChange).data("DisplayDomainLabel");
            } else {
                // Special case when the property input box is empty (data property sem type changed to class sem type)
                if($.trim(inputVal) == "" && $(rowToChange).data("Domain") != "") {
                    var newFullType = $(rowToChange).data("Domain");
                    var newDisplayLabel = $(rowToChange).data("DisplayDomainLabel");

                    $(rowToChange).data("ResourceType", "Class").data("FullType",newFullType).data("DisplayLabel", newDisplayLabel).data("Domain","").data("DisplayDomainLabel","");
                    displayLabel = $(rowToChange).data("DisplayLabel");
                } else {
                    $(rowToChange).data("FullType",uri).data("DisplayLabel",properCasedKey);
                    displayLabel = "<span class='italic'>" + $(rowToChange).data("DisplayLabel") + "</span> of " + $(rowToChange).data("DisplayDomainLabel");
                }
            }
            $("label.displayLabel", rowToChange).html(displayLabel);

        }

        function doesClassExist(inputVal) {
        	var classMap = classAndPropertyListJson["elements"][0]["classMap"]
            
            var found = false;
            var uri = "";
            var properCasedKey = "";
            $.each(classMap, function(index, clazz){
                for(var key in clazz) {
                    if(clazz.hasOwnProperty(key)) {
                        if(key.toLowerCase() == inputVal.toLowerCase()) {
                            found = true;
                            uri = clazz[key];
                            properCasedKey = key;
                        }
                    }
                }
            });
            return {"found": found, "uri": uri, "properCasedKey": properCasedKey};
        }
        
        function validateClassInputValue(classData) {
        	var inputVal = classData.label;
        	var updateLabels = true;
        	
        	hideError();
            $("table#semanticTypesTable tr").removeClass("fixMe");

            var foundObj = doesClassExist(inputVal);
            var found = foundObj.found;
            var uri = foundObj.uri;
            var properCasedKey = foundObj.properCasedKey;

            if(!found) {
               showError("Input class/instance not valid!");
                return false;
            }
            // Use the value in proper case as input value
           
            if (updateLabels) {
                var rowToChange = $(classPropertyUIDiv).parents("tr.editRow").data("editRowObject");
                var displayLabel = "";
                if($(rowToChange).data("ResourceType") == "Class") {
                    $(rowToChange).data("FullType",uri).data("DisplayLabel",properCasedKey);
                    displayLabel = $(rowToChange).data("DisplayLabel");
                } else {
                    // If no value has been input in the data property box, change from data property sem type to class sem type
                    if($.trim($("input.propertyInput", classPropertyUIDiv).val()) == "") {
                        $(rowToChange).data("ResourceType", "Class").data("FullType",uri).data("DisplayLabel",properCasedKey);
                        displayLabel = $(rowToChange).data("DisplayLabel");
                    } else {
                        $(rowToChange).data("Domain",uri).data("DisplayDomainLabel",properCasedKey);
                        displayLabel = "<span class='italic'>" + $(rowToChange).data("DisplayLabel") + "</span> of " + $(rowToChange).data("DisplayDomainLabel");
                    }
                }
                $("label.displayLabel", rowToChange).html(displayLabel);
            }

        }
        
        function hide() {
        	dialog.modal('hide');
        }
        
        function show(wsId, colId, colTitle) {
        	worksheetId = wsId;
        	columnId = colId;
        	columnTitle = colTitle;
        	dialog.modal({keyboard:true, show:true});
        };
        
        
        return {	//Return back the public methods
        	show : show,
        	init : init
        };
    };

    function getInstance() {
    	if( ! instance ) {
    		instance = new PrivateConstructor();
    		instance.init();
    	}
    	return instance;
    }
   
    return {
    	getInstance : getInstance
    };
    
})();



var IncomingOutgoingLinksDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
    	var dialog = $("#incomingOutgoingLinksDialog");
    	var worksheetId, columnId, alignmentId, linkType;
    	var columnLabel, columnUri, columnDomain;
    	
    	var selectedClass, selectedProperty;
    	
    	function init() {
    		//Initialize what happens when we show the dialog
    		dialog.on('show.bs.modal', function (e) {
				hideError();
				selectedClass = {label:"", id:"", uri:""};
				selectedProperty = {label:"", id:"", uri:""};
				setLinkLabel();
				
				$("div.main", dialog).empty();
				
				var classPropertyUI = new ClassPropertyUI("incomingOutgoingLinksDialog_inner",  
						getExistingClassNodes, getPropertyForClass, 
						getAllClassNodes, getAllProperties,
						true,
						200);
	            
	            classPropertyUI.onClassSelect(selectClassInputValue);
	            classPropertyUI.onPropertySelect(selectPropertyInputValue);
	            classPropertyUI.setClassRefresh(false);
	            
				if(linkType == "incoming") {
					classPropertyUI.setClassLabel("From Class");
				} else {
					classPropertyUI.setClassLabel("To Class");
				}
				
				$("div.main", dialog).append(classPropertyUI.generateJS());
				

	        	$("#incomingOutgoingLinksDialog_title", dialog).text("Add " + linkType + 
	        			" link for " + columnLabel);
			});
			
			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function (e) {
				e.preventDefault();
				saveDialog(e);
			});
    	}
    	
    	function selectClassInputValue(clazz) {
    		selectedClass = clazz;
    		setLinkLabel();
    	}
    	
    	function selectPropertyInputValue(prop) {
    		selectedProperty = prop;
    		setLinkLabel();
    	}
    	
    	function setLinkLabel() {
    		var direction = (linkType == "incoming")? "from" : "to";
    		$("#finalLink", dialog).text("Add link '" + selectedProperty.label + "' "  + direction + " '" + selectedClass.label + "'");
    	}
    	
    	function getExistingClassNodes() {
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
    	                result = parseClassList(json, true);
    	            },
    	        error :
    	            function (xhr, textStatus) {
    	                alert("Error occured while getting nodes list!");
    	            }
    	    });
    	    return result;
    	}
    	
    	
    	function getAllClassNodes() {
    		var info = new Object();
    	    info["workspaceId"] = $.workspaceGlobalInformation.id;
    	    info["command"] = "GetInternalNodesListOfAlignmentCommand";
    	    info["alignmentId"] = alignmentId;
    	    
            info["nodesRange"] = "allGraphNodes";
            var result = [];
            $.ajax({
            	url: "RequestController",
            	type: "POST",
            	data: info,
            	dataType: "json",
            	async : false,
            	complete: 
            		function(xhr, textStatus) {
            			var json = $.parseJSON(xhr.responseText);
            			result = parseClassList(json, true);
            		},
            	error:
            		function(xhr, textStatus) {
            			alert("Error occured while getting nodes list!");
            		}
            });
            return result;
    	}
    	
    	function parseClassList(json, sortNodes) {
    		var nodes = [];
    		$.each(json["elements"], function(index, element) {
    	        if(element["updateType"] == "InternalNodesList") {
    	            if(sortNodes) {
    		        	element["nodes"].sort(function(a,b) {
    		                return a["nodeLabel"].toUpperCase().localeCompare(b["nodeLabel"].toUpperCase());
    		            });
    	            }
    	            
    	            $.each(element["nodes"], function(index2, node) {
    	            	var nodeData = ClassPropertyUI.getNodeObject(node["nodeLabel"], node["nodeId"], node["nodeUri"]);
    	            	nodes.push(nodeData);
    	            });
    	        }
    	    });
    		return nodes;
    	}
    	
    	
    	function getAllProperties() {
    		var info = new Object();
    	    info["workspaceId"] = $.workspaceGlobalInformation.id;
    	    info["command"] = "GetDataPropertyHierarchyCommand";
    	    
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
    	                result = parseDataPropertyList(json, true);
    	            },
    	        error :
    	            function (xhr, textStatus) {
    	                alert("Error occured while getting property list!");
    	            }
    	    });
    	    return result;
    	}
    	
    	function getPropertyForClass(selectedClass) {
    		var info = new Object();
    	    info["workspaceId"] = $.workspaceGlobalInformation.id;
    	    info["command"] = "GetLinksOfAlignmentCommand";
    	    info["alignmentId"] = alignmentId;
    	    var startNodeClass = columnDomain;
    	    
    	    info["linksRange"] = "linksWithDomainAndRange";
    	    if(linkType == "incoming") {
    	    	info["domain"] = selectedClass.uri;
    	    	info["range"] = startNodeClass;
    	    } else if(linkType == "outgoing") {
    	    	info["domain"] = startNodeClass;
    	    	info["range"] = selectedClass.uri;
    	    }
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
    	                result = parseDataPropertyList(json, true);
    	            },
    	        error :
    	            function (xhr, textStatus) {
    	                alert("Error occured while getting property list!");
    	            }
    	    });
    	    return result;
    	}
    	
    	function parseDataPropertyList(json, sortNodes) {
    		var nodes = [];
    		$.each(json["elements"], function(index, element) {
    	        if(element["updateType"] == "DataPropertyListUpdate" || element["updateType"] == "DataPropertiesForClassUpdate") {
    	            if(sortNodes) {
    		        	element["data"].sort(function(a,b) {
    		                return a["data"].toUpperCase().localeCompare(b["data"].toUpperCase());
    		            });
    	            }
    	            
    	            $.each(element["data"], function(index2, node) {
    	            	var label = node.data;
    	            	var uri = node.metadata.URIorId;
    	            	var p = ClassPropertyUI.getNodeObject(label, uri, uri);
    	            	nodes.push(p);
    	            });
    	        } else if(element["updateType"] == "LinksList") {
    	        	 if(sortNodes) {
    	 	        	element["edges"].sort(function(a,b) {
    	 	                return a["edgeLabel"].toUpperCase().localeCompare(b["edgeLabel"].toUpperCase());
    	 	            });
    	             }
    	             
    	             $.each(element["edges"], function(index2, node) {
    	            	 var p = ClassPropertyUI.getNodeObject(node["edgeLabel"], 
    	            			 node["edgeId"], node["edgeId"]);
    	             	 nodes.push(p);
    	                 
    	             });
    	        }
    	    });
    		return nodes;
    	}

		function hideError() {
			$("div.error", dialog).hide();
		}
		
		function showError(err) {
			if(err) {
				$("div.error", dialog).text(err);
			}
			$("div.error", dialog).show();
		}
        
        function saveDialog(e) {
        	var startNode = columnId;
        	
        	if(selectedClass.label == "") {
        		showError("Please select the class");
        		return false;
        	}
        	if(selectedProperty.label == "") {
        		showError("Please select the property");
        		return false;
        	}
        	
        	 var info = new Object();
        	 info["workspaceId"] = $.workspaceGlobalInformation.id;
        	 info["command"] = "ChangeInternalNodeLinksCommand";

        	 // Prepare the input for command
        	 var newInfo = [];
        	 
        	// Put the old edge information
        	var initialEdges = [];
        	newInfo.push(getParamObject("initialEdges", initialEdges, "other"));
        	    
        	newInfo.push(getParamObject("alignmentId", alignmentId, "other"));
        	newInfo.push(getParamObject("worksheetId", worksheetId, "worksheetId"));
        	 
        	 // Put the new edge information
        	 var newEdges = [];
        	 var newEdgeObj = {};
        	 
        	 var source, target;
        	 var property = selectedProperty.id;
        	    
        	if(linkType == "incoming") {
        		target = startNode;
        		source = selectedClass.id;
        	} else if(linkType == "outgoing") {
        		source = startNode;
        		target = selectedClass.id;
        	} else {
        		alert("Invalid linkType: " + linkType);
        		return;
        	}
        	
        	newEdgeObj["edgeSourceId"] = source;
            newEdgeObj["edgeTargetId"] = target;
            newEdgeObj["edgeId"] = property;
            newEdges.push(newEdgeObj);
            
        	newInfo.push(getParamObject("newEdges", newEdges, "other"));
        	info["newInfo"] = JSON.stringify(newInfo);
        	info["newEdges"] = newEdges;
        	
        	showLoading(worksheetId);
            var returned = $.ajax({
                url: "RequestController",
                type: "POST",
                data : info,
                dataType : "json",
                complete :
                    function (xhr, textStatus) {
                        var json = $.parseJSON(xhr.responseText);
                        parse(json);
                        hideLoading(worksheetId);
                        hide();
                    },
                error :
                    function (xhr, textStatus) {
                        alert("Error occured while getting nodes list!");
                        hideLoading(worksheetId);
                        hide();
                    }
            });
        };
        
        function hide() {
        	dialog.modal('hide');
        }
        
        function show(wsId, colId, alignId,
        		colLabel, colUri, colDomain, type) {
        	worksheetId = wsId;
        	columnId = colId;
        	alignmentId = alignId;
        	
        	columnLabel = colLabel;
        	columnUri = colUri;
        	columnDomain = colDomain;
        	
        	linkType = type;
        	dialog.modal({keyboard:true, show:true});
        	
        };
        
        
        return {	//Return back the public methods
        	show : show,
        	init : init
        };
    };

    function getInstance() {
    	if( ! instance ) {
    		instance = new PrivateConstructor();
    		instance.init();
    	}
    	return instance;
    }
   
    return {
    	getInstance : getInstance
    };
    
})();