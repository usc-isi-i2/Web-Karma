var ClassSuggestDropdown = (function() {

	var instance = null;


	function PrivateConstructor() {
		var menuId = "classSuggestDropdown";
		var worksheetId, columnId;
		var columnUri, columnLabel, columnDomain, columnCategory, alignmentId;
		var nodeType, isUri; //LiteralNode or InternalNode
		var allClassCache;

		function init() {
			allClassCache = null;
			generateJS();
			window.setTimeout(function() {
				allClassCache = getAllClasses(worksheetId);
			}, 10);
		}

		function hide() {
			$("#" + menuId).hide();
			$(document).off('click', hide);
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

	
		function selectFromMenu(e) {
			target = $(e.target);
			label = target.text();
			

			console.log("Selected class:" + label);
			if(label == 'More...') {
				populateAll();
				e.stopPropagation();
			} else if(columnUri == "BlankNode" || columnCategory == "temporary") {
				var links = D3ModelManager.getInstance().getCurrentLinksToNode(worksheetId, columnId);
				$.each(links, function(index, link) {
					if(link.target.type == "ColumnNode") {
						//Set Semantic Type
						var type = {
							"uri": link.uri,
							"label": link.label,
							"source": {"uri": target.data('uri'), "id": target.data('id'), "label": target.text()}
						}
						setSemanticType(worksheetId, link.target.id, type);
					} else {
						//Change Links Command
						var newEdges = [];
						var edge = {
							"uri": link.uri,
							"label": link.label,
							"target": link.target,
							"source": {"uri": target.data('uri'), "id": target.data('id'), "label": target.text()}
						}
						newEdges.push(edge);
						changeLinks(worksheetId, alignmentId, [], newEdges);
					}
				});

			} else {
				uri = target.data('uri');
				id = target.data('id');
				console.log("Change Node:" + id + ", " + uri);

				var info = generateInfoObject(worksheetId, "", "ChangeNodeCommand");
				var newInfo = info['newInfo'];
				newInfo.push(getParamObject("alignmentId", alignmentId, "other"));
				newInfo.push(getParamObject("oldNodeId", columnId, "other"));
				newInfo.push(getParamObject("newNodeUri", uri, "other"));
				newInfo.push(getParamObject("newNodeId", id, "other"));
				info["newInfo"] = JSON.stringify(newInfo);
				showLoading(worksheetId);
				var returned = sendRequest(info, worksheetId);
				hide();
			}
		}

		function populateAll() {
			if(allClassCache == null) {
				window.setTimeout(populateAll, 10);
				return;
			}

			var allTypes = [];
			
			$.each(allClassCache, function(index, type) {
				allTypes.push({"label": type["label"], "uri": type["uri"], "id": type["id"]});
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
		
			var inTypes = getClassesInModel(worksheetId);
			var items = [];
			if(inTypes != null) {
				$.each(inTypes, function(index, type) {
					items.push({"label": type["label"], "uri": type["uri"], "id": type["id"], "class": "propertyDropdown_compatible"});
				});	

			}
			
			if(items.length > 0) {
				items.push({"label": "divider", "uri": "divider"});
				items.push({"label": "More...", "uri": "More..."});
				renderMenu(items, true);
			} else {
				populateAll();
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
					
					var a = $("<a>")
						.attr("href", "#")
						.attr("tabindex", "-1")
						.text(label)
						.data('uri', uri)
						.data("id", item["id"])
						.click(selectFromMenu);
					li.append(a);
				}
				if(item["class"])
					li.addClass(item["class"]);
				ul.append(li);
			});

		}


		function show(p_worksheetId, p_columnId, p_columnLabel, p_columnUri, p_columnDomain, p_columnCategory, 
				p_alignmentId, p_nodeType, p_isUri,
				event) {
			worksheetId = p_worksheetId;
			columnLabel = p_columnLabel;
			columnId = p_columnId;
			columnUri = p_columnUri;
			columnDomain = p_columnDomain;
			columnCategory = p_columnCategory;
			alignmentId = p_alignmentId;
			nodeType = p_nodeType;
			isUri = p_isUri;

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