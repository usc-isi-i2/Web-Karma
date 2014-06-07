/**
 * ==================================================================================================================
 * 
 * 				Diloag to Set the Semantic Type of a column node
 * 
 * ==================================================================================================================
 */

var SetSemanticTypeDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
    	var dialog = $("#setSemanticTypeDialog");
    	var worksheetId;
    	var columnId;
    	var columnTitle;
    	var existingTypes, selectedPrimaryRow, classAndPropertyListJson;
    	var classPropertyUIDiv;
    	var classUI, propertyUI;
    	
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
			    console.log(typeJsonObject);
			    existingTypes = typeJsonObject["SemanticTypesArray"];
			    
			    var CRFInfo = typeJsonObject["FullCRFModel"];
			    
			    // Populate the table with existing types and CRF suggested types
			    $.each(existingTypes, function(index, type){
			        // Take care of the special meta properties that are set through the advanced options
			    	if (type["isMetaProperty"]) {
			    		if (type["DisplayLabel"] == "km-dev:classLink") {
			    			addUriSemanticType(type["DisplayDomainLabel"], type["DomainUri"], type["DomainId"]);
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
			    
			    addEmptyUriSemanticType();
			    
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
				var propertyArray = getPropertyInstanceLabels();
				
				$('.typeahead').typeahead('destroy');
				
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
			if($("#isSubclassOfClass").prop("checked")) {
	        	var foundObj = doesClassExist($("input#isSubclassOfClassTextBox", dialog).val());
	        	if(!foundObj.found) {
	        		showError("Class for 'specifies class for node' does not exist");
	        		return false;
	        	}
			}
			
			if($("#isSpecializationForEdge").prop("checked")) {
				var foundObj = doesExistingPropertyExist($("input#isSpecializationForEdgeTextBox", dialog).val());
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
		        var domainUri = $(row).data("DomainUri");
		        var domainId = $(row).data("DomainId");

		        // Check if the user selected a fake semantic type object
		        if(domainUri == "fakeDomainURI" || fullType == "fakePropertyURI") {
		            $(row).addClass("fixMe");
		           showError("Semantic type not valid!");
		            notValid = true;
		            return false;
		        }
		        // Check if the type already exists (like the user had same type in a previous row)
		        var exists = false;
		        $.each(existingTypes, function(index2, type){
		            if(type["DomainUri"] == domainUri && fullType == type["FullType"]) {
		                exists = true;
		                return false;
		            }
		        });
		        if(exists)
		            return false;

		        // Create a new object and push it into the array
		        var newType = new Object();
		        newType["FullType"] = fullType;
		        newType["DomainUri"] = domainUri;
		        newType["DomainId"] = domainId;
		        newType["DomainLabel"] = $(row).data("DisplayDomainLabel");
		        
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
            var semTypesArray = getCurrentSelectedTypes();
            if($("#isSubclassOfClass").prop("checked") || $("#isSpecializationForEdge").prop("checked") || 
            		(semTypesArray.length == 1 && semTypesArray[0]["FullType"] == "http://isi.edu/integration/karma/dev#classLink")) {
            	info["command"] = "SetMetaPropertyCommand";
            	var propValue;
            	
            	if(semTypesArray != null && semTypesArray.length == 1 && semTypesArray[0]["FullType"] == "http://isi.edu/integration/karma/dev#classLink") {
					propValue = semTypesArray[0]["DomainLabel"] ;
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

            console.log(info);
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
            fakeSemType["DomainUri"] = "fakeDomainURI";
            fakeSemType["DomainId"] = "fakeDomainID";
            fakeSemType["DisplayLabel"] = "property";
            fakeSemType["DisplayDomainLabel"] = "Class";
            // Add it to the table
            addSemTypeObjectToCurrentTable(fakeSemType, false, false);
        }
        
        function addUriSemanticType(domainLabel, domainUri, domainId) {
        	var type = new Object();
            type["FullType"] = "http://isi.edu/integration/karma/dev#classLink";
            type["DomainId"] = domainId;
            type["DomainUri"] = domainUri;
            type["DisplayLabel"] = "km-dev:classLink";
            type["DisplayDomainLabel"] = domainLabel;
            type["isPrimary"] = true;
            // Add it to the table
            addSemTypeObjectToCurrentTable(type, true, false);
        }
        
        function addEmptyUriSemanticType() {
            // Create a fake sem type object to how in the table
            var fakeSemType = new Object();
            fakeSemType["FullType"] = "http://isi.edu/integration/karma/dev#classLink";
            fakeSemType["DomainUri"] = "fakeDomainURI";
            fakeSemType["DomainId"] = "fakeDomainID";
            fakeSemType["DisplayLabel"] = "km-dev:classLink";
            fakeSemType["DisplayDomainLabel"] = "Class";
            // Add it to the table
            addSemTypeObjectToCurrentTable(fakeSemType, false, false);
        }
        
        function addSemTypeObjectToCurrentTable(semTypeObject, isSelected, isCrfModelSuggested) {
            var table = $("#semanticTypesTable");

            // Check if it is eligible to be added to the table
            var isValid = true;
            $.each($("tr", table), function(index, row){
                if($(row).data("FullType") == semTypeObject["FullType"] && $(row).data("DomainUri") == semTypeObject["DomainUri"]) {
                    // We allow multiple fake semantic type objects to be added
                    if(!(semTypeObject["FullType"] == "fakePropertyURI" && semTypeObject["DomainUri"] == "fakeDomainURI"))
                        isValid = false;
                }
            });
            if(!isValid)
                return false;

            // Add it to the table
            var displayLabel = "";
            var property = semTypeObject["DisplayLabel"];
            if(property == "km-dev:classLink")
            	property = "uri";
            if(semTypeObject["DomainUri"].length == 0 || semTypeObject["DomainUri"] == "")
                displayLabel = property;
            else
                displayLabel = "<span class='italic'>" + property+ "</span> of " + semTypeObject["DisplayDomainLabel"];

            var trTag = $("<tr>").addClass("semTypeRow")
                .data("FullType", semTypeObject["FullType"])
                .data("DomainUri", semTypeObject["DomainUri"])
                .data("DomainId", semTypeObject["DomainId"])
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

            if(semTypeObject["DomainUri"].length == 0 || semTypeObject["DomainUri"] == "")
                trTag.data("ResourceType", "Class");
            else
                trTag.data("ResourceType", "DataProperty");

            table.append(trTag);
        }
        
        function semanticTypesTableCheckBoxHandler() {
        	console.log("semanticTypesTableCheckBoxHandler");
           // var existingTypesArray = existingTypes;
            var parentTr = $(this).parents("tr");
            var table = $("table#semanticTypesTable");

            // Deselect any meta property checkbox
            $("div#semanticTypesAdvacedOptionsDiv input:checkbox").prop('checked', false);

            // If it was checked
            if($(this).is(':checked')) {
            	//if(parentTr.data("DisplayLabel") == "km-dev:classLink"){
                	var rows = $("tr.selected", table);
                	for(var i=0; i<rows.length; i++) {
                		var row = rows[i];
                		if($(row).data("DisplayLabel") == "km-dev:classLink" || parentTr.data("DisplayLabel") == "km-dev:classLink") {
                			 $("input[name='currentSemanticTypeCheckBoxGroup']:checkbox", row).prop('checked',false);
                			 $("input[name='isPrimaryGroup']:radio", row).prop('checked',false);
                			 $(row).removeClass("selected");
                		}
                		
                	}
                	$(this).prop('checked',true);
                //}
                parentTr.addClass("selected");
                var numRows = $("tr.selected", table).length;
                if(numRows == 1)
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
        
        function getClasses() {
        	var classes = getAllClasses(worksheetId);
        	var result = [];
	       	 $.each(classes, function(index, clazz){
	       		 result.push(ClassUI.getNodeObject(clazz.label, clazz.id, clazz.uri));
	       	 });
	       	return result;
        }
        
        function getClassesForProperty(property) {
        	var classes = getAllClassesForProperty(worksheetId, property.uri);
        	var result = [];
        	 $.each(classes, function(index, clazz){
        		 result.push(ClassUI.getNodeObject(clazz.label, clazz.id, clazz.uri));
        	 });
        	return result;
        }
      
        function getProperties() {
        	var props = getAllDataProperties(worksheetId);
        	var result = [];
	       	 $.each(props, function(index, prop){
	       		 result.push(PropertyUI.getNodeObject(prop.label, prop.id, prop.uri));
	       	 });
	       	return result;
        }
        
        function getPropertiesForClass(thisClass) {
        	var props = getAllPropertiesForClass(worksheetId, thisClass.uri);
        	var result = [];
	       	 $.each(props, function(index, prop){
	       		 result.push(PropertyUI.getNodeObject(prop.label, prop.id, prop.uri));
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
        
        function getPropertyInstanceLabels() {
        	var existingLinksMap = classAndPropertyListJson["elements"][0]["existingDataPropertyInstances"];
        	var arr = [];
        	//arr.push("test--test2--test");
        	$.each(existingLinksMap, function(index, prop) {
            	arr.push(prop["label"]);
            	
            });
            return arr;
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
            var edittd = $("<td>").attr("colspan", "4");
            var editTr = $("<tr>").addClass("editRow").append(edittd);
            editTr.insertAfter(parentTrTag);
            editTr.addClass("currentEditRow");
            editTr.data("editRowObject", parentTrTag);
            
            var showPropertiesList = true;
            var classFuncTop = getClassesForProperty, 
            	classFuncBottom = getClasses;
            if($(parentTrTag).data("DisplayLabel") == "km-dev:classLink") {
            	showPropertiesList = false;
            	classFuncTop = null;
            }
            classUI = new ClassUI("semanticTypeEditClass", classFuncTop, classFuncBottom, 100);
            propertyUI = new PropertyUI("semanticTypeEditProperty", getPropertiesForClass, getProperties, 100);
            
            classUI.setHeadings("Classes with Selected Property", "All Classes");
            propertyUI.setHeadings("Properties of Selected Class", "All Properties");
           
            if($(parentTrTag).data("ResourceType") == "Class") {
                var classLabel = $(parentTrTag).data("DisplayLabel");
                var classUri = $(parentTrTag).data("DomainUri");
                classUI.setDefaultClass(classLabel, classUri, classUri);
                propertyUI.setSelectedClass(classLabel, classUri, classUri);
                //defaultProperty = "";
            } else {
               defaultClass = $(parentTrTag).data("DisplayDomainLabel");
               var classUri = $(parentTrTag).data("DomainUri");
               classUI.setDefaultClass(defaultClass, classUri, classUri);
               propertyUI.setSelectedClass(defaultClass, classUri, classUri);
               var defaultProperty = $(parentTrTag).data("DisplayLabel");
               var defaultPropertyUri = $(parentTrTag).data("FullType");
               propertyUI.setDefaultProperty(defaultProperty, defaultPropertyUri, defaultPropertyUri);
               classUI.setSelectedProperty(defaultProperty, defaultPropertyUri, defaultPropertyUri);
            }
             
            classUI.onClassSelect(validateClassInputValue);
            propertyUI.onPropertySelect(validatePropertyInputValue);
            
            classPropertyUIDiv = $("<div>").addClass("row").attr("id", "semanticTypeEdit");
            var classDiv = $("<div>").addClass("col-sm-6");
            var propDiv = $("<div>").addClass("col-sm-6");
            
            classPropertyUIDiv.append(propDiv);
            classPropertyUIDiv.append(classDiv);
            edittd.append(classPropertyUIDiv);
            
           classUI.generateJS(classDiv, true);
           if(showPropertiesList) {
        	   propertyUI.generateJS(propDiv, true);
           }
        }
        
        function doesExistingPropertyExist(propValue) {
        	var existingLinksMap = classAndPropertyListJson["elements"][0]["existingDataPropertyInstances"];
        	var found = false;
        	var id = "";
            $.each(existingLinksMap, function(index, prop) {
                if (prop["label"] == propValue) {
                    id = prop["id"];
                    found = true;
                }
            });
            return {"found": found, "id": id};
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

            classUI.refreshClassDataTop(propertyData.label, propertyData.id, propertyData.uri);
            
            var rowToChange = $(classPropertyUIDiv).parents("tr.editRow").data("editRowObject");
            if(rowToChange != null) {
	            var displayLabel = "";
	
	            if($(rowToChange).data("ResourceType") == "Class") {
	                if($.trim(inputVal) == "")
	                    return false;
	                // existing fullType (which was a class) becomes the domain of the chosen data property. So changing from class sem type to data prop sem type
	                var domain = $(rowToChange).data("FullType");
	                var displayDomainLabel = $(rowToChange).data("DisplayLabel");
	                $(rowToChange).data("FullType",uri).data("DisplayLabel",properCasedKey)
	                    .data("DomainId", domain).data("DisplayDomainLabel",displayDomainLabel)
	                    .data("ResourceType","DataProperty");
	
	                displayLabel = "<span class='italic'>" + $(rowToChange).data("DisplayLabel") + "</span> of " + $(rowToChange).data("DisplayDomainLabel");
	            } else {
	                // Special case when the property input box is empty (data property sem type changed to class sem type)
	                if($.trim(inputVal) == "" && $(rowToChange).data("DomainId") != "") {
	                    var newFullType = $(rowToChange).data("DomainId");
	                    var newDisplayLabel = $(rowToChange).data("DisplayDomainLabel");
	
	                    $(rowToChange).data("ResourceType", "Class").data("FullType",newFullType).data("DisplayLabel", newDisplayLabel).data("DomainId","").data("DomainUri","").data("DisplayDomainLabel","");
	                    displayLabel = $(rowToChange).data("DisplayLabel");
	                } else {
	                    $(rowToChange).data("FullType",uri).data("DisplayLabel",properCasedKey);
	                    displayLabel = "<span class='italic'>" + $(rowToChange).data("DisplayLabel") + "</span> of " + $(rowToChange).data("DisplayDomainLabel");
	                }
	            }
	            $("label.displayLabel", rowToChange).html(displayLabel);
            }
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
           propertyUI.refreshPropertyDataTop(classData.label, classData.id, classData.uri);
            
            if (updateLabels) {
                var rowToChange = $(classPropertyUIDiv).parents("tr.editRow").data("editRowObject");
                if(rowToChange != null) {
	                var displayLabel = "";
	                if($(rowToChange).data("ResourceType") == "Class") {
	                    $(rowToChange).data("FullType",uri).data("DisplayLabel",properCasedKey);
	                    displayLabel = $(rowToChange).data("DisplayLabel");
	                } else {
	                    // If no value has been input in the data property box, change from data property sem type to class sem type
	                	var propertyOld = $(rowToChange).data("DisplayLabel");
	                	if(propertyOld == "km-dev:classLink") propertyOld = "uri";
	                	
	                    if(propertyOld != "uri" && $.trim($("input.propertyInput", classPropertyUIDiv).val()) == "") {
	                        $(rowToChange).data("ResourceType", "Class").data("FullType",uri).data("DisplayLabel",properCasedKey);
	                        displayLabel = $(rowToChange).data("DisplayLabel");
	                    } else {
	                        $(rowToChange).data("DomainUri",uri).data("DomainId", foundObj.uri).data("DisplayDomainLabel",properCasedKey);
	                        displayLabel = "<span class='italic'>" + propertyOld + "</span> of " + $(rowToChange).data("DisplayDomainLabel");
	                    }
	                }
	                $("label.displayLabel", rowToChange).html(displayLabel);
                }
            }

        }
        
        function hide() {
        	dialog.modal('hide');
        }
        
        function show(wsId, colId, colTitle) {
        	worksheetId = wsId;
        	columnId = colId;
        	columnTitle = colTitle;
        	dialog.modal({keyboard:true, show:true, backdrop:'static'});
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

/**
 * ==================================================================================================================
 * 
 * 				Diloag to Add an Incoming / Outgoing Link for to Change a Link
 * 
 * ==================================================================================================================
 */

var IncomingOutgoingLinksDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
    	var dialog = $("#incomingOutgoingLinksDialog");
    	var worksheetId, columnId, alignmentId, linkType;
    	var columnLabel, columnUri, columnDomain, columnType;
    	
    	var selectedFromClass, selectedProperty, selectedToClass;
    	var allClasses, allProperties, selectedClasses, selectedProperties;
    	
    	var fromClassUI, propertyUI, toClassUI;
    	var changeFromNode, changeToNode, changeLink;
    	
    	function init() {
            selectedFromClass = {label:"", id:"", uri:""};
            selectedToClass = {label:"", id:"", uri:""};
            selectedProperty = {label:"", id:"", uri:""};
            
            fromClassUI = new ClassUI("incomingOutgoingLinksDialog_fromClass", getExistingClassNodes, getAllClassNodes, 200);
            toClassUI = new ClassUI("incomingOutgoingLinksDialog_toClass", getExistingClassNodes, getAllClassNodes, 200);
            propertyUI = new PropertyUI("incomingOutgoingLinksDialog_property", getPropertyForClass, getProperties, 200);
            
            fromClassUI.setHeadings("Classes in Model", "All Classes");
            toClassUI.setHeadings("Classes in Model", "All Classes");
            propertyUI.setHeadings("Compatible Properties", "All Properties");
            
            fromClassUI.setClassLabel("From Class");
            toClassUI.setClassLabel("To Class");
            
            fromClassUI.onClassSelect(onSelectFromClassInputValue);
            toClassUI.onClassSelect(onSelectToClassInputValue);
            propertyUI.onPropertySelect(onSelectPropertyInputValue);
            
            
            //Initialize what happens when we show the dialog
            dialog.on('show.bs.modal', function (e) {
                hideError();
                
                setLinkLabel();
                $("div.main", dialog).empty();
                var row = $("<div>").addClass("row");
                $("div.main", dialog).append(row);
                if(linkType == "incoming" || linkType == "changeIncoming") {
                	if(linkType == "incoming")
                		$("#incomingOutgoingLinksDialog_title", dialog).text("Add incoming link for " + columnLabel);
                	else
                		$("#incomingOutgoingLinksDialog_title", dialog).text("Change incoming link for " + columnLabel);
                	
                	var classDiv = $("<div>").addClass("col-sm-6");
                	row.append(classDiv);
                	fromClassUI.setDefaultClass(selectedFromClass.label, selectedFromClass.id, selectedFromClass.uri);
                	fromClassUI.setSelectedProperty(selectedProperty.label, selectedProperty.id, selectedProperty.uri);
                	fromClassUI.generateJS(classDiv, true);
                	
                	var propertyDiv = $("<div>").addClass("col-sm-6");
                	row.append(propertyDiv);
                	propertyUI.setDefaultProperty(selectedProperty.label, selectedProperty.id, selectedProperty.uri);
                	propertyUI.setSelectedClass(selectedFromClass.label, selectedFromClass.id, selectedFromClass.uri);
                	propertyUI.generateJS(propertyDiv, true);
                } else if(linkType == "outgoing" || linkType == "changeOutgoing") {
                	if(linkType == "outgoing")
                		$("#incomingOutgoingLinksDialog_title", dialog).text("Add outgoing link for " + columnLabel);
                	else
                		$("#incomingOutgoingLinksDialog_title", dialog).text("Change outgoing link for " + columnLabel);
                	
                	var classDiv = $("<div>").addClass("col-sm-6");
                	row.append(classDiv);
                	toClassUI.setDefaultClass(selectedToClass.label, selectedToClass.id, selectedToClass.uri);
                	toClassUI.setSelectedProperty(selectedProperty.label, selectedProperty.id, selectedProperty.uri);
                	toClassUI.generateJS(classDiv, true);
                	
                	var propertyDiv = $("<div>").addClass("col-sm-6");
                	row.append(propertyDiv);
                	propertyUI.setDefaultProperty(selectedProperty.label, selectedProperty.id, selectedProperty.uri);
                	propertyUI.setSelectedClass(selectedToClass.label, selectedToClass.id, selectedToClass.uri);
                	propertyUI.generateJS(propertyDiv, true);
                } else if(linkType == "changeLink") {
                	$("#incomingOutgoingLinksDialog_title", dialog).text("Change link");
                	getAllClassNodes();
                	getExistingClassNodes();
//                	var classDiv1 = $("<div>").addClass("col-sm-4");
//                	row.append(classDiv1);
//                	fromClassUI.setDefaultClass(selectedFromClass.label, selectedFromClass.id, selectedFromClass.uri);
//                	fromClassUI.setSelectedProperty(selectedProperty.label, selectedProperty.id, selectedProperty.uri);
//                	fromClassUI.generateJS(classDiv1, true);
                	
                	var propertyDiv = $("<div>").addClass("col-sm-12");
                	row.append(propertyDiv);
                	propertyUI.setDefaultProperty(selectedProperty.label, selectedProperty.id, selectedProperty.uri);
                	propertyUI.setSelectedClass(selectedFromClass.label, selectedFromClass.id, selectedFromClass.uri);
                	propertyUI.generateJS(propertyDiv, true);
                	
//                	var classDiv2 = $("<div>").addClass("col-sm-4");
//                	row.append(classDiv2);
//                	toClassUI.setDefaultClass(selectedToClass.label, selectedToClass.id, selectedToClass.uri);
//                	toClassUI.setSelectedProperty(selectedProperty.label, selectedProperty.id, selectedProperty.uri);
//                	toClassUI.generateJS(classDiv2, true);
                }
            });
            
            //Initialize handler for Save button
            //var me = this;
            $('#btnSave', dialog).on('click', function (e) {
                e.preventDefault();
                saveDialog(e);
            });
        }
    	
    	function setSelectedFromClass(id) {
    		console.log("IncomingOutgoingLinksDialog:setSelectedFromClass:" + id);
    		if(allClasses) {
    			for(var i=0; i<allClasses.length; i++) {
    				var clazz = allClasses[i];
    				var clazzElem = ClassUI.parseNodeObject(clazz);
    				var clazzLbl = clazzElem[0];
    				var clazzId = clazzElem[1];
    				var clazzUri = clazzElem[2];
    				if(clazzId == id) {
    					fromClassUI.setDefaultClass(clazzLbl, clazzId, clazzUri);
    					onSelectFromClassInputValue({"uri":clazzUri, "label":clazzLbl, "id":clazzId});
    					return;
    				}
    			}
    		} else {
    			window.setTimeout(function() {
    				setSelectedFromClass(id);
    			}, 100);
    			return;
    		}
    		if(selectedClasses) {
    			for(var i=0; i<selectedClasses.length; i++) {
    				var clazz = selectedClasses[i];
    				var clazzElem = ClassUI.parseNodeObject(clazz);
    				var clazzLbl = clazzElem[0];
    				var clazzId = clazzElem[1];
    				var clazzUri = clazzElem[2];
    				if(clazzId == id) {
    					fromClassUI.setDefaultClass(clazzLbl, clazzId, clazzUri);
    					onSelectFromClassInputValue({"uri":clazzUri, "label":clazzLbl, "id":clazzId});
    					return;
    				}
    			}
    		} else {
    			window.setTimeout(function() {
    				setSelectedFromClass(id);
    			}, 100);
    			return;
    		}
    	}
    	
    	function setSelectedToClass(id) {
    		console.log("IncomingOutgoingLinksDialog:setSelectedToClass:" + id);
    		if(allClasses) {
    			for(var i=0; i<allClasses.length; i++) {
    				var clazz = allClasses[i];
    				var clazzElem = ClassUI.parseNodeObject(clazz);
    				var clazzLbl = clazzElem[0];
    				var clazzId = clazzElem[1];
    				var clazzUri = clazzElem[2];
    				if(clazzId == id) {
    					toClassUI.setDefaultClass(clazzLbl, clazzId, clazzUri);
    					onSelectToClassInputValue({"uri":clazzUri, "label":clazzLbl, "id":clazzId});
    					return;
    				}
    			}
    		} else {
    			window.setTimeout(function() {
    				setSelectedToClass(id);
    			}, 100);
    			return;
    		}
    		if(selectedClasses) {
    			for(var i=0; i<selectedClasses.length; i++) {
    				var clazz = selectedClasses[i];
    				var clazzElem = ClassUI.parseNodeObject(clazz);
    				var clazzLbl = clazzElem[0];
    				var clazzId = clazzElem[1];
    				var clazzUri = clazzElem[2];
    				if(clazzId == id) {
    					toClassUI.setDefaultClass(clazzLbl, clazzId, clazzUri);
    					onSelectToClassInputValue({"uri":clazzUri, "label":clazzLbl, "id":clazzId});
    					return;
    				}
    			}
    		} else {
    			window.setTimeout(function() {
    				setSelectedToClass(id);
    			}, 100);
    			return;
    		}
    	}
    	
    	function setSelectedProperty(id) {
    		console.log("IncomingOutgoingLinksDialog:setSelectedProperty:" + id);
    		if(allProperties) {
    			for(var i=0; i<allProperties.length; i++) {
    				var prop = allProperties[i];
    				var propElem = PropertyUI.parseNodeObject(prop);
    				var propLbl = propElem[0];
    				var propId = propElem[1];
    				var propUri = propElem[2];
    				
    				if(propId == id) {
    					propertyUI.setDefaultProperty(propLbl, propId, propUri);
    					onSelectPropertyInputValue({"uri":propUri, "label":propLbl, "id":propId});
    					return;
    				}
    			}
    		} else {
    			window.setTimeout(function() {
    				setSelectedProperty(id);
    			}, 100);
    			return;
    		}
    		if(selectedProperties) {
    			for(var i=0; i<selectedProperties.length; i++) {
    				var prop = selectedProperties[i];
    				var propElem = PropertyUI.parseNodeObject(prop);
    				var propLbl = propElem[0];
    				var propId = propElem[1];
    				var propUri = propElem[2];
    				
    				if(propId == id) {
    					propertyUI.setDefaultProperty(propLbl, propId, propUri);
    					if(dialog.hasClass('in')) { //dialog is shown
    						onSelectPropertyInputValue({"uri":propUri, "label":propLbl, "id":propId});
    					}
    					return;
    				}
    			}
    		} else {
    			window.setTimeout(function() {
    				setSelectedProperty(id);
    			}, 100);
    			return;
    		}
    	}
    	
    	function onSelectFromClassInputValue(clazz) {
    		selectedFromClass = clazz;
    		if(dialog.hasClass('in'))  {
	    		propertyUI.refreshPropertyDataTop(clazz.label, clazz.id, clazz.uri);
	    		setLinkLabel();
    		}
    	}
    	
    	function onSelectToClassInputValue(clazz) {
    		selectedToClass = clazz;
    		if(dialog.hasClass('in')) {
	    		propertyUI.refreshPropertyDataTop(clazz.label, clazz.id, clazz.uri);
	    		setLinkLabel();
    		}
    	}
    	
    	function onSelectPropertyInputValue(prop) {
    		selectedProperty = prop;
    		setLinkLabel();
    	}
    	
    	function setLinkLabel() {
//    		var direction = (linkType == "incoming")? "from" : "to";
//    		$("#finalLink", dialog).text("Add link '" + selectedProperty.label + "' "  + direction + " '" + selectedClass.label + "'");
    	}
    	
    	function getExistingClassNodes() {
    		var classes = getClassesInModel(alignmentId);
        	var result = [];
	       	 $.each(classes, function(index, clazz){
	       		 result.push(ClassUI.getNodeObject(clazz.label, clazz.id, clazz.uri));
	       	 });
	       	selectedClasses = result;
	       	return result;
    	}
    	
    	
    	function getAllClassNodes() {
    		var classes = getAllClasses(worksheetId);
        	var result = [];
	       	 $.each(classes, function(index, clazz){
	       		 result.push(ClassUI.getNodeObject(clazz.label, clazz.id, clazz.uri));
	       	 });
	       	allClasses = result;
	       	return result;
    	}
    	
    	function getProperties() {
    		var props
    		if(columnType == "ColumnNode")
    			props = getAllDataProperties(worksheetId);
    		else
    			props = getAllObjectProperties(alignmentId);
        	var result = [];
	       	 $.each(props, function(index, prop){
	       		 result.push(PropertyUI.getNodeObject(prop.label, prop.id, prop.uri));
	       	 });
	       	allProperties = result;
	       	return result;
    	}
    	
    	function getPropertyForClass(selectedClass) {
    		var domain, range;
    	    var startNodeClass = columnDomain;
    	    if(columnType == "ColumnNode")
    	    	startNodeClass = "";
    	    if(linkType == "incoming" || linkType == "changeIncoming" || linkType == "changeLink") {
    	    	domain = selectedClass.uri;
    	    	range = startNodeClass;
    	    } else { //if(linkType == "outgoing" || linkType == "changeOutgoing") {
    	    	domain = startNodeClass;
    	    	range = selectedClass.uri;
    	    }
    	    
    	    var props = getAllPropertiesForDomainRange(alignmentId, domain, range);
    	    var result = [];
	       	 $.each(props, function(index, prop){
	       		 result.push(PropertyUI.getNodeObject(prop.label, prop.id, prop.uri));
	       	 });
	       	selectedProperties = result;
	       	return result;
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
        	var startNodeUri = columnUri;
        	
        	if(selectedFromClass.label == "" && (linkType == "incoming" || linkType == "changeIncoming" || linkType == "changeLink")) {
        		showError("Please select the from class");
        		return false;
        	}
        	
        	if(selectedToClass.label == "" && (linkType == "outgoing" || linkType == "changeOutgoing" || linkType == "changeLink")) {
        		showError("Please select the to class");
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
        	 if(linkType == "changeIncoming" || linkType == "changeOutgoing" || linkType == "changeLink") {
        	    var oldEdgeObj = {};
        	    oldEdgeObj["edgeSourceId"] = changeFromNode;
        	    oldEdgeObj["edgeTargetId"] = changeToNode;
        	    oldEdgeObj["edgeId"] = changeLink;
        	    initialEdges.push(oldEdgeObj);
        	 }
        	    
        	newInfo.push(getParamObject("initialEdges", initialEdges, "other"));
        	    
        	newInfo.push(getParamObject("alignmentId", alignmentId, "other"));
        	newInfo.push(getParamObject("worksheetId", worksheetId, "worksheetId"));
        	 
        	 // Put the new edge information
        	 var newEdges = [];
        	 var newEdgeObj = {};
        	 
        	 var source, target, sourceUri, targetUri;
        	 var property = selectedProperty.id;
        	    
        	if(linkType == "incoming" || linkType == "changeIncoming") {
        		target = startNode;
        		targetUri = startNodeUri;
        		source = selectedFromClass.id;
        		sourceUri = selectedFromClass.uri;
        	} else if(linkType == "outgoing" || linkType == "changeOutgoing") {
        		source = startNode;
        		sourceUri = startNodeUri;
        		target = selectedToClass.id;
        		targetUri = selectedToClass.uri;
        	} else if(linkType == "changeLink") {
        		source = selectedFromClass.id;
        		sourceUri = selectedFromClass.uri;
        		target = selectedToClass.id;
        		targetUri = selectedToClass.uri;
        	} else {
        		alert("Invalid linkType: " + linkType);
        		return;
        	}
        	
        	newEdgeObj["edgeSourceId"] = source;
        	newEdgeObj["edgeSourceUri"] = sourceUri;
            newEdgeObj["edgeTargetId"] = target;
            newEdgeObj["edgeTargetUri"] = targetUri;
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
        		colLabel, colUri, colDomain, colType, type, changeFrom, changeTo, changeLinkUri) {
        	worksheetId = wsId;
        	columnId = colId;
        	alignmentId = alignId;
        	
        	columnLabel = colLabel;
        	columnUri = colUri;
        	columnDomain = colDomain;
        	columnType = colType;
        	
        	linkType = type;
        	dialog.modal({keyboard:true, show:true, backdrop:'static'});
        	
        	if(type == "changeIncoming" || type == "changeOutgoing" || type == "changeLink") {
        		changeFromNode = changeFrom;
        		changeToNode = changeTo;
        		changeLink = changeLinkUri;
        	}
        };
        
        
        function showBlank(wsId, colId, alignId,
                colLabel, colUri, colDomain, colType, type) {
            selectedFromClass = {label:"", id:"", uri:""};
            selectedToClass = {label:"", id:"", uri:""};
            selectedProperty = {label:"", id:"", uri:""};
            show(wsId, colId, alignId,
                    colLabel, colUri, colDomain, colType, type);
        };
        
        
        return {    //Return back the public methods
            show : show,
            showBlank : showBlank,
            init : init,
            setSelectedFromClass : setSelectedFromClass,
            setSelectedToClass : setSelectedToClass,
            setSelectedProperty : setSelectedProperty
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


/**
 * ==================================================================================================================
 * 
 * 				Diloag to Manage all Incoming/Outgoing links of a Node
 * 
 * ==================================================================================================================
 */
var ManageIncomingOutgoingLinksDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
    	var dialog = $("#manageIncomingOutgoingLinksDialog");
    	var worksheetId, columnId, alignmentId, linkType;
    	var columnLabel, columnUri, columnDomain, columnType;
    	var initialLinks;
    	
    	var classUI, propertyUI, editLink, classPropertyUIDiv;
    	
    	function init() {
            
            //Initialize what happens when we show the dialog
            dialog.on('show.bs.modal', function (e) {
                hideError();
                
                $("#incomingLinksTable tr", dialog).remove();
                $("#outgoingLinksTable tr", dialog).remove();
                
                $("#columnName", dialog).text(columnLabel);
                
                initialLinks = getAllLinksForNode(worksheetId, alignmentId, columnUri);
                $.each(initialLinks, function(index2, link) {
                	addLink(link);
                });
            });
            
            
            $('#btnSave', dialog).on('click', function (e) {
                e.preventDefault();
                saveDialog(e);
            });
            
            $('#btnAddIncomingLink', dialog).on('click', function (e) {
                e.preventDefault();
                addIncomingLink(e);
            });
            
            $('#btnAddOutgoingLink', dialog).on('click', function (e) {
                e.preventDefault();
                addOutgoingLink(e);
            });
        }

    	function addLink(link) {
    		var table = (link.type == "incoming")? $("#incomingLinksTable") : $("#outgoingLinksTable");
    		var trTag = $("<tr>");
    		table.append(trTag);
    		var direction = (link.type == "incoming") ? "from" : "to";
    		var classLabel = (link.type == "incoming") ? link.source.label : link.target.label;
    		
    		trTag.data("link", $.extend(true, {}, link)) // deep copy)
				.append($("<td>").append(direction).css("width","5%"))
				.append($("<td>").addClass("bold").append(classLabel).css("width","40%"))
				.append($("<td>").append("via").css("width","5%"))
				.append($("<td>").addClass("bold").append(link.property.label).css("width","40%"))
				.append($("<td>").css("width","5%")
						.append($("<button>").attr("type", "button").addClass("btn").addClass("btn-default").text("Delete").click(deleteLink))
						)
    			.append($("<td>").css("width","5%")
	            		.append($("<button>").attr("type", "button").addClass("btn").addClass("editButton").addClass("btn-default").text("Edit").click(editLink))
	            		.append($("<button>").attr("type", "button").addClass("btn").addClass("hideButton").css("display","none").addClass("btn-default").text("Hide").click(hideEditOptions))
	            		);
    		
    	}
    	
    	function setRowData(row, link) {
    		var classLabel = (link.type == "incoming") ? link.source.label : link.target.label;
    		$("td:nth-child(2)", row).text(classLabel);
    		$("td:nth-child(4)", row).text(link.property.label);
    	}
    	
    	function deleteLink() {
    		var parentTrTag = $(this).parents("tr");
    		parentTrTag.remove();
    	}
    	
    	function editLink() {
    		var parentTrTag = $(this).parents("tr");
    		$(".editButton", parentTrTag).hide();
            $(".hideButton", parentTrTag).show();
            
            var link = $(parentTrTag).data("link");
            
            var table = parentTrTag.parents("table");
            $("tr.editRow", table).remove();
            var edittd = $("<td>").attr("colspan", "5");
            var editTr = $("<tr>").addClass("editRow").append(edittd);
            editTr.insertAfter(parentTrTag);
            editTr.addClass("currentEditRow");
            editTr.data("editRowObject", parentTrTag);
            
            classPropertyUIDiv = $("<div>").addClass("row").attr("id", "linkEdit");
            var classDiv = $("<div>").addClass("col-sm-6");
            var propDiv = $("<div>").addClass("col-sm-6");
            classPropertyUIDiv.append(classDiv);
            classPropertyUIDiv.append(propDiv);
            edittd.append(classPropertyUIDiv);
            
            classUI = new ClassUI("showIncomingOutgoingLinksDialog_class", getExistingClassNodes, getAllClassNodes, 100);
            propertyUI = new PropertyUI("showIncomingOutgoingLinksDialog_property", getPropertyForClass, getProperties, 100);
            classUI.setHeadings("Classes in Model", "All Classes");
            propertyUI.setHeadings("Compatible Properties", "All Properties");
            classUI.onClassSelect(validateClassInputValue);
            propertyUI.onPropertySelect(validatePropertyInputValue);
            
            var clazz = (link.type == "incoming") ? link.source : link.target;
            classUI.setDefaultClass(clazz.label, clazz.id, clazz.uri)
            classUI.setSelectedProperty(link.property.label, link.property.id, link.property.uri);
            classUI.generateJS(classDiv, true);
          
            propertyUI.setDefaultProperty(link.property.label, link.property.id, link.property.uri);
            propertyUI.setSelectedClass(clazz.label, clazz.id, clazz.uri);
            propertyUI.generateJS(propDiv, true);
           
    	}
    	
    	function hideEditOptions() {
    		var parentTrTag = $(this).parents("tr");
        	var table = parentTrTag.parents("table");
        	 $("tr", table).removeClass('currentEditRow');
             $("td.CRFSuggestedText", parentTrTag).text("");
             $("tr.editRow", table).remove();
             
             $(".editButton", parentTrTag).show();
             $(".hideButton", parentTrTag).hide();
    	}
    	
    	function getExistingClassNodes() {
    		var classes = getClassesInModel(alignmentId);
        	var result = [];
	       	 $.each(classes, function(index, clazz){
	       		 result.push(ClassUI.getNodeObject(clazz.label, clazz.id, clazz.uri));
	       	 });
	       	return result;
    	}
    	
    	
    	function getAllClassNodes() {
    		var classes = getAllClasses(worksheetId);
        	var result = [];
	       	 $.each(classes, function(index, clazz){
	       		 result.push(ClassUI.getNodeObject(clazz.label, clazz.id, clazz.uri));
	       	 });
	       	return result;
    	}
    	
    	function getProperties() {
    		var props
    		if(columnType == "ColumnNode")
    			props = getAllDataProperties(worksheetId);
    		else
    			props = getAllObjectProperties(alignmentId);
        	var result = [];
	       	 $.each(props, function(index, prop){
	       		 result.push(PropertyUI.getNodeObject(prop.label, prop.id, prop.uri));
	       	 });
	       	return result;
    	}
    	
    	function getPropertyForClass(selectedClass) {
    		var domain, range;
    	    var startNodeClass = columnDomain;
    	    if(columnType == "ColumnNode")
    	    	startNodeClass = "";
    	    if(editLink.type == "incoming") {
    	    	domain = selectedClass.uri;
    	    	range = startNodeClass;
    	    } else { //if(linkType == "outgoing" || linkType == "changeOutgoing") {
    	    	domain = startNodeClass;
    	    	range = selectedClass.uri;
    	    }
    	    
    	    var props = getAllPropertiesForDomainRange(alignmentId, domain, range);
    	    var result = [];
	       	 $.each(props, function(index, prop){
	       		 result.push(PropertyUI.getNodeObject(prop.label, prop.id, prop.uri));
	       	 });
	       	selectedProperties = result;
	       	return result;
    	}
    	
    	function validatePropertyInputValue(propertyData) {
        	classUI.refreshClassDataTop(propertyData.label, propertyData.id, propertyData.uri);
            var rowToChange = $(classPropertyUIDiv).parents("tr.editRow").data("editRowObject");
            if(rowToChange != null) {
            	var link = rowToChange.data("link");
            	link.property = propertyData;
            	setRowData(rowToChange, link);
            }
        }
    	
    	function validateClassInputValue(classData) {
        	propertyUI.refreshPropertyDataTop(classData.label, classData.id, classData.uri);
        	 var rowToChange = $(classPropertyUIDiv).parents("tr.editRow").data("editRowObject");
             if(rowToChange != null) {
             	var link = rowToChange.data("link");
             	if(link.type == "incoming")
             		link.source = classData;
             	else
             		link.target = classData;
             	setRowData(rowToChange, link);
             }
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
        	 var info = new Object();
        	 info["workspaceId"] = $.workspaceGlobalInformation.id;
        	 info["command"] = "ChangeInternalNodeLinksCommand";

        	 // Prepare the input for command
        	 var newInfo = [];
        	 
        	// Put the old edge information
        	var initialEdges = [];
        	$.each(initialLinks, function(index2, link) {
	        	var oldEdgeObj = {};
	    	    oldEdgeObj["edgeSourceId"] = link.source.id;
	    	    oldEdgeObj["edgeTargetId"] = link.target.id;
	    	    oldEdgeObj["edgeId"] = link.property.id;
	    	    initialEdges.push(oldEdgeObj);
        	});
        	newInfo.push(getParamObject("initialEdges", initialEdges, "other"));
    	    
        	newInfo.push(getParamObject("alignmentId", alignmentId, "other"));
        	newInfo.push(getParamObject("worksheetId", worksheetId, "worksheetId"));
        	
        	var newEdges = [];
        	
        	var tables = [
        	              $("#incomingLinksTable"),
        	              $("#outgoingLinksTable")
        	             ];
        	var newValidated = true;
        	
        	$.each(tables, function(idx, table) {
        		$.each($("tr",table), function(index, row){
        			var newEdgeObj = {};
        			var link = $(row).data("link");
        			if(link) {
        				if(link.source.id == "FakeId" || link.target.id == "FakeId") {
        					alert("Please select a Class");
        					newValidated = false;
        					return false;
        				}
        				if(link.property.id == "FakeId") {
        					alert("Please select a property");
        					newValidated = false;
        					return false;
        				}
	                	newEdgeObj["edgeSourceId"] = link.source.id
	                	newEdgeObj["edgeSourceUri"] = link.source.uri;
	                    newEdgeObj["edgeTargetId"] = link.target.id
	                    newEdgeObj["edgeTargetUri"] = link.target.uri;
	                    newEdgeObj["edgeId"] = link.property.id;
	                    newEdges.push(newEdgeObj);
        				
        			}
            	});
        	});
            
        	if(!newValidated)
        		return;
        	
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
        
        function addIncomingLink(e) {
        	var source = {"id":"FakeId", "label":"Class", "uri":"FakeURI"};
        	var target = {"id":columnUri, "label":columnLabel, "uri":columnDomain};
    		var prop = {"id":"FakeId", "label":"property"};
    		var link = {"type":"incoming", "source":source, "target":target, "property":prop};
    		addLink(link);
        }
        
        function addOutgoingLink(e) {
        	var source = {"id":columnUri, "label":columnLabel, "uri":columnDomain};
        	var target = {"id":"FakeId", "label":"Class", "uri":"FakeURI"};
    		var prop = {"id":"FakeId", "label":"property"};
    		var link = {"type":"outgoing", "source":source, "target":target, "property":prop};
    		addLink(link);
        }
        
        function hide() {
        	dialog.modal('hide');
        }
        
       
        function show(wsId, colId, alignId,
        		colLabel, colUri, colDomain, colType) {
        	worksheetId = wsId;
        	columnId = colId;
        	alignmentId = alignId;
        	
        	columnLabel = colLabel;
        	columnUri = colUri;
        	columnDomain = colDomain;
        	columnType = colType;
        	dialog.modal({keyboard:true, show:true, backdrop:'static'});
        };
        
        return {    //Return back the public methods
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

var searchDataDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
        var dialog = $("#searchDataDialog");
        var worksheetId, columnDomain, columnUri, alignmentId;
        
        function init() {
            
            //Initialize handler for Save button
            //var me = this;
            $('#btnSave', dialog).on('click', function (e) {
                e.preventDefault();
                saveDialog(e);
            });
            
                
        }
        
        function hideError() {
            $("div.error", dialog).hide();
        }
        
        function showError() {
            $("div.error", dialog).show();
        }
        
        function saveDialog(e) {
            hide();
            //  if(!testSparqlEndPoint($("input#txtR2RML_URL").val(), worksheetId)) {
            //     alert("Invalid sparql end point. Could not establish connection.");
            //     return;
            // }

            var info = new Object();
            info["worksheetId"] = worksheetId;
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info['tripleStoreUrl'] = $('#txtModel_URL').val();
            info['context'] = $('#txtGraph_URL_Search').val();
            info["command"] = "SearchForDataToAugmentCommand";
            info["nodeUri"] = columnDomain;
            console.log(info['graphContext']);
            //showLoading(info["worksheetId"]);
            var returned = $.ajax({
                url: "RequestController",
                type: "POST",
                data : info,
                dataType : "json",
                complete :
                    function (xhr, textStatus) {
                        //alert(xhr.responseText);
                        var json = $.parseJSON(xhr.responseText);
                        json = json.elements[0];
                        console.log(json);
                        //parse(json);
                        //hideLoading(info["worksheetId"]);
                        augmentDataDialog.getInstance().show(worksheetId, json, columnUri, alignmentId);
                    },
                error :
                    function (xhr, textStatus) {
                        alert("Error occured while Searching Models!" + textStatus);
                        //hideLoading(info["worksheetId"]);
                    }
            });
        };
        
        function hide() {
            dialog.modal('hide');
        }
        
        function show(wsId, colDomain, colUri, Alnid) {
            worksheetId = wsId;
            columnDomain = colDomain;
            columnUri = colUri;
            alignmentId = Alnid;
            dialog.modal({keyboard:true, show:true, backdrop:'static'});
        };
        
        
        return {    //Return back the public methods
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

var augmentDataDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
        var dialog = $("#augmentDataDialog");
        var worksheetId, columnUri, alignmentId;
        
        function init() {
            //Initialize what happens when we show the dialog
            dialog.on('show.bs.modal', function (e) {
                hideError();
                 $('#txtR2RML_URL_Search').val('http://'+window.location.host + '/openrdf-sesame/repositories/karma_models');
            });
            
            //Initialize handler for Save button
            //var me = this;
            $('#btnSave', dialog).on('click', function (e) {
                e.preventDefault();
                saveDialog(e);
            });
            
                
        }
        
        function hideError() {
            $("div.error", dialog).hide();
        }
        
        function showError() {
            $("div.error", dialog).show();
        }
        
        function saveDialog(e) {
            hide();
            var checkboxes = dialog.find(":checked");
            if (checkboxes.length == 0) {
                hide();
                return;
            }
            var predicates = [];
            var triplesMap = [];
            var otherClass = [];
            for (var i = 0; i < checkboxes.length; i++) {
	            var checkbox = checkboxes[i];
	            var t1 = new Object();
	            var t2 = new Object();
	            var t3 = new Object();
	            console.log(checkbox['value']);
	            var tmpJSON = jQuery.parseJSON(checkbox['value']);
	            t1['predicate'] = checkbox['src'];
	            t2['tripleMap'] = tmpJSON['tripleMap'];
	            t3['otherClass'] = tmpJSON['otherClass'];
	            predicates.push(t1);    
	            triplesMap.push(t2);
	            otherClass.push(t3);
	        	}
	        	var info = new Object();
            info["worksheetId"] = worksheetId;
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["alignmentId"] = alignmentId;
            info["columnUri"] = columnUri;
            info["command"] = "FetchHNodeIdFromAlignmentCommand";
            var hNodeId;
            var returned = $.ajax({
                url: "RequestController",
                type: "POST",
                data : info,
                dataType : "json",
                async: false,
                complete :
                    function (xhr, textStatus) {
                        //alert(xhr.responseText);
                        var json = $.parseJSON(xhr.responseText);
                        //json = $.parseJSON();
                        hNodeId = json.elements[0]['HNodeId'];
                        console.log(hNodeId);
                        //applyModelDialog.getInstance().show(worksheetId, json);
                    },
                error :
                    function (xhr, textStatus) {
                        alert("Error occured while fetching alignment!" + textStatus);
                        //hideLoading(info["worksheetId"]);
                    }
            });
            var info = new Object();
            info["worksheetId"] = worksheetId;
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["command"] = "AugmentDataCommand";
            var newInfo = [];
            newInfo.push(getParamObject("worksheetId", worksheetId, "worksheetId"));
            newInfo.push(getParamObject("predicate", JSON.stringify(predicates), "other"));
            newInfo.push(getParamObject("triplesMap", JSON.stringify(triplesMap), "other"));
            newInfo.push(getParamObject("otherClass", JSON.stringify(otherClass), "other"));
            newInfo.push(getParamObject("columnUri", columnUri, "other"));
            newInfo.push(getParamObject("tripleStoreUrl", $('#txtData_URL').val(), "other"));
            newInfo.push(getParamObject("hNodeId", hNodeId, "hNodeId"));
            info["newInfo"] = JSON.stringify(newInfo);
            showLoading(info["worksheetId"]);
            var returned = $.ajax({
                url: "RequestController",
                type: "POST",
                data : info,
                dataType : "json",
                complete :
                    function (xhr, textStatus) {
                        //alert(xhr.responseText);
                        var json = $.parseJSON(xhr.responseText);
                        console.log(json);
                        parse(json);
                        hideLoading(info["worksheetId"]);
                        //applyModelDialog.getInstance().show(worksheetId, json);
                    },
                error :
                    function (xhr, textStatus) {
                        alert("Error occured while Augmenting Models!" + textStatus);
                        hideLoading(info["worksheetId"]);
                    }
            });
        };
        
        function hide() {
            dialog.modal('hide');
        }
        
        function show(wsId, json, colUri, Alnid) {
            worksheetId = wsId;
            columnUri = colUri;
            alignmentId = Alnid;
            console.log(json);
            if (json.length == 0) {
            	alert("No data to augment!");
            	return;
            }
            dialog.on('show.bs.modal', function (e) {
                hideError();
                var dialogContent = $("#augmentDataDialogColumns", dialog);
                dialogContent.empty();
                //console.log(headers);
                for (var i = 0; i < json.length; i++) {
                    var predicate = json[i]['predicate'];
                    var tripleMap = json[i]['tripleMap'];
                    var otherClass = json[i]['otherClass'];
                    var value = new Object();
                    value['tripleMap'] = tripleMap;
                    value['otherClass'] = otherClass;
                    var tmp = tripleMap.split(",");
                    var row = $("<div>").addClass("checkbox");
                    var label = $("<label>").text(predicate + " " +otherClass + " "+ tmp.length);
                    var input = $("<input>")
                                        .attr("type", "checkbox")
                                .attr("id", "selectPredicates")
                                .attr("value", JSON.stringify(value))
                                .attr("name", "selectPredicates")
                                .attr("src", predicate)
                    label.append(input);
                    row.append(label);
                    dialogContent.append(row);
                }
            });
            dialog.modal({keyboard:true, show:true, backdrop:'static'});
        };
        
        
        return {    //Return back the public methods
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