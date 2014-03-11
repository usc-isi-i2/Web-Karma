/*******************************************************************************
 * Copyright 2012 University of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This code was developed by the Information Integration Group as part
 * of the Karma project at the Information Sciences Institute of the
 * University of Southern California.  For more information, publications,
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

function attachOntologyOptionsRadioButtonHandlers() {
    var optionsDiv = $("#ChangeSemanticTypesDialogBox");

    // Add handler for the search button
    $("#classKeyword").keyup(function(event) {
        if(event.keyCode == 13){
            $("#classSearch").click();
        }
    });
    $("#propertyKeyword").keyup(function(event) {
        if(event.keyCode == 13){
            $("#propertySearch").click();
        }
    });

    $("input[name='isPrimaryGroup']:radio").live("change", (function(){
        // Make the semantic type as selected if it has not been yet
        var tr = $(this).parents("tr");
        if(!$("input[name='currentSemanticTypeCheckBoxGroup']:checkbox", tr).is(':checked')) {
            $("input[name='currentSemanticTypeCheckBoxGroup']:checkbox", tr).prop('checked', true);
            $(tr).addClass("selected");
        }

        // Unselect the previous one
        var previousTr = $(optionsDiv).data("selectedPrimaryRow");
        if(previousTr != null) {
            $("input[name='currentSemanticTypeCheckBoxGroup']:checkbox", previousTr).prop('checked', false);
            $(previousTr).removeClass('selected');
        }
        $(optionsDiv).data("selectedPrimaryRow", tr);
    }));

    $("#classSearch").button().click(function(){
        $("div#classTree").jstree("search", $("#classKeyword").val());
    });
    $("#propertySearch").button().click(function(){
        $("div#propertyTree").jstree("search", $("#propertyKeyword").val());
    });

    $("input#filterPropertyByDomain").change(handleDataPropertyFilter);
    $("input#filterClassByDomain").change(handleClassFilter);
    $("button#addSemanticType").button().click(addEmptySemanticType);

    // Filter for the alternative object properties list
    $("#alternativeParentsTableFilter").keyup( function (event) {
        // fire the above change event after every letter

        //if esc is pressed or nothing is entered
        if (event.keyCode == 27 || $(this).val() == '') {
            //if esc is pressed we want to clear the value of search box
            $(this).val('');

            //we want each row to be visible because if nothing
            //is entered then all rows are matched.
            $('tr').removeClass('visible').show().addClass('visible');
        }

        //if there is text, lets filter
        else {
            filter('#AlternativeParentLinksTable tr', $(this).val(), "edgeLabel");
        }
    });

    $("div#semanticTypingAdvacedOptionsDiv input:checkbox").change(semanticTypesAdvancedOptionsHandler);

    // $.widget( "custom.catcomplete", $.ui.autocomplete, {
    // _renderItemData: function( ul, item ) {
    // return this._renderItem( ul, item ).data( "ui-autocomplete-item", item );
    // },
    // _renderMenu: function( ul, items ) {
    // var that = this,
    // currentCategory = "";
    // $.each( items, function( index, item ) {
    // if ( item.category != currentCategory ) {
    // ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
    // currentCategory = item.category;
    // }
    // that._renderItemData( ul, item);
    // });
    // }
    // });

    /*** Setting advanced semantic typing options ***/
    $("button#semanticTypingAdvancedOptions").button().click(function(){
        var optionsBox = $("div#semanticTypingAdvacedOptionsDiv");
        var state = optionsBox.data("state");
        if (state != null && state == "open") {
            optionsBox.hide().data("state", "close");
        } else {
            optionsBox.show().data("state", "open");

            var classArray = $("#ChangeSemanticTypesDialogBox").data("classAndPropertyListJson")["elements"][0]["classList"];
            var existingLinksMap = $("#ChangeSemanticTypesDialogBox").data("classAndPropertyListJson")["elements"][0]["existingDataPropertyInstances"];
            $("input#isUriOfClassTextBox").autocomplete({autoFocus: true, select:function(event, ui){
                $("input#isUriOfClassTextBox").val(ui.item.value);
                validateClassInputValue(ui.item.value, false);
            }, source: function( request, response ) {
                var matches = $.map( classArray, function(cls) {
                    if ( cls["label"].toUpperCase().indexOf(request.term.toUpperCase()) != -1 ) {
                        return cls;
                    }
                });
                response(matches);
            }
            });

            $("input#isSubclassOfClassTextBox").autocomplete({autoFocus: true, select:function(event, ui){
                $("input#isSubclassOfClassTextBox").val(ui.item.value);
                validateClassInputValue(ui.item.value, false);
            }, source: function( request, response ) {
                var matches = $.map( classArray, function(cls) {
                    if ( cls["label"].toUpperCase().indexOf(request.term.toUpperCase()) != -1 ) {
                        return cls;
                    }
                });
                response(matches);
            }
            });

            $("input#isSpecializationForEdgeTextBox").autocomplete({autoFocus: true, select:function(event, ui){
                // $("input#isSpecializationForEdgeTextBox").val(ui.item.value);
                // validatePropertyInputValue();
            }, source: function( request, response ) {
                var matches = $.map( existingLinksMap, function(prop) {
                    if (prop["label"].toUpperCase().indexOf(request.term.toUpperCase()) != -1 ) {
                        return prop;
                    }
                });
                response(matches);
            }
            });
        }
    });

    $("input#isUriOfClassTextBox").blur(function() {
        if ($("input#isUriOfClassTextBox").val() != "")
            validateClassInputValue($("input#isUriOfClassTextBox").val(), false)
    });
    $("input#isSubclassOfClassTextBox").blur(function() {
        if ($("input#isSubclassOfClassTextBox").val() != "")
            validateClassInputValue($("input#isSubclassOfClassTextBox").val(), false)
    });

    $("div#semanticTypingAdvacedOptionsDiv input:text").focus(function() {
        $(this).parents("tr").find("input[type='checkbox']").attr('checked', true).trigger('change');
    });

    (function( $ ) {
        $.widget( "ui.combobox", {
            _create: function() {
                var input,
                    that = this,
                    wasOpen = false,
                    select = this.element.hide(),
                    selected = select.children( ":selected" ),
                    value = selected.val() ? selected.text() : "",
                    wrapper = this.wrapper = $( "<span>" )
                        .addClass( "ui-combobox" )
                        .insertAfter( select );

                input = $( "<input>" )
                    .appendTo( wrapper )
                    .val( value )
                    .attr( "title", "" )
                    .addClass( "ui-state-default ui-combobox-input" )
                    .autocomplete({
                        delay: 0,
                        minLength: 0,
                        source: function( request, response ) {
                            var matcher = new RegExp( $.ui.autocomplete.escapeRegex(request.term), "i" );
                            response( select.children( "option" ).map(function() {
                                var text = $( this ).text();
                                if ( this.value && ( !request.term || matcher.test(text) ) )
                                    return {
                                        // label: text.replace(
                                        // new RegExp(
                                        // "(?![^&;]+;)(?!<[^<>]*)(" +
                                        // $.ui.autocomplete.escapeRegex(request.term) +
                                        // ")(?![^<>]*>)(?![^&;]+;)", "gi"
                                        // ), "<strong>$1</strong>" ),
                                        value: text,
                                        option: this
                                    };
                            }) );
                        },
                        select: function( event, ui ) {
                            ui.item.option.selected = true;
                            that._trigger( "selected", event, {
                                item: ui.item.option
                            });
                        }
                    })
                    .addClass( "ui-widget ui-widget-content ui-corner-left" );

                // input.data( "ui-autocomplete" )._renderItem = function( ul, item ) {
                // return $( "<li>" )
                // .append( "<a>" + item.label + "</a>" )
                // .appendTo( ul );
                // };

                $( "<a>" )
                    .attr( "tabIndex", -1 )
                    .attr( "title", "Show All Items" )
                    .appendTo( wrapper )
                    .button({
                        icons: {
                            primary: "ui-icon-triangle-1-s"
                        },
                        text: false
                    })
                    .removeClass( "ui-corner-all" )
                    .addClass( "ui-corner-right ui-combobox-toggle" )
                    .mousedown(function() {
                        wasOpen = input.autocomplete( "widget" ).is( ":visible" );
                    })
                    .click(function() {
                        input.focus();

                        // close if already visible
                        if ( wasOpen ) {
                            return;
                        }

                        // pass empty string as value to search for, displaying all results
                        input.autocomplete( "search", "" );
                    });
            },

            _destroy: function() {
                this.wrapper.remove();
                this.element.show();
            }
        });
    })( jQuery );

    $("#rdfTypeSelect").combobox();
    $( "#typeListToggle" ).click(function() {
        $( "#rdfTypeSelect" ).toggle();
    });

    // In d3-alignment-vis.js
    attachHandlersToChangeObjPropertyObjects();

}

