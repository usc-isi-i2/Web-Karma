var ClassTabs = (function() {

	var instance = null;


	function PrivateConstructor() {
		var dialog = $("#classTabs");

		var worksheetId, columnId;
		var columnUri, columnLabel, columnRdfsLabel, columnRdfsComment, alignmentId;
		var nodeType; //LiteralNode or InternalNode
		var allClassCache;
		var onSelectCallback;

		function init() {
			reloadAllCache();
			$('input', dialog).on('keyup', filterDropdown);

			$('#class_tabs a[href="#class_all"]').on('shown.bs.tab', function(e) {
				window.setTimeout(function() {
					$('input', dialog).select();
				}, 10);
				
				console.log("All clicked");
			});
		}

		function reloadAllCache() {
			allClassCache = null;
			window.setTimeout(function() {
				allClassCache = [];
				allClassCache[worksheetId] = getAllClasses(worksheetId);
			}, 10);
		}

		function reloadCache(worksheetId) {
			allClassCache[worksheetId] = null;
			window.setTimeout(function() {
				allClassCache[worksheetId] = getAllClasses(worksheetId);
			}, 10);
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

		function selectFromMenu(e) {
			target = $(e.target);
			if(!target.is("a"))
				target = target.parent();

			label = target.data('label');
			uri = target.data('uri');
			id = target.data('id');
			rdfsLabel = target.data('rdfsLabel');
			console.log("Selected property:" + label);
			onSelectCallback({"label":label, "rdfsLabel":rdfsLabel, "uri":uri, "id": id});
		}

		function populateAll() {
			if(allClassCache == null) {
				window.setTimeout(populateAll, 10);
				return;
			}
			if(allClassCache[worksheetId] == null ) {
				window.setTimeout(populateAll, 10);
				return;
			}

			var allTypes = [];
			
			$.each(allClassCache[worksheetId], function(index, type) {
				allTypes.push({"label": type["label"], "rdfsLabel":type["rdfsLabel"], "uri": type["uri"], "id": type["id"]});
			});

			renderMenu($("#class_all", dialog), allTypes);
			return allTypes.length;
		}

		function populateRecommended() {
			var inTypes = getClassesInModel(worksheetId);
			var items = [];
			if(inTypes != null) {
				$.each(inTypes, function(index, type) {
					items.push({"label": type["label"], "rdfsLabel":type["rdfsLabel"], "uri": type["uri"], "id": type["id"], "class": "propertyDropdown_compatible"});
				});	
			}
			renderMenu($("#class_recommended", dialog), items);
			return inTypes.length;
		}

		function populateCompatible() {
			//var inTypes = getClassesInModel(worksheetId);
			var items = [];
			// if(inTypes != null) {
			// 	$.each(inTypes, function(index, type) {
			// 		items.push({"label": type["label"], "uri": type["uri"], "id": type["id"], "class": "propertyDropdown_compatible"});
			// 	});	
			// }
			renderMenu($("#class_compatible", dialog), items);
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
		          // this.select();
		          break;

		        case 27: // escape
		          hide();
		          break;
		        default:
		          	items = allClassCache[worksheetId];
		          	items = $.grep(items, function (item) {
			        	return (Settings.getInstance().getDisplayLabel(item["label"], item["rdfsLabel"], true).toLowerCase().indexOf(query.toLowerCase()) != -1);
			      	});
			      	renderMenu($("#class_all", dialog), items);
		      }
		}

		function populateMenu() {
			var numRecom = populateRecommended();
			var numCompatible = populateCompatible();
			populateAll();

			if(numRecom != 0) {
				$('#class_tabs a[href="#class_recommended"]').tab('show');
			} else if(numCompatible  != 0) {
				$('#class_tabs a[href="#class_compatible"]').tab('show');
			} else {
				$('#class_tabs a[href="#class_all"]').tab('show');
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

				var li = $("<li>").addClass("col-xs-4").addClass("dialog-list-tab")
				if(label == "divider") {
					li.addClass("divider");
					
				} else {
					
					var a = $("<a>")
						.attr("href", "#")
						.attr("tabindex", "-1")
						.html(Settings.getInstance().getDisplayLabel(label, rdfsLabel))
						.data('uri', uri)
						.data("id", item["id"])
						.data("rdfsLabel", rdfsLabel)
						.data("label", label)
						.click(selectFromMenu);
					li.append(a);
				}
				if(item["class"])
					li.addClass(item["class"]);
				ul.append(li);
			});

		}


		function show(p_worksheetId, p_columnId, 
				p_columnLabel, p_columnRdfsLabel, p_columnRdfsComment,
				p_columnUri, 
				p_alignmentId, p_nodeType, div, callback,
				event) {
			worksheetId = p_worksheetId;
			columnLabel = p_columnLabel;
			columnRdfsLabel = p_columnRdfsComment;
			columnRdfsComment = p_columnRdfsComment;
			columnId = p_columnId;
			columnUri = p_columnUri;
			alignmentId = p_alignmentId;
			nodeType = p_nodeType;
			onSelectCallback = callback;

			$("input", dialog).val('');
			populateMenu();

			div.append(dialog)
			dialog.show();
		};


		return { //Return back the public methods
			show: show,
			init: init,
			reloadCache: reloadCache,
			reloadAllCache: reloadAllCache
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