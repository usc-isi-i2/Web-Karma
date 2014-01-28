function ClassPropertyUI(id, defaultClass, defaultProperty, 
		classFuncTop, propertyFuncTop, 
		classFuncBottom, propertyFuncBottom,
		listSize) {
	var classPropertyDiv;
	var classList1, classList2;
	var propertyList1, propertyList2;
	
	var classData;
	var propertyData;
	
	function populateClassList(dataArray, list1, list2) {
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
	                "dots" : true,
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
	                
	                var properties = propertyFuncTop(classData.uri);
	                populatePropertyList(properties, propertyList1, propertyList2);
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
	                "dots" : true,
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
	                
	                var classes = classFuncTop(propertyData.uri);
	                populateClassList(classes, classList1, classList2);
	            });
	    }
	}
	
	this.generateJS = function() {
		classPropertyDiv = $("<div>").attr("id", id);
		
		var row1 = $("<div>")
						.addClass("row")
						.append($("<div>")
								.addClass("col-sm-6")
								.append($("<input>")
										.attr("type", "text")
										.addClass("form-control")
										.attr("id", id + "_classKeyword")
										.val(defaultClass)
								)
						)
						.append($("<div>")
								.addClass("col-sm-6")
								.append($("<input>")
										.attr("type", "text")
										.addClass("form-control")
										.attr("id", id + "_propertyKeyword")
										.val(defaultProperty)
								)
						);
		
		classPropertyDiv.append(row1);
		
		classList1 = $("<div>").attr("id", id + "_classList1");
		classList2 = $("<div>").attr("id", id + "_classList2");
		propertyList1 = $("<div>").attr("id", id + "_propertyList1");
		propertyList2 = $("<div>").attr("id", id + "_propertyList2");
		
		var row2 =  $("<div>")
						.addClass("row")
						.append($("<div>")
								.addClass("col-sm-6")
								.append(classList1)
								.append($("<div>").addClass("separator"))
								.append(classList2)
						)
						.append($("<div>")
								.addClass("col-sm-6")
								.append(propertyList1)
								.append($("<div>").addClass("separator"))
								.append(propertyList2)
						);
		classPropertyDiv.append(row2);
		
		populateClassList(classFuncTop(defaultProperty), classList1, classList2);
		populateClassList(classFuncBottom(defaultProperty), classList2, classList1);
		
		populatePropertyList(propertyFuncTop(defaultClass), propertyList1, propertyList2);
		populatePropertyList(propertyFuncBottom(defaultClass), propertyList2, propertyList1);
		
		return classPropertyDiv;
	}
	
	
}