function semanticTypesAdvancedOptionsHandler() {
    // Deselect all the existing semantic types
    var semTypesTable = $("table#currentSemanticTypesTable");
    $.each($("tr.selected.semTypeRow",semTypesTable), function(index, row){
        $(this).removeClass('selected');
        $("input[name='currentSemanticTypeCheckBoxGroup']:checkbox", $(this)).prop('checked', false);
        $("input[name='isPrimaryGroup']:radio", $(this)).prop('checked',false);
    });

    $("div#semanticTypingAdvacedOptionsDiv input:checkbox").not($(this)).prop('checked', false);
}

function handleDataPropertyFilter() {
    var uriClass = $("div#propertyOntologyBox").data("classUri");

    if(uriClass  != "" && uriClass != "fakeDomainURI") {
        var info = new Object();
        info["workspaceId"] = $.workspaceGlobalInformation.id;

        if ($("input#filterPropertyByDomain").is(":checked")) {
            info["URI"] = uriClass;
            info["command"] = "GetDataPropertiesForClassCommand";
        } else {
            info["command"] = "GetDataPropertyHierarchyCommand";
        }

        var returned = $.ajax({
            url: "RequestController",
            type: "POST",
            data : info,
            dataType : "json",
            complete :
                function (xhr, textStatus) {
                    //alert(xhr.responseText);
                    var json = $.parseJSON(xhr.responseText);
                    var dataArray = json["elements"][0]["data"];
                    // populatePropertyTreeHierarchy(dataArray);
                    populateTreeHierarchy(dataArray, $("div#propertyTree"), $("div#propertyOntologyBox"), submitPropertyFromHierarchyWindow);
                },
            error :
                function (xhr, textStatus) {
                    alert("Error occured while fetching ontology data!" + textStatus);
                }
        });
    }
}

