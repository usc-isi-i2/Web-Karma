var ClassFunctions = (function() {

	var instance = null;


	function PrivateConstructor() {
		var menuId = "classFunctionsMenu";
		var parentId = "classDialog";
		var hideFunction;

		var worksheetId, columnId;
		var columnUri, columnLabel, columnRdfsLabel, columnRdfsComment, columnDomain, columnCategory, alignmentId;
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
		//}, {
		//		name: "Augment Data",
		//		func: searchData
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
		//	}, {
		//		name: "Invoke Table Service",
		//		func: invokeMLService
			},


		];

		function init() {
			generateJS();
		}

		function hide() {
			if(hideFunction)
				hideFunction();
		}

		function manageLinks(e) {
			hide();
			console.log("showIncomingOutgoingLinks");
			ManageIncomingOutgoingLinksDialog.getInstance().show(worksheetId,
				columnId, alignmentId,
				columnLabel, columnRdfsLabel, columnRdfsComment,
				columnUri, columnDomain, nodeType, isUri);
			e.preventDefault();
		}

		function addIncomingLink(e) {
			hide();
			console.log("addIncomingLink");
			IncomingOutgoingLinksDialog.getInstance().showBlank(worksheetId,
				columnId, alignmentId,
				columnLabel, columnRdfsLabel, columnRdfsComment, 
				columnUri, columnDomain, nodeType, isUri,
				"incoming");
			e.preventDefault();
		};

		function searchData(e) {
			hide();
			AugmentDataDialog.getInstance(worksheetId,
				columnDomain, columnUri, alignmentId).show();
			e.preventDefault();
		}

		function addOutgoingLink(e) {
			hide();
			console.log("addOutgoingLink");
			IncomingOutgoingLinksDialog.getInstance().showBlank(worksheetId,
				columnId, alignmentId,
				columnLabel, columnRdfsLabel, columnRdfsComment, columnUri, columnDomain, nodeType, isUri,
				"outgoing");
			e.preventDefault();
		}

		function addOutgoingLiteral(e) {
			hide();
			console.log("addOutgoingLiteral");
			AddLiteralNodeDialog.getInstance().showWithProperty(worksheetId, columnId, columnDomain);
			e.preventDefault();
		}
		
		function deleteNode(e) {
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
			e.preventDefault();
		}

		function editNode(e) {
			console.log("Edit Node");
			hide();
			AddLiteralNodeDialog.getInstance().showEdit(worksheetId, columnId);
			e.preventDefault();
		}
		
		function exportCSV(e) {
			hide();
			ExportCSVModelDialog.getInstance().show(worksheetId, alignmentId, columnId, "exportCSV");
			e.preventDefault();
		};

		function exportJSON(e) {
			hide();
			console.log("exportJSON");
			// var info = generateInfoObject(worksheetId, "", "ExportJSONCommand");
			// var newInfo = info['newInfo'];
			// newInfo.push(getParamObject("alignmentNodeId", columnId, "other"));
			// info["newInfo"] = JSON.stringify(newInfo);

			// showLoading(worksheetId);
			// var returned = sendRequest(info, worksheetId);
			ExportJSONDialog.getInstance().show(worksheetId, columnId);
			e.preventDefault();
		}

		function exportAvro(e) {
			hide();
			console.log("exportAvro");
			var info = generateInfoObject(worksheetId, "", "ExportAvroCommand");
			var newInfo = info['newInfo'];
			newInfo.push(getParamObject("alignmentNodeId", columnId, "other"));
			info["newInfo"] = JSON.stringify(newInfo);
			showLoading(worksheetId);
			var returned = sendRequest(info, worksheetId);
			e.preventDefault();
		}

		function invokeMLService(e) {
			hide();
			ExportCSVModelDialog.getInstance().show(worksheetId, alignmentId, columnId, "invokeMLService");
			e.preventDefault();
		}

		function duplicateNode(e) {
			var info = generateInfoObject(worksheetId, "", "AddNodeCommand");
			var newInfo = info['newInfo'];
			newInfo.push(getParamObject("label", "", "other"));
			newInfo.push(getParamObject("uri", columnUri, "other"));
			newInfo.push(getParamObject("id", "", "other"));
			info["newInfo"] = JSON.stringify(newInfo);
			showLoading(worksheetId);

			var returned = sendRequest(info, worksheetId);
			hide();
			e.preventDefault();
		}

		function generateJS() {
			var btnList = $("<div>").addClass("btn-group-vertical").css("display", "block");
			for (var i = 0; i < options.length; i++) {
				var option = options[i];

				var btn = $("<button>").addClass("btn").addClass("btn-default")
						.text(option.name)
						.click(option.func)
						.data("category", option.category)
						.data("nodeType", option.nodeType);

				btnList.append(btn);

				if(i % 2 == 0)
					btn.addClass("list-even");
				else
					btn.addClass("list-odd");
				
			}

			var div = $("<div>")
				.attr("id", menuId)
				.append(btnList);

			var container = $("#" + parentId + "Functions");
			container.append(div);
		}

		function enableAllItems() {
			var btns = $("button", "#" + menuId);
			for(var i=0; i<btns.length; i++) {
				var btn = $(btns[i]);
				btn.removeClass("disabled");
				btn.prop("disabled", false);
			}
		}
		
		// function disableItem(value) {
		// 	var btns = $("button", "#" + menuId);
		// 	for(var i=0; i<btns.length; i++) {
		// 		var btn = $(btns[i]);
		// 		if(btn.text() == value) {
		// 			btn.addClass("disabled");
		// 			btn.prop("disabled", true);
		// 			break;
		// 		}
		// 	}
		// }

		function show(p_worksheetId, p_columnId, 
				p_columnLabel, p_columnRdfsLabel, p_columnRdfsComment,
				p_columnUri, p_columnDomain, p_columnCategory, 
				p_alignmentId, p_nodeType, p_isUri, hideFunc,
				event) {
			worksheetId = p_worksheetId;
			columnLabel = p_columnLabel;
			columnRdfsLabel = p_columnRdfsLabel;
			columnRdfsComment = p_columnRdfsComment;
			columnId = p_columnId;
			columnUri = p_columnUri;
			columnDomain = p_columnDomain;
			columnCategory = p_columnCategory;
			alignmentId = p_alignmentId;
			nodeType = p_nodeType;
			isUri = p_isUri;
			hideFunction = hideFunc;

			enableAllItems();

			//if(columnCategory.length > 0) {
			$("button", $("#" + menuId)).each(function(index) {
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
				
				if(!show) {
					btn = $(this);
					btn.addClass("disabled");
					btn.prop("disabled", true);
				}
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