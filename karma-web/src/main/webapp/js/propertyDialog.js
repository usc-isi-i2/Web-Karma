var PropertyDialog = (function() {

	var instance = null;


	function PrivateConstructor() {
		var dialog = $("#propertyDialog");

		var worksheetId;
		var alignmentId;
		var propertyId;
		var propertyUri;
		var sourceNodeId, sourceLabel, sourceDomain, sourceId, sourceNodeType, sourceIsUri;
		var targetNodeId, targetLabel, targetDomain, targetId, targetNodeType, targetIsUri;
		var allPropertiesCache;
		var defaultProperty;

		function init() {
			reloadCache();
			$('input', dialog).on('keyup', filterDropdown);

			$('#property_tabs a[href="#property_all"]').on('shown.bs.tab', function(e) {
				window.setTimeout(function() {
					$('input', dialog).select();
				}, 10);
				
				console.log("All clicked");
			});
		}

		function reloadCache() {
			allPropertiesCache = null;
			defaultProperty = null;
			window.setTimeout(function() {
				allPropertiesCache = getAllDataAndObjectProperties(worksheetId);
				for(var i=0; i<allPropertiesCache.length; i++) {
					type = allPropertiesCache[i];
					if(type.uri == DEFAULT_PROPERTY_URI) {
						defaultProperty  = type;
						break;
					}
				}
				if(defaultProperty == null) {
					defaultProperty = {
						"uri":"http://www.w3.org/2000/01/rdf-schema#label", 
						"label":"rdfs:label",
						"id": "http://www.w3.org/2000/01/rdf-schema#label", 
						"type": "dataProperty"						
					}
				}
			}, 10);
		}

		function getDefaultProperty() {
			return defaultProperty;
		}

		function hide() {
			dialog.modal('hide');
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

		function changeLink(label, uri) {
			oldEdges = [];
			var oldEdgeObj = {};
			oldEdgeObj["source"] = {"id": sourceId, "uri":sourceDomain, "label": sourceLabel};
			oldEdgeObj["target"] = {"id": targetId, "uri":targetDomain, "label": targetLabel};
			oldEdgeObj["uri"] = propertyUri;
			oldEdges.push(oldEdgeObj);

			// Put the new edge information
			var newEdges = [];
			var newEdgeObj = {};
			newEdgeObj["source"] = {"id": sourceId, "uri":sourceDomain, "label": sourceLabel};
			newEdgeObj["target"] = {"id": targetId, "uri":targetDomain, "label": targetLabel};
			newEdgeObj["uri"] = uri
			newEdges.push(newEdgeObj);

			changeLinks(worksheetId, alignmentId, oldEdges, newEdges);
			hide();
		}

		function changeSemanticType(label, uri) {
			var type = {
				"label": label,
				"uri": uri,
				"source": {"id": sourceId, "uri":sourceDomain, "label": sourceLabel}
			}
			setSemanticType(worksheetId, targetId, type);
			hide();
		}

		function selectPropertyFromMenu(e) {
			target = $(e.target);
			label = target.text();
			

			console.log("Selected property:" + label);
			if(label == 'More...') {
				populateAllProperties();
				e.stopPropagation();
				return;
			} else if(sourceDomain == "BlankNode") {
				uri = target.data('uri');
				D3ModelManager.getInstance().changeTemporaryLink(worksheetId, propertyId, uri, label);
				e.stopPropagation();
			} else {
				uri = target.data('uri');
				if(targetNodeType == "ColumnNode") {
					changeSemanticType(label, uri);
				} else {
					changeLink(label, uri);	
				}
			}
			hide();
		}

		function populateAllProperties() {
			if(allPropertiesCache == null) {
				window.setTimeout(populateAllProperties, 10);
				return;
			}

			var allTypes = [];
			var uriFound = false;
			
			if(allTypes.length > 0) 
				allTypes.push({"label": "divider", "uri": "divider"});

			if(targetNodeType == "ColumnNode" && uriFound == false) {
				uriLabel = "uri";
				if(sourceLabel != " ")
					uriLabel += " of " + sourceLabel;
				allTypes.push({"label": uriLabel, "uri": "http://isi.edu/integration/karma/dev#classLink"});
				// allTypes.push({"label": "divider", "uri": "divider"});
			}

			$.each(allPropertiesCache, function(index, type) {
				allTypes.push({"label": type["label"], "uri": type["uri"]});
			});

			renderMenu($("#property_all", dialog), allTypes);
			return allTypes.length;
		}

		function populateCompatibleProperties() {
			var compatibleTypes;
			if(targetNodeType == "ColumnNode") {
				compatibleTypes = getAllPropertiesForClass(worksheetId, sourceDomain);
			} else {
				compatibleTypes = getAllPropertiesForDomainRange(worksheetId, sourceDomain, targetDomain);
			}
			renderMenu($("#property_compatible", dialog), compatibleTypes);	
			return compatibleTypes.length;
		}

		function populateSuggestedProperties() {
			var items = [];

			if(targetNodeType == "ColumnNode") {
				var semSuggestions = getSuggestedSemanticTypes(worksheetId, targetId, sourceDomain);
				var uriFound = false;
				if(semSuggestions != null && semSuggestions["Labels"]) {
					$.each(semSuggestions["Labels"], function(index, type) {
						if(type["DisplayLabel"] == "km-dev:columnSubClassOfLink" ||
								type["DisplayLabel"] == "km-dev:dataPropertyOfColumnLink" ||
								type["DisplayLabel"] == "km-dev:objectPropertySpecialization") {
							return;
						}
						if(type["DisplayLabel"] == "uri" || type["DisplayLabel"] == "km-dev:classLink") {
							uriFound = true;
							type["DisplayLabel"] = uriLabel;
						}
						items.push({"label": type["DisplayLabel"], "uri": type["FullType"], "class": "propertyDropdown_suggestion"});
					});
				}
			} else {
				items = getRecommendedProperties(worksheetId, propertyId);
			}

			renderMenu($("#property_recommended", dialog), items);	
			return items.length;
		}

		function filterDropdown(e) {
			query = $("input", dialog).val();
			switch(e.keyCode) {
		        case 40: // down arrow
		        case 38: // up arrow
		        case 16: // shift
		        case 17: // ctrl
		        case 18: // alt
		          break;

		        case 9: // tab
		        case 13: // enter
		          if (!this.shown) return;
		          break;

		        case 27: // escape
		          hide();
		          break;
		        default:
		          	items = allPropertiesCache;
		          	items = $.grep(items, function (item) {
			        	return (item["label"].toLowerCase().indexOf(query.toLowerCase()) != -1);
			      	});
			      	renderMenu($("#property_all", dialog), items);
		      }
		}

		function populateMenu() {
			numRecom = populateSuggestedProperties();
			numCompatible = populateCompatibleProperties();
			populateAllProperties();

			if(numRecom != 0) {
				$('#property_tabs a[href="#property_recommended"]').tab('show');
			} else if(numCompatible  != 0) {
				$('#property_tabs a[href="#property_compatible"]').tab('show');
			} else {
				$('#property_tabs a[href="#property_all"]').tab('show');
			}
		}

		function renderMenu(div, menuItems) {
			var ul = $("ul", div);
			ul.empty();
			ul.scrollTop(1);

			$.each(menuItems, function(index, item) {
				var label = item["label"];
				var uri = item["uri"];

				var li = $("<li>").addClass("col-xs-4");
				if(label == "divider") {
					li.addClass("divider");
					
				} else {
					if (label == "km-dev:classLink") {
						var a = $("<a>")
							.attr("href", "#")
							.attr("tabindex", "-1")
							.text("uri")
							.click(selectPropertyFromMenu);
						li.append(a);
					} else {
						var a = $("<a>")
							.attr("href", "#")
							.attr("tabindex", "-1")
							.text(label)
							.data('uri', uri)
							.click(selectPropertyFromMenu);
						li.append(a);
					}
				}
				if(item["class"])
					li.addClass(item["class"]);
				ul.append(li);
			});

		}


		function show(p_worksheetId, p_alignmentId, p_propertyId, p_propertyUri, p_propertyLabel, p_propertyStatus,
			p_sourceNodeId, p_sourceNodeType, p_sourceLabel, p_sourceDomain, p_sourceId, p_sourceIsUri,
			p_targetNodeId, p_targetNodeType, p_targetLabel, p_targetDomain, p_targetId, p_targetIsUri,
			event) {
			worksheetId = p_worksheetId;
			alignmentId = p_alignmentId;
			propertyId = p_propertyId;
			linkStatus = p_propertyStatus;
			propertyUri = p_propertyUri;
			sourceNodeId = p_sourceNodeId;
			sourceLabel = p_sourceLabel;
			sourceDomain = p_sourceDomain;
			sourceId = p_sourceId;
			sourceIsUri = p_sourceIsUri;
			targetNodeId = p_targetNodeId;
			targetLabel = p_targetLabel;
			targetDomain = p_targetDomain;
			targetId = p_targetId;
			targetIsUri = p_targetIsUri;
			
			sourceNodeType = p_sourceNodeType;
			targetNodeType = p_targetNodeType;

			$("input", dialog).val('');
			populateMenu();

			$("#propertyDialog_title", dialog).html("Change Property: " + p_propertyLabel);
			if(linkStatus != "TemporaryLink") {
				$("#propertyDialogFunctions", dialog).show();
				PropertyFunctions.getInstance().show(p_worksheetId, p_alignmentId, p_propertyId, p_propertyUri, 
													p_sourceNodeId, p_sourceNodeType, p_sourceLabel, p_sourceDomain, p_sourceId, p_sourceIsUri,
													p_targetNodeId, p_targetNodeType, p_targetLabel, p_targetDomain, p_targetId, p_targetIsUri,
													hide, event);
				$("#propertyDialogSuggestions").removeClass("col-sm-12").addClass("col-sm-10");
			} else {
				$("#propertyDialogFunctions", dialog).hide();
				$("#propertyDialogSuggestions").removeClass("col-sm-10").addClass("col-sm-12");
			}

			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
		};


		return { //Return back the public methods
			show: show,
			init: init,
			getDefaultProperty: getDefaultProperty,
			reloadCache: reloadCache
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