function handleClassFilter() {
    var uriProperty = $("div#propertyOntologyBox").data("propertyUri");

    if(uriProperty  != "" && uriProperty != "fakePropertyURI") {
        var info = new Object();
        info["workspaceId"] = $.workspaceGlobalInformation.id;
        info["worksheetId"] = $("#ChangeSemanticTypesDialogBox").data("worksheetId");
        
        if ($("input#filterClassByDomain").is(":checked")) {
            info["URI"] = uriProperty;
            info["command"] = "GetDomainsForDataPropertyCommand";
        } else {
            info["command"] = "GetOntologyClassHierarchyCommand";
        }
        
        var returned = $.ajax({
            url: "RequestController",
            type: "POST",
            data : info,
            dataType : "json",
            complete :
                function (xhr, textStatus) {
                    //alert(xhr.responseText);
                    var json = $.parseJSON(xhr.responseText);
                    var dataArray = json["elements"][0]["data"];
                    // populateClassTreeHierarchy(dataArray);
                    populateTreeHierarchy(dataArray, $("div#classTree"), $("div#classOntologyBox"), submitClassFromHierarchyWindow);
                },
            error :
                function (xhr, textStatus) {
                    alert("Error occured while fetching ontology data!" + textStatus);
                }
        });
    }
}

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
    var table = $("#currentSemanticTypesTable");

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
        .append($("<td>").append($("<button>").button().text("Edit").click(showSemanticTypeEditOptions)));

    if(isCrfModelSuggested)
    // trTag.append($("<td>").addClass("CRFSuggestedText").text("  (CRF Suggested)"));
        trTag.append($("<td>").addClass("CRFSuggestedText"));
    else
        trTag.append($("<td>"));

    if(isSelected)
        trTag.addClass("selected");

    if(semTypeObject["isPrimary"]) {
        $("input[name='isPrimaryGroup']:radio", trTag).prop('checked', true);
        $("#ChangeSemanticTypesDialogBox").data("selectedPrimaryRow", trTag);
        $("div#rdfTypeSelectDiv input").val(semTypeObject["rdfLiteralType"]);
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

function showSemanticTypeEditOptions() {
    var optionsDiv = $("#ChangeSemanticTypesDialogBox");
    var table = $("#currentSemanticTypesTable");
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


    if($(optionsDiv).data("classAndPropertyListJson") == null){
        alert("Class and property list not yet loaded from the server!");
        return false;
    }

    var classArray = $(optionsDiv).data("classAndPropertyListJson")["elements"][0]["classList"];
    var propertyArray = $(optionsDiv).data("classAndPropertyListJson")["elements"][0]["propertyList"];

    // Remove any existing edit window open for other semantic type
    $("tr.editRow", table).remove();

    var editTr = $("<tr>").addClass("editRow")
        .append($("<td>").attr("colspan",5)
            .append($("<table>")
                .append($("<tr>")
                    .append($("<td>")
                        .append($("<span>").text('Property: '))
                        .append($("<input>").attr("id","propertyInputBox").attr("type","text").attr("size",14).val($(parentTrTag))))
                    .append($("<td>")
                        .append($("<span>").text('  Class: '))
                        .append($("<input>").attr("id","classInputBox").attr("type","text").attr("size",14))))
                .append($("<tr>")
                    .append($("<td>")
                        .append($("<button>").button().attr("id","browsePropertyList").text("Browse...").click(showPropertyHierarchyWindow)))
                    .append($("<td>")
                        .append($("<button>").button().attr("id","browseClassList").text("Browse...").click(showClassHierarchyWindow)))))
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

    var optionsDiv = $("#ChangeSemanticTypesDialogBox");
    var propertyMap = $(optionsDiv).data("classAndPropertyListJson")["elements"][0]["propertyMap"]
    var propertyInputBox = $("input#propertyInputBox");
    var inputVal = $(propertyInputBox).val();

    $("div#SemanticTypeErrorWindow").hide();
    $("table#currentSemanticTypesTable tr").removeClass("fixMe");

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
        $("span#SemanticTypeErrorWindowText").text("Input data property not valid!");
        $("div#SemanticTypeErrorWindow").show();
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
    var optionsDiv = $("#ChangeSemanticTypesDialogBox");
    var classMap = $(optionsDiv).data("classAndPropertyListJson")["elements"][0]["classMap"]
    var classInputBox = $("input#classInputBox");
    // var inputVal = $(classInputBox).val();
    $("div#SemanticTypeErrorWindow").hide();
    $("table#currentSemanticTypesTable tr").removeClass("fixMe");

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
        $("span#SemanticTypeErrorWindowText").text("Input class/instance not valid!");
        $("div#SemanticTypeErrorWindow").show();
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

function showClassHierarchyWindow(event) {
    var classDialogBox = $("div#classOntologyBox");
    classDialogBox.data("uri","").data("label","");
    $("input#filterClassByDomain").attr("checked", false);
    $("#classKeyword").val("");

    // Get the URI of the class for which we can filter by domain
    var editRow = $(this).parents("tr.editRow");
    var typeRow = $(editRow).data("editRowObject");
    var uriProperty = "";
    var labelProperty = "";
    if(typeRow.data("ResourceType") == "DataProperty") {
        uriProperty = typeRow.data("FullType");
        labelProperty = typeRow.data("DisplayLabel");
    } else {
        uriProperty = typeRow.data("Domain");
        labelProperty = typeRow.data("DisplayDomainLabel");
    }
    $("div#propertyOntologyBox").data("propertyUri", uriProperty);
    $("div#propertyOntologyBox").data("propertyLabel", labelProperty);
    if(labelProperty != "")
        $("span#propertyName").text("  " + labelProperty);
    else
        $("span#propertyName").text("none selected");

    // Send the AJAX request
    var info = new Object();
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "GetOntologyClassHierarchyCommand";
    info["worksheetId"] = $("#ChangeSemanticTypesDialogBox").data("worksheetId");

    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                var dataArray = json["elements"][0]["data"];
                // populateClassTreeHierarchy(dataArray);
                populateTreeHierarchy(dataArray, $("div#classTree"), classDialogBox, submitClassFromHierarchyWindow);
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while fetching ontology data!" + textStatus);
            }
    });
}

