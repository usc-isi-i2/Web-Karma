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
		console.log(dataArray);
		
		var selectOnLoad = false;
		if(dataArray.length == 0) {
	        $(list1).html("<i>none</i>");
	    } else {
	        $(list1).jstree({
	            "core" : {
	                "data" : dataArray,
	                "multiple" : false,
	                "animation" : 0
	            },
	            "search" : {
	                "show_only_matches": true,
	                "fuzzy" : false
	            },
	            "plugins" : [ "search", "wholerow"]
	        })
	        	.bind("select_node.jstree", function (e, data) {
	        		var selectedNodeData = data.node.original;
	                classData.label = selectedNodeData.text;
	                classData.uri = selectedNodeData.metadata.uri;
	                classData.id = selectedNodeData.metadata.id;
	                //var a = $.jstree._focused().get_selected();
	                $(list2).jstree("deselect_all");
	                $(list1).jstree("open_node", data.node);
	                
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
		
//		$(list1).jstree({ 'core' : {
//		    'data' : [
//		       { "id" : "ajson1", "parent" : "#", "text" : "Simple root node" },
//		       { "id" : "ajson2", "parent" : "#", "text" : "Root node 2" },
//		       { "id" : "ajson3", "parent" : "ajson2", "text" : "Child 1" },
//		       { "id" : "ajson4", "parent" : "ajson2", "text" : "Child 2" },
//		    ]
//		} });
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
		
		var searchTimer = null;
		$(document).on('keyup', "#" + id + "_classKeyword",function(event){
			if(searchTimer != null)
				window.clearTimeout(searchTimer);
			var searchTimer = window.setTimeout(function() {
				var keyword = $("#" + id + "_classKeyword").val();
				 //console.log("Class keyup: " + keyword);
				 $("div#" + id + "_classList1").jstree("search", keyword);
				 $("div#" + id + "_classList2").jstree("search", keyword);
			}, 1000); //Wait 1 secs before searching
			 
			 
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
	var nodeData = { "id" : treeId, "parent" : "#", "text" : label, metadata:{"uri": uri, id : cId, "label":label} };
	//var nodeData = { attr: { id : treeId }, data: label, metadata:{"uri": uri, id : cId, "label":label} } ;
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
