
var PropertyDropdownMenu = (function() {
	
	var instance = null;
	
	
    function PrivateConstructor() {
    	var menuId = "propertyDropdownMenu";
    	var worksheetId;
    	var alignmentId;
    	var propertyId;
    	var propertyUri;
    	var sourceNodeId, sourceLabel, sourceDomain, sourceId;
    	var targetNodeId, targetLabel, targetDomain, targetId;
    	
    	var options = [
    		   	        //Title, function to call, needs file upload     
    		   	        [ "Change Link", changeLink],
    		   	        [ "Delete", deleteLink],
    		   	        ["divider", null],
    		   	        [ "Change From", changeFrom ],
    		   	        [ "Change To" , changeTo ],
    		   	        
    		   	];
    	
    	function init() {
    		generateJS();
    	}
    	
    	function hide() {
    		$("#" + menuId).hide();
    		$(document).off('click', hide);
    	}
    	
    	function changeLink() {
    		console.log("changeLink");
    		IncomingOutgoingLinksDialog.getInstance().show(worksheetId, 
    				columnId, alignmentId,
    				columnLabel, columnUri, columnDomain,
    				"incoming");
    	};
    	
    	function deleteLink() {
    		console.log("deleteLink");
    	}
    	
    	function changeFrom() {
    		console.log("Change From");

    		var dialog = IncomingOutgoingLinksDialog.getInstance();
    		dialog.show(worksheetId, 
    				targetNodeId, alignmentId,
    				targetLabel, targetId, targetDomain,
    				"incoming");
    		dialog.setSelectedClass(sourceId);
    		dialog.setSelectedProperty(propertyId);
    	}
    	
    	function changeTo() {
    		console.log("Change To");
    		

    		var dialog = IncomingOutgoingLinksDialog.getInstance();
    		dilaog.show(worksheetId, 
    				sourceNodeId, alignmentId,
    				sourceLabel, sourceId, sourceDomain,
    				"outgoing");
    		dialog.setSelectedClass(targetId);
    		dialog.setSelectedProperty(propertyId);
    	}
    	
    	function generateJS() {
    		var ul = $("<ul>");
    		ul.attr("role", "menu")
				.addClass("dropdown-menu")
				.css("display", "block")
				.css("position", "static")
				.css("margin-bottom", "5px");
    		for(var i=0; i<options.length; i++) {
    			var option = options[i];
    			var li = $("<li>");
    			if(option[0] == "divider") {
    				li.addClass("divider");
    			} else {
	    			var a = $("<a>")
								.attr("href", "#")
								.attr("tabindex", "-1")
								.text(option[0])
								.click(option[1]);
	    			li.append(a);
    			}
    			ul.append(li);
    		}
    		
    		var div = $("<div>")
    					.attr("id", menuId)
    					.addClass("dropdown")
    					.addClass("clearfix")
    					.addClass("contextMenu")
    					.append(ul);
    		
    		var container = $("body div.container");
    		container.append(div);
    	}
    	
    	function disableItem(itemIdx) {
    		var div = $("#" + menuId);
    		var li = $("li:eq(" + itemIdx + ")", div);
    		li.addClass("disabled");
    	}
    	
    	
    	function show(p_worksheetId, p_alignmentId, p_propertyId, p_propertyUri, 
    			p_sourceNodeId, p_sourceNodeType, p_sourceLabel, p_sourceDomain, p_sourceId,
    			p_targetNodeId, p_targetNodeType, p_targetLabel, p_targetDomain, p_targetId,
    			event) {
    		worksheetId = p_worksheetId;
    		alignmentId = p_alignmentId;
    		propertyId = p_propertyId;
    		propertyUri = p_propertyUri;
    		sourceNodeId = p_sourceNodeId;
    		sourceLabel = p_sourceLabel;
    		sourceDomain = p_sourceDomain;
    		sourceId = p_sourceId;
    		targetNodeId = p_targetNodeId;
    		targetLabel = p_targetLabel;
    		targetDomain = p_targetDomain;
    		targetId = p_targetId;
    		
    		if(p_sourceNodeType == "ColumnNode") {
    			disableItem(3);
    		}
    		
    		if(p_targetNodeType == "ColumnNode") {
    			disableItem(4);
    		}
    			//console.log("Click for opening Menu");
			$("#" + menuId).css({
		      display: "block",
		      position: "absolute",
		      left: event.pageX,
		      top: event.pageY
		    });
			
			window.setTimeout(function() {
				$(document).on('click', hide);
					 
			}, 10);
        };
        
        
        return {	//Return back the public methods
        	show : show,
        	init : init
        };
    };

    function getInstance() {
    	if( ! instance ) {
    		instance = new PrivateConstructor();
    		instance.init();
    	}
    	return instance;
    }
    
    return {
    	getInstance : getInstance
    };
    	
    
})();
    	