function showPropertyHierarchyWindow(event) {
    var propertyDialogBox = $("div#propertyOntologyBox");
    propertyDialogBox.data("uri","").data("label","");
    $("input#filterPropertyByDomain").attr("checked", false);
    $("#propertyKeyword").val("");

    // Get the URI of the class for which we can filter by domain
    var editRow = $(this).parents("tr.editRow");
    var typeRow = $(editRow).data("editRowObject");
    var uriClass = "";
    var labelClass = "";
    if(typeRow.data("ResourceType") == "Class") {
        uriClass = typeRow.data("FullType");
        labelClass = typeRow.data("DisplayLabel");
    } else {
        uriClass = typeRow.data("Domain");
        labelClass = typeRow.data("DisplayDomainLabel");
    }
    $("div#propertyOntologyBox").data("classUri", uriClass);
    $("div#propertyOntologyBox").data("classLabel", labelClass);
    if(labelClass != "")
        $("span#className").text(" " + labelClass);

    // Send the AJAX request
    var info = new Object();
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["command"] = "GetDataPropertyHierarchyCommand";

    var returned = $.ajax({
        url: "RequestController",
        type: "POST",
        data : info,
        dataType : "json",
        complete :
            function (xhr, textStatus) {
                var json = $.parseJSON(xhr.responseText);
                var dataArray = json["elements"][0]["data"];
                // populatePropertyTreeHierarchy(dataArray);
                populateTreeHierarchy(dataArray, $("div#propertyTree"), propertyDialogBox, submitPropertyFromHierarchyWindow);
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured while fetching ontology data!" + textStatus);
            }
    });
}

