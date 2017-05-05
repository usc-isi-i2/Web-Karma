
var LITERAL_TYPE_ARRAY = [
      					"xsd:string","xsd:boolean","xsd:decimal","xsd:integer","xsd:double","xsd:float","xsd:date","xsd:time",
    					"xsd:dateTime","xsd:dateTimeStamp","xsd:gYear","xsd:gMonth","xsd:gDay","xsd:gYearMonth",
    					"xsd:gMonthDay","xsd:duration","xsd:yearMonthDuration","xsd:dayTimeDuration",
    					"xsd:short","xsd:int","xsd:long","xsd:unsignedByte","xsd:unsignedShort","xsd:unsignedInt",
    					"xsd:unsignedLong","xsd:positiveInteger","xsd:nonNegativeInteger","xsd:negativeInteger",
    					"xsd:nonPositiveInteger","xsd:hexBinary","xsd:base64Binar","xsd:anyURI",
    					"xsd:language","xsd:normalizedString","xsd:token","xsd:NMTOKEN","xsd:Namexsd:NCName"
    							 ];

//Language codes from http://www.loc.gov/standards/iso639-2/php/code_list.php
var LANGUAGE_ARRAY = [
					"aa", "ab", "af", "ak", "sq", "am", "ar", "an", "hy", "as", "av", "ae", 
					"ay", "az", "ba", "bm", "eu", "be", "bn", "bh", "bi", "bo", "bs", "br", 
					"bg", "my", "ca", "cs", "ch", "ce", "zh", "cu", "cv", "kw", "co", "cr", 
					"cy", "cs", "da", "de", "dv", "nl", "dz", "el", "en", "eo", "et", "eu", 
					"ee", "fo", "fa", "fj", "fi", "fr", "fy", "ff", "ka", "de", "gd", 
					"ga", "gl", "gv", "el", "gn", "gu", "ht", "ha", "he", "hz", "hi", "ho", 
					"hr", "hu", "hy", "ig", "is", "io", "ii", "iu", "ie", "ia", "id", "ik", 
					"is", "it", "jv", "ja", "kl", "kn", "ks", "ka", "kr", "kk", "km", "ki", 
					"rw", "ky", "kv", "kg", "ko", "kj", "ku", "lo", "la", "lv", "li", "ln", 
					"lt", "lb", "lu", "lg", "mk", "mh", "ml", "mi", "mr", "ms", "mk", "mg", 
					"mt", "mn", "mi", "ms", "my", "na", "nv", "nr", "nd", "ng", "ne", "nl", 
					"nn", "nb", "no", "ny", "oc", "oj", "or", "om", "os", "pa", "fa", "pi", 
					"pl", "pt", "ps", "qu", "rm", "ro", "ro", "rn", "ru", "sg", "sa", "si", 
					"sk", "sk", "sl", "se", "sm", "sn", "sd", "so", "st", "es", "sq", "sc", 
					"sr", "ss", "su", "sw", "sv", "ty", "ta", "tt", "te", "tg", "tl", "th", 
					"bo", "ti", "to", "tn", "ts", "tk", "tr", "tw", "ug", "uk", "ur", "uz", 
					"ve", "vi", "vo", "cy", "wa", "wo", "xh", "yi", "yo", "za", "zh", "zu"
]
var MAX_NUM_SEMTYPE_SEARCH = 10;


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
		var existingTypes, selectedPrimaryRow, classList = null,
			propertyList = null,
			existingPropertyList = null;
		var classPropertyUIDiv;
		var classUI, propertyUI;
		var loadTree = true;

		function init() {
			//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				existingPropertyList = null;
				classList = null;
				propertyList = null;
				loadTree = true;

				hideError();

				$("#semanticType_columnName", dialog).text(columnTitle);

				$("table#semanticTypesTable tr.semTypeRow", dialog).remove();
				$("table#semanticTypesTable tr.editRow", dialog).remove();
				$("#literalTypeSelect").val("");
				$("#languageSelect").val("");

				dialog.removeData("selectedPrimaryRow");
				// Deselect all the advanced options check boxes
				$("div#semanticTypesAdvacedOptionsDiv").hide();
				$("div#semanticTypesAdvacedOptionsDiv input:checkbox").prop('checked', false);
				$("div#semanticTypesAdvacedOptionsDiv input:text").val("");

				// Store a copy of the existing types.
				// This is tha JSON array which is changed when the user adds/changes through GUI and is submitted to the server.
				var tdTag = $("td#" + columnId);
				var typeJsonObject = $(tdTag).data("typesJsonObject");
				if (typeJsonObject) {
					existingTypes = typeJsonObject["SemanticTypesArray"];
				} else {
					existingTypes = [];
				}

				getClasses();
				getProperties();
				getExistingProperties();
				
				var suggestedTypes = getSuggestedTypes();
				
				var addSemTypeOrAdvOption = function(type, isPrimary, isProvenance, isSelected, isCrfModelSuggested) {
					if (type["DisplayLabel"] == "km-dev:classLink") {
						addUriSemanticType(type["DisplayDomainLabel"], type["DomainRDFSLabel"], type["DomainUri"], type["DomainId"], 
								isPrimary, isProvenance, isSelected, isCrfModelSuggested);
					} else if (type["DisplayLabel"] == "km-dev:columnSubClassOfLink") {
						$("#isSubclassOfClass").prop('checked', true);
						$("input#isSubclassOfClassTextBox").val(type["DisplayDomainLabel"]);
						$("#semanticTypesAdvancedOptions").click();
					} else if (type["DisplayLabel"] == "km-dev:dataPropertyOfColumnLink" ||
							type["DisplayLabel"] == "km-dev:objectPropertySpecialization") {
						$("#isSpecializationForEdge").prop('checked', true);
						$("input#isSpecializationForEdgeTextBox").val(type["DisplayDomainLabel"]);
						$("#semanticTypesAdvancedOptions").click();
						
					} else {
						addSemTypeObjectToCurrentTable(type, isSelected, isCrfModelSuggested);
					}
				};
				
				// Populate the table with existing types and CRF suggested types
				$.each(existingTypes, function(index, type) {
					// Take care of the special meta properties that are set through the advanced options
					addSemTypeOrAdvOption(type, type["isPrimary"], type["isProvenance"], true, false);
				});
				if (suggestedTypes) {
					$.each(suggestedTypes["Labels"], function(index, type) {
						if(type["DisplayLabel"] == "km-dev:columnSubClassOfLink" ||
								type["DisplayLabel"] == "km-dev:dataPropertyOfColumnLink" ||
								type["DisplayLabel"] == "km-dev:objectPropertySpecialization") {
							return;
						}
						addSemTypeOrAdvOption(type, false, false, false, true);
					});
				}

				addEmptySemanticType();
				addEmptyUriSemanticType();
			});

			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});


			$("#semanticTypesAdvancedOptions", dialog).on('click', function(e) {
				e.preventDefault();
				$("#semanticTypesAdvacedOptionsDiv").toggle();
				
				var classArray = getClassLabels();
				var propertyArray = getPropertyInstanceLabels();

				$('.typeahead').typeahead('destroy');
				$("input#isSubclassOfClassTextBox", dialog).typeahead('destroy');
				$("input#isSpecializationForEdgeTextBox", dialog).typeahead('destroy');
				$("input#isSubclassOfClassTextBox", dialog).typeahead({
					source: classArray,
					minLength: 0
				});
				$("input#isSpecializationForEdgeTextBox", dialog).typeahead({
					source: propertyArray,
					minLength: 0
				});
			});
			
			

			$("div#semanticTypesAdvacedOptionsDiv input:checkbox").on('click', function(e) {
				console.log("semanticTypesAdvancedOptions checbox change handler");
				var semTypesTable = $("table#semanticTypesTable");
				$.each($("tr.selected.semTypeRow", semTypesTable), function(index, row) {
					$(this).removeClass('selected');
					$("input[name='currentSemanticTypeCheckBoxGroup']:checkbox", $(this)).prop('checked', false);
					$("input[name='isPrimaryGroup']:radio", $(this)).prop('checked', false);
					$("input[name='isProvenanceGroup']:checkbox", $(this)).prop('checked', false);
				});

				$("div#semanticTypesAdvacedOptionsDiv input:checkbox").not($(this)).prop('checked', false);
			});

			$("#addType", dialog).on("click", function(e) {
				e.preventDefault();
				addEmptySemanticType();  
			});

			
			$("#literalTypeSelect").typeahead( 
				{source:LITERAL_TYPE_ARRAY, minLength:0, items:"all"});

			$("#languageSelect").typeahead( 
				{source:LANGUAGE_ARRAY, minLength:0, items:"all"});
		}
		
		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError(err) {
			if (err) {
				$("div.error", dialog).text(err);
			}
			$("div.error", dialog).show();
		}

		function getSuggestedTypes() {
			return getSuggestedSemanticTypes(worksheetId, columnId);
		}

		function validate() {
			if ($("#isSubclassOfClass").prop("checked")) {
				var foundObj = doesClassExist($("input#isSubclassOfClassTextBox", dialog).val());
				if (!foundObj.found) {
					showError("Class for 'specifies class for node' does not exist");
					return false;
				}
			}

			if ($("#isSpecializationForEdge").prop("checked")) {
				var foundObj = doesExistingPropertyExist($("input#isSpecializationForEdgeTextBox", dialog).val());
				if (!foundObj.found) {
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
			$.each($("tr.selected.semTypeRow", table), function(index, row) {
				var fullType = $(row).data("FullType");
				var domainUri = $(row).data("DomainUri");
				var domainId = $(row).data("DomainId");

				// Check if the user selected a fake semantic type object
				if (domainUri == "fakeDomainURI" || fullType == "fakePropertyURI") {
					$(row).addClass("fixMe");
					showError("Semantic type not valid!");
					notValid = true;
					return false;
				}
				// Check if the type already exists (like the user had same type in a previous row)
				var exists = false;
				$.each(existingTypes, function(index2, type) {
					if (type["DomainUri"] == domainUri && fullType == type["FullType"]) {
						exists = true;
						return false;
					}
				});
				if (exists)
					return false;

				// Create a new object and push it into the array
				var newType = new Object();
				newType["FullType"] = fullType;
				newType["DomainUri"] = domainUri;
				newType["DomainId"] = domainId;
				newType["DomainLabel"] = $(row).data("DisplayDomainLabel");

				// Check if it was chosen primary
				newType["isPrimary"] = $("input[name='isPrimaryGroup']:radio", $(row)).is(":checked");
				newType["isProvenance"] = $("input[name='isProvenanceGroup']:checkbox", $(row)).is(":checked");
				existingTypes.push(newType);
			});
			if (notValid)
				return null;

			return existingTypes;
		}


		function saveDialog(e) {
			hideError();

			if (!validate()) {
				return false;
			}

			var info = generateInfoObject(worksheetId, columnId, "");
			var newInfo = info['newInfo']; // Used for commands that take JSONArray as input and are saved in the history
			var hNodeId = columnId;
			
			info["rdfLiteralType"] = $("#literalTypeSelect").val()
			info["language"] = $("#languageSelect").val()

			// Check if any meta property (advanced options) was selected
			var semTypesArray = getCurrentSelectedTypes();
			if ($("#isSubclassOfClass").prop("checked") || $("#isSpecializationForEdge").prop("checked") ||
				(semTypesArray != null && semTypesArray.length == 1 && semTypesArray[0]["FullType"] == "http://isi.edu/integration/karma/dev#classLink")) {
				info["command"] = "SetMetaPropertyCommand";
				var propValue;

				if (semTypesArray != null && semTypesArray.length == 1 && semTypesArray[0]["FullType"] == "http://isi.edu/integration/karma/dev#classLink") {
					propValue = semTypesArray[0]["DomainLabel"];
					info["metaPropertyName"] = "isUriOfClass";
				} else if ($("#isSubclassOfClass").prop("checked")) {
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
					for (var i = 0; i < classList.length; i++) {
						var clazz = classList[i];
						if (clazz.label.toLowerCase() == propValue.toLowerCase()) {
							info["metaPropertyUri"] = clazz.uri;
							info["metaPropertyId"] = clazz.id
							newInfo.push(getParamObject("metaPropertyUri", clazz.uri, "other"));
							newInfo.push(getParamObject("metaPropertyId", clazz.id, "other"));
							valueFound = true;
							break;
						}
					}
				} else {
					for (var i = 0; i < existingPropertyList.length; i++) {
						var prop = existingPropertyList[i];
						if (prop.id.toLowerCase() == propValue.toLowerCase()) {
							info["metaPropertyUri"] = prop.id;
							info["metaPropertyId"] = prop.id;
							newInfo.push(getParamObject("metaPropertyUri", prop.id, "linkWithHNodeId"));
							newInfo.push(getParamObject("metaPropertyId", prop.id, "linkWithHNodeId"));
							valueFound = true;
							break;
						}
					}
				}
				if (!valueFound) {
					showError("Class/Property does not exist");
					return false;
				}

			} else { // Get the JSON Array that captures all the currently selected semantic types

				if (semTypesArray == null || semTypesArray === false)
					return false;
				info["SemanticTypesArray"] = JSON.stringify(semTypesArray);
				if (semTypesArray.length == 0)
					info["command"] = "UnassignSemanticTypeCommand";
				else
					info["command"] = "SetSemanticTypeCommand";
			}

			info["SemanticTypesArray"] = JSON.stringify(semTypesArray);
			newInfo.push(getParamObject("SemanticTypesArray", semTypesArray, "other"));
			newInfo.push(getParamObject("trainAndShowUpdates", true, "other"));
			newInfo.push(getParamObject("rdfLiteralType", $("#literalTypeSelect").val(), "other"));
			newInfo.push(getParamObject("language", $("#languageSelect").val(), "other"));
			info["newInfo"] = JSON.stringify(newInfo);

			console.log(info);
			showLoading(info["worksheetId"]);
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				complete: function(xhr, textStatus) {
					var json = $.parseJSON(xhr.responseText);
					parse(json);
					existingPropertyList = null;
					classList = null;
					propertyList = null;
					hideLoading(info["worksheetId"]);
				},
				error: function(xhr, textStatus) {
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
			fakeSemType["DomainRDFSLabel"] = "";
			fakeSemType["DisplayRDFSLabel"] = "";
			fakeSemType["DisplayLabel"] = "Property";
			fakeSemType["DisplayDomainLabel"] = "Class";
			// Add it to the table
			addSemTypeObjectToCurrentTable(fakeSemType, false, false);
		}

		function addUriSemanticType(domainLabel, domainRDFSLabel, domainUri, domainId, 
					isPrimary, isProvenance, isSelected, 
					isCrfModelSuggested) {
			var type = new Object();
			type["FullType"] = "http://isi.edu/integration/karma/dev#classLink";
			type["DomainId"] = domainId;
			type["DomainUri"] = domainUri;
			type["DisplayLabel"] = "km-dev:classLink";
			type["DisplayRDFSLabel"] = "";
			type["DisplayDomainLabel"] = domainLabel;
			type["DomainRDFSLabel"] = domainRDFSLabel;
			type["isPrimary"] = isPrimary;
			type["isProvenance"] = isProvenance;
			// Add it to the table
			addSemTypeObjectToCurrentTable(type, isSelected, isCrfModelSuggested);
		}

		function addEmptyUriSemanticType() {
			// Create a fake sem type object to how in the table
			var fakeSemType = new Object();
			fakeSemType["FullType"] = "http://isi.edu/integration/karma/dev#classLink";
			fakeSemType["DomainUri"] = "fakeDomainURI";
			fakeSemType["DomainId"] = "fakeDomainID";
			fakeSemType["DomainRDFSLabel"] = "";
			fakeSemType["DisplayLabel"] = "km-dev:classLink";
			fakeSemType["DisplayDomainLabel"] = "Class";
			fakeSemType["DisplayRDFSLabel"] = "";
			// Add it to the table
			addSemTypeObjectToCurrentTable(fakeSemType, false, false);
		}

		function addSemTypeObjectToCurrentTable(semTypeObject, isSelected, isCrfModelSuggested) {
			var table = $("#semanticTypesTable");

			// Check if it is eligible to be added to the table
			var isValid = true;
			$.each($("tr", table), function(index, row) {
				if ($(row).data("FullType") == semTypeObject["FullType"] && $(row).data("DomainId") == semTypeObject["DomainId"]) {
					// We allow multiple fake semantic type objects to be added
					if (!(semTypeObject["FullType"] == "fakePropertyURI" && semTypeObject["DomainUri"] == "fakeDomainURI"))
						isValid = false;
				}
			});
			if (!isValid)
				return false;

			// Add it to the table
			var displayLabel = "";
			var property = Settings.getInstance().getDisplayLabel(semTypeObject["DisplayLabel"], semTypeObject["DisplayRDFSLabel"]);
			var clazz = Settings.getInstance().getDisplayLabel(semTypeObject["DisplayDomainLabel"], semTypeObject["DomainRDFSLabel"]);

			if (property == "km-dev:classLink")
				property = "uri";
			if (semTypeObject["DomainUri"].length == 0 || semTypeObject["DomainUri"] == "")
				displayLabel = property;
			else
				displayLabel = "<span class='italic'>" + property + "</span> of " + clazz;

			var isProvenance = false;
			if(semTypeObject["isProvenance"]) {
				isProvenance = semTypeObject["isProvenance"];
			}

			var trTag = $("<tr>").addClass("semTypeRow")
				.data("FullType", semTypeObject["FullType"])
				.data("DomainRDFSLabel", semTypeObject["DomainRDFSLabel"])
				.data("DomainUri", semTypeObject["DomainUri"])
				.data("DomainId", semTypeObject["DomainId"])
				.data("DisplayDomainLabel", semTypeObject["DisplayDomainLabel"])
				.data("DisplayLabel", semTypeObject["DisplayLabel"])
				.data("DisplayRDFSLabel",semTypeObject["DisplayRDFSLabel"])
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
				.append($("<td>").append($("<input>")
					.attr("type", "checkbox")
					.attr("name", "isProvenanceGroup")
					.attr("value", semTypeObject["DisplayLabel"])
					.prop("checked", isProvenance)
					.val(semTypeObject["DisplayLabel"])))
				.append($("<td>")
					.append($("<button>").attr("type", "button").addClass("btn").addClass("editButton").addClass("btn-default").text("Edit").click(showSemanticTypeEditOptions))
					.append($("<button>").attr("type", "button").addClass("btn").addClass("hideButton").css("display", "none").addClass("btn-default").text("Hide").click(hideSemanticTypeEditOptions)));

			if (isCrfModelSuggested)
			// trTag.append($("<td>").addClass("CRFSuggestedText").text("  (CRF Suggested)"));
				trTag.append($("<td>").addClass("CRFSuggestedText"));
			else
				trTag.append($("<td>"));

			if (isSelected)
				trTag.addClass("selected");

			if (semTypeObject["isPrimary"]) {
				$("input[name='isPrimaryGroup']:radio", trTag).prop('checked', true);
				selectedPrimaryRow = trTag;
				$("#literalTypeSelect").val(semTypeObject["rdfLiteralType"]);
				$("#languageSelect").val(semTypeObject["language"]);
			}

			if (semTypeObject["DomainUri"].length == 0 || semTypeObject["DomainUri"] == "")
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
			if ($(this).is(':checked')) {
				//if(parentTr.data("DisplayLabel") == "km-dev:classLink"){
				var rows = $("tr.selected", table);
				for (var i = 0; i < rows.length; i++) {
					var row = rows[i];
					if ($(row).data("DisplayLabel") == "km-dev:classLink" || parentTr.data("DisplayLabel") == "km-dev:classLink") {
						$("input[name='currentSemanticTypeCheckBoxGroup']:checkbox", row).prop('checked', false);
						$("input[name='isPrimaryGroup']:radio", row).prop('checked', false);
						$(row).removeClass("selected");
					}

				}
				$(this).prop('checked', true);
				//}
				parentTr.addClass("selected");
				var numRows = $("tr.selected", table).length;
				if (numRows == 1)
					$("input[name='isPrimaryGroup']:radio", parentTr).prop('checked', true);
			}
			// If it was unchecked
			else {
				parentTr.removeClass("selected");
				// If the row was marked as primary, make some other selected row as primary
				if ($("input[name='isPrimaryGroup']:radio", parentTr).is(':checked')) {
					if ($("tr.selected", table).length == 0)
						$("input[name='isPrimaryGroup']:radio", parentTr).prop('checked', false);
					else {
						$.each($("tr.selected", table), function(index, row) {
							if (index == 0) {
								$("input[name='isPrimaryGroup']:radio", row).prop('checked', true);
								return false;
							}
						});
					}
				}
			}
		}

		function getClasses() {
			if (classList == null) {
				classList = getAllClasses(worksheetId);

				if (loadTree) {
					loadTree = ($.workspaceGlobalInformation.UISettings.maxLoadedClasses == -1 ||
						classList.length <= $.workspaceGlobalInformation.UISettings.maxLoadedClasses) ? true : false;
				}
			}

			var result = [];
			$.each(classList, function(index, clazz) {
				result.push(ClassUI.getNodeObject(clazz.label, clazz.rdfsLabel, clazz.id, clazz.uri));
			});
			return result;
		}

		function getClassesForProperty(property) {
			var classes = getAllClassesForProperty(worksheetId, property.uri);
			var result = [];
			$.each(classes, function(index, clazz) {
				result.push(ClassUI.getNodeObject(clazz.label, clazz.rdfsLabel, clazz.id, clazz.uri));
			});
			return result;
		}

		function getProperties() {
			if (propertyList == null) {
				propertyList = getAllDataProperties(worksheetId);
				if (loadTree)
					loadTree = ($.workspaceGlobalInformation.UISettings.maxLoadedProperties == -1 ||
						propertyList.length <= $.workspaceGlobalInformation.UISettings.maxLoadedProperties) ? true : false;
			}

			var result = [];
			$.each(propertyList, function(index, prop) {
				result.push(PropertyUI.getNodeObject(prop.label, prop.rdfsLabel, prop.id, prop.uri));
			});
			return result;
		}

		function getExistingProperties() {
			if (existingPropertyList == null)
				existingPropertyList = getAllExistingProperties(worksheetId);
			return existingPropertyList;
		}

		function getPropertiesForClass(thisClass) {
			var props = getAllPropertiesForClass(worksheetId, thisClass.uri);
			var result = [];
			$.each(props, function(index, prop) {
				result.push(PropertyUI.getNodeObject(prop.label, prop.rdfsLabel, prop.id, prop.uri));
			});
			return result;
		}

		function getClassLabels() {
			var classLabels = [];

			$.each(classList, function(index, clazz) {
				classLabels.push(clazz.label);
			});

			return classLabels;
		}

		function getPropertyLabels() {
			var propLabels = [];

			$.each(propertyList, function(index, prop) {
				propLabels.push(prop.label);
			});

			return propLabels;
		}

		function getPropertyInstanceLabels() {
			var propLabels = [];

			$.each(existingPropertyList, function(index, prop) {
				propLabels.push(prop.id);
			});

			return propLabels;
		}

		function hideSemanticTypeEditOptions() {
			var table = $("#semanticTypesTable");
			$("tr", table).show();
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
			$("tr", table).hide();
			parentTrTag.show();
			$("tr", table).removeClass('currentEditRow');
			$("td.CRFSuggestedText", parentTrTag).text("");

			$(".editButton", parentTrTag).hide();
			$(".hideButton", parentTrTag).show();


			$(parentTrTag).addClass("currentEditRow");

			// Automatically select the row
			if (!$("input[name='currentSemanticTypeCheckBoxGroup']:checkbox", parentTrTag).is(':checked')) {
				$("input[name='currentSemanticTypeCheckBoxGroup']:checkbox", parentTrTag).prop('checked', true);
				$(parentTrTag).addClass("selected");

				if ($("tr.selected", table).length == 1)
					$("input[name='isPrimaryGroup']:radio", parentTrTag).prop('checked', true);
			}


			if (classList == null || propertyList == null) {
				alert("Class and property list not yet loaded from the server!");
				return false;
			}

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
			if ($(parentTrTag).data("DisplayLabel") == "km-dev:classLink") {
				showPropertiesList = false;
				classFuncTop = null;
			}
			classUI = new ClassUI("semanticTypeEditClass", classFuncTop, classFuncBottom, 100, loadTree, getClasses, MAX_NUM_SEMTYPE_SEARCH);
			propertyUI = new PropertyUI("semanticTypeEditProperty", getPropertiesForClass, getProperties, 100, loadTree, getProperties, MAX_NUM_SEMTYPE_SEARCH);

			classUI.setHeadings("Classes with Selected Property", "All Classes");
			propertyUI.setHeadings("Properties of Selected Class", "All Properties");

			if ($(parentTrTag).data("ResourceType") == "Class") {
				var classLabel = $(parentTrTag).data("DisplayLabel");
				var classUri = $(parentTrTag).data("DomainUri");
				var classRDFSLabel = $(parentTrTag).data("DomainRDFSLabel");
				classUI.setDefaultClass(classLabel, classRDFSLabel, classUri, classUri);
				propertyUI.setSelectedClass(classLabel, classRDFSLabel, classUri, classUri);
				//defaultProperty = "";
			} else {
				defaultClass = $(parentTrTag).data("DisplayDomainLabel");
				defaultClassRDFS = $(parentTrTag).data("DomainRDFSLabel");
				var classUri = $(parentTrTag).data("DomainUri");
				classUI.setDefaultClass(defaultClass, defaultClassRDFS, classUri, classUri);
				propertyUI.setSelectedClass(defaultClass, defaultClassRDFS, classUri, classUri);
				var defaultProperty = $(parentTrTag).data("DisplayLabel");
				var defaultPropertyUri = $(parentTrTag).data("FullType");
				var defaultPropertyRDFSLabel = $(parentTrTag).data("DisplayRDFSLabel");
				propertyUI.setDefaultProperty(defaultProperty, defaultPropertyRDFSLabel, defaultPropertyUri, defaultPropertyUri);
				classUI.setSelectedProperty(defaultProperty, defaultPropertyRDFSLabel, defaultPropertyUri, defaultPropertyUri);
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
			if (showPropertiesList) {
				propertyUI.generateJS(propDiv, true);
			}
		}

		function doesExistingPropertyExist(propValue) {
			var found = false;
			var id = "";

			$.each(existingPropertyList, function(index, prop) {
				if (prop.id == propValue) {
					id = prop.id;
					found = true;
				}
			});
			return {
				"found": found,
				"id": id
			};
		}

		function doesPropertyExist(inputVal) {

			var found = false;
			var uri = "";
			var properCasedLabel = "";
			var properCasedRDFSLabel = "";
			$.each(propertyList, function(index, prop) {
				//1. Check if label is a match
				if (prop.label.toLowerCase() == inputVal.toLowerCase()) {
					found = true;
					uri = prop.uri;
					properCasedLabel = prop.label;
					properCasedRDFSLabel = prop.rdfsLabel;
				}

				//2. Check if id is a match
				else if (prop.id.toLowerCase() == inputVal.toLowerCase()) {
					found = true;
					uri = prop.uri;
					properCasedLabel = prop.label;
					properCasedRDFSLabel = prop.rdfsLabel;
				}
			});
			return {
				"found": found,
				"uri": uri,
				"properCasedLabel": properCasedLabel,
				"properCasedRDFSLabel": properCasedRDFSLabel
			};
		}

		function validatePropertyInputValue(propertyData) {
			var inputVal = propertyData.label;

			hideError();

			$("table#semanticTypesTable tr").removeClass("fixMe");

			var foundObj = doesPropertyExist(inputVal);
			var found = foundObj.found;
			var uri = foundObj.uri;
			var properCasedLabel = foundObj.properCasedLabel;
			var properCasedRDFSLabel = foundObj.properCasedRDFSLabel;


			if (!found && $.trim(inputVal) != "") {
				showError("Input data property not valid!");
				return false;
			}

			if (loadTree)
				classUI.refreshClassDataTop(propertyData.label, propertyData.rdfsLabel, propertyData.id, propertyData.uri);

			var rowToChange = $(classPropertyUIDiv).parents("tr.editRow").data("editRowObject");
			if (rowToChange != null) {
				var displayLabel = "";

				if ($(rowToChange).data("ResourceType") == "Class") {
					if ($.trim(inputVal) == "")
						return false;
					// existing fullType (which was a class) becomes the domain of the chosen data property. So changing from class sem type to data prop sem type
					var domain = $(rowToChange).data("FullType");
					var displayDomainLabel = $(rowToChange).data("DisplayLabel");
					$(rowToChange).data("FullType", uri).data("DisplayLabel", properCasedLabel).data("DisplayRDFSLabel", properCasedRDFSLabel)
						.data("DomainId", domain).data("DisplayDomainLabel", displayDomainLabel)
						.data("ResourceType", "DataProperty");

					displayLabel = "<span class='italic'>" + 
							Settings.getInstance().getDisplayLabel($(rowToChange).data("DisplayLabel"),$(rowToChange).data("DisplayRDFSLabel")) + 
								"</span> of " + 
							Settings.getInstance().getDisplayLabel($(rowToChange).data("DisplayDomainLabel"), $(rowToChange).data("DomainRDFSLabel"));
				} else {
					// Special case when the property input box is empty (data property sem type changed to class sem type)
					if ($.trim(inputVal) == "" && $(rowToChange).data("DomainId") != "") {
						var newFullType = $(rowToChange).data("DomainId");
						var newDisplayLabel = $(rowToChange).data("DisplayDomainLabel");
						var newDisplayRDFSLabel = $(rowToChange).data("DomainRDFSLabel");

						$(rowToChange).data("ResourceType", "Class").data("FullType", newFullType).data("DisplayLabel", newDisplayLabel).data("DomainId", "").data("DomainUri", "").data("DisplayDomainLabel", "").data("DomainRDFSLabel", newDisplayRDFSLabel);
						displayLabel = Settings.getInstance().getDisplayLabel($(rowToChange).data("DisplayLabel"),$(rowToChange).data("DisplayRDFSLabel"));
					} else {
						$(rowToChange).data("FullType", uri).data("DisplayLabel", properCasedLabel).data("DisplayRDFSLabel", properCasedRDFSLabel);
						displayLabel = "<span class='italic'>" + 
							Settings.getInstance().getDisplayLabel($(rowToChange).data("DisplayLabel"),$(rowToChange).data("DisplayRDFSLabel")) + 
								"</span> of " + 
							Settings.getInstance().getDisplayLabel($(rowToChange).data("DisplayDomainLabel"), $(rowToChange).data("DomainRDFSLabel"));
					}
				}
				$("label.displayLabel", rowToChange).html(displayLabel);
			}
		}

		function doesClassExist(inputVal) {
			var found = false;
			var uri = "";
			var properCasedLabel = "";
			var properCasedRDFSLabel = "";
			var inputWithAdd = inputVal + " (add)";
			$.each(classList, function(index, clazz) {
				//1. Check if label is a match
				if (clazz.label.toLowerCase() == inputVal.toLowerCase()) {
					found = true;
					uri = clazz.uri;
					properCasedLabel = clazz.label;
					properCasedRDFSLabel = clazz.rdfsLabel;
				} else if (clazz.label.toLowerCase() == inputWithAdd.toLowerCase()) {
					found = true;
					uri = clazz.uri;
					properCasedLabel = clazz.label;
					properCasedRDFSLabel = clazz.rdfsLabel;
				}

				//2.Check if id is a match
				else if (clazz.id.toLowerCase() == inputVal.toLowerCase()) {
					found = true;
					uri = clazz.uri;
					properCasedLabel = clazz.label;
					properCasedRDFSLabel = clazz.rdfsLabel;
				} else if (clazz.id.toLowerCase() == inputWithAdd.toLowerCase()) {
					found = true;
					uri = clazz.uri;
					properCasedLabel = clazz.label;
					properCasedRDFSLabel = clazz.rdfsLabel;
				}
			});
			return {
				"found": found,
				"uri": uri,
				"properCasedLabel": properCasedLabel,
				"properCasedRDFSLabel": properCasedRDFSLabel
			};
		}

		function validateClassInputValue(classData) {
			var inputVal = classData.label;
			var updateLabels = true;

			hideError();
			$("table#semanticTypesTable tr").removeClass("fixMe");

			var foundObj = doesClassExist(inputVal);
			var found = foundObj.found;
			var uri = foundObj.uri;
			var properCasedRDFSLabel = foundObj.properCasedRDFSLabel;
			var properCasedLabel = foundObj.properCasedLabel;

			if (!found) {
				showError("Input class/instance not valid!");
				return false;
			}

			// Use the value in proper case as input value
			if (loadTree) {
				propertyUI.refreshPropertyDataTop(classData.label, classData.rdfsLabel, classData.id, classData.uri);
			}

			if (updateLabels) {
				var rowToChange = $(classPropertyUIDiv).parents("tr.editRow").data("editRowObject");
				if (rowToChange != null) {
					var displayLabel = "";
					if ($(rowToChange).data("ResourceType") == "Class") {
						$(rowToChange).data("FullType", uri).data("DisplayLabel", properCasedLabel).data("DisplayRDFSLabel", properCasedRDFSLabel);
						displayLabel = Settings.getInstance().getDisplayLabel(properCasedLabel, properCasedRDFSLabel);
					} else {
						// If no value has been input in the data property box, change from data property sem type to class sem type
						var propertyOld = Settings.getInstance().getDisplayLabel($(rowToChange).data("DisplayLabel"),$(rowToChange).data("DisplayRDFSLabel"));
						if (propertyOld == "km-dev:classLink") propertyOld = "uri";

						if (propertyOld != "uri" && $.trim($("input.propertyInput", classPropertyUIDiv).val()) == "") {
							$(rowToChange).data("ResourceType", "Class").data("FullType", uri).data("DisplayLabel", properCasedLabel).data("DisplayRDFSLabel", properCasedRDFSLabel);
							displayLabel = Settings.getInstance().getDisplayLabel(properCasedLabel, properCasedRDFSLabel);
						} else {
							$(rowToChange).data("DomainUri", uri).data("DomainId", classData.id).data("DisplayDomainLabel", properCasedLabel).data("DomainRDFSLabel", properCasedRDFSLabel)
							displayLabel = "<span class='italic'>" + propertyOld + "</span> of " + Settings.getInstance().getDisplayLabel(properCasedLabel, properCasedRDFSLabel);
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
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
		};


		return { //Return back the public methods
			show: show,
			init: init
		};
	};

	function getInstance() {
		if (!instance) {
			instance = new PrivateConstructor();
			instance.init();
		}
		return instance;
	}

	return {
		getInstance: getInstance
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
		var columnLabel, columnRdfsLabel, columnRdfsComment, columnUri, columnDomain, columnType, isColumnUri;

		var selectedFromClass, selectedProperty, selectedToClass;
		var allClasses = null,
			allProperties = null,
			selectedClasses = null,
			selectedProperties = null;

		var fromClassUI, propertyUI, toClassUI;
		var changeFromNode, changeToNode, changeLink;

		var loadTree = true;

		function init() {
			selectedFromClass = {
				label: "",
				rdfsLabel: "",
				id: "",
				uri: ""
			};
			selectedToClass = {
				label: "",
				rdfsLabel: "",
				id: "",
				uri: ""
			};
			selectedProperty = {
				label: "",
				rdfsLabel: "",
				id: "",
				uri: ""
			};

			fromClassUI = new ClassUI("incomingOutgoingLinksDialog_fromClass", getExistingClassNodes, getAllClassNodes, 200, 
									loadTree, getAllClassNodes, MAX_NUM_SEMTYPE_SEARCH);
			toClassUI = new ClassUI("incomingOutgoingLinksDialog_toClass", getExistingClassNodes, getAllClassNodes, 200, 
									loadTree, getAllClassNodes, MAX_NUM_SEMTYPE_SEARCH);
			propertyUI = new PropertyUI("incomingOutgoingLinksDialog_property", getPropertyForClass, getProperties, 200, 
									loadTree, getProperties, MAX_NUM_SEMTYPE_SEARCH);

			fromClassUI.setHeadings("Classes in Model", "All Classes");
			toClassUI.setHeadings("Classes in Model", "All Classes");
			propertyUI.setHeadings("Compatible Properties", "All Properties");

			fromClassUI.setClassLabel("From Class");
			toClassUI.setClassLabel("To Class");

			fromClassUI.onClassSelect(onSelectFromClassInputValue);
			toClassUI.onClassSelect(onSelectToClassInputValue);
			propertyUI.onPropertySelect(onSelectPropertyInputValue);


			//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				hideError();
				allClasses = null;
				allProperties = null;
				selectedClasses = null;
				selectedProperties = null;
				loadTree = true;

				setLinkLabel();
				$("div.main", dialog).empty();
				var row = $("<div>").addClass("row");
				$("div.main", dialog).append(row);
				getAllClassNodes();
				getProperties();
				toClassUI.setTreeLoad(loadTree);
				fromClassUI.setTreeLoad(loadTree);
				propertyUI.setTreeLoad(loadTree);

				if (linkType == "incoming" || linkType == "changeIncoming") {
					if (linkType == "incoming")
						$("#incomingOutgoingLinksDialog_title", dialog).text("Add incoming link for " + columnLabel);
					else
						$("#incomingOutgoingLinksDialog_title", dialog).text("Change incoming link for " + columnLabel);

					var classDiv = $("<div>").addClass("col-sm-6");
					row.append(classDiv);
					fromClassUI.setDefaultClass(selectedFromClass.label, selectedFromClass.rdfsLabel, selectedFromClass.id, selectedFromClass.uri);
					fromClassUI.setSelectedProperty(selectedProperty.label, selectedProperty.rdfsLabel, selectedProperty.id, selectedProperty.uri);
					fromClassUI.generateJS(classDiv, true);

					var propertyDiv = $("<div>").addClass("col-sm-6");
					row.append(propertyDiv);
					propertyUI.setDefaultProperty(selectedProperty.label, selectedProperty.rdfsLabel, selectedProperty.id, selectedProperty.uri);
					propertyUI.setSelectedClass(selectedFromClass.label, selectedFromClass.rdfsLabel, selectedFromClass.id, selectedFromClass.uri);
					propertyUI.generateJS(propertyDiv, true);
				} else if (linkType == "outgoing" || linkType == "changeOutgoing") {
					if (linkType == "outgoing")
						$("#incomingOutgoingLinksDialog_title", dialog).text("Add outgoing link for " + columnLabel);
					else
						$("#incomingOutgoingLinksDialog_title", dialog).text("Change outgoing link for " + columnLabel);

					var classDiv = $("<div>").addClass("col-sm-6");
					row.append(classDiv);
					toClassUI.setDefaultClass(selectedToClass.label, selectedToClass.rdfsLabel, selectedToClass.id, selectedToClass.uri);
					toClassUI.setSelectedProperty(selectedProperty.label, selectedProperty.rdfsLabel, selectedProperty.id, selectedProperty.uri);
					toClassUI.generateJS(classDiv, true);

					var propertyDiv = $("<div>").addClass("col-sm-6");
					row.append(propertyDiv);
					propertyUI.setDefaultProperty(selectedProperty.label, selectedProperty.rdfsLabel, selectedProperty.id, selectedProperty.uri);
					propertyUI.setSelectedClass(selectedToClass.label, selectedToClass.rdfsLabel, selectedToClass.id, selectedToClass.uri);
					propertyUI.generateJS(propertyDiv, true);
				} else if (linkType == "changeLink") {
					$("#incomingOutgoingLinksDialog_title", dialog).text("Change link");
					getAllClassNodes();
					getExistingClassNodes();


					var propertyDiv = $("<div>").addClass("col-sm-12");
					row.append(propertyDiv);
					propertyUI.setDefaultProperty(selectedProperty.label, selectedProperty.rdfsLabel, selectedProperty.id, selectedProperty.uri);
					propertyUI.setSelectedClass(selectedFromClass.label, selectedFromClass.rdfsLabel, selectedFromClass.id, selectedFromClass.uri);
					propertyUI.generateJS(propertyDiv, true);

				}
			});

			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});
		}

		function setSelectedFromClass(id) {
			console.log("IncomingOutgoingLinksDialog:setSelectedFromClass:" + id);
			if (allClasses != null) {
				for (var i = 0; i < allClasses.length; i++) {
					var clazz = allClasses[i];
					var clazzElem = ClassUI.parseNodeObject(clazz);
					var clazzLbl = clazzElem[0];
					var clazzRdfsLabel = clazzElem[1];
					var clazzId = clazzElem[2];
					var clazzUri = clazzElem[3];
					if (clazzId == id) {
						fromClassUI.setDefaultClass(clazzLbl, clazzRdfsLabel, clazzId, clazzUri);
						onSelectFromClassInputValue({
							"uri": clazzUri,
							"label": clazzLbl,
							"rdfsLabel": clazzRdfsLabel,
							"id": clazzId
						});
						return;
					}
				}
			} else {
				window.setTimeout(function() {
					setSelectedFromClass(id);
				}, 100);
				return;
			}
			if (selectedClasses != null) {
				for (var i = 0; i < selectedClasses.length; i++) {
					var clazz = selectedClasses[i];
					var clazzElem = ClassUI.parseNodeObject(clazz);
					var clazzLbl = clazzElem[0];
					var clazzRdfsLabel = clazzElem[1];
					var clazzId = clazzElem[2];
					var clazzUri = clazzElem[3];
					if (clazzId == id) {
						fromClassUI.setDefaultClass(clazzLbl, clazzRdfsLabel, clazzId, clazzUri);
						onSelectFromClassInputValue({
							"uri": clazzUri,
							"rdfsLabel": clazzRdfsLabel,
							"label": clazzLbl,
							"id": clazzId
						});
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
			if (allClasses != null) {
				for (var i = 0; i < allClasses.length; i++) {
					var clazz = allClasses[i];
					var clazzElem = ClassUI.parseNodeObject(clazz);
					var clazzLbl = clazzElem[0];
					var clazzRdfsLabel = clazzElem[1];
					var clazzId = clazzElem[2];
					var clazzUri = clazzElem[3];
					if (clazzId == id) {
						toClassUI.setDefaultClass(clazzLbl, clazzRdfsLabel, clazzId, clazzUri);
						onSelectToClassInputValue({
							"uri": clazzUri,
							"rdfsLabel": clazzRdfsLabel,
							"label": clazzLbl,
							"id": clazzId
						});
						return;
					}
				}
			} else {
				window.setTimeout(function() {
					setSelectedToClass(id);
				}, 100);
				return;
			}
			if (selectedClasses != null) {
				for (var i = 0; i < selectedClasses.length; i++) {
					var clazz = selectedClasses[i];
					var clazzElem = ClassUI.parseNodeObject(clazz);
					var clazzLbl = clazzElem[0];
					var clazzRdfsLabel = clazzElem[1];
					var clazzId = clazzElem[2];
					var clazzUri = clazzElem[3];
					if (clazzId == id) {
						toClassUI.setDefaultClass(clazzLbl, clazzRdfsLabel, clazzId, clazzUri);
						onSelectToClassInputValue({
							"uri": clazzUri,
							"rdfsLabel": clazzRdfsLabel,
							"label": clazzLbl,
							"id": clazzId
						});
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
			if (allProperties != null) {
				for (var i = 0; i < allProperties.length; i++) {
					var prop = allProperties[i];
					var propElem = PropertyUI.parseNodeObject(prop);
					var propLbl = propElem[0];
					var propRdfsLabel = propElem[1];
					var propId = propElem[2];
					var propUri = propElem[3];

					if (propId == id) {
						propertyUI.setDefaultProperty(propLbl, propRdfsLabel, propId, propUri);
						onSelectPropertyInputValue({
							"uri": propUri,
							"rdfsLabel": propRdfsLabel,
							"label": propLbl,
							"id": propId
						});
						return;
					}
				}
			} else {
				window.setTimeout(function() {
					setSelectedProperty(id);
				}, 100);
				return;
			}
			if (selectedProperties != null) {
				for (var i = 0; i < selectedProperties.length; i++) {
					var prop = selectedProperties[i];
					var propElem = PropertyUI.parseNodeObject(prop);
					var propLbl = propElem[0];
					var propRdfsLabel = propElem[1];
					var propId = propElem[2];
					var propUri = propElem[3];

					if (propId == id) {
						propertyUI.setDefaultProperty(propLbl, propRdfsLabel, propId, propUri);
						if (dialog.hasClass('in')) { //dialog is shown
							onSelectPropertyInputValue({
								"uri": propUri,
								"label": propLbl,
								"rdfsLabel": propRdfsLabel,
								"id": propId
							});
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

		function getClassFromList(label) {
			var clazz = null;
			var inputVal = label.toLowerCase();
			var inputValWithAdd = inputVal + " (add)";
			for (var i = 0; i < allClasses.length; i++) {
				var thisClass = allClasses[i].metadata;
				if (thisClass.label.toLowerCase() == inputVal || thisClass.label.toLowerCase() == inputValWithAdd || thisClass.id.toLowerCase() == inputVal || thisClass.id.toLowerCase() == inputValWithAdd) {
					clazz = thisClass;
					break;
				}
			}
			return clazz;
		}

		function getPropertyFromList(label) {
			var prop = null;
			var inputVal = label.toLowerCase();
			var inputValWithAdd = inputVal + " (add)";
			for (var i = 0; i < allProperties.length; i++) {
				var thisProp = allProperties[i].metadata;
				if (thisProp.label.toLowerCase() == inputVal || thisProp.label.toLowerCase() == inputValWithAdd || thisProp.id.toLowerCase() == inputVal || thisProp.id.toLowerCase() == inputValWithAdd) {
					prop = thisProp;
					break;
				}
			}
			return prop;
		}

		function onSelectFromClassInputValue(clazz) {
			if (!loadTree) {
				clazz = getClassFromList(clazz.label);
			}

			selectedFromClass = clazz;
			if (dialog.hasClass('in')) {
				propertyUI.refreshPropertyDataTop(clazz.label, clazz.id, clazz.uri);
				setLinkLabel();
			}
		}

		function onSelectToClassInputValue(clazz) {
			if (!loadTree) {
				clazz = getClassFromList(clazz.label);
			}

			selectedToClass = clazz;
			if (dialog.hasClass('in')) {
				propertyUI.refreshPropertyDataTop(clazz.label, clazz.id, clazz.uri);
				setLinkLabel();
			}
		}

		function onSelectPropertyInputValue(prop) {
			if (!loadTree) {
				prop = getPropertyFromList(prop.label);
			}
			selectedProperty = prop;
			setLinkLabel();
		}

		function setLinkLabel() {
			//    		var direction = (linkType == "incoming")? "from" : "to";
			//    		$("#finalLink", dialog).text("Add link '" + selectedProperty.label + "' "  + direction + " '" + selectedClass.label + "'");
		}

		function getExistingClassNodes() {
			if (loadTree && selectedClasses == null) {
				var classes = getClassesInModel(worksheetId);
				var result = [];
				$.each(classes, function(index, clazz) {
					result.push(ClassUI.getNodeObject(clazz.label, clazz.rdfsLabel, clazz.id, clazz.uri));
				});
				selectedClasses = result;
			}
			return selectedClasses;
		}


		function getAllClassNodes() {
			if (allClasses == null) {
				var classes = getAllClasses(worksheetId);
				if (loadTree)
					loadTree = ($.workspaceGlobalInformation.UISettings.maxLoadedClasses == -1 ||
						classes.length <= $.workspaceGlobalInformation.UISettings.maxLoadedClasses) ? true : false;

				var result = [];
				$.each(classes, function(index, clazz) {
					result.push(ClassUI.getNodeObject(clazz.label, clazz.rdfsLabel, clazz.id, clazz.uri));
				});
				allClasses = result;
			}
			return allClasses;
		}

		function getProperties() {
			if (allProperties == null) {
				var props;
				if (columnType == "ColumnNode" || (columnType == "LiteralNode" && isColumnUri == false))
					props = getAllDataProperties(worksheetId);
				else
					props = getAllObjectProperties(worksheetId);
				var result = [];
				$.each(props, function(index, prop) {
					result.push(PropertyUI.getNodeObject(prop.label, prop.rdfsLabel, prop.id, prop.uri));
				});
				allProperties = result;

				if (loadTree)
					loadTree = ($.workspaceGlobalInformation.UISettings.maxLoadedProperties == -1 ||
						allProperties.length <= $.workspaceGlobalInformation.UISettings.maxLoadedProperties) ? true : false;
			}
			return allProperties;
		}

		function getPropertyForClass(selectedClass) {
			var result = [];
			if (loadTree) {
				var domain, range;
				var startNodeClass = columnDomain;
				if (columnType == "ColumnNode" || columnType == "LiteralNode")
					startNodeClass = "";
				if (linkType == "incoming" || linkType == "changeIncoming" || linkType == "changeLink") {
					domain = selectedClass.uri;
					range = startNodeClass;
				} else { //if(linkType == "outgoing" || linkType == "changeOutgoing") {
					domain = startNodeClass;
					range = selectedClass.uri;
				}

				var props = getAllPropertiesForDomainRange(worksheetId, domain, range);
				$.each(props, function(index, prop) {
					result.push(PropertyUI.getNodeObject(prop.label, prop.rdfsLabel, prop.id, prop.uri));
				});
				selectedProperties = result;
			}
			return result;
		}


		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError(err) {
			if (err) {
				$("div.error", dialog).text(err);
			}
			$("div.error", dialog).show();
		}

		function saveDialog(e) {
			var startNode = columnId;
			var startNodeUri = columnUri;

			if (selectedFromClass.label == "" && (linkType == "incoming" || linkType == "changeIncoming" || linkType == "changeLink")) {
				showError("Please select the from class");
				return false;
			}

			if (selectedToClass.label == "" && (linkType == "outgoing" || linkType == "changeOutgoing" || linkType == "changeLink")) {
				showError("Please select the to class");
				return false;
			}
			if (selectedProperty.label == "") {
				showError("Please select the property");
				return false;
			}

			var info = generateInfoObject(worksheetId, "", "ChangeInternalNodeLinksCommand");

			// Prepare the input for command
			var newInfo = info['newInfo'];

			// Put the old edge information
			var initialEdges = [];
			if (linkType == "changeIncoming" || linkType == "changeOutgoing" || linkType == "changeLink") {
				var oldEdgeObj = {};
				oldEdgeObj["edgeSourceId"] = changeFromNode;
				oldEdgeObj["edgeTargetId"] = changeToNode;
				oldEdgeObj["edgeId"] = changeLink;
				initialEdges.push(oldEdgeObj);
			}

			newInfo.push(getParamObject("initialEdges", initialEdges, "other"));

			newInfo.push(getParamObject("alignmentId", alignmentId, "other"));

			// Put the new edge information
			var newEdges = [];
			var newEdgeObj = {};

			var source, target, sourceUri, targetUri;
			var property = selectedProperty.id;

			if (linkType == "incoming" || linkType == "changeIncoming") {
				target = startNode;
				targetUri = startNodeUri;
				source = selectedFromClass.id;
				sourceUri = selectedFromClass.uri;
			} else if (linkType == "outgoing" || linkType == "changeOutgoing") {
				source = startNode;
				sourceUri = startNodeUri;
				target = selectedToClass.id;
				targetUri = selectedToClass.uri;
			} else if (linkType == "changeLink") {
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
			var returned = sendRequest(info, worksheetId);
			hide();
		};

		function hide() {
			dialog.modal('hide');
		}

		function show(wsId, colId, alignId,
			colLabel, colRdfsLabel, colRdfsComment, colUri, colDomain, colType, isColUri,
			type, changeFrom, changeTo, changeLinkUri) {
			worksheetId = wsId;
			columnId = colId;
			alignmentId = alignId;

			columnLabel = colLabel;
			columnRdfsLabel = colRdfsLabel;
			columnRdfsComment = colRdfsComment;
			columnUri = colUri;
			columnDomain = colDomain;
			columnType = colType;
			isColumnUri = isColUri;
			
			linkType = type;
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});

			if (type == "changeIncoming" || type == "changeOutgoing" || type == "changeLink") {
				changeFromNode = changeFrom;
				changeToNode = changeTo;
				changeLink = changeLinkUri;
			}
		};


		function showBlank(wsId, colId, alignId,
			colLabel, colRdfsLabel, colRdfsComment, colUri, colDomain, colType, isColUri, type) {
			selectedFromClass = {
				label: "",
				rdfsLabel: "",
				id: "",
				uri: ""
			};
			selectedToClass = {
				label: "",
				rdfsLabel: "",
				id: "",
				uri: ""
			};
			selectedProperty = {
				label: "",
				rdfsLabel: "",
				id: "",
				uri: ""
			};
			show(wsId, colId, alignId,
				colLabel, colRdfsLabel, colRdfsComment, colUri, colDomain, colType, isColUri, type);
		};


		return { //Return back the public methods
			show: show,
			showBlank: showBlank,
			init: init,
			setSelectedFromClass: setSelectedFromClass,
			setSelectedToClass: setSelectedToClass,
			setSelectedProperty: setSelectedProperty
		};
	};

	function getInstance() {
		if (!instance) {
			instance = new PrivateConstructor();
			instance.init();
		}
		return instance;
	}

	return {
		getInstance: getInstance
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
		var columnLabel, columnRdfsLabel, columnRdfsComment, columnUri, columnDomain, columnType, isColumnUri;
		var initialLinks;

		var classUI, propertyUI, editLink, classPropertyUIDiv;
		var loadTree = true;
		var allClasses = null,
			existingClasses = null,
			allProperties = null;

		function init() {

			//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				hideError();
				allClasses = null;
				existingClasses = null;
				allProperties = null;
				loadTree = true;

				$("#incomingLinksTable tr", dialog).remove();
				$("#outgoingLinksTable tr", dialog).remove();

				$("#columnName", dialog).text(columnLabel);

				initialLinks = getAllLinksForNode(worksheetId, alignmentId, columnId);
				$.each(initialLinks, function(index2, link) {
					addLink(link);
				});
			});


			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});

			$('#btnAddIncomingLink', dialog).on('click', function(e) {
				e.preventDefault();
				addIncomingLink(e);
			});

			$('#btnAddOutgoingLink', dialog).on('click', function(e) {
				e.preventDefault();
				addOutgoingLink(e);
			});
		}

		function addLink(link) {
			var table = (link.type == "incoming") ? $("#incomingLinksTable") : $("#outgoingLinksTable");
			var trTag = $("<tr>");
			table.append(trTag);
			var direction = (link.type == "incoming") ? "from" : "to";
			var classLabel = (link.type == "incoming") ? 
								Settings.getInstance().getDisplayLabel(link.source.label, link.source.rdfsLabel) : 
								Settings.getInstance().getDisplayLabel(link.target.label, link.target.rdfsLabel);
			var propertyLabel = Settings.getInstance().getDisplayLabel(link.property.label, link.property.rdfsLabel);

			trTag.data("link", $.extend(true, {}, link)) // deep copy)
			.append($("<td>").append(direction).css("width", "5%"))
				.append($("<td>").addClass("bold").append(classLabel).css("width", "40%"))
				.append($("<td>").append("via").css("width", "5%"))
				.append($("<td>").addClass("bold").append(propertyLabel).css("width", "40%"))
				.append($("<td>").css("width", "5%")
					.append($("<button>").attr("type", "button").addClass("btn").addClass("deleteButton").addClass("btn-default").text("Delete").click(deleteLink))
			)
				.append($("<td>").css("width", "5%")
					.append($("<button>").attr("type", "button").addClass("btn").addClass("editButton").addClass("btn-default").text("Edit").click(editLink))
					.append($("<button>").attr("type", "button").addClass("btn").addClass("hideButton").css("display", "none").addClass("btn-default").text("Hide").click(hideEditOptions))
			);

		}

		function setRowData(row, link) {
			var classLabel = (link.type == "incoming") ? 
								Settings.getInstance().getDisplayLabel(link.source.label, link.source.rdfsLabel) : 
								Settings.getInstance().getDisplayLabel(link.target.label, link.target.rdfsLabel);
			var propertyLabel = Settings.getInstance().getDisplayLabel(link.property.label, link.property.rdfsLabel);
			$("td:nth-child(2)", row).text(classLabel);
			$("td:nth-child(4)", row).text(propertyLabel);
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

			getAllClassNodes();
			getProperties();

			classUI = new ClassUI("showIncomingOutgoingLinksDialog_class", getExistingClassNodes, getAllClassNodes, 100, 
									loadTree, getAllClassNodes, MAX_NUM_SEMTYPE_SEARCH);
			propertyUI = new PropertyUI("showIncomingOutgoingLinksDialog_property", getPropertyForClass, getProperties, 100, 
									loadTree, getProperties, MAX_NUM_SEMTYPE_SEARCH);
			classUI.setHeadings("Classes in Model", "All Classes");
			propertyUI.setHeadings("Compatible Properties", "All Properties");
			classUI.onClassSelect(validateClassInputValue);
			propertyUI.onPropertySelect(validatePropertyInputValue);

			var clazz = (link.type == "incoming") ? link.source : link.target;
			classUI.setDefaultClass(clazz.label, clazz.rdfsLabel, clazz.id, clazz.uri)
			classUI.setSelectedProperty(link.property.label, link.property.rdfsLabel, link.property.id, link.property.uri);
			classUI.generateJS(classDiv, true);

			propertyUI.setDefaultProperty(link.property.label, link.property.rdfsLabel, link.property.id, link.property.uri);
			propertyUI.setSelectedClass(clazz.label, clazz.rdfsLabel, clazz.id, clazz.uri);
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
			if (loadTree && existingClasses == null) {
				var classes = getClassesInModel(worksheetId);
				var result = [];
				$.each(classes, function(index, clazz) {
					result.push(ClassUI.getNodeObject(clazz.label, clazz.rdfsLabel, clazz.id, clazz.uri));
				});
				existingClasses = result;
			}
			return existingClasses;
		}


		function getAllClassNodes() {
			if (allClasses == null) {
				var classes = getAllClasses(worksheetId);
				if (loadTree)
					loadTree = ($.workspaceGlobalInformation.UISettings.maxLoadedClasses == -1 ||
						classes.length <= $.workspaceGlobalInformation.UISettings.maxLoadedClasses) ? true : false;
				var result = [];
				$.each(classes, function(index, clazz) {
					result.push(ClassUI.getNodeObject(clazz.label, clazz.rdfsLabel, clazz.id, clazz.uri));
				});
				allClasses = result;
			}
			return allClasses;
		}

		function getProperties() {
			if (allProperties == null) {
				var props;
				if (columnType == "ColumnNode" || (columnType == "LiteralNode" && isColumnUri == false))
					props = getAllDataProperties(worksheetId);
				else
					props = getAllObjectProperties(worksheetId);
				var result = [];
				$.each(props, function(index, prop) {
					result.push(PropertyUI.getNodeObject(prop.label, prop.rdfsLabel, prop.id, prop.uri));
				});
				allProperties = result;
				if (loadTree)
					loadTree = ($.workspaceGlobalInformation.UISettings.maxLoadedProperties == -1 ||
						allProperties.length <= $.workspaceGlobalInformation.UISettings.maxLoadedProperties) ? true : false;
			}
			return allProperties;
		}

		function getPropertyForClass(selectedClass) {
			if (loadTree) {
				var domain, range;
				var startNodeClass = columnDomain;
				if (columnType == "ColumnNode" || columnType == "LiteralNode")
					startNodeClass = "";
				if (editLink.type == "incoming") {
					domain = selectedClass.uri;
					range = startNodeClass;
				} else { //if(linkType == "outgoing" || linkType == "changeOutgoing") {
					domain = startNodeClass;
					range = selectedClass.uri;
				}

				var props = getAllPropertiesForDomainRange(worksheetId, domain, range);
				var result = [];
				$.each(props, function(index, prop) {
					result.push(PropertyUI.getNodeObject(prop.label, prop.rdfsLabel, prop.id, prop.uri));
				});
				selectedProperties = result;
				return result;
			}
		}

		function getClassFromList(label) {
			var clazz = null;
			var inputVal = label.toLowerCase();
			var inputValWithAdd = inputVal + " (add)";
			for (var i = 0; i < allClasses.length; i++) {
				var thisClass = allClasses[i].metadata;
				if (thisClass.label.toLowerCase() == inputVal || thisClass.label.toLowerCase() == inputValWithAdd || thisClass.id.toLowerCase() == inputVal || thisClass.id.toLowerCase() == inputValWithAdd) {
					clazz = thisClass;
					break;
				}
			}
			return clazz;
		}

		function getPropertyFromList(label) {
			var prop = null;
			var inputVal = label.toLowerCase();
			var inputValWithAdd = inputVal + " (add)";
			for (var i = 0; i < allProperties.length; i++) {
				var thisProp = allProperties[i].metadata;
				if (thisProp.label.toLowerCase() == inputVal || thisProp.label.toLowerCase() == inputValWithAdd || thisProp.id.toLowerCase() == inputVal || thisProp.id.toLowerCase() == inputValWithAdd) {
					prop = thisProp;
					break;
				}
			}
			return prop;
		}

		function validatePropertyInputValue(propertyData) {
			if (loadTree) {
				classUI.refreshClassDataTop(propertyData.label, propertyData.id, propertyData.uri);
			} else {
				propertyData = getPropertyFromList(propertyData.label);
			}
			var rowToChange = $(classPropertyUIDiv).parents("tr.editRow").data("editRowObject");
			if (rowToChange != null) {
				var link = rowToChange.data("link");
				link.property = propertyData;
				setRowData(rowToChange, link);
			}
		}

		function validateClassInputValue(classData) {
			if (loadTree) {
				propertyUI.refreshPropertyDataTop(classData.label, classData.id, classData.uri);
			} else {
				classData = getClassFromList(classData.label);
			}
			var rowToChange = $(classPropertyUIDiv).parents("tr.editRow").data("editRowObject");
			if (rowToChange != null) {
				var link = rowToChange.data("link");
				if (link.type == "incoming")
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
			if (err) {
				$("div.error", dialog).text(err);
			}
			$("div.error", dialog).show();
		}

		function saveDialog(e) {
			var info = new generateInfoObject(worksheetId, "", "ChangeInternalNodeLinksCommand");

			// Prepare the input for command
			var newInfo = info['newInfo'];

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

			var newEdges = [];

			var tables = [
				$("#incomingLinksTable"),
				$("#outgoingLinksTable")
			];
			var newValidated = true;

			$.each(tables, function(idx, table) {
				$.each($("tr", table), function(index, row) {
					var newEdgeObj = {};
					var link = $(row).data("link");
					if (link) {
						if (link.source.id == "FakeId" || link.target.id == "FakeId") {
							alert("Please select a Class");
							newValidated = false;
							return false;
						}
						if (link.property.id == "FakeId") {
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

			if (!newValidated)
				return;

			newInfo.push(getParamObject("newEdges", newEdges, "other"));
			info["newInfo"] = JSON.stringify(newInfo);
			info["newEdges"] = newEdges;

			showLoading(worksheetId);
			var returned = sendRequest(info, worksheetId);
			hide();
		};

		function addIncomingLink(e) {
			var source = {
				"id": "FakeId",
				"label": "Class",
				"rdfsLabel": "",
				"uri": "FakeURI"
			};
			var target = {
				"id": columnId,
				"label": columnLabel,
				"rdfsLabel": columnRdfsLabel,
				"uri": columnUri
			};
			var prop = {
				"id": "FakeId",
				"label": "Property",
				"rdfsLabel": "",
			};
			var link = {
				"type": "incoming",
				"source": source,
				"target": target,
				"property": prop
			};
			addLink(link);
		}

		function addOutgoingLink(e) {
			var source = {
				"id": columnId,
				"label": columnLabel,
				"rdfsLabel": columnRdfsLabel,
				"uri": columnUri
			};
			var target = {
				"id": "FakeId",
				"label": "Class",
				"rdfsLabel": "",
				"uri": "FakeURI"
			};
			var prop = {
				"id": "FakeId",
				"label": "Property",
				"rdfsLabel": ""
			};
			var link = {
				"type": "outgoing",
				"source": source,
				"target": target,
				"property": prop
			};
			addLink(link);
		}

		function hide() {
			dialog.modal('hide');
		}


		function show(wsId, colId, alignId,
			colLabel, colRdfsLabel, colRdfsComment, colUri, colDomain, colType, isColUri) {
			worksheetId = wsId;
			columnId = colId;
			alignmentId = alignId;

			columnLabel = colLabel;
			columnRdfsLabel = colRdfsLabel;
			columnRdfsComment = colRdfsComment;
			columnUri = colUri;
			columnDomain = colDomain;
			columnType = colType;
			isColumnUri = isColUri;
			
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
		};

		return { //Return back the public methods
			show: show,
			init: init
		};
	};

	function getInstance() {
		if (!instance) {
			instance = new PrivateConstructor();
			instance.init();
		}
		return instance;
	}

	return {
		getInstance: getInstance
	};

})();

var AugmentDataDialog = (function() {
	var instance = null;
	var available;
	var filtered;

	function PrivateConstructor() {
		var dialog = $("#augmentDataDialog");
		var worksheetId, columnUri, alignmentId, columnDomain, table;
		var invertedClasses = {};

		function initVariable(wsId, colDomain, colUri, Alnid) {
			worksheetId = wsId;
			columnUri = colUri;
			alignmentId = Alnid;
			columnDomain = colDomain;

		}

		function refresh() {
			var info = generateInfoObject(worksheetId, "", "SearchForDataToAugmentCommand");
			info['tripleStoreUrl'] = $('#txtModel_URL').html();
			info['context'] = "";
			info["nodeUri"] = columnDomain;
			info["columnUri"] = columnUri;
			var returnJSON = [];
			var incoming, outgoing;
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				async: false,
				complete: function(xhr, textStatus) {
					//alert(xhr.responseText);
					var json = $.parseJSON(xhr.responseText);
					json = json.elements[0];
					console.log(json);
					outgoing = json;
					//hideLoading(info["worksheetId"]);
				},
				error: function(xhr, textStatus) {
					alert("Error occured while Searching Models!" + textStatus);
					//hideLoading(info["worksheetId"]);
				}
			});
			var info = generateInfoObject(worksheetId, "", "SearchForDataToAugmentIncomingCommand");
			info['tripleStoreUrl'] = $('#txtModel_URL').html();
			info['context'] = "";
			info["nodeUri"] = columnDomain;
			info["columnUri"] = columnUri;
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				async: false,
				complete: function(xhr, textStatus) {
					//alert(xhr.responseText);
					var json = $.parseJSON(xhr.responseText);
					json = json.elements[0];
					console.log(json);
					incoming = json;

					//hideLoading(info["worksheetId"]);
				},
				error: function(xhr, textStatus) {
					alert("Error occured while Searching Models!" + textStatus);
					//hideLoading(info["worksheetId"]);
				}
			});
			var i = 0,
				j = 0;
			while (i < outgoing.length && j < incoming.length) {
				if (outgoing[i]['estimate'] * 1 < incoming[j]['estimate'] * 1) {
					returnJSON.push(incoming[j]);
					j += 1;
				} else {
					returnJSON.push(outgoing[i]);
					i += 1;
				}
			}
			for (var k = i; k < outgoing.length; k++)
				returnJSON.push(outgoing[k]);
			for (var k = j; k < incoming.length; k++)
				returnJSON.push(incoming[k]);
			console.log(returnJSON);
			return returnJSON;
		};

		function getHeaderRow() {
			var tr = $("<tr>");
			var th = $("<th>"); //.addClass("CheckboxProperty");
			tr.append(th);

			var th = $("<th>"); //.addClass("FileNameProperty");
			var label = $("<label>").text("Property"); //.addClass("FileNameProperty");
			th.append(label);
			var label = $("<input>").text("")
				.addClass("form-control")
				.addClass("modelSearchControl")
				.attr("id", "txtFilterPredicate")
				.attr("type", "text")
				.on('keyup', applyFilter);
			th.append(label);
			tr.append(th);

			var th = $("<th>"); //.addClass("PublishTimeProperty");
			var label = $("<label>").text("Class"); //.addClass("PublishTimeProperty");
			th.append(label);
			var label = $("<input>").text("")
				.addClass("form-control")
				.addClass("modelSearchControl")
				.attr("id", "txtFilterOtherClass")
				.attr("type", "text")
				.on('keyup', applyFilter);
			th.append(label);
			tr.append(th);

			var th = $("<th>"); //.addClass("URLProperty");
			var label = $("<label>").text("# Matches (approx)"); //.addClass("URLProperty");
			th.append(label);
			var label = $("<input>").text("")
				.addClass("form-control")
				.addClass("modelSearchControl")
				.attr("id", "txtFilterDataCount")
				.attr("type", "text")
				.on('keyup', applyFilter);
			th.append(label);
			tr.append(th);

			var th = $("<th>"); //.addClass("URLProperty");
			var label = $("<label>").text("Direction"); //.addClass("URLProperty");
			th.append(label);
			var searchBtn = $("<i>").addClass("glyphicon")
				.addClass("glyphicon-search")
				.css("float", "right")
				.css("cursor", "pointer")
				.on("click", toggleSearchControls);
			th.append(searchBtn);
			var label = $("<input>").text("")
				.addClass("form-control")
				.addClass("modelSearchControl")
				.attr("id", "txtFilterIncoming")
				.attr("type", "text")
				.on('keyup', applyFilter);
			th.append(label);
			tr.append(th);
			return tr;
		}

		function toggleSearchControls() {
			$(".modelSearchControl").each(function() {
				if ($(this).is(":visible")) {
					$(this).hide();
				} else {
					$(this).show();
				}
			});
		}

		function hideSearchControls() {
			$(".modelSearchControl").each(function() {
				$(this).hide();
			});
		}

		function init() {
			//Initialize what happens when we show the dialog
			var classes = getAllClassesRaw(worksheetId);
			var dataprops = getAllDataProperties(worksheetId);
			var objprops = getAllObjectProperties(worksheetId);
			$.each(classes, function(index, type) {
				invertedClasses[type['uri']] = type['label'];
			});
			$.each(dataprops, function(index, type) {
				invertedClasses[type['uri']] = type['label'];
			});
			$.each(objprops, function(index, type) {
				invertedClasses[type['uri']] = type['label'];
			});
			//console.log(invertedClasses);
			var dialogContent = $("#augmentDataDialogHeaders", dialog);
			table = $("<table>")
				.addClass("table table-striped table-condensed");
			var tr = getHeaderRow();
			table.append(tr);
			dialogContent.append(table);

			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});


		}

		function applyFilter(e) {
			console.log("applyFilter");
			var tmp = [];
			var filterPredicate = $('#txtFilterPredicate').val().toLowerCase();
			var filterOtherClass = $('#txtFilterOtherClass').val().toLowerCase();
			var filterEstimate = $('#txtFilterDataCount').val();
			var filterIncoming = $('#txtFilterIncoming').val().toLowerCase();

			for (var i = 0; i < available.length; i++) {
				var predicate = available[i]['predicate'];
				if (invertedClasses[predicate] != undefined)
					predicate = invertedClasses[predicate];
				else
					predicate = predicate.substring(predicate.lastIndexOf("/") + 1);
				predicate = predicate.toLowerCase();
				var estimate = available[i]['estimate'];
				var otherClass = available[i]['otherClass'];
				var incoming = available[i]['incoming'] === "true" ? "incoming" : "outgoing";
				if (invertedClasses[otherClass] != undefined)
					otherClass = invertedClasses[otherClass];
				else
					otherClass = otherClass.substring(otherClass.lastIndexOf("/") + 1);
				otherClass = otherClass.toLowerCase();
				var flag = true;
				if (predicate.indexOf(filterPredicate) == -1) {
					flag = false;
				} else if (otherClass.indexOf(filterOtherClass) == -1) {
					flag = false;
				} else if (estimate < filterEstimate) {
					flag = false;
				} else if (incoming.indexOf(filterIncoming) == -1) {
					flag = false;
				}
				if (flag)
					tmp.push(available[i]);
			}
			filtered = tmp;
			instance.showTable();
		};

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
			var predicatesOutgoing = [];
			var otherClassOutgoing = [];
			var predicatesIncoming = [];
			var otherClassIncoming = [];
			for (var i = 0; i < checkboxes.length; i++) {
				var checkbox = checkboxes[i];
				var json = $.parseJSON(checkbox['value']);
				var t1 = new Object();
				var t3 = new Object();
				t1['predicate'] = json['predicate'];
				t3['otherClass'] = json['otherClass'];
				if (json['incoming'] === "false") {
					predicatesOutgoing.push(t1);
					otherClassOutgoing.push(t3);
				} else {
					predicatesIncoming.push(t1);
					otherClassIncoming.push(t3);
				}
			}

			var info = generateInfoObject(worksheetId, "", "FetchHNodeIdFromAlignmentCommand");
			info["alignmentId"] = alignmentId;
			info["columnUri"] = columnUri;
			var hNodeId;
			var returned = $.ajax({
				url: "RequestController",
				type: "POST",
				data: info,
				dataType: "json",
				async: false,
				complete: function(xhr, textStatus) {
					//alert(xhr.responseText);
					var json = $.parseJSON(xhr.responseText);
					//json = $.parseJSON();
					hNodeId = json.elements[0]['HNodeId'];
					console.log(hNodeId);
					//applyModelDialog.getInstance().show(worksheetId, json);
				},
				error: function(xhr, textStatus) {
					alert("Error occured while fetching alignment!" + textStatus);
					//hideLoading(info["worksheetId"]);
				}
			});
			showLoading(info["worksheetId"]);

			var info = generateInfoObject(worksheetId, hNodeId, "AugmentDataDispachCommand");
			var newInfo = info['newInfo'];
			newInfo.push(getParamObject("predicateOutgoing", JSON.stringify(predicatesOutgoing), "other"));
			newInfo.push(getParamObject("otherClassOutgoing", JSON.stringify(otherClassOutgoing), "other"));
			newInfo.push(getParamObject("predicateIncoming", JSON.stringify(predicatesIncoming), "other"));
			newInfo.push(getParamObject("otherClassIncoming", JSON.stringify(otherClassIncoming), "other"));
			newInfo.push(getParamObject("columnUri", columnUri, "other"));
			newInfo.push(getParamObject("tripleStoreUrl", $('#txtData_URL').html(), "other"));
			newInfo.push(getParamObject("sameAsPredicate", $('#altPredicate').val(), "other"));
			info["newInfo"] = JSON.stringify(newInfo);
			var returned = sendRequest(info, worksheetId);
		};

		function hide() {
			dialog.modal('hide');
		}

		function showTable() {
			var header = $("#augmentHeader", dialog);
			var type = invertedClasses[columnDomain];
			if (type == undefined)
				type = columnUri.substring(columnUri.lastIndexOf("/") + 1);
			header.text("Augment data for " + type);
			table.find("tr:gt(0)").remove();;
			for (var i = 0; i < filtered.length; i++) {
				var predicate = filtered[i]['predicate'];
				var estimate = filtered[i]['estimate'];
				var otherClass = filtered[i]['otherClass'];
				var incoming = filtered[i]['incoming'] === "true" ? "Incoming" : "Outgoing";
				var tr = $("<tr>");
				var td = $("<td>");
				var json = new Object();
				json['predicate'] = predicate;
				json['otherClass'] = otherClass;
				json['incoming'] = filtered[i]['incoming'];
				var checkbox = $("<input>")
					.attr("type", "checkbox")
					.attr("id", "selectPredicates")
					.attr("value", JSON.stringify(json));
				td.append(checkbox);
				tr.append(td);
				var td = $("<td>");
				var name = invertedClasses[predicate] == undefined ? predicate.substring(predicate.lastIndexOf("/") + 1) : invertedClasses[predicate];
				var label = $("<span>")
					.text(name);
				td.append(label);
				tr.append(td);
				var td = $("<td>");
				name = invertedClasses[otherClass] == undefined ? otherClass.substring(otherClass.lastIndexOf("/") + 1) : invertedClasses[otherClass];
				name = name.replace(" (add)", "");
				var label = $("<span>")
					.text(name);
				td.append(label);
				tr.append(td);
				var td = $("<td>");
				var label = $("<span>")
					.text(estimate);
				td.append(label);
				tr.append(td);
				var td = $("<td>");
				var label = $("<span>")
					.text(incoming);
				td.append(label);
				tr.append(td);
				table.append(tr);
			}
		}

		function show() {
			if (available.length == 0) {
				alert("No data to augment!");
				return;
			}
			showTable();
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
			hideSearchControls();

		};


		return { //Return back the public methods
			show: show,
			init: init,
			initVariable: initVariable,
			refresh: refresh,
			showTable: showTable
		};
	};

	function getInstance(wsId, colDomain, colUri, Alnid) {
		console.log("instance");
		if (!instance) {
			instance = new PrivateConstructor();
			instance.initVariable(wsId, colDomain, colUri, Alnid);
			instance.init();
		}
		instance.initVariable(wsId, colDomain, colUri, Alnid);
		var json = instance.refresh();
		console.log(json);
		available = json;
		filtered = json;
		return instance;
	}

	return {
		getInstance: getInstance
	};

})();



/**
 * ==================================================================================================================
 *
 * 				Diloag to add a New Node
 *
 * ==================================================================================================================
 */
var AddNodeDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#addNodeDialog");
		var worksheetId;

		var classUI;
		var selectedClass;
		var allClasses;

		function init() {

			//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				hideError();
				allClasses = null;

				$(".main", dialog).empty();
				var classDiv = $("<div>");
				$(".main", dialog).append(classDiv);

				getAllClassNodes();
				var loadTree = ($.workspaceGlobalInformation.UISettings.maxLoadedClasses == -1 ||
					allClasses.length <= $.workspaceGlobalInformation.UISettings.maxLoadedClasses) ? true : false;
				classUI = new ClassUI("addNewNode_class", null, getAllClassNodes, 300, loadTree, getAllClassNodes, MAX_NUM_SEMTYPE_SEARCH);
				classUI.setHeadings("Classes in Model", "All Classes");
				classUI.onClassSelect(validateClassInputValue);
				classUI.generateJS(classDiv, true);
			});


			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});
		}

		function getAllClassNodes() {
			if (allClasses == null) {
				var classes = getAllClasses(worksheetId);
				var result = [];
				$.each(classes, function(index, clazz) {
					if (clazz.id) {
						if (!clazz.label.match(/ \(add\)$/))
							return;
					}
					result.push(ClassUI.getNodeObject(clazz.label, clazz.rdfsLabel, clazz.id, clazz.uri));

				});
				allClasses = result;
			}
			return allClasses;
		}


		function validateClassInputValue(classData) {
			selectedClass = classData;
		}

		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError(err) {
			if (err) {
				$("div.error", dialog).text(err);
			}
			$("div.error", dialog).show();
		}

		function saveDialog(e) {
			var info = generateInfoObject(worksheetId, "", "AddNodeCommand");
			var newInfo = info['newInfo'];
			var label = selectedClass.label;
			if (label.length > 6) {
				label = label.substring(0, label.length - 6);
			}
			newInfo.push(getParamObject("label", label, "other"));
			newInfo.push(getParamObject("uri", selectedClass.uri, "other"));
			newInfo.push(getParamObject("id", selectedClass.id, "other"));
			info["newInfo"] = JSON.stringify(newInfo);
			showLoading(worksheetId);

			var returned = sendRequest(info, worksheetId);
			hide();
		};



		function hide() {
			dialog.modal('hide');
		}


		function show(wsId) {
			worksheetId = wsId;
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
		};
		
		return { //Return back the public methods
			show: show,
			init: init
		};
	};

	function getInstance() {
		if (!instance) {
			instance = new PrivateConstructor();
			instance.init();
		}
		return instance;
	}

	return {
		getInstance: getInstance
	};

})();

