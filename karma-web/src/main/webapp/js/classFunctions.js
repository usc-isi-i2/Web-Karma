var ClassFunctions = (function() {

	var instance = null;


	function PrivateConstructor() {
		var menuId = "classFunctionsMenu";
		var parentId = "classDialog";
		var hideFunction;

		var worksheetId, columnId;
		var columnUri, columnLabel, columnDomain, columnCategory, alignmentId;
		var nodeType, isUri; //LiteralNode or InternalNode
		
		var options = [

			//Title, function to call, needs file upload     
			{
				name: "Duplicate Node",
				func: duplicateNode,
				nodeType: "InternalNode"
			},
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
			// }, {
			// 	name: "divider"
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
			if(hideFunction)
				hideFunction();
		}

		function manageLinks() {
			hide();
			console.log("showIncomingOutgoingLinks");
			ManageIncomingOutgoingLinksDialog.getInstance().show(worksheetId,
				columnId, alignmentId,
				columnLabel, columnUri, columnDomain, nodeType, isUri);
		}

		function addIncomingLink() {
			hide();
			console.log("addIncomingLink");
			IncomingOutgoingLinksDialog.getInstance().showBlank(worksheetId,
				columnId, alignmentId,
				columnLabel, columnUri, columnDomain, nodeType, isUri,
				"incoming");
			
		};

		function searchData() {
			hide();
			AugmentDataDialog.getInstance(worksheetId,
				columnDomain, columnUri, alignmentId).show();
		}

		function addOutgoingLink() {
			hide();
			console.log("addOutgoingLink");
			IncomingOutgoingLinksDialog.getInstance().showBlank(worksheetId,
				columnId, alignmentId,
				columnLabel, columnUri, columnDomain, nodeType, isUri,
				"outgoing");
		}

		function addOutgoingLiteral() {
			hide();
			console.log("addOutgoingLiteral");
			AddLiteralNodeDialog.getInstance().showWithProperty(worksheetId, columnId, columnDomain);
		}
		
		function deleteNode() {
			console.log("Delete Node");
			var info = generateInfoObject(worksheetId, "", "DeleteNodeCommand");
			var newInfo = info['newInfo'];
			var label = columnLabel;
			newInfo.push(getParamObject("label", label, "other"));
			newInfo.push(getParamObject("id", columnId, "other"));
			info["newInfo"] = JSON.stringify(newInfo);
			showLoading(worksheetId);
			sendRequest(info, worksheetId);
			hide();
		}

		function editNode() {
			console.log("Edit Node");
			hide();
			AddLiteralNodeDialog.getInstance().showEdit(worksheetId, columnId);
		}
		
		function exportCSV() {
			hide();
			ExportCSVModelDialog.getInstance().show(worksheetId, alignmentId, columnId, "exportCSV");
		};

		function exportJSON() {
			hide();
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
			hide();
			console.log("exportAvro");
			var info = generateInfoObject(worksheetId, "", "ExportAvroCommand");
			var newInfo = info['newInfo'];
			newInfo.push(getParamObject("alignmentNodeId", columnId, "other"));
			info["newInfo"] = JSON.stringify(newInfo);
			showLoading(worksheetId);
			var returned = sendRequest(info, worksheetId);
		}

		function invokeMLService() {
			hide();
			ExportCSVModelDialog.getInstance().show(worksheetId, alignmentId, columnId, "invokeMLService");
		}

		function duplicateNode() {
			var info = generateInfoObject(worksheetId, "", "AddNodeCommand");
			var newInfo = info['newInfo'];
			newInfo.push(getParamObject("label", "", "other"));
			newInfo.push(getParamObject("uri", columnUri, "other"));
			newInfo.push(getParamObject("id", "", "other"));
			info["newInfo"] = JSON.stringify(newInfo);
			showLoading(worksheetId);

			var returned = sendRequest(info, worksheetId);
			hide();
		}

		function generateJS() {
			var ul = $("<ul>").addClass("list-group");
			for (var i = 0; i < options.length; i++) {
				var option = options[i];
				var li = $("<li>").addClass("list-group-item");
				if(i % 2 == 0)
					li.addClass("list-even");
				else
					li.addClass("list-odd");
				if (option.name == "divider") {
					continue;
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
				.append(ul);

			var container = $("#classDialogFunctions");
			container.append(div);
		}

		function show(p_worksheetId, p_columnId, p_columnLabel, p_columnUri, p_columnDomain, p_columnCategory, 
				p_alignmentId, p_nodeType, p_isUri, hideFunc,
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
			hideFunction = hideFunc;

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