function populateTreeHierarchy(dataArray, treeDiv , dialogBox, submitHandler) {
    if(dataArray.length == 0) {
        $(treeDiv).html("<i>none</i>")
    } else {
        $(treeDiv).jstree({
            "json_data" : {
                "data" : dataArray
            },
            "themes" : {
                "theme" : "apple",
                "url": "css/jstree-themes/apple/style.css",
                "dots" : true,
                "icons" : false
            },
            "search" : {
                "show_only_matches": true
            },
            "plugins" : [ "themes", "json_data", "ui" ,"sort", "search"]
        }).bind("select_node.jstree", function (e, data) {
        		var newIndex = -1, isExistingSteinerTreeNode =  false;
        		if(data.rslt.obj.data("newIndex"))
        			newIndex = data.rslt.obj.data("newIndex");
        		if(data.rslt.obj.data("isExistingSteinerTreeNode"))
        			isExistingSteinerTreeNode = data.rslt.obj.data("isExistingSteinerTreeNode")
                dialogBox.data("URIorID",data.rslt.obj.data("URIorId"))
                    .data("label",data.rslt.obj.context.lastChild.wholeText)
                    .data("index", newIndex)
                    .data("isExistingSteinerTreeNode", isExistingSteinerTreeNode);
                var a = $.jstree._focused().get_selected();
                $(treeDiv).jstree("open_node", a);
            });
        dialogBox.dialog({height: 450, buttons: {
            "Cancel": function() { $(this).dialog("close"); },
            "Submit":submitHandler }
        });
    }
}