/**
 * ==================================================================================================================
 * 
 * 				Diloag to add a New Literal Node
 * 
 * ==================================================================================================================
 */
var AddLiteralNodeDialog = (function() {
		var instance = null;

		function PrivateConstructor() {
			var dialog = $("#addLiteralNodeDialog");
			var worksheetId;
			var dialogMode;
			var nodeId, nodeUri;
			var propertyUI, propertyList, loadTree;
			
			function init() {
					dialogMode = "add";
					
					//Initialize what happens when we show the dialog
					dialog.on('show.bs.modal', function (e) {
							hideError();
							loadTree = true;
							
							$("#col-literal", dialog).removeClass();
							if(dialogMode == "add") {
								$(".modal-title", dialog).html("Add Literal Node");
								$("#btnSave", dialog).text("Add");
								$("#col-literal", dialog).addClass("col-sm-12");
								$("#col-property", dialog).hide();
								$("#isUriRow", dialog).show();
							} else if(dialogMode == "addWithProperty") {
								$(".modal-title", dialog).html("Add Literal Node and Link");
								$("#btnSave", dialog).text("Add");
								$("#col-literal", dialog).addClass("col-sm-6");
								$("#col-property", dialog).show();
								
								propertyUI = new PropertyUI("AddLiteralNodeProperty",  getAllProperties, null, 300, loadTree, getAllProperties, MAX_NUM_SEMTYPE_SEARCH);
								propertyUI.setHeadings("All Properties", null);
								var propDiv = $("<div>");
								$("#col-property", dialog).empty();
								$("#col-property", dialog).append(propDiv);
								propertyList = null;
								getAllProperties();
								propertyUI.generateJS(propDiv, true);
								
								var isProvDiv = $("<div>").addClass("checkbox").attr("id", "isProvenanceLiteralRow")
													.append($("<label>")
														.append($("<input>").attr("type", "checkbox").attr("id", "isProvenanceLiteral")
														)
														.append($("<span>").text("Specifies provenance"))
													);
								$("#col-property", dialog).append(isProvDiv);
								$("#isUriRow", dialog).show();
							} else {
								$(".modal-title", dialog).html("Edit Literal Node");
								$("#btnSave", dialog).text("Save");
								$("#col-literal", dialog).addClass("col-sm-12");
								$("#col-property", dialog).hide();
								$("#isUriRow", dialog).show();
							}
							$("#literalType").typeahead( 
									{source:LITERAL_TYPE_ARRAY, minLength:0, items:"all"});
							$("#literalLanguage").typeahead( 
									{source:LANGUAGE_ARRAY, minLength:0, items:"all"});
					});
					
					
					$('#btnSave', dialog).on('click', function (e) {
							e.preventDefault();
							saveDialog(e);
					});
			}

			function getAllProperties() {
				if(propertyList == null) {
					propertyList = getAllDataAndObjectProperties(worksheetId);
				
					if (loadTree)
						loadTree = ($.workspaceGlobalInformation.UISettings.maxLoadedProperties == -1 ||
								propertyList.length <= $.workspaceGlobalInformation.UISettings.maxLoadedProperties) ? true : false;
				}

				var result = [];
				$.each(propertyList, function(index, prop) {
					result.push(PropertyUI.getNodeObject(prop.label, prop.rdfsLabel, prop.id, prop.uri, prop.type));
				});
				
				return result;
			}
			
			function validateClassInputValue(classData) {
				selectedClass = classData;
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
				 var newInfo = [];
				 var literal = $("#literal", dialog).val();

				 if(literal.length == 0) {
				 	alert("Please enter the literal");
				 	e.preventDefault();
				 	return;
				 }
				 var isProvenance = false;
				 if(dialogMode == "addWithProperty") {
				 	isProvenance = $("#isProvenanceLiteral", dialog).is(":checked");
				 	var property = propertyUI.getSelectedProperty();
				 	if(property.uri == "") {
				 		alert("Please select a property");
				 		e.preventDefault();
				 		return;
				 	}
				 }
				 var literalType = $("#literalType", dialog).val();  
				 var language = $("#literalLanguage", dialog).val();  
				 var isUri = $("input#isUri").is(":checked");
				 newInfo.push(getParamObject("literalValue", literal, "other"));
				 newInfo.push(getParamObject("literalType", literalType, "other"));
				 newInfo.push(getParamObject("language", language, "other"));
				 newInfo.push(getParamObject("worksheetId", worksheetId, "worksheetId"));
				 
				 if(dialogMode == "edit")
					 newInfo.push(getParamObject("nodeId", nodeId, "other"));
				 else if(dialogMode == "addWithProperty" && isUri == false) {
					 var property = propertyUI.getSelectedProperty();
					 var type = property.other;
					 if(type == "objectProperty")
						 isUri = true;
					 else
						 isUri = false;
				 }
				 
				 newInfo.push(getParamObject("isUri", isUri, "other"));
				 
				 info["newInfo"] = JSON.stringify(newInfo);
				 info["command"] = "AddLiteralNodeCommand";
				 showLoading(worksheetId);
				 var returned = $.ajax({
						 url: "RequestController",
						 type: "POST",
						 data : info,
						 dataType : "json",
						 complete :
								 function (xhr, textStatus) {
										 var json = $.parseJSON(xhr.responseText);
										 
										 if(dialogMode == "addWithProperty") {
											 var property = propertyUI.getSelectedProperty();
											 var updates = json.elements[0];
											 var literalId, literalUri;
											 
											 $.each(json["elements"], function(i, update) {
												 if(update.updateType == "AddLiteralNodeUpdate") {
													literalId = update.hNodeId;
													literalUri = update.uri;
												 }
											 });
											 
											 addEdge(nodeId, nodeUri, property.id, literalId, literalUri, isProvenance);
										 } else {
											 parse(json);
											 hideLoading(worksheetId);
											 hide();
										 }
								 },
						 error :
								 function (xhr, textStatus) {
										 alert("Error occured while adding the node!");
										 hideLoading(worksheetId);
										 hide();
								 }
				 });
			};
			
			function addEdge(sourceId, sourceUri, propertyId, targetId, targetUri, isProvenance) {
				
				var info = generateInfoObject(worksheetId, "", "ChangeInternalNodeLinksCommand");

				// Prepare the input for command
				var newInfo = info['newInfo'];

				// Put the old edge information
				var initialEdges = [];
				newInfo.push(getParamObject("initialEdges", initialEdges, "other"));

				// Put the new edge information
				var newEdges = [];
				var newEdgeObj = {};

				newEdgeObj["edgeSourceId"] = sourceId;
				newEdgeObj["edgeSourceUri"] = sourceUri;
				newEdgeObj["edgeTargetId"] = targetId;
				newEdgeObj["edgeTargetUri"] = targetUri;
				newEdgeObj["edgeId"] = propertyId;
				newEdgeObj["isProvenance"] = isProvenance;
				newEdges.push(newEdgeObj);

				newInfo.push(getParamObject("newEdges", newEdges, "other"));
				info["newInfo"] = JSON.stringify(newInfo);
				info["newEdges"] = newEdges;
				
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
									 alert("Error adding the edge for the Literal Node");
									 hideLoading(worksheetId);
									 hide();
							 }
			 });
			}
			
			function hide() {
				dialog.modal('hide');
			}
			
		 
			function show(wsId) {
				worksheetId = wsId;
				dialogMode = "add";
				$("#literal", dialog).val("");
				$("#literalType", dialog).val("");
				$("#literalLanguage", dialog).val("");
				$("input#isUri", dialog).attr("checked", false);
				dialog.modal({keyboard:true, show:true, backdrop:'static'});
			};
			
			function showEdit(wsId, columnId) {
				worksheetId = wsId;
				nodeId = columnId;
				
				var info = new Object();
				 info["workspaceId"] = $.workspaceGlobalInformation.id;
				 info["worksheetId"] = worksheetId;
				 info["nodeId"] = nodeId;
				 
				 var value, type, isUri, language;
				 
				 info["command"] = "GetLiteralNodeCommand";
				 showLoading(worksheetId);
				 var returned = $.ajax({
						 url: "RequestController",
						 type: "POST",
						 data : info,
						 async: false,
						 dataType : "json",
						 complete :
								 function (xhr, textStatus) {
										 var json = $.parseJSON(xhr.responseText);
										 var update = json.elements[0];
										 if(update.updateType == "LiteralNodeUpdate") {
											 var node = update.node;
											 value = node.value;
											 type = node.type;
											 language = node.language;
											 isUri = node.isUri;
										 } else {
											 alert("Error getting information about the node");
										 }
										 hideLoading(worksheetId);
								 },
						 error :
								 function (xhr, textStatus) {
										 alert("Error occured while adding the node!");
										 hideLoading(worksheetId);
								 }
				 });
				 
				$("#literal", dialog).val(value);
				$("#literalType", dialog).val(type);
				$("#literalLanguage", dialog).val(language);
				$("input#isUri", dialog).attr("checked", isUri);
				dialogMode = "edit";
				
				dialog.modal({keyboard:true, show:true, backdrop:'static'});
			}
			
			function showWithProperty(wsId, columnId, columnUri) {
				worksheetId = wsId;
				nodeId = columnId;
				nodeUri = columnUri;
				
				$("#literal", dialog).val("");
				$("#literalType", dialog).val("");
				$("#literalLanguage", dialog).val("");
				$("input#isUri", dialog).attr("checked", false);
				dialogMode = "addWithProperty";
				
				dialog.modal({keyboard:true, show:true, backdrop:'static'});
			}
			
			return {    //Return back the public methods
					show : show,
					showEdit : showEdit,
					showWithProperty: showWithProperty,
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


var ExportJSONDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#exportJSONDialog");
		var worksheetId, columnId;
		var contextJSON;
		var contextURL;
		function init() {
			$('#useContextControl').hide();
			$('#useContext').attr("checked", false);
			$('#useContext').change(function (e){
				if (this.checked) {
					$('#useContextControl').show();
				}
				else {
					$('#useContextControl').hide();
				}
			});

			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});

			$('#contextupload').fileupload({
				url: "/",
				add: function(e, data) {
					console.log("add");
					loadContext(data.files);
				}
			});
		}

		function loadContext(filelist) {
			console.log("load preset");
			if(filelist.length > 0) {
				var file = filelist[0];
				if (file.size < 1024 * 1024 * 10) {
					var reader = new FileReader();
					reader.onload = function(e) {
						var json;
						try {
							contextJSON = e.target.result;
							console.log(contextJSON);
						} catch (err) {

						}
					}
					reader.readAsText(file);
				}
			}
		};

		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError() {
			$("div.error", dialog).show();
		}

		function saveDialog(e) {
			hide();
			var info = generateInfoObject(worksheetId, "", "ExportJSONCommand");
			var newInfo = info['newInfo'];
			newInfo.push(getParamObject("alignmentNodeId", columnId, "other"));
			var contextFromModel = "false";
			if ($('#useContext').is(":checked")) {
				if (!$('#useContextFromFile').is(":checked")) {
					contextJSON = "";
				}
				if (!$('#useContextFromURL').is(":checked")) {
					contextURL = "";
				}
				else
				{
					contextURL = $('#useContextFromURLText').val();
				}
				if ($('#useContextFromModel').is(":checked")) {
					contextFromModel = "true";
				}
			}
			else {
				contextJSON = "";
				contextURL = "";
			}
			newInfo.push(getParamObject("contextJSON", contextJSON, "other"));
			newInfo.push(getParamObject("contextFromModel", contextFromModel, "other"));
			newInfo.push(getParamObject("contextURL", contextURL, "other"));
			info["newInfo"] = JSON.stringify(newInfo);
			var returned = sendRequest(info, worksheetId);
		};

		function hide() {
			dialog.modal('hide');
		}

		function show(wsId, colId) {
			worksheetId = wsId;
			columnId = colId;
			contextJSON = "";
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
		};


		return { //Return back the public methods
			show: show,
			init: init,
		};
	};

	function getInstance() {
		console.log("instance");
		if (!instance) {
			instance = new PrivateConstructor();
			instance.init();
		}
		return instance;
	}

	return {
		getInstance: getInstance
	};

})();

