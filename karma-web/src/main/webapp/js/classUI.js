
function ClassUI(id,  
		classFuncTop,classFuncBottom, maxHeight, loadTree, searchFunction, numSearchResults) {

	var classDiv;
	var classList1, classList2;

	var classSelectorCallback = null;
	var classData = {
		"uri": "",
		"label": "",
		"rdfsLabel": "",
		"id": ""
	};
	var selectedPropertyData = {
		"uri": "",
		"label": "",
		"rdfsLabel": "",
		"id": ""
	};

	var classLabel = "Class";
	var textForClassList1 = "",
		textForClassList2 = "";

	function populateClassList(dataArray, list1, list2) {
		if (!loadTree)
			return;

		console.log("PopulateClassList:" + dataArray.length);
		//console.log(dataArray);

		var selectOnLoad = false;
		if (dataArray.length == 0) {
			$(list1).jstree("destroy");
			$(list1).html("<i>none</i>");
		} else {
			$(list1).jstree("destroy");
			$(list1)
				.on("select_node.jstree", function(e, data) {
					var selectedNodeData = data.node.original;
					classData.label = selectedNodeData.metadata.label;
					classData.rdfsLabel = selectedNodeData.metadata.rdfsLabel;
					classData.uri = selectedNodeData.metadata.uri;
					classData.id = selectedNodeData.metadata.id;

					var treeId = ClassUI.getNodeID(classData.label, classData.rdfsLabel, classData.id, classData.uri);
					$(list1).jstree('open_node', treeId); //Open node will scroll to that pos

					$(list2).jstree("deselect_all");
					$("#" + id + "_classKeyword").val(Settings.getInstance().getDisplayLabel(classData.label, classData.rdfsLabel, true));

					if (!selectOnLoad && classSelectorCallback != null) {
						classSelectorCallback(classData);
					}
					selectOnLoad = false;
				})
				.on("loaded.jstree", function(e, data) {
					console.log("loaded classlist: " + $(list1).attr("id"));
					if (classData.label.length > 0 && classData.label != "Class") {
						$("#" + id + "_classKeyword").val(Settings.getInstance().getDisplayLabel(classData.label, classData.rdfsLabel, true));
					}
					window.setTimeout(function() {
						if (classData.label.length > 0 && classData.label != "Class") {
							var treeId = ClassUI.getNodeID(classData.label, classData.rdfsLabel, classData.id, classData.uri);
							console.log("Now select node:" + treeId + " in classList " + $(list1).attr("id"));
							selectOnLoad = true;

							$(list1).jstree('select_node', treeId);

							window.setTimeout(function() {
								selectOnLoad = false;
							}, 500);
							//$(list1).jstree('scroll_to_node', treeId);
						}
					}, 500);
				})
				.jstree({
					"core": {
						"data": dataArray,
						"multiple": false,
						"animation": 0,
						'check_callback': function(operation, node, node_parent, node_position, more) {
							// operation can be 'create_node', 'rename_node', 'delete_node', 'move_node' or 'copy_node'
							// in case of 'rename_node' node_position is filled with the new node name
							//return operation === 'rename_node' ? true : false;
							return true;
						}
					},
					"search": {
						"show_only_matches": true,
						"fuzzy": false
					},
					"plugins": ["search", "wholerow"]
				});
		}
	}

	this.setClassLabel = function(label) {
		classLabel = label;
	};

	this.setTreeLoad = function(treeLoad) {
		loadTree = treeLoad;
	};

	/*
	 * mainDiv: div to which the generate UI should be attached
	 * populateData : if true, data will be populated immediately, else you can use the
	 * populateClassesAndProperties to delay the data addition
	 */
	this.generateJS = function(mainDiv, populateData) {
		classDiv = $("<div>").attr("id", id);
		var classInputDiv = $("<div>")
			.addClass("col-sm-12")
			.addClass("form-group")
			.append($("<label>")
				.text(classLabel)
				.attr("for", id + "_classKeyword"))
			.append($("<input>")
				.attr("type", "text")
				.addClass("form-control")
				.attr("id", id + "_classKeyword")
				.attr("autocomplete", "off")
				.val(Settings.getInstance().getDisplayLabel(classData.label, classData.rdfsLabel, true))
				.addClass("classInput")
			);

		var row1 = $("<div>").addClass("row");
		row1.append(classInputDiv);
		classDiv.append(row1);

		var textbox = "#" + id + "_classKeyword";
		
		if (loadTree) {
			classList1 = $("<div>").attr("id", id + "_classList1").addClass(id + "_classList1").css("overflow", "auto").css("height", maxHeight + "px");
			classList2 = $("<div>").attr("id", id + "_classList2").addClass(id + "_classList2").css("overflow", "auto").css("height", maxHeight + "px");;

			var row2 = $("<div>").addClass("row");
			var classListDiv = $("<div>").addClass("col-sm-12");

			if (classFuncTop != null) {
				classListDiv.append($("<div>").addClass("separatorWithText").text(textForClassList1));
				classListDiv.append(classList1);
			}
			if (classFuncBottom != null) {
				classListDiv.append($("<div>").addClass("separatorWithText").text(textForClassList2));
				classListDiv.append(classList2);
			}

			row2.append(classListDiv);
			classDiv.append(row2);


			var searchTimer = null;
			$(document).on('keyup', textbox, function(event) {
				if (searchTimer != null)
					window.clearTimeout(searchTimer);
				searchTimer = window.setTimeout(function() {
					var keyword = $("#" + id + "_classKeyword").val();
					//console.log("Class keyup: " + keyword);
					$("div#" + id + "_classList1").jstree("search", keyword);
					$("div#" + id + "_classList2").jstree("search", keyword);
				}, 1000); //Wait 1 secs before searching


			});
			
			$(document).on('blur', textbox, function(event) {
				var keyword = $(textbox).val();
				if(keyword == "") {
					 $(textbox).val("Class")
				}
			});
			
		} else {
			
			
			if(searchFunction) {
				var srchClasses = searchFunction();
				var classtxt = [];
				for(var i=0; i<srchClasses.length; i++)
					classtxt.push(srchClasses[i].text);
				
				window.setTimeout(function() {
					$(textbox).typeahead('destroy');
					
					$(textbox).typeahead( 
							{source:classtxt,
									minLength:0,
									items: numSearchResults,
									hideOnBlur: false, 
									moreMessage: "More.. Enter text to filter",
									updater:function(keyword) {
										console.log("updater:" + keyword);
										if(keyword != "More..Enter text to filter") {
											if(classSelectorCallback != null) {
												var classData = {label:keyword, id:keyword, uri:keyword};
							                	classSelectorCallback(classData);
							                }
											this.hide();
										}
										return keyword;
									}});
				}, 1000);
				
			} else {
				$(document).on('blur',  textbox, function(event) {
					var keyword = $(textbox).val();
					if(keyword == "") {
						 $(textbox).val("Class")
					} else if(classSelectorCallback != null) {
						var classData = {label:keyword, id:keyword, uri:keyword};
	                	classSelectorCallback(classData);
	                }
				 });
			}
		}

		$(document).on('focus', textbox, function(event) {
			var keyword = $(textbox).val();
			if(keyword == "Class") {
				 $(textbox).val("")
			}
		});
		
		mainDiv.append(classDiv);

		if (populateData) {
			this.populateClasses();
		}
		return classDiv;
	};

	this.populateClasses = function() {
		if (classFuncTop != null)
			populateClassList(classFuncTop(selectedPropertyData), classList1, classList2);
		if (classFuncBottom != null)
			populateClassList(classFuncBottom(selectedPropertyData), classList2, classList1);
	};

	this.refreshClassDataTop = function(label, rdfsLabel, classId, uri) {
		console.log("classsUI.refreshClassDataTop:" + label + "," + classId + "," + uri);
		this.setSelectedProperty(label, rdfsLabel, classId, uri);
		populateClassList(classFuncTop(selectedPropertyData), classList1, classList2);
	};

	this.refreshClassDataBottom = function(label, rdfsLabel, classId, uri) {
		this.setSelectedProperty(label, rdfsLabel, classId, uri);
		populateClassList(classFuncBottom(selectedPropertyData), classList2, classList1);
	};

	this.onClassSelect = function(callback) {
		classSelectorCallback = callback;
	};

	this.getSelectedClass = function() {
		return classData;
	};


	this.setDefaultClass = function(label, rdfsLabel, classId, uri) {
		console.log("classUI:setDefaultClass:" + label + "," + classId + "," + uri);
		classData.label = label;
		classData.rdfsLabel = rdfsLabel;
		classData.id = classId;
		classData.uri = uri;
	};

	this.setSelectedProperty = function(label, rdfsLabel, propId, uri) {
		console.log("classUI:setSelectedProperty:" + label + "," + propId + "," + uri);
		selectedPropertyData.label = label;
		selectedPropertyData.rdfsLabel = rdfsLabel;
		selectedPropertyData.id = propId;
		selectedPropertyData.uri = uri;
	};

	this.setHeadings = function(heading1, heading2) {
		textForClassList1 = heading1;
		textForClassList2 = heading2;
	};
};

//Static declarations
ClassUI.getNodeObject = function(label, rdfsLabel, cId, uri) {
	var treeId = ClassUI.getNodeID(label, rdfsLabel, cId, uri);
	var text = Settings.getInstance().getDisplayLabel(label, rdfsLabel);
	var nodeData = {
		"id": treeId,
		"parent": "#",
		"text": text,
		metadata: {
			"uri": uri,
			id: cId,
			"label": label,
			"rdfsLabel": rdfsLabel
		}
	};
	//var nodeData = { attr: { id : treeId }, data: label, metadata:{"uri": uri, id : cId, "label":label} } ;
	return nodeData;
};

ClassUI.parseNodeObject = function(nodeData) {
	//return [nodeData.data.title, nodeData.metadata.id, nodeData.metadata.uri];
	return [nodeData.metadata.label, nodeData.metadata.rdfsLabel, nodeData.metadata.id, nodeData.metadata.uri];
};

ClassUI.getNodeID = function(label, rdfsLabel, id, uri) {
	//var str = label.replace(/:/g, "_").replace(/ /g, '_').replace(/\//g, "_").replace(/#/g, "_");
	var str = label.replace(/(:|-| |#|\/|\.|\[|\]|\)|\()/g, "_");
	return str;
};
