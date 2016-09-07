var PropertyTabs = (function() {

	var instance = null;


	function PrivateConstructor() {
		var dialog = $("#propertyTabs");

		var worksheetId;
		var alignmentId;
		var propertyId;
		var propertyUri;
		var sourceNodeId, sourceLabel, sourceRdfsLabel, sourceRdfsComment, sourceDomain, sourceId, sourceNodeType, sourceIsUri;
		var targetNodeId, targetLabel, targetRdfsLabel, targetRdfsComment, targetDomain, targetId, targetNodeType, targetIsUri;
		var allPropertiesCache;
		var defaultProperty;
		var onSelectCallback;

		function init() {
			reloadCache();
			setHandlers();
		}

		function setHandlers() {
			$('input', dialog).on('keyup', filterDropdown);
			$('#property_tabs a[href="#property_recommended"]').tab();
			$('#property_tabs a[href="#property_compatible"]').tab();
			$('#property_tabs a[href="#property_all"]').tab();
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
						"rdfsLabel": "label",
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
			dialog.hide();
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

		function selectPropertyFromMenu(e) {
			target = $(e.target);
			if(!target.is("a"))
				target = target.parent();

			label = target.data('label');
			uri = target.data('uri');
			id = target.data('id');
			rdfsLabel = target.data('rdfsLabel');
			console.log("Selected property:" + label);
			onSelectCallback({"label":label, "rdfsLabel":rdfsLabel, "uri":uri, "id": id}, e);
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
				allTypes.push({"label": type["label"], "rdfsLabel":type["rdfsLabel"], "uri": type["uri"]});
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
				uriLabel = "uri";
				if(sourceLabel != " ")
					uriLabel += " of " + sourceLabel;
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
						items.push({"label": type["DisplayLabel"], 
									"rdfsLabel": type["DisplayRDFSLabel"],
									"uri": type["FullType"], 
									"class": "propertyDropdown_suggestion"});
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
			        	return (Settings.getInstance().getDisplayLabel(item["label"], item["rdfsLabel"], true).toLowerCase().indexOf(query.toLowerCase()) != -1);
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
				var rdfsLabel = item["rdfsLabel"];

				var li = $("<li>").addClass("col-xs-4").addClass("dialog-list-tab");
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
							.html(Settings.getInstance().getDisplayLabel(label, rdfsLabel))
							.data('uri', uri)
							.data('label', label)
							.data('rdfsLabel', rdfsLabel)
							.click(selectPropertyFromMenu);
						li.append(a);
					}
				}
				if(item["class"])
					li.addClass(item["class"]);
				ul.append(li);
			});

		}


		function show(p_worksheetId, p_alignmentId, p_propertyId, p_propertyUri, 
			p_sourceNodeId, p_sourceNodeType, p_sourceLabel, p_sourceRdfsLabel, p_sourceRdfsComment, p_sourceDomain, p_sourceId, p_sourceIsUri,
			p_targetNodeId, p_targetNodeType, p_targetLabel, p_targetRdfsLabel, p_targetRdfsComment, p_targetDomain, p_targetId, p_targetIsUri, div,
			callback, event) {
			worksheetId = p_worksheetId;
			alignmentId = p_alignmentId;
			propertyId = p_propertyId;
			propertyUri = p_propertyUri;
			sourceNodeId = p_sourceNodeId;
			sourceLabel = p_sourceLabel;
			sourceRdfsLabel = p_sourceRdfsLabel;
			sourceRdfsComment = p_sourceRdfsComment;
			sourceDomain = p_sourceDomain;
			sourceId = p_sourceId;
			sourceIsUri = p_sourceIsUri;
			targetNodeId = p_targetNodeId;
			targetLabel = p_targetLabel;
			targetRdfsLabel = p_targetRdfsLabel;
			targetRdfsComment = p_targetRdfsComment;
			targetDomain = p_targetDomain;
			targetId = p_targetId;
			targetIsUri = p_targetIsUri;
			
			sourceNodeType = p_sourceNodeType;
			targetNodeType = p_targetNodeType;
			onSelectCallback = callback;

			$("input", dialog).val('');

			populateMenu();

			div.append(dialog)
			dialog.show();
		};


		return { //Return back the public methods
			show: show,
			init: init,
			hide: hide,
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



