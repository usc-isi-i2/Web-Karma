var ClassDropdownMenu = (function() {

	var instance = null;


	function PrivateConstructor() {
		var menuId = "classDropdownMenu";
		var worksheetId, columnId;
		var columnUri, columnLabel, columnDomain, columnCategory, alignmentId;
		var nodeType, isUri; //LiteralNode or InternalNode
		
		var options = [

			//Title, function to call, needs file upload     
			{
				name: "Add Incoming Link",
				func: addIncomingLink
			}, {
				name: "Add Outgoing Link",
				func: addOutgoingLink
			}, {
	
				name: "Add Outgoing Literal",
				func: addOutgoingLiteral
			}, {
				name: "Manage Links",
				func: manageLinks
			}, {
				name: "Augment Data",
				func: searchData
			}, {
				name: "Edit",
				func: editNode,
				nodeType: "LiteralNode"
			}, {
				name: "Delete",
				func: deleteNode,
				category: "forcedAdded"
			}, {
				name: "divider"
			}, {
				name: "Export CSV",
				func: exportCSV
			}, {
				name: "Export JSON",
				func: exportJSON
			}, {
				name: "Export Avro",
				func: exportAvro
			}, {
				name: "Invoke Table Service",
				func: invokeMLService
			},


		];

		function init() {
			generateJS();
		}

		function hide() {
			$("#" + menuId).hide();
			$(document).off('click', hide);
			$(document).off('keydown', hideOnEsc);
		}

		function hideOnEsc(event) {
			if (event.keyCode === 27) { // ESC
				hide();
			}
		}

		function manageLinks() {
			console.log("showIncomingOutgoingLinks");
			ManageIncomingOutgoingLinksDialog.getInstance().show(worksheetId,
				columnId, alignmentId,
				columnLabel, columnUri, columnDomain, nodeType, isUri);
		}

		function addIncomingLink() {
			console.log("addIncomingLink");
			IncomingOutgoingLinksDialog.getInstance().showBlank(worksheetId,
				columnId, alignmentId,
				columnLabel, columnUri, columnDomain, nodeType, isUri,
				"incoming");
			
		};

		function searchData() {
			AugmentDataDialog.getInstance(worksheetId,
				columnDomain, columnUri, alignmentId).show();
		}

		function addOutgoingLink() {
			console.log("addOutgoingLink");
			IncomingOutgoingLinksDialog.getInstance().showBlank(worksheetId,
				columnId, alignmentId,
				columnLabel, columnUri, columnDomain, nodeType, isUri,
				"outgoing");
		}

		function addOutgoingLiteral() {
			console.log("addOutgoingLiteral");
			AddLiteralNodeDialog.getInstance().showWithProperty(worksheetId, columnId, columnDomain);
		}
		
		function deleteNode() {
			console.log("Delete Node");
			var info = generateInfoObject(worksheetId, "", "DeleteNodeCommand");
			var newInfo = info['newInfo'];
			var label = columnLabel;
			newInfo.push(getParamObject("label", label, "other"));
			newInfo.push(getParamObject("id", columnUri, "other"));
			info["newInfo"] = JSON.stringify(newInfo);
			showLoading(worksheetId);
			sendRequest(info, worksheetId);
			hide();
		}

		function editNode() {
			console.log("Edit Node");
			AddLiteralNodeDialog.getInstance().showEdit(worksheetId, columnId);
		}
		
		function exportCSV() {
			ExportCSVModelDialog.getInstance().show(worksheetId, alignmentId, columnId, "exportCSV");
		};

		function exportJSON() {
			console.log("exportJSON");
			// var info = generateInfoObject(worksheetId, "", "ExportJSONCommand");
			// var newInfo = info['newInfo'];
			// newInfo.push(getParamObject("alignmentNodeId", columnId, "other"));
			// info["newInfo"] = JSON.stringify(newInfo);

			// showLoading(worksheetId);
			// var returned = sendRequest(info, worksheetId);
			ExportJSONDialog.getInstance().show(worksheetId, columnId);
		}

		function exportAvro() {
			console.log("exportAvro");
			var info = generateInfoObject(worksheetId, "", "ExportAvroCommand");
			var newInfo = info['newInfo'];
			newInfo.push(getParamObject("alignmentNodeId", columnId, "other"));
			info["newInfo"] = JSON.stringify(newInfo);
			showLoading(worksheetId);
			var returned = sendRequest(info, worksheetId);
		}

		function invokeMLService() {
			ExportCSVModelDialog.getInstance().show(worksheetId, alignmentId, columnId, "invokeMLService");
		}

		function generateJS() {
			var ul = $("<ul>");
			ul.attr("role", "menu")
				.addClass("dropdown-menu")
				.css("display", "block")
				.css("position", "static")
				.css("margin-bottom", "5px");
			for (var i = 0; i < options.length; i++) {
				var option = options[i];
				var li = $("<li>");
				if (option.name == "divider") {
					li.addClass("divider");
				} else {
					var a = $("<a>")
						.attr("href", "#")
						.attr("tabindex", "-1")
						.text(option.name)
						.click(option.func);
					li.append(a);
					li.data("category", option.category);
					li.data("nodeType", option.nodeType);
				}
				ul.append(li);
			}

			var div = $("<div>")
				.attr("id", menuId)
				.addClass("dropdown")
				.addClass("clearfix")
				.addClass("contextMenu")
				.append(ul);

			var container = $("body div.container");
			container.append(div);
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
			
			//console.log("Click for opening Menu");
			$("#" + menuId).css({
				display: "block",
				position: "absolute",
				left: event.pageX,
				top: event.pageY
			});

			//if(columnCategory.length > 0) {
			$("li", $("#" + menuId)).each(function(index) {
				var show = true;
				
				var category = $(this).data("category");
				if (category) {
					if (category.length > 0) {
						var res = category.split(",");
						var catFound = false;
						for (var j = 0; j < res.length; j++) {
							var cat = res[j];
							if (cat == columnCategory) {
								catFound = true;
								break;
							}
						}
						if (!catFound)
							show = false;
					}
				}
				
				var selNodeType = $(this).data("nodeType");
				if(selNodeType) {
					if (selNodeType.length > 0) {
						var res = selNodeType.split(",");
						var found = false;
						for (var j = 0; j < res.length; j++) {
							var nt = res[j];
							if (nt == nodeType) {
								found = true;
								break;
							}
						}
						if (!found)
							show = false;
					}
				}
				
				if (show)
					$(this).show();
				else
					$(this).hide();
			});
			//}

			window.setTimeout(function() {
				$(document).on('click', hide);
				$(document).on('keydown', hideOnEsc);

			}, 100);
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