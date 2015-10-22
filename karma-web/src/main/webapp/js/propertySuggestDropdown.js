var PropertySuggestDropdown = (function() {

	var instance = null;


	function PrivateConstructor() {
		var menuId = "propertySuggestDropdown";
		var worksheetId;
		var alignmentId;
		var propertyId;
		var propertyUri;
		var sourceNodeId, sourceLabel, sourceDomain, sourceId, sourceNodeType, sourceIsUri;
		var targetNodeId, targetLabel, targetDomain, targetId, targetNodeType, targetIsUri;
		var allPropertiesCache;
		var semanticSuggestions;

		function init() {
			allPropertiesCache = null;
			generateJS();
			window.setTimeout(function() {
				allPropertiesCache = getAllDataAndObjectProperties(worksheetId);
			}, 10);
		}

		function hide() {
			$("#" + menuId).hide();
			$(document).off('click', hide);
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

		function generateJS() {
			var ul = $("<ul>");
			ul.attr("role", "menu")
				.addClass("dropdown-menu")
				.css("display", "block")
				.css("position", "static")
				.css("margin-bottom", "5px");
		
			var box = $("<div>").addClass("input-group").addClass("dropdownInput");
			var search = $("<span>").addClass("input-group-addon").append($("<span>").addClass("glyphicon").addClass("glyphicon-search"));
			var input = $("<input>").attr("type", "text").addClass("form-control").attr("id", "input_" + menuId).addClass("typeahead").attr("autocomplete", "off").val('');
			box.append(search).append(input);

			var div = $("<div>")
				.attr("id", menuId)
				.addClass("dropdown")
				.addClass("clearfix")
				.addClass("contextMenu")
				.append(ul);

			var li = $("<li>");
			li.append(box);
			ul.append(li);

			var container = $("body div.container");
			container.append(div);

			$('.dropdownInput input').click(function(e){e.stopPropagation; return false;}); 
			$('.dropdownInput input')
			        .on('keyup', filterDropdown);
		}

	
		function selectPropertyFromMenu(e) {
			target = $(e.target);
			label = target.text();
			

			console.log("Selected property:" + label);
			if(label == 'More...') {
				populateAllProperties();
				e.stopPropagation();
			} else {
				uri = target.data('uri');
				if(targetNodeType == "ColumnNode") {
					changeSemanticType(label, uri);
				} else {
					changeLink(label, uri);	
				}
			}
		}

		function populateAllProperties() {
			if(allPropertiesCache == null) {
				window.setTimeout(populateAllProperties, 10);
				return;
			}

			var allTypes = [];
			var uriFound = false;
			$.each(semanticSuggestions, function(index, type) {
				allTypes.push({"label": type["label"], "uri": type["uri"], "class": type["class"]});
				if(type["uri"] == "http://isi.edu/integration/karma/dev#classLink")
					uriFound = true;
			});

			if(allTypes.length > 0) 
				allTypes.push({"label": "divider", "uri": "divider"});

			if(targetNodeType == "ColumnNode" && uriFound == false) {
				allTypes.push({"label": "uri of " + sourceLabel, "uri": "http://isi.edu/integration/karma/dev#classLink"});
				allTypes.push({"label": "divider", "uri": "divider"});
			}

			$.each(allPropertiesCache, function(index, type) {
				allTypes.push({"label": type["label"], "uri": type["uri"]});
			});

			renderMenu(allTypes, true);
		}

		function filterDropdown(e) {
			query = $("#input_" + menuId).val();
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
		          this.select();
		          break;

		        case 27: // escape
		          this.hide();
		          break;
		        default:
		          	items = displayMenuItems;
		          	items = $.grep(items, function (item) {
			        	return (item["label"].toLowerCase().indexOf(query.toLowerCase()) != -1);
			      	});
			      	renderMenu(items, false);
		      }
		}

		function populateMenu() {
			semanticSuggestions = []
			if(targetNodeType == "ColumnNode") {
				var semSuggestions = getSuggestedSemanticTypes(worksheetId, targetId, sourceDomain);
				var items = [];
				var uriFound = false;
				if(semSuggestions != null) {
					$.each(semSuggestions["Labels"], function(index, type) {
						if(type["DisplayLabel"] == "km-dev:columnSubClassOfLink" ||
								type["DisplayLabel"] == "km-dev:dataPropertyOfColumnLink" ||
								type["DisplayLabel"] == "km-dev:objectPropertySpecialization") {
							return;
						}
						if(type["DisplayLabel"] == "uri" || type["DisplayLabel"] == "km-dev:classLink") {
							uriFound = true;
							type["DisplayLabel"] = "uri of " + sourceLabel;
						}
						items.push({"label": type["DisplayLabel"], "uri": type["FullType"], "class": "propertyDropdown_suggestion"});
					});
				}

				semanticSuggestions = semanticSuggestions.concat(items);

				if(!uriFound) {
					if(items.length > 0)
						items.push({"label": "divider", "uri": "divider"});
					items.push({"label": "uri of " + sourceLabel, "uri": "http://isi.edu/integration/karma/dev#classLink"});
				}

				var compatibleTypes = getAllPropertiesForClass(worksheetId, sourceDomain);
			
				if(compatibleTypes.length > 0 && items.length > 0)
					items.push({"label": "divider", "uri": "divider"});
				$.each(compatibleTypes, function(index, type) {
					items.push({"label": type["label"], "uri": type["uri"], "class": "propertyDropdown_compatible"});
				});	
				
				if(items.length > 0) {
					items.push({"label": "divider", "uri": "divider"});
					items.push({"label": "More...", "uri": "More..."});
					renderMenu(items, true);
				} else {
					populateAllProperties();
				}
			} else {
				var compatibleTypes = getAllPropertiesForDomainRange(worksheetId, sourceDomain, targetDomain);
				var items = [];
				if(compatibleTypes != null) {
					$.each(compatibleTypes, function(index, type) {
						items.push({"label": type["label"], "uri": type["uri"], "class": "propertyDropdown_compatible"});
					});	

				}
				
				if(items.length > 0) {
					items.push({"label": "divider", "uri": "divider"});
					items.push({"label": "More...", "uri": "More..."});
					renderMenu(items, true);
				} else {
					populateAllProperties();
				}
			}
		}

		function renderMenu(menuItems, storeSet) {
			var ul = $("ul", $("#" + menuId));
			ul.find("li:gt(0)").remove();
			ul.scrollTop(1);

			if(storeSet)
				displayMenuItems = menuItems;

			$.each(menuItems, function(index, item) {
				var label = item["label"];
				var uri = item["uri"];

				var li = $("<li>");
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


		function show(p_worksheetId, p_alignmentId, p_propertyId, p_propertyUri,
			p_sourceNodeId, p_sourceNodeType, p_sourceLabel, p_sourceDomain, p_sourceId, p_sourceIsUri,
			p_targetNodeId, p_targetNodeType, p_targetLabel, p_targetDomain, p_targetId, p_targetIsUri,
			event) {
			worksheetId = p_worksheetId;
			alignmentId = p_alignmentId;
			propertyId = p_propertyId;
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

			$("#input_" + menuId).val('');
			populateMenu();

			window.setTimeout(function() {
				var ul = $("ul", $("#" + menuId));
				ul.scrollTop(1);
			}, 10);
			//console.log("Click for opening Menu");
			$("#" + menuId).css({
				display: "block",
				position: "absolute",
				left: event.pageX,
				top: event.pageY
			});

			window.setTimeout(function(e) {
				$(document).on('click', hide);
			}, 10);
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