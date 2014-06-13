function PropertyUI(id,  propertyFuncTop, propertyFuncBottom, maxHeight) {
	var propertyDiv;
	var propertyList1, propertyList2;
	var propertySelectorCallback = null;
	var propertyData = {"uri":"", "label":"", "id":""};
	
	var selectedClassData = {"uri":"", "label":"", "id":""};
	
	var propertyLabel = "Property";
	var textForPropertyList1 = "", textForPropertyList2 = "";
	
	
	function populatePropertyList(dataArray, list1, list2) {
		var selectOnLoad = false;
		console.log("PopulatePropertyList:" + dataArray.length);
		
		if(dataArray.length == 0) {
			$(list1).jstree("destroy");
	        $(list1).html("<i>none</i>");
	    } else {
	    	$(list1).jstree("destroy");
	        $(list1)
	        	.on("select_node.jstree", function (e, data) {
	        		var selectedNodeData = data.node.original;
	        		propertyData.label =selectedNodeData.text;
	        		propertyData.uri = selectedNodeData.metadata.uri;
	        		propertyData.id = selectedNodeData.metadata.id;
	               
	        		var treeId = PropertyUI.getNodeID(propertyData.label, propertyData.id, propertyData.uri);
	                $(list1).jstree('open_node', treeId); //Open node will scroll to that pos
	                
	                $(list2).jstree("deselect_all");
	                
	                $("#" + id + "_propertyKeyword").val(propertyData.label);
	                if(!selectOnLoad && propertySelectorCallback != null) {
	                	propertySelectorCallback(propertyData);
	                }
	                selectOnLoad = false;
	            })
	            .on("loaded.jstree", function(e,data) {
	            	console.log("property jstree Type: " + $(list1).attr("id"));
	            	if(propertyData.label.length > 0) {
	            		$("#" + id + "_propertyKeyword").val(propertyData.label);
	            	}
	            	window.setTimeout(function() {
						if(propertyData.label.length > 0) {
							var treeId = PropertyUI.getNodeID(propertyData.label, propertyData.id, propertyData.uri);
							console.log("Now select node:" + treeId + " in propertyList:" + $(list1).attr("id"));
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
		        	"core" : {
		                "data" : dataArray,
		                "multiple" : false,
		                "animation" : 0,
		                'check_callback' : function (operation, node, node_parent, node_position, more) {
		                    // operation can be 'create_node', 'rename_node', 'delete_node', 'move_node' or 'copy_node'
		                    // in case of 'rename_node' node_position is filled with the new node name
		                    //return operation === 'rename_node' ? true : false;
		                	return true;
		                }
		            },
		            "search" : {
		                "show_only_matches": true,
		                "fuzzy": false
		            },
		            "plugins" : [ "search", "wholerow"]
		        })
	            ;
	    }
	}
	
	this.setPropertyLabel = function(label) {
		propertyLabel = label;
	};
	
	this.setPropertyRefresh = function(refresh) {
		refreshProperties = refresh;
	};
	
	/*
	 * mainDiv: div to which the generate UI should be attached
	 * populateData : if true, data will be populated immediately, else you can use the
	 * populateClassesAndProperties to delay the data addition
	 */
	this.generateJS = function(mainDiv, populateData) {
		propertyDiv = $("<div>").attr("id", id);
		
		var propertyInputDiv = $("<div>")
								.addClass("col-sm-12")
								.addClass("form-group")
								.append($("<label>")
										.text(propertyLabel)
										.attr("for", id + "_propertyKeyword"))
								.append($("<input>")
										.attr("type", "text")
										.addClass("form-control")
										.attr("id", id + "_propertyKeyword")
										.attr("autocomplete", "off")
										.val(propertyData.label)
										.addClass("propertyInput")
								);
		
		var row1 = $("<div>").addClass("row");
		row1.append(propertyInputDiv);
		propertyDiv.append(row1);
		
		propertyList1 = $("<div>").attr("id", id + "_propertyList1").addClass(id + "_propertyList1").css("overflow","auto").css("height", maxHeight + "px");;
		propertyList2 = $("<div>").attr("id", id + "_propertyList2").addClass(id + "_propertyList2").css("overflow","auto").css("height", maxHeight + "px");;
		
		var row2 =  $("<div>").addClass("row");
						
		var propertyListDiv = $("<div>")
								.addClass("col-sm-12")
								.append($("<div>").addClass("separatorWithText").text(textForPropertyList1))
								.append(propertyList1)
								.append($("<div>").addClass("separatorWithText").text(textForPropertyList2))
								//.append($("<div>").addClass("separator"))
								.append(propertyList2);
		row2.append(propertyListDiv);
		propertyDiv.append(row2);
		
		var searchTimer = null;
		$(document).on('keyup',  "#" + id + "_propertyKeyword", function(event) {
			if(searchTimer != null)
				window.clearTimeout(searchTimer);
			var searchTimer = window.setTimeout(function() {
				var keyword = $("#" + id + "_propertyKeyword").val();
				 //console.log("Property keyup: " + keyword);
				 $("div#" + id + "_propertyList1").jstree("search", keyword);
				 $("div#" + id + "_propertyList2").jstree("search", keyword);
			}, 1000); //Wait 1 secs before searching
		 });
		
		mainDiv.append(propertyDiv);
		
		if(populateData) {
			this.populateProperties();
		} 
		
		return propertyDiv;
	};
	
	this.populateProperties = function() {
		populatePropertyList(propertyFuncTop(selectedClassData), propertyList1, propertyList2);
		populatePropertyList(propertyFuncBottom(selectedClassData), propertyList2, propertyList1);
	};
	
	this.refreshPropertyDataTop = function(label, classId, uri) {
		this.setSelectedClass(label, classId, uri);
		populatePropertyList(propertyFuncTop(selectedClassData), propertyList1, propertyList2);
	};
	
	this.refreshPropertyDataBottom = function(label, classId, uri) {
		this.setSelectedClass(label, classId, uri);
		populatePropertyList(propertyFuncBottom(selectedClassData), propertyList2, propertyList1);
	};
	
	this.onPropertySelect = function(callback) {
		propertySelectorCallback = callback;
	};
	
	this.getSelectedProperty = function() {
		return propertyData;
	};

	this.setDefaultProperty = function(label, propId, uri) {
		console.log("propertyUI:setDefaultProperty:" + label + "," + propId + "," + uri);
		propertyData.label = label;
		propertyData.id = propId;
		propertyData.uri = uri;
	};
	
	this.setSelectedClass = function(label, classId, uri) {
		console.log("propertyUI:setSelectedClass:" + label + "," + classId + "," + uri);
		selectedClassData.label = label;
		selectedClassData.id = classId;
		selectedClassData.uri = uri;
	};
	
	this.setHeadings = function(heading1, heading2) {
		textForPropertyList1 = heading1;
		textForPropertyList2 = heading2;
	};

};

//Static declarations
PropertyUI.getNodeObject = function(label, cId, uri) {
	var treeId = PropertyUI.getNodeID(label, cId, uri);
	
	var nodeData = { "id" : treeId, "parent" : "#", "text" : label, metadata:{"uri": uri, id : cId, "label":label} };
//	var nodeData = { attr: { id : treeId }, data: label, metadata:{"uri": uri, id : cId, "label":label} } ;
	return nodeData;
};

PropertyUI.parseNodeObject = function(nodeData) {
	//return [nodeData.data.title, nodeData.metadata.id, nodeData.metadata.uri];
	return [nodeData.metadata.label, nodeData.metadata.id, nodeData.metadata.uri];
};

PropertyUI.getNodeID = function(label, id, uri) {
	//var str = label.replace(/:/g, "_").replace(/ /g, '_').replace(/\//g, "_").replace(/#/g, "_");
	var str = label.replace( /(:|-| |#|\/|\.|\[|\]|\)|\()/g, "_");
	return str;
};