function submitPropertyFromHierarchyWindow() {
    var propertyDialogBox = $("div#propertyOntologyBox");
    var uri = propertyDialogBox.data("URIorID");
    var label = propertyDialogBox.data("label");

    if(uri == "") {
        alert("Nothing to submit! Please select from the hierarchy!");
        return false;
    }

    var propertyInputBox = $("input#propertyInputBox");
    propertyInputBox.val(label);
    validatePropertyInputValue();

    $(this).dialog("close");
}

function submitClassFromHierarchyWindow() {
    var classDialogBox = $("div#classOntologyBox");
    var uri = classDialogBox.data("URIorID");
    var label = classDialogBox.data("label");
    var isExistingGraphNode = classDialogBox.data("isExistingGraphNode");
    var isExistingSteinerTreeNode = classDialogBox.data("isExistingSteinerTreeNode");
    var indexToAdd = classDialogBox.data("index");

    if(uri == "") {
        alert("Nothing to submit! Please select from the hierarchy!");
        return false;
    }

    if (!isExistingSteinerTreeNode && indexToAdd != -1) {
        label += indexToAdd + " (add)";
    }

    var classInputBox = $("input#classInputBox");
    classInputBox.val(label);
    validateClassInputValue(label, true);

    $(this).dialog("close");
}

