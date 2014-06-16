
var ClassDropdownMenu = (function() {
	
	var instance = null;
	
	
    function PrivateConstructor() {
    	var menuId = "classDropdownMenu";
    	var worksheetId, columnId;
    	var columnUri, columnLabel, columnDomain, alignmentId;
    	
    	var options = [
    		   	        //Title, function to call, needs file upload     
    		   	        [ "Add Incoming Link", addIncomingLink],
    		   	        [ "Add Outgoing Link", addOutgoingLink],
    		   	        [ "Manage Links", manageLinks],
                        [ "Augment Data", searchData], 
    		   	        [ "divider" , null ],
    		   	        [ "Export CSV" , exportCSV ],
    		   	        [ "Export JSON" , exportJSON ],
    		   			[ "Invoke Table Service", invokeMLService ],
    		   			
    		   			
    		   	];
    	
    	function init() {
    		generateJS();
    	}
    	
    	function hide() {
    		$("#" + menuId).hide();
    		$(document).off('click', hide);
    	}
    	
    	function manageLinks() {
    		console.log("showIncomingOutgoingLinks");
    		ManageIncomingOutgoingLinksDialog.getInstance().show(worksheetId, 
    				columnId, alignmentId,
    				columnLabel, columnUri, columnDomain, "InternalNode");
    	}
    	
    	function addIncomingLink() {
    		console.log("addIncomingLink");
    		IncomingOutgoingLinksDialog.getInstance().showBlank(worksheetId, 
    				columnId, alignmentId,
    				columnLabel, columnUri, columnDomain, "InternalNode",
    				"incoming");
    	};

        function searchData() {
            console.log(columnLabel);
            console.log(columnDomain);
            console.log(columnUri);
            console.log(AugmentDataDialog);
            AugmentDataDialog.getInstance(worksheetId, 
                    columnDomain, columnUri, alignmentId).show();
        }
    	
    	function addOutgoingLink() {
    		console.log("addOutgoingLink");
    		IncomingOutgoingLinksDialog.getInstance().showBlank(worksheetId, 
    				columnId, alignmentId,
    				columnLabel, columnUri, columnDomain, "InternalNode",
    				"outgoing");
    	}
    	
    	function exportCSV() {
    		ExportCSVModelDialog.getInstance().show(worksheetId,alignmentId,columnId,"exportCSV");
    	};
    	
    	function exportJSON() {
    		console.log("exportJSON");
    		var info = new Object();
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["command"] = "ExportJSONCommand";

            var newInfo = [];
            newInfo.push(getParamObject("alignmentNodeId", columnId, "other"));
            newInfo.push(getParamObject("worksheetId", worksheetId, "other"));

            info["newInfo"] = JSON.stringify(newInfo);

            showLoading(worksheetId);
            var returned = $.ajax({
                url: "RequestController",
                type: "POST",
                data : info,
                dataType : "json",
                complete :
                    function (xhr, textStatus) {
                        //alert(xhr.responseText);
                        var json = $.parseJSON(xhr.responseText);
                        parse(json);
                        hideLoading(worksheetId);
                    },
                error :
                    function (xhr, textStatus) {
                        alert("Error occured while exporting JSON!" + textStatus);
                        hideLoading(worksheetId);
                    }
            });
    	}
    	
    	function invokeMLService() {
    		ExportCSVModelDialog.getInstance().show(worksheetId,alignmentId,columnId,"invokeMLService");
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
    	
    	function show(p_worksheetId, p_columnId, p_columnLabel, p_columnUri, p_columnDomain, p_alignmentId, event) {
    		worksheetId = p_worksheetId;
    		columnLabel = p_columnLabel;
    		columnId = p_columnId;
    		columnUri = p_columnUri;
    		columnDomain = p_columnDomain;
    		alignmentId = p_alignmentId;
    		
    		
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
    	