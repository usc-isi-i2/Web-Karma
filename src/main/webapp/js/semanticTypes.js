var SetSemanticTypeDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
    	var dialog = $("#setSemanticTypeDialog");
    	var worksheetId;
    	var columnId;
    	var columnTitle;
    	var existingTypes, selectedPrimaryRow, classAndPropertyListJson;
    	
    	function init() {
    		//Initialize what happens when we show the dialog
    		dialog.on('show.bs.modal', function (e) {
				hideError();
				
				$("#semanticType_columnName", dialog).text(columnTitle);
				
			    $("table#semanticTypesTable tr.semTypeRow",dialog).remove();
			    $("table#semanticTypesTable tr.editRow",dialog).remove();
			    $("input#chooseClassKey", dialog).attr("checked", false);
			    
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
			});
			
			$("#addType", dialog).on("click", function(e) {
				e.preventDefault();
				addEmptySemanticType();
			});
			
			
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
                .append($("<td>").append($("<button>").attr("type", "button").addClass("btn").addClass("btn-default").text("Edit").click(showSemanticTypeEditOptions)));

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
        
        function getClasses() {
        	var classArray = classAndPropertyListJson["elements"][0]["classList"];
        	var classArrayLabels = [];
        	$.each(classArray, function(index, className) {
        		classArrayLabels.push(className.label);
        	});
        	return classArrayLabels;
        }
        
        function getProperties() {
        	var propertyArray = classAndPropertyListJson["elements"][0]["propertyList"];
        	return propertyArray;
        }
        
        function showSemanticTypeEditOptions() {
            var table = $("#semanticTypesTable");
            var parentTrTag = $(this).parents("tr");
            $("tr", table).removeClass('currentEditRow');
            $("td.CRFSuggestedText", parentTrTag).text("");

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

            var defaultClass, defaultProperty;
            if($(parentTrTag).data("ResourceType") == "Class") {
                defaultClass = $(parentTrTag).data("DisplayLabel");
                defaultProperty = "";
            } else {
               defaultClass = $(parentTrTag).data("DisplayDomainLabel");
               defaultProperty = $(parentTrTag).data("DisplayLabel");
            }
            
            var classPropertyUI = new ClassPropertyUI("semanticTypeEdit", defaultClass, defaultProperty, 
            		getClasses, getProperties, 5);
            ClassPropertyUI(id, defaultClass, defaultProperty, 
            		classFuncTop, propertyFuncTop, 
            		getClasses, getProperties,
            		5);
            var editTr = $("<tr>").addClass("editRow")
            	.append($("<td>")
            			.attr("colspan", "4")
            			.append(classPropertyUI.generateJS())
            	);
            
            editTr.insertAfter(parentTrTag);
            editTr.addClass("currentEditRow");
            editTr.data("editRowObject", parentTrTag);

            if($(parentTrTag).data("ResourceType") == "Class") {
                $("input#classInputBox").val($(parentTrTag).data("DisplayLabel"));
                $("input#propertyInputBox").val("");
            } else {
                $("input#classInputBox").val($(parentTrTag).data("DisplayDomainLabel"));
                $("input#propertyInputBox").val($(parentTrTag).data("DisplayLabel"));
            }

            $("input#propertyInputBox").autocomplete({autoFocus: true, select:function(event, ui){
                $("input#propertyInputBox").val(ui.item.value);
                validatePropertyInputValue();
            }, source: function( request, response ) {
                var matches = $.map( propertyArray, function(prop) {
                    if ( prop.toUpperCase().indexOf(request.term.toUpperCase()) != -1 ) {
                        return prop;
                    }
                });
                response(matches);
            }
            });

            $("input#classInputBox").autocomplete({autoFocus: true, select:function(event, ui){
                $("input#classInputBox").val(ui.item.value);
                validateClassInputValue($("input#classInputBox").val(), true);
            }, source: function( request, response ) {
                var matches = $.map( classArray, function(cls) {
                    if ( cls["label"].toUpperCase().indexOf(request.term.toUpperCase()) != -1 ) {
                        return cls;
                    }
                });
                response(matches);
            }
            });
            // Validate the value once the input loses focus
            $("input#propertyInputBox").blur(validatePropertyInputValue);
            $("input#classInputBox").blur(function() {
                validateClassInputValue($("input#classInputBox").val(), true)
            });


        }
        
        function validatePropertyInputValue() {
            var propertyMap = classAndPropertyListJson["elements"][0]["propertyMap"]
            var propertyInputBox = $("input#propertyInputBox");
            var inputVal = $(propertyInputBox).val();

            hideError();

            $("table#semanticTypesTable tr").removeClass("fixMe");

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

            if(!found && $.trim(inputVal) != "") {
                showError("Input data property not valid!");
                return false;
            }

            // Use the value in proper case as input value
            $(propertyInputBox).val(properCasedKey);

            var rowToChange = $(propertyInputBox).parents("tr.editRow").data("editRowObject");
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

        function validateClassInputValue(inputVal, updateLabels) {
            var classMap = classAndPropertyListJson["elements"][0]["classMap"]
            var classInputBox = $("input#classInputBox");
            // var inputVal = $(classInputBox).val();
            hideError();
            $("table#semanticTypesTable tr").removeClass("fixMe");

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

            if(!found) {
               showError("Input class/instance not valid!");
                return false;
            }
            // Use the value in proper case as input value
            // $(classInputBox).val(properCasedKey);
            if (updateLabels) {
                var rowToChange = $(classInputBox).parents("tr.editRow").data("editRowObject");
                var displayLabel = "";
                if($(rowToChange).data("ResourceType") == "Class") {
                    $(rowToChange).data("FullType",uri).data("DisplayLabel",properCasedKey);
                    displayLabel = $(rowToChange).data("DisplayLabel");
                } else {
                    // If no value has been input in the data property box, change from data property sem type to class sem type
                    if($.trim($("input#propertyInputBox").val()) == "") {
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