function ClassPropertyUI(id,  
		classFuncTop, propertyFuncTop, 
		classFuncBottom, propertyFuncBottom,
		isClassFirst,
		maxHeight) {
	var classPropertyDiv;
	var classList1, classList2;
	var propertyList1, propertyList2;
	var classSelectorCallback = null, propertySelectorCallback = null;
	var classData = {};
	var propertyData = {};
	var defaultClassData = {"uri":"", "label":"", "id":""};
	var defaultPropertyData = {"uri":"", "label":"", "id":""};
	
	var classLabel = "Class";
	var propertyLabel = "Property";
	var refreshClasses = true;
	var refreshProperties = true;
	var textForClassList1 = "", textForClassList2 = "";
	var textForPropertyList1 = "", textForPropertyList2 = "";
	
	function populateClassList(dataArray, list1, list2) {
		console.log("PopulateClassList:" + dataArray.length);
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
	                classData.label = data.rslt.obj.context.lastChild.wholeText;
	                classData.uri = data.rslt.obj.data("uri");
	                classData.id = data.rslt.obj.data("id");
	                var a = $.jstree._focused().get_selected();
	                $(list2).jstree("deselect_all");
	                $(list1).jstree("open_node", a);
	                
	                if(refreshProperties) {
	                	var properties = propertyFuncTop(classData);
	                	populatePropertyList(properties, propertyList1, propertyList2);
	                }
	                
	                $("#" + id + "_classKeyword").val(classData.label);
	                
	                if(classSelectorCallback != null)
	                	classSelectorCallback(classData);
	            });
	    }
	}
	
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
	        		propertyData.label = data.rslt.obj.context.lastChild.wholeText;
	        		propertyData.uri = data.rslt.obj.data("uri");
	        		propertyData.id = data.rslt.obj.data("id");
	                var a = $.jstree._focused().get_selected();
	                $(list2).jstree("deselect_all");
	                $(list1).jstree("open_node", a);
	                
	                if(refreshClasses) {
		                var classes = classFuncTop(propertyData);
		                populateClassList(classes, classList1, classList2);
	                }
	                
	                $("#" + id + "_propertyKeyword").val(propertyData.label);
	                if(propertySelectorCallback != null) {
	                	propertySelectorCallback(propertyData);
	                }
	            });
	    }
	}
	
	this.setClassLabel = function(label) {
		classLabel = label;
	};
	
	this.setPropertyLabel = function(label) {
		propertyLabel = label;
	};
	
	this.setClassRefresh = function(refresh) {
		refreshClasses = refresh;
	};
	
	this.setPropertyRefresh = function(refresh) {
		refreshProperties = refresh
	};
	
	this.generateJS = function() {
		classPropertyDiv = $("<div>").attr("id", id);
		var classInputDiv = $("<div>")
								.addClass("col-sm-6")
								.addClass("form-group")
								.append($("<label>")
										.text(classLabel)
										.attr("for", id + "_classKeyword"))
								.append($("<input>")
										.attr("type", "text")
										.addClass("form-control")
										.attr("id", id + "_classKeyword")
										.attr("autocomplete", "off")
										.val(defaultClassData.label)
										.addClass("classInput")
								);
		var propertyInputDiv = $("<div>")
								.addClass("col-sm-6")
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
		if(isClassFirst) {
			row1.append(classInputDiv);
			row1.append(propertyInputDiv);
		} else {
			row1.append(propertyInputDiv);
			row1.append(classInputDiv);
		}
					
		
		classPropertyDiv.append(row1);
		
		classList1 = $("<div>").attr("id", id + "_classList1").css("overflow","auto").css("height", maxHeight + "px");
		classList2 = $("<div>").attr("id", id + "_classList2").css("overflow","auto").css("height", maxHeight + "px");;
		propertyList1 = $("<div>").attr("id", id + "_propertyList1").css("overflow","auto").css("height", maxHeight + "px");;
		propertyList2 = $("<div>").attr("id", id + "_propertyList2").css("overflow","auto").css("height", maxHeight + "px");;
		
		var row2 =  $("<div>").addClass("row");
		var classListDiv = $("<div>")
								.addClass("col-sm-6")
								.append($("<div>").addClass("separatorWithText").text(textForClassList1))
								.append(classList1)
								.append($("<div>").addClass("separatorWithText").text(textForClassList2))
								//.append($("<div>").addClass("separator"))
								.append(classList2);
						
		var propertyListDiv = $("<div>")
								.addClass("col-sm-6")
								.append($("<div>").addClass("separatorWithText").text(textForPropertyList1))
								.append(propertyList1)
								.append($("<div>").addClass("separatorWithText").text(textForPropertyList2))
								//.append($("<div>").addClass("separator"))
								.append(propertyList2);
		if(isClassFirst) {
			row2.append(classListDiv);
			row2.append(propertyListDiv);
		} else {
			row2.append(propertyListDiv);
			row2.append(classListDiv);
		}
		
		classPropertyDiv.append(row2);
		
		populateClassList(classFuncTop(defaultPropertyData), classList1, classList2);
		populateClassList(classFuncBottom(defaultPropertyData), classList2, classList1);
		
		populatePropertyList(propertyFuncTop(defaultClassData), propertyList1, propertyList2);
		populatePropertyList(propertyFuncBottom(defaultClassData), propertyList2, propertyList1);
		
		$(document).on('keyup', "#" + id + "_classKeyword",function(event){
			 var keyword = $("#" + id + "_classKeyword").val();
			 //console.log("Class keyup: " + keyword);
			 $("div#" + id + "_classList1").jstree("search", keyword);
			 $("div#" + id + "_classList2").jstree("search", keyword);
			 
		});
		
		
		$(document).on('keyup',  "#" + id + "_propertyKeyword", function(event) {
			var keyword = $("#" + id + "_propertyKeyword").val();
			 //console.log("Property keyup: " + keyword);
			 $("div#" + id + "_propertyList1").jstree("search", keyword);
			 $("div#" + id + "_propertyList2").jstree("search", keyword);
		 });
		
		
		return classPropertyDiv;
	};
	
	this.onClassSelect = function(callback) {
		classSelectorCallback = callback;
	};
	
	this.onPropertySelect = function(callback) {
		propertySelectorCallback = callback;
	};
	
	this.getSelectedClass = function() {
		return classData;
	};
	
	this.getSelectedProperty = function() {
		return propertyData;
	};
	
	this.setDefaultClass = function(label, id, uri) {
		defaultClassData.label = label;
		defaultClassData.id = id;
		defaultClassData.uri = uri;
	};
	
	this.setDefaultProperty = function(label, id, uri) {
		defaultPropertyData.label = label;
		defaultPropertyData.id = id;
		defaultPropertyData.uri = uri;
	};
	
	this.setPropertyHeadings = function(heading1, heading2) {
		textForPropertyList1 = heading1;
		textForPropertyList2 = heading2;
	};
	
	this.setClassHeadings = function(heading1, heading2) {
		textForClassList1 = heading1;
		textForClassList2 = heading2;
	}
};

//Static declarations
ClassPropertyUI.getNodeObject = function(label, id, uri) {
	var nodeData = {data:label, metadata:{"uri": uri, "id" : id}};
	return nodeData;
};