function semanticTypesTableCheckBoxHandler() {
    var optionsDiv = $("#ChangeSemanticTypesDialogBox");
    var existingTypesArray = optionsDiv.data("existingTypes");
    var parentTr = $(this).parents("tr");
    var table = $("table#currentSemanticTypesTable");

    // Deselect any meta property checkbox
    $("div#semanticTypingAdvacedOptionsDiv input:checkbox").prop('checked', false);

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

function getCurrentSelectedTypes() {
    var existingTypes = new Array();
    var table = $("#currentSemanticTypesTable");

    var notValid = false;
    // Loop through each selected row in the table
    $.each($("tr.selected.semTypeRow",table), function(index, row){
        var fullType = $(row).data("FullType");
        var domain = $(row).data("Domain");

        // Check if the user selected a fake semantic type object
        if(domain == "fakeDomainURI" || fullType == "fakePropertyURI") {
            $(row).addClass("fixMe");
            $("span#SemanticTypeErrorWindowText").text("Semantic type not valid!");
            $("div#SemanticTypeErrorWindow").show();
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
        })
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

function getParamObject(name, value, type) {
    var param = new Object();
    param["name"] = name;
    param["value"] = value;
    param["type"] = type;

    return param;
}

function submitSemanticTypeChange() {
    var optionsDiv = $("#ChangeSemanticTypesDialogBox");

    /** Prepare the JSON object **/
    var info = new Object();
    var newInfo = [];	// Used for commands that take JSONArray as input and are saved in the history
    var hNodeId = optionsDiv.data("currentNodeId");
    info["worksheetId"] = $("td#" + hNodeId).parents("div.Worksheet").attr("id");
    info["hNodeId"] = hNodeId;
    info["isKey"] = $("input#chooseClassKey").is(":checked");
    info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["rdfLiteralType"] = $("div#rdfTypeSelectDiv input").val()

    // Check if any meta property (advanced options) was selected
    var isMetaPropertyChecked = false;
    $.each($("div#semanticTypingAdvacedOptionsDiv input:checkbox"), function(index, property) {
        if($(property).prop("checked")) {
            isMetaPropertyChecked = true;
        }
    });

    info["metaPropertyName"] = $("div#semanticTypingAdvacedOptionsDiv input:checkbox[checked=true]").attr("id");
    newInfo.push(getParamObject("metaPropertyName", $("div#semanticTypingAdvacedOptionsDiv input:checkbox[checked=true]").attr("id"), "other"));
    if (isMetaPropertyChecked) {
        var propValue = $("div#semanticTypingAdvacedOptionsDiv input:checkbox[checked=true]").parents("tr").find("input:text").val();
        if (propValue == null || $.trim(propValue) == "") {
            $("span#SemanticTypeErrorWindowText").text("Please provide a value!");
            $("div#SemanticTypeErrorWindow").show();
            return false;
        }


        // Get the proper id
        if (info["metaPropertyName"] == "isUriOfClass" || info["metaPropertyName"] == "isSubclassOfClass") {
            var classMap = $("#ChangeSemanticTypesDialogBox").data("classAndPropertyListJson")["elements"][0]["classMap"];
            $.each(classMap, function(index, clazz){
                for(var key in clazz) {
                    if(clazz.hasOwnProperty(key)) {
                        if(key.toLowerCase() == propValue.toLowerCase()) {
                            info["metaPropertyValue"] = clazz[key];
                            newInfo.push(getParamObject("metaPropertyValue", clazz[key], "other"));
                        }
                    }
                }
            });
        } else {
            var existingLinksMap = $("#ChangeSemanticTypesDialogBox").data("classAndPropertyListJson")["elements"][0]["existingDataPropertyInstances"];
            $.each(existingLinksMap, function(index, prop) {
                if (prop["label"] == propValue) {
                    info["metaPropertyValue"] = prop["id"];
                    newInfo.push(getParamObject("metaPropertyValue", prop["id"], "other"));
                }
            });
        }
        info["command"] = "SetMetaPropertyCommand";
    } else {
        // Get the JSON Array that captures all the currently selected semantic types
        var semTypesArray = getCurrentSelectedTypes();
        if(semTypesArray == null)
            return false;
        info["SemanticTypesArray"] = JSON.stringify(semTypesArray);
        if(semTypesArray.length == 0)
            info["command"] = "UnassignSemanticTypeCommand";
        else
            info["command"] = "SetSemanticTypeCommand";
    }

    // info["worksheetId"] = $("td.columnHeadingCell#" + hNodeId).parents("table.WorksheetTable").attr("id");
    // info["hNodeId"] = hNodeId;
    // info["isKey"] = $("input#chooseClassKey").is(":checked");
    // info["workspaceId"] = $.workspaceGlobalInformation.id;
    info["SemanticTypesArray"] = JSON.stringify(semTypesArray);


    newInfo.push(getParamObject("hNodeId", hNodeId,"hNodeId"));
    newInfo.push(getParamObject("SemanticTypesArray", semTypesArray, "other"));
    newInfo.push(getParamObject("worksheetId", info["worksheetId"], "worksheetId"));
    newInfo.push(getParamObject("isKey", $("input#chooseClassKey").is(":checked"), "other"));
    newInfo.push(getParamObject("trainAndShowUpdates", true, "other"));
    newInfo.push(getParamObject("rdfLiteralType", $("div#rdfTypeSelectDiv input").val(), "other"));
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
                $(optionsDiv).removeData("classAndPropertyListJson");
                hideLoading(info["worksheetId"]);
            },
        error :
            function (xhr, textStatus) {
                alert("Error occured with fetching new rows! " + textStatus);
                hideLoading(info["worksheetId"]);
            }
    });

    optionsDiv.dialog("close");
}