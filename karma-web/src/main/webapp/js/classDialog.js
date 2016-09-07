var ClassDialog = (function() {

	var instance = null;


	function PrivateConstructor() {
		var dialog = $("#classDialog");
		var rightDiv = $("#classDialogRight");

		var worksheetId, columnId;
		var columnUri, columnLabel, columnRdfsLabel, columnRdfsComment, columnDomain, columnCategory, alignmentId;
		var nodeType, isUri; //LiteralNode or InternalNode
		var allClassCache;

		function init() {
			ClassTabs.getInstance().reloadAllCache();
		}

		function reloadCache() {
			ClassTabs.getInstance().reloadAllCache();
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

		function initRightDiv(mode) {
			// showFunctionsMenu();
			// disableItem(mode);
			setTitle(mode);
			var firstChild = rightDiv.children();
			firstChild.hide();
			$("body").append(firstChild);
			rightDiv.empty();
		}

		function onSelectClassFromMenu(clazz) {
			if(columnUri == "BlankNode" || columnCategory == "temporary") {
				var links = D3ModelManager.getInstance().getCurrentLinksToNode(worksheetId, columnId);
				$.each(links, function(index, link) {
					if(link.target.type == "ColumnNode") {
						//Set Semantic Type
						var type = {
							"uri": link.uri,
							"label": link.label,
							"rdfsLabel": link.rdfsLabel,
							"rdfsComment": link.rdfsComment,
							"source": clazz
						}
						setSemanticType(worksheetId, link.target.id, type);
					} else {
						//Change Links Command
						var newEdges = [];
						var edge = {
							"uri": link.uri,
							"label": link.label,
							"rdfsLabel": link.rdfsLabel,
							"rdfsComment": link.rdfsComment,
							"target": link.target,
							"source": clazz
						}
						newEdges.push(edge);
						changeLinks(worksheetId, alignmentId, [], newEdges);
					}
				});

			} else {
				var links = D3ModelManager.getInstance().getCurrentLinksToNode(worksheetId, columnId);
				var oldEdges = []
				var newEdges = []
				$.each(links, function(index, link) {
					if(link.source.id == columnId) {
						//Change source
						if(link.target.type == "ColumnNode") {
							//Set Semantic Type
							var type = {
								"uri": link.uri,
								"label": link.label,
								"rdfsLabel": link.rdfsLabel,
								"rdfsComment": link.rdfsComment,
								"source": clazz
							}
							setSemanticType(worksheetId, link.target.id, type);
						} else {
							//Change Links Command
							var edge = {
								"uri": link.uri,
								"label": link.label,
								"rdfsLabel": link.rdfsLabel,
								"rdfsComment": link.rdfsComment,
								"target": link.target,
								"source": clazz
							} 
							newEdges.push(edge);
							oldEdges.push(link);
						}
					} else {
						//Change target
						var edge = {
							"uri": link.uri,
							"label": link.label,
							"rdfsLabel": link.rdfsLabel,
							"rdfsComment": link.rdfsComment,
							"target": clazz,
							"source": link.source
						} 
						newEdges.push(edge);
						oldEdges.push(link);
					}
				});
				if(oldEdges.length > 0 || newEdges.length > 0)
					changeLinks(worksheetId, alignmentId, oldEdges, newEdges);
			}
			hide();
		}

		function setTitle(title) {
			$("#classDialog_title", dialog).html(title + ": " + Settings.getInstance().getDisplayLabel(columnLabel, columnRdfsLabel));
		}

		function show(p_worksheetId, p_columnId, p_columnLabel, p_columnRdfsLabel, p_columnRdfsComment,
				p_columnUri, p_columnDomain, p_columnCategory, 
				p_alignmentId, p_nodeType, p_isUri,
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

			initRightDiv("Change Class");
			ClassTabs.getInstance().show(p_worksheetId, p_columnId, 
				p_columnLabel, p_columnRdfsLabel, p_columnRdfsComment,
				p_columnUri, 
				p_alignmentId, p_nodeType, rightDiv, onSelectClassFromMenu,
				event);

			if(columnCategory != "temporary") {
				$("#classDialogFunctions", dialog).show();
				ClassFunctions.getInstance().show(p_worksheetId, p_columnId, 
													p_columnLabel, p_columnRdfsLabel, p_columnRdfsLabel,
													p_columnUri, p_columnDomain, p_columnCategory, 
													p_alignmentId, p_nodeType, p_isUri, hide, 
													event);
				$("#classDialogRight").removeClass("col-sm-12").addClass("col-sm-10");
			} else {
				$("#classDialogFunctions", dialog).hide();
				$("#classDialogRight").removeClass("col-sm-10").addClass("col-sm-12");
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