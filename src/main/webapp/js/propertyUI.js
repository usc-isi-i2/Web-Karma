function PropertyUI(id,  propertyFuncTop, propertyFuncBottom, maxHeight) {
	var propertyDiv;
	var propertyList1, propertyList2;
	var propertySelectorCallback = null;
	var propertyData = {};
	var defaultPropertyData = {"uri":"", "label":"", "id":""};
	var selectedClassData = {"uri":"", "label":"", "id":""};
	
	var propertyLabel = "Property";
	var textForPropertyList1 = "", textForPropertyList2 = "";
	
	
	function populatePropertyList(dataArray, list1, list2) {
		if(dataArray.length == 0) {
	        $(list1).html("<i>none</i>");
	    } else {
	        $(list1).jstree({
	            "json_data" : {
	                "data" : dataArray
	            },
	            "themes" : {
	                "theme" : "proton",
	                "url": "uiLibs/jquery/css/jstree-themes/proton/style.css",
	                "dots" : false,
	                "icons" : false
	            },
	            "search" : {
	                "show_only_matches": true
	            },
	            "plugins" : [ "themes", "json_data", "ui", "search"]
	        })
	        	.bind("select_node.jstree", function (e, data) {
	        		propertyData.label = data.rslt.obj.data("label");
	        		propertyData.uri = data.rslt.obj.data("uri");
	        		propertyData.id = data.rslt.obj.data("id");
	                var a = $.jstree._focused().get_selected();
	                $(list2).jstree("deselect_all");
	                $(list1).jstree("open_node", a);
	                
	                $("#" + id + "_propertyKeyword").val(propertyData.label);
	                if(propertySelectorCallback != null) {
	                	propertySelectorCallback(propertyData);
	                }
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
										.val(defaultPropertyData.label)
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
		
		$(document).on('keyup',  "#" + id + "_propertyKeyword", function(event) {
			var keyword = $("#" + id + "_propertyKeyword").val();
			 //console.log("Property keyup: " + keyword);
			 $("div#" + id + "_propertyList1").jstree("search", keyword);
			 $("div#" + id + "_propertyList2").jstree("search", keyword);
		 });
		
		mainDiv.append(propertyDiv);
		
		
		$("#" + id + "_propertyList2").on("loaded.jstree", function (e, data) {
			console.log("propertyList2 jstree loaded");
			if(defaultPropertyData.label.length > 0) {
				$("#" + id + "_propertyKeyword").val(defaultPropertyData.label);
				window.setTimeout(function() {
					if(defaultPropertyData.label.length > 0) {
						var treeId = "#" + PropertyUI.getNodeID(defaultPropertyData.label, defaultPropertyData.id, defaultPropertyData.uri);
						console.log("Now select node:" + treeId + " in propertyList");
						jQuery("#" + id + "_propertyList2").jstree('select_node', treeId, false, false);
					}	
					//jQuery("#" + id + "_propertyList2").jstree('scroll_to_node', treeId);
				}, 500);
			}
		}); 
		
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
		defaultPropertyData.label = label;
		defaultPropertyData.id = propId;
		defaultPropertyData.uri = uri;
	};
	
	this.setSelectedClass = function(label, classId, uri) {
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
	//var nodeData = {data:{title:label, "id":treeId}, metadata:{"uri": uri, "id" : id}, attributes:{"id":treeId}};
	var nodeData = { attr: { id : treeId }, data: label, metadata:{"uri": uri, id : cId, "label":label} } ;
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
