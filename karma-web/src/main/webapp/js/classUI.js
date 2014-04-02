function ClassUI(id,  
		classFuncTop,classFuncBottom, maxHeight) {
	
	var classDiv;
	var classList1, classList2;
	
	var classSelectorCallback = null;
	var classData = {"uri":"", "label":"", "id":""};
	var selectedPropertyData = {"uri":"", "label":"", "id":""};
	
	var classLabel = "Class";
	var textForClassList1 = "", textForClassList2 = "";
	
	function populateClassList(dataArray, list1, list2) {
		console.log("PopulateClassList:" + dataArray.length);
		var selectOnLoad = false;
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
	                classData.label = data.rslt.obj.data("label");
	                classData.uri = data.rslt.obj.data("uri");
	                classData.id = data.rslt.obj.data("id");
	                var a = $.jstree._focused().get_selected();
	                $(list2).jstree("deselect_all");
	                $(list1).jstree("open_node", a);
	                
	                $("#" + id + "_classKeyword").val(classData.label);
	                
	                if(!selectOnLoad && classSelectorCallback != null) {
	                	classSelectorCallback(classData);
	                }
	                selectOnLoad = false;
	            })
	            .bind("loaded.jstree", function (e, data) {
	            	console.log("loaded classlist: " + $(list1).attr("id"));
	            	if(classData.label.length > 0 && classData.label != "Class") {
	            		$("#" + id + "_classKeyword").val(classData.label);
	            	}
	    			window.setTimeout(function() {
	    				if(classData.label.length > 0 && classData.label != "Class") {
	    					var treeId = "#" + ClassUI.getNodeID(classData.label, classData.id, classData.uri);
	    					console.log("Now select node:" + treeId + " in classList " + $(list1).attr("id"));
	    					selectOnLoad = true;
	    					$(list1).jstree('select_node', treeId, true, true);
	    					
	    					window.setTimeout(function() {
								selectOnLoad = false;
							}, 500);
	    					//$(list1).jstree('scroll_to_node', treeId);
	    				}
	    			}, 500);
	            })
	            ;
	    }
	}
	
	this.setClassLabel = function(label) {
		classLabel = label;
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
										.val(classData.label)
										.addClass("classInput")
								);
		
		var row1 = $("<div>").addClass("row");
		row1.append(classInputDiv);
		classDiv.append(row1);
		
		classList1 = $("<div>").attr("id", id + "_classList1").addClass(id + "_classList1").css("overflow","auto").css("height", maxHeight + "px");
		classList2 = $("<div>").attr("id", id + "_classList2").addClass(id + "_classList2").css("overflow","auto").css("height", maxHeight + "px");;
			
		var row2 =  $("<div>").addClass("row");
		var classListDiv = $("<div>").addClass("col-sm-12");
		
		if(classFuncTop != null)
			classListDiv.append($("<div>").addClass("separatorWithText").text(textForClassList1))
						.append(classList1);
		
		if(classFuncBottom != null)
			classListDiv.append($("<div>").addClass("separatorWithText").text(textForClassList2))
						//.append($("<div>").addClass("separator"))
						.append(classList2);
						
		
		row2.append(classListDiv);
		classDiv.append(row2);
		
		$(document).on('keyup', "#" + id + "_classKeyword",function(event){
			 var keyword = $("#" + id + "_classKeyword").val();
			 //console.log("Class keyup: " + keyword);
			 $("div#" + id + "_classList1").jstree("search", keyword);
			 $("div#" + id + "_classList2").jstree("search", keyword);
			 
		});
		
		mainDiv.append(classDiv);
		
		if(populateData) {
			this.populateClasses();
		}
		return classDiv;
	};
	
	this.populateClasses = function() {
		if(classFuncTop != null)
			populateClassList(classFuncTop(selectedPropertyData), classList1, classList2);
		if(classFuncBottom != null)
			populateClassList(classFuncBottom(selectedPropertyData), classList2, classList1);
	};
	
	this.refreshClassDataTop = function(label, classId, uri) {
		console.log("classsUI.refreshClassDataTop:" + label + "," + classId + "," + uri);
		this.setSelectedProperty(label, classId, uri);
		populateClassList(classFuncTop(selectedPropertyData), classList1, classList2);
	};
	
	this.refreshClassDataBottom = function(label, classId, uri) {
		this.setSelectedProperty(label, classId, uri);
		populateClassList(classFuncBottom(selectedPropertyData), classList2, classList1);
	};
	
	this.onClassSelect = function(callback) {
		classSelectorCallback = callback;
	};
	
	this.getSelectedClass = function() {
		return classData;
	};
	
	
	this.setDefaultClass = function(label, classId, uri) {
		console.log("classUI:setDefaultClass:" + label + "," + classId + "," + uri);
		classData.label = label;
		classData.id = classId;
		classData.uri = uri;
	};
	
	this.setSelectedProperty = function(label, propId, uri) {
		console.log("classUI:setSelectedProperty:" + label + "," + propId + "," + uri);
		selectedPropertyData.label = label;
		selectedPropertyData.id = propId;
		selectedPropertyData.uri = uri;
	};
	
	this.setHeadings = function(heading1, heading2) {
		textForClassList1 = heading1;
		textForClassList2 = heading2;
	};
};

//Static declarations
ClassUI.getNodeObject = function(label, cId, uri) {
	var treeId = ClassUI.getNodeID(label, cId, uri);
	//var nodeData = {data:{title:label, "id":treeId}, metadata:{"uri": uri, "id" : id}, attributes:{"id":treeId}};
	var nodeData = { attr: { id : treeId }, data: label, metadata:{"uri": uri, id : cId, "label":label} } ;
	return nodeData;
};

ClassUI.parseNodeObject = function(nodeData) {
	//return [nodeData.data.title, nodeData.metadata.id, nodeData.metadata.uri];
	return [nodeData.metadata.label, nodeData.metadata.id, nodeData.metadata.uri];
};

ClassUI.getNodeID = function(label, id, uri) {
	//var str = label.replace(/:/g, "_").replace(/ /g, '_').replace(/\//g, "_").replace(/#/g, "_");
	var str = label.replace( /(:|-| |#|\/|\.|\[|\]|\)|\()/g, "_");
	return str